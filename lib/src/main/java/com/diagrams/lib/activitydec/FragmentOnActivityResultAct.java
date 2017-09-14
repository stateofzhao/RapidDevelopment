package com.diagrams.lib.activitydec;

import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import com.diagrams.lib.activitydec.core.SimpleIActivityDecorator;
import java.util.List;

/**
 * 解决fragment嵌套 onActivityResult不能回调的问题
 * <p/>
 * Created by lizhaofei on 2017/9/11 19:01
 */
public class FragmentOnActivityResultAct extends SimpleIActivityDecorator {

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FragmentActivity fragmentActivity = null;
        if (mHost instanceof FragmentActivity) {
            fragmentActivity = (FragmentActivity) mHost;
        }
        if (null == fragmentActivity) {
            return;
        }

        FragmentManager fm = fragmentActivity.getSupportFragmentManager();
        int index = requestCode >> 16;
        if (index != 0) {
            index--;
            if (fm.getFragments() == null || index < 0 || index >= fm.getFragments().size()) {
                return;
            }
            Fragment frag = fm.getFragments().get(index);
            if (frag == null) {
            } else {
                handleResult(frag, requestCode, resultCode, data);
            }
        }
    }

    private void handleResult(Fragment frag, int requestCode, int resultCode, Intent data) {
        frag.onActivityResult(requestCode & 0xffff, resultCode, data);
        List<Fragment> frags = frag.getChildFragmentManager().getFragments();
        if (frags != null) {
            for (Fragment f : frags) {
                if (f != null) handleResult(f, requestCode, resultCode, data);
            }
        }
    }
}
