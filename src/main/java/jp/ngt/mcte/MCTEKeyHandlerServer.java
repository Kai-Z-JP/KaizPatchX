package jp.ngt.mcte;

import jp.ngt.mcte.editor.Editor;
import jp.ngt.mcte.editor.EditorManager;
import jp.ngt.ngtlib.io.NGTLog;
import net.minecraft.entity.player.EntityPlayer;

public class MCTEKeyHandlerServer {
	public static final MCTEKeyHandlerServer INSTANCE = new MCTEKeyHandlerServer();

	private MCTEKeyHandlerServer() {
	}

	public void onKeyDown(EntityPlayer player, byte keyCode) {
		if (keyCode == MCTE.KEY_EditMenu) {
			Editor editor = EditorManager.INSTANCE.getEditor(player);
			if (editor != null && editor.getEntity().isSelectEnd()) {
				player.openGui(MCTE.instance, MCTE.guiIdEditor, player.worldObj, editor.getEntity().getEntityId(), 0, 0);
			}
		} else if (keyCode == MCTE.KEY_EditMode) {
			Editor editor = EditorManager.INSTANCE.getEditor(player);
			if (editor != null) {
				byte b = (byte) (editor.getEntity().getEditMode() + 1);
				if (editor.getEntity().isSelectEnd()) {
					b = b > 3 ? 2 : (b < 2 ? 2 : b);
				} else {
					b = b > 1 ? 0 : b;
				}
				editor.getEntity().setEditMode(b);
				NGTLog.sendChatMessage(player, "Set EditMode : " + b);
			}
		} else if (keyCode == MCTE.KEY_Undo) {
			Editor editor = EditorManager.INSTANCE.getEditor(player);
			if (editor != null) {
				editor.undo();
			}
		} else if (keyCode == MCTE.KEY_Clear) {
			Editor editor = EditorManager.INSTANCE.getEditor(player);
			if (editor != null) {
				editor.getEntity().setDead();
			}
		}
	}
}