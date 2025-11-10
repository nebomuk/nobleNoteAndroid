package de.blogspot.noblenoteandroid.viewmodels

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Spanned
import android.view.View
import android.widget.Toast
import androidx.annotation.StringRes
import androidx.lifecycle.*
import de.blogspot.noblenoteandroid.R
import de.blogspot.noblenoteandroid.Pref
import de.blogspot.noblenoteandroid.filesystem.SFile
import de.blogspot.noblenoteandroid.editor.Html
import de.blogspot.noblenoteandroid.fragments.EditorFragment
import de.blogspot.noblenoteandroid.filesystem.FileHelper
import de.blogspot.noblenoteandroid.util.SingleLiveEvent
import de.blogspot.noblenoteandroid.util.loggerFor
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.plusAssign
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class EditorViewModel(app : Application) : AndroidViewModel(app), DefaultLifecycleObserver
{

    private val log = loggerFor();

    private val mCompositeSubscription = CompositeSubscription();

   val undoEnabled =  MutableLiveData<Boolean>(false);

    val redoEnabled = MutableLiveData<Boolean>(false);

    val isModified = MutableLiveData<Boolean>(false);

    val isFocusable = MutableLiveData<Boolean>(true);

    val toolbarFindInTextVisible = MutableLiveData<Boolean>(false);

    val progressBarFileLoadingVisibility = MutableLiveData<Int>(View.GONE);

    val  editorScrollViewVisibility  = MutableLiveData<Int>(View.VISIBLE);

    private val lastModified = MutableLiveData<Long>(0L)

    val editorText = MutableLiveData<CharSequence>("");

    val queryText = MutableLiveData<CharSequence>("");

    val toolbarTitle = MutableLiveData<String>("default_title");

    // string res
    val toast = SingleLiveEvent<Int>()

    val finishActivity = SingleLiveEvent<Boolean>()


    // must be a bound callback, so that it is not executed when updating live data programmatically
    fun onEditorTextChanged(text : CharSequence) {
        isModified.value = true; // do not use post, causes raze hazard when reloading (calls isModified = false)
    }


    private lateinit var mFileUri: Uri;
    private lateinit var mOpenMode: String;

    fun populateFromBundle(extras : Bundle?)
    {
        if(extras == null)
        {
            return;
        }

        mFileUri = Uri.parse(extras.getString(EditorFragment.ARG_NOTE_URI))
        mOpenMode = extras.getString(EditorFragment.ARG_OPEN_MODE)!!
        isFocusable.postValue(!(mOpenMode == EditorFragment.HTML || mOpenMode == EditorFragment.READ_ONLY)) // no editing if html source should be shown
        toolbarTitle.postValue(SFile(mFileUri).nameWithoutExtension)
        queryText.value = (extras.getString(EditorFragment.ARG_QUERY_TEXT).orEmpty());

    }

    override fun onStart(owner: LifecycleOwner)
    {
        if (SFile(mFileUri).lastModified() > lastModified.value!!) {
            reload()
        }
    }

    override fun onStop(owner: LifecycleOwner)
    {
        toolbarFindInTextVisible.value = false;

            // if not mFocusable, changes can not be made
        if (Pref.isAutoSaveEnabled  && isFocusable.value!! && isModified.value!!)
        // then save the note
        {
            saveNote()
        }
    }

    fun onMenuItemDoneClicked()
    {
        saveNote();
        finishActivity.postValue(true);

    }

    /**
     * reloads the current note file
     */
    private fun reload() {
        // load file contents and parse html thread
        log.d( ".reload()");

        mCompositeSubscription += FileHelper.readFile(mFileUri, this.getApplication(), parseHtml = mOpenMode != EditorFragment.HTML) // don't parse html if it should display the html source of the note
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({
                    editorText.value = it;
                    progressBarFileLoadingVisibility.postValue(View.GONE);
                    editorScrollViewVisibility.postValue(View.VISIBLE);
                    isModified.postValue(false)// must be postValue!, reset modification state because modification flag has been set by editor_edit_text.setText

                    if (!queryText.value.isNullOrBlank()) {
                        toolbarFindInTextVisible.postValue(true)
                    }

                    if(lastModified.value != 0L) // 0 is initial value when loading the first time, so we do not show the "reloaded" snackbar
                    {
                        toast.postValue(R.string.noteReloaded)
                    }

                    lastModified.value = SFile(mFileUri).lastModified();

                }, {
                    log.e(it.message)
                    applicationToast(R.string.msg_file_loading_error)
                    finishActivity.postValue(true);

                });
    }

    private fun saveNote() {

        val noteText = Html.toHtml(editorText.value as Spanned, this.getApplication<Application>().resources.displayMetrics);


        /* automatically unsubscribes */ FileHelper.writeFile(filePath = mFileUri, text = noteText)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe { it ->

                    lastModified.value = it
                    isModified.postValue(false);
                    applicationToast(R.string.noteSaved)
                }
    }

    override fun onCleared() {
        super.onCleared()

        mCompositeSubscription.clear();
    }

    fun onDiscardChangesClicked() {
        isModified.value = false;
        finishActivity.postValue(true);
    }

    fun onCopyToClipboardClicked() {

        val clipboard = this.getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Copied Text", editorText.value)
        clipboard.setPrimaryClip(clip);

       toast.postValue(R.string.msg_copied_to_clipboard);
    }

    // toast not bound by EditorActivity's scope
    private fun applicationToast(@StringRes msg :  Int)
    {
            val handler = Handler(Looper.getMainLooper())
            handler.post {
                Toast.makeText(this.getApplication(),msg,Toast.LENGTH_SHORT).show();
            }
    }

}
