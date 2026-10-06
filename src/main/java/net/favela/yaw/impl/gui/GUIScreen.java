package net.favela.yaw.impl.gui;

import lombok.Getter;
import lombok.Setter;
import net.favela.yaw.impl.modules.Module;
import net.favela.yaw.impl.modules.categories.client.GUI;
import net.favela.yaw.impl.util.animation.Anim;
import net.favela.yaw.impl.util.models.Timer;
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

public class GUIScreen extends Screen {

    private static GUIScreen instance;

    private final ArrayList<Frame> frames = new ArrayList<>();
    private final Timer timer = new Timer();
    private final Minecraft mc = Minecraft.getInstance();
    private final Anim screenAnim = new Anim(0f);

    @Setter
    @Getter
    private static Color colorClipboard = null;

    private boolean blink = false;
    private boolean closing = false;
    private boolean searchActive = false;
    private String searchQuery = "";

    private GUIScreen() {
        super(Component.literal("GUIModule"));
        load();
    }

    public static GUIScreen getInstance() {
        if (instance == null) instance = new GUIScreen();
        return instance;
    }

    public String getSym() {
        return blink ? "_" : "";
    }

    public void open() {
        closing = false;
        searchActive = false;
        searchQuery = "";
        mc.gui.setScreen(this);
    }

    public void startClosing() {
        if (closing) return;
        closing = true;
        searchActive = false;
        searchQuery = "";
        applySearch();
    }

    @Override
    public void onClose() {
        startClosing();
    }

    private void finishClose() {
        closing = false;
        screenAnim.set(0f);
        super.onClose();
        if (GUI.get().isEnabled()) GUI.get().toggle();
    }

    private void load() {
        int totalWidth = (104 + GUI.get().width.getInt()) * 6 + 3 * 5;
        int x = Math.max(5, (mc.getWindow().getGuiScaledWidth() - totalWidth) / 2 - 60);
        for (Module.Category category : Module.Category.values()) {
            if (category == Module.Category.HUD) continue;
            frames.add(new Frame(category, x, 50));
            x += 104 + GUI.get().width.getInt() + 3;
        }
    }

    @Override
    public void extractBackground(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float a) {
        if (mc.level != null && GUI.get().blur.get()) {
            mc.options.menuBackgroundBlurriness().set(5);
            graphics.blurBeforeThisStratum();
        }
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, int mouseX, int mouseY, float delta) {
        float fade = screenAnim.to(closing ? 0f : 1f, 20f);
        int alpha = (int) (255 * fade);
        if (GUI.get().darken.get()) {
            int darkAlpha = (int) (GUI.get().darkenStrength.getInt() * fade);
            RenderUtil.rect(graphics, 0, 0, graphics.guiWidth(), graphics.guiHeight(), new Color(0, 0, 0, darkAlpha).getRGB());
        }
        Color grad = GUI.get().backgroundGradient.get();
        RenderUtil.verticalGradient(graphics, 0, 0, graphics.guiWidth(), graphics.guiHeight(),
                new Color(0, 0, 0, 0),
                new Color(grad.getRed(), grad.getGreen(), grad.getBlue(), (int) (grad.getAlpha() * fade)));
        if (timer.passedMs(500)) {
            blink = !blink;
            timer.reset();
        }
        for (Frame frame : frames) {
            if (!frame.isVisible()) continue;
            frame.render(graphics, mouseX, mouseY, delta, alpha);
        }
        renderSearchHint(graphics, alpha);
        if (searchActive) renderSearchBar(graphics, alpha);
        if (closing && fade <= 0.01f) finishClose();
    }

    private void renderSearchHint(GuiGraphicsExtractor context, int alpha) {
        if (searchActive) return;
        int color = new Color(100, 100, 100, alpha).getRGB();
        int textY = context.guiHeight() - mc.font.lineHeight - 2;
        context.text(mc.font, "Press Ctrl + F to search modules", 2, textY, color, true);
    }

    private void renderSearchBar(GuiGraphicsExtractor context, int alpha) {
        int barW = 180;
        int barH = 12 + GUI.get().height.getInt();
        int barX = (context.guiWidth() - barW) / 2;
        int barY = 16;
        Color theme = GUI.get().theme.get();
        float fade = alpha / 255f;
        RenderUtil.rect(context, barX - 1, barY - 1, barX + barW + 1, barY + barH + 1,
                new Color(theme.getRed(), theme.getGreen(), theme.getBlue(), (int) (theme.getAlpha() * fade)).getRGB());
        RenderUtil.rect(context, barX, barY, barX + barW, barY + barH, new Color(0, 0, 0, (int) (200 * fade)).getRGB());
        String display = searchQuery.isEmpty() ? "search..." : searchQuery;
        display += blink ? "_" : "";
        int textColor = searchQuery.isEmpty()
                ? new Color(120, 120, 120, alpha).getRGB()
                : new Color(255, 255, 255, alpha).getRGB();
        context.text(mc.font, display, barX + 3, barY + GUI.get().getTextOffset(), textColor, true);
    }

    private void applySearch() {
        for (Frame frame : frames) frame.setSearchQuery(searchQuery);
    }

    @Override
    public boolean mouseClicked(MouseButtonEvent event, boolean doubleClick) {
        if (closing) return true;
        int mouseX = (int) event.x();
        int mouseY = (int) event.y();
        int button = event.button();
        for (Frame frame : frames) {
            if (!frame.isVisible()) continue;
            frame.mouseClicked(mouseX, mouseY, button);
        }
        return super.mouseClicked(event, doubleClick);
    }

    @Override
    public boolean mouseReleased(MouseButtonEvent event) {
        for (Frame frame : frames) frame.mouseReleased((int) event.x(), (int) event.y(), event.button());
        return super.mouseReleased(event);
    }

    @Override
    public boolean keyPressed(KeyEvent event) {
        int keyCode = event.key();
        if (keyCode == GLFW.GLFW_KEY_F && isControlDown()) {
            searchActive = !searchActive;
            if (!searchActive) {
                searchQuery = "";
                applySearch();
            }
            return true;
        }
        if (searchActive) {
            if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
                searchActive = false;
                searchQuery = "";
                applySearch();
                return true;
            }
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE && !searchQuery.isEmpty()) {
                searchQuery = searchQuery.substring(0, searchQuery.length() - 1);
                applySearch();
                return true;
            }
            return true;
        }
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
        if (searchActive) {
            char c = (char) event.codepoint();
            if (!Character.isISOControl(c)) {
                searchQuery += c;
                applySearch();
            }
            return true;
        }
        for (Frame frame : frames) frame.charTyped((char) event.codepoint(), 0);
        return super.charTyped(event);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontal, double vertical) {
        for (Frame frame : frames) {
            frame.setY((int) (frame.getY() + vertical * 25.0));
        }
        return super.mouseScrolled(mouseX, mouseY, horizontal, vertical);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private boolean isControlDown() {
        long handle = mc.getWindow().handle();
        return GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_LEFT_CONTROL) == GLFW.GLFW_PRESS
                || GLFW.glfwGetKey(handle, GLFW.GLFW_KEY_RIGHT_CONTROL) == GLFW.GLFW_PRESS;
    }
}
