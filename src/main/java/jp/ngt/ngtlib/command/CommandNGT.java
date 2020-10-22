package jp.ngt.ngtlib.command;

import jp.ngt.ngtlib.NGTCore;
import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.ngtlib.network.PacketNotice;
import jp.ngt.ngtlib.util.NGTCertificate;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;

public class CommandNGT extends CommandBase {
	@Override
	public int getRequiredPermissionLevel() {
		return 0;
	}

	@Override
	public String getCommandName() {
		return "ngt";
	}

	@Override
	public String getCommandUsage(ICommandSender icommandsender) {
		return "commands.ngt.usage";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		EntityPlayerMP player = getCommandSenderAsPlayer(sender);

		if (args.length == 1) {
			if (NGTCertificate.registerKey(player, args[0])) {
				NGTCore.NETWORK_WRAPPER.sendTo(new PacketNotice(PacketNotice.Side_CLIENT, "regKey"), player);
			}
		} else {
			NGTLog.sendChatMessage(player, "commands.ngt.invalid_command");
		}
	}
}