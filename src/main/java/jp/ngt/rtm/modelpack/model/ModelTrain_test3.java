package jp.ngt.rtm.modelpack.model;

import jp.ngt.rtm.entity.train.ModelTrainBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

public class ModelTrain_test3 extends ModelTrainBase {
    public ModelRenderer body;

    public ModelTrain_test3() {
        super();
    }

    public ModelTrain_test3(int width, int height) {
        super(width, height);
    }

    @Override
    public void init() {
        textureWidth = 1024;
        textureHeight = 1024;

        this.body = new ModelRenderer(this, 0, 0);
        this.body.addBox(-160F, -41F, -23F, 320, 41, 46);
        this.body.setRotationPoint(0F, 0F, 0F);
        this.body.mirror = true;
        setRotation(body, 0F, 0F, 0F);
    }

    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);
        super.render(entity, f, f1, f2, f3, f4, f5);
        this.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        this.body.render(f5);
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