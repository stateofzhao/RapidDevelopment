package com.diagrams.mvp;

/**
 * <p/>
 * Created by lizhaofei on 2017/9/5 19:07
 */
public class QueryEnumHelper {

    public static boolean isQueryValid(QueryEnum[] queryEna, QueryEnum queryEnum) {
        if (null == queryEna || null == queryEnum) {
            return false;
        }
        for (QueryEnum query : queryEna) {
            if (query.getId() == queryEnum.getId()) {
                return true;
            }
        }
        return false;
    }
}
