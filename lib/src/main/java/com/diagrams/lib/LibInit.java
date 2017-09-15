package com.diagrams.lib;

import android.app.Activity;
import android.app.Application;
import com.diagrams.lib.init.Init;

/**
 * lib 模块初始化工具
 * <p/>
 * Created by lizhaofei on 2017/9/15 18:53
 */
public class LibInit implements Init {

    @Override
    public void initInAppCreate(Application application) {
        AppContextSupplier.set(application);
    }

    @Override
    public void initInEntryActOnCreate(Activity activity) {

    }

    @Override
    public void initInEntryActOnWid(Activity activity) {

    }

    @Override
    public void initInMainActOnCreate(Activity activity) {
        MainActivitySupplier.set(activity);
    }

    @Override
    public void initInMainActOnWid(Activity activity) {

    }

    @Override
    public void exitApp() {
        AppSupplier.setIsExiting(true);
    }
}
