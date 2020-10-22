package jp.ngt.ngtlib.util;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.relauncher.ReflectionHelper;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.NGTCore;
import jp.ngt.ngtlib.network.PacketNBT;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.EnumSkyBlock;
import net.minecraft.world.World;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.nio.ByteBuffer;
import java.util.List;

public final class NGTUtil {
	public static MinecraftServer getServer() {
		return FMLCommonHandler.instance().getMinecraftServerInstance();
	}

	/**
	 * 現在のスレッドがServer threadかどうか
	 */
	public static boolean isServer() {
		return FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER;
	}

	/**
	 * マルチプレイかどうか
	 */
	public static boolean isSMP() {
		if (isServer()) {
			return !MinecraftServer.getServer().isSinglePlayer();
		} else {
			return !NGTUtilClient.getMinecraft().isSingleplayer();
		}
	}

	public static boolean openedLANWorld() {
		return (isServer() || NGTUtilClient.getMinecraft().isSingleplayer()) ? false : NGTUtilClient.getMinecraft().getIntegratedServer().getPublic();
	}

	@SideOnly(Side.CLIENT)
	public static World getClientWorld() {
		return NGTCore.proxy.getWorld();
	}

	@SideOnly(Side.CLIENT)
	public static EntityPlayer getClientPlayer() {
		return NGTCore.proxy.getPlayer();
	}

	public static int getNewRenderType() {
		return NGTCore.proxy.getNewRenderType();
	}

	public static void sendPacketToClient(TileEntity tileEntity) {
		PacketNBT.sendToClient(tileEntity);
	}

	public static void sendPacketToServer(EntityPlayer player, ItemStack stack) {
		PacketNBT.sendToServer(player, stack);
	}

	public static int getChunkLoadDistance() {
		return NGTCore.proxy.getChunkLoadDistance();
	}

	public static double getChunkLoadDistanceSq() {
		int i = getChunkLoadDistance();
		return (double) (i * i);
	}

	/**
	 * 明るさ取得(日照+光源)
	 */
	public static int getLightValue(World world, int x, int y, int z) {
		//ブロックで空が見えないと減少、見えたら夜でも15
		int skyLight = world.getSavedLightValue(EnumSkyBlock.Sky, x, y, z);
		//光源ブロックのみによる明るさ
		int blockLight = world.getBlockLightValue(x, y, z);

		float f0 = world.getCelestialAngle(1.0F);
		world.skylightSubtracted = world.calculateSkylightSubtracted(f0);
		skyLight -= world.skylightSubtracted;
		return skyLight > blockLight ? skyLight : blockLight;
	}

	public static int byteArrayToInteger(byte[] par1) {
		return ByteBuffer.wrap(par1).asIntBuffer().get();
	}

	public static byte[] integerToByteArray(int par1) {
		byte[] bs = new byte[4];
		bs[3] = (byte) (0x000000FF & (par1));
		bs[2] = (byte) (0x000000FF & (par1 >>> 8));
		bs[1] = (byte) (0x000000FF & (par1 >>> 16));
		bs[0] = (byte) (0x000000FF & (par1 >>> 24));
		return bs;
	}

	/**
	 * システム時間を使用
	 */
	public static long getUniqueId() {
		return System.currentTimeMillis();
	}

	public static void setValueToField(Class<?> clazz, Object instance, Object value, String... names) {
		Field field = ReflectionHelper.findField(clazz, names);
		try {
			field.set(instance, value);
		} catch (IllegalArgumentException e) {
			e.printStackTrace();
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
	}

	public static Object getField(Class<?> clazz, Object instance, String... names) {
		Field field = ReflectionHelper.findField(clazz, names);
		try {
			return field.get(instance);
		} catch (IllegalAccessException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * @return 呼び出したメソッドの戻り値
	 */
	public static <E> Object getMethod(Class<? super E> clazz, E instance, String[] names, Class<?>[] types, Object... args) {
		Method method = ReflectionHelper.findMethod(clazz, instance, names, types);
		try {
			return method.invoke(instance, args);
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
		return null;
	}

	/**
	 * Listに配列要素を全て追加
	 */
	public static <E> void addArray(List<E> list, E[] array) {
		for (int i = 0; i < array.length; ++i) {
			list.add(array[i]);
		}
	}

	public static <E> void reverse(E[] array) {
		int half = array.length / 2;
		for (int i = 0; i < half; ++i) {
			E element = array[i];
			int i2 = array.length - i - 1;
			array[i] = array[i2];
			array[i2] = element;
		}
	}

	/**
	 * 配列中に該当要素が含まれるか
	 */
	public static <E> boolean contains(E[] array, E obj) {
		for (int i = 0; i < array.length; ++i) {
			if (array[i] == obj) {
				return true;
			} else if (obj instanceof Object) {
				if (((Object) array[i]).equals(obj)) {
					return true;
				}
			}
		}
		return false;
	}

	public static boolean isEquippedItem(EntityPlayer player, Item item) {
		ItemStack stack = player.getCurrentEquippedItem();
		return stack != null && stack.getItem() == item;
	}
}