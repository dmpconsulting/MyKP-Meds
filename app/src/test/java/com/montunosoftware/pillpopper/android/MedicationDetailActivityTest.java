package com.montunosoftware.pillpopper.android;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow;
import com.montunosoftware.pillpopper.model.NLPReminder;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.shadows.ShadowActivity;
import org.robolectric.shadows.ShadowIntent;
import org.robolectric.shadows.ShadowPopupMenu;

import java.util.HashMap;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {RxRefillDBHandlerShadow.class, DatabaseHandlerShadow.class, SecurePreferencesShadow.class, PillpopperAppContextShadow.class})

public class MedicationDetailActivityTest {
    private MedicationDetailActivity drugDetailActivity;
    private ShadowActivity shadowActivity;
    private ActivityController<MedicationDetailActivity> controller;
    private final String otcPillId = TestConfigurationProperties.MOCK_DRUG_DETAILS_OTC_PILL_ID;
    private final String kphcPillId = TestConfigurationProperties.MOCK_DRUG_DETAILS_KPHC_PILL_ID;
    private ShadowPopupMenu shadowPopUpMenu;


    @Before
    public void setup() {
        TestUtil.setupTestEnvironment();
    }

    @After
    public void tearDown() {
        TestUtil.resetDatabase();
        controller.pause().stop().destroy();
    }

    @Test
    public void checkActivityNotNull() {
        startActivity(true);
        assertNotNull(drugDetailActivity);
    }

    @Test
    public void checkInitDetailViewUI() {
        startActivity(false);
        View view = drugDetailActivity.getActivity().findViewById(R.id.schedule_block);
        TextView editSchedule = view.findViewById(R.id.editSchedule);
        TextView maxDoseNumber = view.findViewById(R.id.maxDoseNumber);
        Button takeAsNeededButton = view.findViewById(R.id.takeAsNeededButton);
        assertEquals(View.GONE, editSchedule.getVisibility());
        if (maxDoseNumber.getText().toString().equalsIgnoreCase("None")) {
            assertEquals(View.VISIBLE, takeAsNeededButton.getVisibility());
        }
    }

    @Test
    public void takeAsNeededSwitch() {
        startActivity(true);
        SwitchCompat takeAsNeeded = drugDetailActivity.findViewById(R.id.takeAsNeededSwitch);
        takeAsNeeded.performClick();
        RelativeLayout scheduleSection = drugDetailActivity.findViewById(R.id.scheduleSection);
        assertTrue(takeAsNeeded.isActivated() ? scheduleSection.getVisibility() == View.VISIBLE : scheduleSection.getVisibility() == View.GONE);
    }

    @Test
    public void setRemindersClicked() {
        startActivity(true);
        drugDetailActivity.onSetRemindersClicked();
        assertTrue(RunTimeData.getInstance().getScheduleData().isEditMedicationClicked());
    }

    @Test
    public void onRecordDoseClicked() {
        startActivity(true);
        drugDetailActivity.onRecordDoseClicked();
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertEquals(GreatJobAlertForTakenAllActivity.class, shadowIntent.getIntentClass());
    }

    @Test
    public void showEditSchedulePopUp() {
        startActivity(true);
        drugDetailActivity.showEditSchedulePopUp();
        PopupMenu shadowMenu = ShadowPopupMenu.getLatestPopupMenu();
        assertNotNull(shadowMenu);
        assertEquals(3, shadowMenu.getMenu().size());
    }

    @Test
    public void performViewClicks(){
        startActivity(true);
        TextView button = drugDetailActivity.findViewById(R.id.editBtn);
        button.performClick();
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertEquals(AddOrEditMedicationActivity.class, shadowIntent.getIntentClass());

        TextView addEditNotes = drugDetailActivity.findViewById(R.id.add_or_edit_notes);
        addEditNotes.performClick();
        Intent startedActivityIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowActivityIntent = shadowOf(startedActivityIntent);
        assertEquals(EditNotesActivity.class, shadowActivityIntent.getIntentClass());

        RelativeLayout expandImage = drugDetailActivity.findViewById(R.id.expand_icon);
        expandImage.performClick();
        assertEquals(View.VISIBLE ,((TextView) drugDetailActivity.findViewById(R.id.instructions_or_notes_full_text)).getVisibility());

        RelativeLayout collapseImage = drugDetailActivity.findViewById(R.id.collapse_icon);
        collapseImage.performClick();
        assertEquals(View.GONE ,((TextView) drugDetailActivity.findViewById(R.id.instructions_or_notes_full_text)).getVisibility());

        Button delete = drugDetailActivity.findViewById(R.id.archive_details_delete_medication_button);
        delete.performClick();

    }

    @Test
    public void miscTestCase(){
        startActivity(true);
        drugDetailActivity.onAddMedicationClicked(new Bundle());
        Intent startedIntent = shadowActivity.getNextStartedActivity();
        ShadowIntent shadowIntent = shadowOf(startedIntent);
        assertEquals(AddMedicationsForScheduleActivity.class, shadowIntent.getIntentClass());

        drugDetailActivity.onNLPRemindersResponseReceived(new NLPReminder());
        assertNull(RunTimeData.getInstance().getAlertDialogInstance());
    }

    public void startActivity(boolean flag) {
        Intent intent = new Intent(RuntimeEnvironment.systemContext, MedicationDetailActivity.class);
        intent.putExtra(PillpopperConstants.PILL_ID, flag ? otcPillId : kphcPillId);
        RunTimeData.getInstance().setMedDetailView(flag);
        RunTimeData.getInstance().setFromArchive(flag);
        HashMap<String, NLPReminder> map = new HashMap<>();
        map.put(otcPillId, new NLPReminder());
        RunTimeData.getInstance().setDrugsNLPRemindersList(map);
        controller = Robolectric.buildActivity(MedicationDetailActivity.class, intent);
        drugDetailActivity = controller.create().start().resume().visible().get();
        shadowActivity = shadowOf(drugDetailActivity);

    }
}
