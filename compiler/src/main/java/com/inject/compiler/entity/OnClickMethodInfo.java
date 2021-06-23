package com.inject.compiler.entity;

import java.util.List;

import javax.lang.model.element.ExecutableElement;

/**
 * Created time : 2021/6/21 15:51.
 *
 * @author 10585
 */
public final class OnClickMethodInfo {
    public final List<String> ids;
    public final boolean fast;
    public final ExecutableElement methodElement;

    public OnClickMethodInfo(List<String> ids, boolean fast, ExecutableElement methodElement) {
        this.ids = ids;
        this.fast = fast;
        this.methodElement = methodElement;
    }
}