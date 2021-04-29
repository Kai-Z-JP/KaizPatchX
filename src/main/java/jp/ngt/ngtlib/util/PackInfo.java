package jp.ngt.ngtlib.util;

public class PackInfo {
    public String name;
    public String homepage;
    public String updateURL;
    public String version;

    public PackInfo(String par1, String par2, String par3, String par4) {
        this.name = par1;
        this.homepage = par2;
        this.updateURL = par3;
        this.version = par4;
    }
}