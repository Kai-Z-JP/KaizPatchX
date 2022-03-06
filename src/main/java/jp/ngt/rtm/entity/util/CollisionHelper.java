package jp.ngt.rtm.entity.util;

import jp.ngt.rtm.modelpack.IModelSelector;
import jp.ngt.rtm.modelpack.modelset.ModelSetBase;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.world.World;

import java.util.List;

public final class CollisionHelper {
    public static final CollisionHelper INSTANCE = new CollisionHelper();

    private CollisionHelper() {
    }

    public void syncCollisionObj(String type, ModelSetBase modelSet) {
        if (modelSet.getCollisionObj() != null) {
            Thread thread = new Thread() {
                @Override
                public void run() {
                    modelSet.getCollisionObj().syncCollisionObj(type, modelSet, this);
                }
            };
            thread.start();
        }
    }

    //net/minecraft/world/World.java:1644へCoreMod挿入必要
    public static void onCollision(World world, Entity entityIn, AxisAlignedBB aabb, List<AxisAlignedBB> aabbList) {
        if (!(entityIn instanceof EntityPlayer)) {
            return;
        }

        double range = 10.5D;//通常0.25
        ((List<Entity>) world.getEntitiesWithinAABBExcludingEntity(entityIn, aabb.expand(range, range, range)))
                .stream()
                .filter(entity -> !entity.isDead && entity instanceof IModelSelector)
                .forEach(entity -> ((IModelSelector) entity).getResourceState().applyCollison(entityIn, entity, aabb, aabbList));
    }
}