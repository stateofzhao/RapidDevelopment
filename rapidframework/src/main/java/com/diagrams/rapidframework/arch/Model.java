package com.diagrams.rapidframework.arch;

import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * MPV中的Model
 * <p/>
 * 通常来说所有的页面，都会有一个初始化的数据请求，所以Model中设计有次方法，之后的请求可以认为都是View
 * 根据用户操作触发的UserAction请求，所以可以这样来设计Model，只响应两种请求：1.初始化数据请求；2.UserAction触发的请求。
 * 这样Model接口就尽可能简单了，并且实现类也没有过多的约束，并且在callback接口中将Model本体给传递回去了
 * 这样监听类就能够尽可能多的拿到自己想要的数据了。
 * <p/>
 * 它需要的参数为：1.{@link QueryEnum}用来处理数据请求；2.{@link UserActionEnum}用来相应Presenter
 * 传递过来的用户操作。
 *
 * Created by Diagrams on 2017/7/22 10:21
 */
public interface Model<Q extends QueryEnum, UA extends UserActionEnum> {

  Q[] getQueries();

  UA[] getUserActions();

  void requestData(Q queryEnum, DataQueryCallback callback);

  /**
   * 传递用户行为，来执行相应的数据请求 ，此方法不是由View中触发的，而是经由Presenter来调用的。
   *
   * @param userActionEnum 用户操作类型，通过一个id来唯一标识
   * @param args 用户操作有可能会触发Model对数据的请求，但是UserActionEnum中有没有QueryEnum，所以此时
   * 就需要
   */
  void deliverUserAction(UA userActionEnum, @Nullable Bundle args, UserActionCallback callback);

  /** 清理Model */
  void cleanUp();

  /** 数据请求回调接口 ，结果回掉接口 直接使用{@link Model}来传递，这样获取数据直接调用相应的Model中的方法即可 */
  public interface DataQueryCallback<M extends Model, E, Q extends QueryEnum> {
    void onModelUpdated(M model, Q queryEnum);

    void onError(E error, Q queryEnum);
  }

  public interface UserActionCallback<M extends Model, E, UA extends UserActionEnum> {
    void onModelUpdated(M model, UA userActionEnum);

    void onError(E error, UA userActionEnum);
  }
}
