package de.blockpickup.listeners;

import de.blockpickup.BlockPickupPlugin;
import de.blockpickup.utils.NBTUtils;
import org.bukkit.Material;
import org.bukkit.entity.*;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;

public class EntityPickupListener implements Listener {

    private final BlockPickupPlugin plugin;

    public EntityPickupListener(BlockPickupPlugin plugin) {
        this.plugin = plugin;
    }

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

        // Erstelle ItemStack für Entity
        ItemStack item = createEntityItem(entity);
        if (item == null) {
            player.sendMessage(plugin.getMessage("pickup-failed"));
            return;
        }

        // Speichere Entity-Daten in Item
        item = NBTUtils.saveEntityData(item, entity);

        // Gebe Item dem Spieler
        if (player.getInventory().firstEmpty() != -1) {
            player.getInventory().addItem(item);
            player.sendMessage(plugin.getMessage("pickup-success"));
            entity.remove();
            event.setCancelled(true);
        } else {
            player.sendMessage(plugin.getMessage("pickup-failed"));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onEntityPlace(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || event.getHand() != EquipmentSlot.HAND) {
            return;
        }

        // Prüfe ob Item Entity-Daten hat
        if (!NBTUtils.hasEntityData(item)) {
            return;
        }

        // Spawne Entity
        if (event.getClickedBlock() != null) {
            Entity entity = NBTUtils.spawnEntityFromItem(item, event.getClickedBlock().getLocation().add(0, 1, 0));

            if (entity != null) {
                player.sendMessage(plugin.getMessage("place-success"));

                // Entferne Item aus Hand
                if (item.getAmount() > 1) {
                    item.setAmount(item.getAmount() - 1);
                } else {
                    player.getInventory().setItemInMainHand(new ItemStack(Material.AIR));
                }

                event.setCancelled(true);
            }
        }
    }

    private ItemStack createEntityItem(Entity entity) {
        Material material = switch (entity.getType()) {
            case COW -> Material.COW_SPAWN_EGG;
            case PIG -> Material.PIG_SPAWN_EGG;
            case SHEEP -> Material.SHEEP_SPAWN_EGG;
            case CHICKEN -> Material.CHICKEN_SPAWN_EGG;
            case HORSE -> Material.HORSE_SPAWN_EGG;
            case DONKEY -> Material.DONKEY_SPAWN_EGG;
            case MULE -> Material.MULE_SPAWN_EGG;
            case LLAMA -> Material.LLAMA_SPAWN_EGG;
            case CAT -> Material.CAT_SPAWN_EGG;
            case WOLF -> Material.WOLF_SPAWN_EGG;
            case VILLAGER -> Material.VILLAGER_SPAWN_EGG;
            case ARMOR_STAND -> Material.ARMOR_STAND;
            case ITEM_FRAME -> Material.ITEM_FRAME;
            case GLOW_ITEM_FRAME -> Material.GLOW_ITEM_FRAME;
            case MINECART -> Material.MINECART;
            case MINECART_CHEST -> Material.CHEST_MINECART;
            case MINECART_FURNACE -> Material.FURNACE_MINECART;
            case MINECART_HOPPER -> Material.HOPPER_MINECART;
            case OAK_BOAT, SPRUCE_BOAT, BIRCH_BOAT, JUNGLE_BOAT, ACACIA_BOAT, DARK_OAK_BOAT, MANGROVE_BOAT, CHERRY_BOAT, BAMBOO_RAFT ->
                getBoatMaterial(entity);
            default -> null;
        };

        if (material == null) {
            return null;
        }

        ItemStack item = new ItemStack(material);

        // Füge Custom Name hinzu falls vorhanden
        if (entity.getCustomName() != null) {
            var meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(entity.getCustomName());
                item.setItemMeta(meta);
            }
        }

        return item;
    }

    private Material getBoatMaterial(Entity entity) {
        if (entity instanceof Boat boat) {
            return switch (boat.getBoatType()) {
                case OAK -> Material.OAK_BOAT;
                case SPRUCE -> Material.SPRUCE_BOAT;
                case BIRCH -> Material.BIRCH_BOAT;
                case JUNGLE -> Material.JUNGLE_BOAT;
                case ACACIA -> Material.ACACIA_BOAT;
                case DARK_OAK -> Material.DARK_OAK_BOAT;
                case MANGROVE -> Material.MANGROVE_BOAT;
                case CHERRY -> Material.CHERRY_BOAT;
                case BAMBOO -> Material.BAMBOO_RAFT;
            };
        }
        return Material.OAK_BOAT;
    }
}
