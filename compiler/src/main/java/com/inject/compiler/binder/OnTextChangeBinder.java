package com.inject.compiler.binder;

import com.inject.annotation.OnTextChanged;
import com.inject.compiler.Common;
import com.inject.compiler.entity.CustomInject;
import com.inject.compiler.entity.IdViewInfo;
import com.inject.compiler.entity.JavaFileInfo;
import com.inject.compiler.entity.TextChangeInfo;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import static com.inject.compiler.Common.isEmpty;

/**
 * Created time : 2021/6/23 9:03.
 * 获取OnTextChanged注解的所有信息
 * 创建OnTextChanged代码
 *
 * @author 10585
 * @see OnTextChanged
 */
public final class OnTextChangeBinder {
    //获取OnTextChanged注解的所有信息
    public static void parseAnnotation(RoundEnvironment roundEnv, Elements elementUtils, Map<String, JavaFileInfo> specs) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(OnTextChanged.class);
        for (Element element : elements) {
            OnTextChanged annotation = element.getAnnotation(OnTextChanged.class);
            OnTextChanged.Listen listen = annotation.listen();
            String[] values = annotation.value();

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

            javaFileInfo.textChangeInfo.add(new TextChangeInfo(ids, listen, executableElement));
        }
    }

    //创建OnTextChanged代码
    public static void createCode(ClassName rCla,
                                  CustomInject custom,
                                  CodeBlock.Builder injectBuilder,
                                  Set<TextChangeInfo> pageChangeInfo,
                                  HashMap<String, IdViewInfo> viewsMap) {
        if (!pageChangeInfo.isEmpty()) {
            injectBuilder.add("/**\n * generate code by annotation OnTextChanged {@link com.inject.annotation.OnTextChanged}\n */\n");
        }

        ClassName textView = ClassName.get("android.widget", "TextView");
        ClassName onTextChangeListener = ClassName.get("com.inject.injector", "TextChangeListener");

        Map<String, String> idViewNames = new TreeMap<>();
        Map<List<String>, TypeSpec.Builder> idListeners = new HashMap<>();

        int index = 0;
        for (TextChangeInfo info : pageChangeInfo) {
            List<String> ids = info.ids;

            OnTextChanged.Listen listen = info.listen;
            ExecutableElement methodElement = info.methodElement;
            String methodName = methodElement.getSimpleName().toString();

            for (String id : ids) {
                String viewName = idViewNames.get(id);
                if (Common.isEmpty(viewName)) {
                    viewName = "textView" + index;
                    IdViewInfo viewInfo = viewsMap.get(id);
                    if (viewInfo != null) {
                        if (Common.isNotNeedCast("android.widget.TextView", viewInfo.type)) {
                            injectBuilder.addStatement("$T $N = $N", textView, viewName, viewInfo.name);
                        } else {
                            injectBuilder.addStatement("$T $N = ($T) $N", textView, viewName, textView, viewInfo.name);
                        }
                    } else {
                        if (custom == null) {
                            injectBuilder.addStatement("$T $N = instance.findViewById($T.id.$N)",
                                    textView, viewName, rCla, id);
                        } else {
                            if (isEmpty(custom.fieldName) && isEmpty(custom.methodName)) {
                                injectBuilder.addStatement("$T $N = instance.$L($T.id.$L)",
                                        textView, viewName, custom.method, rCla, id);
                            } else {
                                injectBuilder.addStatement("$T $N = view.findViewById($T.id.$L)",
                                        textView, viewName, rCla, id);
                            }
                        }

                    }
                    idViewNames.put(id, viewName);
                    index++;
                }
            }

            TypeSpec.Builder onTextChangeBuilder = idListeners.get(ids);
            if (onTextChangeBuilder == null) {
                onTextChangeBuilder = TypeSpec.anonymousClassBuilder("")
                        .addSuperinterface(onTextChangeListener);
                idListeners.put(ids, onTextChangeBuilder);
            }

            if (listen == OnTextChanged.Listen.BEFORE_TEXT_CHANGE) {
                CodeBlock.Builder beforeTextChangedBuilder = CodeBlock.builder();
                beforeTextChangedBuilder.addStatement("instance.$N(s, start, count, after)", methodName);
                //CharSequence s, int start, int count, int after
                MethodSpec.Builder beforeTextChanged = MethodSpec.methodBuilder("beforeTextChanged")
                        .addAnnotation(Override.class)
                        .addParameter(ParameterSpec.builder(CharSequence.class, "s").build())
                        .addParameter(ParameterSpec.builder(int.class, "start").build())
                        .addParameter(ParameterSpec.builder(int.class, "count").build())
                        .addParameter(ParameterSpec.builder(int.class, "after").build())
                        .addModifiers(Modifier.PUBLIC)
                        .addCode(beforeTextChangedBuilder.build());

                onTextChangeBuilder.addMethod(beforeTextChanged.build());
            }

            if (listen == OnTextChanged.Listen.ON_TEXT_CHANGE) {
                CodeBlock.Builder onTextChangedBuilder = CodeBlock.builder();
                onTextChangedBuilder.addStatement("instance.$N(s, start, before, count)", methodName);

                MethodSpec.Builder onTextChanged = MethodSpec.methodBuilder("onTextChanged")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterSpec.builder(CharSequence.class, "s").build())
                        .addParameter(ParameterSpec.builder(int.class, "start").build())
                        .addParameter(ParameterSpec.builder(int.class, "before").build())
                        .addParameter(ParameterSpec.builder(int.class, "count").build())
                        .addCode(onTextChangedBuilder.build());

                onTextChangeBuilder.addMethod(onTextChanged.build());
            }

            if (listen == OnTextChanged.Listen.AFTER_TEXT_CHANGE) {
                CodeBlock.Builder afterTextChangedBuilder = CodeBlock.builder()
                        .addStatement("instance.$N(s)", methodName);
                ClassName editable = ClassName.get("android.text", "Editable");

                MethodSpec.Builder afterTextChanged = MethodSpec.methodBuilder("afterTextChanged")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterSpec.builder(editable, "s").build())
                        .addCode(afterTextChangedBuilder.build());

                onTextChangeBuilder.addMethod(afterTextChanged.build());
            }

        }

        int i = 0;
        for (Map.Entry<List<String>, TypeSpec.Builder> entry : idListeners.entrySet()) {
            List<String> ids = entry.getKey();
            TypeSpec.Builder builder = entry.getValue();
//            FieldSpec.Builder initializer = FieldSpec.builder(onTextChangeListener, "textChangeListener" + i)
//                    .initializer("$L", builder.build());

            injectBuilder.addStatement("$T textChangeListener$L = $L", onTextChangeListener, i, builder.build());
            for (String id : ids) {
                String viewName = idViewNames.get(id);
                injectBuilder.addStatement("$N.addTextChangedListener(textChangeListener$L)", viewName, i);
            }

            i++;
            injectBuilder.add("\n");
        }

        if (pageChangeInfo.isEmpty()) {
            injectBuilder.add("\n");
        }
    }

}