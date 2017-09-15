package com.diagrams.lib.fragmentdec;

import android.os.Build;
import android.os.Bundle;
import android.support.annotation.IntDef;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.ViewCompat;
import android.transition.TransitionInflater;
import android.view.View;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.util.List;

import static com.diagrams.lib.fragmentdec.FragmentControl.SameTagSty.CHECK;
import static com.diagrams.lib.fragmentdec.FragmentControl.SameTagSty.COEXIST;
import static com.diagrams.lib.fragmentdec.FragmentControl.SameTagSty.POP;
import static com.diagrams.lib.fragmentdec.FragmentControl.SameTagSty.REMOVE;
import static com.diagrams.lib.fragmentdec.FragmentControl.SameTagSty.WEAK_CHECK;

/**
 * 针对单个Activity的Fragment管理器
 * <p/>
 * Created by lizhaofei on 2017/9/12 16:53
 */
public class FragmentControl implements FragmentManager.OnBackStackChangedListener {
    private static final String TAG = "FragmentControl";

    public static final String KEY_ANIM_SHARE_NAME = "___key_anim_share_name";

    @Retention(RetentionPolicy.SOURCE) @IntDef({
            POP, COEXIST, REMOVE, CHECK, WEAK_CHECK
    }) public @interface SameTagSty {
        int POP = 1; //移除上面的Fragment
        int COEXIST = 2;//忽略已经存在对应tag的fragment，直接再添加一个新的
        int REMOVE = 3;//移除已经存在的Fragment，然后将新的Fragment添加进来
        int CHECK = 4;//如果已经存在了就抛出异常
        int WEAK_CHECK = 5;//如果已经存在了，不执行任何操作
    }

    /** 监听宿主Activity中Fragment变化的接口 */
    public interface OnActivityFragmentsLisenter {
        void onFragmentRemoved(Fragment removed);

        void onFragmentAdd(Fragment add);

        void onFragmentShow(Fragment show);

        void onFragmentHide(Fragment hide);
    }//END

    //Fragment 动画
    public interface FragmentAnimator {
        @NonNull
        FragmentTransaction configAnim(@NonNull FragmentTransaction fragmentTransaction);
    }//END

    public interface Controllable {
        //创建Fragment之间的共享动画
        Object createShareTransition(String name);
    }

    private FragmentManager fragmentManager;//这个东西在android framework 层应该是一个Activity只有一个

    public FragmentControl(@NonNull FragmentActivity activity) {
        fragmentManager = activity.getSupportFragmentManager();
        fragmentManager.addOnBackStackChangedListener(this);//监听Fragment数量改变
    }

    public boolean showFragmentByTag(Fragment frag, int layoutId, String tag,
            @SameTagSty int tagSty, FragmentAnimator animator) {
        Fragment fragment = getFragmentByTag(tag);
        if (null != fragment) {
            switch (tagSty) {
                case POP: {
                    removeTopFragment(tag);
                    break;
                }
                case COEXIST: {

                    break;
                }
                case REMOVE: {

                    break;
                }
                case CHECK: {

                    break;
                }
                case WEAK_CHECK: {

                    break;
                }
            }
        }

        return false;
    }

    public boolean closeFragmentByTag(String tag) {

        return false;
    }

    /** 当前Activity包含的所有Fragment数量，包括 【不在返回栈中】的Fragment */
    public int sizeOfFragments() {
        final List fragments = fragmentManager.getFragments();
        return null == fragments ? 0 : fragments.size();
    }

    /** 当前Activity包含的【在返回栈中】的fragment数量 */
    public int sizeOfBackStateFragments() {
        return fragmentManager.getBackStackEntryCount();
    }

    /** 移除顶部Fragment */
    public boolean removeTopFragment() {
        final List<Fragment> fragments = fragmentManager.getFragments();
        if (null != fragments && fragments.size() > 0) {
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.remove(fragments.get(fragments.size() - 1));
            fragmentTransaction.commitNowAllowingStateLoss();
            return true;
        }
        return false;
    }

    //移除指定tag上面的所有Fragment
    public void removeTopFragment(String tag) {
        final List<Fragment> fragments = fragmentManager.getFragments();
        final int size = null == fragments ? 0 : fragments.size();
        FragmentTransaction fragmentTransaction = null;
        if (null != fragments && size > 1) {
            for (int i = size - 1; i >= 0; i--) {//按照添加顺序，倒序删除
                final Fragment fragment = fragments.get(i);
                if (null == fragment) {
                    continue;
                }
                final String fTag = fragment.getTag();
                if (null != tag && fTag.equals(tag)) {
                    if (null == fragmentTransaction) {
                        fragmentTransaction = fragmentManager.beginTransaction();
                    }
                    fragmentTransaction.show(fragment);
                    break;
                }
                if (null == fragmentTransaction) {
                    fragmentTransaction = fragmentManager.beginTransaction();
                }
                fragmentTransaction.remove(fragment);
            }
        }
        if (null != fragmentTransaction && !fragmentTransaction.isEmpty()) {
            fragmentTransaction.commitNowAllowingStateLoss();
        }
    }

    @Nullable
    public Fragment getFragmentByTag(String tag) {
        return fragmentManager.findFragmentByTag(tag);
    }

    @Override
    public void onBackStackChanged() {

    }

    //在所有被FragmentControl管理的Fragment的 onCreate(Bundle) 方法中调用
    public static void onFragmentCreate(@NonNull Fragment fragment,
            @Nullable Bundle savedInstanceState, Controllable controllable) {
        final Bundle args = fragment.getArguments();

        //检测动画设置
        if (null != args && args.containsKey(KEY_ANIM_SHARE_NAME)) {
            if (null == controllable) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                    fragment.setSharedElementEnterTransition(
                            TransitionInflater.from(fragment.getContext())
                                    .inflateTransition(android.R.transition.move));
                }
            } else {
                fragment.setSharedElementEnterTransition(
                        controllable.createShareTransition(args.getString(KEY_ANIM_SHARE_NAME)));
            }
        }
    }

    public static class NONE_ANIM implements FragmentAnimator {
        @Override
        @NonNull
        public FragmentTransaction configAnim(@NonNull FragmentTransaction fragmentTransaction) {
            fragmentTransaction.setTransition(FragmentTransaction.TRANSIT_NONE);
            fragmentTransaction.setCustomAnimations(0, 0, 0, 0);
            return fragmentTransaction;
        }
    }//END

    //共享元素动画，注意，需要在两个Fragment中给作为 共享动画的View设置一个  android:transitionName="simple transition name" 属性，
    // 并且属性名必须一致
    public static class SHARE_VIEW_ANIM implements FragmentAnimator {
        private View shareView;
        private Fragment forOpen;

        public SHARE_VIEW_ANIM(View shareView, Fragment forOpen) {
            this.shareView = shareView;
            this.forOpen = forOpen;
        }

        public SHARE_VIEW_ANIM() {
        }

        public SHARE_VIEW_ANIM(View shareView) {
            this.shareView = shareView;
        }

        public void setShareView(View shareView) {
            this.shareView = shareView;
        }

        public void setFragment(Fragment forOpen) {
            this.forOpen = forOpen;
        }

        @Override
        @NonNull
        public FragmentTransaction configAnim(@NonNull FragmentTransaction fragmentTransaction) {
            if (null != shareView && null != forOpen) {
                final String transitionName = ViewCompat.getTransitionName(shareView);
                fragmentTransaction.addSharedElement(shareView, transitionName);
                Bundle args = forOpen.getArguments();
                if (null == args) {
                    args = new Bundle();
                }
                if (args.containsKey(KEY_ANIM_SHARE_NAME)) {
                    throw new RuntimeException("Fragment contain RETAIN key!");
                }
                args.putString(KEY_ANIM_SHARE_NAME, transitionName);
                forOpen.setArguments(args);
            }
            return fragmentTransaction;
        }
    }//END
}
