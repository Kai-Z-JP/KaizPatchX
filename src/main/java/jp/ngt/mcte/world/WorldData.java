package jp.ngt.mcte.world;

import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.ngtlib.util.Config;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.world.World;
import net.minecraft.world.biome.BiomeGenBase;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeMap;

public class WorldData {
	//private Properties prop = new Properties();
	private final Config cfg = new Config();
	private WorldGenerator generator;
	public Map<Integer, BiomeGenBase> biomeMap = new TreeMap<Integer, BiomeGenBase>();//getが早い
	public Block baseBlock = Blocks.stone;
	public String terrainImagePath;
	public String biomesImagePath;
	public int minY;
	public float yScale = 1.0F;//>0.0
	public int seaLevel = 63;
	//public int skyColor;

	/**
	 * @param par1 コンフィグ一時ファイルのパス
	 */
	public WorldData(String par1) {
		File file = new File(par1);
		try {
			this.loadWorldData(file);
		} catch (IOException e) {
		}
	}

	/**
	 * @param par1 MCTEフォルダ
	 * @param par2 コンフィグ一時ファイルのパス(null OK)
	 * @throws IOException
	 */
	protected WorldData(File par1, String par2) throws IOException {
		if (par1.exists() || par2 == null) {
			this.init(par1, false);
		} else {
			par1.mkdir();
			File tmpFile = new File(par2);
			File cfgFile = new File(par1, "mcte.cfg");
			Files.copy(tmpFile.toPath(), cfgFile.toPath());
			tmpFile.delete();
			this.init(par1, true);
		}
	}

	/**
	 * @param par1 MCTEフォルダ
	 * @param par2 初期化処理(画像のコピー)
	 * @throws IOException
	 */
	private void init(File par1, boolean par2) throws IOException {
		File cfgFile = new File(par1, "mcte.cfg");
		this.loadWorldData(cfgFile);
		File terrainImg = new File(par1, "terrain.png");
		File biomesImg = new File(par1, "biomes.png");

		if (par2) {
			if (this.cfg.containsKey("Image", "terrain")) {
				File imgFile = new File(this.cfg.getProperty("Image", "terrain"));
				Files.copy(imgFile.toPath(), terrainImg.toPath());
			}

			if (this.cfg.containsKey("Image", "biomes")) {
				File imgFile = new File(this.cfg.getProperty("Image", "biomes"));
				Files.copy(imgFile.toPath(), biomesImg.toPath());
			}
		}

		this.generator = new WorldGenerator(terrainImg, biomesImg);
	}

	public static void getBiomeColorMapFromString(Map<Integer, BiomeGenBase> par1, String par2) {
		if (par2 == null || par2.length() == 0) {
			return;
		}

		String[] sa0 = par2.split(",");
		for (String s : sa0) {
			String[] sa1 = s.split(":");
			if (sa1.length == 2) {
				try {
					int color = Integer.decode(sa1[0]);
					BiomeGenBase biome = getBiomeFromName(sa1[1]);
					par1.put(color, biome);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public static String getStringFromBiomeColorMap(Map<Integer, BiomeGenBase> par1) {
		StringBuilder sb = new StringBuilder();
		Set<Entry<Integer, BiomeGenBase>> set = par1.entrySet();
		for (Entry<Integer, BiomeGenBase> entry : set) {
			sb.append(String.format("0x%06x", entry.getKey())).append(":").append(entry.getValue().biomeName).append(",");
		}
		return sb.toString();
	}

	/**
	 * 該当するものがない場合、"Plains"を返す
	 */
	public static BiomeGenBase getBiomeFromName(String par1) {
		BiomeGenBase[] ba = BiomeGenBase.getBiomeGenArray();
		for (BiomeGenBase biome : ba) {
			if (biome != null && biome.biomeName.equals(par1)) {
				return biome;
			}
		}
		return BiomeGenBase.plains;
	}

	public static WorldData getWorldData(World world, String cfgPath) {
		File saveDir = world.getSaveHandler().getWorldDirectory();
		File mcteDir = new File(saveDir, "mcte");
		if (mcteDir.exists()) {
			try {
				return new WorldData(mcteDir, null);
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			if (cfgPath != null && cfgPath.length() > 0) {
				try {
					return new WorldData(mcteDir, cfgPath);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}

		return null;
	}

	private void loadWorldData(File par1) throws IOException {
		//this.prop.load(new FileInputStream(par1));
		this.cfg.load(par1);
		this.terrainImagePath = this.cfg.getProperty("Image", "terrain");
		this.biomesImagePath = this.cfg.getProperty("Image", "biomes");
		this.minY = NGTMath.getIntFromString(this.cfg.getProperty("World", "minY"), 0, 255, 0);
		this.yScale = NGTMath.getFloatFromString(this.cfg.getProperty("World", "yScale"), 0.00390625F, 256.0F, 1.0F);
		this.seaLevel = NGTMath.getIntFromString(this.cfg.getProperty("World", "seaLevel"), 0, 256, 63);
		getBiomeColorMapFromString(this.biomeMap, this.cfg.getProperty("Biome", "biome_color"));
		this.baseBlock = Block.getBlockFromName(this.cfg.getProperty("Block", "baseBlock"));
		if (this.baseBlock == null) {
			this.baseBlock = Blocks.stone;
		}
	}

	/**
	 * @param par1 コンフィグ一時ファイルのパス
	 */
	public String saveWorldData(String par1) {
		if (this.terrainImagePath != null) {
			this.cfg.setProperty("Image", "terrain", this.terrainImagePath);
		}
		if (this.biomesImagePath != null) {
			this.cfg.setProperty("Image", "biomes", this.biomesImagePath);
		}
		this.cfg.setProperty("World", "minY", String.valueOf(this.minY));
		this.cfg.setProperty("World", "yScale", String.valueOf(this.yScale));
		this.cfg.setProperty("World", "seaLevel", String.valueOf(this.seaLevel));
		this.cfg.setProperty("Biome", "biome_color", getStringFromBiomeColorMap(this.biomeMap));
		this.cfg.setProperty("Block", "baseBlock", Block.blockRegistry.getNameForObject(this.baseBlock));

		try {
			File file = new File(par1);
			if (!file.exists()) {
				file = File.createTempFile("mcte", ".cfg.tmp");
			}
			//this.prop.store(new FileOutputStream(file), null);
			this.cfg.save(file);
			return file.getAbsolutePath();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return "";
	}

	public WorldGenerator getWorldGenerator() {
		return this.generator;
	}

	protected class WorldGenerator {
		private BufferedImage terrain;
		private BufferedImage biomes;
		private boolean genTerrain;
		private boolean genBiomes;

		/**
		 * null OK
		 *
		 * @param par1 Terrain
		 * @param par2 Biomes
		 */
		public WorldGenerator(File par1, File par2) {
			try {
				this.terrain = ImageIO.read(par1);
				this.genTerrain = true;
			} catch (IOException e) {
				this.genTerrain = false;
			} catch (IllegalArgumentException e) {
				this.genTerrain = false;
			}

			try {
				this.biomes = ImageIO.read(par2);
				this.genBiomes = true;
			} catch (IOException e) {
				this.genBiomes = false;
			} catch (IllegalArgumentException e) {
				this.genBiomes = false;
			}
		}

		public boolean hasTerrainData() {
			return this.genTerrain;
		}

		public boolean hasBiomesData() {
			return this.genBiomes;
		}

		public int getHeight(int x, int z) {
			int height = this.getColor(this.terrain, x, z, 0xff);
			height = (int) ((float) height * WorldData.this.yScale) + WorldData.this.minY;
			if (height > 255) {
				return 255;
			}
			return height;
		}

		public BiomeGenBase getBiome(int x, int z) {
			int color = this.getColor(this.biomes, x, z, 0xffffff);
			if (WorldData.this.biomeMap.containsKey(color)) {
				return WorldData.this.biomeMap.get(color);
			}
			return BiomeGenBase.plains;
		}

		protected int getColor(BufferedImage img, int x, int z, int mask) {
			int w0 = img.getWidth() >> 1;
			int h0 = img.getHeight() >> 1;
			x += w0;
			z += h0;
			if (x >= 0 && x < img.getWidth() && z >= 0 && z < img.getHeight()) {
				return img.getRGB(x, z) & mask;
			}
			return 0x000000;
		}
	}
}