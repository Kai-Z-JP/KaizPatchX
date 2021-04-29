package jp.ngt.rtm.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import io.netty.buffer.ByteBuf;
import jp.ngt.ngtlib.network.PacketCustom;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;

public class PacketNotice extends PacketCustom {
    public static final byte Side_SERVER = 0;
    public static final byte Side_CLIENT = 1;
    /**
     * side | hasPosData << 1
     */
    public byte type;
    public String notice;

    public PacketNotice() {
    }

    public PacketNotice(byte par1, String par2) {
        this.type = par1;
        this.notice = par2;
    }

    public PacketNotice(byte par1, String data, Entity entity) {
        super(entity);
        this.type = (byte) (par1 | 2);
        this.notice = data;
    }

    public PacketNotice(byte par1, String data, TileEntity entity) {
        super(entity);
        this.type = (byte) (par1 | 2);
        this.notice = data;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        super.toBytes(buffer);
        buffer.writeByte(this.type);
        ByteBufUtils.writeUTF8String(buffer, this.notice);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        super.fromBytes(buffer);
        this.type = buffer.readByte();
        this.notice = ByteBufUtils.readUTF8String(buffer);
    }
}