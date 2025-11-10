package de.blogspot.noblenoteandroid.fragments

import android.app.AlertDialog
import android.content.Context
import android.os.Bundle
import android.view.*
import android.view.inputmethod.InputMethodManager
import androidx.fragment.app.Fragment
import android.widget.Toast
import androidx.activity.addCallback
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import de.blogspot.noblenoteandroid.*
import de.blogspot.noblenoteandroid.databinding.FragmentEditorBinding
import de.blogspot.noblenoteandroid.editor.ArrowKeyLinkMovementMethod
import de.blogspot.noblenoteandroid.viewmodels.EditorViewModel
import de.blogspot.noblenoteandroid.editor.FindInTextToolbarController
import de.blogspot.noblenoteandroid.editor.TextViewUndoRedo
import de.blogspot.noblenoteandroid.extensions.getMenuItems
import de.blogspot.noblenoteandroid.extensions.setTitleAndModified
import de.blogspot.noblenoteandroid.util.loggerFor
import de.blogspot.noblenoteandroid.SelectionActionModeCallback
import rx.lang.kotlin.plusAssign
import rx.subscriptions.CompositeSubscription

/**
 * text editor fragment
 */
class EditorFragment : Fragment() {

    private lateinit var binding: FragmentEditorBinding

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?,
                              savedInstanceState: Bundle?): View? {

        //requireActivity().window.requestFeature(Window.FEATURE_ACTION_MODE_OVERLAY)

        binding = FragmentEditorBinding.inflate(inflater,container,false);
        binding.lifecycleOwner = this;

        return binding.root;
    }

    private val log = loggerFor()


    private lateinit var editorViewModel: EditorViewModel

    private val mCompositeSubscription : CompositeSubscription = CompositeSubscription();

    private lateinit var mUndoRedo: TextViewUndoRedo

    private lateinit var  mFindInTextToolbarController: FindInTextToolbarController


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        log.d( ".onViewCreated()");

        ViewCompat.setOnApplyWindowInsetsListener(binding.toolbarContainer) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, systemBars.top, 0, 0)
            insets
        }

        editorViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(requireActivity().application))
                .get(EditorViewModel::class.java);

        lifecycle.addObserver(editorViewModel);

        binding.viewModel = editorViewModel;

        binding.editorEditText.movementMethod = ArrowKeyLinkMovementMethod()

        binding.editorEditText.textHTML


        // hide soft keyboard by default
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        mUndoRedo = TextViewUndoRedo(binding.editorEditText);

        // uncomment to enable close button
/*        toolbar.setNavigationIcon(R.drawable.ic_close_black_24dp)
        toolbar.setNavigationOnClickListener { showExitDialog() }*/

        val extras = arguments ?: return

        editorViewModel.populateFromBundle(extras);

        editorViewModel.isModified.observe(viewLifecycleOwner, Observer { binding.editorEditText.isModified = it })

        editorViewModel.toolbarFindInTextVisible.observe(viewLifecycleOwner,
                Observer { if(it)
                {
                    mFindInTextToolbarController.showToolbar();
                }
                else
                {
                    mFindInTextToolbarController.hideToolbar();
                }})

        editorViewModel.queryText.observe(viewLifecycleOwner, Observer {
            binding.toolbarFindInTextInclude.searchInput.setText(it);
        })


        editorViewModel.toolbarTitle.distinctUntilChanged()
            .observe(viewLifecycleOwner) {
                binding.toolbarInclude.toolbar.setTitleAndModified(it, editorViewModel.isModified.value!!)
            }

        editorViewModel.isModified.distinctUntilChanged()
            .observe(viewLifecycleOwner) {
                binding.toolbarInclude.toolbar.getMenuItems()
                    .firstOrNull { menuItem -> menuItem.itemId == R.id.action_done }?.isEnabled = it
                binding.toolbarInclude.toolbar.setTitleAndModified(editorViewModel.toolbarTitle.value!!, it)
            }


        editorViewModel.toast.observe(viewLifecycleOwner, Observer { Toast.makeText(requireActivity(),it, Toast.LENGTH_SHORT).show(); })

        editorViewModel.finishActivity.observe(viewLifecycleOwner, Observer { 

            val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
            imm.hideSoftInputFromWindow(binding.editorEditText.windowToken,0);
            findNavController().popBackStack() })

        binding.editorEditText.isTextWatcherEnabled = false



        mFindInTextToolbarController = FindInTextToolbarController(
                binding.editorEditText,
                binding.editorScrollView,
                binding.toolbarInclude.toolbar,
                binding.toolbarFindInTextInclude);

        binding.toolbarInclude.toolbar.inflateMenu(R.menu.menu_editor);

        binding.toolbarInclude.toolbar.setOnMenuItemClickListener {
            when(it.itemId)
            {
                R.id.action_redo -> mUndoRedo.redo();
                R.id.action_undo -> mUndoRedo.undo();
                R.id.action_done -> editorViewModel.onMenuItemDoneClicked();
                R.id.action_copy_to_clipboard -> editorViewModel.onCopyToClipboardClicked();
            }
            true
        }

        val itemRedo = binding.toolbarInclude.toolbar.getMenuItems().first { it.itemId == R.id.action_redo }
        val itemUndo = binding.toolbarInclude.toolbar.getMenuItems().first { it.itemId == R.id.action_undo }

        mCompositeSubscription += mUndoRedo.canUndoChanged()
                .subscribe {
                    itemUndo.isEnabled = it
                }

        mCompositeSubscription += mUndoRedo.canRedoChanged()
                .subscribe {
                    itemRedo.isEnabled = it
                }

        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner) {
            onBackPressed()
        }
    }

    private fun onBackPressed() {
        if (binding.toolbarFindInTextInclude.toolbar.visibility == View.VISIBLE) {
            mFindInTextToolbarController.hideToolbar();
        } else {
            if (editorViewModel.isModified.value!!) {
                showExitDialog()
            } else {
                findNavController().navigateUp();
            }
        }
    }

    public override fun onStart() {
        super.onStart()
        log.d( ".onStart()");

        // fix selection & formatting for Honeycomb and newer devices
        binding.editorEditText.customSelectionActionModeCallback =
            SelectionActionModeCallback(binding.editorEditText)
    }

    private fun showExitDialog() {

        val builder = AlertDialog.Builder(requireActivity())
        builder.setMessage(R.string.dialogDiscardKeepEditing)
                .setPositiveButton(R.string.discard) { dialog, id ->
                    editorViewModel.onDiscardChangesClicked();
                }
                .setNegativeButton(R.string.keepEditing, null)
        builder.create().show()
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            if(editorViewModel.isModified.value!!)
            {
                showExitDialog()
                return true
            }
            return false
        }
        return super.onOptionsItemSelected(item)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        log.d( ".onDestroyView()");

        mCompositeSubscription.clear();
    }

    fun <T> LiveData<T>.distinctUntilChanged(): LiveData<T> {
        val mediator = MediatorLiveData<T>()
        var lastValue: T? = null
        mediator.addSource(this) { newValue ->
            if (lastValue != newValue) {
                lastValue = newValue
                mediator.value = newValue
            }
        }
        return mediator
    }


    companion object {

        @JvmStatic
        val ARG_NOTE_URI = "file_path"

        @JvmStatic
        val ARG_OPEN_MODE = "open_mode"

        @JvmStatic
        val ARG_QUERY_TEXT = "query_text"

        @JvmStatic
        val HTML = "html"

        @JvmStatic
        val READ_WRITE = "read_write"

        @JvmStatic
        val READ_ONLY = "read_only"

    }

}
