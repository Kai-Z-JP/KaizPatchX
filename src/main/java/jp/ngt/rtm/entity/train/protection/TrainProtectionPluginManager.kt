package jp.ngt.rtm.entity.train.protection

import jp.ngt.ngtlib.io.NGTLog
import jp.ngt.ngtlib.io.ScriptUtil
import jp.ngt.rtm.entity.train.EntityTrainBase
import jp.ngt.rtm.modelpack.ModelPackManager
import jp.ngt.rtm.modelpack.cfg.TrainConfig
import jp.ngt.rtm.modelpack.modelset.ModelSetVehicleBase
import java.util.concurrent.ConcurrentHashMap

object TrainProtectionPluginManager {
    private val entries: MutableMap<String, Entry> = ConcurrentHashMap()

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

        entries[config.id] = Entry(
            id = config.id,
            displayName = config.displayName,
            defaultEnabled = config.defaultEnabled,
            plugin = createPlugin(config),
        )
    }

    @JvmStatic
    fun enableDefaultPlugins(train: EntityTrainBase) {
        for (id in getDefaultPluginIds(train)) {
            enablePlugin(train, id)
        }
    }

    private fun enablePlugin(train: EntityTrainBase, id: String) {
        if (train.hasProtectionPlugin(id)) {
            return
        }

        try {
            train.enableProtectionPlugin(id)
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
    ): Boolean {
        if (id.isEmpty()) {
            return false
        }
        entries[id] = Entry(
            id = id,
            displayName = displayName,
            defaultEnabled = defaultEnabled,
            plugin = plugin
        )
        return true
    }

    @JvmStatic
    fun getPlugin(id: String): TrainProtectionPlugin? {
        return entries[id]?.plugin
    }

    private fun createPlugin(config: TrainProtectionPluginConfig): TrainProtectionPlugin {
        return ScriptTrainProtectionPlugin.create(ScriptUtil.doScript(ModelPackManager.INSTANCE.getScript(config.scriptPath)))
    }

    private data class Entry(
        val id: String,
        val displayName: String,
        val defaultEnabled: Boolean,
        val plugin: TrainProtectionPlugin
    )
}
