package jp.ngt.mcte.block;

import jp.ngt.mcte.MCTE;
import jp.ngt.mcte.world.MCTEWorld;
import jp.ngt.ngtlib.block.BlockSet;
import net.minecraft.util.Direction;
import net.minecraft.util.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.util.ForgeDirection;

import java.util.stream.IntStream;

/**
 * ミニチュアブロックのRS入出力を担う
 */
public class RSPort {
	private PortObj[] ports;

	public PortObj getPort(MCTEWorld world, int index) {
		if (this.ports == null) {
			this.ports = new PortObj[4];

			for (int i = 0; i < world.blockObject.xSize; ++i) {
				for (int j = 0; j < world.blockObject.ySize; ++j) {
					for (int k = 0; k < world.blockObject.zSize; ++k) {
						BlockSet set = world.getBlockSet(i, j, k);
						if (set.block instanceof BlockPort) {
							PortType type = ((BlockPort) set.block).type;
							if (k == 0) {
								this.ports[2] = new PortObj(type, i, j, k);
							} else if (k == world.blockObject.zSize - 1) {
								this.ports[0] = new PortObj(type, i, j, k);
							} else if (i == 0) {
								this.ports[1] = new PortObj(type, i, j, k);
							} else if (i == world.blockObject.xSize - 1) {
								this.ports[3] = new PortObj(type, i, j, k);
							}
						}
					}
				}
			}

			IntStream.range(0, 4).filter(i -> this.ports[i] == null).forEach(i -> this.ports[i] = new PortObj(PortType.NONE, -1, -1, -1));
		}
		return this.ports[index];
	}

	/**
	 * 隣接ブロックからRS入力時
	 */
	public void onNeighborBlockChange(TileEntityMiniature miniature) {
		//下に設置してるもの以外は除外
		if (ForgeDirection.getOrientation(miniature.attachSide) != ForgeDirection.UP) {
			return;
		}
		//5:なんか90度ずれてるので補正
		int dir = (MathHelper.floor_float(-(miniature.getRotation() + 45.0F) / 90.0F) + 5) % 4;
		IntStream.range(0, 4).forEach(i -> this.checkInput(miniature, i, dir));
	}

	private void checkInput(TileEntityMiniature miniature, int index, int dir) {
		MCTEWorld mw = miniature.getDummyWorld();
		PortObj port = this.getPort(mw, index);
		if (port.type == PortType.IN) {
			int fixDir = (index + dir) & 3;
			int blockX = miniature.xCoord + Direction.offsetX[fixDir];
			int blockZ = miniature.zCoord + Direction.offsetZ[fixDir];
			int power = miniature.getWorldObj().getIndirectPowerLevelTo(blockX, miniature.yCoord, blockZ, Direction.directionToFacing[fixDir]);
			int meta = mw.getBlockMetadata(port.x, port.y, port.z);
			if (power != meta) {
				mw.setBlock(port.x, port.y, port.z, MCTE.portIn, power, 3);
			}
		}
	}

	/**
	 * 面ごとのRS出力レベル
	 */
	public int isProvidingPower(TileEntityMiniature miniature, int side) {
		MCTEWorld mw = miniature.getDummyWorld();
		if (mw != null) {
			int dirB = Direction.facingToDirection[side];
			if (dirB >= 0) {
				dirB = Direction.rotateOpposite[dirB];
				int dirM = (MathHelper.floor_float(-(miniature.getRotation() + 45.0F) / 90.0F) + 5) % 4;
				int fixDir = (dirB - dirM + 4) & 3;
				return this.getPort(mw, fixDir).getPower(mw);
			}
		}
		return 0;
	}

	private static class PortObj {
		public final PortType type;
		public final int x, y, z;

		public PortObj(PortType p1, int p2, int p3, int p4) {
			this.type = p1;
			this.x = p2;
			this.y = p3;
			this.z = p4;
		}

		public int getPower(World world) {
			return this.type == PortType.OUT ? world.getBlockMetadata(this.x, this.y, this.z) : 0;
		}
	}

	public enum PortType {
		IN,
		OUT,
		NONE
	}
}