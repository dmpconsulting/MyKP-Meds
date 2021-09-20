package com.montunosoftware.pillpopper.android.refillreminder;

/**
 * Created by M1023050 on 2/9/2018.
 */

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import androidx.core.content.ContextCompat;

import com.montunosoftware.mymeds.BuildConfig;
import com.montunosoftware.pillpopper.android.refillreminder.notification.RefillReminderAlarmHandler;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;

import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Locale;
import java.util.TimeZone;
import java.util.UUID;

/**
 * Holds all the reusable functions.
 */
public class RefillReminderUtils {


    public static boolean isEmptyString(String string) {

        if(string == null) {
            return true;
        }
        return string.trim().equalsIgnoreCase("")
                || string.trim().equalsIgnoreCase("null")
                || string.length() == 0;

    }

    public static void hideKeyboard(Context context, View view) {
        try {
            InputMethodManager imm = (InputMethodManager) context
                    .getSystemService(Context.INPUT_METHOD_SERVICE);
            imm.hideSoftInputFromWindow(view.getWindowToken(), 0);
        } catch (NullPointerException e) {
            PillpopperLog.say("NPE in Util hideKeyboard", e);
        }
    }

    public static String getRandomGuid() {
        return String.valueOf(UUID.randomUUID()).toUpperCase();
    }

    public static long getTzOffsetSecs(TimeZone paramTimeZone)
    {
        return paramTimeZone.getOffset(GregorianCalendar.getInstance().getTimeInMillis()) / 1000;
    }

    /**
     * Method will return the application version.
     * @return app version
     */
    public static String getAppVersion(Context context) {
        try {
            if(null!=context){
                PackageInfo pInfo = context.getPackageManager().getPackageInfo(
                        context.getPackageName(), 0);
                return pInfo.versionName;
            }
        } catch (PackageManager.NameNotFoundException e) {
            LoggerUtils.exception("NameNotFoundException", e);
        }
        return BuildConfig.VERSION_NAME;
    }

    public static String convertDateLongToIso(String longDate) {
        //	yyyy-MM-dd'T'HH:mm:ss
        if (longDate==null || longDate.equalsIgnoreCase("null") || longDate.trim().isEmpty()) return null;
        if (longDate.equalsIgnoreCase("-1")) return "-1";
        if (longDate.contains("T"))return longDate;
        Date date=new Date(Long.parseLong(longDate) * 1000); // seconds to milliseconds
        SimpleDateFormat sdf = new SimpleDateFormat(RefillReminderConstants.ISO_DATETIME_FORMAT);
        String isoDate = sdf.format(date);
        return isoDate;
    }

    public static String convertDateIsoToLong(String dateStr) {
        if (dateStr==null || dateStr.equalsIgnoreCase("null") || dateStr.trim().isEmpty()) return null;
        if (dateStr.equalsIgnoreCase("-1")) return "-1";
        if (!dateStr.contains("T"))return dateStr;
        SimpleDateFormat sdf = new SimpleDateFormat(RefillReminderConstants.ISO_DATETIME_FORMAT);
        Date longDate = null;
        try {
            longDate = sdf.parse(dateStr);
        } catch (ParseException e) {
            PillpopperLog.say("Opps! DateIsotoLongFormatException " , e);
            return dateStr;
        }
        return String.valueOf(longDate.getTime()/1000); //seconds
    }

    public static Drawable getDrawableWrapper(Context context, int drawable){
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            return ContextCompat.getDrawable(context, drawable);
        } else {
            //to support lower versions below M
            //noinspection deprecation
            return context.getResources().getDrawable(drawable);
        }
    }

    public static String getDate(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMMM dd");
        return simpleDateFormat.format(date);
    }

    public static String getTime(Date date) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("h:mm a");
        return simpleDateFormat.format(date);
    }

    public static Typeface setFontStyle(Context mContext, String textStyle) {
        return Typeface.createFromAsset(mContext.getAssets(), "fonts/" + textStyle);
    }

    public static String getReplyId(){
        return String.valueOf(UUID.randomUUID()).toUpperCase();
    }


    public static String getLanguage(){
        return Locale.getDefault().toString();
    }

    public static void updateRefillAlarm(Context context, String nextRefillReminder) {
        AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
        if(!RefillReminderUtils.isEmptyString(nextRefillReminder) && !nextRefillReminder.equals("-1")) {
            Calendar nextRefillTime = Calendar.getInstance();
            nextRefillTime.setTimeInMillis(Long.parseLong(nextRefillReminder) * 1000);
            Intent alarmIntent = new Intent(context, RefillReminderAlarmHandler.class);
            Bundle bundle = new Bundle();
            bundle.putSerializable(RefillReminderConstants.REFILL_REMINDER_NOTIFICATION_ID, nextRefillReminder);
            alarmIntent.putExtra(RefillReminderConstants.REFILL_REMINDER_NOTIFICATION_BUNDLE, bundle);
            PendingIntent alarmPendingIntent = PendingIntent.getBroadcast(context, Long.valueOf(nextRefillReminder).intValue(), alarmIntent, 0);
            RefillReminderLog.say("Refill -- updateRefillAlarm -- " + Util.convertDateLongToIso(nextRefillReminder));
            alarmManager.set(AlarmManager.RTC_WAKEUP, nextRefillTime.getTimeInMillis(), alarmPendingIntent);
        }
    }

    /**
     *
     * @param nextRefillDateStr next refill date stored in db
     * @return if the next refill date is future return true else false.
     */
    public static boolean isValidFutureRefill(String nextRefillDateStr) {
        try {
            if(!nextRefillDateStr.equalsIgnoreCase("-1")) {
                Date nextRefillDate = new Date();
                nextRefillDate.setTime(Long.parseLong(nextRefillDateStr) * 1000);
                return nextRefillDate.after(new Date());
            }
        } catch (Exception ex) {
            RefillReminderLog.say("isValidFutureRefill", ex);
            return false;
        }
        return false;
    }


    public static long getTzOffsetSecs()
    {
        return TimeZone.getDefault().getOffset(GregorianCalendar.getInstance().getTimeInMillis()) / 1000;
    }
}
