package com.github.Dewynion.embercoretest.command;

import com.github.Dewynion.embercore.reflection.wrapper.entity.WEntityPlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Look, I'll be honest, I've just been changing the code in this class every time I want to com.github.Dewynion.embercore.test
 * something with NMS and using the same command to com.github.Dewynion.embercore.test it.
 */
public class CommandNMSTest implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player))
            return false;
        Player p = (Player) sender;
        WEntityPlayer wep = new WEntityPlayer(p);
        p.sendMessage(String.format("Your ping: %s", wep.getPing()));
        return true;
    }
}
