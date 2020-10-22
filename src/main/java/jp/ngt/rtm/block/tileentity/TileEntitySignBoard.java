package jp.ngt.rtm.block.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.modelpack.texture.ITextureHolder;
import jp.ngt.rtm.modelpack.texture.SignBoardProperty;
import jp.ngt.rtm.modelpack.texture.TextureManager;
import jp.ngt.rtm.modelpack.texture.TextureManager.TexturePropertyType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

public class TileEntitySignBoard extends TileEntity implements ITextureHolder<SignBoardProperty> {
	private SignBoardProperty property;
	private String textureName = "";

	public boolean isGettingPower;
	private byte direction;

	@SideOnly(Side.CLIENT)
	public int counter;

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.direction = nbt.getByte("dir");
		String s = nbt.getString("name");
		this.setTexture(SignBoardProperty.fixName(s));
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		nbt.setByte("dir", this.direction);
		nbt.setString("name", this.textureName);
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		SignBoardProperty prop = this.getProperty();

		if (this.worldObj.isRemote) {
			++this.counter;
			if (this.counter >= prop.frame * prop.animationCycle) {
				this.counter = 0;
			}
		}

		//明るさ更新
		boolean b = this.worldObj.isBlockIndirectlyGettingPowered(this.xCoord, this.yCoord, this.zCoord);
		if (this.isGettingPower ^ b) {
			this.isGettingPower = b;
			this.worldObj.func_147451_t(this.xCoord, this.yCoord, this.zCoord);
		} else if (prop.lightValue == -16)//点滅
		{
			this.worldObj.func_147451_t(this.xCoord, this.yCoord, this.zCoord);
		}
	}

	@Override
	public SignBoardProperty getProperty() {
		if (this.property == null || this.property == SignBoardProperty.DUMMY) {
			this.property = TextureManager.INSTANCE.getProperty(this.getType(), this.textureName);
			if (this.property == null) {
				this.property = SignBoardProperty.DUMMY;
			}
		}
		return this.property;
	}

	@Override
	public void setTexture(String name) {
		this.textureName = name;
		this.property = null;

		if (this.worldObj == null || !this.worldObj.isRemote) {
			this.markDirty();
			this.sendPacket();
			if (this.worldObj != null) {
				this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);//周りの明るさも変更
			}
		} else {
			this.counter = 0;
		}
	}

	@Override
	public TexturePropertyType getType() {
		return TexturePropertyType.SignBoard;
	}

	public byte getDirection() {
		return this.direction;
	}

	public void setDirection(byte par1)//Server only
	{
		this.direction = par1;
		this.sendPacket();
	}

	private void sendPacket() {
		NGTUtil.sendPacketToClient(this);
	}

	@Override
	public Packet getDescriptionPacket() {
		this.sendPacket();
		return null;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		return NGTUtil.getChunkLoadDistanceSq();
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		double height = this.property.height / 2.0F;
		double width = this.property.width / 2.0F;
		double depth = this.property.depth / 2.0F;
		double d0 = width >= depth ? width : depth;
		AxisAlignedBB bb = AxisAlignedBB.getBoundingBox((double) this.xCoord - d0, (double) this.yCoord - height, (double) this.zCoord - d0, (double) this.xCoord + d0 + 1.0D, (double) this.yCoord + height + 1.0D, (double) this.zCoord + d0 + 1.0D);
		return bb;
	}
}