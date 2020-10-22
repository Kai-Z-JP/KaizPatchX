package jp.ngt.rtm.modelpack;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.io.*;
import jp.ngt.ngtlib.renderer.model.*;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.modelpack.cfg.ModelConfig;
import jp.ngt.rtm.modelpack.modelset.ModelSetBase;
import jp.ngt.rtm.network.PacketModelSet;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.model.ModelFormatException;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * モデルパックを管理する
 */
public final class ModelPackManager {
	public static final ModelPackManager INSTANCE = new ModelPackManager();
	private static final String JAVA_MODEL_PATH = "jp/ngt/rtm/modelpack/model/";

	private static final Pattern SC_INCLUDE = Pattern.compile("//include <(.+)>");

	/**
	 * 全ModelSet
	 */
	private final Map<String, Map<String, ModelSetBase>> allModelSetMap = new HashMap();
	/**
	 * SMPで使用可能なModelSet
	 */
	private final Map<String, Map<String, ModelSetBase>> smpModelSetMap = new HashMap();
	private final Map<String, TypeEntry> typeMap = new HashMap();
	private final Map<String, ModelSetBase> dummyMap = new HashMap();
	/**
	 * モデルファイルのキャッシュ
	 */
	private final Map<String, IModelNGT> modelFileMap = new HashMap();
	private final Map<String, Map<String, ResourceLocation>> resourceMap = new HashMap();
	/**
	 * Scriptキャッシュ
	 */
	private final Map<String, String> scriptCache = new HashMap(64);

	private ModelPackManager() {
	}

	/**
	 * ModelSetのタイプを登録
	 */
	public void registerType(String type, Class<? extends ModelConfig> cfg, Class<? extends ModelSetBase> set) {
		TypeEntry entry = new TypeEntry(cfg, set);
		this.typeMap.put(type, entry);
		this.allModelSetMap.put(type, new HashMap<String, ModelSetBase>());
		this.smpModelSetMap.put(type, new HashMap<String, ModelSetBase>());
		ModelSetBase dummy = this.getNewModelSet(entry, new Class[]{});
		this.dummyMap.put(type, dummy);
	}

	/**
	 * jsonからモデルを登録
	 *
	 * @return モデル名
	 */
	public String registerModelset(String type, String json) {
		TypeEntry entry = this.typeMap.get(type);
		ModelConfig cfg = (ModelConfig) NGTJson.getObjectFromJson(json, entry.cfgClass);
		cfg.init();
		ModelSetBase set = this.getNewModelSet(entry, new Class[]{entry.cfgClass}, cfg);
		if (set != null) {
			NGTLog.debug("Registr model : " + cfg.getName() + "(" + type + ")");
			this.allModelSetMap.get(type).put(cfg.getName(), set);
			return cfg.getName();
		} else {
			throw new ModelPackException("Failed to create ModelSet", cfg.getName());
		}
	}

	/**
	 * SMPの時のみServer側で呼ばれる(PlayerLoggedInEvent)
	 */
	public void sendModelSetsToClient(EntityPlayerMP player) {
		int count = 0;
		for (Map<String, ModelSetBase> map : this.allModelSetMap.values()) {
			for (ModelSetBase modelSet : map.values()) {
				ModelConfig cfg = modelSet.getConfig();
				RTMCore.NETWORK_WRAPPER.sendTo(new PacketModelSet(count, cfg.getModelType(), cfg.getName()), player);
				NGTLog.debug("[RTM] Send model to client : " + cfg.getName());
				++count;
			}
		}

		//this.signBoardList.sendSignBoardsToClient(player);
	}

	/**
	 * SMPの時のみClient側で呼ばれる
	 */
	public void addModelSetName(int count, String type, String name) {
		assert NGTUtil.isSMP() && !NGTUtil.isServer();
		if (count == 0) {
			for (Map<String, ModelSetBase> map : this.smpModelSetMap.values()) {
				map.clear();
			}
		}

		//getModelSet()はSMPで呼んではいけない(戒め)
		ModelSetBase modelSet = this.allModelSetMap.get(type).get(name);
		if (modelSet != null) {
			this.smpModelSetMap.get(type).put(name, modelSet);
			NGTLog.debug("[RTM] Add model to SMP map : " + name);
		}
	}

	private ModelSetBase getNewModelSet(TypeEntry entry, Class[] parameterTypes, Object... parameters) {
		try {
			Constructor cons0 = entry.modelsetClass.getConstructor(parameterTypes);
			return (ModelSetBase) cons0.newInstance(parameters);
		} catch (ReflectiveOperationException e) {
			String name = "";
			if (parameters.length > 0) {
				name = ((ModelConfig) parameters[0]).getName();
			}
			throw new ModelPackException("On construct ModelSet", name, e);
		}
	}

	public <T extends ModelSetBase> T getModelSet(String type, String name) {
		boolean isSMPClient = NGTUtil.isSMP() && !NGTUtil.isServer();
		Map<String, Map<String, ModelSetBase>> map = isSMPClient ? this.smpModelSetMap : this.allModelSetMap;
		T modelSet = (T) map.get(type).get(name);
		return (modelSet == null) ? (T) this.dummyMap.get(type) : modelSet;
	}

	public List<ModelSetBase> getModelList(String type) {
		List<ModelSetBase> list = new ArrayList<ModelSetBase>();
		Map<String, Map<String, ModelSetBase>> map = NGTUtil.isSMP() ? this.smpModelSetMap : this.allModelSetMap;
		for (ModelSetBase modelSet : map.get(type).values()) {
			list.add(modelSet);
		}

		Collections.sort(list, new Comparator<ModelSetBase>() {
			@Override
			public int compare(ModelSetBase o1, ModelSetBase o2) {
				return o1.getConfig().getName().compareTo(o2.getConfig().getName());
			}
		});
		return list;
	}

	public void setModelFile(String key, IModelNGT value) {
		this.modelFileMap.put(key, value);
	}

	public IModelNGT getModelFile(String key) {
		return this.modelFileMap.get(key);
	}

	/**
	 * @param modelName モデルファイル名
	 * @param drawMode  GL_TRIANGLES or GL_QUADS
	 */
	@SideOnly(Side.CLIENT)
	public IModelNGT loadModel(String modelName, int drawMode, boolean addModelMap, ModelConfig cfg) {
		if (FileType.CLASS.match(modelName)) {
			return this.loadJavaModel(modelName, addModelMap);
		}

		if (addModelMap && this.modelFileMap.containsKey(modelName)) {
			return this.modelFileMap.get(modelName);
		}

		VecAccuracy accuracy = (cfg.accuracy == null || cfg.accuracy.equals(VecAccuracy.MEDIUM.toString())) ? VecAccuracy.MEDIUM : VecAccuracy.LOW;
		String resource = "models/" + modelName;
		IModelNGT model = null;

		try {
			if (FileType.NGTO.match(modelName)) {
				model = new NGTOModel(new ResourceLocation(resource), cfg.scale);
			} else if (FileType.NGTZ.match(modelName)) {
				model = new NGTZModel(new ResourceLocation(resource), cfg.scale);
			} else//mqo,obj
			{
				model = ModelLoader.loadModel(resource, accuracy, drawMode);
			}
		} catch (ModelFormatException e) {
			throw new ModelFormatException("Can't load model : " + modelName, e);
		}

		if (model == null) {
			throw new ModelPackException("Can't find model file", cfg.getName());
		}

		if (addModelMap) {
			this.modelFileMap.put(modelName, model);
		}

		return model;
	}

	@SideOnly(Side.CLIENT)
	private IModelNGT loadJavaModel(String modelName, boolean addModelMap) {
		if (this.modelFileMap.containsKey(modelName)) {
			IModelNGT obj = this.modelFileMap.get(modelName);
			if (addModelMap) {
				return obj;
			}
		}

		File modelFile = new File(JAVA_MODEL_PATH + modelName);

		try {
			Class clazz = NGTClassUtil.loadClassFromFile(modelFile);
			IModelNGT obj = (IModelNGT) clazz.newInstance();
			if (addModelMap) {
				this.modelFileMap.put(modelName, obj);
			}
			return obj;
		} catch (InstantiationException e) {
			throw new ModelPackException("[RTM] Can't load class", modelName, e);
		} catch (IllegalAccessException e) {
			throw new ModelPackException("[RTM] Can't load class", modelName, e);
		} catch (ClassNotFoundException e) {
			throw new ModelPackException("[RTM] Can't load class", modelName, e);
		}
	}

	private static final String DEFAULT_DOMAIN = "minecraft";

	public ResourceLocation getResource(String path) {
		return this.getResource(DEFAULT_DOMAIN, path);
	}

	public ResourceLocation getResource(String domain, String path) {
		Map<String, ResourceLocation> map = this.resourceMap.get(domain);
		if (map == null) {
			map = new HashMap<String, ResourceLocation>();
			this.resourceMap.put(domain, map);
		} else if (map.containsKey(path)) {
			return map.get(path);
		}

		ResourceLocation resource = new ResourceLocation(domain, path);
		map.put(path, resource);
		return resource;
	}

	public String getScript(String fileName) {
		try {
			return this.loadScript(fileName);
		} catch (IOException e) {
			throw new ModelPackException("Failed to load script", fileName, e);
		}
	}

	private String loadScript(String fileName) throws IOException {
		if (this.scriptCache.containsKey(fileName)) {
			return this.scriptCache.get(fileName);
		}

		//インデントないと"//"以降全てコメント扱い
		String rawScript = NGTText.append(NGTText.readText(this.getResource(fileName)), true);
		while (true) {
			Matcher matcher = SC_INCLUDE.matcher(rawScript);
			if (matcher.find()) {
				String path = matcher.group(1);
				String rep = this.loadScript(path);
				rawScript = matcher.replaceFirst(rep);
			} else {
				break;
			}
		}

		this.scriptCache.put(fileName, rawScript);
		return rawScript;
	}

	public class TypeEntry {
		public final Class<? extends ModelConfig> cfgClass;
		public final Class<? extends ModelSetBase> modelsetClass;

		public TypeEntry(Class<? extends ModelConfig> cls1, Class<? extends ModelSetBase> cls2) {
			this.cfgClass = cls1;
			this.modelsetClass = cls2;
		}
	}
}