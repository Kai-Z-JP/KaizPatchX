package jp.ngt.mcte.block;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.mcte.item.ItemMiniature;
import jp.ngt.mcte.item.ItemMiniature.MiniatureMode;
import jp.ngt.mcte.world.MCTEWorld;
import jp.ngt.ngtlib.block.NGTObject;
import jp.ngt.ngtlib.block.TileEntityPlaceable;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.ngtlib.renderer.DisplayList;
import jp.ngt.ngtlib.renderer.GLHelper;
import jp.ngt.ngtlib.util.NGTUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

import java.util.List;

public class TileEntityMiniature extends TileEntityPlaceable {
	public NGTObject blocksObject;
	public float scale;
	public float offsetX, offsetY, offsetZ;
	public MiniatureMode mode;
	public byte attachSide;
	private MiniatureBlockState state;
	public final RSPort port = new RSPort();

	private AxisAlignedBB selectBox;
	private List<AxisAlignedBB> collisionBoxes;

	private MCTEWorld dummyWorld;
	@SideOnly(Side.CLIENT)
	public DisplayList[] glLists;

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
		super.readFromNBT(nbt);
		this.blocksObject = ItemMiniature.getNGTObject(nbt);
		this.scale = ItemMiniature.getScale(nbt);
		this.offsetX = nbt.getFloat("OffsetX");
		this.offsetY = nbt.getFloat("OffsetY");
		this.offsetZ = nbt.getFloat("OffsetZ");
		this.mode = MiniatureMode.values()[nbt.getByte("Mode")];
		if (nbt.hasKey("AttachSide")) {
			this.attachSide = nbt.getByte("AttachSide");
		} else {
			this.attachSide = 1;
		}

		if (nbt.hasKey("MBState")) {
			this.state = MiniatureBlockState.readFromNBT(nbt.getCompoundTag("MBState"));
		} else {
			this.state = new MiniatureBlockState();
			this.state.lightValue = nbt.getByte("LightValue");
		}

		if (this.worldObj != null && this.worldObj.isRemote) {
			//明るさの更新
			this.worldObj.func_147451_t(this.xCoord, this.yCoord, this.zCoord);
		}

		this.dummyWorld = null;
		this.updateAABB();
	}

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
		super.writeToNBT(nbt);
		ItemMiniature.setScale(this.scale, nbt);
		if (this.blocksObject != null) {
			ItemMiniature.setNGTObject(this.blocksObject, nbt);
		}
		nbt.setFloat("OffsetX", this.offsetX);
		nbt.setFloat("OffsetY", this.offsetY);
		nbt.setFloat("OffsetZ", this.offsetZ);
		nbt.setByte("Mode", (byte) this.mode.id);
		nbt.setByte("AttachSide", this.attachSide);
		nbt.setTag("MBState", this.state.writeToNBT());
	}

	@Override
	public void updateEntity() {
		if (!this.worldObj.isRemote) {
			this.getDummyWorld().tick();
		}
	}

	public MCTEWorld getDummyWorld() {
		if (this.dummyWorld == null && this.blocksObject != null) {
			this.dummyWorld = new MCTEWorld(this.getWorldObj(), this.blocksObject, this.xCoord, this.yCoord, this.zCoord);
		}
		return this.dummyWorld;
	}

	public void setBlockState(NGTObject par1, float par2, float x, float y, float z, MiniatureMode par6) {
		this.blocksObject = par1;
		this.scale = par2;
		this.offsetX = x;
		this.offsetY = y;
		this.offsetZ = z;
		this.mode = par6;
		this.getDescriptionPacket();
		this.markDirty();
	}

	public MiniatureBlockState getMBState() {
		return this.state != null ? this.state : new MiniatureBlockState();
	}

	public void setMBState(MiniatureBlockState par1) {
		this.state = par1;
	}

	/*public int getLightValue()
	{
		return this.lightValue;
		//return (this.blocksObject != null) ? this.blocksObject.getLightValue() : 0;
	}*/

	public AxisAlignedBB getSelectBox(int x, int y, int z) {
		if (this.selectBox == null) {
			if (this.state == null) {
				AxisAlignedBB aabb = (new MiniatureBlockState()).getSelectBox();
				aabb.offset((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D);
				return aabb;
			}

			AxisAlignedBB aabb = this.state.getSelectBox();
			this.rotateAABB(aabb);
			aabb.offset((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D);
			this.selectBox = aabb;
		}
		return this.selectBox;
	}

	public List<AxisAlignedBB> getCollisionBoxes(int x, int y, int z) {
		if (this.collisionBoxes == null) {
			if (this.state == null) {
				List<AxisAlignedBB> list = (new MiniatureBlockState()).getCollisionBoxes();
				for (AxisAlignedBB aabb : list) {
					aabb.offset((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D);
				}
				return list;
			}

			List<AxisAlignedBB> list = this.state.getCollisionBoxes();
			for (AxisAlignedBB aabb : list) {
				this.rotateAABB(aabb);
				aabb.offset((double) x + 0.5D, (double) y + 0.5D, (double) z + 0.5D);
			}
			this.collisionBoxes = list;
		}
		return this.collisionBoxes;
	}

	private void rotateAABB(AxisAlignedBB aabb) {
		Vec3 vecMin = Vec3.createVectorHelper(aabb.minX, aabb.minY, aabb.minZ);
		Vec3 vecMax = Vec3.createVectorHelper(aabb.maxX, aabb.maxY, aabb.maxZ);
		//東西南北方向のみに固定
		float f0 = this.getRotation();
		switch (this.attachSide) {
			case 0:
				break;
			case 1:
				break;
			case 2:
				f0 *= -1.0F;
				break;
			case 3:
				f0 *= -1.0F;
				break;
			case 4:
				f0 *= -1.0F;
				break;
			case 5:
				f0 *= -1.0F;
				break;
		}
		f0 = (float) MathHelper.floor_float((f0 + 45.0F) / 90.0F) * 90.0F;
		float yaw = NGTMath.toRadians(f0);
		vecMin.rotateAroundY(yaw);
		vecMax.rotateAroundY(yaw);
		float ro;
		switch (this.attachSide) {
			case 0://yNeg
				ro = NGTMath.toRadians(180.0F);
				vecMin.rotateAroundZ(ro);
				vecMax.rotateAroundZ(ro);
				break;
			case 1://yPos
				break;
			case 2://z
				ro = NGTMath.toRadians(-90.0F);
				vecMin.rotateAroundX(ro);
				vecMax.rotateAroundX(ro);
				break;
			case 3://z
				ro = NGTMath.toRadians(90.0F);
				vecMin.rotateAroundX(ro);
				vecMax.rotateAroundX(ro);
				break;
			case 4://x
				ro = NGTMath.toRadians(90.0F);
				vecMin.rotateAroundZ(ro);
				vecMax.rotateAroundZ(ro);
				break;
			case 5://x
				ro = NGTMath.toRadians(-90.0F);
				vecMin.rotateAroundZ(ro);
				vecMax.rotateAroundZ(ro);
				break;
		}
		double minX = vecMin.xCoord < vecMax.xCoord ? vecMin.xCoord : vecMax.xCoord;
		double minY = vecMin.yCoord < vecMax.yCoord ? vecMin.yCoord : vecMax.yCoord;
		double minZ = vecMin.zCoord < vecMax.zCoord ? vecMin.zCoord : vecMax.zCoord;
		double maxX = vecMin.xCoord > vecMax.xCoord ? vecMin.xCoord : vecMax.xCoord;
		double maxY = vecMin.yCoord > vecMax.yCoord ? vecMin.yCoord : vecMax.yCoord;
		double maxZ = vecMin.zCoord > vecMax.zCoord ? vecMin.zCoord : vecMax.zCoord;
		aabb.setBounds(minX, minY, minZ, maxX, maxY, maxZ);
	}

	@Override
	public void setRotation(float par1, boolean synch) {
		super.setRotation(par1, synch);
		this.updateAABB();
	}

	/**
	 * 当たり判定再生成
	 */
	private void updateAABB() {
		this.selectBox = null;
		this.collisionBoxes = null;
	}

	@Override
	public Packet getDescriptionPacket() {
		NGTUtil.sendPacketToClient(this);
		return null;
	}

	@Override
	public void onChunkUnload() {
		if (this.worldObj.isRemote) {
			this.deleteGLList();
		}
	}

	@Override
	public void invalidate() {
		if (this.worldObj.isRemote) {
			this.deleteGLList();
		}
	}

	@SideOnly(Side.CLIENT)
	private void deleteGLList() {
		if (this.glLists != null) {
			GLHelper.deleteGLList(this.glLists[0]);
			GLHelper.deleteGLList(this.glLists[1]);
		}
	}

	@Override
	public boolean shouldRenderInPass(int pass) {
		return pass >= 0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public double getMaxRenderDistanceSquared() {
		//return 16384.0D;//128^2
		double d0 = NGTUtil.getChunkLoadDistanceSq() * 0.5D;
		if (this.blocksObject != null) {
			if (d0 < (double) this.blocksObject.xSize) {
				d0 = (double) this.blocksObject.xSize;
			}

			if (d0 < (double) this.blocksObject.ySize) {
				d0 = (double) this.blocksObject.ySize;
			}

			if (d0 < (double) this.blocksObject.zSize) {
				d0 = (double) this.blocksObject.zSize;
			}
		}
		return d0;
	}

	@Override
	@SideOnly(Side.CLIENT)
	public AxisAlignedBB getRenderBoundingBox() {
		if (this.blocksObject == null) {
			return super.getRenderBoundingBox();
		}

		//回転してる時はoffsetを単純に計算出来ない
		if (this.offsetX != 0.0F || this.offsetY != 0.0F || this.offsetZ != 0.0F) {
			return INFINITE_EXTENT_AABB;
		}

		double sc = (double) this.scale;
		double x0 = (double) this.blocksObject.xSize * sc * 0.5D;
		double y0 = (double) this.blocksObject.ySize * sc;
		double z0 = (double) this.blocksObject.zSize * sc * 0.5D;
		/*double px = (double)this.xCoord + 0.5D + (double)this.offsetX * sc;
		double py = (double)this.yCoord + 0.5D + (double)this.offsetY * sc;
		double pz = (double)this.zCoord + 0.5D + (double)this.offsetZ * sc;*/
		double px = (double) this.xCoord + 0.5D;
		double py = (double) this.yCoord;
		double pz = (double) this.zCoord + 0.5D;
		return AxisAlignedBB.getBoundingBox(px - x0, py, pz - z0, px + x0, py + y0, pz + z0);
	}
}