package net.favela.yaw.impl.util.keyboard;

import lombok.Getter;
import lombok.Setter;
import org.lwjgl.glfw.GLFW;

import java.util.Locale;

@Setter
public class Keybinding {

    @Getter
    private BindType mode;

    private int key;

    public Keybinding(int key) {
        this.key = key;
        this.mode = BindType.NORMAL;
    }

    public int get() {
        return this.key;
    }

    public static String format(int key) {
        if (key == -1) return "NONE";
        if (key < -1) return "MOUSE " + (-key - 1);
        String name = GLFW.glfwGetKeyName(key, 0);
        return name != null ? name.toUpperCase(Locale.ROOT) : "KEY " + key;
    }

    public enum BindType {
        NORMAL,
        HOLD
    }
}
