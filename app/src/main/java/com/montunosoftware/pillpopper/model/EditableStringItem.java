package com.montunosoftware.pillpopper.model;

import com.montunosoftware.pillpopper.android.PillpopperActivity;

public interface EditableStringItem
{
	String getId();
	
	void setName(String newName);
	/* we use toString as getName() */

	String canBeDeleted(PillpopperActivity act);
}

/*
public interface EditableStringList
{
	// Get a list of all the items in the current list in the order they should be displayed
	public List<EditableStringItem> getSortedList();
	
	// Put a new item on the list
	public void addNewItem(String name);
	
	// Can a current item be deleted?
	// If yes, returns null.
	// If no, returns an error message to be displayed.
	public String canItemBeDeleted(PillpopperActivity act, EditableStringItem itemToDelete);
	
	// Delete an item on the list.
	public void deleteItem(PillpopperActivity act, EditableStringItem itemToDelete);
	
	// Returns the name of the items being edited -- e.g. "Person", "Drug Type"
	public int getItemNameId();
	public int getPluralNameId();
}
*/