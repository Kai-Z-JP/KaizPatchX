package jp.ngt.mcte.block;

import jp.ngt.mcte.block.RSPort.PortType;
import jp.ngt.mcte.world.MCTEWorld;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

public class BlockPort extends Block {
    public final PortType type;

    public BlockPort(PortType par1) {
        super(Material.rock);
        this.type = par1;
    }

    //隣からRS入力
    @Override
    public void onNeighborBlockChange(World world, int x, int y, int z, Block block) {
        if (this.type == PortType.OUT && world instanceof MCTEWorld) {
            int power = world.getStrongestIndirectPower(x, y, z);
            if (world.getBlockMetadata(x, y, z) != power) {
                world.setBlock(x, y, z, this, power, 3);
                ((MCTEWorld) world).onPortChanged(x, y, z);
            }
        }
    }

    @Override
    public int isProvidingWeakPower(IBlockAccess world, int x, int y, int z, int side) {
        return this.isProvidingStrongPower(world, x, y, z, side);
    }

    @Override
    public int isProvidingStrongPower(IBlockAccess world, int x, int y, int z, int side) {
        return this.type == PortType.IN ? world.getBlockMetadata(x, y, z) : 0;
    }

    @Override
    public boolean canProvidePower() {
        return true;
    }

    @Override
    public boolean shouldCheckWeakPower(IBlockAccess world, int x, int y, int z, int side) {
        return this.type == PortType.OUT;//隣接ブロックからのの信号を使うか
    }
}