package com.taiko.noblenote


import android.app.Activity
import android.app.AlertDialog
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.support.design.widget.Snackbar
import android.text.InputType
import android.util.Log
import android.view.*
import android.widget.Toast
import android.widget.Toolbar
import com.jakewharton.rxbinding.view.clicks
import com.jakewharton.rxbinding.widget.textChanges
import kotlinx.android.synthetic.main.activity_editor.*
import kotlinx.android.synthetic.main.text_formatting_toolbar.view.*
import kotlinx.android.synthetic.main.toolbar_find_in_text.*
import kotlinx.android.synthetic.main.toolbar_find_in_text.view.*
import net.yanzm.actionbarprogress.MaterialIndeterminateProgressDrawable
import net.yanzm.actionbarprogress.MaterialProgressDrawable
import rx.android.schedulers.AndroidSchedulers
import rx.schedulers.Schedulers
import java.io.File




class NoteEditorActivity : Activity() {


    private val TAG = this.javaClass.simpleName

    private var filePath = ""
    private var focusable = true // if set to false, the note is opened in read only mode
    private var openMode: String? = null
    private var lastModified: Long = 0
    private var mFormattingMenuItem: MenuItem? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        //requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);

        super.onCreate(savedInstanceState)
        //This has to be called before setContentVie

        window.requestFeature(Window.FEATURE_ACTION_MODE_OVERLAY)
        setContentView(R.layout.activity_editor)

        editor_scroll_view.visibility = View.INVISIBLE

        progress_bar_file_loading.progressDrawable = MaterialProgressDrawable.create(this)
        progress_bar_file_loading.indeterminateDrawable = MaterialIndeterminateProgressDrawable.create(this)

        val toolbar = findViewById(R.id.toolbar) as Toolbar

        createToolbarMenu(toolbar.menu)

        //setActionBar(toolbar);

        //getActionBar().setDisplayHomeAsUpEnabled(true);

        // hide soft keyboard by default
        window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN)

        val extras = intent.extras ?: return

        filePath = extras.getString(NoteEditorActivity.ARG_FILE_PATH)
        openMode = extras.getString(NoteEditorActivity.ARG_OPEN_MODE)
        focusable = !(openMode == HTML || openMode == READ_ONLY) // no editing if html source should be shown

        //getActionBar().setTitle(new File(filePath).getName());

        lastModified = File(filePath).lastModified()
        reload()

    }

    //	@Override
    //	public void onStart()
    //	{
    //		super.onStart();
    //
    //	}

    public override fun onResume() {
        super.onResume()

        if (File(filePath).lastModified() > lastModified) {
            reload()
            lastModified = File(filePath).lastModified()
            Snackbar.make(layout_root, R.string.noteReloaded, Snackbar.LENGTH_SHORT).show()
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
        FileHelper.readFile(filePath,this,true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe( {
                    editor_edit_text.setText(it)
                    editor_edit_text.movementMethod = ArrowKeyLinkMovementMethod()
                    progress_bar_file_loading.visibility = View.GONE
                    editor_scroll_view.visibility = View.VISIBLE
                    editor_edit_text.isModified = false // reset modification state because modification flag has been set by editor_edit_text.setText

                }, {
                    Log.e(TAG, it.message)
                    Snackbar.make(layout_root!!, R.string.file_loading_error, Snackbar.LENGTH_LONG).show()

                })

    }

    public override fun onPause() {
        super.onPause()

        // does nothing if open mode is set to read only

        // if not focusable, changes can not be made
        if (focusable && editor_edit_text.isModified)
        // then save the note
        {
            FileHelper.writeFile(filePath = filePath,text = editor_edit_text.textHTML)
                    .subscribeOn(Schedulers.io())
                    .subscribe {

                lastModified = it
                editor_edit_text.isModified = false
                runOnUiThread { Toast.makeText(this.applicationContext, R.string.noteSaved, Toast.LENGTH_SHORT).show() }
            }

        }
    }

    fun showExitDialog(runnable: Runnable) {
        if (editor_edit_text.isModified) {
            val builder = AlertDialog.Builder(this@NoteEditorActivity)
            builder.setMessage(R.string.dialogDiscardKeepEditing)
                    .setPositiveButton(R.string.discard) { dialog, id ->
                        editor_edit_text.isModified = false//  does not get saved
                        val handler = Handler()
                        handler.post(runnable)
                    }
                    .setNegativeButton(R.string.keepEditing, null)
            // Create the AlertDialog object and return it
            builder.create().show()
        } else
        // no modification, do not ask to save the document
        {
            editor_edit_text.isModified = false//  does not get saved
            runnable.run()
        }
    }

    override fun onBackPressed() {
        if (mFormattingMenuItem!!.isActionViewExpanded) {
            super.onBackPressed()

        }
        else if(toolbar_find_in_text.visibility == View.VISIBLE)
        {
            toolbar_find_in_text.visibility = View.INVISIBLE
        }
        else {
            showExitDialog(Runnable { super@NoteEditorActivity.onBackPressed() })
        }

    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == android.R.id.home) {
            showExitDialog(Runnable { finish() })
            return true
        }


        return super.onOptionsItemSelected(item)
    }

    fun createToolbarMenu(menu: Menu): Boolean {


        MenuHelper.addCopyToClipboard(this,menu,{editor_edit_text.text})

        val textFormattingToolbar = LayoutInflater.from(this).inflate(R.layout.text_formatting_toolbar, null)
        mFormattingMenuItem = menu.add("FormattingToolbar").setIcon(R.drawable.ic_action_btn_show_text_formatting_toolbar)
                .setActionView(textFormattingToolbar)
        mFormattingMenuItem!!.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS or MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)


        // disable auto complete if the text formatting toolbar is shown, because auto-complete's underlining interferes
        // with the text formatting's underline
        mFormattingMenuItem!!.setOnActionExpandListener(object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                editor_edit_text.isTextWatcherEnabled = true
                editor_edit_text.inputType = editor_edit_text.inputType or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                editor_edit_text.isTextWatcherEnabled = false // this disables "on-typing" text formatting by DroidWriterEditText
                editor_edit_text.inputType = editor_edit_text.inputType and InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS.inv()
                return true
            }
        })

        val itemFindInText = menu.add(R.string.action_find_in_text)
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)
        itemFindInText.setOnMenuItemClickListener {
            toolbar_find_in_text.visibility = View.VISIBLE
            toolbar_find_in_text_edit_text.requestFocus()
//            val imm = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
//            imm.showSoftInput(toolbar_find_in_text_edit_text, InputMethodManager.SHOW_FORCED)
            window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            true
        }
        toolbar_find_in_text.toolbar_find_in_text_close.clicks()
                .map { View.INVISIBLE }
                .subscribe { toolbar_find_in_text.visibility  = it}

        val finder = Finder(editor_edit_text);
        toolbar_find_in_text.toolbar_find_in_text_edit_text.textChanges().subscribe { finder.searchString = it.toString() }

        toolbar_find_in_text.arrow_down.clicks().subscribe { finder.selectNext()  }
        toolbar_find_in_text.arrow_up.clicks().subscribe { finder.selectPrevious() }

        val itemDone = menu.add(R.string.action_done).setIcon(R.drawable.ic_done_black_24dp)
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_ALWAYS)

        itemDone.setOnMenuItemClickListener {
            finish()
            true
        }




        editor_edit_text.setSingleLine(false)
        editor_edit_text.isTextWatcherEnabled = false


        editor_edit_text.setBoldToggleButton(textFormattingToolbar.btnToggleBold)

        editor_edit_text.setItalicsToggleButton(textFormattingToolbar.btnToggleItalic)

        editor_edit_text.setUnderlineToggleButton(textFormattingToolbar.btnToggleUnderline)



        editor_edit_text.isFocusable = focusable // read only if not focusable

        //item.expandActionView(); // show text formatting toolbar by default

        return super.onCreateOptionsMenu(menu)
    }

    companion object {
        val ARG_FILE_PATH = "file_path"
        val ARG_OPEN_MODE = "open_mode"
        val HTML = "html"
        val READ_WRITE = "read_write"
        val READ_ONLY = "read_only"
    }
} 