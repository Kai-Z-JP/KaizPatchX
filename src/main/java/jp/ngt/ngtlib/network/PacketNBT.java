package jp.ngt.ngtlib.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import io.netty.buffer.ByteBuf;
import jp.ngt.ngtlib.NGTCore;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.play.server.S2FPacketSetSlot;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

public class PacketNBT implements IMessage {
    private static final byte Type_Entity = 0;
    private static final byte Type_TileEntity = 1;
    private static final byte Type_PlayerItem = 2;
    public NBTTagCompound nbtData;

    public PacketNBT() {
    }

	/*public PacketNBT(Entity entity, NBTTagCompound nbt, boolean toClient)
	{
		this.nbtData = nbt;
		this.nbtData.setBoolean("ToClient", toClient);
		this.nbtData.setByte("DataType", Type_Entity);
		this.nbtData.setInteger("EntityId", entity.getEntityId());
	}*/

    public PacketNBT(Entity entity, boolean toClient) {
        this.nbtData = new NBTTagCompound();
        entity.writeToNBT(this.nbtData);
        this.nbtData.setBoolean("ToClient", toClient);
        this.nbtData.setByte("DataType", Type_Entity);
        this.nbtData.setInteger("EntityId", entity.getEntityId());
    }

    public PacketNBT(TileEntity tileEntity, NBTTagCompound nbt, boolean toClient) {
        this.nbtData = nbt;
        this.nbtData.setBoolean("ToClient", toClient);
        this.nbtData.setByte("DataType", Type_TileEntity);
        this.nbtData.setInteger("XPos", tileEntity.xCoord);
        this.nbtData.setInteger("YPos", tileEntity.yCoord);
        this.nbtData.setInteger("ZPos", tileEntity.zCoord);
    }

    public PacketNBT(TileEntity tileEntity, boolean toClient) {
        this.nbtData = new NBTTagCompound();
        tileEntity.writeToNBT(this.nbtData);
        this.nbtData.setBoolean("ToClient", toClient);
        this.nbtData.setByte("DataType", Type_TileEntity);
        this.nbtData.setInteger("XPos", tileEntity.xCoord);
        this.nbtData.setInteger("YPos", tileEntity.yCoord);
        this.nbtData.setInteger("ZPos", tileEntity.zCoord);
    }

    /**
     * Playerの所持アイテムにNBT書き込む(Server行きのみ)
     */
    public PacketNBT(EntityPlayer player, ItemStack stack) {
        this.nbtData = new NBTTagCompound();
        this.nbtData.setTag("TagData", stack.getTagCompound());
        this.nbtData.setBoolean("ToClient", false);
        this.nbtData.setByte("DataType", Type_PlayerItem);
        this.nbtData.setInteger("EntityId", player.getEntityId());
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        ByteBufUtils.writeTag(buffer, this.nbtData);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        this.nbtData = ByteBufUtils.readTag(buffer);
    }

    protected void onGetPacket(World world) {
        if (world == null) {
            return;
        }

        byte type = this.nbtData.getByte("DataType");
        if (type == Type_Entity) {
            int id = this.nbtData.getInteger("EntityId");
            Entity entity = world.getEntityByID(id);
            if (entity != null) {
                entity.readFromNBT(this.nbtData);
            }
        } else if (type == Type_TileEntity) {
            int x = this.nbtData.getInteger("XPos");
            int y = this.nbtData.getInteger("YPos");
            int z = this.nbtData.getInteger("ZPos");
            TileEntity tileEntity = world.getTileEntity(x, y, z);
            if (tileEntity != null) {
                tileEntity.readFromNBT(this.nbtData);
                if (!world.isRemote) {
                    tileEntity.markDirty();//セーブする
                }
            }
        } else if (type == Type_PlayerItem) {
            int id = this.nbtData.getInteger("EntityId");
            Entity entity = world.getEntityByID(id);
            if (entity instanceof EntityPlayer) {
                ItemStack stack = ((EntityPlayer) entity).inventory.getCurrentItem();
                if (stack != null) {
                    int index = ((EntityPlayer) entity).inventory.currentItem;
                    NBTTagCompound data = this.nbtData.getCompoundTag("TagData");
                    stack.setTagCompound(data);
                    if (entity instanceof EntityPlayerMP) {
                        this.updateCurrentItem((EntityPlayerMP) entity);
                    }
                }
            }
        }
    }

    /**
     * 手持ちアイテムの同期
     */
    private void updateCurrentItem(EntityPlayerMP player)//NetHandlerPlayerServer.processPlayerBlockPlacement()
    {
        Slot slot = player.inventoryContainer.getSlotFromInventory(player.inventory, player.inventory.currentItem);
        player.playerNetServerHandler.sendPacket(new S2FPacketSetSlot(player.inventoryContainer.windowId, slot.slotNumber, player.inventory.getCurrentItem()));
    }

    public static void sendToServer(Entity entity) {
        NGTCore.NETWORK_WRAPPER.sendToServer(new PacketNBT(entity, false));
    }

    public static void sendToServer(EntityPlayer player, ItemStack item) {
        NGTCore.NETWORK_WRAPPER.sendToServer(new PacketNBT(player, item));
    }

    public static void sendToServer(TileEntity entity) {
        NGTCore.NETWORK_WRAPPER.sendToServer(new PacketNBT(entity, false));
    }

    public static void sendToServer(TileEntity entity, NBTTagCompound nbt) {
        NGTCore.NETWORK_WRAPPER.sendToServer(new PacketNBT(entity, nbt, false));
    }

    public static void sendToClient(Entity entity) {
        NGTCore.NETWORK_WRAPPER.sendToAll(new PacketNBT(entity, true));
    }

    public static void sendToClient(TileEntity entity) {
        NGTCore.NETWORK_WRAPPER.sendToAll(new PacketNBT(entity, true));
    }

    public static void sendTo(Entity entity, EntityPlayerMP player) {
        NGTCore.NETWORK_WRAPPER.sendTo(new PacketNBT(entity, true), player);
    }

    public static void sendTo(TileEntity entity, EntityPlayerMP player) {
        NGTCore.NETWORK_WRAPPER.sendTo(new PacketNBT(entity, true), player);
    }
}