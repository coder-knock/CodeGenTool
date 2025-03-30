package com.coderknock.codegen.tool.gen;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.coderknock.codegen.tool.bundle.TranslationBundleKt;
import com.coderknock.codegen.tool.domin.Result;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.impl.MethodImpl;
import org.jboss.forge.roaster.model.source.*;

import java.util.*;

import static cn.hutool.core.text.StrPool.DOT;

public interface GenerationUtil {

    static Result<String> enumIsXXX(String javaCode) {
        var javaSource = Roaster.parse(JavaSource.class, javaCode);
        if (javaSource.isEnum()) {
            JavaEnumSource enumSource = (JavaEnumSource) javaSource;
            FieldSource<JavaEnumSource> field = null;
            // 引入 Arrays 引用
            enumSource.addImport(Arrays.class);
            for (FieldSource<JavaEnumSource> enumSourceField : enumSource.getFields()) {
                // 枚举中字段必须是 final 得
                if (!enumSourceField.isStatic()) {
                    enumSourceField.setFinal(true);
                }
            }
            List<MethodSource<JavaEnumSource>> methodSources = new ArrayList<>();
            MethodSource<JavaEnumSource> methodSource;
            Set<String> methodNames = new HashSet<>();
            if (CollUtil.isNotEmpty(enumSource.getFields())) {
                field = enumSource.getFields().stream().filter(f -> f.hasAnnotation("equalsFiled")).findFirst().orElseGet(() -> enumSource.getFields().get(0));
            }
            String enumConstantParameterName = StrUtil.lowerFirst(enumSource.getName());
            String enumConstantFullQuote;
            String enumConstantFullQuoteFieldName;

            for (EnumConstantSource enumConstant : enumSource.getEnumConstants()) {
                enumConstantFullQuote = enumSource.getName() + "#" + enumConstant.getName();
                String isExistMethodName = "isExist";
                String fieldParameterName;
                String fieldParameterType;
                String fieldParameters;
                if (Objects.isNull(field)) {
                    fieldParameterName = null;
                    fieldParameterType = null;
                    fieldParameters = null;
                    enumConstantFullQuoteFieldName = enumConstantFullQuote;
                } else {
                    fieldParameterName = StrUtil.lowerFirst(field.getName());
                    fieldParameterType = field.getType().getName();
                    fieldParameters = fieldParameterType + " " + fieldParameterName;
                    enumConstantFullQuoteFieldName = enumConstantFullQuote + "#" + fieldParameterName;
                }
                String isXXXMethodName = "is" + StrUtil.upperFirst(StrUtil.toCamelCase(enumConstant.getName()).toLowerCase());
                MethodSource<JavaEnumSource> isXXXMethod = enumSource.getMethod(isXXXMethodName, enumSource.getName());
                String enumParameters = enumSource.getName() + " " + enumConstantParameterName;
                // 生成基于枚举自身的 is 方法
                if (Objects.isNull(isXXXMethod)) {
                    methodSource = new MethodImpl<>(enumSource);
                    methodSource.getJavaDoc().setFullText(StrUtil.format(TranslationBundleKt.adaptedMessage("enum.extend.is_xxx.enum"), enumConstantFullQuote, enumConstantParameterName, enumConstant.getName(), enumConstant.getName()));
                    methodSource.setPublic().setStatic(true).setName(isXXXMethodName).setReturnType("boolean").setParameters(enumParameters).setBody(StrUtil.format("return {}.equals({});", enumConstant.getName(), enumConstantParameterName));
                    addMethod(isXXXMethodName, enumConstantParameterName, methodNames, methodSources, methodSource);

                }

                MethodSource<JavaEnumSource> isExistMethod = enumSource.getMethod(isExistMethodName, enumSource.getName());
                if (Objects.isNull(isExistMethod)) {
                    methodSource = new MethodImpl<>(enumSource);
                    methodSource.getJavaDoc().setFullText(StrUtil.format(TranslationBundleKt.adaptedMessage("enum.extend.is_exist.doc"), enumSource.getName(), enumConstantParameterName, enumSource.getName(), enumConstantParameterName));
                    methodSource.setPublic().setStatic(true).setName(isExistMethodName).setReturnType("boolean")
                            .setParameters(enumParameters).
                            setBody(StrUtil.format("return Arrays.stream({}.values()).anyMatch(streamValue->streamValue.equals({}));", enumSource.getName(), enumConstantParameterName));
                    addMethod(isExistMethodName, enumConstantParameterName, methodNames, methodSources, methodSource);
                }
                // 生成基于第一个字段的 is 方法
                if (Objects.nonNull(field)) {
                    isXXXMethod = enumSource.getMethod(isXXXMethodName, fieldParameterType);
                    if (Objects.isNull(isXXXMethod)) {
                        methodSource = new MethodImpl<>(enumSource);
                        String template = "return {}.equals({});";
                        if (field.getType().isPrimitive()) {
                            template = "return {}=={};";
                        }
                        methodSource.getJavaDoc().setFullText(StrUtil.format(TranslationBundleKt.adaptedMessage("enum.extend.is_xxx.value"), enumConstantFullQuoteFieldName, fieldParameterName, fieldParameterType, fieldParameterName, enumConstantFullQuoteFieldName, enumConstantFullQuoteFieldName));
                        methodSource.setPublic().setStatic(true).setName(isXXXMethodName).setReturnType("boolean").setParameters(fieldParameters).setBody(StrUtil.format(template, enumConstant.getName() + DOT + fieldParameterName, fieldParameterName));
                        addMethod(isXXXMethodName, fieldParameters, methodNames, methodSources, methodSource);
                    }
                }
                if (Objects.nonNull(field)) {
                    isExistMethod = enumSource.getMethod(isExistMethodName, fieldParameterType);
                    if (Objects.isNull(isExistMethod)) {
                        String template = "return Arrays.stream({}.values()).anyMatch(streamValue->streamValue{}.equals({}));";
                        if (field.getType().isPrimitive()) {
                            template = "return Arrays.stream({}.values()).anyMatch(streamValue->streamValue{} == {});";
                        }
                        methodSource = new MethodImpl<>(enumSource);
                        methodSource.getJavaDoc().setFullText(StrUtil.format(TranslationBundleKt.adaptedMessage("enum.extend.is_exist.doc"), enumSource.getName(), fieldParameterName, fieldParameterType, fieldParameterName));
                        methodSource.setPublic().setStatic(true).setName(isExistMethodName).setReturnType("boolean").setParameters(fieldParameters).setBody(StrUtil.format(template, enumSource.getName(), DOT + fieldParameterName, fieldParameterName));
                        addMethod(isExistMethodName, fieldParameters, methodNames, methodSources, methodSource);
                    }
                }
            }

            if (!methodSources.isEmpty()) {
                methodSources.sort(Comparator.comparing(MethodSource::getName));
                for (MethodSource<JavaEnumSource> source : methodSources) {
                    enumSource.addMethod(source);
                }
            }
            // Idea z
            return Result.success(javaSource.toString().replaceAll("\\r\\n|\\r|\\n", "\n"));
        } else {
            return Result.fail(400, "The selected code is not of enumeration type.");
        }
    }

    private static void addMethod(String methodName, String parameterName, Set<String> methodNames, List<MethodSource<JavaEnumSource>> methodSources, MethodSource<JavaEnumSource> methodSource) {
        String uniName = methodName + "#" + parameterName;
        if (!methodNames.contains(uniName)) {
            methodSources.add(methodSource);
            methodNames.add(uniName);
        }
    }
}