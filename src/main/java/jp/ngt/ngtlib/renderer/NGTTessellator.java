package jp.ngt.ngtlib.renderer;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.client.renderer.GLAllocation;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.shader.TesselatorVertexState;
import net.minecraft.client.util.QuadComparator;
import org.lwjgl.opengl.GL11;

import java.nio.*;
import java.util.Arrays;
import java.util.PriorityQueue;

@SideOnly(Side.CLIENT)
public final class NGTTessellator implements IRenderer {
	public static final NGTTessellator instance = new NGTTessellator();

	private static final int NATIVE_BUFFER_SIZE = 0x600000;//0x200000=2097152

	private static final ByteBuffer byteBuffer = GLAllocation.createDirectByteBuffer(NATIVE_BUFFER_SIZE * 4);
	private static final IntBuffer intBuffer = byteBuffer.asIntBuffer();
	private static final FloatBuffer floatBuffer = byteBuffer.asFloatBuffer();
	private static final ShortBuffer shortBuffer = byteBuffer.asShortBuffer();
	private int[] rawBuffer;
	/**
	 * The index into the raw buffer to be used for the next data.
	 */
	private int rawBufferIndex;
	private int rawBufferSize = 0;
	/**
	 * The number of vertices to be drawn in the next draw call. Reset to 0 between draw calls.
	 */
	private int vertexCount;
	private float textureU, textureV;
	private int brightness;
	private int color;
	private int drawMode;

	private boolean hasColor;
	private boolean hasTexture;
	private boolean hasBrightness;
	private boolean hasNormals;
	private boolean isColorDisabled;

	/**
	 * An offset to be applied along the x-axis for all vertices in this draw call.
	 */
	private float xOffset, yOffset, zOffset;
	/**
	 * The normal to be applied to the face being drawn.
	 */
	private int normal;
	/**
	 * Whether this tessellator is currently in draw mode.
	 */
	private boolean isDrawing;

	private NGTTessellator() {
	}

	/**
	 * Draws the data set up in this tessellator and resets the state to prepare for new drawing.
	 */
	public int draw() {
		if (!this.isDrawing) {
			throw new IllegalStateException("Not tesselating!");
		} else {
			this.isDrawing = false;

			int offs = 0;
			while (offs < this.vertexCount) {
				int vtc = Math.min(this.vertexCount - offs, NATIVE_BUFFER_SIZE >> 5);
				this.intBuffer.clear();
				this.intBuffer.put(this.rawBuffer, offs << 3, vtc << 3);
				this.byteBuffer.position(0);
				this.byteBuffer.limit(vtc << 5);
				offs += vtc;

				if (this.hasTexture) {
					this.floatBuffer.position(3);
					GL11.glTexCoordPointer(2, 32, this.floatBuffer);
					GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
				}

				if (this.hasBrightness) {
					OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
					this.shortBuffer.position(14);
					GL11.glTexCoordPointer(2, 32, this.shortBuffer);
					GL11.glEnableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
					OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
				}

				if (this.hasColor) {
					this.byteBuffer.position(20);
					GL11.glColorPointer(4, true, 32, this.byteBuffer);
					GL11.glEnableClientState(GL11.GL_COLOR_ARRAY);
				}

				if (this.hasNormals) {
					this.byteBuffer.position(24);
					GL11.glNormalPointer(32, this.byteBuffer);
					GL11.glEnableClientState(GL11.GL_NORMAL_ARRAY);
				}

				this.floatBuffer.position(0);
				GL11.glVertexPointer(3, 32, this.floatBuffer);
				GL11.glEnableClientState(GL11.GL_VERTEX_ARRAY);
				GL11.glDrawArrays(this.drawMode, 0, vtc);
				GL11.glDisableClientState(GL11.GL_VERTEX_ARRAY);

				if (this.hasTexture) {
					GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
				}

				if (this.hasBrightness) {
					OpenGlHelper.setClientActiveTexture(OpenGlHelper.lightmapTexUnit);
					GL11.glDisableClientState(GL11.GL_TEXTURE_COORD_ARRAY);
					OpenGlHelper.setClientActiveTexture(OpenGlHelper.defaultTexUnit);
				}

				if (this.hasColor) {
					GL11.glDisableClientState(GL11.GL_COLOR_ARRAY);
				}

				if (this.hasNormals) {
					GL11.glDisableClientState(GL11.GL_NORMAL_ARRAY);
				}
			}

			if (this.rawBufferSize > 0x20000 && this.rawBufferIndex < (this.rawBufferSize << 3)) {
				this.rawBufferSize = 0x10000;
				this.rawBuffer = new int[this.rawBufferSize];
			}

			int i = this.rawBufferIndex << 2;
			this.reset();
			return i;
		}
	}

	public TesselatorVertexState getVertexState(float x, float y, float z) {
		int[] aint = new int[this.rawBufferIndex];
		PriorityQueue priorityqueue = new PriorityQueue(this.rawBufferIndex, new QuadComparator(this.rawBuffer, x + this.xOffset, y + this.yOffset, z + this.zOffset));
		byte b0 = 32;

		for (int i = 0; i < this.rawBufferIndex; i += b0) {
			priorityqueue.add(Integer.valueOf(i));
		}

		for (int i = 0; !priorityqueue.isEmpty(); i += b0) {
			int j = ((Integer) priorityqueue.remove()).intValue();

			for (int k = 0; k < b0; ++k) {
				aint[i + k] = this.rawBuffer[j + k];
			}
		}

		System.arraycopy(aint, 0, this.rawBuffer, 0, aint.length);
		return new TesselatorVertexState(aint, this.rawBufferIndex, this.vertexCount, this.hasTexture, this.hasBrightness, this.hasNormals, this.hasColor);
	}

	public void setVertexState(TesselatorVertexState state) {
		while (state.getRawBuffer().length > this.rawBufferSize && this.rawBufferSize > 0) {
			this.rawBufferSize <<= 1;
		}

		if (this.rawBufferSize > this.rawBuffer.length) {
			this.rawBuffer = new int[this.rawBufferSize];
		}

		System.arraycopy(state.getRawBuffer(), 0, this.rawBuffer, 0, state.getRawBuffer().length);
		this.rawBufferIndex = state.getRawBufferIndex();
		this.vertexCount = state.getVertexCount();
		this.hasTexture = state.getHasTexture();
		this.hasBrightness = state.getHasBrightness();
		this.hasColor = state.getHasColor();
		this.hasNormals = state.getHasNormals();
	}

	/**
	 * Clears the tessellator state in preparation for new drawing.
	 */
	private void reset() {
		this.vertexCount = 0;
		this.byteBuffer.clear();
		this.rawBufferIndex = 0;
	}

	public void startDrawingQuads() {
		this.startDrawing(GL11.GL_QUADS);
	}

	public void startDrawing(int par1) {
		if (this.isDrawing) {
			throw new IllegalStateException("Already tesselating!");
		} else {
			this.isDrawing = true;
			this.reset();
			this.drawMode = par1;
			this.hasNormals = false;
			this.hasColor = false;
			this.hasTexture = false;
			this.hasBrightness = false;
			this.isColorDisabled = false;
		}
	}

	public void setTextureUV(float par1, float par3) {
		this.hasTexture = true;
		this.textureU = par1;
		this.textureV = par3;
	}

	public void setBrightness(int par1) {
		this.hasBrightness = true;
		this.brightness = par1;
	}

	/**
	 * Sets the RGB values as specified, converting from floats between 0 and 1 to integers from 0-255.
	 */
	public void setColorOpaque_F(float par1, float par2, float par3) {
		this.setColorOpaque((int) (par1 * 255.0F), (int) (par2 * 255.0F), (int) (par3 * 255.0F));
	}

	/**
	 * Sets the RGBA values for the color, converting from floats between 0 and 1 to integers from 0-255.
	 */
	public void setColorRGBA_F(float par1, float par2, float par3, float par4) {
		this.setColorRGBA((int) (par1 * 255.0F), (int) (par2 * 255.0F), (int) (par3 * 255.0F), (int) (par4 * 255.0F));
	}

	/**
	 * Sets the RGB values as specified, and sets alpha to opaque.
	 */
	public void setColorOpaque(int par1, int par2, int par3) {
		this.setColorRGBA(par1, par2, par3, 255);
	}

	/**
	 * Sets the RGBA values for the color. Also clamps them to 0-255.
	 */
	public void setColorRGBA(int r, int g, int b, int a) {
		if (!this.isColorDisabled) {
			if (r > 255) {
				r = 255;
			} else if (r < 0) {
				r = 0;
			}

			if (g > 255) {
				g = 255;
			} else if (g < 0) {
				g = 0;
			}

			if (b > 255) {
				b = 255;
			} else if (b < 0) {
				b = 0;
			}

			if (a > 255) {
				a = 255;
			} else if (a < 0) {
				a = 0;
			}

			this.hasColor = true;

			if (ByteOrder.nativeOrder() == ByteOrder.LITTLE_ENDIAN) {
				this.color = a << 24 | b << 16 | g << 8 | r;
			} else {
				this.color = r << 24 | g << 16 | b << 8 | a;
			}
		}
	}

	public void addVertexWithUV(float par1, float par3, float par5, float par7, float par9) {
		this.setTextureUV(par7, par9);
		this.addVertex(par1, par3, par5);
	}

	/**
	 * Adds a vertex with the specified x,y,z to the current draw call. It will trigger a draw() if the buffer gets
	 * full.
	 */
	public void addVertex(float par1, float par3, float par5) {
		if (this.rawBufferIndex >= this.rawBufferSize - 32) {
			if (this.rawBufferSize == 0) {
				this.rawBufferSize = 0x10000;
				this.rawBuffer = new int[this.rawBufferSize];
			} else {
				this.rawBufferSize *= 2;
				this.rawBuffer = Arrays.copyOf(this.rawBuffer, this.rawBufferSize);
			}
		}

		if (this.hasTexture) {
			this.rawBuffer[this.rawBufferIndex + 3] = Float.floatToRawIntBits(this.textureU);
			this.rawBuffer[this.rawBufferIndex + 4] = Float.floatToRawIntBits(this.textureV);
		}

		if (this.hasBrightness) {
			this.rawBuffer[this.rawBufferIndex + 7] = this.brightness;
		}

		if (this.hasColor) {
			this.rawBuffer[this.rawBufferIndex + 5] = this.color;
		}

		if (this.hasNormals) {
			this.rawBuffer[this.rawBufferIndex + 6] = this.normal;
		}

		this.rawBuffer[this.rawBufferIndex + 0] = Float.floatToRawIntBits(par1 + this.xOffset);
		this.rawBuffer[this.rawBufferIndex + 1] = Float.floatToRawIntBits(par3 + this.yOffset);
		this.rawBuffer[this.rawBufferIndex + 2] = Float.floatToRawIntBits(par5 + this.zOffset);
		this.rawBufferIndex += 8;
		++this.vertexCount;
	}

	/**
	 * Sets the color to the given opaque value (stored as byte values packed in an integer).
	 */
	public void setColorOpaque_I(int par1) {
		int j = par1 >> 16 & 0xFF;
		int k = par1 >> 8 & 0xFF;
		int l = par1 & 0xFF;
		this.setColorOpaque(j, k, l);
	}

	/**
	 * Sets the color to the given color (packed as bytes in integer) and alpha values.
	 */
	public void setColorRGBA_I(int par1, int par2) {
		int k = par1 >> 16 & 0xFF;
		int l = par1 >> 8 & 0xFF;
		int i1 = par1 & 0xFF;
		this.setColorRGBA(k, l, i1, par2);
	}

	public void disableColor() {
		this.isColorDisabled = true;
	}

	/**
	 * Sets the normal for the current draw call.
	 */
	public void setNormal(float par1, float par2, float par3) {
		this.hasNormals = true;
		int b0 = (int) (par1 * 127.0F);
		int b1 = (int) (par2 * 127.0F);
		int b2 = (int) (par3 * 127.0F);
		this.normal = b0 & 0xFF | (b1 & 0xFF) << 8 | (b2 & 0xFF) << 16;
	}

	/**
	 * Sets the translation for all vertices in the current draw call.
	 */
	public void setTranslation(float par1, float par3, float par5) {
		this.xOffset = par1;
		this.yOffset = par3;
		this.zOffset = par5;
	}

	/**
	 * Offsets the translation for all vertices in the current draw call.
	 */
	public void addTranslation(float par1, float par2, float par3) {
		this.xOffset += par1;
		this.yOffset += par2;
		this.zOffset += par3;
	}
}