package jp.ngt.rtm.world

import jp.ngt.rtm.entity.train.EntityTrainBase
import net.minecraft.world.ChunkCoordIntPair
import net.minecraftforge.common.ForgeChunkManager.Ticket

internal object FormationChunkTicketData {
    private const val VERSION = 2
    private const val KEY_KIND = "RTMChunkTicketType"
    private const val KIND_FORMATION = "Formation"
    private const val KEY_VERSION = "version"
    private const val KEY_CHUNKS = "chunks"
    private const val KEY_LEADER_UUID_MOST = "leaderUuidMost"
    private const val KEY_LEADER_UUID_LEAST = "leaderUuidLeast"
    const val KEY_FORMATION_ID = "formationId"
    const val KEY_INDEX = "index"

    fun isFormationTicket(ticket: Ticket): Boolean {
        val data = ticket.modData
        return data.getString(KEY_KIND) == KIND_FORMATION &&
                data.getInteger(KEY_VERSION) == VERSION &&
                data.hasKey(KEY_FORMATION_ID) &&
                data.hasKey(KEY_INDEX)
    }

    fun configure(
        ticket: Ticket,
        formationId: Long,
        index: Int,
        leader: EntityTrainBase,
    ) {
        val data = ticket.modData
        data.setString(KEY_KIND, KIND_FORMATION)
        data.setInteger(KEY_VERSION, VERSION)
        data.setLong(KEY_FORMATION_ID, formationId)
        data.setInteger(KEY_INDEX, index)
        data.setLong(KEY_LEADER_UUID_MOST, leader.uniqueID.mostSignificantBits)
        data.setLong(KEY_LEADER_UUID_LEAST, leader.uniqueID.leastSignificantBits)
        val maxDepth = ticket.getMaxChunkListDepth()
        if (maxDepth > 0 && ticket.chunkListDepth != maxDepth) {
            ticket.setChunkListDepth(maxDepth)
        }
    }

    fun writeChunks(ticket: Ticket, chunks: Collection<ChunkCoordIntPair>) {
        ticket.modData.setIntArray(KEY_CHUNKS, encodeChunks(chunks))
    }

    fun readChunks(ticket: Ticket): Set<ChunkCoordIntPair> {
        return decodeChunks(ticket.modData.getIntArray(KEY_CHUNKS))
    }

    internal fun encodeChunks(chunks: Collection<ChunkCoordIntPair>): IntArray {
        val sorted = chunks
            .map { FormationChunkCoordinate(it.chunkXPos, it.chunkZPos) }
            .distinct()
            .sorted()
        return IntArray(sorted.size * 2).also { encoded ->
            sorted.forEachIndexed { index, chunk ->
                encoded[index * 2] = chunk.x
                encoded[index * 2 + 1] = chunk.z
            }
        }
    }

    internal fun decodeChunks(encoded: IntArray): Set<ChunkCoordIntPair> {
        val chunkCount = encoded.size / 2
        return LinkedHashSet<ChunkCoordIntPair>(chunkCount).also { chunks ->
            repeat(chunkCount) { index ->
                chunks += ChunkCoordIntPair(encoded[index * 2], encoded[index * 2 + 1])
            }
        }
    }
}
