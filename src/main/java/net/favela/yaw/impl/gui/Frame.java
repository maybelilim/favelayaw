package net.favela.yaw.impl.gui;

import lombok.Getter;
import lombok.Setter;
import net.favela.yaw.impl.gui.settings.Button;
import net.favela.yaw.impl.gui.settings.ModuleButton;
import net.favela.yaw.impl.management.Manager;
import net.favela.yaw.impl.modules.Module;
import net.favela.yaw.impl.modules.categories.client.GUI;
import net.favela.yaw.impl.util.animation.Anim;
import net.favela.yaw.impl.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.awt.Color;
import java.util.ArrayList;
import java.util.Comparator;

public class Frame {

    @Getter
    private final String name;
    @Setter
    @Getter
    private int x;
    @Setter
    @Getter
    private int y;
    @Getter
    private boolean open;
    @Setter
    @Getter
    private boolean visible = true;

    private int dragXOffset;
    private int dragYOffset;
    private boolean dragging;
    private String searchQuery = "";
    private String searchQueryLower = "";

    private final Minecraft mc = Minecraft.getInstance();
    private final ArrayList<ModuleButton> modules = new ArrayList<>();

    private final Anim openAnim = new Anim(0f);

    public Frame(Module.Category category, int x, int y) {
        this.name = category.getName();
        this.x = x;
        this.y = y;
        this.open = true;
        for (Module module : Manager.MODULE.getModulesByCategory(category)) {
            this.modules.add(new ModuleButton(module));
        }
        this.modules.sort(Comparator.comparing(Button::getName));
    }

    public void setSearchQuery(String query) {
        this.searchQuery = query == null ? "" : query;
        this.searchQueryLower = this.searchQuery.toLowerCase();
    }

    private boolean isSearching() {
        return !searchQuery.isEmpty();
    }

    private boolean matchesSearch(ModuleButton b) {
        return b.getName().toLowerCase().contains(searchQueryLower);
    }

    private float contentHeight() {
        float height = 0;
        for (ModuleButton b : modules) {
            if (b.isHidden()) continue;
            if (isSearching() && !matchesSearch(b)) continue;
            height += b.getHeight() + b.getItemHeight() + 1f + (b.isOpen() ? 1 : 0);
        }
        return height;
    }

    private boolean visible(ModuleButton b) {
        return !b.isHidden() && (!isSearching() || matchesSearch(b));
    }

    public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta, int alpha) {
        if (dragging) {
            x = dragXOffset + mouseX;
            y = dragYOffset + mouseY;
        }
        boolean searching = isSearching();
        Color theme = GUI.get().theme.get();
        float fade = alpha / 255f;

        float fullH = contentHeight();
        if (searching && fullH == 0) return;

        float factor = openAnim.to((open || searching) ? 1f : 0f, 25f);
        float animatedHeight = fullH * factor;

        RenderUtil.rect(context,
                getX() + (GUI.get().outline.get() ? -1 : 0), getY(),
                getX() + getWidth() + (GUI.get().outline.get() ? 1 : 0),
                getY() + getHeight() + 2 + (GUI.get().outline.get() ? 1 : 0) + animatedHeight,
                new Color(0, 0, 0, (int) (100 * fade)).getRGB());

        RenderUtil.rect(context, getX(), getY(), getX() + getWidth(), getY() + getHeight(),
                new Color(theme.getRed(), theme.getGreen(), theme.getBlue(),
                        (int) (theme.getAlpha() * fade)).getRGB());

        drawString(context, name, x + 2, y + GUI.get().getTextOffset(), new Color(255, 255, 255, alpha).getRGB());

        if (GUI.get().showCount.get()) {
            int gray = new Color(170, 170, 170, alpha).getRGB();
            int white = new Color(255, 255, 255, alpha).getRGB();
            float countX = getX() + getWidth() - mc.font.width("[0]") - 2f;
            float ty = y + GUI.get().getTextOffset();
            drawString(context, "[", countX, ty, gray);
            countX += mc.font.width("[");
            drawString(context, Integer.toString(modules.size()), countX, ty, white);
            countX += mc.font.width(Integer.toString(modules.size()));
            drawString(context, "]", countX, ty, gray);
        }

        if (GUI.get().outline.get()) {
            int outlineCol = new Color(theme.getRed(), theme.getGreen(), theme.getBlue(), (int) (theme.getAlpha() * fade)).getRGB();
            RenderUtil.rect(context, getX() - 1, getY(), getX(), getY() + getHeight() + 2 + animatedHeight, outlineCol);
            RenderUtil.rect(context, getX() + getWidth(), getY(), getX() + getWidth() + 1, getY() + getHeight() + 2 + animatedHeight, outlineCol);
            RenderUtil.rect(context, getX() - 1, getY() + getHeight() + 2 + animatedHeight, getX() + getWidth() + 1, getY() + getHeight() + 3 + animatedHeight, outlineCol);
        }

        int scissorTop = getY() + getHeight() + 2;
        int scissorBottom = (int) Math.ceil(getY() + getHeight() + 2 + animatedHeight);
        boolean scissor = scissorBottom > scissorTop;
        if (scissor) context.enableScissor(getX(), scissorTop, getX() + getWidth(), scissorBottom);

        if (factor > 0.001f) {
            float listTop = getY() + getHeight() + 2;
            float itemY = 0;
            for (ModuleButton b : modules) {
                if (!visible(b)) continue;
                b.setX(getX() + 1);
                b.setY(listTop + itemY);
                b.render(context, mouseX, mouseY, delta, alpha);
                itemY += b.getHeight() + b.getItemHeight() + 1f + (b.isOpen() ? 1 : 0);
            }
        }

        if (scissor) context.disableScissor();

        if (factor > 0.001f) {
            for (ModuleButton b : modules) {
                if (!visible(b)) continue;
                b.renderTooltip(context, mouseX, mouseY);
            }
        }
    }

    public void mouseClicked(int mx, int my, int btn) {
        if (isHovering(mx, my)) {
            if (btn == 0) {
                dragXOffset = x - mx;
                dragYOffset = y - my;
                dragging = true;
            }
            if (btn == 1) open = !open;
        }

        if (!open) return;
        for (ModuleButton b : modules) {
            if (!visible(b)) continue;
            b.mouseClicked(mx, my, btn);
        }
    }

    public void mouseReleased(int mx, int my, int btn) {
        if (btn == 0) dragging = false;
        for (ModuleButton b : modules) {
            if (!b.isHidden()) b.mouseReleased(mx, my, btn);
        }
    }

    public void onKeyPressed(int code) {
        if (open) {
            for (ModuleButton b : modules) {
                if (!b.isHidden()) b.onKeyPressed(code);
            }
        }
    }

    public void keyReleased(int code) {
        if (open) {
            for (ModuleButton b : modules) {
                if (!b.isHidden()) b.keyReleased(code);
            }
        }
    }

    public void charTyped(char c, int m) {
        if (open) {
            for (ModuleButton b : modules) {
                if (!b.isHidden()) b.onCharTyped(c, m);
            }
        }
    }

    private boolean isHovering(double mx, double my) {
        return mx >= x && mx <= x + getWidth() && my >= y && my <= y + getHeight();
    }

    private void drawString(GuiGraphicsExtractor ctx, String s, float x, float y, int color) {
        ctx.text(mc.font, s, (int) x, (int) y, color, true);
    }

    public int getWidth() {
        return 100 + GUI.get().width.getInt();
    }

    public int getHeight() {
        return 12 + GUI.get().height.getInt();
    }
}
