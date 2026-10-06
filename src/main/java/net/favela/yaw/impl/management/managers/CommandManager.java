package net.favela.yaw.impl.management.managers;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import lombok.Getter;
import lombok.Setter;
import net.fabricmc.fabric.api.client.message.v1.ClientSendMessageEvents;
import net.favela.yaw.impl.commands.Command;
import net.favela.yaw.impl.commands.impl.ConfigCommand;
import net.favela.yaw.impl.commands.impl.ModuleCommand;
import net.favela.yaw.impl.management.Manager;
import net.favela.yaw.impl.modules.Module;
import net.favela.yaw.impl.util.log.Log;
import net.minecraft.client.multiplayer.ClientSuggestionProvider;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.ArrayList;
import java.util.List;

import static net.favela.yaw.impl.util.wrapper.Wrapper.MC;

public class CommandManager {

    private final List<Command> commands = new ArrayList<>();
    private final CommandDispatcher<SharedSuggestionProvider> dispatcher = new CommandDispatcher<>();

    @Setter
    @Getter
    private String prefix = "!";

    public void initialize() {
        for (Module module : Manager.MODULE.getModules()) {
            add(new ModuleCommand(module));
        }
        add(new ConfigCommand());

        ClientSendMessageEvents.ALLOW_CHAT.register(this::handleChat);
    }

    private boolean handleChat(String message) {
        if (!message.startsWith(prefix)) return true;
        try {
            dispatch(message.substring(prefix.length()));
        } catch (Exception e) {
            Log.error("Command error", e);
        }
        return false;
    }

    public void add(Command command) {
        commands.removeIf(existing -> existing.getName().equals(command.getName()));
        commands.add(command);
        command.registerTo(dispatcher);
    }

    public void dispatch(String message) throws CommandSyntaxException {
        if (MC.getConnection() != null) {
            ClientSuggestionProvider provider = MC.getConnection().getSuggestionsProvider();
            dispatcher.execute(message, provider);
        }
    }

    public Command get(String name) {
        for (Command command : commands) {
            if (command.getName().equals(name)) return command;
        }
        return null;
    }

    public List<Command> getCommands() {
        return List.copyOf(commands);
    }
}
