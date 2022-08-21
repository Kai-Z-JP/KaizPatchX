package jp.ngt.rtm.block.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.util.ColorUtil;
import jp.ngt.ngtlib.util.NGTUtil;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.NetworkManager;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S35PacketUpdateTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.AxisAlignedBB;

import java.util.stream.IntStream;

public class TileEntityPaint extends TileEntity {
    private final int[][] colors = new int[6][256];
    private final int[][] alphas = new int[6][256];
    private final boolean[] hasColor = new boolean[6];

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        int[] ia = nbt.getIntArray("Colors");
        if (ia.length == 6 * 256) {
            IntStream.range(0, 6).forEach(i -> System.arraycopy(ia, i * 256, this.colors[i], 0, 256));
        }

        byte[] ba = nbt.getByteArray("Alphas");
        if (ba.length == 6 * 256) {
            for (int i = 0; i < 6; ++i) {
                for (int j = 0; j < 256; ++j) {
                    this.alphas[i][j] = (int) ba[i * 256 + j] + 128;
                }
            }
        }

        byte[] ba2 = nbt.getByteArray("HasColor");
        if (ba2.length == 6) {
            IntStream.range(0, 6).forEach(i -> this.hasColor[i] = (ba2[i] == 1));
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
        int[] ia = new int[6 * 256];
        IntStream.range(0, 6).forEach(i -> System.arraycopy(this.colors[i], 0, ia, i * 256, 256));
        nbt.setIntArray("Colors", ia);

        byte[] ba = new byte[6 * 256];
        for (int i = 0; i < 6; ++i) {
            for (int j = 0; j < 256; ++j) {
                ba[i * 256 + j] = (byte) (this.alphas[i][j] - 128);
            }
        }
        nbt.setByteArray("Alphas", ba);

        byte[] ba2 = new byte[6];
        IntStream.range(0, 6).forEach(i -> ba2[i] = (byte) (this.hasColor[i] ? 1 : 0));
        nbt.setByteArray("HasColor", ba2);
    }

    /**
     * {p1, p2} = {x, y} or {y, z} or {x, z}
     */
    public void setColor(int color, int alpha, int p1, int p2, int dir) {
        if (p1 < 0 || p1 >= 16 || p2 < 0 || p2 >= 16) {
            return;
        }
        int index = p1 * 16 + p2;
        int c0 = this.colors[dir][index];
        int a0 = this.alphas[dir][index];
        int[] ca = ColorUtil.alphaBlending(color, alpha, c0, a0);
        this.colors[dir][index] = ca[0];
        this.alphas[dir][index] = ca[1];
        this.hasColor[dir] = true;
    }

    public void clearColor(int p1, int p2, int dir) {
        if (p1 < 0 || p1 >= 16 || p2 < 0 || p2 >= 16) {
            return;
        }
        int index = p1 * 16 + p2;
        this.colors[dir][index] = 0;
        this.alphas[dir][index] = 0;
    }

    public boolean hasColor(int dir) {
        return this.hasColor[dir];
    }

    public int getColor(int p1, int p2, int dir) {
        return this.colors[dir][p1 * 16 + p2];
    }

    public int getAlpha(int p1, int p2, int dir) {
        return this.alphas[dir][p1 * 16 + p2];
    }

    @Override
    public void markDirty() {
        super.markDirty();

        if (!this.worldObj.isRemote) {
            boolean flag = false;
            for (int i = 0; i < 6; ++i) {
                if (this.hasColor(i)) {
                    this.hasColor[i] = false;
                    for (int j = 0; j < 256; ++j) {
                        if (this.alphas[i][j] != 0) {
                            this.hasColor[i] = true;
                            flag = true;
                            break;
                        }
                    }
                }
            }

            if (flag) {
                this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
            } else {
                this.worldObj.setBlockToAir(this.xCoord, this.yCoord, this.zCoord);
            }
        }
    }

    @Override
    public Packet getDescriptionPacket() {
        NBTTagCompound nbt = new NBTTagCompound();
        this.writeToNBT(nbt);
        return new S35PacketUpdateTileEntity(this.xCoord, this.yCoord, this.zCoord, 0, nbt);
    }

    @Override
    public void onDataPacket(NetworkManager net, S35PacketUpdateTileEntity pkt) {
        this.readFromNBT(pkt.func_148857_g());
    }

    @Override
    @SideOnly(Side.CLIENT)
    public double getMaxRenderDistanceSquared() {
        return NGTUtil.getChunkLoadDistanceSq();
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return AxisAlignedBB.getBoundingBox(this.xCoord, this.yCoord, this.zCoord, this.xCoord + 1, this.yCoord + 1, this.zCoord + 1);
    }

    @Override
    public boolean shouldRenderInPass(int pass) {
        return pass == 1;//ミニチュア化した際に後から描画されるブロックで隠れないように
    }
}