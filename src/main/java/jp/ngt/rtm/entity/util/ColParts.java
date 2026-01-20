package jp.ngt.rtm.entity.util;

import java.util.ArrayList;
import java.util.List;

public final class ColParts {
    public final String name;
    public final List<ColFace> faces = new ArrayList<>();

    public ColParts(String par1) {
        this.name = par1;
    }
}
