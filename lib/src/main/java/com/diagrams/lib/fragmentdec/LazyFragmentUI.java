package com.diagrams.lib.fragmentdec;

import android.os.Bundle;
import android.os.Looper;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import com.diagrams.lib.Trigger;
import com.diagrams.lib.lazy.PendingObjHandler;

/**
 * 必须在Fragment的生命周期中调用本类相应的方法：
 * {@link #onCreate(Bundle)}、{@link #setUserVisibleHint(boolean)}、{@link
 * #onActivityCreated(Bundle)}、{@link #onResume()}、{@link #onPause()、{@link #onDestroy()}}
 * <p/>
 *
 * 做了两个工作：<br/>
 * 1. 延迟显示>>
 * 为了提高ui性能，仅在 onResume() 中填充UI（如果有数据），在 onPause() 中中断填充UI。
 * 如果是在ViewPager中，会同时兼顾该Fragment是否正在显示，只有正在显示时才会填充UI，否则不填充UI。<br/>
 * 注意：无论是否在ViewPager中，始终能够保证在 onResume() 执行过才认为是可填充UI。在 onPause() 或者 setUserVisibleHint(false)
 * 认为中断填充UI。
 * <p/>
 *
 * 2. 懒加载>>
 * 针对ViewPager(-- 调用{@link #setInViewPager(boolean)}方法来开启此功能 --）做了优化，
 * 只有当前显示时才会【开启初始化加载】，但是从当前显示变为非当前显示时不会取消数据加载，会延迟显示，等变为当前显示时才会填充UI。<br/>
 * 注意：无论是否在ViewPager中，总是能够保证在{@link #onActivityCreated(Bundle)}方法中或之后才【开启初始化加载】。
 * <p/>
 *
 * 要开启 {1. 延迟显示}  需要使用者自己做一个额外的操作：【在接收数据请求结果时调用{@link #slowResultForUI(Object, Object)}方法，
 * 然后在{@link OnLazyListener#optPendingObj(Object, Object)}方法中显示结果】
 * <p/>
 *
 * Created by lizhaofei on 2017/9/8 18:03
 */
//fixme lzf 处理系统回收与恢复
public class LazyFragmentUI<T> {
    private PendingObjHandler<T> mPendingObjHandler;

    private Trigger mInitTrigger;
    private boolean isFirstCreate = true;
    private boolean isSetInViewPagerCalled = false;

    private boolean isInViewPager = false;//便于理解，只要调用了setUserVisibleHint(Boolean)方法设置为true
    private boolean isVisibleToUser = false;
    private boolean isStartInitLoaded = false;
    private boolean isOnResumeState = false;

    private boolean isPreloadInViewPager = false;
    private OnLazyListener<T> mOnLazyListener;

    public interface OnLazyListener<T> {
        /** 开始初始化加载，只会回调一次 */
        void onStartInitLoad();

        void optPendingObj(Object tag, T pendingObj);

        /**
         * 只有 {@link #setInViewPager(boolean)} 设置true时，才会起作用
         *
         * @return true 会在ViewPager中预加载，同时会禁用延迟显示；false 不会预加载，只有显示时才会开启加载
         */
        boolean isPreloadInViewPager();

        /** 可以当作 onVisibleInViewPager */
        void onLazyUIVisible();

        /** 可以当作 onInVisibleInViewPager */
        void onLazyUIInvisible();
    }

    public LazyFragmentUI(@NonNull OnLazyListener<T> listener) {
        mOnLazyListener = listener;
        isPreloadInViewPager = mOnLazyListener.isPreloadInViewPager();
        mPendingObjHandler = new PendingObjHandler<>(new PendingObjHandler.OnPendingListener<T>() {
            @Override
            public void optPendingObj(Object tag, T pendingObj) {
                if (null != mOnLazyListener) {
                    mOnLazyListener.optPendingObj(tag, pendingObj);
                }
            }
        });
        mPendingObjHandler.disconnect();//设置为disconnect()，防止在onResume()还没有执行就已经有结果了
        mPendingObjHandler.setOnConnectChangeListener(new PendingObjHandler.OnConnectChangeListener() {//为了忽略首次disconnect()监听
            @Override
            public void onConnectChange(boolean connect) {
                if (null != mOnLazyListener) {
                    if(connect){
                        mOnLazyListener.onLazyUIVisible();
                    }else{
                        mOnLazyListener.onLazyUIInvisible();
                    }
                }
            }
        });
    }

    public void slowResultForUI(Object tag, T result) {
        if (Thread.currentThread().getId() != Looper.getMainLooper().getThread().getId()) {
            throw new RuntimeException(
                    "LazyFragmentUI method slowResultForUI(Object,Object) must called in MainThread!");
        }
        mPendingObjHandler.updatePendingObj(tag, result);
    }

    /** 必须在onCreate(Bundle) 方法之前调用 */
    public void setInViewPager(boolean isInViewPager) {
        this.isInViewPager = isInViewPager;
        isSetInViewPagerCalled = true;
    }

    /** @see PendingObjHandler#setPendingFlushSty(PendingObjHandler.OnPendingFlushStrategy) */
    public void setPendingTagFlushListener(PendingObjHandler.OnPendingFlushStrategy listener) {
        mPendingObjHandler.setPendingFlushSty(listener);
    }

    private void startInitLoadIfCan() {
        if (!isStartInitLoaded) {
            isStartInitLoaded = true;
            if (null != mOnLazyListener) {
                mOnLazyListener.onStartInitLoad();
            }
        }
    }

    //*******************************************************************以下需要在Fragment对应的生命周期方法中调用
    public void onCreate(@Nullable Bundle savedInstanceState) {
        //检测是否调用了 setInViewPager(Bundle) 方法
        if (!isSetInViewPagerCalled) {
            throw new RuntimeException(
                    "must call setInViewPager(Bundle) method before onCreate(Bundle)!");
        }
        //初始化逻辑
        isFirstCreate = true;
        final int triggerCount;
        if (isInViewPager) {
            triggerCount = 2;
        } else {
            triggerCount = 1;
        }
        mInitTrigger = new Trigger(triggerCount, new Trigger.Listener() {
            @Override
            public void trigger() {
                isFirstCreate = false;
                if (isInViewPager) {
                    if (isPreloadInViewPager) {
                        startInitLoadIfCan();
                    } else {
                        if (isVisibleToUser) {
                            startInitLoadIfCan();
                        }
                    }
                } else {
                    startInitLoadIfCan();
                }
            }
        });
    }

    public void setUserVisibleHint(boolean isVisibleToUser) {
        isInViewPager = true;//外部调用了这个方法，我们就直接将本Fragment设置为在ViewPager中，达到能够适应android的系统调用
        this.isVisibleToUser = isVisibleToUser;

        //初始化加载逻辑
        if (isFirstCreate) {
            mInitTrigger.touch();
        } else {
            if (isVisibleToUser) {
                startInitLoadIfCan();
            }
        }

        //延迟填充UI逻辑
        if (!isPreloadInViewPager) { //没有启用针对“ViewPager”的预加载
            if (isVisibleToUser && isOnResumeState) {//对用户可见，并且是onResume状态
                mPendingObjHandler.connect();
            }

            if (!isVisibleToUser) {//对用户不可见
                mPendingObjHandler.disconnect();
            }
        }
    }

    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        //初始化加载逻辑
        if (isFirstCreate) {
            mInitTrigger.touch();
        }
    }

    public void onResume() {
        //延迟填充UI逻辑
        isOnResumeState = true;
        if (isInViewPager) {
            if (isPreloadInViewPager) {//预加载
                mPendingObjHandler.connect();
            } else {//不预加载
                if (isVisibleToUser) {
                    mPendingObjHandler.connect();
                }
            }
        } else {
            mPendingObjHandler.connect();
        }
    }

    public void onPause() {
        //延迟填充UI逻辑
        isOnResumeState = false;
        mPendingObjHandler.disconnect();//保证不会将结果提交到UI上了
    }

    public void onDestroy() {
        mPendingObjHandler.clear();
    }
}
