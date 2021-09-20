package com.montunosoftware.pillpopper.model;

import android.content.Context;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.PillpopperActivity;

// Class for tracking who takes each drug
public class Person implements EditableStringItem
{
	private String _id;
	private String _name;
	private StateUpdatedListener _stateUpdatedListener = null;
	
	public Person(String id, String name, StateUpdatedListener stateUpdatedListener)
	{
		_id = id;
		_name = name;
		_stateUpdatedListener = stateUpdatedListener;
	}

	/////////////////////////////////////////////////////////////
	
	
	private void _update()
	{
		if (_stateUpdatedListener != null) {
			_stateUpdatedListener.onStateUpdated();
		}
	}
			
	
	/////////////////////
	
	
	@Override
	public void setName(String newName)
	{
		_name = newName;
		_update();
	}

	@Override
	public String getId()
	{
		return _id;
	}

	@Override
	public String toString()
	{
		return _name;
	}

	public static String describePerson(Context context, Person person)
	{
		if (person == null) {
			return context.getString(R.string._me);
		} else {
			return person._name;
		}
	}
	
	@Override
	public String canBeDeleted(PillpopperActivity act)
	{
		State currState = act.getState();
		
		// Check to see if this person is assigned to one or more drugs.
		// If so, don't allow deletion.
		StringBuilder sb = new StringBuilder();
		for (Drug d: currState.getDrugList().getAll()) {
			if (this.equals(d.getPerson())) {
				if (sb.length() > 0) {
					sb.append(", ");
				}
				sb.append(d.getName());
			}
		}
		
		// if drugs found, pop up an error
		if (sb.length() > 0) {
			return String.format(
					act.getString(R.string.person_in_use),
					Person.describePerson(act, this),
					sb.toString());
		} else {
			return null;
		}
	}

	@Override public boolean equals(Object o)
	{
		// Return true if the objects are identical.
		// (This is just an optimization, not required for correctness.)
		if (this == o) {
			return true;
		}

		// Return false if the other object has the wrong type.
		// This type may be an interface depending on the interface's specification.
		if (!(o instanceof Person)) {
			return false;
		}

		// Cast to the appropriate type.
		// This will succeed because of the instanceof, and lets us access private fields.
		Person lhs = (Person) o;

		// Check each field. Primitive fields, reference fields, and nullable reference
		// fields are all treated differently.
		return
				(_id == null ? lhs._id == null : _id.equals(lhs._id)) &&
				(_name == null ? lhs._name == null : _name.equals(lhs._name))
				;
	}

	@Override public int hashCode()
	{
		// Start with a non-zero constant.
		int result = 17;

		// Include a hash for each field.
		result = 31 * result + (_id == null ? 0 : _id.hashCode());
		result = 31 * result + (_name == null ? 0 : _name.hashCode());

		return result;
	}
	
	public static int compare(Person lhs, Person rhs)
	{
		if (lhs == null && rhs == null)
			return 0;
		
		if (lhs == null && rhs != null)
			return -1;
		
		if (lhs != null && rhs == null)
			return 1;
		
		if (lhs._name == null) {
			if (rhs._name == null)
				return 0;
			else
				return -1;
		}
		
		if (rhs._name == null) {
			return 1;
		} else {
			return lhs._name.compareToIgnoreCase(rhs._name);
		}
	}
}
