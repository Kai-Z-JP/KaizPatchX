package jp.ngt.rtm.render;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.modelpack.modelset.ModelSetBase;
import net.minecraft.entity.Entity;
import net.minecraft.world.World;

@SideOnly(Side.CLIENT)
public abstract class EntityPartsRenderer<MS extends ModelSetBase> extends PartsRenderer<Entity, MS> {
	public EntityPartsRenderer(String... par1) {
		super(par1);
	}

	public int getTick(Entity entity) {
		return entity == null ? 0 : entity.ticksExisted;
	}

	@Override
	public World getWorld(Entity entity) {
		return entity.worldObj;
	}
}