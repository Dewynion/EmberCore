package com.github.Dewynion.embercore.listener;


import com.github.Dewynion.embercore.CoreLoadPriority;
import com.github.Dewynion.embercore.EmberCore;
import com.github.Dewynion.embercore.reflection.ReflectionHelper;
import com.github.Dewynion.embercore.reflection.Singleton;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;

@Singleton(priority = CoreLoadPriority.PLUGIN_EVENT_LISTENER_PRIORITY)
public class PluginEventListener implements Listener {

    /**
     * Exists to clear out unnecessary assembly information for
     * disabled plugins.
     */
    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin() instanceof JavaPlugin && ReflectionHelper.registered((JavaPlugin) event.getPlugin())) {
            ReflectionHelper.remove((JavaPlugin) event.getPlugin());
            EmberCore.info("Plugin %s has been disabled. Clearing assembly information.",
                    event.getPlugin().getName());
        }
    }
}
