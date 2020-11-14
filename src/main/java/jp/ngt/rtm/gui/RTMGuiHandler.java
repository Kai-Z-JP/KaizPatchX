package jp.ngt.rtm.gui;

import cpw.mods.fml.common.network.IGuiHandler;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.block.tileentity.TileEntityMovingMachine;
import jp.ngt.rtm.block.tileentity.TileEntityStation;
import jp.ngt.rtm.block.tileentity.TileEntityTrainWorkBench;
import jp.ngt.rtm.electric.TileEntitySignalConverter;
import jp.ngt.rtm.electric.TileEntitySpeaker;
import jp.ngt.rtm.electric.TileEntityTicketVendor;
import jp.ngt.rtm.entity.npc.EntityMotorman;
import jp.ngt.rtm.entity.npc.EntityNPC;
import jp.ngt.rtm.entity.npc.Role;
import jp.ngt.rtm.entity.train.EntityFreightCar;
import jp.ngt.rtm.entity.train.EntityTrainBase;
import jp.ngt.rtm.entity.train.parts.EntityContainer;
import jp.ngt.rtm.gui.rail.GuiRailMarker;
import jp.ngt.rtm.modelpack.IModelSelector;
import jp.ngt.rtm.modelpack.IModelSelectorWithType;
import jp.ngt.rtm.modelpack.texture.ITextureHolder;
import jp.ngt.rtm.rail.TileEntityMarker;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class RTMGuiHandler implements IGuiHandler {
    @Override
    public Object getServerGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == RTMCore.guiIdFreightCar) {
            Entity entity = world.getEntityByID(x);
            if (entity instanceof EntityFreightCar) {
                return new ContainerFreightCar(player.inventory, (EntityFreightCar) entity);
            }
        } else if (ID == RTMCore.guiIdItemContainer) {
            Entity entity = world.getEntityByID(x);
            if (entity instanceof EntityContainer) {
                return new ContainerItemContainer(player.inventory, (EntityContainer) entity);
            }
        } else if (ID == RTMCore.guiIdTrainControlPanel) {
            Entity entity0 = world.getEntityByID(x);
            Entity entity1 = entity0.riddenByEntity;
            if (entity0 instanceof EntityTrainBase && entity1 instanceof EntityPlayer) {
                return new ContainerTrainControlPanel((EntityTrainBase) entity0, (EntityPlayer) entity1);
            }
        } else if (ID == RTMCore.guiIdTrainWorkBench) {
            TileEntityTrainWorkBench tile = (TileEntityTrainWorkBench) world.getTileEntity(x, y, z);
            return new ContainerRTMWorkBench(player.inventory, world, tile, player.capabilities.isCreativeMode);
        } else if (ID == RTMCore.guiIdTicketVendor) {
            TileEntity tile = world.getTileEntity(x, y, z);
            if (tile instanceof TileEntityTicketVendor) {
                return new ContainerTicketVendor(player.inventory, (TileEntityTicketVendor) tile);
            }
        } else if (ID == RTMCore.guiIdNPC) {
            return new ContainerNPC(player, (EntityNPC) world.getEntityByID(x));
        }
        return null;
    }

    @Override
    public Object getClientGuiElement(int ID, EntityPlayer player, World world, int x, int y, int z) {
        if (ID == RTMCore.guiIdSelectEntityModel) {
            Entity entity = world.getEntityByID(x);
            if (entity instanceof IModelSelectorWithType) {
                return new GuiSelectModel(world, (IModelSelectorWithType) entity);
            } else if (entity instanceof IModelSelector) {
                return new GuiSelectModel(world, (IModelSelector) entity);
            }
        } else if (ID == RTMCore.guiIdSelectTileEntityModel) {
            TileEntity tile = world.getTileEntity(x, y, z);
            if (tile instanceof IModelSelectorWithType) {
                return new GuiSelectModel(world, (IModelSelectorWithType) tile);
            } else if (tile instanceof IModelSelector) {
                return new GuiSelectModel(world, (IModelSelector) tile);
            }
        } else if (ID == RTMCore.guiIdSelectItemModel) {
            Item item = player.inventory.getCurrentItem().getItem();
            return new GuiSelectModel(world, (IModelSelectorWithType) item);
        } else if (ID == RTMCore.guiIdFreightCar) {
            Entity entity = world.getEntityByID(x);
            if (entity instanceof EntityFreightCar) {
                return new GuiFreightCar(player.inventory, (EntityFreightCar) entity);
            }
        } else if (ID == RTMCore.guiIdItemContainer) {
            Entity entity = world.getEntityByID(x);
            if (entity instanceof EntityContainer) {
                return new GuiItemContainer(player.inventory, (EntityContainer) entity);
            }
        } else if (ID == RTMCore.guiIdSelectTexture) {
            return new GuiSelectTexture((ITextureHolder) world.getTileEntity(x, y, z));
        } else if (ID == RTMCore.guiIdTrainControlPanel) {
            Entity entity0 = world.getEntityByID(x);
            Entity entity1 = entity0.riddenByEntity;
            if (entity0 instanceof EntityTrainBase && entity1 instanceof EntityPlayer) {
                return new GuiTrainControlPanel(new ContainerTrainControlPanel((EntityTrainBase) entity0, (EntityPlayer) entity1));
            }
        } else if (ID == RTMCore.guiIdTrainWorkBench) {
            TileEntityTrainWorkBench tile = (TileEntityTrainWorkBench) world.getTileEntity(x, y, z);
            return new GuiRTMWorkBench(player.inventory, world, tile, player.capabilities.isCreativeMode);
        } else if (ID == RTMCore.guiIdSignalConverter) {
            return new GuiSignalConverter((TileEntitySignalConverter) world.getTileEntity(x, y, z));
        } else if (ID == RTMCore.guiIdTicketVendor) {
            TileEntity tile = world.getTileEntity(x, y, z);
            if (tile instanceof TileEntityTicketVendor) {
                return new GuiTicketVendor(player.inventory, (TileEntityTicketVendor) tile);
            }
        } else if (ID == RTMCore.guiIdStation) {
            TileEntity tile = world.getTileEntity(x, y, z);
            if (tile instanceof TileEntityStation) {
                return new GuiStation((TileEntityStation) tile);
            }
        } else if (ID == RTMCore.guiIdPaintTool) {
            Entity entity = world.getEntityByID(x);
            if (entity instanceof EntityPlayer) {
                return new GuiPaintTool((EntityPlayer) entity);
            }
        } else if (ID == RTMCore.guiIdMovingMachine) {
            TileEntity tile = world.getTileEntity(x, y, z);
            if (tile instanceof TileEntityMovingMachine) {
                return new GuiMovingMachine((TileEntityMovingMachine) tile);
            }
        } else if (ID == RTMCore.guiIdNPC) {
            EntityNPC npc = (EntityNPC) world.getEntityByID(x);
            if (npc.getRole() == Role.SALESPERSON) {
                return new GuiSalesperson(player, npc);
            } else {
                return new GuiNPC(player, npc);
            }
        } else if (ID == RTMCore.guiIdMotorman) {
            return GuiMotorman.getGui((EntityMotorman) world.getEntityByID(x));
        } else if (ID == RTMCore.guiIdRailMarker) {
            return new GuiRailMarker((TileEntityMarker) world.getTileEntity(x, y, z));
        } else if (ID == RTMCore.guiIdSpeaker) {
            return new GuiSpeaker((TileEntitySpeaker) world.getTileEntity(x, y, z));
        } else if (ID == RTMCore.guiIdCamera) {
            return new GuiCamera(player);
        }
        return null;
    }
}