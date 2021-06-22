package com.inject.compiler;

import java.util.List;

import javax.lang.model.element.ExecutableElement;

/**
 * Created time : 2021/6/21 15:51.
 *
 * @author 10585
 */
final class MethodInfo {
    public final List<String> ids;
    public final boolean fast;
    public final ExecutableElement methodElement;

    public MethodInfo(List<String> ids, boolean fast, ExecutableElement methodElement) {
        this.ids = ids;
        this.fast = fast;
        this.methodElement = methodElement;
    }
}