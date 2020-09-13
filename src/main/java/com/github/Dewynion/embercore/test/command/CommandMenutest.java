package com.github.Dewynion.embercore.test.command;

import com.github.Dewynion.embercore.test.gui.RandomItemPagedMenu;
import com.github.Dewynion.embercore.gui.menu.MenuManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CommandMenutest implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player))
            return false;
        Player p = (Player) sender;
        MenuManager.getInstance().openMenu(p, MenuManager.getInstance().byClass(RandomItemPagedMenu.class));
        return true;
    }
}
