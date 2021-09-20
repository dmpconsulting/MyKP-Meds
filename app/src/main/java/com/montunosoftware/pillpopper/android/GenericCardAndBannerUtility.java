package com.montunosoftware.pillpopper.android;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.net.Uri;
import android.view.Gravity;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.analytics.FireBaseAnalyticsTracker;
import com.montunosoftware.pillpopper.analytics.FireBaseConstants;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.model.genericCardAndBanner.AnnouncementsItem;
import com.montunosoftware.pillpopper.model.genericCardAndBanner.ButtonsItem;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.mykpmeds.activation.util.PermissionUtils;

import static com.montunosoftware.pillpopper.analytics.FireBaseConstants.ScreenEvent.SCREEN_BULK_REMINDER_SETUP;
import static com.montunosoftware.pillpopper.analytics.FireBaseConstants.ScreenEvent.SCREEN_FIND_PHARMACY;
import static com.montunosoftware.pillpopper.analytics.FireBaseConstants.ScreenEvent.SCREEN_PRESCRIPTION_REFILL_AEM;

public class GenericCardAndBannerUtility {
    private static AlertDialog mDialog;
    private static String buttonColor;
    private static String buttonFontColor;

    private static final GenericCardAndBannerUtility mUtil = new GenericCardAndBannerUtility();

    public static GenericCardAndBannerUtility getInstance() {
        return mUtil;
    }

    public static void setButtonColor(Button button, ButtonsItem buttonsItem) {
        setBackgroundAndFontColors(buttonsItem);
        GradientDrawable gd = new GradientDrawable();
        gd.setShape(GradientDrawable.RECTANGLE);
        gd.setColor(Color.parseColor(buttonColor));
        gd.setCornerRadius(20);
        button.setBackground(gd);
        button.setTextColor(Color.parseColor(buttonFontColor));
    }

    private static void setBackgroundAndFontColors(ButtonsItem buttonsItem) {
        if(!Util.isEmptyString(buttonsItem.getButtonColor())){
            buttonColor ="#"+buttonsItem.getButtonColor();
        }else{
            if(buttonsItem.getOrder()==1) {
                buttonColor = "#FFFFFF";
            }else{
                buttonColor = "#006BA6";
            }
        }
        if(!Util.isEmptyString(buttonsItem.getFontColor())){
            buttonFontColor ="#"+buttonsItem.getFontColor();
        }else{
            if(buttonsItem.getOrder()==1) {
                buttonFontColor = "#006BA6";
            }else{
                buttonFontColor = "#FFFFFF";
            }

        }
    }

    public static void buttonAction(AnnouncementsItem bannerData, String action, String link, String destination, Context context) {
        if (action.equalsIgnoreCase("ack")) {
        } else if (action.equalsIgnoreCase("link")) {
            loadUrl(link,context);
        } else if (action.equalsIgnoreCase("tab")) {
            triggerFirebaseEvent(destination,bannerData,context);
            Intent intent = new Intent("TO_OPEN_TAB");
            intent.putExtra("destination_name", destination);
            LocalBroadcastManager.getInstance(context).sendBroadcast(intent);
        }
    }

    private static void triggerFirebaseEvent(String destination, AnnouncementsItem bannerData, Context context) {
        String paramValue = "";
        if (bannerData.getType().equals("home_card")) {
            paramValue = FireBaseConstants.ParamValue.GENERIC_CARD;
        }
        else {
            paramValue = FireBaseConstants.ParamValue.GENERIC_BANNER;
        }

        if(destination.equalsIgnoreCase(SCREEN_BULK_REMINDER_SETUP)){
            FireBaseAnalyticsTracker.getInstance().logEvent(context, FireBaseConstants.Event.BULK_REMINDER_ADD, FireBaseConstants.ParamName.SOURCE, paramValue);
        }
        else if(destination.equalsIgnoreCase(SCREEN_FIND_PHARMACY)){
            FireBaseAnalyticsTracker.getInstance().logEvent(context, FireBaseConstants.Event.FIND_PHARMACY, FireBaseConstants.ParamName.SOURCE, paramValue);
        }
        else if(destination.equalsIgnoreCase(SCREEN_PRESCRIPTION_REFILL_AEM)){
            FireBaseAnalyticsTracker.getInstance().logEvent(context, FireBaseConstants.Event.REFILL_MEDS, FireBaseConstants.ParamName.SOURCE, paramValue);
        }
    }

    public static boolean showButtonIfEligible(ButtonsItem buttonsItem){
        boolean showButton = false;
        String action = buttonsItem.getAction();
        if(!Util.isEmptyString(buttonsItem.getLabel())){
            if(null!=action && action.equalsIgnoreCase("ack")){
                showButton = true;
            }else if(null!=action && action.equalsIgnoreCase("link") && !Util.isEmptyString(buttonsItem.getUrl())){
                showButton = true;
            }else if(null!=action && action.equalsIgnoreCase("tab") && !Util.isEmptyString(buttonsItem.getDestination())){
                showButton = true;
            }
        }
        return showButton;

    }
    private static void loadUrl(String url,Context context) {
        Uri uri = Uri.parse(url);
        Intent intent = new Intent(Intent.ACTION_VIEW, uri);
        context.startActivity(intent);
    }

    public static void makeCall(String phoneNoStr, Context context, String title) {
        if (mDialog == null || !mDialog.isShowing()) {
            try {
                AlertDialog.Builder alertDialog = new AlertDialog.Builder(context);
                alertDialog.setTitle(title);
                alertDialog.setMessage(phoneNoStr);
                alertDialog.setCancelable(false);
                alertDialog.setPositiveButton(context.getResources().getString(R.string.call),
                        (dialog, which) -> {
                            dialog.dismiss();
                            Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNoStr.replace("-", "")));
                            if (PermissionUtils.checkRuntimePermission(AppConstants.PERMISSION_PHONE_CALL_PHONE, Manifest.permission.CALL_PHONE, context)) {
                                context.startActivity(intent);
                            }
                        });
                alertDialog.setNegativeButton(context.getResources().getString(org.kp.tpmg.ttg.R.string.cancelAlert),
                        (dialog, which) -> {
                            dialog.dismiss();
                        });
                mDialog = alertDialog.create();
                mDialog.show();
                RunTimeData.getInstance().setAlertDialogInstance(mDialog);

                TextView tv = mDialog.findViewById(android.R.id.message);
                tv.setGravity(Gravity.CENTER);

                Button btnPositive = mDialog.findViewById(android.R.id.button1);
                Button btnNegative = mDialog.findViewById(android.R.id.button2);

                btnPositive.setTextColor(getColorWrapper(context, R.color.kp_theme_blue));
                btnNegative.setTextColor(getColorWrapper(context, R.color.kp_theme_blue));
            } catch (Exception e) {
                LoggerUtils.exception("Exception while invoking the call");
            }
        }
    }

    private static int getColorWrapper(Context context, int id) {
        return ContextCompat.getColor(context, id);
    }

    public static void dismissDialog(){
        if(null!=mDialog && mDialog.isShowing()){
            mDialog.dismiss();
        }
    }
}
