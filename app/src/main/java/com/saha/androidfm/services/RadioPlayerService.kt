package com.saha.androidfm.services

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.ServiceCompat
import androidx.core.graphics.scale
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.saha.androidfm.MainActivity
import com.saha.androidfm.R

/**
 * Foreground service for managing radio playback and displaying media notifications.
 * 
 * This service:
 * - Runs as a foreground service to keep playback alive when app is in background
 * - Displays a media-style notification with playback controls
 * - Integrates with MediaSession for lock screen and notification controls
 * - Handles play/pause/stop actions from notification buttons
 * - Manages the ExoPlayer instance for background playback
 * 
 * The service is bound by RadioPlayerViewModel and started as a foreground service
 * when playback begins.
 */
class RadioPlayerService : Service() {
    // Binder for allowing ViewModel to bind to this service
    private val binder = LocalBinder()
    
    // MediaSession for lock screen and notification controls
    private var mediaSession: MediaSessionCompat? = null
    
    // Reference to the ExoPlayer instance (managed by ViewModel)
    private var exoPlayer: ExoPlayer? = null
    
    // Current station name for display in notification
    private var stationName: String? = null
    
    // Current playback state
    private var isPlaying: Boolean = false

    companion object {
        // Notification channel ID (required for Android 8.0+)
        private const val CHANNEL_ID = "radio_player_channel"
        
        // Notification ID for this service
        private const val NOTIFICATION_ID = 1
        
        // Action strings for notification button intents
        private const val ACTION_PLAY_PAUSE = "com.saha.androidfm.PLAY_PAUSE"
        private const val ACTION_STOP = "com.saha.androidfm.STOP"
    }

    /**
     * Binder class that allows ViewModel to get a reference to this service.
     * 
     * This enables the ViewModel to call service methods like setPlayer()
     * and updateStationInfo() to keep the notification in sync with playback state.
     */
    inner class LocalBinder : Binder() {
        fun getService(): RadioPlayerService = this@RadioPlayerService
    }

    /**
     * Called when the service is first created.
     * 
     * Sets up the notification channel and media session.
     */
    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()
        initializeMediaSession()
    }

    /**
     * Creates the notification channel for Android 8.0+ (API 26+).
     * 
     * Notification channels are required for displaying notifications on modern Android.
     * This channel is set to LOW importance to minimize interruption.
     */
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Radio Player",
                NotificationManager.IMPORTANCE_LOW // Low importance - doesn't make sound or pop up
            ).apply {
                description = "Shows currently playing radio station"
                setShowBadge(false) // Don't show badge on app icon
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }

    /**
     * Initializes the MediaSession for lock screen and notification controls.
     * 
     * MediaSession allows the system to display playback controls on the lock screen
     * and in notification panels. It also enables integration with Android Auto, etc.
     */
    private fun initializeMediaSession() {
        mediaSession = MediaSessionCompat(this, "RadioPlayerService").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    exoPlayer?.play()
                }

                override fun onPause() {
                    exoPlayer?.pause()
                }

                override fun onStop() {
                    exoPlayer?.stop()
                    ServiceCompat.stopForeground(
                        this@RadioPlayerService,
                        ServiceCompat.STOP_FOREGROUND_REMOVE
                    )
                    this@RadioPlayerService.stopSelf()
                }
            })
            isActive = true
        }
    }

    /**
     * Sets the ExoPlayer instance to monitor for playback state changes.
     * 
     * When the player's state changes, the notification is updated automatically.
     * 
     * @param player The ExoPlayer instance to monitor
     */
    fun setPlayer(player: ExoPlayer) {
        exoPlayer = player
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                this@RadioPlayerService.isPlaying = isPlaying
                updateNotification() // Update notification when playback state changes
            }
        })
    }

    /**
     * Updates the station name displayed in the notification.
     * 
     * @param name The station name to display (defaults to "Radio Station" if null)
     */
    fun updateStationInfo(name: String?) {
        stationName = name ?: "Radio Station"
        updateNotification()
    }

    /**
     * Updates the foreground service notification with current playback state.
     * 
     * This method is called whenever the playback state or station info changes.
     * For Android 10+ (API 29+), it uses the media playback foreground service type.
     */
    private fun updateNotification() {
        val notification = buildNotification()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // For Android 10+ (API 29+), specify foreground service type for media playback
            // This allows the service to run in the background for media playback
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                notification,
                android.content.pm.ServiceInfo.FOREGROUND_SERVICE_TYPE_MEDIA_PLAYBACK
            )
        } else {
            // For older Android versions, use standard foreground service
            startForeground(NOTIFICATION_ID, notification)
        }
    }

    private fun buildNotification(): Notification {
        val intent = Intent(this, MainActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        // Play/Pause action
        val playPauseAction = if (isPlaying) {
            NotificationCompat.Action(
                android.R.drawable.ic_media_pause,
                "Pause",
                createPendingIntent(ACTION_PLAY_PAUSE)
            )
        } else {
            NotificationCompat.Action(
                android.R.drawable.ic_media_play,
                "Play",
                createPendingIntent(ACTION_PLAY_PAUSE)
            )
        }

        // Stop action
        val stopAction = NotificationCompat.Action(
            android.R.drawable.ic_menu_close_clear_cancel,
            "Stop",
            createPendingIntent(ACTION_STOP)
        )

        // Update media session metadata
        mediaSession?.setMetadata(
            MediaMetadataCompat.Builder()
                .putString(MediaMetadataCompat.METADATA_KEY_TITLE, stationName ?: "Radio Station")
                .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, "Live Radio")
                .build()
        )

        // Update playback state
        val playbackState = if (isPlaying) {
            PlaybackStateCompat.STATE_PLAYING
        } else {
            PlaybackStateCompat.STATE_PAUSED
        }

        mediaSession?.setPlaybackState(
            PlaybackStateCompat.Builder()
                .setState(
                    playbackState,
                    PlaybackStateCompat.PLAYBACK_POSITION_UNKNOWN,
                    1.0f
                )
                .setActions(
                    PlaybackStateCompat.ACTION_PLAY
                            or PlaybackStateCompat.ACTION_PAUSE
                            or PlaybackStateCompat.ACTION_STOP
                )
                .build()
        )

        // Load app logo as large icon
        val largeIcon = getAppLogoBitmap()

        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setContentTitle(stationName ?: "Radio Station")
            .setContentText("Live Radio")
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setLargeIcon(largeIcon)
            .setContentIntent(pendingIntent)
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1)
                    .setMediaSession(mediaSession!!.sessionToken)
            )
            .addAction(playPauseAction)
            .addAction(stopAction)
            .setOngoing(true)
            .setPriority(NotificationCompat.PRIORITY_LOW)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    private fun getAppLogoBitmap(): Bitmap? {
        return try {
            val options = BitmapFactory.Options().apply {
                inJustDecodeBounds = false
            }
            val bitmap = BitmapFactory.decodeResource(resources, R.drawable.icon_image, options)

            // Scale down if too large (notifications work best with 256x256 or smaller)
            if (bitmap != null && (bitmap.width > 256 || bitmap.height > 256)) {
                val scaledBitmap = bitmap.scale(256, 256)
                bitmap.recycle()
                scaledBitmap
            } else {
                bitmap
            }
        } catch (_: Exception) {
            // Fallback to launcher icon if station logo fails
            try {
                BitmapFactory.decodeResource(resources, R.drawable.ic_launcher_foreground)
            } catch (_: Exception) {
                null
            }
        }
    }

    private fun createPendingIntent(action: String): PendingIntent {
        val intent = Intent(this, RadioPlayerService::class.java).apply {
            this.action = action
        }
        return PendingIntent.getService(
            this,
            action.hashCode(),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> {
                if (isPlaying) {
                    exoPlayer?.pause()
                } else {
                    exoPlayer?.play()
                }
            }

            ACTION_STOP -> {
                exoPlayer?.stop()
                ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
                stopSelf()
            }
        }
        return START_STICKY
    }

    override fun onBind(intent: Intent?): IBinder = binder

    override fun onDestroy() {
        super.onDestroy()
        mediaSession?.release()
        mediaSession = null
    }
}
