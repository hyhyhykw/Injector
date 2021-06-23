package com.inject.compiler.binder;

import com.inject.annotation.BindArray;
import com.inject.compiler.entity.ArrayInfo;
import com.inject.compiler.entity.JavaFileInfo;
import com.inject.compiler.entity.ViewsType;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import static com.inject.compiler.Common.getByFullName;
import static com.inject.compiler.Common.patterArrayListViews;
import static com.inject.compiler.Common.patterArrayViews;
import static com.inject.compiler.Common.patterCollectionViews;
import static com.inject.compiler.Common.patterLinkedListViews;
import static com.inject.compiler.Common.patterListViews;

/**
 * Created time : 2021/6/23 8:44.
 * 获取BindArray注解的所有信息
 *
 * @author 10585
 * @see BindArray
 */
public final class ArrayBinder {

    //获取BindArray注解的所有信息
    public static void parseAnnotation(RoundEnvironment roundEnv, Elements elementUtils, Map<String, JavaFileInfo> specs) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(BindArray.class);

        for (Element element : elements) {
            BindArray annotation = element.getAnnotation(BindArray.class);
            //类型
            TypeMirror typeMirror = element.asType();
            String type = typeMirror.toString();
            /*类的绝对路径 全类名*/
            String eleQualifiedName;
            /*类名*/
            String eleClsName;
            /*获取包名*/
            String elePackageName;

            DeclaredType paramsType;

            //获取数组或者集合的参数类型
            if (typeMirror instanceof ArrayType) {
                ArrayType arrayType = (ArrayType) typeMirror;
                TypeMirror componentType = arrayType.getComponentType();

                paramsType = (DeclaredType) componentType;
                TypeElement paramsElement = (TypeElement) paramsType.asElement();

                /*类的绝对路径 全类名*/
                eleQualifiedName = paramsElement.getQualifiedName().toString();
                /*类名*/
                eleClsName = paramsElement.getSimpleName().toString();
                /*获取包名*/
                elePackageName = elementUtils
                        .getPackageOf(paramsElement).getQualifiedName().toString();


            } else if (typeMirror instanceof DeclaredType) {
                DeclaredType declaredType = (DeclaredType) typeMirror;
                List<? extends TypeMirror> typeArguments = declaredType.getTypeArguments();

                if (typeArguments.isEmpty()) continue;

                TypeMirror componentType = typeArguments.get(0);
                paramsType = (DeclaredType) componentType;
                TypeElement paramsElement = (TypeElement) paramsType.asElement();

                /*类的绝对路径 全类名*/
                eleQualifiedName = paramsElement.getQualifiedName().toString();
                /*类名*/
                eleClsName = paramsElement.getSimpleName().toString();
                /*获取包名*/
                elePackageName = elementUtils
                        .getPackageOf(paramsElement).getQualifiedName().toString();
            } else {
                continue;
            }


            ViewsType viewsType;

            Matcher listMatcher = patterListViews.matcher(type);
            Matcher collectMatcher = patterCollectionViews.matcher(type);
            Matcher arrayListMatcher = patterArrayListViews.matcher(type);
            Matcher linkedListMatcher = patterLinkedListViews.matcher(type);
            Matcher arrayMatcher = patterArrayViews.matcher(type);

            if (listMatcher.matches()) {
                viewsType = ViewsType.ArrayList;
            } else if (collectMatcher.matches()) {
                viewsType = ViewsType.ArrayList;
            } else if (arrayListMatcher.matches()) {
                viewsType = ViewsType.ArrayList;
            } else if (linkedListMatcher.matches()) {
                viewsType = ViewsType.LinkedList;
            } else if (arrayMatcher.matches()) {
                viewsType = ViewsType.Array;
            } else {
                System.out.println("无法获取类型：type======>" + type);
                continue;
            }
            VariableElement variableElement = (VariableElement) element;
            TypeElement typeElement = (TypeElement) element.getEnclosingElement();

            /*类的绝对路径 全类名*/
            String qualifiedName = typeElement.getQualifiedName().toString();

            /*类名*/
            String clsName = typeElement.getSimpleName().toString();
            /*获取包名*/
            String packageName = elementUtils
                    .getPackageOf(typeElement).getQualifiedName().toString();

            String value = annotation.value();

            String[] split = value.split("\\.");
            String id = split[split.length - 1];


            JavaFileInfo javaFileInfo = specs.get(qualifiedName);
            if (javaFileInfo == null) {
                javaFileInfo = new JavaFileInfo(qualifiedName, packageName, clsName, typeElement);
                specs.put(qualifiedName, javaFileInfo);
            }

            javaFileInfo.arrayInfo.add(new ArrayInfo(id, viewsType,
                    eleQualifiedName, elePackageName, eleClsName, variableElement, paramsType));
        }
    }

    //创建BindArray代码
    public static void createCode(ClassName rCla, CodeBlock.Builder injectBuilder, Set<ArrayInfo> arrayInfo) {
        if (!arrayInfo.isEmpty()) {
            injectBuilder.add("/**\n * generate code by annotation BindArray {@link com.inject.annotation.BindArray}\n */\n");
        }
        int i = 0;
        for (ArrayInfo info : arrayInfo) {
            String id = info.id;

            VariableElement variableElement = info.variableElement;
            String varName = variableElement.getSimpleName().toString();

            String eleQualifiedName = info.eleQualifiedName;
            String elePackageName = info.elePackageName;

            ClassName params = getByFullName(eleQualifiedName, elePackageName);

            ViewsType viewsType = info.type;

            if (eleQualifiedName.equals("java.lang.String") && viewsType == ViewsType.Array) {
                injectBuilder.addStatement("instance.$N = context.getResources().getStringArray($T.array.$N)", varName, rCla, id);
                continue;
            }

            injectBuilder.addStatement("String[] array$L = context.getResources().getStringArray($T.array.$N)",
                    i, rCla, id);

            ClassName listClass = null;
            switch (viewsType) {
                default:
                case ArrayList:
                    listClass = ClassName.get(ArrayList.class);
                case LinkedList:
                    if (listClass == null) {
                        listClass = ClassName.get(LinkedList.class);
                    }
                    injectBuilder.addStatement("instance.$N = new $T<>()", varName, listClass);
                    injectBuilder.add("for (String str : array$L) {" +
                            "\n    instance.$N.add(str);" +
                            "\n}\n", i, varName);
                    break;
                case Array:
                    injectBuilder.addStatement("instance.$N = new $T[array$L.length]", varName, params, i);
                    injectBuilder.add("for (int i = 0; i < array$L.length; i++) {" +
                            "\n    instance.$N[i] = array$L[i];" +
                            "\n}\n", i, varName, i);
                    break;
            }
            i++;
            injectBuilder.add("\n");
        }
    }

}