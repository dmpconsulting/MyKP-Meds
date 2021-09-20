package com.montunosoftware.pillpopper.model;

import com.montunosoftware.pillpopper.android.util.Pair;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DrugTypeList_Standard
{
	private Map<String, DrugType> _drugTypeList;
	int lastIndex = 0;
	public static final String MANAGED = "managed";
	
	
	// add a new drug type to the type list.  If it has been marked as the default, remember its index.  
	private DrugType _addDrugType(DrugType drugType)
	{
		drugType.setIndex(lastIndex++); // remember the order they were added, for sorting
		_drugTypeList.put(drugType.getJsonName(), drugType);
		
		if (drugType.isDefault()) {
			_defaultDrugType = drugType;
		}

		return drugType;
	}
	
	public List<DrugType> getSortedTypeList()
	{
		List<DrugType> retval = new ArrayList<>(_drugTypeList.values());
		Collections.sort(retval, new DrugType.DisplayOrderComparator());
		return retval;
	}
	
	public Collection<DrugType> getDrugTypeCollection()
	{
		return _drugTypeList.values();
	}
	
	public DrugType getDrugTypeByJsonName(String drugTypeString)
	{
		if (drugTypeString == null || !_drugTypeList.containsKey(drugTypeString)) {
			return null;
		} else {
			return _drugTypeList.get(drugTypeString);
		}
	}



	// the default drug type when a drug is first created
	private DrugType _defaultDrugType = null;
	
	public DrugType getDefaultDrugType()
	{
		return _defaultDrugType;
	}

	public void cleanJsonPrefs(JSONObject jsonDrugPrefs)
	{
		for (DrugType drugType: _drugTypeList.values()) {
			drugType.cleanJsonPrefs(jsonDrugPrefs);
		}
	}

	///////////////////////////////////////
	
	public DrugTypeList_Standard()
	{
		_drugTypeList = new HashMap<>();
		_addStandardDrugTypeList();
	}
	

	private void _addGncDrugTypeList()
	{
		_addDrugType(new DrugType(
				"*Pill",
				"pill",
				"Pill",
				"Pills",
				"Pills",
				new DoseFieldType_Numeric("Pills per Serving", "*numpills", 2, 1),
				new DoseFieldType_NumericWithUnits("Pill Strength", "*dose", 4, 2, Arrays.asList("g", "*mg", "mcg", "units", "IU")),
				null
				));
		
		_addDrugType(new DrugType(
				"Pak",
				"supplementPak",
				"Pak",
				"Paks",
				"Paks",
				new DoseFieldType_Numeric("Paks per Serving", "paksPerServing", 2, 0),
				new DoseFieldType_Numeric("Pills per Pak", "pillsPerPak", 2, 0),
				null
				));

		_addDrugType(new DrugType(
				"Drink",
				"supplementDrink",
				"Drink",
				"Drinks",
				"Volume",
				new DoseFieldType_NumericWithUnits("Drink Volume", "drinkVolume", 3, 1, Arrays.asList("mL", "tsp", "tbsp", "oz")),
				new DoseFieldType_NumericWithUnits("Drink Strength", "drinkStrength", 4, 2, Arrays.asList("g/mL", "mg/mL", "mg/tsp", "mg/tbsp", "mg/oz")),
				null
				));
		
		_addDrugType(new DrugType(
				"Shot",
				"supplementShot",
				"Shot",
				"Shots",
				"Shots",
				new DoseFieldType_Numeric("Number of Shots", "numShots", 2, 0),
				new DoseFieldType_NumericWithUnits("Shot Strength", "shotStrength", 4, 2, Arrays.asList("g/mL", "mg/mL", "mg/tsp", "mg/tbsp", "mg/oz")),
				null
				));

		_addDrugType(new DrugType(
				"Chew",
				"supplementChew",
				"Chew",
				"Chews",
				"Chews",
				new DoseFieldType_Numeric("Number of Chews", "numChews", 2, 0),
				null,
				null
				));
		
		_addDrugType(new DrugType(
				"Gel",
				"supplementGel",
				"Gel",
				"Gels",
				"Gels",
				new DoseFieldType_Numeric("Number of Gels", "numGels", 2, 0),
				null,
				null
				));
		
		_addDrugType(new DrugType(
				"Scoop",
				"supplementScoop",
				"Scoop",
				"Scoops",
				"Scoops",
				new DoseFieldType_Numeric("Scoops per Serving", "scoopsPerServing", 2, 0),
				new DoseFieldType_NumericWithUnits("Scoop Volume", "scoopVolume", 3, 1, Arrays.asList("mL", "tsp", "tbsp", "oz", "g")),
				null
				));
		
			
		_addDrugType(new DrugType(
				"Bar",
				"supplementBar",
				"Bar",
				"Bars",
				"Bars",
				new DoseFieldType_Numeric("Number of Bars", "numBars", 2, 0),
				new DoseFieldType_NumericWithUnits("Bar Volume", "barVolume", 3, 1, Arrays.asList("ml", "tsp", "tbsp", "oz")),
				null
				));
		
		_addDrugType(new DrugType(
				"Serving",
				"supplementServing",
				"Serving",
				"Servings",
				"Servings",
				new DoseFieldType_Numeric("Number of Servings", "numServings", 2, 0),
				new DoseFieldType_NumericWithUnits("Serving Strength", "servingStrength", 3, 1, Arrays.asList("mL", "tsp", "tbsp", "oz")),
				null
				));
		
	}
	
	
	@SuppressWarnings("unchecked")
	private void _addStandardDrugTypeList()
	{
		_addDrugType(new DrugType(
				"Drop",
				"drop",
				"Drop",
				"Drops",
				"Drops",
				new DoseFieldType_Numeric("Drops per Dose", "numDrops", 2, 0),
				new DoseFieldType_NumericWithUnits("Drop Strength", "dropStrength", 4, 3, Arrays.asList("%", "IU")),
				new DoseFieldType_MultipleChoice("Drop Location", "dropLocation", Arrays.asList(
                        new Pair<>("mouth", "Mouth"),
                        new Pair<>("leftEye", "Left eye"),
                        new Pair<>("rightEye", "Right eye"),
                        new Pair<>("eachEye", "Each eye"),
                        new Pair<>("leftEar", "Left ear"),
                        new Pair<>("rightEar", "Right ear"),
                        new Pair<>("eachEar", "Each ear"),
                        new Pair<>("scalp", "Scalp"),
                        new Pair<>("leftNostril", "Left nostril"),
                        new Pair<>("rightNostril", "Right nostril"),
                        new Pair<>("eachNostril", "Each nostril")
						))
				));

		_addDrugType(new DrugType(
				"Inhaler",
				"inhaler",
				"Puff",
				"Puffs",
				"Puffs",
				new DoseFieldType_Numeric("Puffs per Dose", "numPuffs", 2, 0),
				new DoseFieldType_NumericWithUnits("Inhaler Strength", "inhalerStrength", 3, 3, Arrays.asList("mcg", "mg/mL", "g/mL", "mg")),
				null
				));
		
		_addDrugType(new DrugType(
				"Injection",
				"injection",
				"Injection",
				"Injections",
				"Amount",
				new DoseFieldType_NumericWithUnits("Injection Amount", "injectionAmount", 3, 2, Arrays.asList("mL", "units", "IU", "cc", "mcg")),
				new DoseFieldType_NumericWithUnits("Injection Strength", "injectionStrength", 4, 2, Arrays.asList("mg/mL", "units/mL", "IU/mL", "mg/cc", "units/cc", "IU/cc")),
				null
				));

		_addDrugType(new DrugType(
				"Infusion",
				"infusion",
				"Infusion",
				"Infusion",
				"Amount",
				new DoseFieldType_NumericWithUnits("Infusion Volume", "infusionVolume", 4, 2, Arrays.asList("mL", "units", "IU", "cc", "mcg")),
				new DoseFieldType_NumericWithUnits("Infusion Strength", "infusionStrength", 4, 2, Arrays.asList("mg/mL", "units/mL", "IU/mL", "mg/cc", "units/cc", "IU/cc")),
				new DoseFieldType_NumericWithUnits("Infusion Rate", "infusionRate", 4, 2, Arrays.asList("mL/day", "*mL/hr", "mL/min", "cc/day", "cc/hr", "cc/min", "units/day", "units/hr", "units/min"))
				));

		_addDrugType(new DrugType(
				"Liquid",
				"liquidDose",
				"Liquid Dose",
				"Liquid Dose",
				"Volume",
				new DoseFieldType_NumericWithUnits("Dose Volume", "liquidDoseVolume", 3, 2, Arrays.asList("mL", "tsp", "tbsp", "oz")),
				new DoseFieldType_NumericWithUnits("Dose Strength", "liquidDoseStrength", 4, 2, Arrays.asList("g/mL", "mg/mL", "mg/tsp", "mg/tbsp", "mg/oz", "IU")),
				null
				));
		
		_addDrugType(new DrugType(
				"Ointment",
				"ointment",
				"Ointment Application",
				"Ointment Application",
				"Applications",
				null,
				new DoseFieldType_NumericWithUnits("Ointment Strength", "ointmentStrength", 2, 2, Arrays.asList("%")),
				null
				));
		
		_addDrugType(new DrugType(
				"Patch",
				"patch",
				"Patch",
				"Patches",
				"Patches",
				new DoseFieldType_Numeric("Patches per Dose", "numPatches", 2, 0),
				new DoseFieldType_NumericWithUnits("Patch Strength", "patchStrength", 4, 2, Arrays.asList("mg/hr", "mcg/hr", "mg/24hr", "mg")),
				null
				));
		
		_addDrugType(new DrugType(
				"*Pill",
				"pill",
				"Pill",
				"Pills",
				"Pills",
				new DoseFieldType_Numeric("Pills per Dose", "*numpills", 2, 2),
				new DoseFieldType_NumericWithUnits("Pill Strength", "*dose", 5, 2, Arrays.asList("g", "*mg", "mcg", "units", "IU", "meq")),
				null
				));

		_addDrugType(new DrugType(
				"Powder",
				"powder",
				"Powder",
				"Powder",
				"Powder",
				new DoseFieldType_NumericWithUnits("Powder Per Dose", "powderPerDose", 4, 2, Arrays.asList("mcg", "*g", "kg", "mL", "L", "meq")),
				new DoseFieldType_NumericWithUnits("Powder Strength", "powderStrength", 4, 2, Arrays.asList("mg", "g", "meq", "IU")),
				null
				));

		_addDrugType(new DrugType(
				"Spray",
				"spray",
				"Spray",
				"Sprays",
				"Sprays",
				new DoseFieldType_Numeric("Sprays per Dose", "numSprays", 2, 0),
				new DoseFieldType_NumericWithUnits("Spray Strength", "sprayStrength", 3, 3, Arrays.asList("mg", "mcg", "mL")),
				new DoseFieldType_MultipleChoice("Spray Location", "sprayLocation", Arrays.asList(
                        new Pair<>("mouth", "Mouth"),
                        new Pair<>("leftEye", "Left eye"),
                        new Pair<>("rightEye", "Right eye"),
                        new Pair<>("eachEye", "Each eye"),
                        new Pair<>("leftEar", "Left ear"),
                        new Pair<>("rightEar", "Right ear"),
                        new Pair<>("eachEar", "Each ear"),
                        new Pair<>("scalp", "Scalp"),
                        new Pair<>("leftNostril", "Left nostril"),
                        new Pair<>("rightNostril", "Right nostril"),
                        new Pair<>("eachNostril", "Each nostril")
						))
				));
		
		_addDrugType(new DrugType(
				"Suppository",
				"suppository",
				"Suppository",
				"Suppositories",
				"Suppositories",
				null,
				new DoseFieldType_NumericWithUnits("Suppository Strength", "suppositoryStrength", 4, 2, Arrays.asList("mg", "mcg", "g")),
				null
				));
		
		_addDrugType(new DrugType(
				"!Medication",
				MANAGED,
				"Dose",
				"Doses",
				"Doses",
				null,
				new DoseFieldType_FreeText("Dosage", "managedDescription"),
				null
				));
	}

}
