package jp.ngt.ngtlib.io;

public interface IProgressWatcher {
    void setMaxValue(int id, int value, String label);

    void addMaxValue(int id, int value);

    void setValue(int id, int value, String label);

    void addValue(int id, String label);

    void setText(int id, String label);

    void finish();
}