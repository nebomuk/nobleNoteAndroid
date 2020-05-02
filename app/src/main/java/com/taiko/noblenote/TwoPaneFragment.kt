package com.taiko.noblenote

import android.os.Bundle
import android.os.Handler
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.android.material.snackbar.Snackbar
import com.jakewharton.rxbinding.view.clicks
import com.taiko.noblenote.databinding.FragmentTwopaneBinding
import com.taiko.noblenote.document.SFile
import com.taiko.noblenote.document.VolumeUtil
import rx.lang.kotlin.plusAssign
import rx.subscriptions.CompositeSubscription
import rx.subscriptions.Subscriptions

/**
 * A simple [Fragment] subclass.
 */
class TwoPaneFragment : Fragment() {


    private lateinit var binding: FragmentTwopaneBinding
    lateinit var mainViewModel: MainViewModel

    private val mCompositeSubscription = CompositeSubscription()

    private var mVolumeSubscription  = Subscriptions.empty()


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {
        binding = FragmentTwopaneBinding.inflate(inflater,container,false);

        binding.lifecycleOwner = viewLifecycleOwner;

        return binding.root;
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        mainViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(requireActivity().application))
                .get(MainViewModel::class.java);

        lifecycle.addObserver(mainViewModel);
        binding.toolbarTwoPane.toolbar.inflateMenu(R.menu.menu_main_twopane)

        binding.toolbarTwoPane.toolbar.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.action_settings -> {
                    findNavController().navigate(R.id.preferenceFragment);
                }
                R.id.action_paste -> {
                    val folderPath = Pref.currentFolderPath.value;

                    if(!FileClipboard.pasteContentIntoFolder(SFile(folderPath)))
                    {
                        Snackbar.make(view,R.string.msg_paste_error, Snackbar.LENGTH_LONG).show();
                    }
                    it.isEnabled = FileClipboard.hasContent;
                }
                R.id.action_search ->
                {
                    findNavController().navigate(R.id.findInFilesFragment);
                }
            }
            true;
        }

        val dlg = VolumeNotAccessibleDialog.create(this);

        lifecycle.addObserver(VolumeUtil);


        LegacyStorageMigration.migrateFromLegacyStorage(this);

        mVolumeSubscription = VolumeUtil.volumeAccessibleObservable(requireActivity(), Pref.rootPath)

                .subscribe {
                    if(it)
                    {
                        if(dlg.isShowing)
                        {
                            dlg.dismiss();
                        }
                        attachUi();
                    }
                    else
                    {
                        dlg.show();
                        detachUi()
                    }
                }
    }


    private fun attachUi()
    {
        log.d(".setupUi()");

        detachUi();
        // replaces existing fragments that have been retaind in saveInstanceState
        childFragmentManager.beginTransaction().replace(R.id.item_master_container, FolderListFragment()).commitAllowingStateLoss()

        val app = requireActivity().application as MainApplication

            binding.fabMenu.setClosedOnTouchOutside(true)

            mCompositeSubscription += binding.fabMenuNote.clicks().subscribe {
                Dialogs.showNewNoteDialog(binding.coordinatorLayout) {app.eventBus.createFileClick.onNext(it)}
                binding.fabMenu.close(true);
            }

            mCompositeSubscription += binding.fabMenuFolder.clicks().subscribe {
                Dialogs.showNewFolderDialog(binding.coordinatorLayout,{app.eventBus.createFolderClick.onNext(it)})
                binding.fabMenu.close(true);
            }

        val handler = Handler();

        val swipeRefreshLayout = binding.swipeRefresh

        swipeRefreshLayout.setOnRefreshListener {
            handler.postDelayed({swipeRefreshLayout.isRefreshing = false},500)
            app.eventBus.swipeRefresh.onNext(Unit)
            log.v("SwipeToRefresh");
        }

        mCompositeSubscription += Subscriptions.create { swipeRefreshLayout.setOnRefreshListener(null);  }

    }

    override fun onDestroyView() {
        super.onDestroyView()
        log.d(".onDestroyView()");
        detachUi()
        mVolumeSubscription.unsubscribe();
    }

    private fun detachUi() {
        mCompositeSubscription.clear()

        for (fragment in childFragmentManager.fragments) {
            childFragmentManager.beginTransaction().remove(fragment).commitAllowingStateLoss()
        }
    }

    /**
     * sets the visibility of the floating action button
     */
    fun setFabVisible(b : Boolean)
    {
        if(b)
        {
            binding.fabMenu.visibility = View.VISIBLE;
        }
        else
        {
            binding.fabMenu.close(false);
            binding.fabMenu.visibility = View.INVISIBLE;
        }
    }

    companion object {
        private val log = loggerFor();
    }

}
