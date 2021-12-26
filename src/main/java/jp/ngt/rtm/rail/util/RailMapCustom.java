package jp.ngt.rtm.rail.util;

import jp.ngt.ngtlib.io.ScriptUtil;
import jp.ngt.rtm.modelpack.ModelPackManager;
import net.minecraft.util.MathHelper;

import javax.script.ScriptEngine;

public final class RailMapCustom extends RailMap {
    private RailPosition startRP;

    private RailPosition endRP;

    private ScriptEngine script;

    public RailMapCustom(RailPosition rp, String scriptName, String args) {
        this.startRP = rp;
        this.init(scriptName, args);
    }

    private void init(String scriptName, String args) {
        this.script = ScriptUtil.doScript(ModelPackManager.INSTANCE.getScript(scriptName));
        int split = (int) (getLength() * 4.0D);
        double[] dzx = getRailPos(split, split);
        double dy = getRailHeight(split, split);
        float yaw = getRailYaw(split, split);
        int x = MathHelper.floor_double(dzx[1]);
        int y = MathHelper.floor_double(dy);
        int z = MathHelper.floor_double(dzx[0]);
        int dir = MathHelper.floor_double((yaw + 360.0F) % 360.0F / 45.0F);
        this.endRP = new RailPosition(x, y, z, (byte) dir, (byte) 0);
    }

    public static String getDefaultArgs(String scriptName) {
        return getDefaultArgs(ScriptUtil.doScript(ModelPackManager.INSTANCE.getScript(scriptName)));
    }

    public static String getDefaultArgs(ScriptEngine se) {
        return (String) ScriptUtil.doScriptFunction(se, "getDefaultArgs", new Object[0]);
    }

    public RailPosition getStartRP() {
        return this.startRP;
    }

    public RailPosition getEndRP() {
        return this.endRP;
    }

    public double getLength() {
        return (Double) ScriptUtil.doScriptFunction(this.script, "getLength", new Object[0]);
    }

    public int getNearlestPoint(int split, double x, double z) {
        return (Integer) ScriptUtil.doScriptFunction(this.script, "getNearlestPoint", new Object[]{split, x, z});
    }

    public double[] getRailPos(int split, int index) {
        return (double[]) ScriptUtil.doScriptFunction(this.script, "getPos", new Object[]{split, index});
    }

    public double getRailHeight(int split, int index) {
        return (Double) ScriptUtil.doScriptFunction(this.script, "getHeight", new Object[]{split, index});
    }

    public float getRailYaw(int split, int index) {
        float yaw = (Float) ScriptUtil.doScriptFunction(this.script, "getYaw", new Object[]{split, index});
        return yaw + this.startRP.anchorYaw;
    }

    public float getRailPitch(int split, int index) {
        return (Float) ScriptUtil.doScriptFunction(this.script, "getPitch", new Object[]{split, index});
    }

    public float getRailRoll(int split, int index) {
        return (Float) ScriptUtil.doScriptFunction(this.script, "getRoll", new Object[]{split, index});
    }
}
