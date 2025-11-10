package de.blogspot.noblenoteandroid.fragments

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding.view.clicks
import de.blogspot.noblenoteandroid.*
import de.blogspot.noblenoteandroid.databinding.FragmentTwopaneBinding
import de.blogspot.noblenoteandroid.Dialogs
import de.blogspot.noblenoteandroid.FileClipboard
import de.blogspot.noblenoteandroid.filesystem.SFile
import de.blogspot.noblenoteandroid.util.loggerFor
import de.blogspot.noblenoteandroid.MainApplication
import de.blogspot.noblenoteandroid.Pref
import rx.lang.kotlin.plusAssign
import rx.subscriptions.CompositeSubscription
import rx.subscriptions.Subscriptions

/**
 * A simple [Fragment] subclass.
 */
class TwoPaneFragment : Fragment() {


    private lateinit var binding: FragmentTwopaneBinding

    private val mCompositeSubscription = CompositeSubscription()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentTwopaneBinding.inflate(inflater,container,false);

        binding.lifecycleOwner = viewLifecycleOwner;

        return binding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.toolbarTwoPane.toolbar.inflateMenu(R.menu.menu_main_twopane)

        binding.toolbarTwoPane.toolbar.setOnMenuItemClickListener(::onToolbarMenuItemClick)

        setupUi()

    }


    private fun setupUi()
    {
        log.d(".setupUi()");

        // replaces existing fragments that have been retaind in saveInstanceState
        childFragmentManager.beginTransaction().replace(R.id.item_master_container, FolderListFragment()).commitAllowingStateLoss()

        val app = requireActivity().application as MainApplication

        binding.fabMenu.setClosedOnTouchOutside(true)

        mCompositeSubscription += binding.fabMenuNote.clicks().subscribe {
            Dialogs.showNewNoteDialog(binding.coordinatorLayout, Pref.currentFolderPath.value) { app.eventBus.createFileClick.onNext(it) }
            binding.fabMenu.close(true);

            FileClipboard.clearContent();
            binding.toolbarTwoPane.toolbar.menu.findItem(R.id.action_paste)?.isVisible = false;
        }

        mCompositeSubscription += binding.fabMenuFolder.clicks().subscribe {
            Dialogs.showNewFolderDialog(binding.coordinatorLayout, { app.eventBus.createFolderClick.onNext(it) })
            binding.fabMenu.close(true);

            FileClipboard.clearContent();
            binding.toolbarTwoPane.toolbar.menu.findItem(R.id.action_paste)?.isVisible = false;
        }

        mCompositeSubscription += app.eventBus.fabMenuVisible.subscribe { binding.fabMenu.visibility = it }

        val handler = Handler();

        val swipeRefreshLayout = binding.swipeRefresh

        swipeRefreshLayout.setOnRefreshListener {
            handler.postDelayed({swipeRefreshLayout.isRefreshing = false},500)
            app.eventBus.swipeRefresh.onNext(Unit)
            log.v("SwipeToRefresh");
        }

        mCompositeSubscription += Subscriptions.create { swipeRefreshLayout.setOnRefreshListener(null);  }

    }

    private fun onToolbarMenuItemClick(it: MenuItem): Boolean {
        when (it.itemId) {
            R.id.action_settings -> {
                findNavController().navigate(R.id.preferenceFragment);
            }
            R.id.action_paste -> {
                val folderPath = Pref.currentFolderPath.value;

                if (!FileClipboard.pasteContentIntoFolder(SFile(folderPath))) {
                    Snackbar.make(binding.root, R.string.msg_paste_error, Snackbar.LENGTH_LONG).show();
                }
                it.isVisible = FileClipboard.hasContent;
            }
            R.id.action_search -> {
                findNavController().navigate(R.id.action_mainFragment_to_findInFilesFragment);
            }
        }
        return true;
    }

    override fun onDestroyView() {
        super.onDestroyView()
        log.d(".onDestroyView()");
        mCompositeSubscription.clear()

        for (fragment in childFragmentManager.fragments) {
            childFragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
        }
    }

    companion object {
        private val log = loggerFor();
    }

}
