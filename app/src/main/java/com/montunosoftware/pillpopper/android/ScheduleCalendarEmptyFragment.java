package com.montunosoftware.pillpopper.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;

import com.montunosoftware.mymeds.R;

/**@author
 * Created by adhithyaravipati on 4/12/16.
 */
public class ScheduleCalendarEmptyFragment extends Fragment {

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.schedule_calendar_empty_layout, container, false);
        return view;
    }
}
