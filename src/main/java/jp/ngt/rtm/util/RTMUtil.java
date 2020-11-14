package jp.ngt.rtm.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.LinkedList;
import java.util.List;

public final class RTMUtil {
    public static final List<String> MESSAGELIST = new LinkedList<>();

    @SideOnly(Side.CLIENT)
    public static void setDebugMessage(String par1) {
        MESSAGELIST.add(par1);
    }
}