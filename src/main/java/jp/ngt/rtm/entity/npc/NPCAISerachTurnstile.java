package jp.ngt.rtm.entity.npc;

import jp.ngt.rtm.RTMBlock;
import jp.ngt.rtm.item.ItemTicket;
import net.minecraft.block.Block;
import net.minecraft.entity.ai.EntityAIBase;
import net.minecraft.item.ItemStack;
import net.minecraft.pathfinding.PathEntity;
import net.minecraft.util.MathHelper;
import net.minecraft.util.Vec3;

public class NPCAISerachTurnstile extends EntityAIBase {
    protected final EntityNPC npc;
    protected float moveSpeed;
    protected final int[] targetBlockPos = new int[3];
    protected PathEntity entityPathNavigate;
    public boolean openedTurnstile;

    public NPCAISerachTurnstile(EntityNPC par1, float par2) {
        this.npc = par1;
        this.moveSpeed = par2;
    }

    @Override
    public boolean shouldExecute() {
        return this.setTargetTurnstile();
    }

    private boolean setTargetTurnstile() {
        this.openedTurnstile = false;
        this.targetBlockPos[1] = -1;
        int x = MathHelper.floor_double(this.npc.posX);
        int y = MathHelper.floor_double(this.npc.posY);
        int z = MathHelper.floor_double(this.npc.posZ);
        int range = 32;
        double distance = Double.MAX_VALUE;
        for (int i = -range; i < range; ++i) {
            for (int j = -8; j < 16; ++j) {
                for (int k = -range; k < range; ++k) {
                    Block block = this.npc.worldObj.getBlock(x + i, y + j, z + k);
                    if (block == RTMBlock.turnstile) {
                        double dsq = this.npc.getDistanceSq(x + i, y + j, z + k);
                        if (dsq < distance) {
                            this.targetBlockPos[0] = x + i;
                            this.targetBlockPos[1] = y + j;
                            this.targetBlockPos[2] = z + k;
                            distance = dsq;
                        }
                    }
                }
            }
        }

        if (this.targetBlockPos[1] != -1) {
            Vec3 vec3 = Vec3.createVectorHelper(this.targetBlockPos[0] + 0.5D, this.targetBlockPos[1], this.targetBlockPos[2] + 0.5D);
            //vec3 = RandomPositionGenerator.findRandomTargetBlockTowards(this.npc, 3, 1, vec3);
            if (vec3 != null) {
                this.entityPathNavigate = this.npc.getNavigator().getPathToXYZ(vec3.xCoord, vec3.yCoord, vec3.zCoord);
                return this.entityPathNavigate != null;
            }
        }

        return false;
    }

    @Override
    public boolean continueExecuting() {
        boolean b0 = this.npc.getNavigator().noPath();
        //double d0 = this.npc.getDistanceSq(this.targetBlockPos[0] + 0.5D, this.targetBlockPos[1] + 0.5D, this.targetBlockPos[2] + 0.5D);
        if (b0 && this.useTicket())//d0 < 9.0D)
        {
            RTMBlock.turnstile.openGate(this.npc.worldObj, this.targetBlockPos[0], this.targetBlockPos[1], this.targetBlockPos[2], null);
            this.openedTurnstile = true;
            return false;
        }
        return !b0;
    }

    private boolean useTicket() {
        InventoryNPC inventory = this.npc.inventory;
        int index = inventory.hasItem(ItemTicket.class);
        if (index >= 0) {
            ItemStack stack = inventory.getStackInSlot(index);
            if (((ItemTicket) stack.getItem()).ticketType != 2) {
                inventory.setInventorySlotContents(index, ItemTicket.consumeTicket(stack));
            }
            return true;
        }
        return false;
    }

    @Override
    public void startExecuting() {
        this.npc.getNavigator().setPath(this.entityPathNavigate, this.moveSpeed);
    }
}