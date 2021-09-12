package jp.ngt.rtm.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.modelpack.IModelSelectorWithType;
import jp.ngt.rtm.modelpack.ModelPackManager;
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
    private ModelSetBase myModelSet;

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
        if (this.myModelSet == null || !this.myModelSet.getConfig().getName().equals(this.getModelName())) {
            this.myModelSet = ModelPackManager.INSTANCE.getModelSet(this.getModelType(), this.getModelName());
        }
        return this.myModelSet;
    }

    @Override
    public ResourceState getResourceState() {
        return this.getModelState(this.selectedItem);
    }

    public ResourceState getModelState(ItemStack itemStack) {
        this.selectedItem = itemStack;
        ResourceState state = new ResourceState(this);
        if (itemStack.getTagCompound().hasKey("State")) {
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
}