package jp.ngt.rtm.electric;

import jp.ngt.rtm.electric.Connection.ConnectionType;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.world.World;

import java.util.*;

public class ElectricalWiringManager {
    private static final Map<Integer, ElectricalWiringManager> INSTANCES = new HashMap<>();

    private final Map<Long, TileEntityElectricalWiring> registry = new HashMap<>();

    public static ElectricalWiringManager get(World world) {
        int dimId = world.provider.dimensionId;
        ElectricalWiringManager manager = INSTANCES.get(dimId);
        if (manager == null) {
            manager = new ElectricalWiringManager();
            INSTANCES.put(dimId, manager);
        }
        return manager;
    }

    public static void onWorldUnload(World world) {
        INSTANCES.remove(world.provider.dimensionId);
    }

    public void register(TileEntityElectricalWiring tile) {
        registry.put(pack(tile.xCoord, tile.yCoord, tile.zCoord), tile);
    }

    public void unregister(TileEntityElectricalWiring tile) {
        registry.remove(pack(tile.xCoord, tile.yCoord, tile.zCoord));
    }

    /**
     * ノード削除。当該座標のタイルを registry から除き、他の全タイルからこの座標への接続を切断する。
     * ブロック破壊・エンティティ死亡時に呼ぶ。
     */
    public void onNodeRemoved(int x, int y, int z) {
        registry.remove(pack(x, y, z));
        for (TileEntityElectricalWiring tile : new ArrayList<>(registry.values())) {
            if (tile.getConnection(x, y, z) != null) {
                tile.setConnectionTo(x, y, z, ConnectionType.NONE, "");
            }
        }
    }

    /**
     * プレイヤーへの TO_PLAYER 接続を全タイルから切断する。
     * プレイヤーログアウト時に呼ぶ。
     */
    public void removePlayerConnections(EntityPlayer player) {
        for (TileEntityElectricalWiring tile : new ArrayList<>(registry.values())) {
            Connection c = tile.getConnection(player.getEntityId(), -1, 0);
            if (c != null && c.type == ConnectionType.TO_PLAYER) {
                tile.setConnectionTo(player.getEntityId(), -1, 0, ConnectionType.NONE, "");
            }
        }
    }

    public TileEntityElectricalWiring getTile(int x, int y, int z) {
        return registry.get(pack(x, y, z));
    }

    /**
     * origin からBFSで全接続ノードへ signal を伝播する。
     * サイクルは visited セットで防止。1tick内に全ノードへ到達する。
     */
    public void propagateSignal(TileEntityElectricalWiring origin, int level) {
        World world = origin.getWorldObj();
        if (world == null || world.isRemote) return;

        Set<TileEntityElectricalWiring> visited = new HashSet<>();
        Deque<TileEntityElectricalWiring> queue = new ArrayDeque<>();
        visited.add(origin);
        processNode(world, origin, level, queue, visited);

        while (!queue.isEmpty()) {
            TileEntityElectricalWiring tile = queue.poll();
            tile.onReceiveSignal(level);
            processNode(world, tile, level, queue, visited);
        }
    }

    private void processNode(World world, TileEntityElectricalWiring tile, int level,
                             Deque<TileEntityElectricalWiring> queue,
                             Set<TileEntityElectricalWiring> visited) {
        for (Connection c : tile.getConnectionList()) {
            if (c.type == ConnectionType.NONE || c.type == ConnectionType.TO_PLAYER) continue;
            if (c.type == ConnectionType.DIRECT) {
                tile.applyToDirectConnection(c, level);
            } else if (c.type == ConnectionType.TO_ENTITY) {
                TileEntityElectricalWiring next = TileEntityElectricalWiring.getWireEntity(world, c.x, c.y, c.z);
                if (next != null && visited.add(next)) {
                    queue.add(next);
                }
            } else { // WIRE
                TileEntityElectricalWiring next = this.getTile(c.x, c.y, c.z);
                if (next != null && visited.add(next)) {
                    queue.add(next);
                }
            }
        }
    }

    private static long pack(int x, int y, int z) {
        return ((long) (x & 0x3FFFFFF) << 38) | ((long) (y & 0xFF) << 30) | (z & 0x3FFFFFFL);
    }
}
