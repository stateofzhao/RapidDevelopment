package com.diagrams.rapid.sample.entry;

import android.os.Bundle;
import android.support.annotation.Nullable;
import com.diagrams.lib.init.InitMgr;
import com.diagrams.rapid.sample.BaseActivity;

/**
 * 欢迎页，app总入口
 * <p/>
 * Created by lizhaofei on 2017/9/1 18:37
 */
public class EntryActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        InitMgr.get().initInEntryActOnCreate(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            InitMgr.get().initInEntryActOnWid(this);
        }
    }
}
