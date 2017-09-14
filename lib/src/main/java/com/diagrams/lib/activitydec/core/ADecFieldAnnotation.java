package com.diagrams.lib.activitydec.core;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 自定义注解，用来标记一个字段是否为“拦截变量”，注意：被标记的属性一定要是Boolean类型，否则报错
 * <p/>
 * Created by lizhaofei on 2017/9/12 15:36
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface ADecFieldAnnotation {
    /** 定义需要拦截哪个方法，这个值是要拦截的方法名，注意：被标记的属性一定要是Boolean类型，否则报错 */
    String interruptMethod();
}
