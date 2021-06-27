package com.inject.compiler.entity;

/**
 * Created time : 2021/6/26 13:47.
 *
 * @author 10585
 */
public final class DpInfo {
    public final String value;
    public final String name;

    public DpInfo(String value, String name) {
        this.value = value;
        this.name = name;
    }

    public boolean isFloat;
    public boolean isPrimitive;
    public String type;
}