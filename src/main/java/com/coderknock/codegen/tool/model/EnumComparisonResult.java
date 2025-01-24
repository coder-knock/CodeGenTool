package com.coderknock.codegen.tool.model;

import java.util.ArrayList;
import java.util.List;

/**
 * 枚举比较结果
 */
public class EnumComparisonResult {

    private boolean hasConflicts;
    private List<EnumConflict> conflicts;
    private List<DuplicateValueInfo> duplicateValues;
    private String enumName;
    private String localBranch;
    private String remoteBranch;

    public EnumComparisonResult() {
        this.conflicts = new ArrayList<>();
        this.duplicateValues = new ArrayList<>();
        this.hasConflicts = false;
    }

    public boolean hasConflicts() {
        return hasConflicts;
    }

    public void setHasConflicts(boolean hasConflicts) {
        this.hasConflicts = hasConflicts;
    }

    public List<EnumConflict> getConflicts() {
        return conflicts;
    }

    public void addConflict(EnumConflict conflict) {
        this.conflicts.add(conflict);
        this.hasConflicts = true;
    }

    public List<DuplicateValueInfo> getDuplicateValues() {
        return duplicateValues;
    }

    public void addDuplicateValue(DuplicateValueInfo duplicateValue) {
        this.duplicateValues.add(duplicateValue);
        this.hasConflicts = true;
    }

    public String getEnumName() {
        return enumName;
    }

    public void setEnumName(String enumName) {
        this.enumName = enumName;
    }

    public String getLocalBranch() {
        return localBranch;
    }

    public void setLocalBranch(String localBranch) {
        this.localBranch = localBranch;
    }

    public String getRemoteBranch() {
        return remoteBranch;
    }

    public void setRemoteBranch(String remoteBranch) {
        this.remoteBranch = remoteBranch;
    }

    /**
     * 枚举冲突信息
     */
    public static class EnumConflict {
        private ConflictType type;
        private String enumConstant;
        private Object localValue;
        private Object remoteValue;
        private String fieldName;
        private String message;

        public EnumConflict(ConflictType type, String enumConstant, String fieldName,
                           Object localValue, Object remoteValue, String message) {
            this.type = type;
            this.enumConstant = enumConstant;
            this.fieldName = fieldName;
            this.localValue = localValue;
            this.remoteValue = remoteValue;
            this.message = message;
        }

        public ConflictType getType() {
            return type;
        }

        public String getEnumConstant() {
            return enumConstant;
        }

        public String getFieldName() {
            return fieldName;
        }

        public Object getLocalValue() {
            return localValue;
        }

        public Object getRemoteValue() {
            return remoteValue;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * 重复值信息（当前枚举内部）
     */
    public static class DuplicateValueInfo {
        private String fieldName;
        private Object value;
        private List<String> affectedConstants;
        private String message;

        public DuplicateValueInfo(String fieldName, Object value, List<String> affectedConstants) {
            this.fieldName = fieldName;
            this.value = value;
            this.affectedConstants = affectedConstants;
            this.message = String.format("字段 '%s' 的值 '%s' 在枚举常量 %s 中重复",
                    fieldName, value, String.join(", ", affectedConstants));
        }

        public String getFieldName() {
            return fieldName;
        }

        public Object getValue() {
            return value;
        }

        public List<String> getAffectedConstants() {
            return affectedConstants;
        }

        public String getMessage() {
            return message;
        }
    }

    /**
     * 冲突类型
     */
    public enum ConflictType {
        /**
         * 枚举常量不存在于远端
         */
        MISSING_IN_REMOTE("枚举常量在远端分支不存在"),
        /**
         * 枚举常量不存在于本地
         */
        MISSING_IN_LOCAL("枚举常量在本地不存在"),
        /**
         * 字段值不匹配
         */
        VALUE_MISMATCH("字段值不匹配"),
        /**
         * 本地重复值
         */
        LOCAL_DUPLICATE("本地存在重复值"),
        /**
         * 远端重复值
         */
        REMOTE_DUPLICATE("远端存在重复值");

        private final String description;

        ConflictType(String description) {
            this.description = description;
        }

        public String getDescription() {
            return description;
        }
    }
}
