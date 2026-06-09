package jp.ngt.rtm.entity.train.protection

import jp.ngt.rtm.entity.train.EntityTrainBase
import jp.ngt.rtm.modelpack.state.DataMap
import net.minecraft.world.World

class TrainProtectionContext(
    val train: EntityTrainBase,
    val protectionPluginId: String,
    val notch: Int,
    val speed: Float
) {
    val world: World
        get() = train.worldObj

    val signal: Int
        get() = train.signal

    val dataMap: DataMap.NamespaceView
        get() = train.resourceState.dataMap.namespace(protectionPluginId)
}
