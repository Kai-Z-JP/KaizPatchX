package jp.ngt.rtm.world.station;

import jp.ngt.ngtlib.math.AABBInt;
import jp.ngt.rtm.block.tileentity.TileEntityStation;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.World;
import net.minecraft.world.WorldSavedData;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.IntStream;

public class StationCollection extends WorldSavedData {
    protected World worldObj;
    protected Map<String, Station> stations = new HashMap<>();

    public StationCollection(String par1) {
        super(par1);
        this.markDirty();
    }

    public void setWorld(World par1) {
        this.worldObj = par1;
        this.stations.values().forEach(station -> station.worldObj = par1);
    }

	@Override
	public void readFromNBT(NBTTagCompound nbt) {
        NBTTagList nbttaglist = nbt.getTagList("Stations", 10);
        IntStream.range(0, nbttaglist.tagCount()).mapToObj(nbttaglist::getCompoundTagAt).forEach(nbt1 -> {
            Station station = new Station();
            station.readFromNBT(nbt1);
            this.stations.put(station.name, station);
        });
    }

	@Override
	public void writeToNBT(NBTTagCompound nbt) {
        NBTTagList nbttaglist = new NBTTagList();
        this.stations.values().forEach(station -> {
            NBTTagCompound nbt1 = new NBTTagCompound();
            station.writeToNBT(nbt1);
            nbttaglist.appendTag(nbt1);
        });
        nbt.setTag("Stations", nbttaglist);
    }

	public boolean add(int x, int y, int z) {
		if (this.getStation(x, y, z) == null) {
			TileEntity tileEntity = this.worldObj.getTileEntity(x, y, z);
			if (tileEntity instanceof TileEntityStation) {
				TileEntityStation teStation = (TileEntityStation) tileEntity;
				teStation.setName(StationManager.INSTANCE.getNewName());
				teStation.getDescriptionPacket();
				teStation.markDirty();

				Station station = new Station(this.worldObj, teStation.getName());
				station.add(new AABBInt(x, y, z, x + teStation.width, y + teStation.height, z + teStation.depth));
				this.markDirty();
			}
			return true;
		}
		return false;
	}

	public void remove(int x, int y, int z) {
		Object[] st = this.getStation(x, y, z);
		if (st != null) {
			Station station = (Station) st[0];
			AABBInt aabb = (AABBInt) st[1];
			station.partsList.remove(aabb);
			if (station.partsList.size() == 0) {
				this.stations.remove(station.name);
			}
			this.markDirty();
		}
	}

	/**
	 * @return {Station, AABBInt}
	 */
	public Object[] getStation(int x, int y, int z) {
		AABBInt aabbBlock = new AABBInt(x, y, z, x + 1, y + 1, z + 1);
		for (Station station : this.stations.values()) {
			for (AABBInt aabb : station.partsList) {
				if (aabbBlock.isCollided(aabb)) {
					return new Object[]{station, aabb};
				}
			}
		}
		return null;
	}
}