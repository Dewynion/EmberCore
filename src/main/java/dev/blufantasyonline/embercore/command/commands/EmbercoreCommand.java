package dev.blufantasyonline.embercore.command.commands;

import com.fasterxml.jackson.annotation.JsonIgnore;
import dev.blufantasyonline.embercore.command.EmbercoreCommandRunner;
import dev.blufantasyonline.embercore.config.serialization.SerializationInfo;
import dev.blufantasyonline.embercore.reflection.annotations.OnEnable;
import dev.blufantasyonline.embercore.util.StringUtil;
import org.bukkit.command.CommandSender;

import java.awt.*;
import java.util.List;

@OnEnable
@SerializationInfo(path = "", useSuperclasses = true)
public class EmbercoreCommand extends EmbercoreCommandRunner {
    @JsonIgnore
    public static final String IDENTIFIER = "embercore";
    @JsonIgnore
    public static final String[] ALIASES = { "ec" };

    public EmbercoreCommand() {
        super(IDENTIFIER, ALIASES);
        description = "EmberCore commands. These vary wildly in function and should only be used by administrators.";
        shortDescription = "EmberCore commands.";
    }

    @Override
    public boolean execute(CommandSender commandSender, List<String> args) {
        return true;
    }
}
