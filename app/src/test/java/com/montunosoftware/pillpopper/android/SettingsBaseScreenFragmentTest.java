package com.montunosoftware.pillpopper.android;


import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.widget.SwitchCompat;
import androidx.test.core.app.ApplicationProvider;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.SupportFragmentTestUtil;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.controller.FrontControllerShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.model.PillpopperRunTime;
import com.montunosoftware.pillpopper.refillreminder.RefillReminderControllerShadow;
import com.montunosoftware.pillpopper.service.getstate.StateDownloadIntentService;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.Assert.assertTrue;
import static org.assertj.core.api.Assertions.assertThat;

/**
 * Created by m1032896 on 5/22/2017.
 * Mindtree Ltd
 * Raghavendra.dg@mindtree.com
 */

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, RefillReminderControllerShadow.class,
        SecurePreferencesShadow.class, FrontControllerShadow.class, DatabaseHandlerShadow.class})
public class SettingsBaseScreenFragmentTest {

    private Context context;
    private SettingsBaseScreenFragment settingsBaseScreenFragment;
    private View view;

    @Before
    public void setup() {
        TestUtil.setupTestEnvironment();
        AppConstants.IS_NATIVE_RX_REFILL_REQUIRED = false;
        context = ApplicationProvider.getApplicationContext();
        settingsBaseScreenFragment = new SettingsBaseScreenFragment();
        SupportFragmentTestUtil.startFragment(settingsBaseScreenFragment, HomeContainerActivity.class);
        view = settingsBaseScreenFragment.getView();
    }

    @After
    public void tearDown() {
        TestUtil.resetDatabase();
        settingsBaseScreenFragment.onStop();
        settingsBaseScreenFragment.onPause();
        settingsBaseScreenFragment.onDestroy();
    }

    @Test
    public void fragmentViewShouldNotBeNull() {
        assertNotNull(view);
    }


    @Test
    public void syncShouldSuccess() {
        Calendar calender = Calendar.getInstance();
        PillpopperRunTime.getInstance().setmLastSyncTime(calender);
        Intent intent = new Intent(StateDownloadIntentService.BROADCAST_GET_STATE_COMPLETED);
        intent.putExtra(PillpopperConstants.KEY_ACTION, PillpopperConstants.ACTION_HISTORY_EVENTS);
        ApplicationProvider.getApplicationContext().sendBroadcast(intent);
        assertTrue(PillpopperRunTime.getInstance().isHistorySyncDone());
        TextView lastSyncTimeTextView = view.findViewById(R.id.last_sync_time_text);
        SimpleDateFormat df = new SimpleDateFormat("MM/d/yy h:mm aaa", Locale.getDefault());
        assertThat(lastSyncTimeTextView.getText().toString()).isEqualTo(df.format(calender.getTime()));
    }

    @Test
    public void checkSignOutReminderSwitch() {
        SwitchCompat reminderSwitch = view.findViewById(R.id.toggleButton_singedOut);
        reminderSwitch.performClick();
        TextView subText = view.findViewById(R.id.singed_out_subtext);
        assertEquals(reminderSwitch.isEnabled() ? context.getString(R.string.signed_out_reminder_on_text) : context.getString(R.string.signed_out_reminder_off_text), subText.getText().toString());
    }

    @Test
    public void onActivityResult() {
        Intent intent = new Intent();
        intent.putExtra(RingtoneManager.EXTRA_RINGTONE_PICKED_URI, Uri.parse("abd"));
        intent.putExtra(AppConstants.INTENT_RESULT_IS_FINGERPRINT_OPT_IN_SETUP_COMPLETE, true);
        intent.putExtra(AppConstants.INTENT_RESULT_IS_FINGERPRINT_OPTED_IN, true);
        settingsBaseScreenFragment.onActivityResult(101, Activity.RESULT_OK,intent);
        TextView mTxtNotificationSound = view.findViewById(R.id.txt_notification_select);
        assertNotNull(mTxtNotificationSound.getText().toString());
        settingsBaseScreenFragment.onActivityResult(1,Activity.RESULT_OK, intent);
        SwitchCompat fingerPrintToggle = view.findViewById(R.id.settings_fingerprint_sign_in_switch);
        assertTrue(fingerPrintToggle.isEnabled());
    }

    @Test
    public void testOnClick() {
        TextView historySelect = view.findViewById(R.id.tv_history_select);
        historySelect.performClick();
        assertNotNull(RunTimeData.getInstance().getAlertDialogInstance());
    }
}
