package jp.ngt.rtm.gui.camera;

import org.lwjgl.input.Keyboard;

public enum CameraKey {
    ZOOM_IN(Keyboard.KEY_X, 'X'),
    ZOOM_OUT(Keyboard.KEY_Z, 'Z'),
    SENSIT_UP(Keyboard.KEY_V, 'V'),
    SENSIT_DOWN(Keyboard.KEY_C, 'C'),
    FOCUS_IN(Keyboard.KEY_N, 'N'),
    FOCUS_OUT(Keyboard.KEY_B, 'B'),
    FOCUS_MODE(Keyboard.KEY_M, 'M'),
    DEBUG(Keyboard.KEY_K, 'K');

    public final int key;

    public final char chara;

    CameraKey(int p1, char p2) {
        this.key = p1;
        this.chara = p2;
    }

    public boolean isDown() {
        return Keyboard.isKeyDown(this.key);
    }

    public boolean isPressed() {
        if (CameraKeySet.PREV_KEY != this.key) {
            if (isDown()) {
                CameraKeySet.PREV_KEY = this.key;
                return true;
            }
        } else if (!isDown()) {
            CameraKeySet.PREV_KEY = 0;
        }
        return false;
    }
}
