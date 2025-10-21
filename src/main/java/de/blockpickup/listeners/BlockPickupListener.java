package de.blockpickup.listeners;

import de.blockpickup.BlockPickupPlugin;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.Container;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

/**
 * Listener für das Aufheben und Platzieren von Blöcken (CarryOn-Style)
 * Blöcke behalten ihren Inventar-Inhalt und werden visuell getragen
 * Verwendet Shift+Rechtsklick um Blöcke aufzuheben (ohne sie zu öffnen)
 */
public class BlockPickupListener implements Listener {

    private final BlockPickupPlugin plugin;

    public BlockPickupListener(BlockPickupPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Aufheben von Blöcken beim Sneaken (egal welche Interaktion)
     */
    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = false)
    public void onBlockInteract(PlayerInteractEvent event) {
        // Nur Interaktionen mit Block (Rechtsklick oder Linksklick)
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK &&
            event.getAction() != Action.LEFT_CLICK_BLOCK) {
            return;
        }

        // Nur für Haupthand
        if (event.getHand() != null && event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        Player player = event.getPlayer();
        Block block = event.getClickedBlock();

        if (block == null) {
            return;
        }

        Material blockType = block.getType();

        // Prüfe ob es ein erlaubter Block ist
        if (!plugin.getConfigManager().isBlockAllowed(blockType)) {
            return;
        }

        // WICHTIG: Nur wenn Spieler sneakt!
        if (!player.isSneaking()) {
            return; // Nicht sneakend = normale Interaktion
        }

        // Ab hier: Spieler sneakt -> Will IMMER aufheben!

        // Prüfe Permission
        if (!player.hasPermission("blockpickup.pickup.block")) {
            player.sendMessage(plugin.getMessage("no-permission"));
            event.setCancelled(true);
            return;
        }

        // Prüfe spezifische Permission für Block-Typ
        String blockName = blockType.name().toLowerCase();
        if (!player.hasPermission("blockpickup.pickup." + blockName)) {
            player.sendMessage(plugin.getMessage("no-permission"));
            event.setCancelled(true);
            return;
        }

        // Prüfe ob Hand leer sein muss
        if (plugin.getConfigManager().requiresEmptyHand()) {
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            if (mainHand.getType() != Material.AIR) {
                player.sendMessage(plugin.getMessage("must-empty-hand"));
                event.setCancelled(true);
                return;
            }
        }

        // Prüfe ob Spieler bereits etwas trägt
        if (plugin.getCarryingManager().isCarrying(player)) {
            player.sendMessage(plugin.getMessage("already-carrying"));
            event.setCancelled(true);
            return;
        }

        // Prüfe ob Block ein Container ist
        BlockState state = block.getState();
        if (!(state instanceof Container container)) {
            return;
        }

        // WICHTIG: Event cancellen damit Block NICHT geöffnet wird!
        event.setCancelled(true);

        // Wenn Creative Mode, entferne Block einfach
        if (player.getGameMode() == GameMode.CREATIVE) {
            block.setType(Material.AIR);
            player.sendMessage(plugin.getMessage("pickup-success"));
            return;
        }

        // Hebe Block auf (CarryOn-Style: mit allem Inhalt, visuell getragen)
        plugin.getCarryingManager().pickupBlock(player, state, block.getLocation());
        player.sendMessage(plugin.getMessage("pickup-success"));
    }
}
