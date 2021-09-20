package com.montunosoftware.pillpopper.database.model;

/**
 * @author
 * Created by M1023050 on 3/14/2016.
 */
public class PillList
{
    private String maxPostponeTime;

    private String notify_after_tz_secs;

    private String last_tz_secs;

    private String notify_after;

    private String lastTaken;

    private String instructions;

    private String dayperiod;

    private String next;

    private String serverEditTime;

    private String hasUndo;

    private String eff_last_taken;

    private String eff_last_taken_tz_secs;

    private String skipPillAfter;

    private String dose;

    private String serverEditGuid;

    private String history;

    private String[] schedule;

    private String pillId;

    private String start_tz_secs;

    private String start;

    private String created;

    private String takePillAfter;

    private String end;

    private String name;

    private String numpills;

    private String end_tz_secs;

    private String interval;

    private String scheduleGuid;

    private PillPreferences preferences;

    public boolean isHeader() {
        return isHeader;
    }

    public void setIsHeader(boolean isHeader) {
        this.isHeader = isHeader;
    }

    private boolean isHeader;

    public String getOverdue() {
        return overdue;
    }

    public void setOverdue(String overdue) {
        this.overdue = overdue;
    }

    private String overdue;

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    private String userId;

    public String getMaxPostponeTime ()
    {
        return maxPostponeTime;
    }

    public void setMaxPostponeTime (String maxPostponeTime)
    {
        this.maxPostponeTime = maxPostponeTime;
    }

    public String getNotify_after ()
    {
        return notify_after;
    }

    public void setNotify_after (String notify_after)
    {
        this.notify_after = notify_after;
    }

    public String getLastTaken()
    {
        return lastTaken;
    }

    public void setLastTaken(String last)
    {
        this.lastTaken = last;
    }

    public String getInstructions ()
    {
        return instructions;
    }

    public void setInstructions (String instructions)
    {
        this.instructions = instructions;
    }

    public String getDayperiod ()
    {
        return dayperiod;
    }

    public void setDayperiod (String dayperiod)
    {
        this.dayperiod = dayperiod;
    }

    public String getNext ()
    {
        return next;
    }

    public void setNext (String next)
    {
        this.next = next;
    }

    public String getServerEditTime ()
    {
        return serverEditTime;
    }

    public void setServerEditTime (String serverEditTime)
    {
        this.serverEditTime = serverEditTime;
    }

    public String getHasUndo ()
    {
        return hasUndo;
    }

    public void setHasUndo (String hasUndo)
    {
        this.hasUndo = hasUndo;
    }

    public String getEff_last_taken ()
    {
        return eff_last_taken;
    }

    public void setEff_last_taken (String eff_last_taken)
    {
        this.eff_last_taken = eff_last_taken;
    }

    public String getSkipPillAfter ()
    {
        return skipPillAfter;
    }

    public void setSkipPillAfter (String skipPillAfter)
    {
        this.skipPillAfter = skipPillAfter;
    }

    public String getDose ()
    {
        return dose;
    }

    public void setDose (String dose)
    {
        this.dose = dose;
    }

    public String getServerEditGuid ()
    {
        return serverEditGuid;
    }

    public void setServerEditGuid (String serverEditGuid)
    {
        this.serverEditGuid = serverEditGuid;
    }

    public String getHistory ()
{
    return history;
}

    public void setHistory (String history)
    {
        this.history = history;
    }

    public String[] getSchedule ()
    {
        return schedule;
    }

    public void setSchedule (String[] schedule)
    {
        this.schedule = schedule;
    }

    public String getPillId ()
    {
        return pillId;
    }

    public void setPillId (String pillId)
    {
        this.pillId = pillId;
    }

    public String getStart ()
    {
        return start;
    }

    public void setStart (String start)
    {
        this.start = start;
    }

    public String getCreated ()
    {
        return created;
    }

    public void setCreated (String created)
    {
        this.created = created;
    }

    public String getTakePillAfter ()
    {
        return takePillAfter;
    }

    public void setTakePillAfter (String takePillAfter)
    {
        this.takePillAfter = takePillAfter;
    }

    public String getName ()
    {
        return name;
    }

    public void setName (String name)
    {
        this.name = name;
    }

    public String getNumpills ()
    {
        return numpills;
    }

    public void setNumpills (String numpills)
    {
        this.numpills = numpills;
    }

    public String getEnd ()
    {
        return end;
    }

    public void setEnd (String end)
    {
        this.end = end;
    }

    public PillPreferences getPreferences ()
    {
        return preferences;
    }

    public void setPreferences (PillPreferences preferences)
    {
        this.preferences = preferences;
    }

    public String getInterval() {
        return interval;
    }

    public void setInterval(String interval) {
        this.interval = interval;
    }

    public String getNotify_afterTZsecs() {
        return notify_after_tz_secs;
    }

    public void setNotify_afterTZsecs(String notify_after_tz_secs) {
        this.notify_after_tz_secs = notify_after_tz_secs;
    }

    public String getLastTZsecs() {
        return last_tz_secs;
    }

    public void setLastTZsecs(String last_tz_secs) {
        this.last_tz_secs = last_tz_secs;
    }

    public String getEff_last_takenTZsecs() {
        return eff_last_taken_tz_secs;
    }

    public void setEff_last_takenTZsecs(String eff_last_taken_tz_secs) {
        this.eff_last_taken_tz_secs = eff_last_taken_tz_secs;
    }

    public String getStartTZsecs() {
        return start_tz_secs;
    }

    public void setStartTZsecs(String start_tz_secs) {
        this.start_tz_secs = start_tz_secs;
    }

    public String getEndTZsecs() {
        return end_tz_secs;
    }

    public void setEndTZsecs(String end_tz_secs) {
        this.end_tz_secs = end_tz_secs;
    }

    public String getScheduleGuid() {
        return scheduleGuid;
    }

    public void setScheduleGuid(String scheduleGuid) {
        this.scheduleGuid = scheduleGuid;
    }

    /*@Override
    public String toString()
    {
        return "ClassPojo [maxPostponeTime = "+maxPostponeTime+", notify_after = "+notify_after+", lastTaken = "+lastTaken+", instructions = "+instructions+", dayperiod = "+dayperiod+", next = "+next+", serverEditTime = "+serverEditTime+", hasUndo = "+hasUndo+", type = "+type+", eff_last_taken = "+eff_last_taken+", skipPillAfter = "+skipPillAfter+", dose = "+dose+", serverEditGuid = "+serverEditGuid+", history = "+history+", schedule = "+schedule+", pillId = "+pillId+", start = "+start+", created = "+created+", takePillAfter = "+takePillAfter+", name = "+name+", numpills = "+numpills+", end = "+end+", preferences = "+preferences+"]";
    }*/
}

