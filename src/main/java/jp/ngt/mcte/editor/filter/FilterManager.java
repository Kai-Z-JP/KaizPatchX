package jp.ngt.mcte.editor.filter;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.mcte.MCTE;
import jp.ngt.mcte.editor.Editor;
import jp.ngt.mcte.editor.EditorManager;
import jp.ngt.mcte.network.PacketFilter;
import jp.ngt.ngtlib.io.NGTFileLoader;
import jp.ngt.ngtlib.io.NGTText;
import net.minecraft.entity.player.EntityPlayer;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Constructor;
import java.util.*;
import java.util.stream.Collectors;

public final class FilterManager {
	public static final FilterManager INSTANCE = new FilterManager();
	public static final String FILTER_PATH = "mcte/filter/";

	private final Map<String, Class<? extends EditFilterBase>> filterClasses;
	@SideOnly(Side.CLIENT)
	private Map<String, EditFilterBase> filters;

	private FilterManager() {
		this.filterClasses = new HashMap<>();
		this.filterClasses.put("Copy", EditFilterCopy.class);
		this.filterClasses.put("Cut", EditFilterCut.class);
		this.filterClasses.put("Delete", EditFilterDelete.class);
		this.filterClasses.put("DeleteEntity", EditFilterDeleteEntity.class);
		this.filterClasses.put("Fill", EditFilterFill.class);
		this.filterClasses.put("FillSurface", EditFilterFillSurface.class);
		this.filterClasses.put("Paste", EditFilterPaste.class);
		this.filterClasses.put("PerlinNoise", EditFilterPerlinNoise.class);
		this.filterClasses.put("Random", EditFilterRandom.class);
	}

	public EditFilterBase getFilterInstance(String name) {
		try {
			Class<? extends EditFilterBase> clazz = this.filterClasses.get(name);
			if (clazz == null) {
				return null;
			}
			Constructor constructor = clazz.getConstructor();
			return (EditFilterBase) constructor.newInstance();
		} catch (ReflectiveOperationException e) {
			e.printStackTrace();
		}
		return null;
	}

	@SideOnly(Side.CLIENT)
	public void loadFilters() {
		this.filters = new LinkedHashMap<>();
		this.initFilter(new EditFilterCopy());
		this.initFilter(new EditFilterCut());
		this.initFilter(new EditFilterDelete());
		this.initFilter(new EditFilterDeleteEntity());
		this.initFilter(new EditFilterFill());
		this.initFilter(new EditFilterFillSurface());
		this.initFilter(new EditFilterPaste());
		this.initFilter(new EditFilterPerlinNoise());
		this.initFilter(new EditFilterRandom());

		File filterFolder = new File(NGTFileLoader.getModsDir().get(0), FILTER_PATH);
		List<File> list = NGTFileLoader.findFileInDirectory(filterFolder, (file) -> file.getName().endsWith(".js"));
		list.stream().map(file -> new EditFilterCustom(NGTText.readText(file, false))).forEach(this::initFilter);
	}

	@SideOnly(Side.CLIENT)
	private void initFilter(EditFilterBase filter) {
		File file = filter.getCfgFile();
		try {
			if (!file.getParentFile().exists()) {
				file.getParentFile().mkdirs();
			}

			if (!file.exists()) {
				file.createNewFile();
			}
		} catch (IOException e) {
			e.printStackTrace();
			return;
		}

		Config cfg = new Config();
		filter.init(cfg);
		cfg.load(file);
		filter.save();

		this.filters.put(filter.getFilterName(), filter);
	}

	@SideOnly(Side.CLIENT)
	public Collection<EditFilterBase> getFilters() {
		return this.filters.values();
	}

	@SideOnly(Side.CLIENT)
	public void execFilter(EntityPlayer player, String name) {
		EditFilterBase filter = this.filters.get(name);
		String[] sa = filter.getCfg().export();
		String builder = Arrays.stream(sa).map(s -> s + ",").collect(Collectors.joining());

		String script = "";
		if (filter instanceof EditFilterCustom) {
			script = ((EditFilterCustom) filter).getScriptText();
		}
		MCTE.NETWORK_WRAPPER.sendToServer(new PacketFilter(player, name, builder, script));
	}

	/**
	 * ServerSide
	 */
	public void execFilter(EntityPlayer player, String name, String data, String script) {
		EditFilterBase filter = this.getFilterInstance(name);
		if (filter == null) {
			filter = new EditFilterCustom(script);
		}
		Config cfg = new Config();
		filter.init(cfg);
		cfg.load(data.split(","));

		Editor editor = EditorManager.INSTANCE.getEditor(player);
		if (editor != null) {
			filter.edit(editor);
		}
	}
}