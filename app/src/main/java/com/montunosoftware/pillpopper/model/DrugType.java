package com.montunosoftware.pillpopper.model;

import com.montunosoftware.pillpopper.android.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;

public class DrugType
{
	private String _drugTypeName;
	public String getDrugTypeName() { return _drugTypeName; }
	protected void setDrugTypeName(String drugTypeName) { _drugTypeName = drugTypeName; }
	
	private String _jsonName;
	public String getJsonName() { return _jsonName; }
	
	private String _singular;
	public String getSingular() { return _singular; }
	
	private String _plural;
	public String getPlural() { return _plural; }
	
	private String _stockUnit;
	public String getStockNoun() { return _stockUnit; }
	
	public static final int MAX_SUPPLEMENTAL_DOSE_FIELDS = 3;
	public static final int MAX_DOSE_FIELDS = MAX_SUPPLEMENTAL_DOSE_FIELDS + 1;
	// 0 field is the units per dose, which is treated specially in some cases.
	// All others are the "supplemental fields".
	private DoseFieldType[] _doseFieldTypes = new DoseFieldType[MAX_DOSE_FIELDS];

	public DoseFieldType getUnitsPerDoseFieldType() { return _doseFieldTypes[0]; }
	public DoseFieldType getSupplementalDoseFieldType(int fieldNum) { return _doseFieldTypes[fieldNum+1]; } // for access to supplemental
	public DoseFieldType getDoseFieldType(int fieldNum) { return _doseFieldTypes[fieldNum]; } // for access to all
	public DoseFieldType[] getAllDoseFieldTypes() { return _doseFieldTypes; }
	
	private int _index = -1;
	public void setIndex(int index) { _index = index; }

	private boolean _isDefault = false;
	public boolean isDefault() { return _isDefault; }
	
	private boolean _notShown = false;
	public boolean notShown() { return _notShown; }
	
	public DrugType(
			String configName,
			String jsonName,
			String singular,
			String plural,
			String stockUnit,
			DoseFieldType unitsPerDoseFieldType,
			DoseFieldType supplementalDoseFieldType0,
			DoseFieldType supplementalDoseFieldType1,
			DoseFieldType supplementalDoseFieldType2
			)
	{
		_init(
				configName,
				jsonName,
				singular,
				plural,
				stockUnit,
				unitsPerDoseFieldType,
				supplementalDoseFieldType0,
				supplementalDoseFieldType1,
				supplementalDoseFieldType2
				);
	}

	public DrugType(
			String configName,
			String jsonName,
			String singular,
			String plural,
			String stockUnit,
			DoseFieldType unitsPerDoseFieldType,
			DoseFieldType supplementalDoseFieldType0,
			DoseFieldType supplementalDoseFieldType1
			)
	{
		_init(
				configName,
				jsonName,
				singular,
				plural,
				stockUnit,
				unitsPerDoseFieldType,
				supplementalDoseFieldType0,
				supplementalDoseFieldType1,
				null
				);
		
	}

	private void _init(
			String configName,
			String jsonName,
			String singular,
			String plural,
			String stockUnit,
			DoseFieldType unitsPerDoseFieldType,
			DoseFieldType supplementalDoseFieldType1,
			DoseFieldType supplementalDoseFieldType2,
			DoseFieldType supplementalDoseFieldType3
			)
	{
		if (configName.charAt(0) == '*') {
			// A "*" at the start of the type name indicates it's the default
			_isDefault = true;
			_drugTypeName = configName.substring(1);
		} else if (configName.charAt(0) == '!') {
			// A "!" indicates it shouldn't be shown on the list of valid drug types
			_notShown = true;
			_drugTypeName = configName.substring(1);
		} else {
			_drugTypeName = configName;	
		}
		
		_jsonName = jsonName;
		_singular = singular;
		_plural = plural;
		_stockUnit = stockUnit;
		_doseFieldTypes[0] = unitsPerDoseFieldType;
		_doseFieldTypes[1] = supplementalDoseFieldType1;
		_doseFieldTypes[2] = supplementalDoseFieldType2;
		_doseFieldTypes[3] = supplementalDoseFieldType3;

		for (DoseFieldType d: _doseFieldTypes) {
			if (d != null) {
				d.setContainingType(this);
			}
		}
	}
	
	public String getNoun(double units)
	{
		if (Math.abs(units - 1.0) < Util.Epsilon) {
			return _singular;
		} else {
			return _plural;
		}
	}

	
	public static int compare(DrugType lhs, DrugType rhs)
	{
		return lhs._index - rhs._index;
	}

	public static class DisplayOrderComparator implements Comparator<DrugType>
	{
		@Override
		public int compare(DrugType lhs, DrugType rhs)
		{
			return DrugType.compare(lhs, rhs);
		}
	}

	
	///////////////////////////////////////////////////////////////////////////////
	//  Marshalling

	protected static final String _JSON_DOSAGE_TYPE = "dosageType";

	public void marshal(JSONObject jsonDrugPrefs) throws JSONException
	{
		// Write the drug type
		jsonDrugPrefs.put(_JSON_DOSAGE_TYPE, getJsonName());
	}

	public static String getJsonDrugTypeName(JSONObject jsonDrugPrefs)
	{
		 return Util.parseJSONStringOrNull(jsonDrugPrefs, _JSON_DOSAGE_TYPE);
	}
	
	// Remove all config data from known types
	public void cleanJsonPrefs(JSONObject jsonDrugPrefs)
	{
		for (DoseFieldType d: _doseFieldTypes) {
			if (d != null) {
				jsonDrugPrefs.remove(d.getJsonName());
			}
		}
	}
	public boolean isType(String jsonTypeName)
	{
		return (jsonTypeName != null && jsonTypeName.equalsIgnoreCase(getJsonName()));
	}
	

	//////////////////////////////////////////////////////////
	
	// temporary GUID assigned to each drug type just used as a key for the drug-type-list selector
	
	String _ephemeralGuid = Util.getRandomGuid();
	
	public String getEphemeralGuid()
	{
		return _ephemeralGuid;
	}

}