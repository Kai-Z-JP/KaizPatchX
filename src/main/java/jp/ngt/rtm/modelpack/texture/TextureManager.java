package jp.ngt.rtm.modelpack.texture;

import jp.ngt.ngtlib.io.IProgressWatcher;
import jp.ngt.ngtlib.io.NGTFileLoadException;
import jp.ngt.ngtlib.io.NGTFileLoader;
import jp.ngt.ngtlib.io.NGTJson;

import java.io.File;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.regex.Pattern;

public final class TextureManager {
    public static final TextureManager INSTANCE = new TextureManager();

    private final Map<TexturePropertyType, Map<String, TextureProperty>> allTextureMap = new HashMap<>();
    //private final Map<TexturePropertyType, Map<String, TextureProperty>> smpTextureMap = new HashMap<TexturePropertyType, Map<String, TextureProperty>>();

    private TextureManager() {
    }

    public List<File> loadTextures(IProgressWatcher par1) {
        par1.setValue(0, 2, "Loading Textures");
        Pattern pattern = Pattern.compile("SignBoard.*\\.json");
        return NGTFileLoader.findFile((file) -> pattern.matcher(file.getName()).matches());
    }

    public void registerTextures(IProgressWatcher par1, List<File> fileList, ExecutorService executor, TexturePropertyType type) {
        Map<String, TextureProperty> map = new HashMap<>();
        this.allTextureMap.put(type, map);
        par1.addMaxValue(1, fileList.size());
        fileList.stream()
                .map(file -> (Runnable) () -> {
                    String json = NGTJson.readFromJson(file);
                    try {
                        TextureProperty property = (TextureProperty) NGTJson.getObjectFromJson(json, type.type);
                        if (property != null) {
                            property.init();
                            map.put(property.texture, property);
                            par1.addValue(1, property.texture);
//						NGTLog.debug("Register Texture : %s (%s)", property.texture, tpt.toString());
                        }
                    } catch (Exception e) {
                        throw new NGTFileLoadException(String.format("[TextureManager] Failed to load : %s", file.getName()), e);
                    }
                })
                .forEach(executor::submit);
    }

    public List<File> loadRailRoadSigns(IProgressWatcher par1) {
        par1.setValue(0, 3, "Loading RailroadSign");
        Pattern pattern = Pattern.compile("rrs_.*\\.png");
        return NGTFileLoader.findFile((file) -> pattern.matcher(file.getName()).matches());
    }

    public List<File> loadFlags(IProgressWatcher par1) {
        par1.setValue(0, 3, "Loading Flag");
        Pattern pattern = Pattern.compile("Flag_.*\\.json");
        return NGTFileLoader.findFile((file) -> pattern.matcher(file.getName()).matches());
    }

    public void registerRailRoadSigns(IProgressWatcher par1, List<File> fileList, ExecutorService executor) {
        Map<String, TextureProperty> map = new HashMap<>();
        this.allTextureMap.put(TexturePropertyType.RRS, map);
        par1.addMaxValue(1, fileList.size());
        //			NGTLog.debug("Register Texture : %s (RRS)", name);
        fileList.stream()
                .map(File::getName)
                .map(name -> (Runnable) () -> {
                    RRSProperty prop = new RRSProperty(name);
                    prop.init();
                    map.put(prop.texture, prop);
                    par1.addValue(1, name);
                })
                .forEach(executor::submit);
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