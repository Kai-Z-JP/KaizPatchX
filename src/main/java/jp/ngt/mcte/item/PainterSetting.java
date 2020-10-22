package jp.ngt.mcte.item;

import jp.ngt.ngtlib.block.BlockSet;
import net.minecraft.init.Blocks;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class PainterSetting {
	/**
	 * 埋め立てるブロック
	 */
	public BlockSet fillBlock;
	/**
	 * 上書きするブロック
	 */
	public BlockSet rewriteBlock;
	/**
	 * 埋め立てるかどうか
	 */
	public boolean fill;
	/**
	 * 上書きするかどうか
	 */
	public boolean rewrite;
	/**
	 * 0:ブロック表面, 1:空中
	 */
	public byte drawMode;
	/**
	 * 半径
	 */
	public int size;

	protected PainterSetting() {
	}

	public static PainterSetting getPainterSettingFromItem(ItemStack stack) {
		PainterSetting setting = new PainterSetting();
		NBTTagCompound nbt = stack.getTagCompound();//null
		if (nbt == null) {
			nbt = new NBTTagCompound();
			stack.setTagCompound(nbt);
		}

		if (nbt.hasKey("fillBlock")) {
			setting.fillBlock = BlockSet.readFromNBT(nbt.getCompoundTag("fillBlock"));
		} else {
			setting.fillBlock = new BlockSet(Blocks.air, 0);
		}

		if (nbt.hasKey("rewriteBlock")) {
			setting.rewriteBlock = BlockSet.readFromNBT(nbt.getCompoundTag("rewriteBlock"));
		} else {
			setting.rewriteBlock = new BlockSet(Blocks.air, 0);
		}

		setting.fill = nbt.getBoolean("fill");
		setting.rewrite = nbt.getBoolean("rewrite");
		setting.drawMode = nbt.getByte("drawMode");
		setting.size = nbt.getInteger("size");

		if (!setting.fill && !setting.rewrite) {
			setting.fill = true;
		}

		if (setting.size <= 0) {
			setting.size = 1;
		}
		return setting;
	}

	public void writePainterSettingToItem(ItemStack stack) {
		NBTTagCompound nbt = stack.getTagCompound();
		if (nbt == null) {
			nbt = new NBTTagCompound();
			stack.setTagCompound(nbt);
		}
		nbt.setTag("fillBlock", this.fillBlock.writeToNBT());
		nbt.setTag("rewriteBlock", this.rewriteBlock.writeToNBT());
		nbt.setBoolean("fill", this.fill);
		nbt.setBoolean("rewrite", this.rewrite);
		nbt.setByte("drawMode", this.drawMode);
		nbt.setInteger("size", this.size);
	}
}