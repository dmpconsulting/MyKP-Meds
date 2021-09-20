package com.montunosoftware.pillpopper.android.util;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.AlarmManager;
import android.app.Dialog;
import android.app.PendingIntent;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Paint;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.PowerManager;
import android.os.SystemClock;
import android.provider.MediaStore;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.autofill.AutofillManager;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.TimePicker;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.common.net.HttpHeaders;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.montunosoftware.mymeds.BuildConfig;
import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.DeleteHtmlFileBroadcase;
import com.montunosoftware.pillpopper.android.NotificationBarOrderedBroadcastHandler;
import com.montunosoftware.pillpopper.android.PillpopperActivity;
import com.montunosoftware.pillpopper.android.firebaseMessaging.FCMHandler;
import com.montunosoftware.pillpopper.android.util.FileHandling.OutputStreamAndFilename;
import com.montunosoftware.pillpopper.android.util.FileHandling.StorageLocation;
import com.montunosoftware.pillpopper.android.view.DialogHelpers;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.database.model.HistoryEditEvent;
import com.montunosoftware.pillpopper.database.model.HistoryEvent;
import com.montunosoftware.pillpopper.database.model.LogEntryModel;
import com.montunosoftware.pillpopper.database.persistence.DatabaseUtils;
import com.montunosoftware.pillpopper.model.Contact;
import com.montunosoftware.pillpopper.model.DiscontinuedDrug;
import com.montunosoftware.pillpopper.model.DoseEvent;
import com.montunosoftware.pillpopper.model.DoseEventCollection;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.HourMinute;
import com.montunosoftware.pillpopper.model.KPAlarmManager;
import com.montunosoftware.pillpopper.model.Person;
import com.montunosoftware.pillpopper.model.PillpopperDay;
import com.montunosoftware.pillpopper.model.PillpopperRunTime;
import com.montunosoftware.pillpopper.model.PillpopperTime;
import com.montunosoftware.pillpopper.model.Region;
import com.montunosoftware.pillpopper.model.RegionResponse;
import com.montunosoftware.pillpopper.model.Schedule;
import com.montunosoftware.pillpopper.model.TTGCookie;
import com.montunosoftware.pillpopper.model.TTGSecureWebViewModel;
import com.montunosoftware.pillpopper.model.UserPreferences;
import com.montunosoftware.pillpopper.model.genericCardAndBanner.AnnouncementsItem;
import com.montunosoftware.pillpopper.model.genericCardAndBanner.AnnouncementsResponse;
import com.montunosoftware.pillpopper.model.genericCardAndBanner.ButtonsItem;
import com.montunosoftware.pillpopper.network.model.GetTokenResponseObj;
import com.montunosoftware.pillpopper.service.getstate.GetStateService;
import com.montunosoftware.pillpopper.service.getstate.StateDownloadIntentService;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.controller.ActivationController;
import org.kp.tpmg.mykpmeds.activation.envswitch.EnvSwitchUtils;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.model.User;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;
import org.kp.tpmg.mykpmeds.activation.util.GenericAlertDialog;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.mykpmeds.activation.util.NetworkManager;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager;
import org.kp.tpmg.ttg.RefillRuntimeData;
import org.kp.tpmg.ttg.RxRefillConstants;
import org.kp.tpmg.ttg.controller.RxRefillController;
import org.kp.tpmg.ttg.utils.RxRefillLoggerUtils;
import org.kp.tpmg.ttgmobilelib.TTGMobileLibConstants;
import org.kp.tpmg.ttgmobilelib.TTGRuntimeData;
import org.kp.tpmg.ttgmobilelib.controller.TTGSignonController;
import org.kp.tpmg.ttgmobilelib.service.TTGHttpUrlConnection;
import org.kp.tpmg.ttgmobilelib.utilities.TTGLoggerUtil;
import org.kp.tpmg.ttgmobilelib.utilities.TTGUtil;

import java.io.BufferedReader;
import java.io.Closeable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Field;
import java.net.HttpCookie;
import java.net.HttpURLConnection;
import java.security.SecureRandom;
import java.text.MessageFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.TreeSet;
import java.util.UUID;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static android.content.Context.POWER_SERVICE;
import static android.view.View.IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS;


public class Util {

    private static ArrayList<String> requestDSTParams;
    private static ArrayList<String> requestTZParams;
    private static final String PILLPOPPER_REQUEST = "pillpopperRequest";
    private static final String PILLPOPPER_MULTI_REQUEST = "pillpopperMultiRequest";
    private static final String REQUEST_ARRAY = "requestArray";
    private static final String PREFERENCES = "preferences";
    private static final String MISSED_DOSES_LAST_CHECKED = "missedDosesLastChecked";
    private static final String isScheduleAddedOrUpdated = "isScheduleAddedOrUpdated";
    private static final String START = "start";
    private static final String END = "end";
    private static final String LAST_TAKEN = "last_taken";
    private static final String EFF_LAST_TAKEN = "eff_last_taken";
    private static final String NOTIFY_AFTER = "notify_after";
    private static final String SCHEDULE_DATE = "scheduleDate";


    private static final String tz_secs = "_tz_secs";
    private static final String PILL_ID = "pillId";
    public static final double Epsilon = 0.0000001;

    private static final String ISO_DATETIME_FORMAT = "yyyy-MM-dd'T'HH:mm:ss";

    private static final Util mUtil = new Util();
    private static SharedPreferenceManager mSharedPrefManager;

    public static Util getInstance() {
        return mUtil;
    }

    //////////////////////////////////////////////////////////////////////////////////////////////


    // Robust long-getter that gets a long either specified as a native JSON
    // integer, or as a string that contains an integer. Returns -1 if the
    // key was not found or is in a numeric format, or if the number was
    // negative.
    public static long parseJSONNonnegativeLong(JSONObject o, String key) {
        long longVal = -1;

        if (o == null)
            return -1;

        try {
            longVal = o.getLong(key);
        } catch (JSONException e) {
            try {
                String stringVal = o.getString(key);
                longVal = Long.parseLong(stringVal);
            } catch (JSONException e2) {
                // Could not get object as either long or string; fail
                PillpopperLog.say("JSONException", e2);
                return -1;
            } catch (NumberFormatException e3) {
                // string exists but couldn't be converted to a long
                PillpopperLog.say("NumberFormatException", e3);
                return -1;
            }
        }

        if (longVal < 0)
            return -1;
        else
            return longVal;
    }

    public static long parseJSONNonnegativeLong(String key) {
        long longVal = -1;

        try {
            longVal = Long.parseLong(key);
        } catch (Exception e3) {
            // string exists but couldn't be converted to a long
            PillpopperLog.say("Exception", e3);
            return -1;
        }

        if (longVal < 0)
            return -1;
        else
            return longVal;
    }

    public static long parseNonnegativeLong(String numberString) {
        if (numberString == null) {
            return -1;
        }

        try {
            return Long.parseLong(numberString);
        } catch (NumberFormatException e) {
            PillpopperLog.say("NumberFormatException", e);
            return -1;
        }
    }

    public static double parseNonnegativeDouble(String numberString) {
        if (numberString == null) {
            return -1;
        }

        try {
            return Double.parseDouble(numberString);
        } catch (NumberFormatException e) {
            PillpopperLog.say("NumberFormatException", e);
            return -1;
        }
    }

    public static String parseJSONStringOrNull(JSONObject o, String key) {
        if (o == null || !o.has(key) || o.isNull(key)) {
            return null;
        } else {
            try {

                return o.getString(key);
            } catch (JSONException e) {
                PillpopperLog.say("JSONException", e);
                return null;
            }
        }
    }

    public static void putJSONString(JSONObject jsonState, String key, String value) throws JSONException {
        if (value != null)
            jsonState.put(key, value);
    }

    public static void putJSONStringFromLong(JSONObject o, String key, long value) throws JSONException {
        o.put(key, String.format(Locale.US, "%d", value));
    }

    public static String cleanString(String s) {
        if (s == null)
            return null;

        s = s.trim();

        if (s.length() == 0)
            return null;

        return s;
    }

    public static String getTextFromDouble(double value) {
        if (value < Util.Epsilon)
            return null;

        int fracDigits;
        double testValue = value;
        for (fracDigits = 0; fracDigits < 5; fracDigits++) {
            if (Math.abs(testValue - Math.round(testValue)) < Util.Epsilon) {
                break;
            }
            testValue *= 10;
        }

        String formatString = String.format(Locale.US, "%%.%df", fracDigits); // format string itself uses US specs
        return String.format(Locale.getDefault(), formatString, value); // user-visible string formatted with user's locale
    }

    public static String getTextFromLong(long value) {
        if (value == 0)
            return null;
        else
            return String.format(Locale.getDefault(), "%d", value);
    }


    // If s1 is null, return null.
    // Otherwise, append s2 (if it is not null).
    public static String maybeAppendString(String s1, String s2) {
        s1 = Util.cleanString(s1);
        s2 = Util.cleanString(s2);

        if (s1 == null)
            return null;
        else if (s2 == null)
            return s1;
        else return s1 + " " + s2;
    }


    public static void sendEmail(
            final PillpopperActivity act,
            final String recipient,
            final String subject,
            final CharSequence body,
            final String attachment,
            final String attachment_filename) {

        AlertDialog dialog;
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(act);

        LayoutInflater inflater = act.getLayoutInflater();
        View view = inflater.inflate(R.layout.delete_med, null);
        alertDialog.setView(view);
        alertDialog.setCancelable(false);
        TextView title = view.findViewById(R.id.delete_drug_title);
        title.setText(act.getAndroidContext().getResources().getString(R.string.email_title));
        TextView message = view.findViewById(R.id.delete_drug_description);
        message.setText(act.getAndroidContext().getResources().getString(R.string.email_warning));

        TextView hideHint = view.findViewById(R.id.delete_drug_hint);
        hideHint.setVisibility(View.GONE);
        alertDialog.setPositiveButton(
                Html.fromHtml("<b>" + act.getAndroidContext().getResources().getString(R.string.continue_text) + "</b>"),
                (dialog12, which) -> {
                    _sendEmail(act, recipient, subject, body, attachment, attachment_filename);
                    dialog12.dismiss();
                });

        alertDialog.setNegativeButton(act.getAndroidContext().getResources().getString(R.string.close_btn_txt),
                (dialog1, which) -> dialog1.dismiss());

        dialog = alertDialog.create();
        if (null != dialog && !dialog.isShowing()) {
            RunTimeData.getInstance().setAlertDialogInstance(dialog);
            dialog.show();
        }

    }


    public static final String DOSECAST_MEDICATION_LIST_HTML = "dosecast_medication_list";
    public static final String DOSECAST_HISTORY_HTML = "dosecast_history";
    public static final String STATUS_CODE_125_STRING = "125";
    public static final int STATUS_CODE_125 = 125;
    public static final String STATUS_CODE_0_STRING = "0";

    public static void cleanAttachments(Context context) throws IOException {
        for (StorageLocation storageLocation : Arrays.asList(StorageLocation.External_Temporary, StorageLocation.Internal)) {
            File getDir = null;

            if (storageLocation == StorageLocation.Internal) {
                getDir = context.getFilesDir();
            } else {
                getDir = FileHandling.getExternalStorageDirectory(context, storageLocation);
            }
            try {
                File yourDir = new File(getDir.toString());
                for (File fi : yourDir.listFiles()) {
                    long converttimeStampString = 0;
                    if (fi.isFile()) {
                        String name = fi.getName();
                        String timestampString[] = name.split("_");
                        try {
                            converttimeStampString = Long.parseLong(timestampString[timestampString.length - 1]); //Long.parseLong(timestampString[1]);
                            if (converttimeStampString <= System.currentTimeMillis()) {
                                FileHandling.deleteFile(context, name, storageLocation);
                            } else {

                                Intent intent = new Intent();
                                PendingIntent pi = PendingIntent.getBroadcast(context, (int) System.currentTimeMillis(), intent, 0);
                                AlarmManager alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
                                alarmManager.set(AlarmManager.RTC, converttimeStampString, pi);
                                LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
                            }
                        } catch (NumberFormatException e) {
                            PillpopperLog.say("Opps! NumberFormatException ", e);
                        }
                    }
                }

            } catch (NullPointerException e) {
                PillpopperLog.say("Opps! NullPointerException ", e);
            } catch (Exception e) {
                PillpopperLog.say("Opps! Exception ", e);
            }
        }
    }

    @SuppressLint("WorldReadableFiles")
    private static void _sendEmail(
            PillpopperActivity act,
            String recipient,
            String subject,
            CharSequence body,
            String attachment,
            String attachment_filename) {
        PillpopperLog.say("sending email re: %s", subject);

        // http://stackoverflow.com/questions/2197741/how-to-send-email-from-my-android-application
        Intent emailIntent = new Intent(Intent.ACTION_SEND);

        emailIntent.setType("text/plain");
        if (recipient != null) {
            emailIntent.putExtra(Intent.EXTRA_EMAIL, new String[]{recipient});
        }

        if (subject != null) {
            emailIntent.putExtra(Intent.EXTRA_SUBJECT, subject);
        }

        if (body != null) {
            emailIntent.putExtra(Intent.EXTRA_TEXT, body);
        }

        if (attachment != null) {
            OutputStreamAndFilename osf = null;

            try {
                // Files written to the external storage directory are guaranteed readable by
                // other applications, but external storage is not guaranteed to exist.
                // Files written to internal storage can be marked world-readable, but
                // apparently on some platforms, the app's internal storage directory
                // is not readable.  There's no way to mark a directory or file as
                // world-readable until API version 9.  Ugh.
                //
                // We'll try to write to external storage first, and if it doesn't work,
                // fall back on world-readable internal storage and hope for the best.
                long triggerTimeStamp = System.currentTimeMillis() + (24 * 60 * 60 * 1000);
                String timeStampAttachedFilename = attachment_filename + "_" + triggerTimeStamp + ".html";
                try {
                    osf = FileHandling.openOutputStream(act, act.getGlobalAppContext(), timeStampAttachedFilename, StorageLocation.External_Temporary, attachment, Context.MODE_WORLD_READABLE);
                    TTGLoggerUtil.info("TAG eXTERNAL");
                } catch (Exception e) {
                    osf = FileHandling.openOutputStream(act, act.getGlobalAppContext(), timeStampAttachedFilename, StorageLocation.Internal, attachment, Context.MODE_WORLD_READABLE);
                    PillpopperLog.say("Exception ", e);
                }
                // From Android N We can not pass the Uri path in intent. This will result an exception "FileUriExposedException ".
                // Instead we have to create a content provider as suggested and exchange the Uri path.
                Uri fileName = FileProvider.getUriForFile(act,
                        BuildConfig.APPLICATION_ID + ".provider",
                        osf.getOutputFilename());
                emailIntent.putExtra(Intent.EXTRA_STREAM, fileName);
                PillpopperLog.say("writing email attachment to " + fileName.toString());
            } catch (Exception e1) {
                PillpopperLog.say("Trying to write to temp file: ", e1);
            } finally {
                if (osf != null) {
                    Util.closeSilently(osf.getOutputStream());
                }
            }
        }

        try {

            act.startActivityForResult(Intent.createChooser(emailIntent, act.getString(R.string.send_mail)), 100);
            long after24Hours = 24 * 60 * 60 * 1000;
            Intent intent = new Intent();
            intent.setAction("com.montunosoftware.dosecast.filehandling.ACTION_FILE_DELETE");
            PendingIntent pi = PendingIntent.getBroadcast(act, (int) System.currentTimeMillis(), intent, 0);
            AlarmManager alarmManager = (AlarmManager) act.getSystemService(Context.ALARM_SERVICE);
            alarmManager.set(AlarmManager.RTC, System.currentTimeMillis() + after24Hours, pi);
            DeleteHtmlFileBroadcase deleteHtmlFileBroadcase = new DeleteHtmlFileBroadcase();
            LocalBroadcastManager.getInstance(act).registerReceiver(deleteHtmlFileBroadcase, new IntentFilter("com.montunosoftware.dosecast.filehandling.ACTION_FILE_DELETE"));
        } catch (Exception e) {
            PillpopperLog.say("Oops!, Exception", e);
        }
    }

    //////// GUID Stuff ///////////////////////////////

    public static String getRandomGuid() {
        return String.valueOf(UUID.randomUUID()).toUpperCase();
    }

    @SuppressLint("TrulyRandom")
    public static String getRandomGuid(int length) {
        SecureRandom ranGen = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < length; i++) {
            sb.append(Long.toHexString(Math.round(ranGen.nextDouble() * 16)));
        }

        return sb.toString();
    }

    public static String friendlyGuid(String s) {
        if (s == null)
            return "";

        if (s.length() < 8)
            return "shortguid-" + s;

        return String.format("%s-%s", s.substring(0, 4), s.substring(4, 8));
    }


    public static void activateSoftKeyboard(final EditText editText) {
        editText.postDelayed(() -> {
            // Ridiculous method to get the soft keyboard to pop up automatically -- from
            // http://stackoverflow.com/questions/5105354/how-to-show-soft-keyboard-when-edittext-is-focused
            editText.requestFocus();
            MotionEvent downEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_DOWN, 0, 0, 0);
            MotionEvent upEvent = MotionEvent.obtain(SystemClock.uptimeMillis(), SystemClock.uptimeMillis(), MotionEvent.ACTION_UP, 0, 0, 0);
            editText.dispatchTouchEvent(downEvent);
            editText.dispatchTouchEvent(upEvent);
            editText.setSelection(editText.getText().length());

            downEvent.recycle();
            upEvent.recycle();
        }, 200);
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

    public static void hideSoftKeyboard(Activity activity) {
        try {
            InputMethodManager inputMethodManager =
                    (InputMethodManager) activity.getSystemService(
                            Activity.INPUT_METHOD_SERVICE);
            inputMethodManager.hideSoftInputFromWindow(
                    activity.getCurrentFocus().getWindowToken(), 0);
        } catch (Exception e) {
            PillpopperLog.say("Exception in Util hideSoftKeyboard", e);
        }
    }

    public static boolean isNetworkAvailable(Context ctx) {
        return NetworkManager.isDataConnectionAvailable(ctx);
    }

    public static void setAlarm(Context mContext, AlarmManager alarmManager) {
        PendingIntent pi = null;
        if ((pi = getAlarmIntent()) == null) {
            Intent i = new Intent(mContext, KPAlarmManager.class);
            i.putExtra("alarm no", i.toString());
            pi = PendingIntent.getBroadcast(mContext, 0, i, 0);
            setAlarmIntent(pi);
            //RunTimeData.getInstance().setAlarmIntent(pi);
        }
        PillpopperLog.say("Setting Alaram For Keep Alive is done");
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, System.currentTimeMillis() + 1000, AppConstants.SESSION_DURATION, pi); // Millisec * Second *
        // Minute

    }

    public static void cancelAlarm(Context mContext) {
        AlarmManager alarmManager = (AlarmManager) mContext
                .getSystemService(Context.ALARM_SERVICE);
        if (getAlarmIntent() == null) return;
        alarmManager.cancel(getAlarmIntent());
    }

    private static PendingIntent alarmIntent;

    public static PendingIntent getAlarmIntent() {
        return alarmIntent;
    }

    public static void setAlarmIntent(PendingIntent alarmIntent) {
        Util.alarmIntent = alarmIntent;
    }

    public static void performSignout(final Context _thisActivity, final PillpopperAppContext _globalAppContextP) {
        RunTimeData.getInstance().setUserLogedInAndAppTimeout(false);
        PillpopperRunTime.getInstance().setIsFirstTimeSyncDone(false);
        _globalAppContextP.getState(_thisActivity).setAccountId(null);
        PillpopperRunTime.getInstance().setTimeZoneChanged(false);
        _globalAppContextP.resetQuickviewShownFlg(_thisActivity);
        _globalAppContextP.kpClearSSOSessionId(_thisActivity);
        _globalAppContextP.kpSignout(_thisActivity);
        PillpopperConstants.setIsCurrentReminderRefreshRequired(false);
        RunTimeData.getInstance().setInturruptScreenVisible(false);
        AppConstants.setByPassLogin(false);
        AppConstants.setIsFromNotification(false);
        PillpopperRunTime.getInstance().setFromMDO(false);
        PillpopperConstants.setIsAlertActedOn(false);
        new Thread(() -> {
            _globalAppContextP.stopKeepAliveService(_thisActivity);
            _thisActivity.stopService(new Intent(_thisActivity, GetStateService.class));
            performVordelSignoff(_thisActivity);
        }).start();

        //clearing the hasstatusupdate response on sign out.
//        PillpopperRunTime.getInstance().setHasStatusUpdateResponseObj(null);
        RunTimeData.getInstance().setRuntimeSSOSessionID(null);
        mSharedPrefManager = SharedPreferenceManager.getInstance(_thisActivity, AppConstants.AUTH_CODE_PREF_NAME);
        mSharedPrefManager.putBoolean(AppConstants.SIGNED_STATE_REMOVAL, false, false);
        mSharedPrefManager.putBoolean(AppConstants.SIGNED_OUT_STATE_REMOVAL, false, false);
        mSharedPrefManager.putString(AppConstants.TIME_STAMP, "0", false);
        AppConstants.setWelcomeScreensDisplayResult("-1"); // reset
        AppConstants.setFdbScreenDisplayResult("-1"); // reset

        // clear stored Rx Refill Data
        clearRxRefillRelatedData(_thisActivity);
    }

    public static void clearRxRefillRelatedData(Context context) {
        if (AppConstants.IS_NATIVE_RX_REFILL_REQUIRED) {
            RxRefillController.getInstance(context).clearRXFDBImageTable();
            RxRefillController.getInstance(context).clearDownloadFailedImagesTable();
        }

        RefillRuntimeData.getInstance().clearAllPrescriptionByRx();
        RefillRuntimeData.getInstance().clearRuntimePrescriptionData();
        RefillRuntimeData.getInstance().setRefreshToken(null);
        RefillRuntimeData.getInstance().setAccessToken(null);
        RefillRuntimeData.getInstance().setTokenType(null);
        RefillRuntimeData.getInstance().setShoppingCartItemsViewModel(null);
        RefillRuntimeData.getInstance().setDeliveryOrPickUpPharmacyDetails(null);
        RefillRuntimeData.getInstance().setPickUpAtPharmacyObject(null);
        RefillRuntimeData.getInstance().setProfileAPIRootResponse(null);
        RefillRuntimeData.getInstance().setAppInOrderConfirmationScreen(false);
        RefillRuntimeData.getInstance().setPickUpAtPharmacyObject(null);
        RefillRuntimeData.getInstance().setCardForPayment(null);
        RefillRuntimeData.getInstance().setSelectedCardToken(null);
        RefillRuntimeData.getInstance().setMemberAddresses(null);
        RefillRuntimeData.getInstance().setDeliveryByUSMailSelected(true);
        RefillRuntimeData.getInstance().setDrugInfoUrl(null);
        RefillRuntimeData.getInstance().setUserEmail(null);
        RefillRuntimeData.getInstance().setNewContact(null);
        RefillRuntimeData.getInstance().setCardForPayment(null);
        RefillRuntimeData.getInstance().setContactDetail(null);
        RefillRuntimeData.getInstance().setPlaceOrderData(null);
    }

    public static void performSignoutForDeepLinking(final Context _thisActivity, final PillpopperAppContext _globalAppContextP) {
        RunTimeData.getInstance().setUserLogedInAndAppTimeout(false);
        PillpopperRunTime.getInstance().setIsFirstTimeSyncDone(false);
        _globalAppContextP.getState(_thisActivity).setAccountId(null);
        _globalAppContextP.resetQuickviewShownFlg(_thisActivity);
        _globalAppContextP.kpClearSSOSessionId(_thisActivity);
        _globalAppContextP.kpSignoutForDeepLinking(_thisActivity);
        PillpopperRunTime.getInstance().setFromMDO(false);
        AppConstants.setByPassLogin(false);
        AppConstants.setIsFromNotification(false);
        PillpopperConstants.setIsAlertActedOn(false);
        new Thread(() -> {
            _globalAppContextP.stopKeepAliveService(_thisActivity);
            _thisActivity.stopService(new Intent(_thisActivity, GetStateService.class));
            performVordelSignoff(_thisActivity);
        }).start();

        //clearing the hasstatusupdate response on sign out.
//        PillpopperRunTime.getInstance().setHasStatusUpdateResponseObj(null);
        RunTimeData.getInstance().setRuntimeSSOSessionID(null);
        SharedPreferenceManager mSharedPrefManager = SharedPreferenceManager.getInstance(_thisActivity, AppConstants.AUTH_CODE_PREF_NAME);
        mSharedPrefManager.putBoolean(AppConstants.SIGNED_STATE_REMOVAL, false, false);
        mSharedPrefManager.putBoolean(AppConstants.SIGNED_OUT_STATE_REMOVAL, false, false);
        mSharedPrefManager.putString(AppConstants.TIME_STAMP, "0", false);
    }

    public static void autoSignoutResetCookies(final Context context) {
        try {
            if (null != context) {
                new Thread(() -> performVordelSignoff(context)).start();
            }
            resetCookies();
            releaseHttpConnection();

        } catch (Exception e) {
            PillpopperLog.say("Opps!, Exception while clearing the Cookies", e);
        }
    }


    protected static void performVordelSignoff(Context context) {
        if (RunTimeData.getInstance().getRuntimeSSOSessionID() != null) {

            TTGRuntimeData.getInstance().setApiKey(AppConstants.getAPIKEY()); // fix for DE22510
            TTGSignonController.getInstance().performSignoff(RunTimeData.getInstance().getRuntimeSSOSessionID(), null);

            resetCookies();
            releaseHttpConnection();
        }
    }

    public static void resetCookies() {
        try {
            PillpopperServer.clearCookies();
        } catch (Exception e) {
            PillpopperLog.say("Opps!, Exception while clearing the Cookies.", e);
        }
    }


    private static void releaseHttpConnection() {
        new Thread(() -> {
            try {
                TTGHttpUrlConnection.getInstance().getSocketFactory().releaseSocket();
                TTGHttpUrlConnection.getInstance().releaseSocketFactoryResources();
            } catch (Exception e) {
                PillpopperLog.say("Problem in Releasing the Socket.", e);
            }
        }).start();
    }

    public static boolean colorNumberPickerText(NumberPicker numberPicker, int color) {
        final int count = numberPicker.getChildCount();
        for (int i = 0; i < count; i++) {
            View child = numberPicker.getChildAt(i);
            if (child instanceof EditText) {
                try {
                    Field selectorWheelPaintField = numberPicker.getClass()
                            .getDeclaredField("mSelectorWheelPaint");
                    selectorWheelPaintField.setAccessible(true);
                    ((Paint) selectorWheelPaintField.get(numberPicker)).setColor(color);
                    ((EditText) child).setTextColor(color);
                    child.setFocusable(false);
                    numberPicker.invalidate();
                    return true;
                } catch (NoSuchFieldException e) {
                    PillpopperLog.say("Oops!, NoSuchFieldException", e);
                } catch (IllegalAccessException e) {
                    PillpopperLog.say("Oops!, IllegalAccessException", e);
                } catch (IllegalArgumentException e) {
                    PillpopperLog.say("Oops!, IllegalArgumentException", e);
                } catch (Exception e) {
                    PillpopperLog.say("Oops!, Exception", e);
                }
            }
        }
        return false;
    }

    public static TTGSecureWebViewModel getRefillWebViewModel(final PillpopperActivity context) {
        TTGSecureWebViewModel kpsecureModel = null;
        if (ActivationUtil.isNetworkAvailable(context)) {

            kpsecureModel = new TTGSecureWebViewModel();
            kpsecureModel.setCloseImageViewId(TTGSecureWebViewModel.NO_CLOSE_OPTION);
            kpsecureModel.setRefreshImageViewId(R.id.refresh_icon);

            kpsecureModel.setTitle(context.getString(R.string.prescription_refills));

            List<TTGCookie> cookies = new ArrayList<>();

            TTGCookie cookie = new TTGCookie();

            String usableNetCookieName = AppConstants.ConfigParams.getRefillCookieName();
            String usableNetCookieDomain = AppConstants.ConfigParams.getRefillCookieDomain();
            String usableNetCookiePath = AppConstants.ConfigParams.getRefillCookiePath();
            String usableNetCookieIsSecure = AppConstants.ConfigParams.getRefillCookieSecure();

            String refillPrescriptionUrl = getKeyValueFromAppProfileRuntimeData(AppConstants.APP_PROFILE_REFILL_PRESCRIPTION_URL_KEY);
            String aemRefillPrescriptionUrl = getKeyValueFromAppProfileRuntimeData(AppConstants.APP_PROFILE_AEM_REFILL_PRESCRIPTION_URL_KEY);
            String usableNetAEMSwitchCode = getKeyValueFromAppProfileRuntimeData(AppConstants.USABLE_NET_AEM_SWITCH_KEY);

            String loadRefillURL = refillPrescriptionUrl;
            boolean shouldAdd905Cookie = false;
            Map<String, List<String>> cookieHeaders = TTGRuntimeData.getInstance().getHeadersMap();

            if (!Util.isEmptyString(usableNetAEMSwitchCode) && "1".equalsIgnoreCase(usableNetAEMSwitchCode)) {
                loadRefillURL = refillPrescriptionUrl;
                shouldAdd905Cookie = false;
            } else if (!Util.isEmptyString(usableNetAEMSwitchCode) && "2".equalsIgnoreCase(usableNetAEMSwitchCode)) {
                for (Map.Entry<String, List<String>> entry : cookieHeaders.entrySet()) {
                    String key = entry.getKey();
                    if (key != null && (key.equalsIgnoreCase(HttpHeaders.SET_COOKIE))) {
                        boolean is905Found = false;
                        for (String cookieStr : entry.getValue()) {
                            if (cookieStr.contains("SL") && cookieStr.contains("905")) {
                                is905Found = true;
                                shouldAdd905Cookie = true;
                                break;
                            }
                        }
                        if (is905Found) {
                            loadRefillURL = aemRefillPrescriptionUrl;
                        }
                    }
                }
            } else if (!Util.isEmptyString(usableNetAEMSwitchCode) && "3".equalsIgnoreCase(usableNetAEMSwitchCode)) {
                shouldAdd905Cookie = true;
                loadRefillURL = aemRefillPrescriptionUrl;
            }

            if (usableNetCookieName == null || usableNetCookieDomain == null
                    || usableNetCookiePath == null || usableNetCookieIsSecure == null || loadRefillURL == null) {
                DialogHelpers.showAlertDialog(context, context.getResources().getString(R.string.app_profile_error_msg_2));
                return null;
            } else {
                cookie.setName(usableNetCookieName);
                cookie.setValue(ActivationController.getInstance().getSSOSessionId(context));
                cookie.setPath(usableNetCookiePath);
                cookie.setSecure(Boolean.parseBoolean(usableNetCookieIsSecure));
                cookie.setDomain(usableNetCookieDomain);

                cookies.add(cookie);

                kpsecureModel.setUrl(loadRefillURL);

                // add all cookies from token and Keep alive
                setAllCookies(cookies);
                // add x-AppName to the cookielist
                TTGCookie appNameCookie = new TTGCookie();
                appNameCookie.setName(AppConstants.X_APP_NAME_KEY);
                appNameCookie.setValue(AppConstants.X_APP_NAME_VALUE);
                appNameCookie.setPath(usableNetCookiePath);
                appNameCookie.setDomain(usableNetCookieDomain);
                appNameCookie.setSecure(Boolean.parseBoolean(usableNetCookieIsSecure));
                cookies.add(appNameCookie);
                kpsecureModel.setCookies(cookies);
            }

        } else {

            context.runOnUiThread(() -> DialogHelpers
                    .showAlertDialogWithHeader(
                            context,
                            context.getResources().getString(R.string.data_unavailable),
                            context.getResources().getString(R.string.network_connection_error),
                            () -> {

                            }));
        }
        return kpsecureModel;
    }

    public static void showGenericStatusAlert(final Context context, String statusMessage) {
        GenericAlertDialog genericAlertDialog = new GenericAlertDialog(context, null, statusMessage, context.getString(R.string.ok_text), (dialog, which) -> dialog.dismiss(), null, null);

        genericAlertDialog.showDialogWithoutBtnPadding();
    }

    public static int handleParseInt(String str) {
        int returnValue;
        try {
            returnValue = Integer.parseInt(str);
        } catch (NumberFormatException numberFormatException) {
            LoggerUtils.exception("---Number Format Exception for value---", numberFormatException);
            returnValue = 0;
        } catch (Exception exception) {
            LoggerUtils.exception("---Handling Exception ---", exception);
            returnValue = 0;
        }
        return returnValue;
    }

    public static Long handleParseLong(String str) {
        long returnValue;
        try {
            returnValue = Long.parseLong(str);
        } catch (NumberFormatException numberFormatException) {
            LoggerUtils.exception("---Number Format Exception for value---", numberFormatException);
            returnValue = 0L;
        } catch (Exception exception) {
            LoggerUtils.exception("---Handling Exception ---", exception);
            returnValue = 0L;
        }
        return returnValue;
    }

    public static void closeSilently(Closeable stream) {
        if (stream == null) {
            return;
        }
        try {
            stream.close();
        } catch (IOException ex) {
            LoggerUtils.exception("IOException", ex);
        }
    }

    /**
     * @param str the HHMM schedule value will be converted to TimeFormat (ex 08:00:00)
     * @return schedule str in time format (ex. 08:00:00)
     */

    public static String convertHHMMtoTimeFormat(String str) {
        if (!Util.isEmptyString(str) && !str.contains(":") && !str.equals("-1")) {
            String timePatternISO = "%02d:%02d:00";
            return String.format(timePatternISO, Long.parseLong(str) / 100, Long.parseLong(str) % 100);
        }
        return str;
    }

    /**
     * @param str the local db value (ex 08:00:00) will be converted to HHMM format adding the tz_secs
     * @return schedule str in HHMM format
     */
    public static String convertTimeFormatToHHMM(String str) {
        if (!Util.isEmptyString(str) && str.contains(":")) {
            String timePatternHHMM = "%d%02d";
            int schTime = Integer.parseInt(str.replace(":", ""));
            return String.valueOf(Integer.parseInt(String.format(timePatternHHMM, schTime / 100, schTime % 100)) / 100);
        }
        return str;
    }


    /**
     * Converts long to iso date formats and Append 'tzsecs' params in the required json request object
     *
     * @param entryJSONObject
     * @param context
     * @return
     */
    public static JSONObject processPillRequestObjectFrom(JSONObject entryJSONObject, Context context) {

        JSONObject pillRequest = null;
        JSONArray jsonArray = null;

        initDSTParams();
        initTZParams();

        try {
            if (entryJSONObject.optJSONObject(PILLPOPPER_REQUEST) != null) {
                pillRequest = entryJSONObject.optJSONObject(PILLPOPPER_REQUEST);
                applyRequiredFormatToParamsIn(pillRequest, context);
            } else if (entryJSONObject.optJSONObject(PILLPOPPER_MULTI_REQUEST) != null) {
                jsonArray = entryJSONObject.optJSONObject(PILLPOPPER_MULTI_REQUEST).optJSONArray(REQUEST_ARRAY);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject obj = (JSONObject) jsonArray.opt(i);
                    applyRequiredFormatToParamsIn(obj, context);
                }
            }

        } catch (JSONException je) {
            PillpopperLog.say("JSONException", je);
        } catch (Exception e) {
            PillpopperLog.say("Exception", e);
        }
        return entryJSONObject;
    }

    /**
     * Apply the Iso conversion and add tz_secs param inside respective Request objects
     *
     * @param pillRequest
     * @param context
     * @throws JSONException
     */
    private static void applyRequiredFormatToParamsIn(JSONObject pillRequest, Context context) throws JSONException {
        final String pillId = String.valueOf(pillRequest.get(PILL_ID));
        HashMap<String, String> tzMap = DatabaseUtils.getInstance(context).getTimeZoneOffsetsFor(pillId);
        JSONObject preferences = pillRequest.optJSONObject(PREFERENCES);
        applyInPillPopperRequestObject(pillRequest, tzMap);
        applyInPreferenceObject(pillRequest, context, pillId, tzMap, preferences);
    }

    private static void applyInPreferenceObject(JSONObject pillRequest, Context context, String pillId, HashMap<String, String> tzMap, JSONObject preferences) throws JSONException {
        if (preferences == null) return;

        if (pillRequest.opt(isScheduleAddedOrUpdated) != null && pillRequest.optBoolean(isScheduleAddedOrUpdated)) {
            preferences.put(PillpopperConstants.SCHEDULECHANGED_TZSECS, String.valueOf(getTzOffsetSecs(TimeZone.getDefault())));
            DatabaseUtils.getInstance(context).updateScheduleDateChangedTZsecs(pillId, String.valueOf(getTzOffsetSecs(TimeZone.getDefault())));
        }

        if (preferences.has(MISSED_DOSES_LAST_CHECKED)) {
            String missedDosesLastChecked = convertDateLongToIso(String.valueOf(preferences.getString(MISSED_DOSES_LAST_CHECKED)));
            preferences.put(MISSED_DOSES_LAST_CHECKED, missedDosesLastChecked);
            if (tzMap != null && (!isEmptyString(missedDosesLastChecked) && !("-1").equalsIgnoreCase(missedDosesLastChecked))) {
                preferences.put(MISSED_DOSES_LAST_CHECKED.concat(tz_secs), tzMap.get(MISSED_DOSES_LAST_CHECKED));
            }
        }
    }

    private static void applyInPillPopperRequestObject(JSONObject pillRequest, HashMap<String, String> tzMap) throws JSONException {
        for (String param : requestDSTParams) {
            if (pillRequest.opt(param) != null) {
                String value = convertDateLongToIso(String.valueOf(pillRequest.get(param)));
                pillRequest.put(param, value);

                if (requestTZParams.contains(param) && tzMap != null && (value != null && !("-1").equalsIgnoreCase(value))) {
                    pillRequest.put(param.concat(tz_secs), tzMap.get(param));
                }
            }
        }
    }

    public static void setIsCreateUserRequestInprogress(boolean isCreateUserRequestInprogress) {
        Util.isCreateUserRequestInprogress = isCreateUserRequestInprogress;
    }

    public static String getMappedSSOEnvironment(String signOn) {
        if (!isEmptyString(signOn)) {
            String signOnUrl = signOn.toLowerCase();
            if (signOnUrl.contains("hreg1")) {
                return "HREG1";
            } else if (signOnUrl.contains("hreg2")) {
                return "HREG2";
            } else if (signOnUrl.contains("pp1")) {
                return "HPPNDC";
            } else if (signOnUrl.contains("pp2")) {
                return "HPPIDC";
            } else if (signOnUrl.contains("hint1")) {
                return "HINT1";
            } else if (signOnUrl.contains("dev20")) {
                return "DEV20";
            } else if (signOnUrl.contains("dev3")) {
                return "DEV3";
            } else if (signOnUrl.contains("hint3")) {
                return "HINT3";
            } else if (signOnUrl.contains("hint10")) {
                return "HINT10";
            } else if (signOnUrl.contains("hint2")) {
                return "HINT2";
            } else {
                LoggerUtils.info("Unrecognized Env in SignOnURL");
                return "";
            }
        }
        return "";
    }

    public LinkedHashMap<Long, List<Drug>> getCurrentRemindersMap() {
        return currentRemindersMap;
    }

    public void setCurrentRemindersMap(LinkedHashMap<Long, List<Drug>> currentRemindersMap) {
        this.currentRemindersMap = currentRemindersMap;
    }

    public LinkedHashMap<Long, List<Drug>> getPassedRemindersMap() {
        return passedRemindersMap;
    }

    public void setPassedRemindersMap(LinkedHashMap<Long, List<Drug>> passedRemindersMap) {
        this.passedRemindersMap = passedRemindersMap;
    }

    public synchronized Context getContext() {
        return context;
    }

    public synchronized void setContext(Context context) {
        this.context = context;
    }

    public List<Long> getListOfTimes() {
        return listOfTimes;
    }

    private static final String keyDVBaseURL = "api-dv";
    private static final String keyQABaseURL = "api-qa";
    private static final String keyQIBaseURL = "api-qi";
    private static final String keyPPBaseURL = "api-pp";
    private static final String keyPRODBaseURL = "api.mydoctor";

    // need to revisit this method to fetch the client id and client secret from BuildConfig file
    public void setClientIdAndClientSecret() {
        String apiManagerTokenURL = AppConstants.ConfigParams.getAPIManagerTokenBaseURL().toLowerCase();
        if (apiManagerTokenURL.contains(keyDVBaseURL)) {
            AppConstants.CLIENT_ID = AppConstants.DEV_CLIENT_ID;
            AppConstants.CLIENT_SECRET = AppConstants.DEV_CLIENT_SECRET;
        } else if (apiManagerTokenURL.contains(keyQABaseURL)) {
            AppConstants.CLIENT_ID = AppConstants.QA_CLIENT_ID;
            AppConstants.CLIENT_SECRET = AppConstants.QA_CLIENT_SECRET;
        } else if (apiManagerTokenURL.contains(keyQIBaseURL)) {
            AppConstants.CLIENT_ID = AppConstants.QI_CLIENT_ID;
            AppConstants.CLIENT_SECRET = AppConstants.QI_CLIENT_SECRET;
        } else if (apiManagerTokenURL.contains(keyPPBaseURL)) {
            AppConstants.CLIENT_ID = AppConstants.PP_CLIENT_ID;
            AppConstants.CLIENT_SECRET = AppConstants.PP_CLIENT_SECRET;
        } else if (apiManagerTokenURL.contains(keyPRODBaseURL)) {
            AppConstants.CLIENT_ID = AppConstants.PR_CLIENT_ID;
            AppConstants.CLIENT_SECRET = AppConstants.PR_CLIENT_SECRET;
        }
    }

    public void initializeFCM(Context context) {
        //initialize FCM enabled flag from AppProfile response before starting the sign_in
        AppConstants.IS_FIREBASE_CLOUD_MESSAGING_ENABLED = AppConstants.shouldEnableFCMPushNotification();
        // initializing Firebase Messaging after getting the value from AppProfile
        if (AppConstants.IS_FIREBASE_CLOUD_MESSAGING_ENABLED && !RunTimeData.getInstance().isFCMInitialized()) {
            new FCMHandler.Builder().init(context).build();
            RunTimeData.getInstance().setFCMInitialized(true);
        }
    }

    public static class NavDrawerUtils {

        private static DrawerLayout mDrawerLayout;

        public static void closeNavigationDrawerIfOpen() {
            try {
                if (mDrawerLayout.isDrawerOpen(Gravity.LEFT)) {
                    mDrawerLayout.closeDrawers();
                }
            } catch (NullPointerException ne) {
                RxRefillLoggerUtils.exception(ne.getMessage());
            }
        }

        public static void setNavigationDrawerLayout(DrawerLayout drawer) {

            try {
                if (drawer != null)
                    mDrawerLayout = drawer;
            } catch (Exception e) {
                RxRefillLoggerUtils.exception(e.getMessage());
            }
        }
    }

    public static String check24hourformat(String timeExtract, PillpopperActivity context) {
        int hour = 0;
        int min = 0;
        String amPm = "";
        int hourIndex = timeExtract.indexOf(":");
        hour = Integer.parseInt(timeExtract.substring(0, hourIndex));
        if (hour > 12) {
            hour = hour - 12;
            amPm = Util.getSystemPMFormat();
        } else {
            amPm = Util.getSystemAMFormat();
        }
        if (hour == 12) {
            amPm = Util.getSystemPMFormat();
        } else if (hour == 0) {
            hour = 12;
            amPm = Util.getSystemAMFormat();
        }
        min = Integer.parseInt(timeExtract.substring(hourIndex + 1, timeExtract.length()));
        if (min < 10) {
            if (hour < 10) {
                timeExtract = "0" + hour + ":" + "0" + min + " " + amPm;
            } else {
                timeExtract = hour + ":" + "0" + min + " " + amPm;
            }
        } else {
            if (hour < 10) {
                timeExtract = "0" + hour + ":" + min + " " + amPm;
            } else {
                timeExtract = hour + ":" + min + " " + amPm;
            }
        }
        return timeExtract;
    }

    public static int checkForSessionExpire(String response) {

        Gson gson = new Gson();
        try {
            if (null != response) {
                JsonElement responseRootNode = gson.fromJson(response,
                        JsonElement.class).getAsJsonObject().get("response");
                String statusCode = getNodeValueAsText(responseRootNode,
                        "statusCode");
                if (statusCode != null) {
                    return Integer.parseInt(statusCode);
                }
            }
        } catch (JsonParseException e) {
            PillpopperLog.say("JsonParseException", e);
        } catch (NumberFormatException e) {
            PillpopperLog.say("NumberFormatException", e);
        } catch (Exception e) {
            PillpopperLog.say("Exception", e);
        }
        return -1;
    }

    public static String getNodeValueAsText(JsonElement rootNode, String key) {
        if (rootNode != null) {
            JsonElement node = rootNode.getAsJsonObject().get(key);
            if (node != null) {
                return node.getAsString();
            }
        }
        return null;
    }

    public static void showSessionexpireAlert(final Context act, final PillpopperAppContext pillpopperAppContext) {
        DialogHelpers.showAlertDialogWithHeader(act,
                R.string.session_expiry_title_text,
                R.string.session_expiry_message_text,
                () -> performSignout(act, pillpopperAppContext));
    }


    private static boolean isCreateUserRequestInprogress = false;


    public static boolean isCreateUserRequestInprogress() {
        return isCreateUserRequestInprogress;
    }

    public static void setCreateUserRequestInprogress(boolean isCreateUserRequestInprogress) {
        Util.setIsCreateUserRequestInprogress(isCreateUserRequestInprogress);
    }

    public static PillpopperTime convertStringtoPillpopperTime(String str) {
        if (null != str) {
            return new PillpopperTime(handleParseLong(str));
        } else {
            return null;
        }
    }

    public static int getAppVersionCode(Context context) {
        int version = 1;
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(
                    context.getPackageName(), 0);
            version = pInfo.versionCode;
            return version;
        } catch (NameNotFoundException e) {
            LoggerUtils.exception("NameNotFoundException", e);
        }
        return BuildConfig.VERSION_CODE;

    }

    public static Map<String, String> buildHeaders(Context context) {
        Map<String, String> headers;
        ActivationController activationController = ActivationController.getInstance();
        SharedPreferenceManager sharedPrefManager = SharedPreferenceManager.getInstance(context, AppConstants.AUTH_CODE_PREF_NAME);

        headers = new HashMap<>();
        headers.put("secureToken", activationController.getSSOSessionId(context));

        if (!Util.isEmptyString(sharedPrefManager.getString(AppConstants.KP_GUID, ""))) {
            headers.put("guid", sharedPrefManager.getString(AppConstants.KP_GUID, ""));
        }

        if (!Util.isEmptyString(activationController.getUserId(context))) {
            headers.put("userId", activationController.getUserId(context));
        }
        headers.put("os", TTGMobileLibConstants.OS);
        headers.put("appVersion",getAppVersion(context));
        headers.put("osVersion",AppConstants.OS_VERSION);

        return headers;
    }

    /**
     * Prepares the Log entry for provided drug and specified action.
     *
     * @param drug          Discontinued Drug
     * @param _thisActivity context
     * @return LogEntryModel
     */

    public static LogEntryModel prepareAcknowledgeDiscontinuedDrugsLogEntry(DiscontinuedDrug drug, Context _thisActivity) {
        LogEntryModel logEntryModel = new LogEntryModel();
        logEntryModel.setDateAdded(System.currentTimeMillis());
        String replyId = getRandomGuid();
        logEntryModel.setReplyID(replyId);

        JSONObject pillpopperRequest = new JSONObject();
        JSONObject pillRequest = new JSONObject();
        JSONObject pillPrefRequest = new JSONObject();

        try {
            pillRequest.put("apiVersion", "6.0.4");
            pillRequest.put("action", "EditPill");
            pillRequest.put("replayId", replyId);
            pillRequest.put("language", Util.getLanguage());
            pillRequest.put("deviceToken", "");
            pillRequest.put("hardwareId", UniqueDeviceId.getHardwareId(_thisActivity));


            pillRequest.put("pillId", drug.getPillId());

            String primaryMemberUserId = FrontController.getInstance(_thisActivity).getPrimaryUserIdIgnoreEnabled();
            pillRequest.put("userId", primaryMemberUserId);
            pillRequest.put("targetUserId", drug.getUserId());
            pillRequest.put("type", "scheduled");

            pillPrefRequest.put("invisible", "1");

            pillRequest.put("preferences", pillPrefRequest);
            pillpopperRequest.put("pillpopperRequest", pillRequest);

        } catch (JSONException e) {
            PillpopperLog.say("JSONException", e);
        }
        logEntryModel.setEntryJSONObject(pillpopperRequest, _thisActivity);
        return logEntryModel;
    }

    public static LogEntryModel prepareLogEntryForAction(String action, Drug drug, PillpopperActivity _thisActivity) {
        LogEntryModel logEntryModel = new LogEntryModel();
        logEntryModel.setDateAdded(System.currentTimeMillis());
        String replyId = getRandomGuid();
        logEntryModel.setReplyID(replyId);
        logEntryModel.setAction(action);

        JSONObject pillpopperRequest = new JSONObject();
        JSONObject pillRequest = new JSONObject();
        JSONObject pillPrefRequest = new JSONObject();
        String scheduleDate = FrontController.getInstance(_thisActivity).getScheduleDateFromHistory(drug.getGuid());
        PillpopperTime drugNotifyAfterTime = (FrontController.getInstance(_thisActivity).getNotifyAfterValue(drug.getGuid()));

        try {
            pillRequest.put("apiVersion", "6.0.4");
            pillRequest.put("action", action);
            pillRequest.put("replayId", replyId);
            pillRequest.put("language", Util.getLanguage());
            pillRequest.put("deviceToken", "");

            DoseEventCollection doseEventCollection = new DoseEventCollection(_thisActivity, drug, PillpopperTime.now(), 60);
            if (null != doseEventCollection && doseEventCollection.getNextEvent() != null && doseEventCollection.getNextEvent().getDate() != null) {

                if (FrontController.getInstance(_thisActivity).isLastActionEventPostpone(drug.getGuid())) {
                    if (drugNotifyAfterTime.after(doseEventCollection.getNextEvent().getDate())) {
                        if (scheduleDate != null) {
                            if (null != Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(scheduleDate)))
                                pillRequest.put("notify_after",
                                        FrontController.getInstance(_thisActivity).updateNotifyAfterValue(drug.getGuid(), doseEventCollection.getNextEvent().getDate().getGmtSeconds(), Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(scheduleDate)).getGmtSeconds()));
                        } else {
                            FrontController.getInstance(_thisActivity).updateNotifyAfterValue(drug.getGuid(), doseEventCollection.getNextEvent().getDate().getGmtSeconds());
                            pillRequest.put("notify_after", doseEventCollection.getNextEvent().getDate().getGmtSeconds());
                        }
                    } else {
                        // as we updated the local database value in case of restoring a med after the postponing it , using that value for the log entry
                        if(null != drug.get_notifyAfter() && drug.get_notifyAfter().after(drugNotifyAfterTime)){
                            pillRequest.put("notify_after", drug.get_notifyAfter().getGmtSeconds());
                        }else{
                            pillRequest.put("notify_after", drugNotifyAfterTime.getGmtSeconds());
                        }
                    }
                } else {
                    if (scheduleDate != null) {

                        PillpopperLog.say("Issue :  ScheduleDate : " + scheduleDate + " : Next " + doseEventCollection.getNextEvent().getDate().getGmtSeconds());

                        FrontController.getInstance(_thisActivity).updateNotifyAfterValue(drug.getGuid(),
                                doseEventCollection.getNextEvent().getDate().getGmtSeconds(),
                                Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(scheduleDate)).getGmtSeconds());
                        pillRequest.put("notify_after", doseEventCollection.getNextEvent().getDate().getGmtSeconds());
                    } else {
                        FrontController.getInstance(_thisActivity).updateNotifyAfterValue(drug.getGuid(), doseEventCollection.getNextEvent().getDate().getGmtSeconds());
                        String _notifyAfterValue = String.valueOf(doseEventCollection.getNextEvent().getDate().getGmtSeconds());
                        pillRequest.put("notify_after", Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(_notifyAfterValue)).getGmtSeconds());
                    }
                }
            }

            if (null != scheduleDate) {
                if (null != Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(scheduleDate))) {
                    pillRequest.put("scheduleDate", Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(scheduleDate)).getGmtSeconds());
                    pillRequest.put("last_taken", Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(scheduleDate)).getGmtSeconds());
                }
            } else {
                pillRequest.put("last_taken", -1);
                // pillRequest.put("scheduleDate",-1);
            }

//            pillRequest.put("dose", drug.getDose());
            pillRequest.put("name", drug.getName());
           // pillRequest.put("type", getScheduleType(drug.getSchedule().getSchedType()));
            pillRequest.put("clientVersion", getAppVersion(_thisActivity));
            pillRequest.put("partnerId", PillpopperConstants.PARTNER_ID);
            pillRequest.put("pillId", drug.getGuid());
            String scheduleChoice = getScheduleChoice(drug);
            pillRequest.put("start", PillpopperDay.marshalLocalDayAsGMTTime(PillpopperDay.PartOfDay.DayStart, scheduleChoice,drug.getSchedule(), drug.getSchedule().getStart()));
            pillRequest.put("end", PillpopperDay.marshalLocalDayAsGMTTime(PillpopperDay.PartOfDay.DayEnd, scheduleChoice,drug.getSchedule(), drug.getSchedule().getEnd()));
            pillRequest.put("interval", 0);
            if (!PillpopperConstants.ACTION_EDIT_PILL.equalsIgnoreCase(action)) {
                pillRequest.put("opId", drug.getOpID());
            }

            if (null != drug.get_effLastTaken()) {
                pillRequest.put("eff_last_taken", drug.get_effLastTaken().getGmtSeconds());
            } else {
                pillRequest.put("eff_last_taken", -1);
            }

            if (action.equalsIgnoreCase(PillpopperConstants.ACTION_POST_PONE_PILL)) {
                if (drug.getPostponeSeconds() != 0) {
                    PillpopperTime postponedTime = new PillpopperTime(calculateUpdatedSchedule(drug.getPostponeSeconds()));
                    if (postponedTime.before(doseEventCollection.getNextEvent().getDate())
                            || postponedTime.getGmtSeconds() == doseEventCollection.getNextEvent().getDate().getGmtSeconds()) {
                        pillRequest.put("seconds", drug.getPostponeSeconds());
                        if (scheduleDate != null) {
                            if (null != Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(scheduleDate)))
                                pillRequest.put("notify_after",
                                        FrontController.getInstance(_thisActivity).updateNotifyAfterValue(drug.getGuid(), calculateUpdatedSchedule(drug.getPostponeSeconds()), Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(scheduleDate)).getGmtSeconds()));
                        } else {
                            pillRequest.put("notify_after", calculateUpdatedSchedule(drug.getPostponeSeconds()));
                            FrontController.getInstance(_thisActivity).updateNotifyAfterValue(drug.getGuid(), calculateUpdatedSchedule(drug.getPostponeSeconds()));
                        }
                    }
                }
            }

            pillRequest.put("hardwareId", UniqueDeviceId.getHardwareId(_thisActivity));
            String primaryMemberUserId = FrontController.getInstance(_thisActivity).getPrimaryUserIdIgnoreEnabled();
            pillRequest.put("userId", primaryMemberUserId);
            pillRequest.put("targetUserId", drug.getUserID());

            pillRequest.put("schedule", FrontController.getInstance(_thisActivity).getSchedulesInTimeFormateByPillId(drug.getGuid()));

            if (drug.isScheduleAddedOrUpdated()) {
                pillRequest.put("isScheduleAddedOrUpdated", true);
            } else {
                pillRequest.put("isScheduleAddedOrUpdated", false);
            }

            pillRequest.put("dayperiod", drug.getSchedule().getDayPeriod());
            pillPrefRequest.put("customDosageID", null != drug.getPreferences().getPreference("customDosageID") ? drug.getPreferences().getPreference("customDosageID") : "");
            pillPrefRequest.put("refillsRemaining", "-1");
            pillPrefRequest.put("personId", drug.getUserID());
            String dosageType = drug.getPreferences().getPreference("dosageType");
            pillPrefRequest.put("dosageType", !Util.isEmptyString(dosageType) ? dosageType : PillpopperConstants.DOSAGE_TYPE_CUSTOM);
            pillPrefRequest.put("invisible", drug.getPreferences().getPreference("invisible"));
            pillPrefRequest.put("archived", drug.getPreferences().getPreference("archived"));
            pillPrefRequest.put("refillAlertDoses", null != drug.getPreferences().getPreference("refillAlertDoses") ? drug.getPreferences().getPreference("refillAlertDoses") : "");
            pillPrefRequest.put("notes", null != drug.getPreferences().getPreference("notes") ? drug.getPreferences().getPreference("notes") : "");
            pillPrefRequest.put("refillQuantity", null != drug.getPreferences().getPreference("refillQuantity") ? drug.getPreferences().getPreference("refillQuantity") : "");
            pillPrefRequest.put("customDescription", null != drug.getPreferences().getPreference("customDescription") ? drug.getPreferences().getPreference("customDescription") : "");
            pillPrefRequest.put("remainingQuantity", null != drug.getPreferences().getPreference("remainingQuantity") ? drug.getPreferences().getPreference("remainingQuantity") : "");
            pillPrefRequest.put("lastManagedIdNotified", null != drug.getPreferences().getPreference("lastManagedIdNotified") ? drug.getPreferences().getPreference("lastManagedIdNotified") : "");
            pillPrefRequest.put("lastManagedIdNeedingNotify", null != drug.getPreferences().getPreference("lastManagedIdNeedingNotify") ? drug.getPreferences().getPreference("lastManagedIdNeedingNotify") : "");
            pillPrefRequest.put("secondaryReminders", "1");
            pillPrefRequest.put("missedDosesLastChecked", FrontController.getInstance(_thisActivity).getMissedDosesLastCheckedValue(drug.getGuid()));
            pillPrefRequest.put("doctorCount", null != drug.getPreferences().getPreference("doctorCount") ? drug.getPreferences().getPreference("doctorCount") : "1");
            pillPrefRequest.put("imageGUID", null != drug.getPreferences().getPreference("imageGUID") ? drug.getPreferences().getPreference("imageGUID") : " ");
            pillPrefRequest.put("pharmacyCount", null != drug.getPreferences().getPreference("pharmacyCount") ? drug.getPreferences().getPreference("pharmacyCount") : "0");
            pillPrefRequest.put("prescriptionNum", null != drug.getPreferences().getPreference("prescriptionNum") ? drug.getPreferences().getPreference("prescriptionNum") : "");
            pillPrefRequest.put("scheduleChoice", scheduleChoice);
            if (null != drug.getSchedule() && null != drug.getSchedule().getTimeList() && drug.getSchedule().getTimeList().length() > 0) {
                pillPrefRequest.put("weekdays", null != drug.getPreferences().getPreference("weekdays") ? drug.getPreferences().getPreference("weekdays") : "");
                pillPrefRequest.put("scheduleFrequency", drug.getScheduledFrequency());
            }

            if (("EditPill").equalsIgnoreCase(action)) {
                pillRequest.put("scheduleGuid", drug.getScheduleGuid());
                if (FrontController.getInstance(_thisActivity).getSchedulesInTimeFormateByPillId(drug.getGuid()).length() > 0) {
                    pillPrefRequest.put("maxNumDailyDoses", "-1");
                    pillPrefRequest.put("limitType", "0");
                } else {
                    pillPrefRequest.put("maxNumDailyDoses", 0 == drug.getSchedule().getDailyLimit() || -1 == drug.getSchedule().getDailyLimit()
                            ? -1 : drug.getSchedule().getDailyLimit());
                    pillPrefRequest.put("limitType", 0 == drug.getSchedule().getDailyLimit() || -1 == drug.getSchedule().getDailyLimit()
                            ? 0 : 1);
                }
                pillPrefRequest.put("databaseNDC", null != drug.getPreferences().getPreference("databaseNDC") ? drug.getPreferences().getPreference("databaseNDC") : " ");
                pillPrefRequest.put("defaultImageChoice", null != drug.getPreferences().getPreference("defaultImageChoice") ? drug.getPreferences().getPreference("defaultImageChoice") : AppConstants.IMAGE_CHOICE_NO_IMAGE);
                pillPrefRequest.put("defaultServiceImageID", null != drug.getPreferences().getPreference("defaultServiceImageID") ? drug.getPreferences().getPreference("defaultServiceImageID") : " ");
                pillPrefRequest.put("noPush", null != drug.getPreferences().getPreference("noPush") ? null != drug.getPreferences().getPreference("noPush") : "");
                pillPrefRequest.put("needFDBUpdate", null != drug.getPreferences().getPreference("needFDBUpdate") ? null != drug.getPreferences().getPreference("needFDBUpdate") : "false");
            }
            pillRequest.put("preferences", pillPrefRequest);
            pillpopperRequest.put("pillpopperRequest", pillRequest);

            PillpopperLog.say("--Adding log entry for action : " + action + " is : " + pillpopperRequest.toString());

        } catch (JSONException e) {
            PillpopperLog.say("Oops!, Exception while preparing the log entry model object", e);
        }
        logEntryModel.setEntryJSONObject(pillpopperRequest, _thisActivity);
        return logEntryModel;
    }

    public static LogEntryModel prepareLogEntryForAction(String action, Drug drug, Context context) {
        LogEntryModel logEntryModel = new LogEntryModel();
        logEntryModel.setDateAdded(System.currentTimeMillis());
        String replyId = getRandomGuid();
        logEntryModel.setReplyID(replyId);
        logEntryModel.setAction(action);

        JSONObject pillpopperRequest = new JSONObject();
        JSONObject pillRequest = new JSONObject();
        JSONObject pillPrefRequest = new JSONObject();
        String scheduleDate = FrontController.getInstance(context).getScheduleDateFromHistory(drug.getGuid());
        PillpopperTime drugNotifyAfterTime = (FrontController.getInstance(context).getNotifyAfterValue(drug.getGuid()));

        try {
            pillRequest.put("apiVersion", "6.0.4");
            pillRequest.put("action", action);
            pillRequest.put("replayId", replyId);
            pillRequest.put("language", Util.getLanguage());
            pillRequest.put("deviceToken", "");

            DoseEventCollection doseEventCollection = null;
            if (AppConstants.updateNotifyAfterValue) {
                // updating  the notify value to next event time from the postponed time if you take action on postponed entry in history details screen
                doseEventCollection = new DoseEventCollection(context, drug, drug.get_notifyAfter(), 60);
                if(null != drug.getHistoryScheduleDate()) {
                    // overiding the scheduleDate only if we taking action for a postponed med from the history details screen because we need original schedule time
                    scheduleDate = convertDateLongToIso(drug.getHistoryScheduleDate());
                }
            } else {
                doseEventCollection = new DoseEventCollection(context, drug, PillpopperTime.now(), 60);
            }
            if (null != doseEventCollection && doseEventCollection.getNextEvent() != null && doseEventCollection.getNextEvent().getDate() != null) {

                if (FrontController.getInstance(context).isLastActionEventPostpone(drug.getGuid())) {
                    if (drugNotifyAfterTime.after(doseEventCollection.getNextEvent().getDate())) {
                        if (scheduleDate != null) {
                            if (null != Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(scheduleDate))) {
                                pillRequest.put("notify_after",
                                        FrontController.getInstance(context).updateNotifyAfterValue(drug.getGuid(), doseEventCollection.getNextEvent().getDate().getGmtSeconds(), Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(scheduleDate)).getGmtSeconds()));
                            }
                        } else {
                            FrontController.getInstance(context).updateNotifyAfterValue(drug.getGuid(), doseEventCollection.getNextEvent().getDate().getGmtSeconds());
                            pillRequest.put("notify_after", doseEventCollection.getNextEvent().getDate().getGmtSeconds());
                        }
                    } else {
                        pillRequest.put("notify_after", drugNotifyAfterTime.getGmtSeconds());
                    }
                } else {
                    if (scheduleDate != null) {
                        pillRequest.put("notify_after",
                                FrontController.getInstance(context).updateNotifyAfterValue(drug.getGuid(), doseEventCollection.getNextEvent().getDate().getGmtSeconds(), Util.convertStringtoPillpopperTime(scheduleDate).getGmtSeconds()));
                    } else {
                        FrontController.getInstance(context).updateNotifyAfterValue(drug.getGuid(), doseEventCollection.getNextEvent().getDate().getGmtSeconds());
                        String _notifyAfterValue = String.valueOf(doseEventCollection.getNextEvent().getDate().getGmtSeconds());
                        pillRequest.put("notify_after", Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(_notifyAfterValue)).getGmtSeconds());
                    }
                }
            }

            if (null != scheduleDate) {
                if (null != Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(scheduleDate))) {
                    pillRequest.put("scheduleDate", Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(scheduleDate)).getGmtSeconds());
                    pillRequest.put("last_taken", action.equalsIgnoreCase(PillpopperConstants.ACTION_POST_PONE_PILL) ?
                            -1 : Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(scheduleDate)).getGmtSeconds());
                }
            } else {
                pillRequest.put("last_taken", -1);
            }

//            pillRequest.put("dose", drug.getDose());
            pillRequest.put("name", drug.getName());
           // pillRequest.put("type", getScheduleType(drug.getSchedule().getSchedType()));
            pillRequest.put("clientVersion", getAppVersion(context));
            pillRequest.put("partnerId", PillpopperConstants.PARTNER_ID);
            pillRequest.put("pillId", drug.getGuid());
            String scheduleChoice = Util.getScheduleChoice(drug);
            pillRequest.put("start", PillpopperDay.marshalLocalDayAsGMTTime(PillpopperDay.PartOfDay.DayStart, scheduleChoice, drug.getSchedule(), drug.getSchedule().getStart()));
            pillRequest.put("end", PillpopperDay.marshalLocalDayAsGMTTime(PillpopperDay.PartOfDay.DayEnd, scheduleChoice,drug.getSchedule(), drug.getSchedule().getEnd()));
            pillRequest.put("interval", 0);

            if (!PillpopperConstants.ACTION_EDIT_PILL.equalsIgnoreCase(action)) {
                pillRequest.put("opId", drug.getOpID());
            }

            if (null != drug.get_effLastTaken()) {
                pillRequest.put("eff_last_taken", drug.get_effLastTaken().getGmtSeconds());
            } else {
                pillRequest.put("eff_last_taken", -1);
            }

            if (action.equalsIgnoreCase(PillpopperConstants.ACTION_POST_PONE_PILL)) {
                if (drug.getPostponeSeconds() != 0) {
                    PillpopperTime postponedTime = new PillpopperTime(calculateUpdatedSchedule(drug.getPostponeSeconds()));
                    if (null != doseEventCollection.getNextEvent()) {
                        if (postponedTime.before(doseEventCollection.getNextEvent().getDate())
                                || postponedTime.getGmtSeconds() == doseEventCollection.getNextEvent().getDate().getGmtSeconds()) {
                            setNotifyAfterForPostponeEvent(drug, context, pillRequest, scheduleDate);
                        }
                    } else {
                        setNotifyAfterForPostponeEvent(drug, context, pillRequest, scheduleDate);
                    }
                }
            }

            //pillRequest.put("created", PillpopperTime.now());
            pillRequest.put("hardwareId", UniqueDeviceId.getHardwareId(context));

            //pillRequest.put("pillTime", drug.getLast());

            String primaryMemberUserId = FrontController.getInstance(context).getPrimaryUserIdIgnoreEnabled();
            pillRequest.put("userId", primaryMemberUserId);
            pillRequest.put("targetUserId", drug.getUserID());

            pillRequest.put("schedule", FrontController.getInstance(context).getSchedulesInTimeFormateByPillId(drug.getGuid()));

            if (drug.isScheduleAddedOrUpdated()) {
                pillRequest.put("isScheduleAddedOrUpdated", true);
            } else {
                pillRequest.put("isScheduleAddedOrUpdated", false);
            }

            pillRequest.put("dayperiod", drug.getSchedule().getDayPeriod());

            if(PillpopperConstants.ACTION_EDIT_PILL.equalsIgnoreCase(action)){
                pillRequest.put("scheduleGuid", drug.getScheduleGuid());
            }
            pillPrefRequest.put("customDescription", null != drug.getPreferences().getPreference("customDescription") ? drug.getPreferences().getPreference("customDescription") : "");
            if(null != drug.getSchedule() && null != drug.getSchedule().getTimeList()){
                pillPrefRequest.put("weekdays", null != drug.getPreferences().getPreference("weekdays") ? drug.getPreferences().getPreference("weekdays") : "");
                pillPrefRequest.put("scheduleFrequency", drug.getScheduledFrequency());
            }
            String dosageType = drug.getPreferences().getPreference("dosageType");
            pillPrefRequest.put("dosageType", !Util.isEmptyString(dosageType) ? dosageType : PillpopperConstants.DOSAGE_TYPE_CUSTOM);
           // get the latest missedDosesLastChecked value from the db.
            // Because the drug object will have the value when it was instantiated in prepareRemindersMapData and it could be old.
            pillPrefRequest.put("missedDosesLastChecked", FrontController.getInstance(context).getMissedDosesLastCheckedValue(drug.getGuid()));
            pillPrefRequest.put("scheduleChoice", scheduleChoice);
            pillPrefRequest.put("defaultImageChoice", null != drug.getPreferences().getPreference("defaultImageChoice") ? drug.getPreferences().getPreference("defaultImageChoice") : " ");
            pillPrefRequest.put("defaultServiceImageID", null != drug.getPreferences().getPreference("defaultServiceImageID") ? drug.getPreferences().getPreference("defaultServiceImageID") : " ");
            pillPrefRequest.put("secondaryReminders", "1");
            pillPrefRequest.put("imageGUID", null != drug.getPreferences().getPreference("imageGUID") ? drug.getPreferences().getPreference("imageGUID") : " ");
            pillPrefRequest.put("prescriptionNum", null != drug.getPreferences().getPreference("prescriptionNum") ? drug.getPreferences().getPreference("prescriptionNum") : "");
            pillPrefRequest.put("customDosageID", null != drug.getPreferences().getPreference("customDosageID") ? drug.getPreferences().getPreference("customDosageID") : "");
            pillPrefRequest.put("refillsRemaining", "-1");
            pillPrefRequest.put("personId", drug.getUserID());
            pillPrefRequest.put("invisible", drug.getPreferences().getPreference("invisible"));
            pillPrefRequest.put("archived", drug.getPreferences().getPreference("archived"));
            pillPrefRequest.put("refillAlertDoses", null != drug.getPreferences().getPreference("refillAlertDoses") ? drug.getPreferences().getPreference("refillAlertDoses") : "");
            pillPrefRequest.put("notes", null != drug.getPreferences().getPreference("notes") ? drug.getPreferences().getPreference("notes") : "");
            pillPrefRequest.put("refillQuantity", null != drug.getPreferences().getPreference("refillQuantity") ? drug.getPreferences().getPreference("refillQuantity") : "");
            pillPrefRequest.put("customDescription", null != drug.getPreferences().getPreference("customDescription") ? drug.getPreferences().getPreference("customDescription") : "");
            pillPrefRequest.put("remainingQuantity", null != drug.getPreferences().getPreference("remainingQuantity") ? drug.getPreferences().getPreference("remainingQuantity") : "");
            pillPrefRequest.put("noPush", null != drug.getPreferences().getPreference("noPush") ? drug.getPreferences().getPreference("noPush") : "");
            pillPrefRequest.put("needFDBUpdate", null != drug.getPreferences().getPreference("needFDBUpdate") ? drug.getPreferences().getPreference("needFDBUpdate") : "");
            pillPrefRequest.put("logMissedDoses",null != drug.getPreferences().getPreference("logMissedDoses") ? drug.getPreferences().getPreference("logMissedDoses") : "");
            pillPrefRequest.put("lastManagedIdNotified", null != drug.getPreferences().getPreference("lastManagedIdNotified") ? drug.getPreferences().getPreference("lastManagedIdNotified") : "");
            pillPrefRequest.put("lastManagedIdNeedingNotify", null != drug.getPreferences().getPreference("lastManagedIdNeedingNotify") ? drug.getPreferences().getPreference("lastManagedIdNeedingNotify"):"");
            pillPrefRequest.put("doctorCount", null != drug.getPreferences().getPreference("doctorCount") ? drug.getPreferences().getPreference("doctorCount") : "1");
            pillPrefRequest.put("pharmacyCount", null != drug.getPreferences().getPreference("pharmacyCount") ? drug.getPreferences().getPreference("pharmacyCount") : "0");
            pillPrefRequest.put("databaseNDC", null != drug.getPreferences().getPreference("databaseNDC") ? drug.getPreferences().getPreference("databaseNDC") : " ");

            pillRequest.put("preferences", pillPrefRequest);
            pillpopperRequest.put("pillpopperRequest", pillRequest);

            PillpopperLog.say("--Adding log entry for action : " + action + " is : " + pillpopperRequest.toString());

        } catch (JSONException e) {
            PillpopperLog.say("Oops!, Exception while preparing the log entry model object", e);
        }
        logEntryModel.setEntryJSONObject(pillpopperRequest, context);
        return logEntryModel;
    }

    private static void setNotifyAfterForPostponeEvent(Drug drug, Context context, JSONObject pillRequest, String scheduleDate) throws JSONException {
        pillRequest.put("seconds", drug.getPostponeSeconds());
        if (scheduleDate != null) {
            PillpopperTime pillPopperTime = Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(scheduleDate));
            if (null != pillPopperTime) {
                pillRequest.put("notify_after",
                        FrontController.getInstance(context).updateNotifyAfterValue(drug.getGuid(), calculateUpdatedSchedule(drug.getPostponeSeconds()),pillPopperTime.getGmtSeconds()));
            }
        } else {
            pillRequest.put("notify_after", calculateUpdatedSchedule(drug.getPostponeSeconds()));
            FrontController.getInstance(context).updateNotifyAfterValue(drug.getGuid(), calculateUpdatedSchedule(drug.getPostponeSeconds()));
        }
    }


    public static LogEntryModel prepareLogEntryForAction_pastReminders(String action, Drug drug, Context _thisActivity) {
        LogEntryModel logEntryModel = new LogEntryModel();
        logEntryModel.setDateAdded(System.currentTimeMillis());
        String replyId = getRandomGuid();
        logEntryModel.setReplyID(replyId);
        logEntryModel.setAction(action);

        JSONObject pillpopperRequest = new JSONObject();
        JSONObject pillRequest = new JSONObject();
        JSONObject pillPrefRequest = new JSONObject();
        String scheduleDate = FrontController.getInstance(_thisActivity).getScheduleDateFromHistory(drug.getGuid());
        PillpopperTime drugNotifyAfterTime = (FrontController.getInstance(_thisActivity).getNotifyAfterValue(drug.getGuid()));

        try {
            pillRequest.put("apiVersion", "6.0.4");
            pillRequest.put("action", action);
            pillRequest.put("replayId", replyId);
            pillRequest.put("language", Util.getLanguage());
            pillRequest.put("deviceToken", "");
            DoseEventCollection doseEventCollection = null;
            if (AppConstants.updateNotifyAfterValue) {
                // updating  the notify value to next event time from the postponed time if you take action on postponed entry in history details screen
                doseEventCollection = new DoseEventCollection(_thisActivity, drug, drug.get_notifyAfter(), 60);
            } else {
                doseEventCollection = new DoseEventCollection(_thisActivity, drug, PillpopperTime.now(), 60);
            }
            if (null != doseEventCollection && doseEventCollection.getNextEvent() != null && doseEventCollection.getNextEvent().getDate() != null) {

                if (FrontController.getInstance(_thisActivity).isLastActionEventPostpone(drug.getGuid())) {
                    if (drugNotifyAfterTime.after(doseEventCollection.getNextEvent().getDate())
                            || drugNotifyAfterTime.getGmtSeconds() == doseEventCollection.getNextEvent().getDate().getGmtSeconds()) {
                        if (scheduleDate != null) {
                            if (null != Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(scheduleDate)))
                                pillRequest.put("notify_after",
                                        FrontController.getInstance(_thisActivity).updateNotifyAfterValue(drug.getGuid(), doseEventCollection.getNextEvent().getDate().getGmtSeconds(), Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(scheduleDate)).getGmtSeconds()));
                             } else {
                            pillRequest.put("notify_after", doseEventCollection.getNextEvent().getDate().getGmtSeconds());
                            FrontController.getInstance(_thisActivity).updateNotifyAfterValue(drug.getGuid(), doseEventCollection.getNextEvent().getDate().getGmtSeconds());
                        }
                    } else {
                        if (null != scheduleDate) {
                            pillRequest.put("notify_after",
                                    FrontController.getInstance(_thisActivity).updateNotifyAfterValue(drug.getGuid(), doseEventCollection.getNextEvent().getDate().getGmtSeconds(), Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(scheduleDate)).getGmtSeconds()));
                        }
                    }
                } else {
                    if (scheduleDate != null) {
                        if (!RunTimeData.getInstance().isFromNotificationAction()) {
                            pillRequest.put("notify_after",
                                    FrontController.getInstance(_thisActivity).updateNotifyAfterValue(drug.getGuid(), doseEventCollection.getNextEvent().getDate().getGmtSeconds(), Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(scheduleDate)).getGmtSeconds()));
                            LoggerUtils.info("notify after value " + doseEventCollection.getNextEvent().getDate().getGmtSeconds() + " for drug -" + drug.getName() + " ");
                        } else {

                            // notification action part
                            PillpopperTime dbNotifyAfterValue = FrontController.getInstance(_thisActivity).getNotifyAfterValue(drug.getGuid());

                            LoggerUtils.info("doseEventCollection.getNextEvent().date " + doseEventCollection.getNextEvent().getDate().getGmtSeconds() + " for drug -" + drug.getName() + " ");

                            if (dbNotifyAfterValue.getGmtSeconds() == drug.getScheduledTime().getGmtSeconds()
                                    || dbNotifyAfterValue.getGmtSeconds() > doseEventCollection.getNextEvent().getDate().getGmtSeconds()) {
                                pillRequest.put("notify_after", doseEventCollection.getNextEvent().getDate().getGmtSeconds());
                            } else if (dbNotifyAfterValue.getGmtSeconds() > doseEventCollection.getNextEvent().getDate().getGmtSeconds()) {
                                pillRequest.put("notify_after", dbNotifyAfterValue.getGmtSeconds());
                                LoggerUtils.info("dbNotifyAfterValue " + dbNotifyAfterValue.getGmtSeconds());
                            } else if (dbNotifyAfterValue.getGmtSeconds() < doseEventCollection.getNextEvent().getDate().getGmtSeconds()
                                    && doseEventCollection.getNextEvent().getDate().before(PillpopperTime.now())) {
                                try{
                                    Drug _currentDrug = FrontController.getInstance(_thisActivity).getDrugByPillId(drug.getGuid());
                                    _currentDrug.computeDBDoseEvents(_thisActivity, _currentDrug, PillpopperTime.now(), 60);
                                    pillRequest.put("notify_after", _currentDrug.get_doseEventCollection().getNextEvent().getDate().getGmtSeconds());
                                } catch (Exception ex){
                                    LoggerUtils.info("Failed to update notify_after for drug  "+ drug.getName());
                                }
                            } else{
                                LoggerUtils.info("Failed to update notify_after for drug  "+ drug.getName());
                            }

                            // updating the notify after value to local db, will create issue DE10937 // hence removed.
//                            if(RunTimeData.getInstance().isFromNotificationActionForCurrent()){
//                                FrontController.getInstance(_thisActivity).updateNotifyAfterValue(drug.getGuid(), doseEventCollection.getNextEvent().date.getGmtSeconds());
//                            }
                        }
                    } else {
                        pillRequest.put("notify_after", doseEventCollection.getNextEvent().getDate().getGmtSeconds());
                        FrontController.getInstance(_thisActivity).updateNotifyAfterValue(drug.getGuid(), doseEventCollection.getNextEvent().getDate().getGmtSeconds());
                    }
                }
            }

            pillRequest.put("scheduleDate", drug.getScheduledTime().getGmtSeconds());

            if (null != scheduleDate) {
                scheduleDate = Util.convertDateIsoToLong(scheduleDate);
                if (null != Util.convertStringtoPillpopperTime(scheduleDate))
                    pillRequest.put("last_taken", Util.convertStringtoPillpopperTime(scheduleDate).getGmtSeconds());
            } else {
                pillRequest.put("last_taken", -1);
                // pillRequest.put("scheduleDate",-1);
            }

//            pillRequest.put("dose", drug.getDose());
            pillRequest.put("name", drug.getName());
           // pillRequest.put("type", getScheduleType(drug.getSchedule().getSchedType()));
            pillRequest.put("clientVersion", getAppVersion(_thisActivity));
            pillRequest.put("partnerId", PillpopperConstants.PARTNER_ID);
            pillRequest.put("pillId", drug.getGuid());
            String scheduleChoice = Util.getScheduleChoice(drug);
            pillRequest.put("start", PillpopperDay.marshalLocalDayAsGMTTime(PillpopperDay.PartOfDay.DayStart, scheduleChoice, drug.getSchedule(), drug.getSchedule().getStart()));
            pillRequest.put("end", PillpopperDay.marshalLocalDayAsGMTTime(PillpopperDay.PartOfDay.DayEnd, scheduleChoice, drug.getSchedule(), drug.getSchedule().getEnd()));
            pillRequest.put("interval", 0);

            pillRequest.put("opId", drug.getOpID());

            if (null != drug.get_effLastTaken()) {
                pillRequest.put("eff_last_taken", drug.get_effLastTaken().getGmtSeconds());
            } else {
                pillRequest.put("eff_last_taken", -1);
            }

            if (action.equalsIgnoreCase(PillpopperConstants.ACTION_POST_PONE_PILL)) {
                if (drug.getPostponeSeconds() != 0) {
                    PillpopperTime postponedTime = new PillpopperTime(calculateUpdatedSchedule(drug.getPostponeSeconds()));
                    if (postponedTime.before(doseEventCollection.getNextEvent().getDate())
                            || postponedTime.getGmtSeconds() == doseEventCollection.getNextEvent().getDate().getGmtSeconds()) {
                        pillRequest.put("seconds", drug.getPostponeSeconds());
                        if (null != scheduleDate) {
                            PillpopperTime pillpopperTime = Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(scheduleDate));
                            if (null != pillpopperTime) {
                                pillRequest.put("notify_after",
                                        FrontController.getInstance(_thisActivity).updateNotifyAfterValue(drug.getGuid(), calculateUpdatedSchedule(drug.getPostponeSeconds()), pillpopperTime.getGmtSeconds()));
                            }
                        } else {
                            pillRequest.put("notify_after", calculateUpdatedSchedule(drug.getPostponeSeconds()));
                            FrontController.getInstance(_thisActivity).updateNotifyAfterValue(drug.getGuid(), calculateUpdatedSchedule(drug.getPostponeSeconds()));
                        }
                    }
                }
            }
            pillRequest.put("hardwareId", UniqueDeviceId.getHardwareId(_thisActivity));
            String primaryMemberUserId = FrontController.getInstance(_thisActivity).getPrimaryUserIdIgnoreEnabled();
            pillRequest.put("userId", primaryMemberUserId);
            pillRequest.put("targetUserId", drug.getUserID());

            pillRequest.put("schedule", FrontController.getInstance(_thisActivity).getSchedulesInTimeFormateByPillId(drug.getGuid()));

            if (drug.isScheduleAddedOrUpdated()) {
                pillRequest.put("isScheduleAddedOrUpdated", true);
            } else {
                pillRequest.put("isScheduleAddedOrUpdated", false);
            }

            pillRequest.put("dayperiod", drug.getSchedule().getDayPeriod());
          //  pillPrefRequest.put("customDosageID", null != drug.getPreferences().getPreference("customDosageID") ? drug.getPreferences().getPreference("customDosageID") : "");
          //  pillPrefRequest.put("refillsRemaining", "-1");
          //  pillPrefRequest.put("personId", drug.getUserID());
           // String dosageType = drug.getPreferences().getPreference("dosageType");
          //  pillPrefRequest.put("dosageType", !Util.isEmptyString(dosageType) ? dosageType : PillpopperConstants.DOSAGE_TYPE_CUSTOM);
         //   pillPrefRequest.put("invisible", drug.getPreferences().getPreference("invisible"));
          //  pillPrefRequest.put("archived", drug.getPreferences().getPreference("archived"));
          //  pillPrefRequest.put("refillAlertDoses", null != drug.getPreferences().getPreference("refillAlertDoses") ? drug.getPreferences().getPreference("refillAlertDoses") : "");
           // pillPrefRequest.put("notes", null != drug.getPreferences().getPreference("notes") ? drug.getPreferences().getPreference("notes") : "");
          //  pillPrefRequest.put("refillQuantity", null != drug.getPreferences().getPreference("refillQuantity") ? drug.getPreferences().getPreference("refillQuantity") : "");
          //  pillPrefRequest.put("customDescription", null != drug.getPreferences().getPreference("customDescription") ? drug.getPreferences().getPreference("customDescription") : "");
         //   pillPrefRequest.put("remainingQuantity", null != drug.getPreferences().getPreference("remainingQuantity") ? drug.getPreferences().getPreference("remainingQuantity") : "");
        //    pillPrefRequest.put("lastManagedIdNotified", null != drug.getPreferences().getPreference("lastManagedIdNotified") ? drug.getPreferences().getPreference("lastManagedIdNotified") : "");
         //   pillPrefRequest.put("lastManagedIdNeedingNotify", null != drug.getPreferences().getPreference("lastManagedIdNeedingNotify") ? drug.getPreferences().getPreference("lastManagedIdNeedingNotify") : "");
          //  pillPrefRequest.put("secondaryReminders", "1");
            pillPrefRequest.put("missedDosesLastChecked", FrontController.getInstance(_thisActivity).getMissedDosesLastCheckedValue(drug.getGuid()));
         //   pillPrefRequest.put("doctorCount", null != drug.getPreferences().getPreference("doctorCount") ? drug.getPreferences().getPreference("doctorCount") : "1");
          //  pillPrefRequest.put("imageGUID", null != drug.getPreferences().getPreference("imageGUID") ? drug.getPreferences().getPreference("imageGUID") : " ");
          //  pillPrefRequest.put("pharmacyCount", null != drug.getPreferences().getPreference("pharmacyCount") ? drug.getPreferences().getPreference("pharmacyCount") : "0");
         //   pillPrefRequest.put("prescriptionNum", null != drug.getPreferences().getPreference("prescriptionNum") ? drug.getPreferences().getPreference("prescriptionNum") : "");
          //  pillPrefRequest.put("weekdays", null != drug.getPreferences().getPreference("weekdays") ? drug.getPreferences().getPreference("weekdays") : "");
          ///  pillPrefRequest.put("scheduleFrequency", drug.getScheduledFrequency());
          /*  if (drug.getIsActionDateRequired()) {
                pillPrefRequest.put("actionDate", Util.convertDateLongToIso(drug.getActionDate()));
            }
            pillPrefRequest.put("recordDate", drug.getRecordDate());
            if (("EditPill").equalsIgnoreCase(action))
                if (drug.getSchedule().getDailyLimit() == 0) {
                    pillPrefRequest.put("maxNumDailyDoses", "-1");
                } else {
                    pillPrefRequest.put("maxNumDailyDoses", drug.getSchedule().getDailyLimit());
                } */

            pillRequest.put("preferences", pillPrefRequest);
            pillpopperRequest.put("pillpopperRequest", pillRequest);

            PillpopperLog.say("--Adding log entry for action : " + action + " is : " + pillpopperRequest.toString());

        } catch (JSONException e) {
            PillpopperLog.say("Oops!, Exception while preparing the log entry model object", e);
        }
        logEntryModel.setEntryJSONObject(pillpopperRequest, _thisActivity);
        return logEntryModel;
    }

    public static LogEntryModel prepareLogEntryForAction(String action, Drug drug, PillpopperActivity _thisActivity, PillpopperTime scheduledTime) {
        LogEntryModel logEntryModel = new LogEntryModel();
        logEntryModel.setDateAdded(System.currentTimeMillis());
        String replyId = getRandomGuid();
        logEntryModel.setReplyID(replyId);
        logEntryModel.setAction(action);

        JSONObject pillpopperRequest = new JSONObject();
        JSONObject pillRequest = new JSONObject();
        JSONObject pillPrefRequest = new JSONObject();
        String scheduleDate = FrontController.getInstance(_thisActivity).getScheduleDateFromHistory(drug.getGuid());
        PillpopperTime drugNotifyAfterTime = (FrontController.getInstance(_thisActivity).getNotifyAfterValue(drug.getGuid()));

        try {
            pillRequest.put("apiVersion", "6.0.4");
            pillRequest.put("action", action);
            pillRequest.put("replayId", replyId);
            pillRequest.put("language", Util.getLanguage());
            pillRequest.put("deviceToken", "");

            DoseEventCollection doseEventCollection = new DoseEventCollection(_thisActivity, drug, scheduledTime, 60);
            if (doseEventCollection.getNextEvent() != null && doseEventCollection.getNextEvent().getDate() != null) {

                if (FrontController.getInstance(_thisActivity).isLastActionEventPostpone(drug.getGuid())) {
                    if (drugNotifyAfterTime.after(doseEventCollection.getNextEvent().getDate())) {
                        if (scheduleDate != null) {
                            if (null != Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(scheduleDate)))
                                pillRequest.put("notify_after",
                                        FrontController.getInstance(_thisActivity).updateNotifyAfterValue(drug.getGuid(), doseEventCollection.getNextEvent().getDate().getGmtSeconds(), Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(scheduleDate)).getGmtSeconds()));
                        } else {
                            pillRequest.put("notify_after", doseEventCollection.getNextEvent().getDate().getGmtSeconds());
                            FrontController.getInstance(_thisActivity).updateNotifyAfterValue(drug.getGuid(), doseEventCollection.getNextEvent().getDate().getGmtSeconds());
                        }
                    }
                } else {
                    if (scheduleDate != null) {
                        pillRequest.put("notify_after",
                                FrontController.getInstance(_thisActivity).updateNotifyAfterValue(drug.getGuid(), doseEventCollection.getNextEvent().getDate().getGmtSeconds(), Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(scheduleDate)).getGmtSeconds()));
                    } else {
                        pillRequest.put("notify_after", doseEventCollection.getNextEvent().getDate().getGmtSeconds());
                        FrontController.getInstance(_thisActivity).updateNotifyAfterValue(drug.getGuid(), doseEventCollection.getNextEvent().getDate().getGmtSeconds());
                    }
                }
            }

            if (null != scheduleDate) {
                scheduleDate = Util.convertDateIsoToLong(scheduleDate);
                if (null != Util.convertStringtoPillpopperTime(scheduleDate)) {
                    pillRequest.put("scheduleDate", Util.convertStringtoPillpopperTime(scheduleDate).getGmtSeconds());
                    pillRequest.put("last_taken", Util.convertStringtoPillpopperTime(scheduleDate).getGmtSeconds());
                }
            } else {
                pillRequest.put("last_taken", -1);
            }

//            pillRequest.put("dose", drug.getDose());
            pillRequest.put("name", drug.getName());
           // pillRequest.put("type", getScheduleType(drug.getSchedule().getSchedType()));
            pillRequest.put("clientVersion", getAppVersion(_thisActivity));
            pillRequest.put("partnerId", PillpopperConstants.PARTNER_ID);
            pillRequest.put("pillId", drug.getGuid());
            String scheduleChoice = Util.getScheduleChoice(drug);
            pillRequest.put("start", PillpopperDay.marshalLocalDayAsGMTTime(PillpopperDay.PartOfDay.DayStart, scheduleChoice, drug.getSchedule(), drug.getSchedule().getStart()));
            pillRequest.put("end", PillpopperDay.marshalLocalDayAsGMTTime(PillpopperDay.PartOfDay.DayEnd, scheduleChoice,drug.getSchedule(), drug.getSchedule().getEnd()));
            pillRequest.put("interval", 0);

            if (!PillpopperConstants.ACTION_EDIT_PILL.equalsIgnoreCase(action)) {
                pillRequest.put("opId", drug.getOpID());
            }

            if (null != drug.get_effLastTaken()) {
                pillRequest.put("eff_last_taken", drug.get_effLastTaken().getGmtSeconds());
            } else {
                pillRequest.put("eff_last_taken", -1);
            }

            if (action.equalsIgnoreCase(PillpopperConstants.ACTION_POST_PONE_PILL)) {
                if (drug.getPostponeSeconds() != 0) {
                    PillpopperTime postponedTime = new PillpopperTime(calculateUpdatedSchedule(drug.getPostponeSeconds()));
                    if (postponedTime.before(doseEventCollection.getNextEvent().getDate())
                            || postponedTime.getGmtSeconds() == doseEventCollection.getNextEvent().getDate().getGmtSeconds()) {
                        pillRequest.put("seconds", drug.getPostponeSeconds());
                        if (scheduleDate != null) {
                            pillRequest.put("notify_after",
                                    FrontController.getInstance(_thisActivity).updateNotifyAfterValue(drug.getGuid(), calculateUpdatedSchedule(drug.getPostponeSeconds()), Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(scheduleDate)).getGmtSeconds()));
                        } else {
                            pillRequest.put("notify_after", calculateUpdatedSchedule(drug.getPostponeSeconds()));
                            FrontController.getInstance(_thisActivity).updateNotifyAfterValue(drug.getGuid(), calculateUpdatedSchedule(drug.getPostponeSeconds()));
                        }
                    }
                }
            }

            pillRequest.put("hardwareId", UniqueDeviceId.getHardwareId(_thisActivity));
            String primaryMemberUserId = FrontController.getInstance(_thisActivity).getPrimaryUserIdIgnoreEnabled();
            pillRequest.put("userId", primaryMemberUserId);
            pillRequest.put("targetUserId", drug.getUserID());

            pillRequest.put("schedule", FrontController.getInstance(_thisActivity).getSchedulesInTimeFormateByPillId(drug.getGuid()));

            if (drug.isScheduleAddedOrUpdated()) {
                pillRequest.put("isScheduleAddedOrUpdated", true);
            } else {
                pillRequest.put("isScheduleAddedOrUpdated", false);
            }

            pillRequest.put("dayperiod", drug.getSchedule().getDayPeriod());
            pillPrefRequest.put("customDosageID", null != drug.getPreferences().getPreference("customDosageID") ? drug.getPreferences().getPreference("customDosageID") : "");
            pillPrefRequest.put("refillsRemaining", "-1");
            pillPrefRequest.put("personId", drug.getUserID());
            pillPrefRequest.put("dosageType", drug.getPreferences().getPreference("dosageType"));
            pillPrefRequest.put("invisible", drug.getPreferences().getPreference("invisible"));
            pillPrefRequest.put("archived", drug.getPreferences().getPreference("archived"));
            pillPrefRequest.put("refillAlertDoses", null != drug.getPreferences().getPreference("refillAlertDoses") ? drug.getPreferences().getPreference("refillAlertDoses") : "");
            pillPrefRequest.put("notes", null != drug.getPreferences().getPreference("notes") ? drug.getPreferences().getPreference("notes") : "");
            pillPrefRequest.put("refillQuantity", null != drug.getPreferences().getPreference("refillQuantity") ? drug.getPreferences().getPreference("refillQuantity") : "");
            pillPrefRequest.put("customDescription", null != drug.getPreferences().getPreference("customDescription") ? drug.getPreferences().getPreference("customDescription") : "");
            pillPrefRequest.put("remainingQuantity", null != drug.getPreferences().getPreference("remainingQuantity") ? drug.getPreferences().getPreference("remainingQuantity") : "");
            pillPrefRequest.put("lastManagedIdNotified", null != drug.getPreferences().getPreference("lastManagedIdNotified") ? drug.getPreferences().getPreference("lastManagedIdNotified") : "");
            pillPrefRequest.put("lastManagedIdNeedingNotify", null != drug.getPreferences().getPreference("lastManagedIdNeedingNotify") ? drug.getPreferences().getPreference("lastManagedIdNeedingNotify") : "");
            pillPrefRequest.put("secondaryReminders", "1");
            pillPrefRequest.put("missedDosesLastChecked", FrontController.getInstance(_thisActivity).getMissedDosesLastCheckedValue(drug.getGuid()));
            pillPrefRequest.put("doctorCount", null != drug.getPreferences().getPreference("doctorCount") ? drug.getPreferences().getPreference("doctorCount") : "1");
            pillPrefRequest.put("imageGUID", null != drug.getPreferences().getPreference("imageGUID") ? drug.getPreferences().getPreference("imageGUID") : " ");
            pillPrefRequest.put("pharmacyCount", null != drug.getPreferences().getPreference("pharmacyCount") ? drug.getPreferences().getPreference("pharmacyCount") : "0");
            pillPrefRequest.put("prescriptionNum", null != drug.getPreferences().getPreference("prescriptionNum") ? drug.getPreferences().getPreference("prescriptionNum") : "");
            pillPrefRequest.put("weekdays", null != drug.getPreferences().getPreference("weekdays") ? drug.getPreferences().getPreference("weekdays") : "");
            pillPrefRequest.put("scheduleFrequency", drug.getScheduledFrequency());
            pillPrefRequest.put("scheduleChoice", scheduleChoice);
            pillPrefRequest.put("databaseNDC", null != drug.getPreferences().getPreference("databaseNDC") ? drug.getPreferences().getPreference("databaseNDC") : " ");
            if (("EditPill").equalsIgnoreCase(action))
                if (drug.getSchedule().getDailyLimit() == 0) {
                    pillPrefRequest.put("maxNumDailyDoses", "-1");
                } else {
                    pillPrefRequest.put("maxNumDailyDoses", drug.getSchedule().getDailyLimit());
                }

            pillRequest.put("preferences", pillPrefRequest);
            pillpopperRequest.put("pillpopperRequest", pillRequest);

            PillpopperLog.say("--Adding log entry for action : " + action + " is : " + pillpopperRequest.toString());

        } catch (JSONException e) {
            PillpopperLog.say("Oops!, Exception while preparing the log entry model object", e);
        }
        logEntryModel.setEntryJSONObject(pillpopperRequest, _thisActivity);
        return logEntryModel;
    }

    public static LogEntryModel prepareLogEntryForEditHistoryEvent(Context _thisActivity, HistoryEditEvent historyEditEvent,String currentAction) {
        LogEntryModel logEntryModel = new LogEntryModel();
        logEntryModel.setDateAdded(System.currentTimeMillis());
        String replyId = getRandomGuid();
        logEntryModel.setReplyID(replyId);

        JSONObject pillpopperRequest = new JSONObject();
        JSONObject pillRequest = new JSONObject();
        JSONObject pillPrefRequest = new JSONObject();

        try {

            pillRequest.put("apiVersion", "Version 6.0.4");
            pillRequest.put("hardwareId", UniqueDeviceId.getHardwareId(_thisActivity));
            pillRequest.put("action", PillpopperConstants.ACTION_EDIT_HISTORY_EVENT);
            String primaryMemberUserId = FrontController.getInstance(_thisActivity).getPrimaryUserIdIgnoreEnabled();
            pillRequest.put("userId", primaryMemberUserId);
            pillRequest.put("targetUserId", historyEditEvent.getPillUserId());
            pillRequest.put("replayId", replyId);
            pillRequest.put("scheduleDate", historyEditEvent.getPillScheduleDate().getGmtSeconds());
            pillRequest.put("partnerId", PillpopperConstants.PARTNER_ID);
            pillRequest.put("clientVersion", getAppVersion(_thisActivity));
            pillRequest.put("deviceToken", "");
            pillRequest.put("personId", historyEditEvent.getPillUserId());
            pillRequest.put("operation", historyEditEvent.getPillOperation());
            pillRequest.put("opId", historyEditEvent.getPillOperationId());
            pillRequest.put("language", Util.getLanguage());
            pillRequest.put("eventDescription", historyEditEvent.getPillEventDescription());
            pillRequest.put("creationDate", historyEditEvent.getPillHistoryCreationDate().getGmtSeconds());
            pillRequest.put("pillId", historyEditEvent.getPillId());
            pillRequest.put("tz_secs", String.valueOf(Util.getTzOffsetSecs(TimeZone.getDefault())));
            pillRequest.put("tz_name", TimeZone.getDefault().getDisplayName());


            if (historyEditEvent.getPillDosageType() != null) {
                pillPrefRequest.put("dosageType", historyEditEvent.getPillDosageType());
                if (("managed").equalsIgnoreCase(historyEditEvent.getPillDosageType())) {
                    pillPrefRequest.put("managedDescription", historyEditEvent.getPillDosage());
                } else {
                    pillPrefRequest.put("customDescription", historyEditEvent.getPillDosage());
                }
            }
            if (null != currentAction && currentAction.equalsIgnoreCase(PillpopperConstants.ACTION_POST_PONE_PILL)) {
                Drug drug = FrontController.getInstance(_thisActivity).getDrugByPillId(historyEditEvent.getPillId());
                pillPrefRequest.put("isPostponedEventActive", true);
                pillPrefRequest.put("finalPostponedDateTime", Util.convertDateLongToIso(Long.toString(drug.get_notifyAfter().getGmtSeconds())));
            } else if (null != currentAction && !currentAction.equalsIgnoreCase(PillpopperConstants.ACTION_POST_PONE_PILL)) {
                pillRequest.put("operation", PillpopperConstants.ACTION_POSTPONE_PILL_HISTORY);
                pillPrefRequest.put("isPostponedEventActive", false);
                pillPrefRequest.put("finalPostponedDateTime", historyEditEvent.getPreferences().getFinalPostponedDateTime());
            } else {
                String action = historyEditEvent.getPillOperation();
                if (null != action && action.equalsIgnoreCase(PillpopperConstants.ACTION_POSTPONE_PILL_HISTORY)) {
                    pillPrefRequest.put("isPostponedEventActive", false);
                    pillPrefRequest.put("finalPostponedDateTime", historyEditEvent.getPreferences().getFinalPostponedDateTime());
                }
            }

            pillPrefRequest.put("scheduleGuid",historyEditEvent.getPreferences().getScheduleGuid());
            if (historyEditEvent.isActionDateRequired()) {
                pillPrefRequest.put("actionDate", Util.convertDateLongToIso(historyEditEvent.getPreferences().getActionDate()));
            }
            pillPrefRequest.put("recordDate", historyEditEvent.getPreferences().getRecordDate());

            /*pillPrefRequest.put("scheduleChoice", historyEditEvent.getPreferences().getScheduleChoice());
            pillPrefRequest.put("dayperiod", historyEditEvent.getPreferences().getDayperiod());
            pillPrefRequest.put("start", historyEditEvent.getPreferences().getStart());
            pillPrefRequest.put("end", historyEditEvent.getPreferences().getEnd());
            pillPrefRequest.put("weekdays", null != historyEditEvent.getPreferences().getWeekdays()? historyEditEvent.getPreferences().getWeekdays() :"");
            pillPrefRequest.put("scheduledFrequency", historyEditEvent.getPreferences().getScheduleFrequency());*/

            pillRequest.put("preferences", pillPrefRequest);
            pillpopperRequest.put("pillpopperRequest", pillRequest);

        } catch (JSONException e) {
            PillpopperLog.say("LogEntry -- Exception while creating HistoryEditEvent JSON request", e);
        }

        logEntryModel.setEntryJSONObject(pillpopperRequest, _thisActivity);
        return logEntryModel;
    }

    private static String getScheduleType(Schedule.SchedType schedType) {

        if (schedType == Schedule.SchedType.INTERVAL) {
            return "interval";
        } else if (schedType == Schedule.SchedType.SCHEDULED) {
            return "scheduled";
        } else if (schedType == Schedule.SchedType.AS_NEEDED) {
            return "asneeded";
        }
        return "scheduled";
    }

    public static String getClientInfo(Context context) {
        return "AppId:" + AppConstants.APP_ID + "," + "AppVersion:" + ActivationUtil.getAppVersion(context) + "," + "DeviceId:" + ActivationUtil.getDeviceId(context);
    }


    /**
     * Prepares the Log entry Model for action "CreateHistoryEvent"
     * This entry needs to be called for actions like
     * "TakePill" , "PostponePill" , "SkipPill"
     *
     * @param action        action
     * @param drug          drug
     * @param _thisActivity application context
     * @return LogEntryModel
     */
    public static LogEntryModel prepareLogEntryForCreateHistoryEvent(String action, Drug drug, PillpopperActivity _thisActivity) {
        LogEntryModel logEntryModel = defaultLogEntry();
        JSONObject pillpopperObject = new JSONObject();
        JSONObject pillRequest = new JSONObject();
        JSONObject pillPrefRequest = new JSONObject();
        logEntryModel.setAction(action);

        try {
            String scheduleDate = FrontController.getInstance(_thisActivity).getScheduleDateFromHistory(drug.getGuid());
            if (null != scheduleDate) {
                scheduleDate = Util.convertDateIsoToLong(scheduleDate);
                PillpopperLog.say("-- Schedule date from db is " + scheduleDate);
                if (null != Util.convertStringtoPillpopperTime(scheduleDate))
                    pillRequest.put("scheduleDate", Util.convertStringtoPillpopperTime(scheduleDate).getGmtSeconds());
            }

            pillRequest.put("apiVersion", "Version 6.0.4");
            pillRequest.put("replayId", logEntryModel.getReplyID());
            pillRequest.put("hardwareId", UniqueDeviceId.getHardwareId(_thisActivity));

            String primaryMemberUserId = FrontController.getInstance(_thisActivity).getPrimaryUserIdIgnoreEnabled();
            pillRequest.put("userId", primaryMemberUserId);
            pillRequest.put("targetUserId", drug.getUserID());

            pillRequest.put("personId", drug.getUserID());

            if (null != FrontController.getInstance(_thisActivity).getEventDescriptionFromHistory(drug.getGuid()))
                pillRequest.put("eventDescription", FrontController.getInstance(_thisActivity).getEventDescriptionFromHistory(drug.getGuid()));
            pillRequest.put("language", Util.getLanguage());
            pillRequest.put("pillId", drug.getGuid());
            pillRequest.put("deviceToken", "");
            pillRequest.put("operation", getHistoryOperation(action));
            pillRequest.put("action", PillpopperConstants.ACTION_CREATE_HISTORY_EVENT);

            Map<String, String> tzParams = FrontController.getInstance(_thisActivity).getCreationTimeZoneFromHistory(drug.getGuid());
            pillRequest.put("tz_secs", tzParams == null ? null : tzParams.get("tz_secs"));
            pillRequest.put("tz_name", tzParams == null ? null : tzParams.get("tz_name"));

            String creationDate = FrontController.getInstance(_thisActivity).getCreationDateByScheduleDateFromHistory(drug.getGuid(), scheduleDate);
            if (isEmptyString(creationDate)) {
                creationDate = FrontController.getInstance(_thisActivity).getCreationDateFromHistory(drug.getGuid());
            }

            if (null != creationDate) {
                if (action.equalsIgnoreCase(PillpopperConstants.ACTION_POST_PONE_PILL)) {
                    // PostPone Action, Needs to consider CreationDate as CreationDate
                    pillRequest.put("creationDate", Util.convertStringtoPillpopperTime(creationDate).getGmtSeconds());
                } else if (action.equalsIgnoreCase(PillpopperConstants.ACTION_SKIP_PILL)) {
                    pillRequest.put("creationDate", Util.convertStringtoPillpopperTime(creationDate).getGmtSeconds());
                } else if (action.equalsIgnoreCase(PillpopperConstants.ACTION_TAKE_PILL)) {

                    if (null != scheduleDate) {
                        // For Take And Skip Actions of Current Reminders, Needs to Consider the schedule
                        if (Util.convertStringtoPillpopperTime(creationDate).getGmtSeconds() < Util.convertStringtoPillpopperTime(scheduleDate).getGmtSeconds()) {
                            // Taken Earlier Action
                            pillRequest.put("creationDate", Util.convertStringtoPillpopperTime(creationDate).getGmtSeconds());
                        } else {
                            // Current Reminders Taken/Skip Action
                            pillRequest.put("creationDate", Util.convertStringtoPillpopperTime(scheduleDate).getGmtSeconds());
                        }
                    } else {
                        // This is the fallback mechanism
                        pillRequest.put("creationDate", Util.convertStringtoPillpopperTime(creationDate).getGmtSeconds());
                    }
                } else if (action.equalsIgnoreCase(PillpopperConstants.ACTION_TAKEN_EARLIER)) {
                    pillRequest.put("creationDate", Util.convertStringtoPillpopperTime(creationDate).getGmtSeconds());
                } else if (action.equalsIgnoreCase(PillpopperConstants.ACTION_MISS_PILL)) {
                    // MissPill
                    if (null != scheduleDate) {
                        pillRequest.put("creationDate", Util.convertStringtoPillpopperTime(scheduleDate).getGmtSeconds());
                    }
                }
            }

            if (null != action && action.equalsIgnoreCase(PillpopperConstants.ACTION_POSTPONE_PILL_HISTORY)) {
                if (drug.getPostponeSeconds() != 0) {
                    pillRequest.put("operationData", drug.getPostponeSeconds());
                }
            }

            pillRequest.put("opId", drug.getOpID());
            pillRequest.put("partnerId", PillpopperConstants.PARTNER_ID);
            pillPrefRequest.put("scheduleGuid", drug.getScheduleGuid());
            pillPrefRequest.put("dosageType", drug.isManaged()? "managed" : "custom");
            pillPrefRequest.put("customDescription", drug.getPreferences().getPreference("customDescription"));
            pillPrefRequest.put("remainingQuantity", drug.getPreferences().getPreference("remainingQuantity"));
            pillPrefRequest.put("missedDosesLastChecked", drug.getPreferences().getPreference("missedDosesLastChecked"));

            /*pillPrefRequest.put("scheduleDate",Util.convertDateLongToIso(scheduleDate));
            pillPrefRequest.put("scheduleFrequency", drug.getScheduledFrequency());
            pillPrefRequest.put("weekdays", drug.getPreferences().getPreference("weekdays"));
            pillPrefRequest.put("start", conpreparevertDateLongToIso(String.valueOf(convertDateLongToIso(String.valueOf(PillpopperDay.marshalLocalDayAsGMTTime(PillpopperDay.PartOfDay.DayStart, drug.getSchedule(), drug.getSchedule().getStart()))))));
            pillPrefRequest.put("end", convertDateLongToIso(String.valueOf(convertDateLongToIso(String.valueOf(PillpopperDay.marshalLocalDayAsGMTTime(PillpopperDay.PartOfDay.DayEnd, drug.getSchedule(), drug.getSchedule().getEnd()))))));
            pillPrefRequest.put("scheduleChoice", drug.getPreferences().getPreference("scheduleChoice"));*/

            pillRequest.put("tz_secs", tzParams == null ? null : tzParams.get("tz_secs"));

            pillRequest.put("preferences", pillPrefRequest);
            pillpopperObject.put("pillpopperRequest", pillRequest);

            logEntryModel.setEntryJSONObject(pillpopperObject, _thisActivity);

        } catch (JSONException e) {
            PillpopperLog.say("Oops!, Exception while preparing the log entry model object for craeteHistoryEvent", e);
        }
        return logEntryModel;
    }

    public static LogEntryModel prepareLogEntryForCreateHistoryEvent(String action, Drug drug, Context context) {
        LogEntryModel logEntryModel = defaultLogEntry();
        JSONObject pillpopperObject = new JSONObject();
        JSONObject pillRequest = new JSONObject();
        JSONObject pillPrefRequest = new JSONObject();
        logEntryModel.setAction(action);

        try {
            String scheduleDate = FrontController.getInstance(context).getScheduleDateFromHistory(drug.getGuid());
            if (null != scheduleDate) {
                PillpopperLog.say("-- Schedule date from db is " + scheduleDate);
                if (null != Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(scheduleDate)))
                    pillRequest.put("scheduleDate", Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(scheduleDate)).getGmtSeconds());
            }

            pillRequest.put("apiVersion", "Version 6.0.4");
            pillRequest.put("replayId", logEntryModel.getReplyID());
            pillRequest.put("hardwareId", UniqueDeviceId.getHardwareId(context));

            String primaryMemberUserId = FrontController.getInstance(context).getPrimaryUserIdIgnoreEnabled();
            pillRequest.put("userId", primaryMemberUserId);
            pillRequest.put("targetUserId", drug.getUserID());

            pillRequest.put("personId", drug.getUserID());

            pillRequest.put("eventDescription", drug.getName() + " " + getHistoryOperation(action));
            pillRequest.put("language", Util.getLanguage());
            pillRequest.put("pillId", drug.getGuid());
            pillRequest.put("deviceToken", "");
            pillRequest.put("operation", getHistoryOperation(action));
            pillRequest.put("action", PillpopperConstants.ACTION_CREATE_HISTORY_EVENT);

            String creationDate = FrontController.getInstance(context).getCreationDateByScheduleDateFromHistory(drug.getGuid(), scheduleDate);
            if (!isEmptyString(creationDate)) {
                pillRequest.put("creationDate", Util.convertStringtoPillpopperTime(creationDate).getGmtSeconds());
            } else {
                creationDate = FrontController.getInstance(context).getCreationDateFromHistory(drug.getGuid());
                if (null != Util.convertStringtoPillpopperTime(creationDate))
                    pillRequest.put("creationDate", Util.convertStringtoPillpopperTime(creationDate).getGmtSeconds());
            }

            if (null != action && action.equalsIgnoreCase(PillpopperConstants.ACTION_POSTPONE_PILL_HISTORY)) {
                if (drug.getPostponeSeconds() != 0) {
                    pillRequest.put("operationData", drug.getPostponeSeconds());
                    // add isPostPoneActive and finalPostponeDate
                    pillPrefRequest.put("isPostponedEventActive", true);
                    pillPrefRequest.put("finalPostponedDateTime",
                            Util.convertDateLongToIso(Long.toString(drug._getPostponeTime(drug.getPostponeSeconds()).getGmtSeconds())));
                }
            }

            pillRequest.put("opId", drug.getOpID());
            pillRequest.put("partnerId", PillpopperConstants.PARTNER_ID);
            Map<String, String> tzParams = FrontController.getInstance(context).getCreationTimeZoneFromHistory(drug.getGuid());
            pillRequest.put("tz_secs", tzParams == null ? null : tzParams.get("tz_secs"));
            pillRequest.put("tz_name", tzParams == null ? null : tzParams.get("tz_name"));
            pillPrefRequest.put("dosageType", drug.isManaged() ? "managed" : "custom");
            pillPrefRequest.put(drug.isManaged() ? "managedDescription" :"customDescription" ,
                    drug.isManaged() ? drug.getPreferences().getPreference("managedDescription") :drug.getPreferences().getPreference("customDescription"));

            if (drug.getIsActionDateRequired()) {
                pillPrefRequest.put("actionDate", Util.convertDateLongToIso(String.valueOf(null != drug.getActionDate()
                        ? drug.getActionDate()
                        : PillpopperTime.now().getGmtSeconds())));
            }
            pillPrefRequest.put("scheduleGuid", drug.getScheduleGuid());
            pillPrefRequest.put("recordDate", drug.getRecordDate());
            /*
            pillPrefRequest.put("dayperiod", drug.getSchedule().getDayPeriod());
            pillPrefRequest.put("start", convertDateLongToIso(String.valueOf(PillpopperDay.marshalLocalDayAsGMTTime(PillpopperDay.PartOfDay.DayStart, drug.getSchedule(), drug.getSchedule().getStart()))));
            pillPrefRequest.put("end", convertDateLongToIso(String.valueOf(PillpopperDay.marshalLocalDayAsGMTTime(PillpopperDay.PartOfDay.DayEnd, drug.getSchedule(), drug.getSchedule().getEnd()))));*/
            String dosageType = drug.getPreferences().getPreference("dosageType");
            pillPrefRequest.put("dosageType", !Util.isEmptyString(dosageType) ? dosageType : PillpopperConstants.DOSAGE_TYPE_CUSTOM);
            /*pillPrefRequest.put("weekdays", null != drug.getPreferences().getPreference("weekdays")? drug.getPreferences().getPreference("weekdays") :"");
            pillPrefRequest.put("scheduleFrequency", drug.getScheduledFrequency());
            pillPrefRequest.put("scheduleChoice", drug.getPreferences().getPreference("scheduleChoice"));*/

            pillRequest.put("tz_secs", tzParams == null ? null : tzParams.get("tz_secs"));
            pillRequest.put("preferences", pillPrefRequest);
            pillpopperObject.put("pillpopperRequest", pillRequest);

            logEntryModel.setEntryJSONObject(pillpopperObject, context);

        } catch (JSONException e) {
            PillpopperLog.say("Oops!, Exception while preparing the log entry model object for createHistoryEvent", e);
        }
        return logEntryModel;
    }

    public static LogEntryModel prepareLogEntryForCreateHistoryEvent_pastReminders(String action, Drug drug, Context _thisActivity) {
        LogEntryModel logEntryModel = defaultLogEntry();
        JSONObject pillpopperObject = new JSONObject();
        JSONObject pillRequest = new JSONObject();
        JSONObject pillPrefRequest = new JSONObject();
        logEntryModel.setAction(action);

        try {
            // String scheduleDate = FrontController.getInstance(_thisActivity).getScheduleDateFromHistory(drug.getGuid());

            pillRequest.put("scheduleDate", drug.getScheduledTime().getGmtSeconds());

            pillRequest.put("apiVersion", "Version 6.0.4");
            pillRequest.put("replayId", logEntryModel.getReplyID());
            pillRequest.put("hardwareId", UniqueDeviceId.getHardwareId(_thisActivity));

            String primaryMemberUserId = FrontController.getInstance(_thisActivity).getPrimaryUserIdIgnoreEnabled();
            pillRequest.put("userId", primaryMemberUserId);
            pillRequest.put("targetUserId", drug.getUserID());

            pillRequest.put("personId", drug.getUserID());

            pillRequest.put("eventDescription", drug.getName() + " " + getHistoryOperation(action));

            pillRequest.put("language", Util.getLanguage());
            pillRequest.put("pillId", drug.getGuid());
            pillRequest.put("deviceToken", "");
            pillRequest.put("operation", getHistoryOperation(action));
            pillRequest.put("action", PillpopperConstants.ACTION_CREATE_HISTORY_EVENT);

            if (PillpopperConstants.ACTION_SKIP_PILL.equalsIgnoreCase(action) || PillpopperConstants.ACTION_SKIP_ALL_PILL.equalsIgnoreCase(action)) {
                pillRequest.put("creationDate", PillpopperTime.now().getGmtSeconds());
            } else {
                pillRequest.put("creationDate", drug.getScheduledTime().getGmtSeconds());
            }

            if (null != action && action.equalsIgnoreCase(PillpopperConstants.ACTION_POSTPONE_PILL_HISTORY)) {
                if (drug.getPostponeSeconds() != 0) {
                    pillRequest.put("operationData", drug.getPostponeSeconds());
                }
            }

            pillRequest.put("opId", drug.getOpID());
            pillRequest.put("partnerId", PillpopperConstants.PARTNER_ID);
            Map<String, String> tzParams = FrontController.getInstance(_thisActivity).getCreationTimeZoneFromHistory(drug.getGuid());
            pillRequest.put("tz_secs", tzParams == null ? null : tzParams.get("tz_secs"));
            pillRequest.put("tz_name", tzParams == null ? null : tzParams.get("tz_name"));
            pillPrefRequest.put("scheduleGuid", drug.getScheduleGuid());
            pillPrefRequest.put("dosageType", drug.isManaged() ? "managed" : "custom");
            pillPrefRequest.put(drug.isManaged() ? "managedDescription" :"customDescription",
                    drug.isManaged() ? drug.getPreferences().getPreference("managedDescription") :drug.getPreferences().getPreference("customDescription"));

            /*Drug newDrug = FrontController.getInstance(_thisActivity).getDrugByPillId(drug.getGuid());
            pillPrefRequest.put("scheduleFrequency", newDrug.getScheduledFrequency());
            pillPrefRequest.put("dayperiod", drug.getSchedule().getDayPeriod());
            pillPrefRequest.put("scheduleDate", null != drug.getOverdueDate() ? convertDateLongToIso(String.valueOf(drug.getOverdueDate())) : drug.getScheduledTime().getGmtSeconds());
            pillPrefRequest.put("weekdays", null != drug.getSchedule().getDays() ? drug.getSchedule().getDays() : "");
            pillPrefRequest.put("start", convertDateLongToIso(String.valueOf(convertDateLongToIso(String.valueOf(PillpopperDay.marshalLocalDayAsGMTTime(PillpopperDay.PartOfDay.DayStart, drug.getSchedule(), drug.getSchedule().getStart()))))));
            pillPrefRequest.put("end", convertDateLongToIso(String.valueOf(convertDateLongToIso(String.valueOf(PillpopperDay.marshalLocalDayAsGMTTime(PillpopperDay.PartOfDay.DayEnd, drug.getSchedule(), drug.getSchedule().getEnd()))))));
            pillPrefRequest.put("scheduleChoice", drug.getPreferences().getPreference("scheduleChoice"));*/

            pillRequest.put("tz_secs", tzParams == null ? null : tzParams.get("tz_secs"));
            pillRequest.put("preferences", pillPrefRequest);
            pillpopperObject.put("pillpopperRequest", pillRequest);
            if (drug.getIsActionDateRequired()) {
                pillPrefRequest.put("actionDate", Util.convertDateLongToIso(drug.getActionDate()));
            }
            pillPrefRequest.put("recordDate", drug.getRecordDate());
            logEntryModel.setEntryJSONObject(pillpopperObject, _thisActivity);

        } catch (JSONException e) {
            PillpopperLog.say("Oops!, Exception while preparing the log entry model object for craeteHistoryEvent", e);
        }
        return logEntryModel;
    }

    private static String getHistoryOperation(String action) {
        if (action.equalsIgnoreCase(PillpopperConstants.ACTION_POSTPONE_PILL_HISTORY)) {
            return PillpopperConstants.ACTION_POSTPONE_PILL_HISTORY;
        } else if (action.equalsIgnoreCase(PillpopperConstants.ACTION_SKIP_PILL_HISTORY)) {
            return PillpopperConstants.ACTION_SKIP_PILL_HISTORY;
        } else if (action.equalsIgnoreCase(PillpopperConstants.ACTION_MISS_PILL)) {
            return PillpopperConstants.ACTION_MISS_PILL_HISTORY;
        }
        return PillpopperConstants.ACTION_TAKE_PILL_HISTORY;
    }

    /**
     * Prepares the LogEntry for delete medication action
     *
     * @param drug          drug
     * @param _thisActivity application context
     * @return LogEntryModel
     */
    public static LogEntryModel prepareLogEntryForDelete(Drug drug, PillpopperActivity _thisActivity) {

        LogEntryModel logEntryModel = defaultLogEntry();
        JSONObject pillpopperRequest = new JSONObject();
        JSONObject request = new JSONObject();
        try {
            request.put("apiVersion", "Version 6.0.4");
            request.put("hardwareId", UniqueDeviceId.getHardwareId(_thisActivity));
            request.put("action", "DeletePill");
            request.put("replayId", logEntryModel.getReplyID());
            request.put("clientVersion", getAppVersion(_thisActivity));
            request.put("partnerId", PillpopperConstants.PARTNER_ID);
            request.put("deleteHistory", 1);
            request.put("deviceToken", "");
            request.put("pillId", drug.getGuid());

            String primaryMemberUserId = FrontController.getInstance(_thisActivity).getPrimaryUserIdIgnoreEnabled();
            request.put("userId", primaryMemberUserId);
            request.put("targetUserId", drug.getUserID());

            pillpopperRequest.put("pillpopperRequest", request);
            PillpopperLog.say("-- Delete Action Request  : " + pillpopperRequest.toString());
        } catch (JSONException e) {
            LoggerUtils.exception("JSONException", e);
        }
        logEntryModel.setEntryJSONObject(pillpopperRequest, _thisActivity);
        return logEntryModel;
    }

    /*prepares log entry for settings action*/
    public static JSONObject prepareSettingsAction(JSONObject preferences, String repyId, String userId, Context context) {

        JSONObject pillpopperRequest = new JSONObject();
        try {
            JSONObject pillPopperRequest = new JSONObject();

            pillPopperRequest.put("action", "SetPreferences");
            pillPopperRequest.put("clientVersion", Util.getAppVersion(context));
            pillPopperRequest.put("preferences", preferences);
            pillPopperRequest.put("userId", userId);
            pillPopperRequest.put("partnerId", PillpopperConstants.PARTNER_ID);
            pillPopperRequest.put("language", Util.getLanguage());
            pillPopperRequest.put("hardwareId", UniqueDeviceId.getHardwareId(context));
            pillPopperRequest.put("targetUserId", userId);
            pillPopperRequest.put("apiVersion", "Version 6.0.4");
            pillPopperRequest.put("deviceToken", "");
            pillPopperRequest.put("replayId", repyId);
            pillpopperRequest.put("pillpopperRequest", pillPopperRequest);

        } catch (JSONException e) {
            LoggerUtils.exception("JSONException", e);
        }

        return pillpopperRequest;
    }

    /**
     * Prepares the log partial log entry object with default params.
     *
     * @return LogEntryModel
     */
    public static LogEntryModel defaultLogEntry() {
        LogEntryModel logEntry = new LogEntryModel();
        logEntry.setDateAdded(System.currentTimeMillis());
        logEntry.setReplyID(getRandomGuid());
        return logEntry;
    }

    public static JSONObject prepareGetState(Context context) {
        JSONObject getStateJsonObject = new JSONObject();
        JSONObject getStatePrefJsonObject = new JSONObject();
        JSONArray requestArray = new JSONArray();
        JSONObject request = new JSONObject();
        SharedPreferenceManager mSharedPrefManager = SharedPreferenceManager.getInstance(context, AppConstants.AUTH_CODE_PREF_NAME);

        try {

            String deviceID = UniqueDeviceId.getHardwareId(context);
            getStateJsonObject.put("hardwareId", deviceID);
            getStatePrefJsonObject.put("deviceName", Util.getDeviceMake());
            getStatePrefJsonObject.put("osVersion", Util.getOSVersion());
            getStatePrefJsonObject.put("userData", mSharedPrefManager.getString(AppConstants.KP_GUID, ""));
            getStateJsonObject.put("preferences", getStatePrefJsonObject);
            getStateJsonObject.put("partnerId", PillpopperConstants.PARTNER_ID);
            getStateJsonObject.put("clientVersion", Util.getAppVersion(context));
            getStateJsonObject.put("replayId", Util.getRandomGuid());
            getStateJsonObject.put("deviceId", deviceID);

            for (User user : FrontController.getInstance(context).getAllEnabledUsers()) {
                JSONObject userJsonObject = new JSONObject();
                userJsonObject.put("userGUID", user.getUserId());
                userJsonObject.put("lastSyncToken",
                        Util.getLastSyncTokenValue(FrontController.getInstance(context).getLastSyncTokenForUser(user.getUserId())));
                requestArray.put(userJsonObject);
                getStateJsonObject.put("proxyUserList", requestArray);
            }
            getStateJsonObject.put("userId", FrontController.getInstance(context).getPrimaryUserIdIgnoreEnabled());
            getStateJsonObject.put("action", "GetState");
            getStateJsonObject.put("language", Util.getLanguage());
            getStateJsonObject.put("appVersion", Util.getAppVersion(context));
            getStateJsonObject.put("apiVersion", "Version 6.0.4");
            request.put("pillpopperRequest", getStateJsonObject);
        } catch (JSONException e) {
            LoggerUtils.exception("JSONException", e);
        }

        PillpopperLog.say("-- Adding getState obj : " + request.toString());
        return request;
    }

    public static boolean isValidDrugForReminders(Drug d) {
        String isdeleted = d.getPreferences().getPreference("deleted");
        String isArchived = d.getPreferences().getPreference("archived");
        String isManagedropped = d.getPreferences().getPreference("managedDropped");
        // get Reminders toggle should be enabled, and it should be not deleted or archived or discontinued
        return ((null != d.getIsRemindersEnabled() && ("Y").equalsIgnoreCase(d.getIsRemindersEnabled()))
              && (!((null != isdeleted && ("1").equalsIgnoreCase(isdeleted)) ||
                (null != isArchived && ("1").equalsIgnoreCase(isArchived)) ||
                (null != isManagedropped && ("1").equalsIgnoreCase(isManagedropped)))));
    }


    public static List<Drug> getDrugListForAction(Context context) {
        List<Drug> drugList = new ArrayList<>();
        for (final Drug d : FrontController.getInstance(context).getDrugListForOverDue(context)) {
            // filter and consider only the drugs of users enabled with reminders
            if (Util.isValidDrugForReminders(d)) {
                d.computeDBDoseEvents(context, d, PillpopperTime.now(), 60);
                drugList.add(d);
                LoggerUtils.info("Debug Drug - " + d.getName());
            }
        }
        return drugList;
    }

    public static String getFormatted12HourTime(int hours, int minutes) {
        if (hours >= 0 && hours < 12) {
            if (hours == 0) {
                hours = 12;
            }
            return String.format("%d:%02d " + Util.getSystemAMFormat(), hours, minutes);
        } else if (hours > 12 && hours <= 23) {
            hours = hours - 12;
            return String.format("%d:%02d " + Util.getSystemPMFormat(), hours, minutes);
        }
        return String.format("%d:%02d " + Util.getSystemPMFormat(), hours, minutes);
    }

    public static boolean isEmptyString(String string) {

        if (string == null) {
            return true;
        }
        return ("").equalsIgnoreCase(string.trim())
                || ("null").equalsIgnoreCase(string.trim())
                || string.length() == 0;

    }

    /**
     * Method will return the OS Version of the device.
     *
     * @return String returns string
     */
    public static String getOSVersion() {
        return android.os.Build.VERSION.RELEASE;
    }

    /**
     * Method will return the Device Model information.
     *
     * @return String returns string
     */
    public static String getDeviceMake() {
        return android.os.Build.MODEL;
    }

    /**
     * Method will return the application version.
     *
     * @return app version
     */
    public static String getAppVersion(Context context) {
        try {
            if (null != context) {
                PackageInfo pInfo = context.getPackageManager().getPackageInfo(
                        context.getPackageName(), 0);
                return pInfo.versionName;
            }

        } catch (NameNotFoundException e) {
            LoggerUtils.exception("NameNotFoundException", e);
        }
        return BuildConfig.VERSION_NAME;

    }


    private LinkedHashMap<Long, List<Drug>> currentRemindersMap;
    private LinkedHashMap<Long, List<Drug>> passedRemindersMap = new LinkedHashMap<>();
    private List<String> users = new ArrayList<>();
    private Context context;
    private List<Long> listOfTimes = new ArrayList<>();


    public synchronized void prepareRemindersMapData(List<Drug> overdueDrugs, Context _thisActivity) {
        setContext(_thisActivity);
        LinkedHashMap<Long, LinkedHashMap<String, List<Drug>>> drugListMapByTime = new LinkedHashMap<>();
        sortDrugTimes(getContext(), overdueDrugs);
        drugListMapByTime = prepareDataByUsers(overdueDrugs);
        LinkedHashMap<Long, List<Drug>> tempDrugListHolder = new LinkedHashMap<>();
        LinkedHashMap<Long, List<Drug>> drugListByTime = getDrugListByTimeMap(drugListMapByTime);
        setContext(_thisActivity);

        setCurrentRemindersMap(new LinkedHashMap<>());
        setPassedRemindersMap(new LinkedHashMap<>());

        if (null != drugListByTime && !drugListByTime.isEmpty()) {
            if (!drugListByTime.keySet().isEmpty()) {
                long firstIndexKey = drugListByTime.keySet().iterator().next();

                String isPendingPastRemindersAvailable = FrontController.getInstance(getContext()).getPendingRemindersStatus(getContext());

                for (Map.Entry<Long, List<Drug>> druglistEntry : drugListByTime.entrySet()) {

                    if (firstIndexKey == druglistEntry.getKey()
                            && ((("1").equalsIgnoreCase(isPendingPastRemindersAvailable) && !isEntryInPastReminderTable(druglistEntry.getKey(), _thisActivity))
                            || ("0").equalsIgnoreCase(isPendingPastRemindersAvailable))) {
                        List<Drug> listForCurrentReminders = new ArrayList<>();
                        List<Drug> listOfPastReminders = new ArrayList<>();
                        for (Drug drug : druglistEntry.getValue()) {
//                            if (!FrontController.getInstance(getContext()).isHistoryEventAvailable(new PillpopperTime(druglistEntry.getKey() / 1000), drug.getGuid())) {
                            if (FrontController.getInstance(getContext()).isEntryAvailableInPastReminder(drug.getGuid(), new PillpopperTime(druglistEntry.getKey() / 1000))) {
                                if ((PillpopperTime.now().getGmtSeconds() - druglistEntry.getKey() / 1000) < 24 * 60 * 60) {
                                    listOfPastReminders.add(drug);
                                }
                            } else {
                                if (!FrontController.getInstance(getContext()).isHistoryEventAvailable(new PillpopperTime(druglistEntry.getKey() / 1000), drug.getGuid())) {
                                    listForCurrentReminders.add(drug);
                                }
                            }
//                            }
                        }

                        if (!listForCurrentReminders.isEmpty()) {
                            List<Drug> finalListForCurrentReminders = listForCurrentReminders;
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    FrontController.getInstance(context).createEntryInHistoryForScheduleTime(finalListForCurrentReminders, AppConstants.HISTORY_OPERATION_EMPTY, new PillpopperTime(druglistEntry.getKey() / 1000));
                                }
                            });

                            listForCurrentReminders = checkForRemindersEnabled(listForCurrentReminders);
                            if (!listForCurrentReminders.isEmpty()) {
                                getCurrentRemindersMap().put(druglistEntry.getKey(), listForCurrentReminders);
                            }
                        }
                        if (!listOfPastReminders.isEmpty()) {
                            List<Drug> finalListOfPastReminders = listOfPastReminders;
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    FrontController.getInstance(context).createEntryInHistoryForScheduleTime(finalListOfPastReminders, AppConstants.HISTORY_OPERATION_EMPTY, new PillpopperTime(druglistEntry.getKey() / 1000));
                                }
                            });
                            listOfPastReminders = checkForRemindersEnabled(listOfPastReminders);
                            if (!listOfPastReminders.isEmpty()) {
                                tempDrugListHolder.put(druglistEntry.getKey(), listOfPastReminders);
                            }
                        }
//                        currentRemindersMap.put(druglistEntry.getKey(), druglistEntry.getValue());
                    } else {
                        //PillpopperLog.say("Past Reminder Preparation : Time is " + druglistEntry.getKey() + " And the drug size : " + druglistEntry.getValue());

                        List<Drug> listForPastReminders = new ArrayList<>();

                        for (Drug drug : druglistEntry.getValue()) {
                            Drug clonedDrug = cloneDrugObject(getContext(), drug);
                            boolean isMissedEventsWritten = false;
                            if (!FrontController.getInstance(getContext()).isHistoryEventAvailable(new PillpopperTime(druglistEntry.getKey() / 1000), clonedDrug.getGuid())
                                /*&& isEligiblePastEvent(clonedDrug, new PillpopperTime(druglistEntry.getKey()/1000))*/&& !clonedDrug.isInvisible()) {
                                if (clonedDrug.getSchedule().getDayPeriod() >= 1) {
                                    if ((PillpopperTime.now().getGmtSeconds() - druglistEntry.getKey() / 1000) < 24 * 60 * 60) {
                                        listForPastReminders.add(clonedDrug);
                                        FrontController.getInstance(context).insertPastReminderPillId(drug.getGuid(), druglistEntry.getKey());
                                    }else{
                                        logMissedDoseEvent(drug,context);
                                    }

                                } /*else if (clonedDrug.getSchedule().getDayPeriod() > 1) {
                                    if ((PillpopperTime.now().getGmtSeconds() - druglistEntry.getKey() / 1000) < 24 * 60 * 60) {
                                        listForPastReminders.add(clonedDrug);
                                        FrontController.getInstance(context).insertPastReminderPillId(drug.getGuid(), druglistEntry.getKey());
                                    } else {
                                        logMissedDoseEvent(drug,context);
                                    }
                                } */
                            }
                        }
                        if (listForPastReminders.size() > 0) {
                            List<Drug> finalListOfPastReminders = listForPastReminders;
                            AsyncTask.execute(new Runnable() {
                                @Override
                                public void run() {
                                    FrontController.getInstance(context).createEntryInHistoryForScheduleTime(finalListOfPastReminders, AppConstants.HISTORY_OPERATION_EMPTY, new PillpopperTime(druglistEntry.getKey() / 1000));
                                }
                            });
                            listForPastReminders = checkForRemindersEnabled(listForPastReminders);
                            if (!listForPastReminders.isEmpty()) {
                                tempDrugListHolder.put(druglistEntry.getKey(), listForPastReminders);
                            }
                        }
                    }
                }
            }
        }
        if (!tempDrugListHolder.isEmpty()) {
            getPassedRemindersMap().putAll(tempDrugListHolder);
        }
        preparePassedRemindersData(getPassedRemindersMap());


        PillpopperRunTime.getInstance().setmCurrentRemindersMap(getCurrentRemindersMap());
        PillpopperRunTime.getInstance().setmPassedRemindersMap(getPassedRemindersMap());
    }
    private static void logMissedDoseEvent(Drug drug, Context context){
        AsyncTask.execute(new Runnable() {
            @Override
            public void run() {
               logMissedDoses(drug,context);
            }
        });
    }

    public List<Drug> checkForRemindersEnabled(List<Drug> listOfDrugs) {
        List<Drug> drugList = new ArrayList<>();
        for (Drug drug : listOfDrugs) {
            if (null != drug.getIsRemindersEnabled() && ("Y").equalsIgnoreCase(drug.getIsRemindersEnabled())) {
                drugList.add(drug);
            }
        }
        return drugList;
    }

    private boolean isEntryInPastReminderTable(Long time, Context _thisActivity) {
        return FrontController.getInstance(_thisActivity).isEntryInPastReminderTable(time);
    }


    public synchronized List<Drug> getRemindersMapDataForNotificationAction(List<Drug> overdueDrugs, long notificaitonTime, Context _thisActivity) {
        LinkedHashMap<Long, LinkedHashMap<String, List<Drug>>> drugListMapByTime = new LinkedHashMap<>();
        sortDrugTimes(getContext(), overdueDrugs);
        drugListMapByTime = prepareDataByUsers(overdueDrugs);
        LinkedHashMap<Long, List<Drug>> drugListByTime = getDrugListByTimeMap(drugListMapByTime);
        setContext(_thisActivity);

        List<Drug> drugsForAction = new ArrayList<>();

        if (null != drugListByTime && !drugListByTime.isEmpty()) {
            if (!drugListByTime.keySet().isEmpty()) {
                for (Map.Entry<Long, List<Drug>> druglistEntry : drugListByTime.entrySet()) {
                    if (druglistEntry.getKey() / 1000 == notificaitonTime) {
                        for (Drug drug : druglistEntry.getValue()) {
                            Drug clonedDrug = cloneDrugObject(getContext(), drug);
                            clonedDrug.setScheduledTime(new PillpopperTime(notificaitonTime));
                            drugsForAction.add(clonedDrug);
                            LoggerUtils.info("Debug - drugName " + clonedDrug.getName() + " - time " + notificaitonTime
                                    + " overdue time " + (clonedDrug.getOverdueDate() == null ? " null " : clonedDrug.getOverdueDate().getGmtSeconds())
                                    + " schedule time " + (clonedDrug.getScheduledTime() == null ? " null " : clonedDrug.getScheduledTime().getGmtSeconds()));
                        }
                    }
                }
            }
        }

        return drugsForAction;
    }

    private LinkedHashMap<Long, List<Drug>> getDrugListByTimeMap(LinkedHashMap<Long, LinkedHashMap<String, List<Drug>>> _drugListMapByTime) {
        LinkedHashMap<Long, List<Drug>> drugListMap = new LinkedHashMap<>();
        for (Map.Entry<Long, LinkedHashMap<String, List<Drug>>> _entry : _drugListMapByTime.entrySet()) {
            long time = _entry.getKey();
            LinkedHashMap<String, List<Drug>> list = _entry.getValue();
            for (Map.Entry<String, List<Drug>> entry : list.entrySet()) {
                if (drugListMap.get(time) == null) {
                    List<Drug> tempList = new ArrayList<>(entry.getValue());
                    drugListMap.put(time, tempList);
                } else {
                    drugListMap.get(time).addAll(entry.getValue());
                }
            }
        }
        return drugListMap;
    }

    private LinkedHashMap<Long, LinkedHashMap<String, List<Drug>>> prepareDataByUsers(List<Drug> overdueDrugs) {
        List<Drug> tempListOfDrugs;
        LinkedHashMap<Long, List<Drug>> mappedWithTimes = new LinkedHashMap<>();
        LinkedHashMap<Long, LinkedHashMap<String, List<Drug>>> drugsMapByUsersTime = new LinkedHashMap<>();
        LinkedHashMap<String, List<Drug>> drugsMapByUserId = new LinkedHashMap<>();

        for (int i = 0; i < getListOfTimes().size(); i++) {
            tempListOfDrugs = new ArrayList<>();
            for (int j = 0; j < overdueDrugs.size(); j++) {
                if (null != overdueDrugs.get(j).getPassedReminderTimes() && !overdueDrugs.get(j).getPassedReminderTimes().isEmpty()) {
                    for (PillpopperTime pillpopperTime : overdueDrugs.get(j).getPassedReminderTimes()) {
                       // PillpopperLog.say("--Adding Times : " + pillpopperTime.getGmtMilliseconds());
                        if (getListOfTimes().get(i) == pillpopperTime.getGmtMilliseconds()) {
                            tempListOfDrugs.add(overdueDrugs.get(j));
                        }
                    }
                } else {
                    if (null != overdueDrugs.get(j).getOverdueDate()) {
                        if (getListOfTimes().get(i) == overdueDrugs.get(j).getOverdueDate().getGmtMilliseconds()) {
                            tempListOfDrugs.add(overdueDrugs.get(j));
                        }
                    }
                }

                if (!tempListOfDrugs.isEmpty()) {
                    mappedWithTimes.put(getListOfTimes().get(i), tempListOfDrugs);
                }
            }
        }


        for (Map.Entry<Long, List<Drug>> entry : mappedWithTimes.entrySet()) {
            tempListOfDrugs = entry.getValue();
            if (null != tempListOfDrugs && !tempListOfDrugs.isEmpty()) {
                Collections.sort(tempListOfDrugs, new Drug.AlphabeticalByNameComparator());
                drugsMapByUserId = getDrugsMapByUserId(tempListOfDrugs);
                drugsMapByUsersTime.put(entry.getKey(), drugsMapByUserId);
            }
        }

        return drugsMapByUsersTime;
    }

    private LinkedHashMap<String, List<Drug>> getDrugsMapByUserId(List<Drug> drugList) {

        List<String> useridList = getUniqueUsersIdsForDrugs(drugList);
        LinkedHashMap<String, List<Drug>> overDuemapByUserId = new LinkedHashMap<>();
        LinkedHashMap<String, List<Drug>> tempuserUserId = new LinkedHashMap<>();
        LinkedHashMap<String, List<Drug>> tempProxUserId = new LinkedHashMap<>();

        for (String userid : useridList) {
            if (userid.equals(FrontController.getInstance(getContext()).getPrimaryUserId())) {
                tempuserUserId.putAll(prepareDrugListUserData(userid, drugList));
            }
        }

        for (Map.Entry<String, List<Drug>> temp : tempuserUserId.entrySet()) {
            overDuemapByUserId.put(temp.getKey(), temp.getValue());
        }

        for (String userid : getSortedProxyMembers()) {
            tempProxUserId.putAll(prepareDrugListUserData(userid, drugList));
        }

        for (Map.Entry<String, List<Drug>> temp : tempProxUserId.entrySet()) {
            overDuemapByUserId.put(temp.getKey(), temp.getValue());
        }
        return overDuemapByUserId;
    }

    private List<String> getSortedProxyMembers() {
        return FrontController.getInstance(getContext()).getProxyMemberUserIds();
    }

    private LinkedHashMap<String, List<Drug>> prepareDrugListUserData(String userId, List<Drug> drugList) {
        LinkedHashMap<String, List<Drug>> drugListByUser = new LinkedHashMap<>();
        List<Drug> tempDrugList = new ArrayList<>();
        if (null != drugList && !drugList.isEmpty()) {
            for (Drug d : drugList) {
                if (userId.equalsIgnoreCase(d.getUserID())) {
                    tempDrugList.add(d);
                }
            }
        }
        if (!tempDrugList.isEmpty()) {
            drugListByUser.put(userId, tempDrugList);
        }
        return drugListByUser;
    }

    private List<String> getUniqueUsersIdsForDrugs(List<Drug> drugList) {
        List<String> uniqUseridList = new ArrayList<>();
        Set<String> list = new TreeSet<>(collectAllUserIds(drugList));
        uniqUseridList.clear();
        uniqUseridList.addAll(list);
        return uniqUseridList;
    }


    private List<String> collectAllUserIds(List<Drug> drugList) {
        List<String> useridList = new ArrayList<>();
        if (null != drugList && !drugList.isEmpty()) {
            for (Drug d : drugList) {
                useridList.add(d.getUserID());
            }
        }
        return useridList;
    }

    private void sortDrugTimes(Context context, List<Drug> mOverdueDrugs) {
        List<Long> times = new ArrayList<>();
        for (Drug d : mOverdueDrugs) {
            if (null != d.getPassedReminderTimes() && !d.getPassedReminderTimes().isEmpty()) {
                for (PillpopperTime pillpopperTime : d.getPassedReminderTimes()) {
//                    if (!FrontController.getInstance(context).isHistoryEventAvailable(pillpopperTime, d.getGuid())) {
                    times.add(pillpopperTime.getGmtMilliseconds());
//                    }
                }
            } else {
                if (null != d.getOverdueDate()/* && !FrontController.getInstance(context).isHistoryEventAvailable(d.getOverdueDate(), d.getGuid())*/) {
                    times.add(d.getOverdueDate().getGmtMilliseconds());
                }
            }
        }
        Set<Long> list = new TreeSet<>(times);
        getListOfTimes().addAll(list);
        Collections.sort(getListOfTimes(), Collections.reverseOrder());
    }

    private void preparePassedRemindersData(LinkedHashMap<Long, List<Drug>> tempDrugListHolder) {
        PillpopperRunTime.getInstance().setPassedReminderersHashMapByUserId(getDrugListByUser(tempDrugListHolder));
    }

    private LinkedHashMap<String, LinkedHashMap<Long, List<Drug>>> getDrugListByUser(LinkedHashMap<Long, List<Drug>> drugsMapByTime) {
        LinkedHashMap<String, LinkedHashMap<Long, List<Drug>>> drugsMapByUserIds = new LinkedHashMap<>();
        LinkedHashMap<Long, List<Drug>> listByTime = new LinkedHashMap<>();
        Set<String> list = new LinkedHashSet<>(collectUserIds(drugsMapByTime, getContext()));
        List<Drug> tempDrugList = null;

        for (String userID : list) {
            listByTime = new LinkedHashMap<>();
            for (Map.Entry<Long, List<Drug>> timesList : drugsMapByTime.entrySet()) {
                tempDrugList = new ArrayList<>();
                for (Drug drug : timesList.getValue()) {
                    if (userID.equalsIgnoreCase(drug.getUserID())) {
                        long timeInSeconds = timesList.getKey() / 1000;
                        drug.setScheduledTime(new PillpopperTime(timeInSeconds));
                        PillpopperLog.say("Adding schedule time - " + timeInSeconds
                        );
                        tempDrugList.add(drug);
                    }
                }
                if (!tempDrugList.isEmpty()) {
                    listByTime.put(timesList.getKey(), tempDrugList);
                }
            }
            drugsMapByUserIds.put(userID, listByTime);
        }
        return drugsMapByUserIds;
    }

    /**
     * Collect All the userids, Fills Primary User first followed by Proxy members.
     *
     * @param drugsMapByTime drug time
     * @param context        context
     * @return List of String
     */
    private List<String> collectUserIds(LinkedHashMap<Long, List<Drug>> drugsMapByTime, Context context) {
        List<String> primaryUseridList = new ArrayList<>();
        List<String> proxyUseridList = new ArrayList<>();
        List<String> useridList = new ArrayList<>();

        for (Map.Entry<Long, List<Drug>> list : drugsMapByTime.entrySet()) {
            for (Drug d : list.getValue()) {
                if (d.getUserID().equalsIgnoreCase(FrontController.getInstance(context).getPrimaryUserId()))
                    primaryUseridList.add(d.getUserID());
            }
        }

        for (Map.Entry<Long, List<Drug>> list : drugsMapByTime.entrySet()) {
            for (Drug d : list.getValue()) {
                if (!d.getUserID().equalsIgnoreCase(FrontController.getInstance(context).getPrimaryUserId()))
                    proxyUseridList.add(d.getUserID());
            }
        }

        if (!proxyUseridList.isEmpty()) {
            useridList.addAll(proxyUseridList);
        }

        ComposableComparator<String> comparator = new ComposableComparator<String>().by(
                new MemberDisplayComparator(context));

        Collections.sort(useridList, comparator);

        if (!primaryUseridList.isEmpty()) {
            useridList.addAll(0, primaryUseridList);
        }
        return useridList;
    }


    /**
     * Inner class to sort the proxy names in alphabetical order
     */
    public static class MemberDisplayComparator implements Comparator<String> {

        private Context ctx;

        public MemberDisplayComparator(Context context) {
            this.ctx = context;
        }

        @Override
        public int compare(String lUserID, String rUserId) {
            if (FrontController.getInstance(ctx).getUserFirstNameByUserId(lUserID) != null && FrontController.getInstance(ctx).getUserFirstNameByUserId(rUserId) != null) {
                return FrontController.getInstance(ctx).getUserFirstNameByUserId(lUserID).compareToIgnoreCase(FrontController.getInstance(ctx).getUserFirstNameByUserId(rUserId));
            } else {
                return 0;
            }
        }

    }

    public static void sendEmail(PillpopperActivity act, List<HistoryEvent> eventList, CharSequence body) {
        PillpopperStringBuilder eventTable = new PillpopperStringBuilder(act, act.getGlobalAppContext());

        eventTable.append(String.format(
                Locale.getDefault(),
                act.getString(R.string.email_html_drug_table_header_row),
                describeAsHtml(act, null))
        );

        for (HistoryEvent historyEvent : eventList) {
            eventTable.append(String.format(
                    Locale.getDefault(),
                    act.getString(R.string.email_html_drug_table_data_row),
                    describeAsHtml(act, historyEvent))
            );
        }

        String attachment = String.format(
                Locale.getDefault(),
                act.getString(R.string.email_drug_table_wrapper),
                eventTable.toString());

        Util.sendEmail(
                act,
                null,
                act.getString(R.string.history_email_subject),
                body,
                attachment,
                Util.DOSECAST_HISTORY_HTML);
    }

    public static String describeAsHtml(PillpopperActivity activity, HistoryEvent historyEvent) {
        PillpopperStringBuilder sb = new PillpopperStringBuilder(activity, activity.getGlobalAppContext());

        if (historyEvent == null)
            sb.appendColumn(R.string.drug_name);
        else {
            sb.appendColumn(historyEvent.getPillName());
        }

        if (historyEvent == null)
            sb.appendColumn(R.string.edit_history_entry_event_time);
        else {

            String dateString = historyEvent.getHeaderTime();
            sb.appendColumn(getEventTimeText(dateString));
        }

        if (historyEvent == null)
            sb.appendColumn(R.string._action);
        else if (historyEvent.getOperationStatus().equalsIgnoreCase("takePill")) {
            sb.appendColumn(activity.getString(R.string.email_txt_taken));
        } else if (historyEvent.getOperationStatus().equalsIgnoreCase("skipPill")) {
            sb.appendColumn(activity.getString(R.string.email_txt_not_taken));
        } else {
            sb.appendColumn("");
        }
        return sb.toString();
    }

    private static String getEventTimeText(String editTime) {

        Date eventDateTime = new Date(handleParseLong(editTime) * 1000L);

        SimpleDateFormat df2 = new SimpleDateFormat("EEEE MMMM d h:mm a");
        String dateText = df2.format(eventDateTime);
        dateText = "<b>" + dateText.substring(0, dateText.indexOf(" ")) + "</b>" + " " + dateText.substring(dateText.indexOf(" ") + 1);
        return dateText;
    }

    public static String getSystemAMFormat() {
        Date date = new Date();//Time is set to 12 am
        date.setHours(0);
        date.setMinutes(0);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.getDisplayName(Calendar.AM_PM, Calendar.SHORT, Locale.getDefault());
    }

    public static String getSystemPMFormat() {

        Date date = new Date();//Time is set to 1 pm
        date.setHours(13);
        date.setMinutes(0);
        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        return cal.getDisplayName(Calendar.AM_PM, Calendar.SHORT, Locale.getDefault());
    }

    public static String formatPhoneNumber(String phoneNumber) {
        if (phoneNumber.length() >= 10) {
            Pattern pattern = Pattern.compile("\\d-\\d{3}-\\d{3}-\\d{4}");
            Matcher matcher = pattern.matcher(phoneNumber);
            if (!matcher.matches()) {
                MessageFormat phoneFormat = new MessageFormat("{0}-{1}-{2}-{3}");
                String[] phoneNumberChunks = {phoneNumber.substring(0, 1), phoneNumber.substring(1, 4), phoneNumber.substring(4, 7), phoneNumber.substring(7, 11)};
                return phoneFormat.format(phoneNumberChunks);
            }
        }
        return phoneNumber;
    }

    public static long calculateUpdatedSchedule(long postponeSeconds) {
        return PillpopperTime.now().getContainingMinute().getGmtSeconds() + postponeSeconds;
    }

    public static String getTime(long time) {
        SimpleDateFormat simpleDate = new SimpleDateFormat("h:mm a");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(time);
        return simpleDate.format(calendar.getTime()).toString();
    }

    public static String getDate(long date) {
        SimpleDateFormat simpleDate = new SimpleDateFormat("d-MMM-yyyy");
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(date);
        String[] splitString = simpleDate.format(calendar.getTime()).split("-");
        String mDate = splitString[0];
        String mMonth = splitString[1];
        return mMonth + " " + mDate;
    }

    public static String getNotificationUri(Context context, String fileName) {

        RingtoneManager manager = new RingtoneManager(context);
        manager.setType(RingtoneManager.TYPE_NOTIFICATION);
        Cursor cursor = null;
        try {
            cursor = manager.getCursor();
            while (cursor.moveToNext()) {

                String notificationTitle = cursor.getString(RingtoneManager.TITLE_COLUMN_INDEX);
                String notificationUri = cursor.getString(RingtoneManager.URI_COLUMN_INDEX);

                String columnIndex = cursor.getString(RingtoneManager.ID_COLUMN_INDEX);
                if (fileName.equalsIgnoreCase(notificationTitle)) {

                    return notificationUri + "/" + columnIndex;
                }
            }
        } catch (Exception e) {
            LoggerUtils.exception("Exception", e);
        } finally {
            if (cursor != null && !cursor.isClosed()) {
                try {
                    cursor.close();
                } catch (Exception e) {
                    LoggerUtils.exception("Exception", e);
                }
            }
        }
        return null;
    }

    public static Bitmap getImageBitmap(Context context, String imageGuid) {
        Drawable drawable = getImageDrawable(context, imageGuid);
        if (drawable != null) {
            return ((BitmapDrawable) drawable).getBitmap();
        }

        return null;
    }

    public static Drawable getImageDrawable(Context context, String imageGuid) {
        Drawable _imageDrawable = null;
        if (imageGuid != null) {
            try {
                _imageDrawable = new BitmapDrawable(MediaStore.Images.Media.getBitmap(context.getContentResolver(), Uri.fromFile(getImageCacheFile(context, imageGuid))));
            } catch (FileNotFoundException e) {
                LoggerUtils.exception("FileNotFoundException", e);
                _imageDrawable = null;
            } catch (IOException e) {
                LoggerUtils.exception("IOException", e);
                _imageDrawable = null;
            }
        }

        return _imageDrawable;
    }

    public static File getImageCacheFile(Context context, String guid) {
        if (guid == null) {
            return null;
        } else {
            try {
                return new File(getImageCacheDir(context, false), "drugimage-" + guid + ".jpg");
            } catch (IOException e) {
                LoggerUtils.exception("IOException", e);
                return null;
            }
        }
    }

    private static final String _IMAGECACHE_DIRNAME = "imagecache";

    // Get the directory where images are stored either on local storage or on the external SD card
    public static File getImageCacheDir(Context context, boolean backupDir) throws IOException {
        File baseDir = null;

        if (backupDir) {
            baseDir = FileHandling.getExternalStorageDirectory(context, StorageLocation.External_Durable);
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

    public static boolean deleteDirectory(File path) {
        if (path.exists()) {
            File[] files = path.listFiles();
            if (files == null) {
                return true;
            }
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    if (!file.delete()) {
                        PillpopperLog.say("Oops, Util - deleteDirectory Failed to delete file");
                    }
                }
            }
        }
        return (path.delete());
    }

    public static void updateOptinSelection(String quickviewSelection, Context context) {
        FrontController.getInstance(context).updateQuickviewSelection(quickviewSelection);
        JSONObject preferences = new JSONObject();
        try {
            preferences.put(PillpopperConstants.ACTION_SETTINGS_SIGNOUT_REMINDERS, quickviewSelection);
            preferences.put("userData", SharedPreferenceManager.getInstance(context, AppConstants.AUTH_CODE_PREF_NAME).getString(AppConstants.KP_GUID, null));
            createLogEntry(preferences, context);
        } catch (JSONException e) {
            LoggerUtils.exception("JSONException", e);
        }

    }

    public static void createLogEntry(JSONObject preferences, Context context) {
        String replyId = Util.getRandomGuid();
        LogEntryModel logEntryModel = new LogEntryModel();
        logEntryModel.setDateAdded(System.currentTimeMillis());
        logEntryModel.setReplyID(replyId);
        JSONObject jsonObj = Util.prepareSettingsAction(preferences, replyId, ActivationController.getInstance().getUserId(context), context);
        logEntryModel.setEntryJSONObject(jsonObj, context);
        FrontController.getInstance(context).addLogEntry(context, logEntryModel);
    }

    public static String getLanguage() {
        return Locale.getDefault().toString();
    }

    public static String getTimeZoneName() {
        TimeZone tz = TimeZone.getDefault();
        return tz.getDisplayName();
    }

    public static int getColorWrapper(Context context, int id) {
        return ContextCompat.getColor(context, id);
    }


    public static int getTimePickerHourWrapper(TimePicker timePicker) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return timePicker.getHour();
        } else {
            //to support lower versions below M
            //noinspection deprecation
            return timePicker.getCurrentHour();
        }
    }

    public static int getTimePickerMinuteWrapper(TimePicker timePicker) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return timePicker.getMinute();
        } else {
            //to support lower versions below M
            //noinspection deprecation
            return timePicker.getCurrentMinute();
        }
    }

    public static void setTimePickerHourWrapper(TimePicker timePicker, int hour) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker.setHour(hour);
        } else {
            //to support lower versions below M
            //noinspection deprecation
            timePicker.setCurrentHour(hour);
        }
    }

    public static void setTimePickerMinuteWrapper(TimePicker timePicker, int min) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            timePicker.setMinute(min);
        } else {
            //to support lower versions below M
            //noinspection deprecation
            timePicker.setCurrentMinute(min);
        }
    }

    public static Drawable getDrawableWrapper(Context context, int drawable) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            return ContextCompat.getDrawable(context, drawable);
        } else {
            //to support lower versions below M
            //noinspection deprecation
            return context.getResources().getDrawable(drawable);
        }
    }


    public static void logMissedDoses(Drug drug, Context context) {
        if (!TextUtils.isEmpty(drug.getPreferences().getPreference("missedDosesLastChecked"))) {
            FrontController frontController = FrontController.getInstance(context);
            PillpopperTime missedDosesLastChecked = convertFloattoPillpopperTime(drug.getPreferences().getPreference("missedDosesLastChecked"));
            List<DoseEvent> scheduledDoseTimes = null;
            boolean isMissedEventsWritten = false;
            if (missedDosesLastChecked != null) {
                scheduledDoseTimes = drug.getDBValidEventsNearDayToLogMissedDoses(drug, missedDosesLastChecked.getLocalDay(), PillpopperDay.today().addDays(-1));
            }

            if (null != scheduledDoseTimes && !scheduledDoseTimes.isEmpty()) {
                int numDoseTimes = scheduledDoseTimes.size();

                LoggerUtils.info("Debug dose events size - " + numDoseTimes);
                for (int i = 0; i < numDoseTimes; i++) {
                    DoseEvent thisEvent = scheduledDoseTimes.get(i);
                    LoggerUtils.info("Debug doseEvent - " + Util.convertDateLongToIso(String.valueOf(thisEvent.getDate().getGmtSeconds())));
                    if ((thisEvent.getDate().getGmtSeconds() == missedDosesLastChecked.getGmtSeconds() || thisEvent.getDate().after(missedDosesLastChecked))
                            && (PillpopperTime.now().getGmtMilliseconds() - thisEvent.getDate().getGmtMilliseconds()) > 24 * 60 * 60 * 1000) {
                        // look for recent postpone event
                        HistoryEvent latestPostponeEvent = DatabaseUtils.getInstance(context).getLastPostponedHistoryEventForSpecificTime(drug.getGuid(),String.valueOf(thisEvent.getDate().getGmtSeconds()));
                        PillpopperTime finalPostponedTime = null;
                        if(null!=latestPostponeEvent && null!= latestPostponeEvent.getPreferences() && null != latestPostponeEvent.getPreferences().getFinalPostponedDateTime()){
                            try{
                                finalPostponedTime = new PillpopperTime(Long.parseLong(convertDateIsoToLong(latestPostponeEvent.getPreferences().getFinalPostponedDateTime())));
                            }catch (Exception e){
                                LoggerUtils.exception(e.getMessage());
                            }
                        }
                        if (!thisEvent.getDate().getLocalDay().before(drug.getSchedule().getStart())
                                // checking whether the finalPostponedTime has crossed  if it a postponedEvent
                                &&((isaPostponeEvent(thisEvent,latestPostponeEvent) && (null != finalPostponedTime)
                                &&(PillpopperTime.now().getGmtMilliseconds() -finalPostponedTime.getGmtMilliseconds() ) > 24 * 60 * 60 * 1000) || !frontController.isHistoryEventForScheduleAvailable(String.valueOf(thisEvent.getDate().getGmtSeconds()), drug.getGuid()))) {

                            if (isaPostponeEvent(thisEvent, latestPostponeEvent)) {
                                editPostponeEvent(drug,latestPostponeEvent,context);
                            }
                            //Add the log entrys
                            PillpopperLog.say("-- History not found for this drug - " + drug.getName() + " - And last Checked Time : " + drug.getPreferences().getPreference("missedDosesLastChecked")
                                    + " - " + PillpopperTime.getDebugString(thisEvent.getDate()));
                            LoggerUtils.info("History event not available for " + String.valueOf(thisEvent.getDate().getGmtSeconds()));
                            if (isValidMissedEvent(drug, thisEvent.getDate())) {
                                FrontController.getInstance(context).addMissedDoseHistoryEvent(drug, PillpopperConstants.ACTION_MISS_PILL, thisEvent.getDate(), context);
                                isMissedEventsWritten = true;
                            }
                        }
                    }
                }
                if (isMissedEventsWritten) { // if this is last doseEvent for this Drug, then write the EditPill Action
                    LoggerUtils.info("Going to write the Log Entry for Edit Pill : Name : " + drug.getName() + " And missedDoseChecked : " + drug.getPreferences().getPreference("missedDosesLastChecked"));
                    FrontController.getInstance(context).addLogEntry(context, prepareLogEntryForEditPillAction(PillpopperConstants.ACTION_EDIT_PILL, drug, context));
                }
            }
            if (isMissedEventsWritten) {
                if (AppConstants.isByPassLogin() || TextUtils.isEmpty(RunTimeData.getInstance().getRuntimeSSOSessionID())) { // If the Quickview Mode then, make it in nonSecure calls
                    StateDownloadIntentService.startActionNonSecureIntermediateGetState(context);
                } else {
                    StateDownloadIntentService.startActionIntermediateGetState(context);
                }
            }
        }
    }

    private static boolean isaPostponeEvent(DoseEvent thisEvent, HistoryEvent latestPostponeEvent) {
        return null != latestPostponeEvent && null != latestPostponeEvent.getPreferences()
                && latestPostponeEvent.getPreferences().isPostponedEventActive()
                && latestPostponeEvent.getHeaderTime().equalsIgnoreCase(String.valueOf(thisEvent.getDate().getGmtSeconds()));
    }


    public static boolean isValidMissedEvent(Drug d, PillpopperTime eventTime) {
        if (null == d.getSchedule().getEnd()) { // Since no End date, its eligible.
            return true;
        }
        PillpopperTime drugEndDateTime = d.getSchedule().getEnd().atLocalTime(new HourMinute(23, 59));
        return (null != eventTime && (eventTime.before(drugEndDateTime) || eventTime == drugEndDateTime));
    }


    public static LogEntryModel prepareLogEntryForEditPillAction(String action, Drug drug, Context context) {
        LogEntryModel logEntryModel = new LogEntryModel();
        logEntryModel.setDateAdded(System.currentTimeMillis());
        String replyId = getRandomGuid();
        logEntryModel.setReplyID(replyId);
        logEntryModel.setAction(action);

        JSONObject pillpopperRequest = new JSONObject();
        JSONObject pillRequest = new JSONObject();
        JSONObject pillPrefRequest = new JSONObject();
        String scheduleDate = FrontController.getInstance(context).getScheduleDateFromHistory(drug.getGuid());
        String missedDosesLastCheckedValue = FrontController.getInstance(context).getMissedDosesLastCheckedValue(drug.getGuid());

        try {
            pillRequest.put("apiVersion", "6.0.4");
            pillRequest.put("action", action);
            pillRequest.put("replayId", replyId);
            pillRequest.put("language", Util.getLanguage());
            pillRequest.put("deviceToken", "");

            pillRequest.put("notify_after", missedDosesLastCheckedValue);

            if (null != scheduleDate) {
                if (null != Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(scheduleDate))) {
                    pillRequest.put("scheduleDate", Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(scheduleDate)).getGmtSeconds());
                    pillRequest.put("last_taken", Util.convertStringtoPillpopperTime(Util.convertDateIsoToLong(scheduleDate)).getGmtSeconds());
                }
            } else {
                pillRequest.put("last_taken", -1);
            }

//            pillRequest.put("dose", drug.getDose());
            pillRequest.put("name", drug.getName());
           // pillRequest.put("type", getScheduleType(drug.getSchedule().getSchedType()));
            pillRequest.put("clientVersion", getAppVersion(context));
            pillRequest.put("partnerId", PillpopperConstants.PARTNER_ID);
            pillRequest.put("pillId", drug.getGuid());
            String scheduleChoice = Util.getScheduleChoice(drug);
            pillRequest.put("start", PillpopperDay.marshalLocalDayAsGMTTime(PillpopperDay.PartOfDay.DayStart, scheduleChoice,drug.getSchedule(), drug.getSchedule().getStart()));
            pillRequest.put("end", PillpopperDay.marshalLocalDayAsGMTTime(PillpopperDay.PartOfDay.DayEnd, scheduleChoice, drug.getSchedule(), drug.getSchedule().getEnd()));
            pillRequest.put("interval", 0);

            if (null != drug.get_effLastTaken()) {
                pillRequest.put("eff_last_taken", drug.get_effLastTaken().getGmtSeconds());
            } else {
                pillRequest.put("eff_last_taken", -1);
            }
            pillRequest.put("hardwareId", UniqueDeviceId.getHardwareId(context));
            pillRequest.put("userId", FrontController.getInstance(context).getPrimaryUserIdIgnoreEnabled());
            pillRequest.put("targetUserId", drug.getUserID());

            pillRequest.put("schedule", FrontController.getInstance(context).getSchedulesInTimeFormateByPillId(drug.getGuid()));

            if (drug.isScheduleAddedOrUpdated()) {
                pillRequest.put("isScheduleAddedOrUpdated", true);
            } else {
                pillRequest.put("isScheduleAddedOrUpdated", false);
            }

            pillRequest.put("dayperiod", drug.getSchedule().getDayPeriod());

            if(PillpopperConstants.ACTION_EDIT_PILL.equalsIgnoreCase(action)){
                pillRequest.put("scheduleGuid", drug.getScheduleGuid());
            }

            pillPrefRequest.put("customDosageID", null != drug.getPreferences().getPreference("customDosageID") ? drug.getPreferences().getPreference("customDosageID") : "");
            pillPrefRequest.put("refillsRemaining", "-1");
            pillPrefRequest.put("personId", drug.getUserID());
            pillPrefRequest.put("invisible", drug.getPreferences().getPreference("invisible"));
            pillPrefRequest.put("archived", drug.getPreferences().getPreference("archived"));
            pillPrefRequest.put("refillAlertDoses", null != drug.getPreferences().getPreference("refillAlertDoses") ? drug.getPreferences().getPreference("refillAlertDoses") : "");
            pillPrefRequest.put("notes", null != drug.getPreferences().getPreference("notes") ? drug.getPreferences().getPreference("notes") : "");
            pillPrefRequest.put("refillQuantity", null != drug.getPreferences().getPreference("refillQuantity") ? drug.getPreferences().getPreference("refillQuantity") : "");
            pillPrefRequest.put("customDescription", null != drug.getPreferences().getPreference("customDescription") ? drug.getPreferences().getPreference("customDescription") : "");
            pillPrefRequest.put("remainingQuantity", null != drug.getPreferences().getPreference("remainingQuantity") ? drug.getPreferences().getPreference("remainingQuantity") : "");
            pillPrefRequest.put("lastManagedIdNotified", null != drug.getPreferences().getPreference("lastManagedIdNotified") ? drug.getPreferences().getPreference("lastManagedIdNotified") : "");
            pillPrefRequest.put("lastManagedIdNeedingNotify", null != drug.getPreferences().getPreference("lastManagedIdNeedingNotify") ? drug.getPreferences().getPreference("lastManagedIdNeedingNotify") : "");
            pillPrefRequest.put("secondaryReminders", "1");

            // get the latest missedDosesLastChecked value from the db.
            // Because the drug object will have the value when it was instantiated in prepareRemindersMapData and it could be old.
            pillPrefRequest.put("missedDosesLastChecked", missedDosesLastCheckedValue);

            pillPrefRequest.put("doctorCount", null != drug.getPreferences().getPreference("doctorCount") ? drug.getPreferences().getPreference("doctorCount") : "1");
            pillPrefRequest.put("imageGUID", null != drug.getPreferences().getPreference("imageGUID") ? drug.getPreferences().getPreference("imageGUID") : " ");
            pillPrefRequest.put("pharmacyCount", null != drug.getPreferences().getPreference("pharmacyCount") ? drug.getPreferences().getPreference("pharmacyCount") : "0");
            pillPrefRequest.put("prescriptionNum", null != drug.getPreferences().getPreference("prescriptionNum") ? drug.getPreferences().getPreference("prescriptionNum") : "");
            pillPrefRequest.put("weekdays", null != drug.getPreferences().getPreference("weekdays") ? drug.getPreferences().getPreference("weekdays") : "");
            pillPrefRequest.put("scheduleFrequency", drug.getScheduledFrequency());

            if (drug.getSchedule().getDailyLimit() == 0) {
                pillPrefRequest.put("maxNumDailyDoses", "-1");
            } else {
                pillPrefRequest.put("maxNumDailyDoses", drug.getSchedule().getDailyLimit());
            }
            pillPrefRequest.put("databaseNDC", null != drug.getPreferences().getPreference("databaseNDC") ? drug.getPreferences().getPreference("databaseNDC") : " ");
            pillPrefRequest.put("defaultImageChoice", null != drug.getPreferences().getPreference("defaultImageChoice") ? drug.getPreferences().getPreference("defaultImageChoice") : " ");
            pillPrefRequest.put("defaultServiceImageID", null != drug.getPreferences().getPreference("defaultServiceImageID") ? drug.getPreferences().getPreference("defaultServiceImageID") : " ");
            pillPrefRequest.put("needFDBUpdate", null != drug.getPreferences().getPreference("needFDBUpdate") ? drug.getPreferences().getPreference("needFDBUpdate") : " ");


            pillRequest.put("preferences", pillPrefRequest);
            pillpopperRequest.put("pillpopperRequest", pillRequest);

            PillpopperLog.say("--Adding log entry for action : " + action + " is : " + pillpopperRequest.toString());

        } catch (JSONException e) {
            PillpopperLog.say("Oops!, Exception while preparing the log entry model object", e);
        }
        logEntryModel.setEntryJSONObject(pillpopperRequest, context);
        return logEntryModel;
    }


    public static PillpopperTime convertFloattoPillpopperTime(String str) {
        String longValue = str;
        if (null != str) {
            if (str.contains(".")) {
                longValue = str.substring(0, str.indexOf("."));
            }
            return new PillpopperTime(handleParseLong(longValue));
        }
        return new PillpopperTime(handleParseLong(longValue));
    }

    public static Drug cloneDrugObject(Context context, Drug drug) {
        Drug clonedDrug = new Drug();

        clonedDrug.setId(drug.getGuid());
        clonedDrug.setName(drug.getName());
        clonedDrug.setCreated(drug.getCreated());
        clonedDrug.setNotes(drug.getNotes());
        clonedDrug.setLastTaken(drug.getLastTaken());
        clonedDrug.set_effLastTaken(drug.get_effLastTaken());
        clonedDrug.set_notifyAfter(drug.get_notifyAfter());
        clonedDrug.setIsRemindersEnabled(drug.getIsRemindersEnabled());
        // Preparing the drugPreferences object

        JSONObject prefJson = new JSONObject();
        try {
            prefJson.put("archived", drug.getPreferences().getPreference("archived"));
            prefJson.put("customDescription", drug.getPreferences().getPreference("customDescription"));
            prefJson.put("customDosageID", drug.getPreferences().getPreference("customDosageID"));
            prefJson.put("doctorCount", drug.getPreferences().getPreference("doctorCount"));
            prefJson.put("imageGUID", drug.getPreferences().getPreference("imageGUID"));
            prefJson.put("invisible", drug.getPreferences().getPreference("invisible"));
            prefJson.put("logMissedDoses", drug.getPreferences().getPreference("logMissedDoses"));
            prefJson.put("missedDosesLastChecked", drug.getPreferences().getPreference("missedDosesLastChecked"));
            prefJson.put("noPush", drug.getPreferences().getPreference("noPush"));
            prefJson.put("notes", drug.getPreferences().getPreference("notes"));
            prefJson.put("personId", drug.getPreferences().getPreference("personId"));
            prefJson.put("pharmacyCount", drug.getPreferences().getPreference("pharmacyCount"));
            prefJson.put("prescriptionNum", drug.getPreferences().getPreference("prescriptionNum"));
            prefJson.put("refillAlertDoses", drug.getPreferences().getPreference("refillAlertDoses"));
            prefJson.put("refillsRemaining", drug.getPreferences().getPreference("refillsRemaining"));
            prefJson.put("remainingQuantity", drug.getPreferences().getPreference("remainingQuantity"));
            prefJson.put("secondaryReminders", drug.getPreferences().getPreference("secondaryReminders"));
            prefJson.put("weekdays", drug.getPreferences().getPreference("weekdays"));
            prefJson.put("lastManagedIdNotified", drug.getPreferences().getPreference("lastManagedIdNotified"));
            prefJson.put("lastManagedIdNeedingNotify", drug.getPreferences().getPreference("lastManagedIdNeedingNotify"));
            prefJson.put("maxNumDailyDoses", drug.getPreferences().getPreference("maxNumDailyDoses"));
            prefJson.put("limitType", drug.getPreferences().getPreference("limitType"));
            prefJson.put("databaseNDC", drug.getPreferences().getPreference("databaseNDC"));
            prefJson.put("managedDropped", drug.getPreferences().getPreference("managedDropped"));
            prefJson.put("managedMedicationId", drug.getPreferences().getPreference("managedMedicationId"));
            prefJson.put("managedDescription", drug.getPreferences().getPreference("managedDescription"));
            prefJson.put("databaseMedForm", drug.getPreferences().getPreference("databaseMedForm"));
            prefJson.put("deleted", drug.getPreferences().getPreference("deleted"));
            prefJson.put("dosageType", drug.getPreferences().getPreference("dosageType"));
            prefJson.put("scheduleChoice", drug.getPreferences().getPreference("scheduleChoice"));
        } catch (JSONException e) {
            PillpopperLog.say("JSONException - Util - cloneDrugObject - ", e);
        }
        clonedDrug.setPreferecences(prefJson);

        Schedule schedule = new Schedule();

        schedule.setTimeList(drug.getSchedule().getTimeList().copy());

        if (drug.getSchedule().getStart() != null) {
            schedule.setStart(new PillpopperDay(drug.getSchedule().getStart().getYear(), drug.getSchedule().getStart().getMonth(), drug.getSchedule().getStart().getDay()));
        } else {
            schedule.setStart(null);
        }

        if (drug.getSchedule().getEnd() != null) {
            schedule.setEnd(new PillpopperDay(drug.getSchedule().getEnd().getYear(), drug.getSchedule().getEnd().getMonth(), drug.getSchedule().getEnd().getDay()));
        } else {
            schedule.setEnd(null);
        }

        //schedule.setSchedType(drug.getSchedule().getSchedType());
        schedule.setDayPeriod(drug.getSchedule().getDayPeriod());
        schedule.setDays(drug.getSchedule().getDays());
        clonedDrug.setSchedule(schedule);
        clonedDrug.setUserID(drug.getUserID());
        clonedDrug.setIsOverdue(drug.getIsOverdue());
        clonedDrug.setScheduledFrequency(drug.getScheduledFrequency());
        clonedDrug.setScheduleGuid(drug.getScheduleGuid());

        return clonedDrug;
    }

    public static String getLastSyncTokenValue(String lastSyncToken) {
        if (null != lastSyncToken && !"".equalsIgnoreCase(lastSyncToken)) {
            return lastSyncToken;
        } else {
            return "-1";
        }
    }

    public static boolean canShowLateReminder(Context context) {
        SharedPreferenceManager sharedPreferenceManager = SharedPreferenceManager.getInstance(context, AppConstants.AUTH_CODE_PREF_NAME);
        boolean signedOutStateRemoval = sharedPreferenceManager.getBoolean(AppConstants.SIGNED_OUT_STATE_REMOVAL, false);
        boolean signedStateRemoval = sharedPreferenceManager.getBoolean(AppConstants.SIGNED_STATE_REMOVAL, false);
        boolean signedOutStateRemovalAndSignInOnce = sharedPreferenceManager.getBoolean(AppConstants.SIGNED_OUT_STATE_REMOVAL_LOGIN_ONCE, false);
        String lateRemindersStatusFromNotifications = sharedPreferenceManager.getString(AppConstants.LATE_REMINDERS_STATUS_FROM_NOTIFICATION, "0");
        String dismissedTimeStamp = sharedPreferenceManager.getString(AppConstants.TIME_STAMP, "0");

        Calendar currentTime = GregorianCalendar.getInstance();
        Calendar dismissedTime = GregorianCalendar.getInstance();
        dismissedTime.setTimeInMillis(handleParseLong(dismissedTimeStamp) * 1000);

        if (AppConstants.isByPassLogin() && lateRemindersStatusFromNotifications.equalsIgnoreCase("1")) {
            return false;
        }

        if (signedStateRemoval) {
            return false;
        } else if (signedOutStateRemoval) {
            //show the late reminder only if its been more than 15 min inside app after dismissal.
            //if there is a current reminder, the flags will be reset.
            if (!AppConstants.isByPassLogin()) {
                // app is signed in state
                return false;
            } else {
                return signedOutStateRemovalAndSignInOnce;
            }
        } else {
            return AppConstants.isByPassLogin();
        }
    }


    public static String getActivationUrl(Context context) {
        String activationUrl = AppConstants.ConfigParams.productionActivationUrl;
        try {
            ApplicationInfo applicationInfo = context.getApplicationContext().getPackageManager().getApplicationInfo(context.getPackageName(), PackageManager.GET_META_DATA);
            Bundle bundle = applicationInfo.metaData;
            if (bundle != null) {
                String env = bundle.getString("buildEnv");
                if (null != env && (("dev").equalsIgnoreCase(env) || ("qa").equalsIgnoreCase(env))) {
                    activationUrl = AppConstants.ConfigParams.nonProductionActivationUrl;
                }
            }
        } catch (NameNotFoundException e) {
            PillpopperLog.say("Util:: Activation url error :", e);
        }
        return activationUrl;
    }

    public static void showForceUpgradeAlert(final Context context, String statusMessage) {
        // show force upgrade alert //
        GenericAlertDialog forceUpgradeAlert = new GenericAlertDialog(context, null, statusMessage, context.getString(R.string.update_now_alert_btn_text), (dialog, which) -> {
            // take user to playstore
            dialog.dismiss();
            try {
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(AppConstants.MARKET_URL)));
            } catch (android.content.ActivityNotFoundException anfe) {
                PillpopperLog.say("ActivityNotFoundException", anfe);
                context.startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(AppConstants.PLAY_STORE_URL)));
            }
            ((Activity) context).finish();
        }, null, null);

        forceUpgradeAlert.showDialogWithoutBtnPadding();
    }

    public static long getTzOffsetSecs(TimeZone paramTimeZone) {
        return paramTimeZone.getOffset(GregorianCalendar.getInstance().getTimeInMillis()) / 1000;
    }

    public static boolean isEmulator() {
        return Build.FINGERPRINT.startsWith("generic")
                || Build.FINGERPRINT.startsWith("unknown")
                || Build.MODEL.contains("google_sdk")
                || Build.MODEL.contains("Emulator")
                || Build.MODEL.contains("Android SDK built for x86")
                || Build.MANUFACTURER.contains("Genymotion")
                || (Build.BRAND.startsWith("generic") && Build.DEVICE.startsWith("generic"))
                || "google_sdk".equals(Build.PRODUCT);
    }

    public static boolean isNeedTOAdjustSchedule(String existing_tz_sec) {

        long newTimeoffset = getTzOffsetSecs(TimeZone.getDefault());
        long existingTimeoffset = Long.parseLong(existing_tz_sec);
        PillpopperLog.say("TimeZone sec : " + newTimeoffset + " : existing : " + existingTimeoffset);
        if (existingTimeoffset != newTimeoffset) {
            PillpopperLog.say("Timezone got changed");
            return true;
        }
        return false;
    }

    public static void showNMAAlert(Context context, String message) {
        Dialog dialog;
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);

        LayoutInflater inflater = ((Activity) context).getLayoutInflater();
        View view = inflater.inflate(R.layout.nma_alert, null);
        alertDialog.setView(view);
        TextView tv_message = view
                .findViewById(R.id.nma_description);
        tv_message.setText(message);
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton(Html.fromHtml("<b>OK</b>"),
                (dialog1, which) -> dialog1.dismiss());
        dialog = alertDialog.create();
        dialog.show();
    }

    /**
     * This method will be called when the app started with spalsh and timezone change detects.
     *
     * @param context
     * @return
     */
    public static JSONObject checkForDSTAndPrepareAdjustPillLogEntryObject(Context context) {
        SharedPreferenceManager mSharedPrefManager = SharedPreferenceManager.getInstance(context, AppConstants.AUTH_CODE_PREF_NAME);

        try {
            List<String> enabledUsers = FrontController.getInstance(context).getEnabledUserIds();

            JSONObject multiRequest = new JSONObject();
            JSONObject finalRequestObj = new JSONObject();
            JSONArray requestArray = new JSONArray();

            if (!enabledUsers.isEmpty()) {
                for (String userID : enabledUsers) {
                    if (null != FrontController.getInstance(context).getUserPreferencesForUser(userID)) {
                        UserPreferences userPref = FrontController.getInstance(context).getUserPreferencesForUser(userID);
                        String timezoneSec = userPref.getTz_sec();
                        String tzName = userPref.getTz_name();
                        String dstOffset = userPref.getDstOffset_secs();

                        if (timezoneSec == null && tzName == null && dstOffset == null) {
                            // This proxy is not having the preferences so reading the primary Member preferences
                            String primaryUserID = FrontController.getInstance(context).getPrimaryUserIdIgnoreEnabled();
                            timezoneSec = FrontController.getInstance(context).getUserPreferencesForUser(primaryUserID).getTz_sec();
                        }

                        /**
                         *Special Logic
                         * Updates last 48 hours events tzSec value with previous timezone, if any history event found tzSec value as null.
                         */
                        FrontController.getInstance(context).updateHistoryOffsetForLast48HourEvents(Long.parseLong(timezoneSec));

                        if ((null != timezoneSec && Util.isNeedTOAdjustSchedule(timezoneSec))) {

                            PillpopperLog.say("Time Zone got changed. Needs to be called AdjustPillSchedule");

                            TimeZone tz = TimeZone.getDefault();

                            JSONObject setPrefJsonObject = new JSONObject();
                            JSONObject setUserPrefJsonObject = new JSONObject();

                            JSONObject pillpopperRequestForSetPref = new JSONObject();


                            setPrefJsonObject.put("action", "SetPreferences");
                            setPrefJsonObject.put("clientVersion", Util.getAppVersion(context));
                            setPrefJsonObject.put("userId", FrontController.getInstance(context).getPrimaryUserIdIgnoreEnabled());
                            setPrefJsonObject.put("partnerId", AppConstants.EDITION);
                            setPrefJsonObject.put("language", Locale.getDefault().toString());
                            setPrefJsonObject.put("hardwareId", UniqueDeviceId.getHardwareId(context));
                            setPrefJsonObject.put("targetUserId", userID);
                            setPrefJsonObject.put("apiVersion", "Version 6.0.4");
                            setPrefJsonObject.put("deviceToken", "");
                            setPrefJsonObject.put("replayId", getRandomGuid());


                            setUserPrefJsonObject.put("tz_secs", String.valueOf(tz.getOffset(GregorianCalendar.getInstance().getTimeInMillis()) / 1000));
                            setUserPrefJsonObject.put("tz_name", tz.getDisplayName());
                            setUserPrefJsonObject.put("userData", mSharedPrefManager.getString(AppConstants.KP_GUID, ""));
                            setUserPrefJsonObject.put("dstOffset_secs", String.valueOf(getDSTOffsetValue()));

                            setPrefJsonObject.put("preferences", setUserPrefJsonObject);

                            pillpopperRequestForSetPref.put("pillpopperRequest", setPrefJsonObject);

                            requestArray.put(pillpopperRequestForSetPref);


                        }
                    }
                }

                if (requestArray.length() > 0) {
                    // requestArray.put(Util.prepareGetState(context));
                    multiRequest.put("requestArray", requestArray);
                    multiRequest.put("getAllOutput", 1);
                    finalRequestObj.put("pillpopperMultiRequest", multiRequest);

                    PillpopperLog.say("Final AdjustPill Request Object : " + finalRequestObj.toString());
                    return finalRequestObj;
                }
            }
        } catch (Exception exception) {
            PillpopperLog.say("Exception while preparing the AdjustPill Request Model", exception);
        }
        return null;
    }

    /**
     * Gets DSTOffset value
     *
     * @return
     */
    public static int getDSTOffsetValue() {
        Calendar calendar = new GregorianCalendar();
        int offset = calendar.get(Calendar.DST_OFFSET);
        if (offset != 0) {
            return offset / 1000;
        }
        return offset;
    }


    /**
     * This method will be called when the action taken from notication and the timezone got changed.
     *
     * @param context
     * @return
     */
    public static JSONArray checkForDSTAndPrepareAdjustPillJSONArray(Context context) {
        SharedPreferenceManager mSharedPrefManager = SharedPreferenceManager.getInstance(context, AppConstants.AUTH_CODE_PREF_NAME);
        try {
            List<String> enabledUsers = FrontController.getInstance(context).getEnabledUserIds();
            JSONArray requestArray = new JSONArray();
            if (!enabledUsers.isEmpty()) {
                for (String userID : enabledUsers) {
                    UserPreferences userPref = FrontController.getInstance(context).getUserPreferencesForUser(userID);
                    String timezoneSec = userPref.getTz_sec();
                    String tzName = userPref.getTz_name();
                    String dstOffset = userPref.getDstOffset_secs();

                    if (timezoneSec == null && tzName == null && dstOffset == null) {
                        // This proxy is not having the preferences so reading the primary Member preferences
                        String primaryUserID = FrontController.getInstance(context).getPrimaryUserIdIgnoreEnabled();
                        timezoneSec = FrontController.getInstance(context).getUserPreferencesForUser(primaryUserID).getTz_sec();
                    }

                    /**
                     *Special Logic
                     * Updates last 48 hours events tzSec value with previous timezone, if any history event found tzSec value as null.
                     */
                    FrontController.getInstance(context).updateHistoryOffsetForLast48HourEvents(Long.parseLong(timezoneSec));


                    if ((null != timezoneSec && Util.isNeedTOAdjustSchedule(timezoneSec))) {
                        PillpopperLog.say("Time Zone got changed. Needs to be called AdjustPillSchedule");

                        TimeZone tz = TimeZone.getDefault();

                        JSONObject setPrefJsonObject = new JSONObject();
                        JSONObject setUserPrefJsonObject = new JSONObject();

                        JSONObject pillpopperRequest = new JSONObject();
                        JSONObject pillpopperRequestForSetPref = new JSONObject();


                        setPrefJsonObject.put("action", "SetPreferences");
                        setPrefJsonObject.put("clientVersion", Util.getAppVersion(context));
                        setPrefJsonObject.put("userId", FrontController.getInstance(context).getPrimaryUserIdIgnoreEnabled());
                        setPrefJsonObject.put("partnerId", AppConstants.EDITION);
                        setPrefJsonObject.put("language", Locale.getDefault().toString());
                        setPrefJsonObject.put("hardwareId", UniqueDeviceId.getHardwareId(context));
                        setPrefJsonObject.put("targetUserId", userID);
                        setPrefJsonObject.put("apiVersion", "Version 6.0.4");
                        setPrefJsonObject.put("deviceToken", "");
                        setPrefJsonObject.put("replayId", getRandomGuid());


                        setUserPrefJsonObject.put("tz_secs", String.valueOf(tz.getOffset(GregorianCalendar.getInstance().getTimeInMillis()) / 1000));
                        setUserPrefJsonObject.put("tz_name", tz.getDisplayName());
                        setUserPrefJsonObject.put("userData", mSharedPrefManager.getString(AppConstants.KP_GUID, ""));
                        setUserPrefJsonObject.put("dstOffset_secs", String.valueOf(getDSTOffsetValue()));

                        setPrefJsonObject.put("preferences", setUserPrefJsonObject);

                        pillpopperRequestForSetPref.put("pillpopperRequest", setPrefJsonObject);

                        requestArray.put(pillpopperRequest);
                        requestArray.put(pillpopperRequestForSetPref);
                        return requestArray;
                    }
                }
            }
            return requestArray;
        } catch (Exception exception) {
            PillpopperLog.say("Exception while preparing the AdjustPill Request Model", exception);
        }
        return null;
    }

    public static String isNull(String value) {
        if (value == null) {
            return "";
        } else {
            return value;
        }
    }

    public static String describeDrugAsHtml(Context ctx, PillpopperAppContext context, Drug d) {
        PillpopperStringBuilder sb = new PillpopperStringBuilder(ctx, context);

        // Drug name
        if (d == null)
            sb.appendColumn(R.string.drug_name);
        else
            sb.appendColumn(d.getName());

        // Drug type
        if (sb.isPremium()) {
            if (d == null)
                sb.appendColumn(R.string.drug_type);
            else {
                if (d.getDoseData() != null && d.getDrugType() != null && d.getDrugType().getDrugTypeName() != null) {
                    sb.appendColumn(d.getDrugType().getDrugTypeName());
                } else {
                    sb.appendColumn("");
                }
            }
        }

        // Dosage
        if (d == null)
            sb.appendColumn(R.string.dosage);
        else {
            if (d.getDoseData() != null && d.getDosageDescription(ctx) != null) {
                sb.appendColumn(d.getDosageDescription(ctx));
            } else {
                if (d.getDose() != null) {
                    sb.appendColumn(d.getDose());
                } else {
                    sb.appendColumn("");
                }
            }
        }
        // For
        if (sb.isPremium() && context.isTrackingMultiplePeople()) {
            if (d == null)
                sb.appendColumn(R.string._for);
            else
                sb.appendColumn(Person.describePerson(sb.getContext(), d.getPerson()));
        }

        // Directions
        if (d == null)
            sb.appendColumn(R.string.drug_directions);
        else
            sb.appendColumn(null != d.getDirections() ? d.getDirections() : "");
        String scheduleChoice = Util.getScheduleChoice(d);
        // Schedule
        Schedule.describeAsHtml(ctx,sb, scheduleChoice,d == null ? null : d.getSchedule(),
                d == null ? null : (d.getPreferences() == null ? null : d.getPreferences().getPreference("maxNumDailyDoses")));

        // Refill info
        if (sb.isPremium() && context.isTrackingInventory()) {
            if (d == null) {
                sb.appendColumn(R.string._units_in_stock);
            } else {
                sb.appendColumn(d.describeUnitsRemaining());
            }
        }

        if (sb.isPremium() && context.isTrackingRefillsRemaining()) {
            if (d == null) {
                sb.appendColumn(R.string.refills_remaining);
            } else {
                sb.appendColumn(Util.getTextFromLong(d.getRefillsRemaining()));
            }
        }

        // Prescription info
        if (sb.isPremium() && context.isUsingDoctorPharmacy()) {
            if (d == null) {
                sb.appendColumn(R.string._doctor);
                sb.appendColumn(R.string._pharmacy);
                sb.appendColumn(R.string._prescription_num);
            } else {
                sb.appendColumn(Contact.getName(d.getDoctor()));
                sb.appendColumn(Contact.getName(d.getPharmacy()));
                sb.appendColumn(d.getPrescriptionNum());
            }
        }

        // Notes, if any
        if (d == null)
            sb.appendColumn(R.string._notes);
        else
            sb.appendColumn(d.getNotes());

        return sb.toString();
    }


    public static String convertDateLongToIso(String longDate) {
        //	yyyy-MM-dd'T'HH:mm:ss
        if (longDate == null || longDate.trim().isEmpty()) return null;
        if (("-1").equalsIgnoreCase(longDate)) return "-1";
        if (longDate.contains("T")) return longDate;
        try {
            Date date = new Date(Long.parseLong(longDate) * 1000); // seconds to milliseconds
            SimpleDateFormat sdf = new SimpleDateFormat(ISO_DATETIME_FORMAT);
            String isoDate = sdf.format(date);
            return isoDate;
        } catch (Exception ex) {
            return longDate;
        }
    }

    public static String convertDateIsoToLong(String dateStr) {
        if (dateStr == null || dateStr.trim().isEmpty()) return null;
        if (("-1").equalsIgnoreCase(dateStr)) return "-1";
        if (!dateStr.contains("T")) return dateStr;
        SimpleDateFormat sdf = new SimpleDateFormat(ISO_DATETIME_FORMAT);
        Date longDate = null;
        try {
            longDate = sdf.parse(dateStr);
        } catch (ParseException e) {
            PillpopperLog.say("Opps! DateIsotoLongFormatException ", e);
            return dateStr;
        }
        return String.valueOf(longDate.getTime() / 1000); //seconds
    }

    public static void disableAutofill(Context cxt, View view) {
        if (Build.VERSION.SDK_INT >= 26) {
            AutofillManager autofillManager = cxt.getSystemService(AutofillManager.class);
            if (null != autofillManager) {
                view.setImportantForAutofill(IMPORTANT_FOR_AUTOFILL_NO_EXCLUDE_DESCENDANTS);
                autofillManager.disableAutofillServices();
            }
        }
    }


    private static void initTZParams() {
        requestTZParams = null;
        requestTZParams = new ArrayList<>();
        requestTZParams.add(PillpopperConstants.LAST_TAKEN_TZSECS);
        requestTZParams.add(PillpopperConstants.EFF_LAST_TAKEN_TZSECS);
        requestTZParams.add(PillpopperConstants.NOTIFY_AFTER_TZSECS);
        requestTZParams.add(PillpopperConstants.MISSED_DOSES_LAST_CHECKED_TZSECS);
    }

    private static void initDSTParams() {
        requestDSTParams = null;
        requestDSTParams = new ArrayList<>();
        requestDSTParams.add(START);
        requestDSTParams.add(END);
        requestDSTParams.add(LAST_TAKEN);
        requestDSTParams.add(EFF_LAST_TAKEN);
        requestDSTParams.add(NOTIFY_AFTER);
        requestDSTParams.add(SCHEDULE_DATE);
    }

    public static String getHomeDate() {
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE, MMMM d");
        return sdf.format(new Date()).toUpperCase();
    }

    public static String getHomeGreeting(Context context) {
        String greeting = "";
        int timeOfDay = Calendar.getInstance().get(Calendar.HOUR_OF_DAY);
        if (timeOfDay >= 0 && timeOfDay < 12) {
            greeting = AppConstants.GOOD_MORNING;
        } else if (timeOfDay >= 12 && timeOfDay < 17) {
            greeting = AppConstants.GOOD_AFTERNOON;
        } else if (timeOfDay >= 17 && timeOfDay < 24) {
            greeting = AppConstants.GOOD_EVENING;
        }
        return greeting;
    }

    public static String getFirstName(String drugName) {

        int refPoint = null != drugName ? drugName.indexOf("(") : -1;
        if (refPoint == -1) {
            return drugName;

        }
        if (!Util.isEmptyString(drugName)) {
            PillpopperLog.say(refPoint + "refpoint :: length -> " + drugName.length());
            String drugFirstName = drugName.substring(0, refPoint);
            if (drugFirstName != null && !drugFirstName.equals("")) {
                return drugFirstName;
            }
        }
        return "";
    }

    public static void saveCardIndex(Context context, int cardIndex) {
        PillpopperLog.say("expanded card saving index : " + String.valueOf(cardIndex));
        SharedPreferenceManager mSharedPrefManager = SharedPreferenceManager.getInstance(context, AppConstants.AUTH_CODE_PREF_NAME);
        mSharedPrefManager.putString(AppConstants.EXPANDED_CARD_INDEX_KEY, String.valueOf(cardIndex), false);
    }

    public static String getKeyValueFromAppProfileRuntimeData(String key) {
        Map<String, String> appProfileConfigList = TTGRuntimeData.getInstance().getConfigListParams();

        if (null != appProfileConfigList) {
            if (appProfileConfigList.isEmpty() || (!appProfileConfigList.containsKey(key) || (appProfileConfigList.get(key) == null || isEmptyString(appProfileConfigList.get(key))))) {
                return null;
            } else {
                return appProfileConfigList.get(key);
            }
        }
        return null;
    }


    public static boolean isAppProfileDownloadTimeMoreThan15Min(Context context) {
        try {
            SharedPreferenceManager manager = SharedPreferenceManager.getInstance(
                    context, AppConstants.AUTH_CODE_PREF_NAME);
            long isLastCallTimeFifteenMinAgo = calculateMinDifference(System.currentTimeMillis(), manager.getLong(AppConstants.APP_PROFILE_INVOKED_TIMESTAMP, 0));
            return isLastCallTimeFifteenMinAgo > AppConstants.APP_PROFILE_TIMER;
        } catch (Exception ex) {
            LoggerUtils.exception(ex.getMessage());
            return true;
        }
    }

    /**
     * Method to calculate the minute difference
     *
     * @param cal1
     * @param cal2
     * @return returns the difference in the minute
     */
    public static long calculateMinDifference(long cal1, long cal2) {
        return TimeUnit.MILLISECONDS.toMinutes(cal1 - cal2);
    }


    /**
     * Returns if the
     *
     * @param context
     * @return
     */
    public static boolean isAppProfileCallRequired(Context context) {
        if (isAppProfileDownloadTimeMoreThan15Min(context)) {
            return true;
        }
        if (isNetworkAvailable(context) && (TTGRuntimeData.getInstance().getConfigListParams() == null ||
                TTGRuntimeData.getInstance().getConfigListParams().size() == 0 ||
                getKeyValueFromAppProfileRuntimeData(AppConstants.KEY_WS_SECURED_BASE_URL) == null ||
                getKeyValueFromAppProfileRuntimeData(AppConstants.KEY_WS_NON_SECURED_BASE_URL) == null ||
                getKeyValueFromAppProfileRuntimeData(AppConstants.KEY_SIGN_IN_URL) == null ||
                getKeyValueFromAppProfileRuntimeData(AppConstants.KEY_GET_SYSTEM_URL) == null ||
                getKeyValueFromAppProfileRuntimeData(AppConstants.KEY_KEEP_ALIVE_URL) == null ||
                getKeyValueFromAppProfileRuntimeData(AppConstants.KEY_PILL_POPPER_SECURED_BASE_URL) == null ||
                getKeyValueFromAppProfileRuntimeData(AppConstants.KEY_PILL_POPPER_NON_SECURED_BASE_URL) == null ||
                getKeyValueFromAppProfileRuntimeData(AppConstants.KEY_KP_SSO_COOKIE_NAME) == null ||
                getKeyValueFromAppProfileRuntimeData(AppConstants.KEY_KP_SSO_COOKIE_DOMAIN) == null ||
                getKeyValueFromAppProfileRuntimeData(AppConstants.KEY_KP_SSO_COOKIE_PATH) == null ||
                getKeyValueFromAppProfileRuntimeData(AppConstants.KEY_KP_SSO_COOKIE_STORE_IS_SECURE) == null) ||
                RunTimeData.getInstance().getRegionListParams() == null ||
                RunTimeData.getInstance().getRegionListParams().size() == 0 ||
                RunTimeData.getInstance().getRegionListParams().isEmpty() ||
                isEmptyKeyOrValueFoundInRegionList()) {
            return true;
        }
        return false;
    }

    public static int convertToDp(int input, Context context) {
        // Get the screen's density scale
        final float scale = context.getResources().getDisplayMetrics().density;
        // Convert the dps to pixels, based on density scale
        return (int) (input * scale + 0.5f);
    }

    public void resetHomeScreenCardsFlags() {
        RunTimeData.getInstance().setHomeCardsShown(false);
        PillpopperRunTime.getInstance().setIsFirstTimeSyncDone(false);
        RunTimeData.getInstance().setFirstTimeLandingOnHomeScreen(false);
        RunTimeData.getInstance().setInitialGetStateCompleted(false);
    }

    public void insertPastRemindersPillIdsIntoDB(Context context, LinkedHashMap<String, LinkedHashMap<Long, List<Drug>>> masterHashMap) {
        for (Map.Entry<String, LinkedHashMap<Long, List<Drug>>> _entry : masterHashMap.entrySet()) {
            LinkedHashMap<Long, List<Drug>> list = _entry.getValue();
            for (Map.Entry<Long, List<Drug>> entry : list.entrySet()) {
                List<Drug> drugs = entry.getValue();
                long time = entry.getKey();
                for (Drug drug : drugs) {
                    try {
                        PillpopperLog.say("Past Reminder - Inserting pill id : " + drug.getGuid() + " : Time is :" + time);
                        FrontController.getInstance(context).insertPastReminderPillId(drug.getGuid(), time);
                    } catch (Exception ex) {
                        PillpopperLog.say("Error: " + ex.getMessage() + " Past Reminder - Inserting pill id : " + drug.getGuid() + " : Time is :" + time);
                    }
                }
            }
        }
    }

    public void removePillSchedulesFromReminders(Context context, String guid) {
        // removing schedules from Late reminders runtime map and past reminder table
        List<String> usersToBeRemoved = new ArrayList<>();
        LinkedHashMap<String, LinkedHashMap<Long, List<Drug>>> passedRemindersHashMapByUserId = PillpopperRunTime.getInstance().getPassedReminderersHashMapByUserId();
        if (null != passedRemindersHashMapByUserId && !passedRemindersHashMapByUserId.isEmpty()) {
            for (Map.Entry<String, LinkedHashMap<Long, List<Drug>>> _entry : passedRemindersHashMapByUserId.entrySet()) {
                LinkedHashMap<Long, List<Drug>> list = _entry.getValue();
                String userID = _entry.getKey();
                List<Drug> drugList = new ArrayList<>();
                for (Map.Entry<Long, List<Drug>> entry : list.entrySet()) {
                    for (Drug d : entry.getValue()) {
                        if (!d.getGuid().equalsIgnoreCase(guid)) {
                            drugList.add(d);
                        }
                    }
                }
                if (drugList.isEmpty()) {
                    usersToBeRemoved.add(userID);
                }
            }
            for (String userId : usersToBeRemoved) {
                passedRemindersHashMapByUserId.remove(userId);
            }

            // removing from passed reminder table
            FrontController.getInstance(context).removePillFromPassedReminderTable(context, guid);

            PillpopperRunTime.getInstance().setPassedReminderersHashMapByUserId(passedRemindersHashMapByUserId);
        }
    }

    public void setChangeImagePopUpMenuVisibility(Context context, Drug _editDrug, PopupMenu imageTakingMenu, boolean isDefaultImage) {

        String imageChoice = _editDrug.getPreferences().getPreference("defaultImageChoice");

        MenuItem deleteImage = imageTakingMenu.getMenu().findItem(R.id.delete_image);
        deleteImage.setVisible(isDefaultImage);

        MenuItem myKpMedsImage = imageTakingMenu.getMenu().findItem(R.id.my_kp_meds_image);
        myKpMedsImage.setVisible(false);
        // When Med is Managed, and FDB Image is available in DB but not Set as default -> set "My KP Meds Image" to visible
        if (_editDrug.isManaged()) {
            if (FrontController.getInstance(context).isFDBImageAvailable(_editDrug.getGuid())
                    && (null != imageChoice && (imageChoice.equalsIgnoreCase(AppConstants.IMAGE_CHOICE_NO_IMAGE)
                    || imageChoice.equalsIgnoreCase(AppConstants.IMAGE_CHOICE_CUSTOM)))) {
                myKpMedsImage.setVisible(true);
                if (!imageChoice.equalsIgnoreCase(AppConstants.IMAGE_CHOICE_CUSTOM)) {
                    deleteImage.setVisible(false);
                }
            } else {
                myKpMedsImage.setVisible(false);
                if (("NOTFOUND").equalsIgnoreCase(_editDrug.getPreferences().getPreference("defaultImageChoice")) || (AppConstants.IMAGE_CHOICE_NO_IMAGE).equalsIgnoreCase(_editDrug.getPreferences().getPreference("defaultImageChoice"))) {
                    deleteImage.setVisible(false);
                }else{
                    deleteImage.setVisible(true);
                }
            }
        }
    }


    /**
     * Common method for configuring the javascript or disable for a webview
     *
     * @return
     */
    public static boolean checkForJavaScriptEnablingOption(String screenName) {
        return "guide".equalsIgnoreCase(screenName);
    }

    public static boolean urlHasRequiredString(@NotNull String url) {
        return url.contains(AppConstants.MESSAGE_DETAIL_IS_DEV_URL) || url.contains(AppConstants.MESSAGE_DETAIL_IS_QA_URL) || url.contains(AppConstants.MESSAGE_DETAIL_IS_PP_OR_PROD_URL)
                || url.contains(AppConstants.MESSAGE_DETAIL_KP_ORG_URL) || url.contains(AppConstants.MESSAGE_DETAIL_KAISER_PERMANENTE_ORG_URL) || url.contains(AppConstants.MESSAGE_DETAIL_KP_DOC_ORG_URL);
    }


    public static Map<String, String> buildHeadersForSecretQuestions(String userAgentCategory, String androidOsVersion, String userAgentType, String apiKey, String appName) {
        Map<String, String> headers = new HashMap<>();

        if (null != userAgentCategory && userAgentCategory.length() > 0) {
            headers.put("X-useragentcategory", userAgentCategory);
        }
        if (null != androidOsVersion && androidOsVersion.length() > 0) {
            headers.put("X-osversion", androidOsVersion);
        }
        if (null != userAgentType && userAgentType.length() > 0) {
            headers.put("X-useragenttype", userAgentType);
        }
        if (null != apiKey && apiKey.length() > 0) {
            headers.put("X-apiKey", apiKey);
        }
        if (null != appName && appName.length() > 0) {
            headers.put("X-appName", appName);
        }
        headers.put("Content-Type","application/json");
        List<HttpCookie> listCookies = TTGHttpUrlConnection.cookieManager.getCookieStore().getCookies();
        StringBuilder cookies = new StringBuilder();
        if (listCookies != null) {
            for (HttpCookie cookie : listCookies) {
                if (cookie.getName().contains("LtpaToken2")){
                    continue;
                }
                LoggerUtils.info("c======" + cookie.getName() + "="
                        + cookie.getValue() + "; domain="
                        + cookie.getDomain());
                String cookieValue;
                cookieValue = cookie.getName() + "=" + cookie.getValue() + "; domain=" + cookie.getDomain();
                cookies.append(cookieValue);
                cookies.append("; ");
            }
        }

        headers.put("Cookie", cookies.toString());
        return headers;
    }

    public static boolean isActiveInterruptSession() {
        if (RunTimeData.getInstance().isInturruptScreenVisible()) {
            if ((System.currentTimeMillis() - AppConstants.TIMEOUT_PERIOD < RunTimeData.getInstance().getInturruptScreenEnteredTimeStamp())) {
                return false;
            } else if (System.currentTimeMillis() - AppConstants.TIMEOUT_PERIOD > RunTimeData.getInstance().getInturruptScreenEnteredTimeStamp()) {
                RunTimeData.getInstance().setUserLogedInAndAppTimeout(true);
                RunTimeData.getInstance().setInterruptScreenBackButtonClicked(true);
                return true;
            }
        }
        return true;
    }

    public static void resetRuntimeInturruptFlags() {
        RunTimeData.getInstance().setSaveTempUserNameForInterrupt(null);
        RunTimeData.getInstance().setTimeOutOccuredDuringInturruptBackGround(false);
    }

    /**
     * Three digit secure random number for history OpeID if any exception occures.
     *
     * @return
     */
    public static int getThreeDigitSecureRandom() {
        SecureRandom random = new SecureRandom();
        int num = random.nextInt(999);
        return num;
    }

    public void checkAndLogMissedDoseEvents(Context mContext) {
        UniqueDeviceId.getHardwareId(mContext);
        for (final Drug d : FrontController.getInstance(mContext).getDrugListForOverDue(mContext)) {
            LoggerUtils.info("Debug Drug - " + d.getName());
            Drug drug = apply31DaysCapForMissedDoseLastChecked(d, mContext);
            if (!TextUtils.isEmpty(drug.getPreferences().getPreference("missedDosesLastChecked")) && !drug.getPreferences().getPreference("missedDosesLastChecked").equals("-1")) {
                PillpopperTime missedDosesLastChecked = Util.convertFloattoPillpopperTime(drug.getPreferences().getPreference("missedDosesLastChecked"));
                LoggerUtils.info("Debug missedDosesLastChecked- " + Util.convertDateLongToIso(String.valueOf(missedDosesLastChecked.getGmtSeconds())));
                if ((PillpopperTime.now().getGmtSeconds() - missedDosesLastChecked.getGmtSeconds()) > 24 * 60 * 60) {
                    LoggerUtils.info("Debug miss dose event");
                     logMissedDoses(drug, mContext.getApplicationContext());
                }
            }
        }
    }

    private Drug apply31DaysCapForMissedDoseLastChecked(Drug drug, Context context) {
        String missedDosesLastChecked = drug.getPreferences().getPreference("missedDosesLastChecked");
        if (!TextUtils.isEmpty(missedDosesLastChecked) && !"-1".equalsIgnoreCase(missedDosesLastChecked)) {

            PillpopperTime missedDosesLastCheckTime = Util.convertFloattoPillpopperTime(missedDosesLastChecked);
            PillpopperDay missedDoseLastCheckedDay = new PillpopperTime(missedDosesLastCheckTime).getLocalDay();
            PillpopperDay thirtyOneDaysOlderPillPopperDay = PillpopperTime.now().get31DaysOldLocalDay();

            long time = thirtyOneDaysOlderPillPopperDay.atLocalTime(new HourMinute(0, 0)).getGmtSeconds();

            PillpopperLog.say("MissPill Correction  missedDoseLastCheck is :  " + PillpopperTime.getDebugString(missedDosesLastCheckTime)
                    + " And 31 days before time is : " + PillpopperTime.getDebugString(new PillpopperTime(time)));

            // for monthly medication the missedDosesLastChecked value will be the latest value until he acts on the reminder.
            if(drug.getSchedule().getDayPeriod() == 30){
                drug.getPreferences().setPreference("missedDosesLastChecked", String.valueOf(missedDosesLastCheckTime.getGmtSeconds()));
                FrontController.getInstance(context).updateLastMissedCheck(drug.getGuid(), String.valueOf(missedDosesLastCheckTime.getGmtSeconds()));
            }else {
                //If the missedDoseLastCheck value is older than the 31 days, hence restrict to 31days.
                if (missedDoseLastCheckedDay.before(thirtyOneDaysOlderPillPopperDay)) {
                    PillpopperLog.say("MissPill Correction Since the missedDoseLastChecked value is older than the 31 days from today. Hence updating ");
                    drug.getPreferences().setPreference("missedDosesLastChecked", String.valueOf(new PillpopperTime(time).getGmtSeconds()));
                    FrontController.getInstance(context).updateLastMissedCheck(drug.getGuid(), String.valueOf(new PillpopperTime(time).getGmtSeconds()));
                } else {
                    // missedDoseLastChecked value is less than 31 days from today. hence we can keep the older value only. Not going to change.
                    PillpopperLog.say("MissPill Correction Since the missedDoseLastChecked value is not older than the 31 days from today.");
                }
            }
        } else {
            PillpopperLog.say("MissPill Correction is not required since its either -1 or null");
        }
        return drug;
    }

    //    Later in future if any new region is introduced then add the region and region code to the below enum
    public enum UserRegion {

        MRN("MRN", "NCAL"),
        SCA("SCA", "SCAL"),
        COL("COL", "CO"),
        GGA("GGA", "GA"),
        HAW("HAW", "HI"),
        MID("MID", "MAS"),
        KNW("KNW", "NW");

        private String region;
        private String regionCode;

        UserRegion(String region, String regionCode) {
            this.region = region;
            this.regionCode = regionCode;
        }

        public String getRegion() {
            return region;
        }

        public String getRegionCode() {
            return regionCode;
        }
    }

    public static String getUserRegionValue(String region) {
        for (UserRegion userRegion : UserRegion.values()) {
            if (!TextUtils.isEmpty(region) && region.equalsIgnoreCase(userRegion.getRegion())) {
                return userRegion.getRegionCode();
            }
        }
        return "";
    }

    /**
     * Gets the Regions List from the AppProfile runtime data
     *
     * @return
     */
    public static List<String> getRegionsFromAppProfileData() {
        Map<String, Boolean> regionsMap = RunTimeData.getInstance().getRegionListParams();

        if (null != regionsMap && !regionsMap.isEmpty()) {
            return new ArrayList<>(regionsMap.keySet());
        }
        return null;
    }

    /**
     * Returns True if any key or value found it as NULL/Empty in the RegionsList map of appprofile runtime data
     *
     * @return
     */
    private static boolean isEmptyKeyOrValueFoundInRegionList() {
        Map<String, Boolean> regionsMap = RunTimeData.getInstance().getRegionListParams();
        try {
            if (null != regionsMap && !regionsMap.isEmpty()) {
                for (Map.Entry<String, Boolean> entry : regionsMap.entrySet()) {
                    if (isEmptyString(entry.getKey()) || isEmptyString(String.valueOf(entry.getValue()))) {
                        return true;
                    }
                }
            }
        } catch (Exception e) {
            PillpopperLog.exception(e.getMessage());
        }
        return false;
    }

    public static Region parseRegionJson(String regionResponse, String region) {
        Gson gson = new Gson();

        try {
            RegionResponse regionsResponse = gson.fromJson(regionResponse, RegionResponse.class);
            for (Region reg : regionsResponse.getRegions()) {
                if (region.equalsIgnoreCase(reg.getCode())) {
                    return reg;
                }
            }
        } catch (Exception e) {
            LoggerUtils.exception(e.getMessage());
        }
        return null;
    }


    public static Region getRegion(String regionResponse, String region) {
        Gson gson = new Gson();

        try {
            JSONObject jsonObject = new JSONObject(regionResponse);
            RegionResponse regionsResponse = gson.fromJson(jsonObject.toString(), RegionResponse.class);
            for (Region reg : regionsResponse.getRegions()) {
                if (region.equalsIgnoreCase(reg.getCode())) {
                    return reg;
                }
            }
        } catch (Exception e) {
            LoggerUtils.exception(e.getMessage());
        }
        return null;
    }

    public static void writeStringAsFile(Context context, final String fileContents, String fileName) {
        FileWriter out = null;
        try {
            out = new FileWriter(new File(context.getFilesDir(), fileName));
            out.write(fileContents);
            out.close();
        } catch (Exception e) {
            LoggerUtils.exception(e.getMessage());
        } finally {
            if (out != null) {
                closeSilently(out);
            }
        }
    }

    public static String readFileAsString(Context context, String fileName) {
        StringBuilder stringBuilder = new StringBuilder("");
        String line;
        BufferedReader in = null;

        try {
            in = new BufferedReader(new FileReader(new File(context.getFilesDir(), fileName)));
            while ((line = in.readLine()) != null) stringBuilder.append(line);

        } catch (FileNotFoundException e) {
            LoggerUtils.exception(e.getMessage());
        } catch (Exception e) {
            LoggerUtils.exception(e.getMessage());
        } finally {
            if (in != null) {
                closeSilently(in);
            }
        }

        return stringBuilder.toString();
    }

    public static void deleteRegionContactFile(Context context) {
        File root = context.getFilesDir();
        File file = new File(root, "RegionContacts.txt");
        if (file.exists()) {
            file.delete();
        }
    }

    /**
     * Loops through the regionList comes from AppProfile with loggedInUser region and assign the value to configurable field.
     *
     * @param context
     */
    public static void initializeRefillNativeFl(Context context) {
        String loggedInUserRegion = ActivationController.getInstance().fetchUserRegion(context);
        Map<String, Boolean> regionListParams = RunTimeData.getInstance().getRegionListParams();
        if (!isEmptyString(loggedInUserRegion) && !regionListParams.isEmpty()) {
            for (Map.Entry<String, Boolean> entry : regionListParams.entrySet()) {
                if (loggedInUserRegion.equalsIgnoreCase(entry.getKey())) {
                    AppConstants.IS_NATIVE_RX_REFILL_REQUIRED = entry.getValue();
                }
            }
        }
    }

    public void loadValuesForRxRefillActivity(Context context) {
        ActivationController activationController = ActivationController.getInstance();
        RefillRuntimeData refillRuntimeData = RefillRuntimeData.getInstance();
        refillRuntimeData.setUserId(activationController.getUserId(context));
        refillRuntimeData.setSSOSessionId(activationController.getSSOSessionId(context));
        refillRuntimeData.setAPP_ID(AppConstants.APP_ID);
        refillRuntimeData.setAppVersion(ActivationUtil.getAppVersion(context));
        refillRuntimeData.setDeviceId(ActivationUtil.getDeviceId(context));
        refillRuntimeData.setFdbImageURL(AppConstants.ConfigParams.getFdbImageURL());
        refillRuntimeData.setKeepAliveCookieDomain(AppConstants.ConfigParams.getKeepAliveCookieDomain());
        refillRuntimeData.setSslSocketFactory(TTGRuntimeData.getInstance().getSslSocketFactory());
        refillRuntimeData.setRegion(activationController.fetchUserRegion(context));
        refillRuntimeData.setMappedRegionValue(Util.getUserRegionValue(activationController.fetchUserRegion(context)));
        refillRuntimeData.setClientID(AppConstants.CLIENT_ID);
        refillRuntimeData.setClientSecret(AppConstants.CLIENT_SECRET);
        refillRuntimeData.setAPIManagerTokenURL(AppConstants.ConfigParams.getAPIManagerTokenBaseURL());
        refillRuntimeData.setUserEmail(activationController.getUserEmail(context));
        refillRuntimeData.setUserGender(FrontController.getInstance(context).getUserGender());
        refillRuntimeData.setUserAge(FrontController.getInstance(context).getUserAge());
        refillRuntimeData.setApiKey(AppConstants.APIKEY);
        refillRuntimeData.setAppName(AppConstants.APPNAME);
        String environment = TTGUtil.getEnvironment(context);
        if (FireBaseConstants.ENVIRONMENT_PR.equalsIgnoreCase(environment)) {
            refillRuntimeData.setEnvironment(environment);
        } else {
            refillRuntimeData.setEnvironment(EnvSwitchUtils.getCurrentEnvironmentName());
        }


        // the access token or refresh token will be null for the first time.
        // the access token and the refresh might get updated in Rx refill screen.
        // so avoiding re assignment for subsequent visits within same session
        if (null == refillRuntimeData.getAccessToken()) {
            refillRuntimeData.setAccessToken(activationController.getAccessToken(context));
        }
        if (null == refillRuntimeData.getRefreshToken()) {
            refillRuntimeData.setRefreshToken(activationController.getRefreshToken(context));
        }
        if (null == refillRuntimeData.getTokenType()) {
            refillRuntimeData.setTokenType(activationController.getTokenType(context));
        }
        if (!TextUtils.isEmpty(RunTimeData.getInstance().getServiceArea())) {
            refillRuntimeData.setServiceArea(RunTimeData.getInstance().getServiceArea());
        }

        refillRuntimeData.setLogging(AppConstants.isLogging());


        // All the required urls for RxRefill
        refillRuntimeData.setPrescriptionURL(getKeyValueFromAppProfileRuntimeData(RxRefillConstants.KEY_PRESCRIPTION_URL));
        refillRuntimeData.setFindByRxURL(getKeyValueFromAppProfileRuntimeData(RxRefillConstants.KEY_FINDBYRX_URL));
        refillRuntimeData.setEntitlementsURL(getKeyValueFromAppProfileRuntimeData(RxRefillConstants.KEY_ENTITLEMENT_URL));
        refillRuntimeData.setPharmacyContentURL(getKeyValueFromAppProfileRuntimeData(RxRefillConstants.KEY_PHARMACY_CONTENT_URL));
        refillRuntimeData.setPharmacyConfigurationURL(getKeyValueFromAppProfileRuntimeData(RxRefillConstants.KEY_PHARMACY_CONFIGURATION_URL));
        refillRuntimeData.setMemberInfoURL(getKeyValueFromAppProfileRuntimeData(RxRefillConstants.KEY_RX_MEMBER_INFO_URL));
        refillRuntimeData.setAddressURL(getKeyValueFromAppProfileRuntimeData(RxRefillConstants.KEY_RX_ADDRESS_URL));
        refillRuntimeData.setProfileURL(getKeyValueFromAppProfileRuntimeData(RxRefillConstants.KEY_RX_PROFILE_URL));
        refillRuntimeData.setTrialClaimsURL(getKeyValueFromAppProfileRuntimeData(RxRefillConstants.KEY_RX_TRIAL_CLAIMS_URL));
        refillRuntimeData.setManagePaymentWebURL(getKeyValueFromAppProfileRuntimeData(RxRefillConstants.KEY_MANAGE_PAYMENT_WEB_URL));
        refillRuntimeData.setPhoneURL(getKeyValueFromAppProfileRuntimeData(RxRefillConstants.KEY_RX_PHONE_URL));
        refillRuntimeData.setLocationsDBURL(getKeyValueFromAppProfileRuntimeData(RxRefillConstants.KEY_LOCATIONS_DB_URL));
        refillRuntimeData.setPlaceOrderUrl(getKeyValueFromAppProfileRuntimeData(RxRefillConstants.KEY_PLACE_ORDER_URL));
        refillRuntimeData.setDrugInfoUrl(getKeyValueFromAppProfileRuntimeData(RxRefillConstants.KEY_DRUG_INFO_URL));

    }

    public static void setTokenValuesForRxRefill(GetTokenResponseObj getTokenResponseObj){
        RefillRuntimeData refillRuntimeData = RefillRuntimeData.getInstance();
        if (isEmptyString(getTokenResponseObj.getAccess_token())) {
            refillRuntimeData.setAccessToken(getTokenResponseObj.getAccess_token());
        }

        if(isEmptyString(getTokenResponseObj.getRefresh_token())){
            refillRuntimeData.setRefreshToken(getTokenResponseObj.getRefresh_token());
        }

        if(isEmptyString(getTokenResponseObj.getToken_type())){
            refillRuntimeData.setTokenType(getTokenResponseObj.getToken_type());
        }
    }

    public static boolean isBatteryOptimizationAlertRequired(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // First Check the User Decision. If not opted then check the system settings for battery optimization.
                if (FrontController.getInstance(context).isBatteryOptmizationDecisionNotOpted(context)) {
                    PowerManager powerManager = (PowerManager) context.getApplicationContext().getSystemService(POWER_SERVICE);
                    return (!powerManager.isIgnoringBatteryOptimizations(context.getPackageName()));
                }
            }
        } catch (Exception e) {
            PillpopperLog.exception(e.getMessage());
        }
        return false;
    }

    public static boolean isBatteryOptimizationCardRequired(Context context) {
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                // First Check the User Decision. If not opted then check the system settings for battery optimization.
                if (FrontController.getInstance(context).showBatteryOptimizationCard(context)) {
                    PowerManager powerManager = (PowerManager) context.getApplicationContext().getSystemService(POWER_SERVICE);
                    return (!powerManager.isIgnoringBatteryOptimizations(context.getPackageName()));
                }
            }
        } catch (Exception e) {
            PillpopperLog.exception(e.getMessage());
        }
        return false;
    }

    public static void storeEnvironment(Context context) {
        try {
            if (null != context) {
                String environment = TTGUtil.getEnvironment(context);
                TTGRuntimeData.getInstance().setEnvironment(
                        null != environment ? environment : "pr");
            } else {
                TTGRuntimeData.getInstance().setEnvironment("pr");
            }
        } catch (Exception e) {
            PillpopperLog.exception(e.getMessage());
            //fallback
            TTGRuntimeData.getInstance().setEnvironment("pr");
        }
    }

    /**
     * Update the API Key for PP,PR with Environment switcher build.
     */
    public static void initAPIKeyAsPerEnvironment() {
        try {
            String environment = EnvSwitchUtils.getCurrentEnvironmentName();
            if (!isEmptyString(environment)) {
                // If it used with environment switcher
                LoggerUtils.info("initAPIKeyAsPerEnvironment : " + environment);
                if ("BETA".equalsIgnoreCase(environment) || "PP".equalsIgnoreCase(environment)) {
                    AppConstants.APIKEY = AppConstants.APIKEY_PR;
                }
            }
        } catch (Exception e) {
            LoggerUtils.exception(e.getMessage());
        }
    }

    public static void logFireBaseEventForDeviceFontScale(Context context) {
        try {
            SharedPreferenceManager manager = SharedPreferenceManager.getInstance(
                    context, AppConstants.AUTH_CODE_PREF_NAME);
            PillpopperLog.say("FireBase event for font scale");
            boolean logGAEventForFontScale = manager.getBoolean(AppConstants.LOG_GA_EVENT_FOR_DYNAMIC_FONT_SCALE, true);
            if (logGAEventForFontScale) {
                // get the device font scale
                float fontScaleValue = context.getResources().getConfiguration().fontScale;
                String fontScaleGAValue;
                if (fontScaleValue == AppConstants.FONT_SCALE_VALUE_DEFAULT) {
                    fontScaleGAValue = FireBaseConstants.LABEL_FONT_SETTING_DEFAULT;
                } else if (fontScaleValue < AppConstants.FONT_SCALE_VALUE_DEFAULT) {
                    fontScaleGAValue = FireBaseConstants.LABEL_FONT_SETTING_SMALL;
                } else {
                    fontScaleGAValue = FireBaseConstants.LABEL_FONT_SETTING_LARGE;
                }

                FireBaseAnalyticsTracker.getInstance().logEvent(context, FireBaseConstants.Event.DYNAMIC_FONT,
                        FireBaseConstants.ParamName.SIZE, fontScaleGAValue);

                manager.putBoolean(AppConstants.LOG_GA_EVENT_FOR_DYNAMIC_FONT_SCALE, false, false);
            }
        } catch (Exception ex) {
            PillpopperLog.say("exception while logGAEventForNotification");
        }
    }

    public static void applyStatusbarColor(Activity context, int colorCode) {
        try {
            if (null != context) {
                Window window = context.getWindow();
                window.setStatusBarColor(colorCode);
            }
        } catch (Exception e) {
            RxRefillLoggerUtils.exception(e.getMessage());
        }
    }

    public static void setVisibility(View[] view, int isVisible) {
        for (View views : view) views.setVisibility(isVisible);
    }

    public static String getWeekdayName(Context context, int weekdayNumber) {
        if (null == context) {
            return "";
        }
        switch (weekdayNumber) {
            case 1:
                return context.getString(R.string.txt_sunday);
            case 2:
                return context.getString(R.string.txt_monday);
            case 3:
                return context.getString(R.string.txt_tuesday);
            case 4:
                return context.getString(R.string.txt_wednesday);
            case 5:
                return context.getString(R.string.txt_thursday);
            case 6:
                return context.getString(R.string.txt_friday);
            case 7:
                return context.getString(R.string.txt_saturday);
            default:
                return "";
        }
    }

    public static void clearHasStatusUpdateValues(Context context) {
        SharedPreferenceManager mSharedPrefManager = SharedPreferenceManager.getInstance(
                context, AppConstants.AUTH_CODE_PREF_NAME);

        mSharedPrefManager.remove(AppConstants.KPHC_MEDS_STATUS_CHANGED);
        mSharedPrefManager.remove(AppConstants.MED_ARCHIVED_OR_REMOVED);
        mSharedPrefManager.remove(AppConstants.PROXY_STATUS_CODE);
        mSharedPrefManager.remove(AppConstants.MEDICATION_SCHEDULE_CHANGED);
        mSharedPrefManager.remove(AppConstants.HAS_STATUS_UPDATE_TIMESTAMP);
    }

    public static boolean hasPendingAlertsNeedForceSignIn(Context context) {

        SharedPreferenceManager mSharedPrefManager = SharedPreferenceManager.getInstance(
                context, AppConstants.AUTH_CODE_PREF_NAME);

        String proxyStatusChanged = mSharedPrefManager.getString(AppConstants.PROXY_STATUS_CODE, "");
        String medArchivedOrRemoved = mSharedPrefManager.getString(AppConstants.MED_ARCHIVED_OR_REMOVED, "");
        String kphcMedsStatusChanged = mSharedPrefManager.getString(AppConstants.KPHC_MEDS_STATUS_CHANGED, "");
        String medicationScheduleChanged = mSharedPrefManager.getString(AppConstants.MEDICATION_SCHEDULE_CHANGED, "");

        if (!Util.isEmptyString(proxyStatusChanged)) {
            if (!("P").equalsIgnoreCase(proxyStatusChanged)) {
                return true;
            }
        }

        if (!Util.isEmptyString(kphcMedsStatusChanged)) {
            if (!("P").equalsIgnoreCase(kphcMedsStatusChanged)) {
                return true;
            }
        }

        if (!Util.isEmptyString(medicationScheduleChanged)) {
            if (!("N").equalsIgnoreCase(medicationScheduleChanged)) {
                return true;
            }
        }

        if (!Util.isEmptyString(medArchivedOrRemoved)) {
            if (("Y").equalsIgnoreCase(medicationScheduleChanged)) {
                return true;
            }
        }

        LoggerUtils.info("Debug -- proxyStatusChanged -- " + proxyStatusChanged);
        LoggerUtils.info("Debug -- medArchivedOrRemoved -- " + medArchivedOrRemoved);
        LoggerUtils.info("Debug -- kphcMedsStatusChanged -- " + kphcMedsStatusChanged);
        LoggerUtils.info("Debug -- medicationScheduleChanged -- " + medicationScheduleChanged);

        return false;
    }

    public static boolean isHasStatusUpdateCallRequired(Context context) {
        try {
            SharedPreferenceManager manager = SharedPreferenceManager.getInstance(
                    context, AppConstants.AUTH_CODE_PREF_NAME);
            long isLastCallTimeFifteenMinAgo = calculateMinDifference(System.currentTimeMillis(), manager.getLong(AppConstants.HAS_STATUS_UPDATE_TIMESTAMP, 0));
            LoggerUtils.info("Debug -- HasStatusUpdate Response -- " + isLastCallTimeFifteenMinAgo);
            return isLastCallTimeFifteenMinAgo > AppConstants.DOWNLOAD_EXPIRY_LIMIT;
        } catch (Exception ex) {
            LoggerUtils.exception(ex.getMessage());
            return true;
        }
    }

    public static boolean isNonSecureAppProfileCallRequired(Context context) {
        return (isNetworkAvailable(context) && isAppProfileDownloadTimeMoreThan15Min(context)); // created new method, if we wanted to add any conditions we wont disturb the existing conditions.
    }

    public static boolean isProductionBuild() {
        return (BuildConfig.FLAVOR.equalsIgnoreCase("prod") || BuildConfig.FLAVOR.equalsIgnoreCase("pr") || BuildConfig.FLAVOR.equalsIgnoreCase("prodProduction"));
    }

    public static void loadExternalBrowser(Context context, String url) {
        try {
            Intent email = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            context.startActivity(email);
        } catch (Exception e) {
            LoggerUtils.exception(e.getMessage());
            /*GenericAlertDialog mAlertDialog = new GenericAlertDialog(context,
                    context.getString(R.string.content_unavailable), context.getString(R.string.content_unavailable_msg), context.getString(R.string.ok_text),
                    new DialogInterface.OnClickListener() {
                        public void onClick(DialogInterface dialog,
                                            int which) {
                            dialog.dismiss();
                        }
                    }, null, null);
            mAlertDialog.showDialog();*/
        }
    }
    public void showTeenAccountErrorAlert(String title, String message,Context context) {
        GenericAlertDialog alertDialog = new GenericAlertDialog(context, title, message, context.getResources().getString(R.string.ok_text),
                alertListener);
        if (null != alertDialog && !alertDialog.isShowing()) {
            alertDialog.showDialog();
        }
        RunTimeData.getInstance().setAlertDisplayedFlg(true);
    }
    private final DialogInterface.OnClickListener alertListener = (dialog, which) -> {
        RunTimeData.getInstance().setClickFlg(false);
        dialog.dismiss();
        RunTimeData.getInstance().setAlertDisplayedFlg(false);
        RunTimeData.getInstance().setRuntimeSSOSessionID(null);
        RunTimeData.getInstance().setUserLogedInAndAppTimeout(false);
        Util.resetRuntimeInturruptFlags();
    };

    public static String getSuffix(Integer value) {
        if (value >= 11 && value <= 13) {
            return "th";
        }
        switch (value % 10) {
            case 1:  return "st";
            case 2:  return "nd";
            case 3:  return "rd";
            default: return "th";
        }
    }

    public static void logFirebaseEventForAction(Context context, String action, int status) {
        if(action.equalsIgnoreCase(PillpopperConstants.ACTION_GET_STATE) || action.equalsIgnoreCase(PillpopperConstants.ACTION_HISTORY_EVENTS)){
            String event = HttpURLConnection.HTTP_OK == status
//                if the status is 200 i.e., success then comparing the action we get the success event string
                    ? action.equalsIgnoreCase(PillpopperConstants.ACTION_GET_STATE) ? FireBaseConstants.Event.GET_STATE_SUCCESS : FireBaseConstants.Event.GET_HISTORY_EVENT_SUCCESS
//                if the status is other than 200 we get failure event string
                    : action.equalsIgnoreCase(PillpopperConstants.ACTION_GET_STATE) ? FireBaseConstants.Event.GET_STATE_FAIL : FireBaseConstants.Event.GET_HISTORY_EVENT_FAIL;
            LoggerUtils.info("----Firebase----" + event);
            FireBaseAnalyticsTracker.getInstance().logEventWithoutParams(context, event);
        }
    }

    public static void resetScheduleData() {
        if(null != RunTimeData.getInstance().getScheduleData()) {
            RunTimeData.getInstance().getScheduleData().setScheduleTime(null);
            RunTimeData.getInstance().getScheduleData().setStartDate(null);
            RunTimeData.getInstance().getScheduleData().setEndDate(null);
            RunTimeData.getInstance().getScheduleData().setSelectedDays(null);
            RunTimeData.getInstance().getScheduleData().setDuration(0);
            RunTimeData.getInstance().getScheduleData().setDurationType(null);
        }
    }

    public static String setOnWeekdays(Context context, String weekdays) {
        StringBuilder days = new StringBuilder();
        if (weekdays != null) {
            if (weekdays.contains("1")) {
                if (days.toString().length() > 0) {
                    days.append(", ");
                }
                days.append(context.getString(R.string.txt_sunday));
            }
            if (weekdays.contains("2")) {
                if (days.toString().length() > 0) {
                    days.append(", ");
                }
                days.append(context.getString(R.string.txt_monday));
            }
            if (weekdays.contains("3")) {
                if (days.toString().length() > 0) {
                    days.append(", ");
                }
                days.append(context.getString(R.string.txt_tuesday));
            }
            if (weekdays.contains("4")) {
                if (days.toString().length() > 0) {
                    days.append(", ");
                }
                days.append(context.getString(R.string.txt_wednesday));
            }
            if (weekdays.contains("5")) {
                if (days.toString().length() > 0) {
                    days.append(", ");
                }
                days.append(context.getString(R.string.txt_thursday));
            }
            if (weekdays.contains("6")) {
                if (days.toString().length() > 0) {
                    days.append(", ");
                }
                days.append(context.getString(R.string.txt_friday));
            }
            if (weekdays.contains("7")) {
                if (days.toString().length() > 0) {
                    days.append(", ");
                }
                days.append(context.getString(R.string.txt_saturday));
            }
        }
        return String.valueOf(days);
    }

    /**
     * Adds all the cookies from token and keepalive response headers.
     * this will be passed to the web pages
     * @param cookies
     */
    public static void setAllCookies(List<TTGCookie> cookies){

        Map<String, List<String>> tokenCookieHeaders = TTGRuntimeData.getInstance().getHeadersMap();
        Map<String, String> tokenCookie = new HashMap<>();

        if (null != tokenCookieHeaders){
            for (Map.Entry<String, List<String>> entry : tokenCookieHeaders.entrySet()) {
                String key = entry.getKey();
                if (key != null && (key.equalsIgnoreCase(HttpHeaders.SET_COOKIE))) {
                    // parse list elements one by one
                    for (String cookieStr : entry.getValue()) {
                        if (null != cookieStr){
                            String[] splitCookieStr = cookieStr.split("=");
                            if (null != splitCookieStr && splitCookieStr.length > 1){
                                tokenCookie.put(splitCookieStr[0].trim(), cookieStr);
                            }
                        }
                    }
                }
            }
        }

        Map<String, List<String>> keepAliveCookieHeaders = TTGRuntimeData.getInstance().getKeepAliveHeaderMap();
        if (null != keepAliveCookieHeaders) {
            for (Map.Entry<String, List<String>> keepAliveCookie : keepAliveCookieHeaders.entrySet()) {
                String key = keepAliveCookie.getKey();
                if (key != null && (key.equalsIgnoreCase(HttpHeaders.SET_COOKIE))) {
                    // parse list elements one by one
                    for (String cookieStr : keepAliveCookie.getValue()) {
                        if (null != cookieStr) {
                            String[] splitCookieStr = cookieStr.split("=");
                            if (null != splitCookieStr && splitCookieStr.length > 1) {
                                tokenCookie.put(splitCookieStr[0].trim(), cookieStr);
                            }
                        }
                    }
                }
            }
        }

        for (Map.Entry<String, String> entry : tokenCookie.entrySet()) {
            try {
                for (HttpCookie httpCookie : HttpCookie.parse(entry.getValue())) {
                    TTGCookie vordalCookie = new TTGCookie();
                    vordalCookie.setName(httpCookie.getName());
                    vordalCookie.setValue(httpCookie.getValue());
                    vordalCookie.setPath(httpCookie.getPath());
                    vordalCookie.setDomain(httpCookie.getDomain());
                    vordalCookie.setSecure(httpCookie.getSecure());
                    cookies.add(vordalCookie);
                }
            } catch (IllegalArgumentException ignored) {
                // this string is invalid, jump to the next one.
            }
        }
    }

    public static boolean getTeenToggleEnabled(){
        boolean hasTeenToggleEnabled = false;
        List<User> userList = RunTimeData.getInstance().getRegistrationResponse().getResponse().getUsers();
        for(User user : userList){
            if(user.getUserType().equalsIgnoreCase("primary") && user.isTeenToggleEnabled()){
                hasTeenToggleEnabled = true;
                break;
            }
        }
        return hasTeenToggleEnabled;
    }
    public static String getFormattedString(Drug drug){
        return getDate(drug.getOverdueDate().getGmtMilliseconds())+ ", " + getTime(drug.getOverdueDate().getGmtMilliseconds());
    }

    public static void dismissForegroundAlertDialogIfAny() {
        AlertDialog mAlertDialog = RunTimeData.getInstance().getAlertDialogInstance();
        if (null != mAlertDialog && mAlertDialog.isShowing()) {
            mAlertDialog.dismiss();
            RunTimeData.getInstance().setAlertDialogInstance(null);
        }
    }

    public static String loadJSONContent(Context context, String filename) {
        String jsonStr = null;
        try {
            InputStream is = context.getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            jsonStr = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            LoggerUtils.exception(ex.getMessage());
        }
        return jsonStr;
    }
    public static List<AnnouncementsItem> getGenericCardsList(Context context){
        List<AnnouncementsItem> genericCardList = new ArrayList<>();
        String userRegionCode =Util.getUserRegionValue(ActivationController.getInstance().fetchUserRegion(context));
        String region = ActivationController.getInstance().fetchUserRegion(context);
        AnnouncementsResponse announcementsResponse = RunTimeData.getInstance().getAnnouncements();
        if (null != announcementsResponse) {
            List<AnnouncementsItem> announcementsList = announcementsResponse.getAnnouncements();
            if (null != announcementsList && !announcementsList.isEmpty()) {
                for (AnnouncementsItem a : announcementsList) {
                    if (null != a && a.getType().equalsIgnoreCase("home_card") && null != a.getTitle() && !Util.isEmptyString(a.getId() + "")) {
                        List<String> regions = a.getRegions();
                        // show the cards if regions is empty or null, or if present in the allowed regions list
                        if (regions.isEmpty() || regions.contains(userRegionCode) || regions.contains(region)) {
                            if(null!=a.getButtons() && a.getButtons().size()==2) {
                                Collections.sort(a.getButtons(), new ButtonsItem.ButtonItemComparator());
                            }
                            genericCardList.add(a);
                        }
                    }
                }
            }
        }
        Collections.sort(genericCardList,new AnnouncementsItem.AnnouncementComparator());
        return genericCardList;
    }

    public static List<AnnouncementsItem> getGenericBannerList(Context context, String baseScreen) {
        List<AnnouncementsItem> genericBannerList = new ArrayList<>();
        String userRegionCode = Util.getUserRegionValue(ActivationController.getInstance().fetchUserRegion(context));
        String region = ActivationController.getInstance().fetchUserRegion(context);
        AnnouncementsResponse announcementsResponse = RunTimeData.getInstance().getAnnouncements();
        if (null != announcementsResponse) {
            List<AnnouncementsItem> announcementsList = announcementsResponse.getAnnouncements();
            if (null != announcementsList && !announcementsList.isEmpty()) {
                for (AnnouncementsItem a : announcementsList) {
                    if (null != a && a.getType().equalsIgnoreCase("banner_full") || null != a && a.getType().equalsIgnoreCase("banner_short")) {
                        if (a.getBaseScreen().equalsIgnoreCase(baseScreen) && null != a.getTitle() && !Util.isEmptyString(a.getId() + "")) {
                            List<String> regions = a.getRegions();
                            // show the banner if regions is empty or null, or if present in the allowed regions list
                            if (regions.isEmpty() || regions.contains(userRegionCode) || regions.contains(region)) {
                                if(null!=a.getButtons() && a.getButtons().size()==2){
                                    Collections.sort(a.getButtons(),new ButtonsItem.ButtonItemComparator());
                                }
                                genericBannerList.add(a);
                            }
                        }
                    }
                }
            }
            Collections.sort(genericBannerList, new AnnouncementsItem.AnnouncementComparator());
        }
        return genericBannerList;
    }

    public static String getEventTime(Context context ,String timeStamp) {
        Date eventTime = new Date(Long.parseLong(timeStamp) * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat(DateFormat.is24HourFormat(context) ? "HH:mm" : "h:mm a", Locale.US);
        return sdf.format(eventTime);
    }

    public static String getEventDay(String timeStamp) {
        Date eventTime = new Date(Long.parseLong(timeStamp) * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("EEEE MM/dd/yyyy" , Locale.US);
        return sdf.format(eventTime);
    }
    public static boolean isOverdueAndPostponedEventSame(PillpopperTime drugOverdueDate, boolean isPostponedEventActive, String finalPostponedDateTime) {
        String postponedDateTime = Util.convertDateIsoToLong(finalPostponedDateTime);
        if(postponedDateTime==null){
            postponedDateTime = "";
        }
        return isPostponedEventActive && null != drugOverdueDate
                && postponedDateTime.equalsIgnoreCase(String.valueOf(drugOverdueDate.getGmtSeconds()));
    }
    public void deleteEmptyAndPostponeEntries(Context context,Drug editDrug) {
        FrontController.getInstance(context).deleteEmptyHistoryEntriesByPillID(editDrug.getGuid());
        List<HistoryEvent> latestPostponeEvents = FrontController.getInstance(context).getActivePostponedEvents(editDrug.getGuid());
        if(null != latestPostponeEvents && !latestPostponeEvents.isEmpty()){
            for(HistoryEvent event : latestPostponeEvents){
                editPostponeEvent(editDrug, event,context);
            }
        }
    }
    public static void editPostponeEvent(Drug drug, HistoryEvent latestPostponeEvent,Context context) {
        // editing postpone event isPostponeActive to false
        if (null != latestPostponeEvent && !Util.isEmptyString(latestPostponeEvent.getHistoryEventGuid())) {
            FrontController.getInstance(context).updateHistoryEventPreferences(latestPostponeEvent.getHistoryEventGuid(), "EditEvent", drug, 0);
            HistoryEditEvent historyEditEvent = FrontController.getInstance(context).getHistoryEditEventDetails(latestPostponeEvent.getHistoryEventGuid());
            historyEditEvent.setActionDateRequired(true);
            FrontController.getInstance(context).addLogEntry(context, Util.prepareLogEntryForEditHistoryEvent(context, historyEditEvent, null));
        }
    }
        public static void invokeFireBaseEvent(Context context, String timePeriod, String source) {
        if (null != context) {
            Bundle bundle = new Bundle();
            bundle.putString(FireBaseConstants.ParamName.TIME_PERIOD, timePeriod);
            if(!Util.isEmptyString(source)) {
                bundle.putString(FireBaseConstants.ParamName.SOURCE, source);
            }
            FireBaseAnalyticsTracker.getInstance().logEvent(context,FireBaseConstants.Event.SHOW_HISTORY,bundle);
        }
    }

    public static void firebaseEventForShowHistory(int selectedPosition, Context context,String source){
        switch (selectedPosition) {
                case 1:
                    invokeFireBaseEvent(context,FireBaseConstants.ParamValue.MONTH_1,source);
                    break;
                case 2:
                    invokeFireBaseEvent(context,FireBaseConstants.ParamValue.MONTHS_3,source);
                    break;
                case 3:
                    invokeFireBaseEvent(context,FireBaseConstants.ParamValue.YEAR_1,source);
                    break;
                case 4:
                    invokeFireBaseEvent(context,FireBaseConstants.ParamValue.YEARS_2,source);
                    break;
                default:
                    invokeFireBaseEvent(context,FireBaseConstants.ParamValue.DAYS_14,source);
        }
    }

    public static String getScheduleChoice(Drug drug){
        String scheduleChoice = AppConstants.SCHEDULE_CHOICE_UNDEFINED;
        if(null != drug && null != drug.getPreferences()){
            scheduleChoice = drug.getPreferences().getPreference("scheduleChoice");
        }
        return null != scheduleChoice ? scheduleChoice : AppConstants.SCHEDULE_CHOICE_UNDEFINED;
    }


    public static List<Drug> getOverdueDrugList(Context context) {
        ArrayList<Drug> overduedrugs = new ArrayList<>();
        for (final Drug d : FrontController.getInstance(context).getDrugListForOverDue(context)) {
            d.computeDBDoseEvents(context, d, PillpopperTime.now(), 60);
            if (d.isoverDUE() && (null == d.getSchedule().getEnd()
                    || (d.getSchedule().getEnd().equals(PillpopperDay.today()) ||
                    d.getSchedule().getEnd().after(PillpopperDay.today())))) {
                if (((!PillpopperRunTime.getInstance().isLauchingFromPast() && isEligibleEvent(d, context))
                        || (null != d.getPassedReminderTimes() && d.getPassedReminderTimes().size() > 0))) {
                    if ((PillpopperTime.now().getGmtMilliseconds() - d.getOverdueDate().getGmtMilliseconds()) < 24 * 60 * 60 * 1000) {
                        overduedrugs.add(d);
                    }
                }
            }
        }
        return overduedrugs;
    }

    public static boolean isEligibleEvent(Drug drug, Context context) {
        return null != drug.getPreferences() && (null != drug.getPreferences().getPreference("missedDosesLastChecked")
                && (drug.getOverdueDate().after(Util.convertStringtoPillpopperTime(drug.getPreferences().getPreference("missedDosesLastChecked"))))
                || String.valueOf(drug.getOverdueDate().getGmtSeconds()).equalsIgnoreCase(drug.getPreferences().getPreference("missedDosesLastChecked")))
                || (FrontController.getInstance(context).isEntryAvailableInPastReminder(drug.getGuid(), drug.getOverdueDate()))
                && (null != drug.getOverdueDate() && null != drug.getCreated() && !drug.getOverdueDate().before(drug.getCreated()));
    }

    public static void registerNotificationReceivers(Context context) {
        IntentFilter filter = new IntentFilter("com.montunosoftware.dosecast.NotificationBarOrderedBroadcastKP");
        try {
            if (null != RunTimeData.getInstance().getNotificationBarOrderedBroadcastHandler()) {
                context.getApplicationContext().unregisterReceiver(RunTimeData.getInstance().getNotificationBarOrderedBroadcastHandler());
            }
        } catch (Exception e) {
            PillpopperLog.say(e);
        } finally {
            RunTimeData.getInstance().setNotificationBarOrderedBroadcastHandler(new NotificationBarOrderedBroadcastHandler());
            context.getApplicationContext().registerReceiver(RunTimeData.getInstance().getNotificationBarOrderedBroadcastHandler(), filter);
        }
    }
    public static String getActionType(ArrayList<Drug> drugList,Context context){
        if(!drugList.isEmpty()){
            ArrayList<Drug> takenDrugList = new ArrayList<>();
            ArrayList<Drug> skippedDrugList = new ArrayList<>();
            ArrayList<Drug> postponedDrugList = new ArrayList<>();
            for (Drug d : drugList) {
                if (null != d.getGuid() && (d.getmAction() == PillpopperConstants.TAKEN ||d.getmAction() == PillpopperConstants.TAKE_EARLIER )) {
                    takenDrugList.add(d);
                }else if(null != d.getGuid() && d.getmAction() == PillpopperConstants.SKIPPED){
                    skippedDrugList.add(d);
                }else if(null != d.getGuid() && d.getmAction() == PillpopperConstants.TAKE_LATER){
                    postponedDrugList.add(d);
                }
            }
            if(skippedDrugList.size() == 0 && postponedDrugList.size() == 0){
                return context.getResources().getString(R.string.taken_action);
            }else if (takenDrugList.size() == 0 && postponedDrugList.size() == 0){
                return context.getResources().getString(R.string.skipped);
            }else if (skippedDrugList.size() == 0 && takenDrugList.size() == 0){
                return context.getResources().getString(R.string.postpone_action);
            }else{
                return context.getResources().getString(R.string.mixed_action);
            }
        }
        return "";
    }

    public static String get24FormatTimeFromHrMin(int nh, int nm) {
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Date dateNewTime;
        String newAddedTime = "";
        try {
            dateNewTime = sdf.parse(nh + ":" + nm);
            newAddedTime = sdf.format(dateNewTime);
        } catch (ParseException e) {
            LoggerUtils.exception("ParseException", e);
        }
        return newAddedTime;
    }

    public static String getAmPmTimeFromHrMin(int nh, int nm) {
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm", Locale.getDefault());
        Date dateNewTime;
        String newAddedTime = "";
        try {
            String amPMStr;
            if (nh >= 12 && nh < 24) {
                amPMStr = Util.getSystemPMFormat();
            } else if (nh == 0) {
                amPMStr = Util.getSystemAMFormat();
                nh = 12;
            } else {
                amPMStr = Util.getSystemAMFormat();
            }
            dateNewTime = sdf.parse(nh + ":" + nm);
            String newTime = sdf.format(dateNewTime);
            newAddedTime = newTime + " " + amPMStr;
        } catch (ParseException e) {
            LoggerUtils.exception("ParseException", e);
        }
        return newAddedTime;
    }

    public static String convertTimeTo24HrFormat(int hrMinFormat) {
        String time24HrFormat = null;
        int hr = 0;
        int min = 0;
        hr = hrMinFormat / 100;
        min = hrMinFormat % 100;
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", Locale.getDefault());
        Date newDateTime = null;
        try {
            newDateTime = sdf.parse("" + hr + ":" + min);
        } catch (ParseException e) {
            LoggerUtils.exception("ParseException", e);
        }
        time24HrFormat = sdf.format(newDateTime);
        return time24HrFormat;
    }

    public static String convertTimeTo12HrFormat(int hrMinFormat) {
        String time12HrFormat = null;
        String amPm;
        int hr = 0;
        int min = 0;
        hr = hrMinFormat / 100;
        min = hrMinFormat % 100;
        if (hr == 0) {
            amPm = Util.getSystemAMFormat();
            hr = 12;
        } else if (hr > 0 && hr < 12) {
            amPm = Util.getSystemAMFormat();
        } else {
            amPm = Util.getSystemPMFormat();
        }
        SimpleDateFormat sdf = new SimpleDateFormat("h:mm", Locale.getDefault());
        Date newDateTime = null;
        try {
            newDateTime = sdf.parse("" + hr + ":" + min);
        } catch (ParseException e) {
            LoggerUtils.exception("ParseException", e);
        }
        String newTime = sdf.format(newDateTime);
        time12HrFormat = newTime + " " + amPm;
        return time12HrFormat;
    }
    public static String getScheduleFormattedDate(String date) {
        if (date.equals("-1")) {
            return "Forever";
        }
        Date eventTime = new Date(Long.parseLong(date) * 1000);
        SimpleDateFormat sdf = new SimpleDateFormat("MMMM d, yyyy", Locale.US);
        return sdf.format(eventTime);
    }

}

