package jp.ngt.rtm.modelpack;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.io.NGTFileLoader;
import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.ngtlib.util.NGTUtilClient;
import net.minecraft.client.resources.SimpleReloadableResourceManager;
import net.minecraft.client.resources.data.IMetadataSerializer;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class RTMResourceHandler {
    /**
     * モデルパックで追加したsound.jsonの登録
     */
    public static List<String> registerSounds() {
        NGTLog.debug("[RTM](Client) Start registering sounds.json");
        List<String> list = new ArrayList<>();
        SimpleReloadableResourceManager rm = (SimpleReloadableResourceManager) NGTUtilClient.getMinecraft().getResourceManager();
        IMetadataSerializer ms = getMetadataSerializer(rm);
        Map resourceManagers = getDomainResourceManagers(rm);
        if (ms == null || resourceManagers == null) {
            NGTLog.debug("[RTM](Client) Can't start registering sounds.json");
            return list;
        }

        List<File> fileList = NGTFileLoader.findFile((file) -> {
            String name = file.getName();
            return name.startsWith("sounds") && name.endsWith(".json");
        });
        fileList.forEach(file -> {
            String path = file.getAbsolutePath();
            int index0 = path.indexOf("assets") + 7;
            int index1 = path.indexOf("sounds", index0) - 1;
            if (index0 > 0 && index1 > 0 && index1 - index0 > 0) {
                String domain = path.substring(index0, index1);
                if (domain.startsWith("sound_")) {
                    list.add(domain);
                    rm.getResourceDomains().add(domain);
                    RTMResourceManager rrm = new RTMResourceManager(ms, file.getParentFile());
                    resourceManagers.put(domain, rrm);
                    NGTLog.debug("[RTM](Client) Register sounds.json, domain:" + domain);
                }
            }
        });
        return list;
    }

    private static IMetadataSerializer getMetadataSerializer(SimpleReloadableResourceManager par1) {
        return (IMetadataSerializer) NGTUtil.getField(SimpleReloadableResourceManager.class, par1, new String[]{"rmMetadataSerializer", "field_110547_c"});
    }

    private static Map getDomainResourceManagers(SimpleReloadableResourceManager par1) {
        return (Map) NGTUtil.getField(SimpleReloadableResourceManager.class, par1, new String[]{"domainResourceManagers", "field_110548_a"});
    }
}