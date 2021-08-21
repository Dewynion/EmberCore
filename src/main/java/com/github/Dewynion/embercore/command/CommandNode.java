package com.github.Dewynion.embercore.command;

import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class CommandNode {
    protected CommandNode parent;
    protected Set<CommandNode> children;

    protected String identifier;
    protected Set<String> aliases;
    protected String description = "No description available.";
    protected int numArgs = 0;

    public CommandNode(String identifier) {
        this.identifier = identifier;
        children = new HashSet<>();
        aliases = new HashSet<>();
        aliases.add(identifier.toLowerCase());
    }

    public final boolean isRoot() {
        return parent == null;
    }

    public final boolean getLeafCommand(CommandSender sender, String[] args) {
        // if there are more arguments than this command accepts, assume there's a subcommand
        if (args.length > Math.abs(numArgs)) {
            for (CommandNode subcommand : children) {
                if (subcommand.aliases.contains(args[0].toLowerCase()))
                    return subcommand.getLeafCommand(sender, Arrays.copyOfRange(args, 1, args.length));
            }
        }
        // if no subcommand was found, assume the player just entered too many arguments and try to execute this
        // command anyway
        return execute(sender, args);
    }

    public abstract boolean execute(CommandSender sender, String[] args);
}
