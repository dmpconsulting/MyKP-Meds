package com.montunosoftware.pillpopper.model;

import com.montunosoftware.pillpopper.android.util.PillpopperParseException;
import com.montunosoftware.pillpopper.android.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;


public class Preferences
{
	////////////////////////////////////////////////////////////////
	// copy support
	
	public synchronized Preferences copy()
	{
		Preferences copy = new Preferences();
		
		copy._stateUpdatedListeners = _stateUpdatedListeners; // share this
		copy._prefDict = new LinkedHashMap<>(_prefDict);
		copy._longPrefDict = new LinkedHashMap<>(_longPrefDict);

		return copy;
	}
	
	
	
	////////////////////////////////////////////////////////////////////
	/// constructors 

	private Map<String, String> _prefDict = new LinkedHashMap<>();

	
	public Preferences()
	{
	}
	
	@SuppressWarnings("unchecked")
	public Preferences(JSONObject jsonPreferences) throws PillpopperParseException
	{
		try {
			if (jsonPreferences == null) {
				return;
			}
				
			Iterator<String> i = jsonPreferences.keys();
		
			while (i.hasNext()) {
				String key = i.next();
				String val = Util.cleanString(jsonPreferences.getString(key));
				
				if (val != null) {
					_prefDict.put(key, val);
				}
			}
		} catch (JSONException e) {
			throw new PillpopperParseException("JSON error parsing preferences array");
		}
	}

	public synchronized JSONObject marshal() throws JSONException
	{
		JSONObject jsonPreferences = new JSONObject();
		
		for (Map.Entry<String,String> entry: _prefDict.entrySet()) {
			jsonPreferences.put(entry.getKey(), entry.getValue());
		}
		
		_marshalLongs(jsonPreferences);
		
		return jsonPreferences;
	}

	
	public String getPreference(String key)
	{
		if (_prefDict.containsKey(key))
			return _prefDict.get(key);
		else
			return null;
	}


	public void setPreference(String key, String value)
	{
		value = Util.cleanString(value);
		
		if (value == null) {
			_prefDict.remove(key);
		} else {
			_prefDict.put(key, value);
		}
		
		_stateUpdated();
	}

	public void removePreference(String key)
	{
		setPreference(key, null);
	}



	private static final String _boolTrueString = "1";
	private static final String _boolFalseString = "0";

	public static String jsonBooleanString(boolean state)
	{
		return state == true ? _boolTrueString : _boolFalseString;
	}
	
	public boolean getBoolean(String key, boolean defaultState)
	{
		String boolString = getPreference(key);
		
		if (boolString == null)
			return defaultState;
		
		if (boolString.equals(_boolTrueString))
			return true;
		
		if (boolString.equals(_boolFalseString))
			return false;
		
		return defaultState;
	}
	
	public void setBoolean(String key, boolean state)
	{
		setPreference(key, jsonBooleanString(state));
	}

	
	////////////     Support for numeric preferences     //////////////////
	
	// Since the database requires all preferences to be stored as strings, but
	// we have a lot of numeric preferences, these functions provide some impedence-matching.
	// The trivial way might be to just convert every int out to a string each time it's set,
	// and parse it from a string back to an int every time it's accessed.  But this seems
	// slow, so here we create an in-memory-only map of numeric preferences.  Trying to read
	// a numeric preference first checks this map; if not there, we try to parse out of the string.
	// Sets stay in the numeric map until the preferences are marshalled.
	
	private HashMap<String, Long> _longPrefDict = new LinkedHashMap<>();
	
	public long getLong(String key, long defaultValue)
	{
		// Do we already have the value parsed in memory?  If so, return it.
		if (_longPrefDict.containsKey(key)) {
			return _longPrefDict.get(key);
		}
		
		// Is there a string preference we haven't yet lazily parsed?
		long val = Util.parseNonnegativeLong(this.getPreference(key));
		
		// If there was no preference, use the default
		if (val < 0) {
			val = defaultValue;
		}
		
		// Store the parsed (or default) value in the map and return it
		_longPrefDict.put(key, val);
		return val;
	}
	
	public void setLong(String key, long value)
	{
			_longPrefDict.put(key, value);
			_stateUpdated();
	}
	
	
	private synchronized void _marshalLongs(JSONObject jsonPreferences) throws JSONException
	{
	    // TPMGMA-54 -Fix
		Map<String, Long> map = new LinkedHashMap<>(_longPrefDict);
		for (Map.Entry<String,Long> entry: map.entrySet()) {
			
			if(("secondaryReminderPeriodSecs").equalsIgnoreCase(entry.getKey())) {
				if(!Util.isCreateUserRequestInprogress()) {
					jsonPreferences.put(entry.getKey(), String.format(Locale.US, "%d", entry.getValue()));
				}
			} else {
				jsonPreferences.put(entry.getKey(), String.format(Locale.US, "%d", entry.getValue()));
			}
		}
	}
	
	/////// State-Updated Callbacks ////////////////////////////////////
	
	// A list of guys that want to get notified every time our state is updated
	List<StateUpdatedListener> _stateUpdatedListeners = new ArrayList<>();
	
	public void registerStateUpdatedListener(StateUpdatedListener stateUpdatedListener)
	{
		_stateUpdatedListeners.add(stateUpdatedListener);
	}

	public void unregisterStateUpdatedListener(StateUpdatedListener stateUpdatedListener)
	{
		_stateUpdatedListeners.remove(stateUpdatedListener);
	}

	
	// This is called whenever the state is updated.
	private void _stateUpdated()
	{
		// Notify any listeners of the update so they can update views
//		Iterator<StateUpdatedListener> iterator = _stateUpdatedListeners.iterator();
//			while (iterator.hasNext()) {
//				synchronized (this) {
//					StateUpdatedListener sul = iterator.next();
//					sul.onStateUpdated();
//				}
//
//		}
	}

}	
