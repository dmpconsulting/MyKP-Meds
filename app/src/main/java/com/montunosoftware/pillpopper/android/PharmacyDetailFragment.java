package com.montunosoftware.pillpopper.android;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Typeface;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.montunosoftware.pillpopper.android.util.Util;

import org.kp.tpmg.ttg.R;
import org.kp.tpmg.ttg.RefillRuntimeData;
import org.kp.tpmg.ttg.RxRefillConstants;
import org.kp.tpmg.ttg.analytics.RxFireBaseAnalyticsTracker;
import org.kp.tpmg.ttg.analytics.RxFireBaseConstants;
import org.kp.tpmg.ttg.controller.RxRefillController;
import org.kp.tpmg.ttg.model.shoppingcart.RxRefillShoppingCartItem;
import org.kp.tpmg.ttg.model.shoppingcart.ShoppingCartItemsViewModel;
import org.kp.tpmg.ttg.utils.LocationHandlerNetworkOrGPS;
import org.kp.tpmg.ttg.utils.RxPermissionUtils;
import org.kp.tpmg.ttg.utils.RxRefillLoggerUtils;
import org.kp.tpmg.ttg.utils.RxRefillUtils;
import org.kp.tpmg.ttg.views.RxRefillBaseFragment;
import org.kp.tpmg.ttg.views.RxRefillTransparentLoadingActivity;
import org.kp.tpmg.ttg.views.pharmacylocator.model.PharmacyContactObj;
import org.kp.tpmg.ttg.views.pharmacylocator.model.PharmacyLocatorObj;
import org.kp.tpmg.ttg.views.pharmacylocator.presentor.PharmacyDetailPresenterInterface;
import org.kp.tpmg.ttg.views.pharmacylocator.presentor.PharmacyDetailPresentorImplemention;
import org.kp.tpmg.ttg.views.pharmacylocator.views.PharmacyDetailInterface;
import org.kp.tpmg.ttg.views.trialclaims.model.TrialClaimRootResponse;
import org.kp.tpmg.ttg.views.trialclaims.presentor.TrialClaimsPresentorImplementation;
import org.kp.tpmg.ttg.views.trialclaims.view.GetPrescriptionsCostInformationInterface;
import org.kp.tpmg.ttg.views.trialclaims.view.TrialClaimsInterface;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.kp.tpmg.ttg.RxRefillConstants.BROADCAST_REFRESH_PHARMACY_LIST;
import static org.kp.tpmg.ttg.RxRefillConstants.REFILLABLE_ONLINE_YES;
import static org.kp.tpmg.ttg.RxRefillConstants.TEMPORARILY_CLOSED_NO;
import static org.kp.tpmg.ttg.RxRefillConstants.TEMPORARILY_CLOSED_YES;
import static org.kp.tpmg.ttg.analytics.RxFireBaseConstants.Event.RX_TRIAL_CLAIM_SUCCESS;

public class PharmacyDetailFragment extends RxRefillBaseFragment implements View.OnClickListener, GoogleMap.OnMarkerClickListener, GoogleMap.OnMarkerDragListener, GoogleMap.OnMapClickListener, OnMapReadyCallback, PharmacyDetailInterface, TrialClaimsInterface, GetPrescriptionsCostInformationInterface {

    private TextView mPharmacyName;
    private TextView mDepartment;
    private TextView mStreet;
    private TextView mCityStateZip;
    private TextView mGetDirections;
    private TextView mDistanceTextView;
    private Bundle mBundle;
    private TextView mFormattedHours;
    private TextView mHoursHeader;
    private TextView mContactHeader;
    private TextView mPharmacyStateInfo;

    private Button mPickUpPharmacyButton;

    private CheckBox mCheckBoxIsPreferredFacility;

    private FrameLayout mPickUpPharmacyButtonLayout;
    private RelativeLayout mIsPreferredFacilityLayout;
    private RelativeLayout mIsRefillableOnlineLayout;
    private LinearLayout mContactInformation;

    private Bundle bundle;
    private PharmacyLocatorObj mPharmacyLocatorObj;
    private MapView mFacilityMapView;
    private GoogleMap mGoogleMap;
    private String mMapUrlStr;
    private Context mContext;
    private Toolbar mToolbar;
    private boolean mIsLocationServiceAvailable;

    // For Testing
    /*private double mCurrentLocationLat = 37.951890;
    private double mCurrentLocationLan = -121.776278;*/

    private double mCurrentLocationLat;
    private double mCurrentLocationLan;

    private Location mUserLocation;
    private LocationHandlerNetworkOrGPS mLocationHandler;

    //Map Fragment latLng constants
    private static final double DEFAULT_LAT_AND_LONG = 0.00;
    private static final double DEFAULT_LATITUDE = 37.951890;
    private static final double DEFAULT_LONGITUDE = -121.776278;
    private static final double MILES_IN_METERS = 0.00062137119;

    private Typeface mFontRegular;
    private Typeface mFontMedium;
    private String mLaunchMode;

    private PharmacyDetailPresenterInterface pharmacyDetailPresentor;
    private boolean valueChanged = false;
    private int isPreferredFacilityValueOnLoad;
    private RefillRuntimeData mRuntime;
    private boolean isInBackGround;
    private boolean mTransactionPending;
    private static AlertDialog mDialog;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setHasOptionsMenu(false);
        mRuntime = RefillRuntimeData.getInstance();
        if (null != getArguments()) {
            mBundle = getArguments();
            mPharmacyLocatorObj = mBundle.getParcelable(RxRefillConstants.BUNDLE_KEY_PHARMACY_OBJ);
            mIsLocationServiceAvailable = mBundle.getBoolean(RxRefillConstants.BUNDLE_KEY_IS_LOCATION_SERVICE_AVAILABLE);
            mCurrentLocationLat = mBundle.getDouble(RxRefillConstants.BUNDLE_KEY_USER_LATITUDE);
            mCurrentLocationLan = mBundle.getDouble(RxRefillConstants.BUNDLE_KEY_USER_LONGITUDE);
            mLaunchMode = mBundle.getString(RxRefillConstants.BUNDLE_KEY_LAUNCH_MODE);
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mContext = getActivity();
        View view = inflater.inflate(R.layout.rx_refill_pharmacy_details, container, false);
        initToolBar(view);
        initUI(view, savedInstanceState);
        RxFireBaseAnalyticsTracker.getInstance().logScreenEvent(getActivity(), RxFireBaseConstants.ScreenEvent.SCREEN_PHARMACY_DETAILS);
        return view;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        loadData();
    }

    private void initUI(View view, Bundle savedInstanceState) {
        mFacilityMapView = view.findViewById(R.id.facility_mapView);

        final Bundle mapViewSavedInstanceState = savedInstanceState != null ? savedInstanceState.getBundle("mapViewSaveState") : null;
        mFacilityMapView.onCreate(mapViewSavedInstanceState);

        mFacilityMapView.onResume();// needed to get the map to display immediately

        try {
            MapsInitializer.initialize(getActivity().getApplicationContext());
        } catch (Exception e) {
            RxRefillLoggerUtils.exception("Exception while loading the map : " + e.getMessage());
        }

        mPharmacyName = view.findViewById(R.id.tv_facility_name);
        mDepartment = view.findViewById(R.id.tv_dept_name);
        mStreet = view.findViewById(R.id.tv_street);
        mCityStateZip = view.findViewById(R.id.tv_city_state_zip);
        mFormattedHours = view.findViewById(R.id.working_hours);
        mContactHeader = view.findViewById(R.id.contact_header);
        mHoursHeader = view.findViewById(R.id.hours_header);
        mContactInformation = view.findViewById(R.id.contact_info_layout);
        mPharmacyStateInfo = view.findViewById(R.id.pharmacy_state_info);
        mDistanceTextView = view.findViewById(R.id.tv_distance_text);
        mGetDirections = view.findViewById(R.id.tv_getDirectionsTextView);

        mPickUpPharmacyButtonLayout = view.findViewById(R.id.btn_pick_up_pharmacy_layout);
        mPickUpPharmacyButton = view.findViewById(R.id.btn_pick_up_pharmacy);

        mIsPreferredFacilityLayout = view.findViewById(R.id.rl_is_preferred_facility);
        mIsRefillableOnlineLayout = view.findViewById(R.id.rl_refillable_online);
        mIsRefillableOnlineLayout.setVisibility(View.GONE);

        mCheckBoxIsPreferredFacility = view.findViewById(R.id.check_box_preferred_pharmacy);

        // get contact information
        pharmacyDetailPresentor = new PharmacyDetailPresentorImplemention(getActivity(), this);
        if (null != mPharmacyLocatorObj && !RxRefillUtils.isEmptyString(mPharmacyLocatorObj.getDeptId()))
            pharmacyDetailPresentor.getPharmacyContactInformation(mPharmacyLocatorObj.getDeptId());

        mPickUpPharmacyButton.setOnClickListener(this);

        // initialize and set fonts
        initializeAndSetFonts();

        setupUIElements(view);

    }

    /**
     * this method will set the visibility of UI elements based on the data received from the List screen
     *
     * @param view
     */
    private void setupUIElements(View view) {

        if (null != mPharmacyLocatorObj.getRefillableOnline()
                && null != mPharmacyLocatorObj.getTemporarilyClosed()
                && mPharmacyLocatorObj.getRefillableOnline().equals(REFILLABLE_ONLINE_YES)
                && mPharmacyLocatorObj.getTemporarilyClosed().equals(TEMPORARILY_CLOSED_NO)) {
            //make the mPickUpPharmacyButtonLayout visible in case of checkout flow
            if (null != mLaunchMode && mLaunchMode.equalsIgnoreCase("pickUpFragment")) {
                mPickUpPharmacyButtonLayout.setVisibility(View.VISIBLE);
                view.findViewById(R.id.emptyView).setVisibility(View.VISIBLE);
            } else {
                mPickUpPharmacyButtonLayout.setVisibility(View.GONE);
                view.findViewById(R.id.emptyView).setVisibility(View.GONE);
            }
            mIsPreferredFacilityLayout.setVisibility(View.VISIBLE);
        } else {
            mPickUpPharmacyButtonLayout.setVisibility(View.GONE);
            view.findViewById(R.id.emptyView).setVisibility(View.GONE);
            mIsRefillableOnlineLayout.setVisibility(View.VISIBLE);
            mIsPreferredFacilityLayout.setVisibility(View.GONE);
            if (mPharmacyLocatorObj.getTemporarilyClosed().equals(TEMPORARILY_CLOSED_YES)) {
                mPharmacyStateInfo.setText(getString(R.string.pharmacy_temporarily_closed));
            } else {
                mPharmacyStateInfo.setText(getString(R.string.pharmacy_not_accepting_orders));
            }
        }

        mCheckBoxIsPreferredFacility.setChecked(mPharmacyLocatorObj.getIsPreferredFacility() == 1);

        mCheckBoxIsPreferredFacility.setOnCheckedChangeListener((compoundButton, checked) -> {
            // update isPreferredFacility State in DB
            mCheckBoxIsPreferredFacility.setContentDescription(checked ? mContext.getString(R.string.content_description_remove_pharmacy) :
                    mContext.getString(R.string.content_description_add_pharmacy));

            if (null != mPharmacyLocatorObj && !RxRefillUtils.isEmptyString(mPharmacyLocatorObj.getDeptId()))
                RxRefillController.getInstance(getActivity()).updateIsPreferredFacility(getActivity(), mPharmacyLocatorObj.getDeptId(), checked ? 1 : 0);
            if (isPreferredFacilityValueOnLoad != (checked ? 1 : 0)) {
                valueChanged = true;
            }
        });

        if (null != mLaunchMode && mLaunchMode.equalsIgnoreCase("pickUpFragment")) {
            mGetDirections.setVisibility(View.GONE);
        } else {
            mGetDirections.setVisibility(View.VISIBLE);
            mGetDirections.setOnClickListener(this);
        }
    }

    /**
     * initialize fonts and set typeface for UI elements
     */
    private void initializeAndSetFonts() {
        mFontRegular = RxRefillUtils.setFontStyle(getActivity(), RxRefillConstants.FONT_ROBOTO_REGULAR);
        mFontMedium = RxRefillUtils.setFontStyle(getActivity(), RxRefillConstants.FONT_ROBOTO_MEDIUM);

        mHoursHeader.setTypeface(mFontMedium);
        mContactHeader.setTypeface(mFontMedium);
        mGetDirections.setTypeface(mFontMedium);
        mFormattedHours.setTypeface(mFontRegular);
        mPharmacyStateInfo.setTypeface(mFontRegular);
        mDistanceTextView.setTypeface(mFontRegular);
    }

    private void initToolBar(View view) {
        mToolbar = view.findViewById(R.id.app_bar);
        ((AppCompatActivity) getActivity()).setSupportActionBar(mToolbar);
        ((AppCompatActivity) getActivity()).getSupportActionBar().setTitle(getString(R.string.pharmacy_details_title));
        ((AppCompatActivity) getActivity()).getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    @Override
    public void onStart() {
        super.onStart();
        mFacilityMapView.getMapAsync(this);
        if (mGoogleMap != null) {
            mGoogleMap.clear();
        }
        initializeMapIfNeeded();
    }

    private void initializeMapIfNeeded() {
        if (mFacilityMapView != null) {
            mFacilityMapView.getMapAsync(this);
        }
        initializeMap();
    }

    /**
     * initialize the map
     */
    private void initializeMap() {
        if (mFacilityMapView != null && null != mGoogleMap) {
            // Hide the zoom controls as the button panel will cover it.
            mGoogleMap.getUiSettings().setZoomControlsEnabled(false);
            // Disable the two finger zoom functionality.
            mGoogleMap.getUiSettings().setZoomGesturesEnabled(false);
            // Add marker to the map.
            addMarkersToMap();
            mGoogleMap.setOnMarkerClickListener(this);
            //mGoogleMap.setOnInfoWindowClickListener(this);
            mGoogleMap.setOnMarkerDragListener(this);
            //mGoogleMap.getUiSettings().setMapToolbarEnabled(this);
            mGoogleMap.setOnMapClickListener(this);
            // Pan to see all markers in view.
            // Cannot zoom to bounds until the map has a size.
            final View mapView = getView();

            if (mapView != null && mapView.getViewTreeObserver().isAlive()) {
                mapView.getViewTreeObserver().addOnGlobalLayoutListener(
                        new ViewTreeObserver.OnGlobalLayoutListener() {
                            @Override
                            public void onGlobalLayout() {
                                if (null != mPharmacyLocatorObj && mPharmacyLocatorObj.getLongitude() != 0 && mPharmacyLocatorObj.getLatitude() != 0) {
                                    mGoogleMap.moveCamera(CameraUpdateFactory
                                            .newLatLngZoom(new LatLng(mPharmacyLocatorObj.getLatitude(), mPharmacyLocatorObj.getLongitude()), 14));
                                    mapView.getViewTreeObserver()
                                            .removeOnGlobalLayoutListener(this);
                                }
                            }
                        });
            }
        }
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        Bundle mapViewSaveState = new Bundle(outState);
        mFacilityMapView.onSaveInstanceState(mapViewSaveState);
        outState.putBundle("mapViewSaveState", mapViewSaveState);
        super.onSaveInstanceState(outState);
    }

    /**
     * adds markers for user current location and pharmacy location
     */
    private void addMarkersToMap() {
        MarkerOptions markerOptions;
        LatLng latLong = new LatLng(mPharmacyLocatorObj.getLatitude(), mPharmacyLocatorObj.getLongitude());
        BitmapDescriptor bitmapMarker = BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_RED);
        markerOptions = new MarkerOptions().position(latLong)
                .title(prepareMarkerTitle()) // After discussion, decided to display the address for marker title, if no address then go for geoCoding.
                .icon(bitmapMarker);

        mGoogleMap.addMarker(markerOptions);

        // create class object
        mLocationHandler = new LocationHandlerNetworkOrGPS(mContext);

        // check if GPS enabled
        if (mLocationHandler.canGetLocation()) {
            mUserLocation = mLocationHandler.getmLocation();
        }

        if (mUserLocation == null || (mUserLocation.getLatitude() == DEFAULT_LAT_AND_LONG && mUserLocation.getLongitude() == DEFAULT_LAT_AND_LONG)) {
            mUserLocation = new Location("user location");
            mUserLocation.setLatitude(DEFAULT_LATITUDE);
            mUserLocation.setLongitude(DEFAULT_LONGITUDE);
        }

        //Adding UserLocation marker only if the Location Service is Available
        if (mIsLocationServiceAvailable) {
            mGoogleMap.addMarker(new MarkerOptions().position(new LatLng(mUserLocation.getLatitude(), mUserLocation.getLongitude())).title("Current Location").icon(BitmapDescriptorFactory
                    .defaultMarker(BitmapDescriptorFactory.HUE_BLUE)));
            Location near_locations = new Location("near by location");
            near_locations.setLatitude(mPharmacyLocatorObj.getLatitude());
            near_locations.setLongitude(mPharmacyLocatorObj.getLongitude());
            double distance = (mUserLocation.distanceTo(near_locations) * MILES_IN_METERS);
            RxRefillLoggerUtils.info("DISTANCE : " + (distance * MILES_IN_METERS));
            if (distance < 25) {
                mGoogleMap.addMarker(markerOptions);
            }
            /*mGoogleMap.moveCamera(CameraUpdateFactory.newLatLng(latLong));
            mGoogleMap.animateCamera(CameraUpdateFactory.zoomTo(4));*/
            mGoogleMap.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(mPharmacyLocatorObj.getLatitude(), mPharmacyLocatorObj.getLongitude()), 14));
            mGoogleMap.animateCamera(CameraUpdateFactory.newLatLng(latLong));
        }
    }

    /**
     * Prepares the title for marker if we have the proper street and
     *
     * @return
     */
    private String prepareMarkerTitle() {
        StringBuilder titleBuilder = new StringBuilder();
        String street = mPharmacyLocatorObj.getStreet();
        if (!RxRefillUtils.isEmptyString(street)) {
            titleBuilder.append(street);
            if (!street.endsWith(".")) {
                titleBuilder.append(", ");
            }
        }
        if (!RxRefillUtils.isEmptyString(mPharmacyLocatorObj.getCityStateAndZip())) {
            titleBuilder.append(mPharmacyLocatorObj.getCityStateAndZip());
        }
        return (!RxRefillUtils.isEmptyString(titleBuilder.toString())) ? titleBuilder.toString()
                : RxRefillUtils.getAddressesFromLatLan(mContext, mPharmacyLocatorObj.getLatitude(), mPharmacyLocatorObj.getLongitude());
    }

    /**
     * load UI elements with data from list
     */
    private void loadData() {
        if (null != mPharmacyLocatorObj) {
            mPharmacyName.setText(mPharmacyLocatorObj.getOfficialName());
            mDepartment.setText(mPharmacyLocatorObj.getDepartmentName());
            mStreet.setText(mPharmacyLocatorObj.getStreet());
            mCityStateZip.setText(mPharmacyLocatorObj.getCityStateAndZip());
            mFormattedHours.setText(getFormattedHours(mPharmacyLocatorObj.getFormattedHours()));
            mDistanceTextView.setText(prepareDistanceText());
            isPreferredFacilityValueOnLoad = mPharmacyLocatorObj.getIsPreferredFacility();
        }
    }

    /**
     * @param formattedHours
     * @return working hours in desired format. Replaces "through" and "to" with "-"
     */
    private String getFormattedHours(String formattedHours) {
        if (!TextUtils.isEmpty(formattedHours)) {
            String result = formattedHours;
            if (result.contains("through")) {
                result = result.replace("through", " - ");
            }
            if (result.contains("to")) {
                result = result.replace("to", " - ");
            }
            return result;
        } else {
            return "";
        }
    }

    @Override
    public void getPharmacyContactInformation(List<PharmacyContactObj> pharmacyContactList) {
        if (null != pharmacyContactList) {
            for (PharmacyContactObj contactObj : pharmacyContactList) {
                inflateDynamicView(contactObj);
            }
        }
    }

    /**
     * @param contactObj adds a layout to the contact information layout pharmacy name and contact number
     */
    private void inflateDynamicView(PharmacyContactObj contactObj) {
        LayoutInflater layoutInflater = getLayoutInflater();
        View childView = layoutInflater.inflate(R.layout.pharmacy_contact_info_layout, null);
        TextView tvContactName = childView.findViewById(R.id.contact_name);
        TextView tvContactNumber = childView.findViewById(R.id.contact_number);
        tvContactName.setTypeface(mFontRegular);
        tvContactNumber.setTypeface(mFontRegular);
        tvContactName.setText(contactObj.getContactName());

        // SpannableString formattedPhoneNumber = new SpannableString(RxRefillUtils.formatPhoneNumber(contactObj.getContactNumber()));
        //formattedPhoneNumber.setSpan(new UnderlineSpan(), 0, formattedPhoneNumber.length(), 0);
        // If the underline for phone number is required enable the above 3 lines and disable below line.
        //tvContactNumber.setText(formattedPhoneNumber);

        tvContactNumber.setText(RxRefillUtils.formatPhoneNumber(contactObj.getContactNumber()));
        mContactInformation.addView(childView);

        tvContactNumber.setOnClickListener(view -> {
            RxFireBaseAnalyticsTracker.getInstance().logEvent(getActivity(),
                    RxFireBaseConstants.Event.CALL,
                    RxFireBaseConstants.ParamName.SOURCE,
                    RxFireBaseConstants.ParamValue.PHARMACY_DETAILS);
            makeCall(RxRefillUtils.formatPhoneNumber(((TextView) view).getText().toString()), getActivity(), getString(R.string.call_pharmacy_title));
        });
    }

    /**
     * prepares the distance text in miles in different scenarios
     *
     * @return
     */
    private String prepareDistanceText() {
        // If no Location Permission, hide the Distance TextView.
        if (!mIsLocationServiceAvailable) {
            mDistanceTextView.setVisibility(View.GONE);
            return "";
        }

        if (null != mPharmacyLocatorObj) {
            mDistanceTextView.setVisibility(View.VISIBLE);
            //One of the edge scenario, Location service is available and lat lan values are empty/null. We will use reverse geoCoding and get the lat lan
            if (mPharmacyLocatorObj.getLatitude() == 0 && mPharmacyLocatorObj.getLongitude() == 0) {
                // get the lat lan from from address and calculate the distance.
                List<Double> latLanList = RxRefillUtils.getLatitudeAndLongitudeFromGoogleMapForAddress((mPharmacyLocatorObj.getZip()), mContext);
                if (null != latLanList && !latLanList.isEmpty()) {
                    double distanceFromCurrentLocation = RxRefillUtils.calculateDistanceInMiles(latLanList.get(0), latLanList.get(1), mUserLocation);
                    if (distanceFromCurrentLocation != 0) {
                        return String.valueOf(mPharmacyLocatorObj.getDistance()).concat(" mi");
                    } else {
                        mDistanceTextView.setVisibility(View.GONE);
                    }
                } else {
                    mDistanceTextView.setVisibility(View.GONE);
                    RxRefillLoggerUtils.info("No Latitude and longitude found.");
                }

            } else {
                //Location Service is available and proper lat lan values are present so display the text as it is.
                return null != mPharmacyLocatorObj.getDistance() ? String.format("%.1f", mPharmacyLocatorObj.getDistance()).concat(" mi") : "";
            }
        }
        return "";
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        if (valueChanged) {
            LocalBroadcastManager.getInstance(getActivity()).sendBroadcast(new Intent(BROADCAST_REFRESH_PHARMACY_LIST));
        }
        if(null!=mDialog && mDialog.isShowing()){
            mDialog.dismiss();
        }
    }

    @Override
    public void onMapClick(LatLng latLng) {

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        this.mGoogleMap = googleMap;
        initializeMapIfNeeded();
        mGoogleMap.setOnMarkerClickListener(marker -> false);
    }

    @Override
    public void onClick(View view) {
        // Since we can not use Switch cases in library application code, we are using the conditions.
        if (view.getId() == R.id.tv_getDirectionsTextView) {

            RxFireBaseAnalyticsTracker.getInstance().logEventWithoutParams(getActivity(), RxFireBaseConstants.ScreenEvent.SCREEN_FIND_PHARMACY_DIRECTIONS);

            if (0 == mPharmacyLocatorObj.getLatitude() && 0 == mPharmacyLocatorObj.getLongitude()) {
                String address = RxRefillUtils.getAddressesFromLatLan(getContext(), mCurrentLocationLat, mCurrentLocationLan);
                if (!RxRefillUtils.isEmptyString(address)) {
                    mMapUrlStr = new StringBuffer(
                            RxRefillConstants.URL_GOOGLE_MAPS)
                            .append(address)
                            .append(RxRefillConstants.GOOGLE_MAPS_DADDR)
                            .toString();
                } else {
                    mMapUrlStr = new StringBuffer(
                            RxRefillConstants.URL_GOOGLE_MAPS)
                            .append(mCurrentLocationLat)
                            .append(RxRefillConstants.COMMA_SEPARATOR)
                            .append(mCurrentLocationLan)
                            .append(RxRefillConstants.GOOGLE_MAPS_DADDR)
                            .toString();
                }

            } else {
                mMapUrlStr = new StringBuffer(
                        RxRefillConstants.URL_GOOGLE_MAPS)
                        .append(mCurrentLocationLat)
                        .append(RxRefillConstants.COMMA_SEPARATOR)
                        .append(mCurrentLocationLan)
                        .append(RxRefillConstants.GOOGLE_MAPS_DADDR)
                        .append(mPharmacyLocatorObj.getLatitude())
                        .append(RxRefillConstants.COMMA_SEPARATOR)
                        .append(mPharmacyLocatorObj.getLongitude())
                        .toString();
            }

            invokeDirectionAPI(mMapUrlStr);
        } else if (view.getId() == R.id.btn_pick_up_pharmacy) {
            // when we pop back stack 2 times it will call onDestroy of RxPharmacyLocatorFragment.
            // Since we are sending broadcast from onDestroy of RxPharmacyLocatorFragment that will update the UI in the Delivery/pickup screen
            mRuntime.setDeliveryByUSMailSelected(false);
            mRuntime.setSaveClicked(true);
            RefillRuntimeData.getInstance().setPickUpAtPharmacyObject(mPharmacyLocatorObj);
            RefillRuntimeData.getInstance().setSaveClicked(true);
            startProgress();

            initAPITrialClaimsCall();
        }
    }

    private void startProgress() {
        if (null != getActivity()) {
            getActivity().startActivityForResult(new Intent(getActivity(), RxRefillTransparentLoadingActivity.class), 0);
        }
    }

    /**
     * Launches the google maps for directions.
     */
    private void invokeDirectionAPI(String url) {
        if (RxRefillUtils.isEmptyString(url)) {
            return;
        }
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setClassName(RxRefillConstants.GOOGLE_ANDROID_APPS_MAPS,
                RxRefillConstants.GOOGLE_ANDROID_MAPS_ACTIVITY);
        try {
            startActivity(intent);
        } catch (Exception e) {
            RxRefillLoggerUtils.exception("Can't load the view");
        }
    }

    @Override
    public boolean onMarkerClick(Marker marker) {
        return false;
    }

    @Override
    public void onMarkerDragStart(Marker marker) {

    }

    @Override
    public void onMarkerDrag(Marker marker) {

    }

    @Override
    public void onMarkerDragEnd(Marker marker) {

    }

    private ShoppingCartItemsViewModel shoppingCartItemsViewModel;
    private TrialClaimsPresentorImplementation trialClaimsPresentorInterface;

    public void initAPITrialClaimsCall() {
        TrialClaimsInterface mCallback;
        mCallback = this;
        shoppingCartItemsViewModel = RefillRuntimeData.getInstance().getShoppingCartItemsViewModel(getActivity());
        trialClaimsPresentorInterface = new TrialClaimsPresentorImplementation(getActivity(), mCallback);
        trialClaimsPresentorInterface.invokeTrialClaimsAPI(RxRefillConstants.TRIAL_CLAIM_INIT_MODE, shoppingCartItemsViewModel.getRxRefillShoppingCartItems().getValue());
    }

    @Override
    public void onTrialClaimsAPISuccess(String mode, TrialClaimRootResponse trialClaimRootResponse) {
        RxFireBaseAnalyticsTracker.getInstance().logEventWithoutParams(getActivity(), RX_TRIAL_CLAIM_SUCCESS);
        if (mode.equalsIgnoreCase(RxRefillConstants.TRIAL_CLAIM_INIT_MODE)) {
            RxRefillLoggerUtils.info("Trial claims Init Success");
            trialClaimsPresentorInterface.invokeTrialClaimsAPI(RxRefillConstants.TRIAL_CLAIM_FETCH_MODE, shoppingCartItemsViewModel.getRxRefillShoppingCartItems().getValue());
        } else if (mode.equalsIgnoreCase(RxRefillConstants.TRIAL_CLAIM_FETCH_MODE)) {
            RxRefillLoggerUtils.info("Trial claims Fetch Success");
            mRuntime = RefillRuntimeData.getInstance();
            mRuntime.setTrailClaimResponse(trialClaimRootResponse);
            loadOrderReviewScreen();
        }
    }

    @Override
    public void onTrialClaimsAPIFailure(String mode) {
        if (mode.equalsIgnoreCase(RxRefillConstants.TRIAL_CLAIM_INIT_MODE)) {
            RxRefillLoggerUtils.info("Trial claims Init failure");
            trialClaimsPresentorInterface.invokeTrialClaimsAPI(RxRefillConstants.TRIAL_CLAIM_FETCH_MODE, shoppingCartItemsViewModel.getRxRefillShoppingCartItems().getValue());
        } else if (mode.equalsIgnoreCase(RxRefillConstants.TRIAL_CLAIM_FETCH_MODE)) {
            RxRefillLoggerUtils.info("Trial claims Fetch failure");
            loadOrderReviewScreen();
        }
    }

    private void loadOrderReviewScreen() {
        dismissProgress();
        GetPrescriptionsCostInformationInterface mCallback;
        mCallback = this;
        trialClaimsPresentorInterface = new TrialClaimsPresentorImplementation(getActivity(), mCallback);
        trialClaimsPresentorInterface.getPrescriptionsCostInformation();
        RefillRuntimeData.getInstance().setUserNameOnChangeClick(null);
    }

    @Override
    public void updatePrescriptionsCostInformation(HashMap<String, String> rxCostMap) {
        try {
            dismissProgress();
            List<RxRefillShoppingCartItem> mShoppingCartItems = shoppingCartItemsViewModel.getRxRefillShoppingCartItems().getValue();
            RxRefillUtils.prepareCartItemModelWithPrice(rxCostMap, mShoppingCartItems, getActivity());
        } catch (Exception ex) {
            RxRefillLoggerUtils.exception("RxRefill Cart Screen -- failed to update UI");
        }
        double estimatedAmount = 0.0;
        for (Map.Entry<String, String> item : rxCostMap.entrySet()) {
            String cost = item.getValue();
            // to avoid java.lang.IllegalStateException: not attached to a context.
            if (TextUtils.isEmpty(cost) || cost.equalsIgnoreCase(isAdded() && null != getActivity() ? getActivity().getString(R.string.cost_na) : "*N/A")) {
                break;
            } else {
                estimatedAmount += Double.valueOf(cost);
            }
        }
        dismissProgress();
        RefillRuntimeData.getInstance().setUserNameOnChangeClick(null);

        if (!isInBackGround) {
            returnToReviewOrderFragment();
        } else {
            mTransactionPending = true;
        }
    }

    private void dismissProgress() {
        if (null != getActivity()) {
            getActivity().finishActivity(0);
        }
    }

    private void returnToReviewOrderFragment() {
        for (int i = 0; i < 3; i++) {
            getFragmentManager().popBackStack();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isInBackGround = true;
    }

    @Override
    public void onResume() {
        super.onResume();
        isInBackGround = false;
        if (mTransactionPending) {
            mTransactionPending = false;
            returnToReviewOrderFragment();
        }
    }

    public static void makeCall(String phoneNoStr, Context context, String title) {
        if (mDialog == null || !mDialog.isShowing()) {
            try {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                alertDialog.setTitle(title);
                alertDialog.setMessage(phoneNoStr);
                alertDialog.setCancelable(false);
                alertDialog.setPositiveButton(context.getResources().getString(R.string.call), (dialog, which) -> {
                    dialog.dismiss();
                    RefillRuntimeData.getInstance().setAllowedPrescriptionListClick(true);
                    Intent intent = new Intent("android.intent.action.CALL", Uri.parse("tel:" + phoneNoStr.replace("-", "")));
                    RefillRuntimeData.getInstance().setRunTimePhoneNumber(phoneNoStr);
                    if (RxPermissionUtils.checkRuntimePermission(106, "android.permission.CALL_PHONE", context)) {
                        context.startActivity(intent);
                    }

                });
                alertDialog.setNegativeButton(context.getResources().getString(R.string.cancelAlert), (dialog, which) -> {
                    RefillRuntimeData.getInstance().setAllowedPrescriptionListClick(true);
                    dialog.dismiss();
                });
                mDialog = alertDialog.create();
                mDialog.show();
                TextView tv = mDialog.findViewById(android.R.id.message);
                tv.setGravity(Gravity.CENTER);
                Button btnPositive = mDialog.findViewById(android.R.id.button1);
                Button btnNegative = mDialog.findViewById(android.R.id.button2);

                btnPositive.setTextColor(Util.getColorWrapper(context, R.color.kp_theme_blue));
                btnNegative.setTextColor(Util.getColorWrapper(context, R.color.kp_theme_blue));
            } catch (Exception var7) {
                RxRefillLoggerUtils.exception("Exception while invoking the call");
            }
        }
    }
}