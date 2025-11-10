/**
 * 
 */
package com.blogspot.noblenoteandroid.editor;

import android.text.Layout;
import android.text.Selection;
import android.text.Spannable;
import android.text.method.ArrowKeyMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.widget.TextView;

/**
 * @author taiko
 * 
 * link opening for EditText
 * 
 * based on
 * http://stackoverflow.com/questions/10572389/clickable-links-and-copy-paste-menu-in-editview-in-android/18541955#18541955
 *
 */
public class ArrowKeyLinkMovementMethod extends ArrowKeyMovementMethod 
{
	    @Override
	    public boolean onTouchEvent(TextView widget, Spannable buffer, MotionEvent event) {
	        int action = event.getAction();

	        if (action == MotionEvent.ACTION_UP ||
	            action == MotionEvent.ACTION_DOWN) {
	            int x = (int) event.getX();
	            int y = (int) event.getY();

	            x -= widget.getTotalPaddingLeft();
	            y -= widget.getTotalPaddingTop();

	            x += widget.getScrollX();
	            y += widget.getScrollY();

	            Layout layout = widget.getLayout();
	            int line = layout.getLineForVertical(y);
	            int off = layout.getOffsetForHorizontal(line, x);

	            ClickableSpan[] link = buffer.getSpans(off, off, ClickableSpan.class);

	            if (link.length != 0) {
	                if (action == MotionEvent.ACTION_UP) {
	                    link[0].onClick(widget);
	                }
	                else if (action == MotionEvent.ACTION_DOWN) {
	                    Selection.setSelection(buffer, buffer.getSpanStart(link[0]), buffer.getSpanEnd(link[0]));
	                }

	                return true;
	            }
	            /*else {
	                that's the line we need to remove
	                Selection.removeSelection(buffer);
	            }*/
	        }

	        return super.onTouchEvent(widget, buffer, event);
	    }

}
