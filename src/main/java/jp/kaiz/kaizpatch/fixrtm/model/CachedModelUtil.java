package jp.kaiz.kaizpatch.fixrtm.model;

import jp.ngt.ngtlib.renderer.model.IModelNGT;

import java.util.List;

public final class CachedModelUtil {
    private CachedModelUtil() {
    }

    public static IModelNGT copyForIsolatedInit(IModelNGT model) {
        return CachedPolygonModel.INSTANCE.copyForIsolatedInit(model);
    }

    public static boolean supportsIsolatedInitCopy(IModelNGT model) {
        return CachedPolygonModel.INSTANCE.supportsIsolatedInitCopy(model);
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
