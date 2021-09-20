package com.montunosoftware.pillpopper.android;

import android.Manifest;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;

import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.kotlin.bannerCard.GenericBannerFragment;
import com.montunosoftware.pillpopper.model.genericCardAndBanner.AnnouncementsItem;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Observer;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.recyclerview.widget.DefaultItemAnimator;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.SimpleItemAnimator;

import org.json.JSONException;
import org.json.JSONObject;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.ttg.R;
import org.kp.tpmg.ttg.RefillRuntimeData;
import org.kp.tpmg.ttg.RxRefillConstants;
import org.kp.tpmg.ttg.database.RxRefillDBHandler;
import org.kp.tpmg.ttg.utils.LocationHandlerNetworkOrGPS;
import org.kp.tpmg.ttg.utils.RxPermissionUtils;
import org.kp.tpmg.ttg.utils.RxRefillLoggerUtils;
import org.kp.tpmg.ttg.utils.RxRefillUtils;
import org.kp.tpmg.ttg.views.RxRefillBaseFragment;
import org.kp.tpmg.ttg.views.pharmacylocator.model.PharmacyLocatorObj;
import org.kp.tpmg.ttg.views.pharmacylocator.presentor.PharmacyLocatorPresentorImplemention;
import org.kp.tpmg.ttg.views.pharmacylocator.views.PharmacyLocatorListInterface;

import java.io.Serializable;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import static java.lang.Math.round;
import static org.kp.tpmg.ttg.RxRefillConstants.BROADCAST_REFRESH_PHARMACY_FOR_ORDER;
import static org.kp.tpmg.ttg.RxRefillConstants.BROADCAST_REFRESH_PHARMACY_LIST;
import static org.kp.tpmg.ttg.RxRefillConstants.PHARMACY_LAUNCH_MODE_PICK_UP;

/**
 * Created by M1023050 on 28-Nov-18.
 */


public class PharmacyLocatorFragment extends RxRefillBaseFragment implements PharmacyLocatorListInterface, PharmacyLocatorListAdapter.OnItemClickListener {

    private RecyclerView mLocatorListRecyclerView;
    private PharmacyLocatorPresentorImplemention pharmacyLocatorPresentor;
    private EditText mLocatorSearchEditText;
    private PharmacyLocatorListAdapter mPharmacyLocatorListAdapter;
    private Context mContext;
    private boolean isLocationAllowed;
    private LocationHandlerNetworkOrGPS mLocationHandler;
    private Location mUserLocation;
    private static final double MILES_IN_METERS = 0.00062137119;
    private String launchMode;
    private ImageView mClearSearch;
    private Toolbar mToolbar;

    MutableLiveData<String> mPharmacyId = new MutableLiveData<>();
    private List<PharmacyLocatorObj> mPharmacyLocatorList;

    private onPharmacyListItemClickListener callBackListener;
    private View view;

    public interface onPharmacyListItemClickListener {
        void onPharmacyItemClicked(Bundle extras);
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mContext = getActivity();
        pharmacyLocatorPresentor = new PharmacyLocatorPresentorImplemention(mContext, this);
        setHasOptionsMenu(false);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.rx_pharmacy_locator, container, false);
        initUI(view);
        setupRecyclerView();
        loadData();
        setBannerLayout();
        initBroadcastReceiver();
        FireBaseAnalyticsTracker.getInstance().logScreenEvent(getActivity(), FireBaseConstants.ScreenEvent.SCREEN_FIND_PHARMACY);
        return view;
    }

    private void setBannerLayout() {
        List<AnnouncementsItem> bannerToShow = Util.getGenericBannerList(getContext(),FireBaseConstants.ScreenEvent.SCREEN_FIND_PHARMACY);
        if (null != bannerToShow && !bannerToShow.isEmpty()) {
            Bundle bundle = new Bundle();
            bundle.putSerializable("bannerData", (Serializable) bannerToShow);
            Fragment fragment = new GenericBannerFragment();
            fragment.setArguments(bundle);
            FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
            fragmentTransaction.add(com.montunosoftware.mymeds.R.id.banner_container, fragment);
            fragmentTransaction.commit();
            view.findViewById(com.montunosoftware.mymeds.R.id.banner_container).setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        setObserver();
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            if (getActivity() instanceof onPharmacyListItemClickListener) {
                callBackListener = (onPharmacyListItemClickListener) getActivity();
            } else {
                RxRefillLoggerUtils.error(getActivity().getLocalClassName() + " must implement onPharmacyListItemClickListener");
            }
        } catch (Exception ex) {
            RxRefillLoggerUtils.error(getActivity().getLocalClassName() + " must implement onPharmacyListItemClickListener");
        }

    }

    /**
     * Clears the SearchBox hint once we enter into the screen
     */
    private void clearSearchText() {
        if (null != mLocatorSearchEditText && null != mLocatorSearchEditText.getText()) {
            mLocatorSearchEditText.getText().clear();
        }
    }

    private void initBroadcastReceiver() {
        LocalBroadcastManager.getInstance(getActivity()).registerReceiver(refreshPharmacyList,
                new IntentFilter(BROADCAST_REFRESH_PHARMACY_LIST));
    }

    private BroadcastReceiver refreshPharmacyList = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            pharmacyLocatorPresentor.getPharmacyLocatorList(launchMode);
        }
    };

    private void loadData() {
        mContext = getActivity();
        mToolbar.setVisibility(View.GONE);
        if (null != getArguments() && null != getArguments().getString(RxRefillConstants.BUNDLE_KEY_LAUNCH_MODE)) {
            launchMode = getArguments().getString(RxRefillConstants.BUNDLE_KEY_LAUNCH_MODE);
            if (!RefillRuntimeData.isIsPharmacyFromHomeFragment()) {
                if (launchMode.equalsIgnoreCase(PHARMACY_LAUNCH_MODE_PICK_UP)) {
                    initToolBar();
                } else {
                    mToolbar.setVisibility(View.GONE);
                }
            }
        }
        RefillRuntimeData.getInstance().setRequestPermissionFragment(PharmacyLocatorFragment.this);
        if (RxPermissionUtils.checkRuntimePermission(RxRefillConstants.PERMISSION_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION, mContext)) {
            isLocationAllowed = true;
            pharmacyLocatorPresentor.checkDepartmentExist();

        }
        clearSearchText();
    }

    private void initToolBar() {
        mToolbar.setVisibility(View.VISIBLE);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle("Find a Pharmacy");
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    private void setupRecyclerView() {
        mLocatorListRecyclerView.setHasFixedSize(true);
        RecyclerView.ItemAnimator animator = mLocatorListRecyclerView.getItemAnimator();
        if (animator instanceof SimpleItemAnimator) {
            ((SimpleItemAnimator) animator).setSupportsChangeAnimations(false);
        }
        mLocatorListRecyclerView.setLayoutManager(new LinearLayoutManager(getActivity()));
        mLocatorListRecyclerView.setItemAnimator(new DefaultItemAnimator());
    }

    private void initUI(View view) {
        mToolbar = view.findViewById(R.id.app_bar);
        mLocatorSearchEditText = view.findViewById(R.id.editText_pharmacy_search);
        mLocatorListRecyclerView = view.findViewById(R.id.locator_recycler_view);
        mClearSearch = view.findViewById(R.id.btn_clear);
        mClearSearch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mClearSearch.setVisibility(View.GONE);
                mLocatorSearchEditText.setText("");
                if (null != mPharmacyLocatorListAdapter
                        && null != mPharmacyLocatorList
                        && mPharmacyLocatorList.size() > 0) {
                    mPharmacyLocatorListAdapter.getFilter().filter(mLocatorSearchEditText.getText().toString());
                }
            }
        });
        mLocatorSearchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }

            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
                if (null != mPharmacyId) {
                    mPharmacyId.postValue(charSequence.toString());
                }
            }

            @Override
            public void afterTextChanged(Editable editable) {

            }
        });
    }

    private void setObserver() {
        if (null != mPharmacyId) {
            mPharmacyId.observe(this, new Observer<String>() {
                @Override
                public void onChanged(@Nullable String s) {
                    validateSearch();
                    if (null != mPharmacyLocatorListAdapter && null != mPharmacyLocatorListAdapter.getFilter()) {
                        mPharmacyLocatorListAdapter.getFilter().filter(s);
                    }
                }
            });
        }
    }

    private void validateSearch() {
        if (mLocatorSearchEditText.getText().length() > 0) {
            mClearSearch.setVisibility(View.VISIBLE);
        } else {
            mClearSearch.setVisibility(View.GONE);
        }
    }

    @Override
    public void getPharmacyLocatorList(List<PharmacyLocatorObj> pharmacyLocatorList) {

        if (null != pharmacyLocatorList && !pharmacyLocatorList.isEmpty()) {
                mLocationHandler = new LocationHandlerNetworkOrGPS(mContext);
                // check if GPS enabled
                if (mLocationHandler.canGetLocation()) {
                    mUserLocation = mLocationHandler.getmLocation();
                }
                if (null != mUserLocation) {
                    for (PharmacyLocatorObj pharmacyObj : pharmacyLocatorList) {
                        calculateDistance(pharmacyObj);
                    }
                }
                loadAdapter(pharmacyLocatorList);
                mPharmacyLocatorList = pharmacyLocatorList;
        } else {
            RxRefillDBHandler.getInstance(getContext()).copyLocalDB();
            pharmacyLocatorPresentor.getPharmacyLocatorList(launchMode);
        }
    }

    @Override
    public void isDepartmentCountEmpty(boolean isDbInterupted) {
        RxRefillLoggerUtils.info(isDbInterupted + "");
        if (isDbInterupted) {
            FireBaseAnalyticsTracker.getInstance().logEvent(getContext(),
                    FireBaseConstants.Event.PHARMACY_BUNDLED_DB_USED,
                    FireBaseConstants.ParamName.REASON,
                    FireBaseConstants.ParamValue.DB_HAS_NO_RECORD);
            RxRefillDBHandler.getInstance(getContext()).copyLocalDB();
        }
        pharmacyLocatorPresentor.getPharmacyLocatorList(launchMode);
    }

    private void loadAdapter(List<PharmacyLocatorObj> pharmacyLocatorList) {
        if (isLocationAllowed) {
            Collections.sort(pharmacyLocatorList, sortByDistance);
        }
        Collections.sort(pharmacyLocatorList, sortByPreferredFacility);
        mPharmacyLocatorListAdapter = new PharmacyLocatorListAdapter(mContext, pharmacyLocatorList, this);

        mLocatorListRecyclerView.setAdapter(mPharmacyLocatorListAdapter);

        if (mLocatorSearchEditText.getText().toString().length() > 0) {
            mPharmacyId.postValue(mLocatorSearchEditText.getText().toString());
        }
    }


    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (grantResults.length > 0
                && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            // permission was granted,
            if (requestCode == RxRefillConstants.PERMISSION_LOCATION) {
                isLocationAllowed = true;
                pharmacyLocatorPresentor.getPharmacyLocatorList(launchMode);
            }
        } else {
            //permission Denied
            if (permissions.length > 0) {
                if (ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext, permissions[0])) {
                    if (requestCode == RxRefillConstants.PERMISSION_LOCATION) {
                        isLocationAllowed = false;
                        onPermissionDenied(requestCode);

                    }
                } else {
                    if (requestCode == RxRefillConstants.PERMISSION_LOCATION) {
                        isLocationAllowed = false;
                        onPermissionDeniedNeverAskAgain(requestCode);
                    }
                }
            }
            pharmacyLocatorPresentor.getPharmacyLocatorList(launchMode);
        }

    }

    public void onPermissionDeniedNeverAskAgain(int requestCode) {
        String message = RxPermissionUtils.permissionDeniedMessage(requestCode, mContext);
        RxPermissionUtils.permissionDeniedDialogForNeverAskAgain(mContext, message);
    }

    public void onPermissionDenied(int requestCode) {
        String message = RxPermissionUtils.permissionDeniedMessage(requestCode, mContext);
        RxPermissionUtils.permissionDeniedDialog(mContext, message);
    }

    /* Alert Dialog positive Listeners */
    private final android.content.DialogInterface.OnClickListener pAlertListener = (dialog, which) -> dialog.dismiss();


    private void calculateDistance(PharmacyLocatorObj pharmacyLocatorObj) {

        Location pharmacyLocation = new Location("pharmacyLocation");
        pharmacyLocation.setLatitude(pharmacyLocatorObj.getLatitude());
        pharmacyLocation.setLongitude(pharmacyLocatorObj.getLongitude());

        // Convert the metres to miles.
        double distance = (mUserLocation.distanceTo(pharmacyLocation) * MILES_IN_METERS);
        double distanceInMiles = round(distance * 100.0) / (100.0); //adjust decimal to two places.
        pharmacyLocatorObj.setDistance(distanceInMiles);

    }

    public static Comparator<PharmacyLocatorObj> sortByDistance = (p1, p2) -> {
        if (null == p1.getDistance() || p2.getDistance() == null) {
            return -1;
        }
        return p1.getDistance() < p2.getDistance() ? -1 : p1.getDistance() > p2.getDistance() ? 1 : 0;
    };

    public static Comparator<PharmacyLocatorObj> sortByPreferredFacility = (p1, p2) -> p2.getIsPreferredFacility() - p1.getIsPreferredFacility();

    @Override
    public void onItemClick(PharmacyLocatorObj pharmacyLocatorObj) {
        RxRefillUtils.hideSoftKeyboard(getActivity());
        // clearSearchText();
        Fragment fragment = new PharmacyDetailFragment();
        Bundle bundle = new Bundle();
        bundle.putParcelable(RxRefillConstants.BUNDLE_KEY_PHARMACY_OBJ, pharmacyLocatorObj);
        bundle.putBoolean(RxRefillConstants.BUNDLE_KEY_IS_LOCATION_SERVICE_AVAILABLE, isLocationAllowed);
        if (null != launchMode) {
            bundle.putString(RxRefillConstants.BUNDLE_KEY_LAUNCH_MODE, launchMode);
        }
        mLocationHandler = new LocationHandlerNetworkOrGPS(mContext);
        if (mLocationHandler.canGetLocation()) {
            mUserLocation = mLocationHandler.getmLocation();
        }
        if (null != mUserLocation) {
            bundle.putDouble(RxRefillConstants.BUNDLE_KEY_USER_LATITUDE, mUserLocation.getLatitude());
            bundle.putDouble(RxRefillConstants.BUNDLE_KEY_USER_LONGITUDE, mUserLocation.getLongitude());
        }

        if (null != launchMode && launchMode.equalsIgnoreCase(PHARMACY_LAUNCH_MODE_PICK_UP)) {
            fragment.setArguments(bundle);
            changeFragment(fragment);
        } else {
            callBackListener.onPharmacyItemClicked(bundle);
        }
    }

    private void changeFragment(Fragment fragment) {
        FragmentTransaction fragmentTransaction = getActivity().getSupportFragmentManager().beginTransaction();
        fragmentTransaction.setCustomAnimations(R.anim.slide_right_in, R.anim.slide_left_out);
        fragmentTransaction.add(R.id.fragment_container, fragment, fragment.getTag());
        fragmentTransaction.hide(PharmacyLocatorFragment.this);
        fragmentTransaction.addToBackStack(PharmacyLocatorFragment.class.getName());
        fragmentTransaction.commit();
    }

    @Override
    public void onDestroy() {
        RxRefillUtils.hideSoftKeyboard(getActivity());
        try {
            LocalBroadcastManager.getInstance(getActivity()).unregisterReceiver(refreshPharmacyList);
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(BROADCAST_REFRESH_PHARMACY_FOR_ORDER));
        } catch (Exception e) {
            RxRefillLoggerUtils.exception(e.getMessage());
        }
        super.onDestroy();
    }

}
