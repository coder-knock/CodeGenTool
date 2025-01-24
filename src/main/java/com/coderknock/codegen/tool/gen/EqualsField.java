package com.coderknock.codegen.tool.gen;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 标记用于 equals 比较的字段
 * <p>
 * 在枚举类中，用于指定哪个字段用于 {@code isXXX()} 方法的比较
 * 如果不指定，默认使用第一个非静态字段
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EqualsField {
}
