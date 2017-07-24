package com.diagrams.rapidframework.arch;

/**
 * 只需要区分Action就行，故只有一个返回id的方法（一个页面差不多触发十几种用户行为就是多的了）。
 *
 * Created by Diagrams on 2017/7/22 10:22
 */
public interface UserActionEnum {
  int getId();
}
