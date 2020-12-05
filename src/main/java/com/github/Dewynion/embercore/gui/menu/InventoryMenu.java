package com.github.Dewynion.embercore.gui.menu;

import com.github.Dewynion.embercore.EmberCore;
import com.github.Dewynion.embercore.util.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.HumanEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.UUID;

public abstract class InventoryMenu {
    protected String title;
    protected ItemStack[] menuContents;
    protected HashMap<UUID, MenuInstance> menuInstances;

    public InventoryMenu(String title) {
        this.title = title;
        menuInstances = new HashMap<>();
        menuContents = new ItemStack[InventoryUtil.INVENTORY_SIZE];
        MenuManager.getInstance().registerMenu(this);
        Bukkit.getServer().getPluginManager().registerEvents(new MenuEventListener(), EmberCore.getInstance());
    }

    public String getTitle() {
        return title;
    }

    /**
     * Opens this menu for the given player. Calls {@link #modifyInstance(Player)},
     * then {@link #onOpenMenu(Player)}.
     */
    public final void open(Player player) {
        MenuInstance inst = addInstance(player);
        modifyInstance(player);
        onOpenMenu(player);
        inst.open();
    }

    /**
     * Closes this menu for the given player. Calls {@link #onCloseMenu(Player)}.
     */
    public final void close(Player player) {
        onCloseMenu(player);
        removeInstance(player);
    }

    protected abstract void onOpenMenu(Player player);

    protected abstract void onCloseMenu(Player player);

    protected boolean hasInstanceOpen(Player player) {
        return menuInstances.containsKey(player.getUniqueId());
    }

    protected <T extends MenuInstance> T getInstance(Player player) {
        return (T) menuInstances.getOrDefault(player.getUniqueId(), null);
    }

    protected <T extends MenuInstance> T newInstance(Player player) {
        return (T) new MenuInstance(this, player);
    }

    protected <T extends MenuInstance> T addInstance(Player player) {
        T inst = newInstance(player);
        if (inst != null)
            menuInstances.put(player.getUniqueId(), inst);
        return (T) inst;
    }

    protected void removeInstance(Player player) {
        menuInstances.remove(player.getUniqueId());
    }

    /**
     * Override to modify the menu of a specific player, such as
     * modifying certain items to contain a personalized name or
     * tooltip per-player.
     **/
    protected abstract void modifyInstance(Player player);

    /**
     * Returns true if the provided entity is a player and if the specified inventory is that of the player's
     * menu instance.
     */
    protected boolean isMenu(HumanEntity ent, Inventory inventory) {
        if (!(ent instanceof Player))
            return false;
        Player p = (Player) ent;
        return hasInstanceOpen(p) && getInstance(p).getMenuInventory().equals(inventory);
    }

    /**
     * Class containing information for each instance of a menu opened by a player.
     */
    public class MenuInstance {
        /** The {@link org.bukkit.entity.Player} that opened this menu. */
        protected final Player accessor;
        /** The {@link com.github.Dewynion.embercore.gui.menu.InventoryMenu} that this instance is based off of. */
        protected final InventoryMenu parent;
        protected String title;
        protected Inventory menuInventory;

        public MenuInstance(InventoryMenu parent, Player accessor) {
            this.accessor = accessor;
            this.parent = parent;
            this.title = parent.getTitle();
            setupInventory();
        }

        /**
         * Creates an {@link Inventory} owned by the {@link MenuInstance#accessor} of this menu instance
         * with {@link MenuInstance#parent#menuContents} as its contents.
         */
        protected void setupInventory() {
            menuInventory = Bukkit.createInventory(accessor, parent.menuContents.length, title);
            menuInventory.setContents(menuContents);
        }

        public void open() {
            accessor.closeInventory();
            accessor.openInventory(menuInventory);
        }

        public Player getAccessor() {
            return accessor;
        }

        public Inventory getMenuInventory() {
            return menuInventory;
        }

        public <T extends InventoryMenu> T getParent() {
            return (T) parent;
        }
    }

    public class MenuEventListener implements Listener {
        @EventHandler
        public void cancelClicks(InventoryClickEvent event) {
            event.setCancelled(isMenu(event.getWhoClicked(), event.getInventory()));
        }

        @EventHandler
        public void onClose(InventoryCloseEvent event) {
            if (isMenu(event.getPlayer(), event.getInventory())) {
                close((Player) event.getPlayer());
            }
        }
    }
}
