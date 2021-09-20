package com.montunosoftware.pillpopper.android.util;

import android.content.Context;
import android.webkit.CookieManager;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.NLPReminder;

import org.apache.http.conn.scheme.SocketFactory;
import org.apache.http.conn.ssl.SSLSocketFactory;
import org.json.JSONArray;
import org.json.JSONObject;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.ttg.RefillRuntimeData;
import org.kp.tpmg.ttg.utils.RxRefillLoggerUtils;
import org.kp.tpmg.ttg.utils.RxRefillUtils;
import org.kp.tpmg.ttgmobilelib.TTGMobileLibConstants;
import org.kp.tpmg.ttgmobilelib.TTGRuntimeData;
import org.kp.tpmg.ttgmobilelib.utilities.TTGSSLSocketFactory;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.UnrecoverableKeyException;
import java.security.cert.CertificateException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import javax.net.ssl.HttpsURLConnection;

public class NLPUtils {

    public static String getScheduledFrequency(String freq){
        switch (freq){
            case "D":
                return "DAILY";
            case "W":
                return "WEEKLY";
            case "M":
                return "MONTHLY";
            default:
                return freq;
        }
    }

    public static String getNLPFormattedDate(String schDate) {
        if (schDate.equalsIgnoreCase("-1")) {
            return "Never";
        }
        try {
            Date date = new Date(Long.parseLong(schDate) * 1000);
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
            return sdf.format(date);
        } catch (Exception ex) {
            LoggerUtils.exception("getNLPFormattedDate failed -- " + schDate);
            return schDate;
        }
    }

    public static ArrayList<String> getFormattedReminderTimes(List<Integer> scheduledTimeList) {
        ArrayList<String> reminderTimes = new ArrayList<>();
        for (Integer time : scheduledTimeList) {
            reminderTimes.add(convertHHMMtoTimeFormat(String.valueOf(time)));
        }
        return reminderTimes;
    }


    public static String convertHHMMtoTimeFormat(String str) {
        try {
            if ((Long.parseLong(str) / 100) < 12) {
                String timePatternISO = "%d.%02d AM";
                return String
                        .format(timePatternISO, (Long.parseLong(str) / 100) % 12,
                                Long.parseLong(str) % 100);
            } else {
                String timePatternISO = "%d.%02d PM";
                return String
                        .format(timePatternISO, (Long.parseLong(str) / 100) % 12 == 0 ? 12 : (Long.parseLong(str) / 100) % 12,
                                Long.parseLong(str) % 100);
            }
        } catch (Exception ex) {
            LoggerUtils.exception(ex.getMessage());
        }
        return str;
    }

    public static String getScheduledEvery(Context context, Drug drug, String scheduledFrequency, long dayPeriod, String daysSelectedForWeekly) {
        LoggerUtils.info("NLP : scheduledFrequency " + scheduledFrequency);
        StringBuilder selectedDayPeriod = new StringBuilder();
        try {
            LoggerUtils.info("NLP : dayPeriod " + dayPeriod);
            if (("D").equalsIgnoreCase(scheduledFrequency)) {
                if (dayPeriod == 1) {
                    selectedDayPeriod.append("DAY");
                } else {
                    selectedDayPeriod.append(context.getResources().getString(R.string.every))
                            .append(" ")
                            .append(dayPeriod)
                            .append(" ")
                            .append(context.getResources().getString(R.string.days));
                }
            } else if (("W").equalsIgnoreCase(scheduledFrequency)) {
                if (dayPeriod / 7 != 1) {
                    selectedDayPeriod.append(context.getResources().getString(R.string.every))
                            .append(" ")
                            .append(dayPeriod / 7 > 1 ? (dayPeriod / 7) + " " : "")
                            .append(context.getResources().getString(R.string.weeks_on)).append(context.getResources().getString(R.string.on)).append(" ")
                            .append(Util.setOnWeekdays(context, String.valueOf(drug.getSchedule().getStart().getDayOfWeek().getDayNumber())));
                } else {
                    selectedDayPeriod.append(context.getResources().getString(R.string.weekly_on))
                            .append(" ")
                            .append(Util.setOnWeekdays(context, daysSelectedForWeekly));
                }
            } else if (("M").equalsIgnoreCase(scheduledFrequency)) {
                String date = "";
                try {
                    date = drug.getSchedule().getStart().getDay() + "" + Util.getSuffix(drug.getSchedule().getStart().getDay());
                } catch (NumberFormatException ne) {
                    LoggerUtils.info(ne.getMessage());
                }
                selectedDayPeriod.append(context.getResources().getString(R.string._monthly)).append(" ").append(date);
            }
        }catch (Exception ex){
            LoggerUtils.exception(ex.getMessage());
        }

        LoggerUtils.info("NLP : selectedDayPeriod -Every - " + selectedDayPeriod.toString());
        return selectedDayPeriod.toString();
    }

    public static boolean isResponseMatched(NLPReminder nlpReminder, NLPReminder usersChoiceReminder){
        if(!nlpReminder.getStartDate().equalsIgnoreCase(usersChoiceReminder.getStartDate())){
            return false;
        }
        if(!(nlpReminder.getEndDate().equalsIgnoreCase(getScheduleFormattedDate(usersChoiceReminder.getEndDate())) || nlpReminder.getEndDate().equalsIgnoreCase(usersChoiceReminder.getEndDate()))){
            return false;
        }
        if(!nlpReminder.getFrequency().equalsIgnoreCase(usersChoiceReminder.getFrequency())){
            return false;
        }
        if(!nlpReminder.getEvery().equalsIgnoreCase(usersChoiceReminder.getEvery())){
            return false;
        }
        // reminderTimes comparision
        if (nlpReminder.getReminderTimes().size() != usersChoiceReminder.getReminderTimes().size()) {
            return false;
        } else if (nlpReminder.getReminderTimes().size() == usersChoiceReminder.getReminderTimes().size()) {
            boolean isMatching = true;
            for (int i = 0; i < nlpReminder.getReminderTimes().size(); i++) {
                if (!(nlpReminder.getReminderTimes().contains(usersChoiceReminder.getReminderTimes().get(i)))) {
                    isMatching = false;
                    break;
                }
            }
            return isMatching;
        }
        return true;
    }

    public static ArrayList<String> getScheduleFormattedReminders(ArrayList<String> reminderTimes) {
        ArrayList<String> formattedReminderTimes = new ArrayList<>();
        for (int i = 0; i < reminderTimes.size(); i++) {
            if (!Util.isEmptyString(reminderTimes.get(i)) && reminderTimes.get(i).contains(".")) {
                formattedReminderTimes.add(reminderTimes.get(i).trim().replace('.', ':'));
            }
        }
        return formattedReminderTimes;
    }

    public static String prepareMobileResponse(NLPReminder nlpReminder) {
        JSONObject finalJson = new JSONObject();
        JSONArray mobileResponse = new JSONArray();
        try {
            JSONObject reminderSchedule = new JSONObject();
            reminderSchedule.putOpt("startDate", nlpReminder.getStartDate());
            reminderSchedule.putOpt("endDate", nlpReminder.getEndDate());
            reminderSchedule.putOpt("frequency", nlpReminder.getFrequency());
            reminderSchedule.putOpt("every", nlpReminder.getEvery());
            reminderSchedule.putOpt("medicine", nlpReminder.getMedicine());
            reminderSchedule.putOpt("dosage", nlpReminder.getDosage());
            reminderSchedule.putOpt("sigId", nlpReminder.getSigId());
            JSONArray reminderTimes = new JSONArray();
            for (String reminderTime : nlpReminder.getReminderTimes()) {
                reminderTimes.put(reminderTime);
            }
            reminderSchedule.putOpt("reminderTimes", reminderTimes);
            mobileResponse.put(reminderSchedule);
            finalJson.put("reminders", mobileResponse);
        } catch (Exception ex) {
            LoggerUtils.exception(ex.getMessage());
        }
        return finalJson.toString();
    }

    public static String getNLPFormattedDate() {
        String pattern = "yyyy-MM-dd";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(new Date());
    }

    public static String getScheduleFormattedDate(String date) {
        if(date.equalsIgnoreCase("Never") || date.equalsIgnoreCase("Forever")){
            return "Forever";
        }
        try {
            Date dateObj = new SimpleDateFormat("yyyy-MM-dd").parse(date);
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MMM d, yyyy", Locale.US);
            return simpleDateFormat.format(dateObj);
        } catch (ParseException e) {
            LoggerUtils.exception(e.getMessage());
        }
        return "";
    }

    public static HttpURLConnection makeRequest(String url, String requestType, Map<String, String> headers, JSONObject requestObj) {

        try {
            initializeSSL();
            URL _url = new URL(url);
            URLConnection connection = _url.openConnection();
            HttpsURLConnection httpConnection = (HttpsURLConnection) connection;
            httpConnection.setConnectTimeout(45000);
            httpConnection.setDoInput(true);
            httpConnection.setRequestMethod(requestType);
            httpConnection.setSSLSocketFactory(TTGRuntimeData.getInstance().getSslSocketFactory());

            if (headers != null && headers.size() > 0) {
                for (Map.Entry<String, String> header : headers.entrySet()) {
                    httpConnection.addRequestProperty(header.getKey(), header.getValue());
                }
            }
            httpConnection.addRequestProperty("Cookie", getCookies());

            OutputStream outputStream = null;
            BufferedWriter writer = null;
            outputStream = httpConnection.getOutputStream();
            writer = new BufferedWriter(new OutputStreamWriter(outputStream));
            if (requestObj != null) {
                writer.write(requestObj.toString());
            }
            writer.flush();
            writer.close();
            outputStream.close();

            httpConnection.connect();

            return httpConnection;
        }  catch (IOException e) {
            RxRefillLoggerUtils.exception("I/O exception: %s", e);
        } catch (Exception e) {
            RxRefillLoggerUtils.exception("Exception: %s", e);
        }
        return null;
    }

    public static String getCookies() {
        if(!RxRefillUtils.isEmptyString(RefillRuntimeData.getInstance().getKeepAliveCookieDomain())){
            try {
                return CookieManager.getInstance().getCookie("https://" + RefillRuntimeData.getInstance().getKeepAliveCookieDomain());
            } catch (Exception e) {
                RxRefillLoggerUtils.exception(e.getMessage());
            }
        }
        return null;
    }

   public static void initializeSSL() {
        createSocketFactory();
    }

    private static SocketFactory createSocketFactory() {
        TTGSSLSocketFactory socketFactory = null;
        try {
            KeyStore trustStore = KeyStore.getInstance(TTGMobileLibConstants.CERTIFICATE_TYPE);
            trustStore.load(null, null);
            socketFactory = new TTGSSLSocketFactory(trustStore);
            socketFactory.setHostnameVerifier(SSLSocketFactory.STRICT_HOSTNAME_VERIFIER);
        } catch (KeyManagementException e) {
            RxRefillLoggerUtils.exception("KeyManagementException", e);
        } catch (UnrecoverableKeyException e) {
            RxRefillLoggerUtils.exception("UnrecoverableKeyException", e);
        } catch (KeyStoreException e) {
            RxRefillLoggerUtils.exception("KeyStoreException", e);
        } catch (NoSuchAlgorithmException e) {
            RxRefillLoggerUtils.exception("NoSuchAlgorithmException", e);
        } catch (CertificateException e) {
            RxRefillLoggerUtils.exception("CertificateException", e);
        } catch (IOException e) {
            RxRefillLoggerUtils.exception("IOException", e);
        }
        return socketFactory;
    }
}
