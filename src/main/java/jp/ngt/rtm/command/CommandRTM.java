package jp.ngt.rtm.command;

import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.entity.train.*;
import jp.ngt.rtm.entity.train.parts.EntityFloor;
import jp.ngt.rtm.entity.train.parts.EntityVehiclePart;
import jp.ngt.rtm.entity.train.util.Formation;
import jp.ngt.rtm.entity.train.util.FormationManager;
import jp.ngt.rtm.entity.train.util.TrainState.TrainStateType;
import jp.ngt.rtm.modelpack.ModelPackManager;
import jp.ngt.rtm.modelpack.cfg.TrainConfig;
import jp.ngt.rtm.modelpack.modelset.ModelSetVehicleBase;
import jp.ngt.rtm.network.PacketNotice;
import jp.ngt.rtm.rail.TileEntityLargeRailBase;
import jp.ngt.rtm.rail.util.RailMap;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.JsonToNBT;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTException;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.AxisAlignedBB;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class CommandRTM extends CommandBase {
    @Override
    public int getRequiredPermissionLevel() {
        return 2;
    }

    @Override
    public String getCommandName() {
        return "rtm";
    }

    @Override
    public String getCommandUsage(ICommandSender commandSender) {
        return "commands.rtm.usage";
    }

    @Override
    public void processCommand(ICommandSender commandSender, String[] s) {
        EntityPlayerMP player = commandSender instanceof EntityPlayerMP ? (EntityPlayerMP) commandSender : null;

        if (s.length >= 1) {
            if (s[0].equalsIgnoreCase("use1122marker") && player != null) {
                RTMCore.NETWORK_WRAPPER.sendTo(new PacketNotice(PacketNotice.Side_CLIENT, "use1122marker," + (s.length == 2 ? Boolean.parseBoolean(s[1]) : "flip")), player);
            } else if (s[0].equalsIgnoreCase("delRidingFormation") && player != null) {
                if (player.ridingEntity instanceof EntityTrainBase) {
                    Formation formation = ((EntityTrainBase) player.ridingEntity).getFormation();
                    if (formation != null) {
                        int countTrain = formation.size();
                        formation.getTrainStream().forEach(EntityTrainBase::setDead);
                        commandSender.addChatMessage(new ChatComponentText("Deleted " + countTrain + "trains."));
                    }
                }
            } else if (s[0].equalsIgnoreCase("delAllTrain")) {
                int countTrain = 0;
                int countEntity = 0;
                List<Entity> list = commandSender.getEntityWorld().loadedEntityList;
                for (Entity entity0 : list) {
                    Entity entity1;
                    if (entity0 instanceof EntityTrainBase) {
                        entity1 = entity0;
                        ++countTrain;
                    } else if (entity0 instanceof EntityBogie || entity0 instanceof EntityFloor) {
                        entity1 = entity0;
                    } else {
                        continue;
                    }
                    ++countEntity;

                    if (!entity1.isDead) {
                        entity1.setDead();
                    }
                }
                int countFormation = FormationManager.getInstance().clearFormations();

                commandSender.addChatMessage(new ChatComponentText("Deleted " + countTrain + "trains."));
                commandSender.addChatMessage(new ChatComponentText("Deleted " + countEntity + "entities."));
                commandSender.addChatMessage(new ChatComponentText("Deleted " + countFormation + "formations."));
            } else if (s.length == 2 && player != null) {
                if (s[0].equalsIgnoreCase("flySpeed")) {
                    float speed = MathHelper.clamp_float(Float.parseFloat(s[1]), 0, 10);
                    RTMCore.NETWORK_WRAPPER.sendTo(new PacketNotice(PacketNotice.Side_CLIENT, "flySpeed," + speed), player);
                } else if (stateArray.contains(s[0].toLowerCase())) {
                    int state = Integer.parseInt(s[1]);

                    double d0 = 16.0D;
                    List<Entity> list = commandSender.getEntityWorld().getEntitiesWithinAABBExcludingEntity(player, AxisAlignedBB.getBoundingBox(player.posX - d0, player.posY - d0, player.posZ - d0, player.posX + d0, player.posY + d0, player.posZ + d0));
                    list.stream().filter(EntityTrainBase.class::isInstance).map(EntityTrainBase.class::cast).forEach(train -> {
                        if (s[0].equalsIgnoreCase("door")) {
                            train.setTrainStateData(TrainStateType.State_Door.id, (byte) state);
                        } else if (s[0].equalsIgnoreCase("pan")) {
                            train.setTrainStateData(TrainStateType.State_Pantograph.id, (byte) state);
                        } else if (s[0].equalsIgnoreCase("speed")) {
                            train.setSpeed(state / 72.0f);
                        }
                    });
                }
            } else if (s.length >= 5 && s[0].equalsIgnoreCase("summon")) {
                World world = commandSender.getEntityWorld();
                String type = s[1];
                String modelName = s[2];
                int x = Integer.parseInt(s[3]);
                int y = Integer.parseInt(s[4]);
                int z = Integer.parseInt(s[5]);
                float cYaw = 0;
                NBTTagCompound nbt = new NBTTagCompound();
                if (s.length >= 7) {
                    cYaw = Float.parseFloat(s[6]);
                    if (s.length >= 8) {
                        String dataMapStr = func_147178_a(commandSender, s, 7).getUnformattedText();
                        try {
                            NBTBase nbtbase = JsonToNBT.func_150315_a(dataMapStr);

                            if (!(nbtbase instanceof NBTTagCompound)) {
                                func_152373_a(commandSender, this, "commands.summon.tagError", "Not a valid tag");
                                return;
                            } else {
                                nbt = (NBTTagCompound) nbtbase;
                            }
                        } catch (NBTException nbtexception) {
                            func_152373_a(commandSender, this, "commands.summon.tagError", nbtexception.getMessage());
                            return;
                        }
                    }
                }

                if (type.startsWith(TrainConfig.TYPE + ":")) {
                    String subType = type.split(":")[1];

                    EntityTrainBase train;
                    switch (subType) {
                        case "CC":
                            train = new EntityFreightCar(world, "");
                            break;
                        case "TC":
                            train = new EntityTanker(world, "");
                            break;
                        case "EC":
                        case "Test":
                        case "DC":
                            train = new EntityTrain(world, "");
                            break;
                        default:
                            commandSender.addChatMessage(new ChatComponentText(String.format("SubType named %s not found.", subType)));
                            return;
                    }

                    ModelSetVehicleBase<TrainConfig> modelSet = ModelPackManager.INSTANCE.getModelSet(TrainConfig.TYPE, modelName);
                    if (modelSet.isDummy() || !modelSet.getConfig().getSubType().equals(subType)) {
                        commandSender.addChatMessage(new ChatComponentText(String.format("Model named %s not found in %s.", modelName, type)));
                        return;
                    }

                    RailMap rm0 = TileEntityLargeRailBase.getRailMapFromCoordinates(world, player, x, y, z);
                    if (rm0 == null) {
                        commandSender.addChatMessage(new ChatComponentText("Rail not found."));
                        return;
                    }

                    int r = 16;
                    List<Entity> list = world.getEntitiesWithinAABBExcludingEntity(player, AxisAlignedBB.getBoundingBox(x - r, y - 4, z - r, x + r + 1, y + 8, z + r + 1));
                    for (Entity entity : list) {
                        if (entity instanceof EntityTrainBase || entity instanceof EntityBogie || entity instanceof EntityVehiclePart) {
                            double distanceSq = entity.getDistanceSq(x, y, z);
                            float f0 = modelSet.getConfig().trainDistance + 4.0F;
                            RailMap rm1 = TileEntityLargeRailBase.getRailMapFromCoordinates(world, player, entity.posX, entity.posY, entity.posZ);
                            if (distanceSq < f0 * f0 && rm0.equals(rm1)) {
                                func_152373_a(commandSender, this, "commands.summon.failed");
                                func_152373_a(commandSender, this, "message.train.obstacle", entity.toString());
                                return;
                            }
                        }
                    }

                    int i0 = rm0.getNearlestPoint(128, (double) x + 0.5D, (double) z + 0.5D);
                    float yw0 = MathHelper.wrapAngleTo180_float(rm0.getRailRotation(128, i0));
                    float yaw = EntityBogie.fixBogieYaw(cYaw, yw0);
                    float pitch = EntityBogie.fixBogiePitch(rm0.getRailPitch(128, i0), yw0, yaw);
                    double posX = rm0.getRailPos(128, i0)[1];
                    double posY = rm0.getRailHeight(128, i0) + EntityTrainBase.TRAIN_HEIGHT;
                    double posZ = rm0.getRailPos(128, i0)[0];

                    train.setPositionAndRotation(posX, posY, posZ, yaw, pitch);
                    train.getResourceState().readFromNBT(nbt);
                    train.setModelName(modelName);
                    train.spawnTrain(world);
                    train.onModelChanged();
                    func_152373_a(commandSender, this, "commands.summon.success");
                } else {
                    commandSender.addChatMessage(new ChatComponentText(String.format("Type named %s not found.", type)));
                }
            }
        }
    }

    private static final List<String> stateArray = Arrays.asList("door", "pan", "speed");

    private static final List<String> commandList = Arrays.asList("use1122marker", "door", "pan", "speed", "delAllTrain", "delRidingFormation", "flySpeed", "summon");

    @Override
    public List<String> addTabCompletionOptions(ICommandSender sender, String[] args) {
        if (args.length == 1) {
            //入力されている文字列と先頭一致
            if (args[0].length() == 0) {
                return commandList;
            }
            return commandList.stream().filter(s -> s.startsWith(args[0])).collect(Collectors.toList());
        }
        return null;
    }

}