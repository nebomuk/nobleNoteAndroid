package com.taiko.noblenote


import android.annotation.SuppressLint
import android.app.Activity
import android.app.AlertDialog
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.view.*
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.widget.Toolbar
import com.google.android.material.snackbar.Snackbar
import com.taiko.noblenote.document.SFile
import com.taiko.noblenote.editor.ArrowKeyLinkMovementMethod
import com.taiko.noblenote.editor.TextViewUndoRedo
import com.taiko.noblenote.extensions.markTitleAsModified
import kotlinx.android.synthetic.main.activity_editor.*
import kotlinx.android.synthetic.main.toolbar.*
import kotlinx.android.synthetic.main.toolbar_find_in_text.*
import kotlinx.android.synthetic.main.toolbar_find_in_text.view.*
import net.yanzm.actionbarprogress.MaterialIndeterminateProgressDrawable
import net.yanzm.actionbarprogress.MaterialProgressDrawable
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.plusAssign
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription
import java.lang.UnsupportedOperationException


class EditorActivity : Activity() {


    private val log = loggerFor()

    private var mFocusable = true // if set to false, the note is opened in read only mode
    private lateinit var mFileUri: Uri;
    private lateinit var mOpenMode: String;
    private var lastModified: Long = 0 // restored in OnRestoreInstanceState
    private var mFormattingMenuItem: MenuItem? = null
    private val mCompositeSubscription : CompositeSubscription = CompositeSubscription();

    private lateinit var mUndoRedo: TextViewUndoRedo

    private lateinit var  mFindInTextToolbarController: FindInTextToolbarController


    override fun onCreate(savedInstanceState: Bundle?) {
        //requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        super.onCreate(savedInstanceState)


        log.d( ".onCreate()");

        //This has to be called before setContentVie

        window.requestFeature(Window.FEATURE_ACTION_MODE_OVERLAY)
        setContentView(R.layout.activity_editor)


        progress_bar_file_loading.progressDrawable = MaterialProgressDrawable.create(this)
        progress_bar_file_loading.indeterminateDrawable = MaterialIndeterminateProgressDrawable.create(this)

        // hide soft keyboard by default
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        mUndoRedo = TextViewUndoRedo(editor_edit_text);

        // uncomment to enable close button
/*        toolbar.setNavigationIcon(R.drawable.ic_close_black_24dp)
        toolbar.setNavigationOnClickListener { showExitDialog() }*/

        val extras = intent.extras ?: return
        mFileUri = Uri.parse(extras.getString(ARG_NOTE_URI))
        mOpenMode = extras.getString(ARG_OPEN_MODE)!!
        mFocusable = !(mOpenMode == HTML || mOpenMode == READ_ONLY) // no editing if html source should be shown


        editor_edit_text.isTextWatcherEnabled = false
        editor_edit_text.isFocusable = mFocusable // read only if not mFocusable


        mFindInTextToolbarController = FindInTextToolbarController(this);

        if(savedInstanceState != null)
        {
            // FIXME disable auto save when configuration changed
            lastModified = savedInstanceState.getLong(FIELD_LAST_MODIFIED,0L);
           editor_edit_text.isModified = savedInstanceState.getBoolean(FIELD_EDIT_TEXT_MODIFIED,false);
            toolbar_find_in_text.visibility = savedInstanceState.getInt(FIELD_FIND_IN_TEXT_TOOLBAR_VISIBILITY,View.GONE);

            progress_bar_file_loading.visibility = View.GONE
            editor_scroll_view.visibility = View.VISIBLE
        }
        else
        {
            editor_scroll_view.visibility = View.INVISIBLE
        }

        populateMenu(toolbar.menu)


    }

    public override fun onStart() {
        super.onStart()
        log.d( ".onStart()");

        if (SFile(mFileUri).lastModified() > lastModified) {
            reload()
        }
        // fix selection & formatting for Honeycomb and newer devices
            editor_edit_text.customSelectionActionModeCallback = SelectionActionModeCallback(editor_edit_text)

    }

    /**
     * reloads the current note file
     */
    private fun reload() {
        // load file contents and parse html thread
        log.d( ".reload()");

            mCompositeSubscription += FileHelper.readFile(mFileUri, this, parseHtml = mOpenMode != HTML) // don't parse html if it should display the html source of the note
                    .subscribeOn(Schedulers.io())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe({
                        editor_edit_text.setText(it)
                        editor_edit_text.movementMethod = ArrowKeyLinkMovementMethod()
                        progress_bar_file_loading.visibility = View.GONE
                        editor_scroll_view.visibility = View.VISIBLE
                        editor_edit_text.isModified = false // reset modification state because modification flag has been set by editor_edit_text.setText

                        val queryText = intent?.extras?.getString(ARG_QUERY_TEXT)
                        if (!queryText.isNullOrBlank()) {
                            toolbar_find_in_text.toolbar_find_in_text_edit_text.setText(queryText);
                            mFindInTextToolbarController.showToolbar()
                        }

                        if(lastModified != 0L) // 0 is initial value when loading the first time, so we do not show the "reloaded" snackbar
                        {
                            Snackbar.make(layout_root, R.string.noteReloaded, Snackbar.LENGTH_SHORT).show()
                        }

                        lastModified = SFile(mFileUri).lastModified()

                    }, {
                        log.e(it.message)
                        Toast.makeText(this, R.string.msg_file_loading_error, Toast.LENGTH_SHORT).show()
                        finish();

                    });
    }

    public override fun onStop() {
        mFindInTextToolbarController.clearFindText() // required because android serializes the highlight on configuration changes and then removing does not longer work

        super.onStop()

        log.d( ".onStop()");


        // does nothing if open mode is set to read only

        // if not mFocusable, changes can not be made
        if (Pref.isAutoSaveEnabled &&  !isChangingConfigurations &&  mFocusable && editor_edit_text.isModified)
        // then save the note
        {

                FileHelper.writeFile(filePath = mFileUri, text = editor_edit_text.textHTML)
                        .subscribeOn(Schedulers.io())
                        .subscribe {

                            lastModified = it
                            editor_edit_text.isModified = false
                            runOnUiThread { Toast.makeText(this.applicationContext, R.string.noteSaved, Toast.LENGTH_SHORT).show() }

                        }
        }
    }

    private fun showExitDialog() {
        if (editor_edit_text.isModified) {
            val builder = AlertDialog.Builder(this@EditorActivity)
            builder.setMessage(R.string.dialogDiscardKeepEditing)
                    .setPositiveButton(R.string.discard) { dialog, id ->
                        editor_edit_text.isModified = false//  does not get saved
                        val handler = Handler()
                        handler.post { finish() }
                    }
                    .setNegativeButton(R.string.keepEditing, null)
            // Create the AlertDialog object and return it
            builder.create().show()
        } else
        // no modification, do not ask to save the document
        {
            editor_edit_text.isModified = false//  does not get saved
            finish();
        }
    }

    override fun onBackPressed() {


        if (mFormattingMenuItem != null) {
            if(mFormattingMenuItem!!.isActionViewExpanded)
            {
                super.onBackPressed()
            }

        }
        else if(toolbar_find_in_text.visibility == View.VISIBLE)
        {
            mFindInTextToolbarController.hideToolbar();
        }
        else {
            showExitDialog()
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            showExitDialog()
            return true
        }


        return super.onOptionsItemSelected(item)
    }


    private fun populateMenu(menu: Menu)  {


        MenuHelper.addCopyToClipboard(this,menu,{editor_edit_text.text})


        val undoItem = menu.add(R.string.action_undo)
                .setIcon(R.drawable.ic_undo_black_24dp)
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                .setAlphabeticShortcut('Z')
                .setOnMenuItemClickListener {
            mUndoRedo.undo();
            true;
        }

        // TODO callback should be invoked when no text has changed
        mCompositeSubscription += mUndoRedo.canUndoChanged().subscribe {

            val draw = resources.getDrawable(R.drawable.ic_undo_black_24dp,theme)
            undoItem.isEnabled = it;
         }


        val redoItem = menu.add(R.string.action_redo)
                .setIcon(R.drawable.ic_redo_black_24dp)
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)
                .setAlphabeticShortcut('Y')
                .setOnMenuItemClickListener {
                    mUndoRedo.redo();
                    true;
                }
        mCompositeSubscription += mUndoRedo.canRedoChanged().subscribe {

            val draw = resources.getDrawable(R.drawable.ic_redo_black_24dp,theme)
            redoItem.isEnabled = it;
        }

        val itemDone = menu.add(R.string.action_done)

                .setIcon(R.drawable.ic_done_black_24dp)
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)

        itemDone.setOnMenuItemClickListener {
            finish()
            true
        }

        itemDone.isVisible = editor_edit_text.isModified;

        mCompositeSubscription += mUndoRedo.canUndoChanged()
                .filter {it }
                .take(1)
                .subscribe {
                    toolbar.markTitleAsModified()
                    itemDone.isVisible = true;
                }

        //mFormattingMenuItem = MenuHelper.addTextFormatting(this,menu,editor_edit_text);

        //item.expandActionView(); // show text formatting toolbar by default

    }

    override fun onResume() {
        super.onResume()

        // isModified is only available after onRestoreInstanceState, which is (maybe) called before onResume
        toolbar.title = SFile(mFileUri).nameWithoutExtension;
        if(editor_edit_text.isModified)
        {
            toolbar.markTitleAsModified()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {

        super.onSaveInstanceState(outState)

        outState.putLong(FIELD_LAST_MODIFIED,lastModified);
        outState.putBoolean(FIELD_EDIT_TEXT_MODIFIED,editor_edit_text.isModified);
        outState.putInt(FIELD_FIND_IN_TEXT_TOOLBAR_VISIBILITY,toolbar_find_in_text.visibility)
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

        @JvmStatic
        val FIELD_LAST_MODIFIED = "last_modified";

        @JvmStatic
        val FIELD_EDIT_TEXT_MODIFIED = "edit_text_modified";

        @JvmStatic
        val FIELD_FIND_IN_TEXT_TOOLBAR_VISIBILITY = "find_in_text_toolbar_visible";
    }
}

