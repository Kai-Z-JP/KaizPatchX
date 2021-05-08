package jp.ngt.rtm.modelpack.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.io.FileType;
import jp.ngt.ngtlib.renderer.model.GroupObject;
import jp.ngt.ngtlib.renderer.model.IModelNGT;
import jp.ngt.ngtlib.renderer.model.Material;
import net.minecraft.client.model.ModelChest;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class ModelContainer_BigChest implements IModelNGT {
    private final ModelChest model = new ModelChest();

    @Override
    public void renderAll(boolean smoothing) {
        GL11.glPushMatrix();
        GL11.glScalef(1.0F, -1.0F, -1.0F);
        GL11.glTranslatef(-1.5F, -3.0F, -1.5F);
        float scale = 3.0F;
        GL11.glScalef(scale, scale, scale);
        this.model.renderAll();
        GL11.glPopMatrix();
    }

    @Override
    public void renderOnly(boolean smoothing, String... groupNames) {
    }

    @Override
    public void renderPart(boolean smoothing, String partName) {
    }

    @Override
    public int getDrawMode() {
        return 0;
    }

    @Override
    public List<GroupObject> getGroupObjects() {
        return new ArrayList<>();
    }

    @Override
    public Map<String, Material> getMaterials() {
        return new HashMap<>();
    }

    @Override
    public FileType getType() {
        return FileType.CLASS;
    }
}