package com.montunosoftware.pillpopper.model;

import android.content.Intent;

import com.montunosoftware.pillpopper.android.PillpopperActivity;

import org.json.JSONException;
import org.json.JSONObject;

//
// Drug doses have configurable fields (e.g. number of pills, drop location). 
// This is the interface implemented by different *types* of fields -- e.g. pure
// numeric, numeric with units, free text, etc.
//
// The underlying data is always represented with DoubleAndString.
//
public abstract class DoseFieldType
{
	private String _configDescription;
	public String getConfigDescription() { return _configDescription; }
	
	private String _jsonName;
	public String getJsonName() { return _jsonName; }

	protected DoseFieldType(String configDescription, String jsonName)
	{
		_configDescription = configDescription;
		_jsonName = jsonName;
	}
	
	////////////////////////////////////////////////////////////////////
	// Get the user-displayable representation of the field.
	// useNoun controls whether the display should say "4 pills" or "4".
	public String getDisplayString(DoubleAndString data, boolean useNoun)
	{
		if (data == null)
			return null;
		else
			return _getDisplayString(data, useNoun);
	}
	
	protected abstract String _getDisplayString(DoubleAndString data, boolean useNoun);
	
	////////////////////////////////////////////////////////////////////
	// Start editing the data for this field
	public static void startEditActivity(
			DoseFieldType doseFieldType,
			PillpopperActivity act,
			DoubleAndString data,
			int resultCode)
	{
		if (doseFieldType != null) {
			doseFieldType._startEditActivity(act, data, resultCode);
		}
	}

	protected abstract void _startEditActivity(PillpopperActivity act, DoubleAndString data, int resultCode);
	

	////////////////////////////////////////////////////////////////////
	// Get the value that was just edited after an activity finishes
	public static DoubleAndString getEditedValue(DoseFieldType doseFieldType, PillpopperActivity act, Intent intent)
	{
		if (doseFieldType == null)
			return null;
		else
			return doseFieldType._getEditedValue(act, intent);
	}
	
	protected abstract DoubleAndString _getEditedValue(PillpopperActivity act, Intent intent);
	
	////////////////////////////////////////////////////////////////////
	// marshal the field into JSON for storage
	public void marshal(DoubleAndString data, JSONObject jsonDrug, JSONObject jsonDrugPrefs) throws JSONException
	{
		if (data == null) {
			return;
		}
		
		// First, determine where to write the result.  A json config name that starts with
		// "*" means that it goes in the pill block; otherwise, the preferences block.
		// (Ugly backwards compatibility.)
		String jsonName;
		JSONObject jsonTarget;
		if (_jsonName.charAt(0) == '*') {
			jsonName = _jsonName.substring(1);
			jsonTarget = jsonDrug;
		} else {
			jsonName = _jsonName;
			jsonTarget = jsonDrugPrefs;
		}

		String marshalledString = _getMarshalledString(data);
		
		if (marshalledString != null) {
			jsonTarget.put(jsonName, marshalledString);
		}
	}
	
	// return the json marshalled string -- internal implementation, not part of public interface
	protected abstract String _getMarshalledString(DoubleAndString data);
	
	////////////////////////////////////////////////////////////////////
	// parse the field back out of JSON
	public DoubleAndString parseJson(JSONObject jsonDrug, JSONObject jsonDrugPrefs)
	{
		// First, determine where to write the result.  A json config name that starts with
		// "*" means that it goes in the pill block; otherwise, the preferences block.
		// (Ugly backwards compatibility.)
		String jsonName;
		JSONObject jsonTarget;
		if (_jsonName.charAt(0) == '*') {
			jsonName = _jsonName.substring(1);
			jsonTarget = jsonDrug;
		} else {
			jsonName = _jsonName;
			jsonTarget = jsonDrugPrefs;
		}

		return _parseJson(jsonTarget, jsonName);
	}
	
	protected abstract DoubleAndString _parseJson(JSONObject o, String key);
	
	protected DrugType _containingType;
	public void setContainingType(DrugType containingType) { _containingType = containingType; }
}
