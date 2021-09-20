package com.montunosoftware.pillpopper.android;

import android.content.Intent;
import android.view.MenuItem;
import android.widget.EditText;

import androidx.test.core.app.ApplicationProvider;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.mymeds.databinding.EditNotesLayoutBinding;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboMenuItem;

import static junit.framework.TestCase.assertNotNull;
import static junit.framework.TestCase.assertSame;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class EditNotesActivityTest {
    private EditNotesActivity editNotesActivity;
    private ActivityController<EditNotesActivity> controller;
    private EditNotesLayoutBinding binding;

    @Before
    public void setup() {
        TestUtil.setupTestEnvironment();
        AppConstants.IS_NATIVE_RX_REFILL_REQUIRED = false;
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), EditNotesActivity.class);
        intent.putExtra("notesValue","Notes");
        // no valid pill id.
        intent.putExtra("ToEditNotes", TestConfigurationProperties.MOCK_DRUG_DETAILS_OTC_PILL_ID);
        controller = Robolectric.buildActivity(EditNotesActivity.class, intent);
        editNotesActivity = controller.create().start().resume().visible().get();
    }

    @After
    public void tearDown() {
        TestUtil.resetDatabase();
    }

    @Test
    public void checkActivityNotNull() {
        assertNotNull(editNotesActivity);
    }

    @Test
    public void onClickSaveMenu() {
        MenuItem addMenuItem = new RoboMenuItem(R.id.save_menu_item);
        assertNotNull(addMenuItem);
        editNotesActivity.onOptionsItemSelected(addMenuItem);
        assertSame(true, addMenuItem.isVisible());
    }

    @Test
    public void onClickHomeMenu() {
        MenuItem homeMenu = new RoboMenuItem(android.R.id.home);
        editNotesActivity.onOptionsItemSelected(homeMenu);
        assertSame(true, homeMenu.isVisible());
    }

    @Test
    public void checkOnEditTextTouch() {
        EditText personalNote = editNotesActivity.getActivity().findViewById(R.id.personal_notes);
        assertSame(true, personalNote.isCursorVisible());
    }

    @Test
    public void checkActivityUI()
    {
        MenuItem addMenuItem=new RoboMenuItem(R.id.save_menu_item);
        assertNotNull(addMenuItem);
        assertSame(true,addMenuItem.isVisible());
    }
}
