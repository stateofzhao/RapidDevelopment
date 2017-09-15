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

    //是否是debug版本
    public static boolean IS_DEBUG = true;
    // 是否是正式包，非正式的试用包也是release包，但可能会有某些日志记录等额外行为
    public static boolean IS_FORMAL = false;


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
