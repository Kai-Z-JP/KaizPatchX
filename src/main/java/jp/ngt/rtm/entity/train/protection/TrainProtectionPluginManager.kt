package jp.ngt.rtm.entity.train.protection

import jp.ngt.ngtlib.io.NGTLog
import jp.ngt.ngtlib.io.ScriptUtil
import jp.ngt.rtm.RTMCore
import jp.ngt.rtm.entity.train.EntityTrainBase
import jp.ngt.rtm.modelpack.ModelPackManager
import jp.ngt.rtm.modelpack.cfg.TrainConfig
import jp.ngt.rtm.modelpack.modelset.ModelSetVehicleBase
import jp.ngt.rtm.network.PacketTrainProtectionPluginList
import net.minecraft.entity.player.EntityPlayerMP
import java.util.concurrent.ConcurrentHashMap

object TrainProtectionPluginManager {
    private val entries: MutableMap<String, Entry> = ConcurrentHashMap()

    @Volatile
    private var syncedPluginInfos: List<TrainProtectionPluginInfo> = emptyList()

    init {
        putProtectionPlugin(
            RtmDefaultTrainProtectionPlugin.ID,
            RtmDefaultTrainProtectionPlugin.DISPLAY_NAME,
            RtmDefaultTrainProtectionPlugin,
            defaultEnabled = true,
        )
    }

    @JvmStatic
    fun register(config: TrainProtectionPluginConfig) {
        config.checkFields()

        putProtectionPlugin(
            config.id,
            config.displayName,
            createPlugin(config),
            config.defaultEnabled,
            config.hidden
        )
    }

    @JvmStatic
    fun enableDefaultPlugins(train: EntityTrainBase) {
        for (id in getDefaultPluginIds(train)) {
            enablePlugin(train, id)
        }
    }

    @JvmStatic
    fun enableSavedPlugins(train: EntityTrainBase) {
        for (info in getLocalPluginInfos()) {
            if (train.isProtectionPluginEnabled(info.id)) {
                enablePlugin(train, info.id)
            }
        }
    }

    private fun enablePlugin(train: EntityTrainBase, id: String) {
        if (train.hasProtectionPlugin(id)) {
            return
        }

        try {
            train.setProtectionPluginEnabled(id, true)
        } catch (e: Exception) {
            NGTLog.debug("[RTM] Failed to enable train protection plugin: $id")
            e.printStackTrace()
        }
    }

    private fun getDefaultPluginIds(train: EntityTrainBase): List<String> {
        if (entries.isEmpty()) {
            return emptyList()
        }

        val modelSet: ModelSetVehicleBase<TrainConfig> = train.getModelSet()
        if (modelSet.isDummy) {
            return emptyList()
        }

        return entries.values
            .filter { it.defaultEnabled }
            .sortedBy { it.id }
            .map { it.id }
    }

    @JvmStatic
    @JvmOverloads
    fun registerJsProtectionPlugin(id: String, jsPlugin: Any?, defaultEnabled: Boolean = false): Boolean {
        return putProtectionPlugin(id, id, ScriptTrainProtectionPlugin.create(jsPlugin), defaultEnabled)
    }

    @JvmStatic
    fun registerProtectionPlugin(id: String, plugin: TrainProtectionPlugin, defaultEnabled: Boolean): Boolean {
        return putProtectionPlugin(id, id, plugin, defaultEnabled)
    }

    private fun putProtectionPlugin(
        id: String,
        displayName: String,
        plugin: TrainProtectionPlugin,
        defaultEnabled: Boolean,
        hidden: Boolean = false,
    ): Boolean {
        if (id.isEmpty() || id.contains(':')) {
            NGTLog.debug("[RTM] Invalid train protection plugin ID: `$id`")
            return false
        }
        entries[id] = Entry(
            id = id,
            displayName = displayName,
            defaultEnabled = defaultEnabled,
            hidden = hidden,
            plugin = plugin
        )
        return true
    }

    @JvmStatic
    fun getPlugin(id: String): TrainProtectionPlugin? {
        return entries[id]?.plugin
    }

    @JvmStatic
    fun getPluginInfos(): List<TrainProtectionPluginInfo> {
        return syncedPluginInfos
    }

    @JvmStatic
    fun setSyncedPluginInfos(pluginInfos: List<TrainProtectionPluginInfo>) {
        syncedPluginInfos = pluginInfos.sortedBy { it.id }
    }

    @JvmStatic
    fun sendPluginInfos(player: EntityPlayerMP) {
        RTMCore.NETWORK_WRAPPER.sendTo(PacketTrainProtectionPluginList(getLocalPluginInfos()), player)
    }

    private fun getLocalPluginInfos(): List<TrainProtectionPluginInfo> {
        return entries.values
            .sortedBy { it.id }
            .map { TrainProtectionPluginInfo(it.id, it.displayName, it.defaultEnabled, it.hidden) }
    }

    private fun createPlugin(config: TrainProtectionPluginConfig): TrainProtectionPlugin {
        return ScriptTrainProtectionPlugin.create(ScriptUtil.doScript(ModelPackManager.INSTANCE.getScript(config.scriptPath)))
    }

    private data class Entry(
        val id: String,
        val displayName: String,
        val defaultEnabled: Boolean,
        val hidden: Boolean,
        val plugin: TrainProtectionPlugin
    )
}
