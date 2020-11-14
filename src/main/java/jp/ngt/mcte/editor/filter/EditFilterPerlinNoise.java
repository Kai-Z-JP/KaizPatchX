package jp.ngt.mcte.editor.filter;

import jp.ngt.mcte.editor.Editor;
import jp.ngt.ngtlib.math.AABBInt;
import jp.ngt.ngtlib.math.PerlinNoise;
import net.minecraft.block.Block;
import net.minecraft.world.World;

public class EditFilterPerlinNoise extends EditFilterBase {
	private final String[] names = {"Perlin2D", "Perlin3D"};

	@Override
	public void init(Config par) {
		super.init(par);
		par.addString("Mode", this.names[0], this.names);
		par.addInt("Octaves", 1, 1, 256);
		par.addFloat("Persistence", 1.0F, 0.0F, 65535.0F);
		par.addFloat("ScaleX", 0.0625F, 0.0F, 255.0F);
		par.addFloat("ScaleY", 0.0625F, 0.0F, 255.0F);
		par.addFloat("ScaleZ", 0.0625F, 0.0F, 255.0F);
	}

	@Override
	public String getFilterName() {
		return "PerlinNoise";
	}

	@Override
	public boolean edit(Editor editor) {
		AABBInt box = editor.getSelectBox();
		if (box != null) {
			editor.record(box);
			String mode = this.getCfg().getString("Mode");
			Repeatable repeater;
			if (this.names[0].equals(mode)) {
				repeater = this.getRepeaterPerlin2D(editor, box);
			} else {
				repeater = this.getRepeaterPerlin3D(editor, box);
			}
			editor.repeat(box, repeater);
			return true;
		}
		return false;
	}

	private Repeatable getRepeaterPerlin2D(final Editor editor, AABBInt box) {
        final double[] r = {(double) (box.maxX - box.minX) / 2.0D, (double) (box.maxY - box.minY) / 2.0D, (double) (box.maxZ - box.minZ) / 2.0D};
        final int octaves = this.getCfg().getInt("Octaves");
        final float persistence = this.getCfg().getFloat("Persistence");
        final float scaleX = this.getCfg().getFloat("ScaleX");
        final float scaleZ = this.getCfg().getFloat("ScaleZ");

        return (box1, index, x, y, z) -> {
            World world = editor.getWorld();
            Block fillBlock = editor.getEntity().getSlotBlock(0);
            int meta = editor.getEntity().getSlotBlockMetadata(0);
            double dx = (double) x * scaleX;
            double dz = (double) z * scaleZ;
            double h = (PerlinNoise.octavePerlin(dx, 0.0D, dz, octaves, persistence) + 0.5D) * r[1] * 2.0D;
            if ((double) y <= h) {
                editor.setBlock(x, y, z, fillBlock, meta);
            }
        };
    }

	private Repeatable getRepeaterPerlin3D(final Editor editor, AABBInt box) {
        final double[] r = {(double) (box.maxX - box.minX) / 2.0D, (double) (box.maxY - box.minY) / 2.0D, (double) (box.maxZ - box.minZ) / 2.0D};
        final int octaves = this.getCfg().getInt("Octaves");
        final float persistence = this.getCfg().getFloat("Persistence");
        final float scaleX = this.getCfg().getFloat("ScaleX");
        final float scaleY = this.getCfg().getFloat("ScaleY");
        final float scaleZ = this.getCfg().getFloat("ScaleZ");

        return (box1, index, x, y, z) -> {
            World world = editor.getWorld();
            Block fillBlock = editor.getEntity().getSlotBlock(0);
            int meta = editor.getEntity().getSlotBlockMetadata(0);
            double dx = (double) x * scaleX;
            double dy = (double) y * scaleY;
            double dz = (double) z * scaleZ;
            if (PerlinNoise.octavePerlin(dx, dy, dz, octaves, persistence) >= 0.0D) {
                editor.setBlock(x, y, z, fillBlock, meta);
            }
        };
    }
}