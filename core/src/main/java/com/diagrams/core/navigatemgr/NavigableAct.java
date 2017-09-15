package com.diagrams.core.navigatemgr;

import android.content.Intent;
import com.diagrams.lib.activitydec.core.ADecFieldAnnotation;
import com.diagrams.lib.activitydec.core.SimpleIActivityDecorator;

/**
 * 能够解析导航协议进行导航，{@link NaviBuilder}相关类
 * <p/>
 * Created by lizhaofei on 2017/9/12 10:07
 */
public class NavigableAct extends SimpleIActivityDecorator {
    private INavigable iNavigable;
    private boolean isActive;

    @ADecFieldAnnotation(interruptMethod = "onNewIntent")
    private boolean interruptOnNewIntent = false;//是否截断 onNewIntent(Intent) 方法

    public NavigableAct(INavigable iNavigable) {
        this.iNavigable = iNavigable;
    }

    @Override
    public void onResume() {
        super.onResume();
        NaviMgr.iamvisible(iNavigable);//触发导航检测（触发 INavigable#onNavigate()方法）
        isActive = true;
    }

    @Override
    public void onPause() {
        super.onPause();
        NaviMgr.iamnotvisible(iNavigable);
        isActive = false;
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        String urlString = intent.getStringExtra(NaviBuilder.NAVIGATE_PARAS_KEY);
        if (urlString != null) {
            NaviMgr.iamvisible(iNavigable);//触发导航检测（触发 INavigable#onNavigate()方法）
        }

        if (isActive) {
            if (NaviMgr.dispatch(intent)) {
                interruptOnNewIntent = true;//拦截后续 装饰者onNewIntent(Intent)方法的执行
            }
        }
    }
}
