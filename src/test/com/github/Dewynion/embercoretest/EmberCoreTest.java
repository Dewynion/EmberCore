package com.github.Dewynion.embercoretest;

import com.github.Dewynion.embercore.EmberCore;
import com.github.Dewynion.embercoretest.command.CommandMenutest;
import org.bukkit.plugin.java.JavaPlugin;

public class EmberCoreTest extends JavaPlugin {
    public void onEnable() {
        EmberCore.setup(this);
    }

    private void registerCommands() {
        getCommand("menutest").setExecutor(new CommandMenutest());
    }
}
