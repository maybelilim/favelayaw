package net.favela.yaw.impl.modules.categories.hud;

import com.google.auto.service.AutoService;
import net.favela.yaw.EntryPoint;
import net.favela.yaw.impl.gui.hud.Hud;
import net.favela.yaw.impl.modules.Module;
import net.favela.yaw.impl.modules.categories.client.GUI;
import net.favela.yaw.impl.setting.settings.BooleanSetting;
import net.favela.yaw.impl.setting.settings.EnumSetting;
import net.favela.yaw.impl.setting.settings.StringSetting;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import static net.favela.yaw.impl.util.wrapper.Wrapper.MC;

@AutoService(Module.class)
public class Welcomer extends Hud {

    public enum WelcomerMode {
        WELCOME, WELCOMETO, HI, HELLO, CUSTOM
    }

    public EnumSetting<WelcomerMode> mode = enm("Mode", WelcomerMode.WELCOME);
    public StringSetting customText = str("CustomText", "Welcome to " + EntryPoint.name());
    public BooleanSetting showName = bool("ShowName", true);
    public BooleanSetting smiley = bool("Smiley", true);
    public BooleanSetting center = bool("Center", true);

    public Welcomer() {
        super("Welcomer", "Displays a welcome message", 100, 100);
    }

    @Override
    public void render(GuiGraphicsExtractor context) {
        super.render(context);
        if (MC.player == null || MC.level == null) return;

        Font font = MC.font;

        String username = MC.getUser().getName();
        String prefix = switch (mode.get()) {
            case WELCOME -> "Welcome ";
            case WELCOMETO -> "Welcome to " + EntryPoint.name() + " ";
            case HI -> "hi ";
            case HELLO -> "Hello ";
            case CUSTOM -> customText.get();
        };

        String name = (mode.get() == WelcomerMode.CUSTOM && !showName.get()) ? "" : username;
        String smile = smiley.get() ? " <3" : "";
        String full = prefix + name + smile;

        int textWidth = font.width(full);
        int y = (int) getY();
        float x = center.get()
                ? (MC.getWindow().getGuiScaledWidth() - textWidth) / 2.0f
                : getX();

        int accent = GUI.get() != null ? GUI.get().theme.getRGB() : 0xFFA387FF;

        x = draw(context, font, prefix, x, y, accent);
        x = draw(context, font, name, x, y, 0xFFFFFFFF);
        draw(context, font, smile, x, y, accent);

        setWidth(textWidth);
        setHeight(font.lineHeight + 2);
    }

    private float draw(GuiGraphicsExtractor context, Font font, String text, float x, int y, int color) {
        if (text.isEmpty()) return x;
        context.text(font, text, (int) x, y, color, true);
        return x + font.width(text);
    }
}
