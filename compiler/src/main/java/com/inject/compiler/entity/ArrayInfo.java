package com.inject.compiler.entity;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;

/**
 * Created time : 2021/6/22 9:42.
 *
 * @author 10585
 */
public final class ArrayInfo {
    public final String id;
    public final ViewsType type;

    public final String eleQualifiedName;
    public final String elePackageName;
    public final String eleClsName;

    public final VariableElement variableElement;
    public final DeclaredType paramsType;

    public ArrayInfo(String id,
                     ViewsType type,
                     String eleQualifiedName,
                     String elePackageName,
                     String eleClsName,
                     VariableElement variableElement,
                     DeclaredType paramsType) {
        this.id = id;
        this.type = type;
        this.eleQualifiedName = eleQualifiedName;
        this.elePackageName = elePackageName;
        this.eleClsName = eleClsName;
        this.variableElement = variableElement;
        this.paramsType = paramsType;
    }
}