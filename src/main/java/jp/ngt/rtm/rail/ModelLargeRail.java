package jp.ngt.rtm.rail;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

@SideOnly(Side.CLIENT)
public class ModelLargeRail extends ModelBase {
	ModelRenderer Shape4;
	ModelRenderer rail1;
	ModelRenderer rail2;

	public ModelLargeRail() {
		textureWidth = 128;
		textureHeight = 16;

		Shape4 = new ModelRenderer(this, 0, 0);
		Shape4.addBox(-17F, -2F, -2F, 34, 2, 4);
		Shape4.setRotationPoint(0F, 0F, 0F);
		Shape4.setTextureSize(128, 64);
		Shape4.mirror = true;
		setRotation(Shape4, 0F, 0F, 0F);
		rail1 = new ModelRenderer(this, 76, 0);
		rail1.addBox(-10F, -4F, -5F, 1, 2, 10);
		rail1.setRotationPoint(0F, 0F, 0F);
		rail1.setTextureSize(128, 16);
		rail1.mirror = true;
		setRotation(rail1, 0F, 0F, 0F);
		rail2 = new ModelRenderer(this, 76, 0);
		rail2.addBox(9F, -4F, -5F, 1, 2, 10);
		rail2.setRotationPoint(0F, 0F, 0F);
		rail2.setTextureSize(128, 16);
		rail2.mirror = true;
		setRotation(rail2, 0F, 0F, 0F);
	}

	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		setRotationAngles(f, f1, f2, f3, f4, f5);
		Shape4.render(f5);
		rail1.render(f5);
		rail2.render(f5);
	}

	private void setRotation(ModelRenderer model, float x, float y, float z) {
		model.rotateAngleX = x;
		model.rotateAngleY = y;
		model.rotateAngleZ = z;
	}

	public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5) {
		super.setRotationAngles(f, f1, f2, f3, f4, f5, null);
	}
}