package com.inject.compiler.binder;

import com.inject.annotation.OnLongClick;
import com.inject.compiler.entity.CustomInject;
import com.inject.compiler.entity.IdEntity;
import com.inject.compiler.entity.IdViewInfo;
import com.inject.compiler.entity.JavaFileInfo;
import com.inject.compiler.entity.SingleMethodInfo;
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
 * 获取OnLongClick注解的所有信息
 * 创建OnLongClick代码
 *
 * @author 10585
 * @see OnLongClick
 */
public final class OnLongClickBinder {

    //获取OnLongClick注解的所有信息
    public static void parseAnnotation(RoundEnvironment roundEnv, Elements elementUtils, Map<String, JavaFileInfo> specs) {
        Set<? extends Element> onClicks = roundEnv.getElementsAnnotatedWith(OnLongClick.class);
        for (Element element : onClicks) {
            OnLongClick onClick = element.getAnnotation(OnLongClick.class);
            String[] values = onClick.value();

            ExecutableElement executableElement = (ExecutableElement) element;
            TypeElement typeElement = (TypeElement) element.getEnclosingElement();

            /*类的绝对路径 全类名*/
            String qualifiedName = typeElement.getQualifiedName().toString();

            /*类名*/
            String clsName = typeElement.getSimpleName().toString();
            /*获取包名*/
            String packageName = elementUtils
                    .getPackageOf(typeElement).getQualifiedName().toString();

            List<IdEntity> ids = new ArrayList<>();
            for (String value : values) {
                String[] split = value.split("\\.");
                String id = split[split.length - 1];
                boolean isAndroidRes = split[0].equals("android");
                IdEntity idEntity = new IdEntity(id, isAndroidRes);
                ids.add(idEntity);
            }

            JavaFileInfo javaFileInfo = specs.get(qualifiedName);
            if (javaFileInfo == null) {
                javaFileInfo = new JavaFileInfo(qualifiedName, packageName, clsName, typeElement);
                specs.put(qualifiedName, javaFileInfo);
            }

            javaFileInfo.longClickMethodMap.add(new SingleMethodInfo(ids, false, executableElement));
        }
    }

    //创建OnLongClick代码
    public static void createCode(ClassName rCla, CustomInject custom,
                                  CodeBlock.Builder injectBuilder,
                                  Set<SingleMethodInfo> methodMap, ClassName viewClick,
                                  HashMap<IdEntity, IdViewInfo> viewsMap) {
        if (!methodMap.isEmpty()) {
            injectBuilder.add("/**\n * generate code by annotation OnLongClick {@link com.inject.annotation.OnLongClick}\n */\n");
        }

        for (SingleMethodInfo info : methodMap) {
            List<IdEntity> idEntities = info.ids;
            ExecutableElement methodElement = info.methodElement;
            String methodName = methodElement.getSimpleName().toString();

            for (IdEntity idEntity : idEntities) {
                IdViewInfo idViewInfo = viewsMap.get(idEntity);

                if (idViewInfo != null) {
                    injectBuilder.addStatement("$T.setViewLongClick($N, instance::$N)",
                            viewClick, idViewInfo.name, methodName);
                } else {
                    boolean isAndroidRes = idEntity.isAndroidRes;
                    String id = idEntity.id;

                    if (custom != null) {
                        if (isEmpty(custom.fieldName) && isEmpty(custom.methodName)) {
                            if (isAndroidRes) {
                                injectBuilder.addStatement("$T.setViewLongClick(instance.$L(android.R.id.$L), instance::$N)",
                                        viewClick, custom.method, id, methodName);
                            } else {
                                injectBuilder.addStatement("$T.setViewLongClick(instance.$L($T.id.$L), instance::$N)",
                                        viewClick, custom.method, rCla, id, methodName);
                            }
                        } else {
                            if (isAndroidRes) {
                                injectBuilder.addStatement("$T.setViewLongClick(view.findViewById(android.R.id.$L), instance::$N)",
                                        viewClick, id, methodName);
                            } else {
                                injectBuilder.addStatement("$T.setViewLongClick(view.findViewById($T.id.$L), instance::$N)",
                                        viewClick, rCla, id, methodName);
                            }
                        }
                    } else {
                        if (isAndroidRes) {
                            injectBuilder.addStatement("$T.setViewLongClick(instance.findViewById(android.R.id.$L), instance::$N)",
                                    viewClick, id, methodName);
                        } else {
                            injectBuilder.addStatement("$T.setViewLongClick(instance.findViewById($T.id.$L), instance::$N)",
                                    viewClick, rCla, id, methodName);
                        }
                    }
                }
            }
        }
        if (!viewsMap.isEmpty()) {
            injectBuilder.add("\n");
        }
    }

}