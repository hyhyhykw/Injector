package com.inject.compiler.binder;

import com.inject.annotation.OnClick;
import com.inject.compiler.entity.CustomInject;
import com.inject.compiler.entity.IdViewInfo;
import com.inject.compiler.entity.JavaFileInfo;
import com.inject.compiler.entity.OnClickMethodInfo;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import static com.inject.compiler.Common.isEmpty;

/**
 * Created time : 2021/6/23 8:40.
 * 获取OnClick注解的所有信息
 * 创建OnClick代码
 *
 * @author 10585
 * @see OnClick
 */
public final class OnClickBinder {

    //获取OnClick注解的所有信息
    public static void parseAnnotation(RoundEnvironment roundEnv, Elements elementUtils, Map<String, JavaFileInfo> specs) {
        Set<? extends Element> onClicks = roundEnv.getElementsAnnotatedWith(OnClick.class);
        for (Element element : onClicks) {
            OnClick onClick = element.getAnnotation(OnClick.class);
            String[] values = onClick.value();
            boolean fast = onClick.fast();

            ExecutableElement executableElement = (ExecutableElement) element;
            TypeElement typeElement = (TypeElement) element.getEnclosingElement();

            /*类的绝对路径 全类名*/
            String qualifiedName = typeElement.getQualifiedName().toString();

            /*类名*/
            String clsName = typeElement.getSimpleName().toString();
            /*获取包名*/
            String packageName = elementUtils
                    .getPackageOf(typeElement).getQualifiedName().toString();

            List<String> ids = new ArrayList<>();
            for (String value : values) {
                String[] split = value.split("\\.");
                String id = split[split.length - 1];
                ids.add(id);
            }

            JavaFileInfo javaFileInfo = specs.get(qualifiedName);
            if (javaFileInfo == null) {
                javaFileInfo = new JavaFileInfo(qualifiedName, packageName, clsName, typeElement);
                specs.put(qualifiedName, javaFileInfo);
            }

            javaFileInfo.onClickMethodMap.add(new OnClickMethodInfo(ids, fast, executableElement));
        }
    }

    //创建OnClick代码
    public static void createCode(ClassName rCla, CustomInject custom, CodeBlock.Builder injectBuilder, Set<OnClickMethodInfo> methodMap, ClassName viewClick, HashMap<String, IdViewInfo> viewsMap) {
        if (!methodMap.isEmpty()) {
            injectBuilder.add("/**\n * generate code by annotation OnClick {@link com.inject.annotation.OnClick}\n */\n");
        }

        for (OnClickMethodInfo info : methodMap) {
            List<String> ids = info.ids;
            ExecutableElement methodElement = info.methodElement;
            String methodName = methodElement.getSimpleName().toString();

            for (String viewId : ids) {
                IdViewInfo idViewInfo = viewsMap.get(viewId);

                if (idViewInfo != null) {
                    injectBuilder.addStatement("$T.setViewClick($N, $L, instance::$N)",
                            viewClick, idViewInfo.name, info.fast, methodName);
                } else {
                    if (custom != null) {
                        if (isEmpty(custom.fieldName) && isEmpty(custom.methodName)) {
                            injectBuilder.addStatement("$T.setViewClick(instance.$L($T.id.$L), $L, instance::$N)",
                                    viewClick, custom.method, rCla, viewId, info.fast, methodName);
                        } else {
                            injectBuilder.addStatement("$T.setViewClick(view.findViewById($T.id.$L), $L, instance::$N)",
                                    viewClick, rCla, viewId, info.fast, methodName);
                        }
                    } else {
                        injectBuilder.addStatement("$T.setViewClick(instance.findViewById($T.id.$L),$L, instance::$N)",
                                viewClick, rCla, viewId, info.fast, methodName);
                    }
                }

            }
        }
        if (!viewsMap.isEmpty()) {
            injectBuilder.add("\n");
        }
    }

}