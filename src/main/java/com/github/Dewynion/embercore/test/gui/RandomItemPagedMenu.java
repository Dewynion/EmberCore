package com.github.Dewynion.embercore.test.gui;

import com.github.Dewynion.embercore.CoreLoadPriority;
import com.github.Dewynion.embercore.EmberCore;
import com.github.Dewynion.embercore.gui.menu.MultiPageMenu;
import com.github.Dewynion.embercore.reflection.Singleton;
import org.bukkit.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.*;

@Singleton(priority = CoreLoadPriority.MENU_PRIORITY)
public class RandomItemPagedMenu extends MultiPageMenu {
    /**
     *  See below - maps player UUIDs to runnables that run while the players
     *  stored here have instances of this menu open.
     **/
    private HashMap<UUID, BukkitRunnable> openMenuRunnables;

    public RandomItemPagedMenu() {
        // Calls the superclass constructor with the base title of this menu.
        super("Random Items");

        // instantiate this.
        openMenuRunnables = new HashMap<>();

        // Generates a large number of completely random items.
        // This will be the same for every player who views the menu.
        List<ItemStack> contents = new ArrayList<ItemStack>();
        Random rand = new Random();
        int matLen = Material.values().length;
        for (int i = 0; i < 100 + rand.nextInt(200); i++) {
            Material mat = Material.values()[rand.nextInt(matLen)];
            if (mat.isAir())
                continue;
            contents.add(new ItemStack(mat));
        }

        // Replaces the "next" button (which defaults to green wool) with an egg.
        next = new ItemStack(Material.EGG);
        ItemMeta meta = next.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Next Page");
        next.setItemMeta(meta);

        // TODO: make this the standard for all InventoryMenus instead of directly replacing the array
        // In the context of MultiPageMenu, setContents() has additional functionality.
        setContents(contents);
        // register the very cool surprise listener
        Bukkit.getServer().getPluginManager().registerEvents(new CluckCluck(), EmberCore.getInstance());
    }

    protected void onOpenMenu(Player player) {
        // Tick obnoxiously each second while the player has the menu open.
        openMenuRunnables.put(player.getUniqueId(), new BukkitRunnable() {
            @Override
            public void run() {
                player.getLocation().getWorld().playSound(player.getLocation(),
                        Sound.BLOCK_STONE_BUTTON_CLICK_OFF,
                        1.0f,
                        1.0f);
            }
        }).runTaskTimer(EmberCore.getInstance(), 0, 20);
    }


    @Override
    protected void onCloseMenu(Player player) {
        // Stop the obnoxious ticking and make sure they're removed from the map.
        openMenuRunnables.get(player.getUniqueId()).cancel();
        openMenuRunnables.remove(player.getUniqueId());
    }

    @Override
    protected void modifyInstance(Player player) {
        // Retrieve the MenuInstance for this player.
        MultiPageMenuInstance inst = getInstance(player);
        if (inst == null)
            return;
        // For each menu page in this player's multi-paged menu instance...
        for (MenuPage page : inst.getPages()) {
            for (ItemStack i : page.getContents()) {
                if (i == null)
                    continue;
                // ...replace each item's display name with an irritating furry joke,
                // customized with the player's name.
                ItemMeta meta = i.getItemMeta();
                meta.setDisplayName("h-hewwo " + player.getName());
                i.setItemMeta(meta);
            }
        }
    }

    public class CluckCluck implements Listener {
        @EventHandler
        public void cluck(InventoryClickEvent event) {
            if (event.getCurrentItem() != null && event.getCurrentItem().equals(next)) {
                Location loc = event.getWhoClicked().getLocation();
                // Cluck loudly every time the player clicks the "next" button.
                loc.getWorld().playSound(loc, Sound.ENTITY_CHICKEN_HURT, 1.0f, 1.0f);
            }
        }
    }
}
