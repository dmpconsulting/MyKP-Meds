package com.montunosoftware.pillpopper.android;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.TextView;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.util.FDADrugDatabase;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;

import java.util.HashMap;
import java.util.List;

@SuppressLint("InflateParams")
public class AddDrugExpandableList extends BaseExpandableListAdapter
{

	private Context mcontext;
	private List<String> mlistDataHeader; // header titles
	// child data in format of header title, child title
	private HashMap<String, List<FDADrugDatabase.DatabaseDrugVariant>> mlistDataChild;

	public AddDrugExpandableList(Context context, List<String> listDataHeader,
			HashMap<String, List<FDADrugDatabase.DatabaseDrugVariant>> listChildData) {
		this.mcontext = context;
		this.mlistDataHeader = listDataHeader;
		this.mlistDataChild = listChildData;
	}

	@Override
	public Object getChild(int groupPosition, int childPosititon) {
		return this.mlistDataChild.get(this.mlistDataHeader.get(groupPosition))
				.get(childPosititon);
	}

	@Override
	public long getChildId(int groupPosition, int childPosition) {
		return childPosition;
	}

	@Override
	public View getChildView(int groupPosition, final int childPosition,
			boolean isLastChild, View convertView, ViewGroup parent) {

		FDADrugDatabase.DatabaseDrugVariant databaseDrugVariant = (FDADrugDatabase.DatabaseDrugVariant)getChild(groupPosition, childPosition);

		if (convertView == null) {
			LayoutInflater infalInflater = (LayoutInflater) this.mcontext
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = infalInflater.inflate(R.layout.child_row, null);
		}

		TextView txtListChild = convertView
				.findViewById(R.id.name);

		txtListChild.setText(databaseDrugVariant.toString());
		return convertView;
	}

	@Override
	public int getChildrenCount(int groupPosition) {
		return this.mlistDataChild.get(this.mlistDataHeader.get(groupPosition)).size();
	}

	@Override
	public Object getGroup(int groupPosition) {
		return this.mlistDataHeader.get(groupPosition);
	}

	@Override
	public int getGroupCount() {
		return this.mlistDataHeader.size();
	}

	@Override
	public long getGroupId(int groupPosition) {
		return groupPosition;
	}

	@Override
	public View getGroupView(int groupPosition, boolean isExpanded,
			View convertView, ViewGroup parent) {
		String headerTitle = "";
		try {
			headerTitle = (String) getGroup(groupPosition);
			if (convertView == null) {
				LayoutInflater infalInflater = (LayoutInflater) this.mcontext
						.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
				convertView = infalInflater.inflate(R.layout.group_row, null);
			}

			TextView lblListHeader = convertView
					.findViewById(R.id.heading);		
			lblListHeader.setText(headerTitle);
		} catch (Exception ex) {
			PillpopperLog.say(ex);
		}
		return convertView;
	}

	@Override
	public boolean hasStableIds() {
		return false;
	}

	@Override
	public boolean isChildSelectable(int groupPosition, int childPosition) {
		return true;
	}
}
