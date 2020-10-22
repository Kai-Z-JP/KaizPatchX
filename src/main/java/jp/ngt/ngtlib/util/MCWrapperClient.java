package jp.ngt.ngtlib.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.NGTCore;
import net.minecraft.entity.player.EntityPlayer;

@SideOnly(Side.CLIENT)
public final class MCWrapperClient {
	//1.12
	/*public static void spawnParticle(World world, String name, double posX, double posY, double posZ, double speedX, double speedY, double speedZ)
    {
		world.spawnParticle(EnumParticleTypes.getByName(name), posX, posY, posZ, speedX, speedY, speedZ);
    }*/

	//1.12
    /*public static void playSound(World world, String name, double posX, double posY, double posZ, float volume, float pitch, boolean distanceDelay)
    {
    	SoundEvent soundevent = (SoundEvent)SoundEvent.REGISTRY.getObject(new ResourceLocation(name));
    	world.playSound(posX, posY, posZ, soundevent, SoundCategory.MASTER, volume, pitch, distanceDelay);
    }*/

	public static EntityPlayer getPlayer() {
		return NGTCore.proxy.getPlayer();
	}

	public static void execCommand(String command) {
		NGTUtilClient.getMinecraft().thePlayer.sendChatMessage("/" + command);//"/"なしは通常のチャットメッセージ
	}
}