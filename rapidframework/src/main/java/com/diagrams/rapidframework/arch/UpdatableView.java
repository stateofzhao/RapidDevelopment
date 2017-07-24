package com.diagrams.rapidframework.arch;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;

/**
 * MVP中的View
 * <p/>
 * 这个不单单是展示的View，并且还赋予了其发送行为的能力。这样做的好处是能够让实现类尽可能的自由，不必每个页面来写一个View接口，
 * 这样做也解放了Presenter的设计，也不必每个页面都需要对应一个Presenter接口，大大降低了样板代码的书写，做到了
 * 整个app中只需要写一次MVP接口就OK了~
 * <p/>
 *
 *
 * Created by Diagrams on 2017/7/22 10:44
 */
public interface UpdatableView<M, Q extends QueryEnum, UA extends UserActionEnum> {
  void displayData(Q queryEnum, M model);

  void displayDataError(Q queryEnum, M model);

  //注意方法命名方式：xxxResult()中直接包含一个参数告诉是否成功
  void displayUserActionResult(UA userActionEnum, M model, boolean success);

  /** 注册UserActionListener，这样就能够让关心的Presenter触发Model处理UserAction的方法 */
  void addUserActionListener(UserActionListener listener);

  Context getContext();

  public interface UserActionListener<UA extends UserActionEnum> {
    void onUserAction(UA userActionEnum, @Nullable Bundle args);
  }
}
