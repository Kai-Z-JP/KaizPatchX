package jp.ngt.ngtlib.command;

import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.ngtlib.item.ItemProtectionKey;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;

public class CommandProtection extends CommandBase {
	@Override
	public int getRequiredPermissionLevel() {
		return 3;//OP権限
	}

	@Override
	public String getCommandName() {
		return "protect";
	}

	@Override
	public String getCommandUsage(ICommandSender sender) {
		return "commands.protect.usage";
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) {
		EntityPlayerMP player = getCommandSenderAsPlayer(sender);

		if (args.length > 0) {
			ItemStack stack = ItemProtectionKey.getKey(args[0]);
			player.entityDropItem(stack, 0.5F);
			NGTLog.sendChatMessage(player, "Give Key [%s]", args[0]);
			return;
		}

		NGTLog.sendChatMessage(player, "Invalid command");
	}
}