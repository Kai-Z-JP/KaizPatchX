package jp.ngt.rtm.world

import jp.ngt.ngtlib.io.NGTLog
import jp.ngt.rtm.RTMCore
import jp.ngt.rtm.entity.train.EntityTrainBase
import jp.ngt.rtm.entity.train.util.Formation
import net.minecraft.world.ChunkCoordIntPair
import net.minecraft.world.World
import net.minecraftforge.common.ForgeChunkManager
import net.minecraftforge.common.ForgeChunkManager.Ticket
import net.minecraftforge.common.ForgeChunkManager.Type
import java.util.*

/**
 * 編成全体を単位としてForgeの強制ロードTicketを管理する。
 *
 * EntityTrainBaseが従来保持していた車両単位Ticketとは関連付けず、代表車両UUIDは
 * 所有権の記録にだけ使用する。復元はTicket NBTの保持チャンクから行う。
 */
object FormationChunkRetention {
    private const val RETRY_INTERVAL_TICKS = 100L
    private const val FAILURE_LOG_INTERVAL_TICKS = 1_200L
    private const val ORPHAN_GRACE_TICKS = 1_200L

    private class ManagedTicket(
        val ticket: Ticket,
        val assignedChunks: MutableSet<ChunkCoordIntPair>,
    )

    private class RetentionState(
        val formationId: Long,
        val createdTick: Long,
    ) {
        val tickets = TreeMap<Int, ManagedTicket>()
        var lastSeenTick = createdTick
        var lastRequiredTicketCount = 0
        var nextRetryTick = 0L
        var lastFailureLogTick: Long? = null
    }

    private class WorldState {
        var tick = 0L
        val formations = linkedMapOf<Long, RetentionState>()
    }

    private val worlds = IdentityHashMap<World, WorldState>()

    @JvmStatic
    fun isFormationTicket(ticket: Ticket): Boolean {
        return FormationChunkTicketData.isFormationTicket(ticket)
    }

    @JvmStatic
    @Synchronized
    fun adoptTicket(ticket: Ticket, world: World) {
        if (!isFormationTicket(ticket) || ticket.world !== world) {
            return
        }

        val data = ticket.modData
        val index = data.getInteger(FormationChunkTicketData.KEY_INDEX)
        val hasEntityAnchor = ticket.entity != null
        if (index < 0 || (hasEntityAnchor && index != 0)) {
            ForgeChunkManager.releaseTicket(ticket)
            return
        }

        val worldState = worlds.getOrPut(world, ::WorldState)
        val formationId = data.getLong(FormationChunkTicketData.KEY_FORMATION_ID)
        val state = worldState.formations.getOrPut(formationId) {
            RetentionState(formationId, worldState.tick)
        }
        val existing = state.tickets[index]
        if (existing != null && existing.ticket !== ticket) {
            ForgeChunkManager.releaseTicket(ticket)
            return
        }

        val capacity = ticket.getMaxChunkListDepth()
        val restoredChunks = FormationChunkTicketData.readChunks(ticket)
            .let { chunks ->
                if (capacity > 0) {
                    chunks.take(capacity).toCollection(linkedSetOf())
                } else {
                    chunks
                }
            }

        val managedTicket = if (ticket.entity is EntityTrainBase) {
            val leader = ticket.entity as EntityTrainBase
            restoredChunks.forEach { chunk ->
                FormationChunkLoader.loadAndForce(world, ticket, chunk)
            }
            ForgeChunkManager.releaseTicket(ticket)

            val replacement = ForgeChunkManager.requestTicket(RTMCore.instance, world, Type.NORMAL)
                ?: return
            FormationChunkTicketData.configure(replacement, formationId, index, leader)
            restoredChunks.forEach { chunk ->
                FormationChunkLoader.loadAndForce(world, replacement, chunk)
            }
            replacement
        } else {
            restoredChunks.forEach { chunk ->
                FormationChunkLoader.loadAndForce(world, ticket, chunk)
            }
            ticket
        }
        FormationChunkTicketData.writeChunks(managedTicket, restoredChunks)
        state.tickets[index] = ManagedTicket(managedTicket, restoredChunks.toMutableSet())
    }

    @JvmStatic
    @Synchronized
    fun update(world: World, formations: Collection<Formation>) {
        if (world.isRemote) {
            return
        }

        val worldState = worlds.getOrPut(world, ::WorldState)
        val now = ++worldState.tick
        val formationById = formations.associateBy { it.id }
        val processed = hashSetOf<Long>()

        formationById.values.forEach { formation ->
            val allLiveTrains = FormationChunkOccupancy.liveTrains(formation)
            val trainsInWorld = allLiveTrains.filter { it.worldObj === world }
            if (trainsInWorld.isEmpty()) {
                if (allLiveTrains.isNotEmpty()) {
                    releaseState(worldState.formations.remove(formation.id))
                }
                return@forEach
            }

            processed += formation.id
            val state = worldState.formations.getOrPut(formation.id) {
                RetentionState(formation.id, now)
            }
            state.lastSeenTick = now
            reconcile(world, state, trainsInWorld, now)
        }

        val iterator = worldState.formations.iterator()
        while (iterator.hasNext()) {
            val (formationId, state) = iterator.next()
            if (formationId !in processed &&
                formationById[formationId]?.let(FormationChunkOccupancy::liveTrains).orEmpty().isEmpty() &&
                now - state.lastSeenTick >= ORPHAN_GRACE_TICKS
            ) {
                releaseState(state)
                iterator.remove()
            }
        }
    }

    @JvmStatic
    @Synchronized
    fun releaseFormation(formationId: Long) {
        worlds.values.forEach { worldState ->
            releaseState(worldState.formations.remove(formationId))
        }
    }

    @JvmStatic
    @Synchronized
    fun releaseAllFormations() {
        worlds.values.forEach { worldState ->
            worldState.formations.values.forEach(::releaseState)
            worldState.formations.clear()
        }
    }

    @JvmStatic
    @Synchronized
    fun onWorldUnload(world: World) {
        // Forgeが保存とWorld側のTicket破棄を担当する。Unloadイベント内で
        // unforceするとForge側のWorld mapが先に破棄済みの場合があるため、
        // ここではRTMの所有参照だけを切る。
        worlds.remove(world)?.formations?.values?.forEach { it.tickets.clear() }
    }

    private fun reconcile(
        world: World,
        state: RetentionState,
        trains: List<EntityTrainBase>,
        now: Long,
    ) {
        val leader = trains.first()
        val occupied = FormationChunkOccupancy.collect(world, trains)
        val retained = FormationChunkPlanner.expand(occupied)
        if (retained.isEmpty()) {
            releaseState(state)
            return
        }

        if (state.tickets.isEmpty() && now >= state.nextRetryTick) {
            requestTicket(world, state, 0, leader, now)
        }
        if (state.tickets.isEmpty()) {
            reportTicketShortage(state, 1, now)
            return
        }

        val capacity = state.tickets.firstEntry().value.ticket.getMaxChunkListDepth()
        val groups = FormationChunkPlanner.partition(retained, capacity)
        val requiredCount = groups.size
        val layoutChanged = requiredCount != state.lastRequiredTicketCount

        if (layoutChanged || now >= state.nextRetryTick) {
            for (index in groups.indices) {
                if (index !in state.tickets &&
                    !requestTicket(world, state, index, leader, now)
                ) {
                    break
                }
            }
        }

        state.tickets.keys.filter { it >= requiredCount }.forEach { index ->
            releaseManagedTicket(state.tickets.remove(index))
        }

        groups.forEachIndexed { index, group ->
            val managed = state.tickets[index] ?: return@forEachIndexed
            FormationChunkTicketData.configure(managed.ticket, state.formationId, index, leader)
            updateAssignedChunks(world, managed, group.mapTo(linkedSetOf()) { it.toMinecraftChunk() })
        }

        val missingCount = groups.indices.count { it !in state.tickets }
        if (missingCount == 0) {
            state.nextRetryTick = 0L
            state.lastFailureLogTick = null
        } else {
            reportTicketShortage(state, missingCount, now)
        }
        state.lastRequiredTicketCount = requiredCount
    }

    private fun requestTicket(
        world: World,
        state: RetentionState,
        index: Int,
        leader: EntityTrainBase,
        now: Long,
    ): Boolean {
        val ticket = ForgeChunkManager.requestTicket(RTMCore.instance, world, Type.NORMAL)
        if (ticket == null) {
            state.nextRetryTick = now + RETRY_INTERVAL_TICKS
            return false
        }

        FormationChunkTicketData.configure(ticket, state.formationId, index, leader)
        state.tickets[index] = ManagedTicket(ticket, linkedSetOf())
        return true
    }

    private fun updateAssignedChunks(
        world: World,
        managed: ManagedTicket,
        desired: Set<ChunkCoordIntPair>,
    ) {
        val currentCoordinates = managed.assignedChunks.map { it.toFormationChunk() }
        val desiredCoordinates = desired.map { it.toFormationChunk() }
        val delta = FormationChunkPlanner.difference(currentCoordinates, desiredCoordinates)
        delta.toUnforce.forEach {
            ForgeChunkManager.unforceChunk(managed.ticket, it.toMinecraftChunk())
        }
        delta.toForce.forEach {
            FormationChunkLoader.loadAndForce(world, managed.ticket, it.toMinecraftChunk())
        }
        managed.assignedChunks.clear()
        managed.assignedChunks += desired
        FormationChunkTicketData.writeChunks(managed.ticket, desired)
    }

    private fun reportTicketShortage(
        state: RetentionState,
        missingCount: Int,
        now: Long,
    ) {
        if (state.nextRetryTick <= now) {
            state.nextRetryTick = now + RETRY_INTERVAL_TICKS
        }

        val previous = state.lastFailureLogTick
        if (previous == null || now - previous >= FAILURE_LOG_INTERVAL_TICKS) {
            NGTLog.debug(
                "[RTM] Formation %d is missing %d chunk ticket(s); operation continues, retrying in 100 ticks.",
                state.formationId,
                missingCount,
            )
            state.lastFailureLogTick = now
        }
    }

    private fun releaseState(state: RetentionState?) {
        state?.tickets?.values?.forEach(::releaseManagedTicket)
        state?.tickets?.clear()
    }

    private fun releaseManagedTicket(managed: ManagedTicket?) {
        managed ?: return
        managed.assignedChunks.toList().forEach { chunk ->
            ForgeChunkManager.unforceChunk(managed.ticket, chunk)
        }
        ForgeChunkManager.releaseTicket(managed.ticket)
        managed.assignedChunks.clear()
    }
}
