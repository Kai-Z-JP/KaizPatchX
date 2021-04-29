package jp.ngt.mcte.block;

import jp.ngt.ngtlib.io.NGTFileLoadException;
import jp.ngt.ngtlib.io.NGTJson;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagIntArray;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.AxisAlignedBB;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

public class MiniatureBlockState {
    public float hardness = 2.0F;
    public byte redstonePower = 0;
    public byte lightValue = 0;
    private int boolStates = 0;
    public float explosionResistance = 10.0F;
    private MiniatureBB mbb = new MiniatureBB();

    public boolean isLadder() {
        return (this.boolStates & 1) > 0;
    }

    public void setLadder(boolean par1) {
        if (par1) {
            this.boolStates |= 1;
        } else {
            this.boolStates ^= 1;
        }
    }

    public boolean isBurning() {
        return (this.boolStates & 2) > 0;
    }

    public boolean isFireSource() {
        return (this.boolStates & 4) > 0;
    }

    public boolean isBed() {
        return (this.boolStates & 8) > 0;
    }

    public void setBed(boolean par1) {
        if (par1) {
            this.boolStates |= 8;
        } else {
            this.boolStates ^= 8;
        }
    }

    public boolean hasCustomAABB() {
        return (this.boolStates & 16) > 0;
    }

    public void setCustomAABB(boolean par1) {
        if (par1) {
            this.boolStates |= 16;
        } else {
            this.boolStates ^= 16;
        }
    }

    public AxisAlignedBB getSelectBox() {
        return this.getAABB(this.mbb.selectBox);
    }

    /**
     * ブロックの座標を加えたAABBを取得
     */
    public List<AxisAlignedBB> getCollisionBoxes() {
        List<AxisAlignedBB> list = new ArrayList<>();
        if (this.hasCustomAABB()) {
            list = Arrays.stream(this.mbb.collisionBoxes).map(this::getAABB).collect(Collectors.toList());
        }
        return list;
    }

    private AxisAlignedBB getAABB(float[] fa) {
        return AxisAlignedBB.getBoundingBox(
                fa[0], fa[1], fa[2],
                fa[3], fa[4], fa[5]);
    }

    public void setAABB(String par1) {
        try {
            this.mbb = (MiniatureBB) NGTJson.getObjectFromJson(par1, MiniatureBB.class);
        } catch (NGTFileLoadException e) {
            this.mbb = new MiniatureBB();
        }
    }

    public String getAabbAsJson() {
        String s = NGTJson.getJsonFromObject(this.mbb);
        s = s.replaceAll(" ", "");//スペース除去
        s = s.replaceAll("\n", "");//改行除去
        s = s.replaceAll(",", ", ");//1スペース入れ
        return s;
    }

    public static MiniatureBlockState readFromNBT(NBTTagCompound nbt) {
        MiniatureBlockState state = new MiniatureBlockState();
        state.hardness = nbt.getFloat("Hardness");
        state.redstonePower = nbt.getByte("RSPower");
        state.lightValue = nbt.getByte("LightValue");
        state.boolStates = nbt.getInteger("BoolStates");
        state.explosionResistance = nbt.getFloat("Resistance");
        state.mbb = MiniatureBB.readFromNBT(nbt.getCompoundTag("MiniatureBB"));
        return state;
    }

    public NBTTagCompound writeToNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        nbt.setFloat("Hardness", this.hardness);
        nbt.setByte("RSPower", this.redstonePower);
        nbt.setByte("LightValue", this.lightValue);
        nbt.setInteger("BoolStates", this.boolStates);
        nbt.setFloat("Resistance", this.explosionResistance);
        nbt.setTag("MiniatureBB", this.mbb.writeToNBT());
        return nbt;
    }

    public static class MiniatureBB {
        public float[] selectBox;
        public float[][] collisionBoxes;

        public MiniatureBB() {
            this.selectBox = new float[]{-0.5F, -0.5F, -0.5F, 0.5F, 0.5F, 0.5F};
            this.collisionBoxes = new float[][]{{-0.5F, -0.5F, -0.5F, 0.5F, 0.5F, 0.5F}};
        }

        public static MiniatureBB readFromNBT(NBTTagCompound nbt) {
            MiniatureBB mbb = new MiniatureBB();
            mbb.selectBox = new float[6];
            int[] ia = nbt.getIntArray("SelectBox");
            IntStream.range(0, 6).forEach(j -> mbb.selectBox[j] = Float.intBitsToFloat(ia[j]));

            NBTTagList tagList = nbt.getTagList("CollisionBoxes", 11);
            mbb.collisionBoxes = new float[tagList.tagCount()][6];
            for (int i = 0; i < tagList.tagCount(); ++i) {
                int[] ia2 = tagList.func_150306_c(i);
                for (int j = 0; j < 6; ++j) {
                    mbb.collisionBoxes[i][j] = Float.intBitsToFloat(ia2[j]);
                }
            }

            return mbb;
        }

        public NBTTagCompound writeToNBT() {
            NBTTagCompound nbt = new NBTTagCompound();
            int[] ia = IntStream.range(0, 6).map(j -> Float.floatToIntBits(this.selectBox[j])).toArray();
            nbt.setIntArray("SelectBox", ia);

            NBTTagList tagList = new NBTTagList();
            Arrays.stream(this.collisionBoxes).map(collisionBox -> IntStream.range(0, 6).map(j -> Float.floatToIntBits(collisionBox[j])).toArray()).map(NBTTagIntArray::new).forEach(tagList::appendTag);
            nbt.setTag("CollisionBoxes", tagList);
            return nbt;
        }
    }
}