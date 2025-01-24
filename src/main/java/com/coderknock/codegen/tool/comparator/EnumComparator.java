package com.coderknock.codegen.tool.comparator;

import cn.hutool.core.util.StrUtil;
import com.coderknock.codegen.tool.model.EnumComparisonResult;
import com.coderknock.codegen.tool.model.EnumComparisonResult.ConflictType;
import com.coderknock.codegen.tool.model.EnumComparisonResult.EnumConflict;
import com.coderknock.codegen.tool.model.EnumComparisonResult.DuplicateValueInfo;
import com.coderknock.codegen.tool.model.ParsedEnumInfo;
import com.coderknock.codegen.tool.model.ParsedEnumInfo.EnumConstantInfo;
import org.jboss.forge.roaster.Roaster;
import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaEnumSource;

import java.lang.reflect.Field;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 枚举解析器和比较器
 */
public class EnumComparator {

    /**
     * 解析枚举代码
     */
    public static ParsedEnumInfo parseEnum(String javaCode) throws Exception {
        var javaSource = Roaster.parse(JavaSource.class, javaCode);
        if (!javaSource.isEnum()) {
            throw new IllegalArgumentException("Not an enum class");
        }

        JavaEnumSource enumSource = (JavaEnumSource) javaSource;
        ParsedEnumInfo parsedInfo = new ParsedEnumInfo();
        parsedInfo.setEnumName(enumSource.getName());
        parsedInfo.setFullQualifiedName(enumSource.getQualifiedName());
        parsedInfo.setEnumSource(enumSource);

        // 获取所有非静态字段（实例字段）
        List<FieldSource<JavaEnumSource>> nonStaticFields = enumSource.getFields().stream()
                .filter(f -> !f.isStatic())
                .collect(Collectors.toList());
        for (FieldSource<JavaEnumSource> field : nonStaticFields) {
            parsedInfo.addInstanceField(field);
        }

        // 查找用于比较的字段（优先使用 @EqualsField 注解，否则使用第一个非静态字段）
        FieldSource<JavaEnumSource> compareField = nonStaticFields.stream()
                .filter(f -> f.hasAnnotation(com.coderknock.codegen.tool.gen.EqualsField.class))
                .findFirst()
                .orElse(nonStaticFields.isEmpty() ? null : nonStaticFields.get(0));
        parsedInfo.setCompareField(compareField);

        // 解析每个枚举常量
        for (var enumConstant : enumSource.getEnumConstants()) {
            ParsedEnumInfo.EnumConstantInfo constantInfo =
                    new ParsedEnumInfo.EnumConstantInfo(enumConstant.getName(), enumConstant.toString());

            // 如果有比较字段，尝试提取其值
            if (compareField != null) {
                Object value = extractFieldValueFromConstant(enumConstant, compareField);
                if (value != null) {
                    constantInfo.setFieldValue(compareField.getName(), value);
                }
            }

            parsedInfo.addConstant(constantInfo);
        }

        return parsedInfo;
    }

    /**
     * 从枚举常量构造参数中提取字段值
     */
    private static Object extractFieldValueFromConstant(
            org.jboss.forge.roaster.model.source.EnumConstantSource enumConstant,
            FieldSource<JavaEnumSource> field
    ) {
        String constantText = enumConstant.toString();
        String fieldType = field.getType().getName();

        // 提取括号内的参数部分
        int openParen = constantText.indexOf('(');
        int closeParen = constantText.lastIndexOf(')');
        if (openParen < 0 || closeParen < 0 || openParen >= closeParen) {
            return null;
        }

        String argsStr = constantText.substring(openParen + 1, closeParen).trim();
        if (StrUtil.isBlank(argsStr)) {
            return null;
        }

        // 分割参数（简单分割，不处理括号嵌套的情况）
        List<String> args = splitArguments(argsStr);

        // 获取字段在构造函数中的位置（假设顺序一致）
        List<FieldSource<JavaEnumSource>> fields = enumSource.getDeclaringClass().getFields().stream()
                .filter(f -> !f.isStatic())
                .collect(Collectors.toList());

        int fieldIndex = -1;
        for (int i = 0; i < fields.size(); i++) {
            if (fields.get(i).getName().equals(field.getName())) {
                fieldIndex = i;
                break;
            }
        }

        if (fieldIndex >= 0 && fieldIndex < args.size()) {
            String argValue = args.get(fieldIndex).trim();
            return parseValue(argValue, fieldType);
        }

        return null;
    }

    /**
     * 分割参数，处理带括号的情况
     */
    private static List<String> splitArguments(String argsStr) {
        List<String> result = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        int parenthesisDepth = 0;

        for (char c : argsStr.toCharArray()) {
            if (c == ',' && parenthesisDepth == 0) {
                if (StrUtil.isNotBlank(current)) {
                    result.add(current.toString().trim());
                    current = new StringBuilder();
                }
            } else {
                if (c == '(') parenthesisDepth++;
                else if (c == ')') parenthesisDepth--;
                current.append(c);
            }
        }

        if (StrUtil.isNotBlank(current)) {
            result.add(current.toString().trim());
        }

        return result;
    }

    /**
     * 解析字符串值为对应类型
     */
    private static Object parseValue(String valueStr, String type) {
        if (StrUtil.isBlank(valueStr)) {
            return null;
        }

        // 移除字符串引号
        if ((valueStr.startsWith("\"") && valueStr.endsWith("\"")) ||
            (valueStr.startsWith("'") && valueStr.endsWith("'"))) {
            valueStr = valueStr.substring(1, valueStr.length() - 1);
        }

        switch (type) {
            case "int":
            case "Integer":
                try {
                    return Integer.parseInt(valueStr);
                } catch (NumberFormatException e) {
                    return valueStr;
                }
            case "long":
            case "Long":
                try {
                    return Long.parseLong(valueStr.replace("L", "").replace("l", ""));
                } catch (NumberFormatException e) {
                    return valueStr;
                }
            case "short":
            case "Short":
                try {
                    return Short.parseShort(valueStr);
                } catch (NumberFormatException e) {
                    return valueStr;
                }
            case "byte":
            case "Byte":
                try {
                    return Byte.parseByte(valueStr);
                } catch (NumberFormatException e) {
                    return valueStr;
                }
            case "float":
            case "Float":
                try {
                    return Float.parseFloat(valueStr.replace("F", "").replace("f", ""));
                } catch (NumberFormatException e) {
                    return valueStr;
                }
            case "double":
            case "Double":
                try {
                    return Double.parseDouble(valueStr.replace("D", "").replace("d", ""));
                } catch (NumberFormatException e) {
                    return valueStr;
                }
            case "boolean":
            case "Boolean":
                return Boolean.parseBoolean(valueStr);
            case "char":
            case "Character":
                return valueStr.length() > 0 ? valueStr.charAt(0) : valueStr;
            case "String":
                return valueStr;
            default:
                return valueStr;
        }
    }

    /**
     * 检查当前枚举内部是否有重复值
     */
    public static void checkDuplicateValues(ParsedEnumInfo enumInfo, EnumComparisonResult result) {
        if (!enumInfo.hasCompareField()) {
            return;
        }

        String fieldName = enumInfo.getCompareField().getName();
        Map<Object, List<String>> valueToConstants = new HashMap<>();

        for (EnumConstantInfo constant : enumInfo.getConstants()) {
            Object value = constant.getFieldValue(fieldName);
            if (value != null) {
                valueToConstants.computeIfAbsent(value, k -> new ArrayList<>())
                        .add(constant.getName());
            }
        }

        for (Map.Entry<Object, List<String>> entry : valueToConstants.entrySet()) {
            if (entry.getValue().size() > 1) {
                result.addDuplicateValue(new DuplicateValueInfo(
                        fieldName,
                        entry.getKey(),
                        entry.getValue()
                ));
            }
        }
    }

    /**
     * 比较两个枚举
     */
    public static EnumComparisonResult compare(
            ParsedEnumInfo local,
            ParsedEnumInfo remote,
            String localBranch,
            String remoteBranch
    ) {
        EnumComparisonResult result = new EnumComparisonResult();
        result.setEnumName(local.getEnumName());
        result.setLocalBranch(localBranch);
        result.setRemoteBranch(remoteBranch);

        // 首先检查本地是否有重复值
        checkDuplicateValues(local, result);

        // 检查远端是否有重复值
        if (remote != null) {
            EnumComparisonResult remoteCheck = new EnumComparisonResult();
            checkDuplicateValues(remote, remoteCheck);
            // 将远端的重复问题合并到结果
            for (DuplicateValueInfo duplicate : remoteCheck.getDuplicateValues()) {
                // 添加到结果中，但标记为远端问题
                result.addConflict(new EnumConflict(
                        ConflictType.REMOTE_DUPLICATE,
                        "",
                        duplicate.getFieldName(),
                        null,
                        duplicate.getValue(),
                        String.format("远端分支中字段 '%s' 的值 '%s' 在枚举常量 %s 中重复",
                                duplicate.getFieldName(),
                                duplicate.getValue(),
                                String.join(", ", duplicate.getAffectedConstants())
                        )
                ));
            }
        }

        // 如果没有远端信息，只返回本地检查结果
        if (remote == null) {
            return result;
        }

        // 比较枚举常量
        Set<String> localNames = new HashSet<>(local.getConstantMap().keySet());
        Set<String> remoteNames = new HashSet<>(remote.getConstantMap().keySet());

        // 检查本地有但远端没有的常量
        for (String name : localNames) {
            if (!remoteNames.contains(name)) {
                result.addConflict(new EnumConflict(
                        ConflictType.MISSING_IN_REMOTE,
                        name,
                        null,
                        null,
                        null,
                        String.format("枚举常量 '%s' 在远端分支 '%s' 中不存在",
                                name, remoteBranch)
                ));
            }
        }

        // 检查远端有但本地没有的常量
        for (String name : remoteNames) {
            if (!localNames.contains(name)) {
                result.addConflict(new EnumConflict(
                        ConflictType.MISSING_IN_LOCAL,
                        name,
                        null,
                        null,
                        null,
                        String.format("枚举常量 '%s' 在本地分支 '%s' 中不存在",
                                name, localBranch)
                ));
            }
        }

        // 如果双方都有这个常量，比较字段值
        if (local.hasCompareField() && remote.hasCompareField()) {
            String fieldName = local.getCompareField().getName();
            for (String name : localNames) {
                if (remoteNames.contains(name)) {
                    EnumConstantInfo localConst = local.getConstant(name);
                    EnumConstantInfo remoteConst = remote.getConstant(name);
                    Object localValue = localConst.getFieldValue(fieldName);
                    Object remoteValue = remoteConst.getFieldValue(fieldName);

                    if (!objectsEqual(localValue, remoteValue)) {
                        result.addConflict(new EnumConflict(
                                ConflictType.VALUE_MISMATCH,
                                name,
                                fieldName,
                                localValue,
                                remoteValue,
                                String.format("枚举常量 '%s' 的字段 '%s' 值不匹配: 本地=%s, 远端=%s",
                                        name, fieldName,
                                        String.valueOf(localValue),
                                        String.valueOf(remoteValue))
                        ));
                    }
                }
            }
        }

        return result;
    }

    /**
     * 比较两个对象是否相等，处理 null 的情况
     */
    private static boolean objectsEqual(Object a, Object b) {
        if (a == null && b == null) return true;
        if (a == null || b == null) return false;
        // 处理数字类型的比较
        if (a instanceof Number && b instanceof Number) {
            return ((Number) a).doubleValue() == ((Number) b).doubleValue();
        }
        return a.equals(b);
    }
}
