package jp.ngt.rtm.entity.vehicle;

import cpw.mods.fml.common.network.internal.FMLNetworkHandler;
import jp.ngt.ngtlib.network.PacketNBT;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.entity.train.EntityBogie;
import jp.ngt.rtm.entity.train.EntityTrainBase;
import jp.ngt.rtm.network.PacketVehicleMovement;
import net.minecraft.entity.DataWatcher;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityTracker;
import net.minecraft.entity.EntityTrackerEntry;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.network.Packet;
import net.minecraft.network.play.server.S12PacketEntityVelocity;
import net.minecraft.network.play.server.S1BPacketEntityAttach;
import net.minecraft.network.play.server.S1CPacketEntityMetadata;
import net.minecraft.util.MathHelper;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.ForgeEventFactory;

import java.util.List;
import java.util.Set;

public class VehicleTrackerEntry extends EntityTrackerEntry {
    private double posX, posY, posZ;
    private boolean isDataInitialized;
    private Entity rider;
    private boolean ridingEntity;

    public VehicleTrackerEntry(EntityTrackerEntry par1, Entity par2) {
        super(par1.myEntity, par1.blocksDistanceThreshold, 2, false);
        this.trackingPlayers = par1.trackingPlayers;
    }

    @Override
    public boolean equals(Object par1) {
        return par1 instanceof EntityTrackerEntry && ((EntityTrackerEntry) par1).myEntity.getEntityId() == this.myEntity.getEntityId();
    }

    @Override
    public int hashCode() {
        return this.myEntity.getEntityId();
    }

    @Override
    public void sendLocationToAllClients(List par1) {
        this.playerEntitiesUpdated = false;

        if (!this.isDataInitialized || this.myEntity.getDistanceSq(this.posX, this.posY, this.posZ) > 16.0D) {
            this.posX = this.myEntity.posX;
            this.posY = this.myEntity.posY;
            this.posZ = this.myEntity.posZ;
            this.isDataInitialized = true;
            this.playerEntitiesUpdated = true;
            this.sendEventsToPlayers(par1);
        }

        if (this.rider != this.myEntity.ridingEntity || this.myEntity.ridingEntity != null && this.ticks % 60 == 0) {
            this.rider = this.myEntity.ridingEntity;
            this.func_151259_a(new S1BPacketEntityAttach(0, this.myEntity, this.myEntity.ridingEntity));
        }

        if (this.ticks % this.updateFrequency == 0)// || this.myEntity.getDataWatcher().hasChanges())
        {
            if (this.myEntity.ridingEntity == null) {
                if (this.myEntity instanceof EntityTrainBase || this.myEntity instanceof EntityBogie) {
                    PacketVehicleMovement packet = new PacketVehicleMovement(this.myEntity);
                    this.lastScaledXPosition = packet.trainX;
                    this.lastScaledYPosition = packet.trainY;
                    this.lastScaledZPosition = packet.trainZ;
                    //this.lastYaw = packet.trainYaw;
                    //this.lastPitch = packet.trainPitch;
                    this.ridingEntity = false;
                    par1.forEach(player -> RTMCore.NETWORK_WRAPPER.sendTo(packet, (EntityPlayerMP) player));
//                    RTMCore.NETWORK_WRAPPER.sendToAll(packet);
                } else if (this.myEntity instanceof EntityVehicle) {
                    PacketVehicleMovement packet = new PacketVehicleMovement(this.myEntity);
                    this.lastScaledXPosition = packet.trainX;
                    this.lastScaledYPosition = packet.trainY;
                    this.lastScaledZPosition = packet.trainZ;
                    par1.forEach(player -> RTMCore.NETWORK_WRAPPER.sendTo(packet, (EntityPlayerMP) player));
//                    RTMCore.NETWORK_WRAPPER.sendToAll(packet);

                    this.motionX = this.myEntity.motionX;
                    this.motionY = this.myEntity.motionY;
                    this.motionZ = this.myEntity.motionZ;
                    this.func_151259_a(new S12PacketEntityVelocity(this.myEntity.getEntityId(), this.motionX, this.motionY, this.motionZ));

                    /*this.lastScaledXPosition = i;
                    this.lastScaledYPosition = j;
                    this.lastScaledZPosition = k;
                    this.lastYaw = iYaw;
                    this.lastPitch = iPitch;*/
                    this.ridingEntity = false;
                }
            } else {
                this.lastScaledXPosition = this.myEntity.myEntitySize.multiplyBy32AndRound(this.myEntity.posX);
                this.lastScaledYPosition = MathHelper.floor_double(this.myEntity.posY * 32.0D);
                this.lastScaledZPosition = this.myEntity.myEntitySize.multiplyBy32AndRound(this.myEntity.posZ);
                this.ridingEntity = true;
            }
        }

        if (this.myEntity.getDataWatcher().hasChanges()) {
            this.sendMetadataToAllAssociatedPlayers();
        }

        ++this.ticks;
    }

    /**
     * DataWatcherの同期
     */
    private void sendMetadataToAllAssociatedPlayers() {
        DataWatcher datawatcher = this.myEntity.getDataWatcher();

        if (datawatcher.hasChanges()) {
            this.func_151261_b(new S1CPacketEntityMetadata(this.myEntity.getEntityId(), datawatcher, false));
        }
    }

    @Override
    public void informAllAssociatedPlayersOfItemDestruction() {
        for (Object trackingPlayer : this.trackingPlayers) {
            EntityPlayerMP player = (EntityPlayerMP) trackingPlayer;
            player.func_152339_d(this.myEntity);
        }
    }

    @Override
    public void removeFromWatchingList(EntityPlayerMP player) {
        if (this.trackingPlayers.contains(player)) {
            player.func_152339_d(this.myEntity);
            this.trackingPlayers.remove(player);
        }
    }

    /**
     * if the player is more than the distance threshold (typically 64) then the player is removed instead
     */
    @Override
    public void tryStartWachingThis(EntityPlayerMP par1) {
        if (par1 != this.myEntity) {
            double d0 = par1.posX - (double) (this.lastScaledXPosition >> 5);// /32
            double d1 = par1.posZ - (double) (this.lastScaledZPosition >> 5);

            if (d0 >= (double) (-this.blocksDistanceThreshold) && d0 <= (double) this.blocksDistanceThreshold && d1 >= (double) (-this.blocksDistanceThreshold) && d1 <= (double) this.blocksDistanceThreshold) {
                if (!this.trackingPlayers.contains(par1) && (this.isPlayerWatchingThisChunk(par1) || this.myEntity.forceSpawn)) {
                    this.trackingPlayers.add(par1);
                    Packet packet = FMLNetworkHandler.getEntitySpawningPacket(this.myEntity);
                    par1.playerNetServerHandler.sendPacket(packet);

                    if (!this.myEntity.getDataWatcher().getIsBlank()) {
                        par1.playerNetServerHandler.sendPacket(new S1CPacketEntityMetadata(this.myEntity.getEntityId(), this.myEntity.getDataWatcher(), true));
                    }

                    this.motionX = this.myEntity.motionX;
                    this.motionY = this.myEntity.motionY;
                    this.motionZ = this.myEntity.motionZ;

                    int posX = MathHelper.floor_double(this.myEntity.posX * 32.0D);
                    int posY = MathHelper.floor_double(this.myEntity.posY * 32.0D);
                    int posZ = MathHelper.floor_double(this.myEntity.posZ * 32.0D);
                    if (posX != this.lastScaledXPosition || posY != this.lastScaledYPosition || posZ != this.lastScaledZPosition) {
                        FMLNetworkHandler.makeEntitySpawnAdjustment(this.myEntity, par1, this.lastScaledXPosition, this.lastScaledYPosition, this.lastScaledZPosition);
                    }
                    if (this.myEntity.ridingEntity != null) {
                        par1.playerNetServerHandler.sendPacket(new S1BPacketEntityAttach(0, this.myEntity, this.myEntity.ridingEntity));
                    }

                    if (this.myEntity instanceof EntityTrainBase) {
                        PacketNBT.sendToClient(this.myEntity);
                    }

                    ForgeEventFactory.onStartEntityTracking(this.myEntity, par1);
                }
            } else if (this.trackingPlayers.contains(par1)) {
                this.trackingPlayers.remove(par1);
                par1.func_152339_d(this.myEntity);
                ForgeEventFactory.onStopEntityTracking(myEntity, par1);
            }
        }
    }

    private boolean isPlayerWatchingThisChunk(EntityPlayerMP par1) {
        return par1.getServerForPlayer().getPlayerManager().isPlayerWatchingChunk(par1, this.myEntity.chunkCoordX, this.myEntity.chunkCoordZ);
    }

    @Override
    public void sendEventsToPlayers(List list) {
        for (Object o : list) {
            this.tryStartWachingThis((EntityPlayerMP) o);
        }
    }

    @Override
    public void removePlayerFromTracker(EntityPlayerMP player) {
        if (this.trackingPlayers.contains(player)) {
            this.trackingPlayers.remove(player);
            player.func_152339_d(this.myEntity);
        }
    }

    /**
     * @param par1 EntityVehicleBase or EntityBogie
     */
    public static boolean trackingVehicle(Entity par1) {
        if (!(par1 instanceof EntityVehicleBase || par1 instanceof EntityBogie)) {
            return false;
        }

        if (par1.worldObj instanceof WorldServer) {
            EntityTracker tracker = ((WorldServer) par1.worldObj).getEntityTracker();
            Set trackedEntities = getTrackedEntities(tracker);
            if (trackedEntities != null) {
                EntityTrackerEntry trackerEntry = null;
                for (Object trackedEntity : trackedEntities) {
                    EntityTrackerEntry entry = (EntityTrackerEntry) trackedEntity;
                    if (entry != null && entry.myEntity == par1) {
                        if (!(entry instanceof VehicleTrackerEntry)) {
                            trackerEntry = entry;
                        }
                        break;
                    }
                }

                if (trackerEntry != null) {
                    trackedEntities.remove(trackerEntry);
                    VehicleTrackerEntry tte = new VehicleTrackerEntry(trackerEntry, par1);
                    trackedEntities.add(tte);
                    return true;
                }
            }
        }
        return false;
    }

    protected static Set getTrackedEntities(EntityTracker tracker) {
        return (Set) NGTUtil.getField(EntityTracker.class, tracker, new String[]{"trackedEntities", "field_72793_b"});
    }
}