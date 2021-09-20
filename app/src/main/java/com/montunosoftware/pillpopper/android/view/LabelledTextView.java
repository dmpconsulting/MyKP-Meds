package com.montunosoftware.pillpopper.android.view;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.montunosoftware.mymeds.R;

public class LabelledTextView extends LinearLayout
{
	private TextView _label;
	private TextView _value;
	private View _layout;
	private ImageView _arrowIcon;
	
	public LabelledTextView(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		_init(context, attrs);
	}

	public LabelledTextView(Context context)
	{
		super(context);
		_init(context, null);
	}

	private void _init(Context context, AttributeSet attrs)
	{
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		inflater.inflate(R.layout.labelled_text, this);

		_layout = this.findViewById(R.id.labelled_text_root);
		_value = this.findViewById(R.id.labelled_text_value);
		_label = this.findViewById(R.id.labelled_text_label);
		_arrowIcon = this.findViewById(R.id.labelled_text_image);

		if (attrs != null) {
			TypedArray a = getContext().obtainStyledAttributes(attrs, R.styleable.LabelledTextView);

			CharSequence label;
			
			label = a.getString(R.styleable.LabelledTextView_label);
			if (label != null)
				setLabel(label);
			
			label = a.getString(R.styleable.LabelledTextView_value);
			if (label != null)
				setValue(label);

			if (a.getBoolean(R.styleable.LabelledTextView_noValue, false))
				setValue(null);
			
			if (a.getBoolean(R.styleable.LabelledTextView_nav, false)) {
				setNavIcon();
			}
			
			a.recycle();
		}
	}
	
	public void setNavIcon()
	{
		_arrowIcon.setImageResource(R.drawable.nxt_arw_unfocus);
	}
	
	@Override
	public void setOnClickListener(OnClickListener listener)
	{
		_layout.setOnClickListener(listener);
	}

	public void setLabelAndValue(CharSequence label, CharSequence value)
	{
		setLabel(label);
		setValue(value);
	}

	public void setLabel(CharSequence value)
	{
		_label.setText(value);
	}

	public void setValue(CharSequence charSequence)
	{
		_value.setText(charSequence);
	}

	public void setValue(boolean onOff)
	{
		if (onOff == true)
			setValue(this.getResources().getString(R.string._on));
		else
			setValue(this.getResources().getString(R.string._off));
	}

	public String getValue()
	{
		return _value.getText().toString().trim();
	}

	public String getLabel()
	{
		return _label.getText().toString();
	}

	public void setEnabled(boolean enabled)
	{
		super.setEnabled(enabled);
		_layout.setEnabled(enabled);
		_arrowIcon.setVisibility(enabled ? View.VISIBLE : View.GONE);		
	}
	
	public void increaseLableTextSize(float size){
		_label.setTextSize(size);
		_value.setTextSize(size);
	}
}
