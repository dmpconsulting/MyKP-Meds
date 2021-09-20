package com.montunosoftware.pillpopper.model;

import android.content.Context;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.PillpopperActivity;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContext;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.PillpopperStringBuilder;
import com.montunosoftware.pillpopper.android.util.Util;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Set;

public class DrugList implements StateUpdatedListener
{
	/////// State-Updated Callbacks ////////////////////////////////////

	// A list of guys that want to get notified every time our state is updated
	List<StateUpdatedListener> _stateUpdatedListeners = new ArrayList<>();
	private boolean _deferringUpdates = false;
	private boolean _updateDeferred = false;

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
		if (_deferringUpdates) {
			_updateDeferred = true;
		} else {
			// Notify any listeners of the update so they can update views
			for (StateUpdatedListener sul: _stateUpdatedListeners) {
				sul.onStateUpdated();
			}
		}
	}

	public void deferUpdates()
	{
		_deferringUpdates = true;
	}

	public void resumeUpdates()
	{
		_deferringUpdates = false;
		if (_updateDeferred) {
			_updateDeferred = false;
			_stateUpdated();
		}
	}


	@Override
	// Called when a drug under us tells us the state has been updated
	public void onStateUpdated()
	{
		_stateUpdated();
	}


	/////////////////////////////////////////////////////////////////
	/// Drug List
	private static final String _JSON_PILL_LIST = "pillList";
	private LinkedHashMap<String, Drug> _drugList = new LinkedHashMap<>();

	private static final String _JSON_DELETED_PILL_LIST = "deletedPillList";
	private Set<String> _deletedDrugs = new HashSet<>();

	public DrugList()
	{
	}

	public DrugList(StateUpdatedListener sul)
	{
		this.registerStateUpdatedListener(sul);
	}

	public DrugList(JSONObject jsonState, DrugTypeList drugTypeList, EditableStringList<Person> personList, Context _pilpopperActivity) throws JSONException
	{
		/*if (PillpopperConstants.isSyncAPIRequired) {
			if (jsonState.has(_JSON_PILL_LIST)) {
				JSONArray jsonDrugList = jsonState.getJSONArray(_JSON_PILL_LIST);

				for (int i = 0; i < jsonDrugList.length(); i++) {
					try {
						Drug newDrug = new Drug(jsonDrugList.getJSONObject(i), drugTypeList, personList, _pilpopperActivity);
//					PillpopperLog.say("Parsed drug: %s", newDrug.toString());
						//addDrug(newDrug);
						if (newDrug.isDeleted() == false *//*&& newDrug.isInvisible() == false*//*) {
							addDrug(newDrug);
						}
					} catch (JSONException e) {
						PillpopperLog.say("Got JSON exception trying to parse a drug!");
					} catch (PillpopperParseException e) {
						PillpopperLog.say("ignoring unparseable drug: %s", e.toString());
						PillpopperLog.say(e.getMessage());
					}
				}

				PillpopperLog.say("Parsed %d drugs", _drugList.size());
			}
		}else*//*{
			// Get State Implementation
			if(jsonState.has("userList")){
				JSONArray usersListArray = jsonState.getJSONArray("userList");
				PillpopperLog.say("--TAG userList JSONArray: " + usersListArray.length());
				for(int i=0; i<usersListArray.length(); i++){
					JSONObject userJSONObject = usersListArray.getJSONObject(i);

					// insert the user related data into DB here.
					PillpopperLog.say("--TAG jsonDrugList Object: " + userJSONObject.toString());

					Gson gson = new Gson();
					UserList userList = gson.fromJson(userJSONObject.toString(), UserList.class);

					JSONArray jsonDrugList = userJSONObject.getJSONArray("pillList");

					for (int j = 0; j < jsonDrugList.length(); j++)
					{
						try {
							Drug newDrug = new Drug(jsonDrugList.getJSONObject(j), drugTypeList, personList, _pilpopperActivity*//*, userList*//*);
							//PillpopperLog.say("Parsed drug: %s", newDrug.toString());
							//addDrug(newDrug);
							if(newDrug.isDeleted() == false *//*&& newDrug.isInvisible() == false*//*){
								addDrug(newDrug);
							}
						} catch (JSONException e) {
							PillpopperLog.say("Got JSON exception trying to parse a drug!");
						} catch (PillpopperParseException e) {
							PillpopperLog.say("ignoring unparseable drug: %s", e.toString());
							PillpopperLog.say(e.getMessage());
						}
					}
					PillpopperLog.say("Parsed %d drugs", _drugList.size());
				}
			}

		}*/

		if (jsonState.has(_JSON_DELETED_PILL_LIST)) {
			JSONArray jsonDeletedDrugList = jsonState.getJSONArray(_JSON_DELETED_PILL_LIST);

			for (int i = 0; i < jsonDeletedDrugList.length(); i++) {
				_deletedDrugs.add(jsonDeletedDrugList.getString(i));
			}
		}
	}

	public void marshal(Context ctx,JSONObject jsonState, boolean forSync, PillpopperAppContext pillpopperAppContext) throws JSONException
	{
		JSONArray jsonDrugList = new JSONArray();

		for (Drug d: getAll()) {
			JSONObject marshalledDrug = d.marshal(ctx,forSync, pillpopperAppContext);

			if (marshalledDrug == null) {
				PillpopperLog.say("warning: could not marshal drug");
			} else {
				jsonDrugList.put(marshalledDrug);
			}
		}

		jsonState.put(_JSON_PILL_LIST, jsonDrugList);

		JSONArray jsonDeletedDrugList = new JSONArray();

		for (String deletedDrug: _deletedDrugs) {
			jsonDeletedDrugList.put(deletedDrug);
		}

		jsonState.put(_JSON_DELETED_PILL_LIST, jsonDeletedDrugList);
	}


	//////////////////////////////////////////////////////////////////////////////////////////////
	/// Sync


	public static class ImageSyncRequests
	{
		private Set<Drug.ImageSyncRequest> _getRequests = new HashSet<>();
		private Set<Drug.ImageSyncRequest> _putRequests = new HashSet<>();

		public Set<Drug.ImageSyncRequest> getRequests() { return _getRequests; }
		public Set<Drug.ImageSyncRequest> putRequests() { return _putRequests; }
	}

	//////////////////////////////////////////////////////////////////////////////////////////////
	///  Iterators

	public Collection<Drug> getAll()
	{
		return _drugList.values();
	}

	public class ActiveDrugsIterator implements Iterator<Drug>, Iterable<Drug>
	{
		private Iterator<Drug> _rawIterator;
		private Drug _nextItem = null;

		public ActiveDrugsIterator()
		{
			_rawIterator = getAll().iterator();
			_findNext();
		}

		private void _findNext()
		{
			while (_rawIterator.hasNext()) {
				_nextItem = _rawIterator.next();

				if (!_nextItem.isArchived()) {
					return;
				}
			}

			_nextItem = null;
		}


		@Override
		public boolean hasNext()
		{
			return _nextItem != null;
		}

		@Override
		public Drug next()
		{
			Drug retval = _nextItem;
			_findNext();
			return retval;
		}

		@Override
		public void remove()
		{
		}

		@Override
		public Iterator<Drug> iterator()
		{
			return this;
		}
	}

	public int size()
	{
		return _drugList.size();
	}


	public Drug getDrugByGuid(String drugGuid)
	{
		return _drugList.get(drugGuid);
	}

	public void emailDrugListAsHtml(PillpopperActivity act, List<Drug> drugList)
	{
		// We do not require the deleted medication entries in drugs list while sending an email.
		//ArrayList<Drug> sortedDrugList = Util.arrayFromIterable(getActive());
		//ArrayList<Drug> sortedDrugList = Util.arrayFromIterable(drugList);

		/*if (drugList==null || drugList.size()==0) {
			String logMessage = act.getResources().getString(R.string.no_medication_email_alert);
			DialogHelpers.showAlertDialog(act, logMessage);
			return;
		}

		Collections.sort(drugList, new Drug.AlphabeticalByNameComparator());*/

		PillpopperStringBuilder drugTable = new PillpopperStringBuilder(act,act.getGlobalAppContext());
		drugTable.append(String.format(
						act.getString(R.string.email_html_drug_table_header_row),
						Util.describeDrugAsHtml(act,act.getGlobalAppContext(), null))
		);

		for (Drug d: drugList) {
			drugTable.append(String.format(
							act.getString(R.string.email_html_drug_table_data_row),
						Util.describeDrugAsHtml(act,act.getGlobalAppContext(), d))
			);
		}

		String attachment = String.format(
				act.getString(R.string.email_drug_table_wrapper), drugTable.toString());

		Util.sendEmail(
				act,
				null,
				act.getString(R.string.email_drug_summary_subject),
				act.getString(R.string.email_drug_body),
				attachment,
				Util.DOSECAST_MEDICATION_LIST_HTML);
	}


	public ActiveDrugsByExludingDeletedIterator getActiveDrugsExcludingDeleted()
	{
		return new ActiveDrugsByExludingDeletedIterator();
	}

	public class ActiveDrugsByExludingDeletedIterator implements Iterator<Drug>, Iterable<Drug>
	{
		private Iterator<Drug> _rawIterator;
		private Drug _nextItem = null;

		public ActiveDrugsByExludingDeletedIterator()
		{
			_rawIterator = getAll().iterator();
			_findNext();
		}

		private void _findNext()
		{
			while (_rawIterator.hasNext()) {
				_nextItem = _rawIterator.next();

				if (!_nextItem.isArchived() && !_nextItem.isDeleted()) {
					return;
				}
			}

			_nextItem = null;
		}


		@Override
		public boolean hasNext()
		{
			return _nextItem != null;
		}

		@Override
		public Drug next()
		{
			Drug retval = _nextItem;
			_findNext();
			return retval;
		}

		@Override
		public void remove()
		{
		}

		@Override
		public Iterator<Drug> iterator()
		{
			return this;
		}
	}




	// This function deletes any cached images that are not referenced by any drug
	// in the current drug list.
	public void garbageCollectImages(Context context)
	{
		// Enumerate all the files in the cache dir
		File[] fileList = null;

		try {
			fileList = Drug.getImageCacheDir(context).listFiles();
		} catch (IOException e) {
			return;
		}

		if (fileList == null) {
			return;
		}

		HashSet<File> fileSet = new HashSet<>(Arrays.asList(fileList));

		// Remove all the files referenced by a drug
		for (Drug d: getAll()) {
			File f = d.getImageCacheFile(context);

			if (f != null) {
				fileSet.remove(f);
			}
		}

		// Remove all files that remain in the set
		for (File f: fileSet) {
			if(f.delete()){
				PillpopperLog.say("garbage collecting %s", f.toString());
			}
		}
	}


}
