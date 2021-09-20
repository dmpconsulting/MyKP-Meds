package com.montunosoftware.pillpopper.android.util;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;

import com.montunosoftware.pillpopper.android.PillpopperActivity;
import com.montunosoftware.pillpopper.android.view.PickListView;
import com.montunosoftware.pillpopper.model.DoseFieldType;
import com.montunosoftware.pillpopper.model.DoseFieldType_MultipleChoice;
import com.montunosoftware.pillpopper.model.DoseFieldType_Numeric;
import com.montunosoftware.pillpopper.model.DoseFieldType_NumericWithUnits;
import com.montunosoftware.pillpopper.model.DoubleAndString;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.DrugType;
import com.montunosoftware.pillpopper.model.DrugType_Custom;
import com.montunosoftware.pillpopper.model.DrugType_Database;
import com.readystatesoftware.sqliteasset.SQLiteAssetHelper;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.TreeSet;

// Interface to the FDA drug database. SQLiteAssetHelper is a public-domain
// helper class that makes it easy to instantiate SQLite databases initialized
// from databases packed into the APK file. For more information, see
// https://github.com/jgilfelt/android-sqlite-asset-helper

public class FDADrugDatabase extends SQLiteAssetHelper
{
	public static class DrugNameSearchResults
	{
		private Collection<String> _results;

		private DrugNameSearchResults(Collection<String> results)
		{
			_results = results;
		}

		public Collection<String> getResults()
		{
			return _results;
		}
	}

	public enum SearchType {
		DRUG_SEARCH_ALL,
		DRUG_SEARCH_RX,
		DRUG_SEARCH_OTC
	}


	//////////////////////////////////////////////////////////////////////


	private static final String DATABASE_NAME = "AndroidFDADatabase";
	private static final int DATABASE_VERSION = 1;

	private static final String MEDICATION_TABLE_NAME = "Medication";

	private static final String MEDFORMTYPE_TABLE_NAME = "MedFormType";
	private static final String MEDFORMTYPE_COLUMN_NAME = "medFormType";

	private static final String MEDROUTES_TABLE_NAME = "MedicationRoute";
	private static final String MEDROUTES_COLUMN_NAME = "route";

	private static final String MEDSTRENGTHUNIT_TABLE_NAME = "MedStrengthUnit";
	private static final String MEDSTRENGTHUNIT_KEY_COLUMN_NAME = "medFormType";
	private static final String MEDSTRENGTHUNIT_VAL_COLUMN_NAME = "unitDesc";

	private static final String MEDDOSEUNIT_TABLE_NAME = "MedDoseUnit";
	private static final String MEDDOSEUNIT_KEY_COLUMN_NAME = "medFormType";
	private static final String MEDDOSEUNIT_VAL_COLUMN_NAME = "unitDesc";

	private static final String MEDAPPLYLOC_TABLE_NAME = "MedApplyLocation";
	private static final String MEDAPPLYLOC_KEY_COLUMN_NAME = "medFormType";
	private static final String MEDAPPLYLOC_VAL_COLUMN_NAME = "locationDesc"; 

	private SQLiteDatabase _db;

	// Meta-information contained in the database.
	private final List<String> _medFormTypes;
	private final HashMap<String, List<String>> _strengthUnits;
	private final HashMap<String, List<String>> _doseUnits;
	private final HashMap<String, List<String>> _applyLocations;

	private final HashMap<String, DrugType> _dbDrugTypes;

	public FDADrugDatabase(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);

		_db = null;

		try {
			_db = getReadableDatabase();
		} catch (SQLiteException e) {
			_strengthUnits = new HashMap<>();
			_doseUnits = new HashMap<>();
			_applyLocations = new HashMap<>();
			_medFormTypes = new ArrayList<>();
			_dbDrugTypes = new HashMap<>();
			return;
		}

		// Get the list of database drug types
		_medFormTypes = _getColumnAsList(MEDFORMTYPE_TABLE_NAME, MEDFORMTYPE_COLUMN_NAME);

		// For each database drug type, get its list of strength units, dose units, and application locations
		_strengthUnits = _collate(MEDSTRENGTHUNIT_TABLE_NAME, MEDSTRENGTHUNIT_KEY_COLUMN_NAME, MEDSTRENGTHUNIT_VAL_COLUMN_NAME);
		_doseUnits = _collate(MEDDOSEUNIT_TABLE_NAME, MEDDOSEUNIT_KEY_COLUMN_NAME, MEDDOSEUNIT_VAL_COLUMN_NAME);
		_applyLocations = _collate(MEDAPPLYLOC_TABLE_NAME, MEDAPPLYLOC_KEY_COLUMN_NAME, MEDAPPLYLOC_VAL_COLUMN_NAME);

		// Get the list of routes and turn it into a selectable field
		List<String> routes = _getColumnAsList(MEDROUTES_TABLE_NAME, MEDROUTES_COLUMN_NAME);
		List<Pair<String, String>> routePairs = new ArrayList<>(routes.size());
		for (String route: routes) {
			routePairs.add(new Pair<>(route, route));
		}
		DoseFieldType_MultipleChoice routeField = new DoseFieldType_MultipleChoice("Route", DrugType_Database.JSON_DATABASE_ROUTE, routePairs);


		// For each database drug type, create a DrugType structure with all the appropriate multiple-choice options
		_dbDrugTypes = new HashMap<>();
		for (String medFormType: _medFormTypes) {
			// For most medFormTypes, we use the generic terms: "1 Dose Due", "Amount per Dose", "Amount per Refill", etc., 
			// with units culled from the database. However, for certain types, such as Tablets, Capsules, and others that
			// use unitless counts for dosing, we special-case here. This lets the user configure as "Tablets per Dose"
			// rather than "Amount per Dose" with a strange selection of units.
			String singular = "Dose";
			String plural = "Doses";
			String stockUnit = "Amount";
			DoseFieldType unitsPerDose =  DoseFieldType_NumericWithUnits.getNumericWithOrWithoutUnits(
					"Amount per Dose", DrugType_Database.JSON_DATABASE_AMOUNT, 4, 2, _doseUnits.get(medFormType));

			if ("Other".equals(medFormType)) {
				unitsPerDose = new DoseFieldType_Numeric("Amount per Dose", DrugType_Database.JSON_DATABASE_AMOUNT, 4, 2);
			} else if ("Tablet".equals(medFormType)) {
				singular = "Tablet";
				plural = "Tablets";
				stockUnit = "Tablets";
				unitsPerDose = new DoseFieldType_Numeric("Tablets per Dose", DrugType_Database.JSON_DATABASE_AMOUNT, 4, 2);
			} else if ("Suppository".equals(medFormType)) {
				singular = "Suppository";
				plural = "Suppositories";
				stockUnit = "Suppositories";
				unitsPerDose = new DoseFieldType_Numeric("Suppositories per Dose", DrugType_Database.JSON_DATABASE_AMOUNT, 4, 2);
			} else if ("Lozenge".equals(medFormType)) {
				singular = "Lozenge";
				plural = "Lozenges";
				stockUnit = "Lozenges";
				unitsPerDose = new DoseFieldType_Numeric("Lozenges per Dose", DrugType_Database.JSON_DATABASE_AMOUNT, 4, 2);
			} else if ("Implant".equals(medFormType)) {
				singular = "Implant";
				plural = "Implants";
				stockUnit = "Implants";
				unitsPerDose = new DoseFieldType_Numeric("Implants per Dose", DrugType_Database.JSON_DATABASE_AMOUNT, 4, 2);
			} else if ("Gum".equals(medFormType)) {
				singular = "Gum";
				plural = "Gums";
				stockUnit = "Gums";
				unitsPerDose = new DoseFieldType_Numeric("Gums per Dose", DrugType_Database.JSON_DATABASE_AMOUNT, 4, 2);
			} else if ("Film".equals(medFormType)) {
				singular = "Film";
				plural = "Films";
				stockUnit = "Films";
				unitsPerDose = new DoseFieldType_Numeric("Films per Dose", DrugType_Database.JSON_DATABASE_AMOUNT, 4, 2);
			} else if ("Cloth/Patch".equals(medFormType)) {
				singular = "Patch";
				plural = "Patches";
				stockUnit = "Patches";
				unitsPerDose = new DoseFieldType_Numeric("Patches per Dose", DrugType_Database.JSON_DATABASE_AMOUNT, 4, 2);
			} else if ("Capsule".equals(medFormType)) {
				singular = "Capsule";
				plural = "Capsules";
				stockUnit = "Capsules";
				unitsPerDose = new DoseFieldType_Numeric("Capsules per Dose", DrugType_Database.JSON_DATABASE_AMOUNT, 4, 2);
			}

			// use apply locations if this drug type has them
			DoseFieldType applyLocationsFieldType = null;

			if (_applyLocations.containsKey(medFormType)) {
				List<String> applyLocationsForMedForm = _applyLocations.get(medFormType);

				List<Pair<String, String>> applyPairs = new ArrayList<>(applyLocationsForMedForm.size());
				for (String applyLocation: applyLocationsForMedForm) {
					applyPairs.add(new Pair<>(applyLocation, applyLocation));
				}

				applyLocationsFieldType = new DoseFieldType_MultipleChoice("Location", DrugType_Database.JSON_DATABASE_LOCATION, applyPairs);
			}

			//			PillpopperLog.say("DB medform: %s", medFormType);

			_dbDrugTypes.put(medFormType, new DrugType_Database(
					medFormType,
					DrugType_Database.JSON_DATABASE_DOSE_TYPE,
					singular,
					plural,
					stockUnit,
					unitsPerDose,
					new DoseFieldType_NumericWithUnits("Strength", DrugType_Database.JSON_DATABASE_STRENGTH, 4, 2, _strengthUnits.get(medFormType)),
					routeField,
					applyLocationsFieldType));
		}
	}

	public DrugType getDatabaseDrugType(String medFormType)
	{
		if (_dbDrugTypes.containsKey(medFormType)) {
			return _dbDrugTypes.get(medFormType);
		} else {
			return null;
		}
	}

	public List<DrugType> getSortedTypeList()
	{
		List<DrugType> retval = new ArrayList<>(_dbDrugTypes.values());
		Collections.sort(retval, new DrugType_Database.AlphabeticalComparator());
		return retval;
	}

	public Collection<DrugType> getDrugTypeCollection()
	{
		return _dbDrugTypes.values();
	}

	// Get all the values of a column in a table
	private List<String> _getColumnAsList(String tableName, String columnName)
	{
		Cursor cursor = _db.query(
				tableName,
				new String[] { columnName },
				null,
				null,
				null,
				null,
				columnName,
				null);

		cursor.moveToFirst();
		List<String> retval = new ArrayList<>(cursor.getCount());
		while (!cursor.isAfterLast()) {
			retval.add(cursor.getString(0));
			cursor.moveToNext();
		}
		cursor.close();
		return retval;
	}

	// Collate all the values of a column by each value of a key column. Fo example:
	// a 1
	// a 2
	// b 3
	// c 4
	// ... would return a: [1, 2]; b: [3, 4]
	private HashMap<String, List<String>> _collate(String tableName, String keyColumnName, String valColumnName)
	{
		Cursor cursor = _db.query(
				tableName,
				new String[] { keyColumnName, valColumnName },
				null,
				null,
				null,
				null,
				valColumnName,
				null);

		cursor.moveToFirst();
		HashMap<String, List<String>> retval = new HashMap<>();

		while (!cursor.isAfterLast()) {
			String key = cursor.getString(0);
			String value = cursor.getString(1);

			// create a new list for this key if it doesn't exist already
			if (!retval.containsKey(key)) {
				retval.put(key, new ArrayList<>());
			}

			// add this value to the key's list
			retval.get(key).add(value);

			cursor.moveToNext();
		}
		cursor.close();

		return retval;
	}


	public DrugNameSearchResults searchForDrugs(CharSequence searchSequence, SearchType searchType)
	{
		if (_db == null) {
			return null;
		}

		String searchStringLowerCase = searchSequence.toString().toLowerCase(Locale.US);
		String pattern = String.format("%s%%", searchSequence);
		String whereClause;
		String[] substArgs;

		switch (searchType) {
		case DRUG_SEARCH_OTC:			
			whereClause = "(brandName like ? or genericName like ?) and medType = ?";
			substArgs = new String[] { pattern, pattern, "OTC" };
			break;
		case DRUG_SEARCH_RX:			
			whereClause = "(brandName like ? or genericName like ?) and medType = ?";
			substArgs = new String[] { pattern, pattern, "Rx" };
			break;
		case DRUG_SEARCH_ALL:
		default:
			whereClause = "brandName like ? or genericName like ?";
			substArgs = new String[] { pattern, pattern };
			break;
		} 

		Cursor cursor = _db.query(
				MEDICATION_TABLE_NAME,
				new String[] { "brandName", "genericName" },
				whereClause,
				substArgs,
				null,
				null,
				null,
				null
				);

		TreeSet<String> results = new TreeSet<>();

		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			String brandName = cursor.getString(0);
			String genericName = cursor.getString(1);

			if (brandName.toLowerCase(Locale.US).startsWith(searchStringLowerCase))
				results.add(brandName);

			if (genericName.toLowerCase(Locale.US).startsWith(searchStringLowerCase))
				results.add(genericName);

			cursor.moveToNext();
		}

		cursor.close();

		return new DrugNameSearchResults(results);
	}


	public class DatabaseDrugVariant
	{
		private String _drugName;
		private String _medForm;
		private String _medFormType;
		private String _medType; // "Rx" vs "OTC" - thank you, government, for these wonderful field names
		private String _ndc;
		private String _route;
		private String _strength;
		private String _unit;

		public DatabaseDrugVariant(
				String drugName,
				String medForm,
				String medFormType,
				String medType,
				String ndc,
				String route,
				String strength,
				String unit)
		{
			set_drugName(drugName);
			set_medForm(medForm);
			set_medFormType(medFormType);
			set_medType(medType);
			set_ndc(ndc);
			set_route(route);
			set_strength(strength);
			set_unit(unit);
		}

		//
		// Given an entry in the database, and a list of candidate suffixes, parse
		// out into a number and a suffix.
		// Example: the unit field of the database can be null, 'g' (1g), '50mL', or '0.45ml'.
		//
		private DoubleAndString _parseEntry(String dbEntry, List<String> unitList)
		{
			String cleanString = Util.cleanString(dbEntry);

			if (cleanString == null) {
				return null;
			}

			for (String unit: unitList) {
				// a naked unit (e.g., "g") means 1 unit
				if (cleanString.equals(unit)) {
					return new DoubleAndString(1.0, unit);
				}

				// For this unit to match, the string must be at least one character longer than the unit,
				// and the character right before the unit must be a number.
				if (cleanString.length() <= unit.length())
					continue;
				if (!Character.isDigit(cleanString.charAt(cleanString.length() - unit.length() - 1)))
					continue;

				// now check if the suffix is our unit
				if (!cleanString.endsWith(unit))
					continue;

				// success! parse.
				double amount = Util.parseNonnegativeDouble(cleanString.substring(0, cleanString.length() - unit.length()));

				if (amount < 0) {
					return new DoubleAndString(0.0, unit);
				} else {
					return new DoubleAndString(amount, unit);
				}
			}

			// suffix not found!  hmm.
			return null;
		}

		private DoubleAndString _parseEntry(String dbEntry, String medFormType, HashMap<String, List<String>> collatedLists)
		{
			if (collatedLists.containsKey(medFormType)) {
				return _parseEntry(dbEntry, collatedLists.get(medFormType));
			} else {
				return null;
			}
		}


		@Override
		public String toString()
		{
			// Construct the menu item that describes the variant
			StringBuilder sb = new StringBuilder();

			sb.append(get_drugName());
			for (String s: new String[] {get_unit(), get_strength(), get_medFormType(), get_route()}) {
				if (s != null && s.length() > 0) {
					sb.append(", ");
					sb.append(s);
				}
			}

			return sb.toString();
		}

		public void apply(PillpopperActivity _thisActivity, Drug drug, String strength)
		{
			DrugType_Custom newDrugType = _thisActivity.getState().getDrugTypeList().addCustomDrugType("custom");
			String _currentSelection = newDrugType.getEphemeralGuid();
			
			drug.setDrugType(_thisActivity.getState().getDrugTypeList().getDrugTypeByEphemeralGuid(_currentSelection));

			drug.setName(get_drugName());
			drug.setUnitsPerDoseData(_parseEntry(get_unit(), get_medFormType(), _doseUnits));
			drug.setSupplementalDoseData(0, parseEntry(strength, get_medFormType(), _strengthUnits));
			drug.setSupplementalDoseData(1, new DoubleAndString(0.0, get_route()));

			drug.setNdc(get_ndc());
			drug.setMedForm(get_medForm());
			drug.setMedType(get_medType());
		}

		public String get_drugName() {
			return _drugName;
		}

		public void set_drugName(String _drugName) {
			this._drugName = _drugName;
		}

		public String get_medForm() {
			return _medForm;
		}

		public void set_medForm(String _medForm) {
			this._medForm = _medForm;
		}

		public String get_medFormType() {
			return _medFormType;
		}

		public void set_medFormType(String _medFormType) {
			this._medFormType = _medFormType;
		}

		public String get_medType() {
			return _medType;
		}

		public void set_medType(String _medType) {
			this._medType = _medType;
		}

		public String get_ndc() {
			return _ndc;
		}

		public void set_ndc(String _ndc) {
			this._ndc = _ndc;
		}

		public String get_route() {
			return _route;
		}

		public void set_route(String _route) {
			this._route = _route;
		}

		public String get_strength() {
			return _strength;
		}

		public void set_strength(String _strength) {
			this._strength = _strength;
		}

		public String get_unit() {
			return _unit;
		}

		public void set_unit(String _unit) {
			this._unit = _unit;
		}
	}
	
	private DoubleAndString parseEntry(String strength, String _medFormType, HashMap<String, List<String>> _strengthUnits2){
		if(null!=strength && !("").equals(strength)){
			try{//Double.parseDouble(amount.replaceAll("[^0-9.]", ""))
				return new DoubleAndString(0.0, strength);
			}catch(NumberFormatException e){
				PillpopperLog.say("Oops!, Exception while dosage parsing, So returning null");
				return null;  
			}
		}
		return null;
	}

	public List<PickListView.MenuItem> getVariantsAsMenu(CharSequence searchSequence)
	{
		String searchString = searchSequence.toString();

		if (_db == null)
			return null;

		Cursor cursor = _db.query(
				MEDICATION_TABLE_NAME,
				new String[] { "brandName", "genericName", "medForm", "medFormType", "medType", "ndc", "route", "strength", "unit" },
				"brandName = ? or genericName = ?",
				new String[] { searchString, searchString },
				"unit,strength,medFormType,route",
				null,
				null,
				null
				);

		ArrayList<PickListView.MenuItem> retval = new ArrayList<>();

		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			String drugName;

			if (cursor.getString(0).equalsIgnoreCase(searchString))
				drugName = cursor.getString(0);
			else
				drugName = cursor.getString(1);

			DatabaseDrugVariant databaseDrugVariant = new DatabaseDrugVariant(
					drugName,
					cursor.getString(2),
					cursor.getString(3),
					cursor.getString(4),
					cursor.getString(5),
					cursor.getString(6),
					cursor.getString(7),
					cursor.getString(8)
					);

			retval.add(new PickListView.MenuItem(
					databaseDrugVariant.toString(),
					databaseDrugVariant));

			cursor.moveToNext();
		}

		cursor.close();

		return retval;
	}
	public List<DatabaseDrugVariant> getListChildData(CharSequence searchSequence)
	{
		String searchString = searchSequence.toString();

		if (_db == null)
			return null;

		Cursor cursor = _db.query(
				MEDICATION_TABLE_NAME,
				new String[] { "brandName", "genericName", "medForm", "medFormType", "medType", "ndc", "route", "strength", "unit" },
				"brandName = ? or genericName = ?",
				new String[] { searchString, searchString },
				"unit,strength,medFormType,route",
				null,
				null,
				null
				);

		ArrayList<DatabaseDrugVariant> retval = new ArrayList<>();

		cursor.moveToFirst();

		while (!cursor.isAfterLast()) {
			String drugName;

			if (cursor.getString(0).equalsIgnoreCase(searchString))
				drugName = cursor.getString(0);
			else
				drugName = cursor.getString(1);

			DatabaseDrugVariant databaseDrugVariant = new DatabaseDrugVariant(
					drugName,
					cursor.getString(2),
					cursor.getString(3),
					cursor.getString(4),
					cursor.getString(5),
					cursor.getString(6),
					cursor.getString(7),
					cursor.getString(8)
					);

			retval.add(databaseDrugVariant);

			cursor.moveToNext();
		}

		cursor.close();

		return retval;
	}

}
