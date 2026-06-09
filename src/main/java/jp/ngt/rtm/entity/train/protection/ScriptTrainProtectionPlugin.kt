package jp.ngt.rtm.entity.train.protection

import jdk.nashorn.api.scripting.ScriptObjectMirror
import jp.ngt.rtm.entity.train.EntityTrainBase
import net.minecraft.entity.player.EntityPlayer
import java.lang.reflect.InvocationTargetException

class ScriptTrainProtectionPlugin(private val scriptPlugin: Any?) : TrainProtectionPlugin() {
    override fun onServerTick(context: TrainProtectionContext): Int {
        val plugin = scriptPlugin ?: return 0

        if (plugin is ScriptObjectMirror) {
            return when {
                plugin.isFunction -> toInt(plugin.call(null, context))
                plugin.hasMember("onServerTick") -> toInt(plugin.callMember("onServerTick", context))
                else -> 0
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

    override fun onATSKeyDown(context: TrainProtectionContext, player: EntityPlayer): Boolean {
        val plugin = scriptPlugin ?: return false

        if (plugin is ScriptObjectMirror) {
            return if (plugin.hasMember("onATSKeyDown")) {
                toBoolean(plugin.callMember("onATSKeyDown", context, player))
            } else {
                false
            }
        }

        return callBooleanMethod(
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

    private fun callBooleanMethod(name: String, parameterTypes: Array<Class<*>>, args: Array<Any>): Boolean {
        return try {
            val method = scriptPlugin!!.javaClass.getMethod(name, *parameterTypes)
            toBoolean(method.invoke(scriptPlugin, *args))
        } catch (_: NoSuchMethodException) {
            false
        } catch (e: IllegalAccessException) {
            throw RuntimeException(e)
        } catch (e: InvocationTargetException) {
            throw RuntimeException(e)
        }
    }

    private fun toInt(value: Any?): Int {
        if (value == null || ScriptObjectMirror.isUndefined(value)) {
            return 0
        }

        if (value is Number) {
            return value.toInt()
        }

        return value.toString().toIntOrNull() ?: 0
    }

    private fun toBoolean(value: Any?): Boolean {
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
