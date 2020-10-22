package jp.ngt.rtm.util;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.pathfinding.PathNavigate;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class PathNavigateCustom extends PathNavigate {
	protected EntityLiving theEntity;
	protected boolean canPassOpenWoodenDoors = true;
	protected boolean canSwim;

	public PathNavigateCustom(EntityLiving entity, World world) {
		super(entity, world);
		this.theEntity = entity;
	}

	@Override
	public void setEnterDoors(boolean par1) {
		super.setEnterDoors(par1);
		this.canPassOpenWoodenDoors = par1;
	}

	@Override
	public void setCanSwim(boolean par1) {
		super.setCanSwim(par1);
		this.canSwim = par1;
	}

	@Override
	public PathEntity getPathToXYZ(double p_75488_1_, double p_75488_3_, double p_75488_5_) {
		return !this.canNavigate() ? null : PathFinderCustom.getEntityPathToXYZ(this.theEntity, MathHelper.floor_double(p_75488_1_), (int) p_75488_3_, MathHelper.floor_double(p_75488_5_), this.getPathSearchRange(), this.canPassOpenWoodenDoors, this.getCanBreakDoors(), this.getAvoidsWater(), this.canSwim);
	}

	@Override
	public PathEntity getPathToEntityLiving(Entity p_75494_1_) {
		return !this.canNavigate() ? null : PathFinderCustom.getPathEntityToEntity(this.theEntity, p_75494_1_, this.getPathSearchRange(), this.canPassOpenWoodenDoors, this.getCanBreakDoors(), this.getAvoidsWater(), this.canSwim);
	}

	protected boolean canNavigate() {
		return this.theEntity.onGround || this.canSwim && this.isInLiquid();
	}

	protected boolean isInLiquid() {
		return this.theEntity.isInWater() || this.theEntity.handleLavaMovement();
	}
}