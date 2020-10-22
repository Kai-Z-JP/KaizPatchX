package jp.ngt.rtm.electric;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.electric.Connection.ConnectionType;
import net.minecraft.util.AxisAlignedBB;

public class TileEntityConnector extends TileEntityConnectorBase {
	private static final int METADATA = 6;

	private int prevOutputSignal = -1;

	@Override
	public void onGetElectricity(int x, int y, int z, int level, int counter) {
		if (this.getBlockMetadata() < METADATA)//in
		{
			super.onGetElectricity(x, y, z, level, counter);
		}
	}

	@Override
	protected void sendElectricity(Connection connection, int level, int counter) {
		if (this.getBlockMetadata() < METADATA && connection.type == ConnectionType.DIRECT)//in
		{
			IProvideElectricity provider = connection.getIProvideElectricity(this.worldObj);
			if (provider != null) {
				provider.setElectricity(this.xCoord, this.yCoord, this.zCoord, level);
			}
		} else {
			super.sendElectricity(connection, level, counter);
		}
	}

	@Override
	public void updateEntity() {
		super.updateEntity();

		if (!this.worldObj.isRemote) {
			if (this.getBlockMetadata() >= METADATA)//out
			{
				this.checkSignalOutput();
			}
		}
	}

	private void checkSignalOutput() {
		Connection connection = this.getBlockConnection();
		if (connection == null) {
			return;
		}

		IProvideElectricity provider = connection.getIProvideElectricity(this.worldObj);
		if (provider != null) {
			int level = provider.getElectricity();
			if (level != this.prevOutputSignal) {
				//this.onGetElectricity(connection.x, connection.y, connection.z, level, 0);
				this.sendElectricityToAll(level);
				this.prevOutputSignal = level;
			}
		}
	}

	/**
	 * 接続タイプ3(TileEntity直付)のを返す
	 */
	private Connection getBlockConnection() {
		for (Connection connection : this.connections) {
			if (connection.type == ConnectionType.DIRECT) {
				return connection;
			}
		}
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
		AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(this.xCoord - 32, this.yCoord - 16, this.zCoord - 32, this.xCoord + 32, this.yCoord + 16, this.zCoord + 32);
		return bb;
	}

	@Override
	public String getSubType() {
		return (this.getBlockMetadata() < METADATA) ? "Input" : "Output";
	}

	@Override
	protected String getDefaultName() {
		return (this.getBlockMetadata() < METADATA) ? "Input01" : "Output01";
	}
}