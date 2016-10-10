/**
 * 
 */
package com.taiko.noblenote;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputFilter;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;


/**
 * @author Taiko-G780
 *
 */
public class RenameDialogActivity extends Activity
{
	public final static String ARG_FILE_NAME = "file_name";
    
	
	 @Override
	    protected void onCreate(Bundle savedInstanceState) {
	        super.onCreate(savedInstanceState);
	        setContentView(R.layout.rename_dialog_activity);
	        final EditText editText = (EditText)findViewById(R.id.rename_dialog_edit_text);

	        String fileName = getIntent().getStringExtra(ARG_FILE_NAME);
	        editText.setText(fileName);
	        
	        editText.setFilters(new InputFilter[]{new FileNameFilter()});
	        
	        //final Pattern p = Pattern.compile("[" + Pattern.quote("\\^/?<>:*|\"") + "]");
	        
	        // setting a on click method via xml did not work
	        Button ok = (Button)findViewById(R.id.ok);
	        ok.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					Intent result = new Intent();
					result.putExtra(ARG_FILE_NAME, editText.getText().toString());
					setResult(RESULT_OK,result);		 
					finish();
				}
			});
	        
	        Button cancel = (Button)findViewById(R.id.cancel);
	        cancel.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					setResult(RESULT_CANCELED);		 
					finish();
				}
			});       
	 }
}
	 
	 
