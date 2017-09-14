package com.diagrams.lib.activitydec;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import com.diagrams.lib.activitydec.core.SimpleIActivityDecorator;

/**
 * 将打开Activity的Intent集中处理，一般用来处理点击通知栏后的处理
 * <p/>
 * Created by lizhaofei on 2017/9/11 18:33
 */
public class DispatchIntentAct extends SimpleIActivityDecorator {
    private Intent prepareIntent;
    private boolean isActive;
    private OnDispatchIntentListener listener;

    public interface OnDispatchIntentListener {
        void onDispatchIntent(Intent intent);

        //与原Activity得onNewIntent(Intent)一样，一般用不到
        void onActNewIntent(Intent intent);
    }

    public DispatchIntentAct(@NonNull OnDispatchIntentListener listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(Bundle bundle) {
        prepareIntent = mHost.getIntent();
        super.onCreate(bundle);
    }

    @Override
    public void onResume() {
        super.onResume();
        isActive = true;
        if (prepareIntent != null) {
            listener.onDispatchIntent(prepareIntent);
            prepareIntent = null;
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        isActive = false;
    }

    @Override
    public void onNewIntent(Intent intent) {
        super.onNewIntent(intent);
        if (isActive) {
            listener.onDispatchIntent(intent);
        } else {
            prepareIntent = intent;
        }
        listener.onActNewIntent(intent);
    }
}
