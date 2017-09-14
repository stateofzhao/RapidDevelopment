package com.diagrams.lib;

import android.app.Application;
import android.os.Handler;
import android.os.Looper;

/**
 * 解耦用的，提供{@link Application}中相关功能
 * <p/>
 * Created by lizhaofei on 2017/9/5 10:47
 */
public class AppSupplier {

    private static boolean sIsExiting = false;

    public static void setIsExiting(boolean exiting) {
        sIsExiting = exiting;
    }

    public static boolean isExiting() {
        return sIsExiting;
    }

    public static long getMainThreadID() {
        return Looper.getMainLooper().getThread().getId();
    }

    private static Handler sMainThreadHandler = new Handler(Looper.getMainLooper());

    public static Handler getMainThreadHandler() {
        return sMainThreadHandler;
    }

    private static boolean sIsMainProcess = true;

    public static void setIsMainProcess(boolean isMainProcess) {
        sIsMainProcess = isMainProcess;
    }

    public static boolean isMainProgress() {
        return sIsMainProcess;
    }

    public static void relase() {

    }
}
