package com.taiko.noblenote

import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import com.taiko.noblenote.editor.DroidWriterEditText
import kotlinx.android.synthetic.main.layout_text_formatting.view.*

/**
 * Created by taiko000
 *
 * toolbar menu utility methods
 */
@Deprecated("obsolete")
object MenuHelper {


    @Deprecated("Will be replaced by proper rich text editor")
    fun addTextFormatting(ctx: Context, menu : Menu, editText: DroidWriterEditText): MenuItem? {
        val textFormattingToolbar = LayoutInflater.from(ctx).inflate(R.layout.layout_text_formatting, null)

        editText.setBoldToggleButton(textFormattingToolbar.btnToggleBold)

        editText.setItalicsToggleButton(textFormattingToolbar.btnToggleItalic)

        editText.setUnderlineToggleButton(textFormattingToolbar.btnToggleUnderline)

        val formattingMenuItem = menu.add("FormattingToolbar").setIcon(R.drawable.ic_action_btn_show_text_formatting_toolbar)
                .setActionView(textFormattingToolbar)
        formattingMenuItem.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS or MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW)


        // disable auto complete if the text formatting toolbar is shown, because auto-complete's underlining interferes
        // with the text formatting's underline
        formattingMenuItem.setOnActionExpandListener (object : MenuItem.OnActionExpandListener {
            override fun onMenuItemActionExpand(item: MenuItem): Boolean {
                editText.isTextWatcherEnabled = true
                editText.inputType = editText.inputType or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
                return true
            }

            override fun onMenuItemActionCollapse(item: MenuItem): Boolean {
                editText.isTextWatcherEnabled = false // this disables "on-typing" text formatting by DroidWriterEditText
                editText.inputType = editText.inputType and InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS.inv()
                return true
            }
        });

        return formattingMenuItem;

    }
}