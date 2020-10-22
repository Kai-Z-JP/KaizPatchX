package jp.ngt.ngtlib.renderer.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.io.FileType;
import net.minecraft.client.model.ModelBase;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * Minecraftデフォルトのモデル形式を扱う
 */
@SideOnly(Side.CLIENT)
public abstract class MCModel extends ModelBase implements IModelNGT {
	@Override
	public void renderAll(boolean smoothing) {
		GL11.glPushMatrix();
		GL11.glScalef(1.0F, -1.0F, -1.0F);
		//GL11.glRotatef(-90.0F, 0.0F, 1.0F, 0.0F);
		this.render(null, 0.0F, 0.0F, 0.0F, 0.0F, 0.0F, 0.0625F);
		GL11.glPopMatrix();
	}

	@Override
	public void renderOnly(boolean smoothing, String... groupNames) {
		this.renderAll(smoothing);
	}

	@Override
	public void renderPart(boolean smoothing, String partName) {
		this.renderAll(smoothing);
	}

	@Override
	public int getDrawMode() {
		return 0;
	}

	@Override
	public ArrayList<GroupObject> getGroupObjects() {
		return new ArrayList<GroupObject>();
	}

	@Override
	public Map<String, Material> getMaterials() {
		return new HashMap<String, Material>();
	}

	@Override
	public FileType getType() {
		return FileType.CLASS;
	}
}