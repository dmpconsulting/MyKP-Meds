package com.montunosoftware.pillpopper.service;

import android.content.Context;
import android.os.AsyncTask;

import com.google.gson.Gson;
import com.montunosoftware.pillpopper.android.util.PillpopperConstants;
import com.montunosoftware.pillpopper.android.util.PillpopperLog;
import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.model.Drug;
import com.montunosoftware.pillpopper.model.KphcDrug;
import com.montunosoftware.pillpopper.network.model.FailedImageObj;
import com.montunosoftware.pillpopper.service.images.sync.ImageSyncManager;
import com.montunosoftware.pillpopper.service.images.sync.ImageSynchronizer;
import com.montunosoftware.pillpopper.service.images.sync.model.FdbRoot;

import org.kp.tpmg.mykpmeds.activation.AppConstants;
import org.kp.tpmg.mykpmeds.activation.model.RunTimeData;
import org.kp.tpmg.mykpmeds.activation.util.LoggerUtils;
import org.kp.tpmg.ttg.network.images.sync.RefillImageSyncManager;

import java.util.List;

/**
 * Created by M1024581 on 6/17/2018.
 */

public class GetFDBImagesForKPHCDrugsAsyncTask extends AsyncTask<Void,Void,Void>
        implements RefillImageSyncManager.OnFDBImageDownloadListener, TokenService.TokenRefreshAPIListener {

    private Context mContext;
    private List<FailedImageObj> mFailedImageObjList;
    private int mFDBRetryCounter = 0;

    public GetFDBImagesForKPHCDrugsAsyncTask(Context context){
        mContext = context;
    }

    @Override
    protected Void doInBackground(Void... voids) {
        // get All KPHC drugs, ordered by Archived and then by Pill name
        List<KphcDrug> drugsList = FrontController.getInstance(mContext).getKPHCDrugsListToFetchFBDImages();

        setNumberOfImagesAPICounter(drugsList);

        for(KphcDrug drug: drugsList) {

            // if the default image choice is empty, set it to undefined for downloading
            if (Util.isEmptyString(drug.getDefaultImageChoice())) {
                if(Util.isEmptyString(drug.getImageGuid())){
                    drug.setDefaultImageChoice(AppConstants.IMAGE_CHOICE_UNDEFINED);
                } else{
                    drug.setDefaultImageChoice(AppConstants.IMAGE_CHOICE_CUSTOM);
                    FrontController.getInstance(mContext).updatePillImagePreferences(drug.getPillId(), AppConstants.IMAGE_CHOICE_CUSTOM, drug.getDefaultServiceImageID());
                }
            }

            // for need FDB update scenario
            if(Boolean.parseBoolean(drug.getNeedFDBUpdate())){
                drug.setDefaultImageChoice(AppConstants.IMAGE_NEED_FDB_UPDATE);
            }

            ImageSynchronizer imageSynchronizer = ImageSyncManager.getInstance(mContext);

            //enable below two lines and use refillImageSynchronizer instead of imageSynchronizer for getImageByNDCCode
//            RefillImageSynchronizer refillImageSynchronizer = RefillImageSyncManager.getInstance(mContext);
//            setValuesForRefillImageDownloader();
            switch (drug.getDefaultImageChoice()) {
                case AppConstants.IMAGE_CHOICE_NO_IMAGE:
                case AppConstants.IMAGE_CHOICE_CUSTOM:
                case AppConstants.IMAGE_CHOICE_FDB:
                    //download getImageById
                    if (!Util.isEmptyString(drug.getDefaultServiceImageID())
                            && !drug.getDefaultServiceImageID().equalsIgnoreCase(AppConstants.IMAGE_NOT_FOUND)) {
                        if(!FrontController.getInstance(mContext).isFDBImageAvailable(drug.getPillId())) {
                            LoggerUtils.info("download getImageById -- " + drug.getPillId() + " -- image id -- " + drug.getDefaultServiceImageID());
                            imageSynchronizer.downloadFdbImageById(drug.getPillId(), drug.getDefaultServiceImageID());
                        }
                    }else {
                        //download getImageById, If the ImageId is NOT Found, download by downloadByNDCCode
                        if(!Util.isEmptyString(drug.getDatabaseNDC())) {
                            imageSynchronizer.downloadFdbImageByNdcCode(drug.getPillId(), drug.getDatabaseNDC());
                        }
                    }
                    break;
                case AppConstants.IMAGE_NEED_FDB_UPDATE:
                case AppConstants.IMAGE_CHOICE_UNDEFINED:
                    //download getImageByNDCCode
                    if(!Util.isEmptyString(drug.getDatabaseNDC())) {
                        LoggerUtils.info("download getImageByNDCCode -- " + drug.getPillId() + " -- NDC code -- " + drug.getDatabaseNDC());
                        imageSynchronizer.downloadFdbImageByNdcCode(drug.getPillId(), drug.getDatabaseNDC());
                    }
                    break;
            }
        }
        return null;
    }

    private void setNumberOfImagesAPICounter(List<KphcDrug> drugsList) {
        int imageAPIDownloadCounter = 0;
        RunTimeData.getInstance().setImageAPIDownloadCounter(0);
        for (KphcDrug drug : drugsList) {
            // if the default image choice is empty, set it to undefined for downloading
            if (Util.isEmptyString(drug.getDefaultImageChoice())) {
                drug.setDefaultImageChoice(Util.isEmptyString(drug.getImageGuid()) ? AppConstants.IMAGE_CHOICE_UNDEFINED : AppConstants.IMAGE_CHOICE_CUSTOM);
            }

            // for need FDB update scenario
            if (Boolean.parseBoolean(drug.getNeedFDBUpdate())) {
                drug.setDefaultImageChoice(AppConstants.IMAGE_NEED_FDB_UPDATE);
            }
            switch (drug.getDefaultImageChoice()) {
                case AppConstants.IMAGE_CHOICE_NO_IMAGE:
                case AppConstants.IMAGE_CHOICE_CUSTOM:
                case AppConstants.IMAGE_CHOICE_FDB:
                    //download getImageById
                    if (!Util.isEmptyString(drug.getDefaultServiceImageID())
                            && !drug.getDefaultServiceImageID().equalsIgnoreCase(AppConstants.IMAGE_NOT_FOUND)) {
                        if (!FrontController.getInstance(mContext).isFDBImageAvailable(drug.getPillId())) {
                            imageAPIDownloadCounter++;
                        }
                    } else {
                        //download getImageById, If the ImageId is NOT Found, download by downloadByNDCCode
                        if (!Util.isEmptyString(drug.getDatabaseNDC())) {
                            imageAPIDownloadCounter++;
                        }
                    }
                    break;
                case AppConstants.IMAGE_NEED_FDB_UPDATE:
                case AppConstants.IMAGE_CHOICE_UNDEFINED:
                    //download getImageByNDCCode
                    if (!Util.isEmptyString(drug.getDatabaseNDC())) {
                        imageAPIDownloadCounter++;
                    }
                    break;
            }
        }
        RunTimeData.getInstance().setImageAPIDownloadCounter(imageAPIDownloadCounter);
        LoggerUtils.info("--API manager-- Image API counter--" + RunTimeData.getInstance().getImageAPIDownloadCounter());
    }


    @Override
    protected void onPostExecute(Void aVoid) {
        super.onPostExecute(aVoid);
        LoggerUtils.info("GetFDBImagesForKPHCDrugsAsyncTask Completed");
    }

    @Override
    public void onFDBImageDownloadComplete(String pillId, String response) {
        Drug editDrug  = FrontController.getInstance(mContext).getDrugByPillId(pillId);
        Gson gson = new Gson();
        FdbRoot fdbRoot = gson.fromJson(response, FdbRoot.class);
        if (fdbRoot.getImages() != null && !fdbRoot.getImages().isEmpty()) {
            fdbRoot.getImages().get(0).setPillId(pillId);
            FrontController.getInstance(mContext).saveFdbImage(mContext, fdbRoot.getImages().get(0));
            if(null != editDrug.getPreferences().getPreference("defaultImageChoice") && !editDrug.getPreferences().getPreference("defaultImageChoice").equalsIgnoreCase(AppConstants.IMAGE_CHOICE_UNDEFINED)) {
                FrontController.getInstance(mContext).updatePillImagePreferences(pillId, editDrug.getPreferences().getPreference("defaultImageChoice"), fdbRoot.getImages().get(0).getId());
            } else{
                FrontController.getInstance(mContext).updatePillImagePreferences(pillId, AppConstants.IMAGE_CHOICE_FDB, fdbRoot.getImages().get(0).getId());
            }
        } else {
            if(null != editDrug.getPreferences().getPreference("defaultImageChoice") && !editDrug.getPreferences().getPreference("defaultImageChoice").equalsIgnoreCase(AppConstants.IMAGE_CHOICE_UNDEFINED)) {
                FrontController.getInstance(mContext).updatePillImagePreferences(pillId, editDrug.getPreferences().getPreference("defaultImageChoice"), AppConstants.IMAGE_NOT_FOUND);
            } else{
                FrontController.getInstance(mContext).updatePillImagePreferences(pillId, AppConstants.IMAGE_CHOICE_FDB, AppConstants.IMAGE_NOT_FOUND);
            }
        }
        // set needFDBUpdate for pillId to false and then add Edit Pill LogEntry
        if(Boolean.parseBoolean(editDrug.getPreferences().getPreference("needFDBUpdate"))){
            FrontController.getInstance(mContext).updateNoNeedFDBImageUpdate(pillId);
        }
        // getDrug details with preferences after update, again for log entry
        editDrug = FrontController.getInstance(mContext).getDrugByPillId(pillId);
        FrontController.getInstance(mContext).addLogEntry(mContext, Util.prepareLogEntryForAction("EditPill", editDrug, mContext));
        PillpopperLog.say("RefillImageSyncManager -- performDownloadFdbImageByNDCCode -- Image Download Successful -- ImageGuid: " + response);
    }

    @Override
    public void tokenRefreshSuccess() {
        if(null!=mFailedImageObjList && !mFailedImageObjList.isEmpty()){
            ImageSynchronizer imageSynchronizer = ImageSyncManager.getInstance(mContext);
            for (FailedImageObj failedImageObj : mFailedImageObjList){
                if(PillpopperConstants.IMAGE_TYPE_NDC.equalsIgnoreCase(failedImageObj.getImageType())){
                    imageSynchronizer.downloadFdbImageByNdcCode(failedImageObj.getPillID(), failedImageObj.getImageId());
                }else{
                    imageSynchronizer.downloadFdbImageById(failedImageObj.getPillID(), failedImageObj.getImageId());
                }
            }
            mFDBRetryCounter++;
        }
    }

    @Override
    public void tokenRefreshError() {
        PillpopperLog.say("Token refresh API failed.");
    }

    /**
     * Check the failed entries and retry count should be less than the configurable count.
     * Invoke the refresh token
     */
    private void invokeRefreshTokenAPI(){
        mFailedImageObjList = FrontController.getInstance(mContext).getFailedImageEntryList();
        if (null != mFailedImageObjList && !mFailedImageObjList.isEmpty() && mFDBRetryCounter < PillpopperConstants.FDB_RETRY_COUNt) {
            TokenService.startRefreshAccessTokenService(mContext, this);
        }
    }
}
