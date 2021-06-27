package com.inject.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created time : 2021/6/26 12:37.
 * 自动绑定Color资源的注解 支持int color和ColorStateList
 * @author 10585
 * @see android.content.res.ColorStateList
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.FIELD)
public @interface BindColor {
    String value();
}