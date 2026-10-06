package net.favela.yaw.impl.gui.hud;

import net.favela.yaw.impl.gui.Frame;
import net.favela.yaw.impl.modules.Module;
import net.favela.yaw.impl.modules.categories.client.GUI;
import net.favela.yaw.impl.modules.categories.client.HUD;
import net.favela.yaw.impl.util.animation.Anim;
import net.favela.yaw.impl.util.render.RenderUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.input.CharacterEvent;
import net.minecraft.client.input.KeyEvent;
import net.minecraft.client.input.MouseButtonEvent;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

import java.awt.Color;
import java.util.ArrayList;

public class HudEditorScreen extends Screen {

    private static HudEditorScreen instance;

    private final ArrayList<Frame> frames = new ArrayList<>();
    private final Minecraft mc = Minecraft.getInstance();
    private final Anim screenAnim = new Anim(0f);

    public Hud currentDragging;
    public boolean anyHover;
    private float dragX;
    private float dragY;
    private boolean dragging;

    private HudEditorScreen() {
        super(Component.literal("favelayaw-hudeditor"));
        load();
    }

    public static HudEditorScreen getInstance() {
        if (instance == null) instance = new HudEditorScreen();
        return instance;
    }

    private void load() {
        int x = (mc.getWindow().getGuiScaledWidth() - (104 + GUI.get().width.getInt())) / 2;
        frames.add(new Frame(Module.Category.HUD, x, 50));
    }

    @Override
    public void onClose() {
        dragging = false;
        currentDragging = null;
        screenAnim.set(0f);
        super.onClose();
        HUD hud = HUD.getInstance();
        if (hud != null && hud.isEnabled()) hud.disable();
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        if (mc.level != null && GUI.get().blur.get()) {
            mc.options.menuBackgroundBlurriness().set(5);
            graphics.blurBeforeThisStratum();
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor context, int mouseX, int mouseY, float delta) {
        anyHover = false;
        float fade = screenAnim.to(1f, 20f);
        int alpha = (int) (255 * fade);

        Color grad = GUI.get().backgroundGradient.get();
        RenderUtil.verticalGradient(context, 0, 0, context.guiWidth(), context.guiHeight(),
                new Color(0, 0, 0, 0),
                new Color(grad.getRed(), grad.getGreen(), grad.getBlue(), (int) (grad.getAlpha() * fade)));

        float centerX = context.guiWidth() / 2f;
        float centerY = context.guiHeight() / 2f;
        RenderUtil.rect(context, centerX - 0.5f, 0, centerX + 0.5f, context.guiHeight(), new Color(255, 255, 255, (int) (90 * fade)).getRGB());
        RenderUtil.rect(context, 0, centerY - 0.5f, context.guiWidth(), centerY + 0.5f, new Color(255, 255, 255, (int) (90 * fade)).getRGB());

        if (dragging && currentDragging != null) {
            updateModulePosition(mouseX, mouseY);
        }

        for (Hud module : Hud.getHudModules()) {
            if (!module.isEnabled()) continue;
            module.render(context);
        }

        for (Frame frame : frames) {
            if (!frame.isVisible()) continue;
            frame.render(context, mouseX, mouseY, delta, alpha);
        }
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubled) {
        if (event.button() == 0) {
            int mouseX = (int) event.x();
            int mouseY = (int) event.y();
            for (Hud module : Hud.getHudModules()) {
                if (!module.isEnabled()) continue;
                if (!module.isHovering(mouseX, mouseY)) continue;
                currentDragging = module;
                dragX = mouseX - module.getX();
                dragY = mouseY - module.getY();
                dragging = true;
                return true;
            }
            currentDragging = null;
        }
        for (Frame frame : frames) {
            if (!frame.isVisible()) continue;
            frame.mouseClicked((int) event.x(), (int) event.y(), event.button());
        }
        return super.mouseClicked(event, doubled);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        dragging = false;
        for (Frame frame : frames) frame.mouseReleased((int) event.x(), (int) event.y(), event.button());
        return super.mouseReleased(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        for (Frame frame : frames) {
            frame.setY((int) (frame.getY() + vertical * 25.0));
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        for (Frame frame : frames) {
            if (!frame.isOpen()) continue;
            switch (keyCode) {
                case GLFW.GLFW_KEY_UP -> frame.setY(frame.getY() - 10);
                case GLFW.GLFW_KEY_DOWN -> frame.setY(frame.getY() + 10);
                case GLFW.GLFW_KEY_LEFT -> frame.setX(frame.getX() - 10);
                case GLFW.GLFW_KEY_RIGHT -> frame.setX(frame.getX() + 10);
            }
            frame.onKeyPressed(keyCode);
        }
        return super.keyPressed(event);
    }

    @Override
    public boolean keyReleased(KeyEvent event) {
        for (Frame frame : frames) frame.keyReleased(event.key());
        return super.keyReleased(event);
    }

    @Override
    public boolean charTyped(CharacterEvent event) {
        for (Frame frame : frames) frame.charTyped((char) event.codepoint(), 0);
        return super.charTyped(event);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void updateModulePosition(int mouseX, int mouseY) {
        Hud module = currentDragging;
        HUD hud = HUD.getInstance();
        int offset = hud != null ? hud.offset.getInt() : 2;
        float scaledWidth = mc.getWindow().getGuiScaledWidth();
        float scaledHeight = mc.getWindow().getGuiScaledHeight();
        float width = module.getWidth();
        float height = module.getHeight();

        float xRange = Math.max(1f, scaledWidth - width - 2 * offset);
        float yRange = Math.max(1f, scaledHeight - height - 2 * offset);
        float x = (mouseX - dragX - offset) / xRange;
        float y = (mouseY - dragY - offset) / yRange;

        x = Math.clamp(x, 0.0f, 1.0f);
        y = Math.clamp(y, 0.0f, 1.0f);

        float snapThreshold = 0.025f;
        x = applySnapping(x, snapThreshold);
        y = applySnapping(y, snapThreshold);

        module.setPosX(x);
        module.setPosY(y);
    }

    private static float applySnapping(float value, float snapThreshold) {
        if (value < snapThreshold) return 0.0f;
        if (value > 1.0f - snapThreshold) return 1.0f;
        if (Math.abs(value - 0.5f) < snapThreshold) return 0.5f;
        return value;
    }
}