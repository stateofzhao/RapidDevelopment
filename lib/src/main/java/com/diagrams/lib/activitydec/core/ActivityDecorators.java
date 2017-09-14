package com.diagrams.lib.activitydec.core;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Iterator;

/**
 * 管理所有装饰者的装饰者
 * <p/>
 * Created by lizhaofei on 2017/9/12 10:32
 */
public class ActivityDecorators implements IActivityDecorator {

    private ArrayList<IActivityDecorator> decorators = new ArrayList<>();
    private Activity host;

    public ActivityDecorators(@NonNull Activity host) {
        host(host);
    }

    public void add(IActivityDecorator activityDecorator) {
        activityDecorator.host(host);
        decorators.add(activityDecorator);
    }

    public void remove(Class<IActivityDecorator> decoratorClass) {
        remove(decoratorClass.getSimpleName());
    }

    public void remove(String decoratorClassSimpleName) {
        Iterator<IActivityDecorator> iterator = decorators.iterator();
        while (iterator.hasNext()) {
            IActivityDecorator value = iterator.next();
            if (value.getClass().getSimpleName().equals(decoratorClassSimpleName)) {
                iterator.remove();
                break;
            }
        }
    }

    @Override
    public void host(Activity activity) {
        host = activity;
    }

    @Override
    public void onPreCreate(Bundle bundle) {
        for (IActivityDecorator activity : decorators) {
            activity.onPreCreate(bundle);
            if (isMethodInterrupt(activity, "onPreCreate")) {//检测方法拦截 注解
                return;
            }
            if(isMethodInterruptByFiled(activity,"onPreCreate")){//检测属性拦截 注解
                return;
            }
        }
    }

    @Override
    public void onCreate(Bundle bundle) {
        for (IActivityDecorator activity : decorators) {
            activity.onCreate(bundle);
            if (isMethodInterrupt(activity, "onCreate")) {//检测方法拦截 注解
                return;
            }
            if(isMethodInterruptByFiled(activity,"onCreate")){//检测属性拦截 注解
                return;
            }
        }
    }

    @Override
    public void onPostCreate(Bundle bundle) {
        for (IActivityDecorator activity : decorators) {
            activity.onPostCreate(bundle);
            if (isMethodInterrupt(activity, "onPostCreate")) {//检测方法拦截 注解
                return;
            }
            if(isMethodInterruptByFiled(activity,"onPostCreate")){//检测属性拦截 注解
                return;
            }
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        for (IActivityDecorator activity : decorators) {
            activity.onActivityResult(requestCode, resultCode, data);
            if (isMethodInterrupt(activity, "onActivityResult")) {//检测方法拦截 注解
                return;
            }
            if(isMethodInterruptByFiled(activity,"onActivityResult")){//检测属性拦截 注解
                return;
            }
        }
    }

    @Override
    public void onStart() {
        for (IActivityDecorator activity : decorators) {
            activity.onStart();
            if (isMethodInterrupt(activity, "onStart")) {//检测方法拦截 注解
                return;
            }
            if(isMethodInterruptByFiled(activity,"onStart")){//检测属性拦截 注解
                return;
            }
        }
    }

    @Override
    public void onResume() {
        for (IActivityDecorator activity : decorators) {
            activity.onResume();
            if (isMethodInterrupt(activity, "onResume")) {//检测方法拦截 注解
                return;
            }
            if(isMethodInterruptByFiled(activity,"onResume")){//检测属性拦截 注解
                return;
            }
        }
    }

    @Override
    public void onPause() {
        for (IActivityDecorator activity : decorators) {
            activity.onPause();
            if (isMethodInterrupt(activity, "onPause")) {//检测方法拦截 注解
                return;
            }
            if(isMethodInterruptByFiled(activity,"onPause")){//检测属性拦截 注解
                return;
            }
        }
    }

    @Override
    public void onNewIntent(Intent intent) {
        for (IActivityDecorator activity : decorators) {
            activity.onNewIntent(intent);
            if (isMethodInterrupt(activity, "onNewIntent")) {//检测方法拦截 注解
                return;
            }
            if(isMethodInterruptByFiled(activity,"onNewIntent")){//检测属性拦截 注解
                return;
            }
        }
    }

    //解析方法上的注解
    private boolean isMethodInterrupt(IActivityDecorator activityDecorator, String methodName) {
        return isMethodInterrupt(activityDecorator.getClass().getSimpleName(), methodName);
    }

    //解析方法上的注解，由于是“配置型标注”所以在类中就已经确定值了所以这里只要类名即可
    private boolean isMethodInterrupt(String className, String methodName) {
        try {
            Class c = Class.forName(className);
            Method[] methods = c.getMethods();
            if (null == methods) {
                return false;
            }
            for (Method method : methods) {
                if (!method.getName().equals(methodName)) {
                    continue;
                }
                Annotation[] as = method.getAnnotations();
                if (null == as) {
                    continue;
                }
                for (Annotation annotation : as) {
                    if (annotation instanceof ADecMethodAnnotation) {
                        ADecMethodAnnotation ADecMethodAnnotation =
                                (ADecMethodAnnotation) annotation;
                        return ADecMethodAnnotation.interrupt();
                    }
                }
            }
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
        return false;
    }

    //解析类属性上的注解，这里是解析具体对象中 注解标注的filed值
    //methodName 要确定是否拦截的方法名
    private boolean isMethodInterruptByFiled(IActivityDecorator activityDecorator,
            @NonNull String methodName) {
        Class c = activityDecorator.getClass();
        Field[] fields = c.getDeclaredFields();//获取类的所有属性
        if (null == fields) {
            return false;
        }
        for (Field field : fields) {
            Annotation[] as = field.getAnnotations();//获取属性上声明的注解
            if (null == as) {
                continue;
            }
            for (Annotation annotation : as) {
                if (annotation instanceof ADecFieldAnnotation) {//是我们自己定义的注解
                    ADecFieldAnnotation aDecFieldAnnotation = (ADecFieldAnnotation) annotation;
                    String aDecAnnotationMethodName = aDecFieldAnnotation.interruptMethod();//获取注解值
                    Class<?> type = field.getType();
                    if (Boolean.class.isAssignableFrom(type)) {//是Boolean类型
                        if (methodName.equals(aDecAnnotationMethodName)) {//要判断的方法名 和 注解标注的名称 一样
                            //获取属性值
                            field.setAccessible(true);
                            try {
                                return field.getBoolean(activityDecorator);//获取具体对象上此属性的值
                            } catch (IllegalAccessException e) {
                                e.printStackTrace();
                            }
                        }
                    } else {
                        throw new RuntimeException(
                                "ADecFieldAnnotation must used for Boolean filed!!");
                    }
                }
            }
        }
        return false;
    }
}
