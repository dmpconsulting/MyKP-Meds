package com.montunosoftware.pillpopper.android.refillreminder.views;

import android.content.Context;
import android.view.View;
import android.widget.Button;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.PillpopperApplicationShadow;
import com.montunosoftware.pillpopper.SupportFragmentTestUtil;
import com.montunosoftware.pillpopper.android.HomeContainerActivity;
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.Assert.assertNotNull;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = TestConfigurationProperties.BUILD_SDK_VERSION, application = PillpopperApplicationShadow.class, shadows = {RxRefillDBHandlerShadow.class, DatabaseHandlerShadow.class, SecurePreferencesShadow.class, PillpopperAppContextShadow.class})
public class RefillRemindersListFragmentTest {
    private HomeContainerActivity homeContainerActivity;
    private Context context;
    private RefillRemindersListFragment refillRemindersListFragment;
    private View view;

    @Before
    public void setUp() {
        homeContainerActivity = Robolectric.buildActivity(HomeContainerActivity.class).create().start().get();
        context = homeContainerActivity.getAndroidContext();
        refillRemindersListFragment = new RefillRemindersListFragment();
        SupportFragmentTestUtil.startFragment(refillRemindersListFragment, HomeContainerActivity.class);
        view=refillRemindersListFragment.getView();
    }

    @Test
    public void fragmentShouldNotNull() {
        assertNotNull(refillRemindersListFragment);
    }

    @Test
    public void viewShouldNotNull() {
        assertNotNull(view);
    }

    @Test
    public void onClickCreateRefillReminder()
    {
        Button createRefillReminder=view.findViewById(R.id.btn_create_refill_reminder);
        assertThat(createRefillReminder.performClick()).isTrue();
    }
    @Test
    public void onClickDeleteButton()
    {
        Button deleteButton=view.findViewById(R.id.btn_delete);
        assertThat(deleteButton.performClick()).isTrue();
    }
}
