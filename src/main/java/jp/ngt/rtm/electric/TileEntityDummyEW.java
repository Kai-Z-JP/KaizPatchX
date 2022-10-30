package jp.ngt.rtm.electric;


import jp.ngt.ngtlib.math.Vec3;

public class TileEntityDummyEW extends TileEntityElectricalWiring {
    public final EntityElectricalWiring entityEW;
    private int prevSignal;

    public TileEntityDummyEW(EntityElectricalWiring par1Entity) {
        super();
        this.entityEW = par1Entity;
    }

    @Override
    public void onGetElectricity(int x, int y, int z, int level, int counter) {
        super.onGetElectricity(x, y, z, level, counter);

        if (!(x == this.xCoord && y == this.yCoord && z == this.zCoord)) {
            this.entityEW.setElectricity(level);
        }
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (!this.worldObj.isRemote) {
            int level = this.entityEW.getElectricity();
            if (level >= 0 && level != this.prevSignal) {
                this.onGetElectricity(this.xCoord, this.yCoord, this.zCoord, level, 0);
                this.prevSignal = level;
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