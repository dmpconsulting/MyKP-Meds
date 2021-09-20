package com.montunosoftware.pillpopper.model;

import android.content.Context;

import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

public class DoseData
{
	private DrugType _drugType;
	public DrugType getDrugType() {	return _drugType; }
	public void setDrugType(DrugType drugType)	{ _drugType = drugType; }

	// as with DrugType, the first position is the units per dose; all others are the supplemental fields
	private DoubleAndString[] _doseDataFields = new DoubleAndString[DrugType.MAX_DOSE_FIELDS];
	
	public DoubleAndString getUnitsPerDoseData() { return _doseDataFields[0]; }
	public DoubleAndString getSupplementalDoseData(int index) { return _doseDataFields[index+1]; } // for access to supplemental data
	public DoubleAndString getDoseDataField(int index) { return _doseDataFields[index]; } // for access to all fields
	public DoubleAndString[] getAllDoseData() { return _doseDataFields; }
	
	public void setUnitsPerDoseData(DoubleAndString doseData) { _doseDataFields[0] = doseData; }
	public void setSupplementalDoseData(int index, DoubleAndString doseData) { _doseDataFields[index+1] = doseData; } // for access to supplemental data
	public void setDoseData(int index, DoubleAndString doseData) { _doseDataFields[index] = doseData; } // for access to all fields
	
	public DoseData(DrugType drugType)
	{
		_drugType = drugType;
	}
	
	/////////////////////////////////////////////////////////////////////////////////////
	// Marshalling/Parsing
	
	private static final String _JSON_NUMPILLS = "numpills"; // note: goes in pill block, not prefs block
	private static final String _JSON_DOSE = "dose"; // note: goes in pill block, not prefs block

	public void marshal(DrugTypeList drugTypeList, JSONObject jsonDrug, JSONObject jsonDrugPrefs) throws JSONException
	{
		// First, make sure all old config data has been removed
		// Commenting this, since we do not require to clear the custom Preferences.
		//drugTypeList.cleanJsonPrefs(jsonDrugPrefs);
		
		// Ugly backwards compatibility: if jsonDrug and jsonDrugPrefs are separate objects,
		// put in the default values.  The un-needed fields are written for marshalling drugs,
		// not used for marshalling history events.
		if (jsonDrug != jsonDrugPrefs) {
			jsonDrug.put(_JSON_NUMPILLS, "0.0");
			jsonDrug.put(_JSON_DOSE,  "");
		} else {
			jsonDrug.remove(_JSON_NUMPILLS);
			jsonDrug.remove(_JSON_DOSE);
		}
		
		_drugType.marshal(jsonDrugPrefs);
		
		for (int i = 0; i < DrugType.MAX_DOSE_FIELDS; i++) {
			if (_drugType.getDoseFieldType(i) != null) {
				_drugType.getDoseFieldType(i).marshal(getDoseDataField(i), jsonDrug, jsonDrugPrefs);
			}
		}
	}
	
	
	public static DoseData parseJson(DrugTypeList drugTypeList, JSONObject jsonDrug, JSONObject jsonDrugPrefs)
	{
		DrugType drugType = drugTypeList.parseJsonDrugType(jsonDrugPrefs);
		
		if (drugType == null) {
			return new DoseData(drugTypeList.getDefaultDrugType());
		} else {
			DoseData doseData = new DoseData(drugType);

			doseData.setDrugType(drugType);
			
			for (int i = 0; i < DrugType.MAX_DOSE_FIELDS; i++) {
				if (drugType.getDoseFieldType(i) != null) {
					doseData.setDoseData(i, drugType.getDoseFieldType(i).parseJson(jsonDrug, jsonDrugPrefs));
				}
			}
			
			return doseData;
		}
	}

	
	// Returns a description of a dosage, e.g. "2 pills".
	// If a dose strength was provided, it will return "2 pills, 250mg"
	public String getDosageDescription(Context ctx,Drug drug)
	{
		StringBuilder sb = new StringBuilder();

		// Units per dose config
		if (getUnitsPerDoseData() != null) {
			if (_drugType.getUnitsPerDoseFieldType() != null) {
				sb.append(_drugType.getUnitsPerDoseFieldType().getDisplayString(getUnitsPerDoseData(), true));
			} /*else {
				sb.append(_drugType.getSingular());
			}*/
		}

		// All supplemental data
		for (int i = 0; i < DrugType.MAX_SUPPLEMENTAL_DOSE_FIELDS; i++) {
			if (_drugType.getSupplementalDoseFieldType(i) != null && getSupplementalDoseData(i) != null) {
				if (sb.length() > 0) {
					sb.append(", ");
				}

				sb.append(_drugType.getSupplementalDoseFieldType(i).getDisplayString(getSupplementalDoseData(i), false));
			}
		}
		
		// This is the a fallback mechanism. If the dose is empty look for the customDescription value
		if(sb.toString().length()==0){
			JSONObject drugJsonObj;
			try {
				if(null!=drug){
					drugJsonObj = new JSONObject(drug.marshal(ctx,false, null).toString());
					JSONObject drugPrefObj = drugJsonObj.getJSONObject("preferences");
					String customDescriptionValue = drugPrefObj.getString("customDescription");
					sb.append(customDescriptionValue);
				}
			} catch (JSONException e) {
				PillpopperLog.say("Oops!, JSONException while getting the customDescription value" + e.getMessage());
			} catch (Exception e) {
				PillpopperLog.say("Oops!, Exception while getting the customDescription value" + e.getMessage());
			}
		}
		return Util.cleanString(sb.toString());
	}
	
	public DoseData copy()
	{
		DoseData retval = new DoseData(_drugType);
		
		for (int i = 0; i < DrugType.MAX_DOSE_FIELDS; i++) {
			retval.setDoseData(i, getDoseDataField(i));
		}

		return retval;
	}
	
}
