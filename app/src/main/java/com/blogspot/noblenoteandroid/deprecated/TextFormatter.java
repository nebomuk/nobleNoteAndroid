package com.blogspot.noblenoteandroid.deprecated;

import android.text.Editable;
import android.text.Selection;
import android.text.Spannable;
import android.text.TextWatcher;
import android.text.style.CharacterStyle;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;

/**
 * @author taiko
 *
 *
 * a calss to experiment with text formatting. All properly functioning methods have been moved into the Format class
 *
 *must call after the editText has been populated
 * textFormatter.getEditText().setOnSelectionChangedListener(textFormatter);
 * textFormatter.getEditText().addTextChangedListener(textFormatter);
 *
 */
public class TextFormatter implements TextWatcher, CustomEditText.OnSelectionChangedListener

{
	
	private CustomEditText editText;
	private CompoundButton btnToggleBold;
	private CompoundButton btnToggleItalic;
	private CompoundButton btnToggleUnderline;
	private CompoundButton btnToggleStrikethrough;
	
	private int styleStart;
	private int lastSelectionStart;
	

	public TextFormatter() 
	{
	}
	
	
	
	/**
	 * 
	 * @param style expects e.g. android.graphics.Typeface.ITALIC
	 * 
	 * @param enable should the style be enabled or disabled
	 */
	void styleSpanFormat(int style, boolean enable)
	{
		int selectionStart = editText.getSelectionStart();
    	
    	styleStart = selectionStart;
    	
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
	 * @param style expects e.g. new UnderlineSpan() as argument
	 * 
	 * @param enable should the style be enabled or disabled
	 */
	void characterStyleFormat(CharacterStyle charStyle, boolean enable)
	{
    	int selectionStart = editText.getSelectionStart();
    	
    	styleStart = selectionStart;
    	
    	int selectionEnd = editText.getSelectionEnd();
    	
    	
    	if (selectionStart > selectionEnd){// swap
    		int tmp = selectionEnd;
    		selectionEnd = selectionStart;
    		selectionStart = tmp;
    	}
    	
    	if (selectionEnd > selectionStart)
    	{
    		Spannable text = editText.getText();
    		CharacterStyle[] ss = text.getSpans(selectionStart, selectionEnd, charStyle.getClass());
    		
    		// remove styles of that type
    		for (int i = 0; i < ss.length; i++) {
    			// the formatting span object may spans to a larger extend than the isSelected range
				// thus the ranges before the selection and after the selection must get their own spans
				if(text.getSpanStart(ss[i]) < selectionStart)
				{
					// set span  to the runtime type of the CharacterStyle subclass
					try {
						text.setSpan(charStyle.getClass().newInstance(), text.getSpanStart(ss[i]),selectionStart, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				if(selectionEnd < text.getSpanEnd(ss[i]))
				{
					try {
						text.setSpan(charStyle.getClass().newInstance(), selectionEnd,text.getSpanEnd(ss[i]), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
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
					text.setSpan(charStyle.getClass().newInstance(), selectionStart, selectionEnd, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				} catch (Exception e) {
					e.printStackTrace();
				}
    		}
    	}
	}
	
	/**
	 * this method enables the corresponding formatting buttons if a text 
	 *  formatting object is found in the selection
	 */
	public void onSelectionChanged(int selectionStart, int selectionEnd) {
		
		// set style start here??
		Log.i("MyTag", "onSelectionChanged position: " + selectionStart);
		
		lastSelectionStart = selectionStart;
		// cursor moved
		if(selectionStart == selectionEnd)
		{
			Spannable text = editText.getText();
			CharacterStyle[] cs = text.getSpans(selectionEnd, selectionEnd, CharacterStyle.class);
			setToggleButtons(cs);
		}
		else if (selectionStart > selectionEnd){// swap
    		int tmp = selectionEnd;
    		selectionEnd = selectionStart;
    		selectionStart = tmp;
    	}
		
		
		if (selectionEnd > selectionStart)
    	{	
			Spannable text = editText.getText();
			CharacterStyle[] cs = text.getSpans(selectionStart, selectionEnd, CharacterStyle.class);
			setToggleButtons(cs);
    	}	
	}
	
	/**
	 * enables or disables the toggle buttons that correspond to the formatting information inside the
	 * CharacterStyle[] array
	 */
	protected void setToggleButtons(CharacterStyle[] cs)
	{
		boolean underline = false;
		boolean strikethrough = false;
		boolean bold = false;
		boolean italic = false;
					
		
		for(int i = 0; i< cs.length; ++i)
		{
			if(cs[i].getClass() == UnderlineSpan.class)
			{
				underline = true;
			}	
			else if(cs[i].getClass() == StrikethroughSpan.class)
			{
				strikethrough = true;
			}
			else if(cs[i].getClass() == StyleSpan.class)
			{
				StyleSpan span = (StyleSpan)cs[i];
				if(span.getStyle() == android.graphics.Typeface.ITALIC)
				{
					italic = true;
				}
				else if(span.getStyle() == android.graphics.Typeface.BOLD)
				{
					bold = true;
				}
				else if(span.getStyle() == android.graphics.Typeface.BOLD_ITALIC)
				{
					bold = italic = true;
				}
			}
		}
		btnToggleBold.setChecked(bold);
		btnToggleItalic.setChecked(italic);
		btnToggleUnderline.setChecked(underline);
		btnToggleStrikethrough.setChecked(strikethrough);
	}
	

	public void afterTextChanged(Editable s) {
		// TODO use lastSelected position and position from here to format inserted text
		// warning: lastSelected pos can be greater than position from here
		// if text is copy pasted, it must not be formatted! copy paste might had happened if position difference is > 1
		Editable text = s;//editText.getText();
		int start = Selection.getSelectionStart(text);
		
		
		// remove chars does not work because lastSelectionStart does not get updated by selectionchanged
		if(start - lastSelectionStart > 1 || start < lastSelectionStart) 
		{
			lastSelectionStart = start;
			return;
		}
		
		class StrikethroughSpanFix extends StrikethroughSpan
		{
			public StrikethroughSpanFix(){}
			public StrikethroughSpanFix(int start, int end) {
				this.start = start;
				this.end = end;
			}
			public int start = 0;
			public int end = 0;
		}
		
		// TODO intermediate unformatted spans
		if(btnToggleStrikethrough.isChecked())
		{
			StrikethroughSpan spans[] = text.getSpans(start-1, start, StrikethroughSpan.class);
			// this (errornously) detecs spans with and span.end before start-1
			// this is why StrikethroughSpanFix does not work here
			if(spans.length != 0)
			{
				int sstart = 0;
				int ssend = 0;
				if(spans[0].getClass() == StrikethroughSpanFix.class)
				{
					StrikethroughSpanFix spanf = (StrikethroughSpanFix)spans[0];
					sstart = spanf.start;
					
					// only last character has the strikethroughFix span while typing new characters 
					// because the span of other formatted characters
					// in the same line spans the whole line and hides the span which is placed over the new characters
					// solution: use a loop or fix the non-working SPAN_EXCLUSIVE_EXCLUSIVE
					if(spanf.end >= start -1)
					{
						ssend = start; //spanf.end;	
					
						text.removeSpan(spans[0]);
						text.setSpan(new StrikethroughSpanFix(sstart,ssend), sstart, ssend, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					}
					else 
					{
						ssend = spanf.end; //spanf.end;	
						text.removeSpan(spans[0]);
						text.setSpan(new StrikethroughSpanFix(sstart,ssend), sstart, ssend, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
						
						text.setSpan(new StrikethroughSpanFix(start-1,start), start-1, start, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					}		
				}		
				else if(spans[0].getClass() == StrikethroughSpan.class)
				{
					sstart = text.getSpanStart(spans[0]);
					ssend = start;//text.getSpanEnd(spans[0]);
					
					text.removeSpan(spans[0]);
					text.setSpan(new StrikethroughSpanFix(sstart,ssend), sstart, ssend, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
				}
			}
			else
				text.setSpan(new StrikethroughSpanFix(start-1,start), start-1, start, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
		}
//			if(text.getSpans(start-2, start-1, StrikethroughSpan.class).length != 0)
//			{
//				StrikethroughSpan[] spans = text.getSpans(start-2, start-1, StrikethroughSpan.class);
//				text.setSpan(spans[0], text.getSpanStart(spans[0]), start, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//			}
			
		
		if(!btnToggleStrikethrough.isChecked() && text.getSpans(start-1, start, StrikethroughSpan.class).length > 0)
		{
			StrikethroughSpan[] spans = text.getSpans(start-1, start, StrikethroughSpan.class);
			for(int i = 0; i<spans.length; ++i)
			{
				// bug: the spanEnd seems to include all text including start, even if the span [start-1,start] is not formatted 
				//Log.i("MyTag","getSpanStart: " + text.getSpanStart(spans[i]) + " getSpanEnd: " + text.getSpanEnd(spans[i]));
				StrikethroughSpanFix fix = (StrikethroughSpanFix)spans[i];
				//Log.i("MyTag","fix start: " + fix.start + " fix end: " + fix.end);
				
				
				// TODO create a new object every time?
				int sstart = fix.start;
				int ssend = fix.end;
				text.removeSpan(spans[i]);
					text.setSpan(new StrikethroughSpanFix(sstart,ssend), sstart , ssend, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
					
//				if(text.getSpanEnd(spans[i]) > start-1) // move span end to the left
//				{
//					text.setSpan(spans[i], text.getSpanStart(spans[i]) , start-1, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//				}
//				if(text.getSpanStart(spans[i]) < start) // move span start to the right
//				{
//					text.setSpan(new StrikethroughSpan(), start, text.getSpanEnd(spans[i]), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//				}
				
				//Log.i("MyTag","2: getSpanStart: " + text.getSpanStart(spans[i]) + " getSpanEnd: " + text.getSpanEnd(spans[i]));
			}
			
		}
		
		lastSelectionStart = start;
		//Log.i("MyTag", "afterTextChanged selection start:" + Selection.getSelectionStart(editText.getText()));
	}

	// useless method because parameters are unpredictable
	public void beforeTextChanged(CharSequence s, int start, int count,
			int after) {
		
		//Log.i("MyTag", "beforeTextChanged");
	}

	// useless method because parameters are unpredictable
	// does not detect if formatting has changed, useless method
	public void onTextChanged(CharSequence s, int start, int before, int count) 
	{
		//Log.i("MyTag", "onTextChanged");
		//Log.i("MyTag", "Start: " + start + " Count: " + count + " Before: " + before);
//		Editable text = editText.getText();
//		if(btnToggleStrikethrough.isChecked() && text.getSpans(start, start+count, StrikethroughSpan.class).length == 0)
//		{
//			editText.getText().setSpan(new StrikethroughSpan(), start, start+count, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);		
//		}
//		else if(!btnToggleStrikethrough.isChecked())
//		{
//			StrikethroughSpan[] spans = text.getSpans(start, start+count, StrikethroughSpan.class);
//			for(int i = 0; i<spans.length; ++i)
//			{
		// errors here
//				if(text.getSpanStart(spans[i]) < start)
//				{
//					text.setSpan(new StrikethroughSpan(),text.getSpanStart(spans[i]) , start, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//				}
//				if(start+count < text.getSpanEnd(spans[i]))
//				{
//					text.setSpan(new StrikethroughSpan(),start+count,text.getSpanEnd(spans[i]), Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
//				}
//				text.removeSpan(spans[i]);
//			}
//		}
		
		//editText.getText().setSpan(new Watcher(), start, end, )
		//Log.i("MyTag", "Length of CharSequence s: " + s.length());
	}

	public CompoundButton getBtnToggleBold() {
		return btnToggleBold;
	}

	public void setBtnToggleBold(CompoundButton btnToggleBold) {
		this.btnToggleBold = btnToggleBold;
		this.btnToggleBold.setOnClickListener(
				new Button.OnClickListener()
		{
			public void onClick(View v) {
				styleSpanFormat(android.graphics.Typeface.BOLD,TextFormatter.this.btnToggleBold.isChecked()); 				
			}
		});
	}

	public CompoundButton getBtnToggleItalic() {
		return btnToggleItalic;
	}

	public void setBtnToggleItalic(CompoundButton btnToggleItalic) {
		this.btnToggleItalic = btnToggleItalic;
		this.btnToggleItalic.setOnClickListener(
				new Button.OnClickListener()
		{
			public void onClick(View v) {
					 styleSpanFormat(android.graphics.Typeface.ITALIC, TextFormatter.this.btnToggleItalic.isChecked()); 				
			}
		});
	}

	public CompoundButton getBtnToggleUnderline() {
		return btnToggleUnderline;
	}

	public void setBtnToggleUnderline(CompoundButton btnToggleUnderline) {
		this.btnToggleUnderline = btnToggleUnderline;
		this.btnToggleUnderline.setOnClickListener(
				new Button.OnClickListener()
		{
			public void onClick(View v) {
					 characterStyleFormat(new UnderlineSpan(),TextFormatter.this.btnToggleUnderline.isChecked()); 
				}
			
		});
	}

	public CompoundButton getBtnToggleStrikethrough() {
		return btnToggleStrikethrough;
	}

	public void setBtnToggleStrikethrough(CompoundButton btnToggleStrikethrough) {
		this.btnToggleStrikethrough = btnToggleStrikethrough;
		this.btnToggleStrikethrough.setOnClickListener(
				new Button.OnClickListener()
		{
			public void onClick(View v) {
					 characterStyleFormat(new StrikethroughSpan(),TextFormatter.this.btnToggleStrikethrough.isChecked()); 
			}
		});
	}

	public CustomEditText getEditText() {
		return editText;
	}

	public void setEditText(CustomEditText editText) {
		this.editText = editText;
	}
}
