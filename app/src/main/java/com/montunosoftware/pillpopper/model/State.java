package com.montunosoftware.pillpopper.model;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;

import com.google.gson.Gson;
import com.montunosoftware.pillpopper.android.DoseAlarmHandler;
import com.montunosoftware.pillpopper.android.HomeContainerActivity;
import com.montunosoftware.pillpopper.android.util.FDADrugDatabase;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContext;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.PillpopperParseException;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.database.DatabaseConstants;
import com.montunosoftware.pillpopper.database.model.IntermittentMultiPillpopperResponse;
import com.montunosoftware.pillpopper.database.model.IntermittentSyncMultiResponse;
import com.montunosoftware.pillpopper.database.model.PillList;
import com.montunosoftware.pillpopper.database.model.PillpopperMultiResponse;
import com.montunosoftware.pillpopper.database.model.PillpopperResponse;
import com.montunosoftware.pillpopper.database.model.ResponseArray;
import com.montunosoftware.pillpopper.database.model.UserList;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandler;
import com.montunosoftware.pillpopper.database.persistence.DatabaseUtils;

import org.json.JSONObject;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.model.SignonResult;
import org.kp.tpmg.mykpmeds.activation.model.User;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager;

import java.io.File;
import java.util.ArrayList;
import java.util.GregorianCalendar;
import java.util.Iterator;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class State implements StateUpdatedListener  {
    public static final String REMINDER_SOUND_DEFAULT = "default";

    public void setPreferences(Preferences pref) {
        _preferences = pref;
    }

    public State(PillpopperAppContext.Edition edition, FDADrugDatabase fdaDrugDatabase) {
        _updateLocale();
        _preferences.registerStateUpdatedListener(this);
        _drugTypeList = new DrugTypeList(edition, fdaDrugDatabase, this);
    }

    // initial get state
    public State(JSONObject jsonState, PillpopperAppContext.Edition edition, FDADrugDatabase fdaDrugDatabase, Context pillpopperactivity) throws PillpopperParseException {
        if (jsonState == null) {
            throw new PillpopperParseException("State::State: null state block");
        }
        PillpopperResponse result;

        try {
            Gson gson = new Gson();
            result = gson.fromJson(jsonState.toString(), PillpopperResponse.class);
            if (null != result) {

                //delete user records which are not in register response
                updateUserData(pillpopperactivity);

                UserList[] userList = result.getUserList();


                if (null != userList) {
                    // Looping the userList object

                    copyLateRemindersToTempTable(pillpopperactivity);
                    SharedPreferenceManager sharedPreferenceManager = SharedPreferenceManager.getInstance(pillpopperactivity, AppConstants.AUTH_CODE_PREF_NAME);

                        for (UserList list : userList) {

                            if (list.hasChanges()) {

                                FrontController.getInstance(pillpopperactivity).deleteDataOfUserId(list.getUserId());

                                PillList[] pillInfo = list.getPillList();

                                // Inserting the User Preference Information into database
                                DatabaseHandler.getInstance(pillpopperactivity).insert(pillpopperactivity, DatabaseConstants.USER_PREFERENCE_TABLE, list, "", "");

                                for (int k = 0; k < list.getPillList().length; k++) {
                                    DatabaseHandler.getInstance(pillpopperactivity).insert(pillpopperactivity, DatabaseConstants.PILL_TABLE, list.getPillList()[k], "", list.getUserId());

                                    DatabaseHandler.getInstance(pillpopperactivity).insertPillSchedule(DatabaseConstants.PILL_SCHEDULE_TABLE, list.getPillList()[k], "");
                                }

                                // Looping the PillList object
                                for (PillList pillList : pillInfo) {
                                    //insert pill Preferences
                                    DatabaseHandler.getInstance(pillpopperactivity).insert(pillpopperactivity, DatabaseConstants.PILL_PREFERENCE_TABLE, pillList, "", "");
                                }
                            }
                        }
                }
                //update lastSyncToken for each user from getState response
                if (null != pillpopperactivity) {
                    updateUsersLastSyncTokenFromGetState(pillpopperactivity, userList);
                }
            }
        } catch (Exception e) {
            LoggerUtils.error("Model_State: error parsing json state block"+e.getMessage());
        }

        restoreLateRemindersFromTempTable(pillpopperactivity);

        _updateLocale();

        // Put these last so we don't get load-time notifications
        _drugList.registerStateUpdatedListener(this);
        _preferences.registerStateUpdatedListener(this);
    }

    private void copyLateRemindersToTempTable(Context context) {
        try {
            DatabaseHandler.getInstance(context).executeSQL(DatabaseConstants.DROP_TABLE_TEMP_LATE_REMINDERS);
            DatabaseHandler.getInstance(context).executeSQL(DatabaseConstants.CREATE_TABLE_TEMP_LATE_REMINDERS);
            DatabaseHandler.getInstance(context).executeSQL(DatabaseConstants.COPY_FROM_LATE_REMINDERS_TO_TEMP_TABLE);
        }catch (Exception e){
            PillpopperLog.say(e.getMessage());
        }
    }

    private void restoreLateRemindersFromTempTable(Context context){
        try {
            DatabaseHandler.getInstance(context).executeSQL(DatabaseConstants.COPY_TO_LATE_REMINDERS_FROM_TEMP_TABLE);
            DatabaseHandler.getInstance(context).executeSQL(DatabaseConstants.DROP_TABLE_TEMP_LATE_REMINDERS);
        }catch (Exception e){
            PillpopperLog.say(e.getMessage());
        }
    }

    private void updateUsersLastSyncTokenFromGetState(Context context,UserList[] result) {
        for(UserList user : result){
            FrontController.getInstance(context).updateUsersLastSyncToken(user.getUserId(), user.getLastSyncToken(), user.hasChanges());
        }
    }

    public State(JSONObject jsonState, Context context) throws PillpopperParseException {
        if (jsonState == null) {
            throw new PillpopperParseException("State::State: null state block");
        }
        IntermittentSyncMultiResponse result;

        ResponseArray responseArray[] = new ResponseArray[0];

        try {
            Gson gson = new Gson();
            result = gson.fromJson(jsonState.toString(), IntermittentSyncMultiResponse.class);
            if(null != result)
                responseArray = result.getPillpopperMultiResponse().getResponseArray();


            if (null != result && responseArray.length>0) {

                UserList[] userList = responseArray[responseArray.length-1].getMultiPillpopperResponse().getUserList();

                if (null != userList) {
                    if(!FrontController.getInstance(context).isLogEntryAvailable()) {
                        //delete user records which are not in register response
                        updateUserData(context);

                        // Looping the userList object
                        for (UserList list : userList) {

                            if (list.hasChanges()) {

                                FrontController.getInstance(context).deleteDataOfUserId(list.getUserId());

                                PillList[] pillInfo = list.getPillList();

                                // Inserting the User Preference Information into database
                                DatabaseHandler.getInstance(context).insert(context, DatabaseConstants.USER_PREFERENCE_TABLE, list, "", "");

                                for (int k = 0; k < list.getPillList().length; k++) {
                                    DatabaseHandler.getInstance(context).insert(context, DatabaseConstants.PILL_TABLE, list.getPillList()[k], "", list.getUserId());
                                    DatabaseHandler.getInstance(context).insertPillSchedule(DatabaseConstants.PILL_SCHEDULE_TABLE, list.getPillList()[k], "");
                                }

                                // Looping the PillList object
                                for (PillList pillList : pillInfo) {
                                    //insert pill Preferences
                                    DatabaseHandler.getInstance(context).insert(context, DatabaseConstants.PILL_PREFERENCE_TABLE, pillList, "", "");
                                }
                            }
                        }
                        //update lastSyncToken for each user from getState response
                        updateUsersLastSyncTokenFromGetState(context, userList);
                    }
                }
            }
        } catch (Exception e) {
            throw new PillpopperParseException("Model_State: error parsing json state block");
        }
        _updateLocale();
        _drugList.registerStateUpdatedListener(this);
        _preferences.registerStateUpdatedListener(this);
    }

    private void updateUserData(Context pillpopperactivity) {
        if (null != RunTimeData.getInstance().getRegistrationResponse() &&
                null != RunTimeData.getInstance().getRegistrationResponse().getResponse()) {

            //from Register Resp
            SignonResult result = RunTimeData.getInstance().getRegistrationResponse().getResponse();

            //data from DB
            List<String> userIds = FrontController.getInstance(pillpopperactivity).getAllUserIds();

            if (userIds != null && !userIds.isEmpty()) {
                for (int i = 0; i < userIds.size(); i++) {
                    boolean userExists = false;
                    for (User user : result.getUsers()) {
                        if (user.getUserId().equalsIgnoreCase(userIds.get(i))) {
                            userExists = true;
                            break;
                        }
                    }
                    if (!userExists) {
                        DatabaseUtils.getInstance(pillpopperactivity).clearDBTableForUser(DatabaseConstants.USER_TABLE,userIds.get(i));
                        FrontController.getInstance(pillpopperactivity).deleteDataOfUserId(userIds.get(i));
                    }
                }

                for (User user : result.getUsers()) {
                    //insert user data if it doesn't exist. else update
                    if (!userIds.contains(user.getUserId())) {
                        FrontController.getInstance(pillpopperactivity).insertUserData(pillpopperactivity, user);
                    } else {
                        FrontController.getInstance(pillpopperactivity).updateUserData(pillpopperactivity,user);
                    }

                    //logic to disable users which was not selected in welcome activity
                    if (null != RunTimeData.getInstance().getSelectedUsersList()
                            && !RunTimeData.getInstance().getSelectedUsersList().isEmpty()
                            && !RunTimeData.getInstance().getSelectedUsersList().contains(user.getUserId())) {
                        FrontController.getInstance(pillpopperactivity).updateMemberPreferencesToDB(user.getUserId(), "N", "N");
                    }
                }
            }else{
                if(result!=null){
                    for (User user : result.getUsers()) {
                        //insert user data if it doesn't exist. else update
                        FrontController.getInstance(pillpopperactivity).insertUserData(pillpopperactivity, user);

                        //logic to disable users which was not selected in welcome activity
                        String userId = user.getUserId();
                        if (null != RunTimeData.getInstance().getSelectedUsersList()
                                && !RunTimeData.getInstance().getSelectedUsersList().isEmpty()
                                && !RunTimeData.getInstance().getSelectedUsersList().contains(userId)) {
                            FrontController.getInstance(pillpopperactivity).updateMemberPreferencesToDB(userId, "N", "N");
                        }
                    }
                }
            }
            //update the selected users as enabled "Y"
            if (null != RunTimeData.getInstance().getSelectedUsersList()) {
                for (String userID : RunTimeData.getInstance().getSelectedUsersList()) {
                    FrontController.getInstance(pillpopperactivity).updateEnableUsersData(userID);
                }
            }

            //check for teen and update the teen user as enabled "N"
            if (null != result) {
                boolean hasTeen = false;
                String userId="";
                for (User user : result.getUsers()) {
                    if (user.isTeen() && user.getEnabled().equalsIgnoreCase("Y")) {
                        hasTeen = true;
                        userId =  user.getUserId();
                    }
                }
                if (hasTeen && !Util.getTeenToggleEnabled()) {
                    DatabaseUtils.getInstance(pillpopperactivity).setTeenUserEnabledFalse(userId);
                    SharedPreferenceManager.getInstance(pillpopperactivity, AppConstants.AUTH_CODE_PREF_NAME).putBoolean("showTeenCard", true, false);
                }
            }

            if(RunTimeData.getInstance().isFromTutorialScreen()){
                updateRemindersPreferencesInDB(pillpopperactivity);
                RunTimeData.getInstance().setFromTutorialScreen(false);
            }
        }
    }

    private void updateRemindersPreferencesInDB(Context pillpopperactivity) {
        for (User user : RunTimeData.getInstance().getEnabledUsersList()) {
            FrontController.getInstance(pillpopperactivity)
                    .updateMemberPreferencesToDB(user.getUserId(), "Y", "Y");
            LoggerUtils.info("Enabled Medications and Reminders for " + user.getFirstName());
        }
    }


    ///// Time zone records

    private static final String _JSON_TZ_NAME = "tz_name";
    private static final String _JSON_TZ_SECS = "tz_secs";
    private static final String _JSON_LANGAUGE = "language";
    private static final String _JSON_OS_VERSION = "osVersion";
    private static final String _JSON_DEVICE_NAME = "deviceName";

    private void _updateLocale() {
        try {
            TimeZone tz = TimeZone.getDefault();
            _preferences.setPreference(_JSON_TZ_NAME, tz.getDisplayName());
            _preferences.setLong(_JSON_TZ_SECS, tz.getOffset(GregorianCalendar.getInstance().getTimeInMillis()) / 1000);
            _preferences.setPreference(_JSON_LANGAUGE, Locale.getDefault().toString());
        }catch (AssertionError e){
            LoggerUtils.info("AssertionError:"+e);
        }catch (Exception e){
            LoggerUtils.info("Exception:"+e);
        }
        _preferences.setPreference(_JSON_OS_VERSION, String.valueOf(Build.VERSION.SDK_INT));
        _preferences.setPreference(_JSON_DEVICE_NAME, "android");
    }

    private static final String _SAVE_FILENAME = "appstate.txt";


    public static void deletePersistentState(Context context) {
        File persistentStateFile = new File(context.getFilesDir(), _SAVE_FILENAME);
        if(!persistentStateFile.delete()){
            PillpopperLog.say("Oops, State - deletePersistentState - Failed to delete file");
        }
    }

    // A list of guys that want to get notified every time our state is updated
    List<StateUpdatedListener> _stateUpdatedListeners = new ArrayList<>();

    public synchronized void registerStateUpdatedListener(StateUpdatedListener stateUpdatedListener) {
        _stateUpdatedListeners.add(stateUpdatedListener);
    }

    public synchronized void unregisterStateUpdatedListener(StateUpdatedListener stateUpdatedListener) {
        _stateUpdatedListeners.remove(stateUpdatedListener);
    }


    // This is called whenever the state is updated.
    private void _stateUpdated() {
        // Notify any listeners of the update so they can update views
        for (StateUpdatedListener stateUpdatedListener : _stateUpdatedListeners) {
            synchronized (this) {
                StateUpdatedListener sul = stateUpdatedListener;
                sul.onStateUpdated();

            }
        }
    }

    @Override
    public void onStateUpdated() {
        // This is the callback *up* from members of State that call us when one of them
        // has updated its state.
        _stateUpdated();
    }


    public static final String JSON_PREFERENCES = "preferences";
    private Preferences _preferences = new Preferences();

    public Preferences getPreferences() {
        return _preferences;
    }

    private long syncTimeMSec;


    ///////////////////////////////////////////////////////////////
    //// Drug List and Drug Type List
    private DrugTypeList _drugTypeList;

    public DrugTypeList getDrugTypeList() {
        return _drugTypeList;
    }

    private DrugList _drugList = new DrugList(this);

    public DrugList getDrugList() {
        return _drugList;
    }

    public PillpopperTime getEarliestAlarm(Context ctx) {
        PillpopperTime earliestAlarm = null;
        PillpopperLog.say("Notification -  earliest Alarm-- Start ");
        for (Drug drug : FrontController.getInstance(ctx).getDrugListForOverDue(ctx)) {
            drug.computeDBDoseEvents(ctx,drug, PillpopperTime.now(), 60);
            // filter and consider only the drugs of users enabled with reminders
            if (Util.isValidDrugForReminders(drug)) {
                if (null != drug.get_doseEventCollection() && null != drug.get_doseEventCollection().getNextEvent()) {
                    PillpopperTime nextEvent = drug.get_doseEventCollection().getNextEvent().getDate();
                    PillpopperLog.say("Notification -  earliest Alarm-- NextEvent -- " + drug.getName() + " --" + PillpopperTime.getDebugString(nextEvent));
                    if (null != drug.getOverdueDate()) {
                        PillpopperLog.say("Notification -  earliest Alarm-- OverdueDate -- " + drug.getName() + " --" + PillpopperTime.getDebugString(drug.getOverdueDate()));
                        long repeatReminderSecs = FrontController.getInstance(ctx).getSecondaryReminderPeriodSecs(FrontController.getInstance(ctx).getPrimaryUserIdIgnoreEnabled());
                        PillpopperTime overdueDateSecondaryReminder = new PillpopperTime(drug.getOverdueDate(), repeatReminderSecs);
                        if (overdueDateSecondaryReminder.after(PillpopperTime.now()) && overdueDateSecondaryReminder.before(nextEvent)) {
                            nextEvent = overdueDateSecondaryReminder;
                        }
                    }

                    if (nextEvent == null)
                        continue;

                    // don't consider, if the nextEvent is before current time
                    if (nextEvent.before(PillpopperTime.now()))
                        continue;

                    if (earliestAlarm == null || nextEvent.before(earliestAlarm)) {
                        earliestAlarm = nextEvent;
                        PillpopperLog.say("Notification -  earliest Alarm-- new " + PillpopperTime.getDebugString(nextEvent));
                    }
                }
            } else {
                continue;
            }
        }

        PillpopperLog.say("Notification -  earliest Alarm-- End ");
        PillpopperLog.say("Notification -  earliest Alarm " + PillpopperTime.getDebugString(earliestAlarm));
        return earliestAlarm;
    }

    private AlarmManager _alarmManager = null;
    private PendingIntent _alarmSendingIntent = null;
    private Intent _alarmReceivingIntent = null;

    public void setAlarm(Context context) {
        if (_alarmManager == null) {
            _alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        }

        if (_alarmReceivingIntent == null) {
            _alarmReceivingIntent = new Intent(context, DoseAlarmHandler.class);
        }

        if (_alarmSendingIntent == null) {
            _alarmSendingIntent = PendingIntent.getBroadcast(context, 0, _alarmReceivingIntent, 0);
        }

        // Cancel an old alarm, if any
        _alarmManager.cancel(_alarmSendingIntent);

        // Set a new alarm, if needed
        PillpopperTime earliestAlarm = getEarliestAlarm(context);
        if (earliestAlarm != null) {
            PillpopperLog.say("Notification - setting alarm for %s", PillpopperTime.getDebugString(earliestAlarm));
            _alarmManager.setExact(AlarmManager.RTC_WAKEUP, earliestAlarm.getGmtSeconds() * 1000, _alarmSendingIntent);
        } else {
            PillpopperLog.say("Notification - not setting alarm; no reminders");
        }

    }


    ///////////////////////////////////////////////////////////////////////////////////////////////////
    // Subscription type

    private String _accountId = null;


    public String getAccountId() {
        return _accountId;
    }

    public void setAccountId(String accountId) {
        _accountId = accountId;
    }

    public boolean isRegistered() {
        return _accountId != null;
    }

    ///////////////////////////////////////////////////////////////////////
    // Reminder sound
    public static final String REMINDER_SOUND_NONE = "silence";

    ///////////////////////////////////////////////////////////////////////
    // Reminder vibration
    private static final String _JSON_REMINDER_VIBRATION = "reminderVibration";

    public boolean getReminderVibration() {
        return _preferences.getBoolean(_JSON_REMINDER_VIBRATION, true);
    }

    public static final String QUICKVIEW_OPTED_IN = "1";
    public static final String QUICKVIEW_OPTED_OUT = "0";

    //////////////////////////////////////////////////////////////////
    /////////////////////////////////////////////////////////////////
    /// Drug Sort Order

    public enum DrugSortOrder {
        ByDrugName,
        ByNextDose,
        ByPerson,
        ByReminderSet,
        ByDrugType,
    }

    public DrugSortOrder getDrugSortOrderByReminderSet() {
        return DrugSortOrder.ByReminderSet;
    }


    private HomeContainerActivity.NavigationHome _selectedHomeFragment = HomeContainerActivity.NavigationHome.DAILY_SCHEDULE;

    public HomeContainerActivity.NavigationHome getSelectedHomeFragment() {
        return _selectedHomeFragment;
    }

    public void setSelectedHomeFragment(HomeContainerActivity.NavigationHome selectedHomeFragment) {
        this._selectedHomeFragment = selectedHomeFragment;
    }

    private PillpopperDay _scheduleViewDay = PillpopperDay.today();

    public PillpopperDay getScheduleViewDay() {
        return _scheduleViewDay;
    }

    public void setScheduleViewDay(PillpopperDay scheduleViewDay) {
        this._scheduleViewDay = scheduleViewDay;
    }

    private Object _syncLock = new Object();
    private boolean _syncInProgress = false;

    /**
     * Parse the intermittent API response and removes the log entry based on the replyID.
     *
     * @param result result
     */
    public void removeLogEntry(Context context,JSONObject result, boolean isQuickViewResponse) {
        Gson gson = new Gson();
        IntermittentSyncMultiResponse multiResponse = gson.fromJson(result.toString(), IntermittentSyncMultiResponse.class);

        if (null != multiResponse) {
            PillpopperMultiResponse multiResp = multiResponse.getPillpopperMultiResponse();
            if (null != multiResp) {
                ResponseArray[] responseArray = multiResp.getResponseArray();
                if (null != responseArray) {
                    for (ResponseArray singleResponseObj : responseArray) {
                        IntermittentMultiPillpopperResponse singleResponse = singleResponseObj.getMultiPillpopperResponse();

                        if(null != singleResponse){
                            if(isQuickViewResponse){
                                // QuickView response object, then look for "dataSyncResult" key, if its "success", then go and delete the log entry based on replayID
                                String dataSyncResultValue = singleResponse.getDataSyncResult();
                                String errorStatusValue = singleResponse.getErrorStatus();
                                if(!Util.isEmptyString(dataSyncResultValue)){
                                    if(PillpopperConstants.SUCCESS_TEXT.equalsIgnoreCase(dataSyncResultValue)){
                                            removeLogEntryByReplyID(context, singleResponse.getReplayId());
                                    }else {
                                        if (!Util.isEmptyString(errorStatusValue) &&
                                                (errorStatusValue.contains(PillpopperConstants.KEY_DUPLICATE_ENTRY) ||
                                                 errorStatusValue.contains(PillpopperConstants.KEY_DUPLICATE_KEY) ||
                                                 errorStatusValue.contains(PillpopperConstants.KEY_UNIQUE_TRANSACTION_CONSTRAINT) ||
                                                 errorStatusValue.contains(PillpopperConstants.KEY_NO_SUCH_PILL))){
                                                removeLogEntryByReplyID(context, singleResponse.getReplayId());
                                        }else{
                                            PillpopperLog.say("QuickView mode: dataSyncResult value is not success, hence log entry will not delete ");
                                        }
                                    }
                                }else{
                                    PillpopperLog.say("QuickView mode: dataSyncResult value is null, hence log entry will not delete ");
                                }
                            }else{
                                // Intermediate Sync response object, then look for "errorText", if this is null then go and delete the entry based on replyID
                                 String errorTextValue = singleResponse.getErrorText();
                                 String devInfoValue = singleResponse.getDevInfo();
                                 if(Util.isEmptyString(errorTextValue)){
                                         removeLogEntryByReplyID(context, singleResponse.getReplayId());
                                 }else{
                                     if (!Util.isEmptyString(devInfoValue) &&
                                             (devInfoValue.contains(PillpopperConstants.KEY_DUPLICATE_ENTRY) ||
                                              devInfoValue.contains(PillpopperConstants.KEY_DUPLICATE_KEY) ||
                                              devInfoValue.contains(PillpopperConstants.KEY_UNIQUE_TRANSACTION_CONSTRAINT) ||
                                              devInfoValue.contains(PillpopperConstants.KEY_NO_SUCH_PILL))){
                                             removeLogEntryByReplyID(context, singleResponse.getReplayId());
                                     }else{
                                         PillpopperLog.say("QuickView mode: dataSyncResult value is not success, hence log entry will not delete ");
                                     }
                                     PillpopperLog.say("InApp mode:dataSyncResult is not success, hence log entry will not delete ");
                                 }
                            }
                        }
                    }
                }
            }
        }
    }

    private void removeLogEntryByReplyID(Context context, String replayId){
        if(!Util.isEmptyString(replayId)) {
            FrontController.getInstance(context).removeLogEntry(replayId);
        }
    }

    // Either start a sync now, or make sure we run another one once the current one finishes.
    public void _startOrScheduleSync(Context ctx,PillpopperAppContext context, boolean initialDownload) {
        if (getAccountId() == null) {
            PillpopperLog.say("SYNC: postponed; no account");
            return;
        }

        boolean syncNow = false;

        synchronized (_syncLock) {
            if (_syncInProgress) {
                PillpopperLog.say("SYNC: Sync requested while one in progress; will schedule one instead");
            } else {
                syncNow = true;
            }
        }
    }

    private static final int _HOLDOFF_TIME_SEC = 30;
    private boolean _holdoffTimerRunning = false;

    // Call this function when sync to the server is "eventually needed".
    // The request will go out within HOLDOFF_TIME_SEC, and will be aggregated
    // with any other requests that come in subsequently.
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void syncNeeded(final Context ctx,final PillpopperAppContext context) {
        final class HoldoffTimer extends AsyncTask<Void, Void, Void> {
            @Override
            protected Void doInBackground(Void... params) {
                try {
                    Thread.sleep(_HOLDOFF_TIME_SEC * 1000);
                } catch (InterruptedException e) {
                    PillpopperLog.say("SYNC: Sleep interrupted! -- %s", e.toString());
                }
                return null;
            }

            @Override
            protected void onPostExecute(Void p) {
                synchronized (_syncLock) {
                    PillpopperLog.say("SYNC: Holdoff timer expired; will run sync now");
                    _holdoffTimerRunning = false;
                }

                if (ActivationController.getInstance().isSessionActive(ctx)) {
                    _startOrScheduleSync(ctx,context, false);
                }
            }
        }

        boolean runNeeded = false;

        synchronized (_syncLock) {
            if (_holdoffTimerRunning) {
                PillpopperLog.say("SYNC: Sync requested - will be rolled into existing request");
            } else {
                PillpopperLog.say("SYNC: Sync requested - will sync in %s seconds", _HOLDOFF_TIME_SEC);
                runNeeded = true;
                _holdoffTimerRunning = true;
            }
        }

        // this is down here because we want to run outside the sync lock
        if (runNeeded) {
            new HoldoffTimer().executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
        }
    }

    public interface OnSyncComplete {
        void onSyncComplete(boolean success);
    }

    private static final String _JSON_LAST_MANAGED_SYNC = "lastManagedUpdate";

    public long getLastManagedSyncTimeMsec() {
        return ((long) 1000) * _preferences.getLong(_JSON_LAST_MANAGED_SYNC, -1);
    }

    public long getSyncTimeMSec() {
        return syncTimeMSec;
    }

    public void setSyncTimeMSec(long syncTimeMSec) {
        this.syncTimeMSec = syncTimeMSec;
    }

}
