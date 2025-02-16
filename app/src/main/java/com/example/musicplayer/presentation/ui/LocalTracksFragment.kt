package com.example.musicplayer.presentation.ui

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.example.musicplayer.R
import com.example.musicplayer.domain.models.Track
import com.example.musicplayer.presentation.viewmodels.LocalTracksViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class LocalTracksFragment : BaseTrackFragment() {
    override val viewModel: LocalTracksViewModel by viewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        checkPermissions()
    }


    override fun onTrackClicked(track: Track) {
        val bundle = Bundle().apply {
            val tracks = viewModel.tracks.value
            putParcelableArrayList("tracks", ArrayList(tracks))
            putInt("currentPosition", tracks.indexOf(track))
        }
        findNavController().navigate(R.id.action_to_player, bundle)
        Toast.makeText(this.context, track.title, Toast.LENGTH_LONG).show()
    }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
            if (isGranted) {
                viewModel.loadTracks()
            } else {
                Toast.makeText(context, "Permission denied", Toast.LENGTH_SHORT).show()
            }
        }

    private fun checkPermissions() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_AUDIO
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }


        if (ContextCompat.checkSelfPermission(
                requireContext(),
                permission
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            requestPermissionLauncher.launch(permission)
        } else {
            viewModel.loadTracks()
        }
    }

}