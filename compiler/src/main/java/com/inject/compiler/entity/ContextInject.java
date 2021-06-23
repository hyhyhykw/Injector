package com.inject.compiler.entity;

/**
 * Created time : 2021/6/22 14:34.
 *
 * @author 10585
 */
public final class ContextInject {

    public final String claName;
    public final String method;
    public final String field;

    public ContextInject(String claName, String method, String field) {
        this.claName = claName;
        this.method = method;
        this.field = field;
    }
}