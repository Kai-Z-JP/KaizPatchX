package jp.ngt.rtm.render;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.renderer.DisplayList;
import jp.ngt.ngtlib.renderer.GLHelper;
import jp.ngt.ngtlib.renderer.NGTRenderHelper;
import jp.ngt.ngtlib.renderer.model.GroupObject;
import jp.ngt.ngtlib.renderer.model.IModelNGT;
import jp.ngt.ngtlib.util.NGTUtil;
import org.lwjgl.opengl.GL11;

import java.util.Arrays;

@SideOnly(Side.CLIENT)
public class Parts {
    public final String[] objNames;
    private GroupObject[] objs;
    private DisplayList[] gLists;

    public Parts(String... par1) {
        this.objNames = par1;
    }

	public void init(PartsRenderer renderer) {
		this.gLists = new DisplayList[renderer.modelObj.textures.length];
	}

	public GroupObject[] getObjects(IModelNGT model) {
		if (this.objs == null) {
			this.objs = new GroupObject[this.objNames.length];
			for (int i = 0; i < this.objs.length; ++i) {
				for (GroupObject obj : model.getGroupObjects()) {
					if (this.objNames[i].equals(obj.name)) {
						this.objs[i] = obj;
						break;
					}
				}
			}
		}
		return this.objs;
	}

	public boolean containsName(String name) {
		return NGTUtil.contains(this.objNames, name);
	}

	public void render(PartsRenderer renderer) {
		boolean smoothing = (renderer.modelSet.getConfig()).smoothing;
		IModelNGT model = renderer.modelObj.model;
		if (model.getGroupObjects().isEmpty()) {
			model.renderOnly(smoothing, this.objNames);
		} else {
			int i = renderer.currentMatId;
			if (!GLHelper.isValid(this.gLists[i])) {
				this.gLists[i] = GLHelper.generateGLList();
				GLHelper.startCompile(this.gLists[i]);
				NGTRenderHelper.renderCustomModel(model, (byte) i, smoothing, this.objNames);
				GLHelper.endCompile();
			} else {
				if (smoothing)
					GL11.glShadeModel(7425);
				if (ignoreMatId(renderer)) {
                    Arrays.stream(this.gLists).forEach(GLHelper::callList);
                } else {
					GLHelper.callList(this.gLists[i]);
				}
				if (smoothing)
					GL11.glShadeModel(7424);
			}
		}
	}

	public boolean ignoreMatId(PartsRenderer renderer) {
		return false;
	}
}