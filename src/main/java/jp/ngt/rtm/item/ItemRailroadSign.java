package jp.ngt.rtm.item;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.block.tileentity.TileEntityRailroadSign;
import net.minecraft.block.Block;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.IIcon;
import net.minecraft.world.World;

public class ItemRailroadSign extends Item {
    public ItemRailroadSign() {
        super();
        this.setHasSubtypes(true);
    }

    @Override
    public boolean onItemUse(ItemStack itemStack, EntityPlayer player, World world, int x, int y, int z, int par7, float par8, float par9, float par10) {
        if (!world.isRemote) {
            if (par7 == 0) {
                y -= 1;
            } else if (par7 == 1) {
                y += 1;
            } else {
                return false;
            }

            if (!world.isAirBlock(x, y, z)) {
                return true;
            }

            Block block = RTMBlock.railroadSign;
            world.setBlock(x, y, z, block, 0, 3);
            TileEntityRailroadSign tile = (TileEntityRailroadSign) world.getTileEntity(x, y, z);
            tile.setRotation(player, player.isSneaking() ? 1.0F : 15.0F, true);
            tile.setTexture("textures/rrs/rrs_01.png");

            world.playSoundEffect((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D,
                    block.stepSound.func_150496_b(), (block.stepSound.getVolume() + 1.0F) / 2.0F, block.stepSound.getPitch() * 0.8F);
            --itemStack.stackSize;
        }
        return true;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public IIcon getIconFromDamage(int par1) {
        return this.itemIcon;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public void registerIcons(IIconRegister register) {
        this.itemIcon = register.registerIcon("rtm:sign_0");
    }
}