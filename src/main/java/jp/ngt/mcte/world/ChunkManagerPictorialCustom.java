package jp.ngt.mcte.world;

import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraft.world.biome.WorldChunkManager;

import java.util.Arrays;

public class ChunkManagerPictorialCustom extends WorldChunkManager {
    protected WorldData worldData;

    public ChunkManagerPictorialCustom(World par1, WorldData par2) {
        super(par1);
        this.worldData = par2;
    }

    @Override
    public BiomeGenBase getBiomeGenAt(int par1, int par2) {
        return this.worldData.getWorldGenerator().getBiome(par1, par2);
    }

    @Override
    public BiomeGenBase[] getBiomesForGeneration(BiomeGenBase[] par1, int par2, int par3, int par4, int par5) {
        int i0 = par4 * par5;
        if (par1 == null || par1.length < i0) {
            par1 = new BiomeGenBase[i0];
        }

        if (par4 == 16 && par5 == 16) {
            for (int i = 0; i < 16; ++i) {
                for (int j = 0; j < 16; ++j) {
                    par1[i * 16 + j] = this.worldData.getWorldGenerator().getBiome(par2 + j, par3 + i);
                }
            }
        } else {
            Arrays.fill(par1, 0, i0, BiomeGenBase.plains);
        }

        return par1;
    }

    @Override
    public float[] getRainfall(float[] par1, int par2, int par3, int par4, int par5) {
        int i0 = par4 * par5;
        if (par1 == null || par1.length < i0) {
            par1 = new float[i0];
        }

        if (par4 == 16 && par5 == 16) {
            for (int i = 0; i < 16; ++i) {
                for (int j = 0; j < 16; ++j) {
                    par1[i * 16 + j] = this.worldData.getWorldGenerator().getBiome(par2 + j, par3 + i).rainfall;
                }
            }
        } else {
            Arrays.fill(par1, 0, i0, BiomeGenBase.plains.rainfall);
        }

        return par1;
    }

    @Override
    public BiomeGenBase[] loadBlockGeneratorData(BiomeGenBase[] par1, int par2, int par3, int par4, int par5) {
        return this.getBiomesForGeneration(par1, par2, par3, par4, par5);
    }

    @Override
    public BiomeGenBase[] getBiomeGenAt(BiomeGenBase[] par1, int par2, int par3, int par4, int par5, boolean par6) {
        return this.getBiomesForGeneration(par1, par2, par3, par4, par5);
    }
}