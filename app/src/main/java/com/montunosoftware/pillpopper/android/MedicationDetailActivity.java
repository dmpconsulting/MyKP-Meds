package com.montunosoftware.pillpopper.android;

import android.content.Intent;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Html;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.PopupMenu;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.mymeds.databinding.MedicationDetailActivityBinding;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.util.NLPUtils;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.android.util.ViewClickHandler;
import com.montunosoftware.pillpopper.android.view.DialogHelpers;
import com.montunosoftware.pillpopper.android.view.EditScheduleRunTimeData;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.database.DatabaseConstants;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.HourMinute;
import com.montunosoftware.pillpopper.model.NLPReminder;
import com.montunosoftware.pillpopper.model.NLPRemindersRequestObject;
import com.montunosoftware.pillpopper.model.PillpopperDay;
import com.montunosoftware.pillpopper.model.PillpopperRunTime;
import com.montunosoftware.pillpopper.model.PillpopperTime;
import com.montunosoftware.pillpopper.model.Schedule;
import com.montunosoftware.pillpopper.model.TimeList;
import com.montunosoftware.pillpopper.service.LogEntryUpdateAsyncTask;
import com.montunosoftware.pillpopper.service.NLPGetRemindersService;
import com.montunosoftware.pillpopper.service.images.loader.ImageUILoaderManager;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.PopUpListener;
import org.kp.tpmg.mykpmeds.activation.activity.EnlargeImageActivity;
import org.kp.tpmg.mykpmeds.activation.activity.LoadingActivity;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;


public class MedicationDetailActivity extends StateListenerActivity implements PopUpListener, View.OnClickListener, ScheduleWizardFragment.OnAddMedicationClicked, ScheduleWizardFragment.onCustomBackPressed, NLPGetRemindersService.NLPRemindersResponseListener {

    private MedicationDetailActivityBinding binding;
    private String pillId;
    private String mLaunchSource;
    private FrontController mFrontController;
    private Drug editDrug;
    private PillpopperDay endReminderDay;
    private View childLayout;
    private final int ADD_HISTORY_ENTRY = 200;
    private final int REQ_EDIT_SCHEDULE = 1;
    private List<Drug> finalDrugs;
    private int clickCount;
    private PopupWindow mPopupWindow;
    private PopupMenu schedulePopUpMenu;
    private static final int SAVE_NOTES = 123;
    private static final int SAVE_OTC_MEDICATION = 112;
    private NumberPicker numberPicker;
    private int newDailyLimit = -1;
    private EditScheduleRunTimeData scheduleData = new EditScheduleRunTimeData();
    ArrayList<String> scheduleTime = new ArrayList<>();
    private PillpopperActivity mPillPopperActivity;
    private Typeface mFontMedium;
    private Typeface mFontRegular;
    private boolean isMedRestored;
    private AlertDialog restoreMedicationAlert;
    private int rlDetailWidth,rlInfoImageWidth;
    private boolean deleteScheduleOKClicked;
    private boolean isPopUpTextRequired;
    private int scrollX,scrollY;
    private boolean isFromHistoryModule;
    private boolean isEditReminderClicked = false;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        binding = DataBindingUtil.setContentView(this, R.layout.medication_detail_activity);
        setFonts();
        setValues();
        setListeners();
        initActionBar();
        setScrollObserver();
        binding.scheduleBlock.placeholderPopup.rlDetail.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
            rlDetailWidth = binding.scheduleBlock.placeholderPopup.rlDetail.getMeasuredWidth();
            rlInfoImageWidth = binding.scheduleBlock.infoImage.getMeasuredWidth();

        });
    }

    private void setScrollObserver() {
        binding.pageContainer.getViewTreeObserver().addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
            @Override
            public void onScrollChanged() {

                // horizontal scroll position
                 scrollX = binding.pageContainer.getScrollX();

                // vertical scroll position
                 scrollY = binding.pageContainer.getScrollY();
            }
        });
    }

    private void setListeners() {
        binding.restoreDeletBlock.archiveDetailsRestoreMedicationButton.setOnClickListener(this);
        binding.restoreDeletBlock.archiveDetailsDeleteMedicationButton.setOnClickListener(this);
        binding.medicationBlock.drugDetailDoseImage.setOnClickListener(this);
        binding.medicationBlock.editBtn.setOnClickListener(this);
        binding.personalNotesBlock.addOrEditNotes.setOnClickListener(this);
        binding.medicationBlock.expandIcon.setOnClickListener(this);
        binding.medicationBlock.collapseIcon.setOnClickListener(this);
        if (RunTimeData.getInstance().isMedDetailView()) {
            binding.scheduleBlock.rlMaxDose.setOnClickListener(this);
        }
        binding.scheduleBlock.takeAsNeededSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            dismissPopUpWindow();
            boolean deleteScheduleAlertShown = false;
            if (isChecked) {
                //show delete alert if there are any schedules added to the medication
                if (editDrug.getSchedule().getTimeList().length() > 0) {
                    showDeleteScheduleAlert(true);
                    deleteScheduleAlertShown = true;
                } else {
                    binding.scheduleBlock.setTakeAsNeededSwitchStatus(isChecked);
                    if(isPopUpTextRequired) {
                        isPopUpTextRequired = isChecked;
                    }
                    editDrug.getPreferences().setPreference("scheduleChoice", AppConstants.SCHEDULE_CHOICE_AS_NEEDED);
                    updateScheduleChoice(AppConstants.SCHEDULE_CHOICE_AS_NEEDED);
                    binding.scheduleBlock.maxDoseNumber.setText(editDrug.getSchedule().getDailyLimit() <= 0 ?
                            getString(R.string.no_dose_limit) : String.valueOf(editDrug.getSchedule().getDailyLimit()));
                }
            } else {
                editDrug.getPreferences().setPreference("scheduleChoice", editDrug.getSchedule().getTimeList().length()>0 ? AppConstants.SCHEDULE_CHOICE_SCHEDULED:AppConstants.SCHEDULE_CHOICE_UNDEFINED);
                updateScheduleChoice(editDrug.getSchedule().getTimeList().length()>0 ? AppConstants.SCHEDULE_CHOICE_SCHEDULED:AppConstants.SCHEDULE_CHOICE_UNDEFINED);
                binding.scheduleBlock.setIsScheduled(false);
                isPopUpTextRequired = false;
                if (RunTimeData.getInstance().isMedDetailView()) {
                    Util.setVisibility(new View[]{binding.scheduleBlock.editSchedule,binding.scheduleBlock.relMain,binding.scheduleBlock.placeholderPopup.rlDetail}, View.GONE);
                }
                loadDrugDetails();
                initDetailViewUI();
            }
            if (!deleteScheduleAlertShown) {
                editDrug.setScheduleGuid(Util.getRandomGuid());
                mFrontController.updateScheduleGUID(editDrug.getScheduleGuid(), editDrug.getGuid());
                addLogEntryForEdit(editDrug, mPillPopperActivity);
            }
        });
        binding.personalNotesBlock.addOrEditNotes.setContentDescription(null!=binding.personalNotesBlock.personalNotesText?getResources().getString(R.string.content_description_add_notes):getResources().getString(R.string.content_description_edit_notes));
    }

    private void updateScheduleChoice(String scheduleChoice) {
        mFrontController.updateScheduleChoice(scheduleChoice, pillId);
    }

    private void setValues() {
        binding.scheduleBlock.setButtonClickHandler(this);
        binding.archiveBlock.setButtonClickHandler(this);
        mFrontController = FrontController.getInstance(this);
        Util.hideKeyboard(this, binding.scheduleBlock.maxDoseNumber);
    }

    private void initDetailViewUI() {
        mPillPopperActivity = (PillpopperActivity) getActivity();
        if (RunTimeData.getInstance().isMedDetailView()) {
            FireBaseAnalyticsTracker.getInstance().logScreenEvent(_thisActivity, FireBaseConstants.ScreenEvent.SCREEN_MED_DETAILS);
            binding.scheduleBlock.relMain.setVisibility(View.VISIBLE);
            if ((("SCHEDULED").equalsIgnoreCase(Util.getScheduleChoice(editDrug))
                    && null != FrontController.getInstance(_thisActivity).getSchdulesByPillId(pillId) && FrontController.getInstance(_thisActivity).getSchdulesByPillId(pillId).length() > 0)) {
                binding.scheduleBlock.editSchedule.setVisibility(View.VISIBLE);
            }else {
                binding.scheduleBlock.editSchedule.setVisibility(View.GONE);
            }
            Util.setVisibility(new View[]{binding.archiveBlock.lrArchiveLayout,
                    binding.personalNotesBlock.addOrEditNotes,
                    binding.scheduleBlock.relSwitchDose,
            }, View.VISIBLE);
            isPopUpTextRequired = true;
            Util.setVisibility(new View[]{binding.restoreDeletBlock.lrMain},View.GONE);
            if (!binding.scheduleBlock.maxDoseNumber.getText().toString().equalsIgnoreCase("None")) {
                Util.setVisibility(new View[]{binding.scheduleBlock.takeAsNeededSwitch, binding.scheduleBlock.takeAsNeededButton}, View.VISIBLE);
            }
            if(binding.scheduleBlock.getTakeAsNeededSwitchStatus()){
                Util.setVisibility(new View[]{binding.scheduleBlock.takeAsNeededSwitch, binding.scheduleBlock.takeAsNeededButton,binding.scheduleBlock.rlMaxDose,binding.scheduleBlock.doseDivider}, View.VISIBLE);
            }
            binding.medicationBlock.editBtn.setVisibility(editDrug.isManaged() ? View.GONE : View.VISIBLE);
        } else {
            /*if(!isPopUpTextRequired) {
                isPopUpTextRequired = true;
            }*/
            Util.setVisibility(new View[]{binding.archiveBlock.lrArchiveLayout, binding.medicationBlock.editBtn, binding.scheduleBlock.editSchedule, binding.personalNotesBlock.addOrEditNotes}, View.GONE);
            if(!RunTimeData.getInstance().isFromArchive()) {
                FireBaseAnalyticsTracker.getInstance().logScreenEvent(_thisActivity, FireBaseConstants.ScreenEvent.SCREEN_MED_DETAILS_UNEDITABLE);
            }else{
                FireBaseAnalyticsTracker.getInstance().logScreenEvent(_thisActivity, FireBaseConstants.ScreenEvent.SCREEN_ARCHIVE_DETAILS);
            }
            if ((("SCHEDULED").equalsIgnoreCase(Util.getScheduleChoice(editDrug))
                    && editDrug.getSchedule().getTimeList().length() > 0 )) {
                binding.scheduleBlock.relMain.setVisibility(View.VISIBLE);
            } else {
                binding.scheduleBlock.relMain.setVisibility(View.GONE);
            }
            if (!binding.scheduleBlock.maxDoseNumber.getText().toString().equalsIgnoreCase("None")) {
                Util.setVisibility(new View[]{binding.scheduleBlock.relMain, binding.scheduleBlock.relSwitchDose}, View.VISIBLE);
                binding.scheduleBlock.takeAsNeededSwitch.setVisibility(View.INVISIBLE);
                binding.scheduleBlock.takeAsNeededButton.setVisibility(View.GONE);
            } else {
                if(binding.scheduleBlock.getTakeAsNeededSwitchStatus()){
                    Util.setVisibility(new View[]{binding.scheduleBlock.relMain, binding.scheduleBlock.relSwitchDose}, View.VISIBLE);
                    binding.scheduleBlock.takeAsNeededSwitch.setVisibility(View.GONE);
                    binding.scheduleBlock.takeAsNeededButton.setVisibility(View.GONE);
                    binding.scheduleBlock.rlMaxDose.setVisibility(View.GONE);
                    binding.scheduleBlock.doseDivider.setVisibility(View.GONE);
                }else{
                    binding.scheduleBlock.relSwitchDose.setVisibility(View.GONE);
                }
            }

            if(binding.scheduleBlock.relMain.getVisibility() == View.GONE){
                binding.scheduleBlock.placeholderPopup.rlDetail.setVisibility(View.GONE);
            }
        }
        Util.setVisibility(new View[]{binding.restoreDeletBlock.lrMain}, RunTimeData.getInstance().isFromArchive() ? View.VISIBLE : View.GONE);
    }

    private void setFonts() {
        mFontMedium = ActivationUtil.setFontStyle(getActivity(), AppConstants.FONT_ROBOTO_MEDIUM);
        mFontRegular = ActivationUtil.setFontStyle(getActivity(), AppConstants.FONT_ROBOTO_REGULAR);
        binding.scheduleBlock.setRobotoMedium(mFontMedium);
        binding.scheduleBlock.setRobotoRegular(mFontRegular);
        binding.medicationBlock.setRobotoMedium(mFontMedium);
        binding.medicationBlock.setRobotoRegular(mFontRegular);
        binding.personalNotesBlock.setRobotoMedium(mFontMedium);
        binding.personalNotesBlock.setRobotoRegular(mFontRegular);
    }

    public void initActionBar() {
        setSupportActionBar((Toolbar) binding.appBar);
        getSupportActionBar().setTitle(getString(R.string.history_edit_screen_medication_label_text) + " " + getString(R.string.details) );
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void initValues() {
        binding.medicationBlock.drugDetailDoseImage.setDefaultImage(R.drawable.rx_image_generic);
        ImageUILoaderManager.getInstance().loadDrugImage(getActivity(), editDrug.getImageGuid(), editDrug.getGuid(), binding.medicationBlock.drugDetailDoseImage, Util.getDrawableWrapper(getActivity(), R.drawable.rx_image_generic));

        if (editDrug.isManaged()) {
            Util.setVisibility(new View[]{binding.medicationBlock.collapseIcon, binding.medicationBlock.instructionsOrNotesFullText}, View.GONE);
            binding.medicationBlock.rxNumber.setVisibility(Util.isEmptyString(editDrug.getPrescriptionNum()) ? View.GONE : View.VISIBLE);
            binding.medicationBlock.expandIcon.setVisibility(null != editDrug.getDirections() ? View.VISIBLE : View.GONE);
            binding.medicationBlock.instructionsOrNotesText.setVisibility(View.VISIBLE);
        } else {
            Util.setVisibility(new View[]{binding.medicationBlock.rxNumber, binding.medicationBlock.collapseIcon, binding.medicationBlock.instructionsOrNotesFullText}, View.GONE);
            binding.medicationBlock.expandIcon.setVisibility(null != editDrug.getNotes() ? View.VISIBLE : View.GONE);
            binding.medicationBlock.instructionsOrNotesText.setVisibility(View.VISIBLE);
        }
        if(RunTimeData.getInstance().isFromArchive()){
            binding.restoreDeletBlock.lrMain.setVisibility(View.VISIBLE);
            if(!editDrug.isManaged()){
                Util.setVisibility(new View[]{binding.restoreDeletBlock.archiveDetailsRestoreMedicationButton,binding.restoreDeletBlock.archiveDetailsRestoreMedicationHintTextview,binding.restoreDeletBlock.archiveDetailsDeleteMedicationButton,binding.restoreDeletBlock.tvDelet},View.VISIBLE);
            }else {
                Util.setVisibility(new View[]{binding.restoreDeletBlock.archiveDetailsRestoreMedicationButton,binding.restoreDeletBlock.archiveDetailsRestoreMedicationHintTextview},View.VISIBLE);
                Util.setVisibility(new View[]{binding.restoreDeletBlock.archiveDetailsDeleteMedicationButton,binding.restoreDeletBlock.tvDelet},View.GONE);
            }
        }
    }

    @Override
    protected void onStart() {
        super.onStart();
        loadDrugDetails();
        initDetailViewUI();
    }

    private void loadDrugDetails() {
        Intent intent = getIntent();
        if (null != intent) {
            pillId = intent.getStringExtra(PillpopperConstants.PILL_ID);
            isFromHistoryModule = intent.getBooleanExtra("isFromHistory",false);
            if (null != intent.getStringExtra(PillpopperConstants.LAUNCH_SOURCE)) {
                mLaunchSource = intent.getStringExtra(PillpopperConstants.LAUNCH_SOURCE);
            }
        }
        if(isFromHistoryModule){
            RunTimeData.getInstance().setIsFromHistory(true);
        }
        if(null == editDrug || isMedRestored) {
            editDrug = mFrontController.getDrugByPillId(pillId);
        }
        binding.memberName.setText(editDrug.getMemberFirstName());
        binding.memberName.setTypeface(ActivationUtil.setFontStyle(this, AppConstants.FONT_ROBOTO_MEDIUM));
        initValues();
        if (null != editDrug && editDrug.isManaged()) {
            ackNewUpdateKPHCMed();
        }
        if(!isPopUpTextRequired) {
            isPopUpTextRequired = (("SCHEDULED").equalsIgnoreCase(Util.getScheduleChoice(editDrug))
                    && null != FrontController.getInstance(_thisActivity).getSchdulesByPillId(pillId) && FrontController.getInstance(_thisActivity).getSchdulesByPillId(pillId).length() > 0);
        }
        binding.scheduleBlock.setIsScheduled(("SCHEDULED").equalsIgnoreCase(Util.getScheduleChoice(editDrug))
                && null != FrontController.getInstance(_thisActivity).getSchdulesByPillId(pillId) && FrontController.getInstance(_thisActivity).getSchdulesByPillId(pillId).length() > 0);

        if (!(("SCHEDULED").equalsIgnoreCase(Util.getScheduleChoice(editDrug))
                && null != FrontController.getInstance(_thisActivity).getSchdulesByPillId(pillId) && FrontController.getInstance(_thisActivity).getSchdulesByPillId(pillId).length() > 0)) {
            binding.scheduleBlock.editSchedule.setVisibility(View.GONE);
        }
        //Check for as needed med and decide the switch status
        if(!isPopUpTextRequired) {
            isPopUpTextRequired = !AppConstants.SCHEDULE_CHOICE_AS_NEEDED.equalsIgnoreCase(Util.getScheduleChoice(editDrug));
        }
        binding.scheduleBlock.setTakeAsNeededSwitchStatus(AppConstants.SCHEDULE_CHOICE_AS_NEEDED.equalsIgnoreCase(Util.getScheduleChoice(editDrug)));
        binding.scheduleBlock.takeAsNeededSwitch.setChecked(AppConstants.SCHEDULE_CHOICE_AS_NEEDED.equalsIgnoreCase(Util.getScheduleChoice(editDrug)));
        binding.medicationBlock.setDrug(editDrug);
        binding.scheduleBlock.setDrug(editDrug);
        binding.personalNotesBlock.setDrug(editDrug);
        if(!isPopUpTextRequired) {
            isPopUpTextRequired = editDrug.isManaged();
        }

        if (null != editDrug) {
            editDrug.getSchedule().setDailyLimit(Util.handleParseLong(editDrug.getMaxDosage()));
            binding.scheduleBlock.maxDoseNumber.setText(binding.scheduleBlock.getTakeAsNeededSwitchStatus() && 0 < editDrug.getSchedule().getDailyLimit() ?
                    String.valueOf(editDrug.getSchedule().getDailyLimit()) : getString(R.string.no_dose_limit));
            //PillpopperDay startReminderDay = editDrug.getSchedule().getStart();
            endReminderDay = editDrug.getSchedule().getEnd();
            scheduleData.setDurationType(null != editDrug.getScheduledFrequency() && editDrug.getScheduledFrequency().equalsIgnoreCase("W") ? "Weekly":"");
            scheduleData.setDuration(editDrug.getSchedule().getDayPeriod());
            scheduleData.setMeditationDuration(getScheduledFrequency(editDrug.getSchedule().getDayPeriod(), editDrug.getPreferences().getPreference("weekdays")));
            binding.scheduleBlock.scheduleLabel.setText(getScheduledFrequency(editDrug.getSchedule().getDayPeriod(), editDrug.getPreferences().getPreference("weekdays")));
            binding.scheduleBlock.expiryDuration.setText(getDuration());
        }
        loadRemindersTime();
    }

    private void getRemindersForNewKPHCMedByNLP() {
        if (null != editDrug && !Util.isEmptyString(editDrug.getDirections())) {
            NLPRemindersRequestObject requestObject = new NLPRemindersRequestObject();
            requestObject.setDosage(editDrug.getDose());
            requestObject.setMedicineName(editDrug.getName());
            requestObject.setStartDate(NLPUtils.getNLPFormattedDate());
            requestObject.setEndDate("Never");
            requestObject.setInstructions(editDrug.getDirections());
            requestObject.setPillId(editDrug.getGuid());
            if (AppConstants.SHOULD_SHOW_NLP_UI) {
                startLoadingActivity();
                new NLPGetRemindersService(this, requestObject, this).execute();
            } else {
                // no UI and callback in this scenario
                new NLPGetRemindersService(this, requestObject, null).execute();
                onEditMedication(0);
            }
        }
    }

    private void startLoadingActivity() {
        if (null != getActivity()) {
            getActivity().startActivityForResult(new Intent(getActivity(), LoadingActivity.class), 0);
        }
    }

    private void loadRemindersTime() {
        editDrug.getSchedule().setTimeList(new TimeList(FrontController.getInstance(_thisActivity).getSchdulesByPillId(pillId)));
        binding.scheduleBlock.reminderTimes.removeAllViews();
        if (editDrug.getSchedule().getTimeList().length() > 0) {
            binding.scheduleBlock.remindersLabel.setText(editDrug.getSchedule().getTimeList().length() == 1 ? getString(R.string.reminder) : getString(R.string.reminders));
            scheduleTime.clear();
            for (HourMinute hm : editDrug.getSchedule().getTimeList().get_doseTimes_copy()) {
                LayoutInflater inflater = MedicationDetailActivity.this.getLayoutInflater();
                childLayout = inflater.inflate(R.layout.reminder_time_layout, null);
                TextView reminderText = childLayout.findViewById(R.id.reminderTime);
                String time = HourMinute.getLocalizedString(hm, this);
                if (!DateFormat.is24HourFormat(_thisActivity)) {
                    if (("00:00").equalsIgnoreCase(time)) {
                        time = AppConstants.MID_DAY + " " + Util.getSystemAMFormat();
                    }
                }
                scheduleTime.add(time);
                scheduleData.setScheduleTime(scheduleTime);
                reminderText.setText(time);
                binding.scheduleBlock.reminderTimes.addView(childLayout);
            }
        }
    }

    public void onSetRemindersClicked() {
        FireBaseAnalyticsTracker.getInstance().logEventWithoutParams(getActivity(), FireBaseConstants.Event.MED_REMINDER_ADD);
        updateScheduleType();
//        editDrug.setScheduleGuid(Util.getRandomGuid());
//        mFrontController.updateScheduleGUID(editDrug.getScheduleGuid(), editDrug.getGuid());
        addLogEntryForEdit(editDrug, _thisActivity);
        // based on conditions invoke the below call
        if (AppConstants.shouldPerformNLP()
                && null != editDrug && editDrug.isManaged()
                && RunTimeData.getInstance().isMedDetailView()) {
            if (!RunTimeData.getInstance().getDrugsNLPRemindersList().containsKey(editDrug.getGuid())) {
                getRemindersForNewKPHCMedByNLP();
            } else {
                if (AppConstants.SHOULD_SHOW_NLP_UI) {
                    showUserReminderSuggestionAlert();
                } else {
                    onEditMedication(0);
                }
            }
        } else {
            onEditMedication(0);
        }
    }

    public void onEditRemindersClicked() {
        if (AppConstants.shouldPerformNLP()
                && null != editDrug && editDrug.isManaged()
                && RunTimeData.getInstance().isMedDetailView() && !RunTimeData.getInstance().getDrugsNLPRemindersList().containsKey(editDrug.getGuid())) {
            getRemindersForNewKPHCMedByNLP();
        }else{
            onEditMedication(1);
        }

    }

    private void loadNLPReminder(NLPReminder reminder) {
        scheduleData.setStartDate(NLPUtils.getScheduleFormattedDate(reminder.getStartDate()));
        if(!Util.isEmptyString(NLPUtils.getScheduleFormattedDate(reminder.getEndDate()))) {
            scheduleData.setEndDate(NLPUtils.getScheduleFormattedDate(reminder.getEndDate()));
        }else{
            scheduleData.setEndDate("Never");
        }
        scheduleData.setScheduleTime(NLPUtils.getScheduleFormattedReminders(reminder.getReminderTimes()));
        scheduleData.setMeditationDuration(reminder.getFrequency());
        scheduleData.setNLPReminder(true);
        if(reminder.getFrequency().equalsIgnoreCase("Daily")){
            binding.scheduleBlock.scheduleDays.setText("Everyday");
            scheduleData.setDuration(1);
        } else if(reminder.getFrequency().equalsIgnoreCase("Weekly")){
            scheduleData.setDuration(7);
        }else if(reminder.getFrequency().equalsIgnoreCase("Monthly")){
            scheduleData.setDuration(30);
        }
        onEditMedication(1);
    }

    public void onRecordDoseClicked() {
        clickCount = FrontController.getInstance(_thisActivity).getPillHistoryEventCountForToday(editDrug.getGuid());
        clickCount = clickCount + 1;
       // mFrontController.updateScheduleType(AppConstants.SCHEDULE_CHOICE_AS_NEEDED, editDrug.getGuid());
        editDrug.setScheduleDeletedFromTrash(false); //DE9367
        //Check if the daily limit is reached
        if (clickCount > editDrug.getSchedule().getDailyLimit() && editDrug.getSchedule().getDailyLimit() != 0 && editDrug.getSchedule().getDailyLimit()!=-1) {
            DialogHelpers.showAlertDialogWithHeader(_thisActivity, getString(R.string.max_reached_title), getString(R.string.max_doasage_limit_reached_msg), null);
        } else {
            finalDrugs = new ArrayList<>();
            finalDrugs.add(editDrug);
            Intent intent = new Intent(MedicationDetailActivity.this, GreatJobAlertForTakenAllActivity.class);
            intent.putExtra("LaunchMode", "AddToHistory");
            startActivityForResult(intent, ADD_HISTORY_ENTRY);
        }
    }

    public void showNumberPicker() {
        LayoutInflater inflater = this.getLayoutInflater();
        View npView = inflater.inflate(R.layout.choose_number, null);
        numberPicker = npView.findViewById(R.id.numberPicker1);
        numberPicker.setDisplayedValues(getResources().getStringArray(R.array.daily_allowance_limit));
        numberPicker.setMinValue(0);
        numberPicker.setMaxValue(10);
        numberPicker.setWrapSelectorWheel(false);
        numberPicker.setValue((int) editDrug.getSchedule().getDailyLimit());
        Util.colorNumberPickerText(numberPicker, Util.getColorWrapper(this, R.color.text_content));
        AlertDialog ab = new AlertDialog.Builder(this).setTitle(getResources().getString(R.string.set_dose_limit_title)).setView(npView)
                .setPositiveButton(getResources().getString(R.string._set_caps), (dialog, whichButton) -> {
                    newDailyLimit = numberPicker.getValue();
                    editDrug.getPreferences().setPreference("scheduleChoice",AppConstants.SCHEDULE_CHOICE_AS_NEEDED);
                    editDrug.setScheduleGuid(Util.getRandomGuid());
                    mFrontController.updateScheduleGUID(editDrug.getScheduleGuid(), pillId);
                    if (newDailyLimit == 0) {
                        binding.scheduleBlock.maxDoseNumber.setText(getString(R.string.no_dose_limit));
                        editDrug.getSchedule().setDailyLimitType(Schedule.DailyLimitType.None);
                        editDrug.getSchedule().setDailyLimit(newDailyLimit);
                        editDrug.getPreferences().setPreference("maxNumDailyDoses", String.valueOf(newDailyLimit));
                        editDrug.setSchedule(editDrug.getSchedule());
                        saveDataToDB(newDailyLimit);
                        return;
                    }
                    saveDataToDB(newDailyLimit);
                    binding.scheduleBlock.maxDoseNumber.setText(String.valueOf(newDailyLimit));
                    editDrug.getPreferences().setPreference("maxNumDailyDoses", newDailyLimit != 0 ? String.valueOf(newDailyLimit): "-1");
                    editDrug.getSchedule().setDailyLimit(newDailyLimit);
                    clickCount = 0;
                    saveDataToDB(newDailyLimit);
                    scheduleData.setAsNeededSwitch(true);
                    updateScheduleType();
                    addLogEntryForEdit(editDrug, _thisActivity);
                    dialog.dismiss();
                }).setNegativeButton(R.string.cancel_text, (dialog, whichButton) -> {
                }).create();
        if (!isFinishing()) {
            RunTimeData.getInstance().setAlertDialogInstance(ab);
            ab.show();
        }
    }

    public void archiveMedicationClicked() {
        showMedArchiveAlert(_thisActivity, editDrug);
    }

    public void dismissPopUpWindow() {
        if (null != mPopupWindow) {
            mPopupWindow.dismiss();
        }
    }

    public void showEditSchedulePopUp() {
        PopupMenu popupMenu = new PopupMenu(_thisActivity, binding.scheduleBlock.editSchedule);
        popupMenu.getMenuInflater().inflate(R.menu.edit_schedule_menu, popupMenu.getMenu());

        popupMenu.setOnDismissListener(popupMenu1 -> MedicationDetailActivity.this.onPopUpDismissed());

        popupMenu.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.editSchedule) {
                // onSetRemindersClicked();
                isEditReminderClicked = true;
                onEditRemindersClicked();
            } else if (item.getItemId() == R.id.deleteSchedule) {
                showDeleteScheduleAlert(false);
            } else if (item.getItemId() == R.id.cancel) {
                popupMenu.dismiss();
                MedicationDetailActivity.this.onPopUpDismissed();
            }
            MedicationDetailActivity.this.onPopUpDismissed();
            return true;
        });
        popupMenu.show();
        MedicationDetailActivity.this.onPopUpShown(popupMenu);
    }

    private void onEditMedication(int from) {

        if (0 == from) {
            scheduleTime.clear();
            scheduleData.setScheduleTime(scheduleTime);
            scheduleData.setDurationType("");
            scheduleData.setDuration(0);
            scheduleData.setMeditationDuration("Set Reminder");
            scheduleData.setStartDate(null);
            scheduleData.setEndDate(null);
            scheduleData.setSelectedDays(null);
        }
        if (null == scheduleData.getMeditationDuration()) {
            scheduleData.setMeditationDuration("Daily");
        }
        RunTimeData.getInstance().setScheduleData(scheduleData);
        RunTimeData.getInstance().getScheduleData().setSelectedDrug(editDrug);
        RunTimeData.getInstance().getScheduleData().setEditMedicationClicked(true);
        RunTimeData.getInstance().setLaunchSource(AppConstants.SINGLE_MEDICATION);
        Intent i = new Intent(this,ScheduleLoadingActivity.class);
        startActivity(i);

    }

    private void saveDataToDB(int newDailyLimit) {
        mFrontController.updateMaxDailyDoses(newDailyLimit, pillId);
    }

    private void updateScheduleType() {
        mFrontController.updateScheduleType(Util.getScheduleChoice(editDrug), pillId);
    }

    //show alert on click of delete schedule
    // setSwitch will be true if the user turns on the toggle switch for the med having schedules
    // else will be false in case the user deletes the schedules from popup menu.
    private void showDeleteScheduleAlert(boolean setSwitch) {
        deleteScheduleOKClicked = false;
        scheduleData.setAsNeededSwitch(setSwitch);
        AlertDialog.Builder dialogBuilder = new AlertDialog.Builder(MedicationDetailActivity.this);

        dialogBuilder.setTitle(setSwitch?getResources().getString(R.string.existing_schedule_title):getResources().getString(R.string.delete_schedule_title));
        dialogBuilder.setMessage(setSwitch ? getResources().getString(R.string.toggle_on_msg) : getResources().getString(R.string.delete_schedule_message));
        MedicationDetailActivity.this.onPopUpDismissed();
        dialogBuilder.setPositiveButton(R.string.ok_text, (dialogInterface, i) -> {
            deleteScheduleOKClicked = true;
            scheduleTime.clear();
            scheduleData.setScheduleTime(scheduleTime);
            scheduleData.setDurationType("");
            scheduleData.setDuration(0);
            scheduleData.setMeditationDuration("Daily");
            scheduleData.setStartDate(null);
            scheduleData.setEndDate(null);
            scheduleData.setSelectedDays(null);
            if (null == scheduleData.getMeditationDuration()) {
                scheduleData.setMeditationDuration("Daily");
            }
            mFrontController.removeSchedules(String.valueOf(editDrug.getGuid()));
            Util.getInstance().removePillSchedulesFromReminders(MedicationDetailActivity.this, editDrug.getGuid());
            //deleting Empty and Postpone history events if we delete the schedule of a med
            Util.getInstance().deleteEmptyAndPostponeEntries(MedicationDetailActivity.this,editDrug);
            mFrontController.updateScheduleType(Schedule.SchedType.SCHEDULED.toString(), editDrug.getGuid());
            editDrug.getSchedule().getTimeList().clearTimesList();
            editDrug.getPreferences().setPreference("maxNumDailyDoses", "-1");
            editDrug.getPreferences().setPreference("missedDosesLastChecked", "-1");
            editDrug.getPreferences().setPreference("scheduleChoice", setSwitch ? AppConstants.SCHEDULE_CHOICE_AS_NEEDED : AppConstants.SCHEDULE_CHOICE_UNDEFINED);
            updateScheduleChoice(Util.getScheduleChoice(editDrug));
            editDrug.getSchedule().setDailyLimit(0);
            editDrug.setScheduleAddedOrUpdated(true);
            binding.scheduleBlock.setIsScheduled(false);
            if(!isPopUpTextRequired) {
                isPopUpTextRequired = false;
            }
            binding.scheduleBlock.editSchedule.setVisibility(View.GONE);
            binding.scheduleBlock.setTakeAsNeededSwitchStatus(setSwitch);
            if(!isPopUpTextRequired) {
                isPopUpTextRequired = !setSwitch;
            }
            binding.scheduleBlock.takeAsNeededSwitch.setChecked(setSwitch);
            List<String> pillIdList = new ArrayList<>();
            pillIdList.add(pillId);
            FrontController.getInstance(_thisActivity).updateMissedDosesLastChecked(pillIdList, "-1");
            removePastRemindersForSelectedDrug(pillId);
            editDrug.setScheduleGuid(Util.getRandomGuid());
            mFrontController.updateScheduleGUID(editDrug.getScheduleGuid(), pillId);
            addLogEntryForEdit(editDrug, _thisActivity);
        });
        dialogBuilder.setNegativeButton(R.string.cancel_text, (dialogInterface, i) -> {
            dialogInterface.cancel();
            onScheduleToTakeAsNeededCancelled();
        });

        dialogBuilder.setCancelable(false);
        if (!isFinishing()) {
            AlertDialog alertDialog = dialogBuilder.create();
            RunTimeData.getInstance().setAlertDialogInstance(alertDialog);
            alertDialog.show();
            // setting the dismiss listener on dialog instance.
            // to revert the toggle on in case of dismissing the Alert by Medication Alert/InApp Push Notification Alert.
            alertDialog.setOnDismissListener(dialogInterface -> {
                if(!deleteScheduleOKClicked)
                    onScheduleToTakeAsNeededCancelled();
            });
        }
    }

    private void onScheduleToTakeAsNeededCancelled() {
        binding.scheduleBlock.setIsScheduled(true);
        isPopUpTextRequired = true;
        binding.scheduleBlock.editSchedule.setVisibility(View.VISIBLE);
        binding.scheduleBlock.setTakeAsNeededSwitchStatus(false);
        isPopUpTextRequired = true;
        binding.scheduleBlock.takeAsNeededSwitch.setChecked(false);
    }

    private void removePastRemindersForSelectedDrug(String drugId) {
            FrontController.getInstance(_thisActivity).removePillFromPassedReminderTable(_thisActivity, drugId);
        try {
            LinkedHashMap<String, LinkedHashMap<Long, List<Drug>>> passedRemindersHashMapByUserId = PillpopperRunTime.getInstance().getPassedReminderersHashMapByUserId();
            passedRemindersHashMapByUserId.remove(editDrug.getUserID());
            PillpopperRunTime.getInstance().setPassedReminderersHashMapByUserId(passedRemindersHashMapByUserId);
        } catch (Exception e) {
            PillpopperLog.say(e);
        }
    }

    // returns the formatted string of start date and end date eg: 22 Jan 2020 - Forever or 22 Jan 2020 - 1 Dec 2020.
    // And also shows the expired schedule text if the end date is less than today.
    public String getDuration() {
        String startDate = PillpopperDay.getDateString(getMeDate(editDrug.getSchedule().getStart().getYear(),
                editDrug.getSchedule().getStart().getMonth(), editDrug.getSchedule().getStart().getDay()), true, R.string._not_set, _thisActivity);
        String endDate;
        if (editDrug.getSchedule().getEnd() == null) {
            endDate = getString(R.string.forever);
        } else {
            endDate = PillpopperDay.getDateString(getMeDate(editDrug.getSchedule().getEnd().getYear(),
                    editDrug.getSchedule().getEnd().getMonth(), editDrug.getSchedule().getEnd().getDay()), true, R.string._not_set, _thisActivity);
        }
        scheduleData.setStartDate(startDate);
        scheduleData.setEndDate(endDate);
        binding.scheduleBlock.expiryDisclaimer.setVisibility(null != endReminderDay && endReminderDay.before(PillpopperDay.today()) ? View.VISIBLE : View.GONE);
        return startDate + " - " + endDate;
    }

    private PillpopperDay getMeDate(int year, int month, int day) {
        if (year < 0 || month < 0 || day < 0) {
            return null;
        } else {
            return new PillpopperDay(year, month, day);
        }
    }

    private String getScheduledFrequency(long dayPeriod, String weekdays) {
        String selectedDayPeriod = "";
        if (editDrug.getScheduledFrequency() == null) {
            if (dayPeriod == 30) {
                String date = "";
                try {
                    date = editDrug.getSchedule().getStart().getDay()+"" + Util.getSuffix(editDrug.getSchedule().getStart().getDay());
                }catch (Exception ne){
                    LoggerUtils.info(ne.getMessage());
                }
                selectedDayPeriod = getResources().getString(R.string._monthly);
                binding.scheduleBlock.scheduleDays.setText(date);
            } else if (dayPeriod % 7 == 0) {
                if (dayPeriod / 7 != 1) {
                    if (weekdays != null
                            && weekdays.length() > 0) {
                        selectedDayPeriod = getResources().getString(R.string.every) + " " + (dayPeriod / 7) + " " + getResources().getString(R.string.weeks_on);
                    } else {
                        scheduleData.setDurationType("Weekly");
                        selectedDayPeriod = getResources().getString(R.string.every) + " " + (dayPeriod / 7) + " " + getResources().getString(R.string.weeks_on) + getResources().getString(R.string.on);
                        binding.scheduleBlock.scheduleDays.setText(Util.setOnWeekdays(this, String.valueOf(editDrug.getSchedule().getStart().getDayOfWeek().getDayNumber())));
                    }
                } else {
                    selectedDayPeriod = getResources().getString(R.string.weekly_on) /*+ " " + setOnWeekdays(weekdays)*/;
                    if(null != editDrug.getPreferences().getPreference("weekdays")) {
                        scheduleData.setSelectedDays(editDrug.getPreferences().getPreference("weekdays").replace(",", ""));
                        binding.scheduleBlock.scheduleDays.setText(Util.setOnWeekdays(this, editDrug.getPreferences().getPreference("weekdays")));
                    }
                }
            } else {
                if (dayPeriod == 1) {
                    selectedDayPeriod = getResources().getString(R.string.txt_remindter_set_time_daily);
                    binding.scheduleBlock.scheduleDays.setText("Everyday");
                } else {
                    selectedDayPeriod = getResources().getString(R.string.every) + " " + dayPeriod + " " + getResources().getString(R.string.days);
                    binding.scheduleBlock.scheduleDays.setText((""));
                }
            }
        } else {
            if (("D").equalsIgnoreCase(editDrug.getScheduledFrequency())) {
                if (dayPeriod == 1) {
                    selectedDayPeriod = getResources().getString(R.string.txt_remindter_set_time_daily);
                    binding.scheduleBlock.scheduleDays.setText("Everyday");
                } else {
                    selectedDayPeriod = getResources().getString(R.string.every) + " " + dayPeriod + " " + getResources().getString(R.string.days);
                    binding.scheduleBlock.scheduleDays.setText((""));
                }
            } else if (("W").equalsIgnoreCase(editDrug.getScheduledFrequency())) {
                if (dayPeriod / 7 != 1) {
                    selectedDayPeriod = getResources().getString(R.string.every) + " " + (dayPeriod / 7) + " " + getResources().getString(R.string.weeks_on) + getResources().getString(R.string.on);
                    binding.scheduleBlock.scheduleDays.setText(Util.setOnWeekdays(this, String.valueOf(editDrug.getSchedule().getStart().getDayOfWeek().getDayNumber())));
                } else {
                    selectedDayPeriod = getResources().getString(R.string.weekly_on);
                    if(null != editDrug.getPreferences().getPreference("weekdays")) {
                        scheduleData.setSelectedDays(editDrug.getPreferences().getPreference("weekdays").replace(",", ""));
                        binding.scheduleBlock.scheduleDays.setText(Util.setOnWeekdays(this, editDrug.getPreferences().getPreference("weekdays")));
                    }
                }
            } else if (("M").equalsIgnoreCase(editDrug.getScheduledFrequency())) {
                String date = "";
                try {
                     date = editDrug.getSchedule().getStart().getDay()+"" + Util.getSuffix(editDrug.getSchedule().getStart().getDay());
                }catch (NumberFormatException ne){
                    LoggerUtils.info(ne.getMessage());
                }
                selectedDayPeriod = getResources().getString(R.string._monthly);
                binding.scheduleBlock.scheduleDays.setText(date);
            }
        }
        return selectedDayPeriod;
    }

    @Override
    public void onResume() {

        super.onResume();

        if(null != binding.medicationBlock.drugDetailDoseImage) {
            ImageUILoaderManager.getInstance().loadDrugImage(getActivity(), editDrug.getImageGuid(), editDrug.getGuid(), binding.medicationBlock.drugDetailDoseImage, Util.getDrawableWrapper(getActivity(), R.drawable.rx_image_generic));
        }
        if(null != binding.getRoot()) {
            setValues();
        }
        if(null != RunTimeData.getInstance().getScheduleData() && RunTimeData.getInstance().getScheduleData().isEditMedicationClicked() &&
                !RunTimeData.getInstance().getScheduleData().getMeditationDuration().equalsIgnoreCase("Set Reminder") &&
                RunTimeData.getInstance().getScheduleData().isMedSavedClicked()) {
            RunTimeData.getInstance().getScheduleData().setMedSavedClicked(false);
            //onBackPressed();
            return;
        }
        RunTimeConstants.getInstance().setNotificationSuppressor(false);
        getState().registerStateUpdatedListener(this);
        ImageUILoaderManager.getInstance().loadDrugImage(getActivity(), editDrug.getImageGuid(), editDrug.getGuid(), binding.medicationBlock.drugDetailDoseImage, Util.getDrawableWrapper(getActivity(), R.drawable.rx_image_generic));
        if (binding.medicationBlock.instructionsOrNotesFullText.getVisibility() == View.VISIBLE) {
            binding.medicationBlock.collapseIcon.setVisibility(View.VISIBLE);
            binding.medicationBlock.expandIcon.setVisibility(View.GONE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case ADD_HISTORY_ENTRY:
                    mFrontController.performTakeDrug(finalDrugs, PillpopperTime.now(), _thisActivity, true, FireBaseConstants.ParamValue.RECORD_DOSE_BUTTON);
                    break;
                case REQ_EDIT_SCHEDULE:
                case SAVE_NOTES:
                case SAVE_OTC_MEDICATION:
                    loadDrugDetails();
                default:
                    break;
            }
        }
    }


    // Show the pop up window on click of
    public void showAsNeededInfo() {
        if(!isPopUpTextRequired && !RunTimeData.getInstance().isFromArchive()) {
            binding.scheduleBlock.tvPopupDisplay.setVisibility(View.VISIBLE);
            scroll();
        }
            LayoutInflater inflater = (LayoutInflater) this.getSystemService(LAYOUT_INFLATER_SERVICE);
            // Inflate the custom layout/view
            View customView = inflater.inflate(R.layout.as_needed_info_layout, null);
            final Handler handler = new Handler(Looper.getMainLooper());
            handler.postDelayed(new Runnable() {
                @Override
                public void run() {

                    // Initialize a new instance of popup window
                    mPopupWindow = new PopupWindow(
                            customView,
                            RelativeLayout.LayoutParams.WRAP_CONTENT,
                            RelativeLayout.LayoutParams.WRAP_CONTENT
                    );
                    ((TextView) customView.findViewById(R.id.header_txt)).setTypeface(mFontMedium);
                    ((TextView) customView.findViewById(R.id.detail_txt)).setTypeface(mFontRegular);

                    mPopupWindow.setFocusable(true);
                    mPopupWindow.showAsDropDown(binding.scheduleBlock.infoImage, -rlDetailWidth / 2 + rlInfoImageWidth / 2, 0, Gravity.BOTTOM);
                    //binding.pageContainer.smoothScrollTo(binding.pageContainer.getScrollY(),binding.pageContainer.getBottom());

                    mPopupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {

                        @Override
                        public void onDismiss() {
                            binding.scheduleBlock.tvPopupDisplay.setVisibility(View.GONE);
                        }
                    });
                }
            }, 200);


    }

    public void scroll(){
        binding.pageContainer.post(new Runnable() {
            @Override
            public void run() {
               // binding.pageContainer.fullScroll(View.FOCUS_DOWN);
                View lastChild = binding.pageContainer.getChildAt(binding.pageContainer.getChildCount() - 1);
                int bottom = lastChild.getBottom() + binding.pageContainer.getPaddingBottom();
                int sy = binding.pageContainer.getScrollY();
                int sh = binding.pageContainer.getHeight();
                int delta = bottom - (sy + sh);

                binding.pageContainer.smoothScrollBy(0, delta);
            }
        });
    }
    protected void showMedArchiveAlert(PillpopperActivity act, final Drug drug) {
        AlertDialog.Builder alertDialog = new AlertDialog.Builder(act);
        alertDialog.setTitle(getString(R.string._archive_med));
        alertDialog.setMessage(getString(R.string._archive_med_description));
        alertDialog.setCancelable(false);
        alertDialog.setPositiveButton(
                Html.fromHtml("<b>" + act.getAndroidContext().getResources().getString(R.string.ok_text) + "</b>"),
                (dialog, which) -> {
                    FireBaseAnalyticsTracker.getInstance().logEvent(MedicationDetailActivity.this,
                            FireBaseConstants.Event.MED_ARCHIVE,
                            FireBaseConstants.ParamName.SOURCE,
                            FireBaseConstants.ParamValue.MED_DETAILS);
                    drug.setArchived(true);
                    FrontController.getInstance(_thisActivity).markDrugAsArchive(pillId);
                    //later "setScheduleAddedOrUpdated(true)" might be removed, as we are considering
                    // "medicationScheduleChanged" for archive also(instead of "medArchivedOrRemoved")
                    if (editDrug.getSchedule().getTimeList().length() > 0) {
                        editDrug.setScheduleAddedOrUpdated(true);
                    }
                    // deleting empty and postpone history events if we are archiving a med
                    Util.getInstance().deleteEmptyAndPostponeEntries(MedicationDetailActivity.this,editDrug);

                    String dosageType = FrontController.getInstance(_thisActivity).getDosageTypeByPillID(pillId);
                    editDrug.getPreferences().setPreference(DatabaseConstants.DOSAGE_TYPE, !Util.isEmptyString(dosageType) ? dosageType : PillpopperConstants.DOSAGE_TYPE_CUSTOM);

                    addLogEntryForEdit(editDrug, _thisActivity);
                    _thisActivity.finish();
                    onBackPressed();
                });
        alertDialog.setNegativeButton(act.getAndroidContext().getResources().getString(R.string.cancel_text),
                (dialog1, which) -> dialog1.dismiss());
        AlertDialog dialog = alertDialog.create();
        if (!isFinishing()) {
            RunTimeData.getInstance().setAlertDialogInstance(dialog);
            dialog.show();
        }
    }

    private void addLogEntryForEdit(Drug drug, PillpopperActivity pillpopperActivity) {
        try {
            LogEntryUpdateAsyncTask logEntryUpdateAsyncTask = new LogEntryUpdateAsyncTask(pillpopperActivity, "EditPill", drug);
            logEntryUpdateAsyncTask.execute();
        } catch (Exception e) {
            PillpopperLog.say("Exception while adding log entry for edit ", e);
        }
    }

    @Override
    public void onPopUpShown(PopupMenu popupMenu) {
        schedulePopUpMenu = popupMenu;
    }

    @Override
    public void onPopUpDismissed() {
        schedulePopUpMenu = null;
    }

    @Override
    public void onClick(View view) {
        if (view == binding.medicationBlock.editBtn) {
            Intent intent = new Intent(MedicationDetailActivity.this, AddOrEditMedicationActivity.class);
            intent.putExtra(PillpopperConstants.LAUNCH_MODE, PillpopperConstants.ACTION_EDIT_PILL);
            if(PillpopperConstants.LAUNCH_SOURCE_SCHEDULE.equalsIgnoreCase(mLaunchSource)){
                intent.putExtra(PillpopperConstants.LAUNCH_SOURCE,PillpopperConstants.LAUNCH_SOURCE_SCHEDULE);
            }
            intent.putExtra("ToSaveOTCMedication", pillId);
            editDrug = null;
            startActivityForResult(intent, SAVE_OTC_MEDICATION);
        } else if (view == binding.medicationBlock.drugDetailDoseImage) {
            ViewClickHandler.preventMultiClick(binding.medicationBlock.drugDetailDoseImage);
            EnlargeImageActivity.expandPillImage(_thisActivity, editDrug.getGuid(), editDrug.getImageGuid());
        } else if (view == binding.medicationBlock.expandIcon) {
            binding.medicationBlock.expandIcon.setVisibility(View.GONE);
            binding.medicationBlock.collapseIcon.setVisibility(View.VISIBLE);
            expand(binding.medicationBlock.instructionsOrNotesText);
        } else if (view == binding.medicationBlock.collapseIcon) {
            binding.medicationBlock.expandIcon.setVisibility(View.VISIBLE);
            binding.medicationBlock.collapseIcon.setVisibility(View.GONE);
            collapse(binding.medicationBlock.instructionsOrNotesText);
        } else if (view == binding.personalNotesBlock.addOrEditNotes) {
            Intent intent = new Intent(MedicationDetailActivity.this, EditNotesActivity.class);
            intent.putExtra("ToEditNotes", pillId);
            editDrug = null;
            startActivityForResult(intent, SAVE_NOTES);
        } else if (view == binding.restoreDeletBlock.archiveDetailsRestoreMedicationButton) {
            //RunTimeData.getInstance().setRestored(true);
            isPopUpTextRequired = true;
            restoreMed();
            binding.scheduleBlock.placeholderPopup.rlDetail.getViewTreeObserver().addOnGlobalLayoutListener(() -> {
                binding.scheduleBlock.placeholderPopup.rlDetail.setVisibility(View.VISIBLE);
                rlDetailWidth = binding.scheduleBlock.placeholderPopup.rlDetail.getMeasuredWidth();
                rlInfoImageWidth = binding.scheduleBlock.infoImage.getMeasuredWidth();

            });

        } else if (view == binding.restoreDeletBlock.archiveDetailsDeleteMedicationButton) {
            deleteMed();
        } else if (view == binding.scheduleBlock.rlMaxDose) {
            if(RunTimeData.getInstance().isMedDetailView()) {
                showNumberPicker();
            }
        }
    }

    private void deleteMed() {
        AlertDialog.Builder deleteMedicationAlertBuilder = new AlertDialog.Builder(mPillPopperActivity, R.style.ArchiveDetailAlertDialog);

        View deleteMedicationDialogLayout = mPillPopperActivity.getInflater().inflate(R.layout.archive_detail_delete_medication_dialog_layout,
                new LinearLayout(mPillPopperActivity), false);
        deleteMedicationAlertBuilder.setView(deleteMedicationDialogLayout);
        deleteMedicationAlertBuilder.setPositiveButton(getResources().getString(R.string._delete), (dialogInterface, i) -> {
            FireBaseAnalyticsTracker.getInstance().logEventWithoutParams(MedicationDetailActivity.this, FireBaseConstants.Event.MED_ARCHIVE_DELETE);
            new DeleteMedicationTask().execute(mPillPopperActivity, pillId);
            dialogInterface.dismiss();
            mPillPopperActivity.finish();
        });

        deleteMedicationAlertBuilder.setNegativeButton(getResources().getString(R.string.cancel_text), (dialogInterface, i) -> dialogInterface.dismiss());
        deleteMedicationAlertBuilder.setCancelable(false);

        AlertDialog deleteMedicationAlert = deleteMedicationAlertBuilder.create();
        if (!isFinishing()) {
            RunTimeData.getInstance().setAlertDialogInstance(deleteMedicationAlert);
            deleteMedicationAlert.show();
        }
    }

    private void restoreMed() {
        new RestoreMedicationTask().execute(mPillPopperActivity, pillId);

        FireBaseAnalyticsTracker.getInstance().logEventWithoutParams(MedicationDetailActivity.this, FireBaseConstants.Event.MED_ARCHIVE_RESTORE);
        isMedRestored = true;
        mLaunchSource = PillpopperConstants.LAUNCH_SOURCE_SCHEDULE;
        AlertDialog.Builder restoreMedicationAlertDialogBuilder = new AlertDialog.Builder(mPillPopperActivity, R.style.ArchiveDetailAlertDialog);
        View restoreMedicationDialogLayout = mPillPopperActivity.getLayoutInflater().inflate(R.layout.archive_detail_restore_medication_dialog_layout,
                new LinearLayout(mPillPopperActivity), false);
        restoreMedicationAlertDialogBuilder.setView(restoreMedicationDialogLayout);
        restoreMedicationAlertDialogBuilder.setPositiveButton(getResources().getString(R.string._ok), (dialogInterface, i) -> dialogInterface.dismiss());

        // this is written, because in case of inApp Reminder alert,
        // we are suppose to dismiss this restored med Alert.
        // and dismissing this alert without navigating to details,
        // would allow duplicate restore action from the user.
        restoreMedicationAlertDialogBuilder.setOnDismissListener(dialogInterface -> {
            RunTimeData.getInstance().setMedDetailView(true);
            RunTimeData.getInstance().setFromArchive(false);
            dialogInterface.dismiss();
            setValues();
            setListeners();
            loadDrugDetails();
            initDetailViewUI();
        });

        restoreMedicationAlertDialogBuilder.setCancelable(false);
        restoreMedicationAlert = restoreMedicationAlertDialogBuilder.create();
        if (!isFinishing()) {
            RunTimeData.getInstance().setAlertDialogInstance(restoreMedicationAlert);
            restoreMedicationAlert.show();
        }
    }

    private void expand(final View v) {
        int matchParentMeasureSpec = View.MeasureSpec.makeMeasureSpec(((View) v.getParent()).getWidth(), View.MeasureSpec.EXACTLY);
        int wrapContentMeasureSpec = View.MeasureSpec.makeMeasureSpec(0, View.MeasureSpec.UNSPECIFIED);
        v.measure(matchParentMeasureSpec, wrapContentMeasureSpec);
        final int targetHeight = v.getMeasuredHeight();
        v.getLayoutParams().height = 1;
        v.setVisibility(View.VISIBLE);
        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                v.getLayoutParams().height = interpolatedTime == 1
                        ? ViewGroup.LayoutParams.WRAP_CONTENT
                        : (int) (targetHeight * interpolatedTime);
                v.requestLayout();
            }
            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                binding.medicationBlock.instructionsOrNotesText.setVisibility(View.GONE);
                binding.medicationBlock.instructionsOrNotesFullText.setVisibility(View.VISIBLE);
            }
            @Override
            public void onAnimationEnd(Animation animation) {
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        animation.setDuration(((int) (targetHeight / v.getContext().getResources().getDisplayMetrics().density)) * 8);
        v.startAnimation(animation);
    }

    private void collapse(final View v) {
        final int initialHeight = v.getMeasuredHeight();
        Animation animation = new Animation() {
            @Override
            protected void applyTransformation(float interpolatedTime, Transformation t) {
                if (interpolatedTime == 1) {
                    v.getLayoutParams().height = interpolatedTime == 1
                            ? ViewGroup.LayoutParams.WRAP_CONTENT
                            : (int) (initialHeight * interpolatedTime);
                } else {
                    v.getLayoutParams().height = initialHeight - (int) (initialHeight * interpolatedTime);
                }
                v.requestLayout();
            }

            @Override
            public boolean willChangeBounds() {
                return true;
            }
        };

        animation.setAnimationListener(new Animation.AnimationListener() {
            @Override
            public void onAnimationStart(Animation animation) {
                binding.medicationBlock.instructionsOrNotesText.setVisibility(View.VISIBLE);
                binding.medicationBlock.instructionsOrNotesFullText.setVisibility(View.GONE);
            }
            @Override
            public void onAnimationEnd(Animation animation) {
            }
            @Override
            public void onAnimationRepeat(Animation animation) {
            }
        });
        animation.setDuration(((int) (initialHeight / v.getContext().getResources().getDisplayMetrics().density)) * 8);
        v.startAnimation(animation);
    }

    @Override
    protected void onStop() {
        getState().unregisterStateUpdatedListener(this);
        super.onStop();
        if (null != editDrug) {
            editDrug = null;
        }
    }

    @Override
    public void onPause() {
        if (isMedRestored && null != restoreMedicationAlert && restoreMedicationAlert.isShowing()) {
            restoreMedicationAlert.dismiss();
            RunTimeData.getInstance().setMedDetailView(true);
            RunTimeData.getInstance().setFromArchive(false);
            setValues();
            setListeners();
            loadDrugDetails();
            initDetailViewUI();
        }
        super.onPause();
    }

    @Override
    public void onBackPressed() {
        if (!TextUtils.isEmpty(mLaunchSource)) {
            PillpopperConstants.setCanShowMedicationList(PillpopperConstants.LAUNCH_SOURCE_SCHEDULE.equalsIgnoreCase(mLaunchSource) || isMedRestored);
            // for dynamic font
            if (PillpopperConstants.isCanShowMedicationList()) {
                RunTimeData.getInstance().setLastSelectedFragmentPosition(HomeContainerActivity.NavigationHome.MEDICATIONS.getPosition());
            }
        }
        super.onBackPressed();
    }

    private void ackNewUpdateKPHCMed() {
        switch (editDrug.getPendingManagedChange()) {
            case Add:
            case Change:
                editDrug.ackPendingChanges();
                //to acknowledge new/update KP HC med
                String pillID = editDrug.getGuid();
                String lastManagedIdNotified = editDrug.getPreferences().getPreference("lastManagedIdNotified");
                mFrontController.updateNewKPHCMed(pillID, lastManagedIdNotified);
                addLogEntryForEdit(editDrug, _thisActivity);
                break;
            default:
                break;
        }
    }

    @Override
    public void onAddMedicationClicked(Bundle data) {
        Intent intent = new Intent(this, AddMedicationsForScheduleActivity.class);
        intent.putExtras(data);
        getActivity().startActivity(intent);
    }

    @Override
    public void onCustomBackPress() {
        binding.appBar.setVisibility(View.VISIBLE);
        initActionBar();
    }

    @Override
    public void onNLPRemindersResponseReceived(NLPReminder responseObject) {
        dismissLoadingActivity();
        if(!isEditReminderClicked) {
            if (null != responseObject) {
                showUserReminderSuggestionAlert();
            } else {
                onEditMedication(0);
            }
        }else{
            onEditMedication(1);
        }
    }

    private void showUserReminderSuggestionAlert() {
        if (RunTimeData.getInstance().getDrugsNLPRemindersList().containsKey(editDrug.getGuid())) {
            NLPReminder reminder = RunTimeData.getInstance().getDrugsNLPRemindersList().get(editDrug.getGuid());
            if(null != reminder && isValidNLPReminder(reminder) && reminder.getFrequency().equalsIgnoreCase("Daily")){
                loadNLPReminder(reminder);
            }else{
                onEditMedication(0);
            }
        } else {
            onEditMedication(0);
        }
    }

    private boolean isValidNLPReminder(NLPReminder reminder) {
        return !Util.isEmptyString(reminder.getEndDate()) && !Util.isEmptyString(reminder.getStartDate()) && null != reminder.getReminderTimes() && !reminder.getReminderTimes().isEmpty() && !Util.isEmptyString(reminder.getFrequency())
                && !Util.isEmptyString(NLPUtils.getScheduleFormattedDate(reminder.getEndDate())) && !Util.isEmptyString(NLPUtils.getScheduleFormattedDate(reminder.getStartDate()))
                && !NLPUtils.getScheduleFormattedReminders(reminder.getReminderTimes()).isEmpty();
    }

    private void dismissLoadingActivity() {
        if (null != getActivity()) {
            getActivity().finishActivity(0);
        }
    }

    public class RestoreMedicationTask
            extends AsyncTask<Object, Void, Void> {

        @Override
        public Void doInBackground(Object... params) {
            PillpopperActivity pillpopperActivity = (PillpopperActivity) params[0];
            String pillId = (String) params[1];
            mFrontController.removeDrugFromArchive(editDrug,pillpopperActivity,pillId);

            Drug drugForEditDrugLog = mFrontController.getDrugByPillId(pillId);
            if (drugForEditDrugLog.getSchedule() != null) {
                drugForEditDrugLog.setScheduleAddedOrUpdated(true);
            }
            mFrontController.addLogEntry(pillpopperActivity, Util.prepareLogEntryForAction("EditPill", drugForEditDrugLog, pillpopperActivity));
            return null;
        }
    }

    public class DeleteMedicationTask
            extends AsyncTask<Object, Void, Void> {

        @Override
        public Void doInBackground(Object... params) {
            PillpopperActivity pillpopperActivity = (PillpopperActivity) params[0];
            String pillId = (String) params[1];
            mFrontController.markDrugAsDeleted(pillId);
            mFrontController.deleteHistoryEntriesByPillID(pillId);
            Drug drugForEditDrugLog = mFrontController.getDrugByPillId(pillId);
            mFrontController.addLogEntry(pillpopperActivity, Util.prepareLogEntryForDelete(drugForEditDrugLog, pillpopperActivity));
            return null;
        }
    }

    @Override
    protected void onDestroy() {
        if(isMedRestored)
            PillpopperConstants.setCanShowMedicationList(true);
        if(null != RunTimeData.getInstance().getScheduleData()) {
            RunTimeData.getInstance().getScheduleData().setEditMedicationClicked(false);
        }
        super.onDestroy();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        if (!isFromHistoryModule) {
            onBackPressed();
        } else {
            finish();
        }
    }
}
