package jp.ngt.rtm.util;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class DummyViewer extends EntityLivingBase {
    public DummyViewer(World world, double x, double y, double z, float yaw, float pitch) {
        super(world);
        this.setLocationAndAngles(x, y, z, yaw, pitch);
    }

    @Override
    public ItemStack getHeldItem() {
        return null;
    }

    @Override
    public ItemStack getEquipmentInSlot(int par1) {
        return null;
    }

    @Override
    public void setCurrentItemOrArmor(int par1, ItemStack par2) {
    }

    @Override
    public ItemStack[] getLastActiveItems() {
        return null;
    }

    @Override
    public void setLocationAndAngles(double x, double y, double z, float yaw, float pitch) {
        super.setLocationAndAngles(x, y, z, yaw, pitch);
        this.chunkCoordX = MathHelper.floor_double(x);// >> 4
        this.chunkCoordY = MathHelper.floor_double(y);
        this.chunkCoordZ = MathHelper.floor_double(z);
    }
}