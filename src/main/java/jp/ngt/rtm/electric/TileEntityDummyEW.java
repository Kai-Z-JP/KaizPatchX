package jp.ngt.rtm.electric;


import jp.ngt.ngtlib.math.Vec3;

public class TileEntityDummyEW extends TileEntityElectricalWiring {
    public final EntityElectricalWiring entityEW;
    private int prevSignal;
    private long lastTopologyRevision = -1L;

    public TileEntityDummyEW(EntityElectricalWiring par1Entity) {
        super();
        this.entityEW = par1Entity;
    }

    @Override
    protected void onReceiveSignal(int level) {
        this.entityEW.setElectricity(level);
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (!this.worldObj.isRemote) {
            int level = this.entityEW.getElectricity();
            ElectricalWiringManager manager = ElectricalWiringManager.get(this.worldObj);
            long topologyRevision = manager.getTopologyRevision();
            if (level >= 0 && (level != this.prevSignal || topologyRevision != this.lastTopologyRevision)) {
                manager.propagateSignal(this, level);
                this.prevSignal = level;
                this.lastTopologyRevision = manager.getTopologyRevision();
            }
        }
    }

    @Override
    public Vec3 getWirePos() {
        return Vec3.ZERO;
    }

    @Override
    public boolean isBlockTile() {
        return false;
    }
}
