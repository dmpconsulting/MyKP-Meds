
package com.montunosoftware.pillpopper.android;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Rect;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.KeyEvent;
import android.view.MenuItem;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AdapterView;
import android.widget.ExpandableListView;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;

import com.android.camera.CropImageIntentBuilder;
import com.montunosoftware.mymeds.R;
import com.montunosoftware.mymeds.databinding.EditMedicationLayoutBinding;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.util.EmojiFilter;
import com.montunosoftware.pillpopper.android.util.FDADrugDatabase;
import com.montunosoftware.pillpopper.android.util.PhotoChooserUtility;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.UIUtils;
import com.montunosoftware.pillpopper.android.util.UniqueDeviceId;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.android.util.ViewClickHandler;
import com.montunosoftware.pillpopper.android.view.ActionEditText;
import com.montunosoftware.pillpopper.android.view.DialogHelpers;
import com.montunosoftware.pillpopper.android.view.ProxySpinnerAdapter;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.database.model.LogEntryModel;
import com.montunosoftware.pillpopper.database.model.PillList;
import com.montunosoftware.pillpopper.database.model.PillPreferences;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.PillpopperTime;
import com.montunosoftware.pillpopper.service.LogEntryUpdateAsyncTask;
import com.montunosoftware.pillpopper.service.images.loader.ImageLoaderUtil;
import com.montunosoftware.pillpopper.service.images.loader.ImageUILoaderManager;

import org.json.JSONException;
import org.json.JSONObject;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.activity.EnlargeImageActivity;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.model.User;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.mykpmeds.activation.util.PermissionUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class AddOrEditMedicationActivity extends StateListenerActivity implements ExpandableListView.OnGroupClickListener, ExpandableListView.OnChildClickListener, View.OnClickListener, TextWatcher, ActionEditText.ActionEDitListener {
    private EditMedicationLayoutBinding binding;
    private FrontController mFrontController;
    private String pillId;
    private Drug drug;
    int lastExpandedPosition = -1;
    FDADrugDatabase fdaDrugDatabase;
    private AddDrugExpandableList drugAdapter;
    private DBSearchTask searchTask;
    private String mUserid;
    private String mActionPill = PillpopperConstants.ACTION_CREATE_PILL;
    private List<User> userList;
    private static final int REQ_CHOOSE_PHOTO = 19;
    private static final int REQ_CROP_PHOTO = 20;
    private static final int REQ_DRUG_DB = 1;
    private String cropImageGuid;
    private int mLastSpinnerPosition;
    private boolean isNewDrug;
    private static final int SAVE_NOTES = 123;

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        binding = DataBindingUtil.setContentView(this, R.layout.edit_medication_layout);
        mFrontController = FrontController.getInstance(_thisActivity);
        initActionBar();
        loadDrugDetails();
        loadSpinnerData();


        binding.setButtonClickHandler(this);
        mActionPill = PillpopperConstants.ACTION_CREATE_PILL;
        if (null != getIntent()) {
            mActionPill = getIntent().getStringExtra(PillpopperConstants.LAUNCH_MODE);
            binding.spinnerMemberName.setTypeface(ActivationUtil.setFontStyle(this, AppConstants.FONT_ROBOTO_MEDIUM));
            binding.medName.setFilters(EmojiFilter.getFilter());
            binding.dosageStrength.setFilters(EmojiFilter.getFilter());
            if (null != mActionPill && mActionPill.equalsIgnoreCase(PillpopperConstants.ACTION_EDIT_PILL)) {
                FireBaseAnalyticsTracker.getInstance().logScreenEvent(_thisActivity, FireBaseConstants.ScreenEvent.SCREEN_EDIT_OTC_MEDICATION);
                pillId = getIntent().getStringExtra("ToSaveOTCMedication");
                drug = mFrontController.getDrugByPillId(pillId);
                binding.setDrug(drug);
                binding.spinnerMemberName.setText(drug.getMemberFirstName());
                binding.drugDetailDoseImage.setDefaultImage(R.drawable.rx_image_generic);
                getSupportActionBar().setTitle(getResources().getString(R.string.edit_drug));
                getSupportActionBar().setHomeActionContentDescription(getResources().getString(R.string.content_description_med_toolbar_up));
                binding.disclaimerText.setVisibility(View.GONE);
                binding.medName.setText(drug.getName());
                ImageUILoaderManager.getInstance().loadDrugImage(getActivity(), drug.getImageGuid(), drug.getGuid(), binding.drugDetailDoseImage, Util.getDrawableWrapper(getActivity(), R.drawable.rx_image_generic));
                binding.dosageStrength.setText(drug.getDose());
                if(null == drug.getNotes()){
                    drug.setNotes("");
                }
                binding.personalNotes.setText(drug.getNotes());
                mUserid = drug.getUserID();
                updateCameraView(drug);
                binding.spinnerArrow.setVisibility(View.GONE);
                binding.disclaimerText.setVisibility(View.GONE);
                binding.medName.setSelection(binding.medName.getText().toString().length());
            } else {//create otc mode
                if (binding.medName.getText().length() == 0)
                    binding.drugDetailDoseImage.setDefaultImage(R.drawable.rx_image_add);
                binding.setShowTextView(false);
                binding.setShowDosageTextview(false);
                FireBaseAnalyticsTracker.getInstance().logScreenEvent(_thisActivity, FireBaseConstants.ScreenEvent.SCREEN_ADD_MEDICATION);
                isNewDrug = true;
            }
        }
        RunTimeData.getInstance().setDrugImageGuidFromEnlargeAct(null);
        RunTimeData.getInstance().setDrugGuidFromEnlargeAct(null);
    }

    public void initActionBar() {
        setSupportActionBar((Toolbar) binding.appBar);
        getSupportActionBar().setTitle(getResources().getString(R.string._med_details_add_medication));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void loadDrugDetails() {
      binding.personalNotes.setOnClickListener(this);
      binding.btnSaveMedications.setOnClickListener(this);
        binding.medNameTxt.setOnTouchListener((view, motionEvent) -> {
            binding.setShowTextView(false);
            binding.dosageStrength.clearFocus();
            binding.medName.requestFocus();
            binding.medName.setFocusable(true);
            binding.medName.setCursorVisible(true);
            binding.medName.setSelection(binding.medName.getText().toString().length());
            Util.activateSoftKeyboard(binding.medName);
            if(binding.dosageStrength.getText().toString().trim().length()>0){
                binding.dosageTxt.setText(binding.dosageStrength.getText().toString().trim());
                binding.setShowDosageTextview(true);
            }
            return true;
        });
        binding.dosageTxt.setOnTouchListener((view, motionEvent) -> {
            binding.setShowDosageTextview(false);
            binding.lvExp.setVisibility(View.GONE);
            binding.medName.clearFocus();
            binding.dosageStrength.requestFocus();
            binding.dosageStrength.setFocusable(true);
            binding.dosageStrength.setCursorVisible(true);
            binding.dosageStrength.setSelection(binding.dosageStrength.getText().toString().length());
            Util.activateSoftKeyboard(binding.dosageStrength);
            if(binding.medName.getText().toString().trim().length()>0){
                binding.medNameTxt.setText(binding.medName.getText().toString().trim());
                binding.setShowTextView(true);
            }
            return true;
        });
        binding.medName.setOnEditorActionListener((textView, i, keyEvent) -> {
            if (i == EditorInfo.IME_ACTION_DONE) {
                binding.lvExp.setVisibility(View.GONE);
            }
            return false;
        });
        binding.dosageStrength.setOnTouchListener((v, event) -> {
            isKeyboardVisible();
            binding.setShowDosageTextview(false);
            if (binding.medName.getText().toString().trim().length() > 0) {
                binding.medNameTxt.setText(binding.medName.getText().toString());
                binding.setShowTextView(true);
            }
            binding.lvExp.setVisibility(View.GONE);
            binding.dosageStrength.setCursorVisible(true);
            if (event.getAction() == KeyEvent.ACTION_UP) {
                binding.dosageStrength.setCursorVisible(true);

            }
            binding.dosageStrength.setSelection(binding.dosageStrength.getText().toString().length());
            return false;
        });
        binding.medName.setOnTouchListener((v, event) -> {
            isKeyboardVisible();
            binding.setShowTextView(false);
            if (binding.dosageStrength.getText().toString().trim().length() > 0) {
                binding.dosageTxt.setText(binding.dosageStrength.getText().toString());
                binding.setShowDosageTextview(true);
            }
            if (event.getAction() == KeyEvent.ACTION_UP) {
                binding.medName.setCursorVisible(true);
            }
            scrollViewReset();
            binding.medName.postDelayed(() -> {
                InputMethodManager keyboard = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
                keyboard.showSoftInput(v, 0);
            }, 100);
            binding.medName.setSelection(binding.medName.getText().toString().length());
            return false;
        });
        binding.medName.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !binding.medName.isCursorVisible()) {
                if (binding.medName.getText().toString().trim().length() > 0) {
                    binding.medNameTxt.setText(binding.medName.getText().toString());
                    binding.setShowTextView(true);
                }
                binding.medName.postDelayed(() -> visibleLayoutView(), 100);
            } else {
                binding.setShowTextView(false);
                if(binding.dosageStrength.getText().toString().trim().length()>0){
                    binding.dosageTxt.setText(binding.dosageStrength.getText().toString().trim());
                    binding.setShowDosageTextview(true);
                }
            }
        });
        binding.dosageStrength.setOnFocusChangeListener((v, hasFocus) -> {
            if (!hasFocus && !binding.dosageStrength.isCursorVisible()) {
                if (binding.dosageStrength.getText().toString().trim().length() > 0) {
                    binding.dosageTxt.setText(binding.dosageStrength.getText().toString());
                    binding.setShowDosageTextview(true);
                }
                binding.dosageStrength.postDelayed(() -> visibleLayoutView(), 100);
            } else {
                binding.setShowDosageTextview(false);
            }
        });
        binding.lvExp.setGroupIndicator(null);
        fdaDrugDatabase = _thisActivity.get_globalAppContext().getFDADrugDatabase();
        binding.lvExp.setOnGroupClickListener(this);
        binding.lvExp.setOnTouchListener((view, motionEvent) -> {
            Util.hideKeyboard(_thisActivity, view);
            scrollViewReset();
            view.getParent().requestDisallowInterceptTouchEvent(true);
            return false;
        });
        binding.lvExp.setMinimumWidth(binding.medName.getWidth());
        binding.lvExp.setOnChildClickListener(this);
        binding.lvExp.setOnGroupExpandListener(groupPosition -> {
            if (groupPosition != lastExpandedPosition)
                binding.lvExp.collapseGroup(lastExpandedPosition);
            lastExpandedPosition = groupPosition;
        });
        binding.medName.addTextChangedListener(this);
        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                if (mLastSpinnerPosition != i && !Util.isEmptyString(mUserid) &&
                        !mUserid.equalsIgnoreCase(((User) adapterView.getItemAtPosition(i)).getUserId())) {
                    selectUser(adapterView, i);
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> adapterView) {

            }
        });
    }

    JSONObject jsonObj;

    private void saveMedicationToDB() {
        if (isValidEditMedication()) {
            RunTimeData.getInstance().setFromArchive(false);
            PillList pill = new PillList();
            pill.setPillId(drug.getGuid());
            PillPreferences preference = new PillPreferences();
            preference.setImageGUID(drug.getImageGuid());
            preference.setDosageType("custom");
            preference.setScheduleChoice(AppConstants.SCHEDULE_CHOICE_UNDEFINED);
            pill.setName(binding.medName.getText().toString());
            pill.setDose(binding.dosageStrength.getText().toString());
            preference.setNotes(binding.personalNotes.getText().toString().trim());
            if (binding.dosageStrength.getText().toString().trim().length() > 0) {
                preference.setCustomDescription(binding.dosageStrength.getText().toString());
            }
            pill.setUserId(mUserid);
            pill.setCreated(String.valueOf(PillpopperTime.now().getGmtSeconds()));
            pill.setPreferences(preference);
            if (PillpopperConstants.ACTION_CREATE_PILL.equalsIgnoreCase(mActionPill)) {

                //First Check guid value. If the GUid value present, means user would have taken the image first.
                if (!Util.isEmptyString(drug.getGuid())) {
                    pill.setPillId(drug.getGuid());
                } else {
                    pill.setPillId(Util.getRandomGuid());
                }
                if (Util.isEmptyString(drug.getImageGuid())) {
                    preference.setImageGUID(drug.getImageGuid());
                }
                FireBaseAnalyticsTracker.getInstance().logEvent(_thisActivity,
                        FireBaseConstants.Event.ADD_MEDS_SAVE,
                        FireBaseConstants.ParamName.SOURCE,
                        FireBaseConstants.ParamValue.ADD_MEDICATIONS);
                //pill.setType(String.valueOf(Schedule.SchedType.INTERVAL));
                pill.setScheduleGuid(Util.getRandomGuid());
                mFrontController.addMedication(_thisActivity, pill, mUserid);
                new CreatePillLogEntryUpdateTask().execute(pill);
                drug.setId(pill.getPillId());
                drug.setName(pill.getName());
                drug.getPreferences().setPreference("customDescription", pill.getDose());
                drug.setUserID(pill.getUserId());
                drug.setNotes(binding.personalNotes.getText().toString());
                updateImage(drug);
                gotoDrugDetails(pill.getPillId());
            } else {
                mFrontController.updateMedication(pill);
                drug.setName(pill.getName());
                drug.setDose(pill.getDose());
                drug.getPreferences().setPreference("customDescription", pill.getDose());
                drug.setUserID(pill.getUserId());
                drug.setNotes(binding.personalNotes.getText().toString().trim());
                FireBaseAnalyticsTracker.getInstance().logEvent(_thisActivity,
                        FireBaseConstants.Event.ADD_MEDS_SAVE,
                        FireBaseConstants.ParamName.SOURCE,
                        FireBaseConstants.ParamValue.EDIT_MEDICATIONS);
                mFrontController.updateNotes(binding.personalNotes.getText().toString().trim(), pill.getPillId());
                LogEntryUpdateAsyncTask logEntryUpdateAsyncTask = new LogEntryUpdateAsyncTask(_thisActivity, PillpopperConstants.ACTION_EDIT_PILL, drug);
                logEntryUpdateAsyncTask.execute();
            }
            setResult(RESULT_OK);
            finish();
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if(null==drug){
            drug = new Drug();
        }
        if (!RunTimeData.getInstance().getIsImageDeleted()) {
            if (null != RunTimeData.getInstance().getDrugImageGuidFromEnlargeAct()&&null!=RunTimeData.getInstance().getDrugGuidFromEnlargeAct()) {
                drug.setImageGuid(RunTimeData.getInstance().getDrugImageGuidFromEnlargeAct());
                drug.setId(RunTimeData.getInstance().getDrugGuidFromEnlargeAct());
                RunTimeData.getInstance().setDrugImageGuidFromEnlargeAct(null);
                RunTimeData.getInstance().setDrugGuidFromEnlargeAct(null);
            }
            ImageUILoaderManager.getInstance().loadDrugImage(getActivity(), drug.getImageGuid(), drug.getGuid(), binding.drugDetailDoseImage, Util.getDrawableWrapper(getActivity(), R.drawable.rx_image_generic));
        } else {
            binding.drugDetailDoseImage.setDefaultImage(isNewDrug ? R.drawable.rx_image_add : R.drawable.rx_image_generic);
            drug.setImageGuid(null);
            ImageUILoaderManager.getInstance().loadDrugImage(getActivity(), null, null, binding.drugDetailDoseImage, Util.getDrawableWrapper(getActivity(), R.drawable.rx_image_add));

        }
        binding.lvExp.setVisibility(View.GONE);
        if(binding.dosageTxt.getVisibility()==View.VISIBLE){
            binding.dosageStrength.setVisibility(View.GONE);
        }
        if(binding.medNameTxt.getVisibility()==View.VISIBLE){
            binding.medName.setVisibility(View.GONE);
        }
    }


    public void onImageClicked() {
        ViewClickHandler.preventMultiClick(binding.drugDetailDoseImage);
        if (!isNewDrug) {
            EnlargeImageActivity.expandPillImage(_thisActivity, drug.getGuid(), drug.getImageGuid());
        } else {
            if ( null != drug.getImageGuid()) {
                if (!Util.isEmptyString(binding.medName.getText().toString())) {
                    RunTimeData.getInstance().setMedName(binding.medName.getText().toString().trim());
                }else{
                    RunTimeData.getInstance().setMedName(null);
                }
                EnlargeImageActivity.expandPillImage(_thisActivity, drug.getGuid(), drug.getImageGuid());
            } else {
                showImageTakingMenu(_thisActivity, binding.addImageBtn, drug);
            }
        }
    }

    private void _launchSearch() {
        _lastSearchRequested++;
        String text = binding.medName.getText().toString().trim();
        // If the search string is long enough, launch a search
        _latestSearchString = text;
        _latestSearchType = _searchType;

        if (text.length() >= 3) {
            synchronized (_thisActivity) {
                if (_searchRunning == false) {
                    _searchRunning = true;
                    listDataHeader.clear();
                    try {
                        if (drugAdapter != null) {
                            drugAdapter.notifyDataSetChanged();
                        }
                    } catch (NullPointerException e) {
                        LoggerUtils.exception("_launchSearch NPE ", e);
                    }
                    searchTask = new DBSearchTask();
                    searchTask.execute();
                }
            }
        }
        if (text.length() == 0) {
            _lastSearchDisplayed = _lastSearchRequested;
            binding.dosageStrength.getText().clear();
            if (listDataHeader != null) {
                listDataHeader.clear();
                if (drugAdapter != null) {
                    drugAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private String _latestSearchString = null;
    private FDADrugDatabase.SearchType _latestSearchType = null;
    private int _lastSearchRequested = 0;
    private int _lastSearchDisplayed = 0;
    private boolean _searchRunning = false;
    List<String> listDataHeader = new ArrayList<>();
    HashMap<String, List<FDADrugDatabase.DatabaseDrugVariant>> listDataChild = new HashMap<>();

    final class DBSearchTask extends AsyncTask<Void, Void, FDADrugDatabase.DrugNameSearchResults> {
        private int _searchSerialNumber;

        private DBSearchTask() {
            super();
        }

        @Override
        protected FDADrugDatabase.DrugNameSearchResults doInBackground(Void... stringArgs) {
            String searchString;
            FDADrugDatabase.SearchType searchType;
            // Check and see if a search is waiting to be launched. If so, launch one.
            synchronized (_thisActivity) {
                if (_latestSearchString != null) {
                    searchString = _latestSearchString;
                    searchType = _latestSearchType;
                    _searchSerialNumber = _lastSearchRequested;
                    _latestSearchString = null;
                    _latestSearchType = null;
                } else {
                    _searchRunning = false;
                    return null;
                }
            }
            PillpopperLog.say("searching for %s", searchString);
            FDADrugDatabase.DrugNameSearchResults retval = fdaDrugDatabase.searchForDrugs(searchString, searchType);
            PillpopperLog.say("search for %s complete", searchString);
            return retval;
        }

        @Override
        protected void onPostExecute(FDADrugDatabase.DrugNameSearchResults results) {
            if (results != null && _searchSerialNumber > _lastSearchDisplayed) {
                _lastSearchDisplayed = _searchSerialNumber;
                _searchRunning = false;
                int iCount = 0;
                List<FDADrugDatabase.DatabaseDrugVariant> l = new ArrayList<>();
                if (results.getResults().isEmpty()) {
                    binding.lvExp.setVisibility(View.GONE);

                } else {
                    try {
                        if (listDataHeader != null) {
                            listDataHeader.clear();
                            if (drugAdapter != null) {
                                drugAdapter.notifyDataSetChanged();
                            }
                        }
                    } catch (NullPointerException e) {
                        LoggerUtils.exception("NPE DBSearchTask", e);
                    }
                    for (String result : results.getResults()) {
                        if(null != listDataHeader) {
                            listDataHeader.add(result);
                            listDataChild.put(listDataHeader.get(iCount), l);
                            iCount++;
                        }
                    }
                    drugAdapter = new AddDrugExpandableList(_thisActivity, listDataHeader, listDataChild);
                    binding.lvExp.setAdapter(drugAdapter);
                    binding.lvExp.setVisibility(View.VISIBLE);
                    if (binding.lvExp.getCount() == 0) {
                        binding.lvExp.setVisibility(View.GONE);
                    }
                    drugAdapter.notifyDataSetChanged();
                }
            }
        }
    }

    private FDADrugDatabase.SearchType _searchType = FDADrugDatabase.SearchType.DRUG_SEARCH_ALL;


    @Override
    public boolean onGroupClick(ExpandableListView expandableListView, View view, int i, long l) {
        List<FDADrugDatabase.DatabaseDrugVariant> variantMenu = fdaDrugDatabase.getListChildData(listDataHeader.get(i).toString());

        listDataChild.put(listDataHeader.get(i), variantMenu);
        drugAdapter.notifyDataSetChanged();
        return false;
    }

    private void scrollViewReset() {
        binding.scrollEditDrug.smoothScrollTo(0, 0);
    }

    @Override
    public boolean onChildClick(ExpandableListView expandableListView, View view, int i, int i1, long l) {
        FDADrugDatabase.DatabaseDrugVariant databaseDrugVariant = (FDADrugDatabase.DatabaseDrugVariant) expandableListView.getExpandableListAdapter().getChild(i, i1);
        binding.dosageStrength.setText(databaseDrugVariant.get_strength());
        binding.medName.setText(databaseDrugVariant.get_drugName());

        searchTask.cancel(true);
        binding.medName.setCursorVisible(false);

        binding.lvExp.setVisibility(View.GONE);
        return false;
    }

    @Override
    public void onClick(View view) {
        if(view==binding.personalNotes){
            //RunTimeData.getInstance().setEditTextChanged(true);
            binding.medName.clearFocus();
            binding.dosageStrength.clearFocus();
            binding.medNameTxt.setText(binding.medName.getText().toString().trim());
            binding.dosageTxt.setText(binding.dosageStrength.getText().toString().trim());
            binding.setShowTextView(true);
            binding.setShowDosageTextview(true);
            Intent intent = new Intent(AddOrEditMedicationActivity.this, EditNotesActivity.class);
            intent.putExtra("ToEditNotes", pillId);
            intent.putExtra("notesValue",binding.personalNotes.getText().toString().trim());
            startActivityForResult(intent, SAVE_NOTES);
        }else if(view == binding.btnSaveMedications){
            Util.hideKeyboard(this, binding.medName);
            saveMedicationToDB();
        }
    }

    @Override
    public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

    }

    @Override
    public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
        if(binding.dosageStrength.getText().toString().trim().length()>0){
            binding.dosageTxt.setText(binding.dosageStrength.getText().toString().trim());
            binding.setShowDosageTextview(true);
        }else{
            binding.setShowDosageTextview(false);
        }
        if (binding.medName.getText().toString().length() >= 3 && binding.medName.isFocused()) {
            _launchSearch();
        }
        if (binding.medName.getText().length() >= 0 && binding.medName.getText().length() <= 2) {
            if (binding.medName.getText().length() > 0) {
                scrollViewMove();
            }
            _searchRunning = false;
            visibleLayoutView();
        }
        if (binding.medName.getText().toString().trim().length() == 0) {
            scrollViewReset();
            _searchRunning = false;
        }
    }

    @Override
    public void afterTextChanged(Editable editable) {

    }

    public void scrollViewMove() {
        binding.scrollEditDrug.scrollTo(0, binding.medName.getTop());
    }

    public void visibleLayoutView() {
        binding.lvExp.setVisibility(View.GONE);
        binding.dosageStrength.setVisibility(View.VISIBLE);
        binding.personalNotesLayout.setVisibility(View.VISIBLE);
    }

    private boolean isValidEditMedication() {
        if (binding.medName.getText().toString().trim().length() == 0) {
            DialogHelpers.showAlertDialog(_thisActivity, R.string.drug_name_required);
            return false;
        }
        if (!UIUtils.isValidInput(binding.medName.getText().toString().trim()) || !UIUtils.isValidInput(binding.personalNotes.getText().toString().trim())
        || !UIUtils.isValidInput(binding.dosageStrength.getText().toString().trim())) {
            DialogHelpers.showAlertDialog(_thisActivity, R.string.textfield_error_message);
            return false;
        }
        return true;
    }

    @Override
    public void onKeyPreIME(int keyCode, KeyEvent event) {
        scrollViewReset();
    }

    private class CreatePillLogEntryUpdateTask extends AsyncTask<PillList, Void, Boolean> {
        @Override
        protected Boolean doInBackground(PillList... params) {
            logCreatePillEntry(params[0]);
            return true;
        }

        @Override
        protected void onPostExecute(Boolean createPillLogEntryresult) {
            if (createPillLogEntryresult) {
                PillpopperLog.say("-- CreatePill Log entry Action got completed");
            }
        }
    }

    @Override
    public void onBackPressed() {
        if (!TextUtils.isEmpty(getIntent().getStringExtra(PillpopperConstants.LAUNCH_SOURCE))) {
            if (PillpopperConstants.LAUNCH_SOURCE_SCHEDULE.equalsIgnoreCase(getIntent().getStringExtra(PillpopperConstants.LAUNCH_SOURCE))) {
                PillpopperConstants.setCanShowMedicationList(true);
            }
        }
        if (getSupportActionBar().getTitle().equals(getResources().getString(R.string.edit_drug))) {

            if (!binding.medName.getText().toString().equalsIgnoreCase(drug.getName()) ||
                    !binding.dosageStrength.getText().toString().equalsIgnoreCase(drug.getDose()) ||
                    !binding.personalNotes.getText().toString().equalsIgnoreCase(null == drug.getNotes() ? "" : drug.getNotes())) {
                showAlertDialogOnBackPress();
            } else{
                super.onBackPressed();
            }
        } else if (getSupportActionBar().getTitle().equals(getResources().getString(R.string._med_details_add_medication))) {
            if (binding.medName.getText().toString().length() > 0 ||
                    binding.dosageStrength.getText().toString().length() > 0 ||
                    binding.personalNotes.getText().toString().length() > 0) {
                showAlertDialogOnBackPress();
            } else{
                super.onBackPressed();
            }
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            // Respond to the action bar's Up/Home button
            case android.R.id.home:
                this.onBackPressed();
                return true;
        }
        return super.onOptionsItemSelected(item);
    }

    private void showAlertDialogOnBackPress() {
            DialogHelpers.showAlertWithSaveCancelListeners(this, R.string.save_updates, R.string.save_changes_on_exit_message,
                    new DialogHelpers.Confirm_CancelListener() {
                        @Override
                        public void onConfirmed() {
                            saveMedicationToDB();
                        }

                        @Override
                        public void onCanceled() {
                            getActivity().finish();
                        }
                    });
    }

    private void logCreatePillEntry(PillList addPill) {
        String replyId = Util.getRandomGuid();
        LogEntryModel logEntryModel = new LogEntryModel();
        logEntryModel.setDateAdded(System.currentTimeMillis());
        logEntryModel.setReplyID(replyId);
        jsonObj = prepareEntryObject("CreatePill", replyId, addPill);
        logEntryModel.setEntryJSONObject(jsonObj, _thisActivity);
        mFrontController.addLogEntry(_thisActivity, logEntryModel);
    }

    private JSONObject prepareEntryObject(String action, String replyId, PillList pill) {
        JSONObject pillpopperRequest = new JSONObject();
        JSONObject pillRequest = new JSONObject();
        JSONObject pillPrefRequest = new JSONObject();
        try {
            pillRequest.put("action", action);
            pillRequest.put("replayId", replyId);
            pillRequest.put("language", Util.getLanguage());
            pillRequest.put("deviceToken", "");
//            pillRequest.put("dose", pill.getDose());
            pillRequest.put("name", pill.getName());
//            pillRequest.put("type", "interval");
            pillRequest.put("clientVersion", Util.getAppVersion(_thisActivity));
            pillRequest.put("partnerId", PillpopperConstants.PARTNER_ID);
            pillRequest.put("pillId", pill.getPillId());
            pillRequest.put("interval", 0);
            pillRequest.put("hardwareId", UniqueDeviceId.getHardwareId(_thisActivity));
            pillRequest.put("userId", mFrontController.getPrimaryUserIdIgnoreEnabled());
            pillRequest.put("targetUserId", pill.getUserId());
            pillRequest.put("created", null != pill.getCreated() ? Long.parseLong(pill.getCreated()) : PillpopperTime.now().getGmtSeconds());
            pillRequest.put("isScheduleAddedOrUpdated", false);

            pillRequest.put("scheduleGuid", pill.getScheduleGuid());

            pillPrefRequest.put("limitType", "0");
            pillPrefRequest.put("customDosageID", "");
            pillPrefRequest.put("refillsRemaining", "-1");
            pillPrefRequest.put("personId", pill.getUserId());
            pillPrefRequest.put("invisible", "0");
            pillPrefRequest.put("archived", "0");
            pillPrefRequest.put("refillAlertDoses", "");
            pillPrefRequest.put("notes", pill.getPreferences().getNotes());
            pillPrefRequest.put("refillQuantity", "");
            pillPrefRequest.put("customDescription", pill.getDose());
            pillPrefRequest.put("noPush", "0");
            pillPrefRequest.put("remainingQuantity", "");
            pillPrefRequest.put("secondaryReminders", "1");
            pillPrefRequest.put("maxNumDailyDoses", "-1");
            pillPrefRequest.put("dosageType", PillpopperConstants.DOSAGE_TYPE_CUSTOM);
            pillPrefRequest.put("imageGUID", pill.getPreferences().getImageGUID());
            pillPrefRequest.put("scheduleChoice", AppConstants.SCHEDULE_CHOICE_UNDEFINED);
            pillPrefRequest.put("defaultServiceImageID",null != pill.getPreferences().getDefaultServiceImageID()? pill.getPreferences().getDefaultServiceImageID() :"");
            pillPrefRequest.put("needFDBUpdate",null != pill.getPreferences().getNeedFDBUpdate()? pill.getPreferences().getNeedFDBUpdate() : "false");
            pillPrefRequest.put("imageGUID", null != pill.getPreferences().getImageGUID()? pill.getPreferences().getImageGUID() : "");
            pillPrefRequest.put("defaultImageChoice", Util.isEmptyString(pill.getPreferences().getImageGUID()) ? AppConstants.IMAGE_CHOICE_NO_IMAGE: AppConstants.IMAGE_CHOICE_CUSTOM);
            pillRequest.put("preferences", pillPrefRequest);
            pillpopperRequest.put("pillpopperRequest", pillRequest);
        } catch (JSONException e) {
            PillpopperLog.say("Oops! Exception while preparing the request object while creating the logentry for : " + action, e);
        }
        return pillpopperRequest;
    }

    private void updateImage(Drug drug) {
        if (!Util.isEmptyString(drug.getImageGuid())
                && !Util.isEmptyString(drug.getGuid())) {
            mFrontController.updatePillImage(drug.getGuid(), drug.getImageGuid());
        }
    }

    private void storeImageToDatabase(Drug drug, String encodedImage) {
        try {
            if (!Util.isEmptyString(drug.getGuid()) && !Util.isEmptyString(drug.getImageGuid()) && !Util.isEmptyString(encodedImage)) {
                FrontController.getInstance(AddOrEditMedicationActivity.this).updateCustomImage(drug.getGuid(), drug.getImageGuid(), encodedImage);
            }
        } catch (Exception e) {
            PillpopperLog.exception("storeImageToDatabase -- " + e.getMessage());
        }
    }

    protected void showImageTakingMenu(final PillpopperActivity activity, View view, final Drug editdrug) {

        PopupMenu imageTakingMenu = new PopupMenu(activity, view);
        imageTakingMenu.getMenuInflater().inflate(R.menu.fdb_image_menu, imageTakingMenu.getMenu());
        //setting false, as not applicable for OTC meds
        imageTakingMenu.getMenu().findItem(R.id.my_kp_meds_image).setVisible(false);
        Util.getInstance().setChangeImagePopUpMenuVisibility(this, drug, imageTakingMenu, false);
        drug = editdrug;
        imageTakingMenu.setOnMenuItemClickListener(item -> {
            if (item.getTitle().toString().equalsIgnoreCase(getResources().getString(R.string.use_camera))) {
                if (PermissionUtils.checkVersionCode()) {
                    if (PermissionUtils.checkRuntimePermission(AppConstants.PERMISSION_CAMERA, Manifest.permission.CAMERA, _thisActivity)) {
                        PhotoChooserUtility.takePhoto(_thisActivity, true);
                    }
                } else {
                    PhotoChooserUtility.takePhoto(_thisActivity, true);
                }

            } else if (item.getTitle().toString().equalsIgnoreCase(getResources().getString(R.string.photo_gallery))) {
                if (PermissionUtils.checkVersionCode()) {
                    if (PermissionUtils.checkRuntimePermission(AppConstants.PERMISSION_READ_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE, AddOrEditMedicationActivity.this)) {
                        PhotoChooserUtility.takePhoto(_thisActivity,false);
                    }
                } else {
                    PhotoChooserUtility.takePhoto(_thisActivity,false);
                }

            }
            return true;
        });
        imageTakingMenu.show();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

            // permission was granted,
            if (requestCode == AppConstants.PERMISSION_CAMERA) {
                PhotoChooserUtility.takePhoto(_thisActivity,true);
            } else if (requestCode == AppConstants.PERMISSION_READ_EXTERNAL_STORAGE) {
                PhotoChooserUtility.takePhoto(_thisActivity,false);
            }

        } else {
            //permission Denied
            if (permissions.length > 0) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[0])) {
                    onPermissionDenied(requestCode);
                } else {
                    onPermissionDeniedNeverAskAgain(requestCode);
                }
            }
        }
    }

    private void performCropFromLibrary(Uri selectedImage){
        final int outputX = 150;
        final int outputY = 150;
        Intent cropIntent = new CropImageIntentBuilder(outputX, outputY, null)
                .setScale(true)
                .setScaleUpIfNeeded(true)
                .setSourceImage(selectedImage)
                .getIntent(this);
        cropIntent.putExtra("return-data", true);
        cropIntent.putExtra("image-path", selectedImage.toString());
        cropIntent.putExtra("scale", true);
        startActivityForResult(cropIntent, REQ_CROP_PHOTO);
    }

    public void updateCameraView(Drug editeddrug) {
        ImageUILoaderManager.getInstance().loadDrugImage(getActivity(), editeddrug.getImageGuid(), editeddrug.getGuid(), binding.drugDetailDoseImage, Util.getDrawableWrapper(getActivity(), R.drawable.rx_image_generic));
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent resultIntent) {
        super.onActivityResult(requestCode, resultCode, resultIntent);
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case REQ_CHOOSE_PHOTO:
                    RunTimeData.getInstance().setIsImageDeleted(false);
                    if (Util.isEmptyString(drug.getImageGuid())) {
                        cropImageGuid = Util.getRandomGuid();
                    } else {
                        cropImageGuid = drug.getImageGuid();
                    }
                    if (resultIntent != null && null != resultIntent.getData()) {
                        // If we have selected the image from gallery resultIntent will not be null.
                        performCropFromLibrary(resultIntent.getData());
                    } else {
                        if (AppConstants.contentUri != null) {
                            getActivity().revokeUriPermission(AppConstants.contentUri, Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                        }
                        RunTimeData.getInstance().setCpCode(null);
                        performCropFromLibrary(Uri.parse(AppConstants.photoFile.getAbsolutePath()));
                    }
                    break;
                case REQ_CROP_PHOTO:
                    drug.setImageGuid(cropImageGuid);
                    Bitmap drugImage = resultIntent.getParcelableExtra("data");
                    String encodedImage = ImageLoaderUtil.encodeImage(drugImage);
                    drug.setId(Util.getRandomGuid());
                    storeImageToDatabase(drug, encodedImage);
                    binding.drugDetailDoseImage.setBitmap(drugImage);
                    ImageUILoaderManager.getInstance().loadDrugImage(getActivity(), drug.getImageGuid(), drug.getGuid(), binding.drugDetailDoseImage, Util.getDrawableWrapper(getActivity(), R.drawable.rx_image_add));
                    cropImageGuid = null;
                    break;
                case REQ_DRUG_DB:
                    FDADrugDatabase.DatabaseDrugVariant variantSelected = DrugDatabaseNameSearchActivity.getSelectedDrugVariant(_thisActivity, resultIntent);
                    String[] splitString = variantSelected.toString().split(",");
                    binding.medName.setText(splitString[0]);
                    break;
                case SAVE_NOTES:
                    binding.dosageStrength.clearFocus();
                    binding.medName.clearFocus();
                    binding.setShowTextView(true);
                    binding.setShowDosageTextview(true);
                    if(null!=resultIntent.getExtras()) {
                        String notes = resultIntent.getExtras().getString("PersonalNotesValue");
                        binding.personalNotes.setText(notes);
                    }
                    break;
                default:
                    break;
            }
        }
    }

    public void loadSpinnerData() {
        if (null != mFrontController.getAllEnabledUsers() || null != RunTimeData.getInstance().getSelectedUsersList()) {
            userList = mFrontController.getAllEnabledUsers();
        }
        if (null == userList || userList.isEmpty()) {
            userList = RunTimeData.getInstance().getRegistrationResponse().getResponse().getUsers();
        }
        ProxySpinnerAdapter spinnerAdapter = new ProxySpinnerAdapter(this, R.layout.user_spinner_item, userList);
        binding.spinner.setAdapter(spinnerAdapter);
        if (null != userList) {
            if (null == mUserid && !userList.isEmpty()) {
                binding.spinnerMemberName.setText(userList.get(0).getFirstName());
                mUserid = userList.get(0).getUserId();
            }
            binding.userSpinnerCard.setClickable(isProxyAvailable());
            binding.spinner.setEnabled(isProxyAvailable());
            binding.spinnerArrow.setVisibility(isProxyAvailable() ? View.VISIBLE : View.GONE);
        }
        if (0 != RunTimeData.getInstance().getSpinnerPosition() && null != userList) {
            binding.spinner.setSelection(RunTimeData.getInstance().getSpinnerPosition());
            binding.spinnerMemberName.setText(userList.get(RunTimeData.getInstance().getSpinnerPosition()).getFirstName());
        }
    }

    private boolean isProxyAvailable() {
        return (!userList.isEmpty() && userList.size() > 1);
    }

    public void onSpinnerClick() {
        if (isProxyAvailable() && PillpopperConstants.ACTION_CREATE_PILL.equalsIgnoreCase(mActionPill)) {
            binding.spinner.performClick();
        }

    }

    private void selectUser(AdapterView<?> parent, int position) {
        User user = (User) parent.getItemAtPosition(position);
        if (!Util.isEmptyString(RunTimeData.getInstance().getKphcSelectedUserID())) {
            mUserid = RunTimeData.getInstance().getKphcSelectedUserID();
            for (User userObject : userList) {
                if (userObject.getUserId().equalsIgnoreCase(RunTimeData.getInstance().getKphcSelectedUserID())) {
                    position = userList.indexOf(userObject);
                }
            }
            RunTimeData.getInstance().setKphcSelectedUserID(null);
        }
        binding.spinnerMemberName.setText(user.getFirstName());
        mUserid = (!Util.isEmptyString(mUserid)) ? user.getUserId() : userList.get(0).getUserId();
        binding.spinner.setSelection(position);
        mLastSpinnerPosition = position;

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        Util.hideSoftKeyboard(this);
        RunTimeData.getInstance().setMedName(null);
        RunTimeData.getInstance().setIsImageDeleted(false);
    }
    private void gotoDrugDetails(String pillId) {
        RunTimeData.getInstance().setMedDetailView(true);
        Intent detailIntent = new Intent(_thisActivity, MedicationDetailActivity.class);
        detailIntent.putExtra(PillpopperConstants.PILL_ID, pillId);
        if (!TextUtils.isEmpty(getIntent().getStringExtra(PillpopperConstants.LAUNCH_SOURCE))) {
            if (PillpopperConstants.LAUNCH_SOURCE_SCHEDULE.equalsIgnoreCase(getIntent().getStringExtra(PillpopperConstants.LAUNCH_SOURCE))) {
                detailIntent.putExtra(PillpopperConstants.LAUNCH_SOURCE, PillpopperConstants.LAUNCH_SOURCE_SCHEDULE);
            }
        }
        detailIntent.setFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
        detailIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        startActivity(detailIntent);
        finish();
    }

    public void isKeyboardVisible() {
        if (mActionPill.equalsIgnoreCase(PillpopperConstants.ACTION_CREATE_PILL)) {
            binding.rootLayout.getViewTreeObserver().addOnGlobalLayoutListener(
                    () -> {

                        Rect r = new Rect();
                        binding.rootLayout.getWindowVisibleDisplayFrame(r);
                        int screenHeight = binding.rootLayout.getRootView().getHeight();

                        // r.bottom is the position above soft keypad or device button.
                        // if keypad is shown, the r.bottom is smaller than that before.
                        int keypadHeight = screenHeight - r.bottom;


                        if (keypadHeight > screenHeight * 0.15) { // 0.15 ratio is perhaps enough to determine keypad height.
                            // keyboard is opened
                            binding.disclaimerText.setVisibility(View.GONE);
                        } else {
                            // keyboard is closed
                            new Handler(Looper.getMainLooper()).postDelayed(() -> binding.disclaimerText.setVisibility(View.VISIBLE), 100);
                        }
                    });
        }

    }

    @Override
    public void onConfigurationChanged(@NonNull Configuration newConfig) {
        super.onConfigurationChanged(newConfig);
        finish();
    }
}
