package jp.kaiz.kaizpatch.fixrtm.model;

import jp.ngt.ngtlib.renderer.model.IModelNGT;

import java.util.List;

public final class CachedModelUtil {
    private CachedModelUtil() {
    }

    public static void compact(IModelNGT model) {
        CachedPolygonModel.INSTANCE.compact(model);
    }

    public static void pin(IModelNGT model) {
        CachedPolygonModel.INSTANCE.pin(model);
    }

    public static void prepareSharedUse(IModelNGT model) {
        CachedPolygonModel.INSTANCE.prepareSharedUse(model);
    }

    public static List<String> getDebugLines() {
        return CachedPolygonModel.INSTANCE.getDebugLines();
    }
}
