package jp.ngt.rtm.electric;

import jp.ngt.ngtlib.block.TileEntityCustom;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.RTMItem;
import jp.ngt.rtm.electric.Connection.ConnectionType;
import jp.ngt.rtm.item.ItemWithModel;
import jp.ngt.rtm.network.PacketWire;
import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Random;
import java.util.stream.IntStream;

public abstract class TileEntityElectricalWiring extends TileEntityCustom {
    protected List<Connection> connections = new ArrayList<>();

    public boolean isActivated;
    private int signal;
    private int prevSignal = -1;

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);

        List<Connection> prevConnections = this.connections;
        this.connections = new ArrayList<>();
        this.connections.addAll(Connection.readListFromNBT(nbt));

        if (this.getWorldObj() != null && this.getWorldObj().isRemote) {
            for (Connection connection : this.connections) {
                if (!prevConnections.contains(connection)) {
                    WireManager.INSTANCE.addWire(this, connection);//新規Conのみ登録
                }
                prevConnections.remove(connection);//重複Con削除
            }

            for (Connection connection : prevConnections) {
                WireManager.INSTANCE.removeWire(this, connection);//解除されたConを削除
            }
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        if (this.isBlockTile()) {
            super.writeToNBT(nbt);
        } else {
            nbt.setInteger("x", this.xCoord);
            nbt.setInteger("y", this.yCoord);
            nbt.setInteger("z", this.zCoord);
        }

        Connection.writeListToNBT(nbt, this.connections);
    }

    protected Connection getConnection(int x, int y, int z) {
        return this.connections.stream().filter(connection -> connection.x == x && connection.y == y && connection.z == z).findFirst().orElse(null);
    }

    public List<Connection> getConnectionList() {
        return this.connections;
    }

    /**
     * Root側で呼び出し
     */
    public boolean setConnectionTo(int x, int y, int z, ConnectionType type, String name) {
        boolean flag = false;
        if (type == ConnectionType.NONE)//切断
        {
            Connection c0 = this.getConnection(x, y, z);
            if (c0 != null) {
                this.connections.remove(c0);
                TileEntityElectricalWiring tile = this.getWireTileEntity(x, y, z, c0.type);
                if (tile != null) {
                    tile.setConnectionFrom(this.xCoord, this.yCoord, this.zCoord, ConnectionType.NONE, "");
                }
                flag = true;
            }
        } else if (type == ConnectionType.TO_PLAYER) {
            this.connections.add(new Connection(true, x, y, z, type, name));
            flag = true;
        } else {
            Block block = this.worldObj.getBlock(x, y, z);
            if (type == ConnectionType.TO_ENTITY || (block instanceof IBlockConnective)) {
                TileEntityElectricalWiring tile = this.getWireTileEntity(x, y, z, type);
                ConnectionType type2 = (type == ConnectionType.TO_ENTITY) ? ConnectionType.WIRE : type;
                if (type == ConnectionType.DIRECT || (tile != null && tile.setConnectionFrom(this.xCoord, this.yCoord, this.zCoord, type2, name))) {
                    this.connections.add(new Connection(true, x, y, z, type, name));
                    flag = true;
                }
            }
        }

        if (!this.worldObj.isRemote && flag) {
            this.markDirty();
            this.getDescriptionPacket();
        }
        return flag;
    }

    /**
     * Server Only
     */
    private boolean setConnectionFrom(int x, int y, int z, ConnectionType type, String name) {
        this.setConnection(x, y, z, type, name);
        if (!this.worldObj.isRemote) {
            this.markDirty();
            this.getDescriptionPacket();
        }
        return true;
    }

    /**
     * パケット送信なし, Server Only
     */
    private void setConnection(int x, int y, int z, ConnectionType type, String name) {
        Connection connection = this.getConnection(x, y, z);
        if (type == ConnectionType.NONE) {
            if (connection != null) {
                this.connections.remove(connection);
            }
        } else {
            if (connection == null) {
                this.connections.add(new Connection(false, x, y, z, type, name));
            }
        }
    }

    /**
     * 信号受信
     */
    public void onGetElectricity(int x, int y, int z, int level, int counter) {
        if (level == 0) {
            if (this.prevSignal < 0) {
                this.prevSignal = 0;
            }
        } else if (level > 0) {
            if (level != this.prevSignal) {
                this.prevSignal = level;
            }
        }
    }

    /**
     * 信号送信
     */
    protected void sendElectricity(Connection connection, int level, int counter) {
        TileEntityElectricalWiring tile = connection.getElectricalWiring(this.worldObj);
        if (tile != null)//counter < 128
        {
            tile.onGetElectricity(this.xCoord, this.yCoord, this.zCoord, level, ++counter);
        }
    }

    /**
     * 全てに信号送信
     */
    protected void sendElectricityToAll(int level) {
        this.connections.stream().filter(connection -> connection.type != ConnectionType.NONE).forEach(connection -> this.sendElectricity(connection, level, 0));
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (this.worldObj.isRemote) {
            if (this.isActivated) {
                Random random = this.worldObj.rand;
                IntStream.range(0, 3).forEach(d -> {
                    double d1 = (float) this.xCoord + random.nextFloat();
                    double d2 = (float) this.yCoord + random.nextFloat();
                    double d3 = (float) this.zCoord + random.nextFloat();
                    this.worldObj.spawnParticle("reddust", d1, d2, d3, 0.0D, 0.0D, 0.0D);
                });
            }
        } else {
            //接続が有効かを確認
            List<Connection> list = new ArrayList<>(this.connections);
            list.stream().filter(connection -> !connection.isAvailable(this.worldObj) || connection.type == ConnectionType.NONE).forEach(connection -> this.setConnectionTo(connection.x, connection.y, connection.z, ConnectionType.NONE, ""));

            //信号の処理
            if (this.prevSignal >= 0 && this.prevSignal != this.signal) {
                this.signal = this.prevSignal;
                this.sendElectricityToAll(this.signal);
            }
            this.prevSignal = -1;
        }
    }

    /**
     * @return プレーヤーがアイテム電線を持っていない場合は""
     */
    public static String getWireType(EntityPlayer player) {
        ItemStack itemStack = player.inventory.getCurrentItem();
        if (itemStack != null && itemStack.getItem() == RTMItem.itemWire) {
            return ((ItemWithModel) itemStack.getItem()).getModelName(itemStack);
        }
        return "";
    }

    /**
     * プレーヤー右クリック時
     */
    public boolean onRightClick(EntityPlayer player) {
        if (this.isActivated) {
            this.isActivated = false;
            this.setConnectionTo(player.getEntityId(), -1, 0, ConnectionType.NONE, "");
        } else {
            String wireType = getWireType(player);
            if (wireType.isEmpty()) {
                if (this.disconnection()) {
                    return true;
                } else {
                    this.isActivated = true;
                }
            } else//アイテムを持っている
            {
                if (this.createConnection(player, wireType)) {
                    if (!player.capabilities.isCreativeMode) {
                        --player.inventory.getCurrentItem().stackSize;
                    }
                    //this.setConnectionTo(player.getEntityId(), -1, 0, 0);
                    return true;
                } else {
                    this.isActivated = true;
                    this.setConnectionTo(player.getEntityId(), -1, 0, ConnectionType.TO_PLAYER, wireType);
                }
            }
        }

        this.getDescriptionPacket();
        return true;
    }

    /**
     * 接続解除
     */
    private boolean disconnection() {
        TileEntityElectricalWiring tile = this.searchActiveTEEW();
        if (tile != null) {
            Connection c0 = this.getConnection(tile.xCoord, tile.yCoord, tile.zCoord);
            if (c0 != null) {
                tile.isActivated = false;
                this.setConnectionTo(tile.xCoord, tile.yCoord, tile.zCoord, ConnectionType.NONE, "");
                return true;
            }
        }
        return false;
    }

    /**
     * @param player
     * @param wireType
     */
    private boolean createConnection(EntityPlayer player, String wireType) {
        TileEntityElectricalWiring tile = this.searchActiveTEEW();

        if (tile != null) {
            Connection c0 = this.getConnection(tile.xCoord, tile.yCoord, tile.zCoord);
            if (c0 == null && !wireType.isEmpty()) {
                boolean isBlock = !(tile instanceof TileEntityDummyEW);
                ConnectionType type = isBlock ? ConnectionType.WIRE : ConnectionType.TO_ENTITY;
                tile.isActivated = false;
                boolean flag;
                if (this instanceof TileEntityDummyEW) {
                    type = ConnectionType.TO_ENTITY;
                    flag = tile.setConnectionTo(this.xCoord, this.yCoord, this.zCoord, type, wireType);
                } else {
                    flag = this.setConnectionTo(tile.xCoord, tile.yCoord, tile.zCoord, type, wireType);
                }

                if (flag) {
                    //Playerとの接続解除
                    tile.setConnectionTo(player.getEntityId(), -1, 0, ConnectionType.NONE, "");
                    return true;
                }
            }
        }
        return false;
    }

    private TileEntityElectricalWiring searchActiveTEEW() {
        int dis0 = 128;
        int dis1 = dis0 * dis0;
        List<TileEntity> tileEntityList = ((List<TileEntity>) this.worldObj.loadedTileEntityList);
        if (tileEntityList != null && !tileEntityList.isEmpty()) {
            TileEntityElectricalWiring teew = tileEntityList.stream()
                    .filter(tile -> tile != this)
                    .filter(TileEntityElectricalWiring.class::isInstance)
                    .map(TileEntityElectricalWiring.class::cast)
                    .filter(tile -> tile.isActivated)
                    .filter(tile -> tile.getDistanceFrom(this.xCoord, this.yCoord, this.zCoord) < dis1)
                    .min(Comparator.comparingDouble(o -> o.getDistanceFrom(this.xCoord, this.yCoord, this.zCoord)))
                    .orElse(null);
            if (teew != null) {
                return teew;
            }
        }

        int dis = 64;
        List<EntityElectricalWiring> entityList = this.worldObj.getEntitiesWithinAABB(EntityElectricalWiring.class, AxisAlignedBB.getBoundingBox(this.xCoord - dis, this.yCoord - dis, this.zCoord - dis, this.xCoord + dis, this.yCoord + dis, this.zCoord + dis));
        if (entityList != null && !entityList.isEmpty()) {
            return entityList.stream()
                    .map(entity -> entity.tileEW)
                    .filter(tileEW -> tileEW.isActivated)
                    .min(Comparator.comparingDouble(o -> o.getDistanceFrom(this.xCoord, this.yCoord, this.zCoord)))
                    .orElse(null);
        }
        return null;
    }

    protected TileEntityElectricalWiring getWireTileEntity(int x, int y, int z, ConnectionType type) {
        Connection connection = this.getConnection(x, y, z);
        if (connection != null && connection.getElectricalWiring(this.worldObj) != null) {
            return connection.getElectricalWiring(this.worldObj);
        }

        if (type == ConnectionType.TO_ENTITY) {
            return getWireEntity(this.worldObj, x, y, z);
        }

        TileEntity tile = this.worldObj.getTileEntity(x, y, z);
        if (tile instanceof TileEntityElectricalWiring) {
            return (TileEntityElectricalWiring) tile;
        }
        return null;
    }

    public static TileEntityElectricalWiring getWireEntity(World world, int x, int y, int z) {
        List<EntityElectricalWiring> list = world.getEntitiesWithinAABB(EntityElectricalWiring.class, AxisAlignedBB.getBoundingBox(x, y, z, x + 1, y + 2, z + 1));
        if (list != null && !list.isEmpty()) {
            return list.stream()
                    .map(entity -> entity.tileEW)
                    .findFirst()
                    .orElse(null);
        }
        return null;
    }

    public boolean isBlockTile() {
        return true;
    }

    /**
     * ブロック破壊時に呼ばれる
     */
    public void onBlockBreaked() {
        this.connections.forEach(connection -> this.sendElectricity(connection, 0, 0));
        //解除されたConを削除
        this.connections.forEach(connection -> WireManager.INSTANCE.removeWire(this, connection));
    }

    @Override
    public Packet getDescriptionPacket() {
        if (this.worldObj == null || !this.worldObj.isRemote) {
            RTMCore.NETWORK_WRAPPER.sendToAll(new PacketWire(this));
        }
        return null;
    }

    @Override
    public void setPos(int x, int y, int z, int prevX, int prevY, int prevZ) {
        int difX = x - prevX;
        int difY = y - prevY;
        int difZ = z - prevZ;
        this.connections.forEach(connection -> {
            connection.x += difX;
            connection.y += difY;
            connection.z += difZ;
        });
        super.setPos(x, y, z, prevX, prevY, prevZ);
    }
}