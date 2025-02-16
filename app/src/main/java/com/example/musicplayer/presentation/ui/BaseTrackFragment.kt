package com.example.musicplayer.presentation.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.musicplayer.R
import com.example.musicplayer.databinding.FragmentTracksBinding
import com.example.musicplayer.domain.models.Track
import com.example.musicplayer.presentation.viewmodels.BaseTrackViewModel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

abstract class BaseTrackFragment : Fragment() {
    protected lateinit var binding: FragmentTracksBinding
    protected abstract val viewModel: BaseTrackViewModel
    private val adapter = TrackAdapter { onTrackClicked(it) }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentTracksBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupToolbar()
        setupRecyclerView()
        setupSearch()
        observeData()
        loadInitialData()
    }

    /**
     * Sets up the toolbar for the fragment.
     */
    private fun setupToolbar() {
        val toolbar = binding.toolbar
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)
    }

    protected open fun loadInitialData() {
        // Override in child fragments to load data
    }

    /**
     * Observes data changes and updates the adapter.
     */
    private fun observeData() {
        viewModel.tracks.onEach { tracks ->
            adapter.submitList(tracks)
        }.launchIn(viewLifecycleOwner.lifecycleScope)

    }

    /**
     * Sets up the search functionality.
     */
    private fun setupSearch() {
        val searchView =
            binding.toolbar.findViewById<androidx.appcompat.widget.SearchView>(R.id.searchView)
        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String): Boolean {
                viewModel.searchTracks(query)
                return true
            }

            override fun onQueryTextChange(newText: String): Boolean {
                return false
            }
        })
    }

    /**
     * Sets up the RecyclerView with the adapter and layout manager.
     */
    private fun setupRecyclerView() {
        binding.recyclerView.apply {
            adapter = this@BaseTrackFragment.adapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    /**
     * Handles track click events.
     *
     * @param track The clicked track.
     */
    abstract fun onTrackClicked(track: Track)

}