package net.favela.yaw.impl.management.managers;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.favela.yaw.impl.management.Manager;
import net.favela.yaw.impl.modules.Module;
import net.minecraft.client.Minecraft;
import org.lwjgl.glfw.GLFW;

import java.util.HashSet;
import java.util.Set;

public class KeybindManager {

    private final Set<Integer> heldKeys = new HashSet<>();

    public void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(this::onTick);
    }

    private void onTick(Minecraft client) {
        long handle = client.getWindow().handle();
        boolean noScreen = client.gui.screen() == null;

        for (Module module : Manager.MODULE.getModules()) {
            int key = module.bind.getKey();
            if (key == -1 || key == 0) continue;

            boolean down = isKeyDown(handle, key);
            boolean firstPress = down && heldKeys.add(key);

            if (firstPress && noScreen) {
                module.toggle();
            }
            if (!down) {
                heldKeys.remove(key);
            }
        }
    }

    private static boolean isKeyDown(long handle, int key) {
        if (key < 0) {
            return GLFW.glfwGetMouseButton(handle, -key - 2) == GLFW.GLFW_PRESS;
        }
        return GLFW.glfwGetKey(handle, key) == GLFW.GLFW_PRESS;
    }
}
