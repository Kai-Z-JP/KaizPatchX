package jp.ngt.rtm.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;

public class PaintProperty {
	public int color = 0xFFFFFF;
	public int alpha = 0xFF;
	public int radius = 1;
	public int type = 0;

	public static PaintProperty readFromNBT(NBTTagCompound nbt) {
		PaintProperty prop = new PaintProperty();
		prop.color = nbt.getInteger("Color");
		prop.alpha = nbt.getInteger("Alpha");
		prop.radius = nbt.getInteger("Radius");
		prop.type = nbt.getInteger("Type");
		return prop;
	}

	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("Color", this.color);
		nbt.setInteger("Alpha", this.alpha);
		nbt.setInteger("Radius", this.radius);
		nbt.setInteger("Type", this.type);
	}

	public static PaintProperty getProperty(ItemStack stack) {
		if (stack.hasTagCompound() && stack.getTagCompound().hasKey("PaintProp")) {
			return readFromNBT(stack.getTagCompound().getCompoundTag("PaintProp"));
		}
		return new PaintProperty();
	}

	public void setProperty(ItemStack stack) {
		NBTTagCompound itemNBT = stack.hasTagCompound() ? stack.getTagCompound() : new NBTTagCompound();
		NBTTagCompound nbt = new NBTTagCompound();
		this.writeToNBT(nbt);
		itemNBT.setTag("PaintProp", nbt);
		stack.setTagCompound(itemNBT);
	}

	public enum EnumPaintType {
		pen_circle(0, 0),
		pen_square(32, 0),
		brush(64, 0),
		eraser_circle(96, 0),
		eraser_square(128, 0);

		public final int iconU;
		public final int iconV;

		private EnumPaintType(int u, int v) {
			this.iconU = u;
			this.iconV = v;
		}
	}
}