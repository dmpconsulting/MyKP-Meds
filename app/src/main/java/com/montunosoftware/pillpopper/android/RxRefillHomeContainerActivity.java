package com.montunosoftware.pillpopper.android;

import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.montunosoftware.pillpopper.controller.FrontController;

import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.ttg.R;
import org.kp.tpmg.ttg.RefillRuntimeData;
import org.kp.tpmg.ttg.utils.RxRefillUtils;
import org.kp.tpmg.ttg.views.RxRefillPrescriptionsListFragment;

import java.util.List;

public class RxRefillHomeContainerActivity extends StateListenerActivity implements PharmacyLocatorFragment.onPharmacyListItemClickListener{

    private Toolbar mToolbar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(null != getIntent() && getIntent().getBooleanExtra("launchPharmacyLocatorDetails", false)) {
            overridePendingTransition(R.anim.slide_right_in, R.anim.slide_left_out);
        }
        setContentView(R.layout.rx_refill_home);
        initToolBar();
        RefillRuntimeData.getInstance().setRxRefillUsersList(FrontController.getInstance(getActivity()).getRxRefillUsersList());
        if(null != getIntent() && getIntent().getBooleanExtra("launchPharmacyLocatorDetails", false)){
            Fragment fragment = new PharmacyDetailFragment();
            fragment.setArguments(getActivity().getIntent().getExtras());
            installFragment(fragment);
        }else {
            installFragment(new RxRefillPrescriptionsListFragment());
        }
        mToolbar.setVisibility(View.GONE);
    }

    private void initToolBar() {
        mToolbar = findViewById(R.id.loading_screen_tool_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Pharmacy");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    public void installFragment(Fragment fragment) {

        FragmentTransaction fragment_transaction = getSupportFragmentManager().beginTransaction();
        fragment_transaction.replace(R.id.fragment_container, fragment, fragment.getTag()).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragment_transaction.commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                RxRefillUtils.hideSoftKeyboard(this);
                handleBackNavigation();
                break;
        }
        return false;
    }

    private void handleBackNavigation() {
        if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            onBackPressed();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        sendPermissionRequestResult(requestCode, permissions, grantResults);
    }

    private void sendPermissionRequestResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        List<Fragment> fragments = getSupportFragmentManager().getFragments();
        if (fragments != null) {
            for (Fragment fragment : fragments) {
                if (fragment != null) {
                    try {
                        fragment.onRequestPermissionsResult(requestCode, permissions, grantResults);
                    } catch (Exception e) {
                        LoggerUtils.exception("exception while fragment onRequestPermissionsResult");
                    }
                }
            }
        }
    }

    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            super.onBackPressed();
            overridePendingTransition(R.anim.slide_left_in, R.anim.slide_right_out);
        } else {
            if(!RefillRuntimeData.getInstance().isAppInOrderConfirmationScreen()) {
                getSupportFragmentManager().popBackStack();
            }
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        RunTimeConstants.getInstance().setNotificationSuppressor(true);
    }

    @Override
    public void onResume() {
        super.onResume();
        RunTimeConstants.getInstance().setNotificationSuppressor(false);
    }

    @Override
    public void onPharmacyItemClicked(Bundle bundle) {
        Fragment fragment = new PharmacyDetailFragment();
        fragment.setArguments(bundle);
        FragmentTransaction fragmenTransaction = getSupportFragmentManager().beginTransaction();
        fragmenTransaction.replace(R.id.fragment_container, fragment, fragment.getTag()).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragmenTransaction.addToBackStack(PharmacyLocatorFragment.class.getName());
        fragmenTransaction.commit();
    }
}
