package jp.ngt.rtm.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.block.tileentity.TileEntityMovingMachine;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.stream.IntStream;

public class EntityMMBoundingBox extends Entity {
	private TileEntityMovingMachine movingMachine;
	private boolean checkCollision;
	private final AxisAlignedBB aabb = AxisAlignedBB.getBoundingBox(0.0D, 0.0D, 0.0D, 0.0D, 0.0D, 0.0D);

	public EntityMMBoundingBox(World world) {
		super(world);
		this.setSize(1.0F, 1.0F);
	}

	public EntityMMBoundingBox(World world, TileEntityMovingMachine tile, boolean p3) {
		this(world);
		this.movingMachine = tile;
		this.checkCollision = p3;
	}

	@Override
	protected void entityInit() {
        this.dataWatcher.addObject(10, -1.0F);
        this.dataWatcher.addObject(11, 0.0F);
        this.dataWatcher.addObject(12, 0.0F);
        this.dataWatcher.addObject(13, 0.0F);
        this.dataWatcher.addObject(14, 0.0F);
        this.dataWatcher.addObject(15, 0.0F);
    }

	@Override
	public AxisAlignedBB getCollisionBox(Entity par1) {
		return (par1 instanceof EntityMMBoundingBox) ? null : par1.getBoundingBox();
	}

	@Override
	public AxisAlignedBB getBoundingBox() {
		return this.boundingBox;
	}

	@Override
	public boolean canBeCollidedWith() {
		return true;
	}

	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	@Override
	protected void readEntityFromNBT(NBTTagCompound nbt) {
	}

	@Override
	protected void writeEntityToNBT(NBTTagCompound nbt) {
	}

	public void setAABB(AxisAlignedBB par1) {
		this.boundingBox.setBB(par1);
		this.dataWatcher.updateObject(10, (float) (par1.minX - 0.5F));
		this.dataWatcher.updateObject(11, (float) (par1.minY));
		this.dataWatcher.updateObject(12, (float) (par1.minZ - 0.5F));
		this.dataWatcher.updateObject(13, (float) (par1.maxX - 0.5F));
		this.dataWatcher.updateObject(14, (float) (par1.maxY));
		this.dataWatcher.updateObject(15, (float) (par1.maxZ - 0.5F));
	}

	private void setBoundsToAABB(AxisAlignedBB par1) {
		if (this.dataWatcher == null) {
			return;
		}
		float f0 = this.dataWatcher.getWatchableObjectFloat(10);
		if (f0 == -1.0F) {
			return;
		}
		float f1 = this.dataWatcher.getWatchableObjectFloat(11);
		float f2 = this.dataWatcher.getWatchableObjectFloat(12);
		float f3 = this.dataWatcher.getWatchableObjectFloat(13);
		float f4 = this.dataWatcher.getWatchableObjectFloat(14);
		float f5 = this.dataWatcher.getWatchableObjectFloat(15);
		par1.setBounds(f0, f1, f2, f3, f4, f5);
	}

	@Override
	public void onUpdate() {
		if (!this.worldObj.isRemote) {
			if (this.movingMachine == null) {
				this.setDead();
			}
		}
	}

	private void applyCollisionToEntities(double dx, double dy, double dz) {
        this.setBoundsToAABB(this.aabb);
        this.aabb.maxY += 0.41999998688697815D;
        this.aabb.maxY += dy < 0.0D ? -dy : 0.0D;
        this.aabb.offset(this.posX, this.posY, this.posZ);

        List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.aabb);
        for (Object o : list) {
            Entity entity = (Entity) o;
            if (!(entity instanceof EntityMMBoundingBox)) {
                this.moveEntity(entity, dx, dy, dz);
            }
        }
    }

	private void moveEntity(Entity entity, double dx, double dy, double dz) {
		AxisAlignedBB entityBB = entity.boundingBox;//getBBはnull
		AxisAlignedBB myBB = this.boundingBox;
		double y0 = myBB.maxY - entityBB.minY;
		boolean flag = false;
		if (this.inY(entity, dy))//接してるor上部に入り込んでる
		{
			if (this.onY(entity, dy)) {
				if (this.inXAndZ(entity))//XZの範囲内に居る時のみ処理
				{
					double y1 = myBB.maxY - (entityBB.minY + dy);
					if (y1 != 0.0D) {
						dy += y1;
					}
					entity.fallDistance = 0.0F;
					entity.motionY = 0.0D;
					entity.onGround = true;//falseだとXZ移動がぬるっとする
					flag = true;
				}
			} else//上に乗ってないものとする
			{
				if (this.inXOrZ(entity)) {
					dy = 0.0D;
					dx = myBB.calculateXOffset(entityBB, dx);
					dz = myBB.calculateZOffset(entityBB, dz);
					flag = true;
				}
			}

			if (flag) {
				double d0 = entityBB.minY + dy + (double) entity.yOffset - (double) entity.ySize;

				/*if(entity.canBePushed() || (entity instanceof EntityPlayer))
				{
					entity.moveEntity(dx, dy, dz);

				}
				else
				{
					entity.setPosition(entity.posX + dx, d0, entity.posZ + dz);
				}*/

				if (this.worldObj.isRemote) {
					//Playerは鯖側での位置更新は無効化される?
					//C03PacketPlayer,EntityClientPlayerMP,NetHandlerPlayServer.processPlayer(C03)
					//if(entity instanceof EntityPlayer)
					{
						entity.setPosition(entity.posX + dx, d0, entity.posZ + dz);
					}
				} else {
					if (!(entity instanceof EntityPlayer)) {
						entity.setPosition(entity.posX + dx, d0, entity.posZ + dz);
						entity.velocityChanged = true;
					}
				}
			}
		}
	}

	private boolean inY(Entity entity, double moveY) {
		AxisAlignedBB entityBB = entity.boundingBox;
		AxisAlignedBB myBB = this.boundingBox;
		if (moveY > 0.0D) {
			return entityBB.minY <= myBB.maxY && entityBB.maxY > myBB.minY;
		} else {
			return entityBB.minY <= myBB.maxY - moveY && entityBB.maxY > myBB.minY;
		}
	}

	private boolean onY(Entity entity, double moveY) {
		AxisAlignedBB entityBB = entity.boundingBox;
		AxisAlignedBB myBB = this.boundingBox;
		double d0 = 0.21;
		if (moveY > 0.0D) {
			return entityBB.minY >= myBB.maxY - moveY - d0 && entityBB.minY <= myBB.maxY;
		} else {
			return entityBB.minY >= myBB.maxY - d0 && entityBB.minY <= myBB.maxY - moveY;
		}
	}

	private boolean inXAndZ(Entity entity) {
		return entity.posX >= this.aabb.minX && entity.posX < this.aabb.maxX
				&& entity.posZ >= this.aabb.minZ && entity.posZ < this.aabb.maxZ;
	}

	private boolean inXOrZ(Entity entity) {
		return (entity.posX >= this.aabb.minX && entity.posX < this.aabb.maxX)
				|| (entity.posZ >= this.aabb.minZ && entity.posZ < this.aabb.maxZ);
	}

	@Override
	public boolean attackEntityFrom(DamageSource damage, float par2) {
		return false;
	}

	public void moveMM(double x, double y, double z) {
		//this.setPosition(this.posX + x, this.posY + y, this.posZ + z);
		this.applyCollisionToEntities(x, y, z);
		this.prevPosX = this.posX;
		this.prevPosY = this.posY;
		this.prevPosZ = this.posZ;
	}

	@Override
	public void setPosition(double x, double y, double z) {
		this.posX = x;
		this.posY = y;
		this.posZ = z;
		this.setBoundsToAABB(this.boundingBox);
		this.boundingBox.offset(x, y, z);
	}

	@Override
	public void moveEntity(double x, double y, double z) {
	}

	@Override
	public void addVelocity(double par1, double par3, double par5) {
	}

	@Override
	public void setPositionAndRotation(double x, double y, double z, float yaw, float pitch) {
		this.prevPosX = this.posX = x;
		this.prevPosY = this.posY = y;
		this.prevPosZ = this.posZ = z;
		this.moveMM(0.0D, 0.0D, 0.0D);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void setPositionAndRotation2(double x, double y, double z, float yaw, float pitch, int count) {
	}

	@SideOnly(Side.CLIENT)
	public static void handleMMMovement(World world, int[] ids, double moveX, double moveY, double moveZ) {
		/*for(int i = 0; i < ids.length; ++i)
		{
			EntityMMBoundingBox entity = (EntityMMBoundingBox)world.getEntityByID(ids[i]);
			entity.moveMM(moveX, moveY, moveZ);
		}*/

        IntStream.range(0, 2).forEach(pass -> {
            Arrays.stream(ids).mapToObj(id -> (EntityMMBoundingBox) world.getEntityByID(id)).forEach(entity -> {
                if (pass == 0) {
                    entity.setPosition(entity.posX + moveX, entity.posY + moveY, entity.posZ + moveZ);
                } else if (pass == 1) {
                    entity.moveMM(moveX, moveY, moveZ);
                }
            });
        });
    }
}