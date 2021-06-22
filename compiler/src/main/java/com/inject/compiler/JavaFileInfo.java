package com.inject.compiler;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;

/**
 * Created time : 2021/6/21 15:31.
 *
 * @author 10585
 */
final class JavaFileInfo {
    public final String qualifiedName;
    public final String packageName;
    public final String claName;
    public final TypeElement type;
    public final Map<String, VariableElement> javaFileMap = new HashMap<>();
    public final Set<MethodInfo> onClickMethodMap = new HashSet<>();
    public final Set<ViewsBindInfo> viewsList = new HashSet<>();

    public JavaFileInfo(String qualifiedName,
                        String packageName,
                        String claName,
                        TypeElement type) {
        this.qualifiedName = qualifiedName;
        this.packageName = packageName;
        this.claName = claName;
        this.type = type;
    }
}