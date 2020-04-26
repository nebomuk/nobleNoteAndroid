package com.taiko.noblenote.extensions

import android.content.Context
import android.os.Bundle
import androidx.annotation.CheckResult
import androidx.annotation.NavigationRes
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.taiko.noblenote.R
import com.taiko.noblenote.document.SFile
import com.taiko.noblenote.editor.EditorFragment

// start the note editor
@CheckResult fun Any.createNoteEditorArgs(file: SFile, argOpenMode : String, argQueryText : String = "") : Bundle

{
    val bundle = Bundle()
    bundle.putString(EditorFragment.ARG_NOTE_URI, file.uri.toString())
    bundle.putString(EditorFragment.ARG_OPEN_MODE, argOpenMode)
    bundle.putString(EditorFragment.ARG_QUERY_TEXT,argQueryText);
    return bundle;
}