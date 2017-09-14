package com.diagrams.rapid.sample.lazyui;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import com.diagrams.lib.fragmentdec.LazyFragmentUI;
import com.diagrams.lib.lazy.PendingObjHandler;
import com.diagrams.mvp.IView;
import com.diagrams.mvp.PresenterImpl;
import com.diagrams.mvp.QueryEnum;
import com.diagrams.mvp.box.ModelWithHttp;
import com.diagrams.rapid.sample.BaseFragment;

/**
 * 懒加载http请求Fragment。<br/>
 * 懒加载参见：{@link LazyFragmentUI}<br/>
 * http请求参见：{@link ModelWithHttp}<br/>
 * <p/>
 * 注意，必须设置一个初始化参数来实现: Bundle args = new Bundle(); args.putBoolean(KEY_IN_VIEWPAGER,boolean)
 * ，否则会直接报错。
 * <p/>
 * Created by lizhaofei on 2017/9/8 20:20
 */
public abstract class BaseHttpLazyFragment<T extends ModelWithHttp<Q, UA>, Q extends ModelWithHttp.HttpQueryEnum, UA extends ModelWithHttp.HttpUserActionEnum>
        extends BaseFragment implements LazyFragmentUI.OnLazyListener<T>, IView<T, Q, UA> {
    public static final String KEY_IN_VIEWPAGER = "____key_in_viewpager";

    private LazyFragmentUI<T> mLazyFragmentUI;

    private PresenterImpl<T, Q, UA> mPresenter;
    protected UserActionListener<UA> mUserActionListener;

    protected abstract T onCreateHttpModel();

    /** 初始化加载-成功，显示内容View */
    protected abstract View onCreateContentView(LayoutInflater inflater, ViewGroup container,
            T result);

    /** 初始化加载-进度条 */
    protected abstract View onCreateLoadingView(LayoutInflater inflater, ViewGroup container);

    /** 初始化加载-请求服务发生错误，包括很多种情况（例如网络超时），可以通过http结果码来查询具体错误信息 */
    protected abstract View onCreateFailureView(LayoutInflater inflater, ViewGroup container);

    /** 初始化加载-显示只打开了wifi联网开关 */
    protected abstract View onCreateOnlyWifiView(LayoutInflater inflater, ViewGroup container);

    /** 初始化加载-显示网络不可用View，注意与网络超时View区分（onCreateFailureView()） */
    protected abstract View onCreateNetUnavailableView(LayoutInflater inflater,
            ViewGroup container);

    /** @param state 参见{@link CommHttpTask#STATE_EMPTY}等 */
    protected void onShowHttpUserAction(UA userAction, int state, T result) {
    }

    protected void onShowUserAction(UA userAction, T result) {
    }

    protected View onCreateTitleView() {
        return null;
    }

    /** 很少被回调，这个是代码发生致命错误才会回调 */
    protected View onCreateErrorView(LayoutInflater inflater, ViewGroup container) {
        return null;
    }

    //显示初始化请求状态
    private void showInitLoadState(Q query, T result) {
        View contentView = null;
        int state = mPresenter.getModel().getHttpTaskLoadState(query);
        if (state == CommHttpTask.STATE_SUCCESS) {
            contentView = onCreateContentView(mLayoutInflater, mContentContainer, result);
        } else if (state == CommHttpTask.STATE_FAILURE) {
            contentView = onCreateFailureView(mLayoutInflater, mContentContainer);
        } else if (state == CommHttpTask.STATE_LOADING) {
            contentView = onCreateLoadingView(mLayoutInflater, mContentContainer);
        } else if (state == CommHttpTask.STATE_NET_UNAVAILABLE) {
            contentView = onCreateNetUnavailableView(mLayoutInflater, mContentContainer);
        } else if (state == CommHttpTask.STATE_ERROR) {
            contentView = onCreateErrorView(mLayoutInflater, mContentContainer);
        } else if (state == CommHttpTask.STATE_ONLY_WIFI) {
            contentView = onCreateOnlyWifiView(mLayoutInflater, mContentContainer);
        }
        showContentView(contentView);
    }

    //显示用户操作状态
    protected void showUserActionState(UA userAction, T result) {
        int state = mPresenter.getModel().getHttpTaskLoadState(userAction);
        if (0 != state) {
            onShowHttpUserAction(userAction, state, result);
        } else {
            onShowUserAction(userAction, result);
        }
    }

    //***********************************************************************************************LazyFragmentUI.OnLazyListener
    @Override
    public void onStartInitLoad() {
        mPresenter.initLoad();
    }

    @Override
    public void optPendingObj(Object tag, T pendingObj) {
        if (tag instanceof ModelWithHttp.HttpQueryEnum) {
            showInitLoadState((Q) tag, pendingObj);
        } else if (tag instanceof QueryEnum) {
            showUserActionState((UA) tag, pendingObj);
        }
    }

    @Override
    public void onLazyUIVisible() {
    }

    @Override
    public void onLazyUIInvisible() {
    }

    //***********************************************************************************************IView
    @Override
    public void addUserActionListener(UserActionListener<UA> listener) {
        mUserActionListener = listener;
    }

    @Override
    public void displayData(T model, Q query) {
        mLazyFragmentUI.slowResultForUI(query, model);//延迟显示UI，fixme lzf 注意这个一定要在UI线程中执行
    }

    @Override
    public void displayErrorMsg(Q query) {
        mLazyFragmentUI.slowResultForUI(query, null);//延迟显示UI，fixme lzf 注意这个一定要在UI线程中执行
    }

    @Override
    public void displayUserActionResult(T model, UA userAction, boolean success) {
        mLazyFragmentUI.slowResultForUI(userAction, model);//延迟显示UI，fixme lzf 注意这个一定要在UI线程中执行
    }

    //***********************************************************************************************Fragment
    protected View mRootView;
    private FrameLayout mTitleContainer;// 用于显示标题栏
    private FrameLayout mContentContainer;// 用于显示内容，在标题栏下面显示
    private FrameLayout mShadeContainer;//在标题栏和内容 上层全屏显示

    private LayoutInflater mLayoutInflater;

    public BaseHttpLazyFragment() {
        //尽早实例化Presenter
        final T model = onCreateHttpModel();
        mPresenter = new PresenterImpl<>(model, this, model.getQueries(), model.getUserActions());

        //尽早实例化LazyFragmentUI
        mLazyFragmentUI = new LazyFragmentUI<>(this);
        mLazyFragmentUI.setPendingTagFlushListener(new PendingObjHandler.OnPendingFlushStrategy() {
            @Override
            public int tagToInteger(Object tag) {//根据id来移除重复的延迟更新数据
                if (tag instanceof ModelWithHttp.HttpQueryEnum) {
                    return ((ModelWithHttp.HttpQueryEnum) tag).getId();
                } else if (tag instanceof ModelWithHttp.HttpUserActionEnum) {
                    return ((ModelWithHttp.HttpUserActionEnum) tag).getId();
                } else {
                    return null == tag ? 0 : tag.hashCode();
                }
            }
        });
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //检测初始化参数
        Bundle args = getArguments();
        boolean isRightArgs = false;
        boolean isInViewPager = false;
        if (null != args && args.containsKey(KEY_IN_VIEWPAGER)) {
            Object keyValue = args.get(KEY_IN_VIEWPAGER);
            if (keyValue instanceof Boolean) {
                isRightArgs = true;
                isInViewPager = (boolean) keyValue;
            }
        }
        if (!isRightArgs) {
            throw new IllegalArgumentException(
                    "please set init params Bundle that must contain key KEY_IN_VIEWPAGER and value must is a boolean!!");
        }
        mLazyFragmentUI.setInViewPager(isInViewPager);
        mLazyFragmentUI.onCreate(savedInstanceState);
    }

    @Override
    public void setUserVisibleHint(boolean isVisibleToUser) {
        super.setUserVisibleHint(isVisibleToUser);
        mLazyFragmentUI.setUserVisibleHint(isVisibleToUser);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
            @Nullable Bundle savedInstanceState) {
        mLayoutInflater = inflater;
        final View rootView = inflater.inflate(R.layout.fragment_base_lazy_http, container, false);
        mRootView = rootView;
        mTitleContainer = rootView.findViewById(R.id.base_lzy_http_title_container);
        mContentContainer = rootView.findViewById(R.id.base_lzy_http_content_container);
        mShadeContainer = rootView.findViewById(R.id.base_lzy_http_shade_container);

        showTitleView(onCreateTitleView());
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        mLazyFragmentUI.onActivityCreated(savedInstanceState);
    }

    @Override
    public void onResume() {
        super.onResume();
        mLazyFragmentUI.onResume();
    }

    @Override
    public void onPause() {
        super.onPause();
        mLazyFragmentUI.onPause();
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        mLazyFragmentUI.onDestroy();
        //fixme PS 其实也完全可以不取消，因为数据请求早晚会结束，所以也不会造成泄露，只是释放早晚的问题
        //取消 所有数据请求，防止本类泄露
        if (null != mUserActionListener) {
            final Bundle args = new Bundle();
            args.putString(ModelWithHttp.KEY_TYPE_CANCEL, "");
            mUserActionListener.onUserAction(null, args);
        }
    }

    protected void showShadeView(View view) {
        if (null == view || !isActive()) {
            return;
        }
        mShadeContainer.removeAllViews();
        mShadeContainer.addView(view);
    }

    private void showContentView(@Nullable View view) {
        if (null == view || !isActive()) {
            return;
        }
        mContentContainer.removeAllViews();
    }

    private void showTitleView(View view) {
        if (null == view || !isActive()) {
            return;
        }
        mTitleContainer.removeAllViews();
        mTitleContainer.addView(view);
    }

    private boolean isActive() {
        return (getActivity() != null && !getActivity().isFinishing() && !isDetached());
    }

    protected void doRefreshAction(UA userAction, Bundle args) {
        if (null != mUserActionListener) {
            if (null == args) {
                args = new Bundle();
            }
            args.putBoolean(ModelWithHttp.KEY_TYPE_REFRESH, true);
            args.putBoolean(ModelWithHttp.KEY_TYPE_LOADMORE, false);
            mUserActionListener.onUserAction(userAction, args);
        }
    }

    protected void doLoadMoreAction(UA userAction, Bundle args) {
        if (null != mUserActionListener) {
            if (null == args) {
                args = new Bundle();
            }
            args.putBoolean(ModelWithHttp.KEY_TYPE_REFRESH, false);
            args.putBoolean(ModelWithHttp.KEY_TYPE_LOADMORE, true);
            mUserActionListener.onUserAction(userAction, args);
        }
    }
}
