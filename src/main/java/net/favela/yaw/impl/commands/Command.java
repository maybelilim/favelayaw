package net.favela.yaw.impl.commands;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.ArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import lombok.Getter;
import net.minecraft.commands.SharedSuggestionProvider;

import java.util.List;

public class Command {

    @Getter
    private final String name;
    @Getter
    private final String description;
    private final List<String> aliases;

    public Command(String name, String description, String... aliases) {
        this.name = name;
        this.description = description;
        this.aliases = List.of(aliases);
    }

    public void executeBuild(LiteralArgumentBuilder<SharedSuggestionProvider> builder) {
    }

    public final void registerTo(CommandDispatcher<SharedSuggestionProvider> dispatcher) {
        register(dispatcher, getName());
        for (String alias : aliases) {
            register(dispatcher, alias);
        }
    }

    public void register(CommandDispatcher<SharedSuggestionProvider> dispatcher, String name) {
        LiteralArgumentBuilder<SharedSuggestionProvider> builder = LiteralArgumentBuilder.literal(name);
        executeBuild(builder);
        dispatcher.register(builder);
    }

    protected static <T> RequiredArgumentBuilder<SharedSuggestionProvider, T> builder(final String name, final ArgumentType<T> type) {
        return RequiredArgumentBuilder.argument(name, type);
    }
}