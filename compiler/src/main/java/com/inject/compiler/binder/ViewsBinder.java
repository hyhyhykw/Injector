package com.inject.compiler.binder;

import com.inject.annotation.BindViews;
import com.inject.compiler.entity.CustomInject;
import com.inject.compiler.entity.IdViewInfo;
import com.inject.compiler.entity.JavaFileInfo;
import com.inject.compiler.entity.ViewsBindInfo;
import com.inject.compiler.entity.ViewsType;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

import java.util.ArrayList;
import java.util.HashMap;
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
import static com.inject.compiler.Common.isEmpty;
import static com.inject.compiler.Common.isNotNeedCast;
import static com.inject.compiler.Common.patterArrayListViews;
import static com.inject.compiler.Common.patterArrayViews;
import static com.inject.compiler.Common.patterCollectionViews;
import static com.inject.compiler.Common.patterLinkedListViews;
import static com.inject.compiler.Common.patterListViews;

/**
 * Created time : 2021/6/23 8:41.
 * 获取BindViews注解的所有信息
 *
 * @author 10585
 * @see BindViews
 */
public final class ViewsBinder {

    //获取BindViews注解的所有信息
    public  static void parseAnnotation(RoundEnvironment roundEnv, Elements elementUtils, Map<String, JavaFileInfo> specs) {
        Set<? extends Element> bindViews = roundEnv.getElementsAnnotatedWith(BindViews.class);
        for (Element element : bindViews) {
            BindViews annotation = element.getAnnotation(BindViews.class);

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

            String[] values = annotation.value();

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

            javaFileInfo.viewsList.add(new ViewsBindInfo(ids, viewsType,
                    eleQualifiedName, elePackageName, eleClsName, variableElement, paramsType));
        }
    }

    //创建BindViews代码
    public  static void createCode(ClassName rCla, CustomInject custom, CodeBlock.Builder injectBuilder, Set<ViewsBindInfo> viewsList, HashMap<String, IdViewInfo> viewsMap) {
        if (!viewsList.isEmpty()) {
            injectBuilder.add("/**\n * generate code by annotation BindViews {@link com.inject.annotation.BindViews}\n */\n");
        }
        for (ViewsBindInfo info : viewsList) {
            List<String> ids = info.ids;

            VariableElement variableElement = info.variableElement;
            String varName = variableElement.getSimpleName().toString();

            String eleQualifiedName = info.eleQualifiedName;
            String elePackageName = info.elePackageName;

            ClassName params = getByFullName(eleQualifiedName, elePackageName);

            ViewsType viewsType = info.type;

            switch (viewsType) {
                default:
                case ArrayList:
                    injectBuilder.addStatement("instance.$N = new $T<>()", varName, ClassName.get(ArrayList.class));
                    break;
                case LinkedList:
                    injectBuilder.addStatement("instance.$N = new $T<>()", varName, ClassName.get(LinkedList.class));
                    break;
                case Array:
                    injectBuilder.addStatement("instance.$N = new $T[$L]", varName, params, ids.size());
                    break;
            }

            for (int i = 0; i < ids.size(); i++) {
                String viewId = ids.get(i);

                IdViewInfo idViewInfo = viewsMap.get(viewId);

                String viewName;
                boolean needCast;
                if (idViewInfo == null) {
                    needCast = false;
                    viewName = varName + i;
                    viewsMap.put(viewId, new IdViewInfo(viewName, info.paramsType));

                    if (custom != null) {
                        if (isEmpty(custom.fieldName) && isEmpty(custom.methodName)) {
                            injectBuilder.addStatement("$T $N = instance.$L($T.id.$L)",
                                    params, viewName, custom.method, rCla, viewId);
                        } else {
                            injectBuilder.addStatement("$T $N = view.findViewById($T.id.$L)",
                                    params, viewName, rCla, viewId);
                        }
                    } else {
                        injectBuilder.addStatement("$T $N = instance.findViewById($T.id.$L)",
                                params, viewName, rCla, viewId);
                    }
                } else {
                    viewName = idViewInfo.name;
                    DeclaredType viewType = idViewInfo.type;
                    needCast = !isNotNeedCast(eleQualifiedName, viewType);
                }

                switch (viewsType) {
                    case ArrayList:
                    case LinkedList:
                    default:
                        if (needCast) {
                            injectBuilder.addStatement("instance.$N.add(($T) $N)",
                                    varName, params, viewName);
                        } else {
                            injectBuilder.addStatement("instance.$N.add($N)",
                                    varName, viewName);
                        }
                        break;
                    case Array:
                        if (needCast) {
                            injectBuilder.addStatement("instance.$N[$L] = ($T) $N",
                                    varName, i, params, viewName);
                        } else {
                            injectBuilder.addStatement("instance.$N[$L] = $N",
                                    varName, i, viewName);
                        }
                        break;
                }
            }

            injectBuilder.add("\n");
        }
    }

}