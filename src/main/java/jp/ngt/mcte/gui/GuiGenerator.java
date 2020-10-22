package jp.ngt.mcte.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.mcte.world.TerrainData;
import jp.ngt.ngtlib.block.BlockSet;
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
import jp.ngt.ngtlib.renderer.NGTRenderHelper;
import net.minecraft.block.Block;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.GuiTextField;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.resources.I18n;
import net.minecraft.creativetab.CreativeTabs;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.world.World;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.Map.Entry;

@SideOnly(Side.CLIENT)
public class GuiGenerator extends GuiScreenCustom {
	protected final int xPos;
	protected final int yPos;
	protected final int zPos;
	TerrainData terrainData = new TerrainData();
	private Thumbnail[] thumbnail = new Thumbnail[2];
	private String[] imgName = {"", ""};
	private GuiButton[] selectButtons = new GuiButton[2];
	private GuiTextField yScaleTF;
	private GuiSlotCustom slotCustom;
	private SlotElement[] slotElements;

	{
		this.slotElements = new SlotElement[1];
		ItemStack stack = new ItemStack(Blocks.stone, 1, 0);
		SlotBlockColor element = new SlotBlockColor(this, 0x000000, stack);
		this.slotElements[0] = element;
	}

	public GuiGenerator(World world, int x, int y, int z) {
		//super(new ContainerGenerator(world, x, y, z));
		this.xPos = x;
		this.yPos = y;
		this.zPos = z;
	}

	@Override
	public void initGui() {
		this.buttonList.clear();
		int i0 = ((this.height - 30) / this.thumbnail.length) + 80;
		this.selectButtons[0] = new GuiButton(120, i0, 0, 40, 20, I18n.format("gui.mcte.select", new Object[0]));
		this.buttonList.add(this.selectButtons[0]);
		this.selectButtons[1] = new GuiButton(121, i0, 20, 40, 20, I18n.format("gui.mcte.select", new Object[0]));
		this.buttonList.add(this.selectButtons[1]);

		this.textFields.clear();
		this.yScaleTF = this.setTextField(i0, 40, 40, 20, String.valueOf(this.terrainData.yScale));

		this.buttonList.add(new GuiButton(100, this.width / 2 - 155, this.height - 28, 150, 20, I18n.format("gui.mcte.generate", new Object[0])));
		this.buttonList.add(new GuiButton(101, this.width / 2 + 5, this.height - 28, 150, 20, I18n.format("gui.cancel", new Object[0])));

		this.slotList.clear();
		int i1 = this.width - 55 - i0;
		this.slotCustom = new GuiSlotCustom(this, 0, this.height - 30, i0 + 45, this.width - 10, i1, 24, this.slotElements);
		this.slotList.add(this.slotCustom);
	}

	@Override
	protected void actionPerformed(GuiButton button) {
		if (button.id == 100) {
			this.terrainData.yScale = NGTMath.getFloatFromString(this.yScaleTF.getText(), 0.00390625F, 256.0F, 1.0F);
			this.yScaleTF.setText(String.valueOf(this.terrainData.yScale));

			this.terrainData.generate(this.xPos, this.yPos, this.zPos);
			this.mc.thePlayer.closeScreen();
		} else if (button.id == 101) {
			this.mc.thePlayer.closeScreen();
		} else if (button.id == 120) {
			this.selectImageFile(0);
		} else if (button.id == 121) {
			this.selectImageFile(1);
		}
	}

	private void selectImageFile(int par1) {
		File file = NGTFileLoader.selectFile(new String[][]{{"PNG_File", "png"}});
		if (file == null) {
			;
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
				this.setBlockButtons(image);
			}

			switch (index) {
				case 0:
					this.terrainData.terrainFile = file;
					break;
				case 1:
					this.terrainData.blocksFile = file;
					break;
			}
		} catch (IOException e) {
		}
	}

	private void setBlockButtons(BufferedImage image) {
		if (image.getType() != BufferedImage.TYPE_4BYTE_ABGR) {
			if (this.slotCustom != null) {
				this.slotList.remove(this.slotCustom);
				this.slotElements = new SlotElement[0];
			}
			return;
		}

		Map<Integer, BlockSet> map = new TreeMap<Integer, BlockSet>();
		int[] ia = new int[4];//{RGBA}
		for (int i = 0; i < image.getWidth(); ++i) {
			for (int j = 0; j < image.getHeight(); ++j) {
				image.getRaster().getPixel(i, j, ia);
				int color = NGTImage.getIntFromARGB(ia[3], ia[0], ia[1], ia[2]) & 0xffffff;
				if (!map.containsKey(color)) {
					BlockSet b0 = new BlockSet(Blocks.stone, 0);
					/*if(this.terrainData.blockMap.containsKey(color))
					{
						b0 = this.terrainData.blockMap.get(color);
					}*/
					map.put(color, b0);
				}
			}
		}

		int i0 = ((this.height - 30) / this.thumbnail.length) + 10;
		int i = 0;
		SlotBlockColor[] sb = new SlotBlockColor[map.size()];
		Set<Entry<Integer, BlockSet>> set = map.entrySet();
		for (Entry<Integer, BlockSet> entry : set) {
			BlockSet bs = entry.getValue();
			sb[i] = new SlotBlockColor(this, entry.getKey(), new ItemStack(bs.block, 1, bs.metadata));
			++i;
		}

		this.slotCustom.setElements(sb);
		this.slotElements = sb;
	}

	@Override
	public void drawScreen(int par1, int par2, float par3) {
		this.drawDefaultBackground();

		int i0 = (this.height - 30) / this.thumbnail.length;
		this.drawString(this.fontRendererObj, I18n.format("gui.mcte.terrainImage", new Object[0]), i0 + 5, 5, 0xffffff);
		this.drawString(this.fontRendererObj, I18n.format("gui.mcte.blocksImage", new Object[0]), i0 + 5, 25, 0xffffff);
		this.drawString(this.fontRendererObj, I18n.format("gui.mcte.yScale", new Object[0]), i0 + 5, 45, 0xffffff);

		super.drawScreen(par1, par2, par3);

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		for (int i = 0; i < this.thumbnail.length; ++i) {
			if (this.thumbnail[i] != null) {
				double d1 = (double) (i0 * i);
				this.thumbnail[i].bindTexture();
				Tessellator tessellator = Tessellator.instance;
				tessellator.startDrawingQuads();
				tessellator.addVertexWithUV((double) i0, (double) i0 + d1, (double) this.zLevel, 1.0D, 1.0D);
				tessellator.addVertexWithUV((double) i0, 0.0D + d1, (double) this.zLevel, 1.0D, 0.0D);
				tessellator.addVertexWithUV(0.0D, 0.0D + d1, (double) this.zLevel, 0.0D, 0.0D);
				tessellator.addVertexWithUV(0.0D, (double) i0 + d1, (double) this.zLevel, 0.0D, 1.0D);
				tessellator.draw();
			}
		}
	}

	@SideOnly(Side.CLIENT)
	public class SlotBlockColor extends SlotElement implements ISelector {
		public final int color;
		public ItemStack stack;

		public SlotBlockColor(GuiGenerator par1, int par2, ItemStack par3) {
			this.color = par2;
			this.stack = par3;
		}

		@Override
		public void draw(Minecraft par1, int par2, int par3, float par4) {
			SlotElementItem.func_148171_c(par1, par2 + 1, par3 + 1, 0, 0, par4);
			GL11.glEnable(GL12.GL_RESCALE_NORMAL);
			RenderHelper.enableGUIStandardItemLighting();
			NGTRenderHelper.getItemRenderer().renderItemIntoGUI(par1.fontRenderer, par1.getTextureManager(), this.stack, par2 + 2, par3 + 2);
			RenderHelper.disableStandardItemLighting();
			GL11.glDisable(GL12.GL_RESCALE_NORMAL);

			GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
			GL11.glDisable(GL11.GL_TEXTURE_2D);
			this.drawModalRect(par2 + 23, par3 + 1, 18, 18, par4, this.color);
			GL11.glEnable(GL11.GL_TEXTURE_2D);
			par1.fontRenderer.drawString(this.stack.getDisplayName(), par2 + 46, par3 + 6, 0xffffff);
		}

		protected void drawModalRect(int par1, int par2, int par3, int par4, float par5, int color) {
			Tessellator tessellator = Tessellator.instance;
			tessellator.startDrawingQuads();
			tessellator.setColorOpaque_I(color);
			tessellator.addVertex((double) (par1 + 0), (double) (par2 + par4), (double) par5);
			tessellator.addVertex((double) (par1 + par3), (double) (par2 + par4), (double) par5);
			tessellator.addVertex((double) (par1 + par3), (double) (par2 + 0), (double) par5);
			tessellator.addVertex((double) (par1 + 0), (double) (par2 + 0), (double) par5);
			tessellator.draw();
		}

		@Override
		public void onClicked(int par1, boolean par2) {
			if (par2) {
				this.selectBlock();
			}
		}

		private void selectBlock() {
			Iterator<Block> iterator = Block.blockRegistry.iterator();
			List<ItemStack> list = new ArrayList<ItemStack>();
			while (iterator.hasNext()) {
				Block block = iterator.next();
				Item item = Item.getItemFromBlock(block);
				if (item != null) {
					block.getSubBlocks(item, CreativeTabs.tabAllSearch, list);
				}
			}

			SlotElementItem[] slots = new SlotElementItem[list.size()];
			for (int i = 0; i < slots.length; ++i) {
				ItemStack stack = list.get(i);
				String s = stack.getDisplayName();
				slots[i] = new SlotElementItem<ItemStack>(this, stack, s, stack);
			}

			GuiGenerator.this.mc.displayGuiScreen(new GuiSelect(GuiGenerator.this, slots));
		}

		@Override
		public void select(Object par1) {
			this.stack = (ItemStack) par1;
			BlockSet block = new BlockSet(Block.getBlockFromItem(this.stack.getItem()), this.stack.getItemDamage());
			GuiGenerator.this.terrainData.blockMap.put(this.color, block);
		}
	}
}