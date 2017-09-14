package com.diagrams.rapid.sample;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import cn.kuwo.lib.uilib.fragment.FragmentControl;

/**
 * Fragment基类
 * <p/>
 * Created by lizhaofei on 2017/9/8 10:58
 */
public class BaseFragment extends Fragment implements FragmentControl.Controllable {

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        FragmentControl.onFragmentCreate(this, savedInstanceState, this);//处理FragmentControl额外附加的操作
    }

    //创建Fragment之间的共享动画
    @Override
    public Object createShareTransition(String name) {
        return null;
    }
}
