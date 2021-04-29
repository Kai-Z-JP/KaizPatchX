package jp.ngt.rtm.world.station;

import jp.ngt.ngtlib.math.AABBInt;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

public class Station {
    protected World worldObj;
    protected String name;
    protected List<AABBInt> partsList = new ArrayList<>();

    public Station() {
        this.name = "";
    }

    public Station(World world, String par2) {
        this.worldObj = world;
        this.name = par2;
    }

    public void readFromNBT(NBTTagCompound nbt) {
        this.name = nbt.getString("Name");
        NBTTagList nbttaglist = nbt.getTagList("Parts", 10);
        IntStream.range(0, nbttaglist.tagCount()).mapToObj(nbttaglist::getCompoundTagAt).forEach(nbt1 -> {
            int x0 = nbt1.getInteger("MinX");
            int y0 = nbt1.getInteger("MinY");
            int z0 = nbt1.getInteger("MinZ");
            int x1 = nbt1.getInteger("MaxX");
            int y1 = nbt1.getInteger("MaxY");
            int z1 = nbt1.getInteger("MaxZ");
            this.partsList.add(new AABBInt(x0, y0, z0, x1, y1, z1));
        });
    }

    public void writeToNBT(NBTTagCompound nbt) {
        nbt.setString("Name", this.name);
        NBTTagList nbttaglist = new NBTTagList();
        this.partsList.forEach(chunk -> {
            NBTTagCompound nbt1 = new NBTTagCompound();
            nbt1.setInteger("MinX", chunk.minX);
            nbt1.setInteger("MinY", chunk.minY);
            nbt1.setInteger("MinZ", chunk.minZ);
            nbt1.setInteger("MaxX", chunk.maxX);
            nbt1.setInteger("MaxY", chunk.maxY);
            nbt1.setInteger("MaxZ", chunk.maxZ);
            nbttaglist.appendTag(nbt1);
        });
        nbt.setTag("Parts", nbttaglist);
    }

    public void add(AABBInt aabb) {
        this.partsList.add(aabb);
    }

	/*public class StationChunk
	{
		public final int blockX;
		public final int blockY;
		public final int blockZ;

		public StationChunk(int x, int y, int z)
		{
			this.blockX = x;
			this.blockY = y;
			this.blockZ = z;
		}

		public int getChunkX()
		{
			return this.blockX >> 4;
		}

		public int getChunkY()
		{
			return this.blockY >> 4;
		}

		public int getChunkZ()
		{
			return this.blockZ >> 4;
		}
	}*/
}