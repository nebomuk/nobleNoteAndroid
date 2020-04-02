package com.taiko.noblenote.editor


import android.app.AlertDialog
import android.os.Bundle
import android.view.*
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.Observer
import androidx.lifecycle.Transformations
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.distinctUntilChanged
import com.taiko.noblenote.*
import com.taiko.noblenote.databinding.ActivityEditorBinding
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


class EditorActivity : AppCompatActivity() {


    private val log = loggerFor()


    private lateinit var editorViewModel: EditorViewModel

    private val mCompositeSubscription : CompositeSubscription = CompositeSubscription();

    private lateinit var mUndoRedo: TextViewUndoRedo

    private lateinit var  mFindInTextToolbarController: FindInTextToolbarController


    override fun onCreate(savedInstanceState: Bundle?) {
        //requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        super.onCreate(savedInstanceState)


        log.d( ".onCreate()");

        //This has to be called before setContentVie


        window.requestFeature(Window.FEATURE_ACTION_MODE_OVERLAY)

        val binding : ActivityEditorBinding = DataBindingUtil.setContentView(this, R.layout.activity_editor)

        binding.lifecycleOwner = this;


        editorViewModel = ViewModelProvider(this, ViewModelProvider.AndroidViewModelFactory(application))
                .get(EditorViewModel::class.java);

        binding.viewModel = editorViewModel;

        supportActionBar?.hide();


        progress_bar_file_loading.progressDrawable = MaterialProgressDrawable.create(this)
        progress_bar_file_loading.indeterminateDrawable = MaterialIndeterminateProgressDrawable.create(this)

        editor_edit_text.movementMethod = ArrowKeyLinkMovementMethod()

        editor_edit_text.textHTML


        // hide soft keyboard by default
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        mUndoRedo = TextViewUndoRedo(editor_edit_text);

        // uncomment to enable close button
/*        toolbar.setNavigationIcon(R.drawable.ic_close_black_24dp)
        toolbar.setNavigationOnClickListener { showExitDialog() }*/

        val extras = intent.extras ?: return

        editorViewModel.populateFromBundle(extras);

        editorViewModel.isModified.observe(this, Observer { editor_edit_text.isModified = it })

        editorViewModel.toolbarFindInTextVisible.observe(this,
                Observer { if(it)
                {
                    mFindInTextToolbarController.showToolbar();
                }
                else
                {
                    mFindInTextToolbarController.hideToolbar();
                }})

        editorViewModel.queryText.observe(this, Observer {
            toolbar_find_in_text.toolbar_find_in_text_edit_text.setText(it);
        })

        Transformations.distinctUntilChanged(editorViewModel.toolbarTitle).observe(this, Observer {
            toolbar.setTitleAndModified(it, editorViewModel.isModified.value!!) })

        Transformations.distinctUntilChanged(editorViewModel.isModified).observe(this, Observer {
            toolbar.getMenuItems().firstOrNull { menuItem -> menuItem.itemId == R.id.action_done }?.isEnabled = it
            toolbar.setTitleAndModified(editorViewModel.toolbarTitle.value!!,it);
        })

        editorViewModel.toast.observe(this, Observer { Toast.makeText(this,it,Toast.LENGTH_SHORT).show(); })

        editorViewModel.finishActivity.observe(this, Observer { finish() })


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

    }

    public override fun onStart() {
        super.onStart()
        log.d( ".onStart()");

        editorViewModel.onStart()

        // fix selection & formatting for Honeycomb and newer devices
            editor_edit_text.customSelectionActionModeCallback = SelectionActionModeCallback(editor_edit_text)

    }



    public override fun onStop() {
        mFindInTextToolbarController.hideToolbar() // required because android serializes the highlight on configuration changes and then removing does not longer work

        super.onStop()

        log.d( ".onStop()");


        editorViewModel.onStop(isChangingConfigurations)
    }



    private fun showExitDialog() {

            val builder = AlertDialog.Builder(this@EditorActivity)
            builder.setMessage(R.string.dialogDiscardKeepEditing)
                    .setPositiveButton(R.string.discard) { dialog, id ->
                        editorViewModel.onDiscardChangesClicked();
                    }
                    .setNegativeButton(R.string.keepEditing, null)
            // Create the AlertDialog object and return it
            builder.create().show()

    }

    override fun onBackPressed() {

        if(toolbar_find_in_text.visibility == View.VISIBLE)
        {
            mFindInTextToolbarController.hideToolbar();
        }
        else {
            if(editorViewModel.isModified.value!!)
            {
                showExitDialog()
            }
            else
            {
                super.onBackPressed();
            }
            
        }

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

    override fun onResume() {
        super.onResume()

        editorViewModel.onResume()

//        if(editorViewModel.isModified.value!!)
//        {
//            val handler = Handler();
//            handler.postDelayed({ toolbar.setTitleModified()},0L);
//
//        }
    }



    override fun onDestroy() {
        super.onDestroy()
        log.d( ".onDestroy()");

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

