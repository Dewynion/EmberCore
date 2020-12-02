package com.github.Dewynion.embercore.reflection.wrapper.entity;

import com.github.Dewynion.embercore.EmberCore;
import com.github.Dewynion.embercore.reflection.Versioning;
import com.github.Dewynion.embercore.reflection.wrapper.NmsClassWrapper;
import org.bukkit.entity.Player;

import java.lang.reflect.Method;
import java.util.logging.Level;

public class WCraftPlayer extends NmsClassWrapper {
    private static Class<?> nmsClass;
    private static Method handleMethod;

    static {
        nmsClass = Versioning.getCraftbukkitClass("entity.CraftPlayer");
        try {
            handleMethod = nmsClass.getMethod("getHandle");
        } catch (Exception e) {
            EmberCore.log(Level.SEVERE, "WCraftPlayer: static declaration failed.");
            e.printStackTrace();
        }
    }

    public WCraftPlayer(Player player) {
        object = player;
    }

    public Object getHandle() throws NullPointerException {
        try {
            return handleMethod.invoke(object);
        } catch (Exception e) {
            EmberCore.log(Level.SEVERE, "WCraftPlayer::getHandle(): Failed to invoke method.");
            e.printStackTrace();
            return null;
        }
    }
}
