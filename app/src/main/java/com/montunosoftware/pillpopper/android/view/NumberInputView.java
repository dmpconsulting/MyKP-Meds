package com.montunosoftware.pillpopper.android.view;


import android.content.Context;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.montunosoftware.mymeds.R;

public class NumberInputView extends LinearLayout
{
	private interface DigitChangedListener {
		void onDigitChanged();
	}

	public interface NumberChangedListener {
		void onNumberChanged(double newNumber);
	}

	private class DigitState
	{
		private int _digitValue;
		private TextView _digitView;
		private DigitChangedListener _listener;
		public DigitState(View selectorView)
		{
			final DigitState _digitState = this;
			
			_digitView = selectorView.findViewById(R.id.digit_view);
			
			Button b;
			b = selectorView.findViewById(R.id.digit_up);
			b.setOnClickListener(v -> _digitState.incrementValue(1));
			
			b = selectorView.findViewById(R.id.digit_down);
			b.setOnClickListener(v -> _digitState.incrementValue(-1));
		}
		
		protected void setDigitChangedListener(DigitChangedListener listener)
		{
			_listener = listener;
		}
		
		protected void incrementValue(int delta)
		{
			setValue(_digitValue + delta);
		}

		public void setValue(int d)
		{
			_digitValue = (d + 10) % 10; // add 10 so -1 rolls over to 9
			_digitView.setText(String.format("%d", _digitValue));
			
			if (_listener != null)
				_listener.onDigitChanged();
		}
		
		public int getDigit()
		{
			return _digitValue;
		}
	}

	private final LayoutInflater _inflater = (LayoutInflater) (getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE));
	private DigitState[] _digits = null;
	private int _wholeDigits = 0, _fracDigits = 0;
	private NumberChangedListener _listener;
	private final NumberInputView thisView = this;
	
	public NumberInputView(Context context)
	{
		super(context);
		_initComponent(context);
	}

	public NumberInputView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		_initComponent(context);
	}

	private void _initComponent(Context context)
	{
		_inflater.inflate(R.layout.spinner_background, this);
		
		// Hook up the reset button
		if (!isInEditMode()) {
			Button b = this.findViewById(R.id.spinner_reset_button);
			b.setOnClickListener(v -> thisView.setNumber(0));
		}
		
		// Display a test pattern if we're in the GUI editor
		if (isInEditMode()) {
			configurePicker(2, 0);
			setNumber(123456.9872);
		}
	}
	
	public void configurePicker(int wholeDigits, int fracDigits)
	{
		_wholeDigits = wholeDigits;
		_fracDigits = fracDigits;
		int totalDigits = wholeDigits + fracDigits;
		_digits = new DigitState[totalDigits];
		
		LinearLayout numberContainer = this.findViewById(R.id.spinner_number_container);
		
		for (int i = 0; i < totalDigits; i++) {
			// Add a decimal point when we get to the boundary between whole
			// and fractional digits
			if (i == wholeDigits) {
				View v = _inflater.inflate(R.layout.spinner_decimal, null);
				numberContainer.addView(v);
			}
				
			// Add a digit
			View v = _inflater.inflate(R.layout.spinner_digit, null);
			numberContainer.addView(v);
			_digits[i] = new DigitState(v);
			_digits[i].setDigitChangedListener(() -> _digitChanged());
		}
	}

	public void setNumber(double number)
	{
		if (_digits == null)
			return;
		
		long intNumber = Math.round(number * Math.pow(10.0, _fracDigits));
		int totalDigits = _wholeDigits + _fracDigits;
		
		for (int i = 0; i < totalDigits; i++) {
			int nextDigit = (int) (intNumber % 10);
			intNumber /= 10;
			_digits[totalDigits - i - 1].setValue(nextDigit);
		}
	}
	
	public void setResetVisibility(boolean isVisible)
	{
		this.findViewById(R.id.spinner_reset_button).setVisibility(isVisible ? View.VISIBLE : View.GONE);
	}
	
	public void setNumberChangedListener(NumberChangedListener listener)
	{
		_listener = listener;
	}
	    

	private void _digitChanged()
	{
		int intTotal = 0;
		
		// Compute the new number assuming no decimal point
		for (int i = 0; i < (_wholeDigits + _fracDigits); i++) {
			intTotal *= 10;
			intTotal += _digits[i].getDigit();
		}
		
		// Now shift the decimal point
		double floatTotal = ((double) intTotal) / Math.pow(10.0, _fracDigits);
		
		if (_listener != null) {
			_listener.onNumberChanged(floatTotal);
		}
	}
	

}
