package com.montunosoftware.pillpopper.android;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;

import androidx.appcompat.widget.Toolbar;
import androidx.test.core.app.ApplicationProvider;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.model.ManageMemberObj;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.model.SignonResponse;
import org.kp.tpmg.mykpmeds.activation.model.SignonResult;
import org.kp.tpmg.mykpmeds.activation.model.User;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;
import org.robolectric.fakes.RoboMenuItem;

import java.util.ArrayList;
import java.util.List;

import static junit.framework.TestCase.assertEquals;
import static junit.framework.TestCase.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;
import static org.robolectric.Shadows.shadowOf;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class, PillpopperAppContextShadow.class, SecurePreferencesShadow.class})
public class SettingsManageMembersActivityTest {
    private SettingsManageMembersActivity settingsManageMembersActivity;
    private ManageMemberObj manageMemberObj = new ManageMemberObj();
    private Context context;


    @Before
    public void setup() {
        TestUtil.setupTestEnvironment();
        mockData();
        context = RuntimeEnvironment.systemContext;
    }

    @After
    public void tearDown() {
        TestUtil.resetDatabase();
    }

    @Test
    public void checkActivityNotNull() {
        assertNotNull(settingsManageMembersActivity);
    }

    @Test
    public void testInitActionBar() {
        settingsManageMembersActivity.onSetUpProxyResponseReceived(0);
        Toolbar toolbar = settingsManageMembersActivity.findViewById(R.id.app_bar);
        String title = toolbar.getTitle().toString();
        assertEquals("Manage Members", title);
    }

    @Test
    public void onClickSaveMenu() {
        settingsManageMembersActivity.onSetUpProxyResponseReceived(1);
        MenuItem addMenuItem = new RoboMenuItem(R.id.save_menu_item);
        assertNotNull(addMenuItem);
        settingsManageMembersActivity.onOptionsItemSelected(addMenuItem);
        assertSame(true, addMenuItem.isVisible());
    }

    @Test
    public void onClickHomeMenu() {
        MenuItem homeMenu = new RoboMenuItem(android.R.id.home);
        settingsManageMembersActivity.onOptionsItemSelected(homeMenu);
        assertSame(true, homeMenu.isVisible());
    }

    @Test
    public void shouldLaunchLoadingActivity() {
        MenuItem addMenuItem = new RoboMenuItem(R.id.save_menu_item);
        Switch medEnableSwitch = settingsManageMembersActivity.findViewById(R.id.switch_medications);
        assertNotNull(addMenuItem);
        medEnableSwitch.setChecked(true);
        manageMemberObj.setMedicationsEnabled("N");
        settingsManageMembersActivity.onOptionsItemSelected(addMenuItem);
        if (("N").equalsIgnoreCase(manageMemberObj.getMedicationsEnabled()) && medEnableSwitch.isChecked()) {
            shadowOf(settingsManageMembersActivity).getResultCode();
        }
    }

    @Test
    public void testRemainderLayoutVisibility() {
        Switch medEnableSwitch = settingsManageMembersActivity.findViewById(R.id.switch_medications);
        Switch remEnabledSwitch = settingsManageMembersActivity.findViewById(R.id.switch_reminders);
        RelativeLayout remindersLayout = settingsManageMembersActivity.findViewById(R.id.rl_manage_reminders);
        medEnableSwitch.setChecked(true);
        if (medEnableSwitch.isChecked()) {
            assertEquals(View.VISIBLE, remindersLayout.getVisibility());
            assertTrue(remEnabledSwitch.isChecked());
        }
    }

    @Test
    public void testLimitedAccess() {
        TextView limitedAccess = settingsManageMembersActivity.findViewById(R.id.limited_access);
        if (!"proxy".equalsIgnoreCase(manageMemberObj.getUserType())) {
            assertEquals(View.GONE, limitedAccess.getVisibility());
        }
    }

    private void mockData() {
        manageMemberObj.setUserFirstName("xyx");
        manageMemberObj.setTeen(true);
        manageMemberObj.setUserId("124");
        manageMemberObj.setUserType("pqr");
        manageMemberObj.setRemindersEnabled("Y");
        manageMemberObj.setMedicationsEnabled("Y");
        setMockRegisterResponse();
        Bundle bundle = new Bundle();
        bundle.putSerializable(SettingsManageMembersActivity.BUNDLE_CONSTANT_MEMBER_OBJ, manageMemberObj);
        Intent intent = new Intent(ApplicationProvider.getApplicationContext(), SettingsManageMembersActivity.class);
        intent.putExtras(bundle);
        ActivityController<SettingsManageMembersActivity> controller = Robolectric.buildActivity(SettingsManageMembersActivity.class, intent);
        settingsManageMembersActivity = controller.create().start().destroy().visible().get();
    }

    private void setMockRegisterResponse() {
        User user = new User();
        user.setAge("55");
        user.setDisplayName("");
        user.setEnabled("true");
        user.setUserType("primary");
        user.setTeenToggleEnabled(true);
        List<User> userList = new ArrayList<>();
        userList.add(user);
        SignonResult result = new SignonResult();
        result.setUsers(userList);
        SignonResponse response = new SignonResponse();
        response.setResponse(result);
        RunTimeData.getInstance().setRegistrationResponse(response);
    }

}
