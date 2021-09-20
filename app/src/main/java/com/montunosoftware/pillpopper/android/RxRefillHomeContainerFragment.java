package com.montunosoftware.pillpopper.android;

/*public class RxRefillHomeContainerFragment {
}*/

import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;

import com.montunosoftware.pillpopper.controller.FrontController;

import org.kp.tpmg.mykpmeds.activation.util.GenericAlertDialog;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.ttg.R;
import org.kp.tpmg.ttg.RefillRuntimeData;
import org.kp.tpmg.ttg.service.RxRefillConnectionService;
import org.kp.tpmg.ttg.utils.RxRefillUtils;
import org.kp.tpmg.ttg.views.RxRefillPrescriptionsListFragment;

import java.util.List;

public class RxRefillHomeContainerFragment extends Fragment implements RxRefillConnectionService.RxRefillServicesListener {

    private Toolbar mToolbar;


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View inflaterView = inflater.inflate(R.layout.rx_refill_home,container,false);
        mToolbar = inflaterView.findViewById(R.id.loading_screen_tool_bar);
        mToolbar.setVisibility(View.GONE);
        RefillRuntimeData.getInstance().setRxRefillUsersList(FrontController.getInstance(getActivity()).getRxRefillUsersList());
        if(null != getArguments() && getArguments().getBoolean("launchPharmacyLocatorFragment", false)){

            installFragment(new PharmacyLocatorFragment());
        }else {
            installFragment(new RxRefillPrescriptionsListFragment());
        }
        return inflaterView;
    }

/*
    private void initToolBar() {
        mToolbar = findViewById(R.id.loading_screen_tool_bar);
        setSupportActionBar(mToolbar);
        getSupportActionBar().setTitle("Pharmacy");
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }
*/

    public void installFragment(Fragment fragment) {

        FragmentTransaction fragment_transaction = getFragmentManager().beginTransaction();
        fragment_transaction.replace(R.id.fragment_container, fragment, fragment.getTag()).setTransition(FragmentTransaction.TRANSIT_FRAGMENT_FADE);
        fragment_transaction.commit();

    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                RxRefillUtils.hideSoftKeyboard(getActivity());
                handleBackNavigation();
                break;
        }
        return false;
    }

    private void handleBackNavigation() {
      /*  if (getSupportFragmentManager().getBackStackEntryCount() > 0) {
            getSupportFragmentManager().popBackStack();
        } else {
            this.finish();
        }*/
    }

    @Override
    public void onPrescriptionsDownloadComplete() {
        getActivity().runOnUiThread(() -> {
//                finishActivity(0);
            RefillRuntimeData.getInstance().setRxRefillUsersList(FrontController.getInstance(getActivity()).getRxRefillUsersList());
            installFragment(new RxRefillPrescriptionsListFragment());
            mToolbar.setVisibility(View.GONE);
        });
    }

    private void showErrorAlert(String title, String msg) {
        GenericAlertDialog alertDialog = new GenericAlertDialog(
                getContext(), title, msg, getString(com.montunosoftware.mymeds.R.string.ok_text),
                alertListener, null, null);
        if (null != alertDialog && !alertDialog.isShowing()) {
            LoggerUtils.info("Debug -- Content Failure -  alert displayed");
            alertDialog.showDialog();
        }
    }

    private final android.content.DialogInterface.OnClickListener alertListener = (dialog, which) -> {
        dialog.dismiss();
        getActivity().finish();
    };

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        sendPermissionRequestResult(requestCode, permissions, grantResults);
    }

    private void sendPermissionRequestResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        List<Fragment> fragments = getFragmentManager().getFragments();
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

/*
    @Override
    public void onBackPressed() {
        int count = getSupportFragmentManager().getBackStackEntryCount();
        if (count == 0) {
            super.onBackPressed();
        } else {
            if(!RefillRuntimeData.getInstance().isAppInOrderConfirmationScreen()) {
                getSupportFragmentManager().popBackStack();
            }
        }
    }
*/

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
}

