package com.montunosoftware.pillpopper.model;

import android.content.Intent;

import com.montunosoftware.pillpopper.android.InputTextActivity;
import com.montunosoftware.pillpopper.android.PillpopperActivity;
import com.montunosoftware.pillpopper.android.util.Util;

import org.json.JSONObject;

public class DoseFieldType_FreeText extends DoseFieldType
{
	protected DoseFieldType_FreeText(String configDescription, String jsonName)
	{
		super(configDescription, jsonName);
	}

	@Override
	public String _getDisplayString(DoubleAndString data, boolean useNoun)
	{
		return data.getString();
	}

	@Override
	protected void _startEditActivity(PillpopperActivity act, DoubleAndString data, int resultCode)
	{
		InputTextActivity.editText(
				act,
				getConfigDescription(),
				data == null ? null : data.getString(),
				null,
				resultCode
				);
	}

	private DoubleAndString _boxString(String s)
	{
		if (s == null)
			return null;
		else {
			return new DoubleAndString(0.0, s);
		}
	}
	
	@Override
	protected DoubleAndString _getEditedValue(PillpopperActivity act, Intent intent)
	{
		return _boxString(InputTextActivity.getReturnValue(intent));
	}

	@Override
	protected String _getMarshalledString(DoubleAndString data)
	{
		return data.getString();
	}

	@Override
	protected DoubleAndString _parseJson(JSONObject o, String key)
	{
		return _boxString(Util.parseJSONStringOrNull(o, key));
	}

}
