package com.montunosoftware.pillpopper.android.util;

import android.view.View;

/**
 * Class to avoid the double taps for the view.
 * */
public class ViewClickHandler {
    private static final long DELAY_IN_MS = 500;

    /**
     * Avoid the double taps for the given  view.
     * Any subsequent taps comes with in the 500 milli seconds of time, it will ignore the tap.
     * This has been done for avoiding double tap behaviour.
     * @param view
     */
    public static void preventMultiClick(final View view) {
        if (!view.isClickable()) {
            return;
        }
        view.setClickable(false);
        view.postDelayed(() -> view.setClickable(true), DELAY_IN_MS);
    }
}
