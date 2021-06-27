package com.inject.compiler.binder;

import com.inject.annotation.OnCheckedChanged;
import com.inject.compiler.Common;
import com.inject.compiler.entity.CustomInject;
import com.inject.compiler.entity.IdEntity;
import com.inject.compiler.entity.IdViewInfo;
import com.inject.compiler.entity.JavaFileInfo;
import com.inject.compiler.entity.SingleMethodInfo;
import com.inject.index.CheckChangeType;
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
import javax.lang.model.type.DeclaredType;
import javax.lang.model.util.Elements;

import static com.inject.compiler.Common.isEmpty;

/**
 * Created time : 2021/6/23 8:40.
 * 获取OnCheckedChanged注解的所有信息
 * 创建OnCheckedChanged代码
 *
 * @author 10585
 * @see OnCheckedChanged
 */
public final class OnCheckedChangeBinder {

    //获取OnLongClick注解的所有信息
    public static void parseAnnotation(RoundEnvironment roundEnv,
                                       Elements elementUtils,
                                       Map<String, JavaFileInfo> specs) {
        Set<? extends Element> onClicks = roundEnv.getElementsAnnotatedWith(OnCheckedChanged.class);
        for (Element element : onClicks) {
            OnCheckedChanged onClick = element.getAnnotation(OnCheckedChanged.class);
            String[] values = onClick.value();
            CheckChangeType type = onClick.type();

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

            javaFileInfo.checkedChangedMethodMap.add(new SingleMethodInfo(ids, false, executableElement, type));
        }
    }

    //创建OnLongClick代码
    public static void createCode(ClassName rCla, CustomInject custom, CodeBlock.Builder injectBuilder, Set<SingleMethodInfo> methodMap, ClassName viewClick, HashMap<IdEntity, IdViewInfo> viewsMap) {
        if (!methodMap.isEmpty()) {
            injectBuilder.add("/**\n * generate code by annotation OnCheckedChanged {@link com.inject.annotation.OnCheckedChanged}\n */\n");
        }

        ClassName compoundButton = ClassName.get("android.widget", "CompoundButton");
        ClassName radioGroup = ClassName.get("android.widget", "RadioGroup");

        for (SingleMethodInfo info : methodMap) {
            List<IdEntity> idEntities = info.ids;

//            List<String> ids = info.ids;
            ExecutableElement methodElement = info.methodElement;
            String methodName = methodElement.getSimpleName().toString();
            CheckChangeType changeType = info.changeType;


            for (IdEntity idEntity : idEntities) {
                IdViewInfo idViewInfo = viewsMap.get(idEntity);

                if (idViewInfo != null) {
                    DeclaredType type = idViewInfo.type;
                    if (Common.isNotNeedCast("android.widget.CompoundButton", type)) {//CompoundButton
                        injectBuilder.addStatement("$T.setCheckedChange($N, instance::$N)",
                                viewClick, idViewInfo.name, methodName);
                    } else if (Common.isNotNeedCast("android.widget.RadioGroup", type)) {//RadioGroup
                        injectBuilder.addStatement("$T.setRadioChange($N, instance::$N)",
                                viewClick, idViewInfo.name, methodName);
                    } else {
                        if (changeType == CheckChangeType.CompoundButton) {
                            injectBuilder.addStatement("$T.setCheckedChange(($T) $N, instance::$N)",
                                    viewClick, compoundButton, idViewInfo.name, methodName);
                        } else if (changeType == CheckChangeType.RadioGroup) {
                            injectBuilder.addStatement("$T.setRadioChange(($T) $N, instance::$N)",
                                    viewClick, radioGroup, idViewInfo.name, methodName);
                        }
                    }

                } else {
                    boolean isAndroidRes = idEntity.isAndroidRes;
                    String id = idEntity.id;
                    boolean isRadio = changeType != CheckChangeType.CompoundButton;
                    if (custom != null) {
                        if (isEmpty(custom.fieldName) && isEmpty(custom.methodName)) {
                            if (isAndroidRes) {
                                injectBuilder.addStatement("$T.$N(instance.$L(android.R.id.$L), instance::$N)",
                                        viewClick, isRadio ? "setRadioChange" : "setCheckedChange", custom.method, id, methodName);
                            } else {
                                injectBuilder.addStatement("$T.$N(instance.$L($T.id.$L), instance::$N)",
                                        viewClick, isRadio ? "setRadioChange" : "setCheckedChange", custom.method, rCla, id, methodName);
                            }
                        } else {
                            if (isAndroidRes) {
                                injectBuilder.addStatement("$T.$N(view.findViewById(android.R.id.$L), instance::$N)",
                                        viewClick, isRadio ? "setRadioChange" : "setCheckedChange", id, methodName);
                            } else {
                                injectBuilder.addStatement("$T.$N(view.findViewById($T.id.$L), instance::$N)",
                                        viewClick, isRadio ? "setRadioChange" : "setCheckedChange", rCla, id, methodName);
                            }
                        }
                    } else {
                        if (isAndroidRes) {
                            injectBuilder.addStatement("$T.$N(instance.findViewById(android.R.id.$L), instance::$N)",
                                    viewClick, isRadio ? "setRadioChange" : "setCheckedChange", id, methodName);
                        } else {
                            injectBuilder.addStatement("$T.$N(instance.findViewById($T.id.$L), instance::$N)",
                                    viewClick, isRadio ? "setRadioChange" : "setCheckedChange", rCla, id, methodName);
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