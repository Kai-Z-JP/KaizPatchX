package jp.ngt.ngtlib.renderer.model;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraftforge.client.model.ModelFormatException;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;

@SideOnly(Side.CLIENT)
public class MtlParser {
    private final Map<String, Material> materials = new HashMap<>();
    private Material currentMaterial;

    public MtlParser(InputStream is) {
        this.loadMaterial(is);
    }

    private void loadMaterial(InputStream inputStream) throws ModelFormatException {
        if (inputStream == null) {
            return;
        }

        BufferedReader reader = null;
        String currentLine;
        int lineCount = 0;
        this.materials.clear();

        try {
            reader = new BufferedReader(new InputStreamReader(inputStream));

            while ((currentLine = reader.readLine()) != null) {
                lineCount++;
                currentLine = currentLine.replaceAll("\\s+", " ").trim();

				if (currentLine.length() == 0 || currentLine.startsWith("#")) {
				} else if (currentLine.startsWith("newmtl ")) {
					String[] sa = currentLine.split(" ");
					this.currentMaterial = new Material((byte) this.materials.size(), null);
					this.materials.put(sa[1], this.currentMaterial);
				}

				//Tr:透過
			}
		} catch (IOException e) {
			throw new ModelFormatException("IO Exception reading model format", e);
		} finally {
            try {
                reader.close();
            } catch (IOException ignored) {
            }
            try {
                inputStream.close();
            } catch (IOException ignored) {
            }
		}
	}

	public Map<String, Material> getMaterials() {
		return this.materials;
	}
}