package jp.ngt.rtm.modelpack.model;

import jp.ngt.rtm.entity.train.ModelTrainBase;
import net.minecraft.client.model.ModelRenderer;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

public class ModelTrain_kiha600 extends ModelTrainBase {
    public ModelRenderer Yukasita;
    public ModelRenderer Yukasita2;
    //public ModelRenderer Renketu1;
    //public ModelRenderer Renketu2;
    public ModelRenderer Yuka;
    public ModelRenderer Syatai;
    public ModelRenderer Yane1;
    public ModelRenderer Yane2;
    public ModelRenderer Yane3;


    public ModelTrain_kiha600() {
        super();
    }

    public ModelTrain_kiha600(int width, int height) {
        super(width, height);
    }

    @Override
    public void init() {
        textureWidth = 1024;
        textureHeight = 1024;

        this.Yukasita = new ModelRenderer(this, 0, 208);
        this.Yukasita.addBox(-158F, 0F, -21F, 316, 16, 42);
        this.Yukasita.setRotationPoint(0F, 0F, 0F);
        this.Yukasita.mirror = true;
        setRotation(Yukasita, 0F, 0F, 0F);

        this.Yukasita2 = new ModelRenderer(this, 0, 351);
        this.Yukasita2.addBox(-157F, 0F, -20F, 314, 10, 40);
        this.Yukasita2.setRotationPoint(0F, 0F, 0F);
        this.Yukasita2.mirror = true;
        setRotation(Yukasita2, 0F, 0F, 0F);

    	/*this.Renketu1 = new ModelRenderer(this, 0, 0);
    	this.Renketu1.addBox(-164F, 0F, -3F, 8, 4, 6);
    	this.Renketu1.setRotationPoint(0F, 0F, 0F);
    	this.Renketu1.mirror = true;
    	setRotation(Renketu1, 0F, 0F, 0F);

    	this.Renketu2 = new ModelRenderer(this, 0, 0);
    	this.Renketu2.addBox(156F, 0F, -3F, 8, 4, 6);
    	this.Renketu2.setRotationPoint(0F, 0F, 0F);
    	this.Renketu2.mirror = true;
    	setRotation(Renketu2, 0F, 0F, 0F);*/

        this.Yuka = new ModelRenderer(this, 0, 266);
        this.Yuka.addBox(-159F, -41F, -22F, 318, 41, 44);
        this.Yuka.setRotationPoint(0F, 0F, 0F);
        Yuka.mirror = true;
        setRotation(Yuka, 0F, 0F, 0F);

        this.Syatai = new ModelRenderer(this, 0, 0);
        this.Syatai.addBox(-160F, -41F, -23F, 320, 41, 46);
        this.Syatai.setRotationPoint(0F, 0F, 0F);
        this.Syatai.mirror = true;
        setRotation(Syatai, 0F, 0F, 0F);

        this.Yane1 = new ModelRenderer(this, 0, 87);
        this.Yane1.addBox(-160F, -42F, -22F, 320, 1, 44);
        this.Yane1.setRotationPoint(0F, 0F, 0F);
        this.Yane1.mirror = true;
        setRotation(Yane1, 0F, 0F, 0F);

        this.Yane2 = new ModelRenderer(this, 0, 132);
        this.Yane2.addBox(-160F, -43F, -20F, 320, 1, 40);
        this.Yane2.setRotationPoint(0F, 0F, 0F);
        this.Yane2.mirror = true;
        setRotation(Yane2, 0F, 0F, 0F);

        this.Yane3 = new ModelRenderer(this, 0, 173);
        this.Yane3.addBox(-160F, -44F, -17F, 320, 1, 34);
        this.Yane3.setRotationPoint(0F, 0F, 0F);
        this.Yane3.mirror = true;
        setRotation(Yane3, 0F, 0F, 0F);
    }

    public void render(Entity entity, float f, float f1, float f2, float f3, float f4, float f5) {
        GL11.glPushMatrix();
        GL11.glRotatef(90.0F, 0.0F, 1.0F, 0.0F);//この部分は通常のModelでは必要ありません（このModelはX方向が正面なので90度回転させています）
        super.render(entity, f, f1, f2, f3, f4, f5);
        this.setRotationAngles(f, f1, f2, f3, f4, f5, entity);
        this.Yukasita.render(f5);
        this.Yukasita2.render(f5);
        //this.Renketu1.render(f5);
        //this.Renketu2.render(f5);
        this.Yuka.render(f5);
        this.Syatai.render(f5);
        this.Yane1.render(f5);
        this.Yane2.render(f5);
        this.Yane3.render(f5);
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