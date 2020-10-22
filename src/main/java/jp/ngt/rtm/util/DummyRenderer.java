package jp.ngt.rtm.util;

import com.google.gson.JsonSyntaxException;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.block.tileentity.MirrorObject;
import net.minecraft.block.Block;
import net.minecraft.block.material.Material;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.WorldClient;
import net.minecraft.client.particle.EffectRenderer;
import net.minecraft.client.particle.EntityRainFX;
import net.minecraft.client.particle.EntitySmokeFX;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.culling.ClippingHelperImpl;
import net.minecraft.client.renderer.culling.Frustrum;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.client.shader.Framebuffer;
import net.minecraft.client.shader.ShaderGroup;
import net.minecraft.client.shader.ShaderLinkHelper;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.boss.BossStatus;
import net.minecraft.util.MathHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.Vec3;
import net.minecraft.world.biome.BiomeGenBase;
import net.minecraftforge.client.ForgeHooksClient;
import net.minecraftforge.client.IRenderHandler;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.Display;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GLContext;

import java.io.IOException;
import java.nio.FloatBuffer;
import java.util.Random;

@SideOnly(Side.CLIENT)
public final class DummyRenderer {
	private static final Logger logger = LogManager.getLogger();
	private static final ResourceLocation locationRainPng = new ResourceLocation("textures/environment/rain.png");
	private static final ResourceLocation locationSnowPng = new ResourceLocation("textures/environment/snow.png");
	/**
	 * Anaglyph field (0=R, 1=GB)
	 */
	public static int anaglyphField;//R+GB->立体視モード

	private Minecraft mc;
	private float farPlaneDistance;
	private int rendererUpdateCount;
	private final DynamicTexture lightmapTexture;
	private final int[] lightmapColors;
	private final ResourceLocation locationLightMap;
	private float bossColorModifier;
	private float bossColorModifierPrev;
	private boolean cloudFog;
	public ShaderGroup theShaderGroup;
	private static final ResourceLocation[] shaderResourceLocations = new ResourceLocation[]{new ResourceLocation("shaders/post/notch.json"), new ResourceLocation("shaders/post/fxaa.json"), new ResourceLocation("shaders/post/art.json"), new ResourceLocation("shaders/post/bumpy.json"), new ResourceLocation("shaders/post/blobs2.json"), new ResourceLocation("shaders/post/pencil.json"), new ResourceLocation("shaders/post/color_convolve.json"), new ResourceLocation("shaders/post/deconverge.json"), new ResourceLocation("shaders/post/flip.json"), new ResourceLocation("shaders/post/invert.json"), new ResourceLocation("shaders/post/ntsc.json"), new ResourceLocation("shaders/post/outline.json"), new ResourceLocation("shaders/post/phosphor.json"), new ResourceLocation("shaders/post/scan_pincushion.json"), new ResourceLocation("shaders/post/sobel.json"), new ResourceLocation("shaders/post/bits.json"), new ResourceLocation("shaders/post/desaturate.json"), new ResourceLocation("shaders/post/green.json"), new ResourceLocation("shaders/post/blur.json"), new ResourceLocation("shaders/post/wobble.json"), new ResourceLocation("shaders/post/blobs.json"), new ResourceLocation("shaders/post/antialias.json")};
	public static final int shaderCount = shaderResourceLocations.length;
	private int shaderIndex;
	/**
	 * Previous frame time in milliseconds
	 */
	private long prevFrameTime;
	/**
	 * End time of last render (ns)
	 */
	private long renderEndNanoTime;
	/**
	 * Is set, updateCameraAndRender() calls updateLightmap(); set by updateTorchFlicker()
	 */
	private boolean lightmapUpdateNeeded;
	private float torchFlickerX;
	private float torchFlickerDX;
	private float torchFlickerY;
	private float torchFlickerDY;
	private Random random;
	private int rainSoundCounter;
	private float[] rainXCoords;
	private float[] rainYCoords;
	private FloatBuffer fogColorBuffer;
	private float fogColorRed;
	private float fogColorGreen;
	private float fogColorBlue;
	private float fogColor2;
	private float fogColor1;

	public DummyRenderer(Minecraft par1) {
		this.mc = par1;
		this.shaderIndex = shaderCount;
		this.prevFrameTime = Minecraft.getSystemTime();
		this.random = new Random();
		this.fogColorBuffer = GLAllocation.createDirectFloatBuffer(16);
		this.lightmapTexture = new DynamicTexture(16, 16);
		this.locationLightMap = par1.getTextureManager().getDynamicTextureLocation("lightMap", this.lightmapTexture);
		this.lightmapColors = this.lightmapTexture.getTextureData();
		this.theShaderGroup = null;
	}

    /*public boolean isShaderActive()
    {
        return OpenGlHelper.shadersSupported && this.theShaderGroup != null;
    }*/

    /*public void deactivateShader()
    {
        if(this.theShaderGroup != null)
        {
            this.theShaderGroup.deleteShaderGroup();
        }

        this.theShaderGroup = null;
        this.shaderIndex = shaderCount;
    }*/

	public void activateNextShader(Framebuffer buffer) {
		if (OpenGlHelper.shadersSupported) {
			if (this.theShaderGroup != null) {
				this.theShaderGroup.deleteShaderGroup();
			}

			this.shaderIndex = (this.shaderIndex + 1) % (shaderResourceLocations.length + 1);

			if (this.shaderIndex != shaderCount) {
				try {
					logger.info("Selecting effect " + shaderResourceLocations[this.shaderIndex]);
					this.theShaderGroup = new ShaderGroup(this.mc.getTextureManager(), this.mc.getResourceManager(), buffer, shaderResourceLocations[this.shaderIndex]);
					this.theShaderGroup.createBindFramebuffers(this.mc.displayWidth, this.mc.displayHeight);
				} catch (IOException ioexception) {
					logger.warn("Failed to load shader: " + shaderResourceLocations[this.shaderIndex], ioexception);
					this.shaderIndex = shaderCount;
				} catch (JsonSyntaxException jsonsyntaxexception) {
					logger.warn("Failed to load shader: " + shaderResourceLocations[this.shaderIndex], jsonsyntaxexception);
					this.shaderIndex = shaderCount;
				}
			} else {
				this.theShaderGroup = null;
				logger.info("No effect selected");
			}
		}
	}

	public void updateRenderer(MirrorObject mirror) {
		if (OpenGlHelper.shadersSupported && ShaderLinkHelper.getStaticShaderLinkHelper() == null) {
			ShaderLinkHelper.setNewStaticShaderLinkHelper();
		}

		this.updateTorchFlicker();
		this.fogColor2 = this.fogColor1;

		EntityLivingBase entity = mirror.getViewer();
		float f = this.mc.theWorld.getLightBrightness(MathHelper.floor_double(entity.posX), MathHelper.floor_double(entity.posY), MathHelper.floor_double(entity.posZ));
		float f1 = (float) this.mc.gameSettings.renderDistanceChunks / 16.0F;
		float f2 = f * (1.0F - f1) + f1;
		this.fogColor1 += (f2 - this.fogColor1) * 0.1F;
		++this.rendererUpdateCount;
		this.addRainParticles(mirror);
		this.bossColorModifierPrev = this.bossColorModifier;

		if (BossStatus.hasColorModifier) {
			this.bossColorModifier += 0.05F;

			if (this.bossColorModifier > 1.0F) {
				this.bossColorModifier = 1.0F;
			}

			BossStatus.hasColorModifier = false;
		} else if (this.bossColorModifier > 0.0F) {
			this.bossColorModifier -= 0.0125F;
		}
	}

	public ShaderGroup getShaderGroup() {
		return this.theShaderGroup;
	}

	public void updateShaderGroupSize(int par1, int par2) {
		if (OpenGlHelper.shadersSupported) {
			if (this.theShaderGroup != null) {
				this.theShaderGroup.createBindFramebuffers(par1, par2);
			}
		}
	}

	private void orientCamera(MirrorObject mirror, float par1) {
		EntityLivingBase viewer = mirror.getViewer();
		double d0 = viewer.posX;
		double d1 = viewer.posY;
		double d2 = viewer.posZ;

        /*if(this.mc.gameSettings.thirdPersonView > 0)
        {
            double d7 = (double)(this.thirdPersonDistanceTemp + (this.thirdPersonDistance - this.thirdPersonDistanceTemp) * par1);
            float  f6 = viewer.rotationYaw;
            float  f2 = viewer.rotationPitch;

            if(this.mc.gameSettings.thirdPersonView == 2)
            {
                f2 += 180.0F;
            }

            double d3 = (double)(-MathHelper.sin(f6 / 180.0F * (float)Math.PI) * MathHelper.cos(f2 / 180.0F * (float)Math.PI)) * d7;
            double d4 = (double)(MathHelper.cos(f6 / 180.0F * (float)Math.PI) * MathHelper.cos(f2 / 180.0F * (float)Math.PI)) * d7;
            double d5 = (double)(-MathHelper.sin(f2 / 180.0F * (float)Math.PI)) * d7;

            for (int k = 0; k < 8; ++k)
            {
                float f3 = (float)((k & 1) * 2 - 1);
                float f4 = (float)((k >> 1 & 1) * 2 - 1);
                float f5 = (float)((k >> 2 & 1) * 2 - 1);
                f3 *= 0.1F;
                f4 *= 0.1F;
                f5 *= 0.1F;
                MovingObjectPosition mop = this.mc.theWorld.rayTraceBlocks(Vec3.createVectorHelper(d0 + (double)f3, d1 + (double)f4, d2 + (double)f5), Vec3.createVectorHelper(d0 - d3 + (double)f3 + (double)f5, d1 - d5 + (double)f4, d2 - d4 + (double)f5));

                if(mop != null)
                {
                    double d6 = mop.hitVec.distanceTo(Vec3.createVectorHelper(d0, d1, d2));
                    if (d6 < d7)
                    {
                        d7 = d6;
                    }
                }
            }

            if(this.mc.gameSettings.thirdPersonView == 2)
            {
                GL11.glRotatef(180.0F, 0.0F, 1.0F, 0.0F);
            }

            GL11.glRotatef(viewer.rotationPitch - f2, 1.0F, 0.0F, 0.0F);
            GL11.glRotatef(viewer.rotationYaw - f6, 0.0F, 1.0F, 0.0F);
            GL11.glTranslatef(0.0F, 0.0F, (float)(-d7));
            GL11.glRotatef(f6 - viewer.rotationYaw, 0.0F, 1.0F, 0.0F);
            GL11.glRotatef(f2 - viewer.rotationPitch, 1.0F, 0.0F, 0.0F);
        }
        else
        {
            GL11.glTranslatef(0.0F, 0.0F, -0.1F);
        }*/

		GL11.glRotatef(viewer.rotationYaw + 180.0F, 0.0F, 1.0F, 0.0F);
		GL11.glRotatef(viewer.rotationPitch, 1.0F, 0.0F, 0.0F);

		this.cloudFog = this.mc.renderGlobal.hasCloudFog(d0, d1, d2, par1);
	}

	/**
	 * sets up projection, view effects, camera position/rotation
	 */
	private void setupCameraTransform(MirrorObject mirror, float par1, int par2) {
		this.farPlaneDistance = (float) (this.mc.gameSettings.renderDistanceChunks << 4);//*16

		/*射影************************************************************************/

		GL11.glMatrixMode(GL11.GL_PROJECTION);
		GL11.glLoadIdentity();

		if (this.mc.gameSettings.anaglyph) {
			GL11.glTranslatef((float) (-(par2 * 2 - 1)) * 0.07F, 0.0F, 0.0F);
		}

		//Project.gluPerspective(mirror.fov, mirror.aspect, mirror.depth, this.farPlaneDistance * 2.0F);
		GL11.glFrustum(mirror.left, mirror.right, mirror.bottom, mirror.top, mirror.depth, (double) this.farPlaneDistance * 2.0D);

		/*************************************************************************************/

		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		GL11.glLoadIdentity();

		if (this.mc.gameSettings.anaglyph) {
			GL11.glTranslatef((float) (par2 * 2 - 1) * 0.1F, 0.0F, 0.0F);
		}

		this.orientCamera(mirror, par1);
	}

	/**
	 * Disable secondary texture unit used by lightmap
	 */
	public void disableLightmap(double p_78483_1_) {
		OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GL11.glDisable(GL11.GL_TEXTURE_2D);
		OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}

	/**
	 * Enable lightmap in secondary texture unit
	 */
	public void enableLightmap(double p_78463_1_) {
		OpenGlHelper.setActiveTexture(OpenGlHelper.lightmapTexUnit);
		GL11.glMatrixMode(GL11.GL_TEXTURE);
		GL11.glLoadIdentity();
		float f = 0.00390625F;
		GL11.glScalef(f, f, f);
		GL11.glTranslatef(8.0F, 8.0F, 8.0F);
		GL11.glMatrixMode(GL11.GL_MODELVIEW);
		this.mc.getTextureManager().bindTexture(this.locationLightMap);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_LINEAR);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_CLAMP);
		GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_CLAMP);
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		GL11.glEnable(GL11.GL_TEXTURE_2D);
		OpenGlHelper.setActiveTexture(OpenGlHelper.defaultTexUnit);
	}

	/*Recompute a random value that is applied to block color in updateLightmap()*/
	private void updateTorchFlicker() {
		this.torchFlickerDX = (float) ((double) this.torchFlickerDX + (Math.random() - Math.random()) * Math.random() * Math.random());
		this.torchFlickerDY = (float) ((double) this.torchFlickerDY + (Math.random() - Math.random()) * Math.random() * Math.random());
		this.torchFlickerDX = (float) ((double) this.torchFlickerDX * 0.9D);
		this.torchFlickerDY = (float) ((double) this.torchFlickerDY * 0.9D);
		this.torchFlickerX += (this.torchFlickerDX - this.torchFlickerX) * 1.0F;
		this.torchFlickerY += (this.torchFlickerDY - this.torchFlickerY) * 1.0F;
		this.lightmapUpdateNeeded = true;
	}

	private void updateLightmap(float par1) {
		WorldClient world = this.mc.theWorld;
		if (world == null) {
			return;
		}

		for (int i = 0; i < 256; ++i) {
			float f1 = world.getSunBrightness(1.0F) * 0.95F + 0.05F;
			float f2 = world.provider.lightBrightnessTable[i / 16] * f1;
			float f3 = world.provider.lightBrightnessTable[i % 16] * (this.torchFlickerX * 0.1F + 1.5F);

			if (world.lastLightningBolt > 0) {
				f2 = world.provider.lightBrightnessTable[i / 16];
			}

			float f4 = f2 * (world.getSunBrightness(1.0F) * 0.65F + 0.35F);
			float f5 = f2 * (world.getSunBrightness(1.0F) * 0.65F + 0.35F);
			float f6 = f3 * ((f3 * 0.6F + 0.4F) * 0.6F + 0.4F);
			float f7 = f3 * (f3 * f3 * 0.6F + 0.4F);
			float f8 = f4 + f3;
			float f9 = f5 + f6;
			float f10 = f2 + f7;
			f8 = f8 * 0.96F + 0.03F;
			f9 = f9 * 0.96F + 0.03F;
			f10 = f10 * 0.96F + 0.03F;
			float f11;

			if (this.bossColorModifier > 0.0F) {
				f11 = this.bossColorModifierPrev + (this.bossColorModifier - this.bossColorModifierPrev) * par1;
				f8 = f8 * (1.0F - f11) + f8 * 0.7F * f11;
				f9 = f9 * (1.0F - f11) + f9 * 0.6F * f11;
				f10 = f10 * (1.0F - f11) + f10 * 0.6F * f11;
			}

			if (world.provider.dimensionId == 1) {
				f8 = 0.22F + f3 * 0.75F;
				f9 = 0.28F + f6 * 0.75F;
				f10 = 0.25F + f7 * 0.75F;
			}

			if (f8 > 1.0F) {
				f8 = 1.0F;
			}

			if (f9 > 1.0F) {
				f9 = 1.0F;
			}

			if (f10 > 1.0F) {
				f10 = 1.0F;
			}

			f11 = this.mc.gameSettings.gammaSetting;
			float f12 = 1.0F - f8;
			float f13 = 1.0F - f9;
			float f14 = 1.0F - f10;
			f12 = 1.0F - f12 * f12 * f12 * f12;
			f13 = 1.0F - f13 * f13 * f13 * f13;
			f14 = 1.0F - f14 * f14 * f14 * f14;
			f8 = f8 * (1.0F - f11) + f12 * f11;
			f9 = f9 * (1.0F - f11) + f13 * f11;
			f10 = f10 * (1.0F - f11) + f14 * f11;
			f8 = f8 * 0.96F + 0.03F;
			f9 = f9 * 0.96F + 0.03F;
			f10 = f10 * 0.96F + 0.03F;

			if (f8 > 1.0F) {
				f8 = 1.0F;
			} else if (f8 < 0.0F) {
				f8 = 0.0F;
			}

			if (f9 > 1.0F) {
				f9 = 1.0F;
			} else if (f9 < 0.0F) {
				f9 = 0.0F;
			}

			if (f10 > 1.0F) {
				f10 = 1.0F;
			} else if (f10 < 0.0F) {
				f10 = 0.0F;
			}

			int j = (int) (f8 * 255.0F);
			int k = (int) (f9 * 255.0F);
			int l = (int) (f10 * 255.0F);
			this.lightmapColors[i] = 255 << 24 | j << 16 | k << 8 | l;
		}

		this.lightmapTexture.updateDynamicTexture();
		this.lightmapUpdateNeeded = false;
	}

	public void updateCameraAndRender(Framebuffer buffer, float par2, MirrorObject mirror) {
		if (this.lightmapUpdateNeeded) {
			this.updateLightmap(par2);
		}

		if (!Display.isActive() && this.mc.gameSettings.pauseOnLostFocus && (!this.mc.gameSettings.touchscreen || !Mouse.isButtonDown(1))) {
			;
		} else {
			this.prevFrameTime = Minecraft.getSystemTime();
		}

		if (!this.mc.skipRenderWorld) {
			if (this.mc.theWorld != null) {
                /*if(this.mc.isFramerateLimitBelowMax())
                {
                	int i1 = this.mc.gameSettings.limitFramerate;
                    this.renderWorld(par2, this.renderEndNanoTime + (long)(1000000000 / i1), mirror);
                }
                else*/
				{
					this.renderWorld(par2, 0L, mirror);
				}

                /*if(OpenGlHelper.shadersSupported)
                {
                    if(this.theShaderGroup != null)
                    {
                        GL11.glMatrixMode(GL11.GL_TEXTURE);
                        GL11.glPushMatrix();
                        GL11.glLoadIdentity();
                        this.theShaderGroup.loadShaderGroup(par2);
                        GL11.glPopMatrix();
                    }
                    buffer.bindFramebuffer(false);//true
                }*/

				this.renderEndNanoTime = System.nanoTime();
			}
		}
	}

	public void renderWorld(float par1, long par2, MirrorObject mirror) {
		if (this.lightmapUpdateNeeded) {
			this.updateLightmap(par1);
		}

		GL11.glEnable(GL11.GL_CULL_FACE);
		GL11.glEnable(GL11.GL_DEPTH_TEST);
		GL11.glEnable(GL11.GL_ALPHA_TEST);
		GL11.glAlphaFunc(GL11.GL_GREATER, 0.5F);

		EntityLivingBase viewer = mirror.getViewer();
		double d0 = viewer.lastTickPosX;
		double d1 = viewer.lastTickPosY;
		double d2 = viewer.lastTickPosZ;

		RenderGlobal renderglobal = this.mc.renderGlobal;

		int pass = this.mc.gameSettings.anaglyph ? 2 : 1;
		for (int j = 0; j < pass; ++j)//アナグリフのパス
		{
			if (this.mc.gameSettings.anaglyph) {
				anaglyphField = j;
				if (anaglyphField == 0) {
					GL11.glColorMask(false, true, true, false);
				} else {
					GL11.glColorMask(true, false, false, false);
				}
			}

			int size = RTMCore.mirrorTextureSize;
			GL11.glViewport(0, 0, size, size);
			this.updateFogColor(mirror, par1);
			GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
			GL11.glEnable(GL11.GL_CULL_FACE);

			this.setupCameraTransform(mirror, par1, j);
			//ActiveRenderInfo.updateRenderInfo(this.mc.thePlayer, this.mc.gameSettings.thirdPersonView == 2);

			ClippingHelperImpl.getInstance();//init

			if (this.mc.gameSettings.renderDistanceChunks >= 4) {
				this.setupFog(mirror, -1, par1);
				renderglobal.renderSky(par1);
			}

			GL11.glEnable(GL11.GL_FOG);
			this.setupFog(mirror, 1, par1);

			if (this.mc.gameSettings.ambientOcclusion != 0) {
				GL11.glShadeModel(GL11.GL_SMOOTH);
			}

			WorldRenderer[] array = SubRenderGlobal.getRenderers(renderglobal);
			if (array.length == 0) {
				renderglobal.loadRenderers();
			}

			//視錐台カリング
			Frustrum frustrum = new Frustrum();
			frustrum.setPosition(d0, d1, d2);
			renderglobal.clipRenderersByFrustum(frustrum, par1);

			if (j == 0) {
				//ブロックのGLリスト作成
				while (!renderglobal.updateRenderers(viewer, false))// && par2 != 0L)
				{
					long k = par2 - System.nanoTime();
					if (k < 0L || k > 1000000000L) {
						break;
					}
				}
			}

			if (viewer.posY < 128.0D) {
				this.renderCloudsCheck(mirror, renderglobal, par1);
			}

			this.setupFog(mirror, 0, par1);
			GL11.glEnable(GL11.GL_FOG);
			this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);
			RenderHelper.disableStandardItemLighting();
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glPushMatrix();
			//ブロック描画(pass:1)
			//renderglobal.sortAndRender(this.mc.thePlayer, 0, (double)par1);
			mirror.renderBlocks(frustrum, 0);
			GL11.glShadeModel(GL11.GL_FLAT);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);

			//Entity&TileEntity描画(pass:2)
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glPopMatrix();
			GL11.glPushMatrix();
			RenderHelper.enableStandardItemLighting();
			ForgeHooksClient.setRenderPass(0);
			renderglobal.renderEntities(viewer, frustrum, par1);
			ForgeHooksClient.setRenderPass(0);
			//ToDo: Try and figure out how to make particles render sorted correctly.. {They render behind water}
			RenderHelper.disableStandardItemLighting();
			this.disableLightmap((double) par1);
			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glPopMatrix();
			GL11.glPushMatrix();

			GL11.glMatrixMode(GL11.GL_MODELVIEW);
			GL11.glPopMatrix();

            /*GL11.glEnable(GL11.GL_BLEND);
            OpenGlHelper.glBlendFunc(770, 1, 1, 0);
            renderglobal.drawBlockDamageTexture(Tessellator.instance, viewer, par1);
            GL11.glDisable(GL11.GL_BLEND);*/

			EffectRenderer effectrenderer = this.mc.effectRenderer;
			this.enableLightmap((double) par1);
			effectrenderer.renderLitParticles(viewer, par1);
			RenderHelper.disableStandardItemLighting();
			this.setupFog(mirror, 0, par1);
			effectrenderer.renderParticles(viewer, par1);
			this.disableLightmap((double) par1);

			GL11.glDepthMask(false);
			GL11.glEnable(GL11.GL_CULL_FACE);

			this.renderRainSnow(mirror, par1);
			GL11.glDepthMask(true);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glEnable(GL11.GL_CULL_FACE);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
			this.setupFog(mirror, 0, par1);
			GL11.glEnable(GL11.GL_BLEND);
			GL11.glDepthMask(false);
			this.mc.getTextureManager().bindTexture(TextureMap.locationBlocksTexture);

			//ブロック描画(pass:2)
			if (this.mc.gameSettings.fancyGraphics) {
				if (this.mc.gameSettings.ambientOcclusion != 0) {
					GL11.glShadeModel(GL11.GL_SMOOTH);
				}

				GL11.glEnable(GL11.GL_BLEND);
				OpenGlHelper.glBlendFunc(770, 771, 1, 0);

				if (this.mc.gameSettings.anaglyph) {
					switch (anaglyphField) {
						case 0:
							GL11.glColorMask(false, true, true, true);
							break;
						case 1:
							GL11.glColorMask(true, false, false, true);
							break;
					}
				}
			}

			mirror.renderBlocks(frustrum, 1);
			//renderglobal.sortAndRender(this.mc.thePlayer, 1, (double)par1);

			if (this.mc.gameSettings.fancyGraphics) {
				GL11.glDisable(GL11.GL_BLEND);
				GL11.glShadeModel(GL11.GL_FLAT);
			}

			//Entity&TileEntity描画(pass:2)
			RenderHelper.enableStandardItemLighting();
			ForgeHooksClient.setRenderPass(1);
			renderglobal.renderEntities(viewer, frustrum, par1);
			ForgeHooksClient.setRenderPass(-1);
			RenderHelper.disableStandardItemLighting();

			GL11.glDepthMask(true);
			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glDisable(GL11.GL_FOG);

			if (viewer.posY >= 128.0D) {
				this.renderCloudsCheck(mirror, renderglobal, par1);
			}

			if (!this.mc.gameSettings.anaglyph) {
				return;
			}
		}

		GL11.glColorMask(true, true, true, false);
	}

	/**
	 * Render clouds if enabled
	 */
	private void renderCloudsCheck(MirrorObject mirror, RenderGlobal par1, float par2) {
		if (this.mc.gameSettings.shouldRenderClouds()) {
			GL11.glPushMatrix();
			this.setupFog(mirror, 0, par2);
			GL11.glEnable(GL11.GL_FOG);
			par1.renderClouds(par2);
			GL11.glDisable(GL11.GL_FOG);
			this.setupFog(mirror, 1, par2);
			GL11.glPopMatrix();
		}
	}

	private void addRainParticles(MirrorObject mirror) {
		float f = this.mc.theWorld.getRainStrength(1.0F);

		if (!this.mc.gameSettings.fancyGraphics) {
			f *= 0.5F;//f /= 2.0F;
		}

		if (f != 0.0F) {
			this.random.setSeed((long) this.rendererUpdateCount * 312987231L);
			EntityLivingBase entity = mirror.getViewer();
			WorldClient worldclient = this.mc.theWorld;
			int i = MathHelper.floor_double(entity.posX);
			int j = MathHelper.floor_double(entity.posY);
			int k = MathHelper.floor_double(entity.posZ);
			byte b0 = 10;
			double d0 = 0.0D;
			double d1 = 0.0D;
			double d2 = 0.0D;
			int l = 0;
			int i1 = (int) (100.0F * f * f);

			if (this.mc.gameSettings.particleSetting == 1) {
				i1 >>= 1;
			} else if (this.mc.gameSettings.particleSetting == 2) {
				i1 = 0;
			}

			for (int j1 = 0; j1 < i1; ++j1) {
				int k1 = i + this.random.nextInt(b0) - this.random.nextInt(b0);
				int l1 = k + this.random.nextInt(b0) - this.random.nextInt(b0);
				int i2 = worldclient.getPrecipitationHeight(k1, l1);
				Block block = worldclient.getBlock(k1, i2 - 1, l1);
				BiomeGenBase biomegenbase = worldclient.getBiomeGenForCoords(k1, l1);

				if (i2 <= j + b0 && i2 >= j - b0 && biomegenbase.canSpawnLightningBolt() && biomegenbase.getFloatTemperature(k1, i2, l1) >= 0.15F) {
					float f1 = this.random.nextFloat();
					float f2 = this.random.nextFloat();

					if (block.getMaterial() == Material.lava) {
						this.mc.effectRenderer.addEffect(new EntitySmokeFX(worldclient, (double) ((float) k1 + f1), (double) ((float) i2 + 0.1F) - block.getBlockBoundsMinY(), (double) ((float) l1 + f2), 0.0D, 0.0D, 0.0D));
					} else if (block.getMaterial() != Material.air) {
						++l;

						if (this.random.nextInt(l) == 0) {
							d0 = (double) ((float) k1 + f1);
							d1 = (double) ((float) i2 + 0.1F) - block.getBlockBoundsMinY();
							d2 = (double) ((float) l1 + f2);
						}

						this.mc.effectRenderer.addEffect(new EntityRainFX(worldclient, (double) ((float) k1 + f1), (double) ((float) i2 + 0.1F) - block.getBlockBoundsMinY(), (double) ((float) l1 + f2)));
					}
				}
			}
		}
	}

	protected void renderRainSnow(MirrorObject mirror, float par2) {
		IRenderHandler renderer = this.mc.theWorld.provider.getWeatherRenderer();
		if (renderer != null) {
			renderer.render(par2, this.mc.theWorld, this.mc);
			return;
		}

		float f1 = this.mc.theWorld.getRainStrength(par2);

		if (f1 > 0.0F) {
			this.enableLightmap((double) par2);

			if (this.rainXCoords == null) {
				this.rainXCoords = new float[1024];
				this.rainYCoords = new float[1024];

				for (int i = 0; i < 32; ++i) {
					for (int j = 0; j < 32; ++j) {
						float f2 = (float) (j - 16);
						float f3 = (float) (i - 16);
						float f4 = MathHelper.sqrt_float(f2 * f2 + f3 * f3);
						this.rainXCoords[i << 5 | j] = -f3 / f4;
						this.rainYCoords[i << 5 | j] = f2 / f4;
					}
				}
			}

			EntityLivingBase entity = mirror.getViewer();
			WorldClient world = this.mc.theWorld;
			int k2 = MathHelper.floor_double(entity.posX);
			int l2 = MathHelper.floor_double(entity.posY);
			int i3 = MathHelper.floor_double(entity.posZ);
			Tessellator tessellator = Tessellator.instance;
			GL11.glDisable(GL11.GL_CULL_FACE);
			GL11.glNormal3f(0.0F, 1.0F, 0.0F);
			GL11.glEnable(GL11.GL_BLEND);
			OpenGlHelper.glBlendFunc(770, 771, 1, 0);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
			double d0 = entity.lastTickPosX;
			double d1 = entity.lastTickPosY;
			double d2 = entity.lastTickPosZ;
			int k = MathHelper.floor_double(d1);
			byte b0 = 5;

			if (this.mc.gameSettings.fancyGraphics) {
				b0 = 10;
			}

			boolean flag = false;
			byte b1 = -1;
			float f5 = (float) this.rendererUpdateCount + par2;

			if (this.mc.gameSettings.fancyGraphics) {
				b0 = 10;
			}

			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			flag = false;

			for (int l = i3 - b0; l <= i3 + b0; ++l) {
				for (int i1 = k2 - b0; i1 <= k2 + b0; ++i1) {
					int j1 = (l - i3 + 16) * 32 + i1 - k2 + 16;
					float f6 = this.rainXCoords[j1] * 0.5F;
					float f7 = this.rainYCoords[j1] * 0.5F;
					BiomeGenBase biomegenbase = world.getBiomeGenForCoords(i1, l);

					if (biomegenbase.canSpawnLightningBolt() || biomegenbase.getEnableSnow()) {
						int k1 = world.getPrecipitationHeight(i1, l);
						int l1 = l2 - b0;
						int i2 = l2 + b0;

						if (l1 < k1) {
							l1 = k1;
						}

						if (i2 < k1) {
							i2 = k1;
						}

						float f8 = 1.0F;
						int j2 = k1;

						if (k1 < k) {
							j2 = k;
						}

						if (l1 != i2) {
							this.random.setSeed((long) (i1 * i1 * 3121 + i1 * 45238971 ^ l * l * 418711 + l * 13761));
							float f9 = biomegenbase.getFloatTemperature(i1, l1, l);
							float f10;
							double d4;

							if (world.getWorldChunkManager().getTemperatureAtHeight(f9, k1) >= 0.15F) {
								if (b1 != 0) {
									if (b1 >= 0) {
										tessellator.draw();
									}

									b1 = 0;
									this.mc.getTextureManager().bindTexture(locationRainPng);
									tessellator.startDrawingQuads();
								}

								f10 = ((float) (this.rendererUpdateCount + i1 * i1 * 3121 + i1 * 45238971 + l * l * 418711 + l * 13761 & 31) + par2) / 32.0F * (3.0F + this.random.nextFloat());
								double d3 = (double) ((float) i1 + 0.5F) - entity.posX;
								d4 = (double) ((float) l + 0.5F) - entity.posZ;
								float f12 = MathHelper.sqrt_double(d3 * d3 + d4 * d4) / (float) b0;
								float f13 = 1.0F;
								tessellator.setBrightness(world.getLightBrightnessForSkyBlocks(i1, j2, l, 0));
								tessellator.setColorRGBA_F(f13, f13, f13, ((1.0F - f12 * f12) * 0.5F + 0.5F) * f1);
								tessellator.setTranslation(-d0 * 1.0D, -d1 * 1.0D, -d2 * 1.0D);
								tessellator.addVertexWithUV((double) ((float) i1 - f6) + 0.5D, (double) l1, (double) ((float) l - f7) + 0.5D, (double) (0.0F * f8), (double) ((float) l1 * f8 / 4.0F + f10 * f8));
								tessellator.addVertexWithUV((double) ((float) i1 + f6) + 0.5D, (double) l1, (double) ((float) l + f7) + 0.5D, (double) (1.0F * f8), (double) ((float) l1 * f8 / 4.0F + f10 * f8));
								tessellator.addVertexWithUV((double) ((float) i1 + f6) + 0.5D, (double) i2, (double) ((float) l + f7) + 0.5D, (double) (1.0F * f8), (double) ((float) i2 * f8 / 4.0F + f10 * f8));
								tessellator.addVertexWithUV((double) ((float) i1 - f6) + 0.5D, (double) i2, (double) ((float) l - f7) + 0.5D, (double) (0.0F * f8), (double) ((float) i2 * f8 / 4.0F + f10 * f8));
								tessellator.setTranslation(0.0D, 0.0D, 0.0D);
							} else {
								if (b1 != 1) {
									if (b1 >= 0) {
										tessellator.draw();
									}

									b1 = 1;
									this.mc.getTextureManager().bindTexture(locationSnowPng);
									tessellator.startDrawingQuads();
								}

								f10 = ((float) (this.rendererUpdateCount & 511) + par2) / 512.0F;
								float f16 = this.random.nextFloat() + f5 * 0.01F * (float) this.random.nextGaussian();
								float f11 = this.random.nextFloat() + f5 * (float) this.random.nextGaussian() * 0.001F;
								d4 = (double) ((float) i1 + 0.5F) - entity.posX;
								double d5 = (double) ((float) l + 0.5F) - entity.posZ;
								float f14 = MathHelper.sqrt_double(d4 * d4 + d5 * d5) / (float) b0;
								float f15 = 1.0F;
								tessellator.setBrightness((world.getLightBrightnessForSkyBlocks(i1, j2, l, 0) * 3 + 15728880) / 4);
								tessellator.setColorRGBA_F(f15, f15, f15, ((1.0F - f14 * f14) * 0.3F + 0.5F) * f1);
								tessellator.setTranslation(-d0 * 1.0D, -d1 * 1.0D, -d2 * 1.0D);
								tessellator.addVertexWithUV((double) ((float) i1 - f6) + 0.5D, (double) l1, (double) ((float) l - f7) + 0.5D, (double) (0.0F * f8 + f16), (double) ((float) l1 * f8 / 4.0F + f10 * f8 + f11));
								tessellator.addVertexWithUV((double) ((float) i1 + f6) + 0.5D, (double) l1, (double) ((float) l + f7) + 0.5D, (double) (1.0F * f8 + f16), (double) ((float) l1 * f8 / 4.0F + f10 * f8 + f11));
								tessellator.addVertexWithUV((double) ((float) i1 + f6) + 0.5D, (double) i2, (double) ((float) l + f7) + 0.5D, (double) (1.0F * f8 + f16), (double) ((float) i2 * f8 / 4.0F + f10 * f8 + f11));
								tessellator.addVertexWithUV((double) ((float) i1 - f6) + 0.5D, (double) i2, (double) ((float) l - f7) + 0.5D, (double) (0.0F * f8 + f16), (double) ((float) i2 * f8 / 4.0F + f10 * f8 + f11));
								tessellator.setTranslation(0.0D, 0.0D, 0.0D);
							}
						}
					}
				}
			}

			if (b1 >= 0) {
				tessellator.draw();
			}

			GL11.glEnable(GL11.GL_CULL_FACE);
			GL11.glDisable(GL11.GL_BLEND);
			GL11.glAlphaFunc(GL11.GL_GREATER, 0.1F);
			this.disableLightmap((double) par2);
		}
	}

	/**
	 * calculates fog and calls glClearColor
	 */
	private void updateFogColor(MirrorObject mirror, float par2) {
		WorldClient world = this.mc.theWorld;
		EntityLivingBase entity = mirror.getViewer();
		float f1 = 0.25F + 0.75F * (float) this.mc.gameSettings.renderDistanceChunks / 16.0F;
		f1 = 1.0F - (float) Math.pow((double) f1, 0.25D);
		Vec3 vec3 = world.getSkyColor(entity, par2);
		float f2 = (float) vec3.xCoord;
		float f3 = (float) vec3.yCoord;
		float f4 = (float) vec3.zCoord;
		Vec3 vec31 = world.getFogColor(par2);
		this.fogColorRed = (float) vec31.xCoord;
		this.fogColorGreen = (float) vec31.yCoord;
		this.fogColorBlue = (float) vec31.zCoord;
		float f5;

		if (this.mc.gameSettings.renderDistanceChunks >= 4) {
			Vec3 vec32 = MathHelper.sin(world.getCelestialAngleRadians(par2)) > 0.0F ? Vec3.createVectorHelper(-1.0D, 0.0D, 0.0D) : Vec3.createVectorHelper(1.0D, 0.0D, 0.0D);
			f5 = (float) entity.getLook(par2).dotProduct(vec32);

			if (f5 < 0.0F) {
				f5 = 0.0F;
			} else if (f5 > 0.0F) {
				float[] afloat = world.provider.calcSunriseSunsetColors(world.getCelestialAngle(par2), par2);

				if (afloat != null) {
					f5 *= afloat[3];
					this.fogColorRed = this.fogColorRed * (1.0F - f5) + afloat[0] * f5;
					this.fogColorGreen = this.fogColorGreen * (1.0F - f5) + afloat[1] * f5;
					this.fogColorBlue = this.fogColorBlue * (1.0F - f5) + afloat[2] * f5;
				}
			}
		}

		this.fogColorRed += (f2 - this.fogColorRed) * f1;
		this.fogColorGreen += (f3 - this.fogColorGreen) * f1;
		this.fogColorBlue += (f4 - this.fogColorBlue) * f1;
		float f8 = world.getRainStrength(par2);

		if (f8 > 0.0F) {
			f5 = 1.0F - f8 * 0.5F;
			float f9 = 1.0F - f8 * 0.4F;
			this.fogColorRed *= f5;
			this.fogColorGreen *= f5;
			this.fogColorBlue *= f9;
		}

		f5 = world.getWeightedThunderStrength(par2);

		if (f5 > 0.0F) {
			float f9 = 1.0F - f5 * 0.5F;
			this.fogColorRed *= f9;
			this.fogColorGreen *= f9;
			this.fogColorBlue *= f9;
		}

		Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(this.mc.theWorld, entity, par2);

		if (this.cloudFog) {
			Vec3 vec33 = world.getCloudColour(par2);
			this.fogColorRed = (float) vec33.xCoord;
			this.fogColorGreen = (float) vec33.yCoord;
			this.fogColorBlue = (float) vec33.zCoord;
		} else if (block.getMaterial() == Material.water) {
			float f10 = (float) EnchantmentHelper.getRespiration(entity) * 0.2F;
			this.fogColorRed = 0.02F + f10;
			this.fogColorGreen = 0.02F + f10;
			this.fogColorBlue = 0.2F + f10;
		} else if (block.getMaterial() == Material.lava) {
			this.fogColorRed = 0.6F;
			this.fogColorGreen = 0.1F;
			this.fogColorBlue = 0.0F;
		}

        /*float f10 = this.fogColor2 + (this.fogColor1 - this.fogColor2) * par2;
        this.fogColorRed *= f10;
        this.fogColorGreen *= f10;
        this.fogColorBlue *= f10;*/
		double d0 = entity.lastTickPosY * world.provider.getVoidFogYFactor();

		if (d0 < 1.0D) {
			if (d0 < 0.0D) {
				d0 = 0.0D;
			}

			d0 *= d0;
			this.fogColorRed = (float) ((double) this.fogColorRed * d0);
			this.fogColorGreen = (float) ((double) this.fogColorGreen * d0);
			this.fogColorBlue = (float) ((double) this.fogColorBlue * d0);
		}

		if (this.bossColorModifier > 0.0F) {
			float f11 = this.bossColorModifierPrev + (this.bossColorModifier - this.bossColorModifierPrev) * par2;
			this.fogColorRed = this.fogColorRed * (1.0F - f11) + this.fogColorRed * 0.7F * f11;
			this.fogColorGreen = this.fogColorGreen * (1.0F - f11) + this.fogColorGreen * 0.6F * f11;
			this.fogColorBlue = this.fogColorBlue * (1.0F - f11) + this.fogColorBlue * 0.6F * f11;
		}

		if (this.mc.gameSettings.anaglyph) {
			float f11 = (this.fogColorRed * 30.0F + this.fogColorGreen * 59.0F + this.fogColorBlue * 11.0F) / 100.0F;
			float f6 = (this.fogColorRed * 30.0F + this.fogColorGreen * 70.0F) / 100.0F;
			float f7 = (this.fogColorRed * 30.0F + this.fogColorBlue * 70.0F) / 100.0F;
			this.fogColorRed = f11;
			this.fogColorGreen = f6;
			this.fogColorBlue = f7;
		}

		//背景色設定, a:0.0でテクスチャの背景が透明になる
		GL11.glClearColor(this.fogColorRed, this.fogColorGreen, this.fogColorBlue, 1.0F);
	}

	/**
	 * Sets up the fog to be rendered. If the arg passed in is -1 the fog starts at 0 and goes to 80% of far plane
	 * distance and is used for sky rendering.
	 */
	private void setupFog(MirrorObject mirror, int par1, float par2) {
		EntityLivingBase entity = mirror.getViewer();
		boolean flag = false;

		if (par1 == 999) {
			GL11.glFog(GL11.GL_FOG_COLOR, this.setFogColorBuffer(0.0F, 0.0F, 0.0F, 1.0F));
			GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR);
			GL11.glFogf(GL11.GL_FOG_START, 0.0F);
			GL11.glFogf(GL11.GL_FOG_END, 8.0F);

			if (GLContext.getCapabilities().GL_NV_fog_distance) {
				GL11.glFogi(34138, 34139);
			}

			GL11.glFogf(GL11.GL_FOG_START, 0.0F);
		} else {
			GL11.glFog(GL11.GL_FOG_COLOR, this.setFogColorBuffer(this.fogColorRed, this.fogColorGreen, this.fogColorBlue, 1.0F));
			GL11.glNormal3f(0.0F, -1.0F, 0.0F);
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			Block block = ActiveRenderInfo.getBlockAtEntityViewpoint(this.mc.theWorld, entity, par2);

			if (this.cloudFog) {
				GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
				GL11.glFogf(GL11.GL_FOG_DENSITY, 0.1F);
			} else if (block.getMaterial() == Material.water) {
				GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);

				{
					GL11.glFogf(GL11.GL_FOG_DENSITY, 0.1F - (float) EnchantmentHelper.getRespiration(entity) * 0.03F);
				}
			} else if (block.getMaterial() == Material.lava) {
				GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_EXP);
				GL11.glFogf(GL11.GL_FOG_DENSITY, 2.0F);
			} else {
				float f1 = this.farPlaneDistance;
				if (this.mc.theWorld.provider.getWorldHasVoidParticles() && !flag) {
					double d0 = (double) ((entity.getBrightnessForRender(par2) & 15728640) >> 20) / 16.0D + (entity.lastTickPosY + 4.0D) / 32.0D;

					if (d0 < 1.0D) {
						if (d0 < 0.0D) {
							d0 = 0.0D;
						}

						d0 *= d0;
						float f2 = 100.0F * (float) d0;

						if (f2 < 5.0F) {
							f2 = 5.0F;
						}

						if (f1 > f2) {
							f1 = f2;
						}
					}
				}

				GL11.glFogi(GL11.GL_FOG_MODE, GL11.GL_LINEAR);

				if (par1 < 0) {
					GL11.glFogf(GL11.GL_FOG_START, 0.0F);
					GL11.glFogf(GL11.GL_FOG_END, f1);
				} else {
					GL11.glFogf(GL11.GL_FOG_START, f1 * 0.75F);
					GL11.glFogf(GL11.GL_FOG_END, f1);
				}

				if (GLContext.getCapabilities().GL_NV_fog_distance) {
					GL11.glFogi(34138, 34139);
				}

				if (this.mc.theWorld.provider.doesXZShowFog((int) entity.posX, (int) entity.posZ)) {
					GL11.glFogf(GL11.GL_FOG_START, f1 * 0.05F);
					GL11.glFogf(GL11.GL_FOG_END, Math.min(f1, 192.0F) * 0.5F);
				}
			}

			GL11.glEnable(GL11.GL_COLOR_MATERIAL);
			GL11.glColorMaterial(GL11.GL_FRONT, GL11.GL_AMBIENT);
		}
	}

	/**
	 * Update and return fogColorBuffer with the RGBA values passed as arguments
	 */
	private FloatBuffer setFogColorBuffer(float r, float g, float b, float a) {
		this.fogColorBuffer.clear();
		this.fogColorBuffer.put(r).put(g).put(b).put(a);
		this.fogColorBuffer.flip();
		return this.fogColorBuffer;
	}
}