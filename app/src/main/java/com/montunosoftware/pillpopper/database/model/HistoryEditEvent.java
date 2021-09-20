package com.montunosoftware.pillpopper.database.model;

import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.model.PillpopperTime;

/**
 * @author
 * Created by adhithyaravipati on 7/6/16.
 */
public class HistoryEditEvent {

    private String proxyName;

    private String pillId;
    private String pillUserId;
    private String historyEventGuid;
    private String pillBrandName;
    private String pillGenericName;
    private String pillDosage;
    private String pillDosageType;

    private PillpopperTime pillScheduleDate;
    private PillpopperTime pillHistoryCreationDate;
    private PillpopperTime pillHistoryEditDate;

    private String pillOperation;
    private int pillOperationId;
    private String pillEventDescription;
    private String pillImageGuid;
    private String isInvisible;
    private String isArchived;

    private GetHistoryPreferences preferences;
    private boolean isActionDateRequired;

    public boolean isActionDateRequired() {
        return isActionDateRequired;
    }

    public void setActionDateRequired(boolean actionDateRequired) {
        isActionDateRequired = actionDateRequired;
    }

    public String getTz_secs() {
        return tz_secs;
    }

    public void setTz_secs(String tz_secs) {
        this.tz_secs = tz_secs;
    }

    private String tz_secs;

    public String getProxyName() {
        return proxyName;
    }

    public void setProxyName(String proxyName) {
        this.proxyName = proxyName;
    }

    public String getPillId() {
        return pillId;
    }

    public void setPillId(String pillId) {
        this.pillId = pillId;
    }

    public void setPillName(String pillName) {

        if (null != pillName) {
            int refPoint = pillName.contains("(") ? pillName.indexOf("(") : -1;

            if (refPoint == -1) {
                this.pillBrandName = pillName;
            } else {
                this.pillBrandName = pillName.substring(0, refPoint);
                String genericName = pillName.substring(refPoint);
                if (!Util.isEmptyString(genericName)) {
                    genericName = genericName.replaceAll("\\(", "");
                    genericName = genericName.replaceAll("\\)", "");
                    this.pillGenericName = genericName;
                }
            }
        }
    }

    public String getPillUserId() {
        return pillUserId;
    }

    public void setPillUserId(String pillUserId) {
        this.pillUserId = pillUserId;
    }

    public String getHistoryEventGuid() {
        return historyEventGuid;
    }

    public void setHistoryEventGuid(String historyEventGuid) {
        this.historyEventGuid = historyEventGuid;
    }

    public String getPillBrandName() {
        return pillBrandName;
    }

    public void setPillBrandName(String pillBrandName) {
        this.pillBrandName = pillBrandName;
    }

    public String getPillGenericName() {
        return pillGenericName;
    }

    public void setPillGenericName(String pillGenericName) {
        this.pillGenericName = pillGenericName;
    }

    public String getPillDosage() {
        return pillDosage;
    }

    public void setPillDosage(String pillDosage) {
        this.pillDosage = pillDosage;
    }

    public String getPillDosageType() {
        return pillDosageType;
    }

    public void setPillDosageType(String pillDosageType) {
        this.pillDosageType = pillDosageType;
    }

    public PillpopperTime getPillScheduleDate() {
        return pillScheduleDate;
    }

    public void setPillScheduleDate(PillpopperTime pillScheduleDate) {
        this.pillScheduleDate = pillScheduleDate;
    }

    public void setPillScheduleDate(String pillScheduleDate) {
        this.pillScheduleDate = Util.convertStringtoPillpopperTime(pillScheduleDate);
    }

    public PillpopperTime getPillHistoryCreationDate() {
        return pillHistoryCreationDate;
    }

    public void setPillHistoryCreationDate(PillpopperTime pillHistoryCreationDate) {
        this.pillHistoryCreationDate = pillHistoryCreationDate;
    }

    public void setPillHistoryCreationDate(String pillHistoryCreationDate) {
        this.pillHistoryCreationDate = Util.convertStringtoPillpopperTime(pillHistoryCreationDate);
    }

    public PillpopperTime getPillHistoryEditDate() {
        return pillHistoryEditDate;
    }

    public void setPillHistoryEditDate(PillpopperTime pillHistoryEditDate) {
        this.pillHistoryEditDate = pillHistoryEditDate;
    }

    public void setPillHistoryEditDate(String pillHistoryEditDate) {
        this.pillHistoryEditDate = Util.convertStringtoPillpopperTime(pillHistoryEditDate);
    }

    public String getPillOperation() {
        return pillOperation;
    }

    public void setPillOperation(String pillOperation) {
        this.pillOperation = pillOperation;
    }

    public int getPillOperationId() {
        return pillOperationId;
    }

    public void setPillOperationId(String pillOperationId) {
        this.pillOperationId = Util.handleParseInt(pillOperationId);
    }

    public String getPillEventDescription() {
        return pillEventDescription;
    }

    public void setPillEventDescription(String pillEventDescription) {
        this.pillEventDescription = pillEventDescription;
    }


    public String getPillImageGuid() {
        return pillImageGuid;
    }

    public void setPillImageGuid(String pillImageGuid) {
        this.pillImageGuid = pillImageGuid;
    }

    public String getIsInvisible() {
        return isInvisible;
    }

    public void setIsInvisible(String isInvisible) {
        this.isInvisible = isInvisible;
    }

    public String getIsArchived() {
        return isArchived;
    }

    public void setIsArchived(String isArchived) {
        this.isArchived = isArchived;
    }

    public GetHistoryPreferences getPreferences() {
        return preferences;
    }

    public void setPreferences(GetHistoryPreferences preferences) {
        this.preferences = preferences;
    }
}
