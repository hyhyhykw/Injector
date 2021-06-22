package com.inject.compiler;

import com.inject.annotation.BindView;
import com.inject.annotation.BindViews;
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
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.ArrayType;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

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
        types.add(BindViews.class.getName());
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

    private Set<CustomInject> customInjects;

    private static final String REGEX_LIST_VIEWS = "java\\.util\\.List<(.+?)>";
    private static final String REGEX_COLLECTION_VIEWS = "java\\.util\\.Collection<(.+?)>";
    private static final String REGEX_ARRAYLIST_VIEWS = "java\\.util\\.ArrayList<(.+?)>";

    private static final String REGEX_LINKED_VIEWS = "java\\.util\\.LinkedList<(.+?)>";

    private static final String REGEX_ARRAY_VIEWS = "(.+?)\\[]";


    private static final Pattern patterListViews = Pattern.compile(REGEX_LIST_VIEWS);
    private static final Pattern patterCollectionViews = Pattern.compile(REGEX_COLLECTION_VIEWS);
    private static final Pattern patterArrayListViews = Pattern.compile(REGEX_ARRAYLIST_VIEWS);

    private static final Pattern patterLinkedListViews = Pattern.compile(REGEX_LINKED_VIEWS);

    private static final Pattern patterArrayViews = Pattern.compile(REGEX_ARRAY_VIEWS);

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (isProcessed) return false;
        isProcessed = true;
        customInjects = customInject();

        String pkgName = processingEnv.getOptions().get(OPTION_PKG_NAME);
        String indexName = processingEnv.getOptions().get(OPTION_INDEX);
        Elements elementUtils = processingEnv.getElementUtils();

        Map<String, JavaFileInfo> specs = new HashMap<>();

        //获取BindView注解的所有信息
        parseBindView(roundEnv, elementUtils, specs);

        //获取BindViews注解的所有信息
        parseBindViews(roundEnv, elementUtils, specs);

        //获取OnClick注解的所有信息
        parseOnClick(roundEnv, elementUtils, specs);

        try {
            createCode(specs, pkgName, indexName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    private ClassName getByFullName(String fullName, String packageName) {
        String substring = fullName.replace(packageName, "")
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
            instance = ClassName.get(packageName, substring);
        }

        return instance;
    }

    private void createCode(Map<String, JavaFileInfo> specs,
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

        for (JavaFileInfo value : specs.values()) {
            TypeElement type = value.type;
            CustomInject custom = getCustom(type);

            String claName = value.claName;
            String packageName = value.packageName;
            String qualifiedName = value.qualifiedName;

            ClassName instance = getByFullName(qualifiedName, packageName);

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

            Map<String, VariableElement> varMap = value.javaFileMap;
            Set<MethodInfo> methodMap = value.onClickMethodMap;
            Set<ViewsBindInfo> viewsList = value.viewsList;

            ClassName viewClick = ClassName.get("com.inject.injector", "ViewClick");

            injectBuilder.add("\n");

            HashMap<String, IdViewInfo> viewsMap = new HashMap<>();

            //BindView
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
            injectBuilder.add("\n");
            //BindViews
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
            }
            injectBuilder.add("\n");
            //onClick 方法
            for (MethodInfo info : methodMap) {
                List<String> ids = info.ids;
                ExecutableElement methodElement = info.methodElement;
                String methodName = methodElement.getSimpleName().toString();

                for (String viewId : ids) {
                    IdViewInfo idViewInfo = viewsMap.get(viewId);

                    if (idViewInfo != null) {
                        injectBuilder.addStatement("$T.setViewClick($N, $L, instance::$N)",
                                viewClick, idViewInfo.name, info.fast, methodName);
                    } else {
                        if (custom != null) {
                            if (isEmpty(custom.fieldName) && isEmpty(custom.methodName)) {
                                injectBuilder.addStatement("$T.setViewClick(instance.$L($T.id.$L), $L, instance::$N)",
                                        viewClick, custom.method, rCla, viewId, info.fast, methodName);
                            } else {
                                injectBuilder.addStatement("$T.setViewClick(view.findViewById($T.id.$L), $L, instance::$N)",
                                        viewClick, rCla, viewId, info.fast, methodName);
                            }
                        } else {
                            injectBuilder.addStatement("$T.setViewClick(instance.findViewById($T.id.$L),$L, instance::$N)",
                                    viewClick, rCla, viewId, info.fast, methodName);
                        }
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
                    .indent("    ")
                    .skipJavaLangImports(true)
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
                .indent("    ")
                .skipJavaLangImports(true)
                .build();


        javaFile.writeTo(processingEnv.getFiler());
    }

    private boolean isNotNeedCast(String qualifiedName, DeclaredType viewType) {
        TypeElement type = (TypeElement) viewType.asElement();
        if (qualifiedName.equals(type.getQualifiedName().toString())) {
            return true;
        }
        TypeMirror superclass = type.getSuperclass();
        TypeKind kind = superclass.getKind();
        while (kind == TypeKind.DECLARED) {
            DeclaredType declaredType = (DeclaredType) superclass;
            TypeElement element = (TypeElement) declaredType.asElement();
            String className = element.getQualifiedName().toString();
            if (qualifiedName.equals(className)) return true;

            superclass = element.getSuperclass();
            kind = superclass.getKind();
        }


        return false;
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

    //获取OnClick注解的所有信息
    private static void parseOnClick(RoundEnvironment roundEnv, Elements elementUtils, Map<String, JavaFileInfo> specs) {
        Set<? extends Element> onClicks = roundEnv.getElementsAnnotatedWith(OnClick.class);
        for (Element element : onClicks) {
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

            javaFileInfo.onClickMethodMap.add(new MethodInfo(ids, fast, executableElement));
        }
    }

    //获取BindViews注解的所有信息
    private static void parseBindViews(RoundEnvironment roundEnv, Elements elementUtils, Map<String, JavaFileInfo> specs) {
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

    //获取BindView注解的所有信息
    private static void parseBindView(RoundEnvironment roundEnv, Elements elementUtils, Map<String, JavaFileInfo> specs) {
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
            javaFileInfo.javaFileMap.put(id, variableElement);
        }
    }

}