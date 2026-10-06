package net.favela.yaw.impl.setting.settings;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import lombok.Getter;
import net.favela.yaw.impl.setting.Setting;

import java.util.function.BooleanSupplier;

public class NumberSetting extends Setting<Number> {

    @Getter private final Number min;
    @Getter private final Number max;
    @Getter private final Number step;
    private final String displayIfMin;
    private final String displayIfMax;

    public NumberSetting(String name, String description, BooleanSupplier visibility,
                         Number min, Number max, Number defaultValue, Number step,
                         String displayIfMin, String displayIfMax) {
        super(name, description, visibility, defaultValue);
        this.min = min;
        this.max = max;
        this.step = step != null ? step : defaultStep(defaultValue);
        this.displayIfMin = displayIfMin;
        this.displayIfMax = displayIfMax;
    }

    public NumberSetting(String name, String description, BooleanSupplier visibility, Number min, Number max, Number defaultValue, Number step) {
        this(name, description, visibility, min, max, defaultValue, step, null, null);
    }

    public NumberSetting(String name, String description, BooleanSupplier visibility, Number min, Number max, Number defaultValue) {
        this(name, description, visibility, min, max, defaultValue, null, null, null);
    }

    public NumberSetting(String name, String description, BooleanSupplier visibility, Number min, Number max, Number defaultValue, String displayIfMin) {
        this(name, description, visibility, min, max, defaultValue, null, displayIfMin, null);
    }

    public NumberSetting(String name, String description, Number min, Number max, Number defaultValue, Number step, String displayIfMin, String displayIfMax) {
        this(name, description, null, min, max, defaultValue, step, displayIfMin, displayIfMax);
    }

    public NumberSetting(String name, String description, Number min, Number max, Number defaultValue, Number step) {
        this(name, description, null, min, max, defaultValue, step, null, null);
    }

    public NumberSetting(String name, String description, Number min, Number max, Number defaultValue) {
        this(name, description, null, min, max, defaultValue, null, null, null);
    }

    private static Number defaultStep(Number defaultValue) {
        return switch (defaultValue) {
            case Long ignored -> 1L;
            case Float ignored -> 0.1f;
            case Double ignored -> 0.1d;
            default -> 1;
        };
    }

    @Override
    public void set(Number value) {
        this.value = switch (defaultValue) {
            case Integer ignored -> Math.clamp(value.intValue(), min.intValue(), max.intValue());
            case Long ignored -> Math.clamp(value.longValue(), min.longValue(), max.longValue());
            case Float ignored -> Math.clamp(value.floatValue(), min.floatValue(), max.floatValue());
            case Double ignored -> Math.clamp(value.doubleValue(), min.doubleValue(), max.doubleValue());
            default -> value;
        };
    }

    public String getRenderText() {
        if (displayIfMin != null && value.equals(min)) return displayIfMin;
        if (displayIfMax != null && value.equals(max)) return displayIfMax;
        return value.toString();
    }

    public float getFloat() { return value.floatValue(); }
    public int getInt() { return value.intValue(); }

    @Override
    public JsonElement toJson() {
        return new JsonPrimitive(value);
    }

    @Override
    public void fromJson(JsonElement element) {
        if (element == null || !element.isJsonPrimitive()) return;
        set(switch (defaultValue) {
            case Integer ignored -> element.getAsInt();
            case Long ignored -> element.getAsLong();
            case Float ignored -> element.getAsFloat();
            default -> element.getAsDouble();
        });
    }
}