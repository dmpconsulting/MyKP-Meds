package com.montunosoftware.pillpopper.android;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.cardview.widget.CardView;

import com.montunosoftware.mymeds.R;

public class GreatJobAlertForTakenAllActivity extends Activity {

    //    private RelativeLayout mRLSlideDown;
    private Animation mSlideDownAnim;
    private CardView mCardView;
    private String launchingFrom;
    private final int SLIDE_DOWN_DURATION = 300;
    private final int SLIDE_DOWN_START_ANIM_DELAY = 1150;
    private final int GREAT_JOB_FINISH_ACT_DELAY = 2300;
    private long mTakenEarlier;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.great_job_lyt_act);

        TextView undo = findViewById(R.id.great_job_undo);
        TextView greatJobText = findViewById(R.id.great_job_text);
        LinearLayout greatJobLayout = findViewById(R.id.ll_grey_undo);
        ImageView great_job_icon = findViewById(R.id.great_job_img);
        TextView greatJobTitle = findViewById(R.id.great_job_title);
        mCardView = findViewById(R.id.card_view);

        Intent intent = getIntent();

        if (null != intent) {
            launchingFrom = intent.getStringExtra("LaunchMode");
            mTakenEarlier = intent.getLongExtra("TakenEarlier", -1);

            mSlideDownAnim = AnimationUtils.loadAnimation(this,
                    ("AddToHistory").equalsIgnoreCase(launchingFrom) ? R.anim.record_dose_slide_down : R.anim.slide_down);
            mSlideDownAnim.setDuration(SLIDE_DOWN_DURATION);

            if (null != launchingFrom && ("LateReminders").equalsIgnoreCase(launchingFrom)) {
                mCardView.setVisibility(View.GONE);
                undo.setVisibility(View.GONE);
                great_job_icon.setImageResource(R.drawable.action_greatjob);
                greatJobText.setText(R.string.action_taken_msg);
                greatJobTitle.setVisibility(View.VISIBLE);
                startAnimation();
            } else if (null != launchingFrom && ("AddToHistory").equalsIgnoreCase(launchingFrom)) {
                setContentView(R.layout.record_dose_layout);
                mCardView = findViewById(R.id.card_view);
                mCardView.setVisibility(View.GONE);
                startAnimation();
            } else if (null != launchingFrom && ("passedReminders").equalsIgnoreCase(launchingFrom)) {
                mCardView.setVisibility(View.GONE);
                greatJobLayout.setVisibility(View.GONE);
                great_job_icon.setImageResource(R.drawable.alert_checkmark);
                greatJobText.setText(R.string.passed_reminders_keep_it_up_message);
                greatJobTitle.setVisibility(View.GONE);
            } else {
                mCardView.setVisibility(View.GONE);
                undo.setVisibility(View.GONE);
                great_job_icon.setImageResource(R.drawable.action_greatjob);
                greatJobText.setText(R.string.action_taken_msg);
                greatJobTitle.setVisibility(View.VISIBLE);
                startAnimation();
            }
        }

        undo.setOnClickListener(v -> {
           /* GoogleAnalyticTracker.getInstance(GreatJobAlertForTakenAllActivity.this)
                    .sendEvent(GoogleAnalyticConstants.CATEGORY_REMINDERS,
                            GoogleAnalyticConstants.ACTION_REMINDERS,
                            GoogleAnalyticConstants.LABEL_GREAT_JOB_UNDO);
            Intent intent = new Intent();
            PillpopperRunTime.getInstance().setNeedtoShowAsNeeded(true);
            setResult(RESULT_CANCELED, intent);
            finish();*/
        });


    }

    @Override
    protected void onResume() {
        super.onResume();
        if (null != launchingFrom && !("AddToHistory").equalsIgnoreCase(launchingFrom)) {
            startAnimation();
        }
    }

    private void startAnimation() {
        new Handler().postDelayed(() -> {
            mCardView.setVisibility(View.VISIBLE);
            mCardView.startAnimation(mSlideDownAnim);
        }, SLIDE_DOWN_START_ANIM_DELAY);

        new Handler().postDelayed(() -> {
            if (!isFinishing()) {
                Intent intent = new Intent();
                intent.putExtra("TakenEarlierTime", mTakenEarlier);
                setResult(RESULT_OK, intent);
                finish();
            }
        }, GREAT_JOB_FINISH_ACT_DELAY);
    }
}