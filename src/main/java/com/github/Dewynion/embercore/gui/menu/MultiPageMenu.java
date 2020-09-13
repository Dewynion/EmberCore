package com.github.Dewynion.embercore.gui.menu;

import com.github.Dewynion.embercore.EmberCore;
import com.github.Dewynion.embercore.util.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.DyeColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.material.Wool;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;

public abstract class MultiPageMenu extends InventoryMenu {
    public static final int NEXT_INDEX = InventoryUtil.indexOfSlot(InventoryUtil.LAST_ROW_INDEX,
            InventoryUtil.LAST_COL_INDEX - 3);
    public static final int PREV_INDEX = InventoryUtil.indexOfSlot(InventoryUtil.LAST_ROW_INDEX,
            3);
    public static final ItemStack DEFAULT_NEXT = new Wool(DyeColor.GREEN).toItemStack(1);
    public static final ItemStack DEFAULT_PREVIOUS = new Wool(DyeColor.RED).toItemStack(1);
    static {
        ItemMeta nextMeta = DEFAULT_NEXT.getItemMeta();
        nextMeta.setDisplayName(ChatColor.GREEN + "Next Page");
        DEFAULT_NEXT.setItemMeta(nextMeta);
        ItemMeta prevMeta = DEFAULT_PREVIOUS.getItemMeta();
        prevMeta.setDisplayName(ChatColor.RED + "Previous Page");
        DEFAULT_PREVIOUS.setItemMeta(prevMeta);
    }

    protected final List<MenuPage> pages;

    public ItemStack next, previous;

    public MultiPageMenu(String title) {
        super(title);
        pages = new ArrayList<>();
        menuInstances = new HashMap<>();
        next = DEFAULT_NEXT.clone();
        previous = DEFAULT_PREVIOUS.clone();
        Bukkit.getServer().getPluginManager().registerEvents(new PageSwitchListener(), EmberCore.getInstance());
    }

    public MenuPage getPage(int index) {
        return pages.get(index);
    }

    public int numPages() {
        return pages.size();
    }

    public void addContents(List<ItemStack> contents) {
        // menupage adds itself and will keep recursively adding new pages until
        // contents is empty.
        new MenuPage(this, pages.size(), contents);
    }

    public void setContents(List<ItemStack> contents) {
        clear();
        addContents(contents);
    }

    public void clear() {
        pages.clear();
    }

    protected <T extends MenuInstance> T newInstance(Player player) {
        return (T) new MultiPageMenuInstance(this, player);
    }

    public class MenuPage {
        private ItemStack[] contents;
        private MultiPageMenu parent;
        private int pageNumber;

        public MenuPage(MultiPageMenu parent, int pageNumber, List<ItemStack> contents) {
            this.parent = parent;
            this.pageNumber = pageNumber;
            int index = 0;
            int contentSize = InventoryUtil.INVENTORY_SIZE;
            this.contents = new ItemStack[contentSize];
            LinkedList<ItemStack> itemQueue = new LinkedList<>(contents);
            for (int i = 0; i < contents.size(); i++) {
                // if it's in the last row, move the remainder of the queue to another page to leave room
                // for the navigation buttons.
                if (i >= InventoryUtil.LAST_ROW_INDEX)
                    break;
                this.contents[index++] = itemQueue.poll();
            }
            parent.pages.add(this);
            if (!itemQueue.isEmpty())
                parent.addContents(itemQueue);
        }

        public MultiPageMenu getParent() {
            return parent;
        }

        public int getPageNumber() {
            return pageNumber;
        }

        /** This does allow for modification on the fly. */
        public ItemStack[] getContents() {
            return contents;
        }
    }

    public class MultiPageMenuInstance extends MenuInstance {
        protected final List<MenuPage> pages;
        protected final MultiPageMenu pagedParent;
        protected int page = 0;
        public MultiPageMenuInstance(MultiPageMenu parent, Player player) {
            super(parent, player);
            pagedParent = parent;
            pages = new ArrayList<>(parent.pages);
        }

        public void open() {
            openPage(0);
        }

        public void openPage(int page) {
            menuInventory = Bukkit.createInventory(accessor, InventoryUtil.INVENTORY_SIZE,
                    title + " p. " + (page + 1));
            ItemStack[] contents = pages.get(page).contents;
            if (!isLastPage())
                contents[NEXT_INDEX] = pagedParent.next.clone();
            if (!isFirstPage())
                contents[PREV_INDEX] = pagedParent.previous.clone();
            menuInventory.setContents(contents);
            accessor.openInventory(menuInventory);
        }

        public void nextPage() {
            page += isLastPage() ? 0 : 1;
            openPage(page);
        }

        public void prevPage() {
            page -= isFirstPage() ? 0 : 1;
            openPage(page);
        }

        private boolean isFirstPage() {
            return page == 0;
        }

        private boolean isLastPage() {
            return page == pagedParent.numPages() - 1;
        }

        public List<MenuPage> getPages() {
            return pages;
        }
    }

    public class PageSwitchListener implements Listener {
        @EventHandler
        public void navigatePages(InventoryClickEvent event) {
            if (!isMenu(event.getWhoClicked(), event.getInventory()))
                return;
            ItemStack item = event.getCurrentItem();
            MultiPageMenuInstance inst = getInstance((Player) event.getWhoClicked());
            // ignore if the clicked item is nothing
            if (item == null)
                return;
            // make sure to check in case someone overrides next/previous with nothing in a subclass.
            if (item.equals(next))
                inst.nextPage();
            else if (item.equals(previous))
                inst.prevPage();
        }
    }
}
