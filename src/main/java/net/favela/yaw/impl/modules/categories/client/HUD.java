package net.favela.yaw.impl.modules.categories.client;

import com.google.auto.service.AutoService;
import lombok.Getter;
import net.favela.yaw.impl.gui.hud.HudEditorScreen;
import net.favela.yaw.impl.modules.Module;
import net.favela.yaw.impl.setting.settings.NumberSetting;

import java.awt.Color;

import static net.favela.yaw.impl.util.wrapper.Wrapper.MC;

@AutoService(Module.class)
public class HUD extends Module {

    @Getter
    private static HUD instance;

    public NumberSetting offset = num("Offset", 0, 5, 2);

    public HUD() {
        super("HUD", "Edit HUD element positions", Category.CLIENT);
        instance = this;
    }

    @Override
    public void onEnable() {
        if (GUI.get() != null && GUI.get().isEnabled()) GUI.get().disable();
        MC.gui.setScreen(HudEditorScreen.getInstance());
    }

    @Override
    public void onDisable() {
        if (MC.gui.screen() instanceof HudEditorScreen screen) {
            screen.onClose();
        }
    }

    public Color getColor() {
        Color theme = GUI.get().theme.get();
        return new Color(theme.getRed(), theme.getGreen(), theme.getBlue(), 255);
    }
}
