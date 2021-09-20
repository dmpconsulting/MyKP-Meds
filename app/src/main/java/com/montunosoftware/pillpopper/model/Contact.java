package com.montunosoftware.pillpopper.model;

import android.app.Activity;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.provider.ContactsContract.CommonDataKinds.Phone;
import android.provider.ContactsContract.CommonDataKinds.StructuredPostal;

import com.montunosoftware.pillpopper.android.util.UniqueDeviceId;
import com.montunosoftware.pillpopper.android.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Vector;

public class Contact
{

	private String _hardwareID;
	// Note: caution, if you decide to compare these for equality,
		// that Util.getHardwareId can return "deadbeef" for a nonsense HWID.
		// Two "deadbeef"s should compare UNEQUAL. (Once we get there,
		// I suggest wrapping the hwid string in a class that knows how to
		// do the comparison sensibly.
	
	private String _contactID;
	public String getContactID() { return _contactID; }
	
	private String _name;
	
	public static String getName(Contact c)
	{
		if (c == null)
			return null;
		else
			return c._name;
	}
	
	public Contact(
			String hardwareID,
			String contactID,
			String name)
	{
		_hardwareID = hardwareID;
		_contactID = contactID;
		_name = name;
	}
	
	

	public static Contact parseJSON(JSONObject o, String key)
	{
		String triple = Util.parseJSONStringOrNull(o, key);
		if (triple==null) {
			return null;
		}
		
		String[] pieces = triple.split(":");
		if (pieces.length != 3)
		{
			// TODO this is an unexpected event; maybe I should log it?
			return null;
		}
		//PillpopperLog.Say("Contact parsed "+triple+" into "+pieces.toString());
		return new Contact(pieces[0], pieces[1], pieces[2]);
	}
	
	public void marshal(JSONObject o, String key) throws JSONException
	{
		String value = _hardwareID + ":" + _contactID + ":" + _name;
		o.put(key, value);
	}


	public Contact copy()
	{
		return new Contact(_hardwareID, _contactID, _name);
	}
	
	public Contact updateName(Activity act)
	{
		if (UniqueDeviceId.getHardwareId(act).equals(_hardwareID)) {
			String updatedName = lookupName(act.getBaseContext());
			if (!("").equals(updatedName)) {
				return new Contact(_hardwareID, _contactID, updatedName);
			} else {
				// this record is apparently not present on this device; keep the
				// existing value.
			}
		} else {
			// the contact wasn't recorded on this device; don't fuss with it.
			// TODO are we supposed to do some sort of fuzzy matching? Gulp.
		}
		return null;
	}

	public Vector<PhoneRecord> lookupPhoneNumbers(Activity act)
	{
		Vector<PhoneRecord> records = new Vector<>();
		Vector<String[]> rawRecords = lookupFields(
				act.getBaseContext(), Phone.CONTENT_URI,
				new String[] { Phone.NUMBER, Phone.TYPE });
		for (String[] rawRecord : rawRecords) {
			String type = act.getString(Phone.getTypeLabelResource(Integer.parseInt(rawRecord[1])));
			records.add(new PhoneRecord(rawRecord[0], type));
		}
		return records;
	}
	
	public Vector<AddressRecord> lookupAddresses(Activity act)
	{
		Vector<AddressRecord> records = new Vector<>();
		Vector<String[]> rawRecords = lookupFields(
				act.getBaseContext(), StructuredPostal.CONTENT_URI,
				new String[] { StructuredPostal.FORMATTED_ADDRESS, StructuredPostal.TYPE });
		for (String[] rawRecord : rawRecords) {
			String type = act.getString(StructuredPostal.getTypeLabelResource(Integer.parseInt(rawRecord[1])));
			records.add(new AddressRecord(rawRecord[0], type));
		}
		return records;
	}	
	
	private String lookupName(Context context)
	{
		Vector<String[]> nameRows = lookupFields(context, Phone.CONTENT_URI,
				new String[] { Phone.DISPLAY_NAME });
		if (nameRows.isEmpty()) {
			return "";
		}
		return nameRows.get(0)[0];
	}

	private Vector<String[]> lookupFields(Context context, Uri uri, String[] field_keys)
	{
		Vector<String[]> valueRows = new Vector<>();

		Cursor cursor = context.getContentResolver().query(
				uri,
				null /* projection */,
				Phone.LOOKUP_KEY + " = ?",
				new String[] { _contactID },
				null /* sort */);
		try {
			while (cursor.moveToNext())
			{
				String[] values = new String[field_keys.length];
				for (int i=0; i<field_keys.length; i++) {
					String field_key = field_keys[i];
//					if (field_key.equals(Phone.NUMBER)) {
//						PillpopperLog.Say("Columns:");
//						for (String s : cursor.getColumnNames()) {
//							PillpopperLog.Say("  "+s+":"+cursor.getString(cursor.getColumnIndex(s)));
//						}
//					}
					String fieldValue = cursor.getString(cursor.getColumnIndex(field_key));
					if (fieldValue==null) {
						fieldValue = "";
					}
					values[i] = fieldValue;
				}
				valueRows.add(values);
			}
		} finally {
			cursor.close();
		}
		return valueRows;
	}
}

