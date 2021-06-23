package com.inject.compiler.entity;

import com.inject.annotation.OnTextChanged;

import java.util.List;

import javax.lang.model.element.ExecutableElement;

/**
 * Created time : 2021/6/23 9:13.
 *
 * @author 10585
 */
public final class TextChangeInfo {
    public final List<String> ids;
    public final OnTextChanged.Listen listen;
    public final ExecutableElement methodElement;

    public TextChangeInfo(List<String> ids, OnTextChanged.Listen listen, ExecutableElement methodElement) {
        this.ids = ids;
        this.listen = listen;
        this.methodElement = methodElement;
    }
}