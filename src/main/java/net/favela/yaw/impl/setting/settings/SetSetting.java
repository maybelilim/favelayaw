package net.favela.yaw.impl.setting.settings;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import net.favela.yaw.impl.setting.Setting;
import net.favela.yaw.impl.util.log.Log;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.function.BooleanSupplier;

public class SetSetting<T> extends Setting<Set<T>> {

    private final Class<T> type;

    public SetSetting(String name, String description, BooleanSupplier visibility, Class<T> type, Set<T> defaultValues) {
        super(name, description, visibility, Set.copyOf(defaultValues));
        this.type = type;
        this.value = new LinkedHashSet<>(defaultValues);
    }

    public SetSetting(String name, String description, Class<T> type, Set<T> defaultValues) {
        this(name, description, null, type, defaultValues);
    }

    public SetSetting(String name, String description, Class<T> type) {
        this(name, description, null, type, Set.of());
    }

    @Override
    public Set<T> get() {
        return Collections.unmodifiableSet(value);
    }

    @Override
    public void set(Set<T> newValues) {
        this.value = new LinkedHashSet<>(newValues);
    }

    @Override
    public void reset() {
        this.value = new LinkedHashSet<>(defaultValue);
    }

    public Class<T> getType() {
        return type;
    }

    public boolean add(T value) { return this.value.add(value); }
    public boolean remove(T value) { return this.value.remove(value); }
    public boolean contains(T value) { return this.value.contains(value); }
    public void clear() { value.clear(); }
    public int size() { return value.size(); }
    public boolean isEmpty() { return value.isEmpty(); }

    public void toggle(T value) {
        if (!this.value.remove(value)) this.value.add(value);
    }

    @Override
    public JsonElement toJson() {
        JsonArray array = new JsonArray();
        for (T v : value) {
            switch (v) {
                case String s -> array.add(new JsonPrimitive(s));
                case Number n -> array.add(new JsonPrimitive(n));
                case Boolean b -> array.add(new JsonPrimitive(b));
                case Enum<?> e -> array.add(new JsonPrimitive(e.name()));
                default -> array.add(new JsonPrimitive(v.toString()));
            }
        }
        return array;
    }

    @Override
    @SuppressWarnings({"unchecked", "rawtypes"})
    public void fromJson(JsonElement element) {
        if (element == null || !element.isJsonArray()) return;
        value.clear();
        for (JsonElement e : element.getAsJsonArray()) {
            if (!e.isJsonPrimitive()) continue;
            try {
                if (type == String.class) value.add((T) e.getAsString());
                else if (type == Integer.class) value.add((T) Integer.valueOf(e.getAsInt()));
                else if (type == Double.class) value.add((T) Double.valueOf(e.getAsDouble()));
                else if (type == Float.class) value.add((T) Float.valueOf(e.getAsFloat()));
                else if (type == Long.class) value.add((T) Long.valueOf(e.getAsLong()));
                else if (type == Boolean.class) value.add((T) Boolean.valueOf(e.getAsBoolean()));
                else if (type.isEnum()) value.add((T) Enum.valueOf((Class) type, e.getAsString()));
            } catch (Exception ex) {
                Log.warn("Skipping invalid value '{}' for set setting {}", e.getAsString(), getName());
            }
        }
    }
}