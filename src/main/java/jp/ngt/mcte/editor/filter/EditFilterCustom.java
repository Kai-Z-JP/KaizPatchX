package jp.ngt.mcte.editor.filter;

import jp.ngt.mcte.editor.Editor;
import jp.ngt.ngtlib.io.ScriptUtilV2;
import org.graalvm.polyglot.Context;

public class EditFilterCustom extends EditFilterBase {
    private final Context context;
    private final String scriptText;

    public EditFilterCustom(String data) {
        this.context = ScriptUtilV2.doScript(data);
        this.scriptText = data;
    }

    @Override
    public void init(Config par1) {
        super.init(par1);
        ScriptUtilV2.doScriptFunction(this.context, "initFilter", par1);
    }

    @Override
    public String getFilterName() {
        return (String) ScriptUtilV2.doScriptFunction(this.context, "getFilterName");
    }

    @Override
    public boolean edit(Editor editor) {
        return (Boolean) ScriptUtilV2.doScriptFunction(this.context, "edit", editor, this);
    }

    public String getScriptText() {
        return this.scriptText;
    }
}