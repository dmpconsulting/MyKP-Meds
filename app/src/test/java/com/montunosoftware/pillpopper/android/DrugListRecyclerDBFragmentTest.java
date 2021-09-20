
package com.montunosoftware.pillpopper.android;

import androidx.recyclerview.widget.RecyclerView;

import android.view.MenuItem;
import android.view.View;
import android.widget.Button;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;

import com.montunosoftware.pillpopper.SupportFragmentTestUtil;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.controller.FrontControllerShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow;


import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboMenuItem;


import java.util.Objects;

import static org.assertj.core.api.Assertions.*;
import static org.junit.Assert.assertNotNull;


@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class,
        shadows = {RxRefillDBHandlerShadow.class, DatabaseHandlerShadow.class, SecurePreferencesShadow.class,
                PillpopperAppContextShadow.class, FrontControllerShadow.class})
public class DrugListRecyclerDBFragmentTest {

    private DrugListRecyclerDBFragment drugListRecyclerDBFragment ;
    private View view;

    @Before
    public void setup() {
        TestUtil.setupTestEnvironment();
        drugListRecyclerDBFragment = new DrugListRecyclerDBFragment();
        SupportFragmentTestUtil.startFragment(drugListRecyclerDBFragment, StateListenerActivity.class);
        view = drugListRecyclerDBFragment.getView();
    }

    @Test
    public void fragmentViewShouldNotBeNull() {
        assertNotNull(view);
    }

    @Test
    public void medListShouldNotBeNull() {
        RecyclerView medListView =  view.findViewById(R.id.recycler_view);
        assertThat(medListView);
    }

    @Test
    public void addMedicationTest() {
        Button addMed = view.findViewById(R.id.fragment_druglist_add_medication);
        assertThat(addMed.performClick()).isTrue();
    }

    @Test
    public void addMedicationFromMenuTest() {
        MenuItem addMedication = new RoboMenuItem(R.id.drug_list_menu_create);
        drugListRecyclerDBFragment.getActivity().onOptionsItemSelected(addMedication);
       assertThat(Shadows.shadowOf(drugListRecyclerDBFragment.getActivity()).getNextStartedActivity());
    }

    @Test
    public void archiveMedicationFromMenuTest() {
        MenuItem archiveMedication = new RoboMenuItem(R.id.drug_list_edit);
        drugListRecyclerDBFragment.getActivity().onOptionsItemSelected(archiveMedication);
        assertThat(Shadows.shadowOf(drugListRecyclerDBFragment.getActivity()).getNextStartedActivity());
    }

    @Test
    public void testOnRequestPermissionsResult() { ;
        int requestCode;
        int[] grantResults;
        grantResults = new int[]{1};
        String[] permissions = {" "};
        requestCode = AppConstants.PERMISSION_READ_EXTERNAL_STORAGE;
        drugListRecyclerDBFragment.onRequestPermissionsResult(requestCode,permissions,grantResults);

    }

    @After
    public void tearDown() {
        TestUtil.resetDatabase();
        drugListRecyclerDBFragment.onPause();
        drugListRecyclerDBFragment.onStop();
    }

}

