package jp.ngt.rtm.modelpack;

import com.google.common.collect.Lists;
import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.io.NGTLog;
import net.minecraft.client.resources.IResource;
import net.minecraft.client.resources.IResourceManager;
import net.minecraft.client.resources.SimpleResource;
import net.minecraft.client.resources.data.IMetadataSerializer;
import net.minecraft.util.ResourceLocation;

import java.io.*;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Set;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

/**
 * RTMResourceHandlerで使用
 */
@SideOnly(Side.CLIENT)
public class RTMResourceManager implements IResourceManager {
    protected final List resourcePacks = new ArrayList();
    private final IMetadataSerializer frmMetadataSerializer;
    private final File domain;

    /**
     * @param par2 ドメインフォルダ
     */
    public RTMResourceManager(IMetadataSerializer par1, File par2) {
        this.frmMetadataSerializer = par1;
        this.domain = par2;
    }

    @Override
    public Set getResourceDomains() {
        return null;
    }

    @SuppressWarnings("resource")
    @Override
    public IResource getResource(ResourceLocation par1) throws IOException {
        if (this.domain == null) {
            throw new FileNotFoundException(par1.toString());
        }

        if (this.domain.getAbsolutePath().contains(par1.getResourceDomain())) {
            InputStream stream = null;
            if (this.domain.getAbsolutePath().contains(".zip")) {
                String path = this.domain.getAbsolutePath();
                int index = path.indexOf(".zip");
                String zipPath = path.substring(0, index + 4);
                try {
                    ZipFile zip = new ZipFile(zipPath);
                    Enumeration<? extends ZipEntry> enu = zip.entries();
                    while (enu.hasMoreElements()) {
                        ZipEntry ze = enu.nextElement();
                        if (!ze.isDirectory()) {
                            File fileInZip = new File(zipPath, ze.getName());
                            if (par1.getResourcePath().contains(fileInZip.getName())) {
                                stream = zip.getInputStream(ze);
                            }
                        }
                    }
                    //zip.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            } else {
                File resource = new File(this.domain, par1.getResourcePath());
                stream = new FileInputStream(resource);
            }

            if (stream != null) {
                return new SimpleResource(par1, stream, null, this.frmMetadataSerializer);
            } else {
                NGTLog.debug("[RTM](Client)Can't get input stream : " + par1.getResourcePath());
            }
        }

        throw new FileNotFoundException(par1.toString());
    }

    @Override
    public List getAllResources(ResourceLocation par1) throws IOException {
        ArrayList list = Lists.newArrayList();
        list.add(this.getResource(par1));
        if (!list.isEmpty()) {
            return list;
        }
        throw new FileNotFoundException(par1.toString());
    }
}