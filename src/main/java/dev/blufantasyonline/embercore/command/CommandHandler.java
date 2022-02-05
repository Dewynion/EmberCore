package dev.blufantasyonline.embercore.command;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.blufantasyonline.embercore.EmberCore;
import dev.blufantasyonline.embercore.reflection.PluginLoader;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.*;

public final class CommandHandler {
    @JsonIgnore
    private static HashMap<JavaPlugin, LinkedHashMap<String, CommandRunner>> rootCommands = new HashMap<>();
    @JsonIgnore
    private static Collection<CommandRunner> EMPTY_COMMANDS = new HashSet<>();

    public static Collection<CommandRunner> getRootCommands(JavaPlugin plugin) {
        if (!PluginLoader.registered(plugin)) {
            return Collections.unmodifiableCollection(EMPTY_COMMANDS);
        }
        rootCommands.putIfAbsent(plugin, new LinkedHashMap<>());
        return rootCommands.get(plugin).values();
    }

    public static void addRootCommand(JavaPlugin plugin, CommandRunner rootCommand) {
        if (!PluginLoader.registered(plugin)) {
            EmberCore.warn("Tried to add root command '%s' for plugin '%s', but this plugin is not registered with EmberCore.",
                    rootCommand.identifier.toLowerCase(), plugin.getName());
            return;
        }

        rootCommands.putIfAbsent(plugin, new LinkedHashMap<>());
        rootCommands.get(plugin).put(rootCommand.identifier, rootCommand);
        // add alias in case of duplicate registration between plugins
        rootCommand.aliases.add(String.format("%s:%s", plugin.getName().toLowerCase(), rootCommand.identifier));

        PluginCommand command = plugin.getCommand(rootCommand.identifier);
        command.setAliases(rootCommand.aliases)
                .setDescription(rootCommand.shortDescription)
                .setUsage(rootCommand.usage);
        if (!rootCommand.permission.isBlank())
            command.setPermissionMessage(rootCommand.permissionMessage)
                    .setPermission(rootCommand.permission);

        // finally automatically register an executor
        command.setExecutor((commandSender, command1, label, args) -> rootCommand.executeLeaf(commandSender, args));
    }
}
