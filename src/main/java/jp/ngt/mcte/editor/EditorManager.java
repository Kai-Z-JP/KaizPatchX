package jp.ngt.mcte.editor;

import jp.ngt.mcte.MCTE;
import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.ngtlib.util.PermissionManager;
import net.minecraft.entity.player.EntityPlayer;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

public class EditorManager {
    public static final EditorManager INSTANCE = new EditorManager();

    /**
     * [PlayerName, Editor]
     */
    private final Map<String, Editor> editorMap = new HashMap<>();

    private EditorManager() {
    }

    public void add(String playerName, Editor editor) {
        this.editorMap.put(playerName, editor);
    }

    public void remove(EntityEditor entity) {
        String key = this.editorMap.entrySet().stream().filter(entry -> entry.getValue().getEntity().equals(entity)).findFirst().map(Entry::getKey).orElse("");

        if (!key.isEmpty()) {
            this.editorMap.remove(key);
        }
    }

    public void removeAll() {
        NGTLog.sendChatMessageToAll("Clear %s Editors", this.editorMap.size());
        for (Entry<String, Editor> entry : this.editorMap.entrySet()) {
            entry.getValue().getEntity().setDead();
        }
        this.editorMap.clear();
    }

    public boolean canPlayerUseEditor(EntityPlayer par1) {
        return PermissionManager.INSTANCE.hasPermission(par1, MCTE.USE_EDITOR);
    }

    public Editor getEditor(EntityPlayer par1) {
        return this.getEditor(par1.getCommandSenderName());
    }

    public Editor getEditor(String par1) {
        return this.editorMap.entrySet().stream().filter(entry -> entry.getKey().equals(par1)).findFirst().map(Entry::getValue).orElse(null);
    }
}