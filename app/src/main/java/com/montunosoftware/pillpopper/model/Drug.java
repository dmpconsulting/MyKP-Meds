package com.montunosoftware.pillpopper.model;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.format.DateFormat;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.RunTimeConstants;
import com.montunosoftware.pillpopper.android.util.FileHandling;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContext;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.PillpopperParseException;
import com.montunosoftware.pillpopper.android.util.UniqueDeviceId;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.android.util.WDHM;
import com.montunosoftware.pillpopper.model.Schedule.SchedType;

import org.json.JSONException;
import org.json.JSONObject;
import org.kp.tpmg.mykpmeds.activation.AppConstants;

import java.io.File;
import java.io.IOException;
import java.io.Serializable;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class Drug implements Cloneable,Serializable
{

	private String isRemindersEnabled;
	private String scheduleGuid;

	public Drug() {

	}

	public Drug copy()
	{
		Drug clone;
		try {
			clone = (Drug) super.clone();
		} catch (CloneNotSupportedException e) {
			PillpopperLog.say("Drug clone failed");
			return null;
		}

		// kill state updated listeners on the copy -- must go first,
		// otherwise the operations below may cause spurious updates
		clone.set_stateUpdatedListeners(new ArrayList<>()); // empty, do not copy contents!

		// make deep copies of mutable items
		clone._setSchedule(getSchedule()._copy());
		clone.set_doseData(get_doseData().copy());
		clone._drugPrefs = _drugPrefs.copy();

		// kill undo state
		clone._undo = null;

		return clone;
	}

	private DrugTypeList _drugTypeList;

	private void initDrug(DrugTypeList drugTypeList)
	{
		_id = Util.getRandomGuid();
		_drugTypeList = drugTypeList;
		if(drugTypeList != null)
			this.setDrugType(drugTypeList.getDefaultDrugType());
		this._created = PillpopperTime.now();
		this._localEditTime = this._created;
//		this._history = new History(this);
	}

	public Drug(DrugTypeList drugTypeList)
	{
		initDrug(drugTypeList);
	}



	public Drug(JSONObject jsonDrug, DrugTypeList drugTypeList, EditableStringList<Person> personList, Context context) throws PillpopperParseException
	{

		//PillpopperLog.say("-- drugInfo : " + jsonDrug.toString());

		initDrug(drugTypeList);

		try
		{
			_id = Util.parseJSONStringOrNull(jsonDrug, _JSON_ID);
			_directions = Util.parseJSONStringOrNull(jsonDrug, _JSON_DIRECTIONS);

			_name = Util.parseJSONStringOrNull(jsonDrug, _JSON_NAME);

			JSONObject jsonDrugPrefs = new JSONObject();
			if (jsonDrug.has(_JSON_PREFERENCES)) {
				jsonDrugPrefs = jsonDrug.getJSONObject(_JSON_PREFERENCES);
				_drugPrefs = new Preferences(jsonDrugPrefs);
			}

			_person = personList.getItemById(_drugPrefs.getPreference(_JSON_PERSON_ID));
			_created = PillpopperTime.parseJSON(jsonDrug, _JSON_CREATED);
			_lastTaken = PillpopperTime.parseJSONBackCompat(jsonDrug, _JSON_LAST_TAKEN_BACKCOMPAT, _JSON_LAST_TAKEN);
			set_effLastTaken(PillpopperTime.parseJSONBackCompat(jsonDrug, _JSON_EFF_LAST_TAKEN_BACKCOMPAT, _JSON_EFF_LAST_TAKEN));
			_notifyAfter = PillpopperTime.parseJSONBackCompat(jsonDrug, _JSON_NOTIFY_AFTER_BACKCOMPAT, _JSON_NOTIFY_AFTER);
			_serverEditTime = PillpopperTime.parseJSON(jsonDrug, _JSON_SERVER_EDIT_TIME);
			_serverEditGuid = Util.parseJSONStringOrNull(jsonDrug, _JSON_SERVER_EDIT_GUID);

			if (_localEditGuid == null) {
				_localEditGuid = _getLocalEditGuid();
			}

			this._setSchedule(Schedule.parseJSON(jsonDrug, jsonDrugPrefs, _name, context));

			_parseRefills(jsonDrugPrefs);
			setDoctor(Contact.parseJSON(jsonDrugPrefs, _JSON_DOCTOR_CONTACT));
			setPharmacy(Contact.parseJSON(jsonDrugPrefs, _JSON_PHARMACY_CONTACT));

			if ((_doseData = DoseData.parseJson(_drugTypeList, jsonDrug, jsonDrugPrefs)) == null) {
				_doseData = new DoseData(_drugTypeList.getDefaultDrugType());
			}

			/*if(PillpopperConstants.isSyncAPIRequired) {
				if (jsonDrug.has(_JSON_HISTORY)) {
					_history = History.parseJSON(_drugTypeList, this, jsonDrug.getJSONObject(_JSON_HISTORY));
				}
			}*/

			///// THIS MUST GO LAST /////
			// the below elements get modified every time drug data is updated, which happens
			// while the drug is being constructed. Therefore, this must go last in order for the
			// parsing to give us the same edit GUID and edit time as was written.

			// if there's a last local edit time, use it; otherwise continue using 'now' as initialized
			PillpopperTime parsedLastLocalEdit = PillpopperTime.parseJSON(jsonDrug, _JSON_LOCAL_EDIT_TIME);

			if (parsedLastLocalEdit != null) {
				_localEditTime = parsedLastLocalEdit;
			}

			_localEditGuid = Util.parseJSONStringOrNull(jsonDrug, _JSON_LOCAL_EDIT_GUID);


			/*if(!PillpopperConstants.isSyncAPIRequired){
				Gson gson = new Gson();
				PillList pillInfo = gson.fromJson(jsonDrug.toString(), PillList.class);

				PillpopperLog.say("--- PillINfo object : " + "PillId : " + pillInfo.getPillId()
						+ " Name : " + pillInfo.getName() + " : ANd Pref Object " + pillInfo.getPreferences());

				//insert pill information
				DatabaseHandler.getInstance(_pilpopperActivity).insert(DatabaseConstants.PILL_TABLE, pillInfo, "");

				//insert pill Preferences
				DatabaseHandler.getInstance(_pilpopperActivity).insert(DatabaseConstants.PILL_PREFERENCE_TABLE, pillInfo, "");

				//insert pill schedule
				DatabaseHandler.getInstance(_pilpopperActivity).insert(DatabaseConstants.PILL_SCHEDULE_TABLE, pillInfo, "");

				// TODO this needs to ve removed if we do not want to show the table strctures.
				DatabaseHandler.getInstance(_pilpopperActivity).showTableData(DatabaseConstants.PILL_TABLE);
				DatabaseHandler.getInstance(_pilpopperActivity).showTableData(DatabaseConstants.PILL_PREFERENCE_TABLE);
				DatabaseHandler.getInstance(_pilpopperActivity).showTableData(DatabaseConstants.PILL_SCHEDULE_TABLE);
			}*/
		}
		catch (JSONException e)
		{
			PillpopperLog.say(e.getMessage());
			throw new PillpopperParseException(String.format("error parsing pill options: %s", e.toString()));
		}
	}


	/// DB Support Added Methods Start

	private String userID;

	private boolean isTempHeadr;

	public boolean isNoDrugsFound() {
		return isNoDrugsFound;
	}

	public void setIsNoDrugsFound(boolean isNoDrugsFound) {
		this.setNoDrugsFound(isNoDrugsFound);
	}

	private boolean isNoDrugsFound;

	public boolean isTempHeadr() {
		return isTempHeadr;
	}

	public void setIsTempHeadr(boolean isTempHeadr) {
		this.isTempHeadr = isTempHeadr;
	}

	public boolean isHeader() {
		return header;
	}

	public void setHeader(boolean header) {
		this.header = header;
	}

	private boolean header;

	public String getUserID() {
		return userID;
	}

	public void setUserID(String userID) {
		this.userID = userID;
	}

	public void setId(String id){
		this._id = id;
	}

	public void setCreated(PillpopperTime created){
		this._created = created;
	}

	public void setLastTaken(PillpopperTime last_taken){
		this._lastTaken = last_taken;
	}

	public void setPreferecences(JSONObject pref){
		try {
			_drugPrefs = new Preferences(pref);
		} catch (PillpopperParseException e) {
			PillpopperLog.say(e.getMessage());
		}
	}

	public Preferences getPreferences(){
		return _drugPrefs;
	}

	private boolean isScheduleDeletedFromTrash;

	public boolean isScheduleDeletedFromTrash() {
		return isScheduleDeletedFromTrash;
	}

	public void setScheduleDeletedFromTrash(boolean scheduleDeletedFromTrash) {
		isScheduleDeletedFromTrash = scheduleDeletedFromTrash;
	}

	public boolean isoverDUE() {

		return _doseEventCollection.getOverdueEvent() != null;

	}

	public String getDose(){
		StringBuilder doseBuilder =new StringBuilder();
		if (null != _drugPrefs.getPreference("customDescription")) {
			doseBuilder.append(_drugPrefs.getPreference("customDescription"));
		} else if (null != _drugPrefs.getPreference("managedDescription")) {
			doseBuilder.append(_drugPrefs.getPreference("managedDescription"));
		}
		return doseBuilder.toString();
	}

	public String getMaxDosage(){
		return _drugPrefs.getPreference("maxNumDailyDoses");
	}

	private String isOverdue;

	public String getIsOverdue() {
		return isOverdue;
	}

	public void setIsOverdue(String isOverdue) {
		this.isOverdue = isOverdue;
	}

	private int scheduleCount;

	public int getScheduleCount() {
		return scheduleCount;
	}

	public void setScheduleCount(int scheduleCount) {
		this.scheduleCount = scheduleCount;
	}

	private int mAction;

	public int getmAction() {
		return mAction;
	}

	public void setmAction(int mAction) {
		this.mAction = mAction;
	}

	public boolean isScheduleAddedOrUpdated() {
		return isScheduleAddedOrUpdated;
	}

	public void setScheduleAddedOrUpdated(boolean scheduleAddedOrUpdated) {
		isScheduleAddedOrUpdated = scheduleAddedOrUpdated;
	}

	private boolean isScheduleAddedOrUpdated = false;

	public long getPostponeSeconds() {
		return postponeSeconds;
	}

	public void setPostponeSeconds(long postponeSeconds) {
		this.postponeSeconds = postponeSeconds;
	}

	private long postponeSeconds;


	// DB Support Added methods End

	public static Drug fromJsonString(Context context,String s, DrugTypeList drugTypeList, EditableStringList<Person> personList) throws PillpopperParseException
	{
		JSONObject jsonDrug;
		try {
			jsonDrug = new JSONObject(s);
		} catch (JSONException e) {
			throw new PillpopperParseException("Drug::fromJsonString: couldn't parse json block");
		}

		return new Drug(jsonDrug, drugTypeList, personList, context);
	}


	private static boolean _debugSync = false;

	// Describe the drug as a JSON object
	public JSONObject marshal(Context ctx,boolean forSync, PillpopperAppContext pillpopperAppContext) throws JSONException
	{
		if (forSync && _debugSync) {
			PillpopperLog.say("SYNC: sending drug %s, local edit guid %s, server edit guid %s, modified %s",
					Util.friendlyGuid(getGuid()),
					Util.friendlyGuid(getLocalEditGuid()),
					Util.friendlyGuid(getServerEditGuid()),
					PillpopperTime.getDebugString(getLocalEditTime())
			);
		}

		JSONObject jsonDrug = new JSONObject();

		jsonDrug.put(_JSON_ID, getGuid());
		jsonDrug.put(_JSON_NAME, getName());
		jsonDrug.put(_JSON_DIRECTIONS, getDirections());
		jsonDrug.put(_JSON_LOCAL_EDIT_GUID, _localEditGuid);
		jsonDrug.put(_JSON_SERVER_EDIT_GUID, _serverEditGuid);

		PillpopperTime.marshal(jsonDrug, _JSON_CREATED, _created);
		PillpopperTime.marshal(jsonDrug, _JSON_LAST_TAKEN, _lastTaken);
		PillpopperTime.marshal(jsonDrug, _JSON_EFF_LAST_TAKEN, get_effLastTaken());
		PillpopperTime.marshal(jsonDrug, _JSON_NOTIFY_AFTER, _notifyAfter);
		PillpopperTime.marshal(jsonDrug, _JSON_LOCAL_EDIT_TIME, _localEditTime);

		if (!forSync) {
			PillpopperTime.marshal(jsonDrug, _JSON_SERVER_EDIT_TIME, _serverEditTime);
		}

		JSONObject jsonDrugPrefs = _drugPrefs.marshal();

		getSchedule().marshal(jsonDrug, jsonDrugPrefs);

		Util.putJSONString(jsonDrugPrefs, _JSON_PERSON_ID, _person == null ? "" : _person.getId());
		_marshalRefills(jsonDrugPrefs);
		get_doseData().marshal(_drugTypeList, jsonDrug, jsonDrugPrefs);
//		jsonDrug.put(_JSON_HISTORY, _history.marshal(ctx,_drugTypeList, forSync, pillpopperAppContext));

		if (_doctor != null) {
			_doctor.marshal(jsonDrugPrefs, _JSON_DOCTOR_CONTACT);
		}

		if (_pharmacy != null) {
			_pharmacy.marshal(jsonDrugPrefs, _JSON_PHARMACY_CONTACT);
		}

		jsonDrug.put(_JSON_PREFERENCES, jsonDrugPrefs);



		return jsonDrug;
	}


	/////// State-Updated Callbacks ////////////////////////////////////

	// A list of guys that want to get notified every time our state is updated
	private List<StateUpdatedListener> _stateUpdatedListeners = new ArrayList<>();

	public void registerStateUpdatedListener(StateUpdatedListener stateUpdatedListener)
	{
		get_stateUpdatedListeners().add(stateUpdatedListener);
	}

	public void unregisterStateUpdatedListener(StateUpdatedListener stateUpdatedListener)
	{
		get_stateUpdatedListeners().remove(stateUpdatedListener);
	}

	private void _stateUpdated()
	{
		// Notify any listeners of the update so they can update views
		for (StateUpdatedListener sul: get_stateUpdatedListeners()) {
			if(RunTimeConstants.getInstance().isBackPressDrugDetailAct()==false){
				sul.onStateUpdated();
			}
		}
	}

	private static final String _JSON_LOCAL_EDIT_TIME = "clientEditTime";
	private static final String _JSON_LOCAL_EDIT_GUID = "clientEditGuid";

	private PillpopperTime _localEditTime;
	private String _localEditGuid;

	private String _getLocalEditGuid()
	{
		StringBuilder sb = new StringBuilder();
		sb.append(UniqueDeviceId.getHardwareId());
		sb.append("-");
		sb.append(Util.getRandomGuid(25));
		return sb.toString();
	}

	private void _drugChanged()
	{
		_localEditTime = PillpopperTime.now();
		_localEditGuid = _getLocalEditGuid();
		_stateUpdated();
	}

	public PillpopperTime getLocalEditTime()
	{
		return _localEditTime;
	}

	public String getLocalEditGuid()
	{
		return _localEditGuid;
	}

	private static final String _JSON_SERVER_EDIT_TIME = "serverEditTime";
	private PillpopperTime _serverEditTime;

	private static final String _JSON_SERVER_EDIT_GUID = "serverEditGuid";
	private String _serverEditGuid;

	public String getServerEditGuid()
	{
		return _serverEditGuid;
	}


	public String toString()
	{
		String scheduleString;

		if (_drugPrefs.getPreference("scheduleChoice").equalsIgnoreCase(SchedType.SCHEDULED.toString())) {
			scheduleString = String.format(Locale.US, "schedule:%s", _schedule.getTimeList().toString());
		} else {
			scheduleString = String.format(Locale.US, "interval:%d", _schedule.getIntervalSeconds());
		}

		return String.format("%s[%s]", getGuid(), scheduleString);
	}


	// GUID
	private static final String _JSON_ID = "pillId";
	private String _id;
	public String getGuid() { return _id; }

	// Name
	private static final String _JSON_NAME = "name";
	private String _name;
	public String getName() { return _name; }
	public void setName(String name) { _name = Util.cleanString(name); _drugChanged(); }

	// Directions
	private static final String _JSON_DIRECTIONS = "instructions";
	private String _directions;
	public String getDirections() { return _directions; }
	public void setDirections(String s) { _directions = s; _drugChanged(); }

	////////////////////////////////////////////////////
	//DB fields
	private String _dose;
	/////////////////////////////////////////////////

	// Prescription Information
	private static final String _JSON_DOCTOR_CONTACT = "doctor";
	private Contact _doctor;
	public Contact getDoctor() { return _doctor; }
	public void setDoctor(Contact doctor) { _doctor = doctor; _drugChanged(); }

	private static final String _JSON_PHARMACY_CONTACT = "pharmacy";
	private Contact _pharmacy;
	public Contact getPharmacy() { return _pharmacy; }
	public void setPharmacy(Contact pharmacy) { _pharmacy = pharmacy; _drugChanged(); }

	private static final String _JSON_PRESCRIPTION_NUM = "prescriptionNum";
	public String getPrescriptionNum() { return _drugPrefs.getPreference(_JSON_PRESCRIPTION_NUM); }
	public void setPrescriptionNum(String prescriptionNum) { _drugPrefs.setPreference(_JSON_PRESCRIPTION_NUM, prescriptionNum); _drugChanged(); }

	// This method queries the local device's contacts database and updates our
	// internal string representation of a contact's name if the device's contact
	// has changed.
	public void update_contacts(Activity act)
	{
		if (getDoctor()!=null) {
			Contact newDoctor = getDoctor().updateName(act);
			if (newDoctor!=null) {
				setDoctor(newDoctor);
			}
		}
		if (getPharmacy()!=null) {
			Contact newPharmacy = getPharmacy().updateName(act);
			if (newPharmacy!=null) {
				setPharmacy(newPharmacy);
			}
		}
	}

	public static void update_contacts(Activity act, List<Drug> drugList)
	{
		for (Drug d : drugList) {
			d.update_contacts(act);
		}
	}

	/////////////////////////////////////////////////////////

	// Notes
	private static final String _JSON_NOTES = "notes";
	public String getNotes() { return _drugPrefs.getPreference(_JSON_NOTES); }
	public void setNotes(String notes) { _drugPrefs.setPreference(_JSON_NOTES, notes); _drugChanged(); }
	public boolean hasNotes() { return (Util.cleanString(getNotes()) != null); }

	// Created
	private static final String _JSON_CREATED = "created";
	private PillpopperTime _created;
	public PillpopperTime getCreated() { return _created; }

	// For which person
	private static final String _JSON_PERSON_ID = "personId";
	private Person _person;
	public Person getPerson() { return _person; }
	public void setPerson(Person person) { _person = person; _drugChanged(); }

	// Per-drug preferences
	private static final String _JSON_PREFERENCES = "preferences";
	private Preferences _drugPrefs = new Preferences();

	// Refills
	private static final String _JSON_REFILL_ALERT_DAYS_OR_DOSES = "refillAlertDoses";
	private long _refillAlertDaysOrDoses = 0;
	public long getRefillAlertDaysOrDoses() { return _refillAlertDaysOrDoses; }
	public void setRefillAlertDaysOrDoses(long refillAlertDaysOrDoses) {
		if (refillAlertDaysOrDoses >= 0) {
			_refillAlertDaysOrDoses = refillAlertDaysOrDoses;
			_drugChanged();
		}
	}

	private static final String _JSON_UNITS_PER_REFILL = "refillQuantity";
	private double _unitsPerRefill = 0;
	public double getUnitsPerRefill() { return _unitsPerRefill; }
	public void setUnitsPerRefill(double unitsPerRefill) {
		if (unitsPerRefill >= 0.0) {
			_unitsPerRefill = unitsPerRefill;
			_drugChanged();
		}
	}

	private static final String _JSON_UNITS_REMAINING = "remainingQuantity";
	private double _unitsRemaining = 0;
	public double getUnitsRemaining() { return _unitsRemaining; }
	public void setUnitsRemaining(double unitsRemaining) {
		if (unitsRemaining >= 0) {
			_unitsRemaining = unitsRemaining;
			_drugChanged();
		}
	}

	public void setDose(String dose) {
//		_drugPrefs.setPreference("managedDescription", dose);
		this.dose = dose;
	}

	//Doses
	private String dose;

	private static final String _JSON_REFILLS_REMAINING = "refillsRemaining";
	private long _refillsRemaining = 0;
	public long getRefillsRemaining() { return _refillsRemaining; }
	public void setRefillsRemaining(long refillsRemaining) {
		if (refillsRemaining >= 0) {
			_refillsRemaining = refillsRemaining;
			_drugChanged();
		}
	}

	private void _resetRefills()
	{
		_refillsRemaining = 0;
		_unitsRemaining = 0;
		_unitsPerRefill = 0;
		_refillAlertDaysOrDoses = 0;
		_drugChanged();
	}

	private void _marshalRefills(JSONObject jsonDrugPrefs) throws JSONException
	{
		Util.putJSONStringFromLong(jsonDrugPrefs, _JSON_REFILL_ALERT_DAYS_OR_DOSES, getRefillAlertDaysOrDoses());
		jsonDrugPrefs.put(_JSON_UNITS_PER_REFILL, Util.maybeAppendString(String.format(Locale.US, "%f", getUnitsPerRefill()), getRefillUnits()));
		jsonDrugPrefs.put(_JSON_UNITS_REMAINING,  Util.maybeAppendString(String.format(Locale.US, "%f", getUnitsRemaining()), getRefillUnits()));
		Util.putJSONStringFromLong(jsonDrugPrefs, _JSON_REFILLS_REMAINING, getRefillsRemaining());
	}

	private void _parseRefills(JSONObject jsonDrugPrefs)
	{
		setRefillAlertDaysOrDoses(Util.parseJSONNonnegativeLong(jsonDrugPrefs, _JSON_REFILL_ALERT_DAYS_OR_DOSES));

		DoubleAndString unitsPerRefill = DoubleAndString.parseJSON(jsonDrugPrefs, _JSON_UNITS_PER_REFILL);
		if (unitsPerRefill != null && unitsPerRefill.getDouble() > 0) {
			setUnitsPerRefill(unitsPerRefill.getDouble());
		}

		DoubleAndString unitsRemaining = DoubleAndString.parseJSON(jsonDrugPrefs, _JSON_UNITS_REMAINING);
		if (unitsRemaining != null && unitsRemaining.getDouble() > 0) {
			setUnitsRemaining(unitsRemaining.getDouble());
		}

		setRefillsRemaining(Util.parseJSONNonnegativeLong(jsonDrugPrefs, _JSON_REFILLS_REMAINING));
	}

	// Schedule

	private Schedule _schedule = new Schedule();
	public Schedule getSchedule() { return _schedule; }

	// called when the schedule actually changes
	public void setSchedule(Schedule schedule)
	{
		/*if(PillpopperConstants.isSyncAPIRequired){
			_history.setExpectedNextScheduledTime(null);
		}*/
		_setSchedule(schedule);
	}

	// called internally, e.g. in the parser
	public void _setSchedule(Schedule schedule)
	{
		_schedule = schedule;
		_drugChanged();
	}

	// Reminders enabled?
	private static final String _JSON_REMINDERS = "noPush"; // marshalled representation is inverted

	//the reminder off feature has been removed from the app as per US7136
	public boolean getRemindersActive() { return true;}//return !_drugPrefs.getBoolean(_JSON_REMINDERS, false); }

	public void setRemindersActive(boolean remindersActive) {
		_drugPrefs.setBoolean(_JSON_REMINDERS, !remindersActive);
		_drugChanged();
	}

	public boolean canGetReminders()
	{
		return !isArchived() && !_drugPrefs.getPreference("scheduleChoice").equalsIgnoreCase(SchedType.AS_NEEDED.toString());
	}

	// Deleted?
	private static final String _JSON_DELETED = "deleted";
	public void setDeleted(boolean isDeleted) { _drugPrefs.setBoolean(_JSON_DELETED, isDeleted); _drugChanged(); }
	public boolean isDeleted() { return _drugPrefs.getBoolean(_JSON_DELETED, false); }

	// Archived? Note that invisible and managed-dropped both imply archived,
	// because the archived flag does a bunch of other stuff we want, like disabling all notifications and scheduling.
	private static final String _JSON_ARCHIVED = "archived";
	public void setArchived(boolean isArchived) { _drugPrefs.setBoolean(_JSON_ARCHIVED, isArchived); _drugChanged(); }

    public boolean isArchived() {
        return
                isInvisible() ||
                        _drugPrefs.getBoolean(_JSON_ARCHIVED, false);
    }

	// Invisible?
	private static final String _JSON_INVISIBLE = "invisible";
	public void setInvisible(boolean isInvisible) { _drugPrefs.setBoolean(_JSON_INVISIBLE, isInvisible); _drugChanged(); }
	public boolean isExplicitlyInvisible() { return _drugPrefs.getBoolean(_JSON_INVISIBLE, false); }
	public boolean isInvisible() {
		return
				isManagedDropped() ||
						isExplicitlyInvisible()
				;
	}

	// Secondary reminders?
	private static final String _JSON_SECONDARY_REMINDERS = "secondaryReminders";
	public void setSecondaryReminders(boolean secondaryReminders) { _drugPrefs.setBoolean(_JSON_SECONDARY_REMINDERS, secondaryReminders); _drugChanged(); }
	public boolean getSecondaryRemindersActive() { return _drugPrefs.getBoolean(_JSON_SECONDARY_REMINDERS, true); }

	/// Dose data (varies based on drug type)

	private DoseData _doseData;
	public DoseData getDoseData() { return get_doseData(); }
	public DrugType getDrugType()
	{
		return get_doseData().getDrugType();
	}

	/// Drug type
	public void setDrugType(DrugType newDrugType)
	{
		if (newDrugType != null && (get_doseData() == null || !newDrugType.equals(get_doseData().getDrugType()))) {
			set_doseData(new DoseData(newDrugType));
			_resetRefills();

			// hacky -- kill the preferences related to database-type drugs
			setNdc(null);
			setMedForm(null);
			setMedType(null);

			_drugChanged();
		}
	}


	public DoubleAndString getUnitsPerDoseData() { return get_doseData().getUnitsPerDoseData(); }
	public void setUnitsPerDoseData(DoubleAndString unitsPerDose)
	{
		get_doseData().setUnitsPerDoseData(unitsPerDose);

		// If the user just unconfigured the units for this drug,
		// unconfigure the refill data too.
		if (getRefillUnits() == null) {
			this.setUnitsPerRefill(0);
			this.setUnitsRemaining(0);
		}

		_drugChanged();
	}

	public DoubleAndString getSupplementalDoseData(int index) { return get_doseData().getSupplementalDoseData(index); }
	public void setSupplementalDoseData(int index, DoubleAndString supplementalData) { get_doseData().setSupplementalDoseData(index, supplementalData); _drugChanged(); }



	public double getUnitsConsumedPerDose()
	{
		// How many units should be subtracted from the stock every time the dose is taken?
		// If there's no unitsPerDose configuration for this drug, default to 1.
		// Otherwise, if configured, use the configured unitsPerDose.
		// If not configured, do no stockkeeping -- i.e. 0 units per dose.
		if (getDrugType().getUnitsPerDoseFieldType() == null) {
			return 1.0;
		} else if (getUnitsPerDoseData() != null) {
			return getUnitsPerDoseData().getDouble();
		} else {
			return 0;
		}
	}

	// Returns just the dosage description (e.g. "1 pill, 250 mg")
	public String getDosageDescription(Context ctx)
	{
		return get_doseData().getDosageDescription(ctx,this);
	}

	// Returns both the pill name and dosage, e.g. "Lipitor (2 pills, 10mg)"
	public String getDoseNameAndDosage(Context context)
	{
		StringBuilder sb = new StringBuilder();

		sb.append(getName());

		String doseDescription = getDosageDescription(context);

		if (doseDescription != null) {
			sb.append(" (");
			sb.append(doseDescription);
			sb.append(")");
		}

		return sb.toString();
	}

	/////////////  Dose History and Dosing Limit Computations ///////////////

	private static final String _JSON_HISTORY = "history";

	private static final String _JSON_LOG_MISSED_DOSES = "logMissedDoses";

	public boolean canLogMissedDoses()
	{
		return _drugPrefs.getPreference("scheduleChoice").equalsIgnoreCase(SchedType.SCHEDULED.toString()) && !isArchived();
	}

	// Only log missed doses if we can (scheduled and not archived) and configured to do so
	public boolean isLoggingMissedDoses()
	{
		return canLogMissedDoses() && _drugPrefs.getBoolean(_JSON_LOG_MISSED_DOSES, true);
	}

	////

	private static final String _JSON_RECENT_DOSES = "recentDoses";

	////////////////////////// Schedule Computations ////////////////////////////////
	// Last taken
	private static final String _JSON_LAST_TAKEN_BACKCOMPAT = "last";
	private static final String _JSON_LAST_TAKEN = "last_taken";
	private PillpopperTime _lastTaken;
	public PillpopperTime getLastTaken() { return _lastTaken; }

	// Effective last-taken time
	private static final String _JSON_EFF_LAST_TAKEN_BACKCOMPAT = "effLastTaken";
	private static final String _JSON_EFF_LAST_TAKEN = "eff_last_taken";
	private PillpopperTime _effLastTaken;
	public PillpopperTime get_effLastTaken()
	{
		return _effLastTaken;
	}
	// Notify-after time (due to a user postpone)
	private static final String _JSON_NOTIFY_AFTER_BACKCOMPAT = "notifyAfter";
	private static final String _JSON_NOTIFY_AFTER = "notify_after";
	private PillpopperTime _notifyAfter;
	public PillpopperTime get_notifyAfter()	{
		return _notifyAfter;
	}
	public void set_notifyAfter(PillpopperTime _notifyAfter) {
		this._notifyAfter = _notifyAfter;
	}
	// The following are computed dynamically, not stored persistently.
	private DoseEventCollection _doseEventCollection;


	public DoseEventCollection get_doseEventCollection() {
		return _doseEventCollection;
	}

	public void set_doseEventCollection(DoseEventCollection _doseEventCollection) {
		this._doseEventCollection = _doseEventCollection;
	}


	public void computeDBDoseEvents(Context context,Drug d, PillpopperTime now, long secondaryReminderPeriodSecs) {

		//Schedule sched = d.getSchedule();

		_doseEventCollection = new DoseEventCollection(context, d, now, secondaryReminderPeriodSecs);

	}

	public void computePastReminderEvents(Context context,Drug drug, PillpopperTime now){
		_doseEventCollection = new DoseEventCollection(context,drug, now);
	}

	//////////////////////////////// Take/Skip/Postpone Actions ////////////////////////////////////////
	// Take and Skip Dose
	private Date getSimpleDatetimefromHourMin(int hour, int minute)
	{
        SimpleDateFormat format = new SimpleDateFormat("H:mm", Locale.US);
        Date date = null;
        try {
            date = format.parse(hour + ":" + minute);
        } catch (ParseException e) {
			PillpopperLog.say(e.getMessage());
		}
        return date;
	}

	public Date getSimpleDatetimeScheduledStringIn24Hour(String scheduleTime)
	{
		Date dateIn24Hour = null;
		if(scheduleTime.contains("am")||scheduleTime.contains("AM")||scheduleTime.contains("Am")||scheduleTime.contains("Pm")||scheduleTime.contains("pm")||scheduleTime.contains("PM")){
			SimpleDateFormat format=new SimpleDateFormat("h:mm a",Locale.US);
			//converting to 24 hour format to compare with bedtime
			SimpleDateFormat requiredFormat = new SimpleDateFormat("H:mm",Locale.US);
			try {
				Date date = format.parse(scheduleTime);
				dateIn24Hour = requiredFormat.parse(requiredFormat.format(date));
			} catch (ParseException e) {
				PillpopperLog.say(e.getMessage());
			}
		}else{
			SimpleDateFormat requiredFormat = new SimpleDateFormat("H:mm",Locale.US);
			try {
				dateIn24Hour = requiredFormat.parse(scheduleTime);
			} catch (ParseException e) {
				PillpopperLog.say(e.getMessage());
			}
		}
		return dateIn24Hour;
	}



	public DoseEvent getActiveScheduledDoseEvent()
	{
		// Drugs producing reminders: the active dose is the overdue one, if any;
		// otherwise, the next one.
		//
		// Drugs NOT producing reminders, can never be overdue.
		// Therefore, they always return the next event.
		if (getRemindersActive() && _doseEventCollection.getOverdueEvent() != null) {
			return _doseEventCollection.getOverdueEvent();
		} else {
			return _doseEventCollection.getNextEvent();
		}
	}

	public PillpopperTime getActiveScheduledDoseTime()
	{
		DoseEvent activeEvent = getActiveScheduledDoseEvent();

		if (activeEvent == null) {
			return null;
		} else {
			return activeEvent.getOriginal_date() != null ? activeEvent.getOriginal_date() : activeEvent.getDate();
		}
	}


	private PillpopperTime actionTime;

	public void setActionTime(PillpopperTime actionTime)
	{
		this.actionTime = actionTime;
	}



	public String getEarlyDoseWarning(Context context, PillpopperTime now, int moreThanOneDrug)
	{
		if (this.isOverdue())
			return null;

		PillpopperTime activeDoseTime = this.getActiveScheduledDoseTime();

		// drugs without a next scheduled time: no warning
		if (activeDoseTime == null)
			return null;

		PillpopperTime oneHourHence = new PillpopperTime(now, WDHM.SecPerHour);
		String warningMsg = null;
		if (moreThanOneDrug > 1) {
			warningMsg = context.getString(R.string.early_dose_warning_subst_more_than_one);
		}else{
			warningMsg = context.getString(R.string.early_dose_warning_subst);
		}

		if (!activeDoseTime.before(oneHourHence)) {
			return String.format(
					warningMsg,
					this.getName(),
					PillpopperTime.getLocalizedAdaptiveString(activeDoseTime, R.string.__blank, context)
			);
		}

		return null;
	}
	/////////////////////////////////////////////////////////////
	/// Postpone

	public PillpopperTime _getPostponeTime(long postponeTimeSeconds)
	{
		PillpopperTime refDate;

		// first get the reference time
		if (_doseEventCollection.getOverdueEvent() != null) {
			refDate = PillpopperTime.now();
		} else if (_doseEventCollection.getNextEvent() != null) {
			refDate = new PillpopperTime(_doseEventCollection.getNextEvent().getDate());
		} else {
			PillpopperLog.say("Bug: allowed to postpone with no overdue and no future dose!?");
			refDate = PillpopperTime.now();
		}

		// Add the number of seconds to postpone by, then
		// Round to the nearest minute, to prevent
		// postpones from firing in the middle of a minute
		return new PillpopperTime(refDate, postponeTimeSeconds).getContainingMinute();
	}

	private PillpopperTime _getPostponeTimeForUseInterval(long postponeTimeSeconds,PillpopperTime time,boolean check)
	{
		PillpopperTime refDate;

		// first get the reference time
		if (_doseEventCollection.getOverdueEvent() != null) {
			if(check==true && this._drugPrefs.getPreference("scheduleChoice").equalsIgnoreCase(SchedType.SCHEDULED.toString())){
				refDate = time;
			}else{
				refDate = PillpopperTime.now();
			}
		} else if (_doseEventCollection.getNextEvent() != null) {
			refDate = new PillpopperTime(_doseEventCollection.getNextEvent().getDate());
		} else {
			PillpopperLog.say("Bug: allowed to postpone with no overdue and no future dose!?");
			try {
				refDate = time;
			} catch (Exception e) {
				refDate = PillpopperTime.now();
			}
		}

		// Add the number of seconds to postpone by, then
		// Round to the nearest minute, to prevent
		// postpones from firing in the middle of a minute
		return new PillpopperTime(refDate, postponeTimeSeconds).getContainingMinute();
	}


	// Given a list of drugs and a proposed postpone interval, validate that all
	// drugs can be postponed by that interval.  If so, return null.  If not,
	// return a descriptive error message.
	public static String check24hourformat(String timeExtract, Context context){
		int hour=0;
		int min=0;
		String amPm="";
		int hourIndex=timeExtract.indexOf(":");
		hour=Integer.parseInt(timeExtract.substring(0,hourIndex));
		if(hour>12){
			hour=hour-12;
			amPm=context.getResources().getString(R.string._pm);
		}else{
			amPm=context.getResources().getString(R.string._am);
		}
		if(hour==12){
			amPm=context.getResources().getString(R.string._pm);
		}else if(hour==0){
			hour=12;
			amPm=context.getResources().getString(R.string._am);
		}
		min=Integer.parseInt(timeExtract.substring(hourIndex+1,timeExtract.length()));
		if(min < 10){
			timeExtract=hour+":"+"0"+min+" "+amPm;
		}else{
			timeExtract=hour+":"+min+" "+amPm;
		}
		return timeExtract;
	}

	public static String validatePostpones(List<Drug> drugList, long postoneTimeSeconds, Context context)
	{
		StringBuilder sb = new StringBuilder();

		for (Drug d: drugList) {
			if (d._doseEventCollection.getMaxPostponeTime() == null) {
				continue;
			}

			PillpopperTime postponeTime = d._getPostponeTime(postoneTimeSeconds);

			if (!postponeTime.before(d._doseEventCollection.getMaxPostponeTime())) {
				if (sb.length() > 0) {
					sb.append("\n");
				}
				try{
					String time="";
					if(DateFormat.is24HourFormat(context)){
						time=check24hourformat(PillpopperTime.getLocalizedStringOnlyTime(
								d._doseEventCollection.getMaxPostponeTime(), R.string.__blank, context)
								,context);
					}else{
						time= PillpopperTime.getLocalizedStringOnlyTime(
								d._doseEventCollection.getMaxPostponeTime(), R.string.__blank, context);
					}
					sb.append(String.format(
							context.getString(R.string.cannot_be_postponed_subst),
							d.getName(),
							time
					));
				}catch (Exception e) {
					sb.append(String.format(
							context.getString(R.string.cannot_be_postponed_subst),
							d.getName(),
							PillpopperTime.getLocalizedStringOnlyTime(
									d._doseEventCollection.getMaxPostponeTime(), R.string.__blank, context)
					));
				}
			}
		}

		return Util.cleanString(sb.toString());
	}

	public boolean isOverdue()
	{
		if (getRemindersActive() == false) {
			return false;
		}

		return _doseEventCollection.getOverdueEvent() != null;
	}

	public PillpopperTime getOverdueDate()
	{
		if (null==_doseEventCollection ||_doseEventCollection.getOverdueEvent() == null) {
			return null;
		} else {
			return _doseEventCollection.getOverdueEvent().getDate();
		}
	}

	public List<PillpopperTime> getPassedReminderTimes(){
		if (null==_doseEventCollection) {
			return null;
		} else {
			return _doseEventCollection.pastEventsList();
		}
	}

	// When is the next alarm supposed to fire?
	public PillpopperTime getNextAlarmTime()
	{
		// no reminders if the drug is archived
		if (isArchived()) {
			return null;
		} else {
			return _doseEventCollection.getNextAlarmTime();
		}
	}

	// Refills

	// Get the units (e.g. g, mL, cc) of the drug's stock, based on the user's selection
	// of how many units are consumed on each dose.
	//
	// A null return value means that the user has not yet selected the stock units.
	// An empty string ("") means that the stock is unitless (e.g. number of pills)
	public String getRefillUnits()
	{
		// if units per dose is not a field configured with units, the unit is empty.
		// Also covers drug types without configurable dose quantities (e.g. ointment, custom),
		// where unitsPerDoseFieldType is null.
		if (!(getDrugType().getUnitsPerDoseFieldType() instanceof DoseFieldType_NumericWithUnits))
			return "";

		// If the drug type does have a configurable dose quantity with configurable units,
		// but the units haven't yet been selected, return null
		if (getUnitsPerDoseData() == null) {
			return null;
		}

		// Return the selected units.
		return getUnitsPerDoseData().getString();
	}

	public boolean isGettingRefillAlerts()
	{
		return getRefillAlertDaysOrDoses() > 0;
	}

	// Would the user be out of this drug if numUnitsRemaining are how much he had left?
	// Always returns false if we are not tracking inventory.
	private boolean _isEmpty(double numUnitsRemaining, boolean isPremium)
	{
		// Premium feature only; never return a running low warning for non-premium
		if (!isPremium) {
			return false;
		}

		double unitsConsumedPerDose = getUnitsConsumedPerDose();

		// If we don't know how many units get consumed per dose, bail.
		if (unitsConsumedPerDose < Util.Epsilon) {
			return false;
		}

		// Only return a warning if refill-warnings are in use
		if (!isGettingRefillAlerts()) {
			return false;
		}

		return numUnitsRemaining < unitsConsumedPerDose;
	}

	// Is the user out of this drug?
	public boolean isEmpty(boolean isPremium)
	{
		return _isEmpty(_unitsRemaining, isPremium);
	}

	// Would the user be running low of numUnitsRemaining are how much he had left?
	// Always returns false if we are not tracking inventory.
	private boolean _isRunningLow(double numUnitsRemaining, boolean isPremium)
	{
		// Premium feature only; never return a running low warning for non-premium
		if (!isPremium) {
			return false;
		}

		double unitsConsumedPerDose = getUnitsConsumedPerDose();

		// If we don't know how many units get consumed per dose, bail.
		if (unitsConsumedPerDose < Util.Epsilon) {
			return false;
		}

		// Only return a warning if refill-warnings are in use
		if (!isGettingRefillAlerts()) {
			return false;
		}

		if (_isEmpty(numUnitsRemaining, isPremium)) {
			return false;
		}

		double dosesRemaining = Math.floor(numUnitsRemaining / unitsConsumedPerDose);

		//PillpopperLog.Say(String.format("Doses of %s remaining: %f; refill alert: %d", getName(), dosesRemaining, getRefillAlertDaysOrDoses()));

		Schedule sched = getSchedule();
		switch (_drugPrefs.getPreference("scheduleChoice")) {
			// Scheduled drugs are specified in number of DAYS until empty.
			// All other drugs are specified in number of DOSES until empty.
			case AppConstants.SCHEDULE_CHOICE_SCHEDULED:
				double daysUntilEmpty = dosesRemaining * sched.getDayPeriod() / sched.getTimeList().length();

				if (sched.getDayPeriod() == 7) {
					daysUntilEmpty /= Math.max(1, sched.getDaysOfWeek().size());
				}

				//PillpopperLog.Say("Good for %f days", daysUntilEmpty);
				return daysUntilEmpty <= getRefillAlertDaysOrDoses();

			default:
				return dosesRemaining <= getRefillAlertDaysOrDoses();
		}
	}

	// Is the user running low of this drug currently?
	// Always returns false if we are not tracking inventory.
	public boolean isRunningLow(boolean isPremium)
	{
		return _isRunningLow(_unitsRemaining, isPremium);
	}


	// Give a textual description of a low-stock warning threshold.
	// For scheduled drugs, refillWarningValue is a number of days until empty.
	// For other types of schedules, refillWarningValue is a number of doses until empty.
	public String describeRefillWarningPeriod(Context context, long refillWarningValue)
	{
		switch (_drugPrefs.getPreference("scheduleChoice")) {
			case AppConstants.SCHEDULE_CHOICE_SCHEDULED:
				// refillWarningValue is a number of DAYS until empty
				if (refillWarningValue == 0) {
					return context.getString(R.string.refill_alert_none);
				} else if (refillWarningValue == 1) {
					return context.getString(R.string.refill_alert_one_day);
				} else if (refillWarningValue < 7) {
					return String.format(context.getString(R.string.refill_alert_multiple_days), refillWarningValue);
				} else if (refillWarningValue == 7) {
					return context.getString(R.string.refill_alert_one_week);
				} else {
					return String.format(context.getString(R.string.refill_alert_multiple_weeks), refillWarningValue / 7);
				}

			default:
				// refillWarningValue is a number of DOSES
				if (refillWarningValue == 0) {
					return context.getString(R.string.refill_alert_none);
				} else if (refillWarningValue == 1) {
					return context.getString(R.string.refill_alert_one_dose);
				} else {
					return String.format(context.getString(R.string.refill_alert_multiple_doses), refillWarningValue);
				}
		}
	}

	// Describe the amount in stock (possibly including units)
	public String describeUnitsRemaining()
	{
		return Util.maybeAppendString(
				Util.getTextFromDouble(getUnitsRemaining()),
				getRefillUnits());
	}

	// Describe the amount per refill (possibly including units)
	public String describeUnitsPerRefill()
	{
		return Util.maybeAppendString(
				Util.getTextFromDouble(getUnitsPerRefill()),
				getRefillUnits());
	}

	public List<DoseEvent> getDBValidEventsNearDayToLogMissedDoses(Drug drug, PillpopperDay startDay, PillpopperDay endDay) {
		return DoseEventCollection.getDBValidEventsNearDayToLogMissedDoses(drug, startDay, endDay);
	}

	public void setKPHCHeader(boolean KPHCHeader) {
		isKPHCHeader = KPHCHeader;
	}

	public void set_stateUpdatedListeners(List<StateUpdatedListener> _stateUpdatedListeners) {
		this._stateUpdatedListeners = _stateUpdatedListeners;
	}

	public DoseData get_doseData() {
		return _doseData;
	}

	public void set_doseData(DoseData _doseData) {
		this._doseData = _doseData;
	}

	public Drawable get_imageDrawable() {
		return _imageDrawable;
	}

	public void set_imageDrawable(Drawable _imageDrawable) {
		this._imageDrawable = _imageDrawable;
	}

	public void setChecked(boolean checked) {
		isChecked = checked;
	}

	public void setTempHeadr(boolean tempHeadr) {
		isTempHeadr = tempHeadr;
	}

	public void setNoDrugsFound(boolean noDrugsFound) {
		isNoDrugsFound = noDrugsFound;
	}

	public String get_dose() {
		return _dose;
	}

	public void set_dose(String _dose) {
		this._dose = _dose;
	}

	public void setPAHeader(boolean PAHeader) {
		isPAHeader = PAHeader;
	}

	public List<StateUpdatedListener> get_stateUpdatedListeners() {
		return _stateUpdatedListeners;
	}

	public void setIsRemindersEnabled(String isRemindersEnabled) {
		this.isRemindersEnabled = isRemindersEnabled;
	}

	public String getIsRemindersEnabled() {
		return isRemindersEnabled;
	}

    public String getScheduleGuid() {
        return scheduleGuid;
    }

    public void setScheduleGuid(String scheduleGuid) {
        this.scheduleGuid = scheduleGuid;
    }

    // Interface for a function that should be called if a refill is successful (see MaybeRefill)
	public interface OnRefillSuccessful
	{
		void onRefillSuccessful();
	}

	///////////////////////////////////////////////////////////////////
	// Undo Support

	private Drug _undo = null;
	private int _undoName;

	/////////////////////////////////////////////////
	// Managed Drugs

	private static final String _JSON_LAST_ID_NEEDING_NOTIFY = "lastManagedIdNeedingNotify";
	private static final String _JSON_LAST_ID_NOTIFIED = "lastManagedIdNotified";
	private static final String _JSON_MANAGED_DROPPED = "managedDropped";

	public enum PendingManagedChange
	{
		None,
		Add,
		Change,
		Drop,
	}

	public boolean isManaged()
	{
		//return _doseData.getDrugType().isType(DrugTypeList_Standard.MANAGED);
		// Instead of depending on doseData we are depending on preference for deciding managed drug or not
		return null != _drugPrefs.getPreference("dosageType") && _drugPrefs.getPreference("dosageType").equalsIgnoreCase(DrugTypeList_Standard.MANAGED);
	}

	public boolean isManagedDropped()
	{
		return isManaged() && _drugPrefs.getBoolean(_JSON_MANAGED_DROPPED, false);
	}

	public PendingManagedChange getPendingManagedChange()
	{
		String lastAck = Util.cleanString(_drugPrefs.getPreference(_JSON_LAST_ID_NOTIFIED));
		String currVer = Util.cleanString(_drugPrefs.getPreference(_JSON_LAST_ID_NEEDING_NOTIFY));


		String dosageType = Util.cleanString(_drugPrefs.getPreference("dosageType"));

		// if this is not a managed drug, or we have no current version, there's no pending change
		if (currVer == null || /*!isManaged()*/ (null!=dosageType && !("managed").equalsIgnoreCase(dosageType)))
			return PendingManagedChange.None;

		// "silent drops" are not allowed -- so we have moved the check for dropped drugs
		// up above the check for matching version numbers (which comes next). If the drug
		// has been dropped but we haven't yet marked it as invisible here on the client,
		// notify a drop.
		if (isManagedDropped() && !isExplicitlyInvisible()) {
			return PendingManagedChange.Drop;
		}

		// if this is a managed drug and the versions match, it means there was no change
		if (lastAck != null && lastAck.equals(currVer))
			return PendingManagedChange.None;

		// okay, now we know that it's a managed drug, that it has a notification version number,
		// and that the last ack either doesn't match or doesn't exist. So some change has to be
		// notified. If the drug is marked as dropped, call it a drop. Note this covers the case
		// that it was dropped even before the user acknowledged that it was added.

		// this case should no longer fire because of the above drop check, but we keep it in
		// just in case
		if (isManagedDropped()) {
			return PendingManagedChange.Drop;
		}

		// okay! now, if we've never acked this drug, it's an add. Otherwise, it's a change.
		if (lastAck == null)
			return PendingManagedChange.Add;
		else
			return PendingManagedChange.Change;
	}

	public PendingManagedChange getPendingManagedChange(Drug drug)
	{
		String lastAck = Util.cleanString(drug.getPreferences().getPreference(_JSON_LAST_ID_NOTIFIED));
		String currVer = Util.cleanString(drug.getPreferences().getPreference(_JSON_LAST_ID_NEEDING_NOTIFY));


		String dosageType = Util.cleanString(drug.getPreferences().getPreference("dosageType"));

		// if this is not a managed drug, or we have no current version, there's no pending change
		if (currVer == null || /*!isManaged()*/ (null!=dosageType && !("managed").equalsIgnoreCase(dosageType)))
			return PendingManagedChange.None;

		// "silent drops" are not allowed -- so we have moved the check for dropped drugs
		// up above the check for matching version numbers (which comes next). If the drug
		// has been dropped but we haven't yet marked it as invisible here on the client,
		// notify a drop.
		if (isManagedDropped() && !isExplicitlyInvisible()) {
			return PendingManagedChange.Drop;
		}

		// if this is a managed drug and the versions match, it means there was no change
		if (lastAck != null && lastAck.equals(currVer))
			return PendingManagedChange.None;

		// okay, now we know that it's a managed drug, that it has a notification version number,
		// and that the last ack either doesn't match or doesn't exist. So some change has to be
		// notified. If the drug is marked as dropped, call it a drop. Note this covers the case
		// that it was dropped even before the user acknowledged that it was added.

		// this case should no longer fire because of the above drop check, but we keep it in
		// just in case
		if (isManagedDropped()) {
			return PendingManagedChange.Drop;
		}

		// okay! now, if we've never acked this drug, it's an add. Otherwise, it's a change.
		if (lastAck == null)
			return PendingManagedChange.Add;
		else
			return PendingManagedChange.Change;
	}

	public void ackPendingChanges()
	{
		_drugPrefs.setPreference(_JSON_LAST_ID_NOTIFIED, _drugPrefs.getPreference(_JSON_LAST_ID_NEEDING_NOTIFY));
		_drugChanged();
	}

	/////////////////////////////
	// Comparators

	public static class ByArchivalStatusComparator implements Comparator<Drug>
	{
		@Override
		public int compare(Drug lhs, Drug rhs)
		{
			int lhs_int = (lhs.isArchived() ? 1 : 0);
			int rhs_int = (rhs.isArchived() ? 1 : 0);

			return lhs_int - rhs_int;
		}
	}

	public static class OverdueTimeComparator implements Comparator<Drug>{

		@Override
		public int compare(Drug lhs, Drug rhs)
		{
			return (int) (lhs.getOverdueDate().getGmtSeconds() - rhs.getOverdueDate().getGmtSeconds());
		}

	}

	// Simple alphabetical by name comparator
	public static class AlphabeticalByNameComparator implements Comparator<Drug>
	{
		@Override
		public int compare(Drug lhs, Drug rhs)
		{
			if (lhs.getName() != null && rhs.getName() !=null) {
				return lhs.getName().compareToIgnoreCase(rhs.getName());
			} else {
				return 0;
			}
		}
	}

	// Sort-by-next-dose comparator.  Sort each drug by <date/time> (using the definition of date/time
	// in computeNextDose) so that future times are sorted in chronological order at the top of the list
	// and past times are sorted in reverse chronological order at the bottom of the list.
	public static class ByNextDoseComparator implements Comparator<Drug>
	{
		PillpopperTime _now = PillpopperTime.now();

		@Override
		public int compare(Drug lhs, Drug rhs)
		{
			boolean lhsIsFuture = _now.before(lhs._doseEventCollection.getSortDate());
			boolean rhsIsFuture = _now.before(rhs._doseEventCollection.getSortDate());

			// Force all future doses to come before all past doses
			if (lhsIsFuture != rhsIsFuture) {
				if (lhsIsFuture) {
					return -1;
				} else {
					return 1;
				}
			}

			// Sort chronologically
			long timeDiff = (
					lhs._doseEventCollection.getSortDate().getGmtSeconds()
							- rhs._doseEventCollection.getSortDate().getGmtSeconds());

			if (lhsIsFuture) {
				// If they're in the future: sort chronological
				return (int) timeDiff;
			} else {
				// If they're in the past: sort reverse-chronological
				return (int) -timeDiff;
			}
		}
	}

	// By-person-name sort.
	public static class ByPersonComparator implements Comparator<Drug>
	{
		@Override
		public int compare(Drug lhs, Drug rhs)
		{
			return Person.compare(lhs.getPerson(), rhs.getPerson());
		}
	}

	// Comparator for sorting based on reminders for drug
	// if it does no contain reminders should come first followed by reminder set drugs.
	public static class ByRemindersetComparator implements Comparator<Drug>{

		@Override
		public int compare(Drug lhs, Drug rhs) {

			int lhs_timeList = (lhs.getScheduleCount()>0?1:0);
			int rhs_timeList = (rhs.getScheduleCount()>0?1:0);

			if(null != lhs.getSchedule().getEnd() && lhs.getSchedule().getEnd().before(PillpopperDay.today())){
				lhs_timeList = 0;
			}
			if(null != rhs.getSchedule().getEnd() && rhs.getSchedule().getEnd().before(PillpopperDay.today())){
				rhs_timeList = 0;
			}
			return lhs_timeList - rhs_timeList;
		}
	}


	public static class ByDrugTypeComparator implements Comparator<Drug>
	{
		@Override
		public int compare(Drug lhs, Drug rhs)
		{
			return DrugType.compare(lhs.getDrugType(), rhs.getDrugType());
		}
	}

	public static class ByManagedNotificationPendingComparator implements Comparator<Drug>
	{
		@Override
		public int compare(Drug lhs, Drug rhs)
		{
			int lhs_int = (lhs.getPendingManagedChange() == PendingManagedChange.None ? 1 : 0);
			int rhs_int = (rhs.getPendingManagedChange() == PendingManagedChange.None ? 1 : 0);

			return lhs_int - rhs_int;
		}

	}

	/////////////////////////////////////////////////////////////////////////////
	// Image support

	private static final String _IMAGECACHE_DIRNAME = "imagecache";

	// Get the directory where images are stored either on local storage or on the external SD card
	private static File _getImageCacheOrBackupDir(Context context, boolean backupDir) throws IOException
	{
		File baseDir = null;

		if (backupDir) {
			baseDir = FileHandling.getExternalStorageDirectory(context, FileHandling.StorageLocation.External_Durable);
		} else {
			baseDir = context.getFilesDir();
		}

		if (baseDir == null)
			return null;

		File retval = new File(baseDir, _IMAGECACHE_DIRNAME);

		if (!retval.exists()) {
			retval.mkdirs();
		}

		return retval;
	}


	// Copy all images from local storage out to the external SD card
	public static boolean backupImages(Context context)
	{
		try {
			FileHandling.copyDirectory(
					context,
					_getImageCacheOrBackupDir(context, false),
					_getImageCacheOrBackupDir(context, true)
			);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	// Copy all images from the external SD card back to local storage
	public static boolean restoreImages(Context context)
	{
		try {
			FileHandling.copyDirectory(
					context,
					_getImageCacheOrBackupDir(context, true),
					_getImageCacheOrBackupDir(context, false)
			);
			return true;
		} catch (IOException e) {
			return false;
		}
	}

	public static File getImageCacheDir(Context context) throws IOException
	{
		return _getImageCacheOrBackupDir(context, false);
	}

	public static File getImageCacheFile(Context context, String guid)
	{
		if (guid == null) {
			return null;
		} else {
			try {
				return new File(getImageCacheDir(context), "drugimage-" + guid + ".jpg");
			} catch (IOException e) {
				return null;
			}
		}
	}

    public File getImageCacheFile(Context context) {
        return getImageCacheFile(context, getImageGuid());
    }

	private Drawable _imageDrawable;

	public Drawable getImageDrawable(Context context)
	{
		if (get_imageDrawable() == null && getImageGuid() != null) {
			try {
				set_imageDrawable(new BitmapDrawable(MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.fromFile(getImageCacheFile(context)))));
			} catch (IOException e) {
				set_imageDrawable(null);
			}
		}

		return get_imageDrawable();
	}

	private static final String _JSON_DRUGIMAGE_GUID = "imageGUID";

	public void setImageGuid(String guid)
	{
		_drugPrefs.setPreference(_JSON_DRUGIMAGE_GUID, guid);
		_drugPrefs.setBoolean(_JSON_DRUGIMAGE_SENT, false);
		set_imageDrawable(null);
		_drugChanged();

		PillpopperLog.say("set image guid: %s", guid);
	}

	public String getImageGuid()
	{
		return Util.cleanString(_drugPrefs.getPreference(_JSON_DRUGIMAGE_GUID));
	}

	/////// Image Sync ///////////////

	public class ImageSyncRequest
	{
		private File _file;
		private String _imageGuid;
		private String _drugGuid;

		private ImageSyncRequest(Context context)
		{
			_file = Drug.this.getImageCacheFile(context);
			_drugGuid = Drug.this.getGuid();
			_imageGuid = Drug.this.getImageGuid();
		}

		public File getFile() { return _file; }
		public String getDrugGuid() { return _drugGuid; }
		public String getImageGuid() { return _imageGuid; }

		// We bother overriding equals so we can insert these into a set and have it
		// automatically remove duplicate requests.
		@Override
		public boolean equals(Object obj)
		{
			if (this == obj) return true;
			if (!(obj instanceof ImageSyncRequest))
				return false;

			ImageSyncRequest other = (ImageSyncRequest) obj;

			return (_imageGuid == null ? other._imageGuid == null : _imageGuid.equalsIgnoreCase(other._imageGuid));
		}

		@Override
		public int hashCode()
		{
			return _imageGuid == null ? 0 : _imageGuid.hashCode();
		}
	}

	private static final String _JSON_DRUGIMAGE_SENT = "drugimageSent";

	// prepare for outgoing sync: if we need to send our drug image, return the path to it.
	// otherwise, return null.
	public ImageSyncRequest getImageToTransmit(Context context)
	{
		// if no image has been set for this drug, do nothing
		if (getImageGuid() == null)
			return null;

		// if we haven't yet sent this drugimage, tell the syncer to send it
		if (_drugPrefs.getBoolean(_JSON_DRUGIMAGE_SENT, false) == false) {
			PillpopperLog.say("SYNC: Drug %s's image %s hasn't yet been uploaded. Will send it during sync.",
					Util.friendlyGuid(getGuid()),
					Util.friendlyGuid(getImageGuid())
			);

			return new ImageSyncRequest(context);
		} else {
			return null;
		}
	}

	// Is there a missing drug image? If so, get the syncer to retrieve it for us
	public ImageSyncRequest getImageToReceive(Context context)
	{
		File imageFile = getImageCacheFile(context);

		if (imageFile != null && !imageFile.exists()) {
			PillpopperLog.say("SYNC: Drug %s's image %s doesn't exist. Asking for it during sync.",
					Util.friendlyGuid(getGuid()),
					Util.friendlyGuid(getImageGuid())
			);
			return new ImageSyncRequest(context);
		} else {
			return null;
		}
	}


	// completion of outgoing sync:
	// notification that gets called by the syncer if it successfully transmitted a file.
	// It may not be the current image if the image changed in between the time
	// the sync preparation ran and the sync completed.
	public void setImageSuccessfullyTransmitted(Context context, String guidTransmitted)
	{
		if (guidTransmitted == null) {
			PillpopperLog.say("SYNC: Drug:setImageSuccessfullyTransmitted: got null guid");
			return;
		}

		if (guidTransmitted.equalsIgnoreCase(getImageGuid())) {
			PillpopperLog.say("SYNC: Drug %s: recording that our image %s successfully transmitted",
					Util.friendlyGuid(getGuid()),
					Util.friendlyGuid(getImageGuid())
			);
			_drugPrefs.setBoolean(_JSON_DRUGIMAGE_SENT, true);
			_drugChanged();
		} else {
			PillpopperLog.say("SYNC: Drug %s: image %s transmitted, but we're already on image %s! Sync race.",
					Util.friendlyGuid(getGuid()),
					guidTransmitted,
					Util.friendlyGuid(getImageGuid())
			);
		}
	}


	///////////////////////////////////////
	/// database-specific fields.
	/// (perhaps ideally we'd subclass drug)

	public void setNdc(String ndc)
	{
		_drugPrefs.setPreference(DrugType_Database.JSON_DATABASE_NDC, ndc);
		_drugChanged();
	}

	public void setMedType(String medType)
	{
		_drugPrefs.setPreference(DrugType_Database.JSON_DATABASE_MEDTYPE, medType);
		_drugChanged();
	}

	public void setMedForm(String medForm)
	{
		_drugPrefs.setPreference(DrugType_Database.JSON_DATABASE_MEDFORM, medForm);
		_drugChanged();
	}

	public void set_effLastTaken(PillpopperTime _effLastTaken)
	{
		this._effLastTaken = _effLastTaken;
	}

	public String getGenericName(){

		String drugName = getName();
		if (null != drugName) {
			int refPoint = drugName.indexOf("(");

			if (refPoint == -1) {
				return "";
			}
			String genericName = drugName.substring(refPoint);
			if (genericName != null && !("").equals(genericName)) {
				genericName = genericName.replaceAll("\\(", "");
				genericName = genericName.replaceAll("\\)", "");
				return genericName;
			}
		}
			return "";
		}

	public String getFirstName(){

		String drugName = !Util.isEmptyString(getName()) ? getName() : "";
		int refPoint = -1;

		if (!drugName.isEmpty()) {
			refPoint = drugName.indexOf("(");
		}
		if (refPoint==-1) {
			return drugName;
		}

		if(drugName.isEmpty()){
			PillpopperLog.say(refPoint + "refpoint :: length -> " + 0);
		}else {
			PillpopperLog.say(refPoint + "refpoint :: length -> " + drugName.length());
		}

		String drugFirstName = drugName.substring(0,refPoint);
		if (drugFirstName!=null && !drugFirstName.equals("")) {
			return drugFirstName;
		}
		return "";
	}

	public boolean isChecked() {
		return isChecked;
	}

	public void setIsChecked(boolean isChecked) {
		this.isChecked = isChecked;
	}

	private boolean isChecked=false;

	public boolean isPAHeader() {
		return isPAHeader;
	}

	public void setIsPAHeader(boolean isPAHeader) {
		this.isPAHeader = isPAHeader;
	}

	private boolean isPAHeader;

	public boolean isKPHCHeader() {
		return isKPHCHeader;
	}

	public void setIsKPHCHeader(boolean isKPHCHeader) {
		this.isKPHCHeader = isKPHCHeader;
	}

	private boolean isKPHCHeader;

	private String scheduledFrequency;

	public String getScheduledFrequency() {
		return scheduledFrequency;
	}

	public void setScheduledFrequency(String scheduledFrequency) {
		this.scheduledFrequency = scheduledFrequency;
	}

	public String getMemberFirstName() {
		return memberFirstName;
	}

	public void setMemberFirstName(String memberFirstName) {
		this.memberFirstName = memberFirstName;
	}

	private String memberFirstName;

	private PillpopperTime scheduledTime;
	private int opID;

	public PillpopperTime getScheduledTime() {
		return scheduledTime;
	}

	public void setScheduledTime(PillpopperTime scheduledTime) {
		this.scheduledTime = scheduledTime;
	}

	public int getOpID() {
		return opID;
	}

	public void setOpID(int opID) {
		this.opID = opID;
	}


	private String actionDate;

	public String getActionDate() {
		return actionDate;
	}

	public void setActionDate(String actionDate) {
		this.actionDate = actionDate;
	}

	public String getRecordDate() {
		return recordDate;
	}

	public void setRecordDate(String recorderDate) {
		this.recordDate = recorderDate;
	}

	private String recordDate;
	private boolean isActionDateRequired;

	public boolean getIsActionDateRequired() {
		return isActionDateRequired;
	}

	public void setIsActionDateRequired(boolean isActionDateRequired) {
		this.isActionDateRequired = isActionDateRequired;
	}

	private String historyScheduleDate;

	public String getHistoryScheduleDate() {
		return historyScheduleDate;
	}

	public void setHistoryScheduleDate(String historyScheduleDate) {
		this.historyScheduleDate = historyScheduleDate;
	}
}
