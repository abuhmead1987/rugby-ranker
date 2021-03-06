package com.ricknout.rugbyranker.live.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.coordinatorlayout.widget.CoordinatorLayout
import androidx.core.app.ActivityCompat
import androidx.core.os.bundleOf
import androidx.core.view.isVisible
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.ricknout.rugbyranker.core.livedata.EventObserver
import com.ricknout.rugbyranker.core.util.doIfVisibleToUser
import com.ricknout.rugbyranker.core.vo.Sport
import com.ricknout.rugbyranker.live.R
import com.ricknout.rugbyranker.matches.ui.WorldRugbyMatchSpaceItemDecoration
import com.ricknout.rugbyranker.matches.ui.WorldRugbyMatchListAdapter
import dagger.android.support.DaggerFragment
import kotlinx.android.synthetic.main.fragment_live_matches.*
import javax.inject.Inject

class LiveMatchesFragment : DaggerFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory

    private lateinit var viewModel: LiveMatchesViewModel

    private val args: LiveMatchesFragmentArgs by navArgs()

    private val sport: Sport by lazy { args.sport }

    private lateinit var refreshSnackBar: Snackbar

    private lateinit var worldRugbyMatchListAdapter: WorldRugbyMatchListAdapter

    private lateinit var worldRugbyMatchSpaceItemDecoration: WorldRugbyMatchSpaceItemDecoration

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_live_matches, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        viewModel = when (sport) {
            Sport.MENS -> ViewModelProviders.of(requireActivity(), viewModelFactory)
                    .get(MensLiveMatchesViewModel::class.java)
            Sport.WOMENS -> ViewModelProviders.of(requireActivity(), viewModelFactory)
                    .get(WomensLiveMatchesViewModel::class.java)
        }
        setupRecyclerView()
        setupSnackbars()
        setupViewModel()
        setupSwipeRefreshLayout()
    }

    private fun setupRecyclerView() {
        worldRugbyMatchSpaceItemDecoration = WorldRugbyMatchSpaceItemDecoration(requireContext())
        matchesRecyclerView.addItemDecoration(worldRugbyMatchSpaceItemDecoration, 0)
        worldRugbyMatchListAdapter = WorldRugbyMatchListAdapter { worldRugbyMatch ->
            viewModel.predict(worldRugbyMatch)
        }
        matchesRecyclerView.adapter = worldRugbyMatchListAdapter
        matchesRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener() {
            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                viewModel.onScroll(delta = dy)
            }
        })
    }

    private fun setupSnackbars() {
        val coordinatorLayout = ActivityCompat.requireViewById<CoordinatorLayout>(requireActivity(), R.id.coordinatorLayout)
        refreshSnackBar = Snackbar.make(coordinatorLayout, "", Snackbar.LENGTH_SHORT)
    }

    private fun setupViewModel() {
        viewModel.liveWorldRugbyMatches.observe(viewLifecycleOwner, Observer { liveWorldRugbyMatches ->
            val isNull = liveWorldRugbyMatches == null
            progressBar.isVisible = isNull
            if (isNull) {
                return@Observer
            }
            worldRugbyMatchListAdapter.submitList(liveWorldRugbyMatches)
            val isEmpty = liveWorldRugbyMatches.isEmpty()
            emptyState.isVisible = isEmpty
        })
        viewModel.refreshingLiveWorldRugbyMatches.observe(viewLifecycleOwner, Observer { refreshingLiveWorldRugbyMatches ->
            swipeRefreshLayout.isRefreshing = refreshingLiveWorldRugbyMatches
        })
        viewModel.worldRugbyRankingsTeamIds.observe(viewLifecycleOwner, Observer { worldRugbyRankingsTeamIds ->
            worldRugbyMatchListAdapter.worldRugbyRankingsTeamIds = worldRugbyRankingsTeamIds.associateBy({ it }, { true })
        })
        viewModel.scrollToTop.observe(viewLifecycleOwner, EventObserver {
            doIfVisibleToUser { matchesRecyclerView.smoothScrollToPosition(0) }
        })
    }

    private fun setupSwipeRefreshLayout() {
        val swipeRefreshColors = resources.getIntArray(R.array.colors_swipe_refresh)
        swipeRefreshLayout.setColorSchemeColors(*swipeRefreshColors)
        swipeRefreshLayout.setProgressViewOffset(true,
                swipeRefreshLayout.progressViewStartOffset + resources.getDimensionPixelSize(R.dimen.spacing_double),
                swipeRefreshLayout.progressViewEndOffset)
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.refreshLiveWorldRugbyMatches { success ->
                if (!success) {
                    doIfVisibleToUser {
                        refreshSnackBar.setText(R.string.snackbar_failed_to_refresh_live_world_rugby_matches)
                        refreshSnackBar.show()
                    }
                }
            }
        }
    }

    companion object {
        const val TAG = "LiveMatchesFragment"
        private const val ARG_SPORT = "sport"
        fun newInstance(sport: Sport): LiveMatchesFragment {
            val matchesFragment = LiveMatchesFragment()
            matchesFragment.arguments = bundleOf(ARG_SPORT to sport)
            return matchesFragment
        }
    }
}
