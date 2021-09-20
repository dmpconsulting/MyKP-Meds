package com.montunosoftware.pillpopper.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.view.CalendarViewPager;
import com.montunosoftware.pillpopper.android.view.CalendarWeekFragmentNew;
import com.montunosoftware.pillpopper.model.PillpopperDay;

import java.util.ArrayList;
import java.util.List;

/**
 * @author
 * Created by adhithyaravipati on 10/26/16.
 */
public class ScheduleCalendarFragmentNew extends Fragment {

    private ViewPager mCalendarViewPager;
    private ViewPager.OnPageChangeListener mOnPageChangeListener;
    private CalendarViewPagerAdapter mCalendarViewPagerAdapter;

    private List<CalendarWeekFragmentNew> mCalendarAdapterList;

    private int mCurrentViewPagerPosition;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        return inflater.inflate(R.layout.schedule_calendar_base_layout, container, false);
    }

    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {
        mCalendarViewPager = (CalendarViewPager) view.findViewById(R.id.calendar_view_pager);
        initCalendarViewPagerAdapterList();
        mCalendarViewPagerAdapter = new CalendarViewPagerAdapter(getChildFragmentManager(), mCalendarAdapterList);
        mCalendarViewPager.setAdapter(mCalendarViewPagerAdapter);

        mOnPageChangeListener = new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                mCurrentViewPagerPosition = position;
                if(position > mCalendarAdapterList.size() - 2) {
                    mCalendarAdapterList.add(CalendarWeekFragmentNew.newInstance(
                            (mCalendarAdapterList.get(mCalendarAdapterList.size()-1))
                                    .getWeekStartDay().addDays(7), getActivity(), mCalendarAdapterList, mCalendarAdapterList.size())
                    );
                    mCalendarViewPagerAdapter.notifyDataSetChanged();
                }

                PillpopperDay scheduleFocusDay = ((PillpopperActivity) getActivity()).getState().getScheduleViewDay();
                mCalendarAdapterList.get(mCurrentViewPagerPosition).setSelectedDayInWeekWithCallback(scheduleFocusDay);
                if(mCurrentViewPagerPosition > 0) {
                    mCalendarAdapterList.get(mCurrentViewPagerPosition - 1).setSelectedDayInWeek(scheduleFocusDay);
                }
                mCalendarAdapterList.get(mCurrentViewPagerPosition + 1).setSelectedDayInWeek(scheduleFocusDay);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                switch (state) {
                    case ViewPager.SCROLL_STATE_IDLE:

                        break;

                }

            }
        };
        mCalendarViewPager.addOnPageChangeListener(mOnPageChangeListener);

        int currentViewPagerIndex = (int) (((((PillpopperActivity)getActivity()).getState().getScheduleViewDay()).daysAfter(getWeekStartDayFromToday())) / 7);
        mCalendarViewPager.setCurrentItem(currentViewPagerIndex);
        mOnPageChangeListener.onPageSelected(mCalendarViewPager.getCurrentItem());
    }

    @Override
    public void onStart() {
        super.onStart();
        int currentViewPagerIndex = (int) (((((PillpopperActivity)getActivity()).getState().getScheduleViewDay()).daysAfter(getWeekStartDayFromToday())) / 7);
        mCalendarViewPager.setCurrentItem(currentViewPagerIndex);
        mOnPageChangeListener.onPageSelected(mCalendarViewPager.getCurrentItem());
    }

    private void initCalendarViewPagerAdapterList() {
        PillpopperDay scheduleFocusDay = ((PillpopperActivity) getActivity()).getState().getScheduleViewDay();
        mCalendarAdapterList = new ArrayList<>();
        int numberOfWeeksToGenerate = ((int) (scheduleFocusDay.daysAfter(PillpopperDay.today()) / 7)) + 5;
        for(int weekCounter = 0 ; weekCounter < numberOfWeeksToGenerate ; weekCounter++) {
            CalendarWeekFragmentNew weekFragment = CalendarWeekFragmentNew.newInstance(getWeekStartDayFromToday().addDays(weekCounter * 7), getActivity(), mCalendarAdapterList, weekCounter);
            mCalendarAdapterList.add(weekFragment);
        }

    }

    private PillpopperDay getWeekStartDayFromToday() {
        PillpopperDay pillpopperDay = PillpopperDay.today();
        if(pillpopperDay.getDayOfWeek().getDayNumber() > 1) {
            pillpopperDay = pillpopperDay.addDays(-1 * (pillpopperDay.getDayOfWeek().getDayNumber() - 1));
        }
        return pillpopperDay;
    }


    public static class CalendarViewPagerAdapter
        extends FragmentStatePagerAdapter {

        private List<CalendarWeekFragmentNew> fragments;

        public CalendarViewPagerAdapter(FragmentManager fm, List<CalendarWeekFragmentNew> fragments) {
            super(fm);
            this.fragments = fragments;
        }

        @Override
        public Fragment getItem(int position) {
            return this.fragments.get(position);
        }

        @Override
        public int getCount() {
            return this.fragments.size();
        }
    }

}
