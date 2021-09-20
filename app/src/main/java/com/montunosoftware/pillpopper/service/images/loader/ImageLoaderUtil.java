package com.montunosoftware.pillpopper.service.images.loader;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Base64;

import java.io.ByteArrayOutputStream;

/**
 * Created by adhithyaravipati on 11/22/16.
 */

public class ImageLoaderUtil {

    public static int calculateInSampleSize(BitmapFactory.Options options, int requiredWidth, int requiredHeight) {
        //Getting the Raw width and height of the image from the options.
        final int width = options.outWidth;
        final int height = options.outHeight;
        int inSampleSize = 1;

        if (height > requiredHeight || width > requiredWidth) {
            final int halfHeight = height / 2;
            final int halfWidth = width / 2;

            if (requiredHeight != 0 && requiredWidth != 0) {
                while ((halfHeight / inSampleSize) >= requiredHeight
                        && (halfWidth / inSampleSize) >= requiredWidth) {
                    inSampleSize *= 2;
                }
            }
        }
        return inSampleSize;
    }

    public static Bitmap decodeSampledBitmapFromFile(String filePath, int requiredWidth, int requiredHeight) {

        final BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);

        options.inSampleSize = calculateInSampleSize(options, requiredWidth, requiredHeight);

        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inDither = true;
        return BitmapFactory.decodeFile(filePath, options);

    }

    public static Bitmap decodeSampleBitmapFromDatabase(String encodedImage, int requiredWidth, int requiredHeight) {
        byte[] decodedString = Base64.decode(encodedImage, Base64.DEFAULT);

        BitmapFactory.Options options = new BitmapFactory.Options();
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);

        options.inSampleSize = calculateInSampleSize(options, requiredWidth, requiredHeight);

        options.inJustDecodeBounds = false;
        options.inPreferredConfig = Bitmap.Config.RGB_565;
        options.inDither = true;

        return BitmapFactory.decodeByteArray(decodedString, 0, decodedString.length, options);
    }

    public static String encodeImage(Bitmap bitmap) {
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        bitmap.compress(Bitmap.CompressFormat.JPEG,100,baos);
        byte[] b = baos.toByteArray();
        return Base64.encodeToString(b, Base64.DEFAULT);
    }
}
