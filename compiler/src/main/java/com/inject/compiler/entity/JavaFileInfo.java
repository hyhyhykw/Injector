package com.inject.compiler.entity;

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
public final class JavaFileInfo {
    public final String qualifiedName;
    public final String packageName;
    public final String claName;
    public final TypeElement type;
    public final Map<IdEntity, VariableElement> viewIdMap = new HashMap<>();
    public final Map<IdEntity, VariableElement> animMap = new HashMap<>();
    public final Map<IdEntity, VariableElement> drawableMap = new HashMap<>();
    public final Map<IdEntity, VariableElement> colorMap = new HashMap<>();
    public final Map<IdEntity, VariableElement> stringMap = new HashMap<>();

    public final Set<SingleMethodInfo> onClickMethodMap = new HashSet<>();
    public final Set<SingleMethodInfo> checkedChangedMethodMap = new HashSet<>();
    public final Set<SingleMethodInfo> longClickMethodMap = new HashSet<>();
    public final Set<PageChangeInfo> pageChangeInfo = new HashSet<>();
    public final Set<TextChangeInfo> textChangeInfo = new HashSet<>();
    public final Set<ViewsBindInfo> viewsList = new HashSet<>();
    public final Set<ArrayInfo> arrayInfo = new HashSet<>();

    public final Set<DpInfo> dpInfo = new HashSet<>();
    public final Set<DpInfo> spInfo = new HashSet<>();

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