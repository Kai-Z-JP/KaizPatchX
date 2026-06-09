package jp.ngt.rtm.entity.train.protection

import jdk.nashorn.api.scripting.ScriptObjectMirror

class RawSimpleScriptTrainProtectionPlugin(private val function: ScriptObjectMirror) : ScriptTrainProtectionPlugin() {
    override fun onServerTick(context: TrainProtectionContext) = toInt(function.call(null, context))
}
