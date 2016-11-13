/**
 * 
 */
package com.taiko.noblenote;

import android.text.InputFilter;
import android.text.SpannableStringBuilder;
import android.text.Spanned;

/**
 * @author Taiko-G780
 *
 */
public class FileNameFilter implements InputFilter {
	
	boolean isAllowedChar(char c)
	{
		//Matcher m = p.matcher(CharBuffer.wrap(new char[]{c}));
		//return !m.matches();
		
		// this is probably faster
		return !(c == '\\' || c == '^' || c == '/' || c == '?' || c== '<' || c== '>' || c == ':' || c== '*' || c == '|' || c == '\"');
	}
			
	
    @Override
    public CharSequence filter(CharSequence source, int start, int end,
            Spanned dest, int dstart, int dend) {
    	 if (source instanceof SpannableStringBuilder) {
             SpannableStringBuilder sourceAsSpannableBuilder = (SpannableStringBuilder)source;
             for (int i = end - 1; i >= start; i--) { 
                 char currentChar = source.charAt(i);
                  if (!isAllowedChar(currentChar)) 
                  {    
                      sourceAsSpannableBuilder.delete(i, i+1);
                  }     
             }
             return source;
         } else {
             StringBuilder filteredStringBuilder = new StringBuilder();
             for (int i = 0; i < end; i++) { 
                 char currentChar = source.charAt(i);
                 if (isAllowedChar(currentChar))
                 {    
                     filteredStringBuilder.append(currentChar);
                 }     
             }
             return filteredStringBuilder.toString();
         }
    }
}
