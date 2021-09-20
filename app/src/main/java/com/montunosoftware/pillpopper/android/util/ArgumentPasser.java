package com.montunosoftware.pillpopper.android.util;

import com.montunosoftware.pillpopper.android.view.PickListView;
import com.montunosoftware.pillpopper.model.Drug;

import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public class ArgumentPasser
{
	public static class ArgumentPassException extends Exception
	{
		private static final long serialVersionUID = 985242877241343764L;
	}
	
	private HashMap<String, Object> _argMap = new HashMap<>();
	private HashSet<String> _nullArgs = new HashSet<>();
	
	public ArgumentPasser putArg(String key, Object value) throws ArgumentPassException
	{
		if (_argMap.containsKey(key) || _nullArgs.contains(key)) {
			PillpopperLog.say("trying to pass key %s as argument twice", key);
			throw new ArgumentPassException();
		}
		
		if (value == null) {
			_nullArgs.add(key);
		} else {
			_argMap.put(key, value);
		}
		
		return this;
	}
	
	public <ArgType> ArgType _getArg(String key, Class<ArgType> argClass, boolean exceptionIfNotFound) throws ArgumentPassException
	{
		if (_nullArgs.contains(key))
			return null;
		
		if (_argMap.containsKey(key)) {
			Object retval = _argMap.get(key);
			
			if (!argClass.isInstance(retval)) {
				PillpopperLog.say("ArgumentPasser::getArg: Type mismatch when retrieving argument %s; expected %s, got %s",
						key, argClass.getSimpleName(), retval.getClass().getSimpleName());
				throw new ArgumentPassException();
			}
			
			return argClass.cast(retval);
		}

		if (exceptionIfNotFound) {
			PillpopperLog.say("ArgumentPasser::getArg: Argument %s not found!", key);
			throw new ArgumentPassException();
		} else {
			return null;
		}
	}
	
	public <ArgType> ArgType getArg(String key, Class<ArgType> argClass) throws ArgumentPassException
	{
		return _getArg(key, argClass, true);
	}
	
	// These methods are only here to supply this pragma -- in a narrow scope. We need the pragma
	// because Java erases the generic parameters at runtime.
	@SuppressWarnings("unchecked")
	public HashMap<String,Long> getHashMapStringLong(String key) throws ArgumentPassException
		{ return (HashMap<String,Long>) getArg(key, HashMap.class);	}
	
	@SuppressWarnings("unchecked")
	public List<Drug> getListDrug(String key) throws ArgumentPassException
		{ return (List<Drug>) getArg(key, List.class); }
	
	@SuppressWarnings("unchecked")
	public List<Object> getListObject(String key) throws ArgumentPassException
		{ return (List<Object>) getArg(key, List.class); }

	@SuppressWarnings("unchecked")
	public List<PickListView.MenuItem> getListPickListViewMenuItem(String key) throws ArgumentPassException
		{ return (List<PickListView.MenuItem>) getArg(key, List.class); }

	@SuppressWarnings("unchecked")
	public Collection<Object> getOptionalCollectionObject(String key) throws ArgumentPassException
		{ return (Collection<Object>) getOptionalArg(key, Collection.class); }

	public <ArgType> ArgType getOptionalArg(String key, Class<ArgType> argClass) throws ArgumentPassException
	{
		return _getArg(key, argClass, false);
	}

}
