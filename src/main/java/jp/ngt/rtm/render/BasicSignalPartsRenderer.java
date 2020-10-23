package jp.ngt.rtm.render;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.renderer.model.IModelNGT;
import jp.ngt.rtm.electric.SignalLevel;
import jp.ngt.rtm.modelpack.cfg.SignalConfig;
import jp.ngt.rtm.modelpack.modelset.ModelSetSignal;
import jp.ngt.rtm.modelpack.modelset.ModelSetSignal.LightParts;
import jp.ngt.rtm.modelpack.modelset.ModelSetSignalClient;
import net.minecraft.tileentity.TileEntity;
import org.lwjgl.opengl.GL11;

import java.util.LinkedList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class BasicSignalPartsRenderer extends SignalPartsRenderer {
	public final LightParts[] lightParts;
	private final List<String> lightList = new LinkedList<String>();

	public BasicSignalPartsRenderer(SignalConfig cfg, String... args) {
		super(args);
		this.lightParts = ModelSetSignal.parseLightParts(cfg.lights);
	}

	@Override
	public void init(ModelSetSignalClient par1, ModelObject par2) {
		super.init(par1, par2);
	}

	@Override
	public void render(TileEntity entity, int pass, float par3) {
		GL11.glPushMatrix();

		this.lightList.clear();

		IModelNGT model = this.modelObj.model;
		SignalConfig cfg = this.modelSet.getConfig();
		boolean smoothing = cfg.smoothing;

		if (pass == 0) {
			model.renderOnly(smoothing, cfg.modelPartsFixture.objects);
		}

		if (cfg.rotateBody) {
			float yaw = this.getRotation(entity) - this.getBlockDirection(entity);
			float[] fa1 = cfg.modelPartsBody.pos;
			GL11.glTranslatef(fa1[0], fa1[1], fa1[2]);
			GL11.glRotatef(yaw, 0.0F, 1.0F, 0.0F);
			GL11.glTranslatef(-fa1[0], -fa1[1], -fa1[2]);
		}

		if (pass == 0) {
			model.renderOnly(smoothing, cfg.modelPartsBody.objects);
		} else if (pass == 2) {
			//信号上限への対応は個々でのみ行う
			int signal = this.getSignal(entity);
			if (signal > SignalLevel.HIGH_SPEED_PROCEED.level) {
				signal = SignalLevel.HIGH_SPEED_PROCEED.level;
			}

			boolean finish = false;
			int i0 = -1;
			for (int j = 0; j < 2; ++j) {
				if (j == 1) {
					float f0 = 0.0625F;
					GL11.glColor4f(f0, f0, f0, 1.0F);
				}

				for (int i = 0; i < this.lightParts.length; ++i) {
					if (j == 0) {
						boolean render = false;
						if (!finish && signal > 0 && signal <= this.lightParts[i].signalLevel) {
							finish = true;
							render = true;
							int itv = this.lightParts[i].interval;
							if (itv > 0) {
								render = ((this.getTick(entity) / itv) % 2) == 0;//ライト被ってるせい
							}
						}

						if (render) {
							i0 = i;
							for (String s : this.lightParts[i].parts) {
								model.renderPart(smoothing, s);//点灯してるライト
								this.lightList.add(s);
							}
						}
					} else {
						for (String s : this.lightParts[i].parts) {
							if (!this.lightList.contains(s)) {
								model.renderPart(smoothing, s);//点灯してないライト
							}
						}
					}
				}
			}
		}

		GL11.glPopMatrix();
	}
}