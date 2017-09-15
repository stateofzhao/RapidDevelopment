package com.diagrams.rapid.sample;

import android.app.Application;
import com.diagrams.lib.LibInit;
import com.diagrams.lib.init.InitMgr;

/**
 * 进程入口
 * <p/>
 * Created by lizhaofei on 2017/8/30 16:16
 */
public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        //添加所有依赖模块的初始化工具
        InitMgr.addInit(new LibInit());

        InitMgr.get().initInAppCreate(this);
    }
}
