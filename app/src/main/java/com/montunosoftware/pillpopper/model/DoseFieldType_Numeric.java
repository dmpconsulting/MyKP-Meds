package com.montunosoftware.pillpopper.model;

import android.content.Intent;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.InputNumberActivity;
import com.montunosoftware.pillpopper.android.PillpopperActivity;
import com.montunosoftware.pillpopper.android.util.Util;

import org.json.JSONObject;

//
// This class implements dosage fields that are pure numeric (e.g. number of pills, number of puffs).
//

public class DoseFieldType_Numeric extends DoseFieldType
{
	private int _wholeDigits;
	private int _fracDigits;
	
	public DoseFieldType_Numeric(String configDescription, String jsonName, int wholeDigits, int fracDigits)
	{
		super(configDescription, jsonName);
		
		_wholeDigits = wholeDigits;
		_fracDigits = fracDigits;
	}
	
	@Override
	public String _getDisplayString(DoubleAndString data, boolean useNoun)
	{
		// For numeric-only configurations: either emit the raw number, or the number and the noun 
		if (useNoun == false) {
			return Util.getTextFromDouble(data.getDouble());
		} else {
			return String.format("%s %s", Util.getTextFromDouble(data.getDouble()), _containingType.getNoun(data.getDouble()));
		}
	}

	@Override
	protected void _startEditActivity(PillpopperActivity act, DoubleAndString data, int resultCode)
	{
		InputNumberActivity.selectNumber(
				act,
				getConfigDescription(),
				getConfigDescription(),
				data == null ? 0.0 : data.getDouble(),
				_wholeDigits,
				_fracDigits,
				null,
				R.string.__blank,
				resultCode
				);
	}

	@Override
	protected DoubleAndString _getEditedValue(PillpopperActivity act, Intent intent)
	{
		double d = InputNumberActivity.getReturnValue(intent);
		
		if (d < Util.Epsilon) {
			return null;
		} else {
			return new DoubleAndString(d, null);
		}
	}

	@Override
	protected String _getMarshalledString(DoubleAndString data)
	{
		return data.getMarshalledString();
	}

	@Override
	protected DoubleAndString _parseJson(JSONObject o, String key)
	{
		DoubleAndString retval = DoubleAndString.parseJSON(o, key);
		
		if (retval == null || retval.getDouble() < Util.Epsilon) {
			return null;
		} else {
			return new DoubleAndString(retval.getDouble(), null);
		}
	}
}
