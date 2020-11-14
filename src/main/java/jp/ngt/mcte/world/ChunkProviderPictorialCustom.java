package jp.ngt.mcte.world;

import cpw.mods.fml.common.eventhandler.Event.Result;
import net.minecraft.block.Block;
import net.minecraft.block.BlockFalling;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EnumCreatureType;
import net.minecraft.init.Blocks;
import net.minecraft.util.IProgressUpdate;
import net.minecraft.util.MathHelper;
import net.minecraft.world.ChunkPosition;
import net.minecraft.world.SpawnerAnimals;
import net.minecraft.world.World;
import net.minecraft.world.WorldType;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.chunk.Chunk;
import net.minecraft.world.chunk.IChunkProvider;
import net.minecraft.world.gen.*;
import net.minecraft.world.gen.feature.WorldGenDungeons;
import net.minecraft.world.gen.feature.WorldGenLakes;
import net.minecraft.world.gen.structure.MapGenMineshaft;
import net.minecraft.world.gen.structure.MapGenScatteredFeature;
import net.minecraft.world.gen.structure.MapGenStronghold;
import net.minecraft.world.gen.structure.MapGenVillage;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.terraingen.ChunkProviderEvent;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import net.minecraftforge.event.terraingen.TerrainGen;

import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

import static net.minecraftforge.event.terraingen.InitMapGenEvent.EventType.*;
import static net.minecraftforge.event.terraingen.PopulateChunkEvent.Populate.EventType.*;

public class ChunkProviderPictorialCustom implements IChunkProvider {
	/**
	 * RNG.
	 */
	private final Random rand;
	private NoiseGeneratorOctaves noiseOct0;
	private NoiseGeneratorOctaves noiseOct1;
	private NoiseGeneratorOctaves noiseOct2;
	private NoiseGeneratorPerlin noisePer0;
	public NoiseGeneratorOctaves noiseGen5;
	public NoiseGeneratorOctaves noiseGen6;
	public NoiseGeneratorOctaves mobSpawnerNoise;
	private final World worldObj;
	/**
	 * are map structures going to be generated (e.g. strongholds)
	 */
	private final boolean mapFeaturesEnabled;
	private final WorldType field_147435_p;
	private final double[] field_147434_q;
	private final float[] parabolicField;
	private double[] stoneNoise = new double[256];
	private MapGenBase caveGenerator = new MapGenCaves();
	private MapGenStronghold strongholdGenerator = new MapGenStronghold();
	private MapGenVillage villageGenerator = new MapGenVillage();
	private MapGenMineshaft mineshaftGenerator = new MapGenMineshaft();
	private MapGenScatteredFeature scatteredFeatureGenerator = new MapGenScatteredFeature();
	private MapGenBase ravineGenerator = new MapGenRavine();
	private BiomeGenBase[] biomesForGeneration;
    /*double[] field_147427_d;
    double[] field_147428_e;
    double[] field_147425_f;
    double[] field_147426_g;
    int[][] field_73219_j = new int[32][32];*/

	private final WorldData worldData;

	{
		caveGenerator = TerrainGen.getModdedMapGen(caveGenerator, CAVE);
		strongholdGenerator = (MapGenStronghold) TerrainGen.getModdedMapGen(strongholdGenerator, STRONGHOLD);
		villageGenerator = (MapGenVillage) TerrainGen.getModdedMapGen(villageGenerator, VILLAGE);
		mineshaftGenerator = (MapGenMineshaft) TerrainGen.getModdedMapGen(mineshaftGenerator, MINESHAFT);
		scatteredFeatureGenerator = (MapGenScatteredFeature) TerrainGen.getModdedMapGen(scatteredFeatureGenerator, SCATTERED_FEATURE);
		ravineGenerator = TerrainGen.getModdedMapGen(ravineGenerator, RAVINE);
	}

	public ChunkProviderPictorialCustom(World par1, long par2, boolean par3, WorldData par4) {
		this.worldObj = par1;
		this.mapFeaturesEnabled = par3;
		this.worldData = par4;
		this.field_147435_p = par1.getWorldInfo().getTerrainType();
		this.rand = new Random(par2);
		this.noiseOct0 = new NoiseGeneratorOctaves(this.rand, 16);
		this.noiseOct1 = new NoiseGeneratorOctaves(this.rand, 16);
		this.noiseOct2 = new NoiseGeneratorOctaves(this.rand, 8);
		this.noisePer0 = new NoiseGeneratorPerlin(this.rand, 4);
		this.noiseGen5 = new NoiseGeneratorOctaves(this.rand, 10);
		this.noiseGen6 = new NoiseGeneratorOctaves(this.rand, 16);
		this.mobSpawnerNoise = new NoiseGeneratorOctaves(this.rand, 8);
		this.field_147434_q = new double[825];
		this.parabolicField = new float[25];

		for (int j = -2; j <= 2; ++j) {
			for (int k = -2; k <= 2; ++k) {
				float f = 10.0F / MathHelper.sqrt_float((float) (j * j + k * k) + 0.2F);
				this.parabolicField[j + 2 + (k + 2) * 5] = f;
			}
		}

		NoiseGenerator[] noiseGens = {noiseOct0, noiseOct1, noiseOct2, noisePer0, noiseGen5, noiseGen6, mobSpawnerNoise};
		noiseGens = TerrainGen.getModdedNoiseGenerators(par1, this.rand, noiseGens);
		this.noiseOct0 = (NoiseGeneratorOctaves) noiseGens[0];
		this.noiseOct1 = (NoiseGeneratorOctaves) noiseGens[1];
		this.noiseOct2 = (NoiseGeneratorOctaves) noiseGens[2];
		this.noisePer0 = (NoiseGeneratorPerlin) noiseGens[3];
		this.noiseGen5 = (NoiseGeneratorOctaves) noiseGens[4];
		this.noiseGen6 = (NoiseGeneratorOctaves) noiseGens[5];
		this.mobSpawnerNoise = (NoiseGeneratorOctaves) noiseGens[6];
	}

	/**
	 * 地形のベースと海の生成
	 *
	 * @param par1 chunkX
	 * @param par2 chunkZ
	 * @param par3 16*16*256
	 */
	public void func_147424_a(int par1, int par2, Block[] par3) {
		int x = par1 << 4;
		int z = par2 << 4;

		for (int i = 0; i < 16; ++i) {
			for (int j = 0; j < 16; ++j) {
				int height = this.worldData.getWorldGenerator().getHeight(x + i, z + j);
				int y = height;
				if (this.worldData.seaLevel > y) {
					y = this.worldData.seaLevel;
				}
				for (int k = 0; k < y; ++k) {
					if (k < height) {
						par3[k + j * 256 + i * 4096] = this.worldData.baseBlock;
					} else {
						par3[k + j * 256 + i * 4096] = Blocks.water;
					}
				}
			}
		}
	}

	/**
	 * 岩盤の生成もここで行われる
	 */
	public void replaceBlocksForBiome(int par1, int par2, Block[] par3, byte[] par4, BiomeGenBase[] par5) {
		ChunkProviderEvent.ReplaceBiomeBlocks event = new ChunkProviderEvent.ReplaceBiomeBlocks(this, par1, par2, par3, par4, par5, this.worldObj);
		MinecraftForge.EVENT_BUS.post(event);
		if (event.getResult() == Result.DENY) return;

		double d0 = 0.03125D;
		this.stoneNoise = this.noisePer0.func_151599_a(this.stoneNoise, par1 * 16, par2 * 16, 16, 16, d0 * 2.0D, d0 * 2.0D, 1.0D);

		for (int k = 0; k < 16; ++k) {
			for (int l = 0; l < 16; ++l) {
				BiomeGenBase biomegenbase = par5[l + k * 16];
				this.genBiomeTerrain(biomegenbase, this.worldObj, this.rand, par3, par4, par1 * 16 + k, par2 * 16 + l, this.stoneNoise[l + k * 16]);
				//biomegenbase.genTerrainBlocks(this.worldObj, this.rand, par3, par4, par1 * 16 + k, par2 * 16 + l, this.stoneNoise[l + k * 16]);
			}
		}
	}

	private void genBiomeTerrain(BiomeGenBase biome, World world, Random random, Block[] blocks, byte[] bytes, int x, int z, double noise) {
		Block block = biome.topBlock;
		Block block1 = biome.fillerBlock;
		byte b0 = (byte) (biome.field_150604_aj & 255);
		int k = -1;
		int l = (int) (noise / 3.0D + 3.0D + random.nextDouble() * 0.25D);
		int x0 = x & 15;
		int z0 = z & 15;
		int k1 = blocks.length / 256;
		int sea = this.worldData.seaLevel;

		for (int i = 255; i >= 0; --i) {
			int i2 = (z0 * 16 + x0) * k1 + i;

			if (i <= random.nextInt(5)) {
				blocks[i2] = Blocks.bedrock;
			} else {
				Block block2 = blocks[i2];

				if (block2 != null && block2.getMaterial() != Material.air) {
					if (block2 == this.worldData.baseBlock) {
						if (k == -1) {
							if (l <= 0) {
								block = null;
								b0 = 0;
								block1 = this.worldData.baseBlock;
							} else if (i >= sea - 4 && i <= sea + 1) {
								block = biome.topBlock;
								b0 = (byte) (biome.field_150604_aj & 255);
								block1 = biome.fillerBlock;
							}

							if (i < sea && (block == null || block.getMaterial() == Material.air)) {
								if (biome.getFloatTemperature(x, i, z) < 0.15F) {
									block = Blocks.ice;
									b0 = 0;
								} else {
									block = Blocks.water;
									b0 = 0;
								}
							}

							k = l;

							if (i >= sea - 1) {
								blocks[i2] = block;
								bytes[i2] = b0;
							} else if (i < sea - 7 - l) {
								block = null;
								block1 = this.worldData.baseBlock;
								blocks[i2] = Blocks.gravel;
							} else {
								blocks[i2] = block1;
							}
						} else if (k > 0) {
							--k;
							blocks[i2] = block1;

							if (k == 0 && block1 == Blocks.sand) {
								k = random.nextInt(4) + Math.max(0, i - sea);
								block1 = Blocks.sandstone;
							}
						}
					}
				} else {
					k = -1;
				}
			}
		}
	}

	/**
	 * loads or generates the chunk at the chunk location specified
	 */
	@Override
	public Chunk loadChunk(int par1, int par2) {
		return this.provideChunk(par1, par2);
	}

	/**
	 * Will return back a chunk, if it doesn't exist and its not a MP client it will generates all the blocks for the
	 * specified chunk from the map seed and chunk seed
	 */
	@Override
	public Chunk provideChunk(int par1, int par2) {
		this.rand.setSeed((long) par1 * 341873128712L + (long) par2 * 132897987541L);
		Block[] ablock = new Block[65536];
		byte[] abyte = new byte[65536];
		this.func_147424_a(par1, par2, ablock);
		this.biomesForGeneration = this.worldObj.getWorldChunkManager().loadBlockGeneratorData(this.biomesForGeneration, par1 * 16, par2 * 16, 16, 16);
		this.replaceBlocksForBiome(par1, par2, ablock, abyte, this.biomesForGeneration);
		this.caveGenerator.func_151539_a(this, this.worldObj, par1, par2, ablock);
		this.ravineGenerator.func_151539_a(this, this.worldObj, par1, par2, ablock);

		if (this.mapFeaturesEnabled) {
			this.mineshaftGenerator.func_151539_a(this, this.worldObj, par1, par2, ablock);
			this.villageGenerator.func_151539_a(this, this.worldObj, par1, par2, ablock);
			this.strongholdGenerator.func_151539_a(this, this.worldObj, par1, par2, ablock);
			this.scatteredFeatureGenerator.func_151539_a(this, this.worldObj, par1, par2, ablock);
		}

		Chunk chunk = new Chunk(this.worldObj, ablock, abyte, par1, par2);
		byte[] abyte1 = chunk.getBiomeArray();

		IntStream.range(0, abyte1.length).forEach(k -> abyte1[k] = (byte) this.biomesForGeneration[k].biomeID);

		chunk.generateSkylightMap();
		return chunk;
	}

	/**
	 * Checks to see if a chunk exists at x, y
	 */
	@Override
	public boolean chunkExists(int p_73149_1_, int p_73149_2_) {
		return true;
	}

	/**
	 * Populates chunk with ores etc etc
	 */
	@Override
	public void populate(IChunkProvider p_73153_1_, int p_73153_2_, int p_73153_3_) {
		BlockFalling.fallInstantly = true;
		int k = p_73153_2_ * 16;
		int l = p_73153_3_ * 16;
		BiomeGenBase biomegenbase = this.worldObj.getBiomeGenForCoords(k + 16, l + 16);
		this.rand.setSeed(this.worldObj.getSeed());
		long i1 = this.rand.nextLong() / 2L * 2L + 1L;
		long j1 = this.rand.nextLong() / 2L * 2L + 1L;
		this.rand.setSeed((long) p_73153_2_ * i1 + (long) p_73153_3_ * j1 ^ this.worldObj.getSeed());
		boolean flag = false;

		MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Pre(p_73153_1_, worldObj, rand, p_73153_2_, p_73153_3_, flag));

		if (this.mapFeaturesEnabled) {
			this.mineshaftGenerator.generateStructuresInChunk(this.worldObj, this.rand, p_73153_2_, p_73153_3_);
			flag = this.villageGenerator.generateStructuresInChunk(this.worldObj, this.rand, p_73153_2_, p_73153_3_);
			this.strongholdGenerator.generateStructuresInChunk(this.worldObj, this.rand, p_73153_2_, p_73153_3_);
			this.scatteredFeatureGenerator.generateStructuresInChunk(this.worldObj, this.rand, p_73153_2_, p_73153_3_);
		}

		int k1;
		int l1;
		int i2;

		if (biomegenbase != BiomeGenBase.desert && biomegenbase != BiomeGenBase.desertHills && !flag && this.rand.nextInt(4) == 0
				&& TerrainGen.populate(p_73153_1_, worldObj, rand, p_73153_2_, p_73153_3_, flag, LAKE)) {
			k1 = k + this.rand.nextInt(16) + 8;
			l1 = this.rand.nextInt(256);
			i2 = l + this.rand.nextInt(16) + 8;
			(new WorldGenLakes(Blocks.water)).generate(this.worldObj, this.rand, k1, l1, i2);
		}

		if (TerrainGen.populate(p_73153_1_, worldObj, rand, p_73153_2_, p_73153_3_, flag, LAVA) && !flag && this.rand.nextInt(8) == 0) {
			k1 = k + this.rand.nextInt(16) + 8;
			l1 = this.rand.nextInt(this.rand.nextInt(248) + 8);
			i2 = l + this.rand.nextInt(16) + 8;

			if (l1 < 63 || this.rand.nextInt(10) == 0) {
				(new WorldGenLakes(Blocks.lava)).generate(this.worldObj, this.rand, k1, l1, i2);
			}
		}

		boolean doGen = TerrainGen.populate(p_73153_1_, worldObj, rand, p_73153_2_, p_73153_3_, flag, DUNGEON);
		for (k1 = 0; doGen && k1 < 8; ++k1) {
			l1 = k + this.rand.nextInt(16) + 8;
			i2 = this.rand.nextInt(256);
			int j2 = l + this.rand.nextInt(16) + 8;
			(new WorldGenDungeons()).generate(this.worldObj, this.rand, l1, i2, j2);
		}

		biomegenbase.decorate(this.worldObj, this.rand, k, l);
		if (TerrainGen.populate(p_73153_1_, worldObj, rand, p_73153_2_, p_73153_3_, flag, ANIMALS)) {
			SpawnerAnimals.performWorldGenSpawning(this.worldObj, biomegenbase, k + 8, l + 8, 16, 16, this.rand);
		}
		k += 8;
		l += 8;

		doGen = TerrainGen.populate(p_73153_1_, worldObj, rand, p_73153_2_, p_73153_3_, flag, ICE);
		for (k1 = 0; doGen && k1 < 16; ++k1) {
			for (l1 = 0; l1 < 16; ++l1) {
				i2 = this.worldObj.getPrecipitationHeight(k + k1, l + l1);

				if (this.worldObj.isBlockFreezable(k1 + k, i2 - 1, l1 + l)) {
					this.worldObj.setBlock(k1 + k, i2 - 1, l1 + l, Blocks.ice, 0, 2);
				}

				if (this.worldObj.func_147478_e(k1 + k, i2, l1 + l, true)) {
					this.worldObj.setBlock(k1 + k, i2, l1 + l, Blocks.snow_layer, 0, 2);
				}
			}
		}

		MinecraftForge.EVENT_BUS.post(new PopulateChunkEvent.Post(p_73153_1_, worldObj, rand, p_73153_2_, p_73153_3_, flag));

		BlockFalling.fallInstantly = false;
	}

	/**
	 * Two modes of operation: if passed true, save all Chunks in one go.  If passed false, save up to two chunks.
	 * Return true if all chunks have been saved.
	 */
	@Override
	public boolean saveChunks(boolean p_73151_1_, IProgressUpdate p_73151_2_) {
		return true;
	}

	/**
	 * Save extra data not associated with any Chunk.  Not saved during autosave, only during world unload.  Currently
	 * unimplemented.
	 */
	@Override
	public void saveExtraData() {
	}

	/**
	 * Unloads chunks that are marked to be unloaded. This is not guaranteed to unload every such chunk.
	 */
	@Override
	public boolean unloadQueuedChunks() {
		return false;
	}

	/**
	 * Returns if the IChunkProvider supports saving.
	 */
	@Override
	public boolean canSave() {
		return true;
	}

	/**
	 * Converts the instance data to a readable string.
	 */
	@Override
	public String makeString() {
		return "MCTERandomLevelSource";
	}

	/**
	 * Returns a list of creatures of the specified type that can spawn at the given location.
	 */
	public List getPossibleCreatures(EnumCreatureType p_73155_1_, int p_73155_2_, int p_73155_3_, int p_73155_4_) {
		BiomeGenBase biomegenbase = this.worldObj.getBiomeGenForCoords(p_73155_2_, p_73155_4_);
		return p_73155_1_ == EnumCreatureType.monster && this.scatteredFeatureGenerator.func_143030_a(p_73155_2_, p_73155_3_, p_73155_4_) ? this.scatteredFeatureGenerator.getScatteredFeatureSpawnList() : biomegenbase.getSpawnableList(p_73155_1_);
	}

	public ChunkPosition func_147416_a(World p_147416_1_, String p_147416_2_, int p_147416_3_, int p_147416_4_, int p_147416_5_) {
		return "Stronghold".equals(p_147416_2_) && this.strongholdGenerator != null ? this.strongholdGenerator.func_151545_a(p_147416_1_, p_147416_3_, p_147416_4_, p_147416_5_) : null;
	}

	@Override
	public int getLoadedChunkCount() {
		return 0;
	}

	@Override
	public void recreateStructures(int p_82695_1_, int p_82695_2_) {
		if (this.mapFeaturesEnabled) {
			this.mineshaftGenerator.func_151539_a(this, this.worldObj, p_82695_1_, p_82695_2_, null);
			this.villageGenerator.func_151539_a(this, this.worldObj, p_82695_1_, p_82695_2_, null);
			this.strongholdGenerator.func_151539_a(this, this.worldObj, p_82695_1_, p_82695_2_, null);
			this.scatteredFeatureGenerator.func_151539_a(this, this.worldObj, p_82695_1_, p_82695_2_, null);
		}
	}
}