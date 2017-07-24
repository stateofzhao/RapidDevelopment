package com.diagrams.rapidframework.arch;

/**
 * 查询操作，Model初始化的时候一般会传递一个此类集合进去，这样就能够直接通过此集合一眼看到Model能够请求哪些数据。
 * <p/>
 *
 * Created by Diagrams on 2017/7/22 10:21
 */
public interface QueryEnum {
  int getId();

  /** 要获取的表的字段（列名） */
  String[] getProjection();
}
