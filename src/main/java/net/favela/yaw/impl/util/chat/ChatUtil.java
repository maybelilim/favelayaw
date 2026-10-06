package net.favela.yaw.impl.util.chat;

import net.minecraft.client.Minecraft;
import net.minecraft.network.chat.Component;

public final class ChatUtil {

    private ChatUtil() {

    }

    public static void sendMessage(String message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.sendSystemMessage(Component.literal(message));
        }
    }

    public static void sendMessage(Component message) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player != null) {
            mc.player.sendSystemMessage(message);
        }
    }

    public static void sendInfo(String message) {
        sendMessage("§7[§bINFO§7] §f" + message);
    }

    public static void sendError(String message) {
        sendMessage("§7[§cERROR§7] §f" + message);
    }
}