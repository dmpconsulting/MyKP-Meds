package com.montunosoftware.pillpopper.android;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;
import android.widget.RelativeLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import org.kp.tpmg.ttg.R;
import org.kp.tpmg.ttg.utils.MultiClickViewPreventHandler;
import org.kp.tpmg.ttg.views.pharmacylocator.model.PharmacyLocatorObj;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by M1023050 on 28-Nov-18.
 */

public class PharmacyLocatorListAdapter extends RecyclerView.Adapter<PharmacyLocatorListAdapter.ViewHolder> implements Filterable {

    private List<PharmacyLocatorObj> mPharmacyLocatorObjList;
    private Context mContext;
    private StringBuilder distanceString;
    private List<PharmacyLocatorObj> pharmacyListFiltered;
    private OnItemClickListener mOnItemClickListener;

    public PharmacyLocatorListAdapter(Context context, List<PharmacyLocatorObj> mPharmacyLocatorObjList, PharmacyLocatorListAdapter.OnItemClickListener listener) {
        this.mContext = context;
        this.mPharmacyLocatorObjList = mPharmacyLocatorObjList;
        pharmacyListFiltered = mPharmacyLocatorObjList;
        mOnItemClickListener = listener;
    }

    @NonNull
    @Override
    public PharmacyLocatorListAdapter.ViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
        View view = LayoutInflater.from(viewGroup.getContext()).inflate(R.layout.pharmacy_locator_row, viewGroup, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PharmacyLocatorListAdapter.ViewHolder viewHolder, int position) {
        ViewHolder pharmacyViewHolder = viewHolder;
        pharmacyViewHolder.tvFacilityName.setText(pharmacyListFiltered.get(position).getOfficialName());
        pharmacyViewHolder.tvDepartmentName.setText(pharmacyListFiltered.get(position).getDepartmentName());
        pharmacyViewHolder.tvStreetAddress.setText(pharmacyListFiltered.get(position).getStreet());
        pharmacyViewHolder.tvCityStateZip.setText(pharmacyListFiltered.get(position).getCityStateAndZip());

        distanceString = new StringBuilder();
        if(pharmacyListFiltered.get(position).getDistance() != null) {
            distanceString.append(String.format("%.1f", pharmacyListFiltered.get(position).getDistance())).append(" mi");
            pharmacyViewHolder.tvDistance.setText(distanceString.toString());
        } else{
            pharmacyViewHolder.tvDistance.setText("");
        }

        pharmacyViewHolder.rowLayout.setOnClickListener(view -> {
            mOnItemClickListener.onItemClick(pharmacyListFiltered.get(position));
        });

        pharmacyViewHolder.listRow.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                MultiClickViewPreventHandler.preventMultiClick(view);
                mOnItemClickListener.onItemClick(pharmacyListFiltered.get(position));
            }
        });
    }

    @Override
    public int getItemCount() {
        return pharmacyListFiltered.size();
    }

    @Override
    public Filter getFilter() {
        {
            return new Filter() {
                @Override
                protected FilterResults performFiltering(CharSequence charSequence) {
                    String charString = charSequence.toString();
                    if (charString.isEmpty()) {
                        pharmacyListFiltered = mPharmacyLocatorObjList;
                    } else {
                        List<PharmacyLocatorObj> filteredList = new ArrayList<>();
                        for (PharmacyLocatorObj row : mPharmacyLocatorObjList) {

                            // facility official name, department name, (city, state and zip) filter
                            if (row.getOfficialName().toLowerCase().trim().contains(charString.toLowerCase())
                                    || row.getDepartmentName().toLowerCase().trim().contains(charSequence)
                                    || row.getCityStateAndZip().toLowerCase().trim().contains(charSequence)
                                    || row.getStreet().toLowerCase().trim().contains(charSequence)) {
                                filteredList.add(row);
                            }
                        }

                        pharmacyListFiltered = filteredList;
                    }

                    FilterResults filterResults = new FilterResults();
                    filterResults.values = pharmacyListFiltered;
                    return filterResults;
                }

                @Override
                protected void publishResults(CharSequence charSequence, FilterResults filterResults) {
                    pharmacyListFiltered = (List<PharmacyLocatorObj>) filterResults.values;
                    notifyDataSetChanged();
                }
            };
        }
    }



    public class ViewHolder extends RecyclerView.ViewHolder {

        RelativeLayout listRow;
        TextView tvFacilityName;
        TextView tvDepartmentName;
        TextView tvStreetAddress;
        TextView tvCityStateZip;
        TextView tvDistance;
        RelativeLayout rowLayout;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            listRow  = itemView.findViewById(R.id.layout_row);
            tvFacilityName = itemView.findViewById(R.id.tv_facility_name);
            tvDepartmentName = itemView.findViewById(R.id.tv_dept_name);
            tvStreetAddress = itemView.findViewById(R.id.tv_street);
            tvCityStateZip = itemView.findViewById(R.id.tv_city_state_zip);
            tvDistance = itemView.findViewById(R.id.distance_text);
            rowLayout = itemView.findViewById(R.id.layout_row);
        }
    }



    public interface OnItemClickListener {
        void onItemClick(PharmacyLocatorObj pharmacyLocatorObj);
    }

    public void setOnItemClickListener(PharmacyLocatorListAdapter.OnItemClickListener onItemClickListener) {
        mOnItemClickListener = onItemClickListener;
    }

}
