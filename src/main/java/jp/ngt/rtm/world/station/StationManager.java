package jp.ngt.rtm.world.station;

import net.minecraft.world.World;

public final class StationManager {
    public static final StationManager INSTANCE = new StationManager();
    public static final String NAME = "rtm_stations";

    public StationCollection stationCollection;

    private StationManager() {
    }

    public void loadData(World world) {
        this.stationCollection = (StationCollection) world.mapStorage.loadData(StationCollection.class, NAME);

        if (this.stationCollection == null) {
            this.stationCollection = new StationCollection(NAME);
            world.mapStorage.setData(NAME, this.stationCollection);
        }

        this.stationCollection.setWorld(world);
    }

    public String getNewName() {
        int i = 0;
        String s = "station" + i;
        while (this.stationCollection.stations.containsKey(s)) {
            ++i;
            s = "station" + i;
        }
        return s;
    }
}