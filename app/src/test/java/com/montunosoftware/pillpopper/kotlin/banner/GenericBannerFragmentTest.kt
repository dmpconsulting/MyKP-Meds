package com.montunosoftware.pillpopper.kotlin.banner

import android.content.Intent
import android.os.Bundle
import com.montunosoftware.pillpopper.PillpopperApplicationShadow
import com.montunosoftware.pillpopper.SupportFragmentTestUtil
import com.montunosoftware.pillpopper.android.ExpandedGenericBannerActivity
import com.montunosoftware.pillpopper.android.StateListenerActivity
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow
import com.montunosoftware.pillpopper.kotlin.bannerCard.GenericBannerFragment
import com.montunosoftware.pillpopper.model.genericCardAndBanner.AnnouncementsItem
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties
import org.kp.tpmg.mykpmeds.activation.util.TestUtil
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowActivity
import java.io.Serializable

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [TestConfigurationProperties.BUILD_SDK_VERSION], application = PillpopperApplicationShadow::class,
        shadows = [RxRefillDBHandlerShadow::class, DatabaseHandlerShadow::class, SecurePreferencesShadow::class, PillpopperAppContextShadow::class])
class GenericBannerFragmentTest {
    private var genericBannerFragment: GenericBannerFragment? = null
    private var bannerCardData: List<AnnouncementsItem> = ArrayList()

    @Before
    fun setUp() {
        bannerCardData = TestUtil.getAnnouncementsResponse().announcements
        genericBannerFragment = GenericBannerFragment()
        val bundle = Bundle()
        bundle.putSerializable("bannerData", bannerCardData as Serializable?)
        genericBannerFragment?.arguments = bundle
        SupportFragmentTestUtil.startFragment(genericBannerFragment, StateListenerActivity::class.java)
    }

    @Test
    fun fragmentShouldNotNull() {
        assertNotNull(genericBannerFragment)
    }

    @Test
    fun testOnBannerClicked() {
        genericBannerFragment?.onBannerClicked(bannerCardData[0])
        val shadowActivity: ShadowActivity = Shadows.shadowOf(genericBannerFragment?.activity)
        val intent: Intent = shadowActivity.peekNextStartedActivity()
        assertEquals(ExpandedGenericBannerActivity::class.java.canonicalName, intent.component?.className)
    }
}