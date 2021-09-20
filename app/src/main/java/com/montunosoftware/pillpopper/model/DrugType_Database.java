package com.montunosoftware.pillpopper.model;

import com.montunosoftware.pillpopper.android.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Comparator;

public class DrugType_Database extends DrugType
{
	public DrugType_Database(
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
		super(
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


	//////////////
	
	public static final String JSON_DATABASE_DOSE_TYPE = "database";
	public static final String JSON_DATABASE_AMOUNT = "databaseAmount";
	public static final String JSON_DATABASE_STRENGTH = "databaseStrength";
	public static final String JSON_DATABASE_LOCATION = "databaseLocation";
	public static final String JSON_DATABASE_ROUTE = "databaseRoute";
	public static final String JSON_DATABASE_MEDFORM = "databaseMedForm";
	public static final String JSON_DATABASE_MEDFORMTYPE = "databaseMedFormType";
	public static final String JSON_DATABASE_MEDTYPE = "databaseMedType";
	public static final String JSON_DATABASE_NDC = "databaseNDC";

	
	@Override
	public void marshal(JSONObject jsonDrugPrefs) throws JSONException
	{
		jsonDrugPrefs.put(_JSON_DOSAGE_TYPE, JSON_DATABASE_DOSE_TYPE);
		jsonDrugPrefs.put(JSON_DATABASE_MEDFORMTYPE, getDrugTypeName());
	}	


	public static String getMedFormType(JSONObject jsonDrugPrefs)
	{
		return Util.parseJSONStringOrNull(jsonDrugPrefs, JSON_DATABASE_MEDFORMTYPE);
	}

	public static void cleanCustomJsonPrefs(JSONObject jsonDrugPrefs)
	{
		jsonDrugPrefs.remove(JSON_DATABASE_MEDFORMTYPE);
		jsonDrugPrefs.remove(JSON_DATABASE_AMOUNT);
		jsonDrugPrefs.remove(JSON_DATABASE_STRENGTH);
		jsonDrugPrefs.remove(JSON_DATABASE_LOCATION);
		jsonDrugPrefs.remove(JSON_DATABASE_ROUTE);
		
		// medtype, medform and ndc are glommed onto drug, since there's no easy way to represent them in dosedata
	}
	
	public static class AlphabeticalComparator implements Comparator<DrugType>
	{
		@Override
		public int compare(DrugType lhs, DrugType rhs)
		{
			return lhs.getDrugTypeName().compareTo(rhs.getDrugTypeName());
		}

	}
}
