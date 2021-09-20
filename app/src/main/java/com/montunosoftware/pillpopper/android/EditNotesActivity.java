package com.montunosoftware.pillpopper.android;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.text.Selection;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.databinding.DataBindingUtil;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.mymeds.databinding.EditNotesLayoutBinding;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.util.EmojiFilter;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.UIUtils;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.android.view.DialogHelpers;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.service.LogEntryUpdateAsyncTask;

import org.kp.tpmg.mykpmeds.activation.util.GenericAlertDialog;


public class EditNotesActivity extends StateListenerActivity {
    private EditNotesLayoutBinding binding;
    private FrontController mFrontController;
    private String pillId;
    private Drug drug;
    private boolean isClicked = false;
    private String notes;
    private boolean isConfigChanged;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        binding = DataBindingUtil.setContentView(this, R.layout.edit_notes_layout);
        mFrontController = FrontController.getInstance(_thisActivity);
        binding.personalNotes.setFilters(EmojiFilter.getFilter());
        initActionBar();
        loadDrugDetails();
        notes = getIntent().getExtras().getString("notesValue");
        if(Util.isEmptyString(drug.getNotes())){
            if(!Util.isEmptyString(notes)){
                binding.personalNotes.setText(notes);
            }else {
                getSupportActionBar().setTitle(getResources().getString(R.string.add_notes));
                FireBaseAnalyticsTracker.getInstance().logScreenEvent(this, FireBaseConstants.ScreenEvent.SCREEN_ADD_NOTES);
            }
        }else{
            FireBaseAnalyticsTracker.getInstance().logScreenEvent(this, FireBaseConstants.ScreenEvent.SCREEN_EDIT_NOTES);
            binding.personalNotes.setText(drug.getNotes());
        }
    }

    public void initActionBar() {
        setSupportActionBar((Toolbar) binding.appBar);
        getSupportActionBar().setTitle(getResources().getString(R.string.edit_notes));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeActionContentDescription(getResources().getString(R.string.content_description_med_toolbar_up));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.refill_reminder_save_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == R.id.save_menu_item) {
            onSaveClicked();
        }
        if (item.getItemId() == android.R.id.home) {
            showAlert();
        }
        return true;
    }

    private void onSaveClicked() {
            Util.hideSoftKeyboard(this);
            if (UIUtils.isValidInput(binding.personalNotes.getText().toString())) {
                if (null != drug.getGuid()) {
                    mFrontController.updateNotes(binding.personalNotes.getText().toString().trim(), pillId);
                    drug.setNotes(binding.personalNotes.getText().toString().trim());
                    addLogEntryForEdit(drug, this);
                }
                if (drug.isManaged()) {
                    if(null!=getSupportActionBar().getTitle() && getSupportActionBar().getTitle().equals(getResources().getString(R.string.edit_notes))){
                        FireBaseAnalyticsTracker.getInstance().logEvent(this,
                                FireBaseConstants.Event.NOTES_SAVE,
                                FireBaseConstants.ParamName.SOURCE,
                                FireBaseConstants.ParamValue.EDIT_NOTES);
                    }else{
                        FireBaseAnalyticsTracker.getInstance().logEvent(this,
                                FireBaseConstants.Event.NOTES_SAVE,
                                FireBaseConstants.ParamName.SOURCE,
                                FireBaseConstants.ParamValue.ADD_NOTES);
                    }
                    setResult(RESULT_OK);
                    finish();
                } else {
                    Intent intent = new Intent();
                    intent.putExtra("PersonalNotesValue", binding.personalNotes.getText().toString().trim());
                    setResult(RESULT_OK, intent);
                    finish();
                }
            } else {
                DialogHelpers.showAlertDialog(EditNotesActivity.this, R.string.textfield_error_message);
            }
    }

    private void loadDrugDetails() {
        Intent intent = getIntent();
        if (null != intent) {
            pillId = intent.getStringExtra("ToEditNotes");
        }
        drug = mFrontController.getDrugByPillId(pillId);
        binding.setDrugDetails(drug);
        Util.activateSoftKeyboard(binding.personalNotes);
        Selection.setSelection(binding.personalNotes.getText(), binding.personalNotes.getText().toString().length());
        binding.setButtonClickHandler(this);
    }

    private void addLogEntryForEdit(Drug drug, PillpopperActivity pillpopperActivity) {
        try {
            LogEntryUpdateAsyncTask logEntryUpdateAsyncTask = new LogEntryUpdateAsyncTask(pillpopperActivity, "EditPill", drug);
            logEntryUpdateAsyncTask.execute();
        } catch (Exception e) {
            PillpopperLog.say("Exception while adding log entry for edit ", e);
        }
    }

    public void onEditTextTouch() {
        if (!isClicked) {
            isClicked = true;
            binding.personalNotes.setCursorVisible(true);
            binding.personalNotes.setSelection(binding.personalNotes.getText().toString().length());
        }
    }
    private void showAlert() {
        Util.hideSoftKeyboard(this);
        if ((null == drug.getNotes() && binding.personalNotes.getText().toString().length() > 0 && Util.isEmptyString(notes)) || (null != drug.getNotes() && !drug.getNotes().equals(binding.personalNotes.getText().toString().trim())) || (!Util.isEmptyString(notes) && !notes.equals(binding.personalNotes.getText().toString().trim()))) {
            DialogHelpers.showAlertWithSaveCancelListeners(this, R.string.save_updates, R.string.save_changes_on_exit_message,
                    new DialogHelpers.Confirm_CancelListener() {
                        @Override
                        public void onConfirmed() {
                            onSaveClicked();
                        }

                        @Override
                        public void onCanceled() {
                            finish();
                        }
                    });

        } else {
            finish();
        }
    }
    private final DialogInterface.OnClickListener okListener = (dialogInterface, i) -> {
        dialogInterface.dismiss();
        finish();

    };

    @Override
    public void onBackPressed() {
        //super.onBackPressed();
        if (!isConfigChanged) {
            showAlert();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Util.hideSoftKeyboard(this);
    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        isConfigChanged = true;
        onBackPressed();
    }
}

