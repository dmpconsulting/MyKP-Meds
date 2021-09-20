package com.montunosoftware.pillpopper.model;

import android.webkit.WebView;

import com.montunosoftware.pillpopper.android.HomeContainerActivity;

import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;

/**
 * @author
 * Created by M1030430 on 3/22/2016.
 */
public class PillpopperRunTime {

    private List<Drug> mDrugsToBeTaken;
    private Long headerTime;
    private Long headerDate;
    private boolean isFirstTimeSyncDone = false;
    private boolean isLimitedHistorySyncToDo = true;
    private boolean historySyncDone = false;
    private List<Drug> mOverdueDrugs;
    private String proxyName;
    private List<Drug> proxyDrugs;
    private LinkedHashMap<String,List<Drug>> drugsHashMap;
    private HasStatusUpdateResponseObj mHasStatusUpdateResponseObj;
    private WebView webViewInstance;
    private boolean isCardAdjustmentRequired = false;
    private LinkedHashMap<String, LinkedHashMap<Long, List<Drug>>> passedRemindersByUserIdForCards;
    private LinkedHashMap<Long, List<Drug>> currentRemindersByUserIdForCard;

    public boolean isCardAdjustmentRequired() {
        return isCardAdjustmentRequired;
    }

    public void setCardAdjustmentRequired(boolean cardAdjustmentRequired) {
        isCardAdjustmentRequired = cardAdjustmentRequired;
    }

    public boolean isTimeZoneChanged() {
        return isTimeZoneChanged;
    }

    public void setTimeZoneChanged(boolean timeZoneChanged) {
        isTimeZoneChanged = timeZoneChanged;
    }

    private boolean isTimeZoneChanged =false;

    public List<Drug> getDrugsToBeTaken() {
        return mDrugsToBeTaken;
    }

    public void setDrugsToBeTaken(List<Drug> drugsToBeTaken) {
        this.mDrugsToBeTaken = drugsToBeTaken;
    }

    public LinkedHashMap<String, LinkedHashMap<Long, List<Drug>>> getLateRemindersMap() {
        return lateRemindersMap;
    }

    public void setLateRemindersMap(LinkedHashMap<String, LinkedHashMap<Long, List<Drug>>> lateRemindersMap) {
        this.lateRemindersMap = lateRemindersMap;
    }

    private LinkedHashMap<String, LinkedHashMap<Long, List<Drug>>> lateRemindersMap;

    public List<Drug> getQuickViewReminderDrugs() {
        return quickViewReminderDrugs;
    }

    public void setQuickViewReminderDrugs(List<Drug> quickViewReminderDrugs) {
        this.quickViewReminderDrugs = quickViewReminderDrugs;
    }

    private List<Drug> quickViewReminderDrugs;

    public boolean isReminderNeedToShow() {
        return isReminderNeedToShow;
    }

    public void setReminderNeedToShow(boolean reminderNeedToShow) {
        isReminderNeedToShow = reminderNeedToShow;
    }

    private boolean isReminderNeedToShow;

    private Calendar mLastSyncTime;

    public Calendar getmLastSyncTime() {
        return mLastSyncTime;
    }

    public void setmLastSyncTime(Calendar cal) {
        this.mLastSyncTime = cal;
    }

    public LinkedHashMap<String, List<Drug>> getDrugsHashMap() {
        return drugsHashMap;
    }

    public void setDrugsHashMap(LinkedHashMap<String, List<Drug>> drugsHashMap) {
        this.drugsHashMap = drugsHashMap;
    }

    public List<Drug> getmOverdueDrugs() {
        return mOverdueDrugs;
    }

    public void setmOverdueDrugs(List<Drug> mOverdueDrugs) {
        this.mOverdueDrugs = mOverdueDrugs;
    }

    public boolean isFirstTimeSyncDone() {
        return isFirstTimeSyncDone;
    }

    public void setIsFirstTimeSyncDone(boolean isFirstTimeSyncDone) {
        this.isFirstTimeSyncDone = isFirstTimeSyncDone;
    }

    public void setLimitedHistorySyncToDo(boolean isSync) {
        isLimitedHistorySyncToDo =isSync;
    }

    public boolean isLimitedHistorySyncToDo() {
        return isLimitedHistorySyncToDo;
    }

    public void setHistorySyncDone(boolean isSync) {
        historySyncDone =isSync;
    }

    public boolean isHistorySyncDone() {
        return historySyncDone;
    }

    public static final PillpopperRunTime runTimeData = new PillpopperRunTime();

    public Long getHeaderTime() {
        return headerTime;
    }

    public void setHeaderTime(Long headerTime) {
        this.headerTime = headerTime;
    }

    public Long getHeaderDate() {
        return headerDate;
    }

    public void setHeaderDate(Long headerDate) {
        this.headerDate = headerDate;
    }

    public String getProxyName() {
        return proxyName;
    }

    public void setProxyName(String proxyName) {
        this.proxyName = proxyName;
    }

    public List<Drug> getProxyDrugs() {
        return proxyDrugs;
    }

    public void setProxyDrugs(List<Drug> proxyDrugs) {
        this.proxyDrugs = proxyDrugs;
    }

    private PillpopperRunTime() {
    }

    public static PillpopperRunTime getInstance() {
        return runTimeData;
    }

    public LinkedHashMap<String, LinkedHashMap<Long, List<Drug>>> getPassedReminderersHashMapByUserId() {
        return passedReminderersHashMapByUserId;
    }

    public void setPassedReminderersHashMapByUserId(LinkedHashMap<String, LinkedHashMap<Long, List<Drug>>> passedReminderersHashMapByUserId) {
        this.passedReminderersHashMapByUserId = passedReminderersHashMapByUserId;
    }

    private LinkedHashMap<String,LinkedHashMap<Long,List<Drug>>> passedReminderersHashMapByUserId;

    public LinkedHashMap<Long, List<Drug>> getmCurrentRemindersMap() {
        return mCurrentRemindersMap;
    }

    public void setmCurrentRemindersMap(LinkedHashMap<Long, List<Drug>> runtimeCurrentReminders) {
        this.mCurrentRemindersMap = runtimeCurrentReminders;
    }

    private LinkedHashMap<Long, List<Drug>> mCurrentRemindersMap;
    private LinkedHashMap<Long, List<Drug>> mPassedRemindersMap;

    public LinkedHashMap<Long, List<Drug>> getmPassedRemindersMap() {
        return mPassedRemindersMap;
    }

    public void setmPassedRemindersMap(LinkedHashMap<Long, List<Drug>> mRuntimeDrugList) {
        this.mPassedRemindersMap = mRuntimeDrugList;
    }

    public boolean isLauchingFromPast() {
        return isLauchingFromPast;
    }

    public void setLauchingFromPast(boolean lauchingFromPast) {
        isLauchingFromPast = lauchingFromPast;
    }

    private boolean isLauchingFromPast = false;

    public long getRemovalTime() {
        return removalTime;
    }

    public void setRemovalTime(long removalTime) {
        this.removalTime = removalTime;
    }

    private long removalTime;

    public HasStatusUpdateResponseObj getHasStatusUpdateResponseObj() {
        return mHasStatusUpdateResponseObj;
    }

    public void setHasStatusUpdateResponseObj(HasStatusUpdateResponseObj hasStatusUpdateResponseObj) {
        this.mHasStatusUpdateResponseObj = hasStatusUpdateResponseObj;
    }

    public WebView getWebViewInstance() {
        return webViewInstance;
    }

    public void setWebViewInstance(WebView webViewInstance) {
        this.webViewInstance = webViewInstance;
    }

    public boolean isFromMDO() {
        return isFromMDO;
    }

    public void setFromMDO(boolean fromMDO) {
        isFromMDO = fromMDO;
    }

    private boolean isFromMDO = false;

    public LinkedHashMap<String, LinkedHashMap<Long, List<Drug>>> getPassedRemindersByUserIdForCards() {
        return passedRemindersByUserIdForCards;
    }

    public void setPassedRemindersByUserIdForCards(LinkedHashMap<String, LinkedHashMap<Long, List<Drug>>> passedRemindersByUserIdForCards) {
        this.passedRemindersByUserIdForCards = passedRemindersByUserIdForCards;
    }


    public LinkedHashMap<Long, List<Drug>> getCurrentRemindersByUserIdForCard() {
        return currentRemindersByUserIdForCard;
    }

    public void setCurrentRemindersByUserIdForCard(LinkedHashMap<Long, List<Drug>> currentRemindersByUserIdForCard) {
        this.currentRemindersByUserIdForCard = currentRemindersByUserIdForCard;
    }

    private HomeContainerActivity.NavigationHome _selectedHomeFragment = HomeContainerActivity.NavigationHome.DAILY_SCHEDULE;

    public HomeContainerActivity.NavigationHome getSelectedHomeFragment() {
        return _selectedHomeFragment;
    }

    public void setSelectedHomeFragment(HomeContainerActivity.NavigationHome selectedHomeFragment) {
        this._selectedHomeFragment = selectedHomeFragment;
    }
}
