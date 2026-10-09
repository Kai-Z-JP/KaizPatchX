package jp.ngt.rtm.world

import net.minecraft.world.ChunkCoordIntPair
import net.minecraft.world.World
import net.minecraft.world.WorldServer
import net.minecraftforge.common.ForgeChunkManager
import net.minecraftforge.common.ForgeChunkManager.Ticket

internal fun FormationChunkCoordinate.toMinecraftChunk(): ChunkCoordIntPair =
    ChunkCoordIntPair(x, z)

internal fun ChunkCoordIntPair.toFormationChunk(): FormationChunkCoordinate =
    FormationChunkCoordinate(chunkXPos, chunkZPos)

/**
 * Forge 1.7.10のforceChunkは未ロードチャンクを読み込まないため、
 * サーバースレッドで明示ロードしてから保持登録する。
 */
internal object FormationChunkLoader {
    fun loadAndForce(
        world: World,
        ticket: Ticket,
        chunk: ChunkCoordIntPair,
    ) {
        if (world is WorldServer &&
            !world.theChunkProviderServer.chunkExists(chunk.chunkXPos, chunk.chunkZPos)
        ) {
            world.theChunkProviderServer.loadChunk(chunk.chunkXPos, chunk.chunkZPos)
        }
        ForgeChunkManager.forceChunk(ticket, chunk)
    }
}
