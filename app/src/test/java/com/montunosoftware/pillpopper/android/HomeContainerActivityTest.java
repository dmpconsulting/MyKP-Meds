package com.montunosoftware.pillpopper.android;

import android.content.Intent;
import android.widget.ListView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;

import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.model.NavDrawerItem;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.kp.tpmg.mykpmeds.activation.util.TestUtil;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.Shadows;
import org.robolectric.annotation.Config;
import org.robolectric.annotation.LooperMode;

import static android.os.Looper.getMainLooper;
import static org.robolectric.Shadows.shadowOf;

/**
 * Created by M1028309 on 11/27/2017.
 */
@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION,application = PillpopperApplicationShadow.class, shadows = {DatabaseHandlerShadow.class,SecurePreferencesShadow.class, PillpopperAppContextShadow.class})
public class HomeContainerActivityTest {

    private HomeContainerActivity homeContainerActivity;

    @Before
    @LooperMode(LooperMode.Mode.PAUSED)
    public void setup() {
        TestUtil.setupTestEnvironment();
        AppConstants.IS_NATIVE_RX_REFILL_REQUIRED = false;
        Intent intent=new Intent();
        intent.putExtra("NeedMyMedTab", true);
        homeContainerActivity=Robolectric.buildActivity(HomeContainerActivity.class,intent).create().start().resume().visible().get();
        shadowOf(getMainLooper()).idle();
    }

    @Test
    @LooperMode(LooperMode.Mode.PAUSED)
    public void checkActivityNull(){
        Assert.assertNotNull(homeContainerActivity);
        shadowOf(getMainLooper()).idle();
    }

    @Test
    @LooperMode(LooperMode.Mode.PAUSED)
    public void CheckNavigationListItems(){
        final ListView viewById = (ListView) homeContainerActivity.findViewById(R.id.lstdrawer);
        int itemCount=viewById.getAdapter().getCount();
        //Check for newly added 'Home' item
        assert (((NavDrawerItem)viewById.getAdapter().getItem(0)).getTitle().equalsIgnoreCase(homeContainerActivity.getString(R.string.home)));
        assert (((NavDrawerItem)viewById.getAdapter().getItem(0)).getIcon()==R.drawable.navigation_home);
        shadowOf(getMainLooper()).idle();
    }


    @Test
    @LooperMode(LooperMode.Mode.PAUSED)
    public void CheckNavigationHomeClick(){
        final ListView viewById = (ListView) homeContainerActivity.findViewById(R.id.lstdrawer);
        shadowOf(viewById).performItemClick(0);
        System.out.print(homeContainerActivity.getSupportFragmentManager().getFragments());
        shadowOf(getMainLooper()).idle();
    }
}