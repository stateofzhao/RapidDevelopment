package com.diagrams.rapidframework.toolbox;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.util.Preconditions;
import com.android.volley.RequestQueue;
import com.android.volley.toolbox.OkHttpStack;
import com.android.volley.toolbox.Volley;
import com.diagrams.rapidframework.arch.Model;
import com.diagrams.rapidframework.arch.QueryEnum;
import com.diagrams.rapidframework.arch.QueryEnumHelper;
import com.diagrams.rapidframework.arch.UserActionEnum;

/**
 * 能够进行网络加载的Model，此Model中内置两种QueryEnum（{@link InternalEnum}），分别对应 刷新和加载更多 两种数据请求。
 * 继承此类的Model，会被添加这两种 数据请求行为。
 * <p/>
 *
 * Created by Diagrams on 2017/7/24 15:01
 */
public abstract class ModelWithNet<Q extends QueryEnum, UA extends UserActionEnum>
    implements Model<Q, UA> {
  private static RequestQueue requestQueue;

  /**
   * 通过{@link Model#deliverUserAction(UserActionEnum, Bundle, UserActionCallback)}来触发的数据请求，需要将{@link
   * QueryEnum}存放到Bundle中（必须传递QueryEnum的id即Integer类型数据），这样就能触发网络请求了（当然必须首先在queries中定义好了）。
   */
  public static final String KEY_ACTION_QUERY_ID = "KEY_ACTION_QUERY_ID";

  Q[] queries;
  UA[] userActions;

  /** 加载更多页数标记 */
  private int page = 0;
  private int tempPage = 0;
  /**
   * 没有处理手动取消 刷新和加载更多 请求时复位此标识，如果子类中加入 取消 刷新和加载更多 请求的action，
   * 那么应该置此标识为false
   */
  protected boolean tempPageFighting = false;

  public ModelWithNet(Context context, Q[] queries, UA[] userActions) {
    this.queries = queries;
    this.userActions = userActions;
    if (null == requestQueue) {
      requestQueue = Volley.newRequestQueue(context, new OkHttpStack());
    }
  }

  /** 执行网络数据加载 ，建议将网络请求的取消标识设置为 queryEnum */
  public abstract void loadDataFromNet(Q queryEnum, DataQueryCallback callback);

  public abstract void processUserAction(UA userActionEnum, Bundle args,
      UserActionCallback callback);

  /** @return 加载更多需要的页数，从0开始 */
  public int getPage() {
    if (tempPageFighting) {
      return tempPage;
    } else {
      return page;
    }
  }

  @Override public Q[] getQueries() {
    return queries;
  }

  @Override public UA[] getUserActions() {
    return userActions;
  }

  @Override public void requestData(@NonNull Q queryEnum, @NonNull DataQueryCallback callback) {
    Preconditions.checkNotNull(queryEnum);
    Preconditions.checkNotNull(callback);
    if (null != QueryEnumHelper.getQueryForId(queryEnum.getId(), getQueries())) {
      loadDataFromNet(queryEnum, callback);
    } else {
      callback.onError("queryEnum is Invalid : " + queryEnum.getId(), queryEnum);
    }
  }

  @Override public void deliverUserAction(final UA userActionEnum, @Nullable Bundle args,
      @NonNull final UserActionCallback callback) {
    Preconditions.checkNotNull(callback);
    if (null != args && args.containsKey(KEY_ACTION_QUERY_ID)) {
      Object obForQuery = args.get(KEY_ACTION_QUERY_ID);
      if (obForQuery instanceof Integer) {
        Integer queryId = (Integer) obForQuery;
        final Q query = (Q) QueryEnumHelper.getQueryForId(queryId, getQueries());

        if (null != query) {//执行数请求
          requestQueue.cancelAll(query);//先取消之前的请求

          //处理 page
          if (isRefreshQuery(query)) {
            //fixme 此处是否应该取消加载更多的请求
            tempPageFighting = true;
            tempPage = 0;
          } else if (isLoadMoreQuery(query)) {
            //fixme 此处是否应该取消刷新的请求
            tempPageFighting = true;
            tempPage = page;
            tempPage++;
          }

          //适配器模式，将UserActionCallback适配成DataQueryCallback
          DataQueryCallback dataQueryCallback = new DataQueryCallback() {
            @Override public void onModelUpdated(Model model, QueryEnum queryEnum) {
              tempPageFighting = false;
              page = tempPage;//真正改变page

              callback.onModelUpdated(model, userActionEnum);
            }

            @Override public void onError(Object error, QueryEnum queryEnum) {
              //加载失败，需要将page还原
              tempPageFighting = false;

              callback.onError(error, userActionEnum);
            }
          };
          loadDataFromNet(query, dataQueryCallback);
        } else {//给出的请求id不存在于本Model支持列表中
          callback.onError("onUserAction called with a bundle containing KEY_ACTION_QUERY but"
              + "the value is not a valid query id!", userActionEnum);
        }
      } else {
        callback.onError("onUserAction called with a bundle containing KEY_ACTION_QUERY but"
            + "the value is not a valid query id!", userActionEnum);
      }
    } else {//执行其它非 数据请求的操作，下放到实现类中来自定义
      processUserAction(userActionEnum, args, callback);
    }
  }

  @Override public void cleanUp() {
    //取消所有请求
    for (Q queryEnum : queries) {
      requestQueue.cancelAll(queryEnum);
    }
  }

  private boolean isRefreshQuery(Q query) {
    return query.getId() == InternalEnum.REFRESH.getId();
  }

  private boolean isLoadMoreQuery(Q query) {
    return query.getId() == InternalEnum.LOADMORE.getId();
  }

  public enum InternalEnum implements QueryEnum {

    /** 刷新请求 */
    REFRESH(-1, null),

    /** 加载更多请求 */
    LOADMORE(-2, null);

    private int id;
    private String[] projection;

    InternalEnum(int id, String[] projection) {
      this.id = id;
      this.projection = projection;
    }

    @Override public int getId() {
      return id;
    }

    @Override public String[] getProjection() {
      return projection;
    }
  }
}
