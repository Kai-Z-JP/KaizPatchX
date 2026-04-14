package jp.ngt.rtm.electric;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.electric.Connection.ConnectionType;

public class TileEntityConnector extends TileEntityConnectorBase {
    private static final int METADATA = 6;

    private int prevOutputSignal = -1;

    @Override
    protected void applyToDirectConnection(Connection c, int level) {
        if (this.getBlockMetadata() < METADATA) { // 入力モードのみデバイスへ転送
            IProvideElectricity provider = c.getIProvideElectricity(this.worldObj);
            if (provider != null) {
                provider.setElectricity(this.xCoord, this.yCoord, this.zCoord, level);
            }
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
        if (connection == null) return;

        IProvideElectricity provider = connection.getIProvideElectricity(this.worldObj);
        if (provider != null) {
            int level = provider.getElectricity();
            if (level != this.prevOutputSignal) {
                ElectricalWiringManager.get(this.worldObj).propagateSignal(this, level);
                this.prevOutputSignal = level;
            }
        }
    }

    /**
     * 接続タイプ3(TileEntity直付)のを返す
     */
    private Connection getBlockConnection() {
        return this.connections.stream().filter(connection -> connection.type == ConnectionType.DIRECT).findFirst().orElse(null);
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return NGTUtil.getChunkLoadDistanceSq();
    }

//	@Override
//	@SideOnly(Side.CLIENT)
//	public AxisAlignedBB getRenderBoundingBox() {
//		AxisAlignedBB bb = AxisAlignedBB.getBoundingBox(this.xCoord - 32, this.yCoord - 16, this.zCoord - 32, this.xCoord + 32, this.yCoord + 16, this.zCoord + 32);
//		return bb;
//	}

    @Override
    public String getSubType() {
        return (this.getBlockMetadata() < METADATA) ? "Input" : "Output";
    }

    @Override
    protected String getDefaultName() {
        return (this.getBlockMetadata() < METADATA) ? "Input01" : "Output01";
    }
}