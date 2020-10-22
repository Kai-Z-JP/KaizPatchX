package jp.ngt.rtm.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.block.BlockLiquidBase;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.RTMMaterial;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.renderer.texture.IIconRegister;
import net.minecraft.entity.Entity;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.DamageSource;
import net.minecraft.util.IIcon;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;

import java.util.List;
import java.util.Random;

public class BlockFurnaceFire extends BlockLiquidBase {
	private static final Item itemIronOre = Item.getItemFromBlock(Blocks.iron_ore);

	public BlockFurnaceFire(boolean light) {
		super(RTMMaterial.melted);
		this.setLightLevel(light ? 1.0F : 0.0F);
	}

	@Override
	public int getRenderId() {
		return RTMBlock.renderIdLiquid;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public int getRenderBlockPass() {
		return 1;
	}

	@Override
	public boolean getBlocksMovement(IBlockAccess world, int x, int y, int z) {
		return false;
	}

	@Override
	public void updateTick(World world, int x, int y, int z, Random random) {
		super.updateTick(world, x, y, z, random);

		if (!world.isRemote) {
			if (this == RTMBlock.furnaceFire) {
				if (world.getBlockMetadata(x, y, z) == 0) {
					Block block = world.getBlock(x, y - 1, z);
					if (block != RTMBlock.fireBrick && block != RTMBlock.hotStoveBrick && block.isNormalCube(world, x, y, z)) {
						world.setBlock(x, y, z, Blocks.fire, 0, 2);
					}
				}
			} else if (this == RTMBlock.exhaustGas) {
				int m0 = world.getBlockMetadata(x, y, z);
				if (world.getBlock(x, y + 1, z) == RTMBlock.furnaceFire) {
					int m1 = world.getBlockMetadata(x, y + 1, z);
					world.setBlock(x, y, z, RTMBlock.furnaceFire, m1, 2);
					world.setBlock(x, y + 1, z, RTMBlock.exhaustGas, m0, 2);
					world.scheduleBlockUpdate(x, y + 1, z, this, this.tickRate(world));
				} else if (world.getBlock(x, y + 1, z) == Blocks.air) {
					if (random.nextInt(10) == 0) {
						if (m0 > 0) {
							world.setBlock(x, y, z, RTMBlock.exhaustGas, m0 - 1, 2);
						} else {
							world.setBlock(x, y, z, Blocks.air, 0, 2);
						}
					}
				}
			}
		}
	}

	@Override
	public void onEntityCollidedWithBlock(World world, int x, int y, int z, Entity entity) {
		boolean flag1 = true;//焼却

		if (entity instanceof EntityItem) {
			ItemStack itemstack0 = ((EntityItem) entity).getEntityItem();
			int sizeCoke = 0;
			int sizeIron = 0;
			boolean isIron = false;

			if (itemstack0.getItem() == RTMItem.coke) {
				sizeCoke = itemstack0.stackSize / 2;
				entity.setInWeb();
				flag1 = false;
			} else if (itemstack0.getItem() == Items.coal && itemstack0.getItemDamage() == 1) {
				sizeCoke = itemstack0.stackSize / 8;
				entity.setInWeb();
				flag1 = false;
			} else if (itemstack0.getItem() == itemIronOre) {
				sizeIron = itemstack0.stackSize;
				isIron = true;
				entity.setInWeb();
				flag1 = false;
			}

			if (sizeCoke > 0 || sizeIron > 0) {
				if (!world.isRemote) {
					List list = world.getEntitiesWithinAABB(EntityItem.class, AxisAlignedBB.getBoundingBox((double) x, (double) y, (double) z, (double) x + 1.0D, (double) y + 1.0D, (double) z + 1.0D));
					if (list != null && !list.isEmpty()) {
						int i2 = 0;
						for (int i = 0; i < list.size(); ++i) {
							EntityItem entityItem = (EntityItem) list.get(i);
							ItemStack itemstack1 = entityItem.getEntityItem();
							if (itemstack1.getItem() == RTMItem.coke) {
								i2 = itemstack1.stackSize / 2;
							} else if (itemstack1.getItem() == Items.coal && itemstack1.getItemDamage() == 1) {
								i2 = itemstack1.stackSize / 8;
							} else {
								i2 = 0;
							}

							if (i2 > 0) {
								if (sizeCoke + i2 > sizeIron) {
									itemstack1.stackSize = sizeCoke + i2 - sizeIron;
									entityItem.setEntityItemStack(itemstack1);
									sizeCoke = sizeIron;
								} else {
									sizeCoke += i2;
									entityItem.setDead();
								}
							}
						}
					}

					if (isIron) {
						if (sizeCoke == 0) {
							return;
						} else if (sizeCoke < sizeIron) {
							itemstack0.stackSize -= sizeCoke;
							((EntityItem) entity).setEntityItemStack(itemstack0);
							sizeIron = sizeCoke;
						} else {
							entity.setDead();
						}
					} else {
						if (sizeIron == 0) {
							return;
						} else if (sizeCoke > sizeIron) {
							itemstack0.stackSize -= sizeIron;
							((EntityItem) entity).setEntityItemStack(itemstack0);
							sizeCoke = sizeIron;
						} else {
							entity.setDead();
						}
					}

					this.onCollidedIronOre(world, x, y, z, sizeIron);
				}
			}

			if (flag1) {
				entity.motionY = 0.20000000298023224D;
				entity.motionX = (double) ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F);
				entity.motionZ = (double) ((world.rand.nextFloat() - world.rand.nextFloat()) * 0.2F);
				entity.playSound("random.fizz", 0.4F, 2.0F + world.rand.nextFloat() * 0.4F);
			}
		}

		if (flag1) {
			entity.attackEntityFrom(DamageSource.lava, 1.0F);
			entity.setFire(5);
		}
	}

	/**
	 * 材料が投入された時
	 */
	protected void onCollidedIronOre(World world, int x, int y, int z, int amount) {
		if (world.getBlock(x, y - 1, z) instanceof BlockFurnaceFire) {
			if (world.getBlock(x, y, z) == RTMBlock.furnaceFire && world.rand.nextInt(10) == 0) {
				int meta = world.getBlockMetadata(x, y, z);
				amount += world.rand.nextInt(meta + 1);
				if (world.rand.nextInt(5) == 0) {
					world.setBlock(x, y, z, RTMBlock.exhaustGas, 15, 2);
				}
			}
			this.onCollidedIronOre(world, x, y - 1, z, amount);//下のガスブロックへ送る
		} else {
			while (amount > 0) {
				if (!(world.getBlock(x, y, z) instanceof BlockFurnaceFire)) {
					break;
				}
				world.setBlock(x, y, z, Blocks.air);
				amount = BlockLiquidBase.addLiquid(world, x, y, z, RTMBlock.liquefiedPigIron, amount, true);
				++y;
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public void randomDisplayTick(World world, int x, int y, int z, Random random) {
		if (this == RTMBlock.furnaceFire) {
			double d5;
			double d6;
			double d7;

			if (world.getBlock(x, y + 1, z).getMaterial() == Material.air && !world.getBlock(x, y + 1, z).isOpaqueCube()) {
				d5 = (double) ((float) x + random.nextFloat());
				d6 = (double) y + this.maxY;
				d7 = (double) ((float) z + random.nextFloat());
				world.spawnParticle("explode", d5, d6, d7, 0.0D, 0.0D, 0.0D);

	            /*if(random.nextInt(100) == 0)
	            {
	                d5 = (double)((float)x + random.nextFloat());
	                d6 = (double)y + this.maxY;
	                d7 = (double)((float)z + random.nextFloat());
	                world.spawnParticle("lava", d5, d6, d7, 0.0D, 0.0D, 0.0D);
	                world.playSound(d5, d6, d7, "liquid.lavapop", 0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);
	            }*/

				if (random.nextInt(200) == 0) {
					world.playSound((double) x, (double) y, (double) z, "liquid.lava", 0.2F + random.nextFloat() * 0.2F, 0.9F + random.nextFloat() * 0.15F, false);
				}
			}

			if (random.nextInt(10) == 0 && World.doesBlockHaveSolidTopSurface(world, x, y - 1, z) && !world.getBlock(x, y - 2, z).getMaterial().blocksMovement()) {
				d5 = (double) ((float) x + random.nextFloat());
				d6 = (double) y - 1.05D;
				d7 = (double) ((float) z + random.nextFloat());
				world.spawnParticle("dripLava", d5, d6, d7, 0.0D, 0.0D, 0.0D);
			}
		}
	}

	@Override
	protected void meltBlock(World world, int x, int y, int z) {
		if (this == RTMBlock.furnaceFire) {
			super.meltBlock(world, x, y, z);
		}
	}

	@SideOnly(Side.CLIENT)
	public IIcon getIcon(int p_149691_1_, int p_149691_2_) {
		return this.blockIcon;
	}

	@SideOnly(Side.CLIENT)
	public void registerBlockIcons(IIconRegister p_149651_1_) {
		if (this == RTMBlock.exhaustGas) {
			this.blockIcon = p_149651_1_.registerIcon("rtm:exhaustGas");
		} else {
			this.blockIcon = p_149651_1_.registerIcon("rtm:furnaceFire");
		}
	}
}