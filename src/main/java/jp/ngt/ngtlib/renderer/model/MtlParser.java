package jp.ngt.ngtlib.renderer.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.client.model.ModelFormatException;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Stream;

@SideOnly(Side.CLIENT)
public class MtlParser {
    private final Map<String, Material> materials = new HashMap<>();

    public MtlParser(InputStream is) {
        this.loadMaterial(is);
    }

    private void loadMaterial(InputStream inputStream) throws ModelFormatException {
        if (inputStream == null) {
            return;
        }

        this.materials.clear();

        try (Stream<String> stream = new BufferedReader(new InputStreamReader(inputStream)).lines()) {
            stream.map(currentLine -> currentLine.replaceAll("\\s+", " ").trim())
                    .filter(currentLine -> !(currentLine.length() == 0 || currentLine.startsWith("#")) && currentLine.startsWith("newmtl "))
                    .map(currentLine -> currentLine.split(" "))
                    .forEach(sa -> this.materials.put(sa[1], new Material((byte) this.materials.size(), null)));
        }
    }

    public Map<String, Material> getMaterials() {
        return this.materials;
    }
}