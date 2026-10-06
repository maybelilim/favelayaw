package net.favela.yaw;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.loader.api.FabricLoader;
import net.fabricmc.loader.api.metadata.ModMetadata;
import net.favela.yaw.impl.management.Manager;
import net.favela.yaw.impl.util.log.Log;

public class EntryPoint implements ClientModInitializer {

    public static final String MOD_ID = "favelayaw";

    private static String name;
    private static String version;

    @Override
    public void onInitializeClient() {
        ModMetadata meta = metadata();
        name = meta.getName();
        version = meta.getVersion().getFriendlyString();

        Manager.init();
        Log.info("{} {} initialized ({} modules)", name, version, Manager.MODULE.getModules().size());
    }

    private static ModMetadata metadata() {
        return FabricLoader.getInstance()
                .getModContainer(MOD_ID)
                .orElseThrow(() -> new IllegalStateException("Mod container not found: " + MOD_ID))
                .getMetadata();
    }

    public static String name() {
        return name;
    }

    public static String version() {
        return version;
    }
}