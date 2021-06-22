package com.inject.compiler;

import com.inject.annotation.BindView;
import com.inject.annotation.Injector;
import com.inject.annotation.InjectorIndex;
import com.inject.annotation.OnClick;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

/**
 * Created time : 2021/6/20 11:53.
 *
 * @author 10585
 */
@SupportedOptions(value = {BindProcessor.OPTION_PKG_NAME, BindProcessor.OPTION_INDEX})
@SupportedSourceVersion(SourceVersion.RELEASE_8)
public class BindProcessor extends AbstractProcessor {
    public static final String OPTION_PKG_NAME = "pkgName";
    public static final String OPTION_INDEX = "claIndex";
    public static final String OPTION_CUSTOM_INJECT = "myInject";
    public static final String OPTION_CUSTOM_INJECT_METHOD = "injectMethod";

    public static final String OPTION_CUSTOM_INJECT_VIEW_FIELD = "injectViewField";
    public static final String OPTION_CUSTOM_INJECT_VIEW_METHOD = "injectViewMethod";

    //自定义生成代码的方法
    @Override
    public Set<String> getSupportedOptions() {
        Set<String> objects = new HashSet<>(super.getSupportedOptions());
        objects.add(OPTION_CUSTOM_INJECT);
        objects.add(OPTION_CUSTOM_INJECT_METHOD);
        objects.add(OPTION_CUSTOM_INJECT_VIEW_FIELD);
        objects.add(OPTION_CUSTOM_INJECT_VIEW_METHOD);

        for (int i = 1; i <= 10; i++) {
            objects.add(OPTION_CUSTOM_INJECT + i);
            objects.add(OPTION_CUSTOM_INJECT_METHOD + i);
            objects.add(OPTION_CUSTOM_INJECT_VIEW_FIELD + i);
            objects.add(OPTION_CUSTOM_INJECT_VIEW_METHOD + i);
        }
        return objects;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new HashSet<>();
        types.add(BindView.class.getName());
        types.add(OnClick.class.getName());
        return types;
    }

    private boolean isProcessed = false;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        isProcessed = false;
    }


    private boolean isEmpty(String str) {
        return str == null || str.length() == 0;
    }

    private Set<CustomInject> customInject() {
        String inject = processingEnv.getOptions().get(OPTION_CUSTOM_INJECT);
        String injectMethod = processingEnv.getOptions().get(OPTION_CUSTOM_INJECT_METHOD);

        if (isEmpty(injectMethod)) {
            injectMethod = "findViewById";
        }
        Set<CustomInject> customInjects = new HashSet<>();
        if (!isEmpty(inject)) {
            CustomInject customInject = new CustomInject(inject, injectMethod);
            String injectViewField = processingEnv.getOptions().get(OPTION_CUSTOM_INJECT_VIEW_FIELD);
            String injectViewMethod = processingEnv.getOptions().get(OPTION_CUSTOM_INJECT_VIEW_METHOD);
            customInject.fieldName = injectViewField;
            customInject.methodName = injectViewMethod;
            customInjects.add(customInject);
        }
        for (int i = 1; i <= 10; i++) {
            String injectI = processingEnv.getOptions().get(OPTION_CUSTOM_INJECT + i);
            if (isEmpty(injectI)) continue;

            String injectMethodI = processingEnv.getOptions().get(OPTION_CUSTOM_INJECT_METHOD + i);
            if (isEmpty(injectMethodI)) {
                injectMethodI = "findViewById";
            }
            CustomInject customInject = new CustomInject(injectI, injectMethodI);

            String injectViewField = processingEnv.getOptions().get(OPTION_CUSTOM_INJECT_VIEW_FIELD + i);
            String injectViewMethod = processingEnv.getOptions().get(OPTION_CUSTOM_INJECT_VIEW_METHOD + i);
            customInject.fieldName = injectViewField;
            customInject.methodName = injectViewMethod;

            customInjects.add(customInject);
        }

        return customInjects;
    }

    Set<CustomInject> customInjects;

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (isProcessed) return false;
        isProcessed = true;
        customInjects = customInject();

        String pkgName = processingEnv.getOptions().get(OPTION_PKG_NAME);
        String indexName = processingEnv.getOptions().get(OPTION_INDEX);

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(BindView.class);

        Map<String, VarInfo> specs = new HashMap<>();

        for (Element element : elements) {
            if (!(element instanceof VariableElement)) continue;
            VariableElement variableElement = (VariableElement) element;
            //类信息
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
            /*类的绝对路径 全类名*/
            String qualifiedName = typeElement.getQualifiedName().toString();

            /*类名*/
            String clsName = typeElement.getSimpleName().toString();
            /*获取包名*/
            String packageName = processingEnv.getElementUtils()
                    .getPackageOf(typeElement).getQualifiedName().toString();

            BindView annotation = variableElement.getAnnotation(BindView.class);
            String value = annotation.value();
            String[] split = value.split("\\.");
            String id = split[split.length - 1];

            VarInfo varInfo = specs.get(qualifiedName);
            if (varInfo == null) {
                varInfo = new VarInfo(qualifiedName, packageName, clsName, typeElement);
                specs.put(qualifiedName, varInfo);
            }
            varInfo.varMap.put(id, variableElement);
        }


        Set<? extends Element> elements1 = roundEnv.getElementsAnnotatedWith(OnClick.class);
        for (Element element : elements1) {
            ElementKind kind = element.getKind();
            if (kind != ElementKind.METHOD) continue;
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
            String packageName = processingEnv.getElementUtils()
                    .getPackageOf(typeElement).getQualifiedName().toString();


            List<String> ids = new ArrayList<>();
            for (String value : values) {
                String[] split = value.split("\\.");
                String id = split[split.length - 1];
                ids.add(id);
            }

            VarInfo varInfo = specs.get(qualifiedName);
            if (varInfo == null) {
                varInfo = new VarInfo(qualifiedName, packageName, clsName, typeElement);
                specs.put(qualifiedName, varInfo);
            }

            varInfo.methodMap.add(new MethodInfo(ids, fast, executableElement));
        }


        try {
            createCode(specs, pkgName, indexName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private void createCode(Map<String, VarInfo> specs,
                            String pkgName,
                            String indexName) throws Exception {
        ClassName rCla = ClassName.get(pkgName, "R");
        WildcardTypeName variableName = WildcardTypeName.subtypeOf(Injector.class);
        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(Class.class), variableName);

        ParameterizedTypeName typeName = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                TypeName.get(String.class), parameterizedTypeName);

        FieldSpec.Builder indexMap = FieldSpec.builder(typeName, "INDEX", Modifier.STATIC, Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new $T<>()", HashMap.class);
        FieldSpec indexField = indexMap.build();

        CodeBlock.Builder codeBuilder = CodeBlock.builder();

        for (VarInfo value : specs.values()) {
            TypeElement type = value.type;
            CustomInject custom = getCustom(type);

            String claName = value.claName;
            String packageName = value.packageName;
            String qualifiedName = value.qualifiedName;
            String substring = qualifiedName.replace(packageName, "")
                    .substring(1);
            String[] split = substring.split("\\.");
            ClassName instance;
            if (split.length > 1) {
                String[] strings = new String[split.length - 1];

                for (int i = 0; i < split.length; i++) {
                    if (i == 0) continue;
                    String s = split[i];
                    strings[i - 1] = s;
                }

                instance = ClassName.get(packageName, split[0], strings);
            } else {
                instance = ClassName.get(packageName, claName);
            }

            CodeBlock.Builder injectBuilder = CodeBlock.builder();
            injectBuilder.addStatement("$T instance = ($T) object", instance, instance);
            if (custom != null) {
                String fieldName = custom.fieldName;
                String methodName = custom.methodName;

                ClassName view = ClassName.get("android.view", "View");
                if (!isEmpty(fieldName)) {
                    injectBuilder.addStatement("$T view = instance.$L", view, fieldName);
                } else if (!isEmpty(methodName)) {
                    injectBuilder.addStatement("$T view = instance.$L()", view, methodName);
                }
            }

            Set<MethodInfo> methodMap = value.methodMap;

            ClassName viewClick = ClassName.get("com.inject.injector", "ViewClick");


            for (Map.Entry<String, VariableElement> entry : value.varMap.entrySet()) {
                String id = entry.getKey();
                VariableElement entryValue = entry.getValue();

                String name = entryValue.getSimpleName().toString();

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

                for (MethodInfo info : methodMap) {
                    List<String> ids = info.ids;
                    ExecutableElement methodElement = info.methodElement;
                    String methodName = methodElement.getSimpleName().toString();

                    if (ids.contains(id)) {
                        injectBuilder.addStatement(
                                "$T.setViewClick(instance.$N,$L, instance::$N)", viewClick, name, info.fast, methodName
                        );

                        ids.remove(id);
                    }
                }
            }

            for (MethodInfo info : methodMap) {
                List<String> ids = info.ids;
                ExecutableElement methodElement = info.methodElement;
                String methodName = methodElement.getSimpleName().toString();

                for (String s : ids) {
                    if (custom != null) {
                        if (isEmpty(custom.fieldName) && isEmpty(custom.methodName)) {
                            injectBuilder.addStatement("$T.setViewClick(instance.$L($T.id.$L), $L, instance::$N)",
                                    viewClick, custom.method, rCla, s, info.fast, methodName);
                        } else {
                            injectBuilder.addStatement("$T.setViewClick(view.findViewById($T.id.$L), $L, instance::$N)",
                                    viewClick, rCla, s, info.fast, methodName);
                        }
                    } else {
                        injectBuilder.addStatement("$T.setViewClick(instance.findViewById($T.id.$L),$L, instance::$N)",
                                viewClick, rCla, s, info.fast, methodName);
                    }
                }
            }

            ParameterSpec.Builder param = ParameterSpec.builder(Object.class, "object");

            MethodSpec.Builder inject = MethodSpec.methodBuilder("inject")
                    .addParameter(param.build())
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addCode(injectBuilder.build());

            ClassName className = ClassName.get(packageName, claName + "Injector");

            TypeSpec build = TypeSpec.classBuilder(className)
                    .addModifiers(Modifier.FINAL, Modifier.PUBLIC)
                    .addMethod(inject.build())
                    .addSuperinterface(Injector.class)
                    .build();

            JavaFile javaFile = JavaFile.builder(packageName, build)
                    .build();

            javaFile.writeTo(processingEnv.getFiler());

            codeBuilder.addStatement("$N.put($T.class.getName(),$T.class)",
                    indexField,
                    instance,
                    className);
        }


        MethodSpec.Builder indexMethod = MethodSpec.methodBuilder("getIndex")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .returns(typeName)
                .addStatement("return $N", indexField);


        TypeSpec build = TypeSpec.classBuilder(indexName)
                .addModifiers(Modifier.FINAL, Modifier.PUBLIC)
                .addField(indexField)
                .addSuperinterface(InjectorIndex.class)
                .addMethod(indexMethod.build())
                .addStaticBlock(codeBuilder.build())
                .build();

        JavaFile javaFile = JavaFile.builder(pkgName, build)
                .build();


        javaFile.writeTo(processingEnv.getFiler());

    }

    private CustomInject getCustom(TypeElement type) {
        if (customInjects == null || customInjects.isEmpty()) return null;

        TypeMirror superclass = type.getSuperclass();
        TypeKind kind = superclass.getKind();
        while (kind == TypeKind.DECLARED) {
            DeclaredType declaredType = (DeclaredType) superclass;
            TypeElement element = (TypeElement) declaredType.asElement();

            String className = element.getQualifiedName().toString();
            for (CustomInject inject : customInjects) {
                if (inject.claName.equals(className)) return inject;
            }

            superclass = element.getSuperclass();
            kind = superclass.getKind();
        }
        return null;

    }
}