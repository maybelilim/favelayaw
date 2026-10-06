package net.favela.yaw.impl.gui.settings;

import net.favela.yaw.impl.gui.GUIScreen;
import net.favela.yaw.impl.modules.categories.client.GUI;
import net.favela.yaw.impl.setting.settings.StringSetting;
import net.favela.yaw.impl.util.render.RenderUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;

public class StringButton extends Button {

    private final StringSetting setting;
    private String displayText = "";

    public StringButton(StringSetting setting, ModuleButton btn) {
        super(setting, btn);
        this.setting = setting;
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, int mx, int my, float delta, int alpha) {
        super.render(ctx, mx, my, delta, alpha);
        float o = openAnim.to(isOpen() ? 1f : 0f, 20f);
        if (o > 0.001f) {
            Color theme = GUI.get().theme.get();
            RenderUtil.rect(ctx, getX(), getY(), getX() + getWidth(), getY() + getHeight(),
                    new Color(theme.getRed(), theme.getGreen(), theme.getBlue(),
                            (int) (theme.getAlpha() * 0.5f * o * (alpha / 255f))).getRGB());
        }
        renderHover(ctx, mx, my, alpha);
        if (isOpen()) {
            drawString(ctx, "\u00a77" + displayText + GUIScreen.getInstance().getSym(), getX() + 2, getY(), new Color(128, 128, 128, alpha).getRGB(), alpha);
        } else {
            drawString(ctx, setting.get(), getX() + 2, getY(), new Color(255, 255, 255, alpha).getRGB(), alpha);
        }
    }

    @Override
    public void mouseClicked(int mx, int my, int btn) {
        if (isHovering(mx, my) && btn == 0) {
            setOpen(!isOpen());
            displayText = setting.get();
        }
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
                setting.set(displayText);
                setOpen(false);
            }
            case GLFW.GLFW_KEY_ESCAPE -> setOpen(false);
        }
    }

    @Override
    public void onCharTyped(char typedChar, int keyCode) {
        if (isOpen()) displayText += typedChar;
    }

    @Override
    public int getTotal() {
        return getHeight();
    }
}
