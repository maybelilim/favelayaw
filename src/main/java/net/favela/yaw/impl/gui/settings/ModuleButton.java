package net.favela.yaw.impl.gui.settings;

import lombok.Getter;
import lombok.Setter;
import net.favela.yaw.impl.modules.Module;
import net.favela.yaw.impl.modules.categories.client.GUI;
import net.favela.yaw.impl.setting.Setting;
import net.favela.yaw.impl.setting.settings.BindSetting;
import net.favela.yaw.impl.setting.settings.BooleanSetting;
import net.favela.yaw.impl.setting.settings.ColorSetting;
import net.favela.yaw.impl.setting.settings.EnumSetting;
import net.favela.yaw.impl.setting.settings.NumberSetting;
import net.favela.yaw.impl.setting.settings.SetSetting;
import net.favela.yaw.impl.setting.settings.StringSetting;
import net.favela.yaw.impl.util.animation.Anim;
import net.favela.yaw.impl.util.keyboard.Keybinding;
import net.favela.yaw.impl.util.render.RenderUtil;
import net.minecraft.client.gui.GuiGraphicsExtractor;

import java.awt.Color;
import java.util.ArrayList;

public class ModuleButton extends Button {

    private final ArrayList<Button> settings = new ArrayList<>();
    @Getter
    private final Module module;
    @Getter
    private float itemHeight;
    @Setter
    @Getter
    private boolean hidden;

    private final Anim enableAnim = new Anim(0f);
    private final Anim descAnim = new Anim(0f);

    public ModuleButton(Module module) {
        super(module.getName());
        this.module = module;
        for (Setting<?> set : module.getSettings()) {
            Button button = switch (set) {
                case BooleanSetting s -> new BooleanButton(s, this);
                case StringSetting s -> new StringButton(s, this);
                case NumberSetting s -> new SliderButton(s, this);
                case ColorSetting s -> new ColorButton(s, this);
                case BindSetting s -> new BindButton(s, this);
                case EnumSetting<?> s -> new EnumButton(s, this);
                case SetSetting<?> s -> new ArrayButton(s, this);
                default -> null;
            };
            if (button != null) settings.add(button);
        }
    }

    public ArrayList<Button> getModuleSettings() {
        return settings;
    }

    @Override
    public void render(GuiGraphicsExtractor context, int mouseX, int mouseY, float t, int alpha) {
        Color theme = GUI.get().theme.get();
        float fade = alpha / 255f;

        float hover = hoverAnim.to(isHovering(mouseX, mouseY) ? 1f : 0f, 25f);
        float enabled = enableAnim.to(module.isEnabled() ? 1f : 0f, 20f);

        if (GUI.get().darkBackground.get()) {
            RenderUtil.rect(context, getX(), getY(), getX() + getWidth(), getY() + getHeight(),
                    new Color(0, 0, 0, (int) (60 * fade)).getRGB());
        }
        if (enabled > 0.001f) {
            RenderUtil.rect(context, getX(), getY(), getX() + getWidth(), getY() + getHeight(),
                    new Color(theme.getRed(), theme.getGreen(), theme.getBlue(),
                            (int) (theme.getAlpha() * enabled * fade)).getRGB());
        }
        if (hover > 0.001f) {
            RenderUtil.rect(context, getX(), getY(), getX() + getWidth(), getY() + getHeight(),
                    new Color(255, 255, 255, (int) (50 * hover * fade)).getRGB());
        }

        int textCol;
        if (GUI.get().text.get() == GUI.Text.Custom) {
            Color c = module.isEnabled() ? GUI.get().enabledText.get() : GUI.get().disabledText.get();
            textCol = new Color(c.getRed(), c.getGreen(), c.getBlue(), (int) (c.getAlpha() * fade)).getRGB();
        } else {
            textCol = new Color(255, 255, 255, alpha).getRGB();
        }

        String prefix = (GUI.get().text.get() == GUI.Text.Separate && !module.isEnabled()) ? "\u00a77" : "";
        drawString(context, prefix + module.getName(), getX() + 2, getY(), textCol, alpha);

        if (GUI.get().gear.get()) {
            GUI.GearStyle style = GUI.get().gearStyle.get();
            String sym;
            if (style == GUI.GearStyle.Custom) {
                sym = isOpen() ? GUI.get().gearOpen.get() : GUI.get().gearClosed.get();
            } else {
                sym = isOpen() ? style.getOpen() : style.getClosed();
            }
            drawString(context, sym, getX() + getWidth() - 2 - width(sym), getY(), new Color(255, 255, 255, alpha).getRGB(), alpha);
        }

        if (GUI.get().binds.get() && module.bind.getKey() != -1) {
            String bind = "[" + Keybinding.format(module.bind.getKey()) + "]";
            drawString(context, bind, getX() + 2 + width(module.getName()), getY(), new Color(255, 255, 255, alpha).getRGB(), alpha);
        }

        float targetItem = 0f;
        if (isOpen()) {
            targetItem = 1f;
            for (Button btn : settings) {
                if (!btn.getSetting().isVisible()) continue;
                targetItem += btn.getTotal() + 1;
            }
        }
        itemHeight = openAnim.to(targetItem, 25f);

        if (itemHeight > 0.5f) {
            int clipBottom = (int) Math.ceil(getY() + getHeight() + itemHeight) + 2;
            context.enableScissor((int) (getX() - 1), (int) (getY() + getHeight()), (int) (getX() + getWidth() + 1), clipBottom);
            float yo = 1;
            for (Button btn : settings) {
                if (!btn.getSetting().isVisible()) continue;
                btn.setX(getX() + 2);
                btn.setY(getY() + getHeight() + yo);
                btn.setWidth(getWidth() - 4);
                btn.setHeight(getHeight() - 1);
                btn.render(context, mouseX, mouseY, t, alpha);
                yo += btn.getTotal() + 1;
            }
            if (enabled > 0.001f) {
                int borderCol = new Color(theme.getRed(), theme.getGreen(), theme.getBlue(),
                        (int) (theme.getAlpha() * enabled * fade)).getRGB();
                RenderUtil.rect(context, getX(), getY() + getHeight(), getX() + 1, getY() + getHeight() + itemHeight, borderCol);
                RenderUtil.rect(context, getX() + getWidth() - 1, getY() + getHeight(), getX() + getWidth(), getY() + getHeight() + itemHeight, borderCol);
                RenderUtil.rect(context, getX(), getY() + getHeight() + itemHeight, getX() + getWidth(), getY() + getHeight() + itemHeight + 1, borderCol);
            }
            context.disableScissor();
        }
    }

    public void renderTooltip(GuiGraphicsExtractor context, int mouseX, int mouseY) {
        GUI.DescriptionMode mode = GUI.get().showDescription.get();
        boolean show = !hidden
                && !module.getDescription().isEmpty()
                && mode != GUI.DescriptionMode.Off
                && isHovering(mouseX, mouseY);
        float d = descAnim.to(show ? 1f : 0f, 20f);
        if (d <= 0.001f) return;
        int a = (int) (255 * d);
        if (mode == GUI.DescriptionMode.Center) renderDescriptionCenter(context, a);
    }

    private void renderDescriptionCenter(GuiGraphicsExtractor ctx, int a) {
        int screenW = mc.getWindow().getGuiScaledWidth();
        int textW = width(module.getDescription());
        int x = (screenW - textW) / 2;
        drawString(ctx, module.getDescription(), x, 4, new Color(255, 255, 255, a).getRGB(), a);
    }

    @Override
    public void mouseClicked(int mx, int my, int btn) {
        if (isHovering(mx, my)) {
            if (btn == 0) module.toggle();
            else if (btn == 1) setOpen(!isOpen());
        }
        if (!isOpen()) return;
        for (Button b : settings) {
            if (b.getSetting().isVisible()) b.mouseClicked(mx, my, btn);
        }
    }

    @Override
    public void mouseReleased(int mx, int my, int btn) {
        if (isOpen()) {
            for (Button b : settings) {
                if (b.getSetting().isVisible()) b.mouseReleased(mx, my, btn);
            }
        }
    }

    @Override
    public void onKeyPressed(int code) {
        if (isOpen()) {
            for (Button b : settings) {
                if (b.getSetting().isVisible()) b.onKeyPressed(code);
            }
        }
    }

    @Override
    public void keyReleased(int code) {
        if (isOpen()) {
            for (Button b : settings) {
                if (b.getSetting().isVisible()) b.keyReleased(code);
            }
        }
    }

    @Override
    public void onCharTyped(char c, int m) {
        if (isOpen()) {
            for (Button b : settings) {
                if (b.getSetting().isVisible()) b.onCharTyped(c, m);
            }
        }
    }

    @Override
    public int getWidth() {
        return 98 + GUI.get().width.getInt();
    }

    @Override
    public int getHeight() {
        return 12 + GUI.get().height.getInt();
    }
}
