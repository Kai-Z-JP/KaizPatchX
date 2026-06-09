package jp.ngt.rtm.entity.train.protection

import jdk.nashorn.api.scripting.ScriptObjectMirror
import javax.script.ScriptEngine

abstract class ScriptTrainProtectionPlugin : TrainProtectionPlugin() {
    companion object {
        @JvmStatic
        fun create(scriptPlugin: Any?): TrainProtectionPlugin {
            if (scriptPlugin is ScriptEngine) {
                return ScriptEngineTrainProtectionPlugin(scriptPlugin)
            }

            if (scriptPlugin is ScriptObjectMirror) {
                if (scriptPlugin.isFunction) {
                    return RawSimpleScriptTrainProtectionPlugin(scriptPlugin)
                } else {
                    return RawScriptTrainProtectionPlugin(scriptPlugin)
                }
            }

            throw IllegalArgumentException("Unsupported script plugin type: ${scriptPlugin?.javaClass}")
        }
    }

    protected fun toInt(value: Any?): Int {
        if (value == null || ScriptObjectMirror.isUndefined(value)) {
            return 0
        }

        if (value is Number) {
            return value.toInt()
        }

        return value.toString().toIntOrNull() ?: 0
    }

    protected fun toBoolean(value: Any?): Boolean {
        if (value == null || ScriptObjectMirror.isUndefined(value)) {
            return false
        }

        if (value is Boolean) {
            return value
        }

        if (value is Number) {
            return value.toInt() != 0
        }

        return value.toString().toBoolean()
    }
}
