package jp.ngt.rtm.rail.util;

public enum MarkerState {
    DISTANCE, GRID, LINE1, LINE2, ANCHOR21;

    private int bitMask() {
        return 1 << ordinal();
    }

    public boolean get(int data) {
        return (data & bitMask()) > 0;
    }

    public int set(int data, boolean state) {
        int mask = bitMask();
        if (state) {
            return data | mask;
        }
        return (data | mask) - mask;
    }

    public int flip(int data) {
        int mask = bitMask();
        return data ^ mask;
    }
}
