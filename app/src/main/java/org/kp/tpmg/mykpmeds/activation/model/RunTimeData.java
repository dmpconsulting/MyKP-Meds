package org.kp.tpmg.mykpmeds.activation.model;


import android.content.Context;

import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProviders;

import com.montunosoftware.pillpopper.android.NotificationBarOrderedBroadcastHandler;
import com.montunosoftware.pillpopper.android.view.EditScheduleRunTimeData;
import com.montunosoftware.pillpopper.android.view.ScheduleViewModel;
import com.montunosoftware.pillpopper.model.NLPReminder;
import com.montunosoftware.pillpopper.model.PillpopperDay;
import com.montunosoftware.pillpopper.model.PillpopperTime;
import com.montunosoftware.pillpopper.model.genericCardAndBanner.AnnouncementsItem;
import com.montunosoftware.pillpopper.model.genericCardAndBanner.AnnouncementsResponse;

import org.kp.tpmg.ttgmobilelib.model.TTGInteruptAPIResponse;
import org.kp.tpmg.ttgmobilelib.model.TTGUserResponse;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class RunTimeData {
    public static final RunTimeData runTimeData = new RunTimeData();
    public boolean isRxRefillAEMErrorPageLoaded;
    private boolean isUserSelected;
    private boolean isAlertDisplayedFlg;
    private int homeButtonPressed;
    private long oldLockTime = Long.MAX_VALUE;
    private long newLockTime = Long.MAX_VALUE;
    private boolean appVisibleFlg;
    private boolean isClickFlg;
    private String runtimeSSOSessionID;
    private boolean userLoginFlg;
    private boolean isFromMDO;
    private TTGUserResponse signinRespObj;

    private SignonResponse registrationResponse;
    private int mScheduleRecyclerViewScrollPosition = 0;

    //used for deep linking
    private boolean isHomeContainerActivityLaunched;
    private boolean discontinuedAlertShown;
    private boolean isFromNotificationAction;

    private boolean homeCardsShown = false;
    private boolean isBackFromExpandedCard;
    private boolean refreshHomeCardsPending;
    private boolean isAppInExpandedCard;
    private boolean isNotificationGenerated;
    private boolean navigateToRefillScreen;
    private boolean isCurrentReminderCardRefreshRequired;
    private String interruptType;
    private boolean fromInterruptScreen;
    private boolean interruptScreenBackButtonClicked;
    private Map<String, Boolean> regionListParams = new HashMap();
    private String serviceArea;
    private int lastSelectedFragmentPosition = -1;
    private boolean initialGetStateCompleted;
    private String cpCode;
    private boolean isUserLogedInAndAppTimeout;
    private boolean isFromTutorialScreen;
    private List<User> enabledUsersList;
    private String runTimePhoneNumber;
    private PillpopperTime reminderPillpopperTime;
    private PillpopperTime secondaryReminderPillpopperTime;
    private TTGUserResponse userResponse;
    private TTGInteruptAPIResponse mTTtgInteruptAPIResponse;
    private boolean isInturruptScreenVisible = false;
    private long inturruptScreenEnteredTimeStamp;
    private String saveTempUserNameForInterrupt;
    private boolean isTimeOutOccuredDuringInturruptBackGround = false;
    private boolean isAppProfileKeyOrValueMissing;
    private boolean isRegionContactAPICallRequired;
    private boolean isFirstTimeLandingOnHomeScreen;
    private NotificationBarOrderedBroadcastHandler mNotificationBarOrderedBroadcastHandler;

    private boolean mFingerPrintTCInProgress = false;
    private boolean showFingerprintDialog = true;
    private boolean showSplashAnimation = true;
    private boolean isNeedToAnimateLogin = true;
    private boolean isNewScheduleRequired;
    private ScheduleViewModel scheduleViewModel;
    private String kphcSelectedUserID;
    private String medName;
    private PillpopperDay focusDay;
    private boolean accessTokenCalledForFailedFDBImages;
    private int imageAPIDownloadCounter = 0;
    private boolean getImagesSkippedWhileHandleGetState;
    private boolean isNeedToSetValuesForRefill;
    private AlertDialog mAlertDialog;
    private boolean isLoadingInProgress;
    private boolean FCMInitialized;
    private boolean fingerPrintOptInProgress;
    private boolean isFromHistory;
    private boolean isSeeMoreEnabled = false;
    private boolean isSaveButtonEnabled;
    private boolean isScheduleEdited = false;

    // getting the weeks start and end days
    private PillpopperDay weekStartDay;
    private PillpopperDay weekEndDay;

    private boolean isFromSplashScreen = false;

    private boolean isHistoryOverlayShown = false;
    private int homeNavPosition;

    public void setWeekStartDay(PillpopperDay weekStartDay) {
        this.weekStartDay = weekStartDay;
    }

    public PillpopperDay getWeekStartDay() {
        return weekStartDay;
    }

    public void setWeekEndDay(PillpopperDay weekEndDay) {
        this.weekEndDay = weekEndDay;
    }

    public PillpopperDay getWeekEndDay() {
        return weekEndDay;
    }

    public void setSeeMoreEnabled(boolean seeMoreEnabled) {
        this.isSeeMoreEnabled = seeMoreEnabled;
    }

    public boolean getSeeMoreEnabled() {
        return this.isSeeMoreEnabled;
    }

    private AnnouncementsResponse announcements;

    public boolean isHistoryMedChanged() {
        return isHistoryMedChanged;
    }

    public void setHistoryMedChanged(boolean historyMedChanged) {
        isHistoryMedChanged = historyMedChanged;
    }

    private boolean isHistoryMedChanged;
    private String launchSource;


    public boolean isRestored() {
        return isRestored;
    }

    public void setRestored(boolean restored) {
        isRestored = restored;
    }

    private boolean isRestored;
    private boolean isAppProfileInProgress;

    private boolean isRxRefillOnAEMPages;
    private String drugGuidFromEnlargeAct;
    private String drugImageGuidFromEnlargeAct;
    private boolean isImageDeleted;
    private List<String> pillIdList = new ArrayList<>();
    private HashMap<String, NLPReminder> drugsNLPRemindersList = new HashMap<>();

    public boolean isMedDetailView() {
        return isMedDetailView;
    }

    public void setMedDetailView(boolean medDetailView) {
        isMedDetailView = medDetailView;
    }

    private boolean isMedDetailView;

    public boolean isFromArchive() {
        return isFromArchive;
    }

    public void setFromArchive(boolean fromArchive) {
        isFromArchive = fromArchive;
    }

    private boolean isFromArchive;

    public String getKphcSelectedUserID() {
        return kphcSelectedUserID;
    }

    private EditScheduleRunTimeData scheduleData;

    public EditScheduleRunTimeData getScheduleData() {
        return scheduleData;
    }

    public void setScheduleData(EditScheduleRunTimeData scheduleData) {
        this.scheduleData = scheduleData;
    }

    public void setKphcSelectedUserID(String kphcSelectedUserID) {
        this.kphcSelectedUserID = kphcSelectedUserID;
    }


    public int getSpinnerPosition() {
        return SpinnerPosition;
    }

    public void setSpinnerPosition(int spinnerPosition) {
        SpinnerPosition = spinnerPosition;
    }

    private int SpinnerPosition;

    public ScheduleViewModel getScheduleViewModel(FragmentActivity activity) {
        if (scheduleViewModel == null) {
            scheduleViewModel = ViewModelProviders.of(activity).get(ScheduleViewModel.class);
        }
        return scheduleViewModel;
    }

    public void setScheduleViewModel(ScheduleViewModel scheduleViewModel) {
        this.scheduleViewModel = scheduleViewModel;
    }

    public Map<String, Boolean> getRegionListParams() {
        return regionListParams;
    }

    public void setRegionListParams(Map<String, Boolean> regionListParams) {
        this.regionListParams = regionListParams;
    }

    public boolean isInitialGetStateCompleted() {
        return initialGetStateCompleted;
    }

    public void setInitialGetStateCompleted(boolean initialGetStateCompleted) {
        this.initialGetStateCompleted = initialGetStateCompleted;
    }

    public boolean isFirstTimeLandingOnHomeScreen() {
        return isFirstTimeLandingOnHomeScreen;
    }

    public void setFirstTimeLandingOnHomeScreen(boolean firstTimeLanding) {
        isFirstTimeLandingOnHomeScreen = firstTimeLanding;
    }

    public boolean isNotificationGenerated() {
        return isNotificationGenerated;
    }

    public void setNotificationGenerated(boolean notificationGenerated) {
        isNotificationGenerated = notificationGenerated;
    }

    public int getScheduleRecyclerViewScrollPosition() {
        return mScheduleRecyclerViewScrollPosition;
    }

    public void setScheduleRecyclerViewScrollPosition(int scheduleRecyclerViewScrollPosition) {
        this.mScheduleRecyclerViewScrollPosition = scheduleRecyclerViewScrollPosition;
    }

    public SignonResponse getRegistrationResponse() {
        return registrationResponse;
    }

    public void setRegistrationResponse(SignonResponse registrationResponse) {
        this.registrationResponse = registrationResponse;
    }

    public boolean isHasStatusCallInProgress() {
        return isHasStatusCallInProgress;
    }

    public void setHasStatusCallInProgress(boolean hasStatusCallInProgress) {
        isHasStatusCallInProgress = hasStatusCallInProgress;
    }

    private boolean isHasStatusCallInProgress;

    public List<String> getSelectedUsersList() {
        return selectedUsersList;
    }

    public void setSelectedUsersList(List<String> selectedUsersList) {
        this.selectedUsersList = selectedUsersList;
    }

    private List<String> selectedUsersList = new ArrayList<>();

    /**
     * @return the cpCode
     */
    public String getCpCode() {
        return cpCode;
    }

    /**
     * @param cpCode the cpCode to set
     */
    public void setCpCode(String cpCode) {
        this.cpCode = cpCode;
    }

    public boolean isClickFlg() {
        return isClickFlg;
    }

    public void setClickFlg(boolean isClickFlg) {
        this.isClickFlg = isClickFlg;
    }

    public static RunTimeData getInstance() {
        return runTimeData;
    }


    public void setAlertDisplayedFlg(boolean isAlertDisplayedFlg) {
        this.isAlertDisplayedFlg = isAlertDisplayedFlg;
    }

    public int getHomeButtonPressed() {
        return homeButtonPressed;
    }

    public void setHomeButtonPressed(int homeButtonPressed) {
        this.homeButtonPressed = homeButtonPressed;
    }

    public long getOldLockTime() {
        return oldLockTime;
    }

    public void setOldLockTime(long oldLockTime) {
        this.oldLockTime = oldLockTime;
    }

    public boolean isAppVisible() {
        return appVisibleFlg;
    }

    public void setAppVisibleFlg(boolean appVisible) {
        this.appVisibleFlg = appVisible;
    }

    public long getNewLockTime() {
        return newLockTime;
    }

    public void setNewLockTime(long newLockTime) {
        this.newLockTime = newLockTime;
    }

    public void setUserLoginFlg(boolean b) {
        userLoginFlg = b;
    }

    public boolean getUserLoginFlg() {
        return userLoginFlg;
    }

    public TTGUserResponse getSigninRespObj() {
        return signinRespObj;
    }

    public void setSigninRespObj(TTGUserResponse signinRespObj) {
        this.signinRespObj = signinRespObj;
    }

    public boolean isUserLogedInAndAppTimeout() {
        return isUserLogedInAndAppTimeout;
    }

    public void setUserLogedInAndAppTimeout(boolean isUserLogedInAndAppTimeout) {
        this.isUserLogedInAndAppTimeout = isUserLogedInAndAppTimeout;
    }

    public String getRuntimeSSOSessionID() {
        return runtimeSSOSessionID;
    }

    public void setRuntimeSSOSessionID(String runtimeSSOSessionID) {
        this.runtimeSSOSessionID = runtimeSSOSessionID;
    }

    public boolean isFromTutorialScreen() {
        return isFromTutorialScreen;
    }

    public void setFromTutorialScreen(boolean isFromTutorialScreen) {
        this.isFromTutorialScreen = isFromTutorialScreen;
    }

    public List<User> getEnabledUsersList() {
        return enabledUsersList;
    }

    public void setEnabledUsersList(List<User> enabledUsersList) {
        this.enabledUsersList = enabledUsersList;
    }

    public String getRunTimePhoneNumber() {
        return runTimePhoneNumber;
    }

    public void setRunTimePhoneNumber(String runTimePhoneNumber) {
        this.runTimePhoneNumber = runTimePhoneNumber;
    }

    public PillpopperTime getReminderPillpopperTime() {
        return reminderPillpopperTime;
    }

    public void setReminderPillpopperTime(PillpopperTime reminderPillpopperTime) {
        this.reminderPillpopperTime = reminderPillpopperTime;
    }

    public PillpopperTime getSecondaryReminderPillpopperTime() {
        return secondaryReminderPillpopperTime;
    }

    public void setSecondaryReminderPillpopperTime(PillpopperTime secondaryReminderPillpopperTime) {
        this.secondaryReminderPillpopperTime = secondaryReminderPillpopperTime;
    }

    private boolean isDeviceInFingerprintLockout = false;

    public void setHomeContainerActivityLaunched(boolean homeContainerActivityLaunched) {
        this.isHomeContainerActivityLaunched = homeContainerActivityLaunched;
    }

    public boolean isHomeContainerActivityLaunched() {
        return isHomeContainerActivityLaunched;
    }

    public void setDiscontinuedAlertShown(boolean discontinuedAlertShown) {
        this.discontinuedAlertShown = discontinuedAlertShown;
    }

    public void setIsFromMDO(boolean isFromMDO) {
        this.isFromMDO = isFromMDO;
    }

    public boolean isFromMDO() {
        return isFromMDO;
    }

    public boolean isDiscontinuedAlertShown() {
        return discontinuedAlertShown;
    }

    public boolean isFromNotificationAction() {
        return isFromNotificationAction;
    }

    public void setFromNotificationAction(boolean isFromNotificationAction) {
        this.isFromNotificationAction = isFromNotificationAction;
    }

    public boolean isDeviceInFingerprintLockout() {
        return isDeviceInFingerprintLockout;
    }

    public void setDeviceInFingerprintLockout(boolean deviceInFingerprintLockout) {
        isDeviceInFingerprintLockout = deviceInFingerprintLockout;
    }

    public boolean isHomeCardsShown() {
        return homeCardsShown;
    }

    public void setHomeCardsShown(boolean homeCardsShown) {
        this.homeCardsShown = homeCardsShown;
    }

    public void resetRefreshCardsFlags() {
        setBackFromExpandedCard(false);
        setRefreshHomeCardsPending(false);
        setAppInExpandedCard(false);
    }

    public boolean isBackFromExpandedCard() {
        return isBackFromExpandedCard;
    }

    public void setBackFromExpandedCard(boolean backFromExpandedCard) {
        isBackFromExpandedCard = backFromExpandedCard;
    }

    public boolean isRefreshHomeCardsPending() {
        return refreshHomeCardsPending;
    }

    public void setRefreshHomeCardsPending(boolean refreshHomeCardsPending) {
        this.refreshHomeCardsPending = refreshHomeCardsPending;
    }

    public boolean isAppInExpandedCard() {
        return isAppInExpandedCard;
    }

    public void setAppInExpandedCard(boolean appInExpandedCard) {
        isAppInExpandedCard = appInExpandedCard;
    }

    public void setNavigateToRefillScreen(boolean navigateToRefillScreen) {
        this.navigateToRefillScreen = navigateToRefillScreen;
    }

    public boolean isNavigateToRefillScreen() {
        return navigateToRefillScreen;
    }

    public boolean isCurrentReminderCardRefreshRequired() {
        return isCurrentReminderCardRefreshRequired;
    }

    public void setCurrentReminderCardRefreshRequired(boolean currentReminderCardRefreshRequired) {
        isCurrentReminderCardRefreshRequired = currentReminderCardRefreshRequired;
    }

    public TTGUserResponse getUserResponse() {
        return userResponse;
    }

    public void setUserResponse(TTGUserResponse userResponse) {
        this.userResponse = userResponse;
    }

    public void setInterruptType(String interruptType) {
        this.interruptType = interruptType;
    }

    public String getInterruptType() {
        return interruptType;
    }

    public TTGInteruptAPIResponse getmTTtgInteruptAPIResponse() {
        return mTTtgInteruptAPIResponse;
    }

    public void setInterruptGetAPIResponse(TTGInteruptAPIResponse interruptResponse) {
        mTTtgInteruptAPIResponse = interruptResponse;
    }

    public void setFromInterruptScreen(boolean fromInterruptScreen) {
        this.fromInterruptScreen = fromInterruptScreen;
    }

    public boolean isFromInterruptScreen() {
        return fromInterruptScreen;
    }

    public boolean isInterruptScreenBackButtonClicked() {
        return interruptScreenBackButtonClicked;
    }

    public void setInterruptScreenBackButtonClicked(boolean interruptScreenBackButtonClicked) {
        this.interruptScreenBackButtonClicked = interruptScreenBackButtonClicked;
    }

    public boolean isInturruptScreenVisible() {
        return isInturruptScreenVisible;
    }

    public void setInturruptScreenVisible(boolean inturruptScreenVisible) {
        isInturruptScreenVisible = inturruptScreenVisible;
    }

    public long getInturruptScreenEnteredTimeStamp() {
        return inturruptScreenEnteredTimeStamp;
    }

    public void setInturruptScreenEnteredTimeStamp(long inturruptScreenEnteredTimeStamp) {
        this.inturruptScreenEnteredTimeStamp = inturruptScreenEnteredTimeStamp;
    }

    public String getSaveTempUserNameForInterrupt() {
        return saveTempUserNameForInterrupt;
    }

    public void setSaveTempUserNameForInterrupt(String saveTempUserNameForInterrupt) {
        this.saveTempUserNameForInterrupt = saveTempUserNameForInterrupt;
    }

    public boolean isTimeOutOccuredDuringInturruptBackGround() {
        return isTimeOutOccuredDuringInturruptBackGround;
    }

    public void setTimeOutOccuredDuringInturruptBackGround(boolean timeOutOccuredDuringInturruptBackGround) {
        isTimeOutOccuredDuringInturruptBackGround = timeOutOccuredDuringInturruptBackGround;
    }

    public boolean isAppProfileKeyOrValueMissing() {
        return isAppProfileKeyOrValueMissing;
    }

    public void setAppProfileKeyOrValueMissing(boolean appProfileKeyOrValueMissing) {
        isAppProfileKeyOrValueMissing = appProfileKeyOrValueMissing;
    }

    public boolean isRegionContactAPICallRequired() {
        return isRegionContactAPICallRequired;
    }

    public void setRegionContactAPICallRequired(boolean regionContactAPICallRequired) {
        isRegionContactAPICallRequired = regionContactAPICallRequired;
    }

    public void setServiceArea(String serviceArea) {
        this.serviceArea = serviceArea;
    }

    public String getServiceArea() {
        return serviceArea;
    }

    public void setLastSelectedFragmentPosition(int lastSelectedFragmentPosition) {
        this.lastSelectedFragmentPosition = lastSelectedFragmentPosition;
    }

    public int getLastSelectedFragmentPosition() {
        return lastSelectedFragmentPosition;
    }

    public NotificationBarOrderedBroadcastHandler getNotificationBarOrderedBroadcastHandler() {
        return mNotificationBarOrderedBroadcastHandler;
    }

    public void setNotificationBarOrderedBroadcastHandler(NotificationBarOrderedBroadcastHandler notificationBarOrderedBroadcastHandler) {
        this.mNotificationBarOrderedBroadcastHandler = notificationBarOrderedBroadcastHandler;
    }

    public boolean isShowFingerprintDialog() {
        return showFingerprintDialog;
    }

    public void setShowFingerprintDialog(boolean showFingerprintDialog) {
        this.showFingerprintDialog = showFingerprintDialog;
    }

    public boolean ismFingerPrintTCInProgress() {
        return mFingerPrintTCInProgress;
    }

    public void setmFingerPrintTCInProgress(boolean mFingerPrintTCInProgress) {
        this.mFingerPrintTCInProgress = mFingerPrintTCInProgress;
    }

    public boolean isShowSplashAnimation() {
        return showSplashAnimation;
    }

    public void setShowSplashAnimation(boolean showSplashAnimation) {
        this.showSplashAnimation = showSplashAnimation;
    }

    public boolean isNeedToAnimateLogin() {
        return isNeedToAnimateLogin;
    }

    public void setNeedToAnimateLogin(boolean needToAnimateLogin) {
        isNeedToAnimateLogin = needToAnimateLogin;
    }

    public void setIsNewScheduleRequired(boolean isNewScheduleRequired) {
        this.isNewScheduleRequired = isNewScheduleRequired;
    }

    public boolean getIsNewScheduleRequired() {
        return isNewScheduleRequired;
    }

    public boolean isAppProfileInProgress() {
        return isAppProfileInProgress;
    }

    public void setAppProfileInProgress(boolean appProfileInProgress) {
        isAppProfileInProgress = appProfileInProgress;
    }

    public boolean isRxRefillOnAEMPages() {
        return isRxRefillOnAEMPages;
    }

    public void setRxRefillOnAEMPages(boolean rxRefillOnAEMPages) {
        isRxRefillOnAEMPages = rxRefillOnAEMPages;
    }

    public void setDrugGuidFromEnlargeAct(String drugGuidFromEnlargeAct) {
        this.drugGuidFromEnlargeAct = drugGuidFromEnlargeAct;
    }

    public String getDrugGuidFromEnlargeAct() {
        return drugGuidFromEnlargeAct;
    }

    public void setDrugImageGuidFromEnlargeAct(String drugImageGuidFromEnlargeAct) {
        this.drugImageGuidFromEnlargeAct = drugImageGuidFromEnlargeAct;
    }

    public String getDrugImageGuidFromEnlargeAct() {
        return drugImageGuidFromEnlargeAct;
    }

    public void setIsImageDeleted(boolean isImageDeleted) {
        this.isImageDeleted = isImageDeleted;
    }

    public boolean getIsImageDeleted() {
        return isImageDeleted;
    }

    public List<String> getPillIdList() {
        return pillIdList;
    }

    public void setPillIdList(List<String> pillIdList) {
        this.pillIdList = pillIdList;
    }

    public void setMedName(String medName) {
        this.medName = medName;
    }

    public String getMedName() {
        return medName;
    }

    public String getLaunchSource() {
        return launchSource;
    }

    public void setLaunchSource(String launchSource) {
        this.launchSource = launchSource;
    }

    public PillpopperDay getFocusDay() {
        return focusDay;
    }

    public void setFocusDay(PillpopperDay pillpopperDay) {
        this.focusDay = pillpopperDay;
    }

    public void setAccessTokenCalledForFailedFDBImages(boolean accessTokenCalledForFailedFDBImages) {
        this.accessTokenCalledForFailedFDBImages = accessTokenCalledForFailedFDBImages;
    }

    public boolean getAccessTokenCalledForFailedFDBImages() {
        return accessTokenCalledForFailedFDBImages;
    }

    public int getImageAPIDownloadCounter() {
        return imageAPIDownloadCounter;
    }

    public void setImageAPIDownloadCounter(int imageAPIDownloadCounter) {
        this.imageAPIDownloadCounter = imageAPIDownloadCounter;
    }

    public boolean isGetImagesSkippedWhileHandleGetState() {
        return getImagesSkippedWhileHandleGetState;
    }

    public void setGetImagesSkippedWhileHandleGetState(boolean getImagesSkippedWhileHandleGetState) {
        this.getImagesSkippedWhileHandleGetState = getImagesSkippedWhileHandleGetState;
    }

    public void setIsNeedToSetValuesForRefill(boolean isNeedToSetValuesForRefill) {
        this.isNeedToSetValuesForRefill = isNeedToSetValuesForRefill;
    }

    public boolean isNeedToSetValuesForRefill() {
        return isNeedToSetValuesForRefill;
    }

    public Context getmContext() {
        return mContext;
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    public Context mContext;

    public HashMap<String, NLPReminder> getDrugsNLPRemindersList() {
        return drugsNLPRemindersList;
    }

    public void setDrugsNLPRemindersList(HashMap<String, NLPReminder> drugsNLPRemindersList) {
        this.drugsNLPRemindersList = drugsNLPRemindersList;
    }

    public void setAlertDialogInstance(AlertDialog alertDialog) {
        this.mAlertDialog = alertDialog;
    }

    public AlertDialog getAlertDialogInstance() {
        return mAlertDialog;
    }

    public boolean isLoadingInProgress() {
        return isLoadingInProgress;
    }

    public void setLoadingInProgress(boolean loadingInProgress) {
        isLoadingInProgress = loadingInProgress;
    }

    public boolean isFCMInitialized() {
        return FCMInitialized;
    }

    public void setFCMInitialized(boolean fcmInitialized) {
        this.FCMInitialized = fcmInitialized;
    }

    public void setFingerPrintOptInProgress(boolean fingerPrintOptInProgress) {
        this.fingerPrintOptInProgress = fingerPrintOptInProgress;
    }

    public boolean isFingerPrintOptInProgress() {
        return fingerPrintOptInProgress;
    }

    public void setAnnouncements(AnnouncementsResponse announcements) {
        this.announcements = announcements;
    }

    public AnnouncementsResponse getAnnouncements() {
        return announcements;
    }

    private AnnouncementsItem announcementsItem;

    public AnnouncementsItem getAnnouncementsItem() {
        return announcementsItem;
    }

    public void setAnnouncementsItem(AnnouncementsItem announcementsItem) {
        this.announcementsItem = announcementsItem;
    }

    public boolean isBiomerticChecked() {
        return isBiomerticChecked;
    }

    public void setBiomerticChecked(boolean biomerticChecked) {
        isBiomerticChecked = biomerticChecked;
    }

    private boolean isBiomerticChecked;

    private boolean isBiomerticFinished;

    public boolean isBiomerticFinished() {
        return isBiomerticFinished;
    }

    public void setBiomerticFinished(boolean biomerticFinished) {
        isBiomerticFinished = biomerticFinished;
    }

    public void setIsFromHistory(boolean isFromHistory) {
        this.isFromHistory = isFromHistory;
    }

    public boolean isFromHistory() {
        return isFromHistory;
    }

    public PillpopperDay getCalenderStartDate() {
        return calenderStartDate;
    }

    public void setCalenderStartDate(PillpopperDay calenderStartDate) {
        this.calenderStartDate = calenderStartDate;
    }

    public PillpopperDay calenderStartDate;

    public PillpopperDay getCalenderEndDate() {
        return calenderEndDate;
    }

    public void setCalenderEndDate(PillpopperDay calenderEndDate) {
        this.calenderEndDate = calenderEndDate;
    }

    public PillpopperDay calenderEndDate;

    public boolean isFromSplashScreen() {
        return isFromSplashScreen;
    }

    public void setFromSplashScreen(boolean fromSplashScreen) {
        isFromSplashScreen = fromSplashScreen;
    }

    public boolean isHistoryOverlayShown() {
        return isHistoryOverlayShown;
    }

    public void setHistoryOverlayShown(boolean historyOverlayShown) {
        isHistoryOverlayShown = historyOverlayShown;
    }

    public boolean isSaveButtonEnabled() {
        return isSaveButtonEnabled;
    }

    public void setSaveButtonEnabled(boolean saveButtonEnabled) {
        isSaveButtonEnabled = saveButtonEnabled;
    }

    public boolean isScheduleEdited() {
        return isScheduleEdited;
    }

    public void setScheduleEdited(boolean scheduleEdited) {
        isScheduleEdited = scheduleEdited;
    }

    public boolean isUserSelected() {
        return isUserSelected;
    }

    public void setUserSelected(boolean userSelected) {
        isUserSelected = userSelected;
    }

    public int getHomeNavPosition() {
        return homeNavPosition;
    }

    public void setHomeNavPosition(int homeNavPosition) {
        this.homeNavPosition = homeNavPosition;
    }




    public boolean getIsOverlayItemClicked() {
        return isOverlayItemClicked;
    }

    public void setIsOverlayItemClicked(boolean isOverlayItemClicked) {
        this.isOverlayItemClicked = isOverlayItemClicked;
    }

    private boolean isOverlayItemClicked;


    public int getListPosition() {
        return listPosition;
    }

    public void setListPosition(int listPosition) {
        this.listPosition = listPosition;
    }

    private int listPosition;

    public int getListCalendarPosition() {
        return listCalendarPosition;
    }

    public void setListCalendarPosition(int listCalendarPosition) {
        this.listCalendarPosition = listCalendarPosition;
    }

    private int listCalendarPosition;

    public int getWeekSelectedPosition() {
        return weekSelectedPosition;
    }

    public void setWeekSelectedPosition(int weekSelectedPosition) {
        this.weekSelectedPosition = weekSelectedPosition;
    }

    public boolean isCalendarPosChanged() {
        return isCalendarPosChanged;
    }

    public void setCalendarPosChanged(boolean calendarPosChanged) {
        isCalendarPosChanged = calendarPosChanged;
    }

    private boolean isCalendarPosChanged;


    private int weekSelectedPosition;

    private boolean shouldRetainHistoryOverlay = false;

    private boolean isHistoryItemUpdated = false;

    public boolean isShouldRetainHistoryOverlay() {
        return shouldRetainHistoryOverlay;
    }

    public void setShouldRetainHistoryOverlay(boolean shouldRetainHistoryOverlay) {
        this.shouldRetainHistoryOverlay = shouldRetainHistoryOverlay;
    }

    public boolean isHistoryItemUpdated() {
        return isHistoryItemUpdated;
    }

    public void setHistoryItemUpdated(boolean historyItemUpdated) {
        isHistoryItemUpdated = historyItemUpdated;
    }

    private boolean isHistoryConfigChanged = false;

    public boolean isHistoryConfigChanged() {
        return isHistoryConfigChanged;
    }

    public void setHistoryConfigChanged(boolean historyConfigChanged) {
        isHistoryConfigChanged = historyConfigChanged;
    }

    // used only in bulk schedule flow
    private boolean isBulkMedsScheduleSaved;

    public boolean isBulkMedsScheduleSaved() {
        return isBulkMedsScheduleSaved;
    }

    public void setBulkMedsScheduleSaved(boolean bulkMedsScheduleSaved) {
        isBulkMedsScheduleSaved = bulkMedsScheduleSaved;
    }
}
