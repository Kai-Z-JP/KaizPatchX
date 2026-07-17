package jp.ngt.mcte.world;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.mcte.MCTE;
import jp.ngt.ngtlib.block.NGTObject;
import jp.ngt.ngtlib.world.NGTWorld;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import java.util.ArrayList;
import java.util.List;

public class MCTEWorld extends NGTWorld {
    public boolean updated;
    private long tickCount;
    private final List<BUEntry> updateList = new ArrayList<>();

    private boolean hasRenderTransform;
    private double renderScale;
    private double modelOffsetX, modelOffsetY, modelOffsetZ;
    private double placementOffsetX, placementOffsetY, placementOffsetZ;
    private double renderYawCos = 1.0D;
    private double renderYawSin;
    private byte renderAttachSide = 1;

    public MCTEWorld(World par1, NGTObject par2, int x2, int y2, int z2) {
        super(par1, par2, x2, y2, z2);
        this.updated = true;
    }

    public MCTEWorld(World par1, NGTObject par2) {
        super(par1, par2);
        this.updated = true;
    }

    @Override
    public boolean setBlock(int x, int y, int z, Block block, int meta, int flag) {
        if (!this.isRemote) {
            Block oldBlock = this.getBlock(x, y, z);
            if (this.blockObject.setBlockSet(x, y, z, block, meta)) {
                //NGTLog.debug("set b:%s m:%d", block.getLocalizedName(), meta);
                //this.markBlockForUpdate(x, y, z);//WorldServerでは未使用
                this.notifyBlockChange(x, y, z, oldBlock);
                this.world.markBlockForUpdate(this.posX, this.posY, this.posZ);
                this.updated = true;

                if (block.hasComparatorInputOverride()) {
                    this.func_147453_f(x, y, z, block);
                }

                return true;
            }
        }
        return false;
    }

    @Override
    public boolean setBlockMetadataWithNotify(int x, int y, int z, int meta, int flag) {
        Block block = this.getBlock(x, y, z);
        return this.setBlock(x, y, z, block, meta, flag);
    }

    @Override
    public void notifyBlockOfNeighborChange(int x, int y, int z, final Block block) {
        if (x >= 0 && y >= 0 && z >= 0 && x < this.blockObject.xSize && y < this.blockObject.ySize && z < this.blockObject.zSize) {
            Block block2 = this.getBlock(x, y, z);
            if (block2 != Blocks.air) {
                super.notifyBlockOfNeighborChange(x, y, z, block);
            }
        }
    }

    @Override
    public void scheduleBlockUpdate(int x, int y, int z, Block block, int tick) {
        this.scheduleBlockUpdateWithPriority(x, y, z, block, tick, 0);
    }

    @Override
    public void scheduleBlockUpdateWithPriority(int x, int y, int z, Block block, int tick, int priority) {
        this.updateList.add(new BUEntry(x, y, z, block, (long) tick + this.getTotalWorldTime(), priority));
    }

    @Override
    public boolean isBlockTickScheduledThisTick(int x, int y, int z, Block block) {
        return this.updateList.stream().anyMatch(entry -> entry.x == x && entry.y == y && entry.z == z);
    }

    @Override
    public long getTotalWorldTime() {
        return this.tickCount;
    }

    @Override
    public void tick() {
        ++this.tickCount;
        this.tickUpdates(false);
        this.func_147456_g();
    }

    @Override
    public boolean tickUpdates(boolean flag) {
        if (!this.updateList.isEmpty()) {
            List<BUEntry> delList = new ArrayList<>();
            this.updateList.stream()
                    .filter(entry -> entry.time >= this.tickCount)
                    .forEach(entry -> {
                        Block block = this.getBlock(entry.x, entry.y, entry.z);
                        if (Block.isEqualTo(block, entry.block)) {
                            block.updateTick(this, entry.x, entry.y, entry.z, this.rand);
                        }
                        delList.add(entry);
                    });
            this.updateList.removeAll(delList);
        }
        return true;
    }

    /**
     * ランダム更新
     */
    @Override
    protected void func_147456_g() {
		/*int count = this.blockObject.blockList.size() / 256;
		for(int i = 0; i < count; ++i)
		{
			int index = this.rand.nextInt(this.blockObject.blockList.size());
		}*/
    }

    @Override
    public void updateEntities() {
    }

    /**
     * 出力ポートに信号が入った
     */
    public void onPortChanged(int x, int y, int z) {
        this.world.notifyBlockChange(this.posX, this.posY, this.posZ, MCTE.miniature);
    }

    private static class BUEntry {
        public final long time;
        public final int x, y, z;
        public final Block block;
        public final int priority;

        public BUEntry(int p2, int p3, int p4, Block pBlock, long pTime, int pPriority) {
            this.x = p2;
            this.y = p3;
            this.z = p4;
            this.block = pBlock;
            this.time = pTime;
            this.priority = pPriority;
        }
    }


    @SideOnly(Side.CLIENT)
    public void setRenderTransform(float scale,
                                   float modelOffsetX, float modelOffsetY, float modelOffsetZ,
                                   float placementOffsetX, float placementOffsetY, float placementOffsetZ,
                                   float yaw, byte attachSide) {
        this.hasRenderTransform = true;
        this.renderScale = scale;
        this.modelOffsetX = modelOffsetX;
        this.modelOffsetY = modelOffsetY;
        this.modelOffsetZ = modelOffsetZ;
        this.placementOffsetX = placementOffsetX;
        this.placementOffsetY = placementOffsetY;
        this.placementOffsetZ = placementOffsetZ;

        double yawRadians = Math.toRadians(yaw);
        this.renderYawCos = snapRotationValue(Math.cos(yawRadians));
        this.renderYawSin = snapRotationValue(Math.sin(yawRadians));
        this.renderAttachSide = attachSide;
    }

    private static double snapRotationValue(double value) {
        return Math.abs(value) < 1.0E-7D ? 0.0D : value;
    }

    @SideOnly(Side.CLIENT)
    @Override
    public int getLightBrightnessForSkyBlocks(int x, int y, int z, int defaultValue) {
        if (!this.hasRenderTransform) {
            return this.world.getLightBrightnessForSkyBlocks(this.posX, this.posY, this.posZ, defaultValue);
        }

        double localX = ((double) x + 0.5D - (double) this.blockObject.xSize * 0.5D) * this.renderScale + this.modelOffsetX;
        double localY = ((double) y + 0.5D) * this.renderScale + this.modelOffsetY;
        double localZ = ((double) z + 0.5D - (double) this.blockObject.zSize * 0.5D) * this.renderScale + this.modelOffsetZ;

        double rotatedX = localX * this.renderYawCos + localZ * this.renderYawSin + this.placementOffsetX;
        double rotatedY = localY + this.placementOffsetY - 0.5D;
        double rotatedZ = localZ * this.renderYawCos - localX * this.renderYawSin + this.placementOffsetZ;

        double attachedX = rotatedX;
        double attachedY = rotatedY;
        double attachedZ = rotatedZ;
        switch (this.renderAttachSide) {
            case 0:
                attachedX = -rotatedX;
                attachedY = -rotatedY;
                break;
            case 2:
                attachedY = rotatedZ;
                attachedZ = -rotatedY;
                break;
            case 3:
                attachedY = -rotatedZ;
                attachedZ = rotatedY;
                break;
            case 4:
                attachedX = -rotatedY;
                attachedY = rotatedX;
                break;
            case 5:
                attachedX = rotatedY;
                attachedY = -rotatedX;
                break;
            default:
                break;
        }

        int worldX = MathHelper.floor_double((double) this.posX + 0.5D + attachedX);
        int worldY = MathHelper.floor_double((double) this.posY + 0.5D + attachedY);
        int worldZ = MathHelper.floor_double((double) this.posZ + 0.5D + attachedZ);
        return this.world.getLightBrightnessForSkyBlocks(worldX, worldY, worldZ, defaultValue);
    }

    @Override
    public BiomeGenBase getBiomeGenForCoords(int x, int z) {
        return this.world.getBiomeGenForCoords(this.posX + x, this.posZ + z);
    }
}
