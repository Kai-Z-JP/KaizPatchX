package jp.ngt.rtm.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.entity.npc.EntityNPC;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;

public class ContainerNPC extends Container {
	private EntityPlayer player;
	private EntityNPC npc;

	public ContainerNPC(EntityPlayer par1, EntityNPC par2) {
		this.player = par1;
		this.npc = par2;

		int y = 84 + (18 * 3) + 4;

		//Playerインベントリ上段
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				int index = (j + i * 9) + 9;
				this.addSlotToContainer(new Slot(par1.inventory, index, 8 + j * 18, y));
			}
			y += 18;
		}

		y += 4;

		//Playerインベントリ下段
		for (int i = 0; i < 9; ++i) {
			this.addSlotToContainer(new Slot(par1.inventory, i, 8 + i * 18, y));
		}

		//NPCインベントリArmor
		for (int i = 0; i < 4; ++i) {
			int index = i + 27;
			final int armorIndex = i;
			this.addSlotToContainer(new Slot(par2.inventory, index, 8, 8 + i * 18) {
				@Override
				public int getSlotStackLimit() {
					return 1;
				}

				@Override
				public boolean isItemValid(ItemStack item) {
					if (item == null) return false;
					return item.getItem().isValidArmor(item, armorIndex, ContainerNPC.this.npc);
				}

				@Override
				@SideOnly(Side.CLIENT)
				public IIcon getBackgroundIconIndex() {
					return ItemArmor.func_94602_b(armorIndex);
				}
			});
		}

		//NPCインベントリ
		for (int i = 0; i < 3; ++i) {
			for (int j = 0; j < 9; ++j) {
				int index = j + i * 9;
				this.addSlotToContainer(new Slot(par2.inventory, index, 8 + j * 18, 84 + i * 18));
			}
		}
	}

	@Override
	public boolean canInteractWith(EntityPlayer player) {
		return player.getDistanceSqToEntity(this.npc) < 64.0D;
	}

	@Override
	protected void retrySlotClick(int index, int p_75133_2_, boolean p_75133_3_, EntityPlayer player) {
	}
}