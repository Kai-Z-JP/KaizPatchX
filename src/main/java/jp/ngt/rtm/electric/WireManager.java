package jp.ngt.rtm.electric;

import jp.ngt.ngtlib.math.ILine;
import jp.ngt.ngtlib.math.StraightLine;
import jp.ngt.ngtlib.math.Vec3;
import jp.ngt.rtm.electric.Connection.ConnectionType;
import jp.ngt.rtm.entity.train.EntityTrainBase;
import jp.ngt.rtm.modelpack.cfg.WireConfig;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class WireManager {
    public static final WireManager INSTANCE = new WireManager();

    private static final int CHUNK_DIV = 64;
    private static final int SPLIT = 512;
    private static final double Y_TANGE = 2.0D;
    private static final double XZ_TANGE = 1.0D;

    private final Map<WireChunk, List<WireEntry>> loadedWires = new HashMap<>();

    private WireManager() {
    }

    /**
     * TEElectricalWiring.readFromNBT()で呼び出し
     */
    public void addWire(TileEntityElectricalWiring tileEntity, Connection connection) {
        this.editWire(tileEntity, connection, true);
    }

    public void removeWire(TileEntityElectricalWiring tileEntity, Connection connection) {
        this.editWire(tileEntity, connection, false);
    }

    private void editWire(TileEntityElectricalWiring tileEntity, Connection connection, boolean add) {
        if (connection.type == ConnectionType.WIRE) {
            TileEntityElectricalWiring te2 = connection.getElectricalWiring(tileEntity.getWorldObj());
            if (tileEntity instanceof TileEntityConnectorBase && te2 instanceof TileEntityConnectorBase) {
                TileEntityConnectorBase con1 = (TileEntityConnectorBase) tileEntity;
                TileEntityConnectorBase con2 = (TileEntityConnectorBase) te2;
                con1.updateWirePos(null);
                con2.updateWirePos(null);
                Vec3 vec1 = con1.wirePos;
                vec1 = vec1.add(tileEntity.xCoord + 0.5D, tileEntity.yCoord + 0.5D, tileEntity.zCoord + 0.5D);
                Vec3 vec2 = con2.wirePos;
                vec2 = vec2.add(te2.xCoord + 0.5D, te2.yCoord + 0.5D, te2.zCoord + 0.5D);
                WireConfig cfg = connection.getModelSet().getConfig();

                Vec3 startVec = vec1.getY() <= vec2.getY() ? vec1 : vec2;
                Vec3 endVec = vec1.getY() > vec2.getY() ? vec1 : vec2;
                int x1 = (int) (Math.min(vec1.getX(), vec2.getX())) / CHUNK_DIV;
                int x2 = (int) (Math.max(vec1.getX(), vec2.getX())) / CHUNK_DIV;
                int z1 = (int) (Math.min(vec1.getZ(), vec2.getZ())) / CHUNK_DIV;
                int z2 = (int) (Math.max(vec1.getZ(), vec2.getZ())) / CHUNK_DIV;
                double minY = startVec.getY() + cfg.yOffset;
                double maxY = endVec.getY() + cfg.yOffset;
                WireEntry entry = new WireEntry(new StraightLine(startVec.getZ(), startVec.getX(), endVec.getZ(), endVec.getX()), minY, maxY);

                for (int i = x1; i <= x2; ++i) {
                    for (int j = z1; j <= z2; ++j) {
                        WireChunk chunk = new WireChunk(i, j);
                        List<WireEntry> list = this.loadedWires.computeIfAbsent(chunk, k -> new ArrayList<>());

                        if (add) {
                            list.add(entry);
                        } else {
                            list.remove(entry);
                        }
                    }
                }
            }
        }
    }

    /**
     * 指定座標に最も近いワイヤの高さを取得
     */
    public double getWireY(double x, double y, double z) {
        List<WireEntry> list = this.loadedWires.get(new WireChunk(x, z));
        if (list != null) {
            for (WireEntry entry : list) {
                if (entry.inRange(x, y, z)) {
                    int index = entry.lineXZ.getNearlestPoint(SPLIT, x, z);
                    return entry.minY + (entry.maxY - entry.minY) * ((double) index / (double) SPLIT) + EntityTrainBase.TRAIN_HEIGHT;
                }
            }
        }
        return y;
    }

    public static class WireEntry {
        public final ILine lineXZ;
        public final double minX, maxX, minY, maxY, minZ, maxZ;

        public WireEntry(ILine par1, double par2, double par3) {
            this.lineXZ = par1;
            this.minY = par2;
            this.maxY = par3;

            double[] d1 = par1.getPoint(SPLIT, 0);
            double[] d2 = par1.getPoint(SPLIT, SPLIT);
            this.minX = Math.min(d1[1], d2[1]);
            this.maxX = Math.max(d1[1], d2[1]);
            this.minZ = Math.min(d1[0], d2[0]);
            this.maxZ = Math.max(d1[0], d2[0]);
        }

        public boolean inRange(double x, double y, double z) {
            return x >= this.minX - XZ_TANGE && x <= this.maxX + XZ_TANGE
                    && y >= this.minY - Y_TANGE && y <= this.maxY + Y_TANGE
                    && z >= this.minZ - XZ_TANGE && z <= this.maxZ + XZ_TANGE;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof WireEntry) {
                WireEntry entry = (WireEntry) obj;
                return this.minY == entry.minY && this.maxY == entry.maxY && this.lineXZ.equals(entry.lineXZ);
            }
            return false;
        }

        @Override
        public int hashCode() {
            return this.lineXZ.hashCode();
        }
    }

    public static final class WireChunk {
        public final int chunkX, chunkZ;

        public WireChunk(double x, double z) {
            this.chunkX = (int) (x / CHUNK_DIV);
            this.chunkZ = (int) (z / CHUNK_DIV);
        }

        public WireChunk(int x, int z) {
            this.chunkX = x;
            this.chunkZ = z;
        }

        @Override
        public boolean equals(Object obj) {
            if (obj instanceof WireChunk) {
                WireChunk chunk = (WireChunk) obj;
                return this.chunkX == chunk.chunkX && this.chunkZ == chunk.chunkZ;
            }
            return false;
        }

        @Override
        public int hashCode() {
            return (this.chunkX & 0xFFF) | ((this.chunkZ & 0xFFF) << 12);
        }
    }
}