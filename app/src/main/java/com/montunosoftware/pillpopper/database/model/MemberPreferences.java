package com.montunosoftware.pillpopper.database.model;

/**
 * @author
 * Created by M1023050 on 3/14/2016.
 */
public class MemberPreferences {private String postponesDisplayed;

    private String quickviewOptIned;

    private String earlyDoseWarning;

    private String doseHistoryDays;

    private String osVersion;

    private String lateDosePeriodSecs;

    private String tz_secs;

    private String privacyMode;

    private String dstOffset_secs;

    private String kphcSyncInstanceId;

    private String userData;

    private String drugSortOrder;

    private String archivedDrugsDisplayed;

    private String lastManagedUpdate;

    private String reminderSoundFilename;

    private String deviceName;

    private String personNamesCount;

    private String kphcLastSyncAttempt;

    private String secondaryReminderPeriodSecs;

    private String language;

    private String customDrugDosageNamesCount;

    private String tz_name;

    public String getAndroidReminderSoundFilename() {
        return androidReminderSoundFilename;
    }

    public void setAndroidReminderSoundFilename(String androidReminderSoundFilename) {
        this.androidReminderSoundFilename = androidReminderSoundFilename;
    }

    private String androidReminderSoundFilename;

    public String getPostponesDisplayed ()
    {
        return postponesDisplayed;
    }

    public void setPostponesDisplayed (String postponesDisplayed)
    {
        this.postponesDisplayed = postponesDisplayed;
    }

    public String getQuickviewOptIned ()
    {
        return quickviewOptIned;
    }

    public void setQuickviewOptIned (String quickviewOptIned)
    {
        this.quickviewOptIned = quickviewOptIned;
    }

    public String getEarlyDoseWarning ()
    {
        return earlyDoseWarning;
    }

    public void setEarlyDoseWarning (String earlyDoseWarning)
    {
        this.earlyDoseWarning = earlyDoseWarning;
    }

    public String getDoseHistoryDays ()
    {
        return doseHistoryDays;
    }

    public void setDoseHistoryDays (String doseHistoryDays)
    {
        this.doseHistoryDays = doseHistoryDays;
    }

    public String getOsVersion ()
    {
        return osVersion;
    }

    public void setOsVersion (String osVersion)
    {
        this.osVersion = osVersion;
    }

    public String getLateDosePeriodSecs ()
    {
        return lateDosePeriodSecs;
    }

    public void setLateDosePeriodSecs (String lateDosePeriodSecs)
    {
        this.lateDosePeriodSecs = lateDosePeriodSecs;
    }

    public String getTz_secs ()
    {
        return tz_secs;
    }

    public void setTz_secs (String tz_secs)
    {
        this.tz_secs = tz_secs;
    }

    public String getPrivacyMode ()
    {
        return privacyMode;
    }

    public void setPrivacyMode (String privacyMode)
    {
        this.privacyMode = privacyMode;
    }

    public String getDstOffset_secs ()
    {
        return dstOffset_secs;
    }

    public void setDstOffset_secs (String dstOffset_secs)
    {
        this.dstOffset_secs = dstOffset_secs;
    }

    public String getKphcSyncInstanceId ()
    {
        return kphcSyncInstanceId;
    }

    public void setKphcSyncInstanceId (String kphcSyncInstanceId)
    {
        this.kphcSyncInstanceId = kphcSyncInstanceId;
    }

    public String getUserData ()
    {
        return userData;
    }

    public void setUserData (String userData)
    {
        this.userData = userData;
    }

    public String getDrugSortOrder ()
    {
        return drugSortOrder;
    }

    public void setDrugSortOrder (String drugSortOrder)
    {
        this.drugSortOrder = drugSortOrder;
    }

    public String getArchivedDrugsDisplayed ()
    {
        return archivedDrugsDisplayed;
    }

    public void setArchivedDrugsDisplayed (String archivedDrugsDisplayed)
    {
        this.archivedDrugsDisplayed = archivedDrugsDisplayed;
    }

    public String getLastManagedUpdate ()
    {
        return lastManagedUpdate;
    }

    public void setLastManagedUpdate (String lastManagedUpdate)
    {
        this.lastManagedUpdate = lastManagedUpdate;
    }

    public String getReminderSoundFilename ()
    {
        return reminderSoundFilename;
    }

    public void setReminderSoundFilename (String reminderSoundFilename)
    {
        this.reminderSoundFilename = reminderSoundFilename;
    }

    public String getDeviceName ()
    {
        return deviceName;
    }

    public void setDeviceName (String deviceName)
    {
        this.deviceName = deviceName;
    }

    public String getPersonNamesCount ()
    {
        return personNamesCount;
    }

    public void setPersonNamesCount (String personNamesCount)
    {
        this.personNamesCount = personNamesCount;
    }

    public String getKphcLastSyncAttempt ()
    {
        return kphcLastSyncAttempt;
    }

    public void setKphcLastSyncAttempt (String kphcLastSyncAttempt)
    {
        this.kphcLastSyncAttempt = kphcLastSyncAttempt;
    }

    public String getSecondaryReminderPeriodSecs ()
    {
        return secondaryReminderPeriodSecs;
    }

    public void setSecondaryReminderPeriodSecs (String secondaryReminderPeriodSecs)
    {
        this.secondaryReminderPeriodSecs = secondaryReminderPeriodSecs;
    }

    public String getLanguage ()
    {
        return language;
    }

    public void setLanguage (String language)
    {
        this.language = language;
    }

    public String getCustomDrugDosageNamesCount ()
    {
        return customDrugDosageNamesCount;
    }

    public void setCustomDrugDosageNamesCount (String customDrugDosageNamesCount)
    {
        this.customDrugDosageNamesCount = customDrugDosageNamesCount;
    }

    public String getTz_name ()
    {
        return tz_name;
    }

    public void setTz_name (String tz_name)
    {
        this.tz_name = tz_name;
    }

    @Override
    public String toString()
    {
        return "ClassPojo [postponesDisplayed = "+postponesDisplayed+", quickviewOptIned = "+quickviewOptIned+", earlyDoseWarning = "+earlyDoseWarning+", doseHistoryDays = "+doseHistoryDays+", osVersion = "+osVersion+", lateDosePeriodSecs = "+lateDosePeriodSecs+", tz_secs = "+tz_secs+", privacyMode = "+privacyMode+", dstOffset_secs = "+dstOffset_secs+", kphcSyncInstanceId = "+kphcSyncInstanceId+", userData = "+userData+", drugSortOrder = "+drugSortOrder+", archivedDrugsDisplayed = "+archivedDrugsDisplayed+", lastManagedUpdate = "+lastManagedUpdate+", reminderSoundFilename = "+reminderSoundFilename+", deviceName = "+deviceName+", personNamesCount = "+personNamesCount+", kphcLastSyncAttempt = "+kphcLastSyncAttempt+", secondaryReminderPeriodSecs = "+secondaryReminderPeriodSecs+", language = "+language+", customDrugDosageNamesCount = "+customDrugDosageNamesCount+", tz_name = "+tz_name+"]";
    }
}
