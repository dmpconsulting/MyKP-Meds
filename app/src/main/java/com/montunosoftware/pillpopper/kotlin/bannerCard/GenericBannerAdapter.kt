package com.montunosoftware.pillpopper.kotlin.bannerCard

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.databinding.ViewDataBinding
import androidx.recyclerview.widget.RecyclerView
import com.montunosoftware.mymeds.BR
import com.montunosoftware.mymeds.R
import com.montunosoftware.mymeds.databinding.BannerCardAdapterBinding
import com.montunosoftware.pillpopper.android.GenericCardAndBannerUtility
import com.montunosoftware.pillpopper.android.util.Util
import com.montunosoftware.pillpopper.model.genericCardAndBanner.AnnouncementsItem


class GenericBannerAdapter(var mBannerCardData: List<AnnouncementsItem>, var mCallback: BannerClickEvent) : RecyclerView.Adapter<GenericBannerAdapter.ViewHolder>() {
    private lateinit var mcontext: Context
    private lateinit var binding: BannerCardAdapterBinding

    inner class ViewHolder(view : ViewDataBinding) : RecyclerView.ViewHolder(view.root){
            fun bind(cardBanner : AnnouncementsItem){
                binding.setVariable(BR.cardBanner,cardBanner)
            }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
       binding = DataBindingUtil.inflate(LayoutInflater.from(parent.context), R.layout.banner_card_adapter,parent,false)
        mcontext = parent.context
        return ViewHolder(binding)
    }

    override fun getItemCount(): Int {
       return mBannerCardData.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
       var bannerData = mBannerCardData.get(position)
        holder.bind(bannerData)
        if(bannerData.iconName.equals("banner_alert")){
            binding.bannerIcon.setImageResource(R.drawable.ic_alertbanner)
        }else if(bannerData.iconName.equals("banner_info")){
            binding.bannerIcon.setImageResource(R.drawable.ic_info)
        }else if(bannerData.iconName.equals("banner_high_alert") || bannerData.iconName.equals("banner_high-alert")){
            binding.bannerIcon.setImageResource(R.drawable.ic_high_alert)
        }
        if (!Util.isEmptyString(bannerData.color)) {
            binding.relMain.setBackgroundColor(Color.parseColor("#" + bannerData.color))
        } else {
            binding.relMain.setBackgroundColor(Color.parseColor("#49626C"))
        }

        if (!Util.isEmptyString(bannerData.text_color)) {
            binding.bannerTitle.setTextColor(Color.parseColor("#" + bannerData.text_color))
        } else {
            binding.bannerTitle.setTextColor(Color.parseColor("#ffffff"))
        }
        binding.relMain.setOnClickListener(View.OnClickListener {
            mCallback.onBannerClicked(bannerData)
        })
    }
}