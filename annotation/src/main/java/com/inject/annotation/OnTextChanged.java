package com.inject.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Created time : 2021/6/22 14:09.
 *
 * @author 10585
 */
@Retention(RetentionPolicy.SOURCE)
@Target(ElementType.METHOD)
public @interface OnTextChanged {


    enum Listen {
        BEFORE_TEXT_CHANGE,
        ON_TEXT_CHANGE,
        AFTER_TEXT_CHANGE
    }

    String[] value();

    Listen listen() default Listen.ON_TEXT_CHANGE;
}
