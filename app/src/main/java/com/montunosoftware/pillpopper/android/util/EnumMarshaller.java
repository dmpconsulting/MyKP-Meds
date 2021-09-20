package com.montunosoftware.pillpopper.android.util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.EnumMap;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.List;


public class EnumMarshaller<EnumType extends Enum<EnumType>>
{
	private HashMap<String, EnumType> _jsonToEnum;
	private EnumMap<EnumType, String> _enumToJson;
	private HashMap<String, String> _jsonToList;
	Class<EnumType> _enumClass;

	@SuppressWarnings("unchecked")
	public EnumMarshaller(List<Pair<EnumType, String>> list)
	{
		_enumClass = (Class<EnumType>) list.get(0).getLeft().getClass();
		String className = _enumClass.getSimpleName();

		PillpopperLog.say("Creating marshaller for %s", className);

		_enumToJson = new EnumMap<>(_enumClass);
		_jsonToEnum = new HashMap<>();
		_jsonToList= new HashMap<>();

		for (Pair<EnumType, String> entry: list) {
			if (_jsonToEnum.containsKey(entry.getRight())) {
				PillpopperLog.say("repeated json string %s inserted for %s marshaller", entry.getRight(), className);
				//System.exit(0);
			}

			if (_enumToJson.containsKey(entry.getLeft())) {
				PillpopperLog.say("repeated enum %s inserted for %s marshaller", entry.getLeft(), className);
				//System.exit(0);
			}

			_jsonToEnum.put(entry.getRight(), entry.getLeft());		
			_jsonToList.put(entry.getRight().toString(), entry.getLeft().toString());		
			_enumToJson.put(entry.getLeft(), entry.getRight());
		}

		// http://stackoverflow.com/questions/2205891/iterate-enum-values-using-java-generics
		if (_enumClass != null) {
			for (EnumType value: _enumClass.getEnumConstants()) {
				if (!_enumToJson.containsKey(value)) {
					PillpopperLog.say("%s marshal map doesn't contain %s!", className, value.toString());
					//System.exit(0);
				}
			}
		}
	}

	// This method adds a backwards-compatibility representation:
	// an old text type that can be parsed but is never emitted.
	public EnumMarshaller<EnumType> addBackCompat(String oldKey, EnumType value)
	{
		_jsonToEnum.put(oldKey, value);
		_jsonToList.put(oldKey, toString(value));
		return this;
	}

	// parse a single enum
	public EnumType fromString(String s, EnumType defaultValue)
	{
		if (s != null && _jsonToEnum.containsKey(s))
		{
			_jsonToList.get(s);
			return _jsonToEnum.get(s);
		}
		else{
			return defaultValue;
		}
	}

	// Parse a comma-separated list of the enums
	public EnumSet<EnumType> fromStringList(String s)
	{
		EnumSet<EnumType> enumSet = EnumSet.noneOf(_enumClass);

		if (s != null) {
			String[] tokens = s.split(",");

			for (String token: tokens) {
				if (_jsonToEnum.containsKey(token)) {
					enumSet.add(_jsonToEnum.get(token));
				}
			}
		}

		return enumSet;
	}

	// parse a single enum out of a json object
	public EnumType fromJson(JSONObject o, String key, EnumType defaultValue)
	{
		return fromString(Util.parseJSONStringOrNull(o, key), defaultValue);
	}

	public EnumSet<EnumType> fromJsonStringList(JSONObject o, String key)
	{
		return fromStringList(Util.parseJSONStringOrNull(o, key));
	}

	////////////// generating strings/json

	// generate a single, stand-alone string
	public String toString(EnumType e)
	{
		return _enumToJson.get(e);
	}

	// generate a list of strings, comma-separated
	public String toString(Collection<EnumType> enumList)
	{
		StringBuilder sb = new StringBuilder();

		for (EnumType e: enumList) {
			if (sb.length() > 0) {
				sb.append(",");
			}
			sb.append(toString(e));
		}

		return sb.toString();
	}

	// marshal a single enum to a json object
	public void marshal(JSONObject o, String key, EnumType value) throws JSONException
	{
		o.put(key, toString(value));
	}


	// marshal a list to a json object
	public void marshal(JSONObject o, String key, Collection<EnumType> enumList) throws JSONException
	{
		o.put(key, toString(enumList));
	}


	///////////////For weekly String//////////////////////////////////////////

	public void marshalWeekString(JSONObject o, String key, List<String> dayofWeeks_String) throws JSONException
	{

			String data = getMappedDaysToDayNumber(dayofWeeks_String);
			if(!("").equals(data)){
				o.put(key,getMappedDaysToDayNumber(dayofWeeks_String));
			}
	}

	private String getMappedDaysToDayNumber(List<String> dayofWeeks_String)
	{
		String key = dayofWeeks_String.toString();
		if (key.toString().contains("Daily") || key.toString().contains("Monthly") || key.toString().contains("Every")
				|| key.toString().contains("Mo") || key.toString().contains("Tu") || key.toString().contains("We")
				|| key.toString().contains("Th") || key.toString().contains("Fr") || key.toString().contains("Sa")
				|| key.toString().contains("Su")) {
			return "";
		} else {
			if (key.contains(",")) {
				StringBuilder selectedDaysInWeeks = new StringBuilder();
				String[] splitWeek = key.split(",");
				for (String s : splitWeek) {
					if (s.contains("Mo")) {
						selectedDaysInWeeks.append(2 + ",");
					}
					if (s.contains("Tu")) {
						selectedDaysInWeeks.append(3 + ",");
					}
					if (s.contains("We")) {
						selectedDaysInWeeks.append(4 + ",");
					}
					if (s.contains("Th")) {
						selectedDaysInWeeks.append(5 + ",");
					}
					if (s.contains("Fr")) {
						selectedDaysInWeeks.append(6 + ",");
					}
					if (s.contains("Sa")) {
						selectedDaysInWeeks.append(7 + ",");
					}
					if (s.contains("Su")) {
						selectedDaysInWeeks.append(1 + ",");
					}
				}

				String week = selectedDaysInWeeks.toString();
				String check = week.replace("[", "");
				String uncheck = check.replace("]", "");
				PillpopperLog.say("uncheck ....dayofWeeks_String" + uncheck);
				
				String returnStr = "";
				
				if(uncheck.length() > 0){
					returnStr = uncheck.substring(0, uncheck.length() - 1);
				}
				
				return returnStr;
			} else {
				return "";
			}
		}
	}

	public List<String> fromJsonStringListWeek(JSONObject o, String key)
	{

		if (key.toString().contains("Daily") || key.toString().contains("Monthly") || key.toString().contains("Every")
				|| key.toString().contains("Mo") || key.toString().contains("Tu") || key.toString().contains("We")
				|| key.toString().contains("Th") || key.toString().contains("Fr") || key.toString().contains("Sa")
				|| key.toString().contains("Su")) {
			return fromStringListWeekly(Util.parseJSONStringOrNull(o, key));
		} else {
			if (key.contains(",")) {
				StringBuilder selectedDaysInWeeks = new StringBuilder();
				String[] splitWeek = key.split(",");
				for (String s : splitWeek) {
					if (s.contains("2")) {
						selectedDaysInWeeks.append("Mo" + ",");
					}
					if (s.contains("3")) {
						selectedDaysInWeeks.append("Tu" + ",");
					}
					if (s.contains("4")) {
						selectedDaysInWeeks.append("We" + ",");
					}
					if (s.contains("5")) {
						selectedDaysInWeeks.append("Th" + ",");
					}
					if (s.contains("6")) {
						selectedDaysInWeeks.append("Fr" + ",");
					}
					if (s.contains("7")) {
						selectedDaysInWeeks.append("Sa" + ",");
					}
					if (s.contains("1")) {
						selectedDaysInWeeks.append("Su" + ",");
					}
				}

				String week = selectedDaysInWeeks.toString();
				String check = week.replace("[", "");
				String uncheck = check.replace("]", "");
				PillpopperLog.say("uncheck ....dayofWeeks_String" + uncheck);
				return fromStringListWeekly(Util.parseJSONStringOrNull(o, uncheck.substring(0, uncheck.length() - 1)));
			} else {
				return fromStringListWeekly(Util.parseJSONStringOrNull(o, key));
			}
		}
	}
	public List<String> fromStringListWeekly(String s)
	{    
		List<String> enumSet = new ArrayList<>();
		if (s != null) {
			String[] tokens = s.split(",");
			enumSet.addAll(Arrays.asList(tokens));
		}		
		return enumSet;
	}
}
