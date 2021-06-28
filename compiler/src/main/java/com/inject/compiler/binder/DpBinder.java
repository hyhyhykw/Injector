package com.inject.compiler.binder;

import com.inject.annotation.Dp;
import com.inject.compiler.Common;
import com.inject.compiler.entity.DpInfo;
import com.inject.compiler.entity.JavaFileInfo;
import com.squareup.javapoet.CodeBlock;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

/**
 * Created time : 2021/6/26 13:44.
 *
 * @author 10585
 */
public final class DpBinder {
    private static final List<TypeKind> typeKinds = Arrays.asList(
            TypeKind.LONG,
            TypeKind.FLOAT,
            TypeKind.DOUBLE,
            TypeKind.INT
    );

    private static final List<String> typeNumber = Arrays.asList(
            "java.lang.Integer",
            "java.lang.Float",
            "java.lang.Double",
            "java.lang.Long"
    );


    public static void parseAnnotation(RoundEnvironment roundEnv,
                                       Elements elementUtils,
                                       Map<String, JavaFileInfo> specs) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(Dp.class);

        for (Element element : elements) {
            VariableElement variableElement = (VariableElement) element;
            TypeMirror typeMirror = variableElement.asType();
            TypeKind kind = typeMirror.getKind();

            String priType;
            if (!typeKinds.contains(kind)) {
                String type;
                if (kind == TypeKind.DECLARED) {
                    DeclaredType declaredType = (DeclaredType) typeMirror;
                    TypeElement typeElement = (TypeElement) declaredType.asElement();
                    type = typeElement.getQualifiedName().toString();

                    if (!typeNumber.contains(type)) {
                        continue;
                    }
                } else {
                    continue;
                }

                switch (type) {
                    case "java.lang.Integer":
                    default:
                        priType = "int";
                        break;
                    case "java.lang.Long":
                        priType = "long";
                        break;
                    case "java.lang.Float":
                        priType = "";
                        break;
                    case "java.lang.Double":
                        priType = "double";
                        break;
                }
            } else {
                boolean isFloat = kind == TypeKind.FLOAT || kind == TypeKind.DOUBLE;
                priType = isFloat ? "" : "int";
            }


            //类信息
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
            /*类的绝对路径 全类名*/
            String qualifiedName = typeElement.getQualifiedName().toString();
            /*类名*/
            String clsName = typeElement.getSimpleName().toString();
            /*获取包名*/
            String packageName = elementUtils
                    .getPackageOf(typeElement).getQualifiedName().toString();

            Dp bindAnim = element.getAnnotation(Dp.class);
            float value = bindAnim.value();

            JavaFileInfo javaFileInfo = specs.get(qualifiedName);
            if (javaFileInfo == null) {
                javaFileInfo = new JavaFileInfo(qualifiedName, packageName, clsName, typeElement);
                specs.put(qualifiedName, javaFileInfo);
            }


            DpInfo dpInfo = new DpInfo(value + "", variableElement.getSimpleName().toString());
            dpInfo.type = priType;
            javaFileInfo.dpInfo.add(dpInfo);
        }
    }

    public static void createCode(CodeBlock.Builder injectBuilder, Set<DpInfo> info) {
        if (!info.isEmpty()) {
            injectBuilder.add("/**\n * generate code by annotation Dp {@link com.inject.annotation.Dp}\n */\n");
            //            float scale = context.getResources().getDisplayMetrics().density;
//            return (int) (dpVal * scale + 0.5f);
            injectBuilder.addStatement("float scale = metrics.density");
            for (DpInfo dpInfo : info) {
                String type = dpInfo.type;
                if (!Common.isEmpty(type)) {
                    injectBuilder.addStatement("instance.$N = ($N) ($Nf * scale + 0.5f)", dpInfo.name, type, dpInfo.value);
                }else{
                    injectBuilder.addStatement("instance.$N = $Nf * scale + 0.5f", dpInfo.name, dpInfo.value);
                }
            }
            injectBuilder.add("\n");
        }
    }
}