package com.diagrams.lib;

import android.app.Activity;
import android.support.annotation.Nullable;

/**
 * 主界面Activity提供者
 *
 * <p/>
 * Created by lizhaofei on 2017/8/30 18:53
 */
public class MainActivitySupplier {
    private static Activity sActivity;//会泄露，所以一定要是MainActivity，并且在app退出时释放掉

    public static void set(@Nullable Activity activity) {
        if (null != sActivity) {
            return;
        }
        sActivity = activity;
    }

    @Nullable
    public static Activity get() {
        return sActivity;
    }

    public static void relase() {
        sActivity = null;
    }
}
