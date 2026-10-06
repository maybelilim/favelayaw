package net.favela.yaw.impl.gui.settings;

import net.favela.yaw.impl.gui.GUIScreen;
import net.favela.yaw.impl.modules.categories.client.GUI;
import net.favela.yaw.impl.setting.settings.NumberSetting;
import net.favela.yaw.impl.util.animation.Anim;
import net.favela.yaw.impl.util.render.RenderUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.math.BigDecimal;
import java.math.RoundingMode;

public class SliderButton extends Button {

    private final Number min;
    private final Number max;
    private final NumberSetting setting;
    private boolean drag;
    private String displayText = "";

    private final Anim fillAnim = new Anim(0f);

    public SliderButton(NumberSetting setting, ModuleButton btn) {
        super(setting, btn);
        this.setting = setting;
        this.min = setting.getMin();
        this.max = setting.getMax();
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, int mx, int my, float delta, int alpha) {
        super.render(ctx, mx, my, delta, alpha);
        if (drag) drag(mx);
        float fill = setting.get().floatValue() <= min.floatValue() ? 0f : partialMultiplier();
        float animated = fillAnim.to(fill, 25f);
        float fillW = Math.max(1f, getWidth() * animated);
        Color theme = GUI.get().theme.get();
        RenderUtil.rect(ctx, getX(), getY(), getX() + fillW, getY() + getHeight(),
                new Color(theme.getRed(), theme.getGreen(), theme.getBlue(),
                        (int) (theme.getAlpha() * (alpha / 255f))).getRGB());
        renderHover(ctx, mx, my, alpha);
        if (isOpen()) {
            drawString(ctx, displayText + GUIScreen.getInstance().getSym(), getX(), getY(), new Color(255, 255, 255, alpha).getRGB(), alpha);
        } else {
            drawString(ctx, setting.getName(), getX() + 2, getY(), new Color(255, 255, 255, alpha).getRGB(), alpha);
            String value = setting.getRenderText();
            drawString(ctx, "\u00a77" + value, getX() + getWidth() - width(value) - 2, getY(), new Color(255, 255, 255, alpha).getRGB(), alpha);
        }
    }

    @Override
    public void mouseClicked(int mx, int my, int btn) {
        if (isHovering(mx, my)) {
            if (btn == 0 && !isOpen()) drag = true;
            if (btn == 1) setOpen(!isOpen());
        }
    }

    @Override
    public void mouseReleased(int mx, int my, int btn) {
        if (btn == 0 && drag) drag = false;
    }

    @Override
    public void onKeyPressed(int key) {
        if (!isOpen()) return;
        switch (key) {
            case GLFW.GLFW_KEY_BACKSPACE -> {
                if (!displayText.isEmpty()) {
                    displayText = displayText.substring(0, displayText.length() - 1);
                }
            }
            case GLFW.GLFW_KEY_ENTER -> {
                try {
                    switch (setting.get()) {
                        case Float ignored -> setting.set(Float.valueOf(displayText));
                        case Double ignored -> setting.set(Double.valueOf(displayText));
                        case Long ignored -> setting.set(Long.valueOf(displayText));
                        case Integer ignored -> setting.set(Integer.valueOf(displayText));
                        default -> {
                        }
                    }
                    displayText = "";
                    setOpen(false);
                } catch (NumberFormatException e) {
                    displayText = "";
                    setOpen(false);
                }
            }
            case GLFW.GLFW_KEY_ESCAPE -> setOpen(false);
        }
    }

    @Override
    public void onCharTyped(char typedChar, int keyCode) {
        if (!isOpen()) return;
        if (Character.isDigit(typedChar)) {
            displayText += typedChar;
        } else if (typedChar == '-' && !displayText.contains("-")) {
            displayText = displayText + "-";
        } else if ((typedChar == '.' || typedChar == ',') && !displayText.contains(".")
                && !(setting.get() instanceof Integer) && !(setting.get() instanceof Long)) {
            displayText = displayText + ".";
        }
    }

    @Override
    public int getTotal() {
        return getHeight();
    }

    @Override
    public boolean isHovering(double x, double y) {
        return x >= getX() && x <= getX() + getWidth() && y >= getY() && y <= getY() + getHeight();
    }

    private void drag(int mouseX) {
        int scale = decimalPlaces(setting.getStep().floatValue());
        float percent = Math.clamp((mouseX - getX()) / (float) getWidth(), 0f, 1f);
        switch (setting.get()) {
            case Integer ignored -> setting.set((int) (min.floatValue() + percent * (max.intValue() - min.intValue())));
            case Float ignored -> setting.set(round(min.floatValue() + percent * (max.floatValue() - min.floatValue()), scale));
            case Double ignored -> setting.set(round(min.doubleValue() + percent * (max.doubleValue() - min.doubleValue()), scale));
            default -> {
            }
        }
    }

    private static float round(float value, int scale) {
        return new BigDecimal(value).setScale(scale, RoundingMode.HALF_UP).floatValue();
    }

    private static double round(double value, int scale) {
        return new BigDecimal(value).setScale(scale, RoundingMode.HALF_UP).doubleValue();
    }

    private static int decimalPlaces(float number) {
        String string = Float.toString(number);
        int i = string.indexOf('.');
        if (i == -1) return 0;
        return string.length() - i - 1;
    }

    private float partialMultiplier() {
        return (setting.get().floatValue() - min.floatValue()) / (max.floatValue() - min.floatValue());
    }
}
