package net.favela.yaw.impl.modules;

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.function.BooleanSupplier;

import lombok.Getter;
import lombok.Setter;
import net.favela.yaw.impl.event.Events;
import net.favela.yaw.impl.event.events.*;
import net.favela.yaw.impl.modules.categories.client.Notification;
import net.favela.yaw.impl.setting.Setting;
import net.favela.yaw.impl.setting.settings.*;
import net.favela.yaw.impl.util.chat.ChatUtil;

public abstract class Module {

    @Getter
    private final String name;
    @Getter
    private final String description;
    @Getter
    private final Category category;
    @Getter
    private volatile boolean enabled;
    @Getter
    private final List<Setting<?>> settings = new ArrayList<>();
    @Setter
    @Getter
    private boolean hidden = false;

    public BindSetting bind = register(new BindSetting("Bind", "module's bind", -1));
    public BooleanSetting drawn = register(new BooleanSetting("Drawn", "module's drawn state", true));
    public BooleanSetting debug = register(new BooleanSetting("Debug", "", () -> false, false));

    private List<Events.Handler<?>> handlers;

    public Module(String name, String description, Category category) {
        this.name = name;
        this.description = description;
        this.category = category;
    }

    public void onToggle() {
    }

    public void onEnable() {
    }

    public void onDisable() {
    }

    public void onTick() {
    }

    public void onUpdate(UpdateEvent event) {
    }

    public void onRenderBlockOutline(RenderBlockOutlineEvent event) {
    }

    public void onRender2D(Render2DEvent event) {
    }

    public void onRender3D(Render3DEvent event) {
    }

    public void toggle() {
        if (this.enabled) {
            disable();
        } else {
            enable();
        }
    }

    public void enable() {
        if (this.enabled) return;
        this.enabled = true;

        if (handlers == null) handlers = createHandlers();
        handlers.forEach(Events::register);

        notifyState(true);
        this.onEnable();
        this.onToggle();
    }

    public void disable() {
        if (!this.enabled) return;
        this.enabled = false;

        if (handlers != null) handlers.forEach(Events::off);

        notifyState(false);
        this.onDisable();
        this.onToggle();
    }

    public void setEnabled(boolean state) {
        if (state) {
            enable();
        } else {
            disable();
        }
    }

    public void setBind(int bind) {
        this.bind.setKey(bind);
    }

    private List<Events.Handler<?>> createHandlers() {
        return List.of(
                Events.handler(TickEvent.class, e -> onTick()),
                Events.handler(UpdateEvent.class, this::onUpdate),
                Events.handler(RenderBlockOutlineEvent.class, this::onRenderBlockOutline),
                Events.handler(Render2DEvent.class, this::onRender2D),
                Events.handler(Render3DEvent.class, this::onRender3D)
        );
    }

    private void notifyState(boolean state) {
        Notification notif = Notification.getInstance();
        if (notif != null) {
            notif.notify(name, state);
        } else {
            String color = state ? "§a" : "§4";
            String label = state ? "enabled" : "disabled";
            ChatUtil.sendMessage(name + " was " + color + label);
        }
    }

    protected <S extends Setting<?>> S register(S setting) {
        setting.setModule(this);
        this.settings.add(setting);
        return setting;
    }

    protected BooleanSetting bool(String name, String description, boolean defaultValue) {
        return register(new BooleanSetting(name, description, defaultValue));
    }

    protected BooleanSetting bool(String name, String description, BooleanSupplier visibility, boolean defaultValue) {
        return register(new BooleanSetting(name, description, visibility, defaultValue));
    }

    protected BooleanSetting bool(String name, boolean defaultValue) {
        return register(new BooleanSetting(name, "", defaultValue));
    }

    protected BooleanSetting bool(String name, BooleanSupplier visibility, boolean defaultValue) {
        return register(new BooleanSetting(name, "", visibility, defaultValue));
    }

    protected NumberSetting num(String name, String description, Number min, Number max, Number defaultValue) {
        return register(new NumberSetting(name, description, min, max, defaultValue));
    }

    protected NumberSetting num(String name, String description, Number min, Number max, Number defaultValue, BooleanSupplier visibility) {
        return register(new NumberSetting(name, description, visibility, min, max, defaultValue));
    }

    protected NumberSetting num(String name, String description, Number min, Number max, Number defaultValue, Number step) {
        return register(new NumberSetting(name, description, min, max, defaultValue, step));
    }

    protected NumberSetting num(String name, String description, BooleanSupplier visibility, Number min, Number max, Number defaultValue) {
        return register(new NumberSetting(name, description, visibility, min, max, defaultValue));
    }

    protected NumberSetting num(String name, String description, BooleanSupplier visibility, Number min, Number max, Number defaultValue, Number step, String displayIfMin, String displayIfMax) {
        return register(new NumberSetting(name, description, visibility, min, max, defaultValue, step, displayIfMin, displayIfMax));
    }

    protected NumberSetting num(String name, String description, Number min, Number max, Number defaultValue, Number step, String displayIfMin, String displayIfMax) {
        return register(new NumberSetting(name, description, min, max, defaultValue, step, displayIfMin, displayIfMax));
    }

    protected NumberSetting num(String name, Number min, Number max, Number defaultValue) {
        return register(new NumberSetting(name, "", min, max, defaultValue));
    }

    protected NumberSetting num(String name, Number min, Number max, Number defaultValue, BooleanSupplier visibility) {
        return register(new NumberSetting(name, "", visibility, min, max, defaultValue));
    }

    protected NumberSetting num(String name, Number min, Number max, Number defaultValue, Number step) {
        return register(new NumberSetting(name, "", min, max, defaultValue, step));
    }

    protected NumberSetting num(String name, BooleanSupplier visibility, Number min, Number max, Number defaultValue) {
        return register(new NumberSetting(name, "", visibility, min, max, defaultValue));
    }

    protected NumberSetting num(String name, BooleanSupplier visibility, Number min, Number max, Number defaultValue, String displayIfMin) {
        return register(new NumberSetting(name, "", visibility, min, max, defaultValue, displayIfMin));
    }

    protected NumberSetting num(String name, BooleanSupplier visibility, Number min, Number max, Number defaultValue, Number step) {
        return register(new NumberSetting(name, "", visibility, min, max, defaultValue, step));
    }

    protected NumberSetting num(String name, BooleanSupplier visibility, Number min, Number max, Number defaultValue, Number step, String displayIfMin, String displayIfMax) {
        return register(new NumberSetting(name, "", visibility, min, max, defaultValue, step, displayIfMin, displayIfMax));
    }

    protected NumberSetting num(String name, Number min, Number max, Number defaultValue, Number step, String displayIfMin, String displayIfMax) {
        return register(new NumberSetting(name, "", min, max, defaultValue, step, displayIfMin, displayIfMax));
    }

    protected <E extends Enum<E>> EnumSetting<E> enm(String name, String description, E defaultValue) {
        return register(new EnumSetting<>(name, description, defaultValue));
    }

    protected <E extends Enum<E>> EnumSetting<E> enm(String name, String description, BooleanSupplier visibility, E defaultValue) {
        return register(new EnumSetting<>(name, description, visibility, defaultValue));
    }

    protected <E extends Enum<E>> EnumSetting<E> enm(String name, E defaultValue) {
        return register(new EnumSetting<>(name, "", defaultValue));
    }

    protected <E extends Enum<E>> EnumSetting<E> enm(String name, BooleanSupplier visibility, E defaultValue) {
        return register(new EnumSetting<>(name, "", visibility, defaultValue));
    }

    protected BindSetting bind(String name, String description, int defaultKey) {
        return register(new BindSetting(name, description, defaultKey));
    }

    protected BindSetting bind(String name, String description, boolean visibility, int defaultKey) {
        return register(new BindSetting(name, description, visibility, defaultKey));
    }

    protected BindSetting bind(String name, int defaultKey) {
        return register(new BindSetting(name, "", defaultKey));
    }

    protected BindSetting bind(String name, boolean visibility, int defaultKey) {
        return register(new BindSetting(name, "", visibility, defaultKey));
    }

    protected StringSetting str(String name, String description, String defaultValue) {
        return register(new StringSetting(name, description, defaultValue));
    }

    protected StringSetting str(String name, String description, BooleanSupplier visibility, String defaultValue) {
        return register(new StringSetting(name, description, visibility, defaultValue));
    }

    protected StringSetting str(String name, String defaultValue) {
        return register(new StringSetting(name, "", defaultValue));
    }

    protected StringSetting str(String name, BooleanSupplier visibility, String defaultValue) {
        return register(new StringSetting(name, "", visibility, defaultValue));
    }

    protected <T> SetSetting<T> set(String name, String description, Class<T> type) {
        return register(new SetSetting<>(name, description, type));
    }

    protected <T> SetSetting<T> set(String name, String description, Class<T> type, Set<T> defaultValues) {
        return register(new SetSetting<>(name, description, type, defaultValues));
    }

    protected <T> SetSetting<T> set(String name, Class<T> type) {
        return register(new SetSetting<>(name, "", type));
    }

    protected <T> SetSetting<T> set(String name, Class<T> type, Set<T> defaultValues) {
        return register(new SetSetting<>(name, "", type, defaultValues));
    }

    protected ColorSetting color(String name, String description, Color defaultValue) {
        return register(new ColorSetting(name, description, defaultValue));
    }

    protected ColorSetting color(String name, String description, Color defaultValue, boolean allowAlpha) {
        return register(new ColorSetting(name, description, defaultValue, allowAlpha));
    }

    protected ColorSetting color(String name, String description, BooleanSupplier visibility, Color defaultValue, boolean allowAlpha) {
        return register(new ColorSetting(name, description, visibility, defaultValue, allowAlpha));
    }

    protected ColorSetting color(String name, Color defaultValue) {
        return register(new ColorSetting(name, "", defaultValue));
    }

    protected ColorSetting color(String name, Color defaultValue, boolean allowAlpha) {
        return register(new ColorSetting(name, "", defaultValue, allowAlpha));
    }

    protected ColorSetting color(String name, BooleanSupplier visibility, Color defaultValue, boolean allowAlpha) {
        return register(new ColorSetting(name, "", visibility, defaultValue, allowAlpha));
    }

    public enum Category {
        GHOST("Ghost"),
        COMBAT("Combat"),
        MISC("Misc"),
        RENDER("Render"),
        MOVEMENT("Movement"),
        PLAYER("Player"),
        CLIENT("Client"),
        HUD("Hud");

        @Getter
        private final String name;

        Category(String name) {
            this.name = name;
        }
    }
}