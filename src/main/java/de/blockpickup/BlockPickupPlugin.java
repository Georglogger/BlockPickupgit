package de.blockpickup;

import de.blockpickup.commands.BlockPickupCommand;
import de.blockpickup.listeners.BlockPickupListener;
import de.blockpickup.listeners.EntityPickupListener;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class BlockPickupPlugin extends JavaPlugin {

    private static BlockPickupPlugin instance;
    private ConfigManager configManager;
    private CarryingManager carryingManager;

    @Override
    public void onEnable() {
        instance = this;

        // Konfiguration laden
        saveDefaultConfig();
        configManager = new ConfigManager(this);

        // CarryingManager initialisieren (für CarryOn-Style)
        carryingManager = new CarryingManager(this);

        // Listener registrieren
        getServer().getPluginManager().registerEvents(new BlockPickupListener(this), this);
        getServer().getPluginManager().registerEvents(new EntityPickupListener(this), this);

        // Commands registrieren
        getCommand("blockpickup").setExecutor(new BlockPickupCommand(this));

        getLogger().info("BlockPickup wurde aktiviert (CarryOn-Style)!");
    }

    @Override
    public void onDisable() {
        // Cleanup: Alle getragenen Objekte droppen
        if (carryingManager != null) {
            carryingManager.cleanup();
        }

        getLogger().info("BlockPickup wurde deaktiviert!");
    }

    public static BlockPickupPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public CarryingManager getCarryingManager() {
        return carryingManager;
    }

    public String getMessage(String key) {
        String message = getConfig().getString("messages." + key, key);
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public void reloadPluginConfig() {
        reloadConfig();
        configManager = new ConfigManager(this);
    }
}
