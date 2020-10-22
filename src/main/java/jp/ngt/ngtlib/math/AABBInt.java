package jp.ngt.ngtlib.math;

public class AABBInt {
	public int minX, minY, minZ;
	public int maxX, maxY, maxZ;

	public AABBInt(int p1, int p2, int p3, int p4, int p5, int p6) {
		this.minX = p1;
		this.minY = p2;
		this.minZ = p3;
		this.maxX = p4;
		this.maxY = p5;
		this.maxZ = p6;
	}

	public boolean isCollided(AABBInt aabb) {
		return this.minX < aabb.maxX && this.maxX > aabb.minX
				&& this.minY < aabb.maxY && this.maxY > aabb.minY
				&& this.minZ < aabb.maxZ && this.maxZ > aabb.minZ;
	}

	public int sizeX() {
		return this.maxX - this.minX;
	}

	public int sizeY() {
		return this.maxY - this.minY;
	}

	public int sizeZ() {
		return this.maxZ - this.minZ;
	}
}