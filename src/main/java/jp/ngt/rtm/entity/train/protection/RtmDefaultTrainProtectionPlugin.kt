package jp.ngt.rtm.entity.train.protection

import jp.ngt.rtm.entity.train.EntityTrainBase
import net.minecraft.entity.player.EntityPlayer

object RtmDefaultTrainProtectionPlugin : TrainProtectionPlugin() {
    const val ID: String = "rtm_default"
    const val DISPLAY_NAME: String = "RTM Default"

    private const val ATS_COUNT = "atsCount"
    private const val STOP_SIGNAL = 1
    private const val ACKNOWLEDGED_SIGNAL = -1
    private const val RESET_ACK_NOTCH = -8
    private const val WARNING_TICKS = 100
    private const val DATA_FLAG = 0

    override fun onServerTick(context: TrainProtectionContext) {
        var count = context.dataMap.getInt(ATS_COUNT)
        if (count <= 0 && context.signal == STOP_SIGNAL && context.speed > 0.0F) {
            count = 1
        } else if (count > 0) {
            count += 1
        }

        if (count <= 0) {
            return
        }

        if (count >= WARNING_TICKS) {
            context.train.stopTrain(false)
            context.dataMap.remove(ATS_COUNT, DATA_FLAG)
        } else {
            context.dataMap.setInt(ATS_COUNT, count, DATA_FLAG)
        }
    }

    override fun onATSKeyDown(context: TrainProtectionContext, player: EntityPlayer) {
        when (context.signal) {
            STOP_SIGNAL -> {
                context.train.setSignal2(ACKNOWLEDGED_SIGNAL)
                context.dataMap.remove(ATS_COUNT, DATA_FLAG)
            }

            ACKNOWLEDGED_SIGNAL if context.notch == RESET_ACK_NOTCH -> {
                context.train.setSignal2(0)
            }
        }
    }

    override fun onUnregister(train: EntityTrainBase) {
        train.resourceState.dataMap.namespace(ID).remove(ATS_COUNT, DATA_FLAG)
        if (train.signal == ACKNOWLEDGED_SIGNAL) {
            train.setSignal2(0)
        }
    }
}
