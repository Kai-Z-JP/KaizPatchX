package jp.ngt.rtm.entity.train.protection

import jp.ngt.rtm.entity.train.EntityTrainBase
import jp.ngt.rtm.modelpack.state.DataMap
import net.minecraft.world.World

class TrainProtectionContext @JvmOverloads constructor(
    val train: EntityTrainBase,
    val protectionPluginId: String = "",
    val notch: Int = train.notch,
    val speed: Float = train.speed
) {
    val world: World
        get() = train.worldObj

    val signal: Int
        get() = train.signal

    val dataMap: DataMap.NamespaceView
        get() = train.resourceState.dataMap.namespace(protectionPluginId)

    /**
     * 内部ノッチ要求。0は制御要求がないことを意味します。
     */
    var requestedInternalNotch: Int = 0
        private set

    fun requestInternalNotch(notch: Int) {
        requestedInternalNotch = notch
    }
}
