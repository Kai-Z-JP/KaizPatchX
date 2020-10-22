package jp.ngt.rtm.render;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.renderer.model.GroupObject;
import jp.ngt.ngtlib.renderer.model.NGTOModel;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.modelpack.cfg.VehicleBaseConfig;
import jp.ngt.rtm.modelpack.cfg.VehicleBaseConfig.VehicleParts;
import jp.ngt.rtm.modelpack.modelset.ModelSetVehicleBaseClient;
import net.minecraft.entity.Entity;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * 独自のPartsRendererがない場合に使用(乗り物専用)
 */
@SideOnly(Side.CLIENT)
public class BasicVehiclePartsRenderer extends VehiclePartsRenderer {
	private Parts body;
	private PartsWithChildren[] doorLeft, doorRight;
	private PartsWithChildren[] pantographFront, pantographBack;

	public BasicVehiclePartsRenderer(String... par1) {
		super(par1);
	}

	@Override
	public void init(ModelSetVehicleBaseClient par1, ModelObject par2) {
		VehicleBaseConfig cfg = par1.getConfig();
		List<String> list = new ArrayList<String>();

		this.doorLeft = (cfg.door_left != null) ? this.getParts(list, cfg.door_left) : new PartsWithChildren[0];
		this.doorRight = (cfg.door_right != null) ? this.getParts(list, cfg.door_right) : new PartsWithChildren[0];
		this.pantographFront = (cfg.pantograph_front != null) ? this.getParts(list, cfg.pantograph_front) : new PartsWithChildren[0];
		this.pantographBack = (cfg.pantograph_back != null) ? this.getParts(list, cfg.pantograph_back) : new PartsWithChildren[0];

		List<GroupObject> goList = par2.model.getGroupObjects();
		List<String> bodyParts = new ArrayList<String>();
		for (GroupObject obj : goList) {
			if (!list.contains(obj.name)) {
				bodyParts.add(obj.name);
			}
		}

		if (bodyParts.isEmpty() && par2.model instanceof NGTOModel) {
			bodyParts.add(NGTOModel.GROUP_NAME);
		}
		this.body = new Parts(bodyParts.toArray(new String[list.size()]));

		this.partsList.addAll(Arrays.asList(this.doorLeft));
		this.partsList.addAll(Arrays.asList(this.doorRight));
		this.partsList.addAll(Arrays.asList(this.pantographFront));
		this.partsList.addAll(Arrays.asList(this.pantographBack));
		this.partsList.add(this.body);

		super.init(par1, par2);

	}

	private PartsWithChildren[] getParts(List<String> list, VehicleParts[] parts) {
		PartsWithChildren[] array = new PartsWithChildren[parts.length];
		for (int i = 0; i < parts.length; ++i) {
			array[i] = new PartsWithChildren(parts[i].objects);
			NGTUtil.addArray(list, parts[i].objects);
			if (parts[i].childParts != null) {
				NGTUtil.addArray(array[i].childParts, this.getParts(list, parts[i].childParts));
			}
		}
		return array;
	}

	@Override
	public void render(Entity entity, int pass, float par3) {
		this.body.render(this);
		VehicleBaseConfig cfg = this.modelSet.getConfig();

		if (cfg.door_left != null) {
			float move = this.getDoorMovementL(entity);
			for (int j = 0; j < cfg.door_left.length; ++j) {
				this.renderParts(this.sigmoid(move), cfg.door_left[j], this.doorLeft[j]);
			}
		}

		if (cfg.door_right != null) {
			float move = this.getDoorMovementR(entity);
			for (int j = 0; j < cfg.door_right.length; ++j) {
				this.renderParts(this.sigmoid(move), cfg.door_right[j], this.doorRight[j]);
			}
		}

		if (cfg.pantograph_front != null) {
			float move = this.getPantographMovementFront(entity);
			for (int j = 0; j < cfg.pantograph_front.length; ++j) {
				this.renderParts(this.sigmoid(move), cfg.pantograph_front[j], this.pantographFront[j]);
			}
		}

		if (cfg.pantograph_back != null) {
			float move = this.getPantographMovementBack(entity);
			for (int j = 0; j < cfg.pantograph_back.length; ++j) {
				this.renderParts(this.sigmoid(move), cfg.pantograph_back[j], this.pantographBack[j]);
			}
		}
	}

	private void renderParts(float move, VehicleParts parts, PartsWithChildren parts2) {
		GL11.glPushMatrix();
		GL11.glTranslatef(parts.pos[0], parts.pos[1], parts.pos[2]);
		for (int i = 0; i < parts.transform.length; ++i) {
			float[] fa = parts.transform[i];
			if (fa.length == 3) {
				GL11.glTranslatef(fa[0] * move, fa[1] * move, fa[2] * move);
			} else if (fa.length == 4) {
				GL11.glRotatef(fa[0] * move, fa[1], fa[2], fa[3]);
			}
		}
		GL11.glTranslatef(-parts.pos[0], -parts.pos[1], -parts.pos[2]);
		parts2.render(this);

		if (parts.childParts != null) {
			for (int i = 0; i < parts.childParts.length; ++i) {
				this.renderParts(move, parts.childParts[i], (PartsWithChildren) parts2.childParts.get(i));
			}
		}

		GL11.glPopMatrix();
	}
}