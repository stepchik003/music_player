package com.example.musicplayer.presentation.viewmodels

import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.ServiceConnection
import android.os.IBinder
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.musicplayer.domain.models.PlayerState
import com.example.musicplayer.domain.models.Track
import com.example.musicplayer.presentation.service.MusicService
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import java.lang.ref.WeakReference
import javax.inject.Inject

@HiltViewModel
class PlayerViewModel @Inject constructor(
    @ApplicationContext private val context: Context
) : ViewModel() {

    private val _playerState = MutableStateFlow<PlayerState?>(null)
    val playerState: StateFlow<PlayerState?> get() = _playerState

    private var musicService: WeakReference<MusicService>? = null
    private val serviceConnection = object : ServiceConnection {
        override fun onServiceConnected(name: ComponentName?, service: IBinder?) {
            val binder = service as MusicService.LocalBinder
            musicService = WeakReference(binder.getService())
            musicService?.get()?.playerState?.onEach { state ->
                _playerState.value = state
            }?.launchIn(viewModelScope)
        }

        override fun onServiceDisconnected(name: ComponentName?) {
            musicService = null
        }
    }

    fun setPlaylist(playlist: List<Track>, startPosition: Int) {
        val intent = Intent(context, MusicService::class.java).apply {
            putParcelableArrayListExtra("tracks", ArrayList(playlist))
            putExtra("currentPosition", startPosition)
        }
        context.startService(intent)
        context.bindService(intent, serviceConnection, Context.BIND_AUTO_CREATE)
    }

    fun togglePlayback() {
        musicService?.get()?.togglePlayback()
    }

    fun playNext() {
        musicService?.get()?.playNext()
    }

    fun playPrevious() {
        musicService?.get()?.playPrevious()
    }

    fun seekTo(position: Int) {
        musicService?.get()?.seekTo(position)
    }

    fun stopMusic() {
        musicService?.get()?.stopSelf()
    }

    override fun onCleared() {
        super.onCleared()
        context.unbindService(serviceConnection)
    }
}