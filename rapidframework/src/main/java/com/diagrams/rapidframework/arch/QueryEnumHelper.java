package com.diagrams.rapidframework.arch;

import android.support.annotation.NonNull;
import android.support.v4.util.Preconditions;

/**
 * 提供静态帮助方法来解析{@link QueryEnum}
 * <p/>
 *
 * Created by Diagrams on 2017/7/22 16:06
 */
public class QueryEnumHelper {

  public static QueryEnum getQueryForId(int id, @NonNull QueryEnum[] queryEna) {
    Preconditions.checkNotNull(queryEna);
    QueryEnum queryEnum = null;
    for (QueryEnum query : queryEna) {
      if (null != query && query.getId() == id) {
        queryEnum = query;
      }
    }
    return queryEnum;
  }
}
