package com.diagrams.mvp.box;

import android.support.annotation.Nullable;
import android.text.TextUtils;
import com.diagrams.lib.util.KwThreadPool;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * 缓存策略：
 * 1. 有缓存时，                        先读取本地缓存，如果有缓存，并且缓存没有过期就直接使用缓存；如果没有缓存则读取网络数据。
 * 这个比较灵活，可以自由的根据情况来自主决定是否缓存结果到本地，是否读取缓存出来等。
 * 2. 无缓存时（NO_CACHE_STRATEGY）     即不会去读缓存，也不会将缓存存到本地
 * <p/>
 * Created by lizhaofei on 2017/9/6 11:02
 */
//也可以理解为业务module，但是比较上层，不需要初始化处理
public class CommHttpTask {
    //默认值
    private static final int TIME_OUT = 8000;//8s

    //加载状态
    public static final int STATE_SUCCESS = 1;
    public static final int STATE_FAILURE = 2;
    public static final int STATE_LOADING = 3;
    public static final int STATE_NET_UNAVAILABLE = 4;
    public static final int STATE_ONLY_WIFI = 5;
    public static final int STATE_ERROR = 6; //这种状态是出现了 【致命的代码错误】
    public static final int STATE_EMPTY = 7;

    private CacheStrategy mCacheSty;
    private LogEventStrategy mLogEventSty;
    private ResultParseStrategy mResultSty;
    private byte[] mPostData;

    private TaskHandle mCurrentTaskHandle;

    //结果回调
    public interface Callback {
        void callback(int state, String result);
    }//Callback end

    //缓存策略
    public interface CacheStrategy {
        boolean isCacheTimeOut(String url);

        String get(String url);

        /** 注意，进行缓存时，需要自己手动判断data是否符合缓存要求，比如有可能是 "TP=none" 此时就不适合缓存 */
        void cache(String url, String data);

        void delete(String url);
    }//CacheStrategy end

    //统计事件策略
    public interface LogEventStrategy {
        void startOutTimeEvent(String url);

        void endOutTimeEvent(String url);

        void removeOutTimeEvent(String url);

        void sendRequestResultLog(@Nullable HttpResult result);
    }//LogEventStrategy end

    //网络结果解析策略
    public interface ResultParseStrategy {
        String parse(byte[] byteData);
    }//ResultParseStrategy end

    /**
     * @param callback 在UI线程中执行
     * @return 用来取消网络执行
     */
    public TaskHandle request(String url, Callback callback) {
        if (null != mCurrentTaskHandle && mCurrentTaskHandle.isActive()) {
            return mCurrentTaskHandle;
        }
        if (null == mCacheSty) {
            mCacheSty = new DefaultCacheStrategy();
        }
        if (null == mLogEventSty) {
            mLogEventSty = new DefaultLogEventStrategy();
        }
        if (null == mResultSty) {
            mResultSty = new DefaultResultParse();
        }
        final HttpThread task = new HttpThread(url, callback);
        if (null != mPostData) {
            task.post(mPostData);
        }
        KwThreadPool.runThread(KwThreadPool.JobType.IMMEDIATELY, task);
        mCurrentTaskHandle = new TaskHandle(task);
        return mCurrentTaskHandle;
    }

    public void postData(byte[] postData) {
        mPostData = postData;
    }

    public void setCacheStrategy(CacheStrategy cacheStrategy) {
        mCacheSty = cacheStrategy;
    }

    public void setLogEventSty(LogEventStrategy logEventSty) {
        mLogEventSty = logEventSty;
    }

    public void setResultParseSty(ResultParseStrategy resultParseSty) {
        mResultSty = resultParseSty;
    }

    private class HttpThread implements Runnable {
        private String url;
        private Callback callback;
        private boolean requestGet = true;
        private byte[] postData;

        private volatile AtomicBoolean alive;
        private boolean isCancel = false;
        private HttpSession workSession;

        private HttpThread(String url, Callback callback) {
            this.url = url;
            this.callback = callback;
            requestGet = true;
            alive = new AtomicBoolean(true);
        }

        private void cancel() {
            isCancel = true;
            if (null != workSession) {
                workSession.cancel();
            }
            alive.set(false);
        }

        private boolean isActive() {
            return alive.get();
        }

        private void post(byte[] data) {
            requestGet = false;
            postData = data;
        }

        @Override
        public void run() {
            String data;
            if (mCacheSty instanceof NO_CACHE_STRATEGY) {//无缓存请求
                if (isNetAvaliable) {
                    if (isOnlyWifiConnect) {//此时联网了，非wifi，但是打开了仅wifi下联网的开关
                        optViewState(null, STATE_ONLY_WIFI);
                    } else {
                        startHttp();
                    }
                } else {
                    optViewState(null, STATE_NET_UNAVAILABLE);
                }
                alive.set(false);
                return;
            }

            if (isNetAvaliable) {
                if (isOnlyWifiConnect) {//此时联网了，非wifi，但是打开了仅wifi下联网的开关
                    data = mCacheSty.get(url);//此时无论是否过期，都将缓存结果传递出去
                    optViewState(data, STATE_ONLY_WIFI);
                } else {
                    boolean isOut = mCacheSty.isCacheTimeOut(url);
                    if (isOut) {
                        mCacheSty.delete(url);
                        startHttp();
                    } else {
                        data = mCacheSty.get(url);
                        optViewState(data, STATE_SUCCESS);
                    }
                }
            } else {
                data = mCacheSty.get(url);//此时无论是否过期，都将缓存结果传递出去
                optViewState(data, STATE_NET_UNAVAILABLE);
            }
            alive.set(false);
        }

        private void startHttp() {
            callbackOnUiThread(null, STATE_LOADING);
            mLogEventSty.startOutTimeEvent(url);
            HttpResult result = null;
            for (int i = 1; i <= 3 && !isCancel; i++) {
                workSession = new HttpSession();
                workSession.setTimeout(TIME_OUT);
                if (3 != i) {// 重试3次，第三次才用tcp代理
                    workSession.setUseTcpProxy(false);
                }
                if (requestGet) {
                    result = workSession.get(url);
                } else {
                    result = workSession.post(url, postData);
                }
                if (null != result && result.isOk()) {//请求成功
                    break;
                }
            }

            if (null != result && result.isOk() && null != result.data) {
                final String resultStr = mResultSty.parse(result.data);
                if (TextUtils.isEmpty(resultStr)) {//解析失败
                    optViewState(null, STATE_FAILURE);
                    mLogEventSty.removeOutTimeEvent(url);
                } else {
                    mCacheSty.cache(url, resultStr);//缓存结果
                    optViewState(resultStr, STATE_SUCCESS);
                    mLogEventSty.endOutTimeEvent(url);
                }
            } else { // 超时也会走这里，可以根据 result.code 来判断到底是哪种类型错误
                optViewState(null, STATE_FAILURE);
                mLogEventSty.removeOutTimeEvent(url);
            }
            mLogEventSty.sendRequestResultLog(result);
        }

        private void optViewState(String data, int state) {
            if (isCancel) {
                return;
            }
            if (TextUtils.isEmpty(data)) {
                callbackOnUiThread(data, state);
            } else {
                //fixme lzf 觉着不应该在这里处理，所以这里都是直接回调了 callbackOnUiThread()方法，没有做任何转换
                if ("TP=noe".equalsIgnoreCase(data)) {
                    // 在歌手专辑列表或者歌手MV列表，认为是没有数据
                    callbackOnUiThread(data, state);
                } else {
                    callbackOnUiThread(data, state);
                }
            }
        }

        private void callbackOnUiThread(final String data, final int state) {
            if (isCancel) {
                return;
            }
            if (null != callback) {
                if (state == STATE_LOADING) {
                    MessageManager.getInstance().asyncRun(new MessageManager.Runner() {
                        @Override
                        public void call() {
                            if (isCancel) {
                                return;
                            }
                            callback.callback(state, data);
                        }
                    });
                } else {
                    MessageManager.getInstance().syncRun(new MessageManager.Runner() {
                        @Override
                        public void call() {
                            if (isCancel) {
                                return;
                            }
                            callback.callback(state, data);
                        }
                    });
                }
            }
        }
    }//HttpThread end

    //任务句柄，用来取消任务
    public static class TaskHandle {
        private HttpThread task;

        private TaskHandle(HttpThread task) {
            this.task = task;
        }

        public void cancel() {
            if (null != task) {
                task.cancel();
                task = null;
            }
        }

        public boolean isActive() {
            return null != task && task.isActive();
        }
    }//TaskHandle end

    //todo lzf 下面三个默认Http策略需要实现
    private static class DefaultCacheStrategy implements CacheStrategy {
        @Override
        public boolean isCacheTimeOut(String url) {
            return true;
        }

        @Override
        public String get(String url) {
            return null;
        }

        @Override
        public void cache(String url, String data) {
        }

        @Override
        public void delete(String url) {
        }
    }//DefaultCacheStrategy end

    private static class DefaultLogEventStrategy implements LogEventStrategy {
        @Override
        public void startOutTimeEvent(String url) {
        }

        @Override
        public void endOutTimeEvent(String url) {
        }

        @Override
        public void removeOutTimeEvent(String url) {
        }

        @Override
        public void sendRequestResultLog(HttpResult result) {
        }
    }//DefaultLogEventStrategy end

    private static class DefaultResultParse implements ResultParseStrategy {
        @Override
        public String parse(byte[] byteData) {
            return null;
        }
    }//DefaultResultParse end

    /** 无缓存策略 */
    public final static class NO_CACHE_STRATEGY implements CacheStrategy {
        @Override
        public boolean isCacheTimeOut(String url) {
            return false;
        }

        @Override
        public String get(String url) {
            return null;
        }

        @Override
        public void cache(String url, String data) {
        }

        @Override
        public void delete(String url) {
        }
    }//NO_CACHE_STRATEGY end

    //todo 需要工具库提供支持
    private boolean isNetAvaliable = true;
    private boolean isOnlyWifiConnect = true;
}
