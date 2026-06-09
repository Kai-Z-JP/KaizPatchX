package jp.ngt.rtm.entity.train.protection

import jp.ngt.rtm.entity.train.EntityTrainBase
import net.minecraft.entity.player.EntityPlayer

abstract class TrainProtectionPlugin {
    /**
     * @return 内部ノッチ要求 0は制御要求がないことを意味します
     */
    abstract fun onServerTick(context: TrainProtectionContext): Int

    open fun onRegister(train: EntityTrainBase) {
    }

    open fun onUnregister(train: EntityTrainBase) {
    }

    /**
     * @return この保安装置がATSキーを処理するか デフォルトのATS処理をスキップする場合はtrue
     */
    open fun onATSKeyDown(context: TrainProtectionContext, player: EntityPlayer) {
    }
}
