package com.github.Dewynion.embercore.gui;

import com.github.Dewynion.embercore.EmberCore;
import com.github.Dewynion.embercore.util.InventoryUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.NoSuchElementException;

public class InventoryCreator {
    /** The default char to use for empty slots in the inventory. */
    public static final char DEFAULT_EMPTY_SLOT = '0';
    /** The default material to fill empty inventory slots with. */
    public static final Material DEFAULT_EMPTY_MATERIAL = Material.AIR;

    /**
     * See {@link #inventoryFromString(InventoryHolder, String, String, HashMap)}. Called with no title.
     */
    public static Inventory inventoryFromString(InventoryHolder holder, String invString, HashMap<Character,
            ItemStack> charMap) {
        return inventoryFromString(holder, "", invString, charMap);
    }

    /**
     * <p>Returns an {@link Inventory} with the provided owner and title, with contents populated by mapping the given
     * set of characters to a corresponding itemstack.</p>
     *
     * <p>Example:</p>
     * <p>"000010000"</p>
     * <p>map.add('1', new ItemStack(Material.WOOD));</p>
     * <p>will produce a single-row inventory with a single block of wood in the middle.</p>
     *
     * @param holder The owner of the inventory.
     * @param title The title of the inventory window.
     * @param invString A string containing characters present in charMap's key set or {@link #DEFAULT_EMPTY_SLOT}.
     *                  Each char will insert the corresponding itemstack at its position in the inventory. Anything
     *                  past index {@link InventoryUtil#INVENTORY_SIZE} will be ignored.
     * @param charMap Maps chars to ItemStacks. If a character is found in invString that is not present here, throws
     *                a {@link NoSuchElementException}.
     * @return An Inventory containing the provided items at the given positions according to invString.
     */
    public static Inventory inventoryFromString(InventoryHolder holder, String title,
                                                String invString, HashMap<Character, ItemStack> charMap) {
        Inventory inv = Bukkit.createInventory(holder, invString.length(), title);
        char[] invArr = invString.toCharArray();
        ItemStack[] contents = new ItemStack[inv.getSize()];
        for (int i = 0; i < inv.getSize(); i++) {
            if (i > InventoryUtil.INVENTORY_SIZE) {
                EmberCore.getInstance().getLogger().warning("InventoryCreator::inventoryFromString(): " +
                        "provided string is longer than " + InventoryUtil.INVENTORY_SIZE + " characters. Aborting.");
                break;
            }
            char c = invArr[i];
            if (!charMap.containsKey(c)) {
                if (c != DEFAULT_EMPTY_SLOT)
                    throw new NoSuchElementException("EmberCore::InventoryCreator::inventoryFromString(): char " +
                            c + " not present in dict.");
                else
                    contents[i] = new ItemStack(DEFAULT_EMPTY_MATERIAL);
            } else
                contents[i] = charMap.get(c);
        }
        inv.setContents(contents);
        return inv;
    }
}
