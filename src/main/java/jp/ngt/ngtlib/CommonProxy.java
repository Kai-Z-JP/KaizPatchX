package jp.ngt.ngtlib;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;

import java.io.File;

public class CommonProxy {
	public boolean isServer() {
		return true;
	}

	public World getWorld() {
		return null;
	}

	public EntityPlayer getPlayer() {
		return null;
	}

	public File getMinecraftDirectory(String folder) {
		return MinecraftServer.getServer().getFile(folder);
	}

	public String getUserName() {
		return "";
	}

	public int getNewRenderType() {
		return -1;
	}

	public void preInit() {
	}

	public void init() {
	}

	public void postInit() {
	}

	public void removeGuiWarning() {
	}

	public void breakBlock(World world, int x, int y, int z, int meta) {
		world.setBlockToAir(x, y, z);
	}

	public void zoom(EntityPlayer player, int count) {
	}

	public int getChunkLoadDistance() {
		return 256;
	}
}