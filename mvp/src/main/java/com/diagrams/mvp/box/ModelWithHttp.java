package com.diagrams.mvp.box;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.text.TextUtils;
import android.util.SparseArray;
import android.util.SparseIntArray;
import com.diagrams.core.debug.KwDebug;
import com.diagrams.mvp.IModel;
import com.diagrams.mvp.QueryEnum;
import com.diagrams.mvp.QueryEnumHelper;
import com.diagrams.mvp.UserActionEnum;
import com.diagrams.mvp.UserActionEnumHelper;

/**
 * 能够进行http请求的通用model。只要是需要执行网络请求的，本Model都会代为执行，其它的不需要执行网络请求的行为则
 * 需要子类自己来实现（只要在{@link HttpQueryEnum#getUrl()}/{@link HttpUserActionEnum#getUrl(int)}返回空即认为不执行网络请求）。
 * <br/>
 * 可以通过{@link #getHttpTaskLoadState(HttpQueryEnum)}、{@link #getHttpTaskLoadState(HttpUserActionEnum)}方法来获取当前加载状态，从而显示不同的UI。
 * <br/>
 * 注意！注意！注意：千万不要让 【HttpQueryEnum的id】 和 【HttpUserActionEnum的id】 相同。
 * <p/>
 * Created by lizhaofei on 2017/9/6 10:26
 */
public abstract class ModelWithHttp<Q extends ModelWithHttp.HttpQueryEnum, UA extends ModelWithHttp.HttpUserActionEnum>
        implements IModel<Q, UA> {
    /**
     * {@link #deliverUserAction(HttpUserActionEnum, Bundle, UserActionCallback)}第二个参数Bundle的一个约定参数键值，
     * 值必须是boolean型的，用来告诉Model来执行刷新操作
     */
    public static final String KEY_TYPE_REFRESH = "key_type_refresh";
    /**
     * {@link #deliverUserAction(HttpUserActionEnum, Bundle, UserActionCallback)}第二个参数Bundle的一个约定参数键值，
     * 值必须是boolean型的，用来告诉Model来执行加载更多操作
     */
    public static final String KEY_TYPE_LOADMORE = "key_type_loadMore";
    /** 取消所有网络请求 */
    public static final String KEY_TYPE_CANCEL = "key_type_cancel";

    private Q[] mQueries;
    private UA[] mUserActions;
    private SparseArray<CommHttpTask.TaskHandle> mHttpTasks = new SparseArray<>();

    public interface HttpQueryEnum extends QueryEnum {
        String getUrl();

        byte[] getPostData();
    }// HttpQueryEnum end

    public interface HttpUserActionEnum extends UserActionEnum {
        /** @param page 分页页数，从1开始 */
        String getUrl(int page);

        byte[] getPostData(int page);
    }// HttpUserActionEnum end

    //现在才真正明白，为什么这里要传递进来 着两个数组。
    // 我之前的理解一直是：既然Model中已经提前定义了着两个数组，那么完全不必要再多此一举，
    // 使用的时候直接调用 Model#requestData(Model#queryEnum,Callback)即可，
    // Model#deliverUserAction(Model#userAction,args,callback)即可。

    //然后上述理解完全是错误的！其实QueryEnum不单单是用来唯一标记的，它还有一个重要的使命就是携带请求需要的数据。
    // 同理UserActionEnum也不单单是用来唯一区别用户操作的，它也有一个重要的使命就是携带执行用户操作需要的数据。
    // 明白了 QueryEnum和UserActionEnum 的真正意义后，我们再来看为什么要在构造函数传递这两个参数，因为如果
    // 不传递这两个参数，Model就不能够区分执行requestData(QueryEnum q)方法时，【参数q是否是它应该来处理的】，因为
    // Model执行requestData(QueryEnum q)时能够处理所有QueryEnum类型的参数，Model只需要获取q携带的请求数据
    // 来开启请求即可；同理deliverUserAction(UserActionEnum ua)方法也是如此。

    //明白了真正的含义后，那么也应该明白，这样处理才能够开发出通用Model，比如本Model，
    // 就不需要自己来定义具体的QueryEnum[]，我们仅仅需要在requestData()方法中获取到QueryEnum携带的数据来执行请求即可。
    // 同理也不需要定义具体的UserActionEnum[]，在deliverUserAction()方法中，我们获取UserActionEnum携带的数据来执行
    // 操作即可。这样本Model的子类就能够自由的定义自己的QueryEnum[] 和 UserActionEnum[] 即可。【而本Model仅仅是定义了
    // requestData()方法中请求数据的规则===>CommHttpTask中的init请求规则 和 deliverUserAction()方法中执
    // 行用户操作请求的规则===>CommHttpTask中的 Refresh/LoadMore 规则】。
    public ModelWithHttp(Q[] queries, UA[] userActions) {
        mQueries = queries;
        mUserActions = userActions;
    }

    /** 解析网络数据，子类一般要将数据解析到自身属性中 */
    protected abstract void readDataFromString(Q query, String netResult);

    /** 解析网络数据，子类一般要将数据解析到自身属性中 */
    protected abstract void readDataFromString(UA userAction, String netResult);

    /** 处理非Http的请求 */
    protected void processRequest(Q query, @Nullable RequestCallback<Q> callback) {
    }

    /** 处理非Http的用户操作 */
    protected void processUserAction(UA userAction, @Nullable Bundle args,
            @Nullable UserActionCallback<UA> callback) {
    }

    /** 配置Http任务的策略 */
    protected void httpStrategy(@NonNull CommHttpTask httpTask) {
    }

    @Override
    public Q[] getQueries() {
        return mQueries;
    }

    @Override
    public UA[] getUserActions() {
        return mUserActions;
    }

    //取消所有请求
    public void cancelAll() {
        KwDebug.mustMainThread();
        final int size = mHttpTasks.size();
        for (int i = 0; i < size; i++) {
            CommHttpTask.TaskHandle taskHandle = mHttpTasks.valueAt(i);
            if (null != taskHandle) {
                taskHandle.cancel();
            }
        }
        mHttpTasks.clear();
    }

    /**
     * 获取QueryEnum对应的加载状态
     *
     * @return 0表示query没有对应的状态
     */
    public int getHttpTaskLoadState(@NonNull Q query) {
        return mHttpTaskLoadState.get(query.getId());
    }

    /**
     * 获取UserAction对应的加载状态
     *
     * @return 0表示userAction没有对应的状态
     */
    public int getHttpTaskLoadState(@NonNull UA userAction) {
        return mHttpTaskLoadState.get(userAction.getId());
    }

    private SparseIntArray mHttpTaskLoadState = new SparseIntArray(); //对应于CommHttpTask中的状态

    @Override
    public void requestData(final Q query, @Nullable final RequestCallback<Q> callback) {
        KwDebug.mustMainThread();
        if (isQueryActive(query)) {
            return;
        }
        if (!QueryEnumHelper.isQueryValid(mQueries, query)) {//query不在约定的范围内，拒绝执行
            if (null != callback) {
                callback.onError(query);
            }
            return;
        }
        if (TextUtils.isEmpty(query.getUrl())) {//query没有提供url，不在本类职责内，交给子类执行
            processRequest(query, callback);
            return;
        }

        final String url = query.getUrl();
        final byte[] postData = query.getPostData();
        CommHttpTask httpTask = new CommHttpTask();
        httpStrategy(httpTask);
        httpTask.postData(postData);
        final CommHttpTask.TaskHandle taskHandle =
                httpTask.request(url, new CommHttpTask.Callback() {
                    @Override
                    public void callback(int state, String result) {
                        mHttpTaskLoadState.put(query.getId(), state);
                        if (state == CommHttpTask.STATE_SUCCESS) {//只有此状态才有值
                            readDataFromString(query, result);
                        }

                        if (state == CommHttpTask.STATE_ERROR) {
                            if (null != callback) {
                                callback.onError(query);
                            }
                        } else {
                            if (null != callback) {
                                callback.onModelUpdated(ModelWithHttp.this, query);
                            }
                        }

                        mHttpTasks.remove(query.getId());
                    }
                });
        mHttpTasks.put(query.getId(), taskHandle);
    }

    @Override
    public void deliverUserAction(@Nullable final UA userAction, @Nullable Bundle args,
            @Nullable final UserActionCallback<UA> callback) {
        KwDebug.mustMainThread();
        if (null != args && args.containsKey(KEY_TYPE_CANCEL)) {
            cancelAll();
            return;
        }

        if (!UserActionEnumHelper.isUserActionValid(mUserActions,
                userAction)) {//userAction不在约定的范围内，拒绝执行
            if (null != callback) {
                callback.onError(userAction);
            }
            return;
        }

        if (TextUtils.isEmpty(
                userAction.getUrl(getCurrentPage()))) {//userAction没有提供url，不在本类职责内，交给子类执行
            processUserAction(userAction, args, callback);
            return;
        }

        if (isUserActionActive(userAction)) {//当前网络请求正在执行
            return;
        }

        //fixme lzf 目前针对刷新和加载更多的逻辑是，这两个操作互不影响，就是刷新不会自动取消加载更多请求，反之也是
        final boolean isRefresh = isRefreshUserAction(args);
        final boolean isLoadMore = isLoadMoreUserAction(args);
        if (isRefresh && isLoadMore) {//错误状态
            throw new IllegalArgumentException(
                    "[ModelWithHttp] illegalArgumentError deliverUserAction() args both refresh and loadMore !");
        }
        if (isRefresh) {
            mTempPageFighting = true;
            mTempPage = 1;
        } else if (isLoadMore) {
            mTempPageFighting = true;
            mTempPage = mCurrentPage + 1;
        }
        final String url = userAction.getUrl(mTempPage);
        final byte[] postData = userAction.getPostData(mTempPage);
        CommHttpTask httpTask = new CommHttpTask();
        httpStrategy(httpTask);
        httpTask.postData(postData);
        final CommHttpTask.TaskHandle taskHandle =
                httpTask.request(url, new CommHttpTask.Callback() {
                    @Override
                    public void callback(int state, String result) {
                        mHttpTaskLoadState.put(userAction.getId(), state);
                        if (state == CommHttpTask.STATE_SUCCESS) {//只有此状态才有值
                            readDataFromString(userAction, result);
                        }

                        if (state == CommHttpTask.STATE_ERROR) {
                            if (null != callback) {
                                callback.onError(userAction);
                            }
                        } else {
                            if (null != callback) {
                                callback.onModelUpdated(ModelWithHttp.this, userAction);
                            }
                            if (state == CommHttpTask.STATE_SUCCESS) {
                                mCurrentPage++;
                            }
                        }

                        mHttpTasks.remove(userAction.getId());
                    }
                });
        mHttpTasks.put(userAction.getId(), taskHandle);
    }

    private boolean isRefreshUserAction(Bundle args) {
        boolean result = false;
        if (null != args && args.containsKey(KEY_TYPE_REFRESH)) {
            Object obj = args.get(KEY_TYPE_REFRESH);
            if (obj instanceof Boolean) {
                result = args.getBoolean(KEY_TYPE_REFRESH);
            }
            args.remove(KEY_TYPE_REFRESH);
        }
        return result;
    }

    private boolean isLoadMoreUserAction(Bundle args) {
        boolean result = false;
        if (null != args && args.containsKey(KEY_TYPE_LOADMORE)) {
            Object obj = args.get(KEY_TYPE_LOADMORE);
            if (obj instanceof Boolean) {
                result = args.getBoolean(KEY_TYPE_LOADMORE);
            }
            args.remove(KEY_TYPE_LOADMORE);
        }
        return result;
    }

    //网络任务是否正在执行
    private boolean isQueryActive(Q query) {
        if (null == query) {
            return false;
        }
        CommHttpTask.TaskHandle taskHandle = mHttpTasks.get(query.getId());
        return null != taskHandle && taskHandle.isActive();
    }

    //网络任务是否正在执行
    private boolean isUserActionActive(UA userAction) {
        if (null == userAction) {
            return false;
        }
        CommHttpTask.TaskHandle taskHandle = mHttpTasks.get(userAction.getId());
        return null != taskHandle && taskHandle.isActive();
    }

    private int mCurrentPage = 1;//分页页数，从1开始
    private int mTempPage = 1;
    protected boolean mTempPageFighting = false;

    /** @return 加载更多需要的页数，从1开始 */
    private int getCurrentPage() {
        if (mTempPageFighting) {
            return mTempPage;
        } else {
            return mCurrentPage;
        }
    }
}
