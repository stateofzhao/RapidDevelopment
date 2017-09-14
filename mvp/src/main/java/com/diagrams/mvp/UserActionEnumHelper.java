package com.diagrams.mvp;

import android.support.annotation.Nullable;

/**
 * <p/>
 * Created by lizhaofei on 2017/9/6 18:03
 */
public class UserActionEnumHelper {

    public static boolean isUserActionValid(UserActionEnum[] userActionEna,
            UserActionEnum userActionEnum) {
        if (null == userActionEna || null == userActionEnum) {
            return false;
        }
        UserActionEnum ua = findById(userActionEna, userActionEnum.getId());
        return null != ua;
    }

    @Nullable
    public static UserActionEnum findById(UserActionEnum[] userActionEna, int id) {
        if (null == userActionEna) {
            return null;
        }
        for (UserActionEnum userActionEnum : userActionEna) {
            if (userActionEnum.getId() == id) {
                return userActionEnum;
            }
        }
        return null;
    }
}
