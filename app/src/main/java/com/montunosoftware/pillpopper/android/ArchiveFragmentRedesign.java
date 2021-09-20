package com.montunosoftware.pillpopper.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.android.view.DrugDetailRoundedImageView;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.database.model.ArchiveListDataWrapper;
import com.montunosoftware.pillpopper.database.model.ArchiveListUserDropDownData;
import com.montunosoftware.pillpopper.model.ArchiveListDrug;
import com.montunosoftware.pillpopper.model.StateUpdatedListener;
import com.montunosoftware.pillpopper.service.getstate.StateDownloadIntentService;
import com.montunosoftware.pillpopper.service.images.loader.ImageUILoaderManager;

import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * @author
 * Created by adhithyaravipati on 5/27/16.
 */
public class ArchiveFragmentRedesign extends Fragment implements StateUpdatedListener {

    private PillpopperActivity mPillpopperActivity; //Variable to hold the Pillpopper Activity Context
    private ArrayList<ArchiveListUserDropDownData> mProxyDropDownList;
    private View mBaseLayout;
    private RelativeLayout mProxySelectorLinearLayout;
    private Spinner mProxyUserSpinner;
    private RecyclerView mArchiveListRecyclerView;
    private TextView mNoArchivedMedsTextView;
    private ImageView mProxyDropDownArrowImageView;
    private ArchiveListDataWrapper mArchiveListDataWrapper = new ArchiveListDataWrapper();
    private String mSelectedUserId = null;
    ReminderListenerInterfaces  mReminderShowListener;

    BroadcastReceiver mGetStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
        }
    };
    private View view;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.archive_list_redesign_layout, container, false);

        StateListenerActivity _thisActivity = (StateListenerActivity) getActivity();
        mPillpopperActivity = (PillpopperActivity) getActivity();

        FireBaseAnalyticsTracker.getInstance().logScreenEvent(mPillpopperActivity, FireBaseConstants.ScreenEvent.SCREEN_ARCHIVE_LIST);

        initUiReferences(view);

        new GetArchiveListDataTask().execute();

        return view;
    }

    public void initUiReferences(View view) {
        mBaseLayout = view;
        mProxySelectorLinearLayout = view.findViewById(R.id.archive_list_proxy_picker_linearlayout);
        mProxyDropDownArrowImageView = view.findViewById(R.id.archive_list_proxy_dropdown_imageview);
        mArchiveListRecyclerView = view.findViewById(R.id.archive_list_recyclerview);
        LinearLayoutManager recyclerViewLinearLayoutManager = new LinearLayoutManager(mPillpopperActivity);
        mArchiveListRecyclerView.setLayoutManager(recyclerViewLinearLayoutManager);
        mNoArchivedMedsTextView = view.findViewById(R.id.archive_list_no_archived_medications_textview);
        mProxyUserSpinner = mProxySelectorLinearLayout.findViewById(R.id.archive_list_proxy_name_spinner);
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter getStateIntentFilter = new IntentFilter();
        getStateIntentFilter.addAction(StateDownloadIntentService.BROADCAST_GET_STATE_COMPLETED);
        getActivity().registerReceiver(mGetStateBroadcastReceiver,getStateIntentFilter);

        if(mSelectedUserId != null) {
            new GetArchiveListDataHashMapTask().execute();
        }
    }

    @Override
    public void onResume() {
        super.onResume();

        mReminderShowListener.showReminder(true);
        RunTimeData.getInstance().setFirstTimeLandingOnHomeScreen(true);
    }
    private void installFragment(Fragment f) {
        String TAG_FRAGMENT = "TAG_FRAGMENT";
        FragmentTransaction fragmentTransaction = getFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.fragment_container, f, TAG_FRAGMENT);
        fragmentTransaction.replace(R.id.schedule_fragment_calendar_container, new ScheduleCalendarEmptyFragment());
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();

    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mReminderShowListener = (ReminderListenerInterfaces) context;
        } catch (ClassCastException e) {
            PillpopperLog.say("on Attach ClassCaseException ", e);
            throw new ClassCastException(context.toString() + " must implement ReminderListenerInterfaces");
        }
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mGetStateBroadcastReceiver);
    }

    @Override
    public void onStateUpdated() {

    }

    public void refreshArchiveListForUserId(String userId) {
        if(mArchiveListDataWrapper != null && mArchiveListDataWrapper.getArchivedDrugsHashMap().containsKey(userId)) {
            if( !mArchiveListDataWrapper.getArchivedDrugsHashMap().get(userId).isEmpty()) {
                ArchiveListRecyclerAdapter adapter = new ArchiveListRecyclerAdapter(mArchiveListDataWrapper.getArchivedDrugsHashMap().get(userId));
                mArchiveListRecyclerView.setAdapter(adapter);
                mArchiveListRecyclerView.setVisibility(View.VISIBLE);
                mNoArchivedMedsTextView.setVisibility(View.GONE);
            } else {
                mArchiveListRecyclerView.setVisibility(View.GONE);
                mNoArchivedMedsTextView.setVisibility(View.VISIBLE);
            }
        } else {
            mArchiveListRecyclerView.setVisibility(View.GONE);
            mNoArchivedMedsTextView.setVisibility(View.VISIBLE);
        }
    }

    public void initProxySpinner() {

        if(mProxyDropDownList.size() > 1) {
            mProxyDropDownArrowImageView.setVisibility(View.VISIBLE);
            mProxyUserSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
                @Override
                public void onItemSelected(AdapterView<?> adapterView, View view, int i, long l) {
                    mSelectedUserId = mProxyDropDownList.get(i).getUserId();
                    refreshArchiveListForUserId(mSelectedUserId);
                }

                @Override
                public void onNothingSelected(AdapterView<?> adapterView) {

                }
            });
        } else {
            mProxyDropDownArrowImageView.setVisibility(View.GONE);
            mProxyUserSpinner.setEnabled(false);
            mSelectedUserId = mProxyDropDownList.get(0).getUserId();
        }

        ProxySpinnerAdapter spinnerAdapter = new ProxySpinnerAdapter (
                mPillpopperActivity,
                R.layout.archive_list_proxy_spinner_item,
                mProxyDropDownList,
                mPillpopperActivity.getResources()
        );
        mProxyUserSpinner.setAdapter(spinnerAdapter);
    }

    private void initArchiveList() {
        if(null!=mProxyDropDownList && !mProxyDropDownList.isEmpty()){
            mSelectedUserId = mProxyDropDownList.get(0).getUserId();
            refreshArchiveListForUserId(mSelectedUserId);
            initProxySpinner();
        }
    }

    private class ArchiveListRecyclerAdapter extends RecyclerView.Adapter<ArchiveListRecyclerAdapter.ArchiveListBaseViewHolder> {

        private List<ArchiveListDrug> archivedDrugListForAdapter;
        public static final int TYPE_MEDICATION_WITH_HEADER_OTC = 1;
        public static final int TYPE_MEDICATION_WITH_HEADER_KPHC = 2;
        public static final int TYPE_MEDICATION_LIST_ITEM = 3;

        public class ArchiveListBaseViewHolder extends RecyclerView.ViewHolder {
            public ArchiveListBaseViewHolder(View view) {
                super(view);
            }
        }

        public class ArchiveListMedicationViewHolder extends ArchiveListBaseViewHolder {
            private LinearLayout medicationHolder;
            private TextView genericMedicationNameTextView;
            private TextView medicationNameTextView;
            private TextView medicationDosageTextView;
            private ImageView mNotes_icon;
            private DrugDetailRoundedImageView medicationImage;

            public ArchiveListMedicationViewHolder(View view) {
                super(view);
                medicationHolder = view.findViewById(R.id.archive_list_item_medication_holder);
                genericMedicationNameTextView = view.findViewById(R.id.archive_list_item_medication_generic_name_textview);
                medicationNameTextView = view.findViewById(R.id.archive_list_item_medication_name_textview);
                medicationDosageTextView = view.findViewById(R.id.archive_list_item_medication_dosage_textview);
                mNotes_icon = view.findViewById(R.id.archive_notes_icon);
                medicationImage = view.findViewById(R.id.archive_list_item_medication_image_imageview);
                medicationImage.setDefaultImage(R.drawable.pill_default);
            }
        }

        public ArchiveListRecyclerAdapter(List<ArchiveListDrug> archivedDrugListForAdapter) {
            this.archivedDrugListForAdapter = archivedDrugListForAdapter;
        }

        @Override
        public int getItemCount() {
            return this.archivedDrugListForAdapter.size();
        }

        @Override
        public int getItemViewType(int position) {

            if (position == 0) {
                if (archivedDrugListForAdapter.get(position).isManaged()) {
                    return TYPE_MEDICATION_WITH_HEADER_KPHC;
                } else {
                    return TYPE_MEDICATION_WITH_HEADER_OTC;
                }
            } else {
                if (!archivedDrugListForAdapter.get(position - 1).isManaged()
                        && archivedDrugListForAdapter.get(position).isManaged()) {
                    return TYPE_MEDICATION_WITH_HEADER_KPHC;
                }
            }
            return TYPE_MEDICATION_LIST_ITEM;
        }

        @Override
        public ArchiveListBaseViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
            View view = null;
            switch (viewType) {
                case TYPE_MEDICATION_WITH_HEADER_OTC:
                    view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.archive_list_item_with_otc_header_layout, viewGroup, false);
                    return new ArchiveListMedicationViewHolder(view);
                case TYPE_MEDICATION_WITH_HEADER_KPHC:
                    view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.archive_list_item_with_kphc_header_layout, viewGroup, false);
                    return new ArchiveListMedicationViewHolder(view);
                case TYPE_MEDICATION_LIST_ITEM:
                    view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.archive_list_item, viewGroup, false);
                    return new ArchiveListMedicationViewHolder(view);
            }
            return new ArchiveListBaseViewHolder(view);
        }

        @Override
        public void onBindViewHolder(ArchiveListBaseViewHolder archiveListBaseViewHolder, int i) {

            final String pillId = (archivedDrugListForAdapter.get(i)).getPillId();
            final String brandName = (archivedDrugListForAdapter.get(i)).getBrandName();
            final String genericName = (archivedDrugListForAdapter.get(i)).getGenericName();
            final String dose = (archivedDrugListForAdapter.get(i)).getDose();
            String notes = (archivedDrugListForAdapter.get(i)).getNotes();
            final String imageGuid = (archivedDrugListForAdapter.get(i)).getImageGuid();
            ArchiveListMedicationViewHolder archiveListMedicationViewHolder = (ArchiveListMedicationViewHolder) archiveListBaseViewHolder;

            archiveListMedicationViewHolder.medicationHolder.setOnClickListener(view -> {
                RunTimeData.getInstance().setMedDetailView(false);
                RunTimeData.getInstance().setFromArchive(true);
                Intent intent = new Intent(getActivity(), MedicationDetailActivity.class);
                intent.putExtra(PillpopperConstants.PILL_ID, pillId);
                startActivity(intent);
            });

            if (null != brandName && !("").equalsIgnoreCase(brandName) && !(" ").equalsIgnoreCase(brandName) && !("null").equalsIgnoreCase(brandName)) {
                archiveListMedicationViewHolder.genericMedicationNameTextView.setText(brandName);
                archiveListMedicationViewHolder.genericMedicationNameTextView.setVisibility(View.VISIBLE);
            } else {
                archiveListMedicationViewHolder.genericMedicationNameTextView.setVisibility(View.GONE);
            }

            if (null != genericName && !("").equalsIgnoreCase(genericName) && !(" ").equalsIgnoreCase(genericName) && !("null").equalsIgnoreCase(genericName)) {
                archiveListMedicationViewHolder.medicationNameTextView.setText(genericName);
                archiveListMedicationViewHolder.medicationNameTextView.setVisibility(View.VISIBLE);
            } else {
                archiveListMedicationViewHolder.medicationNameTextView.setVisibility(View.GONE);
            }

            if (null != dose && !("").equalsIgnoreCase(dose) && !(" ").equalsIgnoreCase(dose) && !("null").equalsIgnoreCase(dose)) {
                archiveListMedicationViewHolder.medicationDosageTextView.setText(dose);
                archiveListMedicationViewHolder.medicationDosageTextView.setVisibility(View.VISIBLE);
            } else {
                archiveListMedicationViewHolder.medicationDosageTextView.setVisibility(View.GONE);
            }

            if (null != notes && !notes.isEmpty()) {
                archiveListMedicationViewHolder.mNotes_icon.setVisibility(View.VISIBLE);
            } else {
                archiveListMedicationViewHolder.mNotes_icon.setVisibility(View.INVISIBLE);
            }
            ImageUILoaderManager.getInstance().loadDrugImage(getActivity(), imageGuid,pillId, archiveListMedicationViewHolder.medicationImage, Util.getDrawableWrapper(getActivity(),R.drawable.pill_default));
        }
    }

    private class ProxySpinnerAdapter extends ArrayAdapter<String> {
        private ArrayList data;
        LayoutInflater inflater;

        public ProxySpinnerAdapter(PillpopperActivity pillpopperActivity,
                                   int textViewResourceId,
                                   ArrayList objects,
                                   Resources resLocal) {
            super(pillpopperActivity, textViewResourceId, objects);
            data = objects;
            inflater = (LayoutInflater) mPillpopperActivity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        }

        @Override
        public View getDropDownView(int position, View convertView,ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            return getCustomView(position, convertView, parent);
        }

        // This funtion called for each row ( Called data.size() times )
        public View getCustomView(int position, View convertView, ViewGroup parent) {

            View row = inflater.inflate(R.layout.archive_list_proxy_spinner_item, parent, false);
            ArchiveListUserDropDownData tempValues = (ArchiveListUserDropDownData) data.get(position);
            TextView proxyNameTextView = row.findViewById(R.id.archive_list_proxy_name_textview);
            proxyNameTextView.setText(tempValues.getUserFirstName());

            final String newUserIdToUpdateList = tempValues.getUserId();
            return row;
        }
    }

    public class GetArchiveListDataTask
        extends AsyncTask<Void, Void, ArchiveListDataWrapper> {

        @Override
        protected ArchiveListDataWrapper doInBackground(Void... params) {
            return FrontController.getInstance(mPillpopperActivity).getArchiveListData(mPillpopperActivity);
        }

        @Override
        protected void onPostExecute(ArchiveListDataWrapper result) {
            if(result != null) {
                mProxyDropDownList = result.getUserDropDownList();
                mArchiveListDataWrapper.setArchivedDrugsHashMap(result.getArchivedDrugsHashMap());
                initArchiveList();
            }
            mBaseLayout.setVisibility(View.VISIBLE);
        }
    }

    public class GetArchiveListDataHashMapTask
        extends  AsyncTask<String, Void, HashMap<String, ArrayList<ArchiveListDrug>>> {

        @Override
        protected HashMap<String, ArrayList<ArchiveListDrug>> doInBackground(String... params) {
            return FrontController.getInstance(mPillpopperActivity).getArchiveListDataHashMap(mPillpopperActivity);
        }

        @Override
        protected void onPostExecute(HashMap<String, ArrayList<ArchiveListDrug>> result) {
            mArchiveListDataWrapper.setArchivedDrugsHashMap(result);
            refreshArchiveListForUserId(mSelectedUserId);
        }
    }
}