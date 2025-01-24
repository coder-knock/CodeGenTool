package com.coderknock.codegen.tool.inspection

/**
 * 枚举一致性检查配置
 * 使用单例存储配置，插件运行期间生效
 */
class EnumConsistencyConfig {
    var isRealTimeCheckEnabled: Boolean = false
    var compareBranch: String? = null

    companion object {
        private val instance = EnumConsistencyConfig()

        fun getInstance(): EnumConsistencyConfig {
            return instance
        }
    }

    fun setConfig(enabled: Boolean, branch: String?) {
        this.isRealTimeCheckEnabled = enabled
        this.compareBranch = branch
    }

    fun clear() {
        this.isRealTimeCheckEnabled = false
        this.compareBranch = null
    }
}
