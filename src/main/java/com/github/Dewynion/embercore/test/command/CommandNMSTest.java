package com.github.Dewynion.embercore.test.command;

import com.github.Dewynion.embercore.EmberCore;
import com.github.Dewynion.embercore.reflection.wrapper.entity.WCraftPlayer;
import com.github.Dewynion.embercore.reflection.wrapper.entity.WEntityPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.logging.Level;

public class CommandNMSTest implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player))
            return false;
        Player p = (Player) sender;
        EmberCore.log(Level.INFO, p.getClass().getName());
        WCraftPlayer wcp = new WCraftPlayer(p);
        EmberCore.log(Level.INFO, wcp.getObject().getClass().getName());
        EmberCore.log(Level.INFO, wcp.getHandle().getClass().getName());
        WEntityPlayer wep = new WEntityPlayer(p);
        EmberCore.log(Level.INFO, wep.getObject().getClass().getName());
        return true;
    }
}
