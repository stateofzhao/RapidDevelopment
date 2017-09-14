package com.diagrams.mvp;

import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * model 接口
 * <p/>
 * Created by lizhaofei on 2017/9/5 17:24
 */
public interface IModel<Q extends QueryEnum, UA extends UserActionEnum> {

    Q[] getQueries(); //限定的请求集合

    UA[] getUserActions();//限定的用户操作集合

    void requestData(Q query, @Nullable RequestCallback<Q> callback);

    void deliverUserAction(UA userAction, @Nullable Bundle args,
            @Nullable UserActionCallback<UA> callback);

    public interface RequestCallback<Q extends QueryEnum> {
        void onModelUpdated(IModel<Q, ?> model, Q query);//model参数可以去掉

        void onError(Q query);
    }

    public interface UserActionCallback<UA extends UserActionEnum> {
        void onModelUpdated(IModel<?, UA> model, UA userAction);//model参数可以去掉

        void onError(UA userAction);
    }
}
