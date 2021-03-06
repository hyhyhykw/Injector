package com.inject.annotation;

import com.inject.index.CheckChangeType;

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
public @interface OnCheckedChanged {
    String[] value();

    CheckChangeType type() default CheckChangeType.CompoundButton;
}