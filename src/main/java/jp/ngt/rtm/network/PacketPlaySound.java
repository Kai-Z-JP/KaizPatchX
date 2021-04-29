package jp.ngt.rtm.network;

import cpw.mods.fml.common.network.ByteBufUtils;
import cpw.mods.fml.common.network.simpleimpl.IMessage;
import cpw.mods.fml.common.network.simpleimpl.IMessageHandler;
import cpw.mods.fml.common.network.simpleimpl.MessageContext;
import io.netty.buffer.ByteBuf;
import jp.ngt.ngtlib.network.PacketCustom;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.RTMCore;
import net.minecraft.entity.Entity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

public class PacketPlaySound extends PacketCustom implements IMessage, IMessageHandler<PacketPlaySound, IMessage> {
    private ResourceLocation sound;
    private float volume;
    private float pitch;

    public PacketPlaySound() {
    }

    public PacketPlaySound(Entity par1, ResourceLocation par2, float par3, float par4) {
        super(par1);
        this.sound = par2;
        this.volume = par3;
        this.pitch = par4;
    }

    public PacketPlaySound(TileEntity par1, ResourceLocation sound2, float par3, float par4) {
        super(par1);
        this.sound = sound2;
        this.volume = par3;
        this.pitch = par4;
    }

    @Override
    public void toBytes(ByteBuf buffer) {
        super.toBytes(buffer);
        ByteBufUtils.writeUTF8String(buffer, this.sound.getResourceDomain());
        ByteBufUtils.writeUTF8String(buffer, this.sound.getResourcePath());
        buffer.writeFloat(this.volume);
        buffer.writeFloat(this.pitch);
    }

    @Override
    public void fromBytes(ByteBuf buffer) {
        super.fromBytes(buffer);
        String s1 = ByteBufUtils.readUTF8String(buffer);
        String s2 = ByteBufUtils.readUTF8String(buffer);
        this.sound = new ResourceLocation(s1, s2);
        this.volume = buffer.readFloat();
        this.pitch = buffer.readFloat();
    }

    @Override
    public IMessage onMessage(PacketPlaySound message, MessageContext ctx) {
        World world = NGTUtil.getClientWorld();
        if (message.forEntity()) {
            Entity entity = message.getEntity(world);
            if (entity != null) {
                RTMCore.proxy.playSound(entity, message.sound, message.volume, message.pitch);
            }
        } else {
            TileEntity entity = message.getTileEntity(world);
            if (entity != null) {
                RTMCore.proxy.playSound(entity, message.sound, message.volume, message.pitch);
            }
        }
        return null;
    }
}