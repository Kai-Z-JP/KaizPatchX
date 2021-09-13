package jp.ngt.rtm.block.tileentity;

import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.block.OrnamentType;
import net.minecraft.block.Block;
import net.minecraft.block.material.MapColor;

public class TileEntityPole extends TileEntityOrnament {
    @Override
    public void updateEntity() {
        if (this.worldObj != null) {
            int meta = this.getBlockMetadata();
            Block block = this.getBlockType();
            if (block == RTMBlock.linePole) {
                if (this.getModelName().equals("")) {
                    String modelName = TileEntityPole.getFixedModelName(meta);
                    this.setModelName(modelName);
                }
            } else if (block == RTMBlock.framework) {
                if (this.getModelName().equals("")) {
                    this.setModelName("IronFrame01");
                    this.getResourceState().color = MapColor.getMapColorForBlockColored(meta).colorValue;
                }
            }
        }
    }

    @Override
    public OrnamentType getOrnamentType() {
        return OrnamentType.Pole;
    }

    @Override
    protected String getDefaultName() {
        return "LinePole01";
    }

    public static String getFixedModelName(int meta) {
        String modelName;
        switch (meta) {
            case 1:
                modelName = "LinePoleFrame01";
                break;
            case 2:
                modelName = "LinePole02";
                break;
            case 3:
                modelName = "SignalPole01";
                break;
            default:
                modelName = "LinePole01";
                break;
        }
        return modelName;
    }
}
