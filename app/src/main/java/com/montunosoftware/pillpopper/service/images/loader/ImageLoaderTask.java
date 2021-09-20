package com.montunosoftware.pillpopper.service.images.loader;

import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.widget.ImageView;

import com.montunosoftware.pillpopper.android.view.DrugDetailRoundedImageView;

import java.lang.ref.WeakReference;

/**
 * Created by adhithyaravipati on 11/22/16.
 */

public class ImageLoaderTask extends
    AsyncTask <String, Void, Bitmap> {

    private WeakReference<DrugDetailRoundedImageView> mImageViewWeakReference;
    private WeakReference<ImageView> imageViewWeakReference;

    private int mTargetImageViewWidth;
    private int mTargetImageViewHeight;
    private static final String contentDescriptionString = "Tap to enlarge medication image";



    public ImageLoaderTask(DrugDetailRoundedImageView imageView) {
        mImageViewWeakReference = new WeakReference<>(imageView);
        mTargetImageViewWidth = imageView.get_imgView().getWidth();
        mTargetImageViewHeight = imageView.get_imgView().getHeight();

    }

    public ImageLoaderTask(DrugDetailRoundedImageView imageView, boolean isFDBImage) {
        mImageViewWeakReference = new WeakReference<>(imageView);
        mTargetImageViewWidth = imageView.get_imgView().getWidth();
        mTargetImageViewHeight = imageView.get_imgView().getHeight();
    }

    public ImageLoaderTask(ImageView imageView) {
        imageViewWeakReference = new WeakReference<>(imageView);
        mTargetImageViewWidth = imageView.getWidth();
        mTargetImageViewHeight = imageView.getHeight();

    }

    public ImageLoaderTask(ImageView imageView, boolean isFDBImage) {
        imageViewWeakReference = new WeakReference<>(imageView);
        mTargetImageViewWidth = imageView.getWidth();
        mTargetImageViewHeight = imageView.getHeight();
    }

    @Override
    protected Bitmap doInBackground(String... imageFilePath) {
        return ImageLoaderUtil.decodeSampleBitmapFromDatabase(imageFilePath[0], mTargetImageViewWidth, mTargetImageViewHeight);
    }

    @Override
    protected void onPostExecute(Bitmap result) {
        if(mImageViewWeakReference != null && result != null) {
            final DrugDetailRoundedImageView imageView = mImageViewWeakReference.get();
            if(imageView != null) {
                imageView.setBitmap(result);
                imageView.setContentDescription(contentDescriptionString);
            }
        }
    }
}
