package jp.ngt.rtm.entity.vehicle;

import jp.ngt.rtm.RTMItem;
import net.minecraft.block.material.Material;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class EntityShip extends EntityVehicle {
    public EntityShip(World world) {
        super(world);
        this.stepHeight = 0.5F;
    }

    @Override
    protected boolean shouldUpdateMotion() {
        return this.inWater;
    }

    @Override
    protected void updateMotion(EntityLivingBase entity, float moveStrafe, float moveForward) {
        super.updateMotion(entity, moveStrafe, moveForward);

        if (this.speed > 0.0D && this.inWater) {
            this.rotationRoll = moveStrafe * (float) (this.speed / this.getModelSet().getConfig().getMaxSpeed(this.onGround)) * -5.0F;
        }
    }

    @Override
    protected void updateFallState() {
        if (!this.inWater && !this.onGround) {
            this.motionY -= 0.05D;
        } else {
            AxisAlignedBB aabb = this.boundingBox.copy();
            aabb.minY += 0.0625D;
            if (this.worldObj.isAABBInMaterial(aabb, Material.water)) {
                this.motionY += 0.05D;
            } else {
                this.motionY = 0.0D;
            }
        }
    }

    @Override
    public String getDefaultName() {
        return "WoodBoat";
    }

    @Override
    protected ItemStack getVehicleItem() {
        return new ItemStack(RTMItem.itemVehicle, 1, 1);
    }
}