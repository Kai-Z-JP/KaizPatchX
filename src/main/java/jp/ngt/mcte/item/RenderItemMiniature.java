package jp.ngt.mcte.item;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.mcte.MCTE;
import jp.ngt.ngtlib.block.NGTObject;
import jp.ngt.ngtlib.renderer.DisplayList;
import jp.ngt.ngtlib.renderer.GLHelper;
import jp.ngt.ngtlib.renderer.NGTRenderer;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.ngtlib.util.NGTUtilClient;
import jp.ngt.ngtlib.world.NGTWorld;
import net.minecraft.client.renderer.entity.RenderBiped;
import net.minecraft.client.renderer.texture.TextureMap;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.MathHelper;
import net.minecraftforge.client.IItemRenderer;
import net.minecraftforge.client.MinecraftForgeClient;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderPlayerEvent;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class RenderItemMiniature implements IItemRenderer {
	public static final RenderItemMiniature INSTANCE = new RenderItemMiniature();

	private Map<ItemStack, RenderProp> propMap = new HashMap<ItemStack, RenderProp>();

	private RenderItemMiniature() {
	}

	@Override
	public boolean handleRenderType(ItemStack item, ItemRenderType type) {
		return item.hasTagCompound();
	}

	@Override
	public boolean shouldUseRenderHelper(ItemRenderType type, ItemStack item, ItemRendererHelper helper) {
		switch (helper) {
			default:
				return false;
		}
	}

	private void renderItemAsArmor(ItemStack item) {
		this.renderItem(ItemRenderType.EQUIPPED, item);
	}

	@Override
	public void renderItem(ItemRenderType type, ItemStack item, Object... data) {
		RenderProp prop = this.propMap.get(item);
		if (prop == null) {
			NGTObject ngto = ItemMiniature.getNGTObject(item.getTagCompound());
			if (ngto == null) {
				return;
			}
			prop = new RenderProp(ngto, item);
			this.propMap.put(item, prop);
		}

		GL11.glPushMatrix();
		GL11.glEnable(GL12.GL_RESCALE_NORMAL);

		boolean useOffset = true;
		float f0 = prop.scale;
		switch (type) {
			case ENTITY:
				break;
			case EQUIPPED:
				if ("isArmor".equals(data[0])) {
					;
				} else {
					GL11.glTranslatef(0.375F, -0.0625F, -0.0625F);
				}
				break;
			case EQUIPPED_FIRST_PERSON:
				break;
			case FIRST_PERSON_MAP:
				break;
			case INVENTORY:
				//RenderItem.renderItemIntoGUI()
				GL11.glTranslatef(8.0F, 12.0F, 0.0F);
				GL11.glScalef(1.0F, -1.0F, -1.0F);
				GL11.glRotatef(30.0F, 1.0F, 0.0F, 0.0F);
				GL11.glRotatef(45.0F, 0.0F, 1.0F, 0.0F);
				f0 = prop.scaleInInventory * 10.0F;
				useOffset = false;
				break;
			default:
				break;
		}

		if (useOffset) {
			GL11.glTranslatef(prop.offsetX, prop.offsetY, prop.offsetZ);
		}
		GL11.glScalef(f0, f0, f0);
		GL11.glTranslatef(-prop.corX, 0.0F, -prop.corZ);
		int pass = MinecraftForgeClient.getRenderPass();
		if (pass == -1) {
			pass = 0;
		}
		NGTRenderer.renderTileEntities(prop.world, 0.0F, pass);
		NGTRenderer.renderEntities(prop.world, 0.0F, pass);
		this.renderBlocks(prop, 0.0F, pass);

		GLHelper.setLightmapMaxBrightness();
		GL11.glPopMatrix();
	}

	private void renderBlocks(RenderProp prop, float par3, int pass) {
		if (prop.glLists == null) {
			prop.glLists = new DisplayList[2];
		}

		NGTUtilClient.getMinecraft().getTextureManager().bindTexture(TextureMap.locationBlocksTexture);

		boolean smoothing = NGTUtilClient.getMinecraft().gameSettings.ambientOcclusion != 0;
		if (smoothing) {
			GL11.glShadeModel(GL11.GL_SMOOTH);
		}
		if (!GLHelper.isValid(prop.glLists[pass])) {
			prop.glLists[pass] = GLHelper.generateGLList();
			GLHelper.startCompile(prop.glLists[pass]);
			NGTRenderer.renderNGTObject(prop.world, prop.ngto, true, prop.mode, pass);
			GLHelper.endCompile();
		} else {
			GLHelper.callList(prop.glLists[pass]);
		}
		if (smoothing) {
			GL11.glShadeModel(GL11.GL_FLAT);
		}
		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);//明るさ戻す
		GLHelper.enableLighting();
		NGTUtilClient.getMinecraft().entityRenderer.enableLightmap((double) par3);
	}

	@SubscribeEvent
	public void onRenderPlayer(RenderPlayerEvent.Specials.Pre event) {
		GL11.glPushMatrix();
		if (event.entityPlayer.isSneaking()) {
			GL11.glRotatef(25.0F, 1.0F, 0.0F, 0.0F);
		}
		GL11.glTranslatef(0.0F, 1.0F, 0.0F);
		this.renderArmor(event.entityPlayer.inventory.armorItemInSlot(2));//Chest
		GL11.glPopMatrix();

		GL11.glPushMatrix();
		event.renderer.modelBipedMain.bipedHead.postRender(0.0625F);
		this.renderArmor(event.entityPlayer.inventory.armorItemInSlot(3));//Helmet
		GL11.glPopMatrix();
	}

	@SubscribeEvent
	public void onRenderLiving(RenderLivingEvent.Specials.Pre event) {
		if (event.renderer instanceof RenderBiped && event.entity instanceof EntityLiving) {
			EntityLiving living = (EntityLiving) event.entity;
			GL11.glPushMatrix();
			/**RenderLivingEntity.doRender()*******************************************/
			float f2 = living.renderYawOffset;
			float f3 = living.rotationYawHead;
			if (living.isRiding() && living.ridingEntity instanceof EntityLivingBase) {
				EntityLivingBase entitylivingbase1 = (EntityLivingBase) living.ridingEntity;
				f2 = entitylivingbase1.renderYawOffset;
				float f4 = MathHelper.wrapAngleTo180_float(f3 - f2);

				if (f4 < -85.0F) {
					f4 = -85.0F;
				} else if (f4 >= 85.0F) {
					f4 = 85.0F;
				}

				f2 = f3 - f4;

				if (f4 * f4 > 2500.0F) {
					f2 += f4 * 0.2F;
				}
			}
			float f13 = living.rotationPitch;
			GL11.glTranslatef((float) event.x, (float) event.y, (float) event.z);
			//rotateCorpse
			GL11.glRotatef(180.0F - f2, 0.0F, 1.0F, 0.0F);
			if (living.deathTime > 0) {
				float f33 = ((float) living.deathTime - 1.0F) / 20.0F * 1.6F;
				f33 = MathHelper.sqrt_float(f33);

				if (f33 > 1.0F) {
					f33 = 1.0F;
				}

				GL11.glRotatef(f33 * 90.0F, 0.0F, 0.0F, 1.0F);
			}
			float f5 = 0.0625F;
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			GL11.glScalef(-1.0F, -1.0F, 1.0F);
			GL11.glTranslatef(0.0F, -24.0F * f5 - 0.0078125F, 0.0F);
			/************************************************************************/

			GL11.glPushMatrix();
			if (event.entity.isSneaking()) {
				GL11.glRotatef(25.0F, 1.0F, 0.0F, 0.0F);
			}
			GL11.glTranslatef(0.0F, 1.0F, 0.0F);
			this.renderArmor(living.func_130225_q(2));//Chest
			GL11.glPopMatrix();

			GL11.glPushMatrix();
			((RenderBiped) event.renderer).modelBipedMain.bipedHead.postRender(0.0625F);
			this.renderArmor(living.func_130225_q(3));//Helmet
			GL11.glPopMatrix();

			GL11.glPopMatrix();
		}
	}

	private void renderArmor(ItemStack item) {
		if (item == null) {
			return;
		}

		if (item.getItem() == MCTE.itemMiniature) {
			float f1 = 0.625F;
			GL11.glScalef(f1, -f1, -f1);
			this.renderItem(ItemRenderType.EQUIPPED, item, "isArmor");
		}
	}

	@SideOnly(Side.CLIENT)
	private class RenderProp {
		public NGTObject ngto;
		public float offsetX, offsetY, offsetZ;
		public float scale;
		public int mode;

		public NGTWorld world;
		public DisplayList[] glLists;
		public float corX, corZ;
		public float scaleInInventory;

		public RenderProp(NGTObject par1, ItemStack item) {
			this.ngto = par1;
			this.world = new NGTWorld(NGTUtil.getClientWorld(), par1);
			this.corX = (float) par1.xSize * 0.5F;
			this.corZ = (float) par1.zSize * 0.5F;
			NBTTagCompound nbt = item.getTagCompound();
			float[] fa = ItemMiniature.getOffset(nbt);
			this.offsetX = fa[0];
			this.offsetY = fa[1];
			this.offsetZ = fa[2];
			this.scale = ItemMiniature.getScale(nbt);
			this.mode = ItemMiniature.getMode(nbt).id;
			int i0 = par1.xSize > par1.ySize ? (par1.xSize > par1.zSize ? par1.xSize : par1.zSize) : (par1.ySize > par1.zSize ? par1.ySize : par1.zSize);
			this.scaleInInventory = 1.0F / (float) i0;
		}
	}
}