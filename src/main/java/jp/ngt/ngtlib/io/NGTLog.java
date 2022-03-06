package jp.ngt.ngtlib.io;

import cpw.mods.fml.client.FMLClientHandler;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.util.NGTUtilClient;
import net.minecraft.command.ICommandSender;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.IChatComponent;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public final class NGTLog {
    private static final Logger logger = LogManager.getLogger("NGT");

    public static void debug(String par1) {
        debug(par1, new Object[0]);
    }

    public static void debug(String par1, Object... par2) {
        if (par2 == null || par2.length == 0) {
            logger.log(Level.INFO, par1);
        } else {
            logger.log(Level.INFO, String.format(par1, par2));
        }
    }

	/*
	player.sendChatToPlayer(
		ChatMessageComponent.createFromTranslationWithSubstitutions(
			"commands.message.display.outgoing",
			new Object[]{player.getCommandSenderName(), message}
		).setColor(EnumChatFormatting.GRAY).setItalic(Boolean.valueOf(true))
	);
	*/

    /**
     * フォーマットはこちらで行う
     */
    public static void sendChatMessage(ICommandSender player, String message, Object... objects)//ServerCommandManager
    {
        if (player == null) {
            player = FMLClientHandler.instance().getClientPlayerEntity();
        }
        player.addChatMessage(new ChatComponentTranslation(message, objects));
    }

    /**
     * フォーマットはこちらで行う
     */
    public static void sendChatMessageToAll(String message, Object... objects) {
        if (MinecraftServer.getServer() == null) {
            debug("[NGTLog] Can't send message. This is client.");
        } else {
            MinecraftServer.getServer().getConfigurationManager().sendChatMsg(new ChatComponentTranslation(message, objects));
        }
    }

    @SideOnly(Side.CLIENT)
    public static void showChatMessage(IChatComponent component) {
        NGTUtilClient.getMinecraft().ingameGUI.getChatGUI().printChatMessage(component);
    }

    @SideOnly(Side.CLIENT)
    public static void showChatMessage(String message, Object... objects) {
        showChatMessage(new ChatComponentText(String.format(message, objects)));
    }
}