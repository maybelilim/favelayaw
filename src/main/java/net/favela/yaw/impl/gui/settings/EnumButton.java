package net.favela.yaw.impl.gui.settings;

import net.favela.yaw.impl.modules.categories.client.GUI;
import net.favela.yaw.impl.setting.EnumConverter;
import net.favela.yaw.impl.setting.settings.EnumSetting;
import net.favela.yaw.impl.util.render.RenderUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.awt.Color;

public class EnumButton extends Button {

    private final EnumSetting<?> setting;
    private final Enum<?>[] values;

    public EnumButton(EnumSetting<?> setting, ModuleButton btn) {
        super(setting, btn);
        this.setting = setting;
        this.values = setting.getValues();
    }

    private int dropHeight() {
        return values.length * (mc.font.lineHeight + 1) + 2;
    }

    @SuppressWarnings({"rawtypes", "unchecked"})
    private void select(Enum<?> value) {
        ((EnumSetting) setting).set(value);
    }

    @Override
    public void render(GuiGraphicsExtractor context, int mx, int my, float delta, int alpha) {
        Color theme = GUI.get().theme.get();
        float fade = alpha / 255f;
        int col = new Color(theme.getRed(), theme.getGreen(), theme.getBlue(), (int) (theme.getAlpha() * fade)).getRGB();
        RenderUtil.rect(context, getX(), getY(), getX() + getWidth(), getY() + getHeight(), col);
        renderHover(context, mx, my, alpha);
        drawString(context, setting.getName(), getX() + 2, getY(), new Color(255, 255, 255, alpha).getRGB(), alpha);
        String current = EnumConverter.getProperName(setting.get());
        drawString(context, "\u00a77" + current, getX() + getWidth() - 2 - width(current), getY(), new Color(128, 128, 128, alpha).getRGB(), alpha);
        float drop = openAnim.to(isOpen() ? 1f : 0f, 25f);

        if (drop > 0.001f) {
            int dropFull = dropHeight();
            int animated = (int) Math.ceil(dropFull * drop);
            int top = (int) (getY() + getHeight());
            int bottom = top + animated;
            context.enableScissor((int) getX(), top, (int) (getX() + getWidth()), bottom);
            int yOff = getHeight();
            for (Enum<?> v : values) {
                boolean rowHover = mx >= getX() && mx <= getX() + getWidth() && my >= getY() + yOff && my < getY() + yOff + mc.font.lineHeight;
                if (setting.get() == v) {
                    RenderUtil.rect(context, getX() + 2, getY() + yOff, getX() + getWidth() - 2, getY() + yOff + mc.font.lineHeight,
                            new Color(theme.getRed(), theme.getGreen(), theme.getBlue(), (int) (theme.getAlpha() * fade)).getRGB());
                }
                if (rowHover) {
                    RenderUtil.rect(context, getX() + 2, getY() + yOff, getX() + getWidth() - 2, getY() + yOff + mc.font.lineHeight,
                            new Color(255, 255, 255, (int) (30 * fade)).getRGB());
                }
                String text = (setting.get() == v ? "\u00a7a" : "\u00a77") + EnumConverter.getProperName(v);
                int tw = width(text);
                int cc = setting.get() == v ? 128 : 255;
                drawString(context, text, getX() + getWidth() / 2f - tw / 2f, getY() + yOff, new Color(cc, cc, cc, alpha).getRGB(), alpha);
                yOff += mc.font.lineHeight + 1;
            }

            RenderUtil.rect(context, getX(), getY() + getHeight(), getX() + 1, getY() + getHeight() + dropFull - 1, col);
            RenderUtil.rect(context, getX() + getWidth() - 1, getY() + getHeight(), getX() + getWidth(), getY() + getHeight() + dropFull - 1, col);
            RenderUtil.rect(context, getX(), getY() + getHeight() + dropFull - 1, getX() + getWidth(), getY() + getHeight() + dropFull, col);
            context.disableScissor();
        }
    }

    @Override
    public void mouseClicked(int mx, int my, int btn) {
        if (isHovering(mx, my)) {
            if (btn == 0) setting.setNext();
            if (btn == 1) setOpen(!isOpen());
            return;
        }
        if (isOpen() && isHoveringValues(mx, my)) {
            int yOff = getHeight();
            for (Enum<?> v : values) {
                if (my >= getY() + yOff && my < getY() + yOff + mc.font.lineHeight) {
                    select(v);
                    break;
                }
                yOff += mc.font.lineHeight + 1;
            }
        }
    }

    public boolean isHoveringValues(double mx, double my) {
        int h = (mc.font.lineHeight + 1) * values.length;
        return mx >= getX() && mx <= getX() + getWidth() && my >= getY() + getHeight() && my <= getY() + getHeight() + h;
    }

    @Override
    public int getTotal() {
        return getHeight() + (int) Math.ceil(dropHeight() * openAnim.get());
    }
}
