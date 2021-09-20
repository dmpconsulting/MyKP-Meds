package com.montunosoftware.pillpopper.android.refillreminder.notification;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.media.AudioAttributes;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.NotificationBarInternalBroadcastHandler;
import com.montunosoftware.pillpopper.android.NotificationBarOrderedBroadcastHandler;
import com.montunosoftware.pillpopper.android.RunTimeConstants;
import com.montunosoftware.pillpopper.android.refillreminder.RefillReminderConstants;
import com.montunosoftware.pillpopper.android.refillreminder.RefillReminderLog;
import com.montunosoftware.pillpopper.android.refillreminder.RefillReminderUtils;
import com.montunosoftware.pillpopper.android.refillreminder.controllers.RefillReminderController;
import com.montunosoftware.pillpopper.android.refillreminder.models.RefillReminder;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;

import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;

import java.util.Calendar;
import java.util.List;

import static com.montunosoftware.pillpopper.android.refillreminder.RefillReminderConstants.REFILL_REMINDER_CHANNEL_ID;
import static com.montunosoftware.pillpopper.android.refillreminder.RefillReminderConstants.REFILL_REMINDER_CHANNEL_NAME;

public class RefillReminderNotificationUtil {

    private static RefillReminderNotificationUtil notificationUtil;
    private NotificationManager notificationManager;
    private RefillReminderController refillReminderController;
    private AlarmManager alarmManager;
    private Uri notificationUri;
    private AudioAttributes audioAttributes;

    public static RefillReminderNotificationUtil getInstance(Context context) {
        if (notificationUtil == null) {
            notificationUtil = new RefillReminderNotificationUtil(context.getApplicationContext());
        }
        return notificationUtil;
    }

    private RefillReminderNotificationUtil(Context context) {
        createNotificationChannel(context);
        refillReminderController = RefillReminderController.getInstance(context);
        alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
    }


    public void createNotificationChannel(Context context) {
        notificationUri = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
        notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O && notificationManager.getNotificationChannel(REFILL_REMINDER_CHANNEL_ID) == null) {

            audioAttributes = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_NOTIFICATION)
                    .setContentType(AudioAttributes.CONTENT_TYPE_SPEECH)
                    .build();


            NotificationChannel channel = new NotificationChannel(REFILL_REMINDER_CHANNEL_ID, REFILL_REMINDER_CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH);
            channel.setDescription(RefillReminderConstants.REFILL_REMINDER_CHANNEL_DESCRIPTION);
            channel.setSound(notificationUri, audioAttributes);
            channel.enableLights(true);
            channel.setLightColor(Color.RED);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{500});
            notificationManager.createNotificationChannel(channel);
        }
    }

    public void generateNotification(Context context, long notificationID) {
        if (RunTimeConstants.getInstance().isNotificationSuppressor()
            /*RefillReminderUtils.isValidFutureRefill(String.valueOf(notificationID))*/) {
            registerReceivers(context);
            Intent sentIntent = new Intent(context, NotificationBarInternalBroadcastHandler.class);

            PendingIntent contentIntent = PendingIntent.getBroadcast(context, 0, sentIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            RunTimeData.getInstance().setNotificationGenerated(true);
            Notification notification = new NotificationCompat.Builder(context, REFILL_REMINDER_CHANNEL_ID)
                    .setSmallIcon(R.drawable.icon_notification)
                    .setSound(notificationUri)
                    .setContentIntent(contentIntent)
                    .setColor(ContextCompat.getColor(context, R.color.colorPrimaryDark))
                    .setContentTitle(context.getString(R.string.refill_notification_title))
                    .setContentText(context.getString(R.string.refill_notification_message)).build();

            PillpopperLog.say("Refill NotificationID : " + notificationID);
            notificationManager.notify((int) notificationID, notification);

        } else {
            PillpopperLog.say("Refill Reminder alarm not set, as it is a passed time");
        }
    }

    private static void registerReceivers(Context context) {
        try{
            IntentFilter filter = new IntentFilter("com.montunosoftware.dosecast.NotificationBarOrderedBroadcastKP");
            RunTimeData.getInstance().setNotificationBarOrderedBroadcastHandler(new NotificationBarOrderedBroadcastHandler());
            context.getApplicationContext().registerReceiver(RunTimeData.getInstance().getNotificationBarOrderedBroadcastHandler(), filter);
        }catch (Exception e){
            PillpopperLog.say(e);
        }
    }

    public void setNotificationUri(Uri notificationUri) {
        this.notificationUri = notificationUri;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.getNotificationChannel(RefillReminderConstants.REFILL_REMINDER_CHANNEL_ID).setSound(notificationUri, audioAttributes);
        }
    }

    public void createNextRefillReminderAlarms(Context context) {
        Calendar refillReminderCalendar = Calendar.getInstance();
        List<RefillReminder> refillReminders = refillReminderController.getNextRefillReminders();
        for (RefillReminder refillReminder : refillReminders) {
            if (RefillReminderUtils.isValidFutureRefill(String.valueOf(refillReminder.getNextReminderDate()))) {
                long refillReminderTime = Long.parseLong(refillReminder.getNextReminderDate()) * 1000L;
                refillReminderCalendar.setTimeInMillis(refillReminderTime);
                Intent alarmIntent = new Intent(context, RefillReminderAlarmHandler.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(RefillReminderConstants.REFILL_REMINDER_NOTIFICATION_ID, refillReminder.getNextReminderDate());
                alarmIntent.putExtra(RefillReminderConstants.REFILL_REMINDER_NOTIFICATION_BUNDLE, bundle);
                PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, Long.valueOf(refillReminderCalendar.getTimeInMillis() / 1000L).intValue(), alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.setExact(AlarmManager.RTC_WAKEUP, refillReminderCalendar.getTimeInMillis(), alarmPendingIntent);
                RefillReminderLog.say("Refill reminder alarm set for " + refillReminderCalendar.getTime() + " PendingIntent " + refillReminderCalendar.getTimeInMillis() / 1000L);
            }
        }
    }

    public void createNextRefillReminderAlarms(Context context, String nextRefillReminderTime) {
        if (RefillReminderUtils.isValidFutureRefill(String.valueOf(nextRefillReminderTime))) {
            Calendar refillReminderCalendar = Calendar.getInstance();
            long refillReminderTime = Long.parseLong(nextRefillReminderTime) * 1000L;
            refillReminderCalendar.setTimeInMillis(refillReminderTime);
            Intent alarmIntent = new Intent(context, RefillReminderAlarmHandler.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(RefillReminderConstants.REFILL_REMINDER_NOTIFICATION_ID, nextRefillReminderTime);
            alarmIntent.putExtra(RefillReminderConstants.REFILL_REMINDER_NOTIFICATION_BUNDLE, bundle);
            PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, Long.valueOf(nextRefillReminderTime).intValue(), alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
            alarmManager.setExact(AlarmManager.RTC_WAKEUP, refillReminderCalendar.getTimeInMillis(), alarmPendingIntent);
            RefillReminderLog.say("Refill reminder alarm set for " + refillReminderCalendar.getTime() + " PendingIntent " + refillReminderCalendar.getTimeInMillis() / 1000L);
        }
    }

    public void clearNextRefillReminderAlarms(Context context) {
        Calendar refillReminderCalendar = Calendar.getInstance();
        List<RefillReminder> refillReminders = refillReminderController.getNextRefillReminders();
        for (RefillReminder refillReminder : refillReminders) {
            long refillReminderTime = Long.parseLong(refillReminder.getNextReminderDate()) * 1000L;
            refillReminderCalendar.setTimeInMillis(refillReminderTime);
            Intent alarmIntent = new Intent(context, RefillReminderAlarmHandler.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(RefillReminderConstants.REFILL_REMINDER_NOTIFICATION_ID, refillReminder.getNextReminderDate());
            alarmIntent.putExtra(RefillReminderConstants.REFILL_REMINDER_NOTIFICATION_BUNDLE, bundle);
            PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, Integer.parseInt(refillReminder.getNextReminderDate()), alarmIntent, 0);
            alarmManager.cancel(alarmPendingIntent);
        }
    }

    public void updateOverDueDate(Context context, String nextReminderDate) {
        Calendar refillEndDate = Calendar.getInstance();
        Calendar refillNextDate = Calendar.getInstance();
        Calendar refillOverDueDate = Calendar.getInstance();
        List<RefillReminder> refillReminders = refillReminderController.getRefillRemindersByNextReminderTime(nextReminderDate);
        RefillReminderLog.say("Refill -- updateOverDueDate-- size " + refillReminders.size());
        for (RefillReminder refillReminder : refillReminders) {
            refillReminder.setOverdueReminderDate(refillReminder.getNextReminderDate());
            if (refillReminder.isRecurring()) {
                refillNextDate.setTimeInMillis(Long.parseLong(refillReminder.getNextReminderDate()) * 1000L);
                refillOverDueDate.setTimeInMillis(Long.parseLong(refillReminder.getOverdueReminderDate()) * 1000L);
                refillNextDate.add(Calendar.DATE, refillReminder.getFrequency());
                if(!RefillReminderUtils.isEmptyString(refillReminder.getReminderEndDate())){
                    refillEndDate.setTimeInMillis(Long.parseLong(refillReminder.getReminderEndDate()) * 1000L);
                    if (refillNextDate.after(refillEndDate)) {
                        refillReminder.setNextReminderDate("-1");
                    } else {
                        refillReminder.setNextReminderDate(String.valueOf(refillNextDate.getTimeInMillis() / 1000L));
                    }
                }else {
                    refillReminder.setNextReminderDate(String.valueOf(refillNextDate.getTimeInMillis() / 1000L));
                }
            } else {
                refillReminder.setNextReminderDate("-1");
            }
            refillReminderController.updateRefillReminder(refillReminder);
            RefillReminderUtils.updateRefillAlarm(context, refillReminder.getNextReminderDate());
        }
    }


    /**
     * Cancels All the Refill Reminders
     * @param context
     */
    public void cancelAllPendingRefillReminders(Context context) {
        List<RefillReminder> refillReminders = refillReminderController.getRefillReminders();
        for (RefillReminder refillReminder : refillReminders) {
            cancelRefillReminder(refillReminder.getNextReminderDate(), context);
        }
    }

    /**
     * Cancel the deleted Refill Reminder alarm
     * @param nextReminderAlarm
     * @param context
     */
    public void cancelRefillReminder(String nextReminderAlarm, Context context){
        RefillReminderLog.say("Refill Reminder Deleting for : " + nextReminderAlarm);
        try{
            if (!RefillReminderUtils.isEmptyString(nextReminderAlarm)) {
                Intent alarmIntent = new Intent(context, RefillReminderAlarmHandler.class);
                Bundle bundle = new Bundle();
                bundle.putSerializable(RefillReminderConstants.REFILL_REMINDER_NOTIFICATION_ID, nextReminderAlarm);
                alarmIntent.putExtra(RefillReminderConstants.REFILL_REMINDER_NOTIFICATION_BUNDLE, bundle);
                PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, Long.valueOf(nextReminderAlarm).intValue(), alarmIntent, PendingIntent.FLAG_UPDATE_CURRENT);
                alarmManager.cancel(alarmPendingIntent);
                alarmPendingIntent.cancel();
                RefillReminderLog.say("Refill reminder alarm canceled -- " + Util.convertDateLongToIso(nextReminderAlarm));
            }
        } catch (Exception e){
            RefillReminderLog.say(e);
        }
    }



}
