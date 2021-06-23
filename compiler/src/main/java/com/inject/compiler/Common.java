package com.inject.compiler;

import com.squareup.javapoet.ClassName;

import java.util.regex.Pattern;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * Created time : 2021/6/23 8:45.
 *
 * @author 10585
 */
public final class Common {
    public static final String REGEX_LIST_VIEWS = "java\\.util\\.List<(.+?)>";
    public static final String REGEX_COLLECTION_VIEWS = "java\\.util\\.Collection<(.+?)>";
    public static final String REGEX_ARRAYLIST_VIEWS = "java\\.util\\.ArrayList<(.+?)>";

    public static final String REGEX_LINKED_VIEWS = "java\\.util\\.LinkedList<(.+?)>";

    public static final String REGEX_ARRAY_VIEWS = "(.+?)\\[]";

    //Pattern
    public static final Pattern patterListViews = Pattern.compile(REGEX_LIST_VIEWS);
    public static final Pattern patterCollectionViews = Pattern.compile(REGEX_COLLECTION_VIEWS);
    public static final Pattern patterArrayListViews = Pattern.compile(REGEX_ARRAYLIST_VIEWS);

    public static final Pattern patterLinkedListViews = Pattern.compile(REGEX_LINKED_VIEWS);

    public static final Pattern patterArrayViews = Pattern.compile(REGEX_ARRAY_VIEWS);

    //根据类名和包名获取类 可能包含内部类所以需要处理
    public static ClassName getByFullName(String fullName, String packageName) {
        String substring = fullName.replace(packageName, "")
                .substring(1);

        String[] split = substring.split("\\.");
        ClassName instance;
        if (split.length > 1) {
            String[] strings = new String[split.length - 1];

            for (int i = 0; i < split.length; i++) {
                if (i == 0) continue;
                String s = split[i];
                strings[i - 1] = s;
            }

            instance = ClassName.get(packageName, split[0], strings);
        } else {
            instance = ClassName.get(packageName, substring);
        }

        return instance;
    }

    //第一个参数是不是第二个参数的父类
    public static boolean isNotNeedCast(String qualifiedName, DeclaredType viewType) {
        TypeElement type = (TypeElement) viewType.asElement();
        if (qualifiedName.equals(type.getQualifiedName().toString())) {
            return true;
        }
        TypeMirror superclass = type.getSuperclass();
        TypeKind kind = superclass.getKind();
        while (kind == TypeKind.DECLARED) {
            DeclaredType declaredType = (DeclaredType) superclass;
            TypeElement element = (TypeElement) declaredType.asElement();
            String className = element.getQualifiedName().toString();
            if (qualifiedName.equals(className)) return true;

            superclass = element.getSuperclass();
            kind = superclass.getKind();
        }

        return false;
    }

    public static boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }
}