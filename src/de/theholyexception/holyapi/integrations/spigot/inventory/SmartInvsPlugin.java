package de.theholyexception.holyapi.integrations.spigot.inventory;

import org.bukkit.plugin.java.JavaPlugin;

public class SmartInvsPlugin {

    private static SmartInvsPlugin instance;
    private static InventoryManager invManager;

    public void onEnable(JavaPlugin plugin) {
        instance = this;

        invManager = new InventoryManager(plugin);
        invManager.init();
    }

    public static InventoryManager manager() { return invManager; }
    public static SmartInvsPlugin instance() { return instance; }

}
