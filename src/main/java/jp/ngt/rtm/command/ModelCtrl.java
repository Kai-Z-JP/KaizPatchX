package jp.ngt.rtm.command;

import jp.ngt.ngtlib.io.NGTLog;
import jp.ngt.rtm.entity.train.EntityTrainBase;
import jp.ngt.rtm.entity.train.util.TrainState;
import jp.ngt.rtm.entity.train.util.TrainState.TrainStateType;
import jp.ngt.rtm.modelpack.IModelSelector;
import net.minecraft.command.ICommandSender;

public enum ModelCtrl {
    NOTCH(
            s -> s.equals("notch"),
            obj -> obj instanceof EntityTrainBase,
            (target, player, order, value) -> {
                int notch = Integer.parseInt(value);
                return ((EntityTrainBase) target).setNotch(notch);
            },
            "mctrl <train> notch <-8 ~ 5>"),
    DIR(
            s -> s.equals("dir"),
            obj -> obj instanceof EntityTrainBase,
            (target, player, order, value) -> {
                int dir = Integer.parseInt(value);
                ((EntityTrainBase) target).setTrainDirection(dir);
                return true;
            },
            "mctrl <train> dir <0 or 1>"),
    DATA_MAP(
            s -> s.startsWith("dm:"),
            obj -> obj instanceof IModelSelector,
            (target, player, order, value) -> {
                String dataName = order.replace("dm:", "");
                if (!((IModelSelector) target).getResourceState().getDataMap().set(dataName, value, 3)) {
                    NGTLog.sendChatMessage(player, "[" + dataName + "] is not key.");
                    return false;
                }
                return true;
            },
            "mctrl <?> dm:<data name> <(type)value>"),
    VEHICLE_STATE(
            s -> s.startsWith("state:"),
            obj -> obj instanceof EntityTrainBase,
            (target, player, order, value) -> {
                String dataName = order.replace("state:", "");
                try {
                    TrainState state = TrainState.valueOf(value);
                    TrainStateType type = TrainStateType.valueOf(dataName);
                    ((EntityTrainBase) target).setTrainStateData(type.id, state.data);
                    return true;
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                    NGTLog.sendChatMessage(player, "Illegal argument.");
                    return false;
                }
            },
            "mctrl <train> state:<data name> <value>"),
    //    V_MOV_DIST(
//            (s) -> {
//                return s.equals("move");
//            },
//            (obj) -> obj instanceof EntityVehicle,
//            (target, player, order, value) -> {
//                EntityVehicle vehicle = ((EntityVehicle) target);
//                double dist = Double.parseDouble(value);
//                vehicle.controller.setMoveDistance(vehicle, dist);
//                return true;
//            },
//            "mctrl <vehicle> move <distance>"),
//    V_ADD_YAW(
//            (s) -> {
//                return s.equals("addYaw");
//            },
//            (obj) -> (obj instanceof EntityVehicle) || (obj instanceof EntityArtillery),
//            (target, player, order, value) -> {
//                if (target instanceof EntityArtillery) {
//                    EntityArtillery firearm = ((EntityArtillery) target);
//                    float yaw = Float.parseFloat(value);
//                    firearm.controller.addYaw(firearm, yaw);
//                } else {
//                    EntityVehicle vehicle = ((EntityVehicle) target);
//                    float yaw = Float.parseFloat(value);
//                    vehicle.controller.addYaw(vehicle, yaw);
//                }
//                return true;
//            },
//            "mctrl <vehicle or artillery> addYaw <value>"),
//    V_ADD_PITCH(
//            (s) -> {
//                return s.equals("addPitch");
//            },
//            (obj) -> (obj instanceof EntityVehicle) || (obj instanceof EntityArtillery),
//            (target, player, order, value) -> {
//                if (target instanceof EntityArtillery) {
//                    EntityArtillery firearm = ((EntityArtillery) target);
//                    float yaw = Float.parseFloat(value);
//                    firearm.controller.addPitch(firearm, -yaw);
//                } else {
//                    //EntityVehicle vehicle =((EntityVehicle)target);
//                    //float yaw = Float.valueOf(value);
//                    //vehicle.controller.addPitch(vehicle, yaw);
//                }
//                return true;
//            },
//            "mctrl <artillery> addPitch <value>"),
//    FIRE(
//            (s) -> {
//                return s.equals("fire");
//            },
//            (obj) -> obj instanceof EntityArtillery,
//            (target, player, order, value) -> {
//                EntityArtillery firearm = ((EntityArtillery) target);
//                BulletType type = BulletType.getBulletType(firearm.getResourceState().getResourceSet().getConfig().ammoType);
//                int count = Integer.parseInt(value);
//                firearm.fire(null, type, count);
//                return true;
//            },
//            "mctrl <artillery> fire <number of bullet>"),
    NO_FUNC(s -> false, obj -> false, (target, player, order, value) -> false, "");

    public final CommandMatcher matcher;
    public final TargetFilter filter;
    public final CommandExecutor executor;
    public final String discription;

    ModelCtrl(CommandMatcher par1, TargetFilter par2, CommandExecutor par3, String par4) {
        this.matcher = par1;
        this.filter = par2;
        this.executor = par3;
        this.discription = par4;
    }

    public static ModelCtrl getCommand(String par1) {
        for (ModelCtrl ctrl : ModelCtrl.values()) {
            if (ctrl.matcher.match(par1)) {
                return ctrl;
            }
        }
        return ModelCtrl.NO_FUNC;
    }

    public interface CommandMatcher {
        boolean match(String s);
    }

    public interface TargetFilter {
        boolean match(Object obj);
    }

    public interface CommandExecutor {
        boolean exec(Object target, ICommandSender player, String order, String value);
    }
}