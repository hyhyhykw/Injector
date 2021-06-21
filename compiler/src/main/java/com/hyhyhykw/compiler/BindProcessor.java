package com.hyhyhykw.compiler;

import com.hyhyhykw.annotation.BindView;
import com.hyhyhykw.annotation.Injector;
import com.hyhyhykw.annotation.InjectorIndex;
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
import javax.lang.model.element.Element;
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

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        Set<String> types = new HashSet<>();
        types.add(BindView.class.getName());
        return types;
    }

    public static final class VarInfo {
        public final String qualifiedName;
        public final String packageName;
        public final String claName;
        public final TypeElement type;
        public final HashMap<String, VariableElement> varMap = new HashMap<>();

        public VarInfo(String qualifiedName, String packageName, String claName, TypeElement type) {
            this.qualifiedName = qualifiedName;
            this.packageName = packageName;
            this.claName = claName;
            this.type = type;
        }
    }


    private boolean isProcessed = false;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        isProcessed = false;
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (isProcessed) return false;
        isProcessed = true;
        String pkgName = processingEnv.getOptions().get(OPTION_PKG_NAME);
        String indexName = processingEnv.getOptions().get(OPTION_INDEX);

        Set<? extends Element> elements = roundEnv.getElementsAnnotatedWith(BindView.class);

        Map<String, VarInfo> specs = new HashMap<>();

        for (Element element : elements) {
            if (!(element instanceof VariableElement)) continue;
            VariableElement variableElement = (VariableElement) element;
            //类信息
            TypeElement typeElement = (TypeElement) variableElement.getEnclosingElement();
            /*类的绝对路径*/
            String qualifiedName = typeElement.getQualifiedName().toString();

            System.out.println("qualifiedName=====>" + qualifiedName);
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
            boolean isUseView = isUseView(type);

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
            if (isUseView) {
                ClassName view = ClassName.get("android.view", "View");
                injectBuilder.addStatement("$T view = ($T) viewObj", view, view);
            }

            for (Map.Entry<String, VariableElement> entry : value.varMap.entrySet()) {
                String key = entry.getKey();
                VariableElement entryValue = entry.getValue();

                String name = entryValue.getSimpleName().toString();

                if (isUseView) {
                    injectBuilder.addStatement("instance.$N = view.findViewById($T.id.$L)",
                            name, rCla, key);
                } else {
                    injectBuilder.addStatement("instance.$N = instance.findViewById($T.id.$L)",
                            name, rCla, key);
                }
            }

            ParameterSpec.Builder param = ParameterSpec.builder(Object.class, "object");

            MethodSpec.Builder inject = MethodSpec.methodBuilder("inject")
                    .addParameter(param.build())
                    .addParameter(ParameterSpec.builder(Object.class, "viewObj").build())
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

    private boolean isUseView(TypeElement type) {
        TypeMirror superclass = type.getSuperclass();
        TypeKind kind = superclass.getKind();
        while (kind == TypeKind.DECLARED) {
            DeclaredType declaredType = (DeclaredType) superclass;
            TypeElement element = (TypeElement) declaredType.asElement();

            String className = element.getQualifiedName().toString();
            if (className.equals("androidx.fragment.app.Fragment")) {
                return true;
            }
            if (className.equals("android.support.v4.app.Fragment")) {
                return true;
            }

            System.out.println("className======>" + className);
            if (className.equals("androidx.recyclerview.widget.RecyclerView.ViewHolder")) {
                return true;
            }

            superclass = element.getSuperclass();
            kind = superclass.getKind();
        }
        return false;
    }
}