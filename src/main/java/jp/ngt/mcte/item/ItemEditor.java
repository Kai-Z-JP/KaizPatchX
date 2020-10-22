package jp.ngt.mcte.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.mcte.editor.Editor;
import jp.ngt.mcte.editor.EditorManager;
import jp.ngt.mcte.editor.EntityEditor;
import jp.ngt.ngtlib.io.NGTLog;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.StatCollector;
import net.minecraft.world.World;

import java.util.List;

public class ItemEditor extends Item {
	public ItemEditor() {
		super();
		this.setMaxStackSize(1);
	}

	@Override
	public ItemStack onItemRightClick(ItemStack itemstack, World world, EntityPlayer player) {
		if (world.isRemote) {
			return itemstack;
		} else {
			if (EditorManager.INSTANCE.canPlayerUseEditor(player)) {
				Editor editor = EditorManager.INSTANCE.getEditor(player);
				if (editor != null) {
					MovingObjectPosition target = editor.getEntity().getTarget(false);
					if (target != null) {
						editor.getEntity().setPos(editor.getEntity().isSelectEnd(), target.blockX, target.blockY, target.blockZ);
					}
					return itemstack;
				}
			} else {
				NGTLog.sendChatMessage(player, "You don't have permission to use Editor.");
			}
			return itemstack;
		}
	}

	@Override
	public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10) {
		if (!world.isRemote) {
			if (EditorManager.INSTANCE.canPlayerUseEditor(player)) {
				Editor editor = EditorManager.INSTANCE.getEditor(player);
				if (editor != null) {
					editor.getEntity().setPos(editor.getEntity().isSelectEnd(), x, y, z);
					return true;
				}

				EntityEditor entityEditor = Editor.getNewEditor(world, player, x, y, z);
				entityEditor.setPositionAndRotation(player.posX, player.posY, player.posZ, 0.0F, 0.0F);
				world.spawnEntityInWorld(entityEditor);
			} else {
				NGTLog.sendChatMessage(player, "You don't have permission to use Editor.");
			}
		}
		return true;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void addInformation(ItemStack itemstack, EntityPlayer player, List list, boolean par4) {
		for (int i = 0; i < 8; ++i) {
			list.add(EnumChatFormatting.GRAY + StatCollector.translateToLocal("usage.editor." + i));
		}
	}
}