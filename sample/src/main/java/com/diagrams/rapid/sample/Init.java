package com.diagrams.rapid.sample;

import android.app.Activity;
import android.app.Application;
import android.support.annotation.Nullable;
import cn.kuwo.lib.AppContextSupplier;
import cn.kuwo.lib.AppSupplier;
import cn.kuwo.lib.MainActivitySupplier;
import cn.kuwo.lib.share.core.ShareMgrImpl;
import cn.kuwo.lib.utils.KwDebug;

/**
 * 初始化
 * <p/>
 * Created by lizhaofei on 2017/9/1 16:30
 */
public class Init {
    private static boolean sApp = false;
    private static boolean sEntryCreate = false;
    private static boolean sEntryWin = false;
    private static boolean sInitOnce = false;
    private static boolean sInitMCM = false;
    private static boolean sInitMWM = false;

    public static void setIsMainProcess(boolean isMainProcess) {
        AppSupplier.setIsMainProcess(isMainProcess);
    }

    /** 在{@link App#onCreate()} 中执行，也就是说这个是一个进程开启后必须要执行的初始化 ，所有进程中都会执行 */
    public static void initInApp(Application application) {
        if (sApp) {
            return;
        }
        sApp = true;
        AppContextSupplier.set(application);
    }

    //================================主进程特有==================================start

    /** 在 EntryActivity#onCreate(Bundle) 中执行 ，只在主进程中执行 */
    public static void initInEntryActivityOnCreate(Activity activity) {
        if (sEntryCreate) {
            return;
        }
        sEntryCreate = true;
    }

    /** 在  EntryActivity#onWindowFocusChanged(Boolean) 中执行 ，只在主进程中执行 */
    public static void initInEntryActivityOnWid(Activity activity) {
        if (sEntryWin) {
            return;
        }
        sEntryWin = true;

        //fixme lzf 需要再Post一下

        initOnce(activity);
    }

    /** 在主进程的 MainActivity#onCreate() 中执行 */
    public static void initInMainActOnCreateMProcess(Activity activity) {
        if (sInitMCM) {
            return;
        }
        sInitMCM = true;

        MainActivitySupplier.set(activity);
    }

    /** 在主进程的 MainActivity#onWindowFocusChanged(Boolean) 中执行 */
    public static void initInMainActOnWinMProcess() {
        if (sInitMWM) {
            return;
        }
        sInitMWM = true;
    }//================================主进程特有==================================end

    //其实直接放到initApp()中也行，因为所有进程都会执行，但是为了启动速度，只能提出来，
    // 其它进程的初始化也可以调用这个方法来完成 滞后到Activity中才执行的初始化。

    /**
     * 在调用者进程中执行，注意，一定要在进程的“MainActivity”中调用，因为此方法会直接以静态的方式
     * 持有传进进来的Activity
     *
     * @param activity 如果有值，一定要是MainActivity，否则会造成Activity的泄露，当然也可以传递null
     */
    public static void initOnce(@Nullable Activity activity) {
        if (sInitOnce) {
            return;
        }
        sInitOnce = true;
        MainActivitySupplier.set(activity);//尽早设置

        ShareMgrImpl.getInstance().initSdk(AppContextSupplier.get());
    }

    private static boolean prepareExisting;

    public static void exitApp() {
        KwDebug.mustMainThread();

        if (prepareExisting) {
            KwDebug.classicAssert(false, "prepareExisting");
            return;
        }

        prepareExisting = true;

        //todo 做一些善后，然后设置 为已经退出状态
        AppSupplier.setIsExiting(true);
    }
}
