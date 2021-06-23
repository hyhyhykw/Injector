package com.inject.compiler.entity;

import java.util.Objects;

/**
 * Created time : 2021/6/21 15:31.
 *
 * @author 10585
 */
public final class CustomInject {
    public final String claName;
    public final String method;
    public String fieldName;
    public String methodName;

    public CustomInject(String claName, String method) {
        this.claName = claName;
        this.method = method;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CustomInject that = (CustomInject) o;
        return Objects.equals(claName, that.claName) &&
                Objects.equals(method, that.method);
    }

    @Override
    public int hashCode() {
        return Objects.hash(claName, method);
    }
}