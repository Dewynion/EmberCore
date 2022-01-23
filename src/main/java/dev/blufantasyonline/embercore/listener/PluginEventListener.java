package dev.blufantasyonline.embercore.listener;


import dev.blufantasyonline.embercore.CoreLoadPriority;
import dev.blufantasyonline.embercore.EmberCore;
import dev.blufantasyonline.embercore.reflection.PluginLoader;
import dev.blufantasyonline.embercore.reflection.annotations.OnEnable;
import dev.blufantasyonline.embercore.util.armorstand.ArmorStandUtil;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.server.PluginDisableEvent;
import org.bukkit.plugin.java.JavaPlugin;

@OnEnable(priority = CoreLoadPriority.PLUGIN_EVENT_LISTENER_PRIORITY)
public final class PluginEventListener implements Listener {
    /**
     * Exists to clear out unnecessary assembly information for
     * disabled plugins.
     */
    @EventHandler
    public void onPluginDisable(PluginDisableEvent event) {
        if (event.getPlugin() instanceof JavaPlugin) {
            JavaPlugin plugin = (JavaPlugin) event.getPlugin();
            if (PluginLoader.registered(plugin)) {
                EmberCore.info("Plugin %s has been disabled. Clearing cached information.",
                        plugin.getName());
                PluginLoader.remove(plugin);
                if (ArmorStandUtil.registered(plugin))
                    ArmorStandUtil.removeAll(plugin);
            }
        }
    }
}
