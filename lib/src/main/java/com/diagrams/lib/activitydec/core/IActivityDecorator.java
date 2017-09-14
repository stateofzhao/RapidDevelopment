package com.diagrams.lib.activitydec.core;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

/**
 * 与Activity有相同的Api
 *
 * <p/>
 * Created by lizhaofei on 2017/9/5 15:04
 */
public interface IActivityDecorator {

    void host(Activity activity);

    void onPreCreate(Bundle bundle);

    void onCreate(Bundle bundle);

    void onPostCreate(Bundle bundle);

    void onActivityResult(int requestCode, int resultCode, Intent data);

    void onStart();

    void onResume();

    void onPause();

    void onNewIntent(Intent intent);
}
