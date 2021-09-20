package com.montunosoftware.pillpopper.service.images;

import android.content.Context;
import android.graphics.Bitmap;

import com.android.camera.gallery.LruCache;
import com.android.volley.toolbox.ImageLoader;

/**
 * Created by adhithyaravipati on 11/21/16.
 */

public class ImageCacheManager extends LruCache<String, Bitmap>
        implements ImageLoader.ImageCache {

    public ImageCacheManager(Context ctx) {
        this(getCacheSize(ctx));
    }

    public ImageCacheManager(int sizeInKiloBytes) {
        super(sizeInKiloBytes);
    }

    protected int sizeOf(String key, Bitmap value) {
        return value.getRowBytes() * value.getHeight();
    }

    @Override
    public Bitmap getBitmap(String url) {
        return get(url);
    }

    @Override
    public void putBitmap(String url, Bitmap bitmap) {
        put(url, bitmap);
    }

    // Returns a cache size equal to approximately three screens worth of images.
    public static int getCacheSize(Context ctx) {
        final int maxMemory = (int) (Runtime.getRuntime().maxMemory() / 1024);
        final int cacheSize = maxMemory / 8;
        return cacheSize;
    }
}