package jp.ngt.rtm.modelpack;

import jp.ngt.ngtlib.renderer.model.Material;
import jp.ngt.ngtlib.renderer.model.TextureSet;
import jp.ngt.rtm.entity.vehicle.EntityVehicleBase;
import jp.ngt.rtm.modelpack.modelset.ModelSetVehicleBaseClient;
import net.minecraft.client.renderer.texture.TextureManager;
import net.minecraft.entity.Entity;

import java.util.Map;

/**
 * 車体or台車モデルに使用 (.java, .obj, .mqo)
 */
public interface IVehicleModel {
	void init();

	/**
	 * モデルの描画
	 *
	 * @param entity
	 * @param textures
	 * @param pass     0;通常, 1:半透明, 2~4:発光
	 * @param manager
	 */
	void render(Entity entity, TextureSet[] textures, int pass, TextureManager manager, boolean smoothing);

	void renderTrainParts(EntityVehicleBase vehicle, ModelSetVehicleBaseClient modelSet, int pass, TextureManager manager, boolean smoothing);

	/**
	 * obj, mqo or java
	 */
	boolean isObjModel();

	/**
	 * インスタンスの再使用を許可するかどうか
	 */
	boolean reuseInstance();

	/**
	 * ModelSetで使用するテクスチャ込みのマテリアルを取得
	 *
	 * @param map {マテリアル名, テクスチャのパス}
	 */
	Material[] getMaterials(Map<String, String> map);
}