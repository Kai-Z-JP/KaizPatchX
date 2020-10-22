package jp.ngt.rtm.modelpack;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.modelpack.modelset.ModelSetBase;
import jp.ngt.rtm.modelpack.state.ResourceState;

public interface IModelSelector {
	ResourceState getResourceState();

	String getModelType();

	String getModelName();

	void setModelName(String par1);

	/**
	 * {x,y,z} or {entityId, -1, 0}
	 */
	int[] getPos();

	@SideOnly(Side.CLIENT)
	boolean closeGui(String par1);

	ModelSetBase getModelSet();
}