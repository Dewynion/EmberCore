package dev.blufantasyonline.embercore.command.commands.plugin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.blufantasyonline.embercore.EmberCore;
import dev.blufantasyonline.embercore.command.EmbercoreCommandRunner;
import dev.blufantasyonline.embercore.config.serialization.SerializationInfo;
import dev.blufantasyonline.embercore.reflection.PluginLoader;
import dev.blufantasyonline.embercore.reflection.annotations.AfterEnable;
import dev.blufantasyonline.embercore.reflection.annotations.OnEnable;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

@OnEnable
@SerializationInfo(path = "plugin.reload", useSuperclasses = true)
public class PluginReloadCommand extends EmbercoreCommandRunner {
    @JsonIgnore
    public static final String IDENTIFIER = "reload";

    private String requiredPermission = "embercore.plugin.reload";
    private String noPermissionsText = "You can't do that.";

    public PluginReloadCommand() {
        super(CorePluginCommand.class, IDENTIFIER);
        this.shortDescription = "Reloads a plugin managed by EmberCore.";
        this.description = shortDescription;
        this.usage = "/<command> [plugin name]";
    }

    @AfterEnable
    private void updatePermissions() {
        this.permission = requiredPermission;
        this.permissionMessage = noPermissionsText;
    }

    @Override
    public boolean execute(CommandSender sender, List<String> args) {
        if (args.size() < 1) {
            sendMessage(sender, "Please provide the name of a plugin to reload.");
            return true;
        }

        String pluginName = args.get(0);
        JavaPlugin plugin = (JavaPlugin) Bukkit.getServer().getPluginManager().getPlugin(pluginName);
        if (plugin == null) {
            sendMessage(sender, "No plugin with that name is present on this server.");
        } else if (!PluginLoader.registered(plugin)) {
            sendMessage(sender, plugin.getName() + " isn't managed by EmberCore. " +
                    "For a list of valid plugins, please use /ec plugin list.");
        } else if (plugin == EmberCore.getInstance()) {
            sendMessage(sender, "EmberCore cannot reload itself.");
        } else {
            // This doesn't actually work for loading EmberRPG modules. Whoops. They just get unloaded.
            sendMessage(sender, "Reloading plugin: " + plugin.getName());
            // scuffed
            Bukkit.getServer().getPluginManager().disablePlugin(plugin);
            Bukkit.getServer().getPluginManager().enablePlugin(plugin);
            sendMessage(sender, "Reload complete.");
        }

        return true;
    }
}
