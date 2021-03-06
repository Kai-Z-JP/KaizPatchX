package jp.ngt.rtm.modelpack.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.entity.train.ModelBogieBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;

@SideOnly(Side.CLIENT)
public class ModelBogie extends ModelBogieBase {
    ModelRenderer Shape9;
    ModelRenderer Shape10;
    ModelRenderer Shape11;
    ModelRenderer baL;
    ModelRenderer baR;
    ModelRenderer bolster;
    ModelRenderer coupling1;
    ModelRenderer coupling2;
    ModelRenderer wslf1;
    ModelRenderer wslf2;
    ModelRenderer wslf3;
    ModelRenderer wslf4;
    ModelRenderer wblf1;
    ModelRenderer wblf2;
    ModelRenderer wblf3;
    ModelRenderer wblf4;
    ModelRenderer wblf5;
    ModelRenderer wslb1;
    ModelRenderer wslb2;
    ModelRenderer wslb3;
    ModelRenderer wslb4;
    ModelRenderer wblb1;
    ModelRenderer wblb2;
    ModelRenderer wblb3;
    ModelRenderer wblb4;
    ModelRenderer wblb5;
    ModelRenderer wsrf1;
    ModelRenderer wsrf2;
    ModelRenderer wsrf3;
    ModelRenderer wsrf4;
    ModelRenderer wbrf1;
    ModelRenderer wbrf2;
    ModelRenderer wbrf3;
    ModelRenderer wbrf4;
    ModelRenderer wbrf5;
    ModelRenderer wsrb1;
    ModelRenderer wsrb2;
    ModelRenderer wsrb3;
    ModelRenderer wsrb4;
    ModelRenderer wbrb1;
    ModelRenderer wbrb2;
    ModelRenderer wbrb3;
    ModelRenderer wbrb4;
    ModelRenderer wbrb5;

    public ModelBogie() {
        super();
    }

    public ModelBogie(int width, int height) {
        super(width, height);
    }

    @Override
    public void init() {
        textureWidth = 256;
        textureHeight = 256;

        Shape9 = new ModelRenderer(this, 0, 0);
        Shape9.addBox(-8F, 0F, -24F, 16, 9, 48);
        Shape9.setRotationPoint(0F, 4F, 0F);
        Shape9.setTextureSize(256, 256);
        Shape9.mirror = true;
        setRotation(Shape9, 0F, 0F, 0F);
        Shape10 = new ModelRenderer(this, 0, 60);
        Shape10.addBox(0F, 0F, -25F, 4, 9, 50);
        Shape10.setRotationPoint(10F, 4F, 0F);
        Shape10.setTextureSize(256, 256);
        Shape10.mirror = true;
        setRotation(Shape10, 0F, 0F, 0F);
        Shape11 = new ModelRenderer(this, 0, 60);
        Shape11.addBox(-4F, 0F, -25F, 4, 9, 50);
        Shape11.setRotationPoint(-10F, 4F, 0F);
        Shape11.setTextureSize(256, 256);
        Shape11.mirror = true;
        setRotation(Shape11, 0F, 0F, 0F);
        baL = new ModelRenderer(this, 130, 20);
        baL.addBox(-5F, 0F, -7F, 5, 12, 14);
        baL.setRotationPoint(19F, 2F, 0F);
        baL.setTextureSize(256, 256);
        baL.mirror = true;
        setRotation(baL, 0F, 0F, 0F);
        baR = new ModelRenderer(this, 130, 20);
        baR.addBox(0F, 0F, -7F, 5, 12, 14);
        baR.setRotationPoint(-19F, 2F, 0F);
        baR.setTextureSize(256, 256);
        baR.mirror = true;
        setRotation(baR, 0F, 0F, 0F);
        bolster = new ModelRenderer(this, 130, 0);
        bolster.addBox(-20F, 0F, -7F, 40, 2, 14);
        bolster.setRotationPoint(0F, 0F, 0F);
        bolster.setTextureSize(256, 256);
        bolster.mirror = true;
        setRotation(bolster, 0F, 0F, 0F);
        coupling1 = new ModelRenderer(this, 170, 20);
        coupling1.addBox(-1F, 1F, -8F, 2, 2, 8);
        coupling1.setRotationPoint(0F, 0F, -37F);
        coupling1.setTextureSize(256, 256);
        coupling1.mirror = true;
        setRotation(coupling1, 0F, 0F, 0F);
        coupling2 = new ModelRenderer(this, 170, 32);
        coupling2.addBox(-3F, 0F, -12F, 6, 4, 4);
        coupling2.setRotationPoint(0F, 0F, -37F);
        coupling2.setTextureSize(256, 256);
        coupling2.mirror = true;
        setRotation(coupling2, 0F, 0F, 0F);
        wslf1 = new ModelRenderer(this, 0, 0);
        wslf1.addBox(-1F, -6F, -2F, 1, 12, 4);
        wslf1.setRotationPoint(10F, 10F, -18F);
        wslf1.setTextureSize(256, 256);
        wslf1.mirror = true;
        setRotation(wslf1, 0F, 0F, 0F);
        wslf2 = new ModelRenderer(this, 0, 0);
        wslf2.addBox(-1F, -5F, -4F, 1, 10, 8);
        wslf2.setRotationPoint(10F, 10F, -18F);
        wslf2.setTextureSize(256, 256);
        wslf2.mirror = true;
        setRotation(wslf2, 0F, 0F, 0F);
        wslf3 = new ModelRenderer(this, 0, 0);
        wslf3.addBox(-1F, -4F, -5F, 1, 8, 10);
        wslf3.setRotationPoint(10F, 10F, -18F);
        wslf3.setTextureSize(256, 256);
        wslf3.mirror = true;
        setRotation(wslf3, 0F, 0F, 0F);
        wslf4 = new ModelRenderer(this, 0, 0);
        wslf4.addBox(-1F, -2F, -6F, 1, 4, 12);
        wslf4.setRotationPoint(10F, 10F, -18F);
        wslf4.setTextureSize(256, 256);
        wslf4.mirror = true;
        setRotation(wslf4, 0F, 0F, 0F);
        wblf1 = new ModelRenderer(this, 0, 0);
        wblf1.addBox(-1F, -7F, -2F, 1, 14, 4);
        wblf1.setRotationPoint(9F, 10F, -18F);
        wblf1.setTextureSize(256, 256);
        wblf1.mirror = true;
        setRotation(wblf1, 0F, 0F, 0F);
        wblf2 = new ModelRenderer(this, 0, 0);
        wblf2.addBox(-1F, -6F, -4F, 1, 12, 8);
        wblf2.setRotationPoint(9F, 10F, -18F);
        wblf2.setTextureSize(256, 256);
        wblf2.mirror = true;
        setRotation(wblf2, 0F, 0F, 0F);
        wblf3 = new ModelRenderer(this, 0, 0);
        wblf3.addBox(-1F, -5F, -5F, 1, 10, 10);
        wblf3.setRotationPoint(9F, 10F, -18F);
        wblf3.setTextureSize(256, 256);
        wblf3.mirror = true;
        setRotation(wblf3, 0F, 0F, 0F);
        wblf4 = new ModelRenderer(this, 0, 0);
        wblf4.addBox(-1F, -4F, -6F, 1, 8, 12);
        wblf4.setRotationPoint(9F, 10F, -18F);
        wblf4.setTextureSize(256, 256);
        wblf4.mirror = true;
        setRotation(wblf4, 0F, 0F, 0F);
        wblf5 = new ModelRenderer(this, 0, 0);
        wblf5.addBox(-1F, -2F, -7F, 1, 4, 14);
        wblf5.setRotationPoint(9F, 10F, -18F);
        wblf5.setTextureSize(256, 256);
        wblf5.mirror = true;
        setRotation(wblf5, 0F, 0F, 0F);
        wslb1 = new ModelRenderer(this, 0, 0);
        wslb1.addBox(-1F, -6F, -2F, 1, 12, 4);
        wslb1.setRotationPoint(10F, 10F, 18F);
        wslb1.setTextureSize(256, 256);
        wslb1.mirror = true;
        setRotation(wslb1, 0F, 0F, 0F);
        wslb2 = new ModelRenderer(this, 0, 0);
        wslb2.addBox(-1F, -5F, -4F, 1, 10, 8);
        wslb2.setRotationPoint(10F, 10F, 18F);
        wslb2.setTextureSize(256, 256);
        wslb2.mirror = true;
        setRotation(wslb2, 0F, 0F, 0F);
        wslb3 = new ModelRenderer(this, 0, 0);
        wslb3.addBox(-1F, -4F, -5F, 1, 8, 10);
        wslb3.setRotationPoint(10F, 10F, 18F);
        wslb3.setTextureSize(256, 256);
        wslb3.mirror = true;
        setRotation(wslb3, 0F, 0F, 0F);
        wslb4 = new ModelRenderer(this, 0, 0);
        wslb4.addBox(-1F, -2F, -6F, 1, 4, 12);
        wslb4.setRotationPoint(10F, 10F, 18F);
        wslb4.setTextureSize(256, 256);
        wslb4.mirror = true;
        setRotation(wslb4, 0F, 0F, 0F);
        wblb1 = new ModelRenderer(this, 0, 0);
        wblb1.addBox(-1F, -7F, -2F, 1, 14, 4);
        wblb1.setRotationPoint(9F, 10F, 18F);
        wblb1.setTextureSize(256, 256);
        wblb1.mirror = true;
        setRotation(wblb1, 0F, 0F, 0F);
        wblb2 = new ModelRenderer(this, 0, 0);
        wblb2.addBox(-1F, -6F, -4F, 1, 12, 8);
        wblb2.setRotationPoint(9F, 10F, 18F);
        wblb2.setTextureSize(256, 256);
        wblb2.mirror = true;
        setRotation(wblb2, 0F, 0F, 0F);
        wblb3 = new ModelRenderer(this, 0, 0);
        wblb3.addBox(-1F, -5F, -5F, 1, 10, 10);
        wblb3.setRotationPoint(9F, 10F, 18F);
        wblb3.setTextureSize(256, 256);
        wblb3.mirror = true;
        setRotation(wblb3, 0F, 0F, 0F);
        wblb4 = new ModelRenderer(this, 0, 0);
        wblb4.addBox(-1F, -4F, -6F, 1, 8, 12);
        wblb4.setRotationPoint(9F, 10F, 18F);
        wblb4.setTextureSize(256, 256);
        wblb4.mirror = true;
        setRotation(wblb4, 0F, 0F, 0F);
        wblb5 = new ModelRenderer(this, 0, 0);
        wblb5.addBox(-1F, -2F, -7F, 1, 4, 14);
        wblb5.setRotationPoint(9F, 10F, 18F);
        wblb5.setTextureSize(256, 256);
        wblb5.mirror = true;
        setRotation(wblb5, 0F, 0F, 0F);
        wsrf1 = new ModelRenderer(this, 0, 0);
        wsrf1.addBox(0F, -6F, -2F, 1, 12, 4);
        wsrf1.setRotationPoint(-10F, 10F, -18F);
        wsrf1.setTextureSize(256, 256);
        wsrf1.mirror = true;
        setRotation(wsrf1, 0F, 0F, 0F);
        wsrf2 = new ModelRenderer(this, 0, 0);
        wsrf2.addBox(0F, -5F, -4F, 1, 10, 8);
        wsrf2.setRotationPoint(-10F, 10F, -18F);
        wsrf2.setTextureSize(256, 256);
        wsrf2.mirror = true;
        setRotation(wsrf2, 0F, 0F, 0F);
        wsrf3 = new ModelRenderer(this, 0, 0);
        wsrf3.addBox(0F, -4F, -5F, 1, 8, 10);
        wsrf3.setRotationPoint(-10F, 10F, -18F);
        wsrf3.setTextureSize(256, 256);
        wsrf3.mirror = true;
        setRotation(wsrf3, 0F, 0F, 0F);
        wsrf4 = new ModelRenderer(this, 0, 0);
        wsrf4.addBox(0F, -2F, -6F, 1, 4, 12);
        wsrf4.setRotationPoint(-10F, 10F, -18F);
        wsrf4.setTextureSize(256, 256);
        wsrf4.mirror = true;
        setRotation(wsrf4, 0F, 0F, 0F);
        wbrf1 = new ModelRenderer(this, 0, 0);
        wbrf1.addBox(0F, -7F, -2F, 1, 14, 4);
        wbrf1.setRotationPoint(-9F, 10F, -18F);
        wbrf1.setTextureSize(256, 256);
        wbrf1.mirror = true;
        setRotation(wbrf1, 0F, 0F, 0F);
        wbrf2 = new ModelRenderer(this, 0, 0);
        wbrf2.addBox(0F, -6F, -4F, 1, 12, 8);
        wbrf2.setRotationPoint(-9F, 10F, -18F);
        wbrf2.setTextureSize(256, 256);
        wbrf2.mirror = true;
        setRotation(wbrf2, 0F, 0F, 0F);
        wbrf3 = new ModelRenderer(this, 0, 0);
        wbrf3.addBox(0F, -5F, -5F, 1, 10, 10);
        wbrf3.setRotationPoint(-9F, 10F, -18F);
        wbrf3.setTextureSize(256, 256);
        wbrf3.mirror = true;
        setRotation(wbrf3, 0F, 0F, 0F);
        wbrf4 = new ModelRenderer(this, 0, 0);
        wbrf4.addBox(0F, -4F, -6F, 1, 8, 12);
        wbrf4.setRotationPoint(-9F, 10F, -18F);
        wbrf4.setTextureSize(256, 256);
        wbrf4.mirror = true;
        setRotation(wbrf4, 0F, 0F, 0F);
        wbrf5 = new ModelRenderer(this, 0, 0);
        wbrf5.addBox(0F, -2F, -7F, 1, 4, 14);
        wbrf5.setRotationPoint(-9F, 10F, -18F);
        wbrf5.setTextureSize(256, 256);
        wbrf5.mirror = true;
        setRotation(wbrf5, 0F, 0F, 0F);
        wsrb1 = new ModelRenderer(this, 0, 0);
        wsrb1.addBox(0F, -6F, -2F, 1, 12, 4);
        wsrb1.setRotationPoint(-10F, 10F, 18F);
        wsrb1.setTextureSize(256, 256);
        wsrb1.mirror = true;
        setRotation(wsrb1, 0F, 0F, 0F);
        wsrb2 = new ModelRenderer(this, 0, 0);
        wsrb2.addBox(0F, -5F, -4F, 1, 10, 8);
        wsrb2.setRotationPoint(-10F, 10F, 18F);
        wsrb2.setTextureSize(256, 256);
        wsrb2.mirror = true;
        setRotation(wsrb2, 0F, 0F, 0F);
        wsrb3 = new ModelRenderer(this, 0, 0);
        wsrb3.addBox(0F, -4F, -5F, 1, 8, 10);
        wsrb3.setRotationPoint(-10F, 10F, 18F);
        wsrb3.setTextureSize(256, 256);
        wsrb3.mirror = true;
        setRotation(wsrb3, 0F, 0F, 0F);
        wsrb4 = new ModelRenderer(this, 0, 0);
        wsrb4.addBox(0F, -2F, -6F, 1, 4, 12);
        wsrb4.setRotationPoint(-10F, 10F, 18F);
        wsrb4.setTextureSize(256, 256);
        wsrb4.mirror = true;
        setRotation(wsrb4, 0F, 0F, 0F);
        wbrb1 = new ModelRenderer(this, 0, 0);
        wbrb1.addBox(0F, -7F, -2F, 1, 14, 4);
        wbrb1.setRotationPoint(-9F, 10F, 18F);
        wbrb1.setTextureSize(256, 256);
        wbrb1.mirror = true;
        setRotation(wbrb1, 0F, 0F, 0F);
        wbrb2 = new ModelRenderer(this, 0, 0);
        wbrb2.addBox(0F, -6F, -4F, 1, 12, 8);
        wbrb2.setRotationPoint(-9F, 10F, 18F);
        wbrb2.setTextureSize(256, 256);
        wbrb2.mirror = true;
        setRotation(wbrb2, 0F, 0F, 0F);
        wbrb3 = new ModelRenderer(this, 0, 0);
        wbrb3.addBox(0F, -5F, -5F, 1, 10, 10);
        wbrb3.setRotationPoint(-9F, 10F, 18F);
        wbrb3.setTextureSize(256, 256);
        wbrb3.mirror = true;
        setRotation(wbrb3, 0F, 0F, 0F);
        wbrb4 = new ModelRenderer(this, 0, 0);
        wbrb4.addBox(0F, -4F, -6F, 1, 8, 12);
        wbrb4.setRotationPoint(-9F, 10F, 18F);
        wbrb4.setTextureSize(256, 256);
        wbrb4.mirror = true;
        setRotation(wbrb4, 0F, 0F, 0F);
        wbrb5 = new ModelRenderer(this, 0, 0);
        wbrb5.addBox(0F, -2F, -7F, 1, 4, 14);
        wbrb5.setRotationPoint(-9F, 10F, 18F);
        wbrb5.setTextureSize(256, 256);
        wbrb5.mirror = true;
        setRotation(wbrb5, 0F, 0F, 0F);
    }

    @Override
    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        super.render(entity, f, f1, f2, f3, f4, f5);
        setRotationAngles(f, f1, f2, f3, f4, f5);
        Shape9.render(f5);
        Shape10.render(f5);
        Shape11.render(f5);
        baL.render(f5);
        baR.render(f5);
        bolster.render(f5);
        coupling1.render(f5);
        coupling2.render(f5);
        wslf1.render(f5);
        wslf2.render(f5);
        wslf3.render(f5);
        wslf4.render(f5);
        wblf1.render(f5);
        wblf2.render(f5);
        wblf3.render(f5);
        wblf4.render(f5);
        wblf5.render(f5);
        wslb1.render(f5);
        wslb2.render(f5);
        wslb3.render(f5);
        wslb4.render(f5);
        wblb1.render(f5);
        wblb2.render(f5);
        wblb3.render(f5);
        wblb4.render(f5);
        wblb5.render(f5);
        wsrf1.render(f5);
        wsrf2.render(f5);
        wsrf3.render(f5);
        wsrf4.render(f5);
        wbrf1.render(f5);
        wbrf2.render(f5);
        wbrf3.render(f5);
        wbrf4.render(f5);
        wbrf5.render(f5);
        wsrb1.render(f5);
        wsrb2.render(f5);
        wsrb3.render(f5);
        wsrb4.render(f5);
        wbrb1.render(f5);
        wbrb2.render(f5);
        wbrb3.render(f5);
        wbrb4.render(f5);
        wbrb5.render(f5);
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