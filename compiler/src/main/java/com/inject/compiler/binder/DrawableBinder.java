package com.inject.compiler.binder;

import com.inject.annotation.BindDrawable;
import com.inject.compiler.entity.IdEntity;
import com.inject.compiler.entity.JavaFileInfo;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;

import java.util.Map;
import java.util.Set;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.util.Elements;

/**
 * Created time : 2021/6/23 8:38.
 * 获取BindDrawable注解的所有信息
 *
 * @author 10585
 * @see BindDrawable
 */
public final class DrawableBinder {

    //获取BindDrawable注解的所有信息
    public static void parseAnnotation(RoundEnvironment roundEnv, Elements elementUtils, Map<String, JavaFileInfo> specs) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(BindDrawable.class);

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

            BindDrawable bindAnim = element.getAnnotation(BindDrawable.class);
            String value = bindAnim.value();

            String[] split = value.split("\\.");
            String id = split[split.length - 1];

            JavaFileInfo javaFileInfo = specs.get(qualifiedName);
            if (javaFileInfo == null) {
                javaFileInfo = new JavaFileInfo(qualifiedName, packageName, clsName, typeElement);
                specs.put(qualifiedName, javaFileInfo);
            }

            boolean isAndroidRes = split[0].equals("android");
            IdEntity idEntity = new IdEntity(id, isAndroidRes);
            javaFileInfo.drawableMap.put(idEntity, variableElement);
        }
    }

    //创建BindDrawable代码
    public static void createCode(ClassName rCla, CodeBlock.Builder injectBuilder, Map<IdEntity, VariableElement> drawableMap) {
        if (!drawableMap.isEmpty()) {
            injectBuilder.add("/**\n * generate code by annotation BindDrawable {@link com.inject.annotation.BindDrawable}\n */\n");
        }

        ClassName contextCompat = ClassName.get("androidx.core.content", "ContextCompat");

        for (Map.Entry<IdEntity, VariableElement> entry : drawableMap.entrySet()) {
            IdEntity idEntity = entry.getKey();
            boolean isAndroidRes = idEntity.isAndroidRes;
            String id = idEntity.id;

            VariableElement element = entry.getValue();

            String name = element.getSimpleName().toString();
            if (isAndroidRes) {
                injectBuilder.addStatement("instance.$N = $T.getDrawable(context, android.R.drawable.$N)",
                        name, contextCompat, id);
            } else {
                injectBuilder.addStatement("instance.$N = $T.getDrawable(context, $T.drawable.$N)",
                        name, contextCompat, rCla, id);
            }
        }
        if (!drawableMap.isEmpty()) {
            injectBuilder.add("\n");
        }
    }

}