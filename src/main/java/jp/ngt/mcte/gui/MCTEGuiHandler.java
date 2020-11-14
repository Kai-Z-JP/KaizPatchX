package jp.ngt.mcte.gui;

import cpw.mods.fml.common.network.IGuiHandler;
import jp.ngt.mcte.MCTE;
import jp.ngt.mcte.editor.EntityEditor;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

public class MCTEGuiHandler implements IGuiHandler {
	@Override
	public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == MCTE.guiIdEditor) {
            Entity entity = world.getEntityByID(x);
            if (entity instanceof EntityEditor) {
                return new ContainerEditor((EntityEditor) entity);
            }
        }

		return null;
	}

	@Override
	public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
		if (ID == MCTE.guiIdEditor) {
            Entity entity = world.getEntityByID(x);
            if (entity instanceof EntityEditor) {
                return new GuiEditor((EntityEditor) entity);
            }
        } else if (ID == MCTE.guiIdGenerator) {
			return new GuiGenerator(world, x, y, z);
		} else if (ID == MCTE.guiIdPainter) {
			return new GuiPainter(player);
		} else if (ID == MCTE.guiIdItemMiniature) {
			return new GuiItemMiniature(player);
		}

		return null;
	}
}