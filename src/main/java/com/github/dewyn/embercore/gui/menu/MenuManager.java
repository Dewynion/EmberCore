package com.github.dewyn.embercore.gui.menu;

import com.github.dewyn.embercore.CoreLoadPriority;
import com.github.dewyn.embercore.reflection.Singleton;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;

@Singleton(priority = CoreLoadPriority.MENU_MANAGER_PRIORITY)
public class MenuManager {
    private static MenuManager instance;
    public static MenuManager getInstance() {
        return instance;
    }

    private Set<InventoryMenu> menus;

    public MenuManager() {
        instance = this;
        menus = new HashSet<>();
    }

    public void registerMenu(InventoryMenu menu) {
        menus.add(menu);
    }

    public InventoryMenu bySimpleClassName(String name) {
        for (InventoryMenu menu : menus) {
            if (menu.getClass().getSimpleName().equalsIgnoreCase(name))
                return menu;
        }
        return null;
    }

    public InventoryMenu byClass(Class<? extends InventoryMenu> clazz) {
        for (InventoryMenu menu : menus) {
            if (menu.getClass().equals(clazz))
                return menu;
        }
        return null;
    }

    public void openMenu(Player player, InventoryMenu menu) {
        menu.open(player);
    }
}
