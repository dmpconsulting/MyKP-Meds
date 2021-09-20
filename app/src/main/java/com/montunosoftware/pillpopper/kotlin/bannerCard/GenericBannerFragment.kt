package com.montunosoftware.pillpopper.kotlin.bannerCard

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.montunosoftware.mymeds.R
import com.montunosoftware.mymeds.databinding.BannerCardFragmentBinding
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker
import com.montunosoftware.pillpopper.analytics.FireBaseConstants
import com.montunosoftware.pillpopper.android.ExpandedGenericBannerActivity
import com.montunosoftware.pillpopper.android.GenericCardAndBannerUtility
import com.montunosoftware.pillpopper.model.genericCardAndBanner.AnnouncementsItem
import org.kp.tpmg.mykpmeds.activation.AppConstants
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager

class GenericBannerFragment : Fragment(), BannerClickEvent {

    private var mBannerCardData: List<AnnouncementsItem> = ArrayList()
    private lateinit var mBundle: Bundle
    private lateinit var mBinding: BannerCardFragmentBinding
    private var mSharedPrefManager: SharedPreferenceManager? = null


    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        mBinding = DataBindingUtil.inflate(inflater, R.layout.banner_card_fragment, container, false)
        if (null != arguments) {
            mBundle = arguments as Bundle
            mBannerCardData = mBundle.getSerializable("bannerData")!! as List<AnnouncementsItem>
        }
        mSharedPrefManager = SharedPreferenceManager.getInstance(context, AppConstants.AUTH_CODE_PREF_NAME)
        var mCallback: BannerClickEvent
        mCallback = this
        var adapter = GenericBannerAdapter(mBannerCardData, mCallback)
        mBinding.rvBannerCard.layoutManager = LinearLayoutManager(context)
        mBinding.rvBannerCard.adapter = adapter
        return mBinding.root
    }

    override fun onBannerClicked(bannerData: AnnouncementsItem) {
        FireBaseAnalyticsTracker.getInstance().logEvent(context, FireBaseConstants.Event.GENERIC_BANNER_CLICK, FireBaseConstants.ParamName.SOURCE, bannerData.baseScreen)
        if (bannerData.type.equals("banner_short")) {
            GenericCardAndBannerUtility.buttonAction(bannerData,bannerData.buttons?.get(0)!!.action, bannerData.buttons?.get(0)!!.url, bannerData.buttons?.get(0)!!.destination, activity)
        } else {
            val intent = Intent(context, ExpandedGenericBannerActivity::class.java)
            intent.putExtra("announcement", bannerData)
            requireActivity().startActivity(intent)
        }
    }}

interface BannerClickEvent {
    fun onBannerClicked(listItem: AnnouncementsItem)

}