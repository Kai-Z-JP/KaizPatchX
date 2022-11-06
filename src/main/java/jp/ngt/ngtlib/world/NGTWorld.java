package jp.ngt.ngtlib.world;

import cpw.mods.fml.common.FMLLog;
import jp.ngt.ngtlib.block.BlockSet;
import jp.ngt.ngtlib.block.NGTObject;
import jp.ngt.ngtlib.block.TileEntityCustom;
import jp.ngt.ngtlib.util.NGTUtil;
import net.minecraft.block.Block;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.profiler.Profiler;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.village.VillageCollection;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;
import net.minecraft.world.WorldSettings;
import net.minecraft.world.WorldSettings.GameType;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraftforge.common.util.ForgeDirection;
import org.apache.logging.log4j.Level;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class NGTWorld extends World implements IBlockAccessNGT {
    public final World world;
    public final NGTObject blockObject;
    /**
     * 明るさの取得に使用
     */
    public final int posX, posY, posZ;
    private final List<TileEntity> renderTileEntities = new ArrayList<>();
    private final Map<Integer, Entity> entityIdMap = new HashMap<>();
    public boolean tileEntityLoaded;

    private int nextEntityId;

    public NGTWorld(World par1, NGTObject par2) {
        this(par1, par2, 0, -1, 0);
    }

    public NGTWorld(World par1, NGTObject par2, int x2, int y2, int z2) {
        //Server用コンストラクタ呼び出し
        super(new SaveHandlerDummy(), "NGT",
                new WorldSettings(0L, GameType.CREATIVE, false, false, WorldType.FLAT),
                new WorldProviderDummy(), new Profiler());
		/*super(new SaveHandlerDummy(), "NGT", new WorldProviderDummy(),
				new WorldSettings(0L, GameType.CREATIVE, false, false, WorldType.FLAT), new Profiler());*/

        this.provider.registerWorld(this);
        this.villageCollectionObj = new VillageCollection(this);

        this.world = par1;
        this.blockObject = par2;
        this.posX = x2;
        this.posY = y2;
        this.posZ = z2;
        this.isRemote = par1.isRemote;
        this.loadTileEntity();
        this.loadEntity();
    }

    private void loadTileEntity() {
        //鯖側でできない処理含むためNGTRendererに移動
        this.tileEntityLoaded = false;
    }

    private void loadEntity() {
        NBTTagList tagList = this.blockObject.getEntityList();
        for (int i = 0; i < tagList.tagCount(); ++i) {
            NBTTagCompound nbt = tagList.getCompoundTagAt(i);
            Entity entity2 = this.getEntityFromNBT(nbt);

            if (entity2 != null) {
                this.addEntity(entity2);
                Entity entity = entity2;

                for (NBTTagCompound nbt2 = nbt; nbt2.hasKey("Riding", 10); nbt2 = nbt2.getCompoundTag("Riding")) {
                    Entity entity1 = EntityList.createEntityFromNBT(nbt2.getCompoundTag("Riding"), this);

                    if (entity1 != null) {
                        this.addEntity(entity1);
                        entity.mountEntity(entity1);
                    }

                    entity = entity1;
                }
            }
        }

        //EntityBogieの関連付けタイミング
        for (int pass = 0; pass < 2; ++pass) {
            for (int j = 0; j < this.loadedEntityList.size(); ++j) {
                Entity entity = (Entity) this.loadedEntityList.get(j);
                double x = entity.posX;
                double y = entity.posY - entity.yOffset;
                double z = entity.posZ;
                try {
                    entity.onUpdate();
                } catch (Exception ignored) {
                    //etc. cast WorldServer
                }
                entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);
            }
        }
    }

    private Entity getEntityFromNBT(NBTTagCompound nbt) {
        if ("Player".equals(nbt.getString("id"))) {
        }
        return EntityList.createEntityFromNBT(nbt, this);
    }

    private void addEntity(Entity entity) {
        double x = entity.posX - this.blockObject.origX;
        double y = entity.posY - this.blockObject.origY - entity.yOffset;
        double z = entity.posZ - this.blockObject.origZ;
        entity.setLocationAndAngles(x, y, z, entity.rotationYaw, entity.rotationPitch);
        this.loadedEntityList.add(entity);

        entity.setEntityId(++this.nextEntityId);
        this.entityIdMap.put(entity.getEntityId(), entity);
    }

    //AnvilChunkLoader
    public static NBTTagList writeEntitiesToNBT(List<Entity> list) {
        NBTTagList tagList = new NBTTagList();
        //if(entity instanceof EntityPlayer){continue;}
        list.forEach(entity -> {
            NBTTagCompound nbt = new NBTTagCompound();
            try {
                if (entity.writeToNBTOptional(nbt)) {
                    tagList.appendTag(nbt);
                }
            } catch (Exception e) {
                FMLLog.log(Level.ERROR, e,
                        "An Entity type %s has thrown an exception trying to write NBT.",
                        entity.getClass().getName());
            }
        });
        return tagList;
    }

    @Override
    protected IChunkProvider createChunkProvider() {
        return null;
    }

    @Override
    protected boolean chunkExists(int x, int z) {
        return false;
    }

    @Override
    public Chunk getChunkFromChunkCoords(int x, int z) {
        return null;
    }

    @Override
    public boolean setBlock(int x, int y, int z, Block block, int meta, int flag) {
        return false;
    }

    @Override
    public boolean setBlockMetadataWithNotify(int x, int y, int z, int meta, int flag) {
        return false;
    }

    @Override
    protected int func_152379_p() {
        return 0;
    }

    @Override
    public Entity getEntityByID(int id) {
        return this.entityIdMap.get(id);
    }

    @Override
    public List getEntitiesWithinAABBExcludingEntity(Entity entity, AxisAlignedBB aabb, IEntitySelector selector) {
        return new ArrayList();
    }

    @Override
    public List selectEntitiesWithinAABB(Class clazz, AxisAlignedBB aabb, IEntitySelector selector) {
        return new ArrayList();
    }

    @Override
    public boolean spawnEntityInWorld(Entity par1) {
        this.loadedEntityList.add(par1);
        return true;
    }

    /******************************************************************/

    public List<TileEntity> getTileEntityList() {
        return this.renderTileEntities;
    }

    public void setTileEntityList(List<TileEntity> list) {
        this.renderTileEntities.addAll(list);
    }

    public List<Entity> getEntityList() {
        return this.loadedEntityList;
    }

    @Override
    public BlockSet getBlockSet(int x, int y, int z) {
        return this.blockObject.getBlockSet(x, y, z);
    }

    @Override
    public Block getBlock(int x, int y, int z) {
        return this.getBlockSet(x, y, z).block;
    }

    @Override
    public TileEntity getTileEntity(int x, int y, int z) {
        BlockSet set = this.getBlockSet(x, y, z);
        if (set.block.hasTileEntity(set.metadata)) {
            TileEntity tile = null;
            try {
                tile = set.block.createTileEntity(this, set.metadata);
                tile.setWorldObj(this);
                if (set.nbt != null) {
                    tile.readFromNBT((NBTTagCompound) set.nbt.copy());
                }
            } catch (Exception e) {
                return null;
            }

            if (tile instanceof TileEntityCustom) {
                ((TileEntityCustom) tile).setPos(x, y, z, x + this.blockObject.origX, y + this.blockObject.origY, z + this.blockObject.origZ);
            } else {
                tile.xCoord = x;
                tile.yCoord = y;
                tile.zCoord = z;
            }
            return tile;
        }
        return null;
    }

    @Override
    public float getLightBrightness(int x, int y, int z) {
        return this.world.provider.lightBrightnessTable[this.getBlockLightValue(x, y, z)];
    }

    @Override
    public int getBlockLightValue(int x, int y, int z) {
        return this.getLightValue(x, y, z, 0, false);
    }

    @Override
    public int getLightBrightnessForSkyBlocks(int x, int y, int z, int defaultValue) {
        return this.getLightValue(x, y, z, defaultValue, true);
    }

    private int getLightValue(int x, int y, int z, int defaultValue, boolean blend) {
        int skyLight = this.getSkyBlockTypeBrightness(EnumSkyBlock.Sky, x, y, z);
        int blockLight = this.getSkyBlockTypeBrightness(EnumSkyBlock.Block, x, y, z);

        if (blockLight < defaultValue) {
            blockLight = defaultValue;
        }

        int brightness = NGTUtil.getLightValue(this.world, this.posX, this.posY, this.posZ);
        if (blockLight < brightness) {
            blockLight = brightness;
        }

        if (this.posY < 0) {
            skyLight = blockLight = 15;
        }

        return blend ? (skyLight << 20 | blockLight << 4) : (Math.max(skyLight, blockLight));
    }

    @Override
    public int getSkyBlockTypeBrightness(EnumSkyBlock skyBlock, int x, int y, int z) {
        boolean inLange = x >= 0 && y >= 0 && z >= 0 && x < this.blockObject.xSize && y < this.blockObject.ySize && z < this.blockObject.zSize;

        if (!inLange || this.getBlock(x, y, z).getUseNeighborBrightness()) {
            int l = this.getSpecialBlockBrightness(skyBlock, x, y + 1, z);
            int i1 = this.getSpecialBlockBrightness(skyBlock, x + 1, y, z);
            int j1 = this.getSpecialBlockBrightness(skyBlock, x - 1, y, z);
            int k1 = this.getSpecialBlockBrightness(skyBlock, x, y, z + 1);
            int l1 = this.getSpecialBlockBrightness(skyBlock, x, y, z - 1);

            if (i1 > l) {
                l = i1;
            }

            if (j1 > l) {
                l = j1;
            }

            if (k1 > l) {
                l = k1;
            }

            if (l1 > l) {
                l = l1;
            }

            return l;
        } else {
            return this.getBlock(x, y, z).getLightValue();
            //return this.getSpecialBlockBrightness(skyBlock, x, y, z);
        }
    }

    private int getSpecialBlockBrightness(EnumSkyBlock skyBlock, int x, int y, int z) {
        Block block = this.getBlock(x, y, z);
        if (skyBlock == EnumSkyBlock.Block) {
            return block.getLightValue();
        }
        return this.canBlockSeeTheSky(x, y, z) ? skyBlock.defaultLightValue : 0;
    }

    @Override
    public boolean canBlockSeeTheSky(int x, int y, int z) {
        int y0 = y + 1;
        while (y0 < this.blockObject.ySize) {
            if (this.getBlock(x, y0, z) != Blocks.air) {
                return false;
            }
            ++y0;
        }
        return true;//this.world.canBlockSeeTheSky(this.posX, this.posY, this.posZ);
    }

    @Override
    public int getBlockMetadata(int x, int y, int z) {
        BlockSet set = this.getBlockSet(x, y, z);
        if (set.block == Blocks.air) {
            return 15;//明るさ変わらず
        }
        return set.metadata;
    }

	/*@Override
	public int isBlockProvidingPowerTo(int x, int y, int z, int side)
	{
		return 0;
	}*/

    @Override
    public boolean isAirBlock(int x, int y, int z) {
        return this.getBlockSet(x, y, z).block == Blocks.air;
    }

    @Override
    public BiomeGenBase getBiomeGenForCoords(int x, int z) {
        return BiomeGenBase.ocean;
    }

    @Override
    public int getHeight() {
        return 64;
    }

    @Override
    public boolean extendedLevelsInChunkCache() {
        return false;
    }

    @Override
    public boolean isSideSolid(int x, int y, int z, ForgeDirection side, boolean _default) {
        boolean inLange = x >= 0 && y >= 0 && z >= 0 && x < this.blockObject.xSize && y < this.blockObject.ySize && z < this.blockObject.zSize;
        if (inLange) {
            return this.getBlockSet(x, y, z).block.isSideSolid(this, x, y, z, side);
        }
        return _default;
    }

    @Override
    public boolean blockExists(int x, int y, int z) {
        //return (x >= 0 && y >= 0 && z >= 0 && x < this.blockObject.xSize && y < this.blockObject.ySize && z < this.blockObject.zSize);
        return true;
    }

    @Override
    public boolean func_147451_t(int x, int y, int z)//ミニチュアのreadNBTで光更新したときのぬるぽを防ぐ
    {
        return false;
    }

    @Override
    public void markTileEntityChunkModified(int x, int y, int z, TileEntity tile) {
    }
}