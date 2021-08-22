package com.github.Dewynion.embercore.command;

import com.google.common.collect.ImmutableSet;
import org.bukkit.command.CommandSender;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public abstract class CommandNode {
    protected String identifier;
    protected Set<String> aliases;
    protected String description = "No description available.";
    protected String usage = "/<commmand>";
    protected int numArgs = 0;
    protected boolean playerOnly = false;

    /** The parent of this node. May be null. DO NOT set directly; use {@link #setParent(CommandNode)} instead. */
    private CommandNode parent;
    /** The children of this node. May be empty. Do not modify directly; assign children to this node via {@link #setParent(CommandNode)}. */
    private Set<CommandNode> children;

    public CommandNode(String identifier, String... aliases) {
        this.identifier = identifier;
        children = new HashSet<>();
        this.aliases = new HashSet<>();
        this.aliases.add(identifier.toLowerCase());
        this.aliases.addAll(Arrays.asList(aliases));
    }

    /**
     * Is this node the root node of its tree? i.e. - does it have no parent?
     */
    public final boolean isRoot() {
        return parent == null;
    }

    /**
     *  Sets the parent CommandNode of this node. Handles removal of this node from its old parent's children
     *  (if applicable) and addition to the new parent's children (if applicable).
     */
    public final void setParent(CommandNode newParent) {
        if (parent != null)
            parent.children.remove(this);
        parent = newParent;
        if (parent != null)
            parent.children.add(this);
    }

    /** Returns the usage string of this command, replacing "< command >" (minus spaces) with the identifier. */
    public final String getUsage() {
        return usage.replace("<command>", identifier);
    }

    public final String getIdentifier() {
        return identifier;
    }

    /**
     * Returns the description of this command.
     */
    public final String getDescription() {
        return description;
    }

    /**
     * Whether the command contained within this node can only be run by a player.
     * Does nothing on its own; exists solely for convenience in command handlers designed for either player
     * or console use.
     */
    public final boolean isPlayerOnly() {
        return playerOnly;
    }

    /**
     * Returns the furthest command node using this node as the root and each
     * subsequent string in the argument array as an identifier for the direct child of this node.
     * <br><br>
     * For example, assume the array <code>{"stem", "leaf"}</code>.
     * Executing this method will attempt to find a child node with the identifier "stem",
     * and from that node, a child node with the identifier "leaf".
     */
    public final CommandNode getLeafCommand(String[] args) {
        // if there are more arguments than this command accepts, assume there's a subcommand
        if (args.length > Math.abs(numArgs)) {
            for (CommandNode subcommand : children) {
                if (subcommand.aliases.contains(args[0].toLowerCase()))
                    return subcommand.getLeafCommand(Arrays.copyOfRange(args, 1, args.length));
            }
        }
        // if no subcommand was found, assume the player just entered too many arguments and return this command.
        return this;
    }

    public final boolean executeLeaf(CommandSender sender, String[] args) {
        if (args.length > Math.abs(numArgs)) {
            for (CommandNode subcommand : children) {
                if (subcommand.aliases.contains(args[0].toLowerCase()))
                    return subcommand.executeLeaf(sender, Arrays.copyOfRange(args, 1, args.length));
            }
        }
        return execute(sender, args);
    }

    public final Set<CommandNode> getChildren() {
        return ImmutableSet.copyOf(children);
    }

    /**
     * Generates a command string from this node and its parents.<br>
     * For example, given structure
     * <pre>{@code RootNode("root") -> StemNode("stem") -> LeafNode("leaf")}</pre>
     * invoking this method on <code>LeafNode</code> will generate the string <code>"root stem leaf"</code>.
     */
    public final String getCommandString() {
        CommandNode current = this;
        StringBuilder sb = new StringBuilder();
        while (current != null) {
            sb.insert(0, current.identifier).insert(0, " ");
            current = current.parent;
        }
        return sb.toString().trim();
    }

    public abstract boolean execute(CommandSender sender, String[] args);
}
