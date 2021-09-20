package com.montunosoftware.pillpopper.model;

import android.content.Intent;

import com.montunosoftware.pillpopper.android.PillpopperActivity;
import com.montunosoftware.pillpopper.android.util.Util;

import org.json.JSONObject;

import java.util.List;

public class DoseFieldType_NumericWithUnits extends DoseFieldType
{
	private int _wholeDigits;
	private int _fracDigits;
	private List<String> _unitList;
	private int _defaultUnitIndex = 0;
	
	public DoseFieldType_NumericWithUnits(
			String configDescription,
			String jsonName,
			int wholeDigits, int fracDigits,
			List<String> unitList)
	{
		super(configDescription, jsonName);
		
		if (unitList == null || unitList.isEmpty()) {
			throw new IllegalArgumentException("unit list must be non-null");
		}
		
		_wholeDigits = wholeDigits;
		_fracDigits = fracDigits;
		_unitList = unitList;
		
		// find the default unit
		for (int i = 0; i < _unitList.size(); i++) {
			if (_unitList.get(i).charAt(0) == '*') {
				_unitList.set(i, _unitList.get(i).substring(1));
				_defaultUnitIndex = i;
			}
		}
	}
	
	// If the unit list is null or empty, return a DoseFieldType_Numeric.
	// Otherwise, return DoseFieldType_NumericWithUnits.
	public static DoseFieldType getNumericWithOrWithoutUnits(
			String configDescription,
			String jsonName,
			int wholeDigits, int fracDigits,
			List<String> unitList)
	{
		if (unitList == null || unitList.isEmpty()) {
			return new DoseFieldType_Numeric(configDescription, jsonName, wholeDigits, fracDigits);
		} else {
			return new DoseFieldType_NumericWithUnits(configDescription, jsonName, wholeDigits, fracDigits, unitList);
		}
	}
			
	
	public int getWholeDigits() { return _wholeDigits; }
	public int getFracDigits () { return _fracDigits; }
	public List<String> getUnitList() { return _unitList; }

	@Override
	public String _getDisplayString(DoubleAndString data, boolean useNoun)
	{
		if	(data.getDouble() > Util.Epsilon) {
			return String.format("%s %s", Util.getTextFromDouble(data.getDouble()), data.getString());
		} else {
			return null;
		}
	}

	@Override
	protected void _startEditActivity(PillpopperActivity act, DoubleAndString data, int resultCode)
	{
		boolean allowRemove;
		
		if (data == null) {
			allowRemove = false;
			data = new DoubleAndString(0.0, _unitList.get(_defaultUnitIndex));
		} else {
			allowRemove = true;
		}
	}

	private DoubleAndString _maybeReturnDoubleAndString(DoubleAndString das)
	{
		if (das == null || das.getDouble() < Util.Epsilon) {
			return null;
		} else {
			return das;
		}		
	}
	
	@Override
	protected DoubleAndString _getEditedValue(PillpopperActivity act, Intent intent)
	{
		return _maybeReturnDoubleAndString(null);
	}

	@Override
	protected String _getMarshalledString(DoubleAndString data)
	{
		return data.getMarshalledString();
	}

	@Override
	protected DoubleAndString _parseJson(JSONObject o, String key)
	{
		return _maybeReturnDoubleAndString(DoubleAndString.parseJSON(o, key));
	}

}
