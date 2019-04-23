package com.rxhttp.compiler;

import com.squareup.javapoet.*;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.annotation.processing.Filer;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

public class RxHttpGenerator {

    static final String CLASSNAME   = "RxHttp";
    private static final String packageName = "rxhttp.wrapper.param";

    static ClassName RXHTTP = ClassName.get(packageName, CLASSNAME);

    private ParamsAnnotatedClass mParamsAnnotatedClass;
    private ParserAnnotatedClass mParserAnnotatedClass;
    private DomainAnnotatedClass mDomainAnnotatedClass;

    public void setParamsAnnotatedClass(ParamsAnnotatedClass paramsAnnotatedClass) {
        mParamsAnnotatedClass = paramsAnnotatedClass;
    }

    public void setDomainAnnotatedClass(DomainAnnotatedClass domainAnnotatedClass) {
        mDomainAnnotatedClass = domainAnnotatedClass;
    }

    public void setParserAnnotatedClass(ParserAnnotatedClass parserAnnotatedClass) {
        mParserAnnotatedClass = parserAnnotatedClass;
    }

    public void generateCode(Elements elementUtils, Filer filer) throws IOException {
        TypeElement superClassName = elementUtils.getTypeElement(packageName + ".Param");

        List<MethodSpec> methodList = new ArrayList<>(); //方法集合
        MethodSpec constructorMethod = MethodSpec.constructorBuilder()
                .addModifiers(Modifier.PRIVATE)
                .addParameter(TypeName.get(superClassName.asType()), "param")
                .addStatement("this.param = param")
                .build();
        methodList.add(constructorMethod); //添加构造方法

        MethodSpec.Builder method = MethodSpec.methodBuilder("getParam")
                .addModifiers(Modifier.PUBLIC)
                .addStatement("return param")
                .returns(TypeName.get(superClassName.asType()));
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("setParam")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(TypeName.get(superClassName.asType()), "param")
                .addStatement("this.param = param")
                .addStatement("return this")
                .returns(RxHttpGenerator.RXHTTP);
        methodList.add(method.build());

        WildcardTypeName subString = WildcardTypeName.subtypeOf(TypeName.get(String.class));
        WildcardTypeName subObject = WildcardTypeName.subtypeOf(TypeName.get(Object.class));
        TypeName mapName = ParameterizedTypeName.get(ClassName.get(Map.class), subString, subObject);

        method = MethodSpec.methodBuilder("add")
                .addModifiers(Modifier.PUBLIC)
                .addParameter(mapName, "map")
                .addStatement("param.add(map)")
                .addStatement("return this")
                .returns(RxHttpGenerator.RXHTTP);
        methodList.add(method.build());

        method = MethodSpec.methodBuilder("with")
                .addModifiers(Modifier.PUBLIC, Modifier.STATIC)
                .addParameter(TypeName.get(superClassName.asType()), "param")
                .addStatement("return new $L(param)", CLASSNAME)
                .returns(RxHttpGenerator.RXHTTP);
        methodList.add(method.build());

        methodList.addAll(mParamsAnnotatedClass.getMethodList());
        methodList.addAll(mDomainAnnotatedClass.getMethodList());
        methodList.addAll(mParserAnnotatedClass.getMethodList());

        TypeSpec typeSpec = TypeSpec
                .classBuilder(CLASSNAME)
                .addModifiers(Modifier.PUBLIC)
                .addField(TypeName.get(superClassName.asType()), "param", Modifier.PRIVATE)
                .addMethods(methodList)
                .build();

        // Write file
        JavaFile.builder(packageName, typeSpec).build().writeTo(filer);
    }
}
