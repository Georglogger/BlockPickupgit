package de.blockpickup;

import org.bukkit.Material;
import org.bukkit.entity.EntityType;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ConfigManager {

    private final BlockPickupPlugin plugin;
    private Set<Material> allowedBlocks;
    private Set<EntityType> allowedEntities;
    private boolean requireSneak;
    private boolean requireEmptyHand;
    private boolean blocksEnabled;
    private boolean entitiesEnabled;

    public ConfigManager(BlockPickupPlugin plugin) {
        this.plugin = plugin;
        loadConfig();
    }

    private void loadConfig() {
        requireSneak = plugin.getConfig().getBoolean("require-sneak", true);
        requireEmptyHand = plugin.getConfig().getBoolean("require-empty-hand", false);
        blocksEnabled = plugin.getConfig().getBoolean("blocks.enabled", true);
        entitiesEnabled = plugin.getConfig().getBoolean("entities.enabled", true);

        // Lade erlaubte Block-Typen
        allowedBlocks = new HashSet<>();
        List<String> blockTypes = plugin.getConfig().getStringList("blocks.allowed-types");
        for (String type : blockTypes) {
            try {
                Material material = Material.valueOf(type.toUpperCase());
                allowedBlocks.add(material);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Unbekannter Block-Typ in config.yml: " + type);
            }
        }

        // Lade erlaubte Entity-Typen
        allowedEntities = new HashSet<>();
        List<String> entityTypes = plugin.getConfig().getStringList("entities.allowed-types");
        for (String type : entityTypes) {
            try {
                EntityType entityType = EntityType.valueOf(type.toUpperCase());
                allowedEntities.add(entityType);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Unbekannter Entity-Typ in config.yml: " + type);
            }
        }
    }

    public boolean isBlockAllowed(Material material) {
        return blocksEnabled && allowedBlocks.contains(material);
    }

    public boolean isEntityAllowed(EntityType entityType) {
        return entitiesEnabled && allowedEntities.contains(entityType);
    }

    public boolean requiresSneak() {
        return requireSneak;
    }

    public boolean requiresEmptyHand() {
        return requireEmptyHand;
    }

    public boolean areBlocksEnabled() {
        return blocksEnabled;
    }

    public boolean areEntitiesEnabled() {
        return entitiesEnabled;
    }
}
