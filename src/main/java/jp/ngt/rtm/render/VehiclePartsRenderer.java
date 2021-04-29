package jp.ngt.rtm.render;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.entity.train.EntityBogie;
import jp.ngt.rtm.entity.train.EntityTrainBase;
import jp.ngt.rtm.entity.vehicle.EntityVehicleBase;
import jp.ngt.rtm.modelpack.modelset.ModelSetVehicleBaseClient;
import net.minecraft.entity.Entity;

@SideOnly(Side.CLIENT)
public class VehiclePartsRenderer extends EntityPartsRenderer<ModelSetVehicleBaseClient> {
    /**
     * 台車の場合はfalse
     */
    private boolean isvehicle;

    public VehiclePartsRenderer(String... par1) {
        super(par1);

        if (par1.length >= 1) {
            if ("true".equals(par1[0])) {
                this.isvehicle = true;
            } else if ("false".equals(par1[0])) {
                this.isvehicle = false;
            }
        }
    }

    @Override
    public void init(ModelSetVehicleBaseClient par1, ModelObject par2) {
        super.init(par1, par2);
    }

    public float getWheelRotationR(Entity entity) {
        if (entity == null) {
            return 0.0F;
        } else if (!this.isvehicle && entity instanceof EntityBogie) {
            EntityBogie bogie = (EntityBogie) entity;
            EntityTrainBase train = bogie.getTrain();
            if (train != null) {
                return train.wheelRotationR * (bogie.getBogieId() == 0 ? 1.0F : -1.0F);
            }
        }
        return ((EntityVehicleBase) entity).wheelRotationR;
    }

    public float getWheelRotationL(Entity entity) {
        if (entity == null) {
            return 0.0F;
        } else if (!this.isvehicle && entity instanceof EntityBogie) {
            EntityBogie bogie = (EntityBogie) entity;
            EntityTrainBase train = bogie.getTrain();
            if (train != null) {
                return train.wheelRotationL * (bogie.getBogieId() == 0 ? 1.0F : -1.0F);
            }
        }
        return ((EntityVehicleBase) entity).wheelRotationL;
    }

    public float getDoorMovementR(Entity entity) {
        if (entity == null || !this.isvehicle) {
            return 0.0F;
        }
        return (float) ((EntityVehicleBase) entity).doorMoveR / (float) EntityVehicleBase.MAX_DOOR_MOVE;
    }

    public float getDoorMovementL(Entity entity) {
        if (entity == null || !this.isvehicle) {
            return 0.0F;
        }
        return (float) ((EntityVehicleBase) entity).doorMoveL / (float) EntityVehicleBase.MAX_DOOR_MOVE;
    }

    public float getPantographMovementFront(Entity entity) {
        if (entity == null || !this.isvehicle) {
            return 0.0F;
        }
        return (float) ((EntityVehicleBase) entity).pantograph_F / (float) EntityVehicleBase.MAX_PANTOGRAPH_MOVE;
    }

    public float getPantographMovementBack(Entity entity) {
        if (entity == null || !this.isvehicle) {
            return 0.0F;
        }
        return (float) ((EntityVehicleBase) entity).pantograph_B / (float) EntityVehicleBase.MAX_PANTOGRAPH_MOVE;
    }
}