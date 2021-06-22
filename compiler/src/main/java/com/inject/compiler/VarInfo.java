package com.inject.compiler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * Created time : 2021/6/21 15:31.
 *
 * @author 10585
 */
final class VarInfo {
    public final String qualifiedName;
    public final String packageName;
    public final String claName;
    public final TypeElement type;
    public final HashMap<String, VariableElement> varMap = new HashMap<>();
    public final Set<MethodInfo> methodMap = new HashSet<>();

    public VarInfo(String qualifiedName,
                   String packageName,
                   String claName,
                   TypeElement type) {
        this.qualifiedName = qualifiedName;
        this.packageName = packageName;
        this.claName = claName;
        this.type = type;
    }
}