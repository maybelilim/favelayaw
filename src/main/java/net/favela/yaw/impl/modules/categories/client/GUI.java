package net.favela.yaw.impl.modules.categories.client;

import com.google.auto.service.AutoService;
import lombok.Getter;
import net.favela.yaw.impl.gui.GUIScreen;
import net.favela.yaw.impl.modules.Module;
import net.favela.yaw.impl.setting.settings.BooleanSetting;
import net.favela.yaw.impl.setting.settings.ColorSetting;
import net.favela.yaw.impl.setting.settings.EnumSetting;
import net.favela.yaw.impl.setting.settings.NumberSetting;
import net.favela.yaw.impl.setting.settings.StringSetting;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;

import static net.favela.yaw.impl.util.wrapper.Wrapper.MC;

@Getter
@AutoService(Module.class)
public class GUI extends Module {

    private static GUI instance;

    public ColorSetting theme = color("Theme", "GUI accent color", new Color(163, 135, 255, 255), true);
    public NumberSetting width = num("Width", "Extra module width", -50, 150, 0);
    public NumberSetting height = num("Height", "Extra module height", -2, 4, 0);
    public EnumSetting<Text> text = enm("Text", "Module text style", Text.Separate);
    public BooleanSetting darkBackground = bool("DarkBackground", "Darken module background", false);
    public BooleanSetting gear = bool("Gear", "Show open indicator", true);
    public EnumSetting<GearStyle> gearStyle = enm("GearStyle", () -> gear.get(), GearStyle.Plus);
    public StringSetting gearOpen = str("GearOpen", () -> gear.get() && gearStyle.get() == GearStyle.Custom, "-");
    public StringSetting gearClosed = str("GearClosed", () -> gear.get() && gearStyle.get() == GearStyle.Custom, "+");
    public BooleanSetting binds = bool("Binds", "Show module binds", false);
    public ColorSetting enabledText = color("EnabledText", () -> text.get() == Text.Custom, Color.WHITE, true);
    public ColorSetting disabledText = color("DisabledText", () -> text.get() == Text.Custom, Color.GRAY, true);
    public EnumSetting<DescriptionMode> showDescription = enm("Description", "Description mode", DescriptionMode.Center);
    public BooleanSetting outline = bool("Outline", "Outline frames", true);
    public BooleanSetting showCount = bool("ShowCount", "Show module count", true);
    public BooleanSetting blur = bool("Blur", "Blur background", false);
    public BooleanSetting darken = bool("Darken", "Darken background", true);
    public NumberSetting darkenStrength = num("DarkenStrength", () -> darken.get(), 0, 255, 120);
    public ColorSetting backgroundGradient = color("BackgroundGradient", new Color(0, 0, 0, 90), true);

    public GUI() {
        super("GUI", "Opens the click gui", Category.CLIENT);
        instance = this;
        setBind(GLFW.GLFW_KEY_RIGHT_SHIFT);
    }

    public static GUI get() {
        return instance;
    }

    @Override
    public void onEnable() {
        GUIScreen.getInstance().open();
    }

    @Override
    public void onDisable() {
        if (MC.gui.screen() instanceof GUIScreen screen) {
            screen.startClosing();
        }
    }

    public int getTextOffset() {
        int h = height.getInt();
        if (h == -2) return 1;
        if (h == 2) return 3;
        return 2;
    }

    public enum Text {
        Normal, Separate, Custom
    }

    public enum DescriptionMode {
        Off, Center
    }

    public enum GearStyle {
        Plus("+", "-"),
        Custom("+", "-");

        @Getter
        private final String closed;
        @Getter
        private final String open;

        GearStyle(String closed, String open) {
            this.closed = closed;
            this.open = open;
        }
    }
}
