package jp.ngt.mcte.editor.filter;

import jp.ngt.mcte.editor.Editor;
import jp.ngt.ngtlib.io.ScriptUtil;

import javax.script.ScriptEngine;

public class EditFilterCustom extends EditFilterBase {
	private final ScriptEngine script;
	private final String scriptText;

	public EditFilterCustom(String data) {
		this.script = ScriptUtil.doScript(data);
		this.scriptText = data;
	}

	@Override
	public void init(Config par1) {
		super.init(par1);
		ScriptUtil.doScriptFunction(this.script, "initFilter", par1);
	}

	@Override
	public String getFilterName() {
		return (String) ScriptUtil.doScriptFunction(this.script, "getFilterName");
	}

	@Override
	public boolean edit(Editor editor) {
		return (Boolean) ScriptUtil.doScriptFunction(this.script, "edit", editor, this);
	}

	public String getScriptText() {
		return this.scriptText;
	}
}