package com.montunosoftware.pillpopper.kotlin.banner

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.widget.RelativeLayout
import androidx.databinding.DataBindingUtil
import com.montunosoftware.mymeds.R
import com.montunosoftware.mymeds.databinding.BannerCardAdapterBinding
import com.montunosoftware.pillpopper.PillpopperApplicationShadow
import com.montunosoftware.pillpopper.SupportFragmentTestUtil
import com.montunosoftware.pillpopper.android.StateListenerActivity
import com.montunosoftware.pillpopper.android.util.PillpopperAppContextShadow
import com.montunosoftware.pillpopper.database.persistence.DatabaseHandlerShadow
import com.montunosoftware.pillpopper.database.persistence.RxRefillDBHandlerShadow
import com.montunosoftware.pillpopper.kotlin.bannerCard.GenericBannerAdapter
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
import org.robolectric.annotation.Config
import java.io.Serializable

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [TestConfigurationProperties.BUILD_SDK_VERSION], application = PillpopperApplicationShadow::class,
        shadows = [RxRefillDBHandlerShadow::class, DatabaseHandlerShadow::class, SecurePreferencesShadow::class, PillpopperAppContextShadow::class])
class GenericBannerAdapterTest {
    private var genericBannerAdapter: GenericBannerAdapter? = null
    private var genericBannerFragment: GenericBannerFragment? = null
    private var bannerCardData: List<AnnouncementsItem> = ArrayList()
    private var context: Context? = null

    @Before
    fun setUp() {
        bannerCardData = TestUtil.getAnnouncementsResponse().announcements
        genericBannerFragment = GenericBannerFragment()
        val bundle = Bundle()
        bundle.putSerializable("bannerData", bannerCardData as Serializable?)
        genericBannerFragment?.arguments = bundle
        SupportFragmentTestUtil.startFragment(genericBannerFragment, StateListenerActivity::class.java)
        context = genericBannerFragment?.context
        genericBannerAdapter = GenericBannerAdapter(bannerCardData, genericBannerFragment!!)
        genericBannerAdapter?.onCreateViewHolder(RelativeLayout(context), 0)
    }

    @Test
    fun adapterShouldNotNull() {
        assertNotNull(genericBannerAdapter)
    }

    @Test
    fun testGetItemCount() {
        assertEquals(2, genericBannerAdapter?.itemCount)
    }

    @Test
    fun testOnCreateViewHolder() {
        assertNotNull(genericBannerAdapter?.onCreateViewHolder(RelativeLayout(context), 0))
    }

    @Test
    fun testOnBindViewHolder() {
        val binding: BannerCardAdapterBinding = DataBindingUtil.inflate(
                LayoutInflater.from(context),
                R.layout.banner_card_adapter, null, false)
        genericBannerAdapter?.onBindViewHolder(genericBannerAdapter!!.ViewHolder(binding), 0)
    }
}