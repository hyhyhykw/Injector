package com.inject.compiler.entity;

import java.util.List;

import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;

/**
 * Created time : 2021/6/22 9:42.
 *
 * @author 10585
 */
public final class ViewsBindInfo {
    public final List<IdEntity> ids;
    public final ViewsType type;

    public final String eleQualifiedName;
    public final String elePackageName;
    public final String eleClsName;

    public final VariableElement variableElement;
    public final DeclaredType paramsType;

    public ViewsBindInfo(List<IdEntity> ids,
                         ViewsType type,
                         String eleQualifiedName,
                         String elePackageName,
                         String eleClsName,
                         VariableElement variableElement, DeclaredType paramsType) {
        this.ids = ids;
        this.type = type;
        this.eleQualifiedName = eleQualifiedName;
        this.elePackageName = elePackageName;
        this.eleClsName = eleClsName;
        this.variableElement = variableElement;
        this.paramsType = paramsType;
    }
}