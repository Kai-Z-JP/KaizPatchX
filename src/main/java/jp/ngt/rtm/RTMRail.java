package jp.ngt.rtm;

import cpw.mods.fml.common.registry.GameRegistry;
import jp.ngt.rtm.rail.*;
import jp.ngt.rtm.rail.util.RailProperty;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

public final class RTMRail {
	private static final String[] railTexture = {"rtm:tp", "rtm:tp", "gravel", "stone", "snow", "rtm:asphalt"};

	public static Block largeRailBase0;
	public static Block largeRailCore0;
	public static Block largeRailSwitchBase0;
	public static Block largeRailSwitchCore0;
	public static Block largeRailSlopeBase0;
	public static Block largeRailSlopeCore0;

	public static Block[] largeRailBase = new Block[railTexture.length];
	public static Block[] largeRailCore = new Block[railTexture.length];
	public static Block[] largeRailSwitchBase = new Block[railTexture.length];
	public static Block[] largeRailSwitchCore = new Block[railTexture.length];
	public static Block[] largeRailSlopeBase = new Block[railTexture.length];
	public static Block[] largeRailSlopeCore = new Block[railTexture.length];

	public static void init() {
		largeRailBase0 = (new BlockLargeRailBase(2)).setBlockName("rtm:LRBase").setBlockTextureName(railTexture[2]);
		largeRailCore0 = (new BlockLargeRailCore(2)).setBlockName("rtm:LRCore").setBlockTextureName(railTexture[2]);
		largeRailSwitchBase0 = (new BlockLargeRailSwitchBase(2)).setBlockName("rtm:LRSBase").setBlockTextureName(railTexture[2]);
		largeRailSwitchCore0 = (new BlockLargeRailSwitchCore(2)).setBlockName("rtm:LRSCore").setBlockTextureName(railTexture[3]);
		largeRailSlopeBase0 = (new BlockLargeRailSlopeBase(2)).setBlockName("rtm:LRLBase").setBlockTextureName(railTexture[2]);
		largeRailSlopeCore0 = (new BlockLargeRailSlopeCore(2)).setBlockName("rtm:LRLCore").setBlockTextureName(railTexture[2]);

		for (int i = 0; i < railTexture.length; ++i) {
			largeRailBase[i] = (new BlockLargeRailBase(i)).setBlockName("rtm:LRBase." + i).setBlockTextureName(railTexture[i]);
			largeRailCore[i] = (new BlockLargeRailCore(i)).setBlockName("rtm:LRCore." + i).setBlockTextureName(railTexture[i]);
			largeRailSwitchBase[i] = (new BlockLargeRailSwitchBase(i)).setBlockName("rtm:LRSBase." + i).setBlockTextureName(railTexture[i]);
			largeRailSwitchCore[i] = (new BlockLargeRailSwitchCore(i)).setBlockName("rtm:LRSCore." + i).setBlockTextureName(railTexture[i]);
			largeRailSlopeBase[i] = (new BlockLargeRailSlopeBase(i)).setBlockName("rtm:LRLBase." + i).setBlockTextureName(railTexture[i]);
			largeRailSlopeCore[i] = (new BlockLargeRailSlopeCore(i)).setBlockName("rtm:LRLCore." + i).setBlockTextureName(railTexture[i]);
		}

		GameRegistry.registerBlock(largeRailBase0, "rtm:LRBase");
		GameRegistry.registerBlock(largeRailCore0, "rtm:LRCore");
		GameRegistry.registerBlock(largeRailSwitchBase0, "rtm:LRSBase");
		GameRegistry.registerBlock(largeRailSwitchCore0, "rtm:LRSCore");
		GameRegistry.registerBlock(largeRailSlopeBase0, "rtm:LRLBase");
		GameRegistry.registerBlock(largeRailSlopeCore0, "rtm:LRLCore");

		for (int i = 0; i < railTexture.length; ++i)//インスタンス使いまわして登録は不可
		{
			GameRegistry.registerBlock(largeRailBase[i], "LRBase_" + i);
			GameRegistry.registerBlock(largeRailCore[i], "LRCore_" + i);
			GameRegistry.registerBlock(largeRailSwitchBase[i], "LRSBase_" + i);
			GameRegistry.registerBlock(largeRailSwitchCore[i], "LRSCore_" + i);
			GameRegistry.registerBlock(largeRailSlopeBase[i], "LRLBase_" + i);
			GameRegistry.registerBlock(largeRailSlopeCore[i], "LRLCore_" + i);
		}

		GameRegistry.registerTileEntity(TileEntityLargeRailBase.class, "TERailBase");
		GameRegistry.registerTileEntity(TileEntityLargeRailNormalCore.class, "TERailCore");
		GameRegistry.registerTileEntity(TileEntityLargeRailSwitchBase.class, "TERailSwitchBase");
		GameRegistry.registerTileEntity(TileEntityLargeRailSwitchCore.class, "TERailSwitchCore");
		GameRegistry.registerTileEntity(TileEntityLargeRailSlopeBase.class, "TERailSlopeBase");
		GameRegistry.registerTileEntity(TileEntityLargeRailSlopeCore.class, "TERailSlopeCore");
		GameRegistry.registerTileEntity(TileEntityMarker.class, "TEMarker");
	}

	//互換性
	public static RailProperty getProperty(byte shape, int texType) {
		String s0 = "1067mm";
		switch (texType) {
			case 0:
				switch (shape) {
					case 0:
						s0 = "Monorail_StraddleBeam";
						break;
					case 1:
						s0 = "GuidingRail";
						break;
					case 2:
						s0 = "1067mm_Abt";
						break;
					case 3:
						s0 = "1067mm_HP_Wood";
						break;
				}
				break;
			case 5:
				switch (shape & 1) {
					case 0:
						s0 = "1067mm_Tram";
						break;
					case 1:
						s0 = "1435mm_Tram";
						break;
				}
				break;
			default:
				switch (shape) {
					case 0:
						s0 = "1067mm_Wood";
						break;
					case 2:
						s0 = "1067mm_PC";
						break;
					case 1:
						s0 = "1435mm_Wood";
						break;
					case 3:
						s0 = "1435mm_PC";
						break;
					case 4:
						s0 = "1524mm_Wood";
						break;
					case 6:
						s0 = "1524mm_PC";
						break;
					case 5:
						s0 = "762mm_Wood";
						break;
					case 7:
						s0 = "762mm_PC";
						break;
				}
				break;
		}

		Block block = Blocks.gravel;
		int meta = 0;
		switch (texType) {
			case 0:
			case 1:
				block = Blocks.air;
				break;
			case 2:
				block = Blocks.gravel;
				break;
			case 3:
				block = Blocks.stone;
				break;
			case 4:
				block = Blocks.snow;
				break;
			case 5:
				block = Blocks.wool;
				meta = 7;
				break;
		}
		float h = (texType == 4) ? 0.18125F + 0.0625F : 0.0625F;
		return new RailProperty(s0, block, meta, h);
	}
}