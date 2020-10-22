package jp.ngt.rtm.block.tileentity;

import jp.ngt.ngtlib.block.EnumFace;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.ngtlib.util.NGTUtilClient;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.block.BlockMirror.MirrorType;
import jp.ngt.rtm.util.DummyRenderer;
import jp.ngt.rtm.util.DummyViewer;
import jp.ngt.rtm.util.SubRenderGlobal;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.culling.ICamera;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.util.Timer;
import net.minecraft.util.Vec3;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * 同じ鏡面に属するMirrorComponentの集合
 */
public class MirrorObject {
	public static final List<MirrorObject> MIRROR_OBJECTS = new ArrayList<MirrorObject>();
	private static Timer TIMER;
	private static int tick;

	private List<MirrorComponent> components = new ArrayList<MirrorComponent>();
	private DummyViewer dummyViewer;
	private Framebuffer buffer;
	private SubRenderGlobal renderGlobal;

	public MirrorType type;
	public EnumFace face;
	public Vec3 facePos;

	private boolean skipRender;
	public float top;
	public float bottom;
	public float left;
	public float right;
	/*public float width;
	public float height;
	public float fov;
	public float aspect;*/
	public float depth;

	private MirrorObject(World world, EnumFace par1, Vec3 par2, MirrorType par3) {
		this.face = par1;
		this.facePos = par2;
		this.type = par3;

		this.dummyViewer = new DummyViewer(world, 0.0D, 0.0D, 0.0D, 0.0F, 0.0F);
		int size = RTMCore.mirrorTextureSize;
		this.buffer = new Framebuffer(size, size, true);
		this.buffer.setFramebufferColor(0.0F, 0.0F, 0.0F, 0.0F);
		this.renderGlobal = new SubRenderGlobal(this);
	}

	public static void add(World world, MirrorComponent par1, EnumFace par2, MirrorType par3) {
		Vec3 vec = par1.getMirrorPos(par2);

		for (int i = 0; i < MIRROR_OBJECTS.size(); ++i) {
			MirrorObject obj = MIRROR_OBJECTS.get(i);
			if (par2 == obj.face && NGTMath.isVecEquals(obj.facePos, vec)) {
				obj.components.add(par1);
				par1.mirrorObject = obj;
				return;
			}
		}

		MirrorObject obj2 = new MirrorObject(world, par2, vec, par3);
		obj2.components.add(par1);
		par1.mirrorObject = obj2;
		MIRROR_OBJECTS.add(obj2);
	}

	public static void remove(MirrorComponent par1) {
		for (int i = 0; i < MIRROR_OBJECTS.size(); ++i) {
			MirrorObject obj = MIRROR_OBJECTS.get(i);
			if (obj.components.contains(par1)) {
				obj.components.remove(par1);
				if (obj.components.size() == 0) {
					MIRROR_OBJECTS.remove(i);
					obj.onRemoved();
				}
				return;
			}
		}
	}

	public static void onTick(DummyRenderer renderer)//RenderMirror.update()
	{
		if (TIMER == null) {
			TIMER = (Timer) NGTUtil.getField(Minecraft.class, NGTUtilClient.getMinecraft(), "timer", "field_71428_T");
		}

		++tick;

		if (tick == RTMCore.mirrorRenderingFrequency) {
			tick = 0;
			float f0 = TIMER.renderPartialTicks;

			for (int i = 0; i < MIRROR_OBJECTS.size(); ++i) {
				MirrorObject obj = MIRROR_OBJECTS.get(i);
				obj.update(renderer, f0);
			}
		}
	}

	private void onRemoved() {
		this.buffer.deleteFramebuffer();
	}

	private void update(DummyRenderer renderer, float partialTicks)//MC.runGameLoop()
	{
		Minecraft mc = NGTUtilClient.getMinecraft();
		EntityLivingBase prevViewer = mc.renderViewEntity;

		this.skipRender = !this.setViewPosition(prevViewer);

		this.clear();
		this.updateMirrorComponents(prevViewer);
		this.updateViewFrustrum();

		if (this.skipRender) {
			this.clear();
			return;
		}

		mc.renderViewEntity = this.dummyViewer;

		NGTUtilClient.checkGLError("Pre_Mirror");

		GL11.glPushMatrix();
		GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);//バッファクリア
		this.buffer.bindFramebuffer(true);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		renderer.updateCameraAndRender(this.buffer, partialTicks, this);
		GL11.glFlush();//直ちに実行
		this.buffer.unbindFramebuffer();
		GL11.glPopMatrix();

		NGTUtilClient.checkGLError("Post_Mirror");

		GL11.glViewport(0, 0, mc.displayWidth, mc.displayHeight);

		mc.renderViewEntity = prevViewer;
	}

	private void updateMirrorComponents(EntityLivingBase viewer) {
		boolean b = true;
		for (int i = 0; i < this.components.size(); ++i) {
			MirrorComponent component = this.components.get(i);
			component.update(this, viewer);
			b &= component.skipRender();
		}
		this.skipRender |= b;
	}

	/**
	 * @return 鏡面がプレーヤーから見えてるかどうか
	 */
	private boolean setViewPosition(EntityLivingBase viewer) {
		double x0 = viewer.posX - this.facePos.xCoord;
		double y0 = viewer.posY - (double) getEyeHeight(viewer) - this.facePos.yCoord;
		double z0 = viewer.posZ - this.facePos.zCoord;

		double vx = x0 * (double) this.face.flip[0] + this.facePos.xCoord;
		double vy = y0 * (double) this.face.flip[1] + this.facePos.yCoord;
		double vz = z0 * (double) this.face.flip[2] + this.facePos.zCoord;
		this.setViewPoint(vx, vy, vz);

		double d0 = x0 * (double) this.face.normal[0] + y0 * (double) this.face.normal[1] + z0 * (double) this.face.normal[2];
		this.depth = Math.abs((float) d0);
		return d0 > 0.0D;

		//float f0 = this.lookVec.x * this.face.normal[0] + this.lookVec.y * this.face.normal[1] + this.lookVec.z * this.face.normal[2];
		//return f0 <= 0.0F;
	}

	public static float getEyeHeight(EntityLivingBase viewer) {
		//return viewer.getEyeHeight();
		return viewer.yOffset - 1.62F;
	}

	public void renderBlocks(ICamera cam, int par1) {
		this.renderGlobal.renderBlocks(cam, par1);
	}

	private void clear() {
		//this.width = 0.0F;
		//this.height = 0.0F;
		this.top = 0.0F;
		this.bottom = 0.0F;
		this.left = 0.0F;
		this.right = 0.0F;
	}

	private void updateViewFrustrum() {
		//this.fov = NGTMath.toDegrees((float)Math.atan2(this.height, this.depth)) * 2.0F;
		//this.aspect = this.width / this.height;

		for (int i = 0; i < this.components.size(); ++i) {
			MirrorComponent component = this.components.get(i);
			//component.updateUV(this.width, this.height);
			component.updateFrustrum(this.top, this.bottom, this.left, this.right);
		}
	}

	private void setViewPoint(double px, double py, double pz) {
		float yaw = 0.0F;
		float pitch = 0.0F;
		switch (this.face) {
			case BOTTOM:
				pitch = -90.0F;
				break;
			case TOP:
				pitch = 90.0F;
				break;
			case BACK:
				yaw = 180.0F;
				break;
			case FRONT:
				yaw = 0.0F;
				break;
			case LEFT:
				yaw = 90.0F;
				break;
			case RIGHT:
				yaw = -90.0F;
				break;
			default:
				break;
		}

		this.dummyViewer.setLocationAndAngles(px, py, pz, yaw, pitch);
	}

	public void setSize(float t, float b, float l, float r) {
		if (t > this.top) {
			this.top = t;
		}

		if (b < this.bottom) {
			this.bottom = b;
		}

		if (l < this.left) {
			this.left = l;
		}

		if (r > this.right) {
			this.right = r;
		}
	}

	/*public void setWidthAndHeight(float w, float h)
	{
		if(this.width < w)
		{
			this.width = w;
		}

		if(this.height < h)
		{
			this.height = h;
		}
	}*/

	public EntityLivingBase getViewer() {
		return this.dummyViewer;
	}

	public boolean skipRender() {
		return this.skipRender;
	}

	public void bindTexture() {
		this.buffer.bindFramebufferTexture();
	}

	public void unbindTexture() {
		this.buffer.unbindFramebufferTexture();
	}
}