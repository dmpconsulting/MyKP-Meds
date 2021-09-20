package com.montunosoftware.pillpopper.model;

import com.montunosoftware.pillpopper.android.ScheduleMainTimeHeader;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * @author
 * Created by adhithyaravipati on 8/26/16.
 */
public class ScheduleListItemDataWrapper implements Comparable<ScheduleListItemDataWrapper> {

    private boolean possibleNextActiveListItem = false;

    private String userId;
    private String userFirstName;
    private String userType;
    private String pillTime;
    private boolean getTimeVisibility;
    private int isProxyAvailable;

    private boolean showThreeDotAction = false;

    public int isProxyAvailable() {
        return isProxyAvailable;
    }

    public void setProxyAvailable(int proxyAvailable) {
        isProxyAvailable = proxyAvailable;
    }

    private ScheduleMainTimeHeader scheduleMainTimeHeader;

    private ArrayList<String> pillIdsForTakenAction = new ArrayList<>();

    private List<ScheduleMainDrug> drugList;

    @Override
    public int compareTo(ScheduleListItemDataWrapper another) {
        if(scheduleMainTimeHeader.getHeaderPillpopperTime().equals(another.getScheduleMainTimeHeader().getHeaderPillpopperTime())) {
            if(userType.equals(another.getUserType())) {
                return userFirstName.compareTo(another.getUserFirstName());
            }
            return userType.compareTo(another.getUserType());
        }
        return scheduleMainTimeHeader.getHeaderPillpopperTime().compareTo(another.getScheduleMainTimeHeader().getHeaderPillpopperTime());
    }
    public boolean isSetTimeVisibility() {
        return getTimeVisibility;
    }

    public void setSetTimeVisibility(boolean setTimeVisibility) {
        this.getTimeVisibility = setTimeVisibility;
    }
    public String getUserType() {
        return userType;
    }

    public void setUserType(String userType) {
        this.userType = userType;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getUserFirstName() {
        return userFirstName;
    }

    public void setUserFirstName(String userFirstName) {
        this.userFirstName = userFirstName;
    }

    public String getPillTime() {
        return pillTime;
    }

    public void setPillTime(String pillTime, PillpopperDay focusDay) {

        this.pillTime = pillTime;
        scheduleMainTimeHeader = new ScheduleMainTimeHeader(pillTime,focusDay);
    }

    public void setPillTime(PillpopperTime pillTimeInPillpopperTime) {
        int hours = pillTimeInPillpopperTime.getLocalHourMinute().getHour();
        int minutes = pillTimeInPillpopperTime.getLocalHourMinute().getMinute();

        String pillTime = String.format("%02d%02d",hours,minutes);

        setPillTime(pillTime, pillTimeInPillpopperTime.getLocalDay());
    }

    public ScheduleMainTimeHeader getScheduleMainTimeHeader() {
        return scheduleMainTimeHeader;
    }

    public List<ScheduleMainDrug> getDrugList() {
        return drugList;
    }

    public void setDrugList(List<ScheduleMainDrug> drugList) {
        this.drugList = drugList;
    }


    public void setShowThreeDotAction(boolean showThreeDotAction) {
        this.showThreeDotAction = showThreeDotAction;
    }

    public boolean showThreeDotAction() {
        return this.showThreeDotAction;
    }

    public ArrayList<String> getPillIdsForTakenAction() {
        return pillIdsForTakenAction;
    }

    public void setPillIdsForTakenAction(ArrayList<String> pillIdsForTakenAction) {
        this.pillIdsForTakenAction = pillIdsForTakenAction;
    }

    public boolean isPossibleNextActiveListItem() {
        return possibleNextActiveListItem;
    }

    public void setPossibleNextActiveListItem(boolean possibleNextActiveListItem) {
        this.possibleNextActiveListItem = possibleNextActiveListItem;
    }
    public static class AlphabeticalByNameComparator implements Comparator<ScheduleMainDrug>
    {
        @Override
        public int compare(ScheduleMainDrug lhs, ScheduleMainDrug rhs)
        {
            if (lhs.getPillName() != null && rhs.getPillName() !=null) {
                return lhs.getPillName().compareToIgnoreCase(rhs.getPillName());
            } else {
                return 0;
            }
        }
    }
}
