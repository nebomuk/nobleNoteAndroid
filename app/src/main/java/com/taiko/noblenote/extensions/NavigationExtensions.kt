package com.taiko.noblenote.extensions

import android.os.Bundle
import androidx.annotation.CheckResult
import com.taiko.noblenote.filesystem.SFile
import com.taiko.noblenote.fragments.EditorFragment

// start the note editor
@CheckResult fun Any.createNoteEditorArgs(file: SFile, argOpenMode : String, argQueryText : String) : Bundle

{
    val bundle = Bundle()
    bundle.putString(EditorFragment.ARG_NOTE_URI, file.uri.toString())
    bundle.putString(EditorFragment.ARG_OPEN_MODE, argOpenMode)
    bundle.putString(EditorFragment.ARG_QUERY_TEXT,argQueryText);
    return bundle;
}