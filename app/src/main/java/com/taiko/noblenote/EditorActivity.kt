package com.taiko.noblenote


import android.app.Activity
import android.app.AlertDialog
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import com.google.android.material.snackbar.Snackbar
import androidx.core.app.ActivityCompat
import android.view.*
import android.widget.Toast
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
import java.io.File


class EditorActivity : Activity() {


    private val log = loggerFor()

    private var mFocusable = true // if set to false, the note is opened in read only mode
    private lateinit var mFilePath: String;
    private lateinit var mOpenMode: String;
    private var lastModified: Long = 0
    private var mFormattingMenuItem: MenuItem? = null
    private val mCompositeSubscription : CompositeSubscription = CompositeSubscription();

    private lateinit var mUndoRedo: TextViewUndoRedo

    private lateinit var  mFindInTextToolbarController: FindInTextToolbarController

    private val mPermissionRequestCode = 0xEA;

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if(requestCode != mPermissionRequestCode)
        {
            return;
        }

        if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            log.d( " permission granted, reloading file");
            // permission was granted
            reload()

        } else {

            log.d( " permission denied, finish() and setting root path to internal storage");
            // permission denied
            Toast.makeText(this, R.string.msg_external_storage_permission_denied, Toast.LENGTH_SHORT).show();
            Pref.rootPath.onNext(Pref.fallbackRootPath);
            editor_edit_text.isModified = false; // avoid attempts so save the file in onStop
            finish(); // permission denied or sd not mounted, finish editor activity and show the empty file list of the parent activity
        }
        return;
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        //requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        super.onCreate(savedInstanceState)

        log.d( ".onCreate()");

        //This has to be called before setContentVie

        window.requestFeature(Window.FEATURE_ACTION_MODE_OVERLAY)
        setContentView(R.layout.activity_editor)

        mUndoRedo = TextViewUndoRedo(editor_edit_text)

        editor_scroll_view.visibility = View.INVISIBLE

        progress_bar_file_loading.progressDrawable = MaterialProgressDrawable.create(this)
        progress_bar_file_loading.indeterminateDrawable = MaterialIndeterminateProgressDrawable.create(this)

        // hide soft keyboard by default
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        // uncomment to enable close button
/*        toolbar.setNavigationIcon(R.drawable.ic_close_black_24dp)
        toolbar.setNavigationOnClickListener { showExitDialog() }*/

        val extras = intent.extras ?: return
        mFilePath = extras.getString(ARG_FILE_PATH)!!
        mOpenMode = extras.getString(ARG_OPEN_MODE)!!
        mFocusable = !(mOpenMode == HTML || mOpenMode == READ_ONLY) // no editing if html source should be shown

        toolbar.title = File(mFilePath).nameWithoutExtension
        populateMenu(toolbar.menu)

        editor_edit_text.isTextWatcherEnabled = false
        editor_edit_text.isFocusable = mFocusable // read only if not mFocusable


        mFindInTextToolbarController = FindInTextToolbarController(this);

        if(!Pref.isInternalStorage)
        {

            if(!FileHelper.checkFilePermission(this))
            {
                ActivityCompat.requestPermissions(this, arrayOf(android.Manifest.permission.WRITE_EXTERNAL_STORAGE), mPermissionRequestCode);
            }
        }


        val intentFilter = IntentFilter(Intent.ACTION_MEDIA_REMOVED);
        intentFilter.addAction(Intent.ACTION_MEDIA_EJECT);

        // switch to internal storage when sd unmounted
        mCompositeSubscription += RxBroadcastReceiver.create(this, intentFilter).filter { !Pref.isInternalStorage }.subscribe {
            Toast.makeText(this,getString(R.string.msg_external_storage_not_mounted) + " "
                    + getString(R.string.msg_switching_internal_storage), Toast.LENGTH_LONG).show();
            editor_edit_text.isModified = false; // avoid attempts so save the file in onStop
            Pref.rootPath.onNext(Pref.fallbackRootPath);
            finish();
        }


    }

    public override fun onStart() {
        super.onStart()
        log.d( ".onStart()");

        if (FileHelper.checkFilePermission(this) && File(mFilePath).lastModified() > lastModified) {
            reload()
        }
        // fix selection & formatting for Honeycomb and newer devices
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            editor_edit_text.customSelectionActionModeCallback = SelectionActionModeCallback(editor_edit_text)
        }
    }

    /**
     * reloads the current note file
     */
    private fun reload() {
        // load file contents and parse html thread
        log.d( ".reload()");

            mCompositeSubscription += FileHelper.readFile(mFilePath, this, parseHtml = mOpenMode != HTML) // don't parse html if it should display the html source of the note
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

                        lastModified = File(mFilePath).lastModified()

                    }, {
                        log.e(it.message)
                        Toast.makeText(this, R.string.msg_file_loading_error, Toast.LENGTH_SHORT).show()
                        finish();

                    });
    }

    public override fun onStop() {
        super.onStop()

        log.d( ".onStop()");

        // does nothing if open mode is set to read only

        // if not mFocusable, changes can not be made
        if (Pref.isAutoSaveEnabled &&  !isChangingConfigurations &&  mFocusable && editor_edit_text.isModified && FileHelper.checkFilePermission(this))
        // then save the note
        {

                FileHelper.writeFile(filePath = mFilePath, text = editor_edit_text.textHTML)
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

            val draw = resources.getDrawable(R.drawable.ic_undo_black_24dp)
            draw.setTintCompat(this,getColorForState(it))
            undoItem.isEnabled = it;
            undoItem.icon = draw
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

            val draw = resources.getDrawable(R.drawable.ic_redo_black_24dp)
            draw.setTintCompat(this, getColorForState(it))
            redoItem.isEnabled = it;
            redoItem.icon = draw
        }


        //mFormattingMenuItem = MenuHelper.addTextFormatting(this,menu,editor_edit_text);



        val itemDone = menu.add(R.string.action_done).setIcon(R.drawable.ic_done_black_24dp)
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)

        itemDone.setOnMenuItemClickListener {
            finish()
            true
        }

        menu.add(R.string.action_auto_save)
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)
                .setCheckable(true)
                .setEnabled(mFocusable)
                .setChecked(Pref.isAutoSaveEnabled)
                .setOnMenuItemClickListener {
                    it.isChecked = !it.isChecked;
                    Pref.isAutoSaveEnabled = it.isChecked
                    true
                }



        //item.expandActionView(); // show text formatting toolbar by default

    }





    override fun onDestroy() {
        super.onDestroy()
        log.d( ".onDestroy()");

        mCompositeSubscription.clear();
    }








    companion object {

        @JvmStatic
        fun getColorForState(enabled : Boolean) : /*color int*/ Int
        {
            return if(enabled) R.color.md_grey_800 else R.color.md_grey_400
        }

        @JvmStatic
        val ARG_FILE_PATH = "file_path"

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

