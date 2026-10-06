package net.favela.yaw.impl.gui.hud;

import lombok.Getter;
import lombok.Setter;
import net.favela.yaw.impl.modules.Module;
import net.favela.yaw.impl.modules.categories.client.GUI;
import net.favela.yaw.impl.modules.categories.client.HUD;
import net.favela.yaw.impl.setting.settings.NumberSetting;
import net.favela.yaw.impl.util.render.RenderUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.ChatScreen;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static net.favela.yaw.impl.util.wrapper.Wrapper.MC;

public abstract class Hud extends Module {

    private static final List<Hud> HUD_MODULES = new ArrayList<>();

    private final NumberSetting posX = num("PosX", () -> false, 0f, 1f, 0.5f);
    private final NumberSetting posY = num("PosY", () -> false, 0f, 1f, 0.5f);

    @Setter
    @Getter
    private float width;
    @Setter
    @Getter
    private float height;

    public Hud(String name, String description, float width, float height) {
        super(name, description, Category.HUD);
        this.width = width;
        this.height = height;
        HUD_MODULES.add(this);
    }

    public static List<Hud> getHudModules() {
        return Collections.unmodifiableList(HUD_MODULES);
    }

    public static void renderAll(GuiGraphicsExtractor context) {
        for (Hud module : HUD_MODULES) {
            if (module.isEnabled()) module.render(context);
        }
    }

    public void render(GuiGraphicsExtractor context) {
    }

    public float getPosX() {
        return posX.getFloat();
    }

    public float getPosY() {
        return posY.getFloat();
    }

    public void setPosX(float v) {
        posX.set(v);
    }

    public void setPosY(float v) {
        posY.set(v);
    }

    public int getOffset() {
        HUD editor = HUD.getInstance();
        return editor != null ? editor.offset.getInt() : 2;
    }

    public float getX() {
        int offset = getOffset();
        return offset + getPosX() * (MC.getWindow().getGuiScaledWidth() - width - 2 * offset);
    }

    public float getY() {
        int offset = getOffset();
        float baseY = offset + getPosY() * (MC.getWindow().getGuiScaledHeight() - height - 2 * offset);
        if (MC.gui.screen() instanceof ChatScreen) {
            float heightWithChat = MC.getWindow().getGuiScaledHeight() - 14;
            baseY = Math.min(baseY + getHeight(), heightWithChat) - getHeight();
        }
        return baseY;
    }

    public void renderEditor(GuiGraphicsExtractor context) {
        if (MC.player == null || MC.level == null) return;
        float x = getX();
        float y = getY();
        HudEditorScreen editor = HudEditorScreen.getInstance();
        boolean shouldDrawDescription = isHovering() && !editor.anyHover;
        if (editor.currentDragging != null) {
            shouldDrawDescription = editor.currentDragging == this;
        }
        if (shouldDrawDescription) {
            int textWidth = MC.font.width(getName());
            int textHeight = MC.font.lineHeight;
            float textX = x + width + 5;
            if (textX + textWidth > MC.getWindow().getGuiScaledWidth()) {
                textX = x - 5 - textWidth;
            }
            context.text(MC.font, getName(), (int) textX, (int) (y + height / 2f - textHeight / 2f), -1, true);
            editor.anyHover = true;
        }
        if (editor.currentDragging == this) {
            RenderUtil.rect(context, x - 2, y - 2, x + width + 1, y + height + 1, new Color(255, 255, 255, 80).getRGB());
        }
        Color theme = GUI.get().theme.get();
        int outlineCol = new Color(theme.getRed(), theme.getGreen(), theme.getBlue(), (int) (theme.getAlpha() * 0.8f)).getRGB();
        RenderUtil.drawRectOutline(context, (int) (x - 1), (int) (y - 1), (int) (x + width + 1), (int) (y + height + 1), outlineCol);
    }

    public void setBounds(float x, float y, float width, float height) {
        this.width = width;
        this.height = height;
        setPosX(x / MC.getWindow().getGuiScaledWidth());
        setPosY(y / MC.getWindow().getGuiScaledHeight());
    }

    public int getMouseX() {
        return (int) (MC.mouseHandler.xpos() / MC.getWindow().getGuiScale());
    }

    public int getMouseY() {
        return (int) (MC.mouseHandler.ypos() / MC.getWindow().getGuiScale());
    }

    public boolean isHovering() {
        return isHovering(getMouseX(), getMouseY());
    }

    public boolean isHovering(int mouseX, int mouseY) {
        float x = getX();
        float y = getY();
        return mouseX >= x - 1 && mouseX <= x + width + 1 && mouseY >= y - 1 && mouseY <= y + height + 1;
    }
}