package com.montunosoftware.pillpopper.android;

import android.content.Intent;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.test.core.app.ApplicationProvider;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.model.SignonResponse;
import org.kp.tpmg.mykpmeds.activation.model.SignonResult;
import org.kp.tpmg.mykpmeds.activation.model.User;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.core.Is.is;
import static org.hamcrest.core.IsNull.notNullValue;


@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class AddOrEditMedicationTestActivity {

    private AddOrEditMedicationActivity addNewDrugActivity;
    private ActivityController<AddOrEditMedicationActivity> controller;
    private ShadowActivity addNewDrugActivityShadow;

    @Before
    public void setup() {
        TestUtil.setupTestEnvironment();
        AppConstants.IS_NATIVE_RX_REFILL_REQUIRED = false;

    }

    private void startActivity(String value) {
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), AddOrEditMedicationActivity.class);
        intent.putExtra(PillpopperConstants.LAUNCH_MODE, value);
        controller = Robolectric.buildActivity(AddOrEditMedicationActivity.class, intent);
        RunTimeData.getInstance().setRegistrationResponse(prepareResponse());
        addNewDrugActivity = controller.create().start().resume().visible().get();
        addNewDrugActivityShadow = Shadows.shadowOf(addNewDrugActivity);
    }

    @After
    public void tearDown() {
        TestUtil.resetDatabase();
        controller.pause().stop().destroy();
    }

    @Test
    public void checkActivityNotNull() {
        startActivity(PillpopperConstants.ACTION_EDIT_PILL);
        assertNotNull(addNewDrugActivity);
    }

    private SignonResponse prepareResponse() {
        SignonResponse response = new SignonResponse();
        SignonResult result = new SignonResult();
        List<User> userList = new ArrayList<>();
        User user = new User();
        user.setAge("50");
        user.setMrn("12345678");
        user.setDisplayName("Test Name");
        user.setEnabled("Y");
        user.setTeen(false);
        userList.add(user);
        result.setUsers(userList);
        response.setResponse(result);
        return response;
    }
    @Test
    public void checkActivityUI() {
        startActivity(PillpopperConstants.ACTION_EDIT_PILL);
        TextView spinnerMemberName = addNewDrugActivity.getActivity().findViewById(R.id.spinner_member_name);
        ImageView spinnerArrow = addNewDrugActivity.getActivity().findViewById(R.id.spinner_arrow);
        TextView disclaimerText = addNewDrugActivity.getActivity().findViewById(R.id.disclaimer_text);
        assertNotNull(spinnerMemberName.getText());
        assertEquals(View.GONE, spinnerArrow.getVisibility());
        assertEquals(View.GONE, disclaimerText.getVisibility());
    }

    @Test
    public void testOnRequestPermissionsResult() {
        startActivity(PillpopperConstants.ACTION_EDIT_PILL);
        int requestCode;
        int[] grantResults;
        requestCode = AppConstants.PERMISSION_CAMERA;
        grantResults = new int[]{0};
        String[] permissions = {" "};
        addNewDrugActivity.onRequestPermissionsResult(requestCode, permissions, grantResults);
        requestCode = AppConstants.PERMISSION_READ_EXTERNAL_STORAGE;
        addNewDrugActivity.onRequestPermissionsResult(requestCode, permissions, grantResults);
        grantResults = new int[]{1};
        addNewDrugActivity.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @Test
    public void testOnClickPersonalNotes() {
        startActivity(PillpopperConstants.ACTION_EDIT_PILL);
        TextView personalNotes = addNewDrugActivity.findViewById(R.id.personal_notes);
        assertThat(personalNotes.performClick()).isTrue();
        Intent startedIntent = addNewDrugActivityShadow.getNextStartedActivity();
        ShadowIntent shadowIntent = Shadows.shadowOf(startedIntent);
        assertThat(shadowIntent, is(notNullValue()));
    }

    @Test
    public void testOnClickSaveMedicationButton() {
        startActivity(PillpopperConstants.ACTION_CREATE_PILL);
        com.montunosoftware.pillpopper.android.view.ActionEditText actionEditText = addNewDrugActivity.findViewById(R.id.med_name);
        actionEditText.setText("abc");
        TextView personalNotes = addNewDrugActivity.findViewById(R.id.personal_notes);
        personalNotes.setText("take medicine");
        com.montunosoftware.pillpopper.android.view.ActionEditText dosageStrength = addNewDrugActivity.findViewById(R.id.dosage_strength);
        dosageStrength.setText("2");
        Button saveMedication = addNewDrugActivity.findViewById(R.id.btn_save_medications);
        assertThat(saveMedication.performClick()).isTrue();
    }

    @Test
    public void testOnImageClicked() {
        startActivity(PillpopperConstants.ACTION_CREATE_PILL);
        TextView addImage = addNewDrugActivity.findViewById(R.id.addImageBtn);
        assertThat(addImage.performClick()).isTrue();
    }

}
