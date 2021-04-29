package jp.ngt.rtm.render;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

import java.util.ArrayList;
import java.util.List;

@SideOnly(Side.CLIENT)
public class PartsWithChildren extends Parts {
    public final List<Parts> childParts = new ArrayList<>();

    public PartsWithChildren(String... par1) {
        super(par1);
    }

    public void addParts(Parts par1) {
        this.childParts.add(par1);
    }

    @Override
    public void init(PartsRenderer renderer) {
        super.init(renderer);

        this.childParts.forEach(parts -> parts.init(renderer));
    }
}