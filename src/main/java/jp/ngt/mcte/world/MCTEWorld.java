package jp.ngt.mcte.world;

import jp.ngt.mcte.MCTE;
import jp.ngt.mcte.block.TileEntityMiniature;
import jp.ngt.ngtlib.block.NGTObject;
import jp.ngt.ngtlib.world.NGTWorld;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public class MCTEWorld extends NGTWorld {
    public boolean updated;
    private long tickCount;
    private final List<BUEntry> updateList = new ArrayList<>();

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
                TileEntityMiniature miniature = (TileEntityMiniature) this.world.getTileEntity(this.posX, this.posY, this.posZ);
                miniature.getDescriptionPacket();
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
}