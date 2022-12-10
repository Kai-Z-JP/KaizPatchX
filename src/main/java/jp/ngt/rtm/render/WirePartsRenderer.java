package jp.ngt.rtm.render;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.io.ScriptUtil;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.ngtlib.math.Vec3;
import jp.ngt.rtm.electric.Connection;
import jp.ngt.rtm.electric.TileEntityElectricalWiring;
import jp.ngt.rtm.modelpack.cfg.WireConfig;
import jp.ngt.rtm.modelpack.modelset.ModelSetWireClient;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nullable;
import java.util.stream.IntStream;

@SideOnly(Side.CLIENT)
public class WirePartsRenderer extends TileEntityPartsRenderer<ModelSetWireClient> {
    private final boolean useScript;

    public WirePartsRenderer(String... par1) {
        super(par1);

        this.useScript = true;
    }

    public WirePartsRenderer(boolean par1, String... par2) {
        super(par2);

        this.useScript = par1;
    }

    public void renderWire(TileEntityElectricalWiring tileEntity, Connection connection, Vec3 target, float par8, RenderPass pass) {
        this.bindTexture(this.modelSet.modelObj.textures[0].material.texture);

        this.renderWireStatic(tileEntity, connection, target, par8, pass.id);

        this.renderWireDynamic(tileEntity, connection, target, par8, pass.id);
    }

    protected void renderWireStatic(TileEntityElectricalWiring tileEntity, Connection connection, Vec3 target, float par8, int pass) {
        if (this.useScript) {
            ScriptUtil.doScriptFunction(this.script, "renderWireStatic", tileEntity, connection, target, par8, pass);
        } else {
        }
    }

    protected void renderWireDynamic(TileEntityElectricalWiring tileEntity, Connection connection, Vec3 target, float par8, int pass) {
        if (this.useScript) {
            ScriptUtil.doScriptFunction(this.script, "renderWireDynamic", tileEntity, connection, target, par8, pass);
        } else if (pass == RenderPass.NORMAL.id) {//スクリプトなしモデルでの発光防止
            WireConfig cfg = connection.getModelSet().getConfig();
            if (cfg.deflectionCoefficient > 0.0F) {
                this.renderWireDeflection(tileEntity, connection, target, par8, pass, null);
            } else {
                this.renderWireStraight(tileEntity, connection, target, par8, pass, null);
            }
        }
    }

    public void renderWireStraight(TileEntityElectricalWiring tileEntity, Connection connection, Vec3 target, float par8, int pass, @Nullable Parts parts) {
        GL11.glPushMatrix();
        GL11.glRotatef(target.getYaw() + 180.0F, 0.0F, 1.0F, 0.0F);
        GL11.glRotatef(target.getPitch() - 90.0F, 1.0F, 0.0F, 0.0F);

        ModelSetWireClient modelSet = (ModelSetWireClient) connection.getModelSet();
        WireConfig cfg = modelSet.getConfig();
        double length = target.length();
        int split = MathHelper.floor_double(length / cfg.sectionLength);
        float scaleY = (float) ((length / (double) split) / cfg.sectionLength);
        GL11.glScalef(1.0F, scaleY, 1.0F);
        IntStream.range(0, split).forEach(i -> {
            if (this.shouldRenderObject(tileEntity, split, i, pass)) {
                if (parts == null) {
                    modelSet.modelObj.model.renderAll(cfg.smoothing);
                } else {
                    parts.render(this);
                }
            }
            GL11.glTranslatef(0.0F, cfg.sectionLength, 0.0F);
        });

        GL11.glPopMatrix();
    }

    @Deprecated
    protected void renderWireDeflection2(TileEntityElectricalWiring tileEntity, Connection connection, Vec3 target, float par8, int pass) {
        double lx = Math.sqrt(target.getX() * target.getX() + target.getZ() * target.getZ());
        if (lx == 0.0D)//XZ成分なし時は直線扱い
        {
            this.renderWireStraight(tileEntity, connection, target, par8, pass, null);
            return;
        }

        ModelSetWireClient modelSet = (ModelSetWireClient) connection.getModelSet();
        WireConfig cfg = modelSet.getConfig();

        GL11.glPushMatrix();
        GL11.glRotatef(target.getYaw(), 0.0F, 1.0F, 0.0F);
        float pitch = target.getPitch();
        double ly = target.getY();
        //長さ係数は必ず1以上
        float lc = 1.0F + cfg.lengthCoefficient;
        //傾きが大きいほどたわみ係数を小さく
        //XZ長が長いほどたわみ係数を小さく
        double alpha = cfg.deflectionCoefficient * NGTMath.cos(pitch) / Math.pow(lc, lx);
        double a = 0.0D;
        if (lx > 0.0D) {
            a = (lx - (ly / (alpha * lx))) / 2.0D;
        }
        double x = 0.0D;
        while (x < lx) {
            GL11.glPushMatrix();
            double y = alpha * ((x * x) - (2.0D * a * x));
            double slope = 2.0D * alpha * (x - a);
            double slopeRad = Math.atan(slope);
            float pitchC = -(float) NGTMath.toDegrees(slopeRad);
            GL11.glTranslatef(0.0F, (float) y, (float) x);
            GL11.glRotatef(pitchC + 90.0F, 1.0F, 0.0F, 0.0F);
            modelSet.modelObj.model.renderAll(cfg.smoothing);
            GL11.glPopMatrix();

            float dx = MathHelper.cos((float) slopeRad) * cfg.sectionLength;
            x += dx;//xの増加量を補正して加算(傾きが大きいほど小さく)
        }
        GL11.glPopMatrix();
    }

    public void renderWireDeflection(TileEntityElectricalWiring tileEntity, Connection connection, Vec3 target, float par8, int pass, @Nullable Parts parts) {
        double lx = Math.sqrt(target.getX() * target.getX() + target.getZ() * target.getZ());
        if (lx == 0.0D)//XZ成分なし時は直線扱い
        {
            this.renderWireStraight(tileEntity, connection, target, par8, pass, parts);
            return;
        }

        ModelSetWireClient modelSet = (ModelSetWireClient) connection.getModelSet();
        WireConfig cfg = modelSet.getConfig();

        GL11.glPushMatrix();
        GL11.glRotatef(target.getYaw(), 0.0F, 1.0F, 0.0F);
        float pitch = target.getPitch();
        double ly = target.getY();
        //長さ係数は必ず1以上
        float lc = 1.0F + cfg.lengthCoefficient;
        //傾きが大きいほどたわみ係数を小さく
        //XZ長が長いほどたわみ係数を小さく
        double alpha = cfg.deflectionCoefficient * NGTMath.cos(pitch) / Math.pow(lc, lx);
        double a = 0.0D;
        if (lx > 0.0D) {
            a = (lx - (ly / (alpha * lx))) / 2.0D;
        }

        double x = 0.0D;
        int i = 0;
        while (x < lx) {
            GL11.glPushMatrix();
            double y = alpha * ((x * x) - (2.0D * a * x));
            double slope = 2.0D * alpha * (x - a);
            double slopeRad = Math.atan(slope);
            //長さを若干短く補正して、パーツの隙間をなくす
            float dx = MathHelper.cos((float) slopeRad) * cfg.sectionLength * 0.99F;
            double nextX = x + dx;//xの増加量を補正して加算(傾きが大きいほど小さく)
            double nextY = alpha * ((nextX * nextX) - (2.0D * a * nextX));
            double cX = (x + nextX) * 0.5D;
            double cY = (y + nextY) * 0.5D;
            double difX = nextX - x;
            double difY = nextY - y;
            float pitchC = -(float) NGTMath.toDegrees(Math.atan2(difY, difX));
            //2点の中間位置で描画し、両端のズレをなくす
            GL11.glTranslatef(0.0F, (float) cY, (float) cX);
            GL11.glRotatef(pitchC + 90.0F, 1.0F, 0.0F, 0.0F);
            GL11.glTranslatef(0.0F, -cfg.sectionLength * 0.5F, 0.0F);
            if (this.shouldRenderObject(tileEntity, 0, i, pass)) {
                if (parts == null) {
                    modelSet.modelObj.model.renderAll(cfg.smoothing);
                } else {
                    parts.render(this);
                }
            }
            GL11.glPopMatrix();

            x = nextX;
            ++i;
        }
        GL11.glPopMatrix();
    }

    private boolean shouldRenderObject(TileEntityElectricalWiring tileEntity, int len, int pos, int pass) {
        if (this.useScript) {
            return (Boolean) ScriptUtil.doScriptFunction(this.getScript(), "shouldRenderObject", tileEntity, len, pos, pass);
        } else {
            return true;
        }
    }
}