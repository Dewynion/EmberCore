package com.github.Dewynion.embercore.reflection.wrapper.entity;

import com.github.Dewynion.embercore.reflection.Versioning;
import com.github.Dewynion.embercore.reflection.wrapper.NmsClassWrapper;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;

public class WEntityPlayer extends NmsClassWrapper {
    private static Class<?> nmsClass;
    static {
        nmsClass = Versioning.getNMSClass("EntityPlayer");
    }

    public WEntityPlayer(Player player) {
        WCraftPlayer wcp = new WCraftPlayer(player);
        object = wcp.getHandle();
        Validate.notNull(object, "WEntityPlayer: could not instantiate NMS object.");
    }
}
