package net.favela.yaw.impl.gui.settings;

import lombok.Getter;
import lombok.Setter;
import net.favela.yaw.impl.modules.categories.client.GUI;
import net.favela.yaw.impl.setting.Setting;
import net.favela.yaw.impl.util.animation.Anim;
import net.favela.yaw.impl.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.awt.Color;

public class Button {

    @Setter
    @Getter
    private String name;
    @Getter
    private final Setting<?> setting;
    @Getter
    private final ModuleButton button;
    @Setter
    @Getter
    private float x;
    @Setter
    @Getter
    private float y;
    @Setter
    @Getter
    private int width;
    @Setter
    @Getter
    private int height;
    @Setter
    @Getter
    private int total;
    @Setter
    @Getter
    private boolean open;

    protected final Anim hoverAnim = new Anim(0f);
    protected final Anim openAnim = new Anim(0f);

    protected final Minecraft mc = Minecraft.getInstance();

    public Button(Setting<?> setting, ModuleButton button) {
        this.setting = setting;
        this.button = button;
        this.name = setting.getName();
    }

    public Button(String name) {
        this.setting = null;
        this.button = null;
        this.name = name;
    }

    public void render(GuiGraphicsExtractor context, int mx, int my, float t, int alpha) {
        if (button != null && !button.isOpen() && open) open = false;
    }

    public void mouseClicked(int mx, int my, int btn) {
    }

    public void mouseReleased(int mx, int my, int btn) {
    }

    public void onKeyPressed(int code) {
    }

    public void keyReleased(int code) {
    }

    public void onCharTyped(char c, int m) {
    }

    public boolean isHovering(double mx, double my) {
        return mx >= getX() && mx <= getX() + getWidth() && my >= getY() && my <= getY() + getHeight();
    }

    protected boolean isHovering(double mx, double my, float x1, float y1, float x2, float y2) {
        return mx >= x1 && mx <= x2 && my >= y1 && my <= y2;
    }

    protected void renderHover(GuiGraphicsExtractor ctx, int mx, int my, int alpha) {
        float hover = hoverAnim.to(isHovering(mx, my) ? 1f : 0f, 25f);
        if (hover <= 0.001f) return;
        RenderUtil.rect(ctx, getX(), getY(), getX() + getWidth(), getY() + getHeight(),
                new Color(255, 255, 255, (int) (50 * hover * (alpha / 255f))).getRGB());
    }

    protected void drawString(GuiGraphicsExtractor ctx, String s, float x, float y, int color, int alpha) {
        Color c = new Color(color);
        int col = new Color(c.getRed(), c.getGreen(), c.getBlue(), alpha).getRGB();
        ctx.text(mc.font, s, (int) x, (int) (y + getTextOffset()), col, true);
    }

    protected int width(String s) {
        return mc.font.width(s);
    }

    protected int lineHeight() {
        return mc.font.lineHeight;
    }

    protected int getTextOffset() {
        return GUI.get().getTextOffset();
    }
}
