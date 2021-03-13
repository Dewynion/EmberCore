package com.github.Dewynion.embercore;

public class CoreLoadPriority {
    public static final int CONFIG_MANAGER_PRIORITY = 0;
    public static final int PROJECTILE_REGISTRY_PRIORITY = CONFIG_MANAGER_PRIORITY + 1;
    public static final int PLUGIN_EVENT_LISTENER_PRIORITY = PROJECTILE_REGISTRY_PRIORITY + 1;
}
