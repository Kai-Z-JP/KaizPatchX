package jp.ngt.rtm.entity.ai;

import jp.ngt.rtm.entity.npc.EntityNPC;
import jp.ngt.rtm.item.ItemGun;
import net.minecraft.entity.ai.EntityAIArrowAttack;
import net.minecraft.item.ItemStack;

public class EntityAIRangedAttackWithItem extends EntityAIArrowAttack {
    private final EntityNPC npc;

    public EntityAIRangedAttackWithItem(EntityNPC par1, double speed, int par3, int maxTime, float range) {
        super(par1, speed, par3, maxTime, range);
        this.npc = par1;
    }

    @Override
    public boolean shouldExecute() {
        ItemStack item = this.npc.getHeldItem();
        if (item != null && item.getItem() instanceof ItemGun) {
            return super.shouldExecute();
        }
        return false;
    }

    @Override
    public boolean continueExecuting() {
        return super.continueExecuting();
    }
}