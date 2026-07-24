package jp.ngt.rtm.entity.train.protection

import jp.ngt.rtm.entity.train.EntityTrainBase
import net.minecraft.entity.player.EntityPlayer

abstract class TrainProtectionPlugin {
    /**
     * ServerTickごとに呼び出されます。
     * 内部ノッチを要求する場合は context.requestInternalNotch(notch) を呼び出します。
     */
    abstract fun onServerTick(context: TrainProtectionContext)

    open fun onRegister(train: EntityTrainBase) {
    }

    open fun onUnregister(train: EntityTrainBase) {
    }

    /**
     * ATSキー押下時に呼び出されます
     */
    open fun onATSKeyDown(context: TrainProtectionContext, player: EntityPlayer) {
    }
}
