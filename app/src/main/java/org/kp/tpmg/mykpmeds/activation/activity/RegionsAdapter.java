package org.kp.tpmg.mykpmeds.activation.activity;

import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.montunosoftware.mymeds.R;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;

public class RegionsAdapter extends RecyclerView.Adapter<RegionsAdapter.RegionHolder> {

    private final Typeface mRobotoRegular;
    private Context mContext;
    private String[] mRegionsList;

    public RegionsAdapter(Context context, String[] regionsList) {
        this.mContext = context;
        this.mRegionsList = regionsList;
        mRobotoRegular = ActivationUtil.setFontStyle(mContext, AppConstants.FONT_ROBOTO_REGULAR);
    }

    @NonNull
    @Override
    public RegionHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.sign_in_help_region_child, viewGroup, false);
        return new RegionHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull RegionHolder regionHolder, int position) {
        if(position % 2 == 0) {
            regionHolder.mRegionText.setText(mRegionsList[position/2].trim());
        } else {
            if(mRegionsList.length % 2 == 0) {
                // if the list is having even number of elements
                regionHolder.mRegionText.setText(mRegionsList[mRegionsList.length / 2 + position / 2].trim());
            }else{
                regionHolder.mRegionText.setText(mRegionsList[mRegionsList.length / 2 + position / 2 + 1].trim());
            }
        }
    }

    @Override
    public int getItemCount() {
        return mRegionsList.length;
    }

    class RegionHolder extends RecyclerView.ViewHolder {

        private TextView mRegionText;

        public RegionHolder(@NonNull View itemView) {
            super(itemView);
            mRegionText = itemView.findViewById(R.id.region);
            mRegionText.setTypeface(mRobotoRegular);
        }
    }
}
