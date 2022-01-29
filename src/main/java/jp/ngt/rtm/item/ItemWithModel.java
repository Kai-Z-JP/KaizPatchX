package jp.ngt.rtm.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.block.TileEntityPlaceable;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.modelpack.IModelSelectorWithType;
import jp.ngt.rtm.modelpack.modelset.ModelSetBase;
import jp.ngt.rtm.modelpack.state.ResourceState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.world.World;

import java.util.List;

public abstract class ItemWithModel extends Item implements IModelSelectorWithType {
    /**
     * モデル選択時の読み書きに使用
     */
    @SideOnly(Side.CLIENT)
    private ItemStack selectedItem;
    @SideOnly(Side.CLIENT)
    private EntityPlayer selectedPlayer;

    public ItemWithModel() {
        super();
        this.setHasSubtypes(true);
    }

    @Override
    public ItemStack onItemRightClick(ItemStack itemStack, World world, EntityPlayer player) {
        if (world.isRemote) {
            if (!this.getModelType(itemStack).isEmpty()) {
                this.selectedItem = itemStack;
                this.selectedPlayer = player;
                player.openGui(RTMCore.instance, RTMCore.guiIdSelectItemModel, player.worldObj, 0, 0, 0);
            }
        }
        return itemStack;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void addInformation(ItemStack itemStack, EntityPlayer player, List list, boolean par4) {
        list.add(EnumChatFormatting.GRAY + getModelName(itemStack));
        if (this.getModelState(itemStack).getDataMap().getEntries().size() > 0) {
            list.add(EnumChatFormatting.DARK_PURPLE + "(+DataMap)");
        }
        if (ItemWithModel.hasOffset(itemStack)) {
            list.add(EnumChatFormatting.DARK_PURPLE + "(+Offset)");
        }
    }

    protected abstract String getModelType(ItemStack itemStack);

    protected abstract String getDefaultModelName(ItemStack itemStack);

    public String getSubType(ItemStack itemStack) {
        return "";
    }

    public String getModelName(ItemStack itemStack) {
        if (itemStack.hasTagCompound()) {
            return itemStack.getTagCompound().getString("ModelName");
        } else {
            ItemWithModel item = (ItemWithModel) itemStack.getItem();
            itemStack.setTagCompound(new NBTTagCompound());
            itemStack.getTagCompound().setString("ModelName", item.getDefaultModelName(itemStack));
            return item.getDefaultModelName(itemStack);
        }
    }

    public void setModelName(ItemStack itemStack, String name) {
        if (!itemStack.hasTagCompound()) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        itemStack.getTagCompound().setString("ModelName", name);
    }

    @Override
    public String getModelType() {
        return this.getModelType(this.selectedItem);
    }

    @Override
    public String getModelName() {
        return getModelName(this.selectedItem);
    }

    @Override
    public void setModelName(String par1) {
        this.setModelName(this.selectedItem, par1);
        NGTUtil.sendPacketToServer(this.selectedPlayer, this.selectedItem);
    }

    @Override
    public int[] getPos() {
        return new int[3];
    }

    @Override
    public boolean closeGui(String par1, ResourceState par2) {
        this.setModelName(this.selectedItem, par1);
        this.setModelState(this.selectedItem, par2);
        return true;
    }

    @Override
    public String getSubType() {
        return this.getSubType(this.selectedItem);
    }

    @Override
    public ModelSetBase getModelSet() {
        return null;
    }

    @Override
    public ResourceState getResourceState() {
        return this.getModelState(this.selectedItem);
    }

    public ResourceState getModelState(ItemStack itemStack) {
        ResourceState state = new ResourceState(this);
        if (itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("State")) {
            state.readFromNBT(itemStack.getTagCompound().getCompoundTag("State"));
        } else {
            NBTTagCompound nbt;
            if (!itemStack.hasTagCompound()) {
                nbt = new NBTTagCompound();
                itemStack.setTagCompound(nbt);
            } else {
                nbt = itemStack.getTagCompound();
            }
            nbt.setTag("State", state.writeToNBT());
        }
        return state;
    }

    public void setModelState(ItemStack itemStack, ResourceState state) {
        if (!itemStack.hasTagCompound()) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        itemStack.getTagCompound().setTag("State", state.writeToNBT());

        if (this.selectedPlayer != null) {
            NGTUtil.sendPacketToServer(this.selectedPlayer, this.selectedItem);
        }
    }

    public static boolean hasOffset(ItemStack itemStack) {
        return itemStack.hasTagCompound() && itemStack.getTagCompound().hasKey("yaw");
    }

    private static float[] getOffset(ItemStack itemStack) {
        if (itemStack.hasTagCompound()) {
            NBTTagCompound nbt = itemStack.getTagCompound();
            float offsetX = nbt.getFloat("offsetX");
            float offsetY = nbt.getFloat("offsetY");
            float offsetZ = nbt.getFloat("offsetZ");
            return new float[]{offsetX, offsetY, offsetZ};
        } else {
            return new float[3];
        }
    }

    private static float getRotation(ItemStack itemStack) {
        return itemStack.hasTagCompound() ? itemStack.getTagCompound().getFloat("yaw") : 0;
    }

    private static void setOffset(ItemStack itemStack, float offsetX, float offsetY, float offsetZ) {
        if (!itemStack.hasTagCompound()) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        NBTTagCompound nbt = itemStack.getTagCompound();
        nbt.setFloat("offsetX", offsetX);
        nbt.setFloat("offsetY", offsetY);
        nbt.setFloat("offsetZ", offsetZ);
    }

    private static void setRotation(ItemStack itemStack, float rotation) {
        if (!itemStack.hasTagCompound()) {
            itemStack.setTagCompound(new NBTTagCompound());
        }
        NBTTagCompound nbt = itemStack.getTagCompound();
        nbt.setFloat("yaw", rotation);
    }

    public static void applyOffsetToTileEntity(ItemStack itemStack, TileEntityPlaceable tile) {
        float[] offset = ItemWithModel.getOffset(itemStack);
        tile.setOffset(offset[0], offset[1], offset[2], true);
        tile.setRotation(ItemWithModel.getRotation(itemStack), true);
    }

    public static void copyOffsetToItemStack(TileEntityPlaceable tileEntity, ItemStack itemStack) {
        ItemWithModel.setOffset(itemStack, tileEntity.getOffsetX(), tileEntity.getOffsetY(), tileEntity.getOffsetZ());
        ItemWithModel.setRotation(itemStack, tileEntity.getRotation());
    }
}