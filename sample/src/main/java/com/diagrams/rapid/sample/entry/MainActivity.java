package com.diagrams.rapid.sample.entry;

import android.os.Bundle;
import android.support.annotation.Nullable;
import com.diagrams.rapid.sample.BaseActivity;
import com.diagrams.rapid.sample.Init;

/**
 * app主页
 * <p/>
 * Created by lizhaofei on 2017/9/4 10:14
 */
public class MainActivity extends BaseActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Init.initInMainActOnCreateMProcess(this);
    }

    @Override
    public void onWindowFocusChanged(boolean hasFocus) {
        super.onWindowFocusChanged(hasFocus);
        if (hasFocus) {
            Init.initInMainActOnWinMProcess();
        }
    }
}
