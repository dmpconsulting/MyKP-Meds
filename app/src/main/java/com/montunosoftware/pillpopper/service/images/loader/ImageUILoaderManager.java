package com.montunosoftware.pillpopper.service.images.loader;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.montunosoftware.pillpopper.android.util.Util;
import com.montunosoftware.pillpopper.android.view.DrugDetailRoundedImageView;
import com.montunosoftware.pillpopper.controller.FrontController;
import com.montunosoftware.pillpopper.model.Drug;

import org.kp.tpmg.mykpmeds.activation.AppConstants;

/**
 * Created by adhithyaravipati on 11/21/16.
 */

public class ImageUILoaderManager implements ImageUILoader {

    private static ImageUILoader mImageUILoader = null;


    public static ImageUILoader getInstance() {
        if (mImageUILoader == null) {
            mImageUILoader = new ImageUILoaderManager();
        }
        return mImageUILoader;
    }

    @Override
    public void loadDrugImage(Context context, String imageGuid, String pillId, DrugDetailRoundedImageView imageView, Drawable errorImage) {
        FrontController frontController = FrontController.getInstance(context);
        Drug drug = frontController.getDrugForImageLoad(pillId);
        if (!drug.isManaged() && imageGuid != null && !Util.isEmptyString(imageGuid)) {
            String encodeImage = frontController.getCustomImage(imageGuid);
            if(encodeImage != null) {
                new ImageLoaderTask(imageView).execute(encodeImage);
            }
        } else {
            String choiceType = drug.getPreferences().getPreference("defaultImageChoice");
            String serviceId = drug.getPreferences().getPreference("defaultServiceImageID");
            if (AppConstants.IMAGE_CHOICE_FDB.equalsIgnoreCase(choiceType)) {
                String encodeImage = frontController.getFdbImageByPillId(pillId);
                if (encodeImage != null) {
                    if (AppConstants.IMAGE_NOT_FOUND.equalsIgnoreCase(serviceId)) {
                        new ImageLoaderTask(imageView).execute(encodeImage);
                    } else {
                        new ImageLoaderTask(imageView, true).execute(encodeImage);
                    }
                } else {
                    imageView.setBitmap(null);
                }
            } else if (AppConstants.IMAGE_CHOICE_CUSTOM.equalsIgnoreCase(choiceType)) {
                if (drug.getImageGuid() != null && !Util.isEmptyString(drug.getImageGuid())) {
                    String encodeImage = frontController.getCustomImage(drug.getImageGuid());
                    if (encodeImage != null) {
                        new ImageLoaderTask(imageView).execute(encodeImage);
                    }else {
                        imageView.setBitmap(null);
                    }
                }
            } else {
                imageView.setBitmap(null);
            }
        }
    }

    @Override
    public void loadDrugImage(Context context, String imageGuid, String pillId, ImageView imageView, Drawable errorImage) {
        FrontController frontController = FrontController.getInstance(context);
        Drug drug = frontController.getDrugForImageLoad(pillId);
        if (!drug.isManaged() && imageGuid != null && !Util.isEmptyString(imageGuid)) {
            String encodeImage = frontController.getCustomImage(imageGuid);
            if (encodeImage != null) {
                new ImageLoaderTask(imageView).execute(encodeImage);
            }
        } else {
            String choiceType = drug.getPreferences().getPreference("defaultImageChoice");
            String serviceId = drug.getPreferences().getPreference("defaultServiceImageID");
            if (AppConstants.IMAGE_CHOICE_FDB.equalsIgnoreCase(choiceType)) {
                String encodeImage = frontController.getFdbImageByPillId(pillId);
                if (encodeImage != null) {
                    if (AppConstants.IMAGE_NOT_FOUND.equalsIgnoreCase(serviceId)) {
                        new ImageLoaderTask(imageView).execute(encodeImage);
                    } else {
                        new ImageLoaderTask(imageView, true).execute(encodeImage);
                    }
                } else {
                    imageView.setImageBitmap(null);
                }
            } else if (AppConstants.IMAGE_CHOICE_CUSTOM.equalsIgnoreCase(choiceType)) {
                if (drug.getImageGuid() != null && !Util.isEmptyString(drug.getImageGuid())) {
                    String encodeImage = frontController.getCustomImage(drug.getImageGuid());
                    if (encodeImage != null) {
                        new ImageLoaderTask(imageView).execute(encodeImage);
                    }else {
                        imageView.setImageBitmap(null);
                    }
                }
            } else {
                imageView.setImageBitmap(null);
            }
        }
    }
}
