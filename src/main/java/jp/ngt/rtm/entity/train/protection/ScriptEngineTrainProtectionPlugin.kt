package jp.ngt.rtm.entity.train.protection

import jp.ngt.rtm.entity.train.EntityTrainBase
import net.minecraft.entity.player.EntityPlayer
import javax.script.Invocable
import javax.script.ScriptEngine
import javax.script.ScriptException

class ScriptEngineTrainProtectionPlugin(private val engine: ScriptEngine) : ScriptTrainProtectionPlugin() {
    override fun onServerTick(context: TrainProtectionContext) {
        callVoidFunction("onServerTick", context)
    }

    override fun onRegister(train: EntityTrainBase) {
        callVoidFunction("onRegister", train)
    }

    override fun onUnregister(train: EntityTrainBase) {
        callVoidFunction("onUnregister", train)
    }

    override fun onATSKeyDown(context: TrainProtectionContext, player: EntityPlayer) {
        callVoidFunction("onATSKeyDown", context, player)
    }

    private fun callVoidFunction(name: String, vararg args: Any) {
        try {
            (engine as Invocable).invokeFunction(name, *args)
        } catch (_: NoSuchMethodException) {
        } catch (e: ScriptException) {
            throw RuntimeException(e)
        }
    }
}
