package jp.ngt.rtm.rail.util;

import jp.ngt.ngtlib.io.NGTLog;
import net.minecraft.world.World;

import java.util.ArrayList;
import java.util.List;

public final class RailMaker {
	private World worldObj;
	private List<RailPosition> rpList;

	public RailMaker(World world, List<RailPosition> par2) {
		this.worldObj = world;
		this.rpList = par2;
	}

	public RailMaker(World world, RailPosition[] par2) {
		this.worldObj = world;
		this.rpList = new ArrayList<RailPosition>();
		for (RailPosition rp : par2) {
			this.rpList.add(rp);
		}
	}

	private SwitchType getSwitchType() {
		if (this.rpList.size() == 3) {
			int i0 = 0;
			for (RailPosition rp : this.rpList) {
				i0 += (rp.switchType == 1) ? 1 : 0;
			}

			if (i0 == 1) {
				return new SwitchType.SwitchBasic();
			}
		} else if (this.rpList.size() == 4) {
			int i0 = 0;
			for (RailPosition rp : this.rpList) {
				i0 += (rp.switchType == 1) ? 1 : 0;
			}

			if (i0 == 2) {
				return new SwitchType.SwitchSingleCross();
			} else if (i0 == 4) {
				for (int i = 0; i < this.rpList.size(); ++i) {
					for (int j = i + 1; j < this.rpList.size(); ++j)//全組み合わせ(重複なし)
					{
						if (this.rpList.get(i).direction == this.rpList.get(j).direction) {
							return new SwitchType.SwitchScissorsCross();
						}
					}
				}
				return new SwitchType.SwitchDiamondCross();
			}
		}

		return null;
	}

	public SwitchType getSwitch() {
		SwitchType type = this.getSwitchType();
		if (type != null) {
			List<RailPosition> switchList = new ArrayList<RailPosition>();//分岐あり
			List<RailPosition> normalList = new ArrayList<RailPosition>();//分岐なし
			for (RailPosition rp : this.rpList) {
				if (rp.switchType == 1) {
					switchList.add(rp);
				} else {
					normalList.add(rp);
				}
			}

			if (type.init(switchList, normalList)) {
				return type;
			}
		}

		if (this.worldObj != null && !this.worldObj.isRemote) {
			RailPosition rp = this.rpList.get(0);
			NGTLog.sendChatMessageToAll("message.rail.switch_type", new Object[]{rp.blockX, rp.blockY, rp.blockZ});
		}

		return null;
	}
}