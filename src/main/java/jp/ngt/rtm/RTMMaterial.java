package jp.ngt.rtm;

import net.minecraft.block.material.MapColor;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.MaterialLiquid;

public class RTMMaterial extends Material {
    public static final Material rail = (new RTMMaterial(MapColor.obsidianColor)).setRequiresTool();
    public static final Material fireproof = (new RTMMaterial(MapColor.obsidianColor)).setRequiresTool();
    public static final Material melted = (new MaterialLiquid(MapColor.tntColor));

    public RTMMaterial(MapColor color) {
        super(color);
    }
}