package jp.ngt.rtm.world

import jp.ngt.rtm.entity.train.EntityTrainBase
import jp.ngt.rtm.entity.train.util.Formation
import net.minecraft.entity.Entity
import net.minecraft.util.MathHelper
import net.minecraft.world.World

internal object FormationChunkOccupancy {
    fun liveTrains(formation: Formation): List<EntityTrainBase> {
        return formation.entries
            .mapNotNull { it?.train }
            .filterNot { it.isDead }
    }

    fun collect(
        world: World,
        trains: List<EntityTrainBase>,
    ): Set<FormationChunkCoordinate> {
        val occupied = linkedSetOf<FormationChunkCoordinate>()
        trains.forEach { train ->
            occupied += coordinateOf(train)
            for (index in 0..1) {
                train.getBogie(index)?.takeIf { !it.isDead && it.worldObj === world }?.let {
                    occupied += coordinateOf(it)
                }
            }
            train.vehicleFloors
                .filterNotNull()
                .filter { !it.isDead && it.worldObj === world }
                .forEach { occupied += coordinateOf(it) }
        }
        return occupied
    }

    private fun coordinateOf(entity: Entity): FormationChunkCoordinate {
        return FormationChunkCoordinate(
            MathHelper.floor_double(entity.posX) shr 4,
            MathHelper.floor_double(entity.posZ) shr 4,
        )
    }
}
