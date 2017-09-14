package com.diagrams.rapid.sample;

import android.app.Application;

/**
 * 进程入口
 * <p/>
 * Created by lizhaofei on 2017/8/30 16:16
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Init.initInApp(this);
    }
}
