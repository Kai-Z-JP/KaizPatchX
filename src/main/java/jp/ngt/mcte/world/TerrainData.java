package jp.ngt.mcte.world;

import jp.ngt.mcte.MCTE;
import jp.ngt.mcte.network.PacketGenerator;
import jp.ngt.ngtlib.block.BlockSet;
import jp.ngt.ngtlib.io.NGTImage;
import net.minecraft.block.Block;
import net.minecraft.init.Blocks;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.TreeMap;

public class TerrainData {
	private static final BlockSet default_BlockSet = new BlockSet(Blocks.stone, 0);

	public File terrainFile;
	public File blocksFile;
	public Map<Integer, BlockSet> blockMap = new TreeMap<>();
	public float yScale = 1.0F;

	{
		this.blockMap.put(0x000000, default_BlockSet);
	}

	public void generate(int x, int y, int z) {
		BufferedImage terrain = null;
		if (this.terrainFile != null) {
			try {
				terrain = ImageIO.read(this.terrainFile);
			} catch (IOException ignored) {
			}
		}

		BufferedImage blocks = null;
		if (this.blocksFile != null) {
			try {
				blocks = ImageIO.read(this.blocksFile);
			} catch (IOException ignored) {
			}
		}

		int height = 0;
		int width = 0;
		if (terrain != null) {
			height = terrain.getHeight();
			width = terrain.getWidth();
		} else if (blocks != null) {
			height = blocks.getHeight();
			width = blocks.getWidth();
		}

		for (int i = 0; i < height; ++i) {
			for (int j = 0; j < width; ++j) {
				int color0;
				int value = 1;
				if (terrain != null) {
					color0 = terrain.getRGB(j, i) & 0xFFFFFF;
					value = NGTImage.getColorValue(color0);
					value = (int) ((float) value * this.yScale);
					if (value > 255) {
						value = 255;
					}
				}

				int color1 = 0x000000;
				if (blocks != null) {
					color1 = blocks.getRGB(j, i) & 0xFFFFFF;
				}
				BlockSet set = this.blockMap.get(color1);
				if (set == null) {
					set = default_BlockSet;
				}

				for (int k = 0; k < value; ++k) {
					int x0 = x + j;
					int y0 = y + k;
					int z0 = z + i;
					MCTE.NETWORK_WRAPPER.sendToServer(new PacketGenerator(x0, y0, z0, Block.blockRegistry.getNameForObject(set.block), set.metadata));
				}
			}
		}
	}

	/*public static TerrainData getTerrainDataFromText(File file)
	{
		String[] sa = NGTText.readText(file);
		if(sa == null || sa.length < 1)
		{
			return null;
		}

		int dataFormat = sa[0].equals("NGT_Terrain_Data_0") ? 0 : 1;
		int dataType = 0;
		if(dataFormat == 0)
		{
			BlockSet set = null;

			for(String s : sa)
			{
				dataType = s.equals("#Info") ? Data_Info : (s.equals("#Block") ? Data_Block : dataType);

				if(dataType == Data_Info)
				{
					;
				}
				else if(dataType == Data_Block)
				{
					set = getBlockAndColor(s);
				}
			}

			BufferedImage img = getImageFromText(file);
			if(set != null && img != null)
			{
				return new TerrainDataV(img, set);
			}
		}
		else if(dataFormat == 1)
		{
			Map<Integer, BlockSet> map = new HashMap<Integer, BlockSet>();

			for(String s : sa)
			{
				dataType = s.equals("#Info") ? Data_Info : (s.equals("#Block") ? Data_Block : dataType);

				if(dataType == Data_Info)
				{
					;
				}
				else if(dataType == Data_Block)
				{
					BlockSet set = getBlockAndColor(s);
					if(set != null)
					{
						map.put(set.x, set);
					}
				}
			}

			BufferedImage img = getImageFromText(file);
			if(map.size() > 0 && img != null)
			{
				return new TerrainDataImage(img, map);
			}
		}

		return null;
	}*/

	/**
	 * BlockSet.x = color
	 */
	private static BlockSet getBlockAndColor(String s) {
		String[] sa0 = s.split(":");
		if (sa0 != null && sa0.length == 2) {
			String[] sa1 = sa0[1].split(",");
			if (sa1 != null && sa1.length == 2) {
				try {
					int color = Integer.decode(sa0[0]);
					Block block = Block.getBlockFromName(sa1[0]);
					int meta = Integer.parseInt(sa1[1]);
					return new BlockSet(color, 0, 0, block, meta);
				} catch (NumberFormatException e) {
					e.printStackTrace();
				}
			}
		}
		return null;
	}

	private static BufferedImage getImageFromText(File file) {
		String imgName = file.getName().replace(".txt", ".png");
		File imgFile = new File(file.getParentFile(), imgName);
		try {
			return ImageIO.read(imgFile);
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	/*public static class TerrainDataImage extends TerrainData
	{
		private final BufferedImage image;
		private final Map<Integer, BlockSet> blockMap;

		public TerrainDataImage(BufferedImage par1Image, Map<Integer, BlockSet> par2Map)
		{
			//super(par1Image.getWidth(), 1, par1Image.getHeight());
			this.image = par1Image;
			this.blockMap = par2Map;
		}

		@Override
		public void generate(int x, int y, int z)
		{
			for(int i = 0; i < image.getHeight(); ++i)
			{
				for(int j = 0; j < image.getWidth(); ++j)
				{
					int color = image.getRGB(j, i) & 0xFFFFFF;
					BlockSet set = this.blockMap.get(color);
					if(set != null)
					{
						int x0 = x + j;
						int z0 = z + i;
						MCTE.NETWORK_WRAPPER.sendToServer(new PacketGenerator(x0, y, z0, Block.blockRegistry.getNameForObject(set.block), (byte)set.metadata));
					}
				}
			}
		}
	}*/

	/*public static class TerrainDataV extends TerrainData
	{
		private final BufferedImage image;
		private final BlockSet blockSet;

		public TerrainDataV(BufferedImage par1Image, BlockSet par2BlockSet)
		{
			//super(par1Image.getWidth(), 255, par1Image.getHeight());
			this.image = par1Image;
			this.blockSet = par2BlockSet;
		}

		@Override
		public void generate(int x, int y, int z)
		{
			for(int i = 0; i < image.getHeight(); ++i)
			{
				for(int j = 0; j < image.getWidth(); ++j)
				{
					int color = image.getRGB(j, i);
					int value = NGTImage.getColorValue(color) / 8;
					if(value > 255)
					{
						value = 255;
					}

					for(int k = 0; k < value; ++k)
					{
						int x0 = x + j;
						int y0 = y + k;
						int z0 = z + i;
						MCTE.NETWORK_WRAPPER.sendToServer(new PacketGenerator(x0, y0, z0, Block.blockRegistry.getNameForObject(this.blockSet.block), (byte)this.blockSet.metadata));
					}
				}
			}
		}
	}*/
}