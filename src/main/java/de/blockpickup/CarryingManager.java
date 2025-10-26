package de.blockpickup;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.BlockState;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.EulerAngle;
import org.bukkit.util.Vector;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manager für das Tragen von Blöcken und Entities (CarryOn-Style)
 */
public class CarryingManager {

    private final BlockPickupPlugin plugin;

    // Speichert welcher Spieler was trägt
    private final Map<UUID, CarriedObject> carryingPlayers = new HashMap<>();

    // Armor Stands für visuelle Darstellung
    private final Map<UUID, ArmorStand> visualDisplays = new HashMap<>();

    // Task IDs für Display Updates
    private final Map<UUID, Integer> displayUpdateTasks = new HashMap<>();

    public CarryingManager(BlockPickupPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Prüft ob ein Spieler gerade etwas trägt
     */
    public boolean isCarrying(Player player) {
        return carryingPlayers.containsKey(player.getUniqueId());
    }

    /**
     * Spieler hebt ein Entity auf
     */
    public void pickupEntity(Player player, Entity entity) {
        if (isCarrying(player)) {
            return;
        }

        // Speichere Entity-Daten
        CarriedObject carried = new CarriedObject(entity);
        carryingPlayers.put(player.getUniqueId(), carried);

        // Entferne Original-Entity
        entity.remove();

        // Erstelle visuelle Darstellung
        createVisualDisplay(player, entity);

        // Verlangsame Spieler
        applySlowness(player);
    }

    /**
     * Spieler hebt einen Block auf
     */
    public void pickupBlock(Player player, BlockState blockState, Location location) {
        if (isCarrying(player)) {
            return;
        }

        // Speichere Block-Daten
        CarriedObject carried = new CarriedObject(blockState, location);
        carryingPlayers.put(player.getUniqueId(), carried);

        // Entferne Original-Block
        location.getBlock().setType(Material.AIR);

        // Erstelle visuelle Darstellung
        createVisualDisplay(player, blockState.getType());

        // Verlangsame Spieler
        applySlowness(player);
    }

    /**
     * Spieler legt das getragene Objekt ab
     */
    public void placeCarried(Player player, Location location) {
        if (!isCarrying(player)) {
            return;
        }

        CarriedObject carried = carryingPlayers.get(player.getUniqueId());

        if (carried.isEntity()) {
            // Spawne Entity wieder
            carried.spawnEntity(location);
        } else {
            // Platziere Block wieder
            carried.placeBlock(location);
        }

        // Entferne visuelle Darstellung
        removeVisualDisplay(player);

        // Entferne Slowness
        removeSlowness(player);

        // Entferne aus Map
        carryingPlayers.remove(player.getUniqueId());
    }

    /**
     * Spieler droppt das getragene Objekt (z.B. bei Tod oder Disconnect)
     */
    public void dropCarried(Player player) {
        if (!isCarrying(player)) {
            return;
        }

        // Platziere an Spieler-Position
        placeCarried(player, player.getLocation());
    }

    /**
     * Erstellt die visuelle Darstellung für Entity
     */
    private void createVisualDisplay(Player player, Entity entity) {
        ArmorStand display = player.getWorld().spawn(
            player.getEyeLocation().add(player.getLocation().getDirection().multiply(0.5)),
            ArmorStand.class
        );

        display.setVisible(false);
        display.setGravity(false);
        display.setMarker(true);
        display.setSmall(true);
        display.setInvulnerable(true);
        display.setCustomName(entity.getName());
        display.setCustomNameVisible(true);

        // Versuche Entity-Kopf anzuzeigen (funktioniert bei einigen Entities)
        // Für volle Entity-Darstellung müssten wir Packets verwenden

        visualDisplays.put(player.getUniqueId(), display);

        // Update Position kontinuierlich
        startDisplayUpdate(player);
    }

    /**
     * Erstellt die visuelle Darstellung für Block
     */
    private void createVisualDisplay(Player player, Material blockType) {
        ArmorStand display = player.getWorld().spawn(
            player.getEyeLocation().add(player.getLocation().getDirection().multiply(0.5)),
            ArmorStand.class
        );

        display.setVisible(false);
        display.setGravity(false);
        display.setMarker(true);
        display.setSmall(true);
        display.setInvulnerable(true);

        // Setze Block als Helm (wird über Kopf angezeigt)
        display.getEquipment().setHelmet(new ItemStack(blockType));
        display.setHeadPose(new EulerAngle(0, 0, 0));

        visualDisplays.put(player.getUniqueId(), display);

        // Update Position kontinuierlich
        startDisplayUpdate(player);
    }

    /**
     * Startet kontinuierliches Update der Display-Position
     */
    private void startDisplayUpdate(Player player) {
        // Stoppe existierenden Task falls vorhanden
        stopDisplayUpdate(player);

        int taskId = plugin.getServer().getScheduler().runTaskTimer(plugin, () -> {
            if (!isCarrying(player)) {
                stopDisplayUpdate(player);
                return;
            }

            ArmorStand display = visualDisplays.get(player.getUniqueId());
            if (display == null || !display.isValid()) {
                stopDisplayUpdate(player);
                return;
            }

            // Position vor dem Spieler auf Kopfhöhe
            Vector direction = player.getLocation().getDirection();
            Location displayLoc = player.getEyeLocation()
                .add(direction.multiply(0.5))
                .add(0, 0.5, 0);

            display.teleport(displayLoc);

        }, 0L, 1L).getTaskId(); // Update jede Tick

        displayUpdateTasks.put(player.getUniqueId(), taskId);
    }

    /**
     * Stoppt das Display-Update für einen Spieler
     */
    private void stopDisplayUpdate(Player player) {
        Integer taskId = displayUpdateTasks.remove(player.getUniqueId());
        if (taskId != null) {
            plugin.getServer().getScheduler().cancelTask(taskId);
        }
    }

    /**
     * Entfernt die visuelle Darstellung
     */
    private void removeVisualDisplay(Player player) {
        stopDisplayUpdate(player);
        ArmorStand display = visualDisplays.remove(player.getUniqueId());
        if (display != null && display.isValid()) {
            display.remove();
        }
    }

    /**
     * Verlangsamt den Spieler
     */
    private void applySlowness(Player player) {
        player.setWalkSpeed(0.1f); // Normal ist 0.2f
    }

    /**
     * Entfernt Verlangsamung
     */
    private void removeSlowness(Player player) {
        player.setWalkSpeed(0.2f); // Zurück zu normal
    }

    /**
     * Cleanup beim Plugin-Disable
     */
    public void cleanup() {
        // Alle Spieler lassen ihre getragenen Objekte fallen
        for (UUID uuid : new HashMap<>(carryingPlayers).keySet()) {
            Player player = plugin.getServer().getPlayer(uuid);
            if (player != null) {
                dropCarried(player);
            }
        }

        // Stoppe alle Display-Update Tasks
        for (Integer taskId : displayUpdateTasks.values()) {
            plugin.getServer().getScheduler().cancelTask(taskId);
        }

        // Entferne alle Displays
        for (ArmorStand display : visualDisplays.values()) {
            if (display.isValid()) {
                display.remove();
            }
        }

        carryingPlayers.clear();
        visualDisplays.clear();
        displayUpdateTasks.clear();
    }

    /**
     * Klasse zum Speichern der getragenen Objekt-Daten
     */
    public static class CarriedObject {
        private final boolean isEntity;

        // Entity-Daten
        private String entitySerializedData;
        private org.bukkit.entity.EntityType entityType;

        // Block-Daten
        private BlockState blockState;
        private Location originalLocation;

        // Constructor für Entity
        public CarriedObject(Entity entity) {
            this.isEntity = true;
            this.entityType = entity.getType();

            // Serialisiere ALLE Entity-Daten als NBT/String
            this.entitySerializedData = de.blockpickup.utils.NBTUtils.serializeCompleteEntity(entity);
        }

        // Constructor für Block
        public CarriedObject(BlockState blockState, Location location) {
            this.isEntity = false;
            this.blockState = blockState;
            this.originalLocation = location;
        }

        public boolean isEntity() {
            return isEntity;
        }

        public void spawnEntity(Location location) {
            if (entityType != null && entitySerializedData != null) {
                // Spawne Entity an neuer Position
                Entity entity = location.getWorld().spawnEntity(location, entityType);

                // Lade ALLE gespeicherten Daten zurück (sofort)
                de.blockpickup.utils.NBTUtils.deserializeCompleteEntity(entity, entitySerializedData);

                // Manche Eigenschaften brauchen einen Tick um richtig geladen zu werden
                BlockPickupPlugin.getInstance().getServer().getScheduler().runTaskLater(
                    BlockPickupPlugin.getInstance(),
                    () -> {
                        // Lade Daten nochmal nach 1 Tick für Sicherheit
                        de.blockpickup.utils.NBTUtils.deserializeCompleteEntity(entity, entitySerializedData);
                    },
                    1L
                );
            }
        }

        public void placeBlock(Location location) {
            if (blockState != null) {
                // Setze Block-Typ
                location.getBlock().setType(blockState.getType());

                // Warte 1 Tick damit Block richtig geladen ist
                BlockPickupPlugin.getInstance().getServer().getScheduler().runTaskLater(
                    BlockPickupPlugin.getInstance(),
                    () -> {
                        // Kopiere BlockState-Daten (inkl. Inventar!)
                        BlockState newState = location.getBlock().getState();

                        // Wenn Container: Kopiere Inventar und Daten
                        if (blockState instanceof org.bukkit.block.Container sourceContainer &&
                            newState instanceof org.bukkit.block.Container targetContainer) {

                            // Kopiere alle Items
                            for (int i = 0; i < sourceContainer.getInventory().getSize(); i++) {
                                org.bukkit.inventory.ItemStack item = sourceContainer.getInventory().getItem(i);
                                if (item != null) {
                                    targetContainer.getInventory().setItem(i, item.clone());
                                }
                            }

                            // Kopiere Custom Name
                            if (sourceContainer.getCustomName() != null) {
                                targetContainer.setCustomName(sourceContainer.getCustomName());
                            }

                            // Spezielle Daten für Öfen (WICHTIG: Damit Schmelzen weitergeht!)
                            if (blockState instanceof org.bukkit.block.Furnace sourceFurnace &&
                                newState instanceof org.bukkit.block.Furnace targetFurnace) {

                                // Setze alle Ofen-Zeiten
                                targetFurnace.setBurnTime(sourceFurnace.getBurnTime());
                                targetFurnace.setCookTime(sourceFurnace.getCookTime());
                                targetFurnace.setCookTimeTotal(sourceFurnace.getCookTimeTotal());

                                // Update Ofen SOFORT damit er weiterschmilzt
                                targetFurnace.update(true, false);
                            }

                            // Spezielle Daten für Brewing Stands
                            if (blockState instanceof org.bukkit.block.BrewingStand sourceBrewingStand &&
                                newState instanceof org.bukkit.block.BrewingStand targetBrewingStand) {
                                targetBrewingStand.setBrewingTime(sourceBrewingStand.getBrewingTime());
                                targetBrewingStand.setFuelLevel(sourceBrewingStand.getFuelLevel());
                                targetBrewingStand.update(true, false);
                            }

                            // Update Container (für alle anderen Container-Typen)
                            if (!(newState instanceof org.bukkit.block.Furnace) &&
                                !(newState instanceof org.bukkit.block.BrewingStand)) {
                                targetContainer.update(true, false);
                            }
                        } else {
                            // Kein Container: Normales Update
                            newState.update(true, false);
                        }
                    },
                    1L
                );
            }
        }
    }
}
