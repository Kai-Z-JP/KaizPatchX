package jp.ngt.rtm.gui.camera;

public class CameraKeySet {
    public static final CameraKeySet ZOOM = new CameraKeySet(CameraKey.ZOOM_IN, CameraKey.ZOOM_OUT, 1.0F, 30.0F, 0.05F);

    public static final CameraKeySet SENSITIVITY = new CameraKeySet(CameraKey.SENSIT_UP, CameraKey.SENSIT_DOWN, 0.0F, 1.0F, 0.01F);

    public static final CameraKeySet FOCUS = new CameraKeySet(CameraKey.FOCUS_IN, CameraKey.FOCUS_OUT, 0.0F, 1.0F, 0.005F);

    public static int PREV_KEY;

    private final CameraKey upKey;

    private final CameraKey downKey;

    private final float min;

    private final float max;

    private final float increment;

    public CameraKeySet(CameraKey key1, CameraKey key2, float f1, float f2, float f3) {
        this.upKey = key1;
        this.downKey = key2;
        this.min = f1;
        this.max = f2;
        this.increment = f3;
    }

    public float updateValue(float prevValue) {
        float value = prevValue;
        if (this.upKey.isDown()) {
            value += this.increment;
            if (value > this.max)
                value = this.max;
        }
        if (this.downKey.isDown()) {
            value -= this.increment;
            if (value < this.min)
                value = this.min;
        }
        return value;
    }
}
