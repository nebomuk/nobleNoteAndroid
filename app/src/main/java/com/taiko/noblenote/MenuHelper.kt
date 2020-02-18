package com.taiko.noblenote

import android.content.Context
import android.text.InputType
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import kotlinx.android.synthetic.main.layout_text_formatting.view.*

/**
 * Created by taiko000
 *
 * toolbar menu utility methods
 */
object MenuHelper {

    /**
     * adds a copy text to clipboard action to the given menu
     */
    fun addCopyToClipboard(ctx: Context, menu: Menu, textSource: () -> CharSequence) {
        val itemCopyToClipboard = menu.add(R.string.action_copy_to_clipboard)
                .setIcon(R.drawable.ic_action_content_copy)
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_NEVER)
        itemCopyToClipboard.setOnMenuItemClickListener {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as android.text.ClipboardManager
                clipboard.text = textSource()
            } else {
                val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Copied Text", textSource())
                clipboard.setPrimaryClip(clip);
            }

            Toast.makeText(ctx, R.string.msg_copied_to_clipboard, Toast.LENGTH_SHORT).show()
            true
        }

    }

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