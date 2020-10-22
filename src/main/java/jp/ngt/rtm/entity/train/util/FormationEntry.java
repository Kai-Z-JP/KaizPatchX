package jp.ngt.rtm.entity.train.util;

import jp.ngt.ngtlib.util.NGTUtil;
import jp.ngt.rtm.entity.train.EntityTrainBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;

public class FormationEntry implements Comparable<FormationEntry> {
	public final EntityTrainBase train;
	public byte entryId;
	public byte dir;

	private float mileage;
	private byte jointCount = -1;

	/**
	 * @param par1 車両
	 * @param par2 位置
	 * @param par3 向き
	 */
	public FormationEntry(EntityTrainBase par1, int par2, int par3) {
		this.train = par1;
		this.entryId = (byte) par2;
		this.dir = (byte) par3;
	}

	public static FormationEntry readFromNBT(NBTTagCompound nbt) {
		int trainId = nbt.getInteger("TrainId");
		byte pos = nbt.getByte("EntryPos");
		byte dir = nbt.getByte("EntryDir");
		World world = NGTUtil.getClientWorld();
		EntityTrainBase entity = (EntityTrainBase) world.getEntityByID(trainId);
		if (entity == null) {
			return null;
		}
		return new FormationEntry(entity, pos, dir);
	}

	public void writeToNBT(NBTTagCompound nbt) {
		nbt.setInteger("TrainId", this.train.getEntityId());
		nbt.setByte("EntryPos", this.entryId);
		nbt.setByte("EntryDir", this.dir);
	}

	/**
	 * 編成データ更新(Entity側も)
	 *
	 * @param par1
	 * @param i    位置
	 */
	public void updateFormationData(Formation par1, int i) {
		this.entryId = (byte) i;
		this.train.setFormation(par1);
	}

	@Override
	public int compareTo(FormationEntry obj) {
		return this.entryId - obj.entryId;
	}
}