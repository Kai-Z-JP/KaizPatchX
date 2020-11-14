package jp.ngt.rtm.block.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.block.BlockSet;
import jp.ngt.ngtlib.block.NGTObject;
import jp.ngt.ngtlib.block.TileEntityCustom;
import jp.ngt.ngtlib.renderer.DisplayList;
import jp.ngt.ngtlib.renderer.GLHelper;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.ngtlib.world.NGTWorld;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.entity.EntityMMBoundingBox;
import jp.ngt.rtm.network.PacketMoveMM;
import jp.ngt.rtm.network.PacketNotice;
import jp.ngt.rtm.rail.BlockLargeRailBase;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class TileEntityMovingMachine extends TileEntity {
    public boolean isCore;
    public int width, height, depth;
    /**
     * 相対座標
     */
    public int pairBlockX, pairBlockY, pairBlockZ;
    public float speed = 0.0625F;
    public boolean guideVisibility = true;
    /**
     * 移動してるオブジェクトの相対座標
     */
    public double posX, posY, posZ;
    public double prevPosX, prevPosY, prevPosZ;
    /**
     * 1:終点方向, 0:停止, -1:始点方向, -2:ブロック再設置
     */
    public byte moveDir;

    /**
     * 移動速度
     */
    private double motionX, motionY, motionZ;
    private final List<EntityMMBoundingBox> bbList = new ArrayList<>();
    private int[] bbIds;

    public NGTObject blocksObject;
    @SideOnly(Side.CLIENT)
    public World dummyWorld;
    @SideOnly(Side.CLIENT)
    public DisplayList[] glLists;

    private final AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(0.0D, 0.0D, 0.0D, 1.0D, 1.0D, 1.0D);

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.isCore = nbt.getBoolean("IsCore");
        int[] ia0 = nbt.getIntArray("Range");
        this.width = ia0[0];
        this.height = ia0[1];
        this.depth = ia0[2];
        int[] ia1 = nbt.getIntArray("PairBlock");
        this.pairBlockX = ia1[0];
        this.pairBlockY = ia1[1];
        this.pairBlockZ = ia1[2];
        this.speed = nbt.getFloat("Speed");
        this.posX = nbt.getDouble("PosX");
        this.posY = nbt.getDouble("PosY");
        this.posZ = nbt.getDouble("PosZ");
        this.moveDir = nbt.getByte("MoveDir");
        this.guideVisibility = nbt.getBoolean("GuideVisibility");

        if (this.worldObj == null && this.moveDir != 0 && nbt.hasKey("NGTO")) {
            this.blocksObject = NGTObject.readFromNBT(nbt.getCompoundTag("NGTO"));
            this.moveDir = -2;
        }

        this.prevPosX = this.posX;
        this.prevPosY = this.posY;
        this.prevPosZ = this.posZ;
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setBoolean("IsCore", this.isCore);
        nbt.setIntArray("Range", new int[]{this.width, this.height, this.depth});
        nbt.setIntArray("PairBlock", new int[]{this.pairBlockX, this.pairBlockY, this.pairBlockZ});
        nbt.setFloat("Speed", this.speed);
        nbt.setDouble("PosX", this.posX);
        nbt.setDouble("PosY", this.posY);
        nbt.setDouble("PosZ", this.posZ);
        nbt.setByte("MoveDir", this.moveDir);
        nbt.setBoolean("GuideVisibility", this.guideVisibility);

        if (this.moveDir != 0 && this.blocksObject != null) {
            nbt.setTag("NGTO", this.blocksObject.writeToNBT());
        }
    }

    @Override
    public void updateEntity() {
        if (!this.isCore) {
            return;
        }

        if (this.moveDir == 1 || this.moveDir == -1) {
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;
            this.posX += this.motionX;
            this.posY += this.motionY;
            this.posZ += this.motionZ;

            boolean flag = false;
            if (this.moveDir == 1) {
                if (Math.abs(this.posX) > (double) Math.abs(this.pairBlockX)) {
                    this.posX = this.pairBlockX;
                    flag = true;
                }

                if (Math.abs(this.posY) > (double) Math.abs(this.pairBlockY)) {
                    this.posY = this.pairBlockY;
                    flag = true;
                }

                if (Math.abs(this.posZ) > (double) Math.abs(this.pairBlockZ)) {
                    this.posZ = this.pairBlockZ;
                    flag = true;
                }
            } else {
                if (Math.abs((double) this.pairBlockX - this.posX) > (double) Math.abs(this.pairBlockX)) {
                    this.posX = 0.0D;
                    flag = true;
                }


                if (Math.abs((double) this.pairBlockY - this.posY) > (double) Math.abs(this.pairBlockY)) {
                    this.posY = 0.0D;
                    flag = true;
                }


                if (Math.abs((double) this.pairBlockZ - this.posZ) > (double) Math.abs(this.pairBlockZ)) {
                    this.posZ = 0.0D;
                    flag = true;
                }
            }

            this.moveEntities();

            if (flag && !this.worldObj.isRemote) {
                this.setMovement((byte) 0);
            }
        } else if (this.moveDir == -2) {
            this.moveDir = 0;
            this.editBlock(1);
            this.onBlockChanged();
        }
    }

    private void moveEntities() {
        if (!this.worldObj.isRemote) {
            double mX = this.posX - this.prevPosX;
            double mY = this.posY - this.prevPosY;
            double mZ = this.posZ - this.prevPosZ;

            //MMBBを全て動かしてから各Entityの当たり判定処理

            IntStream.range(0, 2).forEach(pass ->
                    this.bbList.forEach(entity -> {
                        if (pass == 0) {
                            entity.setPosition(entity.posX + mX, entity.posY + mY, entity.posZ + mZ);
                        } else if (pass == 1) {
                            entity.moveMM(mX, mY, mZ);
                        }
                    }));

            //同期は後で
            if (this.bbIds != null) {
                RTMCore.NETWORK_WRAPPER.sendToAll(new PacketMoveMM(this.bbIds, mX, mY, mZ));
            }
        }
    }

    /**
     * Server Only
     */
    public void onBlockChanged() {
        if (!this.hasPair()) {
            return;
        }

        if (!this.isCore) {
            this.getCore().onBlockChanged();
        }

        int md;
        boolean bs = this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord);
        boolean be = this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord + this.pairBlockX, this.yCoord + this.pairBlockY, this.zCoord + this.pairBlockZ);
        if (be && !bs) {
            md = 1;
        } else if (bs && !be) {
            md = -1;
        } else {
            md = 0;
        }

        if (this.moveDir != md) {
            this.setMovement((byte) md);
        }
    }

    public void setMovement(byte par1) {
        this.moveDir = par1;
        if (par1 == 0) {
            this.motionX = this.motionY = this.motionZ = 0.0D;
            this.prevPosX = this.posX;
            this.prevPosY = this.posY;
            this.prevPosZ = this.posZ;
            this.editBlock(1);
        } else {
            Vec3 vec = Vec3.createVectorHelper(this.pairBlockX, this.pairBlockY, this.pairBlockZ).normalize();
            double d0 = (par1 == 1) ? 1.0D : -1.0D;
            this.motionX = vec.xCoord * (double) this.speed * d0;
            this.motionY = vec.yCoord * (double) this.speed * d0;
            this.motionZ = vec.zCoord * (double) this.speed * d0;
            this.editBlock(0);
        }

        if (!this.worldObj.isRemote) {
            this.sendPacket();
            this.markDirty();

            String s = "MM," + par1;
            RTMCore.NETWORK_WRAPPER.sendToAll(new PacketNotice(PacketNotice.Side_CLIENT, s, this));
        }
    }

    /**
     * @param mode 0:DelBlocks, 1:PutBlocks
     */
    private void editBlock(int mode) {
        if (mode == 1 && this.blocksObject == null) {
            return;
        }

        List<BlockSet> list = new ArrayList<>();
        List aabbList = new ArrayList();
        int index = 0;
        int x0 = this.xCoord + (int) this.posX + 1;
        int y0 = this.yCoord + (int) this.posY + 1;
        int z0 = this.zCoord + (int) this.posZ + 1;
        for (int pass = 0; pass < 2; ++pass) {
            for (int i = 0; i < this.width; ++i) {
                for (int j = 0; j < this.height; ++j) {
                    for (int k = 0; k < this.depth; ++k) {
                        int x = x0 + i;
                        int y = y0 + j;
                        int z = z0 + k;
                        if (mode == 0) {
                            if (pass == 0) {
                                list.add(this.getBlockSet(x, y, z));

                                if (!this.worldObj.isRemote) {
                                    Block block = this.worldObj.getBlock(x, y, z);
                                    int meta = this.worldObj.getBlockMetadata(x, y, z);
                                    AxisAlignedBB aabb = null;
                                    if (block instanceof BlockLargeRailBase) {
                                        aabbList.clear();
                                        block.addCollisionBoxesToList(this.worldObj, x, y, z,
                                                AxisAlignedBB.getBoundingBox(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D), aabbList, null);
                                        if (!aabbList.isEmpty()) {
                                            aabb = (AxisAlignedBB) aabbList.get(0);
                                        }
                                    } else {
                                        aabb = block.getCollisionBoundingBoxFromPool(this.worldObj, x, y, z);
                                    }

                                    if (block.canCollideCheck(meta, false) && aabb != null) {
                                        aabb.offset(-x, -y, -z);
                                        boolean b = this.worldObj.isAirBlock(x, y + 1, z);
                                        EntityMMBoundingBox entity = new EntityMMBoundingBox(this.worldObj, this, b);
                                        entity.setPositionAndRotation((double) x + 0.5D, y, (double) z + 0.5D, 0.0F, 0.0F);
                                        entity.setAABB(aabb);
                                        this.worldObj.spawnEntityInWorld(entity);
                                        this.bbList.add(entity);
                                    }
                                }
                            } else {
                                if (!this.worldObj.isRemote) {
                                    this.worldObj.setBlockToAir(x, y, z);
                                }
                            }
                        } else {
                            if (pass == 0) {
                                if (!this.worldObj.isRemote) {
                                    BlockSet set = this.blocksObject.blockList.get(index);
                                    this.setBlockSet(set, x, y, z);
                                }
                            }
                        }
                        ++index;
                    }
                }
            }
        }

        if (!this.worldObj.isRemote && mode == 1) {
            this.bbList.forEach(Entity::setDead);
            this.bbList.clear();
            this.bbIds = null;
        }

        if (mode == 0) {
            this.blocksObject = NGTObject.createNGTO(list, this.width, this.height, this.depth, x0, y0, z0);
            if (this.worldObj.isRemote) {
                this.setNewDummyWorld();
            } else {
                int size = this.bbList.size();
                this.bbIds = new int[size];
                IntStream.range(0, size).forEach(i -> this.bbIds[i] = this.bbList.get(i).getEntityId());
            }
        } else {
            this.blocksObject = null;
            if (this.worldObj.isRemote && this.glLists != null) {
                GLHelper.deleteGLList(this.glLists[0]);
                GLHelper.deleteGLList(this.glLists[1]);
                this.glLists = null;
                this.dummyWorld = null;
            }
        }
    }

    @SideOnly(Side.CLIENT)
    private void setNewDummyWorld() {
        int x = this.xCoord + 1 + (int) this.posX;
        int y = this.yCoord + 1 + (int) this.posY;
        int z = this.zCoord + 1 + (int) this.posZ;
        this.dummyWorld = new NGTWorld(NGTUtil.getClientWorld(), this.blocksObject, x, y, z);
    }

    private BlockSet getBlockSet(int x, int y, int z) {
        Block block = this.worldObj.getBlock(x, y, z);
        int meta = this.worldObj.getBlockMetadata(x, y, z);
        NBTTagCompound nbt = null;
        if (block.hasTileEntity(meta)) {
            TileEntity tile = this.worldObj.getTileEntity(x, y, z);
            if (tile != null) {
                nbt = new NBTTagCompound();
                tile.writeToNBT(nbt);
            }
        }
        return new BlockSet(block, meta, nbt);
    }

    private void setBlockSet(BlockSet set, int x, int y, int z) {
        this.worldObj.setBlock(x, y, z, set.block, set.metadata, 3);

        if (set.block.hasTileEntity(set.metadata)) {
            TileEntity tile = this.worldObj.getTileEntity(x, y, z);
            if (tile != null) {
                int prevX = 0;
                int prevY = 0;
                int prevZ = 0;

                if (set.nbt != null) {
                    NBTTagCompound nbt0 = (NBTTagCompound) set.nbt.copy();
                    prevX = nbt0.getInteger("x");
                    prevY = nbt0.getInteger("y");
                    prevZ = nbt0.getInteger("z");
                    nbt0.setInteger("x", x);
                    nbt0.setInteger("y", y);
                    nbt0.setInteger("z", z);
                    tile.readFromNBT(nbt0);
                }

                if (tile instanceof TileEntityCustom) {
                    ((TileEntityCustom) tile).setPos(x, y, z, prevX, prevY, prevZ);
                } else {
                    tile.xCoord = x;
                    tile.yCoord = y;
                    tile.zCoord = z;
                }
            }
        }
    }

    public void setData(int p1, int p2, int p3, float p4, boolean p5) {
        this.width = p1;
        this.height = p2;
        this.depth = p3;
        this.speed = p4;
        this.guideVisibility = p5;
        this.sendPacket();
        this.markDirty();
    }

    public boolean hasPair() {
        return !(this.pairBlockX == 0 && this.pairBlockY == 0 && this.pairBlockZ == 0);
    }

    public TileEntityMovingMachine getPair() {
        return (TileEntityMovingMachine) this.worldObj.getTileEntity(this.xCoord + this.pairBlockX, this.yCoord + this.pairBlockY, this.zCoord + this.pairBlockZ);
    }

    public void setPair(TileEntityMovingMachine par1) {
        this.pairBlockX = par1.xCoord - this.xCoord;
        this.pairBlockY = par1.yCoord - this.yCoord;
        this.pairBlockZ = par1.zCoord - this.zCoord;
        this.sendPacket();
        this.markDirty();
    }

    public void searchMM(int x, int y, int z) {
        int range = 128;
        for (int i = -range; i < range; ++i) {
            for (int j = 0; j < 256; ++j) {
                for (int k = -range; k < range; ++k) {
                    if (i == 0 && j == y && k == 0) {
                        continue;
                    }

                    if (this.worldObj.getBlock(x + i, j, z + k) == RTMBlock.movingMachine) {
                        TileEntityMovingMachine tile = (TileEntityMovingMachine) this.worldObj.getTileEntity(x + i, j, z + k);
                        if (!tile.hasPair()) {
                            this.isCore = true;
                            this.setPair(tile);
                            tile.setPair(this);
                            return;
                        }
                    }
                }
            }
        }
    }

    public TileEntityMovingMachine getCore() {
        return (this.isCore || !this.hasPair()) ? this : this.getPair();
    }

    public void reset(boolean par1) {
        if (par1 && this.hasPair()) {
            TileEntityMovingMachine tile = this.getPair();
            if (tile != null) {
                tile.reset(false);
            }
        }

        this.isCore = false;
        this.pairBlockX = this.pairBlockY = this.pairBlockZ = 0;
        this.posX = this.posY = this.posZ = 0.0D;
        this.motionX = this.motionY = this.motionZ = 0.0D;
        this.sendPacket();
        this.markDirty();
    }

    protected void sendPacket() {
        if (!this.worldObj.isRemote) {
            NGTUtil.sendPacketToClient(this);
        }
    }

    @Override
    public Packet getDescriptionPacket() {
        this.sendPacket();
        return null;
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass >= 0;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return Double.MAX_VALUE;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }
}