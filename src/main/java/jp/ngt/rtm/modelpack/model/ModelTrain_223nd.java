package jp.ngt.rtm.modelpack.model;

import jp.ngt.rtm.entity.train.ModelTrainBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

public class ModelTrain_223nd extends ModelTrainBase {
    ModelRenderer yuka;
    ModelRenderer yukashita;
    ModelRenderer migi1;
    ModelRenderer migi2;
    ModelRenderer migi3;
    ModelRenderer hidari1;
    ModelRenderer hidari2;
    ModelRenderer hidari3;
    ModelRenderer yane;
    ModelRenderer usiro1;
    ModelRenderer usiro2;
    ModelRenderer usiro3;
    ModelRenderer mae1;
    ModelRenderer mae2;
    ModelRenderer mae3;
    ModelRenderer ac1;
    ModelRenderer ac2;
    ModelRenderer horo1;
    ModelRenderer horo2;
    ModelRenderer panta1;
    ModelRenderer panta2;
    ModelRenderer panta3;
    ModelRenderer panta4;
    ModelRenderer panta5;
    ModelRenderer panta6;

    public ModelTrain_223nd() {
        super();
    }

    public ModelTrain_223nd(int width, int height) {
        super(width, height);
    }

    @Override
    public void init() {
        textureWidth = 1024;
        textureHeight = 1024;

        yuka = new ModelRenderer(this, 0, 0);
        yuka.addBox(-160F, -1F, -22F, 320, 1, 44);
        yuka.setRotationPoint(0F, 0F, 0F);
        yuka.setTextureSize(1024, 1024);
        yuka.mirror = true;
        setRotation(yuka, 0F, 0F, 0F);
        yukashita = new ModelRenderer(this, 0, 45);
        yukashita.addBox(-159F, 0F, -20F, 318, 14, 40);
        yukashita.setRotationPoint(0F, 0F, 0F);
        yukashita.setTextureSize(1024, 1024);
        yukashita.mirror = true;
        setRotation(yukashita, 0F, 0F, 0F);
        migi1 = new ModelRenderer(this, 0, 100);
        migi1.addBox(-160F, -8F, -1F, 320, 8, 1);
        migi1.setRotationPoint(0F, -43F, 24F);
        migi1.setTextureSize(1024, 1024);
        migi1.mirror = true;
        setRotation(migi1, 0.7853982F, 0F, 0F);
        migi2 = new ModelRenderer(this, 0, 112);
        migi2.addBox(-160F, -30F, -1F, 320, 30, 1);
        migi2.setRotationPoint(0F, -13F, 24F);
        migi2.setTextureSize(1024, 1024);
        migi2.mirror = true;
        setRotation(migi2, 0F, 0F, 0F);
        migi3 = new ModelRenderer(this, 0, 148);
        migi3.addBox(-160F, 0F, -1F, 320, 13, 1);
        migi3.setRotationPoint(0F, -13F, 24F);
        migi3.setTextureSize(1024, 1024);
        migi3.mirror = true;
        setRotation(migi3, -0.1570796F, 0F, 0F);
        hidari1 = new ModelRenderer(this, 0, 168);
        hidari1.addBox(-160F, -8F, 0F, 320, 8, 1);
        hidari1.setRotationPoint(0F, -43F, -24F);
        hidari1.setTextureSize(1024, 1024);
        hidari1.mirror = true;
        setRotation(hidari1, -0.7853982F, 0F, 0F);
        hidari2 = new ModelRenderer(this, 0, 182);
        hidari2.addBox(-160F, -30F, -1F, 320, 30, 1);
        hidari2.setRotationPoint(0F, -13F, -23F);
        hidari2.setTextureSize(1024, 1024);
        hidari2.mirror = true;
        setRotation(hidari2, 0F, 0F, 0F);
        hidari3 = new ModelRenderer(this, 0, 218);
        hidari3.addBox(-160F, 0F, 0F, 320, 13, 1);
        hidari3.setRotationPoint(0F, -13F, -24F);
        hidari3.setTextureSize(1024, 1024);
        hidari3.mirror = true;
        setRotation(hidari3, 0.1570796F, 0F, 0F);
        yane = new ModelRenderer(this, 0, 240);
        yane.addBox(-160F, -1F, -19F, 320, 1, 38);
        yane.setRotationPoint(0F, -47F, 0F);
        yane.setTextureSize(1024, 1024);
        yane.mirror = true;
        setRotation(yane, 0F, 0F, 0F);
        usiro1 = new ModelRenderer(this, 0, 282);
        usiro1.addBox(-1F, -4F, -19F, 1, 4, 38);
        usiro1.setRotationPoint(160F, -44F, 0F);
        usiro1.setTextureSize(1024, 1024);
        usiro1.mirror = true;
        setRotation(usiro1, 0F, 0F, 0F);
        usiro2 = new ModelRenderer(this, 80, 282);
        usiro2.addBox(-1F, -36F, -23F, 1, 36, 46);
        usiro2.setRotationPoint(160F, -8F, 0F);
        usiro2.setTextureSize(1024, 1024);
        usiro2.mirror = true;
        setRotation(usiro2, 0F, 0F, 0F);
        usiro3 = new ModelRenderer(this, 180, 282);
        usiro3.addBox(-1F, -8F, -22F, 1, 8, 44);
        usiro3.setRotationPoint(160F, 0F, 0F);
        usiro3.setTextureSize(1024, 1024);
        usiro3.mirror = true;
        setRotation(usiro3, 0F, 0F, 0F);
        mae1 = new ModelRenderer(this, 0, 370);
        mae1.addBox(0F, -4F, -19F, 1, 4, 38);
        mae1.setRotationPoint(-160F, -44F, 0F);
        mae1.setTextureSize(1024, 1024);
        mae1.mirror = true;
        setRotation(mae1, 0F, 0F, 0F);
        mae2 = new ModelRenderer(this, 80, 370);
        mae2.addBox(0F, -36F, -23F, 1, 36, 46);
        mae2.setRotationPoint(-160F, -8F, 0F);
        mae2.setTextureSize(1024, 1024);
        mae2.mirror = true;
        setRotation(mae2, 0F, 0F, 0F);
        mae3 = new ModelRenderer(this, 180, 370);
        mae3.addBox(0F, -8F, -22F, 1, 8, 44);
        mae3.setRotationPoint(-160F, 0F, 0F);
        mae3.setTextureSize(1024, 1024);
        mae3.mirror = true;
        setRotation(mae3, 0F, 0F, 0F);
        ac1 = new ModelRenderer(this, 280, 282);
        ac1.addBox(-24F, -6F, -12F, 48, 6, 24);
        ac1.setRotationPoint(-60F, -48F, 0F);
        ac1.setTextureSize(1024, 1024);
        ac1.mirror = true;
        setRotation(ac1, 0F, 0F, 0F);
        ac2 = new ModelRenderer(this, 280, 282);
        ac2.addBox(-24F, -6F, -12F, 48, 6, 24);
        ac2.setRotationPoint(60F, -48F, 0F);
        ac2.setTextureSize(1024, 1024);
        ac2.mirror = true;
        setRotation(ac2, 0F, 0F, 0F);
        horo1 = new ModelRenderer(this, 740, 0);
        horo1.addBox(-5F, -38F, -8F, 5, 38, 16);
        horo1.setRotationPoint(-160F, -1F, 0F);
        horo1.setTextureSize(1024, 1024);
        horo1.mirror = true;
        setRotation(horo1, 0F, 0F, 0F);
        horo2 = new ModelRenderer(this, 740, 0);
        horo2.addBox(0F, -38F, -8F, 5, 38, 16);
        horo2.setRotationPoint(160F, -1F, 0F);
        horo2.setTextureSize(1024, 1024);
        horo2.mirror = true;
        setRotation(horo2, 0F, 0F, 0F);
        panta1 = new ModelRenderer(this, 280, 370);
        panta1.addBox(-12F, -3F, -8F, 24, 3, 16);
        panta1.setRotationPoint(-120F, -48F, 0F);
        panta1.setTextureSize(1024, 1024);
        panta1.mirror = true;
        setRotation(panta1, 0F, 0F, 0F);
        panta2 = new ModelRenderer(this, 360, 370);
        panta2.addBox(-2F, 0F, -14F, 4, 2, 28);
        panta2.setRotationPoint(-120F, -74F, 0F);
        panta2.setTextureSize(1024, 1024);
        panta2.mirror = true;
        setRotation(panta2, 0F, 0F, 0F);
        panta3 = new ModelRenderer(this, 380, 420);
        panta3.addBox(0F, 0F, -8F, 18, 1, 16);
        panta3.setRotationPoint(-120F, -74F, 0F);
        panta3.setTextureSize(1024, 1024);
        panta3.mirror = true;
        setRotation(panta3, 0F, 0F, 0.5235988F);
        panta4 = new ModelRenderer(this, 380, 420);
        panta4.addBox(-18F, 0F, -8F, 18, 1, 16);
        panta4.setRotationPoint(-120F, -74F, 0F);
        panta4.setTextureSize(1024, 1024);
        panta4.mirror = true;
        setRotation(panta4, 0F, 0F, -0.5235988F);
        panta5 = new ModelRenderer(this, 280, 420);
        panta5.addBox(-10F, 0F, -8F, 28, 1, 16);
        panta5.setRotationPoint(-120F, -56F, 0F);
        panta5.setTextureSize(1024, 1024);
        panta5.mirror = true;
        setRotation(panta5, 0F, 0F, -0.5235988F);
        panta6 = new ModelRenderer(this, 280, 420);
        panta6.addBox(-18F, 0F, -8F, 28, 1, 16);
        panta6.setRotationPoint(-120F, -56F, 0F);
        panta6.setTextureSize(1024, 1024);
        panta6.mirror = true;
        setRotation(panta6, 0F, 0F, 0.5235988F);
    }

    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        GL11.glPushMatrix();
        GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
        super.render(entity, f, f1, f2, f3, f4, f5);
        this.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        yuka.render(f5);
        yukashita.render(f5);
        migi1.render(f5);
        migi2.render(f5);
        migi3.render(f5);
        hidari1.render(f5);
        hidari2.render(f5);
        hidari3.render(f5);
        yane.render(f5);
        usiro1.render(f5);
        usiro2.render(f5);
        usiro3.render(f5);
        mae1.render(f5);
        mae2.render(f5);
        mae3.render(f5);
        ac1.render(f5);
        ac2.render(f5);
        horo1.render(f5);
        horo2.render(f5);
        panta1.render(f5);
        panta2.render(f5);
        panta3.render(f5);
        panta4.render(f5);
        panta5.render(f5);
        panta6.render(f5);
        GL11.glPopMatrix();
    }

    private void setRotation(ModelRenderer model, float x, float y, float z) {
        model.rotateAngleX = x;
        model.rotateAngleY = y;
        model.rotateAngleZ = z;
    }

    public void setRotationAngles(float f, float f1, float f2, float f3, float f4, float f5, Entity entity) {
        super.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
    }
}