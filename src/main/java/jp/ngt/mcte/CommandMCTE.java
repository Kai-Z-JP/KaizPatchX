package jp.ngt.mcte;

import jp.ngt.mcte.editor.Editor;
import jp.ngt.mcte.editor.EditorManager;
import jp.ngt.ngtlib.io.NGTLog;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;

public class CommandMCTE extends CommandBase {
    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public boolean canCommandSenderUseCommand(ICommandSender commandSender) {
        return true;
    }

    @Override
    public String getCommandName() {
        return "mcte";
    }

    @Override
    public String getCommandUsage(ICommandSender par1) {
        return "commands.mcte.usage";
    }

    @Override
    public void processCommand(ICommandSender sender, String[] strings) {
        if (strings.length >= 1) {
            if (strings[0].equals("clear")) {
                EditorManager.INSTANCE.removeAll();
            } else if (strings[0].equals("minesweeper")) {
                EntityPlayer player = getCommandSenderAsPlayer(sender);//コマブロ->例外

                Editor editor = EditorManager.INSTANCE.getEditor(player);
                if (editor == null) {
                    NGTLog.sendChatMessage(player, "Please Select Range");
                    return;
                }

                int difficulty = 1;
                if (strings.length == 2) {
                    try {
                        difficulty = Integer.parseInt(strings[1]);
                    } catch (NumberFormatException ignored) {
                    }

                    if (difficulty <= 0) {
                        NGTLog.sendChatMessage(player, "Illegal number");
                        difficulty = 1;
                    }
                } else {
                    switch (player.worldObj.difficultySetting) {
                        case PEACEFUL:
                            difficulty = 9;
                            break;
                        case EASY:
                            difficulty = 8;
                            break;
                        case NORMAL:
                            difficulty = 7;
                            break;
                        case HARD:
                            difficulty = 6;
                            break;
                    }
                }

                editor.editBlocks(Editor.EditType_Minesweeper, difficulty);
            }
        }
    }
}