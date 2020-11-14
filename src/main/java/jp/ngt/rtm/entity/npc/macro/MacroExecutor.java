package jp.ngt.rtm.entity.npc.macro;

import jp.ngt.rtm.RTMCore;
import jp.ngt.rtm.entity.npc.macro.TrainCommand.CommandType;
import jp.ngt.rtm.entity.train.EntityTrainBase;
import jp.ngt.rtm.entity.train.util.TrainState;
import jp.ngt.rtm.entity.train.util.TrainState.TrainStateType;
import jp.ngt.rtm.modelpack.cfg.TrainConfig;
import net.minecraft.util.ResourceLocation;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class MacroExecutor {
	private final List<TrainCommand> commands = new ArrayList<>();
	private boolean executing;
	private long startTime;

	public MacroExecutor(String[] args) {
		Arrays.stream(args).map(TrainCommand::parse).filter(Objects::nonNull).forEach(this.commands::add);
		this.executing = false;
	}

	public boolean start(World world) {
		if (this.executing) {
			return false;
		} else {
			this.executing = true;
			this.startTime = world.getWorldTime();
			return true;
		}
	}

	public boolean stop(World world) {
		if (this.executing) {
			this.executing = false;
			this.startTime = 0L;
			return true;
		} else {
			return false;
		}
	}

	public boolean finished() {
		return this.commands.isEmpty();
	}

	public void tick(World world, EntityTrainBase train) {
		TrainCommand command = this.commands.get(0);
		long time = world.getWorldTime() - this.startTime;
		if (time >= command.time) {
			this.execCommand(train, command.type, command.parameter);
			this.commands.remove(0);
		}
	}

	private void execCommand(EntityTrainBase train, CommandType type, Object param) {
		try {
			switch (type) {
				case Notch:
					this.execNotch(train, Integer.parseInt(param.toString()));
					return;
				case Horn:
					this.execHorn(train);
					return;
				case Chime:
					this.execChime(train, null);
					return;
				case Door:
					this.execDoor(train, TrainState.valueOf(param.toString()));
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void execNotch(EntityTrainBase train, int notch) {
		train.addNotch(train.riddenByEntity, notch);
	}

	private void execHorn(EntityTrainBase train) {
		TrainConfig cfg = train.getModelSet().getConfig();
		String[] sa = cfg.sound_Horn.split(":");
		if (sa.length == 2) {
			RTMCore.proxy.playSound(train, new ResourceLocation(sa[0], sa[1]), 1.0F, 1.0F);
		} else {
			RTMCore.proxy.playSound(train, new ResourceLocation("rtm", sa[0]), 1.0F, 1.0F);
		}
	}

	private void execChime(EntityTrainBase train, String name) {
		String[] sa = name.split(":");
		if (sa.length == 2) {
			RTMCore.proxy.playSound(train, new ResourceLocation(sa[0], sa[1]), 1.0F, 1.0F);
		}
	}

	private void execDoor(EntityTrainBase train, TrainState state) {
		train.setTrainStateData(TrainStateType.State_Door.id, state.data);
	}
}