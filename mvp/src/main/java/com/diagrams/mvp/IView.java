package com.diagrams.mvp;

import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * View
 * <p/>
 * Created by lizhaofei on 2017/9/5 17:35
 */
public interface IView<M extends IModel<Q, UA>, Q extends QueryEnum, UA extends UserActionEnum> {

    //注册相应用户操作的坚挺接口
    void addUserActionListener(UserActionListener<UA> listener);

    //显示数据，一般对应于Presenter中的initRequest()
    void displayData(M model, Q query);

    //显示请求失败的信息
    void displayErrorMsg(Q query);

    //显示用户操作结果
    void displayUserActionResult(M model, UA userAction, boolean success);

    //用户操作回调接口
    public interface UserActionListener<UA extends UserActionEnum> {
        void onUserAction(UA userAction, @Nullable Bundle args);
    }
}
