package jp.ngt.rtm.entity.train.protection

import java.util.regex.Pattern

class TrainProtectionPluginConfig @JvmOverloads constructor(
    @JvmField
    var id: String = "",
    @JvmField
    var displayName: String = id,
    @JvmField
    var scriptPath: String = "",
    @JvmField
    var defaultEnabled: Boolean = false
) {
    companion object {
        @JvmField
        val CONFIG_FILE_PATTERN: Pattern = Pattern.compile("TrainProtectionPlugin_.*\\.json")
    }

    fun checkFields() {
        if (id.isEmpty()) {
            throw IllegalArgumentException("Train protection plugin id is empty")
        }

        if (scriptPath.isEmpty()) {
            throw IllegalArgumentException("Train protection plugin scriptPath is empty")
        }
    }
}
