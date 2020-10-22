package jp.ngt.mcte.world;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.mcte.gui.GuiCreatePictorialWorld;
import jp.ngt.ngtlib.io.NGTLog;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.WorldChunkManager;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.ChunkProviderGenerate;
import net.minecraft.world.gen.layer.GenLayer;

public class WorldTypePictorial extends WorldType {
	public static WorldType PICTORIAL;

	public static void init() {
		//WorldTypeのコンストラクタで登録は行われる
		//WorldType.parseWorldType()でサーバーのコンフィグからWorldTypeを取得(大小文字区別なし)
		PICTORIAL = new WorldTypePictorial("pictorial");
	}

	public WorldTypePictorial(String name) {
		super(name);
	}

	@Override
	public WorldChunkManager getChunkManager(World world) {
		WorldData worldData = WorldData.getWorldData(world, world.getWorldInfo().getGeneratorOptions());

		if (worldData == null || !worldData.getWorldGenerator().hasBiomesData()) {
			NGTLog.debug("set default ChunkManager");
			return new WorldChunkManager(world);
		} else {
			return new ChunkManagerPictorialCustom(world, worldData);
		}
	}

	@Override
	public IChunkProvider getChunkGenerator(World world, String generatorOptions) {
		WorldData worldData = WorldData.getWorldData(world, generatorOptions);

		if (worldData == null || !worldData.getWorldGenerator().hasTerrainData()) {
			NGTLog.debug("set default ChunkGenerator");
			return new ChunkProviderGenerate(world, world.getSeed(), world.getWorldInfo().isMapFeaturesEnabled());
		} else {
			return new ChunkProviderPictorialCustom(world, world.getSeed(), world.getWorldInfo().isMapFeaturesEnabled(), worldData);
		}
	}

	@Override
	public int getMinimumSpawnHeight(World world) {
		return (int) this.getHorizon(world) + 1;
	}

	public double getHorizon(World world) {
		WorldData worldData = WorldData.getWorldData(world, world.getWorldInfo().getGeneratorOptions());
		if (worldData != null) {
			return worldData.seaLevel;
		}
		return 63.0D;
	}

	@Override
	public boolean isCustomizable() {
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void onCustomizeButton(Minecraft instance, GuiCreateWorld guiCreateWorld) {
		instance.displayGuiScreen(new GuiCreatePictorialWorld(guiCreateWorld));
	}

	@Override
	public GenLayer getBiomeLayer(long worldSeed, GenLayer parentLayer) {
		return new GenLayerPictorialCustom(worldSeed);
	}
}