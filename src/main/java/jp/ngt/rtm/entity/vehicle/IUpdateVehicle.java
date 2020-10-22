package jp.ngt.rtm.entity.vehicle;

import net.minecraft.server.gui.IUpdatePlayerListBox;

public interface IUpdateVehicle extends IUpdatePlayerListBox {
	void onModelChanged();
}