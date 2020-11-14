package jp.ngt.rtm.modelpack.texture;

import jp.ngt.ngtlib.io.IProgressWatcher;
import jp.ngt.ngtlib.io.NGTFileLoadException;
import jp.ngt.ngtlib.io.NGTFileLoader;
import jp.ngt.ngtlib.io.NGTJson;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

public final class TextureManager {
    public static final TextureManager INSTANCE = new TextureManager();

    private final Map<TexturePropertyType, Map<String, TextureProperty>> allTextureMap = new HashMap<>();
    //private final Map<TexturePropertyType, Map<String, TextureProperty>> smpTextureMap = new HashMap<TexturePropertyType, Map<String, TextureProperty>>();

    private TextureManager() {
    }

    public List<File> loadTextures(IProgressWatcher par1) {
        par1.setValue(0, 2, "Loading Textures");
        return NGTFileLoader.findFile((file) -> {
            String name = file.getName();
            return name.startsWith("SignBoard") && name.endsWith(".json");
        });
    }

    public void registerTextures(IProgressWatcher par1, List<File> fileList, ExecutorService executor, List<Future<?>> list) {
        Arrays.stream(TexturePropertyType.values()).filter(tpt -> tpt.useJson).forEach(tpt -> {
            Map<String, TextureProperty> map = new HashMap<>();
            this.allTextureMap.put(tpt, map);
            par1.addMaxValue(1, fileList.size());
            fileList.forEach(file -> list.add(executor.submit(() -> {
                {
                    String json = NGTJson.readFromJson(file);
                    try {
                        TextureProperty property = (TextureProperty) NGTJson.getObjectFromJson(json, tpt.type);
                        if (property != null) {
                            property.init();
                            map.put(property.texture, property);
                            par1.addValue(1, property.texture);
//						NGTLog.debug("Register Texture : %s (%s)", property.texture, tpt.toString());
                        }
                    } catch (Exception e) {
                        throw new NGTFileLoadException(String.format("[TextureManager] Failed to load : %s", file.getName()), e);
                    }
                }
            })));
        });
    }

    public List<File> loadRailRoadSigns(IProgressWatcher par1) {
        par1.setValue(0, 3, "Loading RailroadSign");
        return NGTFileLoader.findFile((file) -> {
            String name = file.getName();
            return name.startsWith("rrs_") && name.endsWith(".png");
        });
    }

    public void registerRailRoadSigns(IProgressWatcher par1, List<File> fileList, ExecutorService executor, List<Future<?>> list) {
        Map<String, TextureProperty> map = new HashMap<>();
        this.allTextureMap.put(TexturePropertyType.RRS, map);
        par1.addMaxValue(1, fileList.size());
        //			NGTLog.debug("Register Texture : %s (RRS)", name);
        fileList.stream().map(File::getName).forEach(name -> list.add(executor.submit(() -> {
            RRSProperty prop = new RRSProperty(name);
            prop.init();
            map.put(prop.texture, prop);
            par1.addValue(1, name);
        })));

    }

    public <T extends TextureProperty> T getProperty(TexturePropertyType type, String key) {
        //Map<String, SignBoardProperty> map = NGTUtil.isSMP() ? this.smpTextureMap : this.allTextureMap;
        if (this.allTextureMap.containsKey(type)) {
            return (T) this.allTextureMap.get(type).get(key);//Serverでぬるぽ出ないように
        }
        return null;
    }

    public List<TextureProperty> getTextureList(TexturePropertyType type) {
        List<TextureProperty> list = new ArrayList<>(this.allTextureMap.get(type).values());

        list.sort(Comparator.comparing(o -> o.texture));
        return list;
    }

    public enum TexturePropertyType {
        SignBoard(SignBoardProperty.class, true),
        Flag(FlagProperty.class, true),
        RRS(RRSProperty.class, false);

        public final Class<? extends TextureProperty> type;
        public final boolean useJson;

        TexturePropertyType(Class<? extends TextureProperty> clazz, boolean par2) {
            this.type = clazz;
            this.useJson = par2;
        }
    }
}