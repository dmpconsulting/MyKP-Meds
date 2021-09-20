package com.montunosoftware.pillpopper.model;

import com.montunosoftware.pillpopper.android.util.Util;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Scanner;


//
// This class defines a list of items that have both a string representation and an ID that tags them.
// The list is stored in Preferences.
// It uses slightly complex generics to avoid replicating code, because the same pattern is used in two places:
//
// 1) Who is taking a drug. People (represented with a Person object) have string names and are tagged with IDs.
//    The person list is stored in the preferences.
// 2) Custom drug types. Such types (represented with a DrugType_Custom object) also have string names and are
//    tagged with IDs.
// 
// The activity that allows users to edit such lists, EditStringListActivity, takes one of these
// editable string lists as input. The members of the list (i.e. Person and DrugType_Custom)
// must implement the EditableStringItem interface, which gives the edit screen an interface for
// changing item names and determining whether items can be deleted.
//

public class EditableStringList<ItemType extends EditableStringItem> implements StateUpdatedListener
{
	private final String _jsonBaseName;
	private final int _singularNameStringId;
	private final int _pluralNameStringId;
	private StateUpdatedListener _stateUpdatedListener = null;
	private final ItemFactory<ItemType> _factory;
	
	private static final String _JSON_COUNT_EXTENSION = "Count";

	private Map<String, ItemType> _itemMap = new LinkedHashMap<>();

	// Initialize a new, empty list 
	public EditableStringList(
			String jsonBaseName,
			int singularNameStringId,
			int pluralNameStringId,
			StateUpdatedListener stateUpdatedListener,
			ItemFactory<ItemType> factory)
	{
		_jsonBaseName = jsonBaseName;
		_singularNameStringId = singularNameStringId;
		_pluralNameStringId = pluralNameStringId;
		_stateUpdatedListener = stateUpdatedListener;
		_factory = factory;
	}

	
	////// basic accessors
	
	public int getItemNameStringId()
	{
		return _singularNameStringId;
	}
	
	public int getPluralNameStringId()
	{
		return _pluralNameStringId;
	}
	
	public int length()
	{
		return _itemMap.size();
	}

	// Returns a Person if one is defined with that ID; null otherwise
	public ItemType getItemById(String id)
	{
		if (_itemMap.containsKey(id)) {
			return _itemMap.get(id);
		} else {
			return null;
		}
	}


	//////// marshalling and parsing
	
	
	private String _getCountJsonName()
	{
		return _jsonBaseName + _JSON_COUNT_EXTENSION;
	}
	
	private String _getItemJsonName(long i)
	{
		return String.format(Locale.US, "%s%d", _jsonBaseName, i);
	}
	
	
	public void marshal(JSONObject jsonPrefs) throws JSONException
	{
		Util.putJSONStringFromLong(jsonPrefs, _getCountJsonName(), _itemMap.size());

		long i = 0;
		for (Map.Entry<String, ItemType> mapItem: _itemMap.entrySet()) {
			ItemType item = mapItem.getValue();
			jsonPrefs.put(_getItemJsonName(i), String.format(Locale.US, "%s:%s", item.getId(), item.toString()));
			i++;
		}
	}
	

	// Parse the person list out of the preferences block
	public void parse(Preferences preferences)
	{
		// might return -1 if the key is not found
		long numPeople = Util.parseNonnegativeLong(preferences.getPreference(_getCountJsonName()));
		
		if (numPeople > 0) { 
			for (long i = 0; i < numPeople; i++) {
				_parseAndAddItem(preferences, i);
			}
		}
	}
	
	private void _parseAndAddItem(Preferences preferences, long i)
	{
		String prefString = preferences.getPreference(_getItemJsonName(i));
		
		if (prefString == null)
			return;
		
		Scanner scanner = new Scanner(prefString);
		scanner.useDelimiter(":");

		try {
			String id = scanner.next();
			String name = scanner.next();
			_itemMap.put(id, _factory.create(id, name, this));
		} catch (NoSuchElementException e) {
			return;
		} finally {
			scanner.close();
		}
	}

	///// state update callbacks
	
	private void _update()
	{
		if (_stateUpdatedListener != null) {
			_stateUpdatedListener.onStateUpdated();
		}
	}

	@Override
	public void onStateUpdated()
	{
		// one of the underlying "Person" objects changed.
		// propagate the change notification upwards.
		_update();
	}


	/////////  creating and deleting items
	
	public ItemType createNewItem(String name)
	{
		String id = Util.getRandomGuid(8); // ugh, wish this was longer, but trying to maintain compatibility with the iOS version 

		ItemType newItem = _factory.create(id, name, this);
		_itemMap.put(id, newItem);
		_update();
		return newItem;
	}

	public void deleteItem(EditableStringItem item)
	{
		String id = item.getId();
		
		if (_itemMap.containsKey(id)) {
			_itemMap.remove(id);
			_update();
		}
	}

	//////////////////////////////////////////////////////////////////////////
	// Editable String List interface implementation

	private class AlphabeticalItemComparator implements Comparator<ItemType>
	{
		@Override
		public int compare(ItemType lhs, ItemType rhs)
		{
			return lhs.toString().compareToIgnoreCase(rhs.toString());
		}
	}
	
	public List<ItemType> getSortedList()
	{
		ArrayList<ItemType> sortedList = new ArrayList<>(_itemMap.values());
		Collections.sort(sortedList, new AlphabeticalItemComparator());
		return sortedList;
	}

	public Collection<ItemType> getCollection()
	{
		return _itemMap.values();
	}
}
