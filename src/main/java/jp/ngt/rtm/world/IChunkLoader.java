package jp.ngt.rtm.world;

import net.minecraftforge.common.ForgeChunkManager.Ticket;

public interface IChunkLoader {
	/**
	 * チャンクローダー機能が有効かどうか
	 */
	boolean isChunkLoaderEnable();

	void forceChunkLoading(int chunkX, int chunkZ);

	void forceChunkLoading();

	void setChunkTicket(Ticket ticket);
}