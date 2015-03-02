package com.taiko.noblenote.deprecated;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.TouchDelegate;
import android.view.View;
import android.widget.CheckBox;

/**
 * CheckBox that expands its touch area to its parent
 */
public class ExpandedCheckBox extends CheckBox {

	// Provide the same constructors as the superclass
	public ExpandedCheckBox(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		this.post(expandTouchAreaRunnable);
	}

	// Provide the same constructors as the superclass
	public ExpandedCheckBox(Context context, AttributeSet attrs) {
		super(context, attrs);
		this.post(expandTouchAreaRunnable);
		
	}

	// Provide the same constructors as the superclass
	public ExpandedCheckBox(Context context) {
		super(context);
		this.post(expandTouchAreaRunnable);
	}
	
	Runnable expandTouchAreaRunnable = new Runnable()
	{
		@Override
		public void run()
		{
			final View parent = (View)getParent();
			final View child = ExpandedCheckBox.this;
			expandTouchArea(parent,child);
		}
	};
	
	  public static void expandTouchArea(final View bigView, final View smallView) {
		  bigView.post(new Runnable() {
		      @Override
		      public void run() {
		          Rect rect = new Rect();
		          // whole parent rectangle
		          rect.top = 0;
		          rect.left = 0;
		          rect.right = bigView.getWidth();
		          rect.bottom = bigView.getHeight();
//		          smallView.getHitRect(rect);
//		          rect.top -= extraPadding;
//		          rect.left -= extraPadding;
//		          rect.right += extraPadding;
//		          rect.bottom += extraPadding;
		          bigView.setTouchDelegate(new TouchDelegate(rect, smallView));
		      }
		  });
	  }
	
//	@Override
//	public void getHitRect(Rect outRect)
//	{
//		int mPadding = 8;
//		outRect.set(getLeft() - mPadding*8, getTop() - mPadding, getRight() + mPadding*2, getBottom() + mPadding); 		
//	}

//	@Override
//	public boolean onTouchEvent(MotionEvent event) {
//		// Make the checkbox not respond to any user event
//		return false;
//	}
//
//	@Override
//	public boolean onKeyDown(int keyCode, KeyEvent event) {
//		// Make the checkbox not respond to any user event
//		return false;
//	}
//
//	@Override
//	public boolean onKeyMultiple(int keyCode, int repeatCount, KeyEvent event) {
//		// Make the checkbox not respond to any user event
//		return false;
//	}
//
//	@Override
//	public boolean onKeyPreIme(int keyCode, KeyEvent event) {
//		// Make the checkbox not respond to any user event
//		return false;
//	}
//
//	@Override
//	public boolean onKeyShortcut(int keyCode, KeyEvent event) {
//		// Make the checkbox not respond to any user event
//		return false;
//	}
//
//	@Override
//	public boolean onKeyUp(int keyCode, KeyEvent event) {
//		// Make the checkbox not respond to any user event
//		return false;
//	}
//
//	@Override
//	public boolean onTrackballEvent(MotionEvent event) {
//		// Make the checkbox not respond to any user event
//		return false;
//	}
}
