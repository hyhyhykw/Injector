package com.inject.compiler.binder;

import com.inject.annotation.BindView;
import com.inject.compiler.entity.CustomInject;
import com.inject.compiler.entity.IdViewInfo;
import com.inject.compiler.entity.JavaFileInfo;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;

import static com.inject.compiler.Common.isEmpty;

/**
 * Created time : 2021/6/23 8:43.
 * 获取BindView注解的所有信息
 *
 * @author 10585
 * @see BindView
 */
public final class ViewBinder {

    public static void parseAnnotation(RoundEnvironment roundEnv, Elements elementUtils, Map<String, JavaFileInfo> specs) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(BindView.class);
        for (Element element : elements) {
            VariableElement variableElement = (VariableElement) element;
            //类信息
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
            /*类的绝对路径 全类名*/
            String qualifiedName = typeElement.getQualifiedName().toString();

            /*类名*/
            String clsName = typeElement.getSimpleName().toString();
            /*获取包名*/
            String packageName = elementUtils
                    .getPackageOf(typeElement).getQualifiedName().toString();

            BindView annotation = variableElement.getAnnotation(BindView.class);
            String value = annotation.value();
            String[] split = value.split("\\.");
            String id = split[split.length - 1];

            JavaFileInfo javaFileInfo = specs.get(qualifiedName);
            if (javaFileInfo == null) {
                javaFileInfo = new JavaFileInfo(qualifiedName, packageName, clsName, typeElement);
                specs.put(qualifiedName, javaFileInfo);
            }
            javaFileInfo.viewIdMap.put(id, variableElement);
        }
    }


    //创建BindView代码
    public static void createCode(ClassName rCla, CustomInject custom, CodeBlock.Builder injectBuilder, Map<String, VariableElement> varMap, HashMap<String, IdViewInfo> viewsMap) {
        if (!varMap.isEmpty()) {
            injectBuilder.add("/**\n * generate code by annotation BindView {@link com.inject.annotation.BindView}\n */\n");
        }

        for (Map.Entry<String, VariableElement> entry : varMap.entrySet()) {
            String id = entry.getKey();
            VariableElement entryValue = entry.getValue();

            String name = entryValue.getSimpleName().toString();

            viewsMap.put(id, new IdViewInfo("instance." + name, (DeclaredType) entryValue.asType()));

            if (custom != null) {
                if (isEmpty(custom.fieldName) && isEmpty(custom.methodName)) {
                    injectBuilder.addStatement("instance.$N = instance.$L($T.id.$L)",
                            name, custom.method, rCla, id);
                } else {
                    injectBuilder.addStatement("instance.$N = view.findViewById($T.id.$L)",
                            name, rCla, id);
                }
            } else {
                injectBuilder.addStatement("instance.$N = instance.findViewById($T.id.$L)",
                        name, rCla, id);
            }
        }

        if (!varMap.isEmpty()) {
            injectBuilder.add("\n");
        }
    }

}