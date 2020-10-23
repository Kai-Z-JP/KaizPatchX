package jp.ngt.rtm.entity;

import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.item.ItemInstalledObject.IstlObjType;
import net.minecraft.item.ItemStack;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

public class EntityBumpingPost extends EntityInstalledObject {
	public EntityBumpingPost(World par1) {
		super(par1);
		this.setSize(1.5F, 1.5F);
	}

	@Override
	public AxisAlignedBB getBoundingBox() {
		return this.boundingBox;
	}

	@Override
	protected void dropItems() {
		this.entityDropItem(new ItemStack(RTMItem.installedObject, 1, IstlObjType.BUMPING_POST.id), 0.0F);
	}

	@Override
	public String getSubType() {
		return "BumpingPost";
	}

	@Override
	protected String getDefaultName() {
		return "BumpingPost_Type2";
	}

	@Override
	protected ItemStack getItem() {
		return new ItemStack(RTMItem.installedObject, 1, IstlObjType.BUMPING_POST.id);
	}
}