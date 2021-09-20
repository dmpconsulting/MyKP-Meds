package com.montunosoftware.pillpopper.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.Window;
import android.widget.TextView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.view.NumberInputView;

public class InputNumberActivity extends PillpopperActivity
{
	private static final String _KEY_TITLE = "KEY_TITLE";
	private static final String _KEY_HELP_TEXT = "KEY_HELP_TEXT";
	private static final String _KEY_LABEL = "KEY_LABEL";
	private static final String _KEY_INITIAL_NUMBER = "KEY_INITIAL_NUMBER";
	private static final String _KEY_WHOLE_DIGITS = "KEY_WHOLE_DIGITS";
	private static final String _KEY_FRAC_DIGITS = "KEY_FRAC_DIGITS";
	private static final String _KEY_CUSTOM_FORMAT = "KEY_CUSTOM_FORMAT";

	private static final String _KEY_SAVED_NUMBER = "KEY_SAVED_NUMBER";

	private static final String _KEY_RETURN_VALUE = "KEY_RETURN_VALUE";

	private double _currentNumber;
	private String _formatString;
	private TextView _valueText;

	public static void selectNumber(
			PillpopperReplyContext context,
			String title,
			String label,
			double initialNumber,
			int wholeDigits,
			int fracDigits,
			String customFormatString,
			int helpTextId,
			int resultCode)
	{
		Intent i = new Intent(context.getAndroidContext(), InputNumberActivity.class);
		i.putExtra(_KEY_TITLE, title);
		i.putExtra(_KEY_LABEL, label);
		i.putExtra(_KEY_INITIAL_NUMBER, initialNumber);
		i.putExtra(_KEY_WHOLE_DIGITS, wholeDigits);
		i.putExtra(_KEY_FRAC_DIGITS, fracDigits);
		i.putExtra(_KEY_CUSTOM_FORMAT, customFormatString);
		i.putExtra(_KEY_HELP_TEXT, context.getAndroidResources().getString(helpTextId));
		context.startActivityForResult(i, resultCode);
	}

	public static double getReturnValue(Intent intent)
	{
		return intent.getDoubleExtra(_KEY_RETURN_VALUE, 0.0);
	}

	///////////

	private void _updateView()
	{
		_valueText.setText(String.format(_formatString, _currentNumber));
	}

	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		final InputNumberActivity thisActivity = this;
		this.setContentView(R.layout.number_input_dialog);

		// Get the reference to the value label
		_valueText = this.findViewById(R.id.number_input_value);

		// Set the title of the dialog box
		TextView title = this.findViewById(R.id.number_input_title);
		title.setText(this.getIntent().getStringExtra(_KEY_TITLE));

		// Set the help text
		TextView helpText = this.findViewById(R.id.number_input_help);
		helpText.setText(this.getIntent().getStringExtra(_KEY_HELP_TEXT));

		// Set the label text
		TextView label = this.findViewById(R.id.number_input_label);
		label.setText(this.getIntent().getStringExtra(_KEY_LABEL));

		// Done button
		setSaveButtons(R.id.up_button, R.id.done_button, v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(_KEY_RETURN_VALUE, _currentNumber);
            thisActivity.setResult(RESULT_OK, resultIntent);
            thisActivity.finish();
        });

		if (bundle == null) {
			_currentNumber = this.getIntent().getDoubleExtra(_KEY_INITIAL_NUMBER, 0.0);
		} else {
			_currentNumber = bundle.getDouble(_KEY_SAVED_NUMBER);
		}
		NumberInputView numPicker = this.findViewById(R.id.number_input_number_spinner);
		numPicker.configurePicker(
				this.getIntent().getIntExtra(_KEY_WHOLE_DIGITS, 0),
				this.getIntent().getIntExtra(_KEY_FRAC_DIGITS, 0)
				);
		numPicker.setNumber(_currentNumber);
		numPicker.setNumberChangedListener(newNumber -> {
            _currentNumber = newNumber;
            thisActivity._updateView();
        });

		_formatString = this.getIntent().getStringExtra(_KEY_CUSTOM_FORMAT);

		// If no format string was provided, create a default format string like
		// "%.3f", where "3" is the number of frac digits
		if (_formatString == null) {
			_formatString = String.format("%%.%df", this.getIntent().getIntExtra(_KEY_FRAC_DIGITS, 1));
		}

		_updateView();
	}


	@Override
	public void onSaveInstanceState(Bundle outState)
	{
		super.onSaveInstanceState(outState);

		outState.putDouble(_KEY_SAVED_NUMBER, _currentNumber);
	}


}
