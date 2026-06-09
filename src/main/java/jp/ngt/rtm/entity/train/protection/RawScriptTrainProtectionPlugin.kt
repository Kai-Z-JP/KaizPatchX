package jp.ngt.rtm.entity.train.protection

import jdk.nashorn.api.scripting.ScriptObjectMirror
import jp.ngt.rtm.entity.train.EntityTrainBase
import net.minecraft.entity.player.EntityPlayer
import java.lang.reflect.InvocationTargetException

class RawScriptTrainProtectionPlugin(private val scriptPlugin: Any?) : ScriptTrainProtectionPlugin() {
    override fun onServerTick(context: TrainProtectionContext): Int {
        val plugin = scriptPlugin ?: return 0

        if (plugin is ScriptObjectMirror) {
            return if (plugin.hasMember("onServerTick")) {
                toInt(plugin.callMember("onServerTick", context))
            } else {
                0
            }
        }

        return callJavaMethod("onServerTick", arrayOf(TrainProtectionContext::class.java), arrayOf(context))
    }

    override fun onRegister(train: EntityTrainBase) {
        callVoidMethod("onRegister", arrayOf(EntityTrainBase::class.java), arrayOf(train))
    }

    override fun onUnregister(train: EntityTrainBase) {
        callVoidMethod("onUnregister", arrayOf(EntityTrainBase::class.java), arrayOf(train))
    }

    override fun onATSKeyDown(context: TrainProtectionContext, player: EntityPlayer) {
        val plugin = scriptPlugin ?: return

        if (plugin is ScriptObjectMirror) {
            if (plugin.hasMember("onATSKeyDown")) {
                toBoolean(plugin.callMember("onATSKeyDown", context, player))
            }
            return
        }

        callVoidMethod(
            "onATSKeyDown",
            arrayOf(TrainProtectionContext::class.java, EntityPlayer::class.java),
            arrayOf(context, player)
        )
    }

    private fun callJavaMethod(name: String, parameterTypes: Array<Class<*>>, args: Array<Any>): Int {
        return try {
            val method = scriptPlugin!!.javaClass.getMethod(name, *parameterTypes)
            toInt(method.invoke(scriptPlugin, *args))
        } catch (_: NoSuchMethodException) {
            0
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException(e)
        }
    }

    private fun callVoidMethod(name: String, parameterTypes: Array<Class<*>>, args: Array<Any>) {
        val plugin = scriptPlugin ?: return

        if (plugin is ScriptObjectMirror) {
            if (plugin.hasMember(name)) {
                plugin.callMember(name, *args)
            }
            return
        }

        callJavaMethod(name, parameterTypes, args)
    }
}
