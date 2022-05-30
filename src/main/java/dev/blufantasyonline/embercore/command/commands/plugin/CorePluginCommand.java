package dev.blufantasyonline.embercore.command.commands.plugin;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.blufantasyonline.embercore.command.EmbercoreCommandRunner;
import dev.blufantasyonline.embercore.command.commands.EmbercoreCommand;
import dev.blufantasyonline.embercore.config.serialization.SerializationInfo;
import dev.blufantasyonline.embercore.reflection.annotations.OnEnable;
import org.bukkit.command.CommandSender;

import java.util.List;

@OnEnable
@SerializationInfo(path = "plugin", useSuperclasses = true)
public class CorePluginCommand extends EmbercoreCommandRunner {
    @JsonIgnore
    public static final String IDENTIFIER = "plugin";

    public CorePluginCommand() {
        super(EmbercoreCommand.class, IDENTIFIER);
        description = "Commands related to plugins managed by EmberCore.";
    }

    @Override
    public boolean execute(CommandSender sender, List<String> args) {
        // TODO: some kind of built-in help thing that sends child commands + descriptions
        return true;
    }
}
