package com.example.musicplayer.presentation.service

import android.app.Notification
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.drawable.Drawable
import android.os.Binder
import android.os.Build
import android.os.Handler
import android.os.IBinder
import android.os.Looper
import android.support.v4.media.session.MediaSessionCompat
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.example.musicplayer.R
import com.example.musicplayer.domain.models.PlayerState
import com.example.musicplayer.domain.models.Track
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

@AndroidEntryPoint
class MusicService : Service() {

    private lateinit var exoPlayer: ExoPlayer
    private lateinit var mediaSession: MediaSessionCompat
    private lateinit var notificationManager: NotificationManager
    private var isNotificationCreated = false

    private var playlist: List<Track> = emptyList()
    private var currentPosition: Int = 0

    private val _playerState = MutableStateFlow<PlayerState?>(null)
    val playerState: StateFlow<PlayerState?> get() = _playerState

    private val handler = Handler(Looper.getMainLooper())
    private val updateProgressRunnable = object : Runnable {
        override fun run() {
            updatePlayerState()
            handler.postDelayed(this, 1000)
        }
    }

    override fun onCreate() {
        super.onCreate()
        initializePlayer()
        initializeMediaSession()
        initializeNotificationManager()
        startProgressUpdates()
    }

    /**
     * Starts periodic updates for player state.
     */
    private fun startProgressUpdates() {
        handler.post(updateProgressRunnable)
    }

    /**
     * Stops periodic updates for player state.
     */
    private fun stopProgressUpdates() {
        handler.removeCallbacks(updateProgressRunnable)
    }

    /**
     * Initializes the ExoPlayer instance.
     */
    private fun initializePlayer() {
        exoPlayer = ExoPlayer.Builder(this).build()
        exoPlayer.addListener(object : Player.Listener {
            override fun onPlaybackStateChanged(state: Int) {
                when (state) {
                    Player.STATE_ENDED -> playNext()
                    Player.STATE_READY -> updatePlayerState()
                    Player.STATE_BUFFERING -> {}
                    Player.STATE_IDLE -> {}
                }
            }

            override fun onIsPlayingChanged(isPlaying: Boolean) {
                updatePlayerState()
            }
        })
    }

    /**
     * Initializes the MediaSession for media controls.
     */
    private fun initializeMediaSession() {
        mediaSession = MediaSessionCompat(this, "MusicService").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() {
                    exoPlayer.play()
                    updatePlayerState()
                }

                override fun onPause() {
                    exoPlayer.pause()
                    updatePlayerState()
                }

                override fun onSkipToNext() {
                    playNext()

                }

                override fun onSkipToPrevious() {
                    playPrevious()
                }

                override fun onSeekTo(pos: Long) {
                    exoPlayer.seekTo(pos)
                    updatePlayerState()
                }
            })
            isActive = true
        }
    }

    /**
     * Initializes the NotificationManager for media notifications.
     */
    private fun initializeNotificationManager() {
        notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            "PLAY" -> exoPlayer.play()
            "PAUSE" -> exoPlayer.pause()
            "NEXT" -> playNext()
            "PREVIOUS" -> playPrevious()
            "SEEK" -> {
                val position = intent.getIntExtra("position", 0)
                exoPlayer.seekTo(position.toLong())
            }

            "STOP" -> stopSelf()
            else -> {
                val tracks: ArrayList<Track>? =
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) intent?.getParcelableArrayListExtra(
                        "tracks",
                        Track::class.java
                    )
                    else intent?.getParcelableArrayListExtra("tracks")
                val position = intent?.getIntExtra("currentPosition", 0) ?: 0

                if (tracks != null) {
                    playlist = tracks
                    currentPosition = position
                    playTrack(playlist[currentPosition])
                }
            }
        }

        return START_STICKY
    }

    /**
     * Plays the specified track.
     *
     * @param track The track to play.
     */
    private fun playTrack(track: Track) {
        exoPlayer.setMediaItem(MediaItem.fromUri(track.preview))
        exoPlayer.prepare()
        exoPlayer.play()
        updatePlayerState()
    }

    /**
     * Plays the next track in the playlist.
     */
    fun playNext() {
        if (currentPosition < playlist.size - 1) {
            currentPosition++
            playTrack(playlist[currentPosition])
        }
    }

    /**
     * Plays the previous track in the playlist.
     */
    fun playPrevious() {
        if (currentPosition > 0) {
            currentPosition--
            playTrack(playlist[currentPosition])
        }
    }

    /**
     * Toggles playback between play and pause.
     */
    fun togglePlayback() {
        if (exoPlayer.isPlaying) {
            exoPlayer.pause()
        } else {
            exoPlayer.play()
        }
        updatePlayerState()
    }

    /**
     * Seeks to the specified position in the current track.
     *
     * @param position The position to seek to.
     */
    fun seekTo(position: Int) {
        exoPlayer.seekTo(position.toLong())
        updatePlayerState()
    }

    /**
     * Updates the player state and notification.
     */
    private fun updatePlayerState() {
        _playerState.value = PlayerState(
            track = playlist[currentPosition],
            isPlaying = exoPlayer.isPlaying,
            currentPosition = exoPlayer.currentPosition.toInt(),
            duration = exoPlayer.duration.toInt()
        )
        updateNotification()
    }

    /**
     * Updates the media notification.
     */
    private fun updateNotification() {
        val notification = createNotification()
        if (!isNotificationCreated) {
            startForeground(NOTIFICATION_ID, notification)
            isNotificationCreated = true
        } else {
            notificationManager.notify(NOTIFICATION_ID, notification)
        }
    }

    /**
     * Creates a media notification with playback controls.
     *
     * @return The created notification.
     */
    private fun createNotification(): Notification {
        val playIntent = Intent(this, MusicService::class.java).apply {
            action = "PLAY"
        }
        val playPendingIntent = PendingIntent.getService(
            this,
            0,
            playIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val pauseIntent = Intent(this, MusicService::class.java).apply {
            action = "PAUSE"
        }
        val pausePendingIntent = PendingIntent.getService(
            this,
            0,
            pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val playPauseAction = if (exoPlayer.isPlaying) {
            NotificationCompat.Action(
                R.drawable.pause_circle,
                "Pause",
                pausePendingIntent
            )
        } else {
            NotificationCompat.Action(
                R.drawable.play_circle,
                "Play",
                playPendingIntent
            )
        }

        val nextIntent = Intent(this, MusicService::class.java).apply {
            action = "NEXT"
        }
        val nextPendingIntent = PendingIntent.getService(
            this,
            0,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val previousIntent = Intent(this, MusicService::class.java).apply {
            action = "PREVIOUS"
        }
        val previousPendingIntent = PendingIntent.getService(
            this,
            1,
            previousIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        var albumPic: Bitmap? = null

        Glide.with(this)
            .asBitmap()
            .load(playlist[currentPosition].albumPicBig)
            .into(object : CustomTarget<Bitmap>() {
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: Transition<in Bitmap>?
                ) {
                    albumPic = resource
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    albumPic = BitmapFactory.decodeResource(resources, R.drawable.img_1)
                }
            })
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.img)
            .setContentTitle(playlist[currentPosition].title)
            .setContentText(playlist[currentPosition].artistName)
            .setLargeIcon(albumPic)
            .addAction(
                NotificationCompat.Action(
                    R.drawable.skip_previous, "Previous",
                    previousPendingIntent
                )
            )
            .addAction(playPauseAction)
            .addAction(
                NotificationCompat.Action(
                    R.drawable.skip_next, "Next",
                    nextPendingIntent
                )
            )
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .build()
    }

    override fun onDestroy() {
        super.onDestroy()
        exoPlayer.release()
        mediaSession.release()
        stopProgressUpdates()
    }


    inner class LocalBinder : Binder() {
        fun getService(): MusicService = this@MusicService
    }

    private val binder = LocalBinder()

    override fun onBind(intent: Intent?): IBinder {
        return binder
    }

    companion object {
        const val NOTIFICATION_ID = 1
        const val CHANNEL_ID = "music_channel"
    }
}
