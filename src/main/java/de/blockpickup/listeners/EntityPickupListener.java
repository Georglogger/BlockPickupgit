package de.blockpickup.listeners;

import de.blockpickup.BlockPickupPlugin;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Listener für das Aufheben und Platzieren von Entities (CarryOn-Style)
 * Entities werden NICHT zu Spawn Eggs, sondern visuell getragen
 */
public class EntityPickupListener implements Listener {

    private final BlockPickupPlugin plugin;

    public EntityPickupListener(BlockPickupPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Entity aufheben (Shift + Rechtsklick)
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityInteract(PlayerInteractEntityEvent event) {
        // Nur für rechte Hand
        if (event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        Entity entity = event.getRightClicked();

        // Prüfe ob Entities aktiviert sind
        if (!plugin.getConfigManager().areEntitiesEnabled()) {
            return;
        }

        // Prüfe ob es ein erlaubter Entity-Typ ist
        if (!plugin.getConfigManager().isEntityAllowed(entity.getType())) {
            return;
        }

        // Prüfe Permission
        if (!player.hasPermission("blockpickup.pickup.entity")) {
            return;
        }

        // Prüfe ob Spieler sneaken muss
        if (plugin.getConfigManager().requiresSneak() && !player.isSneaking()) {
            return;
        }

        // Prüfe ob Hand leer sein muss
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (plugin.getConfigManager().requiresEmptyHand() && mainHand.getType() != Material.AIR) {
            player.sendMessage(plugin.getMessage("must-empty-hand"));
            return;
        }

        // Prüfe ob Spieler bereits etwas trägt
        if (plugin.getCarryingManager().isCarrying(player)) {
            player.sendMessage(plugin.getMessage("already-carrying"));
            return;
        }

        // Hebe Entity auf (CarryOn-Style: visuelle Darstellung, kein Item)
        plugin.getCarryingManager().pickupEntity(player, entity);
        player.sendMessage(plugin.getMessage("pickup-success"));

        event.setCancelled(true);
    }

    /**
     * Getragenes Objekt platzieren (Rechtsklick auf Block)
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlaceCarried(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        // Nur Rechtsklick
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) {
            return;
        }

        // Prüfe ob Spieler etwas trägt
        if (!plugin.getCarryingManager().isCarrying(player)) {
            return;
        }

        // Prüfe ob auf Block geklickt wurde
        if (event.getClickedBlock() == null) {
            return;
        }

        // Platziere getragenes Objekt auf dem Block
        var location = event.getClickedBlock().getLocation().add(0, 1, 0);
        plugin.getCarryingManager().placeCarried(player, location);

        player.sendMessage(plugin.getMessage("place-success"));
        event.setCancelled(true);
    }

    /**
     * Getragenes Objekt droppen beim Sneaken (falls nicht mehr gewünscht)
     */
    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerSneak(PlayerToggleSneakEvent event) {
        Player player = event.getPlayer();

        // Nur wenn Spieler anfängt zu sneaken UND etwas trägt
        if (!event.isSneaking()) {
            return;
        }

        if (!plugin.getCarryingManager().isCarrying(player)) {
            return;
        }

        // Wenn Spieler sneakt während er trägt UND nichts in der Hand hat
        // -> Lege Objekt ab
        ItemStack mainHand = player.getInventory().getItemInMainHand();
        if (mainHand.getType() == Material.AIR) {
            plugin.getCarryingManager().dropCarried(player);
            player.sendMessage(plugin.getMessage("dropped-carried"));
        }
    }

    /**
     * Getragenes Objekt droppen beim Verlassen
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (plugin.getCarryingManager().isCarrying(player)) {
            plugin.getCarryingManager().dropCarried(player);
        }
    }

    /**
     * Getragenes Objekt droppen beim Tod
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        if (plugin.getCarryingManager().isCarrying(player)) {
            plugin.getCarryingManager().dropCarried(player);
        }
    }
}
