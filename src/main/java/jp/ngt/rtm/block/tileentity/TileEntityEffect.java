package jp.ngt.rtm.block.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.math.NGTMath;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;
import net.minecraft.world.chunk.IChunkProvider;

import java.util.List;

public class TileEntityEffect extends TileEntity {
    public static final int Phase1 = 40;
    public static final int Phase2 = 400;
    public static final int Phase3 = 800;

    public static final float Scale = 20.0F;
    public static final float Slope1 = 0.03125F;
    public static final float Slope2 = 0.015625F;
    public static final float Radius = 2.0F;
    public static final float MaxDistance = 128.0F;

    public static final DamageSource nuclearDamage = (new DamageSource("nuclear")).setFireDamage();

    public int tickCount;

    public TileEntityEffect() {
        super();
        //this.MaxDistance = (float)(16 * MinecraftServer.getServer().getConfigurationManager().getViewDistance());
    }

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.tickCount = nbt.getInteger("count");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("count", this.tickCount);
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (this.shouldUpdateAsAtomicBomb()) {
            if (!this.worldObj.isRemote) {
                if (this.tickCount > Phase3) {
                    this.worldObj.setBlockToAir(this.xCoord, this.yCoord, this.zCoord);
                } else if (this.tickCount > Phase1) {
                    if (this.tickCount % 3 == 0) {
                        float f0 = (float) (this.tickCount - Phase1);
                        double flameSize = this.getSigmoid(f0, Slope1) * Scale * Radius;
                        double blastSize = this.getLinear(f0);
                        this.doExplosion(flameSize, blastSize);
                    }

                }
            }

            ++this.tickCount;
        }
    }

    public boolean shouldUpdateAsAtomicBomb() {
//        return this.getBlockMetadata() == RTMCore.ATOMIC_BOM_META;
        return false;
    }

    private void doExplosion(double flameSize, double blastSize) {
        double tileX = (double) this.xCoord + 0.5D;
        double tileY = (double) this.yCoord + 0.5D;
        double tileZ = (double) this.zCoord + 0.5D;

        if (blastSize > MaxDistance) {
            blastSize = MaxDistance;
        }

        double d0 = flameSize * flameSize;
        double d1 = blastSize * blastSize;

        //ブロック破壊
        if (this.tickCount < Phase2 && this.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing")) {
            int i0 = (int) (blastSize);
            int i1 = (int) (blastSize * 2.0F);
            int i2 = (int) d0;
            int i3 = (int) d1;
            int i0y = i0;
            int i1y = i1;
            if (this.yCoord - i0y < 0) {
                i0y = this.yCoord;
            }

            if (this.yCoord - i0y + i1y >= 256) {
                i1y = 256 - this.yCoord + i0y;
            }

            for (int i = 0; i < i1; ++i) {
                for (int j = 0; j < i1y; ++j) {
                    for (int k = 0; k < i1; ++k) {
                        int x0 = i - i0;
                        int y0 = j - i0y;
                        int z0 = k - i0;
                        int i4 = x0 * x0 + y0 * y0 + z0 * z0;
                        int flag = 0;
                        if (i4 <= i2) {
                            flag = 1;
                        } else if (i4 <= i3) {
                            flag = 2;
                        }

                        if (flag > 0) {
                            int x = x0 + this.xCoord;
                            int y = y0 + this.yCoord;
                            int z = z0 + this.zCoord;
                            if (this.isChunksExist(x, y, z)) {
                                Block block = this.worldObj.getBlock(x, y, z);
                                if (block == Blocks.air) {
                                    continue;
                                }

                                float hardness = block.getBlockHardness(this.worldObj, x, y, z);
                                if (flag == 1) {
                                    if (hardness >= 0.0F && hardness < 500.0F) {
                                        this.setBlock(x, y, z, Blocks.air);
                                    }
                                } else {
                                    if (hardness >= 0.0F && hardness < 0.5F) {
                                        if (block != Blocks.fire) {
                                            this.setBlock(x, y, z, Blocks.air);
                                            if (block.getMaterial() == Material.plants || block.getMaterial() == Material.leaves) {
                                                this.setBlock(x, y, z, Blocks.fire);
                                            }
                                        }
                                    } else if (block == Blocks.grass || block == Blocks.farmland || block == Blocks.mycelium) {
                                        this.setBlock(x, y, z, Blocks.dirt);
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }

        //Entityへの爆風処理
        double time = (double) (Phase3 - this.tickCount) / (double) (Phase3 - Phase1);//0.0~1.0
        List<Entity> list = this.worldObj.getEntitiesWithinAABBExcludingEntity(null, this.getAABB(tileX, tileY, tileZ, blastSize));
        //List list = this.worldObj.loadedEntityList;//ConcurrentModificationException
        list.forEach(entity -> {
            double distanceSq = entity.getDistanceSq(tileX, tileY, tileZ);
            if (distanceSq < d1) {
                double dx = entity.posX - tileX;
                double dy = entity.posY + (double) entity.getEyeHeight() - tileY;
                double dz = entity.posZ - tileZ;
                Vec3 vec3 = Vec3.createVectorHelper(dx, dy, dz);
                double density = NGTMath.pow(0.5D, this.getBlockDensity(tileX, tileY, tileZ, vec3));
                double distance = Math.sqrt(distanceSq);
                double d4 = 1.0D - (distance / (double) MaxDistance);
                double d5 = time * density * d4;
                double acceleration = d5 / distance * 2.5D;//max1.0 * a
                float damage = (float) d5 * 10.0F;

                if (damage >= 0.25F) {
                    if (distanceSq < d0)//ブロック破壊範囲内
                    {
                        entity.attackEntityFrom(DamageSource.inFire, damage * 2.0F);
                        entity.setFire(5);
                    } else {
                        entity.attackEntityFrom(nuclearDamage, damage);
                    }
                }

                if (acceleration > 0.0D) {
                    if (entity instanceof EntityPlayer) {
                        if (((EntityPlayer) entity).capabilities.isFlying) {
                            return;
                        }
                    }

                    entity.velocityChanged = true;
                    entity.motionX += dx * acceleration;
                    entity.motionY += dy * acceleration * 1.2F;
                    entity.motionZ += dz * acceleration;
                }
            }
        });
    }

    public double getSigmoid(float x, float a)//(1/(1+e^-x*0.5))-0.5
    {
        return ((1.0D / (1.0D + Math.pow(Math.E, -x * a))) - 0.5D) * 2.0D;
    }

    public double getLinear(float x) {
        return x * Slope2 * Scale * Radius;
    }

    private AxisAlignedBB getAABB(double x, double y, double z, double size) {
        double minY = y - size;
        double maxY = y + size;

        if (minY < 0.0D) {
            minY = 0.0D;
        }

        if (maxY > 256.0D) {
            maxY = 256.0D;
        }

        return AxisAlignedBB.getBoundingBox(x - size, minY, z - size, x + size, maxY, z + size);
    }

    private boolean isChunksExist(int par1, int par2, int par3) {
        if (par2 >= 0 && par2 < 256) {
            IChunkProvider chunkProvider = this.worldObj.getChunkProvider();
            par1 >>= 4;
            par3 >>= 4;
            return chunkProvider.chunkExists(par1, par3);
        } else {
            return false;
        }
    }

    private int getBlockDensity(double posX, double posY, double posZ, Vec3 par2) {
        Vec3 vec3 = par2.normalize();
        double x0 = vec3.xCoord;
        double y0 = vec3.yCoord;
        double z0 = vec3.zCoord;
        int i0 = 0;

        while (vec3.squareDistanceTo(0.0D, 0.0D, 0.0D) < par2.squareDistanceTo(0.0D, 0.0D, 0.0D)) {
            int x = MathHelper.floor_double(posX + vec3.xCoord);
            int y = MathHelper.floor_double(posY + vec3.yCoord);
            int z = MathHelper.floor_double(posZ + vec3.zCoord);
            Block block = this.worldObj.getBlock(x, y, z);
            int meta = this.worldObj.getBlockMetadata(x, y, z);
            if ((block.getCollisionBoundingBoxFromPool(this.worldObj, x, y, z) != null) && block.canCollideCheck(meta, false)) {
                ++i0;
            }

            vec3.xCoord += x0;
            vec3.yCoord += y0;
            vec3.zCoord += z0;
        }

        return i0;
    }

    private boolean setBlock(int x, int y, int z, Block block) {
        return this.worldObj.setBlock(x, y, z, block, 0, 2);//Client描画アプデのみ
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        this.writeToNBT(nbt);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 0, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.func_148857_g());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return this.shouldUpdateAsAtomicBomb() ? Double.POSITIVE_INFINITY : 32.0D * 32.0D;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return INFINITE_EXTENT_AABB;
    }
}