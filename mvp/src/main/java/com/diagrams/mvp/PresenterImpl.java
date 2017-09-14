package com.diagrams.mvp;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

/**
 * 整个工程只有一个Presenter，减少类的数量
 * <p/>
 * Created by lizhaofei on 2017/9/5 17:50
 */
public class PresenterImpl<M extends IModel<Q, UA>, Q extends QueryEnum, UA extends UserActionEnum>
        implements IPresenter, IView.UserActionListener<UA> {
    private M mModel;
    private IView<M, Q, UA>[] mViews;
    private Q[] mInitQueries;
    private UA[] mValidActions;

    public PresenterImpl(@NonNull M model, @Nullable IView<M, Q, UA> view,
            @Nullable Q[] initQueries, @Nullable UA[] validActions) {
        this(model, new IView[] { view }, initQueries, validActions);
    }

    public PresenterImpl(@NonNull M model, @Nullable IView<M, Q, UA>[] views,
            @Nullable Q[] initQueries, @Nullable UA[] validActions) {
        mModel = model;
        mInitQueries = initQueries;
        mValidActions = validActions;
        if (null != views) {
            mViews = views;
            for (IView<M, Q, UA> view : mViews) {
                if (null != view) {
                    view.addUserActionListener(this);
                }
            }
        }
    }

    @Override
    public void initLoad() {
        if (null == mInitQueries) {
            return;
        }
        for (Q queryEnum : mInitQueries) {
            mModel.requestData(queryEnum, new IModel.RequestCallback<Q>() {
                @Override
                public void onModelUpdated(IModel<Q, ?> model, Q query) {
                    if (null != mViews) {
                        for (IView<M, Q, UA> view : mViews) {
                            view.displayData(mModel, query);
                        }
                    } else {

                    }
                }

                @Override
                public void onError(Q query) {
                    if (null != mViews) {
                        for (IView<M, Q, UA> view : mViews) {
                            view.displayErrorMsg(query);
                        }
                    } else {

                    }
                }
            });
        }
    }

    @Override
    public void onUserAction(UA userAction, @Nullable Bundle args) {
        boolean isActionValid = false;
        if (null != mValidActions && null != userAction) {
            for (UA actionEnum : mValidActions) {
                if (actionEnum.getId() == userAction.getId()) {
                    isActionValid = true;
                    break;
                }
            }
        }
        if (null == userAction) {//此时放行，因为有可能会 利用args来执行一些通用操作
            isActionValid = true;
        }
        if (isActionValid) {
            mModel.deliverUserAction(userAction, args, new IModel.UserActionCallback<UA>() {
                @Override
                public void onModelUpdated(IModel<?, UA> model, UA userAction) {
                    if (null != mViews) {
                        for (IView<M, Q, UA> view : mViews) {
                            view.displayUserActionResult(mModel, userAction, true);
                        }
                    } else {

                    }
                }

                @Override
                public void onError(UA userAction) {
                    if (null != mViews) {
                        for (IView<M, Q, UA> view : mViews) {
                            view.displayUserActionResult(null, userAction, false);
                        }
                    } else {

                    }
                }
            });
        } else {
            throw new RuntimeException("invalid userAction : [" + userAction.getId() + "]");
        }
    }

    public M getModel() {
        return mModel;
    }
}
