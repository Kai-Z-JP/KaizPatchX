package jp.ngt.rtm.block.tileentity;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import jp.ngt.ngtlib.block.BlockUtil;
import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.block.OrnamentType;
import net.minecraft.block.Block;
import net.minecraft.nbt.NBTTagCompound;
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

        if (this.getModelName().isEmpty()) {
            this.setModelName("Pipe01_Connectable");
        }
    }

    @Override
    public void writeToNBT(NBTTagCompound nbt) {
        if (this.getModelName().isEmpty()) {
            this.setModelName("Pipe01_Connectable");
        }

        super.writeToNBT(nbt);
//        nbt.setByte("dir", this.direction);
        nbt.setByteArray("connection", this.connection);
    }

    /**
     * 接続を再設定
     */
    public void refresh() {
        this.searchConnection();
        this.markDirty();
        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
    }

    public byte getDirection() {
        return this.direction;
    }

    public void setDirection(byte par1) {
        this.direction = par1;
        this.markDirty();
        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
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

        this.markDirty();
        this.worldObj.markBlockForUpdate(this.xCoord, this.yCoord, this.zCoord);
    }

    public boolean isConnected(byte side) {
        return this.connection[side] == 2 || this.connection[side] == 3;
    }

    @Override
    @SideOnly(Side.CLIENT)
    public AxisAlignedBB getRenderBoundingBox() {
        return AxisAlignedBB.getBoundingBox(0, 0, 0, 1, 1, 1)
                .offset(this.xCoord, this.yCoord, this.zCoord)
                .offset(this.getOffsetX(), this.getOffsetY(), this.getOffsetZ());
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