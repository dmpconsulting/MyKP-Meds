package com.montunosoftware.pillpopper

import android.content.Context
import android.content.Intent
import android.widget.Button
import androidx.test.core.app.ApplicationProvider
import com.montunosoftware.mymeds.R
import com.montunosoftware.pillpopper.android.ExpandedGenericBannerActivity
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow
import com.montunosoftware.pillpopper.model.genericCardAndBanner.AnnouncementsItem
import org.assertj.core.api.Assertions.assertThat
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties
import org.kp.tpmg.mykpmeds.activation.util.TestUtil
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [TestConfigurationProperties.BUILD_SDK_VERSION], application = PillpopperApplicationShadow::class,
        shadows = [RxRefillDBHandlerShadow::class, DatabaseHandlerShadow::class, SecurePreferencesShadow::class, PillpopperAppContextShadow::class])
class ExpandedGenericBannerActivityTest {
    private var expandedGenericBannerActivity: ExpandedGenericBannerActivity? = null
    private var bannerData: AnnouncementsItem? = null
    private var context: Context? = null
    private var controller: ActivityController<ExpandedGenericBannerActivity>? = null

    @Before
    fun setUp() {
        TestUtil.setupTestEnvironment()
        context = ApplicationProvider.getApplicationContext()
        bannerData = TestUtil.getAnnouncementsResponse().announcements[1]
        val intent = Intent(context, ExpandedGenericBannerActivity::class.java)
        intent.putExtra("announcement", bannerData)
        controller = Robolectric.buildActivity(ExpandedGenericBannerActivity::class.java, intent)
        expandedGenericBannerActivity = controller!!.create().start().resume().get()
    }

    @Test
    fun testShouldNotNull() {
        assertNotNull(expandedGenericBannerActivity)
    }

    @Test
    fun testOnButtonClick() {
        val kpButton: Button = expandedGenericBannerActivity!!.findViewById<Button>(R.id.kpButton)
        val acknowledgeButton: Button = expandedGenericBannerActivity!!.findViewById<Button>(R.id.acknowledge_button)
        assertThat(kpButton.performClick()).isTrue
        assertThat(acknowledgeButton.performClick()).isTrue
    }

    @Test
    fun tearDown() {
        TestUtil.resetDatabase()
        controller!!.pause().stop().destroy()
    }
}