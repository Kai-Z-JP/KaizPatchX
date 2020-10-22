package jp.ngt.rtm.electric;

public interface IProvideElectricity {
	/**
	 * 送信する信号の取得
	 */
	int getElectricity();

	/**
	 * 信号受信
	 */
	void setElectricity(int x, int y, int z, int level);
}