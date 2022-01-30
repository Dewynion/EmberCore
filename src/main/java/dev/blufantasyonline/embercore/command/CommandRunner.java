package dev.blufantasyonline.embercore.command;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.google.common.collect.ImmutableSet;
import dev.blufantasyonline.embercore.EmberCore;
import dev.blufantasyonline.embercore.reflection.PluginLoader;
import org.bukkit.command.Command;
import org.bukkit.command.CommandException;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.*;

public abstract class CommandRunner {
    private static final String DEFAULT_PERMISSION_MESSAGE = "Missing permission!";

    protected String identifier;
    protected List<String> aliases = new ArrayList<>();
    protected String description = "No description available.";
    protected String shortDescription = "No description available.";
    protected String permission = "";
    protected String permissionMessage = "";
    protected String usage = "/<commmand>";
    protected int numArgs = 0;
    protected boolean playerOnly = false;

    /**
     * The parent of this node. May be null. DO NOT set directly; use {@link #setParent(CommandRunner)} instead.
     */
    private CommandRunner parent;
    private Class<? extends CommandRunner> parentClass;
    /**
     * The children of this node. May be empty. Do not modify directly; assign children to this node via {@link #setParent(CommandRunner)}.
     */
    private Map<String, CommandRunner> children = new HashMap<>();

    private final static String[] EMPTY_ARGS = new String[0];

    public CommandRunner(String identifier, String... aliases) {
        this(null, identifier, aliases);
    }

    public CommandRunner(Class<? extends CommandRunner> parentClass, String identifier, String... aliases) {
        this.identifier = identifier.toLowerCase();
        this.parentClass = parentClass;
        this.aliases.addAll(Arrays.stream(aliases).map(String::toLowerCase).toList());
    }

    /**
     * Is this node the root node of its tree? i.e. - does it have no parent?
     */
    public final boolean isRoot() {
        return parent == null && parentClass == null;
    }

    /**
     * Sets the parent CommandNode of this node. Handles removal of this node from its old parent's children
     * (if applicable) and addition to the new parent's children (if applicable).
     */
    public void setParent(CommandRunner newParent) {
        if (parent != null)
            parent.children.remove(identifier);
        parent = newParent;
        if (parent != null) {
            parent.children.put(identifier, this);
            parentClass = parent.getClass();
        } else {
            parentClass = null;
        }
    }

    public Class<? extends CommandRunner> getParentClass() {
        return parentClass;
    }

    /**
     * Returns the usage string of this command, replacing "< command >" (minus spaces) with the full command.
     */
    public final String getUsage() {
        return usage.replace("<command>", getCommandString());
    }

    public final String getIdentifier() {
        return identifier;
    }

    /**
     * Returns the description of this command.
     */
    public final String getDescription() {
        return description.isBlank() ? shortDescription : description;
    }

    public final String getShortDescription() {
        return shortDescription.isBlank() ? description : shortDescription;
    }

    public final String getPermission() {
        return permission;
    }
    /**
     * Whether the command contained within this node can only be run by a player.
     * Does nothing on its own; exists solely for convenience in command handlers designed for either player
     * or console use.
     */
    public final boolean isPlayerOnly() {
        return playerOnly;
    }

    public final Collection<CommandRunner> getChildren() {
        return children.values();
    }

    public final CommandRunner getParent() {
        return parent;
    }

    public final CommandRunner getLeafCommand(String[] args) {
        return getLeafCommand(args);
    }

    /**
     * Generates a command string from this node and its parents.<br>
     * For example, given structure
     * <pre>{@code RootNode("root") -> StemNode("stem") -> LeafNode("leaf")}</pre>
     * invoking this method on <code>LeafNode</code> will generate the string <code>"root stem leaf"</code>.
     */
    public final String getCommandString() {
        CommandRunner current = this;
        StringBuilder sb = new StringBuilder();
        while (current != null) {
            sb.insert(0, current.identifier).insert(0, " ");
            current = current.parent;
        }
        return sb.toString().trim();
    }

    public final boolean execute(CommandSender sender, String[] args) {
        return execute(sender, Arrays.asList(args));
    }

    public void sendMessage(CommandSender sender, String message, Object... args) {
        sender.sendMessage(String.format(message, args));
    }

    // TODO: annotated argument types + automated conversion
    public abstract boolean execute(CommandSender sender, List<String> args);

    /**
     * Returns the furthest command node using this node as the root and each
     * subsequent string in the argument array as an identifier for the direct child of this node.
     * <br><br>
     * For example, assume the array <code>{"stem", "leaf"}</code>.
     * Executing this method will attempt to find a child node with the identifier "stem",
     * and from that node, a child node with the identifier "leaf".
     */
    final CommandRunner getLeafCommand(ArrayList<String> args) {
        CommandRunner leaf = this;
        while (args.size() > leaf.numArgs && !leaf.children.isEmpty()) {
            String subcommand = args.get(0).toLowerCase();
            if (leaf.children.containsKey(subcommand)) {
                args.remove(0);
                leaf = leaf.children.get(subcommand);
            } else
                break;
        }
        // if no subcommand was found, assume the player just entered too many arguments and return this command.
        return leaf;
    }

    final boolean executeLeaf(CommandSender sender, String[] args) throws CommandException {
        ArrayList<String> argsList = new ArrayList<>(Arrays.asList(args));
        CommandRunner leaf = getLeafCommand(argsList);

        if (leaf.playerOnly && !(sender instanceof Player)) {
            sender.sendMessage("This command can only be executed by a player.");
            return true;
        } else if (leaf.numArgs > argsList.size()) {
            sendMessage(sender, "Usage: %s", leaf.getUsage());
            return true;
        } else if (!sender.hasPermission(leaf.permission)) {
            String msg = permissionMessage.isEmpty() ? DEFAULT_PERMISSION_MESSAGE : permissionMessage;
            sendMessage(sender, msg);
            return true;
        }

        return leaf.execute(sender, argsList);
    }
}
