package jp.ngt.rtm.electric;

import jp.ngt.rtm.modelpack.ModelPackManager;
import jp.ngt.rtm.modelpack.modelset.ModelSetWire;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class Connection {
    public final boolean isRoot;
    public int x;
    public int y;
    public int z;
    public final ConnectionType type;
    public final String wireName;

    private Object connectedObject;
    private ModelSetWire modelSet;

    public Connection(boolean par0, int par1, int par2, int par3, ConnectionType par4, String par5) {
        this.isRoot = par0;
        this.x = par1;
        this.y = par2;
        this.z = par3;
        this.type = par4;
        this.wireName = par5;
    }

    public static List<Connection> readListFromNBT(NBTTagCompound nbt, World world) {
        NBTTagList tagList = nbt.getTagList("connections", 10);
        List<Connection> connectionList = IntStream.range(0, tagList.tagCount()).mapToObj(tagList::getCompoundTagAt).map(Connection::readFromNBT).collect(Collectors.toList());
        if (world != null) {
            connectionList.forEach(connection -> connection.setTileEntity(world));
        }
        return connectionList;
    }

    public void setTileEntity(World world) {
        this.connectedObject = world.getTileEntity(x, y, z);
    }

    public static Connection readFromNBT(NBTTagCompound nbt) {
        boolean b0 = nbt.getBoolean("IsRoot");
        if (!nbt.hasKey("IsRoot"))//v34互換
        {
            b0 = true;
        }
        int x = nbt.getInteger("x");
        int y = nbt.getInteger("y");
        int z = nbt.getInteger("z");
        int type = nbt.getInteger("type");
        String name = nbt.getString("ModelName");
        if (name.isEmpty()) {
            //v34互換
            switch (type) {
                case 1:
                case 50:
                    name = "BasicWireBlack";
                    break;
                case 2:
                    name = "SimpleCatenary";
                    type = 1;
                    break;
                case 51:
                    name = "SimpleCatenary";
                    type = 50;
                    break;
            }
        }
        return new Connection(b0, x, y, z, ConnectionType.getType(type), name);
    }

    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setBoolean("IsRoot", this.isRoot);
        nbt.setInteger("x", this.x);
        nbt.setInteger("y", this.y);
        nbt.setInteger("z", this.z);
        nbt.setInteger("type", this.type.id);
        nbt.setString("ModelName", this.wireName);
    }

    public static void writeListToNBT(NBTTagCompound nbt, List<Connection> list) {
        NBTTagList tagList = new NBTTagList();
        list.forEach(connection -> {
            NBTTagCompound nbt0 = new NBTTagCompound();
            connection.writeToNBT(nbt0);
            tagList.appendTag(nbt0);
        });
        nbt.setTag("connections", tagList);
    }

    public ModelSetWire getModelSet() {
        if (this.modelSet == null || this.modelSet.isDummy()) {
            this.modelSet = ModelPackManager.INSTANCE.getModelSet("ModelWire", this.wireName);
        }
        return this.modelSet;
    }

    public TileEntityElectricalWiring getElectricalWiring(World world) {
        if (this.type == ConnectionType.WIRE || this.type == ConnectionType.TO_ENTITY) {
            if (this.connectedObject != null && this.connectedObject instanceof TileEntityElectricalWiring) {
                return (TileEntityElectricalWiring) this.connectedObject;
            } else {
                if (this.type == ConnectionType.TO_ENTITY) {
                    this.connectedObject = TileEntityElectricalWiring.getWireEntity(world, x, y, z);
                    return (TileEntityElectricalWiring) this.connectedObject;
                } else {
                    TileEntity te = world.getTileEntity(this.x, this.y, this.z);
                    if (te instanceof TileEntityElectricalWiring) {
                        this.connectedObject = te;
                        return (TileEntityElectricalWiring) this.connectedObject;
                    }
                }
            }
        }

        return null;
    }

    public IProvideElectricity getIProvideElectricity(World world) {
        if (this.type == ConnectionType.DIRECT) {
            //接続先Block破壊->再設置で元のTEを参照してしまうの防止
			/*if(this.connectedObject != null && this.connectedObject instanceof IProvideElectricity)
			{
				return (IProvideElectricity)this.connectedObject;
			}
			else
			{
				TileEntity te = world.getTileEntity(this.x, this.y, this.z);
    			if(te instanceof IProvideElectricity)
    			{
    				this.connectedObject = te;
    				return (IProvideElectricity)this.connectedObject;
    			}
			}*/

            TileEntity te = world.getTileEntity(this.x, this.y, this.z);
            if (te instanceof IProvideElectricity) {
                return (IProvideElectricity) te;
            }
        }
        return null;
    }

    public EntityPlayer getPlayer(World world) {
        if (type == ConnectionType.TO_PLAYER) {
            if (this.isAvailable(world)) {
                return (EntityPlayer) this.connectedObject;
            }
        }
        return null;
    }

    /**
     * 接続が有効かどうか
     */
    public boolean isAvailable(World world) {
        if (this.type == ConnectionType.DIRECT) {
            return true;
        }

        if (this.type == ConnectionType.TO_PLAYER) {
            if (this.connectedObject instanceof EntityPlayer) {
                return !TileEntityElectricalWiring.getWireType((EntityPlayer) this.connectedObject).isEmpty();
            } else {
                Entity entity = world.getEntityByID(this.x);
                if (entity instanceof EntityPlayer) {
                    this.connectedObject = entity;
                    return true;
                }
            }
            return false;
        }

        if (this.connectedObject == null) {
            this.getElectricalWiring(world);
        }

        if (this.connectedObject instanceof TileEntityDummyEW) {
            TileEntityDummyEW tile = (TileEntityDummyEW) this.connectedObject;
            if (tile.entityEW == null || tile.entityEW.isDead) {
                return false;
            }
        } else if (this.connectedObject instanceof TileEntityElectricalWiring) {
            return !((TileEntityElectricalWiring) this.connectedObject).isInvalid();
        }

        return this.connectedObject != null;
    }

    /**
     * 0:接続なし,
     * 1:普通の電線,
     * 3:直接接続,
     * 4:直接接続(ATCなど),
     * 50:Player{eID, -1, 0} (電線),
     */
    public enum ConnectionType {
        NONE(0, false),
        WIRE(1, true),
        //TRAIN_WIRE(2, true),//2:架線
        DIRECT(3, false),
        TO_ENTITY(4, true),
        TO_PLAYER(50, true);
        //TO_PLAYER_2(51, true);//51:Player{eID, -1, 0} (架線)

        public final byte id;
        public final boolean isVisible;

        ConnectionType(int par1, boolean par2) {
            this.id = (byte) par1;
            this.isVisible = par2;
        }

        public static ConnectionType getType(int par1) {
            return Arrays.stream(ConnectionType.values()).filter(type -> type.id == par1).findFirst().orElse(NONE);
        }
    }
}