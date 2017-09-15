package com.diagrams.mvp.box;

/**
 * 在ModelWithHttp中使用的 请求网络的任务接口
 * <p/>
 * Created by lizhaofei on 2017/9/15 11:22
 */
public interface IHttpTask<T> {
    int STATE_SUCCESS = 1;
    int STATE_TIME_OUT = 2;
    int STATE_LOADING = 3;
    int STATE_SERVER_ERROR = 4;//服务器错误
    int STATE_ERROR = 6; //这种状态是出现了 【客户端致命的代码错误】

    public interface Callback<T> {
        void onCallback(int state, T result);
    }

    void request(Callback<T> callback);

    void cancel();

    boolean isActive();
}
