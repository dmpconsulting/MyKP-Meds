package com.montunosoftware.pillpopper.kotlin.quickview

import android.content.Context
import android.content.Intent
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProvider
import androidx.test.core.app.ApplicationProvider
import com.montunosoftware.mymeds.R
import com.montunosoftware.mymeds.databinding.CurrentReminderDetailViewRedesignBinding
import com.montunosoftware.pillpopper.PillpopperApplicationShadow
import com.montunosoftware.pillpopper.android.PillpopperActivity
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow
import com.montunosoftware.pillpopper.android.util.UniqueDeviceId
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow
import com.montunosoftware.pillpopper.model.Drug
import com.montunosoftware.pillpopper.model.PillpopperRunTime
import com.montunosoftware.pillpopper.model.PillpopperTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.kp.tpmg.mykpmeds.activation.util.SecurePreferencesShadow
import org.kp.tpmg.mykpmeds.activation.util.TestConfigurationProperties
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows
import org.robolectric.annotation.Config
import org.robolectric.shadows.ShadowActivity


@RunWith(RobolectricTestRunner::class)
@Config(sdk = [TestConfigurationProperties.BUILD_SDK_VERSION], application = PillpopperApplicationShadow::class,
        shadows = [RxRefillDBHandlerShadow::class, DatabaseHandlerShadow::class, SecurePreferencesShadow::class, PillpopperAppContextShadow::class])
class ReminderOverDueAdapterTest {
    private var singleCurrentReminder: SingleCurrentReminder? = null
    private var context: Context? = null
    private var reminderOverDueAdapter: ReminderOverDueAdapter? = null
    private var activity: PillpopperActivity? = null
    private var allDrugs: List<Drug>? = null
    private lateinit var currentReminderViewModel: CurrentReminderActivityViewModel


    @Before
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
        activity = Robolectric.buildActivity(PillpopperActivity::class.java).get()
        mockData()
        singleCurrentReminder = Robolectric.buildActivity(SingleCurrentReminder::class.java).create().resume().get()
        currentReminderViewModel = ViewModelProvider(singleCurrentReminder!!).get(CurrentReminderActivityViewModel::class.java)
        reminderOverDueAdapter = ReminderOverDueAdapter(allDrugs!!, activity, singleCurrentReminder!!, allDrugs!!, currentReminderViewModel)
        reminderOverDueAdapter?.onCreateViewHolder(RelativeLayout(context), 1)
    }


    private fun mockData() {
        UniqueDeviceId.init(context)
        allDrugs = ArrayList()
        val drug = Drug()
        drug.userID = "abc"
        drug.isTempHeadr = true
        drug.isNoDrugsFound = false
        drug.isHeader = false
        drug.name = "(xyz)"
        drug.scheduledTime = PillpopperTime(1212)
        drug.setmAction(1)
        (allDrugs as ArrayList).add(0, drug)
        (allDrugs as ArrayList).add(1, drug)
        PillpopperRunTime.getInstance().proxyDrugs = allDrugs
    }

    @Test
    fun shouldNotNull() {
        assertNotNull(reminderOverDueAdapter)
    }

    @Test
    fun testGetItemCount() {
        assertEquals(allDrugs?.size, reminderOverDueAdapter?.itemCount)
    }

    @Test
    fun testOnCreateViewHolder() {
        assertNotNull(reminderOverDueAdapter?.onCreateViewHolder(RelativeLayout(context), 1))
    }

    @Test
    fun testOnBindViewHolder() {
        val binding: CurrentReminderDetailViewRedesignBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.current_reminder_detail_view_redesign, null, false)
        reminderOverDueAdapter?.onBindViewHolder(reminderOverDueAdapter!!.ViewHolder(binding.root), 0)
    }

    @Test
    fun testGetItemId() {
        val position = 1
        assertEquals(position.toLong(), reminderOverDueAdapter?.getItemId(1))
    }

    @Test
    fun testOnSkip() {
        reminderOverDueAdapter?.onSkip()
        val shadowActivity: ShadowActivity = Shadows.shadowOf(singleCurrentReminder)
        val intent: Intent = shadowActivity.peekNextStartedActivity()
        assertEquals(ReminderAlertActivity::class.java.canonicalName, intent.component?.className)
    }

    @Test
    fun testGetItem() {
        assertNotNull(reminderOverDueAdapter?.getItem(0))
    }

    @Test
    fun testGetItemViewType() {
        assertEquals(0, reminderOverDueAdapter?.getItemViewType(0))
    }
}