package com.inject.compiler;

import javax.lang.model.type.DeclaredType;

/**
 * Created time : 2021/6/22 12:30.
 *
 * @author 10585
 */
final class IdViewInfo {
    public final String name;
    public final DeclaredType type;

    public IdViewInfo(String name, DeclaredType type) {
        this.name = name;
        this.type = type;
    }
}