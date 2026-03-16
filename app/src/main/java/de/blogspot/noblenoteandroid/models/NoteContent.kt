package de.blogspot.noblenoteandroid.models

import android.text.Spanned

data class NoteContent(
    val text: Spanned,
    val metadata: NoteMetadata = NoteMetadata()
)
