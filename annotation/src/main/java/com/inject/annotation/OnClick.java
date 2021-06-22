package com.inject.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created time : 2021/6/21 15:28.
 *
 * @author 10585
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface OnClick {
    String[] value();

    /**
     * 是否防止快速点击
     * @return 默认true
     */
    boolean fast() default true;
}