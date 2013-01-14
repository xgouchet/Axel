package fr.xgouchet.xmleditor.ui.widget;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.EditText;
import fr.xgouchet.xmleditor.R;

public class ClearableEditText extends EditText {

	public ClearableEditText(Context context) {
		super(context);
		clearInit(context);
	}

	public ClearableEditText(Context context, AttributeSet attrs) {
		super(context, attrs);
		clearInit(context);
	}

	public ClearableEditText(Context context, AttributeSet attrs, int defStyle) {
		super(context, attrs, defStyle);
		clearInit(context);
	}

	/**
	 * @see android.widget.TextView#setError(java.lang.CharSequence)
	 */
	public void setError(CharSequence error) {
		if (TextUtils.isEmpty(getText())) {
			super.setError(error);
		} else {
			setError(error, mClearDrawable);
		}
	}

	/**
	 * Initialise the clear button
	 * @param context
	 */
	private void clearInit(Context context) {
		mClearDrawable = context.getResources()
				.getDrawable(R.drawable.ic_clear);
		mClearDrawable.setBounds(0, 0, mClearDrawable.getIntrinsicWidth(),
				mClearDrawable.getIntrinsicHeight());
		addTextChangedListener(mClearTextWatcher);
		setOnTouchListener(mClearTouchListener);
	}

	/**
	 * 
	 */
	private void setClearButtonVisible(boolean visible) {
		Drawable compound[] = getCompoundDrawables();
		if (visible) {
			setCompoundDrawables(compound[0], compound[1], mClearDrawable,
					compound[3]);
		} else {
			setCompoundDrawables(compound[0], compound[1], null, compound[3]);
		}
	}

	private Drawable mClearDrawable;

	private TextWatcher mClearTextWatcher = new TextWatcher() {
		public void onTextChanged(CharSequence s, int start, int before,
				int count) {
			if (TextUtils.isEmpty(getText())) {
				setClearButtonVisible(false);
			} else {
				setClearButtonVisible(true);
			}
		}

		public void beforeTextChanged(CharSequence s, int start, int count,
				int after) {
		}

		public void afterTextChanged(Editable s) {
		}
	};

	private OnTouchListener mClearTouchListener = new OnTouchListener() {
		public boolean onTouch(View v, MotionEvent event) {
			if (getCompoundDrawables()[2] == null) {
				return false;
			}

			if (event.getAction() != MotionEvent.ACTION_UP) {
				return false;
			}

			if (event.getX() > (getWidth() - getPaddingRight() - mClearDrawable
					.getIntrinsicWidth())) {
				setText("");
				setClearButtonVisible(false);
			}
			return false;
		}
	};
}
