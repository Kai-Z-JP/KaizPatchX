package jp.ngt.mcte.editor.filter;

import jp.ngt.mcte.editor.Editor;
import jp.ngt.ngtlib.block.BlockSet;
import jp.ngt.ngtlib.math.AABBInt;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class EditFilterFill extends EditFilterBase {
    private Map<String, FillBase> typeMap;

    @Override
    public void init(Config par) {
        super.init(par);

        this.typeMap = new LinkedHashMap<>();
        this.typeMap.put("FillAll", new FillAll());
        this.typeMap.put("FillCylinderX", new FillCylinderX());
        this.typeMap.put("FillCylinderY", new FillCylinderY());
        this.typeMap.put("FillCylinderZ", new FillCylinderZ());
        this.typeMap.put("FillEllipsoid", new FillEllipsoid());

        String[] sa = new String[this.typeMap.size()];
        int index = 0;
        for (String key : this.typeMap.keySet()) {
            sa[index] = key;
            ++index;
        }

        par.addString("FillType", "FillAll", sa);
    }

    @Override
    public String getFilterName() {
        return "Fill";
    }

    @Override
    public boolean edit(Editor editor) {
        AABBInt box = editor.getSelectBox();
        BlockSet blockSet = editor.getFillItem();
        String type = this.cfg.getString("FillType");
        FillBase fillBase = this.typeMap.get(type);

        if (box != null && blockSet != BlockSet.AIR && fillBase != null) {
            editor.record(box);
            editor.repeat(box, fillBase);
            //editor.fill(box, blockSet);
            fillBase.getBlockPos().forEach(pos -> editor.setBlock(pos[0], pos[1], pos[2], blockSet));
            return true;
        }
        return false;
    }

    private abstract static class FillBase implements Repeatable {
        private final List<int[]> posList = new ArrayList<>();

        public void init() {
            this.posList.clear();
        }

        public List<int[]> getBlockPos() {
            return this.posList;
        }

        public void addPos(int x, int y, int z) {
            this.posList.add(new int[]{x, y, z});
        }
    }

    private static class FillAll extends FillBase {
        @Override
        public void processing(AABBInt box, int index, int x, int y, int z) {
            this.addPos(x, y, z);
        }
    }

    private static class FillCylinderX extends FillBase {
        @Override
        public void processing(AABBInt box, int index, int x, int y, int z) {
            double[] center = {(double) (box.maxX + box.minX) / 2.0D, (double) (box.maxY + box.minY) / 2.0D, (double) (box.maxZ + box.minZ) / 2.0D};
            double[] r = {(double) (box.maxX - box.minX) / 2.0D, (double) (box.maxY - box.minY) / 2.0D, (double) (box.maxZ - box.minZ) / 2.0D};
            double y0 = (double) y + 0.5D - center[1];
            double z0 = (double) z + 0.5D - center[2];
            double d0 = (y0 * y0 / (r[1] * r[1])) + (z0 * z0 / (r[2] * r[2]));
            if (d0 <= 1.0D) {
                this.addPos(x, y, z);
            }
        }
    }

    private static class FillCylinderY extends FillBase {
        @Override
        public void processing(AABBInt box, int index, int x, int y, int z) {
            double[] center = {(double) (box.maxX + box.minX) / 2.0D, (double) (box.maxY + box.minY) / 2.0D, (double) (box.maxZ + box.minZ) / 2.0D};
            double[] r = {(double) (box.maxX - box.minX) / 2.0D, (double) (box.maxY - box.minY) / 2.0D, (double) (box.maxZ - box.minZ) / 2.0D};
            double x0 = (double) x + 0.5D - center[0];
            double z0 = (double) z + 0.5D - center[2];
            double d0 = (x0 * x0 / (r[0] * r[0])) + (z0 * z0 / (r[2] * r[2]));
            if (d0 <= 1.0D) {
                this.addPos(x, y, z);
            }
        }
    }

    private static class FillCylinderZ extends FillBase {
        @Override
        public void processing(AABBInt box, int index, int x, int y, int z) {
            double[] center = {(double) (box.maxX + box.minX) / 2.0D, (double) (box.maxY + box.minY) / 2.0D, (double) (box.maxZ + box.minZ) / 2.0D};
            double[] r = {(double) (box.maxX - box.minX) / 2.0D, (double) (box.maxY - box.minY) / 2.0D, (double) (box.maxZ - box.minZ) / 2.0D};
            double x0 = (double) x + 0.5D - center[0];
            double y0 = (double) y + 0.5D - center[1];
            double d0 = (x0 * x0 / (r[0] * r[0])) + (y0 * y0 / (r[1] * r[1]));
            if (d0 <= 1.0D) {
                this.addPos(x, y, z);
            }
        }
    }

    private static class FillEllipsoid extends FillBase {
        @Override
        public void processing(AABBInt box, int index, int x, int y, int z) {
            double[] center = {(double) (box.maxX + box.minX) / 2.0D, (double) (box.maxY + box.minY) / 2.0D, (double) (box.maxZ + box.minZ) / 2.0D};
            double[] r = {(double) (box.maxX - box.minX) / 2.0D, (double) (box.maxY - box.minY) / 2.0D, (double) (box.maxZ - box.minZ) / 2.0D};
            double x0 = (double) x + 0.5D - center[0];
            double y0 = (double) y + 0.5D - center[1];
            double z0 = (double) z + 0.5D - center[2];
            double d0 = (x0 * x0 / (r[0] * r[0])) + (y0 * y0 / (r[1] * r[1])) + (z0 * z0 / (r[2] * r[2]));
            if (d0 <= 1.0D) {
                this.addPos(x, y, z);
            }
        }
    }
}