package jp.ngt.mcte.world;

import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.gen.layer.GenLayer;
import net.minecraft.world.gen.layer.IntCache;

public class GenLayerPictorialCustom extends GenLayer {
	public GenLayerPictorialCustom(long par1) {
		super(par1);
	}

	@Override
	public int[] getInts(int x, int z, int width, int depth) {
		int[] dest = IntCache.getIntCache(width * depth);

		for (int dz = 0; dz < depth; dz++) {
			for (int dx = 0; dx < width; dx++) {
				this.initChunkSeed(dx + x, dz + z);
				//dest[(dx + dz * width)] = this.biomes[this.nextInt(this.biomes.length)].biomeID;
				dest[(dx + dz * width)] = BiomeGenBase.plains.biomeID;
			}
		}
		return dest;
	}
}