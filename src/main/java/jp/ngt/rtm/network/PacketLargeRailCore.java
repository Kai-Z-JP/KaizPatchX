package jp.ngt.rtm.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.rail.TileEntityLargeRailCore;
import jp.ngt.rtm.rail.TileEntityLargeRailNormalCore;
import jp.ngt.rtm.rail.TileEntityLargeRailSlopeCore;
import jp.ngt.rtm.rail.TileEntityLargeRailSwitchCore;
import jp.ngt.rtm.rail.util.RailPosition;
import jp.ngt.rtm.rail.util.RailProperty;
import jp.ngt.rtm.rail.util.SwitchType;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.stream.IntStream;

public class PacketLargeRailCore implements IMessage, IMessageHandler<PacketLargeRailCore, IMessage> {
    public static final byte TYPE_NORMAL = 0;
    public static final byte TYPE_SLOPE = 1;
    public static final byte TYPE_SWITCH = 2;

    private byte dataType;
    private int x, y, z;
    private int sX, sY, sZ;
    private NBTTagCompound property;
    private byte type;

    private RailPosition[] railPositions;

    public PacketLargeRailCore() {
    }

    public PacketLargeRailCore(TileEntityLargeRailCore tile, byte par2Type) {
        this.dataType = par2Type;
        this.x = tile.xCoord;
        this.y = tile.yCoord;
        this.z = tile.zCoord;
        this.sX = tile.getStartPoint()[0];
        this.sY = tile.getStartPoint()[1];
        this.sZ = tile.getStartPoint()[2];
        this.property = new NBTTagCompound();
        tile.getProperty().writeToNBT(this.property);
        this.railPositions = tile.getRailPositions();

        switch (par2Type) {
            case TYPE_NORMAL:
                break;
            case TYPE_SLOPE:
                TileEntityLargeRailSlopeCore tile0 = (TileEntityLargeRailSlopeCore) tile;
                this.type = tile0.getSlopeType();
                break;
            case TYPE_SWITCH:
                TileEntityLargeRailSwitchCore tile1 = (TileEntityLargeRailSwitchCore) tile;
                SwitchType st = tile1.getSwitch();
                this.type = st != null ? st.id : -1;
                break;
        }
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        buffer.writeByte(this.dataType);
        buffer.writeInt(this.x);
        buffer.writeInt(this.y);
        buffer.writeInt(this.z);
        buffer.writeInt(this.sX);
        buffer.writeInt(this.sY);
        buffer.writeInt(this.sZ);

        ByteBufUtils.writeTag(buffer, this.property);
        buffer.writeByte(this.type);

        buffer.writeByte(this.railPositions.length);
        Arrays.stream(this.railPositions).forEach(rp -> ByteBufUtils.writeTag(buffer, rp.writeToNBT()));
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        this.dataType = buffer.readByte();
        this.x = buffer.readInt();
        this.y = buffer.readInt();
        this.z = buffer.readInt();
        this.sX = buffer.readInt();
        this.sY = buffer.readInt();
        this.sZ = buffer.readInt();

        this.property = ByteBufUtils.readTag(buffer);
        this.type = buffer.readByte();

        byte size = buffer.readByte();
        if (size > 0) {
            this.railPositions = new RailPosition[size];
            IntStream.range(0, size).forEach(i -> {
                NBTTagCompound nbt = ByteBufUtils.readTag(buffer);
                this.railPositions[i] = RailPosition.readFromNBT(nbt);
            });
        }
    }

    @Override
    public IMessage onMessage(PacketLargeRailCore message, MessageContext ctx) {
        World world = NGTUtil.getClientWorld();
        TileEntity tile = world.getTileEntity(message.x, message.y, message.z);
        if (tile instanceof TileEntityLargeRailCore) {
            TileEntityLargeRailCore tile0 = (TileEntityLargeRailCore) tile;
            tile0.setStartPoint(message.sX, message.sY, message.sZ);
            tile0.setProperty(RailProperty.readFromNBT(message.property));
            tile0.setRailPositions(message.railPositions);
            if (message.dataType == TYPE_NORMAL && tile instanceof TileEntityLargeRailNormalCore) {
            } else if (message.dataType == TYPE_SLOPE && tile instanceof TileEntityLargeRailSlopeCore) {
                TileEntityLargeRailSlopeCore tile1 = (TileEntityLargeRailSlopeCore) tile;
                tile1.setSlopeType(message.type);
            } else if (message.dataType == TYPE_SWITCH && tile instanceof TileEntityLargeRailSwitchCore) {
                TileEntityLargeRailSwitchCore tile1 = (TileEntityLargeRailSwitchCore) tile;
                //tile1.setSwitchType(message.type);
            }
            //tile0.createRailMap();
            tile0.getRailMap(null).getRailBlockList(RailProperty.readFromNBT(message.property)).forEach(pos -> world.markBlockForUpdate(pos[0], pos[1], pos[2]));
        }
        return null;
    }
}