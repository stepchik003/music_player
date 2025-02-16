package com.example.musicplayer.presentation.ui

import android.annotation.SuppressLint
import android.os.Bundle
import android.view.GestureDetector
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.SeekBar
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.RoundedCorners
import com.bumptech.glide.request.RequestOptions
import com.example.musicplayer.R
import com.example.musicplayer.databinding.FragmentPlayerBinding
import com.example.musicplayer.domain.models.PlayerState
import com.example.musicplayer.domain.models.Track
import com.example.musicplayer.presentation.viewmodels.PlayerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import kotlin.math.abs

@AndroidEntryPoint
class PlayerFragment : Fragment() {
    private lateinit var binding: FragmentPlayerBinding
    private val viewModel: PlayerViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentPlayerBinding.inflate(inflater, container, false)
        return binding.root
    }

    @SuppressLint("ClickableViewAccessibility")
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize player with tracks and current position from arguments
        val tracks = arguments?.getParcelableArrayList<Track>("tracks")
        val currentPosition = arguments?.getInt("currentPosition", 0) ?: 0

        if (tracks != null) {
            viewModel.setPlaylist(tracks, currentPosition)
        }

        // Observe player state changes and update UI
        viewLifecycleOwner.lifecycleScope.launch {
            viewModel.playerState.collect { state ->
                if (state != null) {
                    updateUI(state)
                }
            }
        }

        // Set up click listeners for playback controls
        binding.btnPlayPause.setOnClickListener { viewModel.togglePlayback() }
        binding.btnPrevious.setOnClickListener { viewModel.playPrevious() }
        binding.btnNext.setOnClickListener { viewModel.playNext() }

        // Set up seek bar listener for manual track seeking
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                if (fromUser) {
                    viewModel.seekTo(progress)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {}

            override fun onStopTrackingTouch(seekBar: SeekBar?) {}
        })

        // Set up gesture detector for swipe-down to collapse player
        val gestureDetector =
            GestureDetector(requireContext(), object : GestureDetector.SimpleOnGestureListener() {
                override fun onFling(
                    e1: MotionEvent?,
                    e2: MotionEvent,
                    velocityX: Float,
                    velocityY: Float
                ): Boolean {
                    if (e1 != null) {
                        val deltaY = e2.y - e1.y
                        if (deltaY > 100 && abs(velocityY) > 100) {
                            navigateBack()
                            return true
                        }
                    }
                    return false
                }
            })
        binding.root.setOnTouchListener { _, motionEvent ->
            gestureDetector.onTouchEvent(motionEvent)
            true
        }

        // Handle back button press to collapse player
        requireActivity().onBackPressedDispatcher.addCallback(
            viewLifecycleOwner,
            object : OnBackPressedCallback(true) {
                override fun handleOnBackPressed() {
                    navigateBack()
                }
            })

    }

    /**
     * Updates the UI with the current player state.
     *
     * @param state The current state of the player.
     */
    private fun updateUI(state: PlayerState) {

        // Update seek bar and time labels
        binding.seekBar.max = state.duration
        if (!binding.seekBar.isPressed) {
            binding.seekBar.progress = state.currentPosition
        }

        binding.currentTime.text = formatTime(state.currentPosition)
        binding.totalTime.text = formatTime(state.duration)

        // Update play/pause button icon
        val playPauseIcon =
            if (state.isPlaying) R.drawable.outline_pause_circle_24 else R.drawable.outline_play_circle_24
        binding.btnPlayPause.setImageResource(playPauseIcon)

        if (binding.trackTitle.text != state.track.title ||
            binding.trackArtist.text != state.track.artistName
        ) {
            binding.trackTitle.text = state.track.title
            binding.trackArtist.text = state.track.artistName

            // Show album title if available
            if (
                state.track.album != null
            ) {
                binding.albumTitle.visibility = View.VISIBLE
                binding.albumTitle.text = state.track.album
            } else binding.albumTitle.visibility = View.GONE

            // Load album cover image
            Glide.with(this)
                .load(state.track.albumPicBig)
                .error(R.drawable.img_1)
                .apply(RequestOptions.bitmapTransform(RoundedCorners(16)))
                .into(binding.albumCover)
        }
    }

    /**
     * Formats time in milliseconds to a string in "mm:ss" format.
     *
     * @param millis Time in milliseconds.
     * @return Formatted time string.
     */
    private fun formatTime(millis: Int): String {
        val seconds = millis / 1000
        val minutes = seconds / 60
        val remainingSeconds = seconds % 60
        return String.format("%02d:%02d", minutes, remainingSeconds)
    }

    override fun onDestroy() {
        super.onDestroy()
        viewModel.stopMusic() // Stop music when the fragment is destroyed
    }

    override fun onCreateAnimation(transit: Int, enter: Boolean, nextAnim: Int): Animation {
        return if (enter) {
            // Slide-up animation for entering
            AnimationUtils.loadAnimation(requireContext(), R.anim.slide_up)
        } else {
            // Slide-down animation for exiting
            AnimationUtils.loadAnimation(requireContext(), R.anim.slide_down)
        }
    }

    /**
     * Navigates back to the previous fragment.
     */
    private fun navigateBack() {
        findNavController().popBackStack()

    }
}