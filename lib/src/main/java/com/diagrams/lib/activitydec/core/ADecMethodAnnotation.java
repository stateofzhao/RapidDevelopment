package com.diagrams.lib.activitydec.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，来描述是否拦截方法回调
 * <p/>
 * Created by lizhaofei on 2017/9/12 11:33
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)//暂时定义成运行时通过反射获取，会稍微影响性能，之后有时间的定义成 CLASS，然后自定义Processor来处理,http://blog.csdn.net/qq_28195645/article/details/52097626
public @interface ADecMethodAnnotation {
    public boolean interrupt() default false;
}
