package com.montunosoftware.pillpopper.android;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContext;
import com.montunosoftware.pillpopper.android.util.PillpopperApplication;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.PillpopperServer;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.android.view.DialogHelpers;
import com.montunosoftware.pillpopper.android.view.RegistrationPopup;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.PillpopperDay;
import com.montunosoftware.pillpopper.model.PillpopperRunTime;
import com.montunosoftware.pillpopper.model.PillpopperTime;
import com.montunosoftware.pillpopper.model.StateUpdatedListener;
import com.montunosoftware.pillpopper.service.getstate.StateDownloadIntentService;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author
 * Created by adhithyaravipati on 7/11/16.
 */
public class StateListenerActivity extends PillpopperActivity implements StateUpdatedListener, ReminderListenerInterfaces {

    private SharedPreferenceManager mSharedPrefManager;
    private RegistrationPopup _registrationPopup;
    private boolean isOptinShowing = false;

    @Override
    protected void onStart() {
        super.onStart();
        IntentFilter getStateFailedIntentFilter = new IntentFilter();
        getStateFailedIntentFilter.addAction("GetStateFailedFilter");
        _thisActivity.registerReceiver(getStateFailedReceiver, getStateFailedIntentFilter);

        if (!RunTimeData.getInstance().isInitialGetStateCompleted()) {
            LoggerUtils.info("API Calls -- BROADCAST_GET_STATE_FAILED registered");
            IntentFilter initialGetStateFailedIntentFilter = new IntentFilter();
            initialGetStateFailedIntentFilter.addAction(StateDownloadIntentService.BROADCAST_GET_STATE_FAILED);
            _thisActivity.registerReceiver(initialGetStateFailedReceiver, initialGetStateFailedIntentFilter);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        RunTimeConstants.getInstance().setNotificationSuppressor(false);
        RunTimeConstants.getInstance().setBackPressDrugDetailAct(false);

        if (ActivationController.getInstance().isSessionActive(_thisActivity)) {
            mSharedPrefManager = SharedPreferenceManager.getInstance(this, AppConstants.AUTH_CODE_PREF_NAME);
            if (((mSharedPrefManager.getBoolean("forceSignInRequired", false) || (!getState().isRegistered())
                    && !AppConstants.isByPassLogin()))) {

                if (mSharedPrefManager.getBoolean("forceSignInRequired", false)) {
                    mSharedPrefManager.putBoolean("forceSignInRequired", false, false);
                    PillpopperLog.say("forceSignInRequired is reset to false and isSyncAPIRequired set to false");
                }

                if (get_globalAppContext().isPartnerLibrary()) {
                    if (_registrationPopup == null) {
                        _registrationPopup = new RegistrationPopup();
                    }
                    // KP only!
                    String kpUserId = get_globalAppContext().kpGetUserId(this);

                    if (kpUserId != null) {
                        getState().getPreferences().setPreference(PillpopperServer.IDENTIFY_BY_PREF, kpUserId);
                    }
                    // end KP only!
                    _registrationPopup.register(_thisActivity, new RegistrationPopup.RegistrationCallbacks() {
                        @Override
                        public void onRegistrationSuccess() {
                            Util.setCreateUserRequestInprogress(false);
                        }

                        @Override
                        public void onRegistrationFailure() {
                            Util.performSignout( _thisActivity,_thisActivity.getGlobalAppContext());
                        }
                    });

                }
            }
            try {    // Try catch only for robolectric class cast exception only
                // Setting Keep ALive
                PillpopperApplication pillPopperApp = (PillpopperApplication) _thisActivity.getApplication();
                pillPopperApp.startKeepAliveTimer();
            }catch (Exception e){
                PillpopperLog.say("Exception catch only incase of Robolectric unit test issues", e);
            }

            IntentFilter registerPopupRemoverFilter = new IntentFilter();
            registerPopupRemoverFilter.addAction(StateDownloadIntentService.BROADCAST_REMOVE_REGISTRATION_POPUP);
            try {
                _thisActivity.registerReceiver(registrationPopupRemoveReceiver, registerPopupRemoverFilter);
            } catch (Exception e){
                PillpopperLog.say(e);
            }

            registerGetStateReceiver();
        }
    }

    private void registerGetStateReceiver() {
        refreshReminder();
        IntentFilter mGetStateReceiverIntentFilter = new IntentFilter();
        mGetStateReceiverIntentFilter.addAction(StateDownloadIntentService.BROADCAST_GET_STATE_COMPLETED);
        try {
            _thisActivity.registerReceiver(mGetStateBroadcastReceiver, mGetStateReceiverIntentFilter);
        } catch (Exception e){
            PillpopperLog.say(e);
        }
    }


    public void refreshReminder() {
        get_globalAppContext().setState(this,getState());
        updateReminderScreen();
        IntentFilter filter = new IntentFilter();
        filter.addAction(AppConstants.ACTION_REFRESH);
        try{
            _thisActivity.registerReceiver(_broadcastReceiver, filter, PillpopperAppContext.PILLPOPPER_BROADCAST_PERMISSION, null);
        } catch (Exception e){
            PillpopperLog.say(e);
        }
    }

    @Override
    public void showReminder(boolean needRemindershown) {
        refreshReminder();
    }

    BroadcastReceiver mReminderReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            updateReminderScreen();
        }
    };

    BroadcastReceiver mGetStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
           //Performing below checks to identify whether any Discontinued KPHC meds or Interval meds are present.
           // if so we display Discontinued KPHC / discontinued Interval Meds alert and show reminder on clicking oK button.
            //check for discontinued KPHC meds
            if(FrontController.getInstance(_thisActivity).getDiscontinuedMedicationsCount() == 0){
                // doesn't have discontinued KPHC meds.
                // now check for discontinued Interval meds
                final List<Drug> pillList = FrontController.getInstance(_thisActivity).getAllIntervalDrugs(context);
                if (!pillList.isEmpty() && mSharedPrefManager.getBoolean("discontinuedIntervalMedsAlertShown", false)) {
                    updateReminderScreen();
                }
            }

            boolean signedOutStateRemoval =  mSharedPrefManager.getBoolean(AppConstants.SIGNED_OUT_STATE_REMOVAL,false);
            if(signedOutStateRemoval) {
                //will be used, when user dismissed the late reminders, logs in to app and then kills the app without signout
                // in this case the flags has to be reset, which is done on tapping sign on button and in util canShowLateReminder method
                mSharedPrefManager.putBoolean(AppConstants.SIGNED_OUT_STATE_REMOVAL_LOGIN_ONCE, true, false);
            }

            FrontController.getInstance(StateListenerActivity.this).showLateRemindersWhenFromNotifications(StateListenerActivity.this);
            LoggerUtils.info("PassedRemindersStatusFromNotifications flag is reset to 0 -  StateListenerAct");

            if (!RunTimeData.getInstance().isInitialGetStateCompleted()) {
                // resetting the forceSign in flag to false. after successful getState and getHistoryEvent
                mSharedPrefManager.putBoolean(AppConstants.FORCE_SIGN_IN_SHARED_PREF_KEY, false, false);
                runOnUiThread(() -> {
                    LoggerUtils.info("API Calls -- BROADCAST_GET_STATE_FAILED unregistered");
                    unregisterBroadcastReceiver(initialGetStateFailedReceiver);
                });
            }
        }
    };

    BroadcastReceiver registrationPopupRemoveReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            PillpopperLog.say("Registration Completed");
            if(null!=_registrationPopup && _registrationPopup.isShowing()){
                _registrationPopup.dismiss();
                _registrationPopup = null;
            }
        }
    };

    BroadcastReceiver getStateFailedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(null!=_registrationPopup && _registrationPopup.isShowing()){
                _registrationPopup.dismiss();
                _registrationPopup = null;
            }
            showErrorAlert();
        }
    };

    BroadcastReceiver initialGetStateFailedReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if(null!=_registrationPopup && _registrationPopup.isShowing()){
                _registrationPopup.dismiss();
                _registrationPopup = null;
            }
            showErrorAlert();
        }
    };

    private void showErrorAlert() {
        try {
            AlertDialog.Builder mErrorAlertBuilder = new AlertDialog.Builder(_thisActivity);
            mErrorAlertBuilder.setMessage(R.string.server_failure)
                    .setPositiveButton(R.string._ok, (dialog, id) -> {
                        dialog.cancel();
                        Util.performSignout(_thisActivity, get_globalAppContext());

              });
            AlertDialog alert = mErrorAlertBuilder.create();
            alert.setCancelable(false);
            if(!alert.isShowing()){
                alert.show();
            }
        }catch (Exception exception){
            PillpopperLog.say("Exception while fetching the data", exception);
        }
    }


    @Override
    public void onPause() {
        unregisterBroadcastReceiver(_broadcastReceiver);
        unregisterBroadcastReceiver(mReminderReceiver);
        unregisterBroadcastReceiver(mGetStateBroadcastReceiver);
        unregisterBroadcastReceiver(registrationPopupRemoveReceiver);
        unregisterBroadcastReceiver(getStateFailedReceiver);

        RunTimeConstants.getInstance().setNotificationSuppressor(true);
        super.onPause();
    }

    private void unregisterBroadcastReceiver(BroadcastReceiver broadcastReceiver) {
        try {
            _thisActivity.unregisterReceiver(broadcastReceiver);
        } catch (IllegalArgumentException e) {
            PillpopperLog.say("Caught illegal argument exception when unregistering broadcast receiver", e);
        }
    }

    @Override
    public void onStateUpdated() {
        //updateReminderScreen();
    }

    private BroadcastReceiver _broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            PillpopperLog.say("Broadcast received from DoseAlarmHandler");
            updateReminderScreen();
        }
    };

    protected void updateReminderScreen() {
        get_globalAppContext().ensureNoArguments();
        mSharedPrefManager = SharedPreferenceManager.getInstance(getActivity(), AppConstants.AUTH_CODE_PREF_NAME);
        if(PillpopperRunTime.getInstance().isReminderNeedToShow()
                && PillpopperRunTime.getInstance().isFirstTimeSyncDone()){
            new ReminderCalculationTask().execute();
        }else{
            PillpopperLog.say("Waiting for New GetState or else First Time Sync has not been completed");
        }
    }

    private class ReminderCalculationTask extends AsyncTask<Void, Void, List<Drug>> {
        @Override
        protected List<Drug> doInBackground(Void... params) {

            List<Drug> overDrugList = new ArrayList<>();

            for (final Drug d : FrontController.getInstance(_thisActivity).getDrugListForOverDue(_thisActivity)) {
                d.computeDBDoseEvents(_thisActivity,d, PillpopperTime.now(), 60);
                if (d.isoverDUE() && (null == d.getSchedule().getEnd()
                        || (d.getSchedule().getEnd().equals(PillpopperDay.today()) ||
                        d.getSchedule().getEnd().after(PillpopperDay.today())))) {
                    if (((!PillpopperRunTime.getInstance().isLauchingFromPast() && isEligibleEvent(d)))) {
                        if((PillpopperTime.now().getGmtMilliseconds() - d.getOverdueDate().getGmtMilliseconds()) < 24 * 60 * 60 * 1000){
                            overDrugList.add(d);
                        }
                    }
                }
            }
            List<Drug> pendingDrugList = new ArrayList<>();
            mSharedPrefManager = SharedPreferenceManager.getInstance(getActivity(), AppConstants.AUTH_CODE_PREF_NAME);
            if (null != overDrugList && !overDrugList.isEmpty()) {
                overDueDoseDialog(overDrugList);
            }else{
                if(null != FrontController.getInstance(_thisActivity).getPendingRemindersStatus(_thisActivity) &&
                        FrontController.getInstance(_thisActivity).getPendingRemindersStatus(_thisActivity).equalsIgnoreCase("1")
                        && !PillpopperRunTime.getInstance().isFromMDO()){
                    PillpopperLog.say("Previous Past Reminders are present need to call Passed Reminders ");

                    Util util = Util.getInstance();

                    for(Drug drug : FrontController.getInstance(_thisActivity).getPassedReminderDrugs(_thisActivity)) {
                        drug.computePastReminderEvents(_thisActivity,drug, PillpopperTime.now());

                        if ((null == drug.getSchedule().getEnd()
                                || (drug.getSchedule().getEnd().equals(PillpopperDay.today()) ||
                                drug.getSchedule().getEnd().after(PillpopperDay.today())))) {
                            pendingDrugList.add(drug);
                        }
                    }
                    if(null!=pendingDrugList && !pendingDrugList.isEmpty()) {
                        util.prepareRemindersMapData(pendingDrugList, _thisActivity);
                    }
                }
            }
            return overDrugList;
        }

        @Override
        protected void onPostExecute(List<Drug> drugs) {
                if (!PillpopperConstants.isAlertActedOn()) {
                    DialogHelpers.showPostSignInAlert(_thisActivity);
                }
            // clear the hasStatusUpdateresponse and timeStamp values
            Util.clearHasStatusUpdateValues(_thisActivity);
        }
    }

    public boolean isEligibleEvent(Drug drug) {
        return null != drug.getPreferences() && (null != drug.getPreferences().getPreference("missedDosesLastChecked")
                && (drug.getOverdueDate().after(Util.convertStringtoPillpopperTime(drug.getPreferences().getPreference("missedDosesLastChecked"))))
                || String.valueOf(drug.getOverdueDate().getGmtSeconds()).equalsIgnoreCase(drug.getPreferences().getPreference("missedDosesLastChecked")))
                || (FrontController.getInstance(_thisActivity).isEntryAvailableInPastReminder(drug.getGuid(), drug.getOverdueDate()))
                && (null != drug.getOverdueDate() && null != drug.getCreated() && !drug.getOverdueDate().before(drug.getCreated()));
    }

    private Dialog _overdueDialog = null;
    LinkedHashMap<Long, List<Drug>> currentRemindersMap = new LinkedHashMap<>();
    LinkedHashMap<Long, List<Drug>> passedRemindersMap = new LinkedHashMap<>();

    private void overDueDoseDialog(List<Drug> overdueDrugs) {
        // If there's an overdue dialog up, and it's wrong, cancel it.
        // If there's no overdue dialog and there should be one, create it.
        List<Drug> pendingDrugList = new ArrayList<>();
        if (_overdueDialog != null) {
            _overdueDialog.cancel();
        }
        if (null == _overdueDialog && null != overdueDrugs && !overdueDrugs.isEmpty() && !isOptinShowing) {
            // original without else
            Util util = Util.getInstance();
            util.prepareRemindersMapData(overdueDrugs, _thisActivity);

            currentRemindersMap = PillpopperRunTime.getInstance().getmCurrentRemindersMap();
            passedRemindersMap = PillpopperRunTime.getInstance().getmPassedRemindersMap();

            if (null != currentRemindersMap && currentRemindersMap.size() > 0) {
                PillpopperLog.say("-- Current Reminders Not Null... Have to call Current Reminders " + currentRemindersMap.size());
                if (!PillpopperConstants.isRemindersBeingShown()) {
                    PillpopperConstants.setIsRemindersBeingShown(true);
                    PillpopperConstants.setIsCurrentReminderRefreshRequired(false);
                }
            } else if (null != passedRemindersMap && passedRemindersMap.size() > 0) {
                PillpopperLog.say("-- Passed Reminders Not Null... Have to call Passed Reminders " + passedRemindersMap.size());
            } else {
                if(null != FrontController.getInstance(_thisActivity).getPendingRemindersStatus(_thisActivity) &&
                        ("1").equalsIgnoreCase(FrontController.getInstance(_thisActivity).getPendingRemindersStatus(_thisActivity))
                        && !PillpopperRunTime.getInstance().isFromMDO()){
                    PillpopperLog.say("Previous Past Reminders are present need to call Passed Reminders ");
                    for(Drug drug : FrontController.getInstance(_thisActivity).getPassedReminderDrugs(_thisActivity)) {
                        drug.computePastReminderEvents(_thisActivity,drug, PillpopperTime.now());
                        if ((null == drug.getSchedule().getEnd()
                                || (drug.getSchedule().getEnd().equals(PillpopperDay.today()) ||
                                drug.getSchedule().getEnd().after(PillpopperDay.today())))) {
                            pendingDrugList.add(drug);
                        }
                    }
                    if(null!=pendingDrugList && !pendingDrugList.isEmpty()) {
                        util.prepareRemindersMapData(pendingDrugList, _thisActivity);
                        FrontController.getInstance(_thisActivity).updateAsPendingRemindersPresent(_thisActivity);
                    }
                }
            }
        }
    }
}
