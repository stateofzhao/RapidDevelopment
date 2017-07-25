package com.diagrams.rapidframework.arch;

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Preconditions;
import com.diagrams.rapidframework.util.LogUtils;

/**
 * 工程中唯一的{@link Presenter}实现类，有两个作用：<br/>
 * 1.执行初始化数据请求（职能来自{@link Presenter}定义），并且将结果传递给{@link UpdatableView}。<br/>
 * 2.传递{@link UpdatableView}的{@link UserActionEnum}给Model，并且将
 * Model的执行结果回传给{@link UpdatableView}（通过实现{@link UpdatableView.UserActionListener}来实现）。
 * <p/>
 *
 * Created by Diagrams on 2017/7/25 10:14
 */
public class PresenterImpl implements Presenter, UpdatableView.UserActionListener {

  private static final String TAG = LogUtils.makeLogTag(PresenterImpl.class);

  Model model;
  UpdatableView[] updatableViews;
  QueryEnum[] initQueries;
  UserActionEnum[] userActions;

  public PresenterImpl(@NonNull Model model, @Nullable UpdatableView updatableViews,
      @NonNull QueryEnum[] initQueries, UserActionEnum[] userActions) {
    this(model, new UpdatableView[] { updatableViews }, initQueries, userActions);
  }

  /**
   * @param model 数据来源{@link Model}
   * @param updatableViews 显示Model数据的View
   * @param initQueries 初始化请求的查询列表
   * @param userActions Presenter能够相应的View Action列表，注意一定要传递完全，因为后续View触发的一些列
   * Action都会检测此列表中是否包含Action
   */
  public PresenterImpl(@NonNull Model model, @Nullable UpdatableView[] updatableViews,
      @NonNull QueryEnum[] initQueries, UserActionEnum[] userActions) {
    this.model = model;
    this.updatableViews = updatableViews;
    this.initQueries = initQueries;
    this.userActions = userActions;

    Preconditions.checkNotNull(model);

    //给UpdatableView注册监听事件
    if (null != updatableViews) {
      for (UpdatableView view : updatableViews) {
        view.addUserActionListener(this);
      }
    }
  }

  @Override public void loadInitialQueries() {
    if (null != initQueries && initQueries.length > 0) {
      for (QueryEnum query : initQueries) {
        model.requestData(query, new Model.DataQueryCallback() {
          @Override public void onModelUpdated(Model model, QueryEnum queryEnum) {
            postQuerySuccess(model, queryEnum);
          }

          @Override public void onError(Object error, QueryEnum queryEnum) {
            postQueryError(error, queryEnum);
          }
        });
      }
    } else {
      //没有需要初始化加载的数据，直接通知View更新
      postQuerySuccess(model, null);
    }
  }

  @Override public void onUserAction(UserActionEnum userActionEnum, @Nullable Bundle args) {
    Preconditions.checkNotNull(userActionEnum);
    if (isActionValid(userActionEnum)) {
      model.deliverUserAction(userActionEnum, args, new Model.UserActionCallback() {
        @Override public void onModelUpdated(Model model, UserActionEnum userActionEnum) {
          postActionResult(userActionEnum, model, true);
        }

        @Override public void onError(Object error, UserActionEnum userActionEnum) {
          if (null != error) {
            LogUtils.LOGE(TAG, error.toString());
          }
          postActionResult(userActionEnum, null, false);
        }
      });
    } else {
      //无效的操作Action
      LogUtils.LOGE(TAG, "userAction [" + userActionEnum.getId() + "] is not list in UserActions");
      postActionResult(userActionEnum, null, false);
    }
  }

  private boolean isActionValid(UserActionEnum userAction) {
    boolean isValid = false;
    if (null != userActions) {
      for (UserActionEnum userActionEnum : userActions) {
        if (userAction.getId() == userActionEnum.getId()) {
          isValid = true;
          break;
        }
      }
    }
    return isValid;
  }

  private void postQuerySuccess(Model model, QueryEnum query) {
    if (null != updatableViews) {
      for (UpdatableView view : updatableViews) {
        view.displayData(query, model);
      }
    } else {
      LogUtils.LOGE(TAG, "loadInitialQueries(), cannot notify a null view!");
    }
  }

  private void postQueryError(Object error, QueryEnum queryEnum) {
    if (null != updatableViews) {
      for (UpdatableView view : updatableViews) {
        view.displayDataError(queryEnum, error);
      }
    } else {
      LogUtils.LOGE(TAG, "loadInitialQueries(), cannot notify a null view!");
    }
  }

  private void postActionResult(UserActionEnum userActionEnum, Model model, boolean success) {
    if (null != updatableViews) {
      for (UpdatableView view : updatableViews) {
        view.displayUserActionResult(userActionEnum, model, success);
      }
    } else {
      LogUtils.LOGE(TAG, "onUserAction(), cannot notify a null view!");
    }
  }
}
