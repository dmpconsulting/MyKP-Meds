package com.montunosoftware.pillpopper.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.util.Util;

public class InputTextActivity extends PillpopperActivity
{
	private static final String _KEY_TITLE = "KEY_TITLE";
	private static final String _KEY_INITIAL_CONTENT = "KEY_INITIAL_CONTENT";
	private static final String _KEY_HELPTEXT = "KEY_HELPTEXT";
	private static final String _KEY_RETURN_VALUE = "KEY_RETURN_VALUE";
	
	public static void editText(Activity act, String title, String initialContent, String helpText, int resultCode)
	{
		_editText(act, title, initialContent, helpText, resultCode, InputTextActivity.class);
	}

	public static String getReturnValue(Intent intent)
	{
		return Util.cleanString(intent.getStringExtra(_KEY_RETURN_VALUE));
	}

	protected static void _editText(Activity act, String title, String initialContent, String helpText, int resultCode, Class<?> cls)
	{
		Intent intent = new Intent(act.getApplicationContext(), cls);
		intent.putExtra(_KEY_TITLE, title);
		intent.putExtra(_KEY_INITIAL_CONTENT, initialContent);
		intent.putExtra(_KEY_HELPTEXT,  helpText);
		
		act.startActivityForResult(intent, resultCode);
	}
	
	////////////////////
	
	@Override
	public void onCreate(Bundle bundle)
	{
		super.onCreate(bundle);

		this.setContentView(R.layout.input_text_dialog);

		// Set the title of the dialog box
		TextView title = this.findViewById(R.id.textentry_title);
		title.setText(this.getIntent().getStringExtra(_KEY_TITLE));

		// Set the help text
		TextView help = this.findViewById(R.id.textentry_help);
		help.setText(this.getIntent().getStringExtra(_KEY_HELPTEXT));
		
		// Populate the initial contents of the text entry box
		final EditText editText = this.findViewById(R.id.textentry_field);
		editText.setText(this.getIntent().getStringExtra(_KEY_INITIAL_CONTENT));
		editText.setSelection(editText.getText().length());
		Util.activateSoftKeyboard(editText);

		// Done button
		setSaveButtons(R.id.up_button, R.id.done_button, v -> {
            Intent resultIntent = new Intent();
            resultIntent.putExtra(_KEY_RETURN_VALUE, editText.getText().toString());
            _thisActivity.setResult(RESULT_OK, resultIntent);
            _thisActivity.finish();
        });
		
		// Clear button
		Button b;
		b = this.findViewById(R.id.textentry_clear_button);
		b.setOnClickListener(v -> editText.setText(null));
	}

}
