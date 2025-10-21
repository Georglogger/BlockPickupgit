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
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Listener für das Aufheben und Platzieren von Blöcken (CarryOn-Style)
 * Blöcke behalten ihren Inventar-Inhalt und werden visuell getragen
 */
public class BlockPickupListener implements Listener {

    private final BlockPickupPlugin plugin;

    public BlockPickupListener(BlockPickupPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material blockType = block.getType();

        // Prüfe ob es ein erlaubter Block ist
        if (!plugin.getConfigManager().isBlockAllowed(blockType)) {
            return;
        }

        // Prüfe Permission
        if (!player.hasPermission("blockpickup.pickup.block")) {
            return;
        }

        // Prüfe spezifische Permission für Block-Typ
        String blockName = blockType.name().toLowerCase();
        if (!player.hasPermission("blockpickup.pickup." + blockName)) {
            return;
        }

        // Prüfe ob Spieler sneaken muss
        if (plugin.getConfigManager().requiresSneak() && !player.isSneaking()) {
            return;
        }

        // Prüfe ob Hand leer sein muss
        if (plugin.getConfigManager().requiresEmptyHand()) {
            ItemStack mainHand = player.getInventory().getItemInMainHand();
            if (mainHand.getType() != Material.AIR) {
                player.sendMessage(plugin.getMessage("must-empty-hand"));
                return;
            }
        }

        // Prüfe ob Spieler bereits etwas trägt
        if (plugin.getCarryingManager().isCarrying(player)) {
            player.sendMessage(plugin.getMessage("already-carrying"));
            return;
        }

        // Prüfe ob Block ein Container ist
        BlockState state = block.getState();
        if (!(state instanceof Container container)) {
            return;
        }

        // Verhindere normalen Drop und Block-Break
        event.setDropItems(false);
        event.setCancelled(true);

        // Wenn Creative Mode, entferne Block einfach
        if (player.getGameMode() == GameMode.CREATIVE) {
            block.setType(Material.AIR);
            return;
        }

        // Hebe Block auf (CarryOn-Style: mit allem Inhalt, visuell getragen)
        plugin.getCarryingManager().pickupBlock(player, state, block.getLocation());
        player.sendMessage(plugin.getMessage("pickup-success"));
    }
}
