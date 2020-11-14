package jp.ngt.rtm.block.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.block.BlockLiquidBase;
import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.RTMCore;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.util.stream.IntStream;

public class TileEntityConverterCore extends TileEntityConverter {
    private static final int Max_Capacity = 720;
    private byte direction;
    private int capacity;
    /**
     * 0:empty, 1:pigIron, 2:burning, 3:finish
     */
    private int mode = 0;
    private int count = 0;

    private int prevMode = 0;
    private float pitch = 0.0F;

    public boolean powered = false;
    private boolean prevPowered = false;

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.direction = nbt.getByte("dir");
        this.capacity = nbt.getInteger("capacity");
        this.mode = nbt.getInteger("mode");
        this.count = nbt.getInteger("count");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setByte("dir", this.direction);
        nbt.setInteger("capacity", this.capacity);
        nbt.setInteger("mode", this.mode);
        nbt.setInteger("count", this.count);
    }

    @Override
    public void updateEntity() {
        this.prevPowered = this.powered;
        this.powered = false;

        this.prevMode = this.mode;

        if (!this.prevPowered && this.pitch == 0.0F) {
            if (this.worldObj.getBlock(this.xCoord, this.yCoord + 1, this.zCoord) == RTMBlock.liquefiedPigIron) {
                if (this.capacity < Max_Capacity) {
                    int meta = this.worldObj.getBlockMetadata(this.xCoord, this.yCoord + 1, this.zCoord);
                    this.capacity += meta + 1;
                    if (this.capacity > Max_Capacity) {
                        this.capacity = Max_Capacity;
                    }
                    this.worldObj.setBlock(this.xCoord, this.yCoord + 1, this.zCoord, Blocks.air);

                    if (this.capacity > (int) ((float) Max_Capacity * 0.75F)) {
                        this.mode = 1;
                    }
                }
            } else if (this.capacity > 0) {
                if (this.mode == 1) {
					/*if(this.worldObj.getBlock(this.xCoord, this.yCoord - 4, this.zCoord) == RTMBlock.slot)
					{
						this.mode = 2;
					}*/
                    this.mode = 2;
                } else if (this.mode == 2) {
                    ++this.count;

                    if (this.count > this.capacity * 16)//反応時間
                    {
                        this.mode = 3;
                        this.count = 0;
                    }
                }
            }
        }

        if (this.mode == 0 || this.mode == 3) {
            if (this.prevPowered) {
                this.pitch += 0.5F;
            } else {
                this.pitch -= 0.5F;
            }

            if (this.pitch > 90.0F) {
                this.pitch = 90.0F;
            }

            if (this.pitch < 0.0F) {
                this.pitch = 0.0F;
            }

            if (this.prevPowered && this.pitch > 27.0F) {
                Vec3 vec3 = Vec3.createVectorHelper(0.0D, 2.04805D, 2.9275D);
                vec3.rotateAroundX(NGTMath.toRadians(-this.pitch));
                vec3.rotateAroundY(NGTMath.toRadians((float) -this.direction * 90.0F));

                if (this.mode == 3) {
                    if (this.capacity > 0) {
                        --this.capacity;
                    } else {
                        this.mode = 0;
                    }
                }

                if (this.worldObj.isRemote) {
                    if (this.mode == 3 && this.capacity > 0) {
                        IntStream.range(0, 10).forEach(i -> {
                            double x0 = this.xCoord + vec3.xCoord + 0.5D - 0.25D + this.worldObj.rand.nextFloat() * 0.5F;
                            double y0 = this.yCoord + vec3.yCoord - 0.25D + this.worldObj.rand.nextFloat() * 0.5F;
                            double z0 = this.zCoord + vec3.zCoord + 0.5D - 0.25D + this.worldObj.rand.nextFloat() * 0.5F;
                            RTMCore.proxy.spawnModParticle(this.worldObj, x0, y0, z0, 0.0D, -0.125D, 0.0D);
                        });
                    }
                } else {
                    if (this.mode == 3 && this.capacity > 0) {
                        int x = MathHelper.floor_double(this.xCoord + vec3.xCoord + 0.5D);
                        int y = MathHelper.floor_double(this.yCoord + vec3.yCoord);
                        int z = MathHelper.floor_double(this.zCoord + vec3.zCoord + 0.5D);
                        while (this.worldObj.getBlock(x, y, z) == Blocks.air || this.worldObj.getBlock(x, y, z) == RTMBlock.liquefiedSteel) {
                            --y;
                        }
                        BlockLiquidBase.addLiquid(this.worldObj, x, y + 1, z, RTMBlock.liquefiedSteel, 1, true);
                    }
                }
            }
        }

        if (!this.worldObj.isRemote && this.mode != this.prevMode) {
            this.sendPacket();
            NGTLog.sendChatMessageToAll("message.converter.mode" + this.mode);
        }
    }

    public float getPitch() {
        return this.pitch;
    }

    public int getMode() {
        return this.mode;
    }

    public byte getDirection() {
        return this.direction;
    }

    public void setDirection(byte par1) {
        this.direction = par1;
        this.sendPacket();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return NGTUtil.getChunkLoadDistanceSq();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return AxisAlignedBB.getBoundingBox(this.xCoord - 3, this.yCoord - 3, this.zCoord - 3, this.xCoord + 4, this.yCoord + 4, this.zCoord + 4);
    }
}