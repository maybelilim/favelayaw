package net.favela.yaw.impl.gui.settings;

import net.favela.yaw.impl.modules.categories.client.GUI;
import net.favela.yaw.impl.setting.settings.BooleanSetting;
import net.favela.yaw.impl.util.animation.Anim;
import net.favela.yaw.impl.util.render.RenderUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.awt.Color;

public class BooleanButton extends Button {

    private final BooleanSetting setting;
    private final Anim onAnim = new Anim(0f);

    public BooleanButton(BooleanSetting setting, ModuleButton btn) {
        super(setting, btn);
        this.setting = setting;
    }

    @Override
    public void render(GuiGraphicsExtractor ctx, int mx, int my, float t, int alpha) {
        float on = onAnim.to(setting.get() ? 1f : 0f, 25f);
        if (on > 0.001f) {
            Color theme = GUI.get().theme.get();
            RenderUtil.rect(ctx, getX(), getY(), getX() + getWidth(), getY() + getHeight(),
                    new Color(theme.getRed(), theme.getGreen(), theme.getBlue(),
                            (int) (theme.getAlpha() * on * (alpha / 255f))).getRGB());
        }
        renderHover(ctx, mx, my, alpha);
        if (GUI.get().text.get() == GUI.Text.Separate) {
            int c = setting.get() ? 255 : 128;
            drawString(ctx, (setting.get() ? "" : "\u00a77") + setting.getName(), getX() + 2, getY(),
                    new Color(c, c, c, alpha).getRGB(), alpha);
        } else {
            drawString(ctx, setting.getName(), getX() + 2, getY(), new Color(255, 255, 255, alpha).getRGB(), alpha);
        }
    }

    @Override
    public void mouseClicked(int mx, int my, int btn) {
        if (isHovering(mx, my) && btn == 0) {
            setting.setValue(!setting.get());
        }
    }

    @Override
    public int getTotal() {
        return getHeight();
    }
}
