package com.montunosoftware.pillpopper.service.images.loader;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.montunosoftware.pillpopper.android.view.DrugDetailRoundedImageView;

/**
 * Created by adhithyaravipati on 11/21/16.
 */

public interface ImageUILoader {

    void loadDrugImage(Context context, String imageGuid, String pillId, DrugDetailRoundedImageView imageView, Drawable errorImage);

    void loadDrugImage(Context context, String imageGuid, String pillId, ImageView imageView, Drawable errorImage);

}
