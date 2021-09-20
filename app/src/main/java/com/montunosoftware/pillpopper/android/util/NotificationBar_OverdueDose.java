package com.montunosoftware.pillpopper.android.util;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;

import androidx.core.app.NotificationCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.NotificationBarInternalBroadcastHandler;
import com.montunosoftware.pillpopper.android.RunTimeConstants;
import com.montunosoftware.pillpopper.android.inAppReminders.InAppReminderAlertsActivity;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.PillpopperTime;
import com.montunosoftware.pillpopper.model.State;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;

import static android.content.Intent.FLAG_ACTIVITY_NEW_TASK;

public class NotificationBar_OverdueDose {

    private static final long[] NOTIFICATION_VIBRATION_PATTERN = new long[] {0L, 300L, 300L, 300L};
    public static final String Tapped = "com.montunosoftware.pillpopper.OVERDUE_DRUG_TAPPED";
    public static final String NOTIFICATION_MANAGED_DRUG_TAPPED  = "com.montunosoftware.pillpopper.MANAGED_DRUG_TAPPED";
    private static final String CHANNEL_ID = "KP_MYMEDS_CHANNEL";
    private static final CharSequence name = "Medication Reminder";
    private static final int importance = NotificationManager.IMPORTANCE_HIGH;
    private static NotificationChannel notificationChannel=null;

    private static long lastNotificationTime;
    private static boolean isSecondaryReminder;
    private static int SIGNIN_REQUIRED_NOTIFICATION_ID = 9999;
    private static boolean isPrimaryReminder;
    private static PillpopperTime reminderTime;

    public static void cancelNotificationBar(Context context) {
        PillpopperLog.say("cancelling notification bar");
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        addNotificationChannel(nm);
        nm.cancelAll();
    }

    public static void updateNotificationBar(Context context, State currState) {

        /*for (Drug d : FrontController.getInstance(context).getDrugListForOverDue(context)) {
            d.computeDBDoseEvents(context,d, PillpopperTime.now(),60);
            if (d.isoverDUE() && (null == d.getSchedule().getEnd() || (d.getSchedule().getEnd().equals(PillpopperDay.today()) ||
                    d.getSchedule().getEnd().after(PillpopperDay.today())))) {
                if (((!PillpopperRunTime.getInstance().isLauchingFromPast() && Util.isEligibleEvent(d, context))
                        || (null != d.getPassedReminderTimes() && d.getPassedReminderTimes().size() > 0))) {
                    if ((PillpopperTime.now().getGmtMilliseconds() - d.getOverdueDate().getGmtMilliseconds()) < 24 * 60 * 60 * 1000) {
                        drugList.add(d);
                    }
                }
            }
        }*/
        // avoid code repetition
        List<Drug> drugList = Util.getOverdueDrugList(context);

//        for(Drug drug : drugList){
        drugList = Util.getInstance().checkForRemindersEnabled(drugList);
        LoggerUtils.info("InAppReminder Alerts - at updateNotificationBar");
        if (isNeedToNotify(context, drugList)) {
            LoggerUtils.info("InAppReminder Alerts - at isNeedToNotify");
            if (RunTimeConstants.getInstance().isNotificationSuppressor()) {
                LoggerUtils.info("InAppReminder Alerts - at isNotificationSuppressor");

                Util.registerNotificationReceivers(context);

                RunTimeData.getInstance().setNotificationGenerated(true);
                FrontController.getInstance(context).updateAsNoPendingReminders(context);
//				cancelNotificationBarById(context, (int) RunTimeData.getInstance().getReminderPillpopperTime().getGmtSeconds());
                Intent notificationIntent = new Intent(context, NotificationBarInternalBroadcastHandler.class);
                notificationIntent.removeExtra(NotificationBar.NotificationBar_Type);
                notificationIntent.putExtra(NotificationBar.NotificationBar_Type, Tapped);
                notificationIntent.setAction(Tapped + System.currentTimeMillis());
                // FLAG_UPDATE_CURRENT needed so extras go with the intent
                // http://stackoverflow.com/questions/3140072/android-keeps-caching-my-intents-extras-how-to-declare-a-pending-intent-that-ke


                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context,CHANNEL_ID);
                notificationBuilder.setAutoCancel(false);
                notificationBuilder.setContentTitle(context.getString(R.string.med_reminder_notification_title));
                notificationBuilder.setContentText(getNotificationContentText(context));
                notificationBuilder.setSmallIcon(R.drawable.icon_notification);
                notificationBuilder.setColor(Util.getColorWrapper(context, R.color.colorPrimaryDark));
                    /* USE only incase if group notifcation is  needed
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        notificationBuilder.setOnlyAlertOnce(true);
                    */
                int notificationId = getNotificationId();

                // for the content Intent
                notificationIntent.removeExtra(NotificationBar.NOTIFICATION_ID);
                notificationIntent.putExtra(NotificationBar.NOTIFICATION_ID, notificationId);

                //notification action buttons
                if(AppConstants.INCLUDE_NOTIFICATION_ACTIONS_FEATURE) {
                    // to refresh the past reminders and add them to past reminder table
                    Util.getInstance().prepareRemindersMapData(drugList, context);

                    notificationBuilder.addAction(R.drawable.transparent_color_drawable,
                            NotificationBar.NOTIFICATION_ACTION_TAKE,
                            getBroadcastPendingIntent(context, NotificationBar.NOTIFICATION_ACTION_TAKE, notificationId));
                    notificationBuilder.addAction(R.drawable.transparent_color_drawable,
                            NotificationBar.NOTIFICATION_ACTION_SKIP,
                            getBroadcastPendingIntent(context, NotificationBar.NOTIFICATION_ACTION_SKIP, notificationId));
                }


                /* Notification notification = new Notification(R.drawable.ic_app_icon,
                        getNotificationMessage(context, drugList),
                        System.currentTimeMillis()
                ); */

                // SDK 30 changes
                notificationBuilder.setWhen(System.currentTimeMillis());
                notificationBuilder.setTicker(getNotificationMessage(context,drugList));

                Notification notification = notificationBuilder.build();

                PendingIntent contentIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                notificationBuilder.setContentIntent(contentIntent);

                if((System.currentTimeMillis()-lastNotificationTime)>PillpopperConstants.NOTIFICATION_SOUND_MUTE_THRESHOLD) {
                    lastNotificationTime = System.currentTimeMillis();
                    String reminderSoundPath = FrontController.getInstance(context).getReminderSoundPathFromDB();
                    if (reminderSoundPath == null || State.REMINDER_SOUND_DEFAULT.equalsIgnoreCase(reminderSoundPath)) {
                        notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                    } else if (!State.REMINDER_SOUND_NONE.equalsIgnoreCase(reminderSoundPath)) {
                        notificationBuilder.setSound(Uri.parse(reminderSoundPath));
                    }
                }

                if (currState.getReminderVibration()) {
                    notification.defaults |= Notification.DEFAULT_VIBRATE;
                    notificationBuilder.setVibrate(NOTIFICATION_VIBRATION_PATTERN);
                }

                notificationBuilder.setLights(0xff00ff00, 300, 1000);
                NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
                addNotificationChannel(nm);
                    /*USE only incase if group notifcation is  needed
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
                        NotificationBar_OverdueDose.cancelNotificationBarById(context, notificationId);
                    */

                RunTimeData.getInstance().setHistoryMedChanged(true); // to refresh the history screen onResume

                nm.notify(notificationId, notificationBuilder.build());
                // context.registerReceiver(NotificationBarInternalBroadcastHandler,null);

            } else {
                refreshReminders(context);
                if (!isSecondaryReminder || isPrimaryReminder) {
                    LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("REFRESH_CURRENT_REMINDERS"));
                }
                LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("REFRESH_CURRENT_REMINDERS_FROM_EXPANDED_CARD"));

                if (!RunTimeData.getInstance().isLoadingInProgress()) {
                    Util.dismissForegroundAlertDialogIfAny();
                    LoggerUtils.info("InAppReminder Alerts - at before loadInAppReminderAlertsActivity");
                    Util.getInstance().prepareRemindersMapData(drugList, context);
                    if (AppConstants.IS_IN_EXPANDED_HOME_CARD) {
                        new Handler().postDelayed(() -> loadInAppReminderAlertsActivity(context), 1500);
                    } else {
                        loadInAppReminderAlertsActivity(context);
                    }
                }
            }
        }
    }



    public static void loadInAppReminderAlertsActivity(Context context){
        if (!AppConstants.isByPassLogin() && ActivationController.getInstance().isSessionActive(context)) {
            if (isPrimaryReminder && isSecondaryReminder) {
                // if the secondary reminder is overlapping with primary, give priority to primary reminder.
                RunTimeData.getInstance().setReminderPillpopperTime(reminderTime);
            }
            LoggerUtils.info("InAppReminder Alerts - at loadInAppReminderAlertsActivity");
            int alertId = getNotificationId();
            Bundle bundle = new Bundle();
            bundle.putString("ReminderType", "Medication Reminder");
            bundle.putInt("AlertID", alertId);
            Intent alertIntent = new Intent(context, InAppReminderAlertsActivity.class);
            alertIntent.putExtras(bundle);
            alertIntent.addFlags(FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(alertIntent);
        }
    }

    private static void refreshReminders(Context context) {
        Intent refreshReminderBroadcastIntent = new Intent();
        refreshReminderBroadcastIntent.setAction(AppConstants.ACTION_REFRESH);
        context.sendBroadcast(refreshReminderBroadcastIntent);
    }

    private static PendingIntent getBroadcastPendingIntent(Context context, String action, int notificationId) {
        Intent notificationIntent = new Intent(context, NotificationBarInternalBroadcastHandler.class);

        notificationIntent.removeExtra(NotificationBar.NotificationBar_Type);
        notificationIntent.removeExtra(NotificationBar.NOTIFICATION_TAPPED_ACTION);
        notificationIntent.removeExtra(NotificationBar.NOTIFICATION_ID);

        notificationIntent.putExtra(NotificationBar.NotificationBar_Type, Tapped);
        notificationIntent.putExtra(NotificationBar.NOTIFICATION_TAPPED_ACTION, action);
        notificationIntent.putExtra(NotificationBar.NOTIFICATION_ID, notificationId);

        LoggerUtils.info("NActions -  - notification Id -> " + notificationId);

        notificationIntent.setAction(action + "_" + Tapped + System.currentTimeMillis());
        return PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
    }

    private static String getNotificationMessage(Context context, List<Drug> drugList) {
        int count = 0;
        if (null != drugList) {
            SimpleDateFormat simpleDate = new SimpleDateFormat("h:mm a");
            Calendar calendar = Calendar.getInstance();

            if (RunTimeData.getInstance().getReminderPillpopperTime() != null) {
                for (int i = 0; i < drugList.size(); i++) {
                    if (RunTimeData.getInstance().getReminderPillpopperTime().getGmtMilliseconds() == drugList.get(i).getOverdueDate().getGmtMilliseconds()) {
                        count++;
                    }
                }
                calendar.setTimeInMillis(RunTimeData.getInstance().getReminderPillpopperTime().getGmtMilliseconds());
            } else {
                for (int i = 0; i < drugList.size(); i++) {
                    if (RunTimeData.getInstance().getSecondaryReminderPillpopperTime().getGmtMilliseconds() == drugList.get(i).getOverdueDate().getGmtMilliseconds()) {
                        count++;
                    }
                }
                calendar.setTimeInMillis(RunTimeData.getInstance().getSecondaryReminderPillpopperTime().getGmtMilliseconds());
            }

            // if count is 2 or more, then we confirm that there are more than 1 drug overdue at the same time.
            if (count > 1) {
                return String.format(context.getString(R.string.reminders_notification_message), simpleDate.format(calendar.getTime()).toString());
            } else {
                return String.format(context.getString(R.string.reminder_notification_message), simpleDate.format(calendar.getTime()).toString());
            }
        }
        return null;
    }

    private static String getNotificationContentText(Context context) {
        SimpleDateFormat simpleDate = new SimpleDateFormat("h:mm a");
        Calendar calendar = Calendar.getInstance();
        if (null != RunTimeData.getInstance().getReminderPillpopperTime()) {
            calendar.setTimeInMillis(RunTimeData.getInstance().getReminderPillpopperTime().getGmtMilliseconds());
        } else if (null != RunTimeData.getInstance().getSecondaryReminderPillpopperTime()) {
            calendar.setTimeInMillis(RunTimeData.getInstance().getSecondaryReminderPillpopperTime().getGmtMilliseconds());
        }
        return String.format(context.getString(R.string.reminder_notification_message), simpleDate.format(calendar.getTime()));
    }

    private static boolean isNeedToNotify(Context context, List<Drug> drugList) {
        int isNeedtoNotifyDrugCount = 0;
        isSecondaryReminder = false;
        isPrimaryReminder = false;
        reminderTime = PillpopperTime.now();
        for(Drug drug : drugList) {
            if (drug.getOverdueDate() == null) {
                PillpopperLog.say("NotificationBar_OverdueDose - isNeedToNotify - Returning false. The drug list passed to this method is empty.");
                continue;
            }

            long secondaryReminderPeriodInSeconds = FrontController.getInstance(context).getSecondaryReminderPeriodSecs(
                    FrontController.getInstance(context).getPrimaryUserIdIgnoreEnabled());
            PillpopperTime reminderPillpopperTime = drug.getOverdueDate();
            PillpopperTime secondaryReminderPillpopperTime = new PillpopperTime(reminderPillpopperTime, secondaryReminderPeriodInSeconds);

            if (PillpopperTime.now().getContainingMinute().equals(reminderPillpopperTime.getContainingMinute())) {
                PillpopperLog.say("NotificationBar_OverdueDose - isNeedToNotify - Returning true. The current device time and the overdue dose time are the same. Time: "
                        + secondaryReminderPillpopperTime.getGmtSeconds());
                reminderTime = reminderPillpopperTime;
                RunTimeData.getInstance().setReminderPillpopperTime(reminderPillpopperTime);
                isPrimaryReminder = true;
            }

            if (PillpopperTime.now().getContainingMinute().equals(secondaryReminderPillpopperTime.getContainingMinute())) {
                PillpopperLog.say("NotificationBar_OverdueDose - isNeedToNotify - Returning true. The current device time and the secondary reminder time for the dose are the same. Time: "
                        + PillpopperTime.now().getContainingMinute().getGmtSeconds());
                RunTimeData.getInstance().setReminderPillpopperTime(reminderPillpopperTime);
                RunTimeData.getInstance().setSecondaryReminderPillpopperTime(secondaryReminderPillpopperTime);
                isSecondaryReminder = true;
            }

            if(PillpopperTime.now().getContainingMinute().equals(reminderPillpopperTime.getContainingMinute())
                    || PillpopperTime.now().getContainingMinute().equals(secondaryReminderPillpopperTime.getContainingMinute())){
                if(!FrontController.getInstance(context).isHistoryEventForScheduleAvailable(String.valueOf(reminderPillpopperTime.getContainingMinute().getGmtSeconds()), drug.getGuid())
                        && isEligibleReminder(drug, reminderPillpopperTime)){
                    isNeedtoNotifyDrugCount++;
                }
            }
            PillpopperLog.say("NotificationBar_OverdueDose - isNeedToNotify - Returning false. The current device time is not the same as any of the pill's overdue time or the secondary reminder time.");
        }

        return isNeedtoNotifyDrugCount > 0;
    }

    public static int getNotificationId() {
        int notificationId = 0;
        if (null != RunTimeData.getInstance().getReminderPillpopperTime()) {
            notificationId = (int) RunTimeData.getInstance().getReminderPillpopperTime().getGmtSeconds();
        }
        return notificationId;
    }

    public static String getChannelId(){
        return CHANNEL_ID;
    }

    public static Uri getNotificationSound(Context context){
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            return nm.getNotificationChannel(CHANNEL_ID).getSound();
        }else {
            return RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        }
    }

    public static void cancelNotificationBarById(Context context, int id) {
        PillpopperLog.say("cancelling notification bar");
        NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        addNotificationChannel(nm);
        nm.cancel(id);
    }

    //Channel ID needed as part of oreo plus OS chnages

    private static void addNotificationChannel(NotificationManager nm) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationChannel = new NotificationChannel(CHANNEL_ID, name, importance);
            nm.createNotificationChannel(notificationChannel);
        }
    }

    /**
     *
     * The purpose of this method, is to avoid the secondary reminder to be triggered
     * when the user has made changes to schedules after the reminder time.
     * for example, if there is a current reminder at 9PM and user logged in to the app and made schedule changes,
     * then 9PM reminder and schedule of that day will be gone, and thus the secondary reminder of 9PM has to avoided.
     * @param drug
     * @param reminderTime
     * @return true if reminder time is after missedDosesLastChecked value of the drug or in any other exception
     * @return false, if the reminder time is behind missedDosesLastChecked.
     *
     */
    public static boolean isEligibleReminder(Drug drug, PillpopperTime reminderTime){
        if (null != reminderTime && null != drug && null != drug.getPreferences()){
            String missedDosesLastChecked = drug.getPreferences().getPreference("missedDosesLastChecked");
            if (!Util.isEmptyString(missedDosesLastChecked)) {
                try {
                    return reminderTime.after(new PillpopperTime(Long.parseLong(missedDosesLastChecked)));
                } catch (Exception e) {
                    PillpopperLog.exception(e.getMessage());
                }
            }
        }
        return true;
    }

    public static void createSignInRequiredNotification(Context context) {
        if (!AppConstants.isByPassLogin()) {
            RunTimeData.getInstance().setNotificationGenerated(false);
            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(context, CHANNEL_ID);
            notificationBuilder.setAutoCancel(true);
            notificationBuilder.setContentTitle(context.getString(R.string.force_sign_in_notification_title));
            notificationBuilder.setContentText(context.getString(R.string.force_sign_in_notification_message));
            notificationBuilder.setStyle(new NotificationCompat.BigTextStyle()
                    .bigText(context.getString(R.string.force_sign_in_notification_message)));
            notificationBuilder.setSmallIcon(R.drawable.icon_notification);
            notificationBuilder.setColor(Util.getColorWrapper(context, R.color.colorPrimaryDark));

           /* Notification notification = new Notification(R.drawable.ic_app_icon,
                    context.getString(R.string.signin_myMeds),
                    System.currentTimeMillis()
            );*/

            notificationBuilder.setWhen(System.currentTimeMillis());
            Notification notification = notificationBuilder.build();

            Intent notificationIntent = new Intent(context, NotificationBarInternalBroadcastHandler.class);
            notificationIntent.putExtra(NotificationBar.NotificationBar_Type, NotificationBar.NOTIFICATION_SIGN_IN);
            PendingIntent contentIntent = PendingIntent.getBroadcast(context, 0, notificationIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            notificationBuilder.setContentIntent(contentIntent);

            if ((System.currentTimeMillis() - lastNotificationTime) > PillpopperConstants.NOTIFICATION_SOUND_MUTE_THRESHOLD) {
                lastNotificationTime = System.currentTimeMillis();
                String reminderSoundPath = FrontController.getInstance(context).getReminderSoundPathFromDB();
                if (reminderSoundPath == null || State.REMINDER_SOUND_DEFAULT.equalsIgnoreCase(reminderSoundPath)) {
                    notificationBuilder.setSound(RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION));
                } else if (!State.REMINDER_SOUND_NONE.equalsIgnoreCase(reminderSoundPath)) {
                    notificationBuilder.setSound(Uri.parse(reminderSoundPath));
                }
            }

            notification.defaults |= Notification.DEFAULT_VIBRATE;
            notificationBuilder.setVibrate(NOTIFICATION_VIBRATION_PATTERN);

            notificationBuilder.setLights(0xff00ff00, 300, 1000);
            NotificationManager nm = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
            addNotificationChannel(nm);

            nm.notify(SIGNIN_REQUIRED_NOTIFICATION_ID, notificationBuilder.build());
        }
    }
}
