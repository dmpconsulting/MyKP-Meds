package com.montunosoftware.pillpopper.kotlin.quickview

import android.content.Context
import android.content.Intent
import com.montunosoftware.pillpopper.PillpopperApplicationShadow
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow
import com.montunosoftware.pillpopper.model.PillpopperRunTime
import kotlinx.android.synthetic.main.reminder_singlemed_detail_activity.*
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.kp.tpmg.mykpmeds.activation.activity.EnlargeImageActivity
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties
import org.kp.tpmg.mykpmeds.activation.util.TestUtil
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowActivity


@RunWith(RobolectricTestRunner::class)

@Config(sdk = [TestConfigurationProperties.BUILD_SDK_VERSION], application = PillpopperApplicationShadow::class,
        shadows = [RxRefillDBHandlerShadow::class, DatabaseHandlerShadow::class, SecurePreferencesShadow::class, PillpopperAppContextShadow::class])
class ReminderSingleMedDetailActivityTest {
    private var reminderSingleMedDetailActivity: ReminderSingleMedDetailActivity? = null
    private var context: Context? = null
    private var intent: Intent? = null

    @Before
    fun setUp() {
        TestUtil.setupTestEnvironment()
        intent = Intent()
        intent!!.putExtra("pill_id", "abc")
        intent!!.putExtra("drug_guid", "xyz")
        intent!!.putExtra("launchMode", "CurrentReminder")
        PillpopperRunTime.getInstance().headerTime = 1212345
        reminderSingleMedDetailActivity = Robolectric.buildActivity(ReminderSingleMedDetailActivity::class.java, intent).create().start().get()
        context = reminderSingleMedDetailActivity!!.applicationContext
    }

    @Test
    fun testShouldNotNull() {
        assertNotNull(reminderSingleMedDetailActivity)
    }

    @Test
    fun testOnClickPillImage() {
        reminderSingleMedDetailActivity!!.pill_image.performClick()
        val shadowActivity: ShadowActivity = Shadows.shadowOf(reminderSingleMedDetailActivity)
        val intent: Intent = shadowActivity.peekNextStartedActivity()
        assertEquals(EnlargeImageActivity::class.java.canonicalName, intent.component?.className)

    }

}