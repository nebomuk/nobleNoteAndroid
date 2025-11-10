package com.blogspot.noblenoteandroid.deprecated;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.EditText;

public class CustomEditText extends EditText {

	private OnSelectionChangedListener onSelectionChangedListener;	
	
	public CustomEditText(Context context) {
		super(context);
		// TODO Auto-generated constructor stub
	}

	public CustomEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		// TODO Auto-generated constructor stub
	}

	public CustomEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		// TODO Auto-generated constructor stub
	}
	
	public void setOnSelectionChangedListener(OnSelectionChangedListener listener)
	{
		this.onSelectionChangedListener = listener;
	}
	
	// this method is called by the superclass constructor
	@Override
	protected	void onSelectionChanged(int selStart, int selEnd)
	{
		super.onSelectionChanged(selStart, selEnd);
		if(onSelectionChangedListener != null)
			onSelectionChangedListener.onSelectionChanged(selStart, selEnd);	
	}
	
	public interface OnSelectionChangedListener
	{
		void onSelectionChanged(int selStart, int selEnd);
	}

}
