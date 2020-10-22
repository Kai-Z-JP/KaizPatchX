package jp.ngt.ngtlib.math;

public interface ILine {
	/**
	 * @param par1 分割数
	 * @param par2 進める割合
	 * @return double[]{x, y}
	 */
	double[] getPoint(int par1, int par2);

	/**
	 * @param par1 分割数
	 * @param par2 x
	 * @param par3 z
	 * @return 進める数
	 */
	int getNearlestPoint(int par1, double par2, double par3);

	/**
	 * @param par1 分割数
	 * @param par2 進める割合
	 * @return 傾き（ラジアン）
	 */
	double getSlope(int par1, int par2);

	/**
	 * @return 線分の長さ
	 */
	double getLength();
}