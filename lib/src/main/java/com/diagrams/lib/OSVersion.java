package com.diagrams.lib;

import android.os.Build;

/**
 * android版本比较工具
 * <p/>
 * Created by lizhaofei on 2017/8/30 18:01
 */
public class OSVersion {

    /**
     * 3.0 API 11
     */
    public static boolean hasHoneycomb() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB;
    }

    /**
     * 3.1 API 12 ,NDK 6
     */
    public static boolean hasHoneycombMR1() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR1;
    }

    /**
     * 4.4 API19
     */
    public static boolean hasKitKat() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;
    }

    /**
     * 6.0 API 23
     */
    public static boolean hasM() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.M;
    }
}
