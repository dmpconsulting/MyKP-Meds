package com.montunosoftware.pillpopper.android.util;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.text.InputType;
import android.widget.EditText;
import android.widget.NumberPicker;
import android.widget.TimePicker;

import com.montunosoftware.mymeds.R;

import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * @author Created by adhithyaravipati on 9/30/16.
 */
public class UIUtils {

    private static ProgressDialog mProgressDialog;

    public static void applyDefaultTimePickerStyle(Context context, TimePicker timePicker) {
        Resources system = Resources.getSystem();
        int hourNumberPickerId = system.getIdentifier("hour", "id", "android");
        int minuteNumberPickerId = system.getIdentifier("minute", "id", "android");
        int ampmNumberPickerId = system.getIdentifier("amPm", "id", "android");

        NumberPicker hourNumberPicker = timePicker.findViewById(hourNumberPickerId);
        NumberPicker minuteNumberPicker = timePicker.findViewById(minuteNumberPickerId);
        NumberPicker ampmNumberPicker = timePicker.findViewById(ampmNumberPickerId);

        setNumberPickerDividerColour(context, hourNumberPicker, R.color.kp_theme_blue);
        setNumberPickerDividerColour(context, minuteNumberPicker, R.color.kp_theme_blue);
        setNumberPickerDividerColour(context, ampmNumberPicker, R.color.kp_theme_blue);
    }

    public static void applyDefaultTimePickerStyle(Context context, TimePicker timePicker, final int minutesInterval) {
        Resources system = Resources.getSystem();
        int minuteNumberPickerId = system.getIdentifier("minute", "id", "android");

        NumberPicker minuteNumberPicker = timePicker.findViewById(minuteNumberPickerId);

        minuteNumberPicker.setMinValue(0);
        minuteNumberPicker.setMaxValue((60 / minutesInterval) - 1);
        List<String> displayedValues = new ArrayList<>();
        for (int i = 0; i < 60; i += minutesInterval) {
            displayedValues.add(String.format("%02d", i));
        }
        minuteNumberPicker.setDisplayedValues(displayedValues.toArray(new String[displayedValues.size()]));

        applyDefaultTimePickerStyle(context, timePicker);
    }

    public static void setNumberPickerDividerColour(Context context, NumberPicker number_picker, int colorResource) {
        final int count = number_picker.getChildCount();

        for (int i = 0; i < count; i++) {

            try {
                Field dividerField = number_picker.getClass().getDeclaredField("mSelectionDivider");
                dividerField.setAccessible(true);
                ColorDrawable colorDrawable = new ColorDrawable(Util.getColorWrapper(context, colorResource));
                dividerField.set(number_picker, colorDrawable);

                Field inputField = number_picker.getClass().getDeclaredField("mInputText");
                inputField.setAccessible(true);
                EditText inputText = (EditText) inputField.get(number_picker);
                inputText.setRawInputType(InputType.TYPE_CLASS_NUMBER);

                number_picker.invalidate();
            } catch (NoSuchFieldException e) {
                PillpopperLog.say("NoSuchFieldException", e);
            } catch (IllegalAccessException e) {
                PillpopperLog.say("IllegalAccessException", e);
            } catch (IllegalArgumentException e) {
                PillpopperLog.say("IllegalArgumentException", e);
            }
        }
    }

    public static void showProgressDialog(Context context, String message) {
        if (context != null && !((Activity) context).isFinishing()) {
            if(null==mProgressDialog || !mProgressDialog.isShowing()){
                mProgressDialog = new ProgressDialog(context);

                if (mProgressDialog.isShowing()) return;

                mProgressDialog.setMessage(message);
                mProgressDialog.setCancelable(false);
                mProgressDialog.show();
                RunTimeData.getInstance().setLoadingInProgress(true);
            }
        }
    }

    public static void dismissProgressDialog() {
        if (mProgressDialog != null && mProgressDialog.isShowing()) {
            mProgressDialog.cancel();
            mProgressDialog = null;
            RunTimeData.getInstance().setLoadingInProgress(false);
        }
    }

    public static boolean isValidInput(String note) {
        Pattern webLinkREGEX = Pattern.compile(PillpopperConstants.WEB_LINK_REGEX);
        Matcher webLinkMatcher = webLinkREGEX.matcher(note);
        Pattern htmlTagPattern = Pattern.compile(PillpopperConstants.HTML_TAG_PATTERN);
        Matcher htmlTagMatcher = htmlTagPattern.matcher(note);
        Pattern invalidCharsPattern = Pattern.compile(PillpopperConstants.INVALID_CHARS_PATTERN);
        Matcher invalidCharsMatcher = invalidCharsPattern.matcher(note);
        return !(webLinkMatcher.find() || htmlTagMatcher.find() || invalidCharsMatcher.find());
    }
}
