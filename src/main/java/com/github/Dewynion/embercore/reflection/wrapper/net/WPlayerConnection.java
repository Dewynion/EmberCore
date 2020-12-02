package com.github.Dewynion.embercore.reflection.wrapper.net;

import com.github.Dewynion.embercore.reflection.Versioning;
import com.github.Dewynion.embercore.reflection.wrapper.NmsClassWrapper;

public class WPlayerConnection extends NmsClassWrapper {
    private static Class<?> nmsClass;
    static {
        nmsClass = Versioning.getNMSClass("PlayerConnection");
    }
}
