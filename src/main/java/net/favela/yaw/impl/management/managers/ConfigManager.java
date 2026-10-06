package net.favela.yaw.impl.management.managers;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientLifecycleEvents;
import net.favela.yaw.EntryPoint;
import net.favela.yaw.impl.management.Manager;
import net.favela.yaw.impl.modules.Module;
import net.favela.yaw.impl.setting.Setting;
import net.favela.yaw.impl.util.log.Log;
import net.minecraft.client.Minecraft;

import java.io.IOException;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.nio.file.AtomicMoveNotSupportedException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Stream;

public class ConfigManager {

    public static final String DEFAULT_CONFIG = "default";
    private static final Pattern SAFE_NAME = Pattern.compile("[A-Za-z0-9 _\\-]{1,32}");

    private final Path configDir;
    private final Path activeConfigFile;
    private final Gson gson = new GsonBuilder().setPrettyPrinting().create();

    @Setter
    @Getter
    private String currentConfig = DEFAULT_CONFIG;

    public ConfigManager() {
        Path base = Minecraft.getInstance().gameDirectory.toPath().resolve("favelayaw");
        Path dir = base.resolve("configs");
        try {
            Files.createDirectories(dir);
        } catch (IOException e) {
            Log.error("Failed to create config directory", e);
        }
        this.configDir = dir;
        this.activeConfigFile = base.resolve("active_config.txt");
    }

    public void registerLifecycle() {
        ClientLifecycleEvents.CLIENT_STOPPING.register(client -> {
            save();
            writeActiveConfig(currentConfig);
            Log.info("{} config '{}' saved on shutdown", EntryPoint.name(), currentConfig);
        });
    }

    private Path fileFor(String name) {
        return configDir.resolve(name + ".json");
    }

    private static boolean isValidName(String name) {
        return name != null && SAFE_NAME.matcher(name).matches();
    }

    public boolean exists(String name) {
        return Files.isRegularFile(fileFor(name));
    }

    public List<String> list() {
        List<String> names = new ArrayList<>();
        try (Stream<Path> stream = Files.list(configDir)) {
            stream.filter(Files::isRegularFile)
                    .map(p -> p.getFileName().toString())
                    .filter(n -> n.endsWith(".json"))
                    .map(n -> n.substring(0, n.length() - 5))
                    .forEach(names::add);
        } catch (IOException e) {
            Log.error("Failed to list configs", e);
        }
        names.sort(String::compareToIgnoreCase);
        return names;
    }

    public boolean create(String name) {
        if (!isValidName(name) || exists(name)) return false;
        currentConfig = name;
        save(name);
        writeActiveConfig(name);
        return true;
    }

    public boolean delete(String name) {
        if (!isValidName(name) || !exists(name)) return false;
        try {
            Files.delete(fileFor(name));
        } catch (IOException e) {
            Log.error("Failed to delete config {}", name, e);
            return false;
        }
        if (currentConfig.equals(name)) {
            currentConfig = DEFAULT_CONFIG;
            save(DEFAULT_CONFIG);
            writeActiveConfig(DEFAULT_CONFIG);
        }
        return true;
    }

    public void save() {
        save(currentConfig);
    }

    public void save(String name) {
        if (!isValidName(name)) {
            Log.warn("Refusing to save config with invalid name '{}'", name);
            return;
        }
        JsonObject root = new JsonObject();
        for (Module module : Manager.MODULE.getModules()) {
            JsonObject moduleObj = new JsonObject();
            moduleObj.addProperty("enabled", module.isEnabled());
            JsonObject settingsObj = new JsonObject();
            for (Setting setting : module.getSettings()) {
                try {
                    settingsObj.add(setting.getName(), setting.toJson());
                } catch (Exception e) {
                    Log.error("Failed to save setting {} of module {}", setting.getName(), module.getName(), e);
                }
            }
            moduleObj.add("settings", settingsObj);
            root.add(module.getName(), moduleObj);
        }
        try {
            writeFile(fileFor(name), gson.toJson(root));
            Log.info("Config saved to {}", fileFor(name));
        } catch (IOException e) {
            Log.error("Failed to save config", e);
        }
    }

    public void load() {
        String active = readActiveConfig();
        if (active != null && exists(active)) {
            currentConfig = active;
        }
        if (!exists(DEFAULT_CONFIG)) {
            save(DEFAULT_CONFIG);
            Log.info("Created default config");
        }
        load(currentConfig);
    }

    public boolean load(String name) {
        if (!isValidName(name) || !exists(name)) {
            Log.info("No config file found for {}, using defaults", name);
            return false;
        }
        try (Reader reader = Files.newBufferedReader(fileFor(name), StandardCharsets.UTF_8)) {
            JsonObject root = JsonParser.parseReader(reader).getAsJsonObject();
            for (Module module : Manager.MODULE.getModules()) {
                if (!root.has(module.getName())) continue;
                JsonObject moduleObj = root.getAsJsonObject(module.getName());
                if (moduleObj.has("enabled")) {
                    boolean enabled = moduleObj.get("enabled").getAsBoolean();
                    if (enabled != module.isEnabled()) module.toggle();
                }
                if (moduleObj.has("settings")) {
                    JsonObject settingsObj = moduleObj.getAsJsonObject("settings");
                    for (Setting setting : module.getSettings()) {
                        if (settingsObj.has(setting.getName())) {
                            try {
                                setting.fromJson(settingsObj.get(setting.getName()));
                            } catch (Exception e) {
                                Log.error("Failed to load setting {} of module {}", setting.getName(), module.getName(), e);
                            }
                        }
                    }
                }
            }
            currentConfig = name;
            writeActiveConfig(name);
            Log.info("Config loaded from {}", fileFor(name));
            return true;
        } catch (Exception e) {
            Log.error("Failed to load config", e);
            return false;
        }
    }

    private void writeFile(Path file, String content) throws IOException {
        Path temp = file.resolveSibling(file.getFileName() + ".tmp");
        Files.writeString(temp, content, StandardCharsets.UTF_8);
        try {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING, StandardCopyOption.ATOMIC_MOVE);
        } catch (AtomicMoveNotSupportedException e) {
            Files.move(temp, file, StandardCopyOption.REPLACE_EXISTING);
        }
    }

    private String readActiveConfig() {
        try {
            if (Files.exists(activeConfigFile)) {
                String name = Files.readString(activeConfigFile).trim();
                return name.isEmpty() ? null : name;
            }
        } catch (IOException e) {
            Log.error("Failed to read active config marker", e);
        }
        return null;
    }

    private void writeActiveConfig(String name) {
        try {
            Files.writeString(activeConfigFile, name);
        } catch (IOException e) {
            Log.error("Failed to write active config marker", e);
        }
    }
}
