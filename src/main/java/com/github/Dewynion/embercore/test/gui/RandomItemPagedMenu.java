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

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Singleton(priority = CoreLoadPriority.MENU_PRIORITY)
public class RandomItemPagedMenu extends MultiPageMenu {
    public RandomItemPagedMenu() {
        super("Random Items");
        List<ItemStack> contents = new ArrayList<ItemStack>();
        Random rand = new Random();
        int matLen = Material.values().length;
        for (int i = 0; i < 100 + rand.nextInt(200); i++) {
            Material mat = Material.values()[rand.nextInt(matLen)];
            if (mat.isAir())
                continue;
            contents.add(new ItemStack(mat));
        }
        next = new ItemStack(Material.EGG);
        ItemMeta meta = next.getItemMeta();
        meta.setDisplayName(ChatColor.GREEN + "Next Page");
        next.setItemMeta(meta);
        setContents(contents);
        Bukkit.getServer().getPluginManager().registerEvents(new CluckCluck(), EmberCore.getInstance());
    }

    protected void onOpenMenu(Player player) {

    }

    @Override
    protected void onCloseMenu(Player player) {

    }

    @Override
    protected void modifyInstance(Player player) {
        MultiPageMenuInstance inst = getInstance(player);
        if (inst == null)
            return;
        for (MenuPage page : inst.getPages()) {
            for (ItemStack i : page.getContents()) {
                if (i == null)
                    continue;
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
                loc.getWorld().playSound(loc, Sound.ENTITY_CHICKEN_HURT, 1.0f, 1.0f);
            }
        }
    }
}
