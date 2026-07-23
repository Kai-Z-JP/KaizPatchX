package jp.ngt.rtm.entity

import jp.ngt.rtm.RTMCore
import jp.ngt.rtm.RTMItem
import jp.ngt.rtm.entity.train.EntityTrainBase
import jp.ngt.rtm.item.ItemInstalledObject.IstlObjType
import jp.ngt.rtm.modelpack.DataFormProvider
import jp.ngt.rtm.modelpack.cfg.DataFormConfig
import net.minecraft.entity.player.EntityPlayer
import net.minecraft.item.ItemStack
import net.minecraft.world.World
import java.util.*
import kotlin.math.abs

class EntityTransponder(world: World) : EntityInstalledObject(world), DataFormProvider {
    private val trainEntryTracker = EntryTracker<UUID>()

    init {
        setSize(1.0F, 0.0625F)
        ignoreFrustumCheck = true
    }

    override fun onUpdate() {
        super.onUpdate()
        if (!worldObj.isRemote) {
            updateTrainEntries()
        }
    }

    private fun updateTrainEntries() {
        val detectionBox = boundingBox.expand(0.0, EntityTrainBase.TRAIN_HEIGHT.toDouble(), 0.0)
        val trains = worldObj
            .getEntitiesWithinAABB(EntityTrainBase::class.java, detectionBox)
            .filterIsInstance<EntityTrainBase>()
        val trainsById = trains.associateBy { it.uniqueID }
        for (entityId in trainEntryTracker.update(trainsById.keys)) {
            val train = trainsById[entityId] ?: continue
            val executer = scriptExecuter
            executer.execScript(this, TRAIN_PASS_EVENT, this, train, executer)
        }
    }

    /**
     * 車両の進行側が、この地上子の指向方向を向いているかを返す
     */
    fun isTrainFacingDirection(train: EntityTrainBase): Boolean =
        isTrainFacingTransponderDirection(rotationYaw, train.rotationYaw, train.trainDirection)

    override fun interactFirst(player: EntityPlayer): Boolean {
        if (player.isSneaking && player.currentEquippedItem == null) {
            if (worldObj.isRemote) {
                player.openGui(
                    RTMCore.instance,
                    RTMCore.guiIdSelectEntityModel.toInt(),
                    worldObj,
                    entityId,
                    0,
                    0
                )
            }
            return true
        }

        if (worldObj.isRemote && dataFormConfig?.isValid == true) {
            val pos = getPos()
            player.openGui(
                RTMCore.instance,
                RTMCore.guiIdDataForm.toInt(),
                worldObj,
                pos[0],
                pos[1],
                pos[2]
            )
        }
        return true
    }

    protected override fun dropItems() {
        entityDropItem(ItemStack(RTMItem.installedObject, 1, IstlObjType.TRANSPONDER.id.toInt()), 0.0F)
    }

    override fun getSubType(): String = "Transponder"

    protected override fun getDefaultName(): String = "Transponder_01"

    protected override fun getItem(): ItemStack =
        ItemStack(RTMItem.installedObject, 1, IstlObjType.TRANSPONDER.id.toInt())

    override val dataFormConfig: DataFormConfig?
        get() = modelSet.config.customForm

    override val dataFormPermission: String
        get() = RTMCore.EDIT_RAIL

    companion object {
        const val TRAIN_PASS_EVENT = "onTrainPass"
    }
}

internal fun isTrainFacingTransponderDirection(
    transponderYaw: Float,
    trainYaw: Float,
    trainDirection: Int
): Boolean {
    val transponderHeadingYaw = transponderYaw + 180.0F
    val headingYaw = trainYaw + if (trainDirection == 1) 180.0F else 0.0F
    val angleDifference = (((headingYaw - transponderHeadingYaw) % 360.0F + 540.0F) % 360.0F) - 180.0F
    return abs(angleDifference) < 90.0F
}
