package com.montunosoftware.pillpopper.android.view;

import android.content.Context;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.fragment.app.Fragment;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.PillpopperActivity;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.model.PillpopperDay;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;

import java.util.List;

/**
 * @author Created by adhithyaravipati on 10/26/16.
 */
public class CalendarWeekFragmentNew extends Fragment {

    public interface OnDateSelectedListener {
        void onDateSelected(PillpopperDay selectedDay);
    }

    private View mInflatedView;

    private PillpopperDay mWeekStartDay;
    private PillpopperDay mSelectedDayInWeek;

    private Context mContext;

    private OnDateSelectedListener mOnDateSelectedListener;

    private List<CalendarWeekFragmentNew> mParentViewPagerAdapter;
    private int mPositionInParentViewPager;
    private Typeface mFontRegular;
    private Typeface mFontBold;


    private final LinearLayout[] mDayLinearLayout = new LinearLayout[7];
    private final TextView[] mDateTextView = new TextView[7];
    private final TextView[] mDayTextView = new TextView[7];
    private final View[] mCurrentDayIndicatorView = new View[7];

    public static final CalendarWeekFragmentNew newInstance(PillpopperDay weekStartDay,
                                                            Context context, List<CalendarWeekFragmentNew> parentViewPagerAdapter,
                                                            int positionInViewPager) {
        CalendarWeekFragmentNew fragment = new CalendarWeekFragmentNew();
        LoggerUtils.info("weekStartDay_"+weekStartDay+"positionInViewPager_"+positionInViewPager);
        fragment.setWeekStartDay(weekStartDay);
        fragment.setmContext(context);
        fragment.setmParentViewPagerAdapter(parentViewPagerAdapter);
        fragment.setPositionInParentViewPager(positionInViewPager);
        return fragment;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        mInflatedView = inflater.inflate(R.layout.schedule_calendar_week_layout, container, false);
        mFontBold = ActivationUtil.setFontStyle(getActivity(), AppConstants.FONT_ROBOTO_BOLD);
        mFontRegular = ActivationUtil.setFontStyle(getActivity(), AppConstants.FONT_ROBOTO_REGULAR);
        return mInflatedView;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mOnDateSelectedListener = (OnDateSelectedListener) context;
            mContext = context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString() + " must implement OnCalendarDateSelectedListener");
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        PillpopperDay pillpopperDay;
        initUi(mInflatedView);
        populateUi();
        if (mContext != null
                && ((PillpopperActivity) mContext).getState() != null
                && ((PillpopperActivity) mContext).getState().getScheduleViewDay() != null) {
            pillpopperDay = ((PillpopperActivity) mContext).getState().getScheduleViewDay();
        } else {
            pillpopperDay = mWeekStartDay.addDays(PillpopperDay.today().getDayOfWeek().getDayNumber() - 1);
        }

        setSelectedDayInWeek(pillpopperDay);
    }


    private void initUi(View view) {
        mDayLinearLayout[0] = view.findViewById(R.id.schedule_calendar_sunday_holder);
        mDayLinearLayout[1] = view.findViewById(R.id.schedule_calendar_monday_holder);
        mDayLinearLayout[2] = view.findViewById(R.id.schedule_calendar_tuesday_holder);
        mDayLinearLayout[3] = view.findViewById(R.id.schedule_calendar_wednesday_holder);
        mDayLinearLayout[4] = view.findViewById(R.id.schedule_calendar_thursday_holder);
        mDayLinearLayout[5] = view.findViewById(R.id.schedule_calendar_friday_holder);
        mDayLinearLayout[6] = view.findViewById(R.id.schedule_calendar_saturday_holder);

        for (int index = 0; index < 7; index++) {
            mDateTextView[index] = (TextView) mDayLinearLayout[index].getChildAt(1);
            mDayTextView[index] = (TextView) mDayLinearLayout[index].getChildAt(0);
            mCurrentDayIndicatorView[index] = mDayLinearLayout[index].getChildAt(2);
            mDateTextView[index].setTypeface(mFontBold);
            mDayTextView[index].setTypeface(mFontRegular);
        }

        initUiColorsAndListeners();
    }

    private void initUiColorsAndListeners() {
        for (int index = 0; index < 7; index++) {
            final PillpopperDay pillpopperDay = mWeekStartDay.addDays(index);

            if (pillpopperDay.before(PillpopperDay.today())) {
                mDateTextView[index].setTextColor(Util.getColorWrapper(mContext, R.color.schedule_past_date_color));
                mDayTextView[index].setTextColor(Util.getColorWrapper(mContext, R.color.create_rem_txt_color));
            } else {
                mDateTextView[index].setTextColor(Util.getColorWrapper(mContext, R.color.black));
                mDayTextView[index].setTextColor(Util.getColorWrapper(mContext, R.color.create_rem_txt_color));
                mDayLinearLayout[index].setOnClickListener(
                        v -> {
                            setSelectedDayInWeekWithCallback(pillpopperDay);
                            if (mPositionInParentViewPager > 0) {
                                mParentViewPagerAdapter.get(mPositionInParentViewPager - 1).setSelectedDayInWeek(pillpopperDay);
                            }
                            mParentViewPagerAdapter.get(mPositionInParentViewPager + 1).setSelectedDayInWeek(pillpopperDay);
                        }
                );
            }
        }
    }


    private void populateUi() {
        for (int index = 0; index < 7; index++) {
            PillpopperDay pillpopperDay = mWeekStartDay.addDays(index);
            mDateTextView[index].setText(Integer.toString(pillpopperDay.getDay()));
            if (pillpopperDay.equals(PillpopperDay.today())) {
                mDateTextView[index].setBackgroundResource(R.drawable.select_days_background_circle);
            }
        }
    }

    public void setOnDateSelectedListener(OnDateSelectedListener onDateSelectedListener) {
        this.mOnDateSelectedListener = onDateSelectedListener;
    }

    public void setSelectedDayInWeek(PillpopperDay selectedDayInWeek) {
        PillpopperDay pillpopperDay = mWeekStartDay.addDays(selectedDayInWeek.getDayOfWeek().getDayNumber() - 1);
        if (pillpopperDay.before(PillpopperDay.today())) {
            selectedDayInWeek = PillpopperDay.today();
        }
        setSelectedDayInWeekUi(selectedDayInWeek);
    }

    private void setSelectedDayInWeekUi(PillpopperDay selectedDayInWeek) {
        if (mInflatedView != null) {
            populateUi();
            int index = selectedDayInWeek.getDayOfWeek().getDayNumber() - 1;
            if (mSelectedDayInWeek != null) {
                int oldIndexSelected = mSelectedDayInWeek.getDayOfWeek().getDayNumber() - 1;
                mDateTextView[oldIndexSelected].setBackgroundResource(R.color.white);
                mDateTextView[oldIndexSelected].setTextColor(Util.getColorWrapper(mContext, R.color.black));
                mDayTextView[oldIndexSelected].setTextColor(Util.getColorWrapper(mContext, R.color.create_rem_txt_color));
                populateUi();

            }
            mSelectedDayInWeek = selectedDayInWeek;
            mDateTextView[index].setBackgroundResource(R.drawable.select_date_background_circle_blue);
            mDateTextView[index].setTextColor(Util.getColorWrapper(mContext, R.color.white));
        }
    }


    public void setSelectedDayInWeekWithCallback(PillpopperDay selectedDayInWeek) {
        setSelectedDayInWeek(selectedDayInWeek);
        if (this.mOnDateSelectedListener != null) {
            PillpopperDay callbackPillpopperDay = mWeekStartDay.addDays(selectedDayInWeek.getDayOfWeek().getDayNumber() - 1);
            if (callbackPillpopperDay.before(PillpopperDay.today())) {
                callbackPillpopperDay = PillpopperDay.today();
            }
            this.mOnDateSelectedListener.onDateSelected(callbackPillpopperDay);
        }
    }

    public void setWeekStartDay(PillpopperDay weekStartDay) {
        this.mWeekStartDay = weekStartDay;
    }

    public PillpopperDay getWeekStartDay() {
        return mWeekStartDay;
    }

    public Context getmContext() {
        return mContext;
    }

    public void setmContext(Context mContext) {
        this.mContext = mContext;
    }

    public List<CalendarWeekFragmentNew> getmParentViewPagerAdapter() {
        return mParentViewPagerAdapter;
    }

    public void setmParentViewPagerAdapter(List<CalendarWeekFragmentNew> mParentViewPagerAdapter) {
        this.mParentViewPagerAdapter = mParentViewPagerAdapter;
    }

    public int getPositionInParentViewPager() {
        return mPositionInParentViewPager;
    }

    public void setPositionInParentViewPager(int positionInParentViewPager) {
        this.mPositionInParentViewPager = positionInParentViewPager;
    }
}
