package com.montunosoftware.pillpopper.android;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Point;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewParent;
import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.animation.AnimationSet;
import android.view.animation.AnimationUtils;
import android.view.animation.Transformation;
import android.widget.AdapterView;
import android.widget.Button;

import androidx.annotation.IntDef;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.core.widget.NestedScrollView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.MutableLiveData;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.mymeds.databinding.ScheduleWizardBinding;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.util.ComposableComparator;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.android.util.ViewClickHandler;
import com.montunosoftware.pillpopper.android.view.CustomScheduleWizardFragment;
import com.montunosoftware.pillpopper.android.view.DialogHelpers;
import com.montunosoftware.pillpopper.android.view.ProxySpinnerAdapter;
import com.montunosoftware.pillpopper.android.view.ScheduleViewModel;
import com.montunosoftware.pillpopper.android.view.WeeklyScheduleWizardFragment;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.service.images.loader.ImageUILoaderManager;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.activity.EnlargeImageActivity;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.model.User;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.kp.tpmg.mykpmeds.activation.AppConstants.BROADCAST_REFRESH_FOR_MED_IMAGES;
import static org.kp.tpmg.mykpmeds.activation.AppConstants.BUNDLE_EXTRA_DRUG_TO_REFRESH;

public class ScheduleWizardFragment extends Fragment {
    public static final int NONE = -1;
    public static final int DAILY = 1;
    public static final int WEEKLY = 2;
    public static final int CUSTOM = 3;
    public static final int MONTHLY = 4;
    private static final long ANIMATION_DURATION = 500;
    private ComposableComparator<Drug> drugNameComparator = new ComposableComparator<Drug>().by(new Drug.AlphabeticalByNameComparator());
    private Context mContext;
    private String selectedUserId;
    private List<User> mProxyDropDownList;
    private ScheduleWizardBinding binding;
    private ScheduleViewModel scheduleViewmodel;
    private OnAddMedicationClicked addMedicationClickListener;
    private MutableLiveData<Integer> frequencySelector;
    private MutableLiveData<List<Drug>> mMedicationsList;
    private boolean mIsAnimAdded;
    private boolean mIsDailyFragAnimAdded;
    private boolean mIsFragAnimAdded;
    private ScheduleWizardMedicationListAdapter adapter = new ScheduleWizardMedicationListAdapter();
    private int mLastSpinnerPosition;
    private FrontController mFrontController;

    @ScheduleType
    private int mSelectedScheduleType = NONE;
    private boolean isCanceledClicked;
    private boolean isOnStop;
    private int scheduleSelection = -1;
    private List<Drug> drugList;
    private String selectedScheduleType = "";
    private Integer lastSelectedIndex = -1;
    private FragmentManager fragmentManager;
    private ReminderTimeFragment reminderTimeFragment;
    private SaveReminderTimeFragmentInterface mSaveReminderTimeFragmentInterface;
    private Typeface mFontMedium;
    private Typeface mFontRegular;
    private static final String TAG_FRAGMENT = "TAG_FRAGMENT";



    @ScheduleType
    public int getSelectedScheduleType() {
        return mSelectedScheduleType;
    }

    public void setSelectedScheduleType(int selectedScheduleTypeelectedType) {
        mSelectedScheduleType = selectedScheduleTypeelectedType;
    }

    @Override
    public void onStop() {
        super.onStop();
        isOnStop = true;
    }

    @Override
    public void onDestroy() {

        //Objects.requireNonNull(((AppCompatActivity) Objects.requireNonNull(getActivity())).getSupportActionBar()).show();
        scheduleViewmodel.getWeeklySelectedDays().postValue(new ArrayList<>());
        scheduleViewmodel.getCustomFrequencyNumber().postValue("");
        super.onDestroy();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        this.mContext = context;
        fragmentManager = getActivity().getSupportFragmentManager();
        if(getActivity() instanceof SaveReminderTimeFragmentInterface) {
            mSaveReminderTimeFragmentInterface = (SaveReminderTimeFragmentInterface) getActivity();
        }

    }

    @Override
    public void onResume() {
        super.onResume();
        setObserver();

        RunTimeData.getInstance().setUserSelected(false);

        isOnStop = false;
        if (-1 != scheduleSelection) {
            onScheduleMedication(scheduleSelection);
        }
        binding.svMain.smoothScrollTo(0,0);
        binding.spinner.setClickable(AppConstants.BULK_MEDICATION.equalsIgnoreCase(RunTimeData.getInstance().getLaunchSource()) && isProxyAvailable() );
        binding.userSpinnerCard.setClickable(AppConstants.BULK_MEDICATION.equalsIgnoreCase(RunTimeData.getInstance().getLaunchSource()) && isProxyAvailable());
        binding.spinnerArrow.setVisibility(AppConstants.BULK_MEDICATION.equalsIgnoreCase(RunTimeData.getInstance().getLaunchSource()) && isProxyAvailable() ? View.VISIBLE : View.GONE);
        //refresh the med image
        if (null != RunTimeData.getInstance().getScheduleData() && null != RunTimeData.getInstance().getScheduleData().getSelectedDrug()) {
            if (!RunTimeData.getInstance().getIsImageDeleted()) {
                Drug drug = RunTimeData.getInstance().getScheduleData().getSelectedDrug();
                ImageUILoaderManager.getInstance().loadDrugImage(mContext, drug.getImageGuid(), drug.getGuid(), binding.singleMedDetailsLayout.drugDetailImage, Util.getDrawableWrapper(mContext, R.drawable.pill_default));
            } else {
                binding.singleMedDetailsLayout.drugDetailImage.setDefaultImage(R.drawable.rx_image_generic);
                RunTimeData.getInstance().getScheduleData().getSelectedDrug().setImageGuid(null);
                RunTimeData.getInstance().setIsImageDeleted(false);
                ImageUILoaderManager.getInstance().loadDrugImage(getActivity(), null, null, binding.singleMedDetailsLayout.drugDetailImage, Util.getDrawableWrapper(mContext, R.drawable.pill_default));
            }
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mFrontController = FrontController.getInstance(mContext);
        mProxyDropDownList = mFrontController.getAllEnabledUsers();
        clearScheduleViewModel();
        if(null != getActivity()) {
            scheduleViewmodel = RunTimeData.getInstance().getScheduleViewModel(getActivity());
        }
        frequencySelector = scheduleViewmodel.getFrequencySelector();
        mMedicationsList = scheduleViewmodel.getMedicationsList();
        binding = DataBindingUtil.inflate(inflater, R.layout.schedule_wizard, container, false);
        mFontMedium = ActivationUtil.setFontStyle(mContext,AppConstants.FONT_ROBOTO_MEDIUM);
        mFontRegular = ActivationUtil.setFontStyle(mContext,AppConstants.FONT_ROBOTO_REGULAR);
        triggerFireBase();
        setValue();
        initEditMode();
        initUiReferences();
        loadSpinnerData();
        setObserver();
        checkForEmptyStateVisibility();
        initBroadCastReceivers();
        return binding.getRoot();
    }

    private void setValue() {
        binding.setMedicationCount(mFrontController.getDrugsListByUserId(selectedUserId).size());
        binding.setRobotoMedium(mFontMedium);
        binding.setRobotoRegular(mFontRegular);
        binding.setScheduleWizard(this);
        binding.setAdapter(adapter);
    }

    private void initEditMode() {
        if(isSchedulingFromMedicationDetailScreen()) {
            // ((DrawerLocker) getActivity()).setDrawerEnabled(false);
            selectedScheduleType = RunTimeData.getInstance().getScheduleData().getMeditationDuration();
            initToolbar();
            binding.spinnerMemberName.setText(RunTimeData.getInstance().getScheduleData().getSelectedDrug().getMemberFirstName());
            binding.editBtn.setVisibility(View.GONE);
            binding.setMedicationCount(0);
            showSingleMedDetailsLayout();
            ArrayList<Drug> mSelectedDrugs = new ArrayList<>();
            mSelectedDrugs.add(RunTimeData.getInstance().getScheduleData().getSelectedDrug());
            ScheduleViewModel scheduleViewmodel = RunTimeData.getInstance().getScheduleViewModel(getActivity());
            scheduleViewmodel.getMedicationsList().postValue(mSelectedDrugs);
            selectedUserId = RunTimeData.getInstance().getScheduleData().getSelectedDrug().getUserID();
            if(RunTimeData.getInstance().getScheduleData().getMeditationDuration().equalsIgnoreCase("Daily")) {
                onScheduleMedication(1);
            } else if(RunTimeData.getInstance().getScheduleData().getMeditationDuration().contains("Weekly")) {
                onScheduleMedication(2);
            } else if(RunTimeData.getInstance().getScheduleData().getMeditationDuration().contains("Monthly")){
                onScheduleMedication(4);
                scheduleViewmodel.getStartDateSelector().postValue(mSelectedDrugs.get(0).getSchedule().getStart().getDay());
            } else if(!RunTimeData.getInstance().getScheduleData().getMeditationDuration().contains("Set Reminder")){
                onScheduleMedication(3);
            }

        }else {
            binding.toolbar.setVisibility(View.GONE);
        }
    }

    private boolean isSchedulingFromMedicationDetailScreen() {
        return null != RunTimeData.getInstance().getScheduleData() && RunTimeData.getInstance().getScheduleData().isEditMedicationClicked();
    }

    private void showSingleMedDetailsLayout() {
        if (null != RunTimeData.getInstance().getScheduleData().getSelectedDrug()) {
            Drug drug = RunTimeData.getInstance().getScheduleData().getSelectedDrug();
            binding.medicationListView.setVisibility(View.GONE);
            binding.singleMedDetailsLayout.medicationDetailsLayout.setVisibility(View.VISIBLE);
            binding.singleMedDetailsLayout.setRobotoMedium(mFontMedium);
            binding.singleMedDetailsLayout.setRobotoRegular(mFontRegular);
            binding.singleMedDetailsLayout.setScheduleWizardFragment(this);
            binding.singleMedDetailsLayout.setMedication(drug);
            ImageUILoaderManager.getInstance().loadDrugImage(mContext, drug.getImageGuid(), drug.getGuid(), binding.singleMedDetailsLayout.drugDetailImage, Util.getDrawableWrapper(mContext, R.drawable.pill_default));
            setExpandAndCollapseIcons(drug);
            if(isaNLPReminder()){
                RunTimeData.getInstance().setScheduleEdited(true);
            }

        }
    }

    private void setExpandAndCollapseIcons(Drug drug) {
        if (drug.isManaged()) {
            if (!Util.isEmptyString(drug.getDirections())) {
                if (RunTimeData.getInstance().getScheduleData().getMeditationDuration().contains("Set Reminder") || RunTimeData.getInstance().getScheduleData().isNLPReminder()) {
                    showExpandedText();
                } else {
                    showContractedText();
                }
            }
        } else {
            if (!Util.isEmptyString(drug.getNotes())) {
                if (RunTimeData.getInstance().getScheduleData().getMeditationDuration().contains("Set Reminder")) {
                    showExpandedText();
                } else {
                    showContractedText();
                }
            }

        }
        binding.singleMedDetailsLayout.expandIcon.setOnClickListener(view -> {
            binding.singleMedDetailsLayout.expandIcon.setVisibility(View.GONE);
            binding.singleMedDetailsLayout.collapseIcon.setVisibility(View.VISIBLE);
            expand(binding.singleMedDetailsLayout.instructionsOrNotesText);
        });
        binding.singleMedDetailsLayout.collapseIcon.setOnClickListener(view -> {
            binding.singleMedDetailsLayout.expandIcon.setVisibility(View.VISIBLE);
            binding.singleMedDetailsLayout.collapseIcon.setVisibility(View.GONE);
            collapse(binding.singleMedDetailsLayout.instructionsOrNotesText);
        });
    }

    private void showExpandedText() {
        binding.singleMedDetailsLayout.collapseIcon.setVisibility(View.VISIBLE);
        binding.singleMedDetailsLayout.expandIcon.setVisibility(View.GONE);
        binding.singleMedDetailsLayout.instructionsOrNotesText.setVisibility(View.GONE);
        binding.singleMedDetailsLayout.instructionsOrNotesFullText.setVisibility(View.VISIBLE);
    }

    private void showContractedText() {
        binding.singleMedDetailsLayout.collapseIcon.setVisibility(View.GONE);
        binding.singleMedDetailsLayout.expandIcon.setVisibility(View.VISIBLE);
        binding.singleMedDetailsLayout.instructionsOrNotesText.setVisibility(View.VISIBLE);
        binding.singleMedDetailsLayout.instructionsOrNotesFullText.setVisibility(View.GONE);
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
                binding.singleMedDetailsLayout.instructionsOrNotesText.setVisibility(View.GONE);
                binding.singleMedDetailsLayout.instructionsOrNotesFullText.setVisibility(View.VISIBLE);
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
                binding.singleMedDetailsLayout.instructionsOrNotesText.setVisibility(View.VISIBLE);
                binding.singleMedDetailsLayout.instructionsOrNotesFullText.setVisibility(View.GONE);
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


    public interface onCustomBackPressed{
        void onCustomBackPress();
    }

    private void initToolbar() {

        binding.toolbar.setNavigationIcon(R.drawable.back_arrow);
        if (RunTimeData.getInstance().getScheduleData().getMeditationDuration().contains("Set Reminder") || RunTimeData.getInstance().getScheduleData().isNLPReminder()) {
            binding.toolbar.setTitle(R.string.create_schedule);
        } else {
            binding.toolbar.setTitle(R.string.edit_schedule);
        }
        binding.toolbar.setNavigationOnClickListener(v -> {
            RunTimeData.getInstance().getScheduleData().setIsFromScheduleMed(true);
            getActivity().onBackPressed();
        });
    }

    private void triggerFireBase() {
        if (!AppConstants.BULK_MEDICATION.equalsIgnoreCase(RunTimeData.getInstance().getLaunchSource())) {
            FireBaseAnalyticsTracker.getInstance().logScreenEvent(mContext, FireBaseConstants.ScreenEvent.SCREEN_CREATE_OR_EDIT_SCHEDULE);
        } else {
            FireBaseAnalyticsTracker.getInstance().logScreenEvent(mContext, FireBaseConstants.ScreenEvent.SCREEN_BULK_REMINDER_SETUP);
        }
    }

    private void clearScheduleViewModel() {
        ScheduleViewModel scheduleViewModel = RunTimeData.getInstance().getScheduleViewModel(getActivity());
        scheduleViewModel.getMedicationsList().setValue(new ArrayList<>());
        scheduleViewModel.getFrequencySelector().setValue(0);
        RunTimeData.getInstance().setScheduleViewModel(null);
    }

    public void expandImage(Drug drug, Context context) {
        EnlargeImageActivity.expandPillImage(context, drug.getGuid(), drug.getImageGuid());
    }

    public void showDrugDetails(Drug drug, Context context, View view) {
        ViewClickHandler.preventMultiClick(view);
        RunTimeData.getInstance().setMedDetailView(false);
        RunTimeData.getInstance().setFromArchive(false);
        Intent intent = new Intent(context, MedicationDetailActivity.class);
        intent.putExtra(PillpopperConstants.PILL_ID, drug.getGuid());
        context.startActivity(intent);
    }

    private void setObserver() {
        frequencySelector.observe(getViewLifecycleOwner(), integer -> {
            if(WEEKLY != integer)
                scheduleViewmodel.getWeeklySelectedDays().postValue(new ArrayList<>());
            if (lastSelectedIndex != -1 && !lastSelectedIndex.equals(integer)) {
                Util.resetScheduleData();
                lastSelectedIndex = integer;
            }
            binding.frameContainer2.setVisibility((WEEKLY == integer || CUSTOM == integer) ? View.VISIBLE : View.GONE);
        });

        mMedicationsList.observe(getViewLifecycleOwner(), drugs -> {
            if (null != drugs && !drugs.isEmpty()) {
                this.drugList = drugs;
                hideAndShowLayouts(drugs);
                Collections.sort(drugs, drugNameComparator);
                adapter.setData(drugs, getActivity());
                binding.setAdapter(adapter);
            }
        });
        scheduleViewmodel.getWeeklySelectedDays().observe(getViewLifecycleOwner(), strings -> {
            if(null==strings){
                return;
            }
            StringBuilder weeklyStringBuilder = new StringBuilder();
            for (String weekString : strings) {
                weeklyStringBuilder.append(weekString);
                weeklyStringBuilder.append(" ,");
            }
            if(!weeklyStringBuilder.toString().equalsIgnoreCase(""))
                binding.tvDays.setText(weeklyStringBuilder.toString());
        });

        scheduleViewmodel.getStartDateSelector().observe(getViewLifecycleOwner(), value -> {
            binding.monthlyDisclaimer.setVisibility(null != frequencySelector && null != frequencySelector.getValue() && frequencySelector.getValue() == 4 && value == 31 ? View.VISIBLE : View.GONE);
            if (null != frequencySelector && null != frequencySelector.getValue() && frequencySelector.getValue() == DAILY) {
                binding.tvDays.setVisibility(View.VISIBLE);
                binding.tvDays.setText("Everyday");
            } else {
                binding.tvDays.setVisibility(null != frequencySelector && null != frequencySelector.getValue() && frequencySelector.getValue() == 4 ? View.VISIBLE : View.GONE);
                binding.tvDays.setText(null != frequencySelector && null != frequencySelector.getValue() && frequencySelector.getValue() == 4 ? "every " + value + Util.getSuffix(value) : "");
            }
        });
        if(isSchedulingFromMedicationDetailScreen()){
            binding.labelLayout.setVisibility(View.GONE);
        }
    }


    private void hideAndShowLayouts(List<Drug> drugs) {
        if (!drugs.isEmpty()) {
            Util.setVisibility(new View[]{binding.createNewScheduleLabel,binding.addMedicationButtonLayout},View.GONE);
            Util.setVisibility(new View[]{binding.medicationEditorLayout,binding.medicationEditorLayout,binding.frequencySelectorLayout},View.VISIBLE);
            if(!mIsAnimAdded) {
                mIsAnimAdded = true;
                setAnim();
            }
        } else {
            showDefaultLayout();
        }
    }

    private void setAnim() {
        binding.addMedicationsForNewSchedule.setVisibility(View.VISIBLE);
        Animation animSlideUp = AnimationUtils.loadAnimation(mContext,
                R.anim.anim_fade_in_new);
        AnimationSet animationSet = new AnimationSet(true);
        animationSet.setInterpolator(new AccelerateInterpolator());
        animationSet.addAnimation(animSlideUp);
        binding.addMedicationsForNewSchedule.startAnimation(animationSet);
    }

    private void setAnimVisibility(View[] view,float value, int visibility) {
        for (View views : view) {
            views.animate()
                    .alpha(value)
                    .setDuration(ANIMATION_DURATION)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            views.setVisibility(visibility);
                        }
                    });
        }
    }

    public void initUiReferences() {

        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long l) {
                if(null == RunTimeData.getInstance().getScheduleData() || !RunTimeData.getInstance().getScheduleData().isEditMedicationClicked()) {
                    if (mLastSpinnerPosition != position && !Util.isEmptyString(selectedUserId) &&
                            !selectedUserId.equalsIgnoreCase(((User) parent.getItemAtPosition(position)).getUserId())) {
                        // First Check if any user meds added to ViewModel,then show the alert.
                        if (null != mMedicationsList.getValue() && !mMedicationsList.getValue().isEmpty()) {
                            if(!RunTimeData.getInstance().isSaveButtonEnabled()){
                                showDiscardAlert(position);
                            } else {
                                showSaveAlert(position);
                            }
                        } else {
                            if (!isCanceledClicked) {
                                selectUser(parent, position);
                            }
                        }
                    } else {
                        if (!isCanceledClicked) {
                            selectUser(parent, position);
                        }
                    }
                }
            }
            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {
                // Do nothing
            }
        });
        addMedicationClickListener = (OnAddMedicationClicked) getActivity();
    }

    private void showSaveAlert(int position) {
        DialogHelpers.showAlertWithSaveCancelListeners(getActivity(), R.string.save_updates, R.string.save_changes_on_exit_message,
                new DialogHelpers.Confirm_CancelListener() {
                    @Override
                    public void onConfirmed() {
                        RunTimeData.getInstance().setUserSelected(true);
                        RunTimeData.getInstance().setSpinnerPosition(position);
                        if(null != reminderTimeFragment){
                            reminderTimeFragment.saveScheduleOnBackPress(mContext);
                        }
                        isCanceledClicked = !RunTimeData.getInstance().isBulkMedsScheduleSaved();
                        Util.hideSoftKeyboard(getActivity());
//                        replaceFragmentOnUserSelection(position);
                    }

                    @Override
                    public void onCanceled() {
                        replaceFragmentOnUserSelection(position);
                    }
                });

        if (null != RunTimeData.getInstance().getAlertDialogInstance()) {
            // dismissed incase of inAPP reminder
            RunTimeData.getInstance().getAlertDialogInstance().setOnDismissListener(dialog -> binding.spinner.setSelection(mLastSpinnerPosition));
        }
    }

    private void replaceFragmentOnUserSelection(int position) {
        isCanceledClicked = false;
        Util.hideSoftKeyboard(getActivity());
        RunTimeData.getInstance().setSpinnerPosition(position);
        replaceHomeFragment(new ScheduleWizardFragment());
    }

    private void showDiscardAlert(int position) {
        DialogHelpers.showAlertWithConfirmDiscardListeners(getActivity(), R.string.discard_schedule_title, R.string.discard_schedule_message,
                new DialogHelpers.Confirm_CancelListener() {
                    @Override
                    public void onConfirmed() {
                        replaceFragmentOnUserSelection(position);
                    }

                    @Override
                    public void onCanceled() {
                        isCanceledClicked = true;
                        binding.spinner.setSelection(mLastSpinnerPosition);
                    }
                });

        if (null != RunTimeData.getInstance().getAlertDialogInstance()) {
            RunTimeData.getInstance().getAlertDialogInstance().setOnDismissListener(dialog -> binding.spinner.setSelection(mLastSpinnerPosition));
        }
    }

    /**
     * Shows the selected user in spinner, If not by default would take the first user/Primary User and will be shown.
     * @param parent
     * @param position
     */
    private void selectUser(AdapterView<?> parent, int position) {
        User user = (User) parent.getItemAtPosition(position);
        if(!Util.isEmptyString(RunTimeData.getInstance().getKphcSelectedUserID())){
            selectedUserId = RunTimeData.getInstance().getKphcSelectedUserID();
            for(User userObject : mProxyDropDownList){
                if(userObject.getUserId().equalsIgnoreCase(RunTimeData.getInstance().getKphcSelectedUserID())){
                    position = mProxyDropDownList.indexOf(userObject);
                }
            }
            RunTimeData.getInstance().setKphcSelectedUserID(null);
        }
        if(null == RunTimeData.getInstance().getScheduleData() || !RunTimeData.getInstance().getScheduleData().isEditMedicationClicked()) {
            binding.spinnerMemberName.setText(user.getFirstName());
        }
        selectedUserId = (!Util.isEmptyString(selectedUserId)) ? user.getUserId() : mProxyDropDownList.get(0).getUserId();
        binding.spinner.setSelection(position);
        mLastSpinnerPosition = position;
        try {
            if (!RunTimeData.getInstance().getScheduleData().isEditMedicationClicked()) {
                binding.setMedicationCount(mFrontController.getDrugsListByUserId(selectedUserId).size());
            }
        } catch (Exception ne) {
            LoggerUtils.info(ne.getMessage());
            binding.setMedicationCount(mFrontController.getDrugsListByUserId(selectedUserId).size());
        }
        checkForEmptyStateVisibility();
    }

    private void checkForEmptyStateVisibility() {
        binding.noMedicationLayout.setVisibility(mFrontController.getDrugsListByUserId(selectedUserId).isEmpty()?View.VISIBLE:View.GONE);
    }

    private void showDefaultLayout() {
        Util.setVisibility(new View[]{binding.createNewScheduleLabel,binding.addMedicationButtonLayout},View.VISIBLE);
        Util.setVisibility(new View[]{binding.medicationEditorLayout,binding.addMedicationsForNewSchedule},View.GONE);
    }

    public void loadSpinnerData() {
        ProxySpinnerAdapter spinnerAdapter = new ProxySpinnerAdapter(mContext, R.layout.user_spinner_item, mProxyDropDownList);
        binding.spinner.setAdapter(spinnerAdapter);
        if (null != mProxyDropDownList) {
            if (null == selectedUserId && !mProxyDropDownList.isEmpty()) {
                selectedUserId = mProxyDropDownList.get(0).getUserId();
                binding.userSpinnerCard.setContentDescription(isProxyAvailable() ? getString(R.string.content_description_member_name) :
                        mProxyDropDownList.get(0).getFirstName());
            }
            if(null == RunTimeData.getInstance().getScheduleData() || !RunTimeData.getInstance().getScheduleData().isEditMedicationClicked()) {
                binding.spinner.setClickable(isProxyAvailable());
                binding.userSpinnerCard.setClickable(isProxyAvailable());
                binding.spinner.setEnabled(isProxyAvailable());
                binding.spinnerArrow.setVisibility(isProxyAvailable() ? View.VISIBLE : View.GONE);
            }
        }
        if (0 != RunTimeData.getInstance().getSpinnerPosition()) {
            binding.spinner.setSelection(RunTimeData.getInstance().getSpinnerPosition());
        }
    }

    private boolean isProxyAvailable(){
        return (!mProxyDropDownList.isEmpty() && mProxyDropDownList.size() > 1);
    }

    public void replaceFragment(Fragment fragment) {
        binding.frameContainer1.removeAllViewsInLayout();
        binding.frameContainer2.removeAllViewsInLayout();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (!mIsFragAnimAdded) {
            mIsFragAnimAdded = true;
            fragmentTransaction.setCustomAnimations(R.anim.anim_fade_in_new, R.anim.fadeout);
        }
        fragmentTransaction.add(R.id.frame_container1, fragment);
        fragmentTransaction.commit();
        binding.frameContainer1.setVisibility(View.VISIBLE);
    }

    public void replaceFragmentDaily(Fragment fragment) {
        binding.frameContainer1.removeAllViewsInLayout();
        binding.frameContainer2.removeAllViewsInLayout();

        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (!mIsDailyFragAnimAdded) {
            mIsDailyFragAnimAdded = true;
            fragmentTransaction.setCustomAnimations(R.anim.anim_fade_in_new, R.anim.fadeout);
        }
        fragmentTransaction.add(R.id.frame_container2, fragment);
        fragmentTransaction.commit();
    }

    public void replaceHomeFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, fragment, TAG_FRAGMENT );
        fragmentTransaction.commit();
    }

    private void scrollToView(final NestedScrollView scrollViewParent, final View view) {
        // Get deepChild Offset
        Point childOffset = new Point();
        getDeepChildOffset(scrollViewParent, view.getParent(), view, childOffset);
        // Scroll to child.
        scrollViewParent.smoothScrollTo(0, childOffset.y);
    }

    private void getDeepChildOffset(final ViewGroup mainParent, final ViewParent parent, final View child, final Point accumulatedOffset) {
        ViewGroup parentGroup = (ViewGroup) parent;
        accumulatedOffset.x += child.getLeft();
        accumulatedOffset.y += child.getTop();
        if (parentGroup.equals(mainParent)) {
            return;
        }
        getDeepChildOffset(mainParent, parentGroup.getParent(), parentGroup, accumulatedOffset);
    }

    public void onAddMedicationClicked(View view) {
        ViewClickHandler.preventMultiClick(view);
        RunTimeData.getInstance().setScheduleViewModel(scheduleViewmodel);
        Bundle data = new Bundle();
        data.putString("selectedUserId", selectedUserId);
        data.putString("selectedUserName", mProxyDropDownList.get(binding.spinner.getSelectedItemPosition()).getFirstName());
        addMedicationClickListener.onAddMedicationClicked(data);
        Util.hideKeyboard(getContext(), binding.editBtn);
    }

    public void onScheduleMedication(int from) {
        handleMultipleClicks(from);
        binding.edit.setClickable(false);
        this.scheduleSelection = from;
        scrollToView(binding.svMain,binding.frameContainer1);
        setAnimVisibility(new View[]{binding.lrMedTime,binding.lrReminderTittle},0f,View.GONE);
        setAnimVisibility(new View[]{binding.lrMedType},1f,View.VISIBLE);

        if (getSelectedScheduleType() != from) {
            Util.setVisibility(new View[]{binding.frameContainer1,binding.frameContainer2},View.GONE);
            if(from != 4)
                binding.tvDays.setText("");
        }
        handleScheduleTypeText(from);
        handleScheduleTypeViewAnimation(from);
    }

    private void handleMultipleClicks(int type) {
        switch (type) {
            case 1:
                ViewClickHandler.preventMultiClick(binding.dailyBtn);
                binding.weeklyBtn.setClickable(false);
                binding.customBtn.setClickable(false);
                binding.monthlyBtn.setClickable(false);
                break;
            case 2:
                ViewClickHandler.preventMultiClick(binding.weeklyBtn);
                binding.dailyBtn.setClickable(false);
                binding.customBtn.setClickable(false);
                binding.monthlyBtn.setClickable(false);
                break;
            case 3:
                ViewClickHandler.preventMultiClick(binding.customBtn);
                binding.dailyBtn.setClickable(false);
                binding.weeklyBtn.setClickable(false);
                binding.monthlyBtn.setClickable(false);
                break;
            case 4:
                ViewClickHandler.preventMultiClick(binding.monthlyBtn);
                binding.dailyBtn.setClickable(false);
                binding.weeklyBtn.setClickable(false);
                binding.customBtn.setClickable(false);
                break;
            default:
                break;
        }
    }

    private void handleScheduleTypeText(int type) {
        binding.summaryDivider.setVisibility(View.GONE);
        binding.summarySelectedDivider.setVisibility(View.VISIBLE);
        switch (type) {
            case 1:
                binding.tvMedTime.setText(mContext.getResources().getString(R.string.txt_remindter_set_time_daily));
                break;
            case 2:
                binding.tvMedTime.setText(mContext.getResources().getString(R.string.txt_remindter_set_time_weekly));
                break;
            case 3:
                binding.tvMedTime.setText(mContext.getResources().getString(R.string.custom));
                break;
            case 4:
                binding.tvMedTime.setText(mContext.getResources().getString(R.string.txt_remindter_set_time_monthly));
                break;
            default:
                break;
        }
    }

    private void handleScheduleTypeViewAnimation(int from) {
        final Handler handler = new Handler();
        handler.postDelayed(() -> {
            switch (from) {
                case DAILY:
                    binding.monthlyDisclaimer.setVisibility(View.GONE);
                    if (!isOnStop) {
                        try {
                            if (null != RunTimeData.getInstance().getScheduleData() && selectedScheduleType.equalsIgnoreCase("Daily")) {
                                RunTimeData.getInstance().getScheduleData().setReminderAdded(true);
                            } else {
                                RunTimeData.getInstance().getScheduleData().setReminderAdded(false);
                            }
                        }catch (Exception ne)
                        {
                            LoggerUtils.info(ne.getMessage());
                        }
                        mIsFragAnimAdded = false;
                        scheduleViewmodel.getCustomFrequencyNumber().postValue("");
                        frequencySelector.postValue(DAILY);
                        setbackGroundColor(binding.dailyBtn, binding.weeklyBtn, binding.customBtn,binding.monthlyBtn);
                        if (getSelectedScheduleType() != DAILY) {
                             if(null != RunTimeData.getInstance().getScheduleData() &&
                                    RunTimeData.getInstance().getScheduleData().isEditMedicationClicked() &&
                                    !RunTimeData.getInstance().getScheduleData().isReminderAdded()) {
                                 Util.resetScheduleData();
                            }
                             replaceFragment(getReminderTimeFragmentInstance());
                        }
                        setSelectedScheduleType(DAILY);
                    } else {
                        onEditClicked();
                    }
                    break;
                case WEEKLY:
                    binding.monthlyDisclaimer.setVisibility(View.GONE);
                    if(!isOnStop) {
                    mIsDailyFragAnimAdded = false;
                    mIsFragAnimAdded = false;
                    frequencySelector.postValue(WEEKLY);
                    scheduleViewmodel.getCustomFrequencyNumber().postValue("");
                    setbackGroundColor(binding.weeklyBtn, binding.dailyBtn, binding.customBtn,binding.monthlyBtn);
                    try {
                        if (selectedScheduleType.contains("Weekly")) {
                            RunTimeData.getInstance().getScheduleData().setReminderAdded(true);
                        } else {
                            RunTimeData.getInstance().getScheduleData().setReminderAdded(false);
                        }
                    }catch (Exception ne){
                        LoggerUtils.info(ne.getMessage());
                    }
                        if (getSelectedScheduleType() != WEEKLY) {
                            replaceFragment(new WeeklyScheduleWizardFragment());
                            replaceFragmentDaily(getReminderTimeFragmentInstance());
                        }
                        setSelectedScheduleType(WEEKLY);
                    } else {
                        onEditClicked();
                    }
                    // setting isNLPReminder as false because if you change the frequency from daily to other and come back to daily we should not show the NLP suggestion
                    if(isaNLPReminder()){
                        RunTimeData.getInstance().getScheduleData().setNLPReminder(false);
                    }
                    break;
                case CUSTOM:
                    binding.monthlyDisclaimer.setVisibility(View.GONE);
                    if(!isOnStop) {
                        mIsFragAnimAdded = false;
                        frequencySelector.postValue(CUSTOM);
                        scheduleViewmodel.getWeeklySelectedDays().postValue(new ArrayList<>());
                        setbackGroundColor(binding.customBtn, binding.dailyBtn, binding.weeklyBtn,binding.monthlyBtn);
                        try {
                            if (!selectedScheduleType.contains("Weekly") && !selectedScheduleType.equalsIgnoreCase("Daily") && !selectedScheduleType.contains("monthly")) {
                                RunTimeData.getInstance().getScheduleData().setReminderAdded(true);
                            } else {
                                RunTimeData.getInstance().getScheduleData().setReminderAdded(false);
                            }
                        } catch (Exception ne){
                            LoggerUtils.info(ne.getMessage());
                        }
                        if (getSelectedScheduleType() != CUSTOM) {
                            replaceFragment(new CustomScheduleWizardFragment());
                            replaceFragmentDaily(getReminderTimeFragmentInstance());
                          //  RunTimeData.getInstance().setScheduleEdited(true);
                        }
                        setSelectedScheduleType(CUSTOM);
                    } else {
                        onEditClicked();
                    }
                    if(isaNLPReminder()){
                        RunTimeData.getInstance().getScheduleData().setNLPReminder(false);
                    }
                    break;
                case MONTHLY:
                    if(!isOnStop) {
                        try {
                            if (null != RunTimeData.getInstance().getScheduleData() && selectedScheduleType.equalsIgnoreCase("Monthly")) {
                                RunTimeData.getInstance().getScheduleData().setReminderAdded(true);
                            } else {
                                RunTimeData.getInstance().getScheduleData().setReminderAdded(false);
                            }
                        }catch (Exception ne)
                        {
                            LoggerUtils.info(ne.getMessage());
                        }
                        mIsFragAnimAdded = false;
                        scheduleViewmodel.getCustomFrequencyNumber().postValue("");
                        frequencySelector.postValue(MONTHLY);
                        setbackGroundColor(binding.monthlyBtn, binding.weeklyBtn, binding.customBtn,binding.dailyBtn);
                        if (getSelectedScheduleType() != MONTHLY) {
                            if(null != RunTimeData.getInstance().getScheduleData() &&
                                    RunTimeData.getInstance().getScheduleData().isEditMedicationClicked() &&
                                    !RunTimeData.getInstance().getScheduleData().isReminderAdded()) {
                                Util.resetScheduleData();
                            }
                            replaceFragment(getReminderTimeFragmentInstance());
                        }
                        setSelectedScheduleType(MONTHLY);
                    } else {
                        onEditClicked();
                    }
                    if(isaNLPReminder()){
                        RunTimeData.getInstance().getScheduleData().setNLPReminder(false);
                    }
                    break;
                default:
                    break;
            }
            binding.edit.setClickable(true);
        }, 1200);
    }
    private boolean isaNLPReminder(){
        return null != RunTimeData.getInstance().getScheduleData() && RunTimeData.getInstance().getScheduleData().isNLPReminder();
    }

    private ReminderTimeFragment getReminderTimeFragmentInstance() {
        reminderTimeFragment = new ReminderTimeFragment();
        if(null != mSaveReminderTimeFragmentInterface) {
            mSaveReminderTimeFragmentInterface.saveReminderTimeFragmentInstance(reminderTimeFragment);
        }
        return reminderTimeFragment;
    }


    public void onEditClicked() {
        resetFrequencyButtons();
        setAnimVisibility(new View[]{binding.lrMedType},0f,View.GONE);
        setAnimVisibility(new View[]{binding.lrMedTime,binding.lrReminderTittle},1f,View.VISIBLE);
        binding.summaryDivider.setVisibility(View.VISIBLE);
        binding.summarySelectedDivider.setVisibility(View.GONE);
        Util.hideKeyboard(getContext(), binding.edit);
    }

    private void resetFrequencyButtons() {
        binding.dailyBtn.setClickable(true);
        binding.weeklyBtn.setClickable(true);
        binding.customBtn.setClickable(true);
        binding.monthlyBtn.setClickable(true);
    }

    public void onSpinnerClick() {
        if(null == RunTimeData.getInstance().getScheduleData() || (null != RunTimeData.getInstance().getScheduleData() && !RunTimeData.getInstance().getScheduleData().isEditMedicationClicked())) {
            if (isProxyAvailable()) {
                binding.spinner.performClick();
            }
        }
    }

    private void setbackGroundColor(Button view1, Button view2, Button view3, Button view4) {
        view1.setTextColor(Util.getColorWrapper(mContext, R.color.white));
        view2.setTextColor(Util.getColorWrapper(mContext, R.color.black));
        view3.setTextColor(Util.getColorWrapper(mContext, R.color.black));
        view4.setTextColor(Util.getColorWrapper(mContext, R.color.black));

        view1.setBackground(ContextCompat.getDrawable(mContext, R.drawable.blue_round_button));
        view2.setBackground(ContextCompat.getDrawable(mContext, R.drawable.white_round_button_style));
        view3.setBackground(ContextCompat.getDrawable(mContext, R.drawable.white_round_button_style));
        view4.setBackground(ContextCompat.getDrawable(mContext, R.drawable.white_round_button_style));
    }

    @IntDef({NONE, DAILY, WEEKLY, CUSTOM, MONTHLY})
    private @interface ScheduleType {
    }

    public interface OnAddMedicationClicked {
        void onAddMedicationClicked(Bundle data);
    }

    public boolean isScheduleDiscardAlertRequired(){
        return (null != mMedicationsList.getValue() && !mMedicationsList.getValue().isEmpty());
    }

    private void initBroadCastReceivers() {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(refreshMedicationsImage, new IntentFilter(BROADCAST_REFRESH_FOR_MED_IMAGES));
    }

    private BroadcastReceiver refreshMedicationsImage = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String pillId = intent.getStringExtra(BUNDLE_EXTRA_DRUG_TO_REFRESH);
            try {
                if (!TextUtils.isEmpty(pillId)) {
                    List<Drug> selectedDrugsList = new ArrayList<>(scheduleViewmodel.getMedicationsList().getValue());
                    boolean isRemoved = false;
                    for (Drug drug : scheduleViewmodel.getMedicationsList().getValue()) {
                        if (drug.getGuid().equalsIgnoreCase(pillId)) {
                            selectedDrugsList.remove(drug);
                            isRemoved = true;
                        }
                    }
                    if(isRemoved) {
                        Drug restoreDrug = FrontController.getInstance(getActivity()).getDrugByPillId(pillId);
                        selectedDrugsList.add(restoreDrug);
                    }
                    scheduleViewmodel.getMedicationsList().setValue(selectedDrugsList);
                    adapter.setData(selectedDrugsList, getActivity());
                    adapter.notifyDataSetChanged();
                }
            }catch (Exception ne){
                LoggerUtils.info(ne.getMessage());
            }
        }
    };


    @Override
    public void onDestroyView() {
        super.onDestroyView();
        if(!getActivity().isFinishing() && null != refreshMedicationsImage) {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(refreshMedicationsImage);
        }
        RunTimeData.getInstance().setSpinnerPosition(0);
    }


    interface SaveReminderTimeFragmentInterface {
        void saveReminderTimeFragmentInstance(ReminderTimeFragment reminderTimeFragment);
    }
}