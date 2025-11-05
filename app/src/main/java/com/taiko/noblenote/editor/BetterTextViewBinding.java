package com.taiko.noblenote.editor;

import android.graphics.drawable.Drawable;
import android.os.Build;
import android.text.Editable;
import android.text.InputFilter;
import android.text.InputType;
import android.text.Spanned;
import android.text.TextWatcher;
import android.text.method.DialerKeyListener;
import android.text.method.DigitsKeyListener;
import android.text.method.KeyListener;
import android.text.method.PasswordTransformationMethod;
import android.text.method.TextKeyListener;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import androidx.databinding.BindingAdapter;
import androidx.databinding.InverseBindingAdapter;
import androidx.databinding.InverseBindingListener;
import androidx.databinding.adapters.ListenerUtil;
import androidx.databinding.adapters.TextViewBindingAdapter;

import com.taiko.noblenote.R;


/**
 * based on the androidx TextViewBindingAdapter
 * charSequenceText binding returns CharSequence instead of string to preserve formatting
 * does not call observers (LiveData) when text is set programmatically
 */
public class BetterTextViewBinding {

    @BindingAdapter("onFocusChange")
    public static void onFocusChange(EditText text, final View.OnFocusChangeListener listener) {
        text.setOnFocusChangeListener(listener);
    }

    private static Boolean textWatcherEnabled = true;

    @BindingAdapter("charSequenceText")
    public static void setText(TextView view, CharSequence text) {
        final CharSequence oldText = view.getText();
        if (text == oldText || (text == null && oldText.length() == 0)) {
            return;
        }
        if (text instanceof Spanned) {
            if (text.equals(oldText)) {
                return; // No change in the spans, so don't set anything.
            }
        } else if (!haveContentsChanged(text, oldText)) {
            return; // No content changes, so don't set anything.
        }
        textWatcherEnabled = false;
        view.setText(text);
        view.postDelayed(new Runnable() {
            @Override
            public void run() {
                textWatcherEnabled = true;
            }
        },0);
    }

    @InverseBindingAdapter(attribute = "charSequenceText", event = "textAttrChanged")
    public static CharSequence getTextCharSequence(TextView view) {
        return view.getText();
    }


    private static boolean haveContentsChanged(CharSequence str1, CharSequence str2) {
        if ((str1 == null) != (str2 == null)) {
            return true;
        } else if (str1 == null) {
            return false;
        }
        final int length = str1.length();
        if (length != str2.length()) {
            return true;
        }
        for (int i = 0; i < length; i++) {
            if (str1.charAt(i) != str2.charAt(i)) {
                return true;
            }
        }
        return false;
    }

    @BindingAdapter(value = {"beforeTextChanged", "onTextChanged", "afterTextChanged", "textAttrChanged"}, requireAll = false)
    public static void setTextWatcher(TextView view,
                                      final BetterTextViewBinding.BeforeTextChanged before,
                                      final BetterTextViewBinding.OnTextChanged on,
                                      final BetterTextViewBinding.AfterTextChanged after,
                                      final InverseBindingListener textAttrChanged)
    {


        final TextWatcher newValue;
        if (before == null && after == null && on == null && textAttrChanged == null) {
            newValue = null;
        } else {
            newValue = new TextWatcher() {
                @Override
                public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                    if(!textWatcherEnabled)
                    {
                        return;
                    }

                    if (before != null) {
                        before.beforeTextChanged(s, start, count, after);
                    }
                }

                @Override
                public void onTextChanged(CharSequence s, int start, int before, int count) {
                    if(!textWatcherEnabled)
                    {
                        return;
                    }
                    if (on != null) {
                        on.onTextChanged(s, start, before, count);
                    }
                    if (textAttrChanged != null) {
                        textAttrChanged.onChange();
                    }
                }

                @Override
                public void afterTextChanged(Editable s) {
                    if(!textWatcherEnabled)
                    {
                        return;
                    }
                    if (after != null) {
                        after.afterTextChanged(s);
                    }
                }
            };
        }
        final TextWatcher oldValue = ListenerUtil.trackListener(view, newValue, R.id.textWatcher);
        if (oldValue != null) {
            view.removeTextChangedListener(oldValue);
        }
        if (newValue != null) {
            view.addTextChangedListener(newValue);
        }
    }

    public interface AfterTextChanged {
        void afterTextChanged(Editable s);
    }

    public interface BeforeTextChanged {
        void beforeTextChanged(CharSequence s, int start, int count, int after);
    }

    public interface OnTextChanged {
        void onTextChanged(CharSequence s, int start, int before, int count);
    }
}
