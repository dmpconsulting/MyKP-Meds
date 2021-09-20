package com.montunosoftware.pillpopper.kotlin.lateremider

import android.content.Context
import android.content.Intent
import androidx.lifecycle.ViewModelProvider
import com.montunosoftware.mymeds.R
import com.montunosoftware.pillpopper.PillpopperApplicationShadow
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow
import com.montunosoftware.pillpopper.android.view.DrugDetailRoundedImageView
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow
import com.montunosoftware.pillpopper.kotlin.quickview.ReminderAlertActivity
import com.montunosoftware.pillpopper.kotlin.quickview.ReminderSingleMedDetailActivity
import com.montunosoftware.pillpopper.model.Drug
import com.montunosoftware.pillpopper.model.PillpopperRunTime
import com.montunosoftware.pillpopper.model.PillpopperTime
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.kp.tpmg.mykpmeds.activation.AppConstants
import org.kp.tpmg.mykpmeds.activation.activity.EnlargeImageActivity
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowActivity


@RunWith(RobolectricTestRunner::class)
@Config(sdk = [TestConfigurationProperties.BUILD_SDK_VERSION], application = PillpopperApplicationShadow::class,
        shadows = [RxRefillDBHandlerShadow::class, DatabaseHandlerShadow::class, SecurePreferencesShadow::class, PillpopperAppContextShadow::class])
class LateReminderDetailTest {
    private var lateReminderDetail: LateReminderDetail? = null
    private var context: Context? = null
    private var controller: ActivityController<LateReminderDetail>? = null
    private var intent: Intent? = null
    private var drugList: List<Drug>? = null
    private lateinit var shadowActivity: ShadowActivity
    private lateinit var viewModelprovider: LateReminderDetailViewModel

    @Before
    fun setUp() {
        intent = Intent()
        intent!!.putExtra("isLastGroup", true)
        mockData()
        controller = Robolectric.buildActivity(LateReminderDetail::class.java, intent)
        lateReminderDetail = controller!!.create().start().resume().get()
        shadowActivity = Shadows.shadowOf(lateReminderDetail)
        context = lateReminderDetail!!.application
        viewModelprovider = ViewModelProvider(lateReminderDetail!!).get(LateReminderDetailViewModel::class.java)
    }

    private fun mockData() {
        drugList = ArrayList()
        val drug = Drug()
        drug.userID = "abc"
        drug.isTempHeadr = true
        drug.isNoDrugsFound = false
        drug.isHeader = true
        drug.scheduledTime = PillpopperTime(1212)
        drug.setmAction(0)

        (drugList as ArrayList).add(0, drug)
        PillpopperRunTime.getInstance().setmOverdueDrugs(drugList)
        PillpopperRunTime.getInstance().proxyDrugs = drugList
        PillpopperRunTime.getInstance().headerTime = 1221
        AppConstants.setByPassLogin(true)
    }

    @Test
    fun testShouldNotNull() {
        assertNotNull(lateReminderDetail)
    }

    @Test
    fun testOnTakenAllClicked() {
        lateReminderDetail?.onTakenAllClicked()
        shadowActivity = Shadows.shadowOf(lateReminderDetail)
        val intent: Intent = shadowActivity.peekNextStartedActivity()
        assertEquals(ReminderAlertActivity::class.java.canonicalName, intent.component?.className)
    }

    @Test
    fun testOnSkippedAllClicked() {
        lateReminderDetail?.onSkippedAllClicked()
        shadowActivity = Shadows.shadowOf(lateReminderDetail)
        val intent: Intent = shadowActivity.peekNextStartedActivity()
        assertEquals(ReminderAlertActivity::class.java.canonicalName, intent.component?.className)
    }

    @Test
    fun testOnSkip() {
        lateReminderDetail?.onSkip()
        shadowActivity = Shadows.shadowOf(lateReminderDetail)
        val intent: Intent = shadowActivity.peekNextStartedActivity()
        assertEquals(ReminderAlertActivity::class.java.canonicalName, intent.component?.className)
    }

    @Test
    fun testImageClick(){
        viewModelprovider.onImageClicked(Drug())
        val startedIntent = shadowActivity.nextStartedActivity
        val shadowIntent = Shadows.shadowOf(startedIntent)
        assertEquals(EnlargeImageActivity::class.java, shadowIntent.intentClass)
    }

    @Test
    fun testItemClick(){
        viewModelprovider.onItemClick(Drug())
        val startedIntent = shadowActivity.nextStartedActivity
        val shadowIntent = Shadows.shadowOf(startedIntent)
        assertEquals(ReminderSingleMedDetailActivity::class.java, shadowIntent.intentClass)
    }

    @After
    fun tearDown() {
        controller!!.pause().stop().destroy()
    }
}