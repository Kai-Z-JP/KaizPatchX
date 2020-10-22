package jp.ngt.ngtlib.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.NGTCore;
import jp.ngt.ngtlib.protection.ProtectionManager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;

import java.util.List;

public class ItemProtectionKey extends Item {
	public ItemProtectionKey() {
		this.setMaxStackSize(1);
	}

	public static ItemStack getKey(String code) {
		ItemStack stack = new ItemStack(NGTCore.protection_key, 1, 0);
		NBTTagCompound nbt = new NBTTagCompound();
		nbt.setString(ProtectionManager.KEY_ID, code);
		stack.setTagCompound(nbt);
		return stack;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean par4) {
		//list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("usage.train." + i));
		NBTTagCompound nbt = itemStack.getTagCompound();
		list.add(EnumChatFormatting.GRAY + "ID:" + nbt.getString(ProtectionManager.KEY_ID));
	}
}