package com.diagrams.lib.init;

import android.app.Activity;
import android.app.Application;

/**
 * 初始化接口，注意，子类生命周期会存在整个app运行期间，所以不应该在其中存有能造成内存泄露的数据
 * <p/>
 * Created by lizhaofei on 2017/9/15 18:43
 */
public interface Init {

    //在Application的onCreate()方法中执行
    void initInAppCreate(Application application);

    //在“欢迎页”Activity的onCreate()方法中执行
    void initInEntryActOnCreate(Activity activity);

    //在“欢迎页”Activity的onWindowFocusChanged(Boolean)方法中执行
    void initInEntryActOnWid(Activity activity);

    //在“主页面”Activity的onCreate()方法中执行
    void initInMainActOnCreate(Activity activity);

    //在“主页面”Activity的onWindowFocusChanged(Boolean)方法中执行
    void initInMainActOnWid(Activity activity);

    //此时退出了，释放所有资源
    void exitApp();
}
