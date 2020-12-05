package com.github.Dewynion.embercore.reflection.wrapper.entity;

import com.github.Dewynion.embercore.EmberCore;
import com.github.Dewynion.embercore.reflection.Versioning;
import com.github.Dewynion.embercore.reflection.wrapper.NmsClassWrapper;
import org.apache.commons.lang.Validate;
import org.bukkit.entity.Player;

import javax.annotation.Nonnull;
import java.lang.reflect.Field;
import java.util.logging.Level;

public class WEntityPlayer extends NmsClassWrapper {
    private static Class<?> nmsClass;
    private static Field pingField;

    static {
        nmsClass = Versioning.getNMSClass("EntityPlayer");
        try {
            pingField = nmsClass.getField("ping");
            pingField.setAccessible(true);
        } catch (Exception ex) {
            EmberCore.log(Level.SEVERE, ex.getMessage());
            ex.printStackTrace();
        }
    }

    private Player player;

    public WEntityPlayer(@Nonnull Player player) {
        this.player = player;
        WCraftPlayer wcp = new WCraftPlayer(player);
        object = wcp.getHandle();
        Validate.notNull(object, "WEntityPlayer: could not instantiate NMS object.");
    }

    public int getPing() {
        try {
            return pingField.getInt(object);
        } catch (IllegalAccessException ex) {
            EmberCore.log(Level.WARNING, String.format("Unable to retrieve player ping for player %s (UID %s)",
                    player.getDisplayName(), player.getUniqueId()));
            return -1;
        }
    }
}
