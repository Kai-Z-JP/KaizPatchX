package jp.ngt.ngtlib.io;

public interface IProgressWatcher {
	void setMaxValue(int id, int value, String label);

	void setValue(int id, int value, String label);

	void setText(int id, String label);

	void finish();
}