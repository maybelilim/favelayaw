package net.favela.yaw.impl.gui.settings;

import net.favela.yaw.impl.gui.GUIScreen;
import net.favela.yaw.impl.modules.categories.client.GUI;
import net.favela.yaw.impl.setting.settings.ColorSetting;
import net.favela.yaw.impl.util.render.RenderUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.awt.Color;

public class ColorButton extends Button {

    private static final int PAD = 3;
    private static final int HUE_WIDTH = 6;
    private static final int ALPHA_HEIGHT = 6;
    private static final int OUTLINE = 0xFF000000;

    private final ColorSetting setting;
    private float[] selectedColor;
    private int lastMouseX;
    private int lastMouseY;
    private boolean dragHue;
    private boolean dragAlpha;
    private boolean dragColor;
    private boolean copyClick;
    private boolean pasteClick;
    private boolean rainbowClick;

    public ColorButton(ColorSetting setting, ModuleButton btn) {
        super(setting, btn);
        this.setting = setting;
        Color v = setting.getRaw();
        selectedColor = Color.RGBtoHSB(v.getRed(), v.getGreen(), v.getBlue(), null);
    }

    private static int scaleAlpha(int argb, float factor) {
        int a = (int) ((argb >>> 24) * factor);
        return (a << 24) | (argb & 0xFFFFFF);
    }

    private int boxSize() {
        return getWidth() - PAD * 3 - HUE_WIDTH;
    }

    private float boxX() {
        return getX() + PAD;
    }

    private float boxTop() {
        return getY() + getHeight() + PAD;
    }

    private float hueX() {
        return boxX() + boxSize() + PAD;
    }

    private float alphaTop() {
        return boxTop() + boxSize() + PAD;
    }

    private float buttonsTop() {
        float y = boxTop() + boxSize() + PAD;
        if (setting.isAllowAlpha()) y += ALPHA_HEIGHT + PAD;
        return y;
    }

    private float alphaLeft() {
        return getX() + PAD;
    }

    private float alphaRight() {
        return getX() + getWidth() - PAD;
    }

    private int expandedTotal() {
        int total = getHeight() + PAD;
        total += boxSize() + PAD;
        if (setting.isAllowAlpha()) total += ALPHA_HEIGHT + PAD;
        total += getHeight() + PAD;
        total += getHeight() + PAD;
        return total;
    }

    private static float clamp(float value) {
        return Math.clamp(value, 0f, 1f);
    }

    private void applyDragging() {
        if (!dragColor && !dragHue && !dragAlpha) return;

        int size = boxSize();
        float bx = boxX();
        float by = boxTop();

        if (dragHue) selectedColor[0] = clamp((lastMouseY - by) / size);
        if (dragColor) {
            selectedColor[1] = clamp((lastMouseX - bx) / size);
            selectedColor[2] = 1f - clamp((lastMouseY - by) / size);
        }
        int a = setting.getRaw().getAlpha();
        if (dragAlpha && setting.isAllowAlpha()) {
            a = (int) (255 * clamp((lastMouseX - alphaLeft()) / (alphaRight() - alphaLeft())));
        }
        Color c = new Color(Color.HSBtoRGB(selectedColor[0], selectedColor[1], selectedColor[2]));
        setting.set(new Color(c.getRed(), c.getGreen(), c.getBlue(), a));
    }

    private void drawColorArea(GuiGraphicsExtractor context, float fade) {
        int size = boxSize();
        float bx = boxX();
        float by = boxTop();
        float hx = hueX();
        int outline = scaleAlpha(OUTLINE, fade);

        RenderUtil.horizontalGradient(context, bx, by, bx + size, by + size,
                new Color(scaleAlpha(0xFFFFFFFF, fade), true),
                new Color(scaleAlpha(Color.getHSBColor(selectedColor[0], 1, 1).getRGB(), fade), true));
        RenderUtil.verticalGradient(context, bx, by, bx + size, by + size,
                new Color(0, true), Color.BLACK);
        RenderUtil.drawRectOutline(context, (int) bx, (int) by, (int) (bx + size), (int) (by + size), outline);
        float svx = bx + size * selectedColor[1];
        float svy = by + size * (1f - selectedColor[2]);
        RenderUtil.drawRectOutline(context, (int) (svx - 2), (int) (svy - 2), (int) (svx + 2), (int) (svy + 2), scaleAlpha(0xFFFFFFFF, fade));
        RenderUtil.drawRectOutline(context, (int) (svx - 3), (int) (svy - 3), (int) (svx + 3), (int) (svy + 3), outline);

        for (float i = 0; i < size; i += 0.5f) {
            RenderUtil.rect(context, hx, by + i, hx + HUE_WIDTH, by + i + 0.5f,
                    scaleAlpha(Color.getHSBColor(i / size, 1, 1).getRGB(), fade));
        }
        RenderUtil.drawRectOutline(context, (int) hx, (int) by, (int) (hx + HUE_WIDTH), (int) (by + size), outline);
        float huey = by + size * selectedColor[0];
        RenderUtil.rect(context, hx - 1, huey - 1, hx + HUE_WIDTH + 1, huey + 1, scaleAlpha(0xFFFFFFFF, fade));
        RenderUtil.drawRectOutline(context, (int) (hx - 1), (int) (huey - 1), (int) (hx + HUE_WIDTH + 1), (int) (huey + 1), outline);
    }

    private void drawAlphaArea(GuiGraphicsExtractor context, float fade) {
        if (!setting.isAllowAlpha()) return;
        Color v = setting.getRaw();
        float ax1 = alphaLeft();
        float ax2 = alphaRight();
        float ay = alphaTop();
        int outline = scaleAlpha(OUTLINE, fade);
        RenderUtil.horizontalGradient(context, ax1, ay, ax2, ay + ALPHA_HEIGHT,
                new Color(scaleAlpha(v.getRGB() & 0x00FFFFFF, fade), true),
                new Color(scaleAlpha(v.getRGB() | 0xFF000000, fade), true));
        RenderUtil.drawRectOutline(context, (int) ax1, (int) ay, (int) ax2, (int) (ay + ALPHA_HEIGHT), outline);
        float apos = ax1 + (ax2 - ax1) * (v.getAlpha() / 255f);
        RenderUtil.rect(context, apos - 1, ay - 1, apos + 1, ay + ALPHA_HEIGHT + 1, scaleAlpha(0xFFFFFFFF, fade));
        RenderUtil.drawRectOutline(context, (int) (apos - 1), (int) (ay - 1), (int) (apos + 1), (int) (ay + ALPHA_HEIGHT + 1), outline);
    }

    private void drawActionButtons(GuiGraphicsExtractor context, int mx, int my, int alpha, float fade) {
        float by = buttonsTop();
        float halfW = (getWidth() - PAD) / 2f;
        float copyX1 = getX();
        float copyX2 = getX() + halfW;
        float pasteX1 = copyX2 + PAD;
        float pasteX2 = getX() + getWidth();

        copyClick = mx >= copyX1 && mx <= copyX2 && my >= by && my <= by + getHeight();
        pasteClick = mx >= pasteX1 && mx <= pasteX2 && my >= by && my <= by + getHeight();
        drawButton(context, "Copy", copyX1, by, copyX2, copyClick, alpha, fade);
        drawButton(context, "Paste", pasteX1, by, pasteX2, pasteClick, alpha, fade);

        float rainbowY = by + getHeight() + PAD;
        rainbowClick = mx >= getX() && mx <= getX() + getWidth() && my >= rainbowY && my <= rainbowY + getHeight();
        drawButton(context, setting.isRainbow() ? "Rainbow: On" : "Rainbow: Off", getX(), rainbowY, getX() + getWidth(), rainbowClick, alpha, fade);
    }

    private void drawButton(GuiGraphicsExtractor context, String label, float x1, float y1, float x2, boolean hovered, int alpha, float fade) {
        float y2 = y1 + getHeight();
        Color theme = GUI.get().theme.get();
        RenderUtil.rect(context, x1, y1, x2, y2,
                new Color(theme.getRed(), theme.getGreen(), theme.getBlue(), (int) (theme.getAlpha() * fade)).getRGB());
        if (hovered) RenderUtil.rect(context, x1, y1, x2, y2, new Color(255, 255, 255, (int) (45 * fade)).getRGB());
        RenderUtil.drawRectOutline(context, (int) x1, (int) y1, (int) x2, (int) y2, scaleAlpha(OUTLINE, fade));
        float textX = x1 + (x2 - x1 - width(label)) / 2f;
        drawString(context, label, textX, y1 - 1, new Color(255, 255, 255, alpha).getRGB(), alpha);
    }

    @Override
    public void render(GuiGraphicsExtractor context, int mx, int my, float delta, int alpha) {
        lastMouseX = mx;
        lastMouseY = my;
        float fade = alpha / 255f;

        renderHover(context, mx, my, alpha);
        drawString(context, setting.getName(), getX() + PAD, getY(), new Color(255, 255, 255, alpha).getRGB(), alpha);

        float swX2 = getX() + getWidth() - PAD;
        float swX1 = swX2 - 12;
        float swY1 = getY() + 2;
        float swY2 = getY() + getHeight() - 2;
        RenderUtil.rect(context, swX1, swY1, swX2, swY2, scaleAlpha(setting.getRaw().getRGB(), fade));
        RenderUtil.drawRectOutline(context, (int) swX1, (int) swY1, (int) swX2, (int) swY2, scaleAlpha(OUTLINE, fade));

        float ex = openAnim.to(isOpen() ? 1f : 0f, 25f);
        if (ex <= 0.001f) return;

        int extra = expandedTotal() - getHeight();
        int animatedBottom = (int) Math.ceil(getY() + getHeight() + extra * ex);
        context.enableScissor((int) (getX() - 1), (int) (getY() + getHeight()), (int) (getX() + getWidth() + 1), animatedBottom);

        applyDragging();
        drawColorArea(context, fade);
        drawAlphaArea(context, fade);
        drawActionButtons(context, mx, my, alpha, fade);

        context.disableScissor();
    }

    @Override
    public void mouseClicked(int mx, int my, int btn) {
        if (isHovering(mx, my) && btn == 1) setOpen(!isOpen());
        if (btn != 0 || !isOpen()) return;

        int size = boxSize();
        float bx = boxX();
        float by = boxTop();
        float hx = hueX();

        if (isHovering(mx, my, (int) bx, (int) by, (int) (bx + size), (int) (by + size))) dragColor = true;
        if (isHovering(mx, my, (int) hx, (int) by, (int) (hx + HUE_WIDTH), (int) (by + size))) dragHue = true;
        if (setting.isAllowAlpha()) {
            float ay = alphaTop();
            if (isHovering(mx, my, (int) alphaLeft(), (int) ay, (int) alphaRight(), (int) (ay + ALPHA_HEIGHT))) dragAlpha = true;
        }

        if (copyClick) GUIScreen.setColorClipboard(setting.getRaw());
        if (pasteClick && GUIScreen.getColorClipboard() != null) {
            Color c = GUIScreen.getColorClipboard();
            setting.set(new Color(c.getRed(), c.getGreen(), c.getBlue(), setting.getRaw().getAlpha()));
            selectedColor = Color.RGBtoHSB(setting.getRaw().getRed(), setting.getRaw().getGreen(), setting.getRaw().getBlue(), null);
        }
        if (rainbowClick) setting.setRainbow(!setting.isRainbow());
    }

    @Override
    public void mouseReleased(int mx, int my, int btn) {
        dragHue = dragColor = dragAlpha = false;
    }

    @Override
    public int getTotal() {
        int extra = expandedTotal() - getHeight();
        return getHeight() + (int) Math.ceil(extra * openAnim.get());
    }
}
