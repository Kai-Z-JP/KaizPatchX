package jp.ngt.mcte.editor.filter;

import jp.ngt.mcte.editor.Editor;
import jp.ngt.ngtlib.io.NGTFileLoader;

import java.io.File;

public abstract class EditFilterBase {
	protected Config cfg;

	public EditFilterBase() {
	}

	public void init(Config par) {
		this.cfg = par;
	}

	public void save() {
		this.cfg.save(this.getCfgFile());
	}

	public File getCfgFile() {
		return new File(NGTFileLoader.getModsDir().get(0), FilterManager.FILTER_PATH + this.getFilterName() + ".cfg");
	}

	public Config getCfg() {
		return this.cfg;
	}

	public abstract String getFilterName();

	/*@Deprecated
	public String getCfgName()
	{
		return this.getFilterName();
	}*/

	public abstract boolean edit(Editor editor);
}