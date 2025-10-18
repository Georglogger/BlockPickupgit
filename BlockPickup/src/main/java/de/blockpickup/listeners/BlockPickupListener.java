package de.blockpickup.listeners;

import de.blockpickup.BlockPickupPlugin;
import de.blockpickup.utils.NBTUtils;
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
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.BlockStateMeta;

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

        // Prüfe ob Block ein Container ist
        BlockState state = block.getState();
        if (!(state instanceof Container container)) {
            return;
        }

        // Verhindere normalen Drop
        event.setDropItems(false);

        // Erstelle ItemStack mit Block
        ItemStack item = new ItemStack(blockType);

        // Wenn Creative Mode, gib Item direkt ohne Inhalt
        if (player.getGameMode() == GameMode.CREATIVE) {
            block.setType(Material.AIR);
            return;
        }

        // Speichere Container-Daten in ItemStack
        if (item.getItemMeta() instanceof BlockStateMeta meta) {
            BlockState blockState = meta.getBlockState();
            if (blockState instanceof Container itemContainer) {
                // Kopiere Inventar
                for (int i = 0; i < container.getInventory().getSize(); i++) {
                    ItemStack content = container.getInventory().getItem(i);
                    if (content != null) {
                        itemContainer.getInventory().setItem(i, content.clone());
                    }
                }

                // Speichere Custom Name falls vorhanden
                if (container.getCustomName() != null) {
                    itemContainer.setCustomName(container.getCustomName());
                }

                meta.setBlockState(blockState);
                item.setItemMeta(meta);
            }
        }

        // Füge zusätzliche NBT-Daten hinzu (z.B. Brennzeit bei Öfen)
        item = NBTUtils.saveBlockData(item, block);

        // Gebe Item dem Spieler oder droppe es
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(item);
            player.sendMessage(plugin.getMessage("pickup-success"));
        } else {
            block.getWorld().dropItemNaturally(block.getLocation(), item);
        }

        // Entferne Block
        block.setType(Material.AIR);
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        ItemStack item = event.getItemInHand();
        Block block = event.getBlockPlaced();

        // Prüfe ob Item BlockStateMeta hat
        if (!(item.getItemMeta() instanceof BlockStateMeta meta)) {
            return;
        }

        BlockState itemState = meta.getBlockState();
        if (!(itemState instanceof Container itemContainer)) {
            return;
        }

        // Warte einen Tick und setze dann die Daten
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            BlockState placedState = block.getState();
            if (placedState instanceof Container placedContainer) {
                // Kopiere Inventar
                for (int i = 0; i < itemContainer.getInventory().getSize(); i++) {
                    ItemStack content = itemContainer.getInventory().getItem(i);
                    if (content != null) {
                        placedContainer.getInventory().setItem(i, content.clone());
                    }
                }

                // Setze Custom Name
                if (itemContainer.getCustomName() != null) {
                    placedContainer.setCustomName(itemContainer.getCustomName());
                }

                placedContainer.update(true);

                // Lade zusätzliche NBT-Daten
                NBTUtils.loadBlockData(item, block);
            }
        }, 1L);
    }
}
