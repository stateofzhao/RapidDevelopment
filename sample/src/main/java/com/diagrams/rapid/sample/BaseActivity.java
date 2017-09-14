package com.diagrams.rapid.sample;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.FragmentActivity;
import cn.kuwo.core.navigatemgr.INavigable;
import cn.kuwo.core.navigatemgr.NaviBuilder;
import cn.kuwo.core.navigatemgr.NaviPathParam;
import cn.kuwo.core.navigatemgr.NavigableItems;
import cn.kuwo.lib.uilib.activitydec.DispatchIntentAct;
import cn.kuwo.lib.uilib.activitydec.FragmentOnActivityResultAct;
import cn.kuwo.lib.uilib.activitydec.NavigableAct;
import cn.kuwo.lib.uilib.activitydec.core.ActivityDecorators;
import cn.kuwo.lib.uilib.activitydec.TransStatusBarAct;

/**
 * Activity基类，只做与Activity生命周期有关的处理，所有处理都采用装饰组件的形式来实现。
 * <p/>
 * Created by lizhaofei on 2017/9/1 18:07
 */
public class BaseActivity extends FragmentActivity {
    private ActivityDecorators mActivityDecorators;

    /** @see DispatchIntentAct */
    protected void onDispatchIntent(Intent intent) {

    }

    /** @see DispatchIntentAct */
    protected void onActNewIntent(Intent intent) {

    }

    /** @see NavigableAct */
    protected boolean onNavigate(NaviBuilder builder, NavigableItems toID,
            NaviPathParam currentParams) {
        return false;
    }

    /** @see NavigableAct */
    protected NavigableItems getNavigableID() {
        return NavigableItems.NAVI_NONE;
    }

    //**********************************************************************************************Activity
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        initActivityDecorators();
        mActivityDecorators.onPreCreate(savedInstanceState);
        super.onCreate(savedInstanceState);
        mActivityDecorators.onCreate(savedInstanceState);
    }

    @Override
    protected void onPostCreate(@Nullable Bundle savedInstanceState) {
        super.onPostCreate(savedInstanceState);
        mActivityDecorators.onPostCreate(savedInstanceState);
    }

    @Override
    protected void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        mActivityDecorators.onNewIntent(intent);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mActivityDecorators.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mActivityDecorators.onStart();
    }

    @Override
    protected void onResume() {
        super.onResume();
        mActivityDecorators.onResume();
    }

    @Override
    protected void onPause() {
        super.onPause();
        mActivityDecorators.onPause();
    }

    //初始化所有Activity装饰器
    private void initActivityDecorators() {
        if (null != mActivityDecorators) {
            return;
        }
        mActivityDecorators = new ActivityDecorators(this);

        //透明状态栏
        final TransStatusBarAct transStatusBarAct = new TransStatusBarAct();
        //解决fragment嵌套 onActivityResult不能回调的问题
        final FragmentOnActivityResultAct fra = new FragmentOnActivityResultAct();
        //INavigable
        final NavigableAct navigableAct = new NavigableAct(new INavigable() {
            @Override
            public NavigableItems getNavigableID() {
                return BaseActivity.this.getNavigableID();
            }

            @Override
            public boolean onNavigate(NaviBuilder builder, NavigableItems toID,
                    NaviPathParam currentParams) {
                return BaseActivity.this.onNavigate(builder, toID, currentParams);
            }
        });
        //分发Intent
        final DispatchIntentAct dispatchIntentAct =
                new DispatchIntentAct(new DispatchIntentAct.OnDispatchIntentListener() {
                    @Override
                    public void onDispatchIntent(Intent intent) {
                        BaseActivity.this.onDispatchIntent(intent);
                    }

                    @Override
                    public void onActNewIntent(Intent intent) {
                        BaseActivity.this.onActNewIntent(intent);
                    }
                });

        mActivityDecorators.add(transStatusBarAct);
        mActivityDecorators.add(fra);
        mActivityDecorators.add(navigableAct);//注意这个要在 dispatchIntentAct 前面，因为这个优先级更高，它可能会拦截修饰方法向下传递
        mActivityDecorators.add(dispatchIntentAct);
    }
}
