package com.montunosoftware.pillpopper.model;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.util.FDADrugDatabase;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContext;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class DrugTypeList
{
	private static final String _JSON_CUSTOM_DRUG_TYPE_LIST = "customDrugDosageNames";

	private static final Object _globalSync = new Object(); 
	private static DrugTypeList_Standard _standardDrugTypeList;
	private FDADrugDatabase fdaDrugDatabase;
	private EditableStringList<DrugType_Custom> customDrugTypeList;

	private void _init(Preferences preferences, PillpopperAppContext.Edition edition, FDADrugDatabase fdaDrugDatabase, StateUpdatedListener stateUpdatedListener)
	{
		synchronized(_globalSync) {
			if (_standardDrugTypeList == null) {
				_standardDrugTypeList = new DrugTypeList_Standard();
			}
		}
		
		customDrugTypeList = new EditableStringList<>(
                _JSON_CUSTOM_DRUG_TYPE_LIST,
                R.string.drug_type_custom,
                R.string.drug_types_custom,
                stateUpdatedListener,
				(id, name, stateUpdatedListener1) -> new DrugType_Custom(id, name, stateUpdatedListener1));
		
		if (preferences != null) {
			customDrugTypeList.parse(preferences);
		}
		
		this.fdaDrugDatabase = fdaDrugDatabase;
	}

	public DrugTypeList(PillpopperAppContext.Edition edition, FDADrugDatabase fdaDrugDatabase, StateUpdatedListener stateUpdatedListener)
	{
		_init(null, edition, fdaDrugDatabase, stateUpdatedListener);
	}

	public void marshal(JSONObject jsonPrefs) throws JSONException
	{
		customDrugTypeList.marshal(jsonPrefs);
	}

	public DrugType parseJsonDrugType(JSONObject jsonDrugPrefs)
	{
		String jsonDrugTypeName = DrugType.getJsonDrugTypeName(jsonDrugPrefs);

		if (DrugType_Custom.JSON_CUSTOM_DOSE_TYPE.equals(jsonDrugTypeName)) {
			// If this is a custom drug, look it up by the custom drug type ID
			return customDrugTypeList.getItemById(DrugType_Custom.getCustomDrugTypeId(jsonDrugPrefs));
		} else if (DrugType_Database.JSON_DATABASE_DOSE_TYPE.equals(jsonDrugTypeName)) {
			return fdaDrugDatabase.getDatabaseDrugType(DrugType_Database.getMedFormType(jsonDrugPrefs));
		} else {
			return _standardDrugTypeList.getDrugTypeByJsonName(jsonDrugTypeName); 
		}
	}
	
	public DrugType getDrugTypeByEphemeralGuid(String ephemeralGuid)
	{
		Collection<DrugType> drugTypeList = new ArrayList<>();
		
		drugTypeList.addAll(_standardDrugTypeList.getDrugTypeCollection());
		drugTypeList.addAll(customDrugTypeList.getCollection());
		drugTypeList.addAll(fdaDrugDatabase.getDrugTypeCollection());
		
		for (DrugType drugType: drugTypeList) {
			if (drugType.getEphemeralGuid().equals(ephemeralGuid))
				return drugType;
		}
		
		return null;
	}

	public void cleanJsonPrefs(JSONObject jsonDrugPrefs)
	{
		_standardDrugTypeList.cleanJsonPrefs(jsonDrugPrefs);
		DrugType_Custom.cleanCustomJsonPrefs(jsonDrugPrefs);
		DrugType_Database.cleanCustomJsonPrefs(jsonDrugPrefs);
	}


	////////////////
	
	
	public DrugType getDefaultDrugType()
	{
//		return _standardDrugTypeList.getDefaultDrugType(); KP ONLY
		return fdaDrugDatabase.getDatabaseDrugType("Tablet"); // KP ONLY
	}

	public List<DrugType> getSortedStandardTypes()
	{
//		return _standardDrugTypeList.getSortedTypeList();  KP ONLY
		return fdaDrugDatabase.getSortedTypeList(); // KP ONLY
	}
	
	public List<DrugType_Custom> getSortedCustomTypes()
	{
		return customDrugTypeList.getSortedList();
	}
	
	
	///////////////////
	
	
	public DrugType_Custom addCustomDrugType(String newName)
	{
		PillpopperLog.say("Adding new drug type: %s", newName);
		initializeFreeTextProperties();
		return customDrugTypeList.createNewItem(newName);
	}
	
	private void initializeFreeTextProperties()
	{
		customDrugTypeList = new EditableStringList<>(
                _JSON_CUSTOM_DRUG_TYPE_LIST,
                R.string.drug_type_custom,
                R.string.drug_types_custom,
                null,
				(id, name, stateUpdatedListener) -> new DrugType_Custom(id, name, stateUpdatedListener));
		
	}

}
