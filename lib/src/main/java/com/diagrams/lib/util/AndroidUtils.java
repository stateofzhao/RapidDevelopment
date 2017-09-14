package com.diagrams.lib.util;

import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * <p/>
 * Created by lizhaofei on 2017/9/14 18:26
 */
public class AndroidUtils {

    /** 将 dp 转换成px */
    public static float dip2px(Context context, float dimen) {
        float dpi = getScreenDPI(context);
        return dimen * (dpi / 160);

        // ---------这个方法和上面的返回值一样
        // return TypedValue.applyDimension(
        // TypedValue.COMPLEX_UNIT_DIP, dimen,
        // context.getResources().getDisplayMetrics());
        //

        // ---------这个方法不对，始终返回0
        // TypedValue tv = new TypedValue();
        // return TypedValue.complexToDimensionPixelSize(tv.data,
        // context.getResources().getDisplayMetrics());
    }

    /** 获取屏幕的 dpi值 */
    public static float getScreenDPI(Context context) {
        final DisplayMetrics displayMetrics = new DisplayMetrics();
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        wm.getDefaultDisplay().getMetrics(displayMetrics);

        return (float) displayMetrics.densityDpi;
    }
}
