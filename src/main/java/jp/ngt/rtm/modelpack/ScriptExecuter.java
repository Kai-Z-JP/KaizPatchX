package jp.ngt.rtm.modelpack;

import jp.ngt.ngtlib.io.ScriptUtil;
import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.modelpack.modelset.ModelSetBase;
import net.minecraft.command.ICommandManager;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChunkCoordinates;
import net.minecraft.util.IChatComponent;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

public class ScriptExecuter implements ICommandSender {
    private IModelSelector caller;
    public long count;

    protected Object callMethod(IModelSelector selector, String name, Object... args) {
        ModelSetBase set = selector.getModelSet();
        if (set.serverSE != null) {
            return ScriptUtil.doScriptIgnoreError(set.serverSE, name, args);
        }
        return null;
    }

    public void execScript(IModelSelector selector) {
        this.caller = selector;
        this.callMethod(selector, "onUpdate", selector, this);
        ++this.count;
    }

    public void execCommand(String command) {
        MinecraftServer server = NGTUtil.getServer();

        if (server != null && server.isCommandBlockEnabled())//&& server.isAnvilFileSet()
        {
            ICommandManager icommandmanager = server.getCommandManager();
            try {
                icommandmanager.executeCommand(this, command);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
            }
        }
    }

    //1.12
	/*public void fireBullet(World world, Entity shooter, String type, double posX, double posY, double posZ, double speedX, double speedY, double speedZ)
	{
		EntityBullet bullet = new EntityBullet(world, shooter, BulletType.getBulletType(type), posX, posY, posZ, speedX, speedY, speedZ);
		world.spawnEntity(bullet);
	}*/

    @Override
    public World getEntityWorld() {
        if (this.caller instanceof Entity) {
            return ((Entity) this.caller).worldObj;
        } else if (this.caller instanceof TileEntity) {
            return ((TileEntity) this.caller).getWorldObj();
        }
        return null;
    }

    @Override
    public String getCommandSenderName() {
        return "RTM Script Executer";
    }

    @Override
    public IChatComponent func_145748_c_() {
        return new ChatComponentText(this.getCommandSenderName());
    }

    @Override
    public void addChatMessage(IChatComponent p_145747_1_) {
        // TODO 自動生成されたメソッド・スタブ

    }

    @Override
    public boolean canCommandSenderUseCommand(int permLevel, String commandName) {
        return permLevel <= 2;
    }

    @Override
    public ChunkCoordinates getPlayerCoordinates() {
        int x = 0;
        int y = 0;
        int z = 0;
        if (this.caller instanceof Entity) {
            Entity entity = (Entity) this.caller;
            x = MathHelper.floor_double(entity.posX);
            y = MathHelper.floor_double(entity.posY);
            z = MathHelper.floor_double(entity.posZ);
        } else if (this.caller instanceof TileEntity) {
            TileEntity entity = (TileEntity) this.caller;
            x = entity.xCoord;
            y = entity.yCoord;
            z = entity.zCoord;
        }
        return new ChunkCoordinates(x >> 4, y, z >> 4);
    }
}