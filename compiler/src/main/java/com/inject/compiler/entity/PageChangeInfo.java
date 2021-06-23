package com.inject.compiler.entity;

import com.inject.annotation.OnPageChange;

import javax.lang.model.element.ExecutableElement;

/**
 * Created time : 2021/6/23 9:13.
 *
 * @author 10585
 */
public final class PageChangeInfo {
    public final String id;
    public final OnPageChange.Listen listen;
    public final ExecutableElement methodElement;

    public PageChangeInfo(String id, OnPageChange.Listen listen, ExecutableElement methodElement) {
        this.id = id;
        this.listen = listen;
        this.methodElement = methodElement;
    }
}