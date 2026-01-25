@file:kotlin.OptIn(ExperimentalMaterial3Api::class)
@file:Suppress("COMPOSE_APPLIER_CALL_MISMATCH")

package com.saha.androidfm.views.screens.liveSteam

import android.app.Activity
import android.net.Uri
import android.util.Log
import android.view.WindowManager
import androidx.annotation.OptIn
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.VolumeOff
import androidx.compose.material.icons.automirrored.filled.VolumeUp
import androidx.compose.material.icons.filled.Error
import androidx.compose.material.icons.filled.Fullscreen
import androidx.compose.material.icons.filled.FullscreenExit
import androidx.compose.material.icons.filled.Pause
import androidx.compose.material.icons.filled.PlayArrow
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.core.net.toUri
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.media3.common.MediaItem
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.common.util.UnstableApi
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.ui.AspectRatioFrameLayout
import androidx.media3.ui.PlayerView
import com.saha.androidfm.R
import com.saha.androidfm.ui.theme.accent
import com.saha.androidfm.ui.theme.backgroundColor
import com.saha.androidfm.ui.theme.primaryTextColor
import com.saha.androidfm.ui.theme.secondaryTextColor
import com.saha.androidfm.utils.helpers.AdMobInterstitialManager
import com.saha.androidfm.utils.helpers.AppConstants
import com.saha.androidfm.utils.helpers.M3UParser
import com.saha.androidfm.viewmodels.RadioPlayerViewModel
import com.saha.androidfm.views.components.HeightGap
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@ExperimentalMaterial3Api
@OptIn(
    UnstableApi::class, ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class,
    ExperimentalMaterial3Api::class, ExperimentalMaterial3Api::class
)
@Composable
fun LiveSteamScreen(
    radioPlayerViewModel: RadioPlayerViewModel = hiltViewModel()
) {
    val context = LocalContext.current
    val activity = context as? Activity

    // State for video player
    var exoPlayer: ExoPlayer? by remember { mutableStateOf(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var isFullscreen by remember { mutableStateOf(false) }
    var errorMessage by remember { mutableStateOf<String?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var retryKey by remember { mutableIntStateOf(0) }
    var isMuted by remember { mutableStateOf(false) }

    // Observe radio player state
    val isRadioPlaying by radioPlayerViewModel.isPlaying.collectAsState()
    
    // Initialize AdMob Interstitial Ad Manager
    val adManager = remember { AdMobInterstitialManager(context) }
    
    // Load ad when screen is created
    LaunchedEffect(Unit) {
        adManager.loadAd()
    }

    // Helper function to create MediaSource with proper configuration
    fun createMediaSource(ctx: android.content.Context, uri: Uri): MediaSource {
        val httpDataSourceFactory = DefaultHttpDataSource.Factory()
            .setUserAgent("ExoPlayer/Media3")
            .setAllowCrossProtocolRedirects(true)
            .setConnectTimeoutMs(30000)
            .setReadTimeoutMs(30000)

        val dataSourceFactory = DefaultDataSource.Factory(ctx, httpDataSourceFactory)

        val isHls = uri.toString().contains(".m3u8")
        val mediaItem = MediaItem.Builder()
            .setUri(uri)
            .apply {
                if (isHls) setMimeType(MimeTypes.APPLICATION_M3U8)
            }
            .build()

        // Use DefaultMediaSourceFactory which automatically handles HLS, DASH, and Progressive
        return DefaultMediaSourceFactory(dataSourceFactory)
            .createMediaSource(mediaItem)
    }

    // Initialize ExoPlayer with proper MediaSource for video streaming
    // Initialize ExoPlayer
    LaunchedEffect(retryKey) {
        try {
            // Stop radio if it's playing when video starts loading
            if (isRadioPlaying) {
                radioPlayerViewModel.pause()
                Log.d("LiveStreamScreen", "Radio paused - video stream starting")
            }

            val streamUrl = AppConstants.LIVE_STREAM_VIDEO_URL
            Log.d("LiveStreamScreen", "Loading stream URL: $streamUrl")

            var actualStreamUrl = streamUrl

            // ONLY parse .m3u files manually.
            // DO NOT parse .m3u8 files manually as ExoPlayer handles them internally (HLS).
            // Manual parsing of .m3u8 often extracts relative segment paths (.ts) which fails.
            if (streamUrl.endsWith(".m3u") && !streamUrl.contains(".m3u8")) {
                Log.d("LiveStreamScreen", "M3U playlist detected - parsing manually...")
                try {
                    val m3uContent = withContext(Dispatchers.IO) {
                        java.net.URL(streamUrl).openStream().bufferedReader().use { it.readText() }
                    }
                    val streams = M3UParser.parseM3UContent(m3uContent)
                    if (streams.isNotEmpty()) {
                        actualStreamUrl = streams[0].url
                    }
                } catch (e: Exception) {
                    Log.e("LiveStreamScreen", "Error parsing M3U", e)
                }
            }

            val player = ExoPlayer.Builder(context)
                .build()
                .apply {
                    val uri = actualStreamUrl.toUri()
                    val mediaSource = createMediaSource(context, uri)

                    setMediaSource(mediaSource)
                    prepare()
                    playWhenReady = true

                    addListener(object : Player.Listener {
                        override fun onIsPlayingChanged(playing: Boolean) {
                            isPlaying = playing
                            // Stop radio when video starts playing
                            if (playing && isRadioPlaying) {
                                radioPlayerViewModel.pause()
                                Log.d("LiveStreamScreen", "Radio paused - video is now playing")
                            }
                        }

                        override fun onPlaybackStateChanged(playbackState: Int) {
                            when (playbackState) {
                                Player.STATE_BUFFERING -> {
                                    isLoading = true
                                    // Stop radio when video starts buffering
                                    if (isRadioPlaying) {
                                        radioPlayerViewModel.pause()
                                        Log.d(
                                            "LiveStreamScreen",
                                            "Radio paused - video is buffering"
                                        )
                                    }
                                }

                                Player.STATE_READY -> {
                                    isLoading = false
                                    errorMessage = null
                                    // Stop radio when video is ready to play
                                    if (isRadioPlaying) {
                                        radioPlayerViewModel.pause()
                                        Log.d("LiveStreamScreen", "Radio paused - video is ready")
                                    }
                                }

                                Player.STATE_IDLE, Player.STATE_ENDED -> isLoading = false
                            }
                        }

                        override fun onPlayerError(error: androidx.media3.common.PlaybackException) {
                            isLoading = false
                            errorMessage = "Stream error: ${error.message}"
                            Log.e("LiveStreamScreen", "Player error: ${error.errorCodeName}", error)
                        }
                    })
                }
            exoPlayer = player
        } catch (e: Exception) {
            isLoading = false
            errorMessage = "Failed to initialize player: ${e.message}"
        }
    }

    // Cleanup on dispose
    DisposableEffect(Unit) {
        onDispose {
            exoPlayer?.release()
        }
    }

    // Keep screen on while viewing live stream
    DisposableEffect(Unit) {
        activity?.window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        onDispose {
            activity?.window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(backgroundColor)
    ) {
        Column(
            modifier = Modifier.fillMaxSize()
        ) {
            HeightGap(16.dp)

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center
            ) {
                Text(
                    text = stringResource(R.string.live_stream),
                    style = MaterialTheme.typography.titleMedium,
                    color = secondaryTextColor,
                )
            }
            HeightGap(24.dp)


            // Video Player
            Box(
                modifier = if (isFullscreen) Modifier.fillMaxSize()
                else Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
            ) {
                Card(
                    modifier = Modifier.fillMaxSize(),
                    colors = CardDefaults.cardColors(containerColor = Color.Black),
                    shape = RectangleShape
                ) {
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (exoPlayer != null) {
                            AndroidView(
                                factory = { ctx ->
                                    PlayerView(ctx).apply {
                                        this.player = exoPlayer
                                        useController = true
                                        resizeMode = AspectRatioFrameLayout.RESIZE_MODE_FIT
                                        setShowBuffering(PlayerView.SHOW_BUFFERING_WHEN_PLAYING)
                                        // Hide previous and next buttons
                                        setShowPreviousButton(false)
                                        setShowNextButton(false)
                                    }
                                },
                                modifier = Modifier.fillMaxSize(),
                                update = { view ->
                                    view.player = exoPlayer
                                    // Ensure buttons stay hidden on update
                                    view.setShowPreviousButton(false)
                                    view.setShowNextButton(false)
                                }
                            )
                        }

                        if (isLoading) {
                            CircularProgressIndicator(
                                modifier = Modifier.align(Alignment.Center),
                                color = accent
                            )
                        }

                        if (errorMessage != null) {
                            Column(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .padding(16.dp),
                                horizontalAlignment = Alignment.CenterHorizontally,
                                verticalArrangement = Arrangement.Center
                            ) {
                                Icon(
                                    Icons.Default.Error,
                                    null,
                                    tint = Color.Red,
                                    modifier = Modifier.size(48.dp)
                                )
                                Spacer(modifier = Modifier.height(8.dp))
                                Text(
                                    errorMessage!!,
                                    color = Color.White,
                                    textAlign = TextAlign.Center
                                )
                                Button(
                                    onClick = { retryKey++ },
                                    modifier = Modifier.padding(top = 8.dp)
                                ) {
                                    Text("Retry")
                                }
                            }
                        }
                    }
                }

                // Fullscreen button
                IconButton(
                    onClick = { isFullscreen = !isFullscreen },
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(8.dp)
                        .background(Color.Black.copy(0.4f), CircleShape)
                ) {
                    Icon(
                        imageVector = if (isFullscreen) Icons.Default.FullscreenExit else Icons.Default.Fullscreen,
                        contentDescription = null,
                        tint = Color.White
                    )
                }
            }

            if (!isFullscreen) {
                Spacer(modifier = Modifier.height(24.dp))

                // LIVE Badge and Title
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    // LIVE Badge - show status
                    Surface(
                        modifier = Modifier.padding(bottom = 16.dp),
                        shape = RoundedCornerShape(20.dp),
                        color =  accent
                    ) {
                        Row(
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(8.dp)
                        ) {
                            Box(
                                modifier = Modifier
                                    .size(8.dp)
                                    .background(
                                        Color.Red,
                                        CircleShape
                                    )
                            )
                            Text(
                                text = "LIVE",
                                color = Color.White,
                                fontWeight = FontWeight.Bold,
                                style = MaterialTheme.typography.labelMedium
                            )
                        }
                    }

                    // Title
                    Text(
                        text = stringResource(R.string.abc_fm_live_stream),
                        color = primaryTextColor,
                        fontWeight = FontWeight.Bold,
                        style = MaterialTheme.typography.headlineMedium,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    // Subtitle
                    Text(
                        text = stringResource(R.string.watch_live_broadcasts_and_special_events),
                        color = secondaryTextColor,
                        style = MaterialTheme.typography.bodyMedium
                    )
                }

                Spacer(modifier = Modifier.weight(1f))

                // Bottom Controls
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 32.dp, vertical = 24.dp),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    // Refresh/Loop Button
                    IconButton(
                        onClick = {
                            exoPlayer?.seekTo(0)
                            exoPlayer?.play()
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = Icons.Default.Refresh,
                            contentDescription = "Refresh",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }

                    // Play/Pause Button
                    Box(
                        modifier = Modifier
                            .size(72.dp)
                            .background(accent, CircleShape)
                            .clickable {
                                if (isPlaying) {
                                    // If already playing, just pause (no ad needed)
                                    exoPlayer?.pause()
                                } else {
                                    // If not playing, show ad first, then play after ad closes
                                    activity?.let {
                                        adManager.showAd(it) {
                                            // This callback executes after ad is closed
                                            // Now play the video
                                            exoPlayer?.play()
                                        }
                                    } ?: run {
                                        // If no activity context, play directly
                                        exoPlayer?.play()
                                    }
                                }
                            },
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = if (isPlaying) Icons.Default.Pause else Icons.Default.PlayArrow,
                            contentDescription = if (isPlaying) "Pause" else "Play",
                            tint = Color.White,
                            modifier = Modifier.size(36.dp)
                        )
                    }

                    // Volume Button
                    IconButton(
                        onClick = {
                            isMuted = !isMuted
                            exoPlayer?.volume = if (isMuted) 0f else 1f
                        },
                        modifier = Modifier.size(48.dp)
                    ) {
                        Icon(
                            imageVector = if (isMuted) Icons.AutoMirrored.Filled.VolumeOff else Icons.AutoMirrored.Filled.VolumeUp,
                            contentDescription = if (isMuted) "Unmute" else "Mute",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }
        }
    }
}
