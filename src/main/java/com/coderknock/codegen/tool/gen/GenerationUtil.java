package com.coderknock.codegen.tool.gen;

import cn.hutool.core.collection.CollUtil;
import cn.hutool.core.util.StrUtil;
import com.coderknock.codegen.tool.domin.Result;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.*;

import java.util.Objects;

public interface GenerationUtil {

    static Result<String> enumIsXXX(String javaCode) {
        JavaSource javaSource = Roaster.parse(JavaSource.class, javaCode);
        if (javaSource.isEnum()) {
            JavaEnumSource enumSource = (JavaEnumSource) javaSource;
            FieldSource<JavaEnumSource> field = null;
            for (FieldSource<JavaEnumSource> enumSourceField : enumSource.getFields()) {
                // 枚举中字段必须是 final 得
                if (!enumSourceField.isStatic()) {
                    enumSourceField.setFinal(true);
                }
            }

            if (CollUtil.isNotEmpty(enumSource.getFields())) {
                field = enumSource.getFields().stream().filter(f -> f.hasAnnotation("equalsFiled")).findFirst().orElseGet(() -> enumSource.getFields().get(0));
            }

            for (EnumConstantSource enumConstant : enumSource.getEnumConstants()) {
                String methodName = "is" + StrUtil.upperFirst(StrUtil.toCamelCase(enumConstant.getName()).toLowerCase());
                MethodSource<JavaEnumSource> methodMethodSource = enumSource.getMethod(methodName, enumSource.getName());
                // 生成基于枚举自身的 is 方法
                if (Objects.isNull(methodMethodSource)) {
                    enumSource.addMethod().setPublic().setStatic(true).setName(methodName).setReturnType("boolean").setParameters(enumSource.getName() + " " + StrUtil.lowerFirst(enumSource.getName())).setBody(StrUtil.format("return {}.equals({});", enumConstant.getName(), StrUtil.lowerFirst(enumSource.getName())));
                }
                // 生成基于第一个字段的
                if (Objects.nonNull(field)) {
                    methodMethodSource = enumSource.getMethod(methodName, field.getType().getName());
                    if (Objects.isNull(methodMethodSource)) {
                        enumSource.addMethod().setPublic().setStatic(true).setName(methodName).setReturnType("boolean").setParameters(field.getType().getName() + " " + StrUtil.lowerFirst(field.getName())).setBody(StrUtil.format("return {}.equals({});", enumConstant.getName() + StrUtil.DOT + StrUtil.lowerFirst(field.getName()), StrUtil.lowerFirst(field.getName())));
                    }
                }
            }

            return Result.success(javaSource.toString());
        } else {
            return Result.fail(400, "The selected code is not of enumeration type.");
        }
    }
}