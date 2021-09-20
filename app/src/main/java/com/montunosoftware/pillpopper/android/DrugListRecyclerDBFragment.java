package com.montunosoftware.pillpopper.android;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.util.ComposableComparator;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.kotlin.bannerCard.GenericBannerFragment;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.State;
import com.montunosoftware.pillpopper.model.genericCardAndBanner.AnnouncementsItem;
import com.montunosoftware.pillpopper.service.getstate.StateDownloadIntentService;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.PermissionUtils;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * @author
 * Created by M1023050 on 1/28/2016.
 */
public class DrugListRecyclerDBFragment extends Fragment{

    private RecyclerView mRecyclerView;
    private LinearLayout mArchiveHintLayout;
    private Menu mArchiveMenu;
    private List<Drug> mArchivedDrugCount;
    private DrugListRecyclerDBAdapter adapter;
    private RelativeLayout mNoDrugsHelpMesg;
    private DrugListRecyclerDBFragment.RefillLauncherInterface mRefillLauncherInterface;
    private FrontController mFrontController;
    private List<Drug> mPillList = new ArrayList<>();
    private LinkedHashMap<String, List<Drug>> _drugList = new LinkedHashMap<>();

    private String primaryUserId;
    private List<String> proxyUserIds;
    private StateListenerActivity _thisActivity;
    ReminderListenerInterfaces  mReminderShowListener;
    private Button mAddMedications;


    BroadcastReceiver mGetStateBroadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(PillpopperConstants.KEY_ACTION) &&
                    intent.getStringExtra(PillpopperConstants.KEY_ACTION).equals(PillpopperConstants.ACTION_HISTORY_EVENTS)) {
                _drugList = new LinkedHashMap<>();
                loadMembersData();
            }else {
               // adapter.notifyDataSetChanged();
                if(null!=adapter){
                    adapter.notifyItemRangeChanged(0, adapter.getItemCount());
                }
            }
        }
    };
    private View mView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mView = inflater.inflate(R.layout.db_drug_list_fragment, container, false);
        _thisActivity = (StateListenerActivity) getActivity();
        mFrontController = FrontController.getInstance(_thisActivity);
        if(null!=mPillList && !mPillList.isEmpty())
            mPillList.clear();
        mArchiveHintLayout = mView.findViewById(R.id.archive_hint_layout);
        mNoDrugsHelpMesg = mView.findViewById(R.id.druglist_empty_help);
        mRecyclerView = mView.findViewById(R.id.recycler_view);
        mAddMedications = mView.findViewById(R.id.fragment_druglist_add_medication);
        mAddMedications.setOnClickListener(v -> {
            Intent intent = new Intent(_thisActivity,AddOrEditMedicationActivity.class);
            intent.putExtra(PillpopperConstants.LAUNCH_MODE, PillpopperConstants.ACTION_CREATE_PILL);
            startActivity(intent);
        });
        RecyclerView.ItemAnimator animator = mRecyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        FireBaseAnalyticsTracker.getInstance().logScreenEvent(_thisActivity, FireBaseConstants.ScreenEvent.SCREEN_MED_LIST);
        setHasOptionsMenu(true);
        setBannerLayout();
        return mView;
    }

    private void setBannerLayout() {
        List<AnnouncementsItem> bannerToShow = Util.getGenericBannerList(getContext(),FireBaseConstants.ScreenEvent.SCREEN_MED_LIST);
        if (null != bannerToShow && !bannerToShow.isEmpty()) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("bannerData", (Serializable) bannerToShow);
            Fragment fragment = new GenericBannerFragment();
            fragment.setArguments(bundle);
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(R.id.banner_container, fragment);
            fragmentTransaction.commit();
            mView.findViewById(R.id.banner_container).setVisibility(View.VISIBLE);
        }
    }


    private void loadMembersData() {
        loadPrimaryUserData();
        loadProxyUsersData();
        mPillList = getAllDrugs(_drugList);
        getActivity().invalidateOptionsMenu();
        loadAdapter(false);
    }

    private List<Drug> getAllDrugs(LinkedHashMap<String, List<Drug>> drugList) {
        List<Drug> mPillList = new ArrayList<>();
        for (Map.Entry<String, List<Drug>> entry : drugList.entrySet()) {
            mPillList.addAll(entry.getValue());
        }
        return mPillList;
    }

    private void loadProxyUsersData() {
        proxyUserIds = mFrontController.getProxyMemberUserIds();
        for(String userId : proxyUserIds){
            LinkedHashMap<String, List<Drug>> proxyUserData = mFrontController.getDrugListByUserId(_thisActivity, userId);
            if (proxyUserData != null && proxyUserData.size() > 0) {
                for (Map.Entry<String, List<Drug>> entry : proxyUserData.entrySet()) {
                    _drugList.put(entry.getKey(), getSortedList(entry.getValue(), entry.getKey()));
                }
            }else{
                _drugList.put(userId, addHeaders(new ArrayList<>(), userId));//to show no drugs for member
            }
        }
    }

    private void loadPrimaryUserData() {
        primaryUserId = mFrontController.getPrimaryUserId();
        if(!("").equalsIgnoreCase(primaryUserId)) { // primary userId will be "" if turned off in settings
            LinkedHashMap<String, List<Drug>> primaryUserData = mFrontController.getDrugListByUserId(_thisActivity, primaryUserId);
            if (primaryUserData != null && primaryUserData.size() > 0) {
                for (Map.Entry<String, List<Drug>> entry : primaryUserData.entrySet()) {
                    _drugList.put(entry.getKey(), getSortedList(entry.getValue(), entry.getKey()));
                }
            }else{
                _drugList.put(primaryUserId, addHeaders(new ArrayList<>(), primaryUserId));//to show no drugs for member
            }
        }
    }

    private List<Drug> getSortedList(List<Drug> _drugList, String userId) {
        // create a copy of the list so we can sort it without frobbing the
        // original
        List<Drug> sortedDrugList = new ArrayList<>(_drugList);

        State.DrugSortOrder drugSortOrder = _thisActivity.getState().getDrugSortOrderByReminderSet();

        // Regardless of the view mode, we sort all managed drugs with
        // server changes to the top,
        // followed by sorting all archived drugs to the bottom.
        // Note the order of these comparators implies that managed drugs
        // which are archived
        // and have server changes will temporarily bubble up to the top.
        ComposableComparator<Drug> comparator = new ComposableComparator<Drug>().by(
                new Drug.ByManagedNotificationPendingComparator()).by(new Drug.ByArchivalStatusComparator());

        // Now, add a lower-order-bits comparator depending on the view
        // mode.
        switch (drugSortOrder) {
            case ByDrugName:
                comparator = comparator.by(new Drug.AlphabeticalByNameComparator());
                break;
            case ByReminderSet:
                comparator = comparator.by(new Drug.ByRemindersetComparator()).by(new Drug.AlphabeticalByNameComparator());
                break;
        }

        Collections.sort(sortedDrugList, comparator);

        return addHeaders(sortedDrugList, userId);
    }

    private List<Drug> addHeaders(List<Drug> finalDrugList, String userId) {
        List<Drug> listWithHeader = new ArrayList<>();

        Drug temp = new Drug();
        temp.setIsTempHeadr(true);
        temp.setUserID(userId);
        if(finalDrugList.isEmpty()){
            temp.setIsNoDrugsFound(true);
        }
        temp.setMemberFirstName(mFrontController.getUserFirstNameByUserId(userId));
        listWithHeader.add(0, temp);
        listWithHeader.addAll(finalDrugList);

        return listWithHeader;
    }

    @Override
    public void onPause() {
        super.onPause();
    }

    @Override
    public void onStart() {
        super.onStart();
        IntentFilter mGetStateReceiverIntentFilter = new IntentFilter();
        mGetStateReceiverIntentFilter.addAction(StateDownloadIntentService.BROADCAST_GET_STATE_COMPLETED);
        getActivity().registerReceiver(mGetStateBroadcastReceiver,mGetStateReceiverIntentFilter);
    }

    @Override
    public void onStop() {
        super.onStop();
        getActivity().unregisterReceiver(mGetStateBroadcastReceiver);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mRefillLauncherInterface = (DrugListRecyclerDBFragment.RefillLauncherInterface) context;
            mReminderShowListener = (ReminderListenerInterfaces) context;
        } catch (ClassCastException e) {
            //throw new ClassCastException(context.toString() + " must implement RefillLauncherInterface and mReminderShowListener");
            PillpopperLog.say("ClassCastException, must implement RefillLauncherInterface and mReminderShowListener", e);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        if (mReminderShowListener!=null)
        mReminderShowListener.showReminder(true);
        setHasOptionsMenu(true);
        loadMembersData();
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

    private void loadAdapter(boolean isEditTapped){
        final List<Drug> list = mPillList;
        if ((primaryUserId != null && !("").equalsIgnoreCase(primaryUserId)) || (proxyUserIds != null && !proxyUserIds.isEmpty())) {
            enableEditMenu(mArchiveMenu);
            mNoDrugsHelpMesg.setVisibility(View.GONE);
            mRecyclerView.setVisibility(View.VISIBLE);
            mRecyclerView.setHasFixedSize(true);
            mRecyclerView.addItemDecoration(new DividerItemDecoration());
            mRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));

            for(Drug d : list){
                d.setIsChecked(false);
            }
            final List<Drug> tempDrugList = new ArrayList<Drug>(list);
            for(int i=0;i<tempDrugList.size();i++){
                Drug d = tempDrugList.get(i);
                if(d.isArchived() && d.isChecked()){
                    tempDrugList.remove(i);
                }
            }
            adapter = new DrugListRecyclerDBAdapter(_thisActivity, list,isEditTapped, _thisActivity.getState()/*, noDrugUserIds*/);
            mRecyclerView.setAdapter(adapter);
            adapter.setOnItemClickListener((view, position, archivedDrugCount) -> {
                if(tempDrugList.get(position).isChecked() == true){
                    archivedDrugCount.add(tempDrugList.get(position));
                    mArchivedDrugCount = archivedDrugCount;

                }else if(tempDrugList.get(position).isChecked() == false){
                    archivedDrugCount.remove(tempDrugList.get(position));
                    mArchivedDrugCount = archivedDrugCount;
                }

                MenuItem archive = mArchiveMenu.findItem(R.id.drug_list_archive);
                MenuItem editMenu = mArchiveMenu.findItem(R.id.drug_list_edit);
                if (!archivedDrugCount.isEmpty()) {
                    // Enable the archive button
                    enableArchiveMenu(archive);
                } else {
                    //Disable the Archive button
                    disableArchiveMenu(archive, editMenu);
                }
            });
            adapter.setCallBackForRefill(() -> {
                if(mRefillLauncherInterface!=null)
                     mRefillLauncherInterface.refillBannerClicked(true);
            });
        }else{
            mArchiveHintLayout.setVisibility(View.GONE);
            mNoDrugsHelpMesg.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
            hideEditMenu(mArchiveMenu);
        }
        if (adapter != null && adapter.getDrugsCount() <= 0) {
            mArchiveHintLayout.setVisibility(View.GONE);
            mNoDrugsHelpMesg.setVisibility(View.VISIBLE);
            mRecyclerView.setVisibility(View.GONE);
            hideEditMenu(mArchiveMenu);
        }else{
            enableEditMenu(mArchiveMenu);
        }
    }

    private void hideEditMenu(Menu mArchiveMenu) {
        if(null!=mArchiveMenu) {
            mArchiveMenu.findItem(R.id.drug_list_edit).setEnabled(false);
            mArchiveMenu.findItem(R.id.drug_list_edit).getIcon().setAlpha(100);
        }
    }

    private void enableEditMenu(Menu mArchiveMenu) {
        if(null!=mArchiveMenu){
            MenuItem item = mArchiveMenu.findItem(R.id.drug_list_menu_cancel);
            if(null!= item && item.isVisible() == false){
                mArchiveMenu.findItem(R.id.drug_list_edit).setEnabled(true);
                mArchiveMenu.findItem(R.id.drug_list_edit).getIcon().setAlpha(255);
            }
        }
    }


    private void enableCloseMenu(Menu mArchiveMenu) {
        MenuItem menuItem = mArchiveMenu.findItem(R.id.drug_list_menu_cancel);
        menuItem.setEnabled(true);
        menuItem.setVisible(true);
    }

    @Override
    public void onPrepareOptionsMenu(Menu menu) {
        if(null!=mArchiveMenu) {
            if (mNoDrugsHelpMesg.getVisibility() == View.VISIBLE) {
                hideEditMenu(mArchiveMenu);
            }
        }
        _thisActivity.getSupportActionBar().setHomeAsUpIndicator(R.drawable.drawable_hamburger_vector_white);
        super.onPrepareOptionsMenu(menu);
    }

    @Override
    public void onCreateOptionsMenu(final Menu menu, MenuInflater inflater) {
        super.onCreateOptionsMenu(menu, inflater);
        inflater.inflate(R.menu.drug_list_menu, menu);
        mArchiveMenu = menu;
        mArchiveHintLayout.setVisibility(View.GONE);

        menu.findItem(R.id.drug_list_menu_create).setOnMenuItemClickListener(item -> {
            Util.NavDrawerUtils.closeNavigationDrawerIfOpen();
            FireBaseAnalyticsTracker.getInstance().logEvent(getActivity(),
                    FireBaseConstants.Event.ADD_MEDS,
                    FireBaseConstants.ParamName.SOURCE,
                    FireBaseConstants.ParamValue.MED_LIST);
            Intent intent = new Intent(_thisActivity, AddOrEditMedicationActivity.class);
            intent.putExtra(PillpopperConstants.LAUNCH_MODE, PillpopperConstants.ACTION_CREATE_PILL);
            startActivity(intent);
            return true;
        });

        menu.findItem(R.id.drug_list_menu_cancel).setOnMenuItemClickListener(item -> {
            _thisActivity.invalidateOptionsMenu();
            mArchiveHintLayout.setVisibility(View.GONE);
            menu.findItem(R.id.drug_list_menu_create).setVisible(true);
            Util.NavDrawerUtils.closeNavigationDrawerIfOpen();
            loadAdapter(false);
            return true;
        });
        menu.findItem(R.id.drug_list_edit).setOnMenuItemClickListener(item -> {
            item.setVisible(false);
            menu.findItem(R.id.drug_list_menu_create).setVisible(false);
            mArchiveHintLayout.setVisibility(View.VISIBLE);
            enableCloseMenu(mArchiveMenu);
            MenuItem archive = menu.findItem(R.id.drug_list_archive);
            MenuItem editMenuItem = menu.findItem(R.id.drug_list_edit);
            disableArchiveMenu(archive, editMenuItem);
            Util.NavDrawerUtils.closeNavigationDrawerIfOpen();
            loadAdapter(true);
            return true;
        });

        menu.findItem(R.id.drug_list_archive).setOnMenuItemClickListener(item -> {
            if (null != mArchivedDrugCount  && !mArchivedDrugCount.isEmpty()) {
                FireBaseAnalyticsTracker.getInstance().logEvent(getActivity(),
                        FireBaseConstants.Event.MED_ARCHIVE,
                        FireBaseConstants.ParamName.SOURCE,
                        FireBaseConstants.ParamValue.MED_LIST);
                for (Drug drug : mArchivedDrugCount) {
                    if (drug.isChecked() == true) {
                        drug.setIsChecked(false);
                        drug.setArchived(true);
                        mFrontController.markDrugAsArchive(drug.getGuid());
                        //deleting Empty and Postpone history events if we archive medications
                        Util.getInstance().deleteEmptyAndPostponeEntries(getActivity(),drug);
                        if (drug.isManaged()) {
                            drug.ackPendingChanges();
                        }

                        try {
                            //later "setScheduleAddedOrUpdated(true)" might be removed, as we are considering
                            // "medicationScheduleChanged" for archieve also(instead of "medArchivedOrRemoved")
                            if(null!=drug.getSchedule() && drug.getSchedule().getTimeList().length() > 0) {
                                drug.setScheduleAddedOrUpdated(true);
                            }
                            FrontController.getInstance(_thisActivity).addLogEntry(_thisActivity, Util.prepareLogEntryForAction("EditPill", drug, _thisActivity));
                        }catch (Exception e){
                            PillpopperLog.say("Oops Exception while adding log entry", e);
                        }
                    }

                }
                _thisActivity.invalidateOptionsMenu();
                mArchiveHintLayout.setVisibility(View.GONE);
                loadMembersData();
                loadAdapter(false);
            }
            return true;
        });
        loadAdapter(false);
    }

    private void disableArchiveMenu(MenuItem menuItem, MenuItem  editItem){
        menuItem.setVisible(true);
        menuItem.getIcon().setAlpha(100);
        menuItem.setEnabled(false);
        editItem.setVisible(false);
        editItem.setTitle("");
    }

    private void enableArchiveMenu(MenuItem menuItem){
        menuItem.setVisible(true);
        menuItem.getIcon().setAlpha(255);
        menuItem.setEnabled(true);
    }

    private class DividerItemDecoration extends RecyclerView.ItemDecoration{
        private Drawable mDivider;

        public DividerItemDecoration() {
            mDivider = Util.getDrawableWrapper(_thisActivity, R.drawable.line_divider);
        }

        public void onDrawOver(Canvas c, RecyclerView parent, RecyclerView.State state) {
            int left = parent.getPaddingLeft();
            int right = parent.getWidth() - parent.getPaddingRight();

            int childCount = parent.getChildCount();
            for (int i = 0; i < childCount; i++) {
                View child = parent.getChildAt(i);

                RecyclerView.LayoutParams params = (RecyclerView.LayoutParams) child.getLayoutParams();

                int top = child.getBottom() + params.bottomMargin;
                int bottom = top + mDivider.getIntrinsicHeight();

                mDivider.setBounds(left, top, right, bottom);
                mDivider.draw(c);
            }
        }
    }

    public interface RefillLauncherInterface{
        void refillBannerClicked(boolean isBannerCicked);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            if (requestCode == AppConstants.PERMISSION_READ_EXTERNAL_STORAGE){
                adapter.shareMedications();
            }
        } else {
            if(permissions.length > 0 ) {
                if (ActivityCompat.shouldShowRequestPermissionRationale(_thisActivity, permissions[0])) {
                    onPermissionDenied(requestCode);
                } else {
                    onPermissionDeniedNeverAskAgain(requestCode);
                }
            }
        }
    }

    public void onPermissionDeniedNeverAskAgain(int requestCode) {
        String message = PermissionUtils.permissionDeniedMessage(requestCode, _thisActivity);
        PermissionUtils.permissionDeniedDailogueForNeverAskAgain(_thisActivity, message);

    }


    public void onPermissionDenied(int requestCode) {
        String message = PermissionUtils.permissionDeniedMessage(requestCode, _thisActivity);
        PermissionUtils.permissionDeniedDailogue(_thisActivity, message);
    }
}
