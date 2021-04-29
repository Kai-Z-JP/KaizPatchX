package jp.ngt.ngtlib.util;

import org.lwjgl.input.Keyboard;

public class KeyboardUtil {
    public static boolean isIntegerKey(int par) {
        return (par >= Keyboard.KEY_1 && par <= Keyboard.KEY_0) ||
                (par >= Keyboard.KEY_NUMPAD7 && par <= Keyboard.KEY_NUMPAD9) ||
                (par >= Keyboard.KEY_NUMPAD4 && par <= Keyboard.KEY_NUMPAD6) ||
                (par >= Keyboard.KEY_NUMPAD1 && par <= Keyboard.KEY_NUMPAD3) ||
                par == Keyboard.KEY_NUMPAD0 ||
                (par >= Keyboard.KEY_UP && par <= Keyboard.KEY_RIGHT) ||
                par == Keyboard.KEY_MINUS || par == Keyboard.KEY_SUBTRACT ||
                par == Keyboard.KEY_BACK ||
                par == Keyboard.KEY_DELETE;
    }

    public static boolean isDecimalNumberKey(int par) {
        return (par >= Keyboard.KEY_1 && par <= Keyboard.KEY_0) ||
                (par >= Keyboard.KEY_NUMPAD7 && par <= Keyboard.KEY_NUMPAD9) ||
                (par >= Keyboard.KEY_NUMPAD4 && par <= Keyboard.KEY_NUMPAD6) ||
                (par >= Keyboard.KEY_NUMPAD1 && par <= Keyboard.KEY_NUMPAD3) ||
                par == Keyboard.KEY_NUMPAD0 ||
                (par >= Keyboard.KEY_UP && par <= Keyboard.KEY_RIGHT) ||
                par == Keyboard.KEY_MINUS || par == Keyboard.KEY_SUBTRACT ||
                par == Keyboard.KEY_BACK ||
                par == Keyboard.KEY_DELETE ||
                par == Keyboard.KEY_PERIOD || par == Keyboard.KEY_DECIMAL;
    }
}
