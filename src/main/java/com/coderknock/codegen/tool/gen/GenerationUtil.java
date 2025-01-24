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
        try {
            var javaSource = Roaster.parse(JavaSource.class, javaCode);
            if (!javaSource.isEnum()) {
                return Result.fail(400, TranslationBundleKt.adaptedMessage("enum.extend.not.enum.error"));
            }

            JavaEnumSource enumSource = (JavaEnumSource) javaSource;
            // Add Arrays import
            if (!enumSource.hasImport(Arrays.class)) {
                enumSource.addImport(Arrays.class);
            }
            if (!enumSource.hasImport(EqualsField.class)) {
                enumSource.addImport(EqualsField.class);
            }

            List<MethodSource<JavaEnumSource>> methodSources = new ArrayList<>();
            FieldSource<JavaEnumSource> field = null;

            List<FieldSource<JavaEnumSource>> nonStaticFields = enumSource.getFields().stream()
                    .filter(f -> !f.isStatic())
                    .toList();

            if (CollUtil.isNotEmpty(nonStaticFields)) {
                // Find field marked with @EqualsField annotation (fix spelling: equalsFiled -> EqualsField)
                field = nonStaticFields.stream()
                        .filter(f -> f.hasAnnotation(EqualsField.class))
                        .findFirst()
                        .orElse(nonStaticFields.get(0));
            }

            String enumConstantParameterName = StrUtil.lowerFirst(enumSource.getName());
            String enumConstantFullQuote;
            String enumConstantFullQuoteFieldName;

            if (CollUtil.isEmpty(enumSource.getEnumConstants())) {
                return Result.success(javaSource.toString());
            }

            for (EnumConstantSource enumConstant : enumSource.getEnumConstants()) {
                String isXXXMethodName = "is" + StrUtil.upperFirst(StrUtil.toCamelCase(enumConstant.getName()).toLowerCase());
                MethodSource<JavaEnumSource> isXXXMethod = enumSource.getMethod(isXXXMethodName, enumSource.getName());
                String enumParameters = enumSource.getName() + " " + enumConstantParameterName;

                // Generate is method based on enum instance comparison
                if (Objects.isNull(isXXXMethod)) {
                    MethodSource<JavaEnumSource> methodSource = new MethodImpl<>(enumSource);
                    methodSource.getJavaDoc().setFullText(StrUtil.format(TranslationBundleKt.adaptedMessage("enum.extend.is_xxx.enum"), enumConstantFullQuote, enumConstantParameterName, enumConstant.getName(), enumConstant.getName()));
                    methodSource.setPublic().setStatic(true).setName(isXXXMethodName).setReturnType("boolean").setParameters(enumParameters).setBody(StrUtil.format("return {}.equals({});", enumConstant.getName(), enumConstantParameterName));
                    methodSources.add(methodSource);
                }

                // Generate isExist method for enum instance
                String isExistMethodName = "isExist";
                MethodSource<JavaEnumSource> isExistMethod = enumSource.getMethod(isExistMethodName, enumSource.getName());
                if (Objects.isNull(isExistMethod)) {
                    MethodSource<JavaEnumSource> methodSource = new MethodImpl<>(enumSource);
                    methodSource.getJavaDoc().setFullText(StrUtil.format(TranslationBundleKt.adaptedMessage("enum.extend.is_exist.doc"), enumSource.getName(), enumConstantParameterName, enumSource.getName(), enumConstantParameterName));
                    methodSource.setPublic().setStatic(true).setName(isExistMethodName).setReturnType("boolean").setParameters(enumParameters).setBody(StrUtil.format("return Arrays.stream({}.values()).anyMatch(streamValue -> streamValue.equals({}));", enumSource.getName(), enumConstantParameterName));
                    methodSources.add(methodSource);
                }

                // Generate is method based on field value comparison
                if (Objects.nonNull(field)) {
                    String fieldParameterName = StrUtil.lowerFirst(field.getName());
                    String fieldParameterType = field.getType().getName();
                    String fieldParameters = fieldParameterType + " " + fieldParameterName;
                    enumConstantFullQuote = enumSource.getName() + "#" + enumConstant.getName();
                    enumConstantFullQuoteFieldName = enumConstantFullQuote + "#" + fieldParameterName;

                    isXXXMethod = enumSource.getMethod(isXXXMethodName, fieldParameterType);
                    if (Objects.isNull(isXXXMethod)) {
                        MethodSource<JavaEnumSource> methodSource = new MethodImpl<>(enumSource);
                        String template = "return {}.equals({});";
                        if (field.getType().isPrimitive()) {
                            template = "return {} == {};";
                        }
                        methodSource.getJavaDoc().setFullText(StrUtil.format(TranslationBundleKt.adaptedMessage("enum.extend.is_xxx.value"), enumConstantFullQuoteFieldName, fieldParameterName, fieldParameterType, fieldParameterName, enumConstantFullQuoteFieldName, enumConstantFullQuoteFieldName));
                        methodSource.setPublic().setStatic(true).setName(isXXXMethodName).setReturnType("boolean").setParameters(fieldParameters).setBody(StrUtil.format(template, enumConstant.getName() + DOT + fieldParameterName, fieldParameterName));
                        methodSources.add(methodSource);
                    }

                    isExistMethod = enumSource.getMethod(isExistMethodName, fieldParameterType);
                    if (Objects.isNull(isExistMethod)) {
                        String template = "return Arrays.stream({}.values()).anyMatch(streamValue -> streamValue{}.equals({}));";
                        if (field.getType().isPrimitive()) {
                            template = "return Arrays.stream({}.values()).anyMatch(streamValue -> streamValue{} == {});";
                        }
                        MethodSource<JavaEnumSource> methodSource = new MethodImpl<>(enumSource);
                        methodSource.getJavaDoc().setFullText(StrUtil.format(TranslationBundleKt.adaptedMessage("enum.extend.is_exist.doc"), enumSource.getName(), fieldParameterName, fieldParameterType, fieldParameterName));
                        methodSource.setPublic().setStatic(true).setName(isExistMethodName).setReturnType("boolean").setParameters(fieldParameters).setBody(StrUtil.format(template, enumSource.getName(), DOT + fieldParameterName, fieldParameterName));
                        methodSources.add(methodSource);
                    }
                }
            }

            if (!methodSources.isEmpty()) {
                methodSources.sort(Comparator.comparing(MethodSource::getName));
                for (MethodSource<JavaEnumSource> source : methodSources) {
                    enumSource.addMethod(source);
                }
            }
            return Result.success(javaSource.toString());
        } catch (Exception e) {
            return Result.fail(500, TranslationBundleKt.adaptedMessage("enum.extend.parse.error", e.getMessage()));
        }
    }
}