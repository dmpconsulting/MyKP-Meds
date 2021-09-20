package com.montunosoftware.pillpopper.android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.montunosoftware.mymeds.R;

public class LabelledToggleView extends LinearLayout
{
	private TextView _label;
	private ToggleButton _toggle;
	private boolean _isChecked = false;
	private OnCheckedChangeListener _listener;
	
	public LabelledToggleView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		_init(context, attrs);
	}

	public LabelledToggleView(Context context)
	{
		super(context);
		_init(context, null);
	}

	private void _init(Context context, AttributeSet attrs)
	{
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.labelled_toggle, this);

		_label = this.findViewById(R.id.labelled_toggle_label);
		_toggle = this.findViewById(R.id.labelled_toggle_toggle);
		
		// togglebuttons apparently have an annoying bug - when you return to an activity that has one,
		// they get spurious onCheckedChangeListener notifications.  Workaround is to keep track of the
		// state ourselves and only use OnClickListener.
		// http://stackoverflow.com/questions/10658059/can-oncheckedchanged-method-be-called-with-no-interaction
		_toggle.setOnClickListener(v -> {
            setValue(!_isChecked);

            if (_listener != null) {
                _listener.onCheckedChanged(_toggle, _isChecked);
            }
        });

		if (attrs != null) {
			TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LabelledTextView);
			CharSequence label = a.getString(R.styleable.LabelledTextView_label);
			if (label != null)
				setLabel(label);
			a.recycle();
		}
	}
	
	public void setLabel(CharSequence value)
	{
		_label.setText(value);
	}

	public String getLabel()
	{
		return _label.getText().toString();
	}

	public void setOnCheckedChangeListener(OnCheckedChangeListener listener)
	{
		_listener = listener;
	}
	
	public void setValue(boolean isChecked)
	{
		_isChecked = isChecked;
		_toggle.setChecked(_isChecked);
	}
}


