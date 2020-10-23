package jp.ngt.rtm.entity.train.util;

public enum TrainState {
	Door_Close(TrainStateType.State_Door.id, 0, "close"),
	Door_OpenRight(TrainStateType.State_Door.id, 1, "open_right"),
	Door_OpenLeft(TrainStateType.State_Door.id, 2, "open_left"),
	Door_OpenAll(TrainStateType.State_Door.id, 3, "open_all"),

	Light_Off(TrainStateType.State_Light.id, 0, "off"),
	Light_Head(TrainStateType.State_Light.id, 1, "on_0"),
	Light_Head_Tail(TrainStateType.State_Light.id, 2, "on_1"),

	Pantograph_Down(TrainStateType.State_Pantograph.id, 0, "down"),
	Pantograph_Up(TrainStateType.State_Pantograph.id, 1, "up"),

	Direction_Front(TrainStateType.State_Direction.id, 0, "front"),
	Direction_Center(TrainStateType.State_Direction.id, 1, "center"),
	Direction_Back(TrainStateType.State_Direction.id, 2, "back"),

	InteriorLight_Off(TrainStateType.State_InteriorLight.id, 0, "off"),
	InteriorLight_On(TrainStateType.State_InteriorLight.id, 1, "on_0"),
	InteriorLight_Rainbow(TrainStateType.State_InteriorLight.id, 2, "on_1"),
	;

	public final int id;
	public final byte data;
	public final String stateName;

	TrainState(int par1, int par2, String par3) {
		this.id = par1;
		this.data = (byte) par2;
		this.stateName = par3;
	}

	public static TrainState getState(int par1Id, byte par2Data) {
		for (TrainState state : TrainState.values()) {
			if (state.id == par1Id && state.data == par2Data) {
				return state;
			}
		}
		return Door_Close;
	}

	public static TrainStateType getStateType(int par1Id) {
		for (TrainStateType state : TrainStateType.values()) {
			if (state.id == par1Id) {
				return state;
			}
		}
		return TrainStateType.State_Door;
	}

	public enum TrainStateType {
		State_TrainDir(0, "train_dir", 0, 1),
		State_Notch(1, "notch", -8, 5),
		State_Signal(2, "signal", 0, 127),//6
		State_Door(4, "door", 0, 3),
		State_Light(5, "light", 0, 2),
		State_Pantograph(6, "pantograph", 0, 1),
		State_ChunkLoader(7, "chunk_loader", 0, 8),
		State_Destination(8, "destination", 0, 127),
		State_Announcement(9, "announcement", 0, 127),
		/**
		 * 編成内の位置(前,中,後)
		 */
		State_Direction(10, "direction", 0, 2),
		State_InteriorLight(11, "interior_light", 0, 2);

		public final int id;
		public final String stateName;
		public final byte min;
		public final byte max;

		TrainStateType(int par1, String par2, int par3, int par4) {
			this.id = par1;
			this.stateName = par2;
			this.min = (byte) par3;
			this.max = (byte) par4;
		}
	}
}