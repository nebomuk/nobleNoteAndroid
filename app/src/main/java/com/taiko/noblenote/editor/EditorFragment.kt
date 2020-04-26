package com.taiko.noblenote.editor

import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import android.widget.TextView
import android.widget.Toast
import androidx.activity.addCallback
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.taiko.noblenote.*
import com.taiko.noblenote.databinding.ActivityEditorBinding
import com.taiko.noblenote.databinding.FragmentEditorBinding
import com.taiko.noblenote.document.VolumeUtil
import com.taiko.noblenote.extensions.getMenuItems
import com.taiko.noblenote.extensions.setTitleAndModified
import kotlinx.android.synthetic.main.activity_editor.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar_find_in_text.*
import kotlinx.android.synthetic.main.toolbar_find_in_text.view.*
import net.yanzm.actionbarprogress.MaterialIndeterminateProgressDrawable
import net.yanzm.actionbarprogress.MaterialProgressDrawable
import rx.lang.kotlin.plusAssign
import rx.subscriptions.CompositeSubscription

/**
 * A simple [Fragment] subclass.
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

        editorViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(requireActivity().application))
                .get(EditorViewModel::class.java);

        lifecycle.addObserver(editorViewModel);

        binding.viewModel = editorViewModel;


        progress_bar_file_loading.progressDrawable = MaterialProgressDrawable.create(requireContext())
        progress_bar_file_loading.indeterminateDrawable = MaterialIndeterminateProgressDrawable.create(requireContext())

        editor_edit_text.movementMethod = ArrowKeyLinkMovementMethod()

        editor_edit_text.textHTML


        // hide soft keyboard by default
        requireActivity().window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        mUndoRedo = TextViewUndoRedo(editor_edit_text);

        // uncomment to enable close button
/*        toolbar.setNavigationIcon(R.drawable.ic_close_black_24dp)
        toolbar.setNavigationOnClickListener { showExitDialog() }*/

        val extras = arguments ?: return

        editorViewModel.populateFromBundle(extras);

        editorViewModel.isModified.observe(viewLifecycleOwner, Observer { editor_edit_text.isModified = it })

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
            toolbar_find_in_text.toolbar_find_in_text_edit_text.setText(it);
        })

        Transformations.distinctUntilChanged(editorViewModel.toolbarTitle).observe(viewLifecycleOwner, Observer {
            toolbar.setTitleAndModified(it, editorViewModel.isModified.value!!) })

        Transformations.distinctUntilChanged(editorViewModel.isModified).observe(viewLifecycleOwner, Observer {
            toolbar.getMenuItems().firstOrNull { menuItem -> menuItem.itemId == R.id.action_done }?.isEnabled = it
            toolbar.setTitleAndModified(editorViewModel.toolbarTitle.value!!,it);
        })

        editorViewModel.toast.observe(viewLifecycleOwner, Observer { Toast.makeText(requireActivity(),it, Toast.LENGTH_SHORT).show(); })

        editorViewModel.finishActivity.observe(viewLifecycleOwner, Observer { findNavController().popBackStack() })

        editor_edit_text.isTextWatcherEnabled = false

        mFindInTextToolbarController = FindInTextToolbarController(this);

        toolbar.inflateMenu(R.menu.menu_editor);

        toolbar.setOnMenuItemClickListener {
            when(it.itemId)
            {
                R.id.action_redo -> mUndoRedo.redo();
                R.id.action_undo -> mUndoRedo.undo();
                R.id.action_done -> editorViewModel.onMenuItemDoneClicked();
                R.id.action_copy_to_clipboard -> editorViewModel.onCopyToClipboardClicked();
            }
            true
        }

        val itemRedo = toolbar.getMenuItems().first { it.itemId == R.id.action_redo }
        val itemUndo = toolbar.getMenuItems().first { it.itemId == R.id.action_undo }

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
        if (toolbar_find_in_text.visibility == View.VISIBLE) {
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
        editor_edit_text.customSelectionActionModeCallback = SelectionActionModeCallback(editor_edit_text)
    }

    private fun showExitDialog() {

        val builder = AlertDialog.Builder(requireActivity())
        builder.setMessage(R.string.dialogDiscardKeepEditing)
                .setPositiveButton(R.string.discard) { dialog, id ->
                    editorViewModel.onDiscardChangesClicked();
                }
                .setNegativeButton(R.string.keepEditing, null)
        // Create the AlertDialog object and return it
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
