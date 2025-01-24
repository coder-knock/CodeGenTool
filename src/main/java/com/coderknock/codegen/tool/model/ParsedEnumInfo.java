package com.coderknock.codegen.tool.model;

import org.jboss.forge.roaster.model.source.FieldSource;
import org.jboss.forge.roaster.model.source.JavaEnumSource;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 解析后的枚举信息
 */
public class ParsedEnumInfo {

    private String enumName;
    private String fullQualifiedName;
    private JavaEnumSource enumSource;
    private List<EnumConstantInfo> constants;
    private Map<String, EnumConstantInfo> constantMap;
    private List<FieldSource<JavaEnumSource>> instanceFields;
    private FieldSource<JavaEnumSource> compareField;

    public ParsedEnumInfo() {
        this.constants = new ArrayList<>();
        this.constantMap = new HashMap<>();
        this.instanceFields = new ArrayList<>();
    }

    public void addConstant(EnumConstantInfo constant) {
        this.constants.add(constant);
        this.constantMap.put(constant.getName(), constant);
    }

    public String getEnumName() {
        return enumName;
    }

    public void setEnumName(String enumName) {
        this.enumName = enumName;
    }

    public String getFullQualifiedName() {
        return fullQualifiedName;
    }

    public void setFullQualifiedName(String fullQualifiedName) {
        this.fullQualifiedName = fullQualifiedName;
    }

    public JavaEnumSource getEnumSource() {
        return enumSource;
    }

    public void setEnumSource(JavaEnumSource enumSource) {
        this.enumSource = enumSource;
    }

    public List<EnumConstantInfo> getConstants() {
        return constants;
    }

    public Map<String, EnumConstantInfo> getConstantMap() {
        return constantMap;
    }

    public EnumConstantInfo getConstant(String name) {
        return constantMap.get(name);
    }

    public List<FieldSource<JavaEnumSource>> getInstanceFields() {
        return instanceFields;
    }

    public void addInstanceField(FieldSource<JavaEnumSource> field) {
        this.instanceFields.add(field);
    }

    public FieldSource<JavaEnumSource> getCompareField() {
        return compareField;
    }

    public void setCompareField(FieldSource<JavaEnumSource> compareField) {
        this.compareField = compareField;
    }

    public boolean hasCompareField() {
        return compareField != null;
    }

    /**
     * 枚举常量信息
     */
    public static class EnumConstantInfo {
        private String name;
        private Map<String, Object> fieldValues;
        private String originalText;

        public EnumConstantInfo(String name, String originalText) {
            this.name = name;
            this.originalText = originalText;
            this.fieldValues = new HashMap<>();
        }

        public String getName() {
            return name;
        }

        public Map<String, Object> getFieldValues() {
            return fieldValues;
        }

        public void setFieldValue(String fieldName, Object value) {
            this.fieldValues.put(fieldName, value);
        }

        public Object getFieldValue(String fieldName) {
            return fieldValues.get(fieldName);
        }

        public String getOriginalText() {
            return originalText;
        }
    }
}
