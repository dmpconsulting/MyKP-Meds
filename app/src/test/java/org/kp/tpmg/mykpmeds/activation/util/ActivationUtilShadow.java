package org.kp.tpmg.mykpmeds.activation.util;

import org.robolectric.annotation.Implements;

import org.kp.tpmg.mykpmeds.activation.util.ActivationUtil;

import static com.google.common.io.Resources.getResource;

/**
 * Created by M1028309 on 5/11/2017.
 */
@Implements(ActivationUtil.class)
public class ActivationUtilShadow {

    /*@Implementation
    public static Typeface setFontStyle(Context mContext, String textStyle) {
        try {
            return Typeface.createFromAsset(ApplicationProvider.getApplicationContext().getAssets(), getResource("fonts/" + textStyle).toURI().getPath());
        } catch (URISyntaxException e) {
            e.printStackTrace();
        }
        return null;
    }*/
}
