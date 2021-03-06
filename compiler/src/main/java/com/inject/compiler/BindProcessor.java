package com.inject.compiler;

import com.inject.annotation.BindAnim;
import com.inject.annotation.BindArray;
import com.inject.annotation.BindColor;
import com.inject.annotation.BindDrawable;
import com.inject.annotation.BindString;
import com.inject.annotation.BindView;
import com.inject.annotation.BindViews;
import com.inject.annotation.Dp;
import com.inject.annotation.OnCheckedChanged;
import com.inject.annotation.OnClick;
import com.inject.annotation.OnLongClick;
import com.inject.annotation.OnPageChange;
import com.inject.annotation.OnTextChanged;
import com.inject.annotation.OnTouch;
import com.inject.annotation.Sp;
import com.inject.compiler.binder.AnimBinder;
import com.inject.compiler.binder.ArrayBinder;
import com.inject.compiler.binder.ColorBinder;
import com.inject.compiler.binder.DpBinder;
import com.inject.compiler.binder.DrawableBinder;
import com.inject.compiler.binder.OnCheckedChangeBinder;
import com.inject.compiler.binder.OnClickBinder;
import com.inject.compiler.binder.OnLongClickBinder;
import com.inject.compiler.binder.OnPageChangeBinder;
import com.inject.compiler.binder.OnTextChangeBinder;
import com.inject.compiler.binder.OnTouchBinder;
import com.inject.compiler.binder.SpBinder;
import com.inject.compiler.binder.StringBinder;
import com.inject.compiler.binder.ViewBinder;
import com.inject.compiler.binder.ViewsBinder;
import com.inject.compiler.entity.ArrayInfo;
import com.inject.compiler.entity.ContextInject;
import com.inject.compiler.entity.CustomInject;
import com.inject.compiler.entity.DpInfo;
import com.inject.compiler.entity.IdEntity;
import com.inject.compiler.entity.IdViewInfo;
import com.inject.compiler.entity.JavaFileInfo;
import com.inject.compiler.entity.PageChangeInfo;
import com.inject.compiler.entity.SingleMethodInfo;
import com.inject.compiler.entity.TextChangeInfo;
import com.inject.compiler.entity.ViewsBindInfo;
import com.inject.index.Injector;
import com.inject.index.InjectorIndex;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.CodeBlock;
import com.squareup.javapoet.FieldSpec;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.ParameterizedTypeName;
import com.squareup.javapoet.TypeName;
import com.squareup.javapoet.TypeSpec;
import com.squareup.javapoet.WildcardTypeName;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedOptions;
import javax.annotation.processing.SupportedSourceVersion;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import static com.inject.compiler.Common.getByFullName;
import static com.inject.compiler.Common.isEmpty;
import static com.inject.compiler.Common.isNotNeedCast;

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

    public static final String OPTION_CONTEXT_INJECT = "contextInject";
    public static final String OPTION_CONTEXT_INJECT_FIELD = "contextInjectField";
    public static final String OPTION_CONTEXT_INJECT_METHOD = "contextInjectMethod";

    public static final String OPTION_CUSTOM_INJECT_VIEW_FIELD = "injectViewField";
    public static final String OPTION_CUSTOM_INJECT_VIEW_METHOD = "injectViewMethod";

    //??????????????????????????????
    @Override
    public Set<String> getSupportedOptions() {
        Set<String> objects = new HashSet<>(super.getSupportedOptions());
        objects.add(OPTION_CUSTOM_INJECT);
        objects.add(OPTION_CUSTOM_INJECT_METHOD);
        objects.add(OPTION_CUSTOM_INJECT_VIEW_FIELD);
        objects.add(OPTION_CUSTOM_INJECT_VIEW_METHOD);

        objects.add(OPTION_CONTEXT_INJECT);
        objects.add(OPTION_CONTEXT_INJECT_FIELD);
        objects.add(OPTION_CONTEXT_INJECT_METHOD);

        for (int i = 1; i <= 10; i++) {
            objects.add(OPTION_CUSTOM_INJECT + i);
            objects.add(OPTION_CUSTOM_INJECT_METHOD + i);
            objects.add(OPTION_CUSTOM_INJECT_VIEW_FIELD + i);
            objects.add(OPTION_CUSTOM_INJECT_VIEW_METHOD + i);

            objects.add(OPTION_CONTEXT_INJECT + i);
            objects.add(OPTION_CONTEXT_INJECT_FIELD + i);
            objects.add(OPTION_CONTEXT_INJECT_METHOD + i);
        }

        return objects;
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new HashSet<>();
        types.add(BindAnim.class.getName());
        types.add(BindDrawable.class.getName());
        types.add(BindString.class.getName());
        types.add(BindColor.class.getName());
        types.add(BindArray.class.getName());

        types.add(Dp.class.getName());
        types.add(Sp.class.getName());

        types.add(BindView.class.getName());
        types.add(OnClick.class.getName());
        types.add(BindViews.class.getName());
        types.add(OnPageChange.class.getName());
        types.add(OnTextChanged.class.getName());
        types.add(OnLongClick.class.getName());
        types.add(OnCheckedChanged.class.getName());
        types.add(OnTouch.class.getName());

        return types;
    }

    private boolean isProcessed = false;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        customInjects = null;
        contextInjects = null;
        isProcessed = false;
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
    //?????????context?????? ??????context?????????
    private Set<ContextInject> contextInjects;

    private Set<ContextInject> contextInject() {
        String inject = processingEnv.getOptions().get(OPTION_CONTEXT_INJECT);

        Set<ContextInject> contextInjects = new HashSet<>();
        if (!isEmpty(inject)) {
            String contextField = processingEnv.getOptions().get(OPTION_CONTEXT_INJECT_FIELD);
            String contextMethod = processingEnv.getOptions().get(OPTION_CONTEXT_INJECT_METHOD);
            if (!isEmpty(contextField) || !isEmpty(contextMethod)) {
                ContextInject contextInject = new ContextInject(inject, contextMethod, contextField);
                contextInjects.add(contextInject);
            }
        }
        for (int i = 1; i <= 10; i++) {
            String injectI = processingEnv.getOptions().get(OPTION_CONTEXT_INJECT + i);
            if (isEmpty(injectI)) continue;

            String contextMethodI = processingEnv.getOptions().get(OPTION_CONTEXT_INJECT_METHOD + i);
            String contextFieldI = processingEnv.getOptions().get(OPTION_CONTEXT_INJECT_FIELD + i);

            if (isEmpty(contextFieldI) && isEmpty(contextMethodI)) continue;

            ContextInject contextInject = new ContextInject(injectI, contextMethodI, contextFieldI);
            contextInjects.add(contextInject);
        }

        return contextInjects;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (isProcessed) return false;
        isProcessed = true;
        contextInjects = contextInject();
        customInjects = customInject();

        String pkgName = processingEnv.getOptions().get(OPTION_PKG_NAME);
        String indexName = processingEnv.getOptions().get(OPTION_INDEX);
        Elements elementUtils = processingEnv.getElementUtils();

        Map<String, JavaFileInfo> specs = new HashMap<>();

        //??????Dp?????????????????????
        DpBinder.parseAnnotation(roundEnv, elementUtils, specs);

        //??????Sp?????????????????????
        SpBinder.parseAnnotation(roundEnv, elementUtils, specs);

        //??????BindView?????????????????????
        ViewBinder.parseAnnotation(roundEnv, elementUtils, specs);

        //??????BindViews?????????????????????
        ViewsBinder.parseAnnotation(roundEnv, elementUtils, specs);

        //??????OnClick?????????????????????
        OnClickBinder.parseAnnotation(roundEnv, elementUtils, specs);

        //??????OnTouch?????????????????????
        OnTouchBinder.parseAnnotation(roundEnv, elementUtils, specs);

        //??????OnLongClick?????????????????????
        OnLongClickBinder.parseAnnotation(roundEnv, elementUtils, specs);

        //??????OnCheckChange??????
        OnCheckedChangeBinder.parseAnnotation(roundEnv, elementUtils, specs);

        //??????OnPageChange?????????????????????
        OnPageChangeBinder.parseAnnotation(roundEnv, elementUtils, specs);

        //??????OnTextChange?????????????????????
        OnTextChangeBinder.parseAnnotation(roundEnv, elementUtils, specs);

        //??????BindAnim?????????????????????
        AnimBinder.parseAnnotation(roundEnv, elementUtils, specs);

        //??????BindDrawable?????????????????????
        DrawableBinder.parseAnnotation(roundEnv, elementUtils, specs);

        //??????BindColor?????????????????????
        ColorBinder.parseAnnotation(roundEnv, elementUtils, specs);

        //??????BindString?????????????????????
        StringBinder.parseAnnotation(roundEnv, elementUtils, specs);

        //??????BindArray?????????????????????
        ArrayBinder.parseAnnotation(roundEnv, elementUtils, specs);

        try {
            createCode(specs, pkgName, indexName);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return true;
    }

    //??????????????????????????????????????????
    private void createCode(Map<String, JavaFileInfo> specs, String pkgName, String indexName) throws Exception {
        ClassName rCla = ClassName.get(pkgName, "R");
        WildcardTypeName variableName = WildcardTypeName.subtypeOf(Injector.class);
        ParameterizedTypeName parameterizedTypeName = ParameterizedTypeName.get(ClassName.get(Class.class), variableName);

        ParameterizedTypeName typeName = ParameterizedTypeName.get(
                ClassName.get(Map.class),
                TypeName.get(String.class), parameterizedTypeName);

        FieldSpec.Builder indexMap = FieldSpec.builder(typeName, "INDEX", Modifier.STATIC, Modifier.PRIVATE, Modifier.FINAL)
                .initializer("new $T<>()", HashMap.class)
                .addJavadoc("The injector index");
        FieldSpec indexField = indexMap.build();

        CodeBlock.Builder codeBuilder = CodeBlock.builder();


        SimpleDateFormat format = (SimpleDateFormat) SimpleDateFormat.getInstance();
        format.applyPattern("yyyy/MM/dd hh:mm");
        String dateTime = format.format(new Date());


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

            Map<IdEntity, VariableElement> varMap = value.viewIdMap;
            Map<IdEntity, VariableElement> animMap = value.animMap;
            Map<IdEntity, VariableElement> drawableMap = value.drawableMap;
            Map<IdEntity, VariableElement> colorMap = value.colorMap;
            Map<IdEntity, VariableElement> stringMap = value.stringMap;

            Set<DpInfo> dpInfo = value.dpInfo;
            Set<DpInfo> spInfo = value.spInfo;

            Set<SingleMethodInfo> methodMap = value.onClickMethodMap;
            Set<SingleMethodInfo> onTouchMap = value.onTouchMap;
            Set<ViewsBindInfo> viewsList = value.viewsList;
            Set<ArrayInfo> arrayInfo = value.arrayInfo;
            Set<PageChangeInfo> pageChangeInfo = value.pageChangeInfo;
            Set<TextChangeInfo> textChangeInfo = value.textChangeInfo;
            Set<SingleMethodInfo> longClickMethodMap = value.longClickMethodMap;
            Set<SingleMethodInfo> checkedChangedMethodMap = value.checkedChangedMethodMap;


            ClassName viewClick = ClassName.get("com.inject.injector", "ViewClick");
            HashMap<IdEntity, IdViewInfo> viewsMap = new HashMap<>();

            //BindView
            ViewBinder.createCode(rCla, custom, injectBuilder, varMap, viewsMap);

            //??????????????????context??????????????????????????????context??????
            if (!animMap.isEmpty() || !arrayInfo.isEmpty() ||
                    !drawableMap.isEmpty() || !colorMap.isEmpty() ||
                    !stringMap.isEmpty() || !dpInfo.isEmpty() || !spInfo.isEmpty()) {
                createContextField(type, qualifiedName, injectBuilder);
            }

            boolean generateRes = false;
            //
            ClassName resources = ClassName.get("android.content.res", "Resources");
            ClassName metrics = ClassName.get("android.util", "DisplayMetrics");
            if (!dpInfo.isEmpty()) {
                generateRes = true;
                injectBuilder.add("/**\n * get resources from {@link $L}\n */\n", qualifiedName);
                injectBuilder.addStatement("$T resources = context.getResources()", resources);
                injectBuilder.addStatement("$T metrics = resources.getDisplayMetrics()", metrics);
            }
            //Dp
            DpBinder.createCode(injectBuilder, dpInfo);
            if (!generateRes && !spInfo.isEmpty()) {
                generateRes = true;
                injectBuilder.add("/**\n * get resources from {@link $L}\n */\n", qualifiedName);
                injectBuilder.addStatement("$T resources = context.getResources()", resources);
                injectBuilder.addStatement("$T metrics = resources.getDisplayMetrics()", metrics);
            }
            //Sp
            SpBinder.createCode(injectBuilder, spInfo);

            //BindAnim
            AnimBinder.createCode(rCla, injectBuilder, animMap);

            //BindDrawable
            DrawableBinder.createCode(rCla, injectBuilder, drawableMap);

            //BindColor
            ColorBinder.createCode(rCla, injectBuilder, colorMap);

            //BindString
            StringBinder.createCode(rCla, injectBuilder, stringMap);

            //BindArray
            ArrayBinder.createCode(rCla, injectBuilder, arrayInfo, generateRes);

            //BindViews
            ViewsBinder.createCode(rCla, custom, injectBuilder, viewsList, viewsMap);

            //onClick ??????
            OnClickBinder.createCode(rCla, custom, injectBuilder, methodMap, viewClick, viewsMap);

            //OnTouch??????
            OnTouchBinder.createCode(rCla, custom, injectBuilder, onTouchMap, viewClick, viewsMap);

            //onLongClick ??????
            OnLongClickBinder.createCode(rCla, custom, injectBuilder, longClickMethodMap, viewClick, viewsMap);

            //OnCheckChange
            OnCheckedChangeBinder.createCode(rCla, custom, injectBuilder, checkedChangedMethodMap, viewClick, viewsMap);

            //OnPageChange
            OnPageChangeBinder.createCode(rCla, custom, injectBuilder, pageChangeInfo, viewsMap);

            //OnTextChange
            OnTextChangeBinder.createCode(rCla, custom, injectBuilder, textChangeInfo, viewsMap);

            MethodSpec.Builder inject = MethodSpec.methodBuilder("inject")
                    .addParameter(Object.class, "object")
                    .addAnnotation(Override.class)
                    .addModifiers(Modifier.PUBLIC)
                    .addCode(injectBuilder.build());

            ClassName className = ClassName.get(packageName, claName + "Injector");

            TypeSpec build = TypeSpec.classBuilder(className)
                    .addModifiers(Modifier.FINAL, Modifier.PUBLIC)
                    .addJavadoc(
                            "Created time : " + dateTime + ".\nThe injector of " + claName +
                                    " \n\n@author James\n" +
                                    "@see com.inject.index.Injector\n" +
                                    "@see " + instance.toString()
                    )
                    .addMethod(inject.build())
                    .addSuperinterface(Injector.class)
                    .build();

            JavaFile javaFile = JavaFile.builder(packageName, build)
                    .indent("    ")
                    .skipJavaLangImports(true)
                    .build();

            javaFile.writeTo(processingEnv.getFiler());

            codeBuilder.addStatement("$N.put($T.class.getName(), $T.class)",
                    indexField,
                    instance,
                    className);
        }

        MethodSpec.Builder indexMethod = MethodSpec.methodBuilder("getIndex")
                .addAnnotation(Override.class)
                .addModifiers(Modifier.PUBLIC)
                .addJavadoc(
                        "The index of injector class name and the injector\n" +
                                "\n" +
                                "@return injector index"
                )
                .returns(typeName)
                .addStatement("return $N", indexField);


        TypeSpec build = TypeSpec.classBuilder(indexName)
                .addModifiers(Modifier.FINAL, Modifier.PUBLIC)
                .addJavadoc(
                        "Created time : " + dateTime + ".\nAuto Generate Injector Index\n\n@author James\n@see com.inject.index.Injector\n@see com.inject.index.InjectorIndex"
                )
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

    private void createContextField(TypeElement type, String qualifiedName, CodeBlock.Builder injectBuilder) {
        ContextInject contextInject = getContextCustom(type);
        boolean isContext = isNotNeedCast("android.content.Context", (DeclaredType) type.asType());

        injectBuilder.add("/**\n * get context from {@link $L}\n */\n", qualifiedName);
        ClassName contextCla = ClassName.get("android.content", "Context");
        if (contextInject != null) {
            if (!isContext) {
                if (!isEmpty(contextInject.field)) {
                    injectBuilder.addStatement("$T context = instance.$N", contextCla, contextInject.field);
                } else if (!isEmpty(contextInject.method)) {
                    injectBuilder.addStatement("$T context = instance.$N()", contextCla, contextInject.method);
                } else {
                    injectBuilder.addStatement("$T context = ($T) instance", contextCla, contextCla);
                }
            } else {
                injectBuilder.addStatement("$T context = instance", contextCla);
            }
            injectBuilder.add("\n");
        } else {
            if (isContext) {
                injectBuilder.addStatement("$T context = instance", contextCla);
                injectBuilder.add("\n");
            }
        }
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

    private ContextInject getContextCustom(TypeElement type) {
        if (contextInjects == null || contextInjects.isEmpty()) return null;

        TypeMirror superclass = type.getSuperclass();
        TypeKind kind = superclass.getKind();
        while (kind == TypeKind.DECLARED) {
            DeclaredType declaredType = (DeclaredType) superclass;
            TypeElement element = (TypeElement) declaredType.asElement();

            String className = element.getQualifiedName().toString();
            for (ContextInject inject : contextInjects) {
                if (inject.claName.equals(className)) return inject;
            }

            superclass = element.getSuperclass();
            kind = superclass.getKind();
        }
        return null;
    }

}