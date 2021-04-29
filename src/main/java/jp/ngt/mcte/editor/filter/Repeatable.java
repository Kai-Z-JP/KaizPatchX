package jp.ngt.mcte.editor.filter;

import jp.ngt.ngtlib.math.AABBInt;

public interface Repeatable {
    void processing(AABBInt box, int index, int x, int y, int z);
}