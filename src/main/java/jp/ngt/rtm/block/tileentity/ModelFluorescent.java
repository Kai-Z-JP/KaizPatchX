package jp.ngt.rtm.block.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.model.ModelBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

@SideOnly(Side.CLIENT)
public class ModelFluorescent extends ModelBase {
	ModelRenderer body;
	ModelRenderer cover;
	ModelRenderer Shape3;
	ModelRenderer Shape4;
	ModelRenderer Shape5;
	ModelRenderer Shape6;

	public ModelFluorescent() {
		textureWidth = 64;
		textureHeight = 32;

		body = new ModelRenderer(this, 0, 0);
		body.addBox(-7F, -1F, -1F, 14, 2, 2);
		body.setRotationPoint(0F, 0F, 0F);
		body.mirror = true;
		setRotation(body, 0F, 0F, 0F);

		cover = new ModelRenderer(this, 0, 4);
		cover.addBox(-7F, -2F, -2F, 14, 4, 4);
		cover.setRotationPoint(0F, 0F, 0F);
		cover.mirror = true;
		setRotation(cover, 0F, 0F, 0F);

		Shape3 = new ModelRenderer(this, 0, 12);
		Shape3.addBox(-8F, -2F, -2F, 1, 4, 4);
		Shape3.setRotationPoint(0F, 0F, 0F);
		Shape3.mirror = true;
		setRotation(Shape3, 0F, 0F, 0F);

		Shape4 = new ModelRenderer(this, 0, 12);
		Shape4.addBox(7F, -2F, -2F, 1, 4, 4);
		Shape4.setRotationPoint(0F, 0F, 0F);
		Shape4.mirror = true;
		setRotation(Shape4, 0F, 0F, 0F);

		Shape5 = new ModelRenderer(this, 0, 16);
		Shape5.addBox(-8F, -1F, -1F, 1, 2, 2);
		Shape5.setRotationPoint(0F, 0F, 0F);
		Shape5.mirror = true;
		setRotation(Shape5, 0F, 0F, 0F);

		Shape6 = new ModelRenderer(this, 0, 16);
		Shape6.addBox(7F, -1F, -1F, 1, 2, 2);
		Shape6.setRotationPoint(0F, 0F, 0F);
		Shape6.mirror = true;
		setRotation(Shape6, 0F, 0F, 0F);
	}

	public void render(int meta, int pass, float f5) {
		super.render(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, f5);
		this.setRotationAngles(0.0F, 0.0F, 0.0F, 0.0F, 0.0F, f5);

		if (pass == 1) {
			this.body.render(f5);
			if (meta == 4) {
				this.cover.render(f5);
			}
		} else {
			if (meta == 4) {
				this.Shape3.render(f5);
				this.Shape4.render(f5);
			} else {
				this.Shape5.render(f5);
				this.Shape6.render(f5);
			}
		}
	}

	@Override
	public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
		super.render(entity, f, f1, f2, f3, f4, f5);
		this.setRotationAngles(f, f1, f2, f3, f4, f5);
		this.render(4, 0, f5);
		this.render(4, 1, f5);
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