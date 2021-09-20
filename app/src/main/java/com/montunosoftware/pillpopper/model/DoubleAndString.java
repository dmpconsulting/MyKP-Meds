package com.montunosoftware.pillpopper.model;

import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Locale;
import java.util.NoSuchElementException;
import java.util.Scanner;

//
// Immutable double and string, coupled
//
public class DoubleAndString
{
	private double _doubleVal;
	private String _stringVal;

	public double getDouble() { return _doubleVal; }
	public String getString() { return _stringVal; }
	
	// Given a JSON object with a string key of the form "123.42 mg",
	// parse it into its double and string components.
	public static DoubleAndString parseJSON(JSONObject o, String key)
	{
		double doubleVal = -1;
		String stringVal = null;

		String val = Util.parseJSONStringOrNull(o, key);

		if (val == null) {
			return null;
		}

		Scanner s = new Scanner(val);

		try {
			doubleVal = Util.parseNonnegativeDouble(s.next());
			stringVal = s.next();
		} catch (NoSuchElementException e) {
			PillpopperLog.say(e.getMessage());
		}
		s.close();

		return new DoubleAndString(doubleVal, stringVal);
	}
	
	public DoubleAndString(Double doubleVal, String stringVal)
	{
		_doubleVal = doubleVal;
		_stringVal = stringVal;
	}
	
	public String getMarshalledString()
	{
		// numeric or mixed numeric-and-units strings
		StringBuilder sb = new StringBuilder();
		
		sb.append(String.format(Locale.US, "%f", _doubleVal));
		
		if (_stringVal != null) {
			sb.append(" ");
			sb.append(_stringVal);
		}
		
		return sb.toString();
	}
	
	public static void marshal(JSONObject o, String key, DoubleAndString das) throws JSONException
	{
		if (das != null) {
			o.put(key, das.getMarshalledString());
		}
	}

}
