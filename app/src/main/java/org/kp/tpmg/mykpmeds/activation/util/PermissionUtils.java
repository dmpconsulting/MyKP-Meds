package org.kp.tpmg.mykpmeds.activation.util;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;

import androidx.core.app.ActivityCompat;

import com.montunosoftware.mymeds.R;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.ttg.RefillRuntimeData;

/**
 * @author
 * Created by M1027802 on 6/18/2016.
 */
public class PermissionUtils {
    private static int mPermission = -1;
    private static GenericAlertDialog mAlertDialog;


    public static boolean checkRuntimePermission(int permissionCode, String permissionType, Context mContext) {

        boolean isPermissiongranted = false;
        mPermission = ActivityCompat.checkSelfPermission(mContext, permissionType);
        if (mPermission != PackageManager.PERMISSION_GRANTED) {
            requestPermission(permissionCode, permissionType, mContext);
            // This we have committed for 6.0 Gallory commit.
            // But we are not able to reproduce the issue with out this changes also hence reverting back Since this changes causing another issue while logging in its showing wrong message.
           /* if(ActivityCompat.shouldShowRequestPermissionRationale((Activity) mContext,permissionType)) {
                requestPermission(permissionCode, permissionType, mContext);
            }else{
                permissionDeniedDailogueForNeverAskAgain(mContext,permissionDeniedMessage(AppConstants.PERMISSION_WRITE_EXTERNAL_STORAGE, mContext));
            }*/
        } else {
            isPermissiongranted = true;

        }

        return isPermissiongranted;
    }

    public static boolean checkCallPermission(Context mContext) {
        boolean isGranted = false;

        if (checkVersionCode()) {
            mPermission = ActivityCompat.checkSelfPermission(mContext, Manifest.permission.CALL_PHONE);
            if (mPermission == PackageManager.PERMISSION_GRANTED) {
                isGranted = true;
            } else {
                requestPermission(AppConstants.PERMISSION_PHONE_CALL_PHONE, Manifest.permission.CALL_PHONE, mContext);
            }

        } else {
            isGranted = true;
        }
        return isGranted;
    }

    private static void requestPermission(int permissionCode, String permissionType, Context mContext) {
        ActivityCompat.requestPermissions((Activity) mContext, new String[]{permissionType},
                permissionCode);
    }

    public static String permissionDeniedMessage(int permissionType, Context mContext) {
        String msg = "";
        switch (permissionType) {
            case AppConstants.PERMISSION_CONTACTS_READ:
                msg = mContext.getResources().getString(R.string.contact_permission);
                break;

            case AppConstants.PERMISSION_CONTACTS_WRITE:
                msg = mContext.getResources().getString(R.string.contact_permission);
                break;

            case AppConstants.PERMISSION_CAMERA:
                msg = mContext.getResources().getString(R.string.camera_permission);
                break;
            case AppConstants.PERMISSION_WRITE_EXTERNAL_STORAGE:
                msg = mContext.getResources().getString(R.string.storage_permission);
                break;
            case AppConstants.PERMISSION_READ_EXTERNAL_STORAGE:
                msg = mContext.getResources().getString(R.string.storage_permission);
                break;

            case AppConstants.PERMISSION_PHONE_CALL_PHONE:
                msg = mContext.getResources().getString(R.string.call_permission);
                break;
        }


        return msg;
    }


    private static final DialogInterface.OnClickListener nOkListener = (dialog, which) -> dialog.dismiss();
    public static void permissionDeniedDailogueForNeverAskAgain(Context mContext , String message) {

        mAlertDialog = new GenericAlertDialog(
                mContext,
                mContext.getResources().getString(R.string.permission_denied),
                message + " " + mContext.getResources().getString(R.string.permission_denied_settings),
                mContext.getResources().getString(R.string.ok_text),
                nOkListener,
                null,
                null);
        mAlertDialog.showDialog();
    }

    public static boolean checkVersionCode() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }


    public static void permissionDeniedDailogue(Context mContext, String message) {
        mAlertDialog = new GenericAlertDialog(
                mContext,
                mContext.getResources().getString(R.string.permission_denied),
                message,
                mContext.getResources().getString(R.string.ok_text),
                nOkListener,
                null,
                null);
        mAlertDialog.showDialog();
    }

    public static void invokeACall(Context context) {

        Intent intent = null;
        if(null!= RunTimeData.getInstance().getRunTimePhoneNumber()){
             intent = new Intent(Intent.ACTION_CALL, Uri
                    .parse("tel:" + RunTimeData.getInstance().getRunTimePhoneNumber().replace("-", "")));
            RunTimeData.getInstance().setRunTimePhoneNumber(null);
        }else if(null!= RefillRuntimeData.getInstance().getRunTimePhoneNumber()){
             intent = new Intent(Intent.ACTION_CALL, Uri
                    .parse("tel:" + RefillRuntimeData.getInstance().getRunTimePhoneNumber().replace("-", "")));
            RefillRuntimeData.getInstance().setRunTimePhoneNumber(null);
        }

        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CALL_PHONE) == PackageManager.PERMISSION_GRANTED) {
            try {
                context.startActivity(intent);
            } catch (Exception e) {
                LoggerUtils.exception("Can't load the view");
            }
        }
    }

}
