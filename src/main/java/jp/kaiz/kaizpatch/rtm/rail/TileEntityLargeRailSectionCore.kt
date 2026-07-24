package jp.kaiz.kaizpatch.rtm.rail

import jp.kaiz.kaizpatch.rtm.rail.util.RailMapSection
import jp.ngt.rtm.rail.TileEntityLargeRailBase
import jp.ngt.rtm.rail.TileEntityLargeRailCore
import jp.ngt.rtm.rail.TileEntityLargeRailNormalCore
import jp.ngt.rtm.rail.util.RailMapBasic
import jp.ngt.rtm.rail.util.RailPosition
import jp.ngt.rtm.rail.util.RailProperty
import net.minecraft.nbt.NBTTagCompound
import net.minecraft.nbt.NBTTagList
import java.util.*

/** チャンク単位に分割された通常レールの担当区間を保持する */
class TileEntityLargeRailSectionCore : TileEntityLargeRailNormalCore() {
    private var railGroupId: UUID? = null
    private var logicalRailPositions: Array<RailPosition>? = null
    private var sectionStartRatio = 0.0
    private var sectionEndRatio = 1.0
    private var railGroupCorePositions: Array<IntArray> = emptyArray()

    fun configureRailSection(
        groupId: UUID,
        logicalPositions: Array<RailPosition>,
        sectionPositions: Array<RailPosition>,
        startRatio: Double,
        endRatio: Double,
        corePositions: List<IntArray>,
    ) {
        railGroupId = groupId
        logicalRailPositions = copyRailPositions(logicalPositions)
        railPositions = copyRailPositions(sectionPositions)
        sectionStartRatio = startRatio
        sectionEndRatio = endRatio
        railGroupCorePositions = copyPositions(corePositions)
        railmap = null
    }

    fun isRailSection(): Boolean = railGroupId != null && logicalRailPositions?.size == 2

    fun getRailGroupId(): UUID? = railGroupId

    override fun isSameLogicalRail(other: TileEntityLargeRailCore): Boolean {
        if (this === other) return true
        if (!isRailSection() || other !is TileEntityLargeRailSectionCore || !other.isRailSection()) return false
        return railGroupId == other.railGroupId &&
            containsCorePosition(other.xCoord, other.yCoord, other.zCoord) &&
            other.containsCorePosition(xCoord, yCoord, zCoord)
    }

    override fun getLogicalRailPositions(): Array<RailPosition> {
        val logical = logicalRailPositions
        return if (isRailSection() && logical != null) copyRailPositions(logical) else super.getLogicalRailPositions()
    }

    fun getRailGroupCorePositions(): List<IntArray> = railGroupCorePositions.map(IntArray::clone)

    protected override fun readRailData(nbt: NBTTagCompound) {
        super.readRailData(nbt)
        readSectionData(nbt)
    }

    protected override fun writeRailData(nbt: NBTTagCompound) {
        super.writeRailData(nbt)
        writeSectionData(nbt)
    }

    fun readSectionData(parent: NBTTagCompound) {
        railGroupId = null
        logicalRailPositions = null
        sectionStartRatio = 0.0
        sectionEndRatio = 1.0
        railGroupCorePositions = emptyArray()
        railmap = null
        if (!parent.hasKey(SECTION_TAG)) return

        val section = parent.getCompoundTag(SECTION_TAG)
        if (!section.hasKey("GroupMost") || !section.hasKey("GroupLeast") ||
            !section.hasKey("StartRatio") || !section.hasKey("EndRatio") ||
            !section.hasKey("LogicalStartRP") || !section.hasKey("LogicalEndRP") ||
            !section.hasKey("CorePositions")
        ) {
            return
        }

        railGroupId = UUID(section.getLong("GroupMost"), section.getLong("GroupLeast"))
        logicalRailPositions = arrayOf(
            RailPosition.readFromNBT(section.getCompoundTag("LogicalStartRP")),
            RailPosition.readFromNBT(section.getCompoundTag("LogicalEndRP")),
        )
        sectionStartRatio = section.getDouble("StartRatio")
        sectionEndRatio = section.getDouble("EndRatio")

        val positions = section.getTagList("CorePositions", 10)
        railGroupCorePositions = Array(positions.tagCount()) { index ->
            val pos = positions.getCompoundTagAt(index)
            intArrayOf(pos.getInteger("X"), pos.getInteger("Y"), pos.getInteger("Z"))
        }
        railmap = null
    }

    fun writeSectionData(parent: NBTTagCompound) {
        val groupId = railGroupId ?: return
        val logical = logicalRailPositions ?: return
        if (!isRailSection()) return

        val section = NBTTagCompound()
        section.setLong("GroupMost", groupId.mostSignificantBits)
        section.setLong("GroupLeast", groupId.leastSignificantBits)
        section.setDouble("StartRatio", sectionStartRatio)
        section.setDouble("EndRatio", sectionEndRatio)
        section.setTag("LogicalStartRP", logical[0].writeToNBT())
        section.setTag("LogicalEndRP", logical[1].writeToNBT())
        val positions = NBTTagList()
        railGroupCorePositions.forEach { corePos ->
            val pos = NBTTagCompound()
            pos.setInteger("X", corePos[0])
            pos.setInteger("Y", corePos[1])
            pos.setInteger("Z", corePos[2])
            positions.appendTag(pos)
        }
        section.setTag("CorePositions", positions)
        parent.setTag(SECTION_TAG, section)
    }

    override fun createRailMap() {
        if (!isLoaded) return
        val logical = logicalRailPositions
        if (!isRailSection() || logical == null) {
            super.createRailMap()
            return
        }
        val copied = copyRailPositions(logical)
        val source = RailMapBasic(copied[0], copied[1], fixRTMRailMapVersion)
        railmap = RailMapSection(source, railPositions[0], railPositions[1], sectionStartRatio, sectionEndRatio)
    }

    override fun replaceRail(state: RailProperty) {
        if (!isRailSection() || worldObj == null) {
            super.replaceRail(state)
            return
        }
        groupCores.forEach { it.replaceRailLocal(state) }
    }

    private fun replaceRailLocal(state: RailProperty) = super.replaceRail(state)

    override fun addSubRail(state: RailProperty) {
        if (!isRailSection() || worldObj == null) {
            super.addSubRail(state)
            return
        }
        groupCores.forEach { it.addSubRailLocal(state) }
    }

    private fun addSubRailLocal(state: RailProperty) = super.addSubRail(state)

    override fun setSignal(signal: Int) {
        if (!isRailSection() || worldObj == null) {
            super.setSignal(signal)
            return
        }
        groupCores.forEach { it.setSignalLocal(signal) }
    }

    private fun setSignalLocal(signal: Int) = super.setSignal(signal)

    override fun isLogicalRailOccupied(): Boolean {
        if (!isRailSection() || worldObj == null) return super.isLogicalRailOccupied()
        return groupCores.any { it.isLocallyOccupied() }
    }

    private fun isLocallyOccupied(): Boolean = isCollidedTrain

    override fun getRailRenderMinimumSplit(): Int = if (isRailSection()) 1 else 0

    override fun getRailRenderEndOffset(): Int =
        if (isRailSection() && sectionEndRatio < 1.0 - RATIO_EPSILON) 1 else 0

    override fun shouldRenderRailStartCap(): Boolean =
        !isRailSection() || sectionStartRatio <= RATIO_EPSILON

    override fun shouldRenderRailEndCap(): Boolean =
        !isRailSection() || sectionEndRatio >= 1.0 - RATIO_EPSILON

    override fun breakLogicalRail() {
        if (!isRailSection() || worldObj == null) {
            super.breakLogicalRail()
            return
        }

        val cores = groupCores
        cores.forEach { it.breaking = true }
        val logical = copyRailPositions(logicalRailPositions ?: return)
        val fullMap = RailMapBasic(logical[0], logical[1], fixRTMRailMapVersion)
        val candidates = LinkedHashSet<BlockPos>()
        fullMap.getRailBlockList(property).forEach { candidates += BlockPos(it[0], it[1], it[2]) }
        cores.forEach { core ->
            core.getRailMap(null)?.getRailBlockList(core.property)?.forEach {
                candidates += BlockPos(it[0], it[1], it[2])
            }
        }
        railGroupCorePositions.forEach { candidates += BlockPos(it[0], it[1], it[2]) }

        val targets = LinkedHashSet<BlockPos>()
        candidates.forEach { pos ->
            val tile = worldObj.getTileEntity(pos.x, pos.y, pos.z)
            if (tile is TileEntityLargeRailBase) {
                val owner = tile.railCore
                if (owner != null && isSameLogicalRail(owner)) targets += pos
            }
        }
        targets.forEach { pos ->
            worldObj.setBlockToAir(pos.x, pos.y, pos.z)
            worldObj.removeTileEntity(pos.x, pos.y, pos.z)
            worldObj.markBlockForUpdate(pos.x, pos.y, pos.z)
        }
    }

    private val groupCores: List<TileEntityLargeRailSectionCore>
        get() {
            if (!isRailSection() || worldObj == null) return listOf(this)
            val result = railGroupCorePositions.mapNotNull { pos ->
                val tile = worldObj.getTileEntity(pos[0], pos[1], pos[2])
                (tile as? TileEntityLargeRailSectionCore)?.takeIf { isSameLogicalRail(it) }
            }.toMutableList()
            if (this !in result) result += this
            return result
        }

    private fun containsCorePosition(x: Int, y: Int, z: Int): Boolean =
        railGroupCorePositions.any { it[0] == x && it[1] == y && it[2] == z }

    override fun setPos(x: Int, y: Int, z: Int, prevX: Int, prevY: Int, prevZ: Int) {
        val difX = x - prevX
        val difY = y - prevY
        val difZ = z - prevZ
        if (isRailSection()) {
            logicalRailPositions?.forEach { it.movePos(difX, difY, difZ) }
            railGroupCorePositions.forEach {
                it[0] += difX
                it[1] += difY
                it[2] += difZ
            }
        }
        super.setPos(x, y, z, prevX, prevY, prevZ)
        railmap = null
    }

    override fun getRailShapeName(): String {
        val positions = logicalRailPositions
        if (!isRailSection() || positions == null) return super.getRailShapeName()
        return "Type:Normal, " +
            "X:${positions[1].blockX - positions[0].blockX}, " +
            "Y:${positions[1].blockY - positions[0].blockY}, " +
            "Z:${positions[1].blockZ - positions[0].blockZ}"
    }

    companion object {
        const val SECTION_TAG = "RailSection"
        private const val RATIO_EPSILON = 1.0E-7

        private fun copyRailPositions(positions: Array<RailPosition>): Array<RailPosition> =
            Array(positions.size) { RailPosition.readFromNBT(positions[it].writeToNBT()) }

        private fun copyPositions(positions: List<IntArray>): Array<IntArray> =
            Array(positions.size) { positions[it].clone() }
    }

    private data class BlockPos(val x: Int, val y: Int, val z: Int)
}
