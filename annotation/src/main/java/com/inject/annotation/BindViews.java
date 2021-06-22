package com.inject.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created time : 2021/6/22 8:48.
 * 绑定多个view并且组成集合或者数组
 * 支持的类型 1、有序数组 2、集合
 *
 * @author 10585
 * 一般绑定的数据都是有序的，所以不能使用无序的数组 {@link java.util.Set}
 * @see java.util.Collection 集合类型
 * @see java.util.List  集合类型
 * @see java.util.ArrayList 集合类型
 * @see java.util.LinkedList 集合类型
 * @see java.lang.reflect.Array View数组类型
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface BindViews {

    String[] value();
}