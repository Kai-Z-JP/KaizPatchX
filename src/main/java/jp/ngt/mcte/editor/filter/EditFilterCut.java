package jp.ngt.mcte.editor.filter;

import jp.ngt.mcte.editor.Editor;
import jp.ngt.mcte.editor.WorldSnapshot;
import jp.ngt.ngtlib.math.AABBInt;

public class EditFilterCut extends EditFilterBase {
	@Override
	public void init(Config par) {
		super.init(par);
		par.addBoolean("IgnoreWater", false);
	}

	@Override
	public String getFilterName() {
		return "Cut";
	}

	/*@Override
	public String getCfgName()
	{
		return "Copy";
	}*/

	@Override
	public boolean edit(Editor editor) {
		boolean ignoreWater = this.getCfg().getBoolean("IgnoreWater");
		StringBuilder sb = new StringBuilder();
		if (ignoreWater) {
			sb.append(WorldSnapshot.IGNORE_WATER);
		}

		AABBInt box = editor.getSelectBox();
		if (box != null) {
			editor.copy(box, sb.toString());
			editor.record(box);
			editor.delete(box, sb.toString());
			return true;
		}
		return false;
	}
}