package com.montunosoftware.pillpopper.android;

import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.view.DialogHelpers;

import org.jetbrains.annotations.NotNull;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;

public class ScheduleLoadingActivity extends StateListenerActivity implements ScheduleWizardFragment.OnAddMedicationClicked, ReminderTimeFragment.SaveScheduleListener,ScheduleWizardFragment.SaveReminderTimeFragmentInterface {
    private static final String TAG_FRAGMENT = "TAG_FRAGMENT";
    ReminderTimeFragment mReminderTimeFragment;


    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);

        setContentView(R.layout.schedule_loading_activity);
        if(null != RunTimeData.getInstance().getScheduleData() && RunTimeData.getInstance().getScheduleData().isEditMedicationClicked()) {
            FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
            fragmentTransaction.replace(R.id.schedule_container, new ScheduleWizardFragment(),TAG_FRAGMENT);
            fragmentTransaction.setCustomAnimations(R.anim.anim_slide_left, R.anim.anim_slide_right);
            fragmentTransaction.commit();
            return;
        }
    }

    @Override
    public void onAddMedicationClicked(Bundle data) {
        Intent intent = new Intent(this, AddMedicationsForScheduleActivity.class);
        intent.putExtras(data);
        getActivity().startActivity(intent);
    }

    @Override
    public void onBackPressed() {
        RunTimeData.getInstance().getScheduleData().setIsFromScheduleMed(true);
        RunTimeData.getInstance().setMedDetailView(true);
        Fragment fragment = getSupportFragmentManager().findFragmentByTag(TAG_FRAGMENT);
        if (null != fragment && fragment instanceof ScheduleWizardFragment && ((ScheduleWizardFragment) fragment).isScheduleDiscardAlertRequired()) {
            if (RunTimeData.getInstance().isScheduleEdited()) {
                if (!RunTimeData.getInstance().isSaveButtonEnabled()) {
                    // show confirmation Alert first.
                    DialogHelpers.showAlertWithConfirmDiscardListeners(this, R.string.discard_schedule_title, R.string.discard_schedule_on_exit_message,
                            new DialogHelpers.Confirm_CancelListener() {
                                @Override
                                public void onConfirmed() {
                                    RunTimeData.getInstance().setScheduleEdited(false);
                                    getActivity().finish();
                                }

                                @Override
                                public void onCanceled() {
                                    //do nothing
                                }
                            });
                } else {
                    RunTimeData.getInstance().setSaveButtonEnabled(false);
                    RunTimeData.getInstance().setScheduleEdited(false);
                    DialogHelpers.showAlertWithSaveCancelListeners(this, R.string.save_updates, R.string.save_changes_on_exit_message,
                            new DialogHelpers.Confirm_CancelListener() {
                                @Override
                                public void onConfirmed() {
                                    if (null != mReminderTimeFragment) {
                                        mReminderTimeFragment.saveScheduleOnBackPress(ScheduleLoadingActivity.this);
                                    }
                                }

                                @Override
                                public void onCanceled() {
                                    getActivity().finish();
                                }
                            });
                }
            } else {
                super.onBackPressed();
            }
        }
    }

    @Override
    public void onSaveScheduleClicked() {
        // fix for DE21053
        if(!RunTimeData.getInstance().isUserSelected()) {
            RunTimeData.getInstance().getScheduleData().setIsFromScheduleMed(true);
            RunTimeData.getInstance().setMedDetailView(true);
            finish();
        }
        RunTimeData.getInstance().setUserSelected(false);
    }

    @Override
    public void saveReminderTimeFragmentInstance(ReminderTimeFragment reminderTimeFragment) {
        mReminderTimeFragment = reminderTimeFragment;
    }

    @Override
    public void onConfigurationChanged(@NonNull @NotNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        RunTimeData.getInstance().setScheduleEdited(false);
        RunTimeData.getInstance().setSaveButtonEnabled(false);
        finish();
    }
}
