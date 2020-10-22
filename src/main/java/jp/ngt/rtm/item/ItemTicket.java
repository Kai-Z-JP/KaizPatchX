package jp.ngt.rtm.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.StatCollector;

import java.util.List;

public class ItemTicket extends Item {
	public final int ticketType;

	public ItemTicket(int par1) {
		super();
		this.setHasSubtypes(true);
		this.setMaxStackSize(1);
		this.ticketType = par1;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void getSubItems(Item par1, CreativeTabs tabs, List list) {
		switch (this.ticketType) {
			case 0:
				list.add(new ItemStack(par1, 1, 1));
				break;
			case 1:
				list.add(new ItemStack(par1, 1, 11));
				break;
			case 2:
				list.add(new ItemStack(par1, 1, 0));
				break;
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean par4) {
		if (this.ticketType == 1) {
			String s = StatCollector.translateToLocal("item.ticket.remaining");
			list.add(EnumChatFormatting.GRAY + s + ":" + String.valueOf(itemStack.getItemDamage()));
		}

		NBTTagCompound nbt = itemStack.getTagCompound();
		if (nbt != null && nbt.getBoolean("Entered")) {
			String s = StatCollector.translateToLocal("item.ticket.entered");
			list.add(EnumChatFormatting.GRAY + s);
		}
	}

	@Override
	@SideOnly(Side.CLIENT)
	public boolean hasEffect(ItemStack itemSTack) {
		return this.ticketType == 2;
	}

	public static ItemStack consumeTicket(ItemStack itemStack) {
		int damage = itemStack.getItemDamage();
		Item item = itemStack.getItem();
		NBTTagCompound nbt = itemStack.getTagCompound();
		--itemStack.stackSize;

		if (nbt != null && nbt.getBoolean("Entered")) {
			if (damage == 0) {
				return null;
			}
			return new ItemStack(item, 1, damage);
		} else if (damage > 0) {
			ItemStack itemStack2 = new ItemStack(item, 1, --damage);
			NBTTagCompound nbt2 = new NBTTagCompound();
			nbt2.setBoolean("Entered", true);
			itemStack2.setTagCompound(nbt2);
			return itemStack2;
		}
		return null;
	}
}