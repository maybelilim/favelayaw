package net.favela.yaw.impl.modules.categories.client;

import com.google.auto.service.AutoService;
import lombok.Getter;
import net.favela.yaw.EntryPoint;
import net.favela.yaw.impl.modules.Module;
import net.favela.yaw.impl.setting.settings.ColorSetting;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.network.chat.Style;

import java.awt.Color;

@AutoService(Module.class)
public class Notification extends Module {

    @Getter
    private static Notification instance;

    public ColorSetting prefixColor;

    public Notification() {
        super("Notification", "Module toggle notifications", Category.CLIENT);
        instance = this;

        Color defaultColor = GUI.get() != null ? GUI.get().theme.get() : new Color(163, 135, 255, 255);

        prefixColor = color("Prefix Color", "Color of [" + EntryPoint.name() + "] prefix", defaultColor, true);
    }

    public void notify(String moduleName, boolean enabled) {
        if (!this.isEnabled()) return;

        Color c = prefixColor.get();
        int rgb = ((c.getRed() & 0xFF) << 16)
                | ((c.getGreen() & 0xFF) << 8)
                | (c.getBlue() & 0xFF);

        Style accent = Style.EMPTY.withColor(rgb);

        MutableComponent msg = Component.empty()
                .append(Component.literal("[").withStyle(accent))
                .append(Component.literal(EntryPoint.name()).withStyle(accent))
                .append(Component.literal("] ").withStyle(accent))
                .append(Component.literal(moduleName).withStyle(Style.EMPTY.withColor(0xFFFFFF)))
                .append(Component.literal(" was ").withStyle(Style.EMPTY.withColor(ChatFormatting.GRAY)))
                .append(Component.literal(enabled ? "enabled" : "disabled")
                        .withStyle(Style.EMPTY.withColor(enabled ? 0x55FF55 : 0xFF5555)));

        Minecraft mc = Minecraft.getInstance();
        if (mc.gui != null && mc.gui.hud != null) {
            mc.execute(() -> mc.gui.hud.getChat().addClientSystemMessage(msg));
        }
    }
}
