package net.favela.yaw.impl.util.keyboard;

import lombok.Getter;
import lombok.Setter;

@Setter
public class Keybinding {

    private int key;
    @Getter
    private bindType mode;

    public Keybinding(int key) {
        this.key = key;
        this.mode = bindType.NORMAL;
    }

    public static Keybinding none() {
        return new Keybinding(-1);
    }

    public int get() {
        return this.key;
    }

    public enum bindType {
        NORMAL,
        HOLD
    }
}