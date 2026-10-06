package net.favela.yaw.impl.commands.impl;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import net.favela.yaw.impl.commands.Command;
import net.favela.yaw.impl.modules.Module;
import net.favela.yaw.impl.setting.Setting;
import net.favela.yaw.impl.setting.settings.*;
import net.favela.yaw.impl.util.chat.ChatUtil;
import net.minecraft.ChatFormatting;
import net.minecraft.commands.SharedSuggestionProvider;

import java.awt.Color;

public class ModuleCommand extends Command {

    private final Module module;

    public ModuleCommand(Module module) {
        super(module.getName(), "");
        this.module = module;
    }

    @Override
    public void register(CommandDispatcher<SharedSuggestionProvider> dispatcher, String name) {
        dispatcher.register(LiteralArgumentBuilder.<SharedSuggestionProvider>literal(name)
                .then(builder("setting", StringArgumentType.string())
                        .suggests((context, b) -> SharedSuggestionProvider.suggest(
                                module.getSettings().stream().map(Setting::getName),
                                b
                        ))
                        .executes(context -> {
                            Setting<?> setting = findSetting(StringArgumentType.getString(context, "setting"));
                            if (setting == null) {
                                ChatUtil.sendError("Setting not found");
                                return 0;
                            }
                            return displaySettingInfo(setting);
                        })
                        .then(builder("value", StringArgumentType.greedyString()).executes(context -> {
                                    Setting<?> setting = findSetting(StringArgumentType.getString(context, "setting"));
                                    if (setting == null) {
                                        ChatUtil.sendError("Setting not found");
                                        return 0;
                                    }
                                    return setSetting(setting, StringArgumentType.getString(context, "value"));
                                })
                        )
                )
        );
    }

    private Setting<?> findSetting(String settingName) {
        return module.getSettings().stream()
                .filter(s -> s.getName().equalsIgnoreCase(settingName))
                .findFirst()
                .orElse(null);
    }

    private int displaySettingInfo(Setting<?> setting) {
        ChatUtil.sendMessage(ChatFormatting.GRAY + "Module: " + ChatFormatting.WHITE + module.getName());
        ChatUtil.sendMessage(ChatFormatting.GRAY + "Setting: " + ChatFormatting.WHITE + setting.getName());
        ChatUtil.sendMessage(ChatFormatting.GRAY + "Description: " + ChatFormatting.WHITE + setting.getDescription());
        ChatUtil.sendMessage(ChatFormatting.GRAY + "Current value: " + ChatFormatting.GREEN + getSettingValue(setting));

        if (setting instanceof NumberSetting number) {
            ChatUtil.sendMessage(ChatFormatting.GRAY + "Range: " + ChatFormatting.WHITE + number.getMin() + " - " + number.getMax());
            ChatUtil.sendMessage(ChatFormatting.GRAY + "Step: " + ChatFormatting.WHITE + number.getStep());
        } else if (setting instanceof EnumSetting<?> enumSetting) {
            String values = String.join(", ",
                    java.util.Arrays.stream(enumSetting.getValues()).map(Enum::name).toList());
            ChatUtil.sendMessage(ChatFormatting.GRAY + "Values: " + ChatFormatting.WHITE + values);
        }
        return 1;
    }

    private int setSetting(Setting<?> setting, String value) {
        try {
            switch (setting) {
                case BooleanSetting boolSetting -> {
                    boolean newValue = Boolean.parseBoolean(value);
                    boolSetting.setValue(newValue);
                    ChatUtil.sendInfo(ChatFormatting.GREEN + "Set " + ChatFormatting.WHITE + setting.getName()
                            + ChatFormatting.GREEN + " to " + ChatFormatting.WHITE + newValue);
                }
                case NumberSetting numberSetting -> {
                    Number newValue = switch (numberSetting.get()) {
                        case Integer ignored -> Integer.parseInt(value);
                        case Double ignored -> Double.parseDouble(value);
                        case Float ignored -> Float.parseFloat(value);
                        case null, default -> Long.parseLong(value);
                    };
                    numberSetting.set(newValue);
                    ChatUtil.sendInfo(ChatFormatting.GREEN + "Set " + ChatFormatting.WHITE + setting.getName()
                            + ChatFormatting.GREEN + " to " + ChatFormatting.WHITE + numberSetting.get());
                }
                case StringSetting stringSetting -> {
                    stringSetting.set(value);
                    ChatUtil.sendInfo(ChatFormatting.GREEN + "Set " + ChatFormatting.WHITE + setting.getName()
                            + ChatFormatting.GREEN + " to " + ChatFormatting.WHITE + value);
                }
                case EnumSetting<?> enumSetting -> {
                    if (!applyEnum(enumSetting, value)) return 0;
                }
                case ColorSetting colorSetting -> {
                    Color parsed = parseColor(value);
                    if (parsed == null) {
                        ChatUtil.sendError("Invalid color. Use hex: RRGGBB or RRGGBBAA");
                        return 0;
                    }
                    colorSetting.set(parsed);
                    ChatUtil.sendInfo(ChatFormatting.GREEN + "Set " + ChatFormatting.WHITE + setting.getName()
                            + ChatFormatting.GREEN + " to " + ChatFormatting.WHITE + "#%08X".formatted(parsed.getRGB()));
                }
                case BindSetting bindSetting -> {
                    try {
                        int keyCode = Integer.parseInt(value);
                        bindSetting.setKey(keyCode);
                        ChatUtil.sendInfo(ChatFormatting.GREEN + "Set " + ChatFormatting.WHITE + setting.getName()
                                + ChatFormatting.GREEN + " to key code " + ChatFormatting.WHITE + keyCode);
                    } catch (NumberFormatException e) {
                        ChatUtil.sendError("Invalid key code. Please provide an integer.");
                        return 0;
                    }
                }
                case SetSetting<?> setSetting -> {
                    return applySet(setSetting, value);
                }
                default -> {
                    ChatUtil.sendError("Unsupported setting type: " + setting.getClass().getSimpleName());
                    return 0;
                }
            }
            return 1;
        } catch (Exception e) {
            ChatUtil.sendError("Error setting value: " + e.getMessage());
            return 0;
        }
    }

    private static Color parseColor(String raw) {
        String hex = raw.startsWith("#") ? raw.substring(1) : raw;
        if (!hex.chars().allMatch(c -> Character.digit(c, 16) != -1)) return null;
        return switch (hex.length()) {
            case 6 -> Color.decode("#" + hex);
            case 8 -> new Color((int) Long.parseLong(hex, 16), true);
            default -> null;
        };
    }

    private <E extends Enum<E>> boolean applyEnum(EnumSetting<E> setting, String value) {
        try {
            E enumValue = Enum.valueOf(setting.get().getDeclaringClass(), value.toUpperCase());
            setting.set(enumValue);
            ChatUtil.sendInfo(ChatFormatting.GREEN + "Set " + ChatFormatting.WHITE + setting.getName()
                    + ChatFormatting.GREEN + " to " + ChatFormatting.WHITE + enumValue.name());
            return true;
        } catch (IllegalArgumentException e) {
            ChatUtil.sendError("Invalid enum value. Valid values:");
            for (Enum<?> v : setting.getValues()) {
                ChatUtil.sendMessage("  " + ChatFormatting.WHITE + v.name());
            }
            return false;
        }
    }

    private <T> int applySet(SetSetting<T> setting, String value) {
        String[] parts = value.split(" ", 2);
        String action = parts[0].toLowerCase();

        if (action.equals("clear")) {
            setting.clear();
            ChatUtil.sendInfo(ChatFormatting.GREEN + "Cleared " + ChatFormatting.WHITE + setting.getName());
            return 1;
        }

        if (parts.length != 2 || !(action.equals("add") || action.equals("remove") || action.equals("toggle"))) {
            ChatUtil.sendInfo("Usage: add <value>, remove <value>, toggle <value>, or clear");
            return 0;
        }

        T item = parseValue(setting.getType(), parts[1]);
        if (item == null) {
            ChatUtil.sendError("Failed to parse value");
            return 0;
        }

        switch (action) {
            case "add" -> {
                setting.add(item);
                ChatUtil.sendInfo(ChatFormatting.GREEN + "Added " + ChatFormatting.WHITE + parts[1]
                        + ChatFormatting.GREEN + " to " + ChatFormatting.WHITE + setting.getName());
            }
            case "remove" -> {
                setting.remove(item);
                ChatUtil.sendInfo(ChatFormatting.GREEN + "Removed " + ChatFormatting.WHITE + parts[1]
                        + ChatFormatting.GREEN + " from " + ChatFormatting.WHITE + setting.getName());
            }
            case "toggle" -> {
                setting.toggle(item);
                ChatUtil.sendInfo(ChatFormatting.GREEN + "Toggled " + ChatFormatting.WHITE + parts[1]
                        + ChatFormatting.GREEN + " in " + ChatFormatting.WHITE + setting.getName());
            }
        }
        return 1;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    private static <T> T parseValue(Class<T> type, String value) {
        try {
            if (type == String.class) return (T) value;
            if (type == Integer.class) return (T) Integer.valueOf(value);
            if (type == Double.class) return (T) Double.valueOf(value);
            if (type == Float.class) return (T) Float.valueOf(value);
            if (type == Long.class) return (T) Long.valueOf(value);
            if (type == Boolean.class) return (T) Boolean.valueOf(value);
            if (type.isEnum()) return (T) Enum.valueOf((Class<Enum>) type, value.toUpperCase());
        } catch (Exception ignored) {
        }
        return null;
    }

    private static String getSettingValue(Setting<?> setting) {
        return switch (setting) {
            case BooleanSetting boolSetting -> String.valueOf(boolSetting.get());
            case NumberSetting numberSetting -> numberSetting.getRenderText();
            case StringSetting stringSetting -> stringSetting.get();
            case EnumSetting<?> enumSetting -> enumSetting.get().name();
            case BindSetting bindSetting -> String.valueOf(bindSetting.getKey());
            case ColorSetting colorSetting -> "#%08X".formatted(colorSetting.get().getRGB());
            case SetSetting<?> setSetting -> setSetting.get().toString();
            default -> "unknown";
        };
    }
}