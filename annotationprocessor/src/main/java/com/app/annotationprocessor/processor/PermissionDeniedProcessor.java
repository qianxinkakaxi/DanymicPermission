package com.app.annotationprocessor.processor;

import com.app.annotationprocessor.annotation.DynamicPermission;
import com.app.annotationprocessor.annotation.PermissionDenied;
import com.google.auto.service.AutoService;
import com.squareup.javapoet.ClassName;
import com.squareup.javapoet.JavaFile;
import com.squareup.javapoet.MethodSpec;
import com.squareup.javapoet.TypeSpec;


import java.io.IOException;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.Processor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.Diagnostic;


@AutoService(Processor.class)
public class PermissionDeniedProcessor extends AbstractProcessor {

    private Messager mMessager;
    private Elements mElementUtils;

    private Filer mFiler;
    private Types mTypeUtils;

    @Override
    public synchronized void init(ProcessingEnvironment processingEnv) {
        super.init(processingEnv);
        mMessager = processingEnv.getMessager();
        mElementUtils = processingEnv.getElementUtils();
        mFiler = processingEnv.getFiler();
        mTypeUtils = processingEnv.getTypeUtils();
    }

    @Override
    public Set<String> getSupportedAnnotationTypes() {
        HashSet<String> supportTypes = new LinkedHashSet<>();
        supportTypes.add(DynamicPermission.class.getCanonicalName());
        return supportTypes;
    }

    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latestSupported();
    }

    @Override
    public boolean process(Set<? extends TypeElement> set, RoundEnvironment roundEnvironment) {

        System.out.print("set === " + DynamicPermission.class.getClasses().length + ">>>>>>>>>>>>>>>>>>>>>>>");
        mMessager.printMessage(Diagnostic.Kind.NOTE, "processing...");
        Set<? extends Element> elements = roundEnvironment.getElementsAnnotatedWith(DynamicPermission.class);

        for (Element element : elements) {
            List<Element> elementList = findMethods(element, PermissionDenied.class);
            if (elementList.size() != 0) {
                try {
                    analysisAnnotated(element, elementList);
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }


        return true;
    }


    private void analysisAnnotated(Element classElement, List<Element> elementList) throws IOException {

        PackageElement packageElement = mElementUtils.getPackageOf(classElement);


        String packageName = packageElement.getQualifiedName().toString();
//        String className = classElement.getSimpleName().toString();
//        VariableElement variableElement = (VariableElement) packageElement;
        //   TypeElement typeElement = (TypeElement) classElement.getEnclosingElement();


//        System.out.print("packageName === " + packageName + ">>>>>>>>>>>>>>>>>>>>>>> className = " + className);

//        String qualifiedName = classElement.toString();

        String clsName = classElement.getSimpleName().toString();

//        System.out.print("qualifiedName === " + qualifiedName + ">>>>>>>>>>>>>>>>>>>>>>> clsName = " + clsName);

//        MethodSpec main = MethodSpec.methodBuilder("main")
//                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
//                .returns(void.class)
//                .addStatement("$T.out.println($S)", System.class, "Hello, JavaPoet!")
//                .build();


        ClassName namedBoards = ClassName.get(packageName, clsName);


//        MethodSpec flux = MethodSpec.methodBuilder("bind")
//                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
//                .addParameter(namedBoards, "activity")
//                .addStatement("this.$N = $N", "activity", "activity")
//                .build();


        MethodSpec.Builder permissionDenied = MethodSpec.methodBuilder("permissionDenied")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(namedBoards, "activity")
                .addParameter(String[].class, "permissions")
                .addParameter(int[].class, "grantResults")
                .returns(void.class)
                .beginControlFlow("for (int i = 0; i < permissions.length; i++)");
        for (int j = 0; j < elementList.size(); j++) {
            Element element = elementList.get(j);
            PermissionDenied annotation = element.getAnnotation(PermissionDenied.class);
            String[] values = annotation.value();

            String className = element.getSimpleName().toString();

            String condition = "";
            for (int i = 0; i < values.length; i++) {
                if (i == 0) {
                    permissionDenied.beginControlFlow("if(permissions[i].equals(\"$N\") && grantResults[i] != 0)", values[i])
                            .addStatement("activity.$N()", className)
                            .endControlFlow();
                } else {
                    permissionDenied.beginControlFlow("else if(permissions[i].equals(\"$N\") && grantResults[i] != 0)", values[i])
                            .addStatement("activity.$N()", className)
                            .endControlFlow();
                }

            }

        }
        permissionDenied
                .endControlFlow()
                .build();


        TypeSpec helloWorld = TypeSpec.classBuilder(clsName + "_AutoGenerate" + "")
                .addModifiers(Modifier.PUBLIC, Modifier.FINAL)
                .addMethod(permissionDenied.build())
                .build();

        JavaFile javaFile = JavaFile.builder(packageName, helloWorld)
                .build();
        javaFile.writeTo(mFiler);

        //  File file = new File("../asdsad/HELLOWORLD.JAVA");

//        mMessager.printMessage(Diagnostic.Kind.NOTE, System.getProperty("user.dir"));
//        File f2 = new File(System.getProperty("user.dir") + "/aoplibrary/src/main/java");
//        if (!f2.exists()) {
//            f2.mkdir();
//        }
//        javaFile.writeTo(f2);
//        }
//        try {
//            javaFile.writeTo(System.out);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }

    }


    public List<Element> findMethods(Element element, Class<? extends Annotation> clazz) {
        List<Element> methods = new ArrayList<>();
        for (Element enclosedElement : element.getEnclosedElements()) {
            Annotation annotation = enclosedElement.getAnnotation(clazz);
            if (annotation != null) {
                methods.add(enclosedElement);
            }
        }
        return methods;
    }
}
