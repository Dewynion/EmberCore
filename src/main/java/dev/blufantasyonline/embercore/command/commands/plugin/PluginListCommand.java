package dev.blufantasyonline.embercore.command.commands.plugin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.blufantasyonline.embercore.command.EmbercoreCommandRunner;
import dev.blufantasyonline.embercore.config.serialization.SerializationInfo;
import dev.blufantasyonline.embercore.reflection.PluginLoader;
import dev.blufantasyonline.embercore.reflection.annotations.AfterEnable;
import dev.blufantasyonline.embercore.reflection.annotations.OnEnable;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

@OnEnable
@SerializationInfo(path = "plugin.list", useSuperclasses = true)
public final class PluginListCommand extends EmbercoreCommandRunner {
    @JsonIgnore
    public static final String IDENTIFIER = "list";

    private String requiredPermission = "embercore.plugin.list";
    private String noPermissionsText = "You can't do that.";

    public PluginListCommand() {
        super(CorePluginCommand.class, IDENTIFIER);
        this.shortDescription = "Lists plugins managed by EmberCore.";
        this.description = shortDescription;
        this.usage = "/<command>";
    }

    @AfterEnable
    private void updatePermissions() {
        this.permission = requiredPermission;
        this.permissionMessage = noPermissionsText;
    }

    @Override
    public boolean execute(CommandSender sender, List<String> args) {
        sendMessage(sender, "Plugins managed by EmberCore:");
        for (Plugin p : Bukkit.getServer().getPluginManager().getPlugins()) {
            if (!(p instanceof JavaPlugin))
                continue;

            JavaPlugin plugin = (JavaPlugin) p;
            if (PluginLoader.registered(plugin)) {
                // TODO: make this not old and dated
                sendMessage(sender, " - " + plugin.getName());
            }
        }
        return true;
    }
}
