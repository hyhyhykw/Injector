package com.inject.compiler.entity;

import com.inject.index.CheckChangeType;

import java.util.List;

import javax.lang.model.element.ExecutableElement;

/**
 * Created time : 2021/6/21 15:51.
 *
 * @author 10585
 */
public final class SingleMethodInfo {
    public final List<IdEntity> ids;
    public final boolean fast;
    public final ExecutableElement methodElement;
    public final CheckChangeType changeType;

    public SingleMethodInfo(List<IdEntity> ids, boolean fast, ExecutableElement methodElement) {
        this.ids = ids;
        this.fast = fast;
        this.methodElement = methodElement;
        changeType = CheckChangeType.Normal;
    }

    public SingleMethodInfo(List<IdEntity> ids, boolean fast, ExecutableElement methodElement, CheckChangeType changeType) {
        this.ids = ids;
        this.fast = fast;
        this.methodElement = methodElement;
        this.changeType = changeType;
    }
}