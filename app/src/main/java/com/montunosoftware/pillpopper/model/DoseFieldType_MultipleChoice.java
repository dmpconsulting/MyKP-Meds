package com.montunosoftware.pillpopper.model;

import android.content.Intent;

import com.montunosoftware.pillpopper.android.PickListActivity;
import com.montunosoftware.pillpopper.android.PillpopperActivity;
import com.montunosoftware.pillpopper.android.util.Pair;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.android.view.PickListView;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

//
// Dosage fields that are multiple choices -- for example, the location of a drop ("left eye", "right eye")
//
public class DoseFieldType_MultipleChoice extends DoseFieldType
{
	List<Pair<String, String>> _jsonAndDisplayList;
	HashMap<String, String> _jsonToDisplayMap = new HashMap<>();
	
	public DoseFieldType_MultipleChoice(String configDescription, String jsonName, List<Pair<String, String>> list)
	{
		super(configDescription, jsonName);
		_jsonAndDisplayList = list;
		
		for (Pair<String, String> jsonAndDisplay: list) {
			_jsonToDisplayMap.put(jsonAndDisplay.getLeft(), jsonAndDisplay.getRight());
		}
	}

	@Override
	public String _getDisplayString(DoubleAndString data, boolean useNoun)
	{
		return _jsonToDisplayMap.get(data.getString());
	}

	@Override
	protected void _startEditActivity(PillpopperActivity act, DoubleAndString data, int resultCode)
	{
		List<PickListView.MenuItem> pickMenu = new ArrayList<>();
		
		for (Pair<String, String> jsonAndDisplay: _jsonAndDisplayList) {
			pickMenu.add(new PickListView.MenuItem(jsonAndDisplay.getRight(), jsonAndDisplay.getLeft()));
		}
		
		new PickListActivity.Builder(act, pickMenu, resultCode)
			.setTitle(getConfigDescription())
			.allowClear()
			.setInitialSelection(data == null ? null : data.getString())
			.start();
	}

	// Given a json name, return a DoubleAndString wrapping it;
	// make sure it's a valid choice.
	private DoubleAndString _boxString(String s)
	{
		s = Util.cleanString(s);
		
		if (s == null)
			return null;
		
		if (!_jsonToDisplayMap.containsKey(s)) {
			PillpopperLog.say("DoseFieldType_MultipleChoice: bug: got invalid choice '%s'!", s);
			return null;
		}
			
		return new DoubleAndString(0.0, s);
	}
	
	@Override
	protected DoubleAndString _getEditedValue(PillpopperActivity act, Intent intent)
	{
		return _boxString(PickListActivity.getReturnString(act, intent));
	}

	@Override
	protected String _getMarshalledString(DoubleAndString data)
	{
		return data.getString();
	}

	@Override
	protected DoubleAndString _parseJson(JSONObject o, String key)
	{
		return _boxString(Util.parseJSONStringOrNull(o, key));
	}

}
