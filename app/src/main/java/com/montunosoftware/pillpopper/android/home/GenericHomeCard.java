package com.montunosoftware.pillpopper.android.home;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Handler;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;
import android.webkit.WebResourceError;
import android.webkit.WebResourceRequest;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.montunosoftware.mymeds.R;
import com.montunosoftware.pillpopper.android.GenericCardAndBannerUtility;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.model.genericCardAndBanner.AnnouncementsItem;
import com.montunosoftware.pillpopper.model.genericCardAndBanner.ButtonsItem;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;
import org.kp.tpmg.mykpmeds.activation.util.PermissionUtils;
import org.kp.tpmg.mykpmeds.activation.util.SharedPreferenceManager;


import java.util.HashSet;
import java.util.Set;

import static android.app.Activity.RESULT_OK;

public class GenericHomeCard implements Parcelable, HomeCard {

    private Context context;

    private AnnouncementsItem mAnnouncementCard;

    public AnnouncementsItem getAnnouncementCard() {
        return mAnnouncementCard;
    }

    private int mCardIndex;

    public GenericHomeCard(AnnouncementsItem mAnnouncementCard, int cardIndex) {
        this.mAnnouncementCard = mAnnouncementCard;
        this.mCardIndex = cardIndex;
    }

    public GenericHomeCard(Parcel in) {
        mAnnouncementCard = (AnnouncementsItem) in.readSerializable();
    }

    @Override
    public void setContext(Context context) {
        this.context = context;
    }

    @Override
    public int getCardView() {
        return R.layout.generic_home_card;
    }

    @Override
    public String getTitle() {
        return null;
    }

    @Override
    public int getBanner() {
        return 0;
    }

    @Override
    public int getDetailView() {
        return R.layout.generic_home_card_detail_layout;
    }

    @Override
    public String getDescription() {
        return null;
    }

    public static final Parcelable.Creator<GenericHomeCard> CREATOR = new Parcelable.Creator<GenericHomeCard>() {
        @Override
        public GenericHomeCard createFromParcel(Parcel in) {
            return new GenericHomeCard(in);
        }

        @Override
        public GenericHomeCard[] newArray(int size) {
            return new GenericHomeCard[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeSerializable(mAnnouncementCard);
    }

    @Override
    public int getRequestCode() {
        return PillpopperConstants.GENERIC_HOME_CARD_DETAIL;
    }

    @Override
    public String getContentDescription(View view) {
        return getTitle();
    }

    private void delayFinish() {
        ((AppCompatActivity) context).finishActivity(0);
        new Handler().post(() -> {
            try {
                ((AppCompatActivity) context).setResult(RESULT_OK);
                ((AppCompatActivity) context).finish();
            } catch (Exception e) {
                PillpopperLog.exception(e.getMessage());
            }
        });
    }

    public String getCardTitle() {
        String title = "";
        if (null != mAnnouncementCard) {
            title = mAnnouncementCard.getTitle();
        }
        return title;
    }

    public String getCardSubTitle() {
        String subtitle = "";
        if (null != mAnnouncementCard) {
            subtitle = mAnnouncementCard.getSubTitle();
        }
        return subtitle;
    }

    public void initDetailView(WebView messageWebView, Button button1, Button button2) {
        if (null != mAnnouncementCard) {
            String message = mAnnouncementCard.getMessage();

            if (message.contains("<tel>")) {
                String number = message.substring(message.indexOf("<tel>"), message.indexOf("</tel>"));
                number = number.replace("<tel>", "");

                String oldText = "<tel>" + number + "</tel>";
                String newText = "<a href='tel:" + number + "'>" + number + "</a>";
                message = message.replace(oldText, newText);
            }


            messageWebView.setWebViewClient(new KpWebViewClient());
            messageWebView.getSettings().setAllowContentAccess(true);

            messageWebView.setClickable(true);

            message = addLinks(message);
            messageWebView.loadDataWithBaseURL(null, setMessageBody(message), "text/html; charset=UTF-8", null, null);
            messageWebView.setBackgroundColor(Color.TRANSPARENT);
            if(null!=mAnnouncementCard.getButtons()) {
                if (mAnnouncementCard.getButtons().size() == 1) {
                    showButton(button1, mAnnouncementCard.getButtons().get(0));
                } else if (mAnnouncementCard.getButtons().size() == 2) {
                    showButton(button1, mAnnouncementCard.getButtons().get(0));
                    showButton(button2, mAnnouncementCard.getButtons().get(1));
                }
            }
        }
        Util.saveCardIndex(context, mCardIndex);
    }

    private void showButton(Button button, ButtonsItem buttonsItem) {
        if (null!=buttonsItem && GenericCardAndBannerUtility.showButtonIfEligible(buttonsItem)) {
            button.setVisibility(View.VISIBLE);
            button.setText(buttonsItem.getLabel());
            GenericCardAndBannerUtility.setButtonColor(button, buttonsItem);
            button.setOnClickListener(view -> {
                buttonAction(mAnnouncementCard, buttonsItem.getAction(), buttonsItem.getUrl(), buttonsItem.getDestination());
            });
        }
    }

    public void buttonAction(AnnouncementsItem basescreen, String action, String link, String destination) {
        if (null != mAnnouncementCard && mAnnouncementCard.getRetention().equalsIgnoreCase("soft")) {
            SharedPreferenceManager preferenceManager = SharedPreferenceManager.getInstance(context, AppConstants.AUTH_CODE_PREF_NAME);
            Set<String> idSet = preferenceManager.getStringSet("CardIdSet", new HashSet<>());
            idSet.add(String.valueOf(mAnnouncementCard.getId()));
            preferenceManager.putStringSet("CardIdSet", idSet);
            LocalBroadcastManager.getInstance(context).sendBroadcast(new Intent("REFRESH_GENERIC_HOME_CARD"));
        }
        GenericCardAndBannerUtility.buttonAction(basescreen, action, link, destination, context);
        delayFinish();
    }

    private String setMessageBody(String body) {
        return "<html xmlns=\"http://www.w3.org/1999/xhtml\" xml:lang=\"en-US\">" +
                "<pre style=\"word-wrap: break-word; white-space: normal;background-color: transparent; font-family: Avenir-Light; font-size: 12pt; \">" +
                "<head>" +
                "<meta http-equiv=\"content-type\" content=\"text/html; charset=UTF-8\">" +
                "<title>Kaiser Permanente</title>" +
                "<style type=\"text/css\">" +
                "* {  background-color: transparent; font-family: Avenir-Light; font-size: 12pt; }" +
                " </style> </head> <body> "
                + body.trim() +
                "</body></pre> </html>";
    }

    private String convertMessageBody(String msg) {
        msg = msg.replaceAll("\n", "<br/>");
        msg += "<br/>";
        return msg;
    }

    private String addLinks(String body) {
        String data = convertMessageBody(body);

        StringBuilder dataBuffer = new StringBuilder("");
        data = data.replace("<br>", "\n");
        data = data.replace("<br/>", "\n");
        data = data.replace("epichttp", "http");
        if(data.contains("<a href=\"www")) {
            data = data.replace("<a href=\"www", "<a href=\"https://www");
        }
        String word = "";

        for (char c : data.toCharArray()) {
            switch (c) {
                case ' ':
                    if ((word.contains("http") || word.contains("www")) && !word.contains("href")) {
                        if (!dataBuffer.toString().endsWith("href=\" "))
                            dataBuffer.append("<a href=\"" + word + "\">" + word + "</a>");
                        else
                            dataBuffer.append(word);
                    } else {
                        dataBuffer.append(word);
                    }
                    dataBuffer.append(" ");
                    word = "";
                    break;
                case '\n':
                    if ((word.contains("http") || word.contains("www")) && !word.contains("href")) {
                        if (!dataBuffer.toString().endsWith("href=\" "))
                            dataBuffer.append("<a href=\"" + word + "\">" + word + "</a>");
                        else
                            dataBuffer.append(word);
                    } else {
                        dataBuffer.append(word);
                    }
                    dataBuffer.append("<br>");
                    word = "";
                    break;
                case '\t':
                    if ((word.contains("http") || word.contains("www")) && !word.contains("href")) {
                        if (!dataBuffer.toString().endsWith("href=\" "))
                            dataBuffer.append("<a href=\"" + word + "\">" + word + "</a>");
                        else
                            dataBuffer.append(word);
                    } else {
                        dataBuffer.append(word);
                    }
                    dataBuffer.append("&nbsp;");
                    word = "";
                    break;
                default:
                    word = word + c;
            }
        }

        data = dataBuffer.toString();
        data = convertMessageBody(data);
        data = data.replace("&amp;", "&");
        data = data.replace("&lt;", "<");
        data = data.replace("&gt;", ">");
        return data;
    }


    class KpWebViewClient extends WebViewClient {
        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            super.onPageStarted(view, url, favicon);
        }

        @Override
        public boolean shouldOverrideUrlLoading(final WebView view, String url) {
            if (url.startsWith("tel:") || url.startsWith("mobilecare:phone:")) {
                String mPhoneNoStr = url.substring(url.lastIndexOf('=') + 1);
                String phoneNoStr = mPhoneNoStr.replace("tel:", "");

                Intent intent = new Intent(Intent.ACTION_CALL, Uri.parse("tel:" + phoneNoStr.replace("-", "")));
                RunTimeData.getInstance().setRunTimePhoneNumber(phoneNoStr);
                if (PermissionUtils.checkRuntimePermission(AppConstants.PERMISSION_PHONE_CALL_PHONE, Manifest.permission.CALL_PHONE, context)) {
                    GenericCardAndBannerUtility.makeCall(phoneNoStr,context,"");
                }
                return true;
            } else if (url.startsWith("http")) {

                Intent browserIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
                context.startActivity(browserIntent);
            } else if (url.startsWith("mailto:")) {
                Intent email = ActivationUtil.callMailClient(url.replace("mailto:", ""), "My KP Meds Support:", "");
                context.startActivity(email);
                return true;
            }
            return true;
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
        }

        @Override
        public void onReceivedError(WebView view, WebResourceRequest request, WebResourceError error) {
            super.onReceivedError(view, request, error);
        }
    }

}