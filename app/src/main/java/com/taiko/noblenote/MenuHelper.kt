package com.taiko.noblenote

import android.content.Context
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast

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
                .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM)
        itemCopyToClipboard.setOnMenuItemClickListener {
            if (android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
                val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as android.text.ClipboardManager
                clipboard.text = textSource()
            } else {
                val clipboard = ctx.getSystemService(Context.CLIPBOARD_SERVICE) as android.content.ClipboardManager
                val clip = android.content.ClipData.newPlainText("Copied Text", textSource())
                clipboard.primaryClip = clip
            }

            Toast.makeText(ctx, R.string.msg_copied_to_clipboard, Toast.LENGTH_SHORT).show()
            true
        }

    }
}