package com.diagrams.rapidframework.arch;

/**
 * MVP中的Presenter
 * <p/>
 *
 * Presenter设计尽量简单，只有一个方法。这么做的好处就是整个app中只有一个Presenter的实现类，来减少类的数量，
 * 在这个MVP模式中，我们设计了UserActionEnum这个东东，来让Model能够处理UserActionEnum，View能够发出UserActionEnum
 * ，这样Presenter中就没有需要针对View的具体业务逻辑方法了，它仅仅需要传递View发出的UserAction给Model即可。
 * <p/>
 * 它需要的参数为：{@link QueryEnum}（用来执行初始化数据请求）和{@link UserActionEnum}{用来相应View的操作}
 * <p/>
 *
 * Created by Diagrams on 2017/7/22 10:39
 */
public interface Presenter<Q extends QueryEnum, UA extends UserActionEnum> {

  //注意这个方法并没有参数，所以需要经由实现类的构造函数传递过来，给与实现类尽可能多的自由

  /** 进行初始化请求 */
  void loadInitialQueries();
}
