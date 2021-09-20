package com.montunosoftware.pillpopper.android.home;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.recyclerview.widget.RecyclerView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.model.DiscontinuedDrug;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;

import java.util.List;

/**
 * Created by M1023050 on 5/7/2018.
 */

public class KPHCDiscontinueCardExpandedAdapter extends RecyclerView.Adapter<KPHCDiscontinueCardExpandedAdapter.DiscontinuedDrugViewHolder> {

    private Context mContext;
    private List<DiscontinuedDrug> mDiscontinuedDrugList;

    private static final int TYPE_MEDICATION_WITH_PROXY_HEADER_HOLDER = 1;
    private static final int TYPE_MEDICATION_HOLDER = 2;
    private Typeface mRobotoRegular;
    private Typeface mRobotoMedium;

    public KPHCDiscontinueCardExpandedAdapter(Context context, List<DiscontinuedDrug> discontinuedDrugList) {
        this.mContext = context.getApplicationContext();
        this.mDiscontinuedDrugList = discontinuedDrugList;
        mRobotoRegular = ActivationUtil.setFontStyle(context, AppConstants.FONT_ROBOTO_REGULAR);
        mRobotoMedium = ActivationUtil.setFontStyle(context,AppConstants.FONT_ROBOTO_MEDIUM);
    }

    @Override
    public DiscontinuedDrugViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
        View view = null;
        switch (viewType) {
            case TYPE_MEDICATION_WITH_PROXY_HEADER_HOLDER:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.discontinued_medication_alert_with_proxy_header_holder, viewGroup, false);
                return new DiscontinuedDrugWithProxyHeaderViewHolder(view);
            case TYPE_MEDICATION_HOLDER:
                view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.discontinued_medication_alert_medication_holder, viewGroup, false);
                return new DiscontinuedDrugViewHolder((view));
        }
        return new DiscontinuedDrugViewHolder(view);
    }

    @Override
    public void onBindViewHolder(DiscontinuedDrugViewHolder holder, int position) {

        int index = position + 1;

        switch (getItemViewType(position)) {
            case TYPE_MEDICATION_WITH_PROXY_HEADER_HOLDER:
                DiscontinuedDrugWithProxyHeaderViewHolder discontinuedDrugWithProxyHeaderViewHolder = (DiscontinuedDrugWithProxyHeaderViewHolder) holder;
                discontinuedDrugWithProxyHeaderViewHolder.proxyNameTextView.setText(mDiscontinuedDrugList.get(position).getUserFirstName());
                discontinuedDrugWithProxyHeaderViewHolder.medicationBrandNameTextView.setText(mDiscontinuedDrugList.get(position).getBrandName());
                discontinuedDrugWithProxyHeaderViewHolder.medicationDosageTextView.setText(mDiscontinuedDrugList.get(position).getDosage());
                break;
            case TYPE_MEDICATION_HOLDER:
                holder.medicationBrandNameTextView.setText(mDiscontinuedDrugList.get(position).getBrandName());
                holder.medicationDosageTextView.setText(mDiscontinuedDrugList.get(position).getDosage());
                break;
        }

        // adjust the layout gap between two users.
        if(position != 0 && index < mDiscontinuedDrugList.size()) {
            if (!mDiscontinuedDrugList.get(position).getUserId().equalsIgnoreCase(mDiscontinuedDrugList.get(index).getUserId())) {
                adjustMarginForLayouts(position, holder);
            }
        }
    }

    private void adjustMarginForLayouts(int position, DiscontinuedDrugViewHolder holder) {
        LinearLayout.LayoutParams params =  new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
        params.setMargins(0,0, Util.convertToDp(24, mContext), Util.convertToDp(48, mContext));
        if(getItemViewType(position) == TYPE_MEDICATION_WITH_PROXY_HEADER_HOLDER){
            DiscontinuedDrugWithProxyHeaderViewHolder discontinuedDrugWithProxyHeaderViewHolder = (DiscontinuedDrugWithProxyHeaderViewHolder) holder;
            discontinuedDrugWithProxyHeaderViewHolder.mDrugDetailsLayout.setLayoutParams(params);
        }else{
            holder.mDrugDetailsLayout.setLayoutParams(params);
        }
    }

    @Override
    public int getItemCount() {
        return mDiscontinuedDrugList.size();
    }

    @Override
    public int getItemViewType(int position) {
        if (position == 0) {
            return TYPE_MEDICATION_WITH_PROXY_HEADER_HOLDER;
        }

        if (mDiscontinuedDrugList.get(position).getUserId()
                .equals(mDiscontinuedDrugList.get(position - 1).getUserId())) {
            return TYPE_MEDICATION_HOLDER;
        }

        return TYPE_MEDICATION_WITH_PROXY_HEADER_HOLDER;
    }

    public class DiscontinuedDrugViewHolder extends RecyclerView.ViewHolder {

        TextView medicationBrandNameTextView;
        TextView medicationDosageTextView;
        LinearLayout mDrugDetailsLayout;

        public DiscontinuedDrugViewHolder(View view) {
            super(view);
            medicationBrandNameTextView = view.findViewById(R.id.discontinued_medication_brand_name_textview);
            medicationDosageTextView = view.findViewById(R.id.discontinued_medication_dosage_textview);
            mDrugDetailsLayout = view.findViewById(R.id.ll_drug_details);
            medicationBrandNameTextView.setTypeface(mRobotoMedium);
            medicationDosageTextView.setTypeface(mRobotoRegular);
        }
    }

    public class DiscontinuedDrugWithProxyHeaderViewHolder extends DiscontinuedDrugViewHolder {

        TextView proxyNameTextView;

        public DiscontinuedDrugWithProxyHeaderViewHolder(View view) {
            super(view);
            proxyNameTextView = view.findViewById(R.id.discontinued_medication_proxy_name_textview);
            proxyNameTextView.setTypeface(mRobotoRegular);
        }
    }

}