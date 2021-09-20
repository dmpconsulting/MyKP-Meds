package com.montunosoftware.pillpopper;

import android.os.Bundle;
import android.widget.LinearLayout;

import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow;
import com.montunosoftware.pillpopper.android.util.UniqueDeviceIdShadow;
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow;
import com.montunosoftware.pillpopper.refillreminder.TestPillpopperApplication;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;

import org.junit.runner.RunWith;
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow;
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties;
import org.robolectric.Robolectric;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.android.controller.ActivityController;
import org.robolectric.annotation.Config;

/**
 * Utilities for creating Fragments for testing.
 */
@Config( sdk = TestConfigurationProperties.BUILD_SDK_VERSION, manifest="AndroidManifest.xml", application = TestPillpopperApplication.class, shadows = {PillpopperAppContextShadow.class, DatabaseHandlerShadow.class, SecurePreferencesShadow.class, UniqueDeviceIdShadow.class})
public class SupportFragmentTestUtil {

    public static void startFragment(Fragment fragment) {
        buildSupportFragmentManager(FragmentUtilActivity.class)
                .beginTransaction().add(fragment, null).commit();
    }

    public static void startFragment(Fragment fragment, Class<? extends FragmentActivity> fragmentActivityClass) {
        buildSupportFragmentManager(fragmentActivityClass)
                .beginTransaction().add(fragment, null).commit();
    }

    public static void startVisibleFragment(Fragment fragment) {
        buildSupportFragmentManager(FragmentUtilActivity.class)
                .beginTransaction().add(1, fragment, null).commit();
    }

    public static void startVisibleFragment(Fragment fragment, Class<? extends FragmentActivity> fragmentActivityClass, int containerViewId) {
        buildSupportFragmentManager(fragmentActivityClass)
                .beginTransaction().add(containerViewId, fragment, null).commit();
    }

    private static FragmentManager buildSupportFragmentManager(Class<? extends FragmentActivity> fragmentActivityClass) {
        FragmentActivity activity = Robolectric.buildActivity(fragmentActivityClass).create()
                .start()
                .resume()
                .get();
        return activity.getSupportFragmentManager();
    }

    private static class FragmentUtilActivity extends FragmentActivity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            LinearLayout view = new LinearLayout(this);
            view.setId(1);

            setContentView(view);
        }
    }
}
