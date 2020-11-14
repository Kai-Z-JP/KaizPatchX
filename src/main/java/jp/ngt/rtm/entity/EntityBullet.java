package jp.ngt.rtm.entity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.NGTCore;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.item.ItemAmmunition.BulletType;
import net.minecraft.block.Block;
import net.minecraft.block.BlockTNT;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Blocks;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.*;
import net.minecraft.world.World;

import java.util.List;

/**
 * 参考 : 初速 拳銃:340m/s, 徹甲弾:1800m/s ･･･ここでの処理とは関係ない
 */
public class EntityBullet extends EntityArrow {
	private static final int LIFE = 200;

	private BulletType type;
	private int tileX = -1;
	private int tileY = -1;
	private int tileZ = -1;
	private Block landingBlock;
	private int inData;
	private boolean inGround;
	private int ticksInGround;
	private int ticksInAir;

	public EntityBullet(World world) {
		super(world);
		this.renderDistanceWeight = 10.0D;
		this.setBulletSize();
	}

	//EntityArtillery
	public EntityBullet(World world, BulletType type, double x, double y, double z, double mX, double mY, double mZ) {
		this(world);
		this.yOffset = 0.0F;
		this.setBulletType(type);
		this.setPosition(x, y, z);

		this.motionX = mX;
		this.motionY = mY;
		this.motionZ = mZ;
		this.setThrowableHeading(this.motionX, this.motionY, this.motionZ, 10.0F, 0.0F);
	}

	//ItemGun
	public EntityBullet(World world, EntityLivingBase shooter, float speed, BulletType type) {
		this(world);
		this.yOffset = 0.0F;
		this.setBulletType(type);
		this.setLocationAndAngles(shooter.posX, shooter.posY + (double) shooter.getEyeHeight(), shooter.posZ, shooter.rotationYaw, shooter.rotationPitch);
		float yawRad = NGTMath.toRadians(this.rotationYaw);
		float pitchRad = NGTMath.toRadians(this.rotationPitch);
		this.motionX = -MathHelper.sin(yawRad) * MathHelper.cos(pitchRad);
		this.motionZ = MathHelper.cos(yawRad) * MathHelper.cos(pitchRad);
		this.motionY = -MathHelper.sin(pitchRad);
		this.setThrowableHeading(this.motionX, this.motionY, this.motionZ, speed, 1.0F);
		this.shootingEntity = shooter;
	}

	//ItemGun(NPC)
	public EntityBullet(World world, EntityLivingBase shooter, EntityLivingBase target, float speed, BulletType type) {
		this(world);
		this.shootingEntity = shooter;

		this.posY = shooter.posY + (double) shooter.getEyeHeight() - 0.10000000149011612D;
		double d0 = target.posX - shooter.posX;
		double d1 = target.boundingBox.minY + (double) (target.height / 3.0F) - this.posY;
		double d2 = target.posZ - shooter.posZ;
		double distance = MathHelper.sqrt_double(d0 * d0 + d2 * d2);

		if (distance >= 1.0E-7D) {
			float f2 = (float) (NGTMath.toDegrees(Math.atan2(d2, d0))) - 90.0F;
			float f3 = (float) (-(NGTMath.toDegrees(Math.atan2(d1, distance))));
			double d4 = d0 / distance;
			double d5 = d2 / distance;
			this.setLocationAndAngles(shooter.posX + d4, this.posY, shooter.posZ + d5, f2, f3);
			this.yOffset = 0.0F;
			this.setThrowableHeading(d0, d1, d2, speed, 1.0F);
		}
	}

	@Override
	protected void entityInit() {
		this.dataWatcher.addObject(16, (byte) 0);//破壊可能
		this.dataWatcher.addObject(17, (byte) -1);//弾の種類
	}

	private void setBulletSize() {
		if (this.getBulletType() == BulletType.cannon_40cm) {
			this.setSize(0.5F, 0.5F);
		} else {
			this.setSize(0.05F, 0.05F);
		}
	}

	@Override
	public void setThrowableHeading(double par1, double par3, double par5, float par7, float par8) {
		double f2 = Math.sqrt(par1 * par1 + par3 * par3 + par5 * par5);
		double d0 = (double) par7 / f2;
		par1 *= d0;
		par3 *= d0;
		par5 *= d0;
		this.motionX = par1;
		this.motionY = par3;
		this.motionZ = par5;
		float f3 = MathHelper.sqrt_double(par1 * par1 + par5 * par5);
		this.prevRotationYaw = this.rotationYaw = (float) (NGTMath.toDegrees(Math.atan2(par1, par5)));
		this.prevRotationPitch = this.rotationPitch = (float) (NGTMath.toDegrees(Math.atan2(par3, f3)));
		this.ticksInGround = 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void setPositionAndRotation2(double par1, double par3, double par5, float par7, float par8, int par9) {
		this.setPosition(par1, par3, par5);
		this.setRotation(par7, par8);
	}

	@Override
	@SideOnly(Side.CLIENT)
	public void setVelocity(double par1, double par3, double par5) {
		this.motionX = par1;
		this.motionY = par3;
		this.motionZ = par5;

		if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
			float f = MathHelper.sqrt_double(par1 * par1 + par5 * par5);
			this.prevRotationYaw = this.rotationYaw = (float) (NGTMath.toDegrees(Math.atan2(par1, par5)));
			this.prevRotationPitch = this.rotationPitch = (float) (NGTMath.toDegrees(Math.atan2(par3, f)));
			this.setLocationAndAngles(this.posX, this.posY, this.posZ, this.rotationYaw, this.rotationPitch);
			this.ticksInGround = 0;
		}
	}

	@Override
	public void onUpdate() {
		this.onEntityUpdate();

		if (this.prevRotationPitch == 0.0F && this.prevRotationYaw == 0.0F) {
			float f = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
			this.prevRotationYaw = this.rotationYaw = (float) (NGTMath.toDegrees(Math.atan2(this.motionX, this.motionZ)));
			this.prevRotationPitch = this.rotationPitch = (float) (NGTMath.toDegrees(Math.atan2(this.motionY, f)));
		}

		Block block = this.worldObj.getBlock(this.tileX, this.tileY, this.tileZ);
		if (block.getMaterial() != Material.air) {
			block.setBlockBoundsBasedOnState(this.worldObj, this.tileX, this.tileY, this.tileZ);
			AxisAlignedBB aabb = block.getCollisionBoundingBoxFromPool(this.worldObj, this.tileX, this.tileY, this.tileZ);
			if (aabb != null && aabb.isVecInside(Vec3.createVectorHelper(this.posX, this.posY, this.posZ))) {
				this.inGround = true;
			}
		}

		if (this.inGround) {
			int j = this.worldObj.getBlockMetadata(this.tileX, this.tileY, this.tileZ);
			if (block == this.landingBlock && j == this.inData) {
				++this.ticksInGround;
				if (!this.worldObj.isRemote && this.ticksInGround == LIFE) {
					this.setDead();
				}
				this.onLanding(this.tileX, this.tileY, this.tileZ);
			} else {
				this.inGround = false;
				this.ticksInGround = 0;
				this.ticksInAir = 0;
			}
		} else {
			++this.ticksInAir;
			Vec3 vecPos = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
			Vec3 vec3 = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);
			MovingObjectPosition mop = this.worldObj.func_147447_a(vecPos, vec3, false, true, false);
			vec3 = Vec3.createVectorHelper(this.posX + this.motionX, this.posY + this.motionY, this.posZ + this.motionZ);

			if (mop != null)//ブロックをすり抜けないように
			{
				vec3 = Vec3.createVectorHelper(mop.hitVec.xCoord, mop.hitVec.yCoord, mop.hitVec.zCoord);
			}

			Entity hitEntity = null;
			List list = this.worldObj.getEntitiesWithinAABBExcludingEntity(this, this.boundingBox.addCoord(this.motionX, this.motionY, this.motionZ).expand(1.0D, 1.0D, 1.0D));
			if (list.size() > 0) {
                Vec3 vecPos2 = Vec3.createVectorHelper(this.posX, this.posY, this.posZ);
                double d0 = 0.0D;
                for (Object o : list) {
                    Entity entity = (Entity) o;
                    boolean flag = true;
                    if (entity.equals(this.shootingEntity))//撃った人には当たらないように
                    {
                        flag = false;
                    }

                    if (entity.canBeCollidedWith() && flag) {
                        //double dis0 = vecPos2.distanceTo(vec3);
                        //double dis1 = this.getDistanceToEntity(entity);
                        //NGTLog.debug("vec:%S, entity:%S", new Object[]{dis0, dis1});
						AxisAlignedBB aabb = entity.boundingBox.expand(0.5D, 0.5D, 0.5D);
						MovingObjectPosition mop1 = aabb.calculateIntercept(vecPos2, vec3);
						if (mop1 != null) {
							double d1 = vecPos2.distanceTo(mop1.hitVec);
							if (d1 < d0 || d0 == 0.0D) {
								hitEntity = entity;
								d0 = d1;
							}
						}
					}
				}
			}

			if (hitEntity != null) {
				mop = new MovingObjectPosition(hitEntity);
            	/*if(hitEntity instanceof EntityPlayer)
            	{
            		EntityPlayer player = (EntityPlayer)hitEntity;
            		if(!player.capabilities.disableDamage && player != this.shootingEntity)
            		{
            			mop = new MovingObjectPosition(player);
            		}
            	}
            	else
            	{
            		mop = new MovingObjectPosition(hitEntity);
            	}*/
			}

			boolean hitBlock = false;
			if (mop != null) {
				if (mop.entityHit != null) {
					this.onHitEntity(mop.entityHit);
				} else {
					hitBlock = true;
					this.tileX = mop.blockX;
					this.tileY = mop.blockY;
					this.tileZ = mop.blockZ;
					this.landingBlock = block;
					this.inData = this.worldObj.getBlockMetadata(this.tileX, this.tileY, this.tileZ);
                    /*this.motionX = (double)((float)(mop.hitVec.xCoord - this.posX));
                    this.motionY = (double)((float)(mop.hitVec.yCoord - this.posY));
                    this.motionZ = (double)((float)(mop.hitVec.zCoord - this.posZ));*/
                    /*float f2 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionY * this.motionY + this.motionZ * this.motionZ);
                    double d0 = 0.05000000074505806D;
                    this.posX -= this.motionX / (double)f2 * d0;
                    this.posY -= this.motionY / (double)f2 * d0;
                    this.posZ -= this.motionZ / (double)f2 * d0;*/
					//this.playSound("random.bowhit", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
					this.inGround = true;

					if (this.landingBlock.getMaterial() != Material.air) {
						this.landingBlock.onEntityCollidedWithBlock(this.worldObj, this.tileX, this.tileY, this.tileZ, this);
					}

					//this.onLanding(this.tileX, this.tileY, this.tileZ);
				}
			}

			if (hitBlock) {
				this.posX = mop.hitVec.xCoord;
				this.posY = mop.hitVec.yCoord;
				this.posZ = mop.hitVec.zCoord;
			} else {
				this.posX += this.motionX;
				this.posY += this.motionY;
				this.posZ += this.motionZ;
			}

			float f2 = MathHelper.sqrt_double(this.motionX * this.motionX + this.motionZ * this.motionZ);
			this.rotationYaw = (float) (NGTMath.toDegrees(Math.atan2(this.motionX, this.motionZ)));

			for (this.rotationPitch = (float) NGTMath.toDegrees(Math.atan2(this.motionY, f2)); this.rotationPitch - this.prevRotationPitch < -180.0F; this.prevRotationPitch -= 360.0F) {
			}

			while (this.rotationPitch - this.prevRotationPitch >= 180.0F) {
				this.prevRotationPitch += 360.0F;
			}

			while (this.rotationYaw - this.prevRotationYaw < -180.0F) {
				this.prevRotationYaw -= 360.0F;
			}

			while (this.rotationYaw - this.prevRotationYaw >= 180.0F) {
				this.prevRotationYaw += 360.0F;
			}

			this.rotationPitch = this.prevRotationPitch + (this.rotationPitch - this.prevRotationPitch) * 0.2F;
			this.rotationYaw = this.prevRotationYaw + (this.rotationYaw - this.prevRotationYaw) * 0.2F;

			double d3 = 0.999D;
			if (this.isInWater()) {
				for (int l = 0; l < 4; ++l) {
					double d4 = 0.25F;
					this.worldObj.spawnParticle("bubble", this.posX - this.motionX * d4, this.posY - this.motionY * d4, this.posZ - this.motionZ * d4, this.motionX, this.motionY, this.motionZ);
				}
				d3 = 0.9D;
			}

			BulletType type = this.getBulletType();
			if (type == BulletType.cannon_40cm || type == BulletType.cannon_Atomic) {
				this.motionX *= d3;
				this.motionZ *= d3;
				this.motionY -= 0.025D;
			} else {
				this.motionX *= d3;
				this.motionY *= d3;
				this.motionZ *= d3;
				this.motionY -= 0.001D;
			}
			this.setPosition(this.posX, this.posY, this.posZ);

			this.func_145775_I();//ブロック当たり判定
		}
	}

	@Override
	public void writeEntityToNBT(NBTTagCompound nbt) {
		nbt.setShort("xTile", (short) this.tileX);
		nbt.setShort("yTile", (short) this.tileY);
		nbt.setShort("zTile", (short) this.tileZ);
		nbt.setShort("life", (short) this.ticksInGround);
		nbt.setByte("inTile", (byte) Block.getIdFromBlock(this.landingBlock));
		nbt.setByte("inData", (byte) this.inData);
		nbt.setByte("inGround", (byte) (this.inGround ? 1 : 0));
		nbt.setByte("canBreak", (byte) (this.getCanBreakBlock() ? 1 : 0));
		nbt.setByte("bulletType", this.getBulletType().id);
	}

	@Override
	public void readEntityFromNBT(NBTTagCompound nbt) {
		this.tileX = nbt.getShort("xTile");
		this.tileY = nbt.getShort("yTile");
		this.tileZ = nbt.getShort("zTile");
		this.ticksInGround = nbt.getShort("life");
		this.landingBlock = Block.getBlockById(nbt.getByte("inTile") & 255);
		this.inData = nbt.getByte("inData") & 255;
		this.inGround = nbt.getByte("inGround") == 1;
		this.setCanBreakBlock(nbt.getByte("canBreak") == 1);
		this.setBulletType(BulletType.getBulletType(nbt.getByte("bulletType")));
	}

	@Override
	protected boolean canTriggerWalking() {
		return false;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public float getShadowSize() {
		return 0.0F;
	}

	public BulletType getBulletType() {
		if (this.type == null) {
			byte b = this.dataWatcher.getWatchableObjectByte(17);
			if (b < 0) {
				return BulletType.handgun_9mm;
			}
			this.type = BulletType.getBulletType(b);
		}
		return this.type;
	}

	public void setBulletType(BulletType par1) {
		this.dataWatcher.updateObject(17, par1.id);
	}

	@Override
	public boolean canAttackWithItem() {
		return false;
	}

	public void setCanBreakBlock(boolean par1) {
		byte b0 = this.dataWatcher.getWatchableObjectByte(16);

		if (par1) {
			this.dataWatcher.updateObject(16, (byte) (b0 & -2));
		} else {
			this.dataWatcher.updateObject(16, (byte) (b0 | 1));
		}
	}

	public boolean getCanBreakBlock() {
		byte b0 = this.dataWatcher.getWatchableObjectByte(16);
		return (b0 & 1) == 0;
	}

	protected void onHitEntity(Entity entity) {
		//entity.setFire(5);
		float damage = this.getBulletType().damage;
		if (entity.attackEntityFrom(DamageSource.causeThrownDamage(this, this.shootingEntity), damage)) {
			//this.playSound("random.bowhit", 1.0F, 1.2F / (this.rand.nextFloat() * 0.2F + 0.9F));
		}

		if (!this.worldObj.isRemote) {
			this.setDead();
		}
	}

	/**
	 * 着弾時のアクション
	 */
	protected void onLanding(int x, int y, int z) {
		boolean doMobGriefing = this.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing");
		BulletType type = this.getBulletType();
		if (type == BulletType.cannon_40cm) {
			if (!this.worldObj.isRemote) {
				Block block = this.worldObj.getBlock(x, y, z);
				float hardness = block.getBlockHardness(this.worldObj, x, y, z);
				if (hardness > 0.0F && hardness < 500.0F) {
					if (doMobGriefing) {
						this.worldObj.setBlockToAir(x, y, z);
					}
					this.worldObj.newExplosion(this, (double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, 12.0F, false, doMobGriefing);
				}
				this.setDead();
			}
		} else if (type == BulletType.cannon_Atomic) {
			if (!this.worldObj.isRemote) {
				Block block = this.worldObj.getBlock(x, y, z);
				float hardness = block.getBlockHardness(this.worldObj, x, y, z);
				if (hardness > 0.0F && hardness < 500.0F) {
					this.worldObj.setBlock(x, y, z, RTMBlock.effect, RTMCore.ATOMIC_BOM_META, 3);
					this.worldObj.playSoundEffect((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D, "random.explode", 4.0F, (1.0F + (this.worldObj.rand.nextFloat() - this.worldObj.rand.nextFloat()) * 0.2F) * 0.7F);
				}
				this.setDead();
			}
		} else {
			if (this.landingBlock != null && this.getCanBreakBlock()) {
				if (!RTMCore.gunBreakBlock) {
					int meta = this.worldObj.getBlockMetadata(x, y, z);
					this.worldObj.playAuxSFX(2001, x, y, z, Block.getIdFromBlock(this.landingBlock) + (meta << 12));
				} else if (this.isBreakableBlock(this.getBulletType(), this.landingBlock, x, y, z)) {
					int meta = this.worldObj.getBlockMetadata(x, y, z);
					NGTCore.proxy.breakBlock(this.worldObj, x, y, z, meta);
					if (!this.worldObj.isRemote) {
						this.worldObj.setBlockToAir(x, y, z);//world.func_147480_a()
						this.setDead();
					}
				} else if (this.landingBlock.getMaterial() == Material.tnt) {
					if (!this.worldObj.isRemote) {
						((BlockTNT) Blocks.tnt).func_150114_a(this.worldObj, x, y, z, 1, null);
						this.worldObj.setBlockToAir(x, y, z);
						this.setDead();
					}
				} else {
					int meta = this.worldObj.getBlockMetadata(x, y, z);
					this.worldObj.playAuxSFX(2001, x, y, z, Block.getIdFromBlock(this.landingBlock) + (meta << 12));
				}
			}
		}

		this.setCanBreakBlock(false);
	}

	protected boolean isBreakableBlock(BulletType bullet, Block block, int x, int y, int z) {
		boolean doMobGriefing = this.worldObj.getGameRules().getGameRuleBooleanValue("mobGriefing");
		Material mat = block.getMaterial();
		if (bullet == BulletType.handgun_9mm) {
			if (mat == Material.leaves || mat == Material.plants
					|| mat == Material.vine || mat == Material.circuits || mat == Material.carpet
					|| mat == Material.glass || mat == Material.redstoneLight || mat == Material.coral
					|| mat == Material.snow
					|| mat == Material.craftedSnow || mat == Material.cactus || mat == Material.cake) {
				return doMobGriefing;
			}
		} else if (bullet == BulletType.rifle_12_7mm) {
			if (block.getBlockHardness(this.worldObj, x, y, z) < 5.0F && block.getExplosionResistance(this) <= 6.0F) {
				return doMobGriefing;
			}
		} else {
			if (mat == Material.wood || mat == Material.leaves || mat == Material.plants
					|| mat == Material.vine || mat == Material.circuits || mat == Material.carpet
					|| mat == Material.glass || mat == Material.redstoneLight || mat == Material.coral
					|| mat == Material.ice || mat == Material.packedIce || mat == Material.snow
					|| mat == Material.craftedSnow || mat == Material.cactus || mat == Material.cake) {
				return doMobGriefing;
			}
		}
		return false;
	}
}