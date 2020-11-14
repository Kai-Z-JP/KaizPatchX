package jp.ngt.rtm.render;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.modelpack.modelset.ModelSetRailClient;
import jp.ngt.rtm.rail.TileEntityLargeRailCore;
import jp.ngt.rtm.rail.TileEntityLargeRailSwitchCore;
import jp.ngt.rtm.rail.util.Point;
import jp.ngt.rtm.rail.util.RailDir;
import jp.ngt.rtm.rail.util.RailMapSwitch;
import jp.ngt.rtm.rail.util.RailPosition;
import net.minecraft.util.MathHelper;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * JS移植用
 */
@Deprecated
@SideOnly(Side.CLIENT)
public class DynamicRailPartsRenderer extends RailPartsRenderer {
	private static final float TONG_MOVE = 0.35F;
	private static final float TONG_POS = 1.0F / 10.0F;
	private static final float HALF_GAUGE = 0.5647F;
	/**
	 * レール長で割る
	 */
	private static final float YAW_RATE = 450.0F;

	public Parts leftParts;
	public Parts rightParts;
	public Parts tongFL;
	public Parts tongBL;
	public Parts tongFR;
	public Parts tongBR;
	private List<String> dynamicPartNames;

	@Override
	public void init(ModelSetRailClient par1, ModelObject par2) {
        this.leftParts = this.registerParts(new Parts("railL", "sideL"));
        this.rightParts = this.registerParts(new Parts("railR", "sideR"));
        this.tongFL = this.registerParts(new Parts("L0"));
        this.tongBL = this.registerParts(new Parts("L1"));
        this.tongFR = this.registerParts(new Parts("R0"));
        this.tongBR = this.registerParts(new Parts("R1"));

        this.dynamicPartNames = new ArrayList<>();
        NGTUtil.addArray(this.dynamicPartNames, this.leftParts.objNames);
        NGTUtil.addArray(this.dynamicPartNames, this.rightParts.objNames);
        NGTUtil.addArray(this.dynamicPartNames, this.tongFL.objNames);
        NGTUtil.addArray(this.dynamicPartNames, this.tongBL.objNames);
        NGTUtil.addArray(this.dynamicPartNames, this.tongFR.objNames);
        NGTUtil.addArray(this.dynamicPartNames, this.tongBR.objNames);

        //ここでパーツの初期するから最後に
        super.init(par1, par2);
    }

	@Override
	protected void renderRailStatic(TileEntityLargeRailCore tileEntity, double x, double y, double z, float par8) {
		this.renderStaticParts(tileEntity, x, y, z);
	}

	@Override
	protected void renderRailDynamic(TileEntityLargeRailCore tileEntity, double x, double y, double z, float par8) {
		if (this.isSwitchRail(tileEntity)) {
			this.renderRailDynamic2((TileEntityLargeRailSwitchCore) tileEntity, x, y, z);
		}
	}

	@Override
	protected boolean shouldRenderObject(TileEntityLargeRailCore tileEntity, String objName, int len, int pos) {
		if (this.isSwitchRail(tileEntity))//分岐レール
		{
			//可動部パーツは除外
			return !this.dynamicPartNames.contains(objName);
		} else {
			return !this.dynamicPartNames.contains(objName) || this.leftParts.containsName(objName) || this.rightParts.containsName(objName);
		}
	}

	private void renderRailDynamic2(TileEntityLargeRailSwitchCore tileEntity, double par2, double par4, double par6) {
		if (tileEntity.getSwitch() == null) {
			return;
		}

        GL11.glPushMatrix();
        RailPosition rp = tileEntity.getRailPositions()[0];
        double x = rp.posX - (double) rp.blockX;
        //double y = rp.posY - (double)rp.blockY;
        double z = rp.posZ - (double) rp.blockZ;
        GL11.glTranslatef((float) (par2 + x), (float) (par4), (float) (par6 + z));

        this.bindTexture(this.getModelObject().textures[0].material.texture);

        //分岐レールの各頂点-中間点までを描画
        Arrays.stream(tileEntity.getSwitch().getPoints()).forEach(point -> this.renderPoint(tileEntity, point));

        GL11.glPopMatrix();
    }

	private void renderPoint(TileEntityLargeRailSwitchCore tileEntity, Point point) {
		if (point.branchDir == RailDir.NONE)//分岐なし部分
		{
			RailMapSwitch rm = point.rmMain;
			int max = (int) (rm.getLength() * 2.0D);
			int halfMax = max / 2;
			int startIndex = point.mainDirIsPositive ? 0 : halfMax;
			int endIndex = point.mainDirIsPositive ? halfMax : max;
			this.renderRailMapStatic(tileEntity, rm, max, startIndex, endIndex, this.leftParts, this.rightParts);
		} else {
			int tongIndex = MathHelper.floor_double(point.rmMain.getLength() * 2.0D * (double) TONG_POS);//どの位置を末端モデルで描画
			float move = point.getMovement() * TONG_MOVE;
			this.renderRailMapDynamic(tileEntity, point.rmMain, point.branchDir,
					point.mainDirIsPositive, move, tongIndex);

			move = (1.0F - point.getMovement()) * TONG_MOVE;
			this.renderRailMapDynamic(tileEntity, point.rmBranch, point.branchDir.invert(),
					point.branchDirIsPositive, move, tongIndex);
		}
	}

	/**
	 * RailMapごとの描画
	 *
	 * @param move 開通時:0.0
	 */
	private void renderRailMapDynamic(TileEntityLargeRailSwitchCore tileEntity, RailMapSwitch rms, RailDir dir, boolean par3, float move, int tongIndex) {
		float railLength = (float) rms.getLength();
		int max = (int) (railLength * 2.0F);
		int halfMax = max / 2;
		int startIndex = par3 ? 0 : halfMax;
		int endIndex = par3 ? halfMax : max;

		double[] origPos = rms.getRailPos(max, 0);
		int[] startPos = tileEntity.getStartPoint();
		float[] revXZ = RailPosition.REVISION[tileEntity.getRailPositions()[0].direction];
		//レール全体の始点からの移動差分
		float moveX = (float) (origPos[1] - ((double) startPos[0] + 0.5D + (double) revXZ[0]));
		float moveZ = (float) (origPos[0] - ((double) startPos[2] + 0.5D + (double) revXZ[1]));
		//向きによって移動量を反転させる
		float dirFixture = ((par3 && dir == RailDir.LEFT) || (!par3 && dir == RailDir.RIGHT)) ? -1.0F : 1.0F;

		//頂点-中間点
		for (int i = startIndex; i <= endIndex; ++i) {
			double[] p1 = rms.getRailPos(max, i);
			float x0 = moveX + (float) (p1[1] - origPos[1]);
			float z0 = moveZ + (float) (p1[0] - origPos[0]);
			float yaw = rms.getRailRotation(max, i);

			GL11.glPushMatrix();
			GL11.glTranslatef(x0, 0.0F, z0);
			GL11.glRotatef(yaw, 0.0F, 1.0F, 0.0F);

			//分岐してない側のレール
			//開始位置が逆の場合は左右反対側のパーツを描画
			if ((par3 && dir == RailDir.LEFT) || (!par3 && dir == RailDir.RIGHT)) {
				this.rightParts.render(this);
			} else {
				this.leftParts.render(this);
			}

			//トング部分の離れ度合い(0.0-1.0)
			float separateRate = (float) (par3 ? i : max - i) / (float) halfMax;
			separateRate = (1.0F - this.sigmoid2(separateRate)) * move * dirFixture;
			float halfGaugeMove = dirFixture * HALF_GAUGE;
			GL11.glTranslatef(separateRate - halfGaugeMove, 0.0F, 0.0F);
			float yaw2 = separateRate * YAW_RATE / railLength * (par3 ? -1.0F : 1.0F);
			GL11.glRotatef(yaw2, 0.0F, 1.0F, 0.0F);
			GL11.glTranslatef(halfGaugeMove, 0.0F, 0.0F);

			//分岐してる側のレール
			if (dir == RailDir.LEFT) {
				if (par3)//始点を共有
				{
					if (i == tongIndex) {
						this.tongBL.render(this);//トングレール
					} else if (i > tongIndex) {
						this.leftParts.render(this);//リードレール
					}
				} else//終点を共有
				{
					if (i == max - tongIndex) {
						this.tongFR.render(this);//トングレール
					} else if (i < max - tongIndex) {
						this.rightParts.render(this);//リードレール
					}
				}
			} else//dir == RailDir.RIGHT
			{
				if (par3)//始点を共有
				{
					if (i == tongIndex) {
						this.tongBR.render(this);//トングレール
					} else if (i > tongIndex) {
						this.rightParts.render(this);//リードレール
					}
				} else//終点を共有
				{
					if (i == max - tongIndex) {
						this.tongFL.render(this);//トングレール
					} else if (i < max - tongIndex) {
						this.leftParts.render(this);//リードレール
					}
				}
			}

			GL11.glPopMatrix();
		}
	}

	private float sigmoid2(float x) {
		double d0 = (double) x * 3.5D;
		double d1 = d0 / Math.sqrt(1.0D + d0 * d0);//シグモイド関数
		return (float) (d1 * 0.75D + 0.25D);
	}
}