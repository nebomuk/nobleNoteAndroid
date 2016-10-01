package com.taiko.noblenote;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.Snackbar;
import android.support.v4.app.NavUtils;
import android.text.InputType;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;
import android.widget.Toolbar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;


public class NoteEditorActivity extends Activity {

	public static final String ARG_FILE_PATH = "file_path";
	public static final String ARG_OPEN_MODE = "open_mode";
	public static final String HTML = "html";
	public static final String READ_WRITE = "read_write";
	public static final String READ_ONLY = "read_only";
	
	private String filePath = "";
	private boolean focusable = true; // if set to false, the note is opened in read only mode
	private String openMode;
	private View editorScrollView;
	private DroidWriterEditText editText;
	private float displayDensity;
	private long lastModified = 0;
	private View mLayoutRoot;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		 //requestWindowFeature(Window.FEATURE_ACTION_BAR_OVERLAY);
		
		super.onCreate(savedInstanceState);		
		displayDensity = getApplicationContext().getResources().getDisplayMetrics().density;;
		//This has to be called before setContentVie

		getWindow().requestFeature(Window.FEATURE_ACTION_MODE_OVERLAY);
		setContentView(R.layout.note_editor_activity);
		mLayoutRoot = findViewById(R.id.layout_root);

		editorScrollView = findViewById(R.id.editor_scroll_view);
		editorScrollView.setVisibility(View.INVISIBLE);		
		editText = (DroidWriterEditText) findViewById(R.id.editor_edit_text);

		setActionBar((Toolbar) findViewById(R.id.toolbar));
				
		// hide soft keyboard by default
		getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
		
		Bundle extras = getIntent().getExtras();
		if(extras == null)
			return;
		
		filePath = extras.getString(NoteEditorActivity.ARG_FILE_PATH);
		openMode = extras.getString(NoteEditorActivity.ARG_OPEN_MODE);
		focusable = !(openMode.equals(HTML) || openMode.equals(READ_ONLY)); // no editing if html source should be shown
		
		//getActionBar().setTitle(new File(filePath).getName());

		lastModified = new File(filePath).lastModified();
		reload();
		
	}
	
//	@Override
//	public void onStart()
//	{
//		super.onStart();
//
//	}

	@SuppressLint("NewApi")
    @Override
    public void onResume()
    {
		super.onResume();
		
		if(new File(filePath).lastModified() > lastModified)
		{
			reload();
			lastModified = new File(filePath).lastModified();
			Snackbar.make(mLayoutRoot, R.string.noteReloaded, Snackbar.LENGTH_SHORT).show();
		}
		
			// fix selection & formatting for Honeycomb and newer devices
			if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB)
			{
				DroidWriterEditText editText = (DroidWriterEditText) findViewById(R.id.editor_edit_text);
				editText.setCustomSelectionActionModeCallback(new SelectionActionModeCallback(editText));
			}
			
//			long end1 = System.currentTimeMillis();
//			Log.d("MyTag","Execution time fromHtml was "+(end1-start1)+" ms.");
//			long start = System.currentTimeMillis();
//			
//			long end = System.currentTimeMillis();
//			Log.d("MyTag","Execution time  setText was "+(end-start)+" ms.");
//			//view.setText(Html.fromHtml(html));
    	
    }
	
	/**
	 * reloads the current note file
	 */
	private void reload()
	{
		
		// simple timer controlled progress reporting
		final Handler mHandler = new Handler();
		final Runnable mProgressRunner = new Runnable() {
			private int mProgress;
			public void run() {
				mProgress += 8;

				//Normalize our progress along the progress bar's scale
				int progress = (Window.PROGRESS_END - Window.PROGRESS_START) / 100 * mProgress;
				setProgress(progress);

				if (mProgress < 100) {
					mHandler.postDelayed(this, 10);
				}
			}};
			mProgressRunner.run();


			// load file contents and parse html thread
			new Thread(){
				public void run()
				{					

					StringBuilder htmlText = new StringBuilder();			
					try {
						BufferedReader br = new BufferedReader(new FileReader(filePath));
						String line;

						while ((line = br.readLine()) != null) {
							htmlText.append(line);
							htmlText.append('\n');
						}
						br.close();
					}
					catch (IOException e) {
						htmlText.append(e.getMessage());
						//You'll need to add proper error handling here
					}

					// do slow html parsing
					final CharSequence span; 
					if(openMode.equals(HTML))
					{
						span = htmlText.toString();
					}
					else
					{
						span = Html.fromHtml(htmlText.toString(),displayDensity); // time consuming
					}

					// add the span to the text editor as soon as it is created
					NoteEditorActivity.this.runOnUiThread(new Runnable(){
						

						public void run()
						{
							editText.setText(span);
							editText.setMovementMethod(new ArrowKeyLinkMovementMethod());
							mHandler.removeCallbacks(mProgressRunner);
							setProgress(Window.PROGRESS_END);
							editorScrollView.setVisibility(View.VISIBLE);
							editText.setModified(false); // reset modification state because modification flag has been set by editText.setText
						}
					});
				}
			}.start();
		
	}
	
	@Override
	public void onPause()
	{
		super.onPause();
	    DroidWriterEditText editText = (DroidWriterEditText) findViewById(R.id.editor_edit_text);
		
	    // does nothing if open mode is set to read only
	    
	    // if not focusable, changes can not be made
	    if(focusable && editText.isModified()) // then save the note 
	    {
			String html = editText.getTextHTML();
			File file = new File(filePath);
			try {
	        FileWriter writer = new FileWriter(file); 
			writer.append(html);
	        writer.flush();
	        writer.close();
	        editText.setModified(false);
	        lastModified = file.lastModified();
			Toast.makeText(this.getApplicationContext(), R.string.noteSaved, Toast.LENGTH_SHORT).show();
			} catch (IOException e) {
				e.printStackTrace();
			}
	    }
	}
	
	 @Override
	    public boolean onOptionsItemSelected(MenuItem item) {
	        if (item.getItemId() == android.R.id.home) {
	            NavUtils.navigateUpTo(this, new Intent(this, MainActivity.class));
	            return true;
	        }

	        return super.onOptionsItemSelected(item);
	    }
	
	 @Override
	    public boolean onCreateOptionsMenu(Menu menu) {
		 
		 
		 MenuItem itemCopyToClipboard = menu.add("Copy To Clipboard").setIcon(R.drawable.ic_action_content_copy)
				 .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		 itemCopyToClipboard.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			
			@SuppressLint("NewApi")
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				if(android.os.Build.VERSION.SDK_INT < android.os.Build.VERSION_CODES.HONEYCOMB) {
				    android.text.ClipboardManager clipboard = (android.text.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				    clipboard.setText(editText.getText());
				} else {
				    android.content.ClipboardManager clipboard = (android.content.ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
				    android.content.ClipData clip = android.content.ClipData.newPlainText("Copied Text", editText.getText());
				            clipboard.setPrimaryClip(clip);
				}
				Context context = getApplicationContext();
				CharSequence text = "Note copied to clipboard";
				int duration = Toast.LENGTH_SHORT;

				Toast toast = Toast.makeText(context, text, duration);
				toast.show();
				return true;
			}
		});
		 
		 View textFormattingToolbar = LayoutInflater.from(this).inflate(R.layout.text_formatting_toolbar, null); 
		 MenuItem item = menu.add("Toolbar").setIcon(R.drawable.ic_action_btn_show_text_formatting_toolbar)
				 .setActionView(textFormattingToolbar);
		 item.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS | MenuItem.SHOW_AS_ACTION_COLLAPSE_ACTION_VIEW);
		 
		 final DroidWriterEditText editText = (DroidWriterEditText) findViewById(R.id.editor_edit_text);
		 
		 
		 // disable auto complete if the text formatting toolbar is shown, because auto-complete's underlining interferes
		 // with the text formatting's underline
		 item.setOnActionExpandListener(new MenuItem.OnActionExpandListener() 
		 {
			@Override
			public boolean onMenuItemActionExpand(MenuItem item) {
				editText.setTextWatcherEnabled(true); 
				editText.setInputType(editText.getInputType() |  InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
				return true;
			}
			
			@Override
			public boolean onMenuItemActionCollapse(MenuItem item) {
				editText.setTextWatcherEnabled(false); // this disables "on-typing" text formatting by DroidWriterEditText
				editText.setInputType(editText.getInputType() & ~InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS);
				return true;
			}
		});
		 
		 MenuItem itemClose = menu.add("Close").setIcon(R.drawable.ic_action_close)
				 .setShowAsActionFlags(MenuItem.SHOW_AS_ACTION_IF_ROOM);
		 itemClose.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
			
			@Override
			public boolean onMenuItemClick(MenuItem item) {
				if(editText.isModified())
				{
					AlertDialog.Builder builder = new AlertDialog.Builder(NoteEditorActivity.this);
			        builder.setMessage(R.string.saveOnClose)
			               .setPositiveButton(android.R.string.yes, new DialogInterface.OnClickListener() {
			                   public void onClick(DialogInterface dialog, int id) {
			                	   editText.setModified(false);//  does not get saved
			                       finish();
			                   }
			               })
			               .setNegativeButton(android.R.string.no, new DialogInterface.OnClickListener() {
			                   public void onClick(DialogInterface dialog, int id) {
			                   }
			               });
			        // Create the AlertDialog object and return it
			        builder.create().show();
				}
				else // no modification, do not ask to save the document
				{
					editText.setModified(false);//  does not get saved
                    finish();					
				}
		        
				return true;
			}
		});
				 
		 
		 editText.setSingleLine(false);
		 editText.setTextWatcherEnabled(false);
		 
		 ToggleButton btnToggleBold = (ToggleButton) textFormattingToolbar.findViewById(R.id.btnToggleBold);
		 Spanned boldText = Html.fromHtml("<big><b>B</b></big>",displayDensity);
		 btnToggleBold.setText(boldText,TextView.BufferType.SPANNABLE);
		 btnToggleBold.setTextOn(boldText);
		 btnToggleBold.setTextOff(boldText);
		 editText.setBoldToggleButton(btnToggleBold);
		 
		 ToggleButton btnToggleItalic= (ToggleButton)textFormattingToolbar.findViewById(R.id.btnToggleItalic);
		 Spanned italicText = Html.fromHtml("<big><i>I</i></big>",displayDensity);
		 btnToggleItalic.setText(italicText,TextView.BufferType.SPANNABLE);
		 btnToggleItalic.setTextOn(italicText);
		 btnToggleItalic.setTextOff(italicText);
		 editText.setItalicsToggleButton(btnToggleItalic);
		 
		 ToggleButton btnToggleUnderline = (ToggleButton)textFormattingToolbar.findViewById(R.id.btnToggleUnderline);
		 Spanned underlinedText = Html.fromHtml("<big><u>U</u></big>",displayDensity);
		 btnToggleUnderline.setText(underlinedText,TextView.BufferType.SPANNABLE);
		 btnToggleUnderline.setTextOn(underlinedText);
		 btnToggleUnderline.setTextOff(underlinedText);
		 editText.setUnderlineToggleButton(btnToggleUnderline);
		
		 ToggleButton btnToggleStrikethrough = (ToggleButton)textFormattingToolbar.findViewById(R.id.btnToggleStrikethrough);
		 Spanned strikethroughText = Html.fromHtml("<big><s>S</s></big>",displayDensity);
		 btnToggleStrikethrough.setText(strikethroughText,TextView.BufferType.SPANNABLE);
		 btnToggleStrikethrough.setTextOn(strikethroughText);
		 btnToggleStrikethrough.setTextOff(strikethroughText);

		 
		 editText.setFocusable(focusable); // read only if not focusable
		 
		 //item.expandActionView(); // show text formatting toolbar by default

		 return super.onCreateOptionsMenu(menu);
	 }
} 