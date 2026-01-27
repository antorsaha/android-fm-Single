package com.saha.androidfm.viewmodels

import android.app.Application
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import java.lang.ref.WeakReference
import androidx.annotation.OptIn
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.ProgressiveMediaSource
import com.saha.androidfm.services.RadioPlayerService
import com.saha.androidfm.utils.helpers.AppHelper
import com.saha.androidfm.utils.helpers.M3UParser
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject
import androidx.core.net.toUri
import com.saha.androidfm.utils.helpers.AppConstants

private const val TAG = "RadioPlayerViewModel"

/**
 * ViewModel for managing radio player state and playback.
 * 
 * This ViewModel handles:
 * - ExoPlayer initialization and lifecycle management
 * - Radio stream playback (HTTP, HTTPS, M3U, M3U8, local files)
 * - Playback state management (playing, paused, buffering, error)
 * - Notification service integration for background playback
 * - Sleep timer functionality
 * - Error handling and recovery
 * 
 * The ViewModel uses Hilt for dependency injection and manages the player
 * lifecycle independently of UI components.
 */
@HiltViewModel
class RadioPlayerViewModel @Inject constructor(
    application: Application
) : AndroidViewModel(application) {

    // ExoPlayer instance for audio playback
    private var exoPlayer: ExoPlayer? = null
    
    // Weak reference to the foreground service for notifications
    private var radioPlayerServiceRef: WeakReference<RadioPlayerService>? = null
    
    // Flag indicating if the service is currently bound
    private var serviceBound = false

    /**
     * Service connection callback for binding to RadioPlayerService.
     * 
     * This connection allows the ViewModel to communicate with the foreground
     * service that displays playback notifications and manages background playback.
     */
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as RadioPlayerService.LocalBinder
            val service = binder.getService()
            radioPlayerServiceRef = WeakReference(service)
            serviceBound = true
            
            // Transfer player instance to service for notification control
            exoPlayer?.let { player ->
                service.setPlayer(player)
            }
            
            // Update service with current station info
            _stationName.value?.let { stationName ->
                service.updateStationInfo(stationName)
            }
            Log.d(TAG, "Service connected")
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            // Service disconnected - clear references
            radioPlayerServiceRef = null
            serviceBound = false
            Log.d(TAG, "Service disconnected")
        }
    }
    
    private val radioPlayerService: RadioPlayerService?
        get() = radioPlayerServiceRef?.get()

    private val _playerState = MutableStateFlow<PlayerState>(PlayerState.Idle)
    val playerState: StateFlow<PlayerState> = _playerState.asStateFlow()

    private val _isPlaying = MutableStateFlow(false)
    val isPlaying: StateFlow<Boolean> = _isPlaying.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _currentUrl = MutableStateFlow<String?>(null)
    val currentUrl: StateFlow<String?> = _currentUrl.asStateFlow()

    private val _stationName = MutableStateFlow<String?>(null)
    val stationName: StateFlow<String?> = _stationName.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _playbackError = MutableStateFlow<String?>(null)
    val playbackError: StateFlow<String?> = _playbackError.asStateFlow()

    private val _sleepTimerRemainingMillis = MutableStateFlow<Long?>(null)
    val sleepTimerRemainingMillis: StateFlow<Long?> = _sleepTimerRemainingMillis.asStateFlow()

    private var sleepTimerJob: Job? = null

    /**
     * Initialize the ViewModel by creating the ExoPlayer instance.
     */
    init {
        initializePlayer()
    }

    /**
     * Initializes the ExoPlayer instance with proper configuration for radio streaming.
     * 
     * The player is configured to:
     * - Pause automatically when audio becomes noisy (e.g., headphones unplugged)
     * - Not repeat playback (live streams don't loop)
     * - Play at full volume
     * - Handle various playback states and errors
     */
    private fun initializePlayer() {
        try {
            exoPlayer = ExoPlayer.Builder(getApplication())
                // Pause when audio becomes noisy (e.g., headphones unplugged, call starts)
                .setHandleAudioBecomingNoisy(true)
                .build()
                .apply {
                    // Configure for live streaming (no repeat, full volume)
                    repeatMode = Player.REPEAT_MODE_OFF
                    volume = 1.0f
                    
                    // Add listener to track playback state changes
                    addListener(object : Player.Listener {
                        override fun onPlaybackStateChanged(playbackState: Int) {
                            when (playbackState) {
                                Player.STATE_IDLE -> {
                                    _playerState.value = PlayerState.Idle
                                    _isLoading.value = false
                                    _isPlaying.value = false
                                }

                                Player.STATE_BUFFERING -> {
                                    _playerState.value = PlayerState.Buffering
                                    _isLoading.value = true
                                    _isPlaying.value = false
                                }

                                Player.STATE_READY -> {
                                    _playerState.value = PlayerState.Ready
                                    _isLoading.value = false
                                    _isPlaying.value = this@apply.isPlaying
                                }

                                Player.STATE_ENDED -> {
                                    _playerState.value = PlayerState.Ended
                                    _isLoading.value = false
                                    _isPlaying.value = false
                                }
                            }
                        }

                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            _isPlaying.value = isPlaying
                            if (isPlaying) {
                                _playerState.value = PlayerState.Playing
                                startNotificationService()
                            } else if (_playerState.value == PlayerState.Playing) {
                                _playerState.value = PlayerState.Paused
                            }
                            radioPlayerService?.updateStationInfo(_stationName.value)
                        }

                        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                            Log.e(TAG, "Player error: ${error.message}", error)
                            Log.e(TAG, "Error type: ${error.errorCode}", error)
                            Log.e(TAG, "Error cause: ${error.cause?.message}", error.cause)
                            error.printStackTrace()
                            
                            val errorMessage = when {
                                // Network/IO errors (error codes 2000-2999)
                                error.errorCode in 2000..2999 -> {
                                    "Network error: ${error.message ?: "Connection failed"}. Please check your internet connection."
                                }

                                // Parsing errors (error codes 4000-4999)
                                error.errorCode in 4000..4999 -> {
                                    "Unsupported audio format. The stream format may not be supported."
                                }
                                // Default error message
                                else -> {
                                    "Unable to play this station: ${error.message ?: "Unknown error (Code: ${error.errorCode})"}"
                                }
                            }
                            
                            _playbackError.value = errorMessage
                            _isLoading.value = false
                            _isPlaying.value = false
                            _playerState.value = PlayerState.Error
                        }
                    })
                }
        } catch (e: Exception) {
            Log.e(TAG, "Error initializing player", e)
            _errorMessage.value = "Failed to initialize player: ${e.message}"
        }
    }

    /**
     * Starts playback of the radio station.
     * 
     * This method:
     * - Loads the station URL from AppConstants
     * - Determines the URL type (HTTP/HTTPS, M3U, local file)
     * - Creates appropriate MediaItem or MediaSource
     * - Starts playback and updates UI state
     * - Handles errors gracefully
     * 
     * Supports multiple URL formats:
     * - HTTP/HTTPS direct stream URLs
     * - M3U/M3U8 playlist files (parsed to extract stream URL)
     * - Local file paths
     */
    @OptIn(UnstableApi::class)
    fun play() {
        val url = AppConstants.STATION_SEAM_URL
        val stationName = AppConstants.STATION_NAME

        viewModelScope.launch {
            try {
                // Clear any previous errors
                _errorMessage.value = null
                _playbackError.value = null

                Log.d(TAG, "Attempting to play URL: $url")

                // Determine URL type and create appropriate media item
                val mediaItem = if (url.startsWith("http://") || url.startsWith("https://")) {
                    // HTTP/HTTPS direct stream URL
                    Log.d(TAG, "Creating media item from HTTP/HTTPS URL")
                    createMediaItemFromUrl(url, stationName)
                } else if (url.endsWith(".m3u") || url.endsWith(".m3u8")) {
                    // M3U playlist file - parse it to extract stream URL
                    Log.d(TAG, "Parsing M3U file")
                    parseAndPlayM3U(url, stationName)
                    return@launch // Early return - parseAndPlayM3U handles playback
                } else {
                    // Try as local file path
                    Log.d(TAG, "Creating media item from local file")
                    createMediaItemFromFile(url, stationName)
                }

                mediaItem?.let {
                    Log.d(TAG, "Setting media item and starting playback")
                    exoPlayer?.let { player ->
                        try {
                            player.stop() // Stop any current playback
                            player.clearMediaItems() // Clear previous items

                            // Use MediaSource for better HTTP streaming support
                            // Get URI from MediaItem or parse from URL
                            val uri = try {
                                it.localConfiguration?.uri ?: url.toUri()
                            } catch (_: Exception) {
                                url.toUri()
                            }
                            Log.d(TAG, "Creating MediaSource for URI: $uri")
                            val mediaSource = createMediaSource(uri)

                            player.setMediaSource(mediaSource)
                            player.prepare()
                            player.play()
                            _currentUrl.value = url
                            _stationName.value = stationName
                            startNotificationService()
                            Log.d(TAG, "Playback started successfully with MediaSource")
                        } catch (e: Exception) {
                            Log.e(TAG, "Error setting media source: ${e.message}", e)
                            e.printStackTrace()
                            // Fallback to MediaItem if MediaSource fails
                            try {
                                player.setMediaItem(it)
                                player.prepare()
                                player.play()
                                _currentUrl.value = url
                                _stationName.value = stationName
                                startNotificationService()
                                Log.d(TAG, "Playback started with fallback MediaItem")
                            } catch (fallbackError: Exception) {
                                Log.e(
                                    TAG,
                                    "Fallback also failed: ${fallbackError.message}",
                                    fallbackError
                                )
                                _errorMessage.value =
                                    "Error starting playback: ${fallbackError.message}"
                            }
                        }
                    } ?: run {
                        Log.e(TAG, "ExoPlayer is null")
                        _errorMessage.value = "Player not initialized"
                    }
                } ?: run {
                    Log.e(TAG, "Failed to create media item")
                    _errorMessage.value = "Invalid URL or file path"
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error playing: ${e.message}", e)
                e.printStackTrace()
                _errorMessage.value = "Error playing: ${e.message}"
            }
        }
    }

    /**
     * Creates a MediaItem from an HTTP/HTTPS URL.
     * 
     * This method handles special characters in URLs (like semicolons) that might
     * be lost during URI parsing. It attempts to preserve the original URL structure.
     * 
     * @param url The stream URL (HTTP or HTTPS)
     * @param stationName Optional station name for metadata
     * @return MediaItem if successful, null if URL parsing fails
     */
    private fun createMediaItemFromUrl(url: String, stationName: String?): MediaItem? {
        return try {
            // Properly parse the URI to handle special characters like semicolons
            // Some radio stream URLs contain semicolons in the path that need to be preserved
            var uri = url.toUri()
            Log.d(TAG, "Original URL: $url")
            Log.d(TAG, "Parsed URI (first attempt): $uri")
            
            // If the URI doesn't preserve the semicolon, try encoding it
            // This handles URLs like "http://example.com/;?n=param" where semicolon is part of path
            if (!uri.toString().contains(";") && url.contains(";")) {
                // The semicolon might have been lost, try encoding the path
                val encodedUrl = url.replace(";", "%3B")
                uri = encodedUrl.toUri()
                Log.d(TAG, "Encoded URL: $encodedUrl")
                Log.d(TAG, "Parsed URI (after encoding): $uri")
            }
            
            Log.d(
                TAG,
                "URI scheme: ${uri.scheme}, host: ${uri.host}, port: ${uri.port}, path: ${uri.path}, query: ${uri.query}, fragment: ${uri.fragment}"
            )
            Log.d(TAG, "Full URI string: ${uri.toString()}")

            val metadata = MediaMetadata.Builder()
                .setTitle(stationName ?: "Radio Station")
                .setArtist("Live Radio")
                .build()

            val mediaItem = MediaItem.Builder()
                .setUri(uri)
                .setMediaMetadata(metadata)
                .build()

            Log.d(TAG, "Created media item successfully")
            mediaItem
        } catch (e: Exception) {
            Log.e(TAG, "Error creating media item from URL: ${e.message}", e)
            e.printStackTrace()
            _errorMessage.value = "Invalid URL format: ${e.message}"
            null
        }
    }

    /**
     * Creates a MediaSource for HTTP streaming with proper configuration.
     * 
     * This method sets up HTTP data source with:
     * - Appropriate timeouts for live streaming
     * - Headers for ICY metadata support (used by many radio streams)
     * - Cross-protocol redirect support
     * 
     * @param uri The URI of the stream to play
     * @return Configured MediaSource ready for playback
     */
    @OptIn(UnstableApi::class)
    private fun createMediaSource(uri: Uri): MediaSource {
        val context = getApplication<Application>()

        Log.d(TAG, "Creating MediaSource for URI: $uri")

        // Create HTTP data source factory with proper headers for streaming
        // Radio streams often require specific headers and longer timeouts
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("ExoPlayer/Media3")
            .setAllowCrossProtocolRedirects(true) // Allow HTTP to HTTPS redirects
            .setConnectTimeoutMs(30000) // 30 seconds - longer timeout for slow connections
            .setReadTimeoutMs(30000) // 30 seconds - longer timeout for buffering
            .setDefaultRequestProperties(
                mapOf(
                    "Connection" to "keep-alive", // Keep connection alive for continuous streaming
                    "Accept" to "*/*", // Accept any content type
                    "Icy-MetaData" to "1", // Request ICY metadata (song titles, etc.)
                    "User-Agent" to "ExoPlayer/Media3"
                )
            )

        // Create default data source factory
        val dataSourceFactory = DefaultDataSource.Factory(context, httpDataSourceFactory)

        // Create progressive media source for HTTP streaming
        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .build()

        Log.d(TAG, "MediaSource created successfully")
        return ProgressiveMediaSource.Factory(dataSourceFactory)
            .createMediaSource(mediaItem)
    }

    private fun createMediaItemFromFile(filePath: String, stationName: String?): MediaItem? {
        return try {
            val file = File(filePath)
            if (!file.exists()) {
                _errorMessage.value = "File not found: $filePath"
                return null
            }

            val uri = Uri.fromFile(file)
            val metadata = MediaMetadata.Builder()
                .setTitle(stationName ?: file.nameWithoutExtension)
                .setArtist("Local File")
                .build()

            MediaItem.Builder()
                .setUri(uri)
                .setMediaMetadata(metadata)
                .build()
        } catch (e: Exception) {
            Log.e(TAG, "Error creating media item from file", e)
            _errorMessage.value = "Error loading file: ${e.message}"
            null
        }
    }

    private suspend fun parseAndPlayM3U(filePath: String, stationName: String?) {
        withContext(Dispatchers.IO) {
            try {
                val file = File(filePath)
                if (!file.exists()) {
                    withContext(Dispatchers.Main) {
                        _errorMessage.value = "M3U file not found: $filePath"
                    }
                    return@withContext
                }

                val streams = M3UParser.parseM3UFile(file)
                if (streams.isEmpty()) {
                    withContext(Dispatchers.Main) {
                        _errorMessage.value = "No streams found in M3U file"
                    }
                    return@withContext
                }

                // Play the first stream found
                val firstStream = streams[0]
                withContext(Dispatchers.Main) {
                    val mediaItem =
                        createMediaItemFromUrl(firstStream.url, firstStream.name ?: stationName)
                    mediaItem?.let {
                        exoPlayer?.apply {
                            stop()
                            clearMediaItems()
                            setMediaItem(it)
                            prepare()
                            play()
                        }
                        _currentUrl.value = firstStream.url
                        _stationName.value = firstStream.name ?: stationName
                        startNotificationService()
                    } ?: run {
                        _errorMessage.value = "Failed to create media item from M3U stream"
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error parsing M3U file", e)
                withContext(Dispatchers.Main) {
                    _errorMessage.value = "Error parsing M3U file: ${e.message}"
                }
            }
        }
    }

    /**
     * Pauses the currently playing stream.
     */
    fun pause() {
        exoPlayer?.pause()
    }

    /**
     * Resumes playback of a paused stream.
     */
    fun resume() {
        exoPlayer?.play()
    }

    /**
     * Stops playback completely and clears current station info.
     * 
     * This method:
     * - Stops the player
     * - Clears current URL and station name
     * - Updates playing state
     * - Stops the notification service
     */
    fun stop() {
        exoPlayer?.stop()
        _currentUrl.value = null
        _stationName.value = null
        _isPlaying.value = false
        stopNotificationService()
    }

    /**
     * Toggles between play and pause states.
     * 
     * If already playing, pauses.
     * If paused and a URL is loaded, resumes.
     * If no URL is loaded, starts playing from the beginning.
     */
    fun togglePlayPause() {
        if (_isPlaying.value) {
            pause()
        } else {
            if (_currentUrl.value != null) {
                resume()
            } else {
                play()
            }
        }
    }

    fun getPlayer(): ExoPlayer? = exoPlayer

    private fun startNotificationService() {
        val context = getApplication<Application>()
        
        // Check notification permission for Android 13+
        if (!AppHelper.hasNotificationPermission(context)) {
            Log.w(TAG, "Notification permission not granted. Notifications may not work properly.")
            // Continue anyway - foreground service will still work, but notification might not show
        }
        
        val intent = Intent(context, RadioPlayerService::class.java)
        
        if (!serviceBound) {
            context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
        }
        
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(intent)
        } else {
            context.startService(intent)
        }
        
        // Update service with current info
        viewModelScope.launch {
            // Wait a bit for service to bind
            kotlinx.coroutines.delay(100)
            val service = radioPlayerService
            service?.let {
                exoPlayer?.let { player -> it.setPlayer(player) }
                _stationName.value?.let { stationName -> it.updateStationInfo(stationName) }
            }
        }
    }

    private fun stopNotificationService() {
        if (serviceBound) {
            val context = getApplication<Application>()
            context.unbindService(serviceConnection)
            serviceBound = false
        }
        val context = getApplication<Application>()
        val intent = Intent(context, RadioPlayerService::class.java)
        context.stopService(intent)
        radioPlayerServiceRef = null
    }

    /**
     * Sets a sleep timer that will stop playback after the specified number of minutes.
     * 
     * The timer counts down and automatically stops playback when it reaches zero.
     * Setting minutes to 0 or negative cancels any existing timer.
     * 
     * @param minutes Number of minutes until playback should stop (0 to cancel)
     */
    fun setSleepTimer(minutes: Int) {
        sleepTimerJob?.cancel()
        if (minutes <= 0) {
            _sleepTimerRemainingMillis.value = null
            return
        }

        val totalMillis = minutes * 60 * 1000L
        _sleepTimerRemainingMillis.value = totalMillis

        // Start countdown timer
        sleepTimerJob = viewModelScope.launch {
            var remaining = totalMillis
            while (remaining > 0) {
                delay(1000) // Update every second
                remaining -= 1000
                _sleepTimerRemainingMillis.value = remaining
            }
            // Timer finished - stop playback
            _sleepTimerRemainingMillis.value = null
            stop()
        }
    }

    override fun onCleared() {
        super.onCleared()
        sleepTimerJob?.cancel()
        stopNotificationService()
        exoPlayer?.release()
        exoPlayer = null
    }
}

/**
 * Sealed class representing the current state of the radio player.
 * 
 * These states correspond to ExoPlayer's internal states and provide
 * a clean way to represent playback status in the UI.
 */
sealed class PlayerState {
    object Idle : PlayerState()      // Player is idle, no media loaded
    object Buffering : PlayerState() // Player is buffering data
    object Ready : PlayerState()     // Player is ready to play
    object Playing : PlayerState()   // Player is currently playing
    object Paused : PlayerState()    // Player is paused
    object Ended : PlayerState()     // Playback has ended (rare for live streams)
    object Error : PlayerState()     // An error occurred during playback
}

/**
 * Data class representing a stream entry in an M3U playlist file.
 * 
 * @param url The stream URL
 * @param name Optional stream name/title
 * @param duration Optional duration in seconds (null for live streams)
 */
data class M3UStream(
    val url: String,
    val name: String? = null,
    val duration: Int? = null
)
