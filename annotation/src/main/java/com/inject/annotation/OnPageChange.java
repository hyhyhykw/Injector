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
public @interface OnPageChange {
    String value();

    enum Listen{
        ON_PAGE_SCROLLED,ON_PAGE_SELECTED,ON_PAGE_SCROLL_STATE_CHANGED
    }


    Listen listen() default Listen.ON_PAGE_SELECTED;
}
