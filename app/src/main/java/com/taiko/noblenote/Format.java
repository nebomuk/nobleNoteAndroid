package com.taiko.noblenote;

import android.text.Spannable;
import android.text.style.CharacterStyle;
import android.text.style.StyleSpan;
import android.widget.EditText;


/**
 * 
 * @author Taiko
 * 
 * use these methods to format isSelected text in EditText
 * with StyleSpans and CharacterStyles and derived classes
 *
 */

public class Format 
{
	/**
	 * 
	 * @param style expects e.g. android.graphics.Typeface.ITALIC
	 * 
	 */
	public static void toggleStyleSpan(EditText editText, int style)
	{
		int selectionStart = editText.getSelectionStart();
    	
    	int selectionEnd = editText.getSelectionEnd();
    	
    	if (selectionStart > selectionEnd){
    		int tmp = selectionEnd;
    		selectionEnd = selectionStart;
    		selectionStart = tmp;
    	}
    	
    	if (selectionEnd > selectionStart)
    	{
    		Spannable str = editText.getText();
    		StyleSpan[] ss = str.getSpans(selectionStart, selectionEnd, StyleSpan.class);
    		
    		boolean enable = true;
			// remove old formatting of type "style"
    		for (int i = 0; i < ss.length; i++) {
    			if (ss[i].getStyle() == style)
    			{
    				enable  = false;
    				// the formatting span object may spans to a larger extend than the isSelected range
    				// thus the ranges before the selection and after the selection must get their own spans
    				if(str.getSpanStart(ss[i]) < selectionStart)
    				{
    					str.setSpan(new StyleSpan(style),str.getSpanStart(ss[i]) , selectionStart, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    				}
    				if(selectionEnd < str.getSpanEnd(ss[i]))
    				{
    					str.setSpan(new StyleSpan(style),selectionEnd,str.getSpanEnd(ss[i]), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    				}
    				// remove the span object of the selection
    				str.removeSpan(ss[i]);
    			}
            }
    		
    		// set the style for the whole selection
    		if (enable){
    			str.setSpan(new StyleSpan(style), selectionStart, selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    		}
    	}
    }
	

	/**
	 * 
	 * @param style expects e.g. android.graphics.Typeface.ITALIC
	 * 
	 * @param enable should the style be enabled or disabled
	 */
	public static void formatStyleSpan(EditText editText, int style, boolean enable)
	{
		int selectionStart = editText.getSelectionStart();
    	
    	int selectionEnd = editText.getSelectionEnd();
    	
    	if (selectionStart > selectionEnd){
    		int tmp = selectionEnd;
    		selectionEnd = selectionStart;
    		selectionStart = tmp;
    	}
    	
    	if (selectionEnd > selectionStart)
    	{
    		Spannable str = editText.getText();
    		StyleSpan[] ss = str.getSpans(selectionStart, selectionEnd, StyleSpan.class);
    		
    		// remove old formatting of type "style"
    		for (int i = 0; i < ss.length; i++) {
    			if (ss[i].getStyle() == style)
    			{
    				// the formatting span object may spans to a larger extend than the isSelected range
    				// thus the ranges before the selection and after the selection must get their own spans
    				if(str.getSpanStart(ss[i]) < selectionStart)
    				{
    					str.setSpan(new StyleSpan(style),str.getSpanStart(ss[i]) , selectionStart, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    				}
    				if(selectionEnd < str.getSpanEnd(ss[i]))
    				{
    					str.setSpan(new StyleSpan(style),selectionEnd,str.getSpanEnd(ss[i]), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    				}
    				// remove the span object of the selection
    				str.removeSpan(ss[i]);
    			}
            }
    		
    		// set the style for the whole selection
    		if (enable){
    			str.setSpan(new StyleSpan(style), selectionStart, selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    		}
    	}
    }
	

	/**
	 * 
	 * @param style expects e.g. UnderlineSpan.class as argument
	 * 
	 * @param enable should the style be enabled or disabled
	 */
	public static void formatCharacterStyle(EditText editText, Class<? extends CharacterStyle> charStyleClass, boolean enable)
	{
    	int selectionStart = editText.getSelectionStart();
    	
    	int selectionEnd = editText.getSelectionEnd();
    	
    	
    	if (selectionStart > selectionEnd){// swap
    		int tmp = selectionEnd;
    		selectionEnd = selectionStart;
    		selectionStart = tmp;
    	}
    	
    	if (selectionEnd > selectionStart)
    	{
    		Spannable text = editText.getText();
    		CharacterStyle[] ss = text.getSpans(selectionStart, selectionEnd, charStyleClass);
    		
    		// remove styles of that type
    		for (int i = 0; i < ss.length; i++) {
    			// the formatting span object may spans to a larger extend than the isSelected range
				// thus the ranges before the selection and after the selection must get their own spans
				if(text.getSpanStart(ss[i]) < selectionStart)
				{
					// set span  to the runtime type of the CharacterStyle subclass
					try {
						text.setSpan(charStyleClass.newInstance(), text.getSpanStart(ss[i]),selectionStart, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if(selectionEnd < text.getSpanEnd(ss[i]))
				{
					try {
						text.setSpan(charStyleClass.newInstance(), selectionEnd,text.getSpanEnd(ss[i]), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				// remove the span object of the selection
    			text.removeSpan(ss[i]);
            }
    		
    		// set the style for the whole selection
    		if (enable){
    			try {
					text.setSpan(charStyleClass.newInstance(), selectionStart, selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				} catch (Exception e) {
					e.printStackTrace();
				}
    		}
    	}
	}
	
	/**
	 * 
	 * @param style expects e.g. UnderlineSpan.class as argument
	 */
	public static void toggleCharacterStyle(EditText editText, Class<? extends CharacterStyle> charStyleClass)
	{
    	int selectionStart = editText.getSelectionStart();
    	
    	int selectionEnd = editText.getSelectionEnd();
    	
    	
    	if (selectionStart > selectionEnd){// swap
    		int tmp = selectionEnd;
    		selectionEnd = selectionStart;
    		selectionStart = tmp;
    	}
    	
    	if (selectionEnd > selectionStart)
    	{
    		Spannable text = editText.getText();
    		CharacterStyle[] ss = text.getSpans(selectionStart, selectionEnd, charStyleClass);
    		
    		boolean enable = true;
    		// remove styles of that type
    		for (int i = 0; i < ss.length; i++) {
    			
    			enable = false;
    			
    			// the formatting span object may spans to a larger extend than the isSelected range
				// thus the ranges before the selection and after the selection must get their own spans
				if(text.getSpanStart(ss[i]) < selectionStart)
				{
					// set span  to the runtime type of the CharacterStyle subclass
					try {
						text.setSpan(charStyleClass.newInstance(), text.getSpanStart(ss[i]),selectionStart, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if(selectionEnd < text.getSpanEnd(ss[i]))
				{
					try {
						text.setSpan(charStyleClass.newInstance(), selectionEnd,text.getSpanEnd(ss[i]), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				// remove the span object of the selection
    			text.removeSpan(ss[i]);
            }
    		
    		// set the style for the whole selection
    		if (enable){
    			try {
					text.setSpan(charStyleClass.newInstance(), selectionStart, selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				} catch (Exception e) {
					e.printStackTrace();
				}
    		}
    	}
	}
	
	
}
