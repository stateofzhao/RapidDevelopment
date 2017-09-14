package com.diagrams.lib.activitydec.core;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * 缩放IActivityDecorator
 * <p/>
 * Created by lizhaofei on 2017/9/5 15:52
 */
public class SimpleIActivityDecorator implements IActivityDecorator {
    protected Activity mHost;

    @Override
    public void host(Activity activity) {
        mHost = activity;
    }

    @Override
    public void onPreCreate(Bundle bundle) {

    }

    @Override
    public void onCreate(Bundle bundle) {
    }

    @Override
    public void onPostCreate(Bundle bundle) {
    }

    @Override
    public void onNewIntent(Intent intent) {
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
    }

    @Override
    public void onStart() {
    }

    @Override
    public void onResume() {
    }

    @Override
    public void onPause() {
    }
}
