package jp.ngt.mcte.editor.filter;

import jp.ngt.mcte.editor.Editor;
import jp.ngt.mcte.editor.EntityEditor;
import jp.ngt.mcte.editor.WorldSnapshot;
import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.ngtlib.math.AABBInt;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;

import java.util.List;

public class EditFilterDeleteEntity extends EditFilterBase {
	@Override
	public void init(Config par) {
		super.init(par);
	}

	@Override
	public String getFilterName() {
		return "DeleteEntity";
	}

	@Override
	public boolean edit(Editor editor) {
		AABBInt box = editor.getSelectBox();
		if (box != null) {
			//editor.record(box);
			WorldSnapshot snapshot = editor.copy(box, "");
			List<Entity> list2 = snapshot.getEntities();
			list2.stream().filter(obj -> !(obj instanceof EntityEditor) && !(obj instanceof EntityPlayer)).forEach(Entity::setDead);
			NGTLog.sendChatMessage(editor.getEntity().getPlayer(), "Delete Entities : " + list2.size());
			return true;
		}
		return false;
	}
}