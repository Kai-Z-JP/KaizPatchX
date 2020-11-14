package jp.ngt.mcte.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.mcte.MCTEKeyHandlerClient;
import jp.ngt.mcte.world.WorldData;
import jp.ngt.ngtlib.gui.GuiScreenCustom;
import jp.ngt.ngtlib.gui.GuiSelect;
import jp.ngt.ngtlib.gui.GuiSelect.ISelector;
import jp.ngt.ngtlib.gui.GuiSelect.SlotElementItem;
import jp.ngt.ngtlib.gui.GuiSlotCustom;
import jp.ngt.ngtlib.gui.GuiSlotCustom.SlotElement;
import jp.ngt.ngtlib.io.NGTFileLoader;
import jp.ngt.ngtlib.io.NGTImage;
import jp.ngt.ngtlib.io.NGTImage.Thumbnail;
import jp.ngt.ngtlib.math.NGTMath;
import jp.ngt.ngtlib.util.KeyboardUtil;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiCreateWorld;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.biome.BiomeGenBase;
import org.lwjgl.opengl.GL11;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

@SideOnly(Side.CLIENT)
public class GuiCreatePictorialWorld extends GuiScreenCustom {
	protected static Map<String, ItemStack> biomeIconMap = new TreeMap<>();

	private final GuiCreateWorld createWorldGui;
	private WorldData worldData;
	private final Thumbnail[] thumbnail = new Thumbnail[2];
	private final String[] imgName = {"", ""};
	private final GuiButton[] selectButtons = new GuiButton[3];
	private GuiTextField minYTF;
	private GuiTextField yScaleTF;
	private GuiTextField seaLevelTF;
	private GuiSlotCustom slotCustom;
	private SlotElement[] slotElements = new SlotElement[0];

	private boolean canceled = false;

	public GuiCreatePictorialWorld(GuiCreateWorld par1) {
		this.createWorldGui = par1;
	}

	@Override
	public void initGui() {
		super.initGui();

		this.worldData = new WorldData(this.createWorldGui.field_146334_a);
		int i0 = ((this.height - 30) / this.thumbnail.length) + 80;

		this.buttonList.clear();
		this.buttonList.add(new GuiButton(0, this.width / 2 - 155, this.height - 28, 150, 20, I18n.format("gui.done")));
		this.buttonList.add(new GuiButton(1, this.width / 2 + 5, this.height - 28, 150, 20, I18n.format("gui.cancel")));

		this.selectButtons[0] = new GuiButton(10, i0, 0, 40, 20, I18n.format("gui.mcte.select"));
		this.buttonList.add(this.selectButtons[0]);
		this.selectButtons[1] = new GuiButton(11, i0, 20, 40, 20, I18n.format("gui.mcte.select"));
		this.buttonList.add(this.selectButtons[1]);
		this.selectButtons[2] = new GuiButton(12, i0, 40, 40, 20, I18n.format("gui.mcte.select"));
		this.buttonList.add(this.selectButtons[2]);

		this.textFields.clear();
		this.minYTF = this.setTextField(i0, 60, 40, 20, String.valueOf(this.worldData.minY));
		this.yScaleTF = this.setTextField(i0, 80, 40, 20, String.valueOf(this.worldData.yScale));
		this.seaLevelTF = this.setTextField(i0, 100, 40, 20, String.valueOf(this.worldData.seaLevel));
		this.currentTextField = this.minYTF;

		this.slotList.clear();
		int i1 = this.width - 55 - i0;
		this.slotCustom = new GuiSlotCustom(this, 0, this.height - 30, i0 + 45, this.width - 10, i1, 24, this.slotElements);
		this.slotList.add(this.slotCustom);

		if (this.worldData.terrainImagePath != null) {
			this.setImageFile(0, new File(this.worldData.terrainImagePath));
		}
		if (this.worldData.biomesImagePath != null) {
			this.setImageFile(1, new File(this.worldData.biomesImagePath));
		}
	}

	@Override
	public void onGuiClosed() {
		super.onGuiClosed();

		if (this.canceled) {
			if (this.createWorldGui.field_146334_a != null) {
				File file = new File(this.createWorldGui.field_146334_a);
				if (file.exists()) {
					file.delete();
				}
			}
			this.createWorldGui.field_146334_a = "";
		} else {
			this.writeTextFieldsToWorldData();
			this.createWorldGui.field_146334_a = this.worldData.saveWorldData(this.createWorldGui.field_146334_a);
		}
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id == 0) {
			this.mc.displayGuiScreen(this.createWorldGui);
		} else if (button.id == 1) {
			this.canceled = true;
			this.mc.displayGuiScreen(this.createWorldGui);
		} else if (button.id == 10) {
			this.selectImageFile(0);
		} else if (button.id == 11) {
			this.selectImageFile(1);
		} else if (button.id == 12) {
			this.selectBlock();
		}

		super.actionPerformed(button);
	}

	@Override
	protected void keyTyped(char par1, int par2) {
		if (par2 == 1 || par2 == this.mc.gameSettings.keyBindInventory.getKeyCode() || par2 == MCTEKeyHandlerClient.keyEditMenu.getKeyCode())//1:Esc
		{
			this.mc.thePlayer.closeScreen();
		}

		if (KeyboardUtil.isIntegerKey(par2))//14:Back, 211:Del
		{
			this.currentTextField.textboxKeyTyped(par1, par2);
		}

		if (par2 == 28) {
			this.writeTextFieldsToWorldData();
		}
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		int i0 = (this.height - 30) / this.thumbnail.length;

		this.drawDefaultBackground();
		super.drawScreen(par1, par2, par3);

		this.drawString(this.fontRendererObj, I18n.format("gui.mcte.terrainImage"), i0 + 5, 5, 0xffffff);
		this.drawString(this.fontRendererObj, I18n.format("gui.mcte.biomesImage"), i0 + 5, 25, 0xffffff);
		this.drawString(this.fontRendererObj, I18n.format("gui.mcte.baseBlock"), i0 + 5, 45, 0xffffff);

		this.drawString(this.fontRendererObj, I18n.format("gui.mcte.minY"), i0 + 5, 65, 0xffffff);
		this.drawString(this.fontRendererObj, I18n.format("gui.mcte.yScale"), i0 + 5, 85, 0xffffff);
		this.drawString(this.fontRendererObj, I18n.format("gui.mcte.seaLevel"), i0 + 5, 105, 0xffffff);


		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		IntStream.range(0, this.thumbnail.length).filter(i -> this.thumbnail[i] != null).forEach(i -> {
			double d1 = i0 * i;
			this.thumbnail[i].bindTexture();
			Tessellator tessellator = Tessellator.instance;
			tessellator.startDrawingQuads();
			tessellator.addVertexWithUV(i0, (double) i0 + d1, this.zLevel, 1.0D, 1.0D);
			tessellator.addVertexWithUV(i0, 0.0D + d1, this.zLevel, 1.0D, 0.0D);
			tessellator.addVertexWithUV(0.0D, 0.0D + d1, this.zLevel, 0.0D, 0.0D);
			tessellator.addVertexWithUV(0.0D, (double) i0 + d1, this.zLevel, 0.0D, 1.0D);
			tessellator.draw();
		});
	}

	private void selectBlock() {
		Iterator<Block> iterator0 = Block.blockRegistry.iterator();
		List<ItemStack> list0 = new ArrayList<>();
		while (iterator0.hasNext()) {
			Block block = iterator0.next();
			Item item = Item.getItemFromBlock(block);
			if (item != null) {
				block.getSubBlocks(item, CreativeTabs.tabAllSearch, list0);
			}
		}

		List<ItemStack> list1 = new ArrayList<>();
		list0.forEach(stack -> {
			Block block = Block.getBlockFromItem(stack.getItem());
			if (stack.getItemDamage() == 0 && block != null && !block.hasTileEntity(stack.getItemDamage())) {
				list1.add(stack);
			}
		});

		ISelector selector = par1 -> GuiCreatePictorialWorld.this.worldData.baseBlock = (Block) par1;

		SlotElementItem[] slots = new SlotElementItem[list1.size()];
		IntStream.range(0, slots.length).forEach(i -> {
			ItemStack stack = list1.get(i);
			Block block = Block.getBlockFromItem(stack.getItem());
			String s = stack.getDisplayName();
			slots[i] = new SlotElementItem<>(selector, block, s, stack);
		});

		this.mc.displayGuiScreen(new GuiSelect(this, slots));
	}

	private void selectImageFile(int par1) {
		File file = NGTFileLoader.selectFile(new String[][]{{"PNG_File", "png"}});
		if (file == null) {
			//this.prop.setProperty(par1, "");
		} else {
			this.setImageFile(par1, file);
		}
	}

	private void setImageFile(int index, File file) {
		if (this.imgName[index].equals(file.getAbsolutePath()))//同じファイルを選択した場合
		{
			return;
		}

		try {
			BufferedImage image = ImageIO.read(file);
			if (this.thumbnail[index] != null) {
				this.thumbnail[index].deleteTexture();
			}

			this.thumbnail[index] = new Thumbnail(image, 256, 256);
			this.imgName[index] = file.getAbsolutePath();
			if (index == 1) {
				this.setBiomeButtons(image);
			}

			switch (index) {
				case 0:
					this.worldData.terrainImagePath = file.getAbsolutePath();
					break;
				case 1:
					this.worldData.biomesImagePath = file.getAbsolutePath();
					break;
			}
		} catch (IOException ignored) {
		}
	}

	private void setBiomeButtons(BufferedImage image) {
		if (image.getType() != BufferedImage.TYPE_4BYTE_ABGR) {
			if (this.slotCustom != null) {
				this.slotList.remove(this.slotCustom);
				this.slotElements = new SlotElement[0];
			}
			return;
		}

		Map<Integer, BiomeGenBase> map = new TreeMap<>();
		int[] ia = new int[4];//{RGBA}
		for (int i = 0; i < image.getWidth(); ++i) {
			for (int j = 0; j < image.getHeight(); ++j) {
				image.getRaster().getPixel(i, j, ia);
				int color = NGTImage.getIntFromARGB(ia[3], ia[0], ia[1], ia[2]) & 0xffffff;
				if (!map.containsKey(color)) {
					BiomeGenBase b0 = BiomeGenBase.plains;
					if (this.worldData.biomeMap.containsKey(color)) {
						b0 = this.worldData.biomeMap.get(color);
					}
					map.put(color, b0);
				}
			}
		}

		int i = 0;
		SlotBiomeColor[] sb = new SlotBiomeColor[map.size()];
		Set<Entry<Integer, BiomeGenBase>> set = map.entrySet();
		for (Entry<Integer, BiomeGenBase> entry : set) {
			sb[i] = new SlotBiomeColor(this, entry.getKey(), entry.getValue());
			++i;
		}

		this.slotCustom.setElements(sb);
		this.slotElements = sb;
	}

	private void writeTextFieldsToWorldData() {
		this.worldData.minY = NGTMath.getIntFromString(this.minYTF.getText(), 0, 255, 0);
		this.minYTF.setText(String.valueOf(this.worldData.minY));
		this.worldData.yScale = NGTMath.getFloatFromString(this.yScaleTF.getText(), 0.00390625F, 256.0F, 1.0F);
		this.yScaleTF.setText(String.valueOf(this.worldData.yScale));
		this.worldData.seaLevel = NGTMath.getIntFromString(this.seaLevelTF.getText(), 0, 255, 64);
		this.seaLevelTF.setText(String.valueOf(this.worldData.seaLevel));
	}

	@SideOnly(Side.CLIENT)
	public class SlotBiomeColor extends SlotElement implements ISelector {
		public final int color;
		public BiomeGenBase biome;

		public SlotBiomeColor(GuiCreatePictorialWorld par1, int par2, BiomeGenBase par3) {
			this.color = par2;
			this.biome = par3;
		}

		@Override
		public void draw(Minecraft par1, int par2, int par3, float par4) {
			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			this.drawModalRect(par2 + 1, par3 + 1, 18, 18, par4, this.color);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			par1.fontRenderer.drawString(this.biome.biomeName, par2 + 23, par3 + 6, 0xffffff);
		}

		protected void drawModalRect(int par1, int par2, int par3, int par4, float par5, int color) {
			Tessellator tessellator = Tessellator.instance;
			tessellator.startDrawingQuads();
			tessellator.setColorOpaque_I(color);
			tessellator.addVertex(par1, par2 + par4, par5);
			tessellator.addVertex(par1 + par3, par2 + par4, par5);
			tessellator.addVertex(par1 + par3, par2, par5);
			tessellator.addVertex(par1, par2, par5);
			tessellator.draw();
		}

		@Override
		public void onClicked(int par1, boolean par2) {
			if (par2) {
				List<BiomeGenBase> list = IntStream.range(0, BiomeGenBase.getBiomeGenArray().length).filter(i -> BiomeGenBase.getBiomeGenArray()[i] != null).mapToObj(i -> BiomeGenBase.getBiomeGenArray()[i]).collect(Collectors.toList());

				SlotElementItem[] slots = new SlotElementItem[list.size()];
				IntStream.range(0, slots.length).forEach(i -> {
					BiomeGenBase b0 = list.get(i);
					if (biomeIconMap.containsKey(b0.biomeName)) {
						ItemStack stack = biomeIconMap.get(b0.biomeName);
						slots[i] = new SlotElementItem<>(this, b0, b0.biomeName, stack);
					} else {
						slots[i] = new SlotElementItem<>(this, b0, b0.biomeName, new ItemStack(b0.topBlock, 1, 0));
					}
				});

				GuiCreatePictorialWorld.this.mc.displayGuiScreen(new GuiSelect(GuiCreatePictorialWorld.this, slots));
			}
		}

		@Override
		public void select(Object par1) {
			this.biome = (BiomeGenBase) par1;
			GuiCreatePictorialWorld.this.worldData.biomeMap.put(this.color, this.biome);
		}
	}

	static {
		biomeIconMap.put(BiomeGenBase.ocean.biomeName, new ItemStack(Blocks.water, 1, 0));
		//biomeIconMap.put(BiomeGenBase.plains.biomeName, new BlockSet(Blocks.grass, 0));
		//biomeIconMap.put(BiomeGenBase.desert.biomeName, new BlockSet(Blocks.water, 0));
		biomeIconMap.put(BiomeGenBase.extremeHills.biomeName, new ItemStack(Blocks.stone, 1, 0));
		biomeIconMap.put(BiomeGenBase.forest.biomeName, new ItemStack(Blocks.leaves, 1, 0));
		biomeIconMap.put(BiomeGenBase.taiga.biomeName, new ItemStack(Blocks.leaves, 1, 1));
		biomeIconMap.put(BiomeGenBase.swampland.biomeName, new ItemStack(Blocks.waterlily, 1, 0));
		biomeIconMap.put(BiomeGenBase.river.biomeName, new ItemStack(Blocks.water, 1, 0));
		biomeIconMap.put(BiomeGenBase.hell.biomeName, new ItemStack(Blocks.netherrack, 1, 0));
		biomeIconMap.put(BiomeGenBase.sky.biomeName, new ItemStack(Blocks.end_stone, 1, 0));
		biomeIconMap.put(BiomeGenBase.frozenOcean.biomeName, new ItemStack(Blocks.ice, 1, 0));
		biomeIconMap.put(BiomeGenBase.frozenRiver.biomeName, new ItemStack(Blocks.ice, 1, 0));
		//biomeIconMap.put(BiomeGenBase.icePlains.biomeName, new BlockSet(Blocks.snow, 0));
		//biomeIconMap.put(BiomeGenBase.iceMountains.biomeName, new BlockSet(Blocks.snow, 0));
		//biomeIconMap.put(BiomeGenBase.mushroomIsland.biomeName, new BlockSet(Blocks.leaves, 0));
		//biomeIconMap.put(BiomeGenBase.mushroomIslandShore.biomeName, new BlockSet(Blocks.leaves, 0));
		biomeIconMap.put(BiomeGenBase.beach.biomeName, new ItemStack(Blocks.sand, 1, 0));
		//biomeIconMap.put(BiomeGenBase.desertHills.biomeName, new BlockSet(Blocks.leaves, 0));
		biomeIconMap.put(BiomeGenBase.forestHills.biomeName, new ItemStack(Blocks.leaves, 1, 0));
		biomeIconMap.put(BiomeGenBase.taigaHills.biomeName, new ItemStack(Blocks.leaves, 1, 1));
		biomeIconMap.put(BiomeGenBase.extremeHillsEdge.biomeName, new ItemStack(Blocks.stone, 1, 0));
		biomeIconMap.put(BiomeGenBase.jungle.biomeName, new ItemStack(Blocks.leaves, 1, 3));
		biomeIconMap.put(BiomeGenBase.jungleHills.biomeName, new ItemStack(Blocks.leaves, 1, 3));
		biomeIconMap.put(BiomeGenBase.jungleEdge.biomeName, new ItemStack(Blocks.leaves, 1, 3));
		biomeIconMap.put(BiomeGenBase.deepOcean.biomeName, new ItemStack(Blocks.water, 1, 0));
		biomeIconMap.put(BiomeGenBase.stoneBeach.biomeName, new ItemStack(Blocks.stone, 1, 0));
		biomeIconMap.put(BiomeGenBase.coldBeach.biomeName, new ItemStack(Blocks.ice, 1, 0));
		biomeIconMap.put(BiomeGenBase.birchForest.biomeName, new ItemStack(Blocks.leaves, 1, 2));
		biomeIconMap.put(BiomeGenBase.birchForestHills.biomeName, new ItemStack(Blocks.leaves, 1, 2));
		biomeIconMap.put(BiomeGenBase.roofedForest.biomeName, new ItemStack(Blocks.leaves, 1, 0));
		biomeIconMap.put(BiomeGenBase.coldTaiga.biomeName, new ItemStack(Blocks.snow, 1, 0));
		biomeIconMap.put(BiomeGenBase.coldTaigaHills.biomeName, new ItemStack(Blocks.snow, 1, 0));
		biomeIconMap.put(BiomeGenBase.megaTaiga.biomeName, new ItemStack(Blocks.leaves, 1, 1));
		biomeIconMap.put(BiomeGenBase.megaTaigaHills.biomeName, new ItemStack(Blocks.leaves, 1, 1));
		biomeIconMap.put(BiomeGenBase.extremeHillsPlus.biomeName, new ItemStack(Blocks.stone, 1, 0));
		//biomeIconMap.put(BiomeGenBase.savanna.biomeName, new BlockSet(Blocks.leaves, 0));
		//biomeIconMap.put(BiomeGenBase.savannaPlateau.biomeName, new BlockSet(Blocks.leaves, 0));
		biomeIconMap.put(BiomeGenBase.mesa.biomeName, new ItemStack(Blocks.hardened_clay, 1, 0));
		biomeIconMap.put(BiomeGenBase.mesaPlateau_F.biomeName, new ItemStack(Blocks.hardened_clay, 1, 0));
		biomeIconMap.put(BiomeGenBase.mesaPlateau.biomeName, new ItemStack(Blocks.hardened_clay, 1, 0));
	}
}