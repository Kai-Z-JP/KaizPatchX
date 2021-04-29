package jp.ngt.mcte.editor;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.mcte.MCTE;
import jp.ngt.mcte.block.MiniatureBlockState;
import jp.ngt.mcte.item.ItemMiniature;
import jp.ngt.mcte.item.ItemMiniature.MiniatureMode;
import jp.ngt.mcte.network.PacketNBT;
import jp.ngt.mcte.network.PacketRenderBlocks;
import jp.ngt.ngtlib.block.BlockUtil;
import jp.ngt.ngtlib.block.NGTObject;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.ngtlib.renderer.DisplayList;
import jp.ngt.ngtlib.renderer.GLHelper;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.ngtlib.world.NGTWorld;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraft.util.MovingObjectPosition;
import net.minecraft.util.MovingObjectPosition.MovingObjectType;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.io.File;

public class EntityEditor extends Entity implements IInventory {
    //public List<BlockSet> blockList = new ArrayList<BlockSet>();
    //public List entityList;
    private EntityPlayer player;
    private final ItemStack[] slots = new ItemStack[2];
    public int fillMode = 0;

    @SideOnly(Side.CLIENT)
    public NGTObject blocksForRenderer;
    @SideOnly(Side.CLIENT)
    public World dummyWorld;
    @SideOnly(Side.CLIENT)
    public DisplayList displayList;
    @SideOnly(Side.CLIENT)
    private boolean needsUpdate;

    public EntityEditor(World world) {
        super(world);
        this.ignoreFrustumCheck = true;
    }

    protected EntityEditor(World world, EntityPlayer player, int x, int y, int z) {
        this(world);

        this.setPlayer(player);
        this.setPos(true, x, y, z);
    }

    @Override
    protected void entityInit()//max:31
    {
        this.dataWatcher.addObject(10, "");//player
        this.dataWatcher.addObject(11, 0);//x
        this.dataWatcher.addObject(12, 0);//y
        this.dataWatcher.addObject(13, 0);//z
        this.dataWatcher.addObject(14, 0);//x
        this.dataWatcher.addObject(15, 0);//y
        this.dataWatcher.addObject(16, 0);//z
        this.dataWatcher.addObject(17, (byte) 0);//selectEnd
        this.dataWatcher.addObject(18, 0);
        this.dataWatcher.addObject(19, 0);
        this.dataWatcher.addObject(20, 0);//paste
        this.dataWatcher.addObject(21, (byte) 0);//mode
        this.dataWatcher.addObject(22, 0);
        this.dataWatcher.addObject(23, 0);
        this.dataWatcher.addObject(24, 0);
        this.dataWatcher.addObject(25, 0);//clone
    }

    @Override
    protected void readEntityFromNBT(NBTTagCompound nbt) {
    }

    @Override
    protected void writeEntityToNBT(NBTTagCompound nbt) {
    }

    @Override
    public void onUpdate() {
        super.onUpdate();

        if (this.getPlayer() != null) {
            this.posX = this.getPlayer().posX;
            this.posY = this.getPlayer().posY;
            this.posZ = this.getPlayer().posZ;
            this.setPosition(this.posX, this.posY, this.posZ);
        }

        if (!this.worldObj.isRemote) {
            if (this.getPlayer() == null) {
                this.setDead();
            }
        }
    }

    @Override
    public void setDead() {
        super.setDead();

        if (this.worldObj.isRemote) {
            GLHelper.deleteGLList(this.displayList);
        } else {
            EditorManager.INSTANCE.remove(this);
        }
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void setPositionAndRotation2(double par1, double par3, double par5, float par7, float par8, int par9) {
    }

    /**
     * @param selectSide Blockそのものではなく、その側面(=空気ブロック)を選択するか
     */
    public MovingObjectPosition getTarget(boolean selectSide) {
        EntityPlayer player = this.getPlayer();

        if (player != null) {
            byte mode = this.getEditMode();
            if (mode == Editor.EditMode_0 || mode == Editor.EditMode_VisibleBox_0) {
                MovingObjectPosition target = BlockUtil.getMOPFromPlayer(player, 128.0D, true);
                if (target != null && target.typeOfHit == MovingObjectType.BLOCK) {
                    if (selectSide) {
                        switch (target.sideHit) {
                            case 0:
                                --target.blockY;
                                break;
                            case 1:
                                ++target.blockY;
                                break;
                            case 2:
                                --target.blockZ;
                                break;
                            case 3:
                                ++target.blockZ;
                                break;
                            case 4:
                                --target.blockX;
                                break;
                            case 5:
                                ++target.blockX;
                                break;
                        }
                    }
                    return target;
                }
            } else {
                float f = 1.0F;
                float pitch = player.prevRotationPitch + (player.rotationPitch - player.prevRotationPitch) * f;
                float yaw = player.prevRotationYaw + (player.rotationYaw - player.prevRotationYaw) * f;
                double dx = player.prevPosX + (player.posX - player.prevPosX) * (double) f;
                double dy = player.prevPosY + (player.posY - player.prevPosY) * (double) f + 1.62D - (double) player.yOffset;
                double dz = player.prevPosZ + (player.posZ - player.prevPosZ) * (double) f;
                Vec3 vec3 = Vec3.createVectorHelper(dx, dy, dz);
                float f3 = NGTMath.cos(-yaw - 180.0F);//0.017453292=PI/180
                float f4 = NGTMath.sin(-yaw - 180.0F);
                float f5 = -NGTMath.cos(-pitch);
                float f6 = NGTMath.sin(-pitch);
                float x2 = f4 * f5;
                float z2 = f3 * f5;
                double distance = 8.0D;
                Vec3 vec31 = vec3.addVector((double) x2 * distance, (double) f6 * distance, (double) z2 * distance);

                int x = MathHelper.floor_double(vec31.xCoord);
                int y = MathHelper.floor_double(vec31.yCoord);
                int z = MathHelper.floor_double(vec31.zCoord);
                if (y >= 0) {
                    if (y > 255) {
                        y = 255;
                    }
                    return new MovingObjectPosition(x, y, z, 0, vec31, true);
                }
            }
        }
        return null;
    }

    public EntityPlayer getPlayer() {
        if (this.player == null) {
            this.player = this.worldObj.getPlayerEntityByName(this.dataWatcher.getWatchableObjectString(10));
        }
        return this.player;
    }

    public void setPlayer(EntityPlayer par1) {
        this.player = par1;
        this.dataWatcher.updateObject(10, String.valueOf(par1.getCommandSenderName()));
    }

    public int[] getPos(boolean isStart) {
        int i = isStart ? 0 : 3;
        int x = this.dataWatcher.getWatchableObjectInt(11 + i);
        int y = this.dataWatcher.getWatchableObjectInt(12 + i);
        int z = this.dataWatcher.getWatchableObjectInt(13 + i);
        return new int[]{x, y, z};
    }

    public void setPos(boolean isStart, int x, int y, int z) {
        this.setSelectEnd(!isStart);
        int i = isStart ? 0 : 3;
        this.dataWatcher.updateObject(11 + i, x);
        this.dataWatcher.updateObject(12 + i, y);
        this.dataWatcher.updateObject(13 + i, z);
    }

    public boolean isSelectEnd() {
        return this.dataWatcher.getWatchableObjectByte(17) == 1
                && this.dataWatcher.getWatchableObjectInt(12) > 0
                && this.dataWatcher.getWatchableObjectInt(12 + 3) > 0;
    }

    public void setSelectEnd(boolean par1) {
        this.dataWatcher.updateObject(17, (byte) (par1 ? 1 : 0));
    }

    /**
     * @return {x, y, z}
     */
    public int[] getPasteBox() {
        int x = this.dataWatcher.getWatchableObjectInt(18);
        int y = this.dataWatcher.getWatchableObjectInt(19);
        int z = this.dataWatcher.getWatchableObjectInt(20);
        return new int[]{x, y, z};
    }

    public void setPasteBox(int x, int y, int z) {
        this.dataWatcher.updateObject(18, x);
        this.dataWatcher.updateObject(19, y);
        this.dataWatcher.updateObject(20, z);
    }

    /**
     * @return {x, y, z, repeat} 相対座標
     */
    public int[] getCloneBox() {
        int x = this.dataWatcher.getWatchableObjectInt(22);
        int y = this.dataWatcher.getWatchableObjectInt(23);
        int z = this.dataWatcher.getWatchableObjectInt(24);
        int r = this.dataWatcher.getWatchableObjectInt(25);
        return new int[]{x, y, z, r};
    }

    public void setCloneBox(int x, int y, int z, int r) {
        this.dataWatcher.updateObject(22, x);
        this.dataWatcher.updateObject(23, y);
        this.dataWatcher.updateObject(24, z);
        this.dataWatcher.updateObject(25, r);
    }

    public boolean hasCloneBox() {
        return this.dataWatcher.getWatchableObjectInt(25) > 0;
    }

    public byte getEditMode() {
        return this.dataWatcher.getWatchableObjectByte(21);
    }

    public void setEditMode(byte par1) {
        this.dataWatcher.updateObject(21, par1);
    }

    /**
     * @param index 0 or 1
     */
    public Block getSlotBlock(int index) {
        ItemStack stack = this.slots[index];
        if (stack == null) {
            return Blocks.air;
        } else {
            return Block.getBlockFromItem(stack.getItem());
        }
    }

    /**
     * @param index 0 or 1
     */
    public int getSlotBlockMetadata(int index) {
        ItemStack stack = this.slots[index];
        return (stack == null) ? 0 : stack.getItemDamage();
    }

    public void dropMiniature(NGTObject par1, float par2) {
        ItemStack stack = ItemMiniature.createMiniatureItem(par1, par2, 0.0F, 0.0F, 0.0F, MiniatureMode.miniature, new MiniatureBlockState());
        this.entityDropItem(stack, 1.0F);
    }

    /**
     * Clientのブロックリストの更新
     */
    public void updateBlockList(NGTObject ngto) {
        MCTE.NETWORK_WRAPPER.sendToAll(new PacketRenderBlocks(this, ngto));
    }

    /**
     * ブロックとの当たり判定
     */
    @Override
    protected void func_145775_I() {
    }

    @SideOnly(Side.CLIENT)
    public void setUpdate(boolean par1) {
        this.needsUpdate = par1;
        if (par1) {
            this.dummyWorld = new NGTWorld(NGTUtil.getClientWorld(), this.blocksForRenderer);
        }
    }

    @SideOnly(Side.CLIENT)
    public boolean shouldUpdate() {
        return this.needsUpdate;
    }

    //IInventory********************************************************************/

    @Override
    public int getSizeInventory() {
        return this.slots.length;
    }

    @Override
    public ItemStack getStackInSlot(int par1) {
        return this.slots[par1];
    }

    @Override
    public ItemStack decrStackSize(int par1, int par2) {
        if (this.slots[par1] != null) {
            ItemStack itemstack;
            if (this.slots[par1].stackSize <= par2) {
                itemstack = this.slots[par1];
                this.slots[par1] = null;
            } else {
                itemstack = this.slots[par1].splitStack(par2);
                if (this.slots[par1].stackSize == 0) {
                    this.slots[par1] = null;
                }
            }
            return itemstack;
        }
        return null;
    }

    @Override
    public ItemStack getStackInSlotOnClosing(int par1) {
        if (this.slots[par1] != null) {
            ItemStack itemstack = this.slots[par1];
            this.slots[par1] = null;
            return itemstack;
        } else {
            return null;
        }
    }

    @Override
    public void setInventorySlotContents(int par1, ItemStack par2) {
        this.slots[par1] = par2;
        if (par2 != null && par2.stackSize > this.getInventoryStackLimit()) {
            par2.stackSize = this.getInventoryStackLimit();
        }
    }

    @Override
    public String getInventoryName() {
        return "Inventory_Editor";
    }

    @Override
    public boolean hasCustomInventoryName() {
        return false;
    }

    @Override
    public int getInventoryStackLimit() {
        return 1;
    }

    @Override
    public void markDirty() {
    }

    @Override
    public boolean isUseableByPlayer(EntityPlayer par1) {
        return true;
    }

    @Override
    public void openInventory() {
    }

    @Override
    public void closeInventory() {
    }

    @Override
    public boolean isItemValidForSlot(int id, ItemStack stack) {
        return true;
    }

    //IO************************************************************************/

    @SideOnly(Side.CLIENT)
    public void importBlocks(File file) {
        NGTObject obj = NGTObject.importFromFile(file);
        if (obj != null) {
            MCTE.NETWORK_WRAPPER.sendToServer(new PacketNBT(this, obj.writeToNBT()));
        }
    }

    /**
     * Server Only
     */
    public void importBlocksFromNBT(NBTTagCompound nbt) {
        NGTObject ngto = NGTObject.readFromNBT(nbt);
        Editor editor = EditorManager.INSTANCE.getEditor(this.getPlayer());
        if (ngto != null && editor != null) {
            editor.loadData(ngto);
            this.setPasteBox(ngto.xSize, ngto.ySize, ngto.zSize);
            this.updateBlockList(ngto);
        }
    }
}