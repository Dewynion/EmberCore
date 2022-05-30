package dev.blufantasyonline.embercore.command;

import dev.blufantasyonline.embercore.config.CoreConfigFiles;
import dev.blufantasyonline.embercore.config.serialization.SerializationInfo;
import dev.blufantasyonline.embercore.util.StringUtil;
import net.md_5.bungee.api.ChatColor;
import net.md_5.bungee.api.chat.BaseComponent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import org.bukkit.command.CommandSender;

import java.awt.*;

@SerializationInfo(location = CoreConfigFiles.COMMAND_CONFIG_FILE, useSuperclasses = true)
public abstract class EmbercoreCommandRunner extends CommandRunner {
    private Color notQuiteBlack = new Color(16, 21, 28);

    public EmbercoreCommandRunner(String identifier, String... aliases) {
        super(identifier, aliases);
    }

    public EmbercoreCommandRunner(Class<? extends CommandRunner> parentClass, String identifier, String... aliases) {
        super(parentClass, identifier, aliases);
    }

    @Override
    public void sendMessage(CommandSender sender, String message, Object... args) {
        message = String.format(message, args);
        new ComponentBuilder()
                .append(corePrefix())
                .append(message).color(ChatColor.GRAY)
                .create();
        sender.sendMessage(message);
    }

    private BaseComponent[] corePrefix() {
        return new ComponentBuilder("[").color(ChatColor.BLACK)
                .append(StringUtil.gradientText("EmberCore",
                        notQuiteBlack, Color.BLUE, Color.CYAN))
                .append("] ").color(ChatColor.BLACK)
                .create();
    }
}
