package jp.ngt.rtm.gui;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.rtm.modelpack.texture.SignBoardProperty;
import jp.ngt.rtm.modelpack.texture.TextureManager.TexturePropertyType;
import jp.ngt.rtm.modelpack.texture.TextureProperty;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.opengl.GL11;

@SideOnly(Side.CLIENT)
public class GuiButtonSelectTexture extends GuiButton {
	public final TextureProperty property;
	private int movement;

	public GuiButtonSelectTexture(int id, int xPos, int yPos, int w, int h, TextureProperty par4) {
		super(id, xPos, yPos, w, h, "");
		this.property = par4;
	}

	@Override
	public void drawButton(Minecraft mc, int par2, int par3) {
		if (this.movement > 0) {
			if (this.movement < 4) {
				this.yPosition += this.movement;
				this.movement = 0;
			} else {
				this.yPosition += 4;
				this.movement -= 4;
			}
		} else if (this.movement < 0) {
			if (this.movement > -4) {
				this.yPosition += this.movement;
				this.movement = 0;
			} else {
				this.yPosition -= 4;
				this.movement += 4;
			}
		}

		GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
		mc.getTextureManager().bindTexture(this.property.getTexture());
		this.draw();
    	/*int k = this.getHoverState(this.field_146123_n);
    	if(k == 2)
        {
            GL11.glColor4f(0.5F, 0.5F, 1.0F, 0.5F);
        	GL11.glEnable(GL11.GL_BLEND);
        	GL11.glBlendFunc(GL11.GL_SRC_ALPHA , GL11.GL_ONE_MINUS_SRC_ALPHA);
            this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, 0, this.width, this.height);
            GL11.glDisable(GL11.GL_BLEND);
        }*/
	}

	private void draw() {
		float maxV = 1.0F;
		if (this.property.getType() == TexturePropertyType.SignBoard && ((SignBoardProperty) this.property).frame > 1) {
			maxV = 1.0F / (float) ((SignBoardProperty) this.property).frame;
		}
		Tessellator tessellator = Tessellator.instance;
		tessellator.startDrawingQuads();
		tessellator.addVertexWithUV(this.xPosition + this.width, this.yPosition + this.height, this.zLevel, 1.0D, maxV);
		tessellator.addVertexWithUV(this.xPosition + this.width, this.yPosition, this.zLevel, 1.0D, 0.0D);
		tessellator.addVertexWithUV(this.xPosition, this.yPosition, this.zLevel, 0.0D, 0.0D);
		tessellator.addVertexWithUV(this.xPosition, this.yPosition + this.height, this.zLevel, 0.0D, maxV);
		tessellator.draw();
	}

	public void moveButton(int moveY) {
		//this.movement += moveY;
		this.yPosition -= moveY;
	}
}