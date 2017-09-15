package com.diagrams.lib.init;

import android.app.Activity;
import android.app.Application;
import com.diagrams.lib.LibInit;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by lizhaofei on 2017/9/15 18:55
 */
public class InitMgr implements Init {
    private static List<Init> initList = new ArrayList<>();
    private static InitMgr instance = new InitMgr();

    static {
        initList.add(new LibInit());//这个依赖必须最先被回调
    }

    //最先添加进来的最先被回调
    public static void addInit(Init init) {
        if (null != init && !(init instanceof LibInit)) {
            initList.add(init);
        }
    }

    public static InitMgr get() {
        return instance;
    }

    @Override
    public void initInAppCreate(Application application) {
        final int size = initList.size();
        for (int i = 0; i < size; i++) {
            initList.get(i).initInAppCreate(application);
        }
    }

    @Override
    public void initInEntryActOnCreate(Activity activity) {
        final int size = initList.size();
        for (int i = 0; i < size; i++) {
            initList.get(i).initInEntryActOnCreate(activity);
        }
    }

    @Override
    public void initInEntryActOnWid(Activity activity) {
        final int size = initList.size();
        for (int i = 0; i < size; i++) {
            initList.get(i).initInEntryActOnWid(activity);
        }
    }

    @Override
    public void initInMainActOnCreate(Activity activity) {
        final int size = initList.size();
        for (int i = 0; i < size; i++) {
            initList.get(i).initInMainActOnCreate(activity);
        }
    }

    @Override
    public void initInMainActOnWid(Activity activity) {
        final int size = initList.size();
        for (int i = 0; i < size; i++) {
            initList.get(i).initInMainActOnWid(activity);
        }
    }

    @Override
    public void exitApp() {
        final int size = initList.size();
        for (int i = 0; i < size; i++) {
            initList.get(i).exitApp();
        }
    }

    private InitMgr() {
    }
}
