package jp.ngt.rtm.item;


import jp.ngt.rtm.RTMCore;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;

public class ItemCamera extends Item {
    @Override
    public boolean onItemUse(ItemStack itemstack, EntityPlayer player, World world, int x, int y, int z, int side, float p, float q, float r) {
        if (world.isRemote) {
            player.openGui(RTMCore.instance, RTMCore.guiIdCamera, world, x, y, z);
        }
        return false;
    }
}
