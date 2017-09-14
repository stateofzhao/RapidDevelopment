package com.diagrams.lib;

import android.content.Context;
import android.support.annotation.NonNull;

/**
 * applicationContext{{@link Context#getApplicationContext()}}持有者
 * <p/>
 * Created by lizhaofei on 2017/8/30 17:42
 */
public class AppContextSupplier {
    private static Context sContext;

    public static void set(@NonNull Context context) {
        if (null != sContext) {
            return;
        }
        sContext = context.getApplicationContext();
    }

    public static Context get() {
        return sContext;
    }
}
