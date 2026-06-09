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
}
