package com.inject.compiler.binder;

import com.inject.annotation.OnPageChange;
import com.inject.compiler.Common;
import com.inject.compiler.entity.CustomInject;
import com.inject.compiler.entity.IdEntity;
import com.inject.compiler.entity.IdViewInfo;
import com.inject.compiler.entity.JavaFileInfo;
import com.inject.compiler.entity.PageChangeInfo;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.TypeSpec;

import java.util.HashMap;
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
 * 获取OnPageChange注解的所有信息
 * 创建OnPageChange代码
 *
 * @author 10585
 * @see OnPageChange
 */
public final class OnPageChangeBinder {
    //获取OnClick注解的所有信息
    public static void parseAnnotation(RoundEnvironment roundEnv, Elements elementUtils, Map<String, JavaFileInfo> specs) {
        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(OnPageChange.class);
        for (Element element : elements) {
            OnPageChange annotation = element.getAnnotation(OnPageChange.class);
            OnPageChange.Listen listen = annotation.listen();
            String value = annotation.value();

            ExecutableElement executableElement = (ExecutableElement) element;

            TypeElement typeElement = (TypeElement) element.getEnclosingElement();

            /*类的绝对路径 全类名*/
            String qualifiedName = typeElement.getQualifiedName().toString();

            /*类名*/
            String clsName = typeElement.getSimpleName().toString();
            /*获取包名*/
            String packageName = elementUtils
                    .getPackageOf(typeElement).getQualifiedName().toString();

            String[] split = value.split("\\.");
            String id = split[split.length - 1];

            JavaFileInfo javaFileInfo = specs.get(qualifiedName);
            if (javaFileInfo == null) {
                javaFileInfo = new JavaFileInfo(qualifiedName, packageName, clsName, typeElement);
                specs.put(qualifiedName, javaFileInfo);
            }

            boolean isAndroidRes = split[0].equals("android");
            IdEntity idEntity = new IdEntity(id, isAndroidRes);
            javaFileInfo.pageChangeInfo.add(new PageChangeInfo(idEntity, listen, executableElement));
        }
    }

    //创建OnPageChange代码
    public static void createCode(ClassName rCla,
                                  CustomInject custom,
                                  CodeBlock.Builder injectBuilder,
                                  Set<PageChangeInfo> pageChangeInfo,
                                  HashMap<IdEntity, IdViewInfo> viewsMap) {
        if (!pageChangeInfo.isEmpty()) {
            injectBuilder.add("/**\n * generate code by annotation OnPageChange {@link com.inject.annotation.OnPageChange}\n */\n");
        }

        ClassName viewPager = ClassName.get("androidx.viewpager.widget", "ViewPager");
        ClassName onPageChangeListener = ClassName.get("com.inject.injector", "OnPageChangeListener");

        Map<IdEntity, String> idViewNames = new TreeMap<>((o1, o2) -> o1.id.compareTo(o2.id));
        Map<IdEntity, TypeSpec.Builder> idListeners = new TreeMap<>((o1, o2) -> o1.id.compareTo(o2.id));

        int index = 0;
        for (PageChangeInfo info : pageChangeInfo) {
            IdEntity idEntity = info.id;
            String id = idEntity.id;

            boolean isAndroidRes = idEntity.isAndroidRes;
            OnPageChange.Listen listen = info.listen;
            ExecutableElement methodElement = info.methodElement;

            String methodName = methodElement.getSimpleName().toString();
            String viewName = idViewNames.get(idEntity);
            if (Common.isEmpty(viewName)) {
                viewName = "viewPager" + index;
                IdViewInfo viewInfo = viewsMap.get(idEntity);
                if (viewInfo != null) {
                    if (Common.isNotNeedCast("androidx.viewpager.widget.ViewPager", viewInfo.type)) {
                        injectBuilder.addStatement("$T $N = $N", viewPager, viewName, viewInfo.name);
                    } else {
                        injectBuilder.addStatement("$T $N = ($T) $N", viewPager, viewName, viewPager, viewInfo.name);
                    }
                } else {
                    if (custom == null) {
                        if (isAndroidRes) {
                            injectBuilder.addStatement("$T $N = instance.findViewById(android.R.id.$N)",
                                    viewPager, viewName, id);
                        } else {
                            injectBuilder.addStatement("$T $N = instance.findViewById($T.id.$N)",
                                    viewPager, viewName, rCla, id);
                        }
                    } else {
                        if (isEmpty(custom.fieldName) && isEmpty(custom.methodName)) {
                            if (isAndroidRes) {
                                injectBuilder.addStatement("$T $N = instance.$L(android.R.id.$L)",
                                        viewPager, viewName, custom.method, id);
                            } else {
                                injectBuilder.addStatement("$T $N = instance.$L($T.id.$L)",
                                        viewPager, viewName, custom.method, rCla, id);
                            }
                        } else {
                            if (isAndroidRes) {
                                injectBuilder.addStatement("$T $N = view.findViewById(android.R.id.$L)",
                                        viewPager, viewName, id);
                            } else {
                                injectBuilder.addStatement("$T $N = view.findViewById($T.id.$L)",
                                        viewPager, viewName, rCla, id);
                            }
                        }
                    }

                }
                idViewNames.put(idEntity, "viewPager" + index);
                index++;
            }
            TypeSpec.Builder onPageChangeListenerBuilder = idListeners.get(idEntity);
            if (onPageChangeListenerBuilder == null) {
                onPageChangeListenerBuilder = TypeSpec.anonymousClassBuilder("")
                        .addSuperinterface(onPageChangeListener);
                idListeners.put(idEntity, onPageChangeListenerBuilder);
            }

            if (listen == OnPageChange.Listen.ON_PAGE_SCROLLED) {
                CodeBlock.Builder scrollBuilder = CodeBlock.builder();
                scrollBuilder.addStatement("instance.$N(position, positionOffset, positionOffsetPixels)", methodName);
                //int position, float positionOffset, int positionOffsetPixels
                MethodSpec.Builder onPageScrolled = MethodSpec.methodBuilder("onPageScrolled")
                        .addAnnotation(Override.class)
                        .addParameter(ParameterSpec.builder(int.class, "position").build())
                        .addParameter(ParameterSpec.builder(float.class, "positionOffset").build())
                        .addParameter(ParameterSpec.builder(int.class, "positionOffsetPixels").build())
                        .addModifiers(Modifier.PUBLIC)
                        .addCode(scrollBuilder.build());

                onPageChangeListenerBuilder.addMethod(onPageScrolled.build());
            }

            if (listen == OnPageChange.Listen.ON_PAGE_SCROLL_STATE_CHANGED) {
                CodeBlock.Builder onPageStateBuilder = CodeBlock.builder();
                onPageStateBuilder.addStatement("instance.$N(state)", methodName);

                MethodSpec.Builder onPageScrollStateChange = MethodSpec.methodBuilder("onPageScrollStateChanged")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterSpec.builder(int.class, "state").build())
                        .addCode(onPageStateBuilder.build());

                onPageChangeListenerBuilder.addMethod(onPageScrollStateChange.build());
            }

            if (listen == OnPageChange.Listen.ON_PAGE_SELECTED) {
                CodeBlock.Builder onPageSelectBuilder = CodeBlock.builder()
                        .addStatement("instance.$N(position)", methodName);

                MethodSpec.Builder onPageSelected = MethodSpec.methodBuilder("onPageSelected")
                        .addAnnotation(Override.class)
                        .addModifiers(Modifier.PUBLIC)
                        .addParameter(ParameterSpec.builder(int.class, "position").build())
                        .addCode(onPageSelectBuilder.build());

                onPageChangeListenerBuilder.addMethod(onPageSelected.build());
            }
        }

        for (Map.Entry<IdEntity, TypeSpec.Builder> entry : idListeners.entrySet()) {
            IdEntity idEntity = entry.getKey();
            String viewName = idViewNames.get(idEntity);
            TypeSpec.Builder onPageChangeListenerBuilder = idListeners.get(idEntity);
            injectBuilder.addStatement("$N.addOnPageChangeListener($L)", viewName, onPageChangeListenerBuilder.build());
        }

        if (!pageChangeInfo.isEmpty()) {
            injectBuilder.add("\n");
        }
    }

}