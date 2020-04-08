package com.taiko.noblenote.editor

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Bundle
import android.text.Spanned
import android.view.View
import androidx.annotation.StringRes
import androidx.lifecycle.*
import com.taiko.noblenote.*
import com.taiko.noblenote.R
import com.taiko.noblenote.editor.EditorActivity.Companion.ARG_QUERY_TEXT
import com.taiko.noblenote.editor.EditorActivity.Companion.HTML
import com.taiko.noblenote.document.SFile
import rx.android.schedulers.AndroidSchedulers
import rx.lang.kotlin.plusAssign
import rx.schedulers.Schedulers
import rx.subscriptions.CompositeSubscription

class EditorViewModel(app : Application) : AndroidViewModel(app), LifecycleObserver
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

    val toast = SingleLiveEvent<@StringRes Int>()

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

        mFileUri = Uri.parse(extras.getString(EditorActivity.ARG_NOTE_URI))
        mOpenMode = extras.getString(EditorActivity.ARG_OPEN_MODE)!!
        isFocusable.postValue(!(mOpenMode == EditorActivity.HTML || mOpenMode == EditorActivity.READ_ONLY)) // no editing if html source should be shown
        toolbarTitle.postValue(SFile(mFileUri).nameWithoutExtension)
        queryText.postValue(extras.getString(ARG_QUERY_TEXT).orEmpty());

    }

    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    fun onStart()
    {
        if (SFile(mFileUri).lastModified() > lastModified.value!!) {
            reload()
        }
    }

    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    fun onStop()
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

        mCompositeSubscription += FileHelper.readFile(mFileUri, this.getApplication(), parseHtml = mOpenMode != HTML) // don't parse html if it should display the html source of the note
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
                    toast.postValue(R.string.msg_file_loading_error)
                    finishActivity.postValue(true);

                });
    }

    private fun saveNote() {

        val noteText = Html.toHtml(editorText.value as Spanned, this.getApplication<Application>().resources.displayMetrics.density);


        mCompositeSubscription += FileHelper.writeFile(filePath = mFileUri, text = noteText)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe {

                    lastModified.value = it
                    isModified.postValue(false);
                    toast.postValue(R.string.noteSaved)
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

        val clipboard = this.getApplication<Application>().getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
        val clip = android.content.ClipData.newPlainText("Copied Text", editorText.value)
        clipboard.setPrimaryClip(clip);

       toast.postValue(R.string.msg_copied_to_clipboard);
    }

}