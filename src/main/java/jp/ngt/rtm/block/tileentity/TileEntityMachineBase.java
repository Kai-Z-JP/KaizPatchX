package jp.ngt.rtm.block.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.block.TileEntityPlaceable;
import jp.ngt.ngtlib.network.PacketNBT;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.electric.MachineType;
import jp.ngt.rtm.modelpack.IModelSelectorWithType;
import jp.ngt.rtm.modelpack.ModelPackManager;
import jp.ngt.rtm.modelpack.ScriptExecuter;
import jp.ngt.rtm.modelpack.modelset.ModelSetMachine;
import jp.ngt.rtm.modelpack.state.ResourceState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.Vec3;

public abstract class TileEntityMachineBase extends TileEntityPlaceable implements IModelSelectorWithType {
	private final ResourceState state = new ResourceState(this);
	private ModelSetMachine myModelSet;
	private String modelName = "";
	private float pitch;
	private final ScriptExecuter executer = new ScriptExecuter();

	public int tick;
	public boolean isGettingPower;
	protected Vec3 normal;

	/**
	 * メタで保存してた方向データを更新したか
	 */
	private boolean yawFixed;

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		String s = nbt.getString("ModelName");
		if (s == null || s.length() == 0) {
			s = this.getDefaultName();
		}
		this.setModelName(s);
		this.pitch = nbt.getFloat("Pitch");

		this.yawFixed = nbt.hasKey("Yaw");
		this.getResourceState().readFromNBT(nbt.getCompoundTag("State"));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setString("ModelName", this.modelName);
		nbt.setFloat("Pitch", this.pitch);
		nbt.setTag("State", this.getResourceState().writeToNBT());
	}

	@Override
	public void updateEntity() {
		++this.tick;
		if (this.tick == Integer.MAX_VALUE) {
			this.tick = 0;
		}

		if (!this.getWorldObj().isRemote) {
			this.executer.execScript(this);
		}
	}


	@Override
	public void setRotation(EntityPlayer player, float rotationInterval, boolean synch) {
		super.setRotation(player, rotationInterval, synch);
		this.pitch = -player.rotationPitch;
	}

	@Override
	public void setRotation(float par1, boolean synch) {
		super.setRotation(par1, synch);
		this.yawFixed = true;
	}

	public float getPitch() {
		return this.pitch;
	}

	public Vec3 getNormal(float x, float y, float z, float pitch, float yaw) {
		if (this.normal == null) {
			this.normal = Vec3.createVectorHelper(x, y, z);
		}
		return this.normal;
	}

	/**
	 * 右クリック時
	 */
	public void onActivate() {
		if (this.worldObj.isRemote && this.getModelSet().sound_OnActivate != null) {
			RTMCore.proxy.playSound(this, this.getModelSet().sound_OnActivate, 1.0F, 1.0F);
		}
	}

	public abstract MachineType getMachinleType();

	protected void sendPacket() {
		NGTUtil.sendPacketToClient(this);
	}

	@Override
	public Packet getDescriptionPacket() {
		this.sendPacket();
		return null;
	}

	@Override
	public boolean shouldRenderInPass(int pass) {
		return pass >= 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return NGTUtil.getChunkLoadDistanceSq();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(this.xCoord, this.yCoord, this.zCoord, this.xCoord + 1, this.yCoord + 1, this.zCoord + 1);
		return bb;
	}

	public ModelSetMachine getModelSet() {
		if (this.myModelSet == null || this.myModelSet.isDummy()) {
			this.myModelSet = ModelPackManager.INSTANCE.getModelSet("ModelMachine", this.modelName);
			if (this.worldObj == null || !this.worldObj.isRemote) {
				PacketNBT.sendToClient(this);
			}
		}
		return this.myModelSet;
	}

	@Override
	public String getModelType() {
		return "ModelMachine";
	}

	@Override
	public String getModelName() {
		return this.modelName;
	}

	@Override
	public void setModelName(String par1) {
		this.modelName = par1;
		this.myModelSet = null;
		if (this.worldObj == null || !this.worldObj.isRemote) {
			this.markDirty();
			this.sendPacket();
		}
	}

	@Override
	public int[] getPos() {
		return new int[]{this.xCoord, this.yCoord, this.zCoord};
	}

	@Override
	public boolean closeGui(String par1, ResourceState par2) {
		return true;
	}

	@Override
	public String getSubType() {
		return this.getMachinleType().toString();
	}

	@Override
	public ResourceState getResourceState() {
		return this.state;
	}

	/**
	 * NBTにモデル名が含まれない場合に使用
	 */
	protected abstract String getDefaultName();
}