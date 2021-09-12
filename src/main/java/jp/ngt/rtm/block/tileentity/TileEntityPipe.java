package jp.ngt.rtm.block.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.block.BlockUtil;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.block.OrnamentType;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.network.Packet;
import net.minecraft.util.AxisAlignedBB;

import java.util.stream.IntStream;

public class TileEntityPipe extends TileEntityOrnament {
    /**
     * 0:x, 1:y, 2:z
     */
    private byte direction;
    /**
     * 0:Non, 1:Block, 2:Pipe 3, Slot
     */
    public byte[] connection = new byte[6];

    @Override
    public void readFromNBT(NBTTagCompound nbt) {
        super.readFromNBT(nbt);
        if (nbt.hasKey("dir")) {
            byte direction = nbt.getByte("dir");
            byte attachedSide;
            switch (direction) {
                case 0:
                    attachedSide = 4;
                    break;
                default:
                    attachedSide = 0;
                    break;
                case 2:
                    attachedSide = 2;
                    break;
            }
            this.setAttachedSide(attachedSide);
            this.refresh();
        }
        this.connection = nbt.getByteArray("connection");
        if (this.connection.length < 6) {
            this.connection = new byte[6];
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        super.writeToNBT(nbt);
//        nbt.setByte("dir", this.direction);
        nbt.setByteArray("connection", this.connection);
    }

    /**
     * 接続を再設定
     */
    public void refresh() {
        this.searchConnection();
        this.sendPacket();
        this.markDirty();
    }

    @Override
    public void updateEntity() {
        super.updateEntity();

        if (this.worldObj != null && !this.worldObj.isRemote) {
            int meta = this.getBlockMetadata();
            if (meta == 1 && this.getModelName().equals(this.getDefaultName())) {

                this.setModelName("Pipe01_Connectable");
            }
        }
    }

    public byte getDirection() {
        return this.direction;
    }

    public void setDirection(byte par1) {
        this.direction = par1;
        this.sendPacket();
    }

    public void searchConnection() {
        if (this.worldObj == null) {
            return;
        }

        IntStream.range(0, 6).forEach(i -> {
            int x0 = this.xCoord + BlockUtil.facing[i][0];
            int y0 = this.yCoord + BlockUtil.facing[i][1];
            int z0 = this.zCoord + BlockUtil.facing[i][2];
            Block block = this.worldObj.getBlock(x0, y0, z0);
            if (block == RTMBlock.slot) {
                this.connection[i] = 3;
            } else if (block == RTMBlock.pipe) {
                this.connection[i] = 2;
            } else if (block.isOpaqueCube()) {
                this.connection[i] = 1;
            } else {
                this.connection[i] = 0;
            }
        });

        this.sendPacket();
    }

    public boolean isConnected(byte side) {
        return this.connection[side] == 2 || this.connection[side] == 3;
    }

    @Override
    public Packet getDescriptionPacket() {
        this.sendPacket();
        return null;
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
    public OrnamentType getOrnamentType() {
        return OrnamentType.Pipe;
    }

    @Override
    protected String getDefaultName() {
        return "Pipe01";
    }
}