package jp.ngt.rtm.entity.npc;

import jp.ngt.rtm.entity.train.parts.EntityFloor;
import jp.ngt.rtm.entity.vehicle.EntityVehicleBase;
import net.minecraft.command.IEntitySelector;
import net.minecraft.entity.Entity;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.AxisAlignedBB;

import java.util.List;

public class NPCAIRideTrain extends EntityAIBase {
    private static final double MAX_RANGE = 32.0D;

    private static final IEntitySelector SELECTOR = new IEntitySelector() {
        @Override
        public boolean isEntityApplicable(Entity entity) {
            return (entity instanceof EntityFloor);
        }
    };

    private final EntityNPC npc;
    private final float moveSpeed;
    private EntityFloor target;
    private PathEntity entityPathNavigate;

    public EntityVehicleBase targetTrain;

    public NPCAIRideTrain(EntityNPC par1, float par2) {
        this.npc = par1;
        this.moveSpeed = par2;
    }

    @Override
    public boolean shouldExecute() {
        return this.setTargetSeat();
    }

	/*private boolean setTargetTrain()
	{
		double range = 32.0D;
		AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(-range, -8.0D, -range, range, 16.0D, range);
		aabb.offset(this.npc.posX, this.npc.posY, this.npc.posZ);
		List list = this.npc.worldObj.getEntitiesWithinAABBExcludingEntity(this.npc, aabb, new IEntitySelector() {
			@Override
			public boolean isEntityApplicable(Entity entity)
			{
				return (entity instanceof EntityTrainBase);
			}
		});

		if(!list.isEmpty())
		{
			for(Object obj : list)
			{
				EntityTrainBase train = (EntityTrainBase)obj;
				if(train != this.target && train.getSpeed() == 0.0F)
				{
					this.entityPathNavigate = this.npc.getNavigator().getPathToEntityLiving(train);
					if(this.entityPathNavigate != null)
					{
						this.target = train;
						return true;
					}
				}
			}
		}

		return false;
	}*/

    @Override
    public boolean continueExecuting() {
        if (this.target.isDead) {
            return false;
        } else if (this.targetTrain.getSpeed() != 0.0F || this.npc.getDistanceSqToEntity(this.targetTrain) > MAX_RANGE * MAX_RANGE) {
            return false;
        } else if (this.npc.getDistanceSqToEntity(this.target) < 9.0D) {
            if (this.target.ridingEntity == null) {
                this.npc.mountEntity(this.target);
                return false;
            }
            return this.setTargetSeat();
        }

        return !this.npc.getNavigator().noPath();
    }

    private boolean setTargetSeat() {
        AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(-MAX_RANGE, -8.0D, -MAX_RANGE, MAX_RANGE, 16.0D, MAX_RANGE);
        aabb.offset(this.npc.posX, this.npc.posY, this.npc.posZ);
        List list = this.npc.worldObj.getEntitiesWithinAABBExcludingEntity(this.npc, aabb, SELECTOR);

        if (!list.isEmpty()) {
            EntityFloor nextTarget = null;
            double distance = Double.MAX_VALUE;
            for (Object obj : list) {
                EntityFloor floor = (EntityFloor) obj;
                EntityVehicleBase train = floor.getVehicle();
                if (train != null && train != this.targetTrain && train.getSpeed() == 0.0F && floor.riddenByEntity == null) {
                    double dsq = this.npc.getDistanceSqToEntity(floor);
                    if (dsq < distance) {
                        nextTarget = floor;
                        distance = dsq;
                    }
                }
            }

            if (nextTarget != null) {
                this.entityPathNavigate = this.npc.getNavigator().getPathToEntityLiving(nextTarget);
                if (this.entityPathNavigate != null) {
                    this.target = nextTarget;
                    this.targetTrain = nextTarget.getVehicle();
                    return true;
                }
            }
        }

        return false;
    }

    @Override
    public void startExecuting() {
        this.npc.getNavigator().setPath(this.entityPathNavigate, this.moveSpeed);
    }
}