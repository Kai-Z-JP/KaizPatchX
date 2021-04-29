package jp.ngt.mcte.block;

import jp.ngt.mcte.MCTE;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;

public class TileEntityMinesweeper extends TileEntity {
    private int xSize;
    private int zSize;
    private int centerX;
    private int centerZ;

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        this.xSize = nbt.getInteger("xSize");
        this.zSize = nbt.getInteger("zSize");
        this.centerX = nbt.getInteger("centerX");
        this.centerZ = nbt.getInteger("centerZ");
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        nbt.setInteger("xSize", this.xSize);
        nbt.setInteger("zSize", this.zSize);
        nbt.setInteger("centerX", this.centerX);
        nbt.setInteger("centerZ", this.centerZ);
    }

    public void setSize(int x, int z) {
        this.xSize = x;
        this.zSize = z;
    }

    public void setCenter(int x, int z) {
        this.centerX = x;
        this.centerZ = z;
    }

    public void check() {
        boolean flag = true;
        for (int i = 0; i < this.xSize; ++i) {
            for (int j = 0; j < this.zSize; ++j) {
                if (this.worldObj.getBlock(this.centerX + i, this.yCoord, this.centerZ + j) == MCTE.minesweeper) {
                    int meta = this.worldObj.getBlockMetadata(this.centerX + i, this.yCoord, this.centerZ + j);
                    if (meta == 10 || meta == 11 || meta == 12) {
                        flag = false;
                    }
                } else {
                    flag = false;
                }
            }
        }

        if (flag) {
            for (int i = 0; i < this.xSize; ++i) {
                for (int j = 0; j < this.zSize; ++j) {
                    int meta = this.worldObj.getBlockMetadata(this.centerX + i, this.yCoord, this.centerZ + j);
                    if (meta < 9) {
                        this.worldObj.setBlock(this.centerX + i, this.yCoord, this.centerZ + j, Blocks.gold_block, 0, 3);
                    } else if (meta == 13) {
                        this.worldObj.setBlock(this.centerX + i, this.yCoord, this.centerZ + j, Blocks.diamond_block, 0, 3);
                    }
                }
            }
        }
    }
}