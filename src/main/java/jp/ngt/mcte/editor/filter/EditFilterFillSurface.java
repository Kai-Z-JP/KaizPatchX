package jp.ngt.mcte.editor.filter;

import jp.ngt.mcte.editor.Editor;
import jp.ngt.ngtlib.math.AABBInt;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;

public class EditFilterFillSurface extends EditFilterBase {
	@Override
	public void init(Config par) {
		super.init(par);
	}

	@Override
	public String getFilterName() {
		return "FillSurface";
	}

	@Override
	public boolean edit(final Editor editor) {
		AABBInt box = editor.getSelectBox();
		if (box != null) {
            editor.record(box);
            editor.repeat(box, (box1, index, x, y, z) -> {
                World world = editor.getWorld();
                Block baseBlock = world.getBlock(x, y - 1, z);
                Block fillBlock = editor.getEntity().getSlotBlock(0);
                int meta = editor.getEntity().getSlotBlockMetadata(0);
                boolean flag = world.canBlockSeeTheSky(x, y, z) && (baseBlock != Blocks.air) && (baseBlock != fillBlock);
                if (flag) {
                    editor.setBlock(x, y, z, fillBlock, meta);
                }
            });
            return true;
        }
		return false;
	}
}