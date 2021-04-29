package jp.ngt.ngtlib.renderer.model;

import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.io.FileType;

import java.util.List;
import java.util.Map;

import static cpw.mods.fml.relauncher.Side.CLIENT;

/**
 * モデルファイルのデータを格納
 */
@SideOnly(CLIENT)
public interface IModelNGT {
    void renderAll(boolean smoothing);

    void renderOnly(boolean smoothing, String... groupNames);

    void renderPart(boolean smoothing, String partName);

    /**
     * GL_QUADS or GL_TRIANGLES
     */
    int getDrawMode();

    List<GroupObject> getGroupObjects();

    Map<String, Material> getMaterials();

    FileType getType();
}