package jp.ngt.rtm.rail.util;

import jp.ngt.rtm.modelpack.ModelPackManager;
import jp.ngt.rtm.modelpack.modelset.ModelSetRail;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public final class RailProperty {
	public final String railModel;
	public final Block block;
	public final int blockMetadata;
	public final float blockHeight;
	public final String unlocalizedName;

	private ModelSetRail modelSet;

	public RailProperty(String par1, Block par2, int par3, float par4) {
		this.railModel = par1;
		this.block = par2;
		this.blockMetadata = par3;
		this.blockHeight = (par4 <= 0.0F) ? 0.0625F : par4;

		Item item = Item.getItemFromBlock(par2);
		//空気ブロックとかでnull
		String s = (item == null) ? par2.getUnlocalizedName() : (new ItemStack(par2, 1, par3).getUnlocalizedName());
		this.unlocalizedName = s + ".name";
	}

	public static RailProperty readFromNBT(NBTTagCompound nbt) {
		String s0 = nbt.getString("RailModel");
		String s1 = nbt.getString("BlockName");
		Block block = Block.getBlockFromName(s1);
		if (block == null) {
			block = Blocks.air;
		}
		int i0 = nbt.getInteger("BlockMetadata");
		float b0 = nbt.getFloat("BlockHeight");
		return new RailProperty(s0, block, i0, b0);
	}

	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setString("RailModel", this.railModel);
		String s1 = Block.blockRegistry.getNameForObject(this.block);
		nbt.setString("BlockName", s1);
		nbt.setInteger("BlockMetadata", this.blockMetadata);
		nbt.setFloat("BlockHeight", this.blockHeight);
	}

	public ModelSetRail getModelSet() {
		if (this.modelSet == null || this.modelSet.isDummy()) {
			this.modelSet = ModelPackManager.INSTANCE.getModelSet("ModelRail", this.railModel);
		}
		return this.modelSet;
	}
}