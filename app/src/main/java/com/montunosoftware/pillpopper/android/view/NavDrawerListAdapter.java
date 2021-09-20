package com.montunosoftware.pillpopper.android.view;

import android.app.Activity;
import android.content.Context;
import android.graphics.Typeface;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.model.NavDrawerItem;

import org.kp.tpmg.ttg.RxRefillConstants;
import org.kp.tpmg.ttg.utils.RxRefillUtils;

import java.util.ArrayList;


public class NavDrawerListAdapter extends BaseAdapter {

	private Context context;
	private ArrayList<NavDrawerItem> navDrawerItems;
	private static final int NAV_DRAWER_SEPARATOR_POSITION = 7;

	int[] navIdContentDescrition = new int[]{R.string.content_description_nav_home,
			R.string.content_description_nav_medications,
			R.string.content_description_nav_daily_schedule,
			R.string.content_description_nav_medication_reminders,
			R.string.content_description_nav_refill_reminders,
			R.string.content_description_nav_prescription_refills,
			R.string.content_description_nav_history,
			R.string.empty,
			R.string.content_description_nav_find_pharmacy,
			R.string.content_description_nav_settings,
			R.string.content_description_nav_archive,
			R.string.content_description_nav_guide,
			R.string.content_description_nav_support} ;


	public NavDrawerListAdapter(Context context, ArrayList<NavDrawerItem> navDrawerItems){
		this.context = context;
		this.navDrawerItems = navDrawerItems;
	}

	@Override
	public int getCount() {
		return navDrawerItems.size();
	}

	@Override
	public Object getItem(int position) {
		return navDrawerItems.get(position);
	}

	@Override
	public int getItemViewType(int position) {
		return position;
	}
	@Override
	public int getViewTypeCount() {

		return getCount();
	}
	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View rowView = convertView;
		ViewHolder viewHolder = new ViewHolder();
		if (rowView == null) {

			if (position == NAV_DRAWER_SEPARATOR_POSITION) {
				LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
				rowView = mInflater.inflate(R.layout.nav_drawer_item_separator, null);
			} else {
				LayoutInflater mInflater = (LayoutInflater) context.getSystemService(Activity.LAYOUT_INFLATER_SERVICE);
				rowView = mInflater.inflate(R.layout.drawer_list_item, null);
				viewHolder.txtTitle = rowView.findViewById(R.id.nav_title);
				viewHolder.imgIcon = rowView.findViewById(R.id.nav_icon);
				viewHolder.txtTitle1 = rowView.findViewById(R.id.nav_title_1);

				Typeface mFontRegular = RxRefillUtils.setFontStyle(parent.getContext(), RxRefillConstants.FONT_ROBOTO_REGULAR);
				viewHolder.txtTitle.setTypeface(mFontRegular);
				viewHolder.txtTitle1.setTypeface(mFontRegular);
			}
			rowView.setTag(viewHolder);
			rowView.setContentDescription(context.getResources().getString(navIdContentDescrition[position]));
			if (position != NAV_DRAWER_SEPARATOR_POSITION) {
				ViewHolder holder = (ViewHolder) rowView.getTag();
				try {
					holder.txtTitle.setText(navDrawerItems.get(position).getTitle());
					holder.txtTitle1.setText(navDrawerItems.get(position).getTitle());

					if (!navDrawerItems.get(position).getTitle().equalsIgnoreCase(context.getResources().getString(R.string.button_find_pharmacy)) &&
							!navDrawerItems.get(position).getTitle().equalsIgnoreCase(context.getResources().getString(R.string.settings)) &&
							!navDrawerItems.get(position).getTitle().equalsIgnoreCase(context.getResources().getString(R.string.guide)) &&
							!navDrawerItems.get(position).getTitle().equalsIgnoreCase(context.getResources().getString(R.string.archive_)) &&
							!navDrawerItems.get(position).getTitle().equalsIgnoreCase(context.getResources().getString(R.string.support))) {
						holder.imgIcon.setVisibility(View.VISIBLE);
						holder.txtTitle.setVisibility(View.VISIBLE);
						holder.txtTitle1.setVisibility(View.GONE);
						holder.imgIcon.setImageResource(navDrawerItems.get(position).getIcon());
					} else {
						holder.imgIcon.setVisibility(View.GONE);
						holder.txtTitle.setVisibility(View.GONE);
						holder.txtTitle1.setVisibility(View.VISIBLE);
					}
				} catch (Exception ne) {
					PillpopperLog.exception(ne.getMessage());
				}
            }
		}
		return rowView;
	}

	static class ViewHolder {
		public TextView txtTitle1;
		private TextView txtTitle;
		private ImageView imgIcon;
	}

}
