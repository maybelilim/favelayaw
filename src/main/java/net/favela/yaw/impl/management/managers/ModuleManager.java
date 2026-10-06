package net.favela.yaw.impl.management.managers;

import net.favela.yaw.impl.modules.Module;
import net.favela.yaw.impl.util.log.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.ServiceLoader;

public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();
    private final Map<Class<?>, Module> byClass = new HashMap<>();

    public void initialize() {
        List<Module> loaded = new ArrayList<>();
        for (ServiceLoader.Provider<Module> provider : ServiceLoader.load(Module.class, ModuleManager.class.getClassLoader()).stream().toList()) {
            try {
                loaded.add(provider.get());
            } catch (Throwable t) {
                Log.error("Failed to load module {}", provider.type().getName(), t);
            }
        }
        loaded.sort(Comparator.comparingInt((Module m) -> m.getCategory().ordinal())
                .thenComparing(Module::getName, String.CASE_INSENSITIVE_ORDER));
        loaded.forEach(this::register);
    }

    public void register(Module module) {
        if (module == null || byClass.containsKey(module.getClass())) return;
        modules.add(module);
        byClass.put(module.getClass(), module);
    }

    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public List<Module> getModulesByCategory(Module.Category category) {
        List<Module> result = new ArrayList<>();
        for (Module module : modules) {
            if (module.getCategory() == category) result.add(module);
        }
        return result;
    }

    @SuppressWarnings("unchecked")
    public <T extends Module> T get(Class<T> clazz) {
        return (T) byClass.get(clazz);
    }
}
