package com.diagrams.lib.activitydec;

import android.app.Activity;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import com.diagrams.lib.AndroidUtils;
import com.diagrams.lib.OSVersion;
import com.diagrams.lib.activitydec.core.SimpleIActivityDecorator;

/**
 * 处理透明状态栏
 * <p/>
 * Created by lizhaofei on 2017/9/5 15:03
 */
public class TransStatusBarAct extends SimpleIActivityDecorator {
    private static final int NORMAL_THEME_INDICATOR_HEIGHT = 45;//其他普通主题条件下，主页指示器（顶部状态栏）的高度

    private Activity mHost;

    @Override
    public void host(@NonNull Activity activity) {
        mHost = activity;
    }

    @Override
    public void onPreCreate(Bundle bundle) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mHost.getWindow().addFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
        }
    }

    @Override
    public void onPostCreate(Bundle bundle) {
        if (OSVersion.hasM()) { //6.0以止处理浅色状态栏
            Window window = mHost.getWindow();
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView()
                    .setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    @Override
    public void onStart() {
        //api19以上如若做透明状态栏需要调整头部高度
        if (OSVersion.hasKitKat()) {
            View titleBar = mHost.getWindow().getDecorView().findViewWithTag("titleBar");
            resetViewHeight(titleBar,
                    (int) AndroidUtils.dip2px(mHost, NORMAL_THEME_INDICATOR_HEIGHT));
        }
    }

    private void resetViewHeight(View titleBar, int titleHeight) {
        if (titleBar != null) {
            ViewGroup.LayoutParams params = titleBar.getLayoutParams();
            int h = (int) AndroidUtils.dip2px(mHost, 25);
            params.height = titleHeight + h;
            titleBar.setPadding(0, h, 0, 0);
            titleBar.setLayoutParams(params);
        }
    }
}
