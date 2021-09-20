package com.montunosoftware.pillpopper.model;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.PillpopperActivity;
import com.montunosoftware.pillpopper.android.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

public class DrugType_Custom extends DrugType implements EditableStringItem
{
	private final String _id;
	private final StateUpdatedListener _stateUpdatedListener;
	
	public DrugType_Custom(String id, String name, StateUpdatedListener stateUpdatedListener)
	{
		super(
				name,
				id,
				"Dose",
				"Doses",
				"Doses",
				null,
				new DoseFieldType_FreeText("Dosage", _JSON_DOSAGE_DESCRIPTION),
				null
				);

		_stateUpdatedListener = stateUpdatedListener;
		_id = id;
	}


	//////////////
	
	
	private void _update()
	{
		if (_stateUpdatedListener != null) {
			_stateUpdatedListener.onStateUpdated();
		}
	}
			

	//////////////
	
	
	public static final String JSON_CUSTOM_DOSE_TYPE = "custom";
	private static final String _JSON_CUSTOM_ID = "customDosageID";
	private static final String _JSON_DOSAGE_DESCRIPTION = "customDescription";
	
	@Override
	public void marshal(JSONObject jsonDrugPrefs) throws JSONException
	{
		jsonDrugPrefs.put(_JSON_DOSAGE_TYPE, JSON_CUSTOM_DOSE_TYPE);
		jsonDrugPrefs.put(_JSON_CUSTOM_ID, _id);
	}	

	public static String getCustomDrugTypeId(JSONObject jsonDrugPrefs)
	{
		 return Util.parseJSONStringOrNull(jsonDrugPrefs, _JSON_CUSTOM_ID);
	}

	public static void cleanCustomJsonPrefs(JSONObject jsonDrugPrefs)
	{
		jsonDrugPrefs.remove(_JSON_CUSTOM_ID);
		jsonDrugPrefs.remove(_JSON_DOSAGE_DESCRIPTION);
	}
	
	///////// 
	// Implementation of the EditableStringItem interface, which allows the list of custom
	// drug types to be edited

	@Override
	public String getId()
	{
		return _id;
	}

	@Override
	public void setName(String newName)
	{
		super.setDrugTypeName(newName);
		_update();
	}

	@Override
	public String toString()
	{
		return super.getDrugTypeName();
	}
	
	@Override
	public String canBeDeleted(PillpopperActivity act)
	{
		State currState = act.getState();
		
		// Check to see if this drug type is assigned to one or more drugs.
		// If so, don't allow deletion.
		StringBuilder sb = new StringBuilder();
		for (Drug d: currState.getDrugList().getAll()) {
			if (this.equals(d.getDrugType())) {
				if (sb.length() > 0) {
					sb.append(", ");
				}
				sb.append(d.getName());
			}
		}
		
		// if drugs found, pop up an error
		if (sb.length() > 0) {
			return String.format(
					act.getString(R.string.drug_type_in_use),
					this.toString(),
					sb.toString());
		} else {
			return null;
		}
	}

	@Override
	public boolean equals(Object o)
	{
		// Return true if the objects are identical.
		// (This is just an optimization, not required for correctness.)
		if (this == o) {
			return true;
		}

		// Return false if the other object has the wrong type.
		// This type may be an interface depending on the interface's
		// specification.
		if (!(o instanceof DrugType_Custom)) {
			return false;
		}

		// Cast to the appropriate type.
		// This will succeed because of the instanceof, and lets us access
		// private fields.
		DrugType_Custom lhs = (DrugType_Custom) o;

		// Check each field. Primitive fields, reference fields, and nullable
		// reference
		// fields are all treated differently.
		return (_id == null ? lhs._id == null : _id.equals(lhs._id));
	}

	/*FindBugs defects
	 * 
	 * implementing hashcode method and keeping it exact for FindBugs fixes*/
	@Override
	public int hashCode()
	{
		return super.hashCode();
	}

}