package de.blockpickup.utils;

import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.block.Furnace;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import de.blockpickup.BlockPickupPlugin;

public class NBTUtils {

    private static final NamespacedKey ENTITY_TYPE_KEY = new NamespacedKey(BlockPickupPlugin.getInstance(), "entity_type");
    private static final NamespacedKey ENTITY_DATA_KEY = new NamespacedKey(BlockPickupPlugin.getInstance(), "entity_data");
    private static final NamespacedKey CUSTOM_NAME_KEY = new NamespacedKey(BlockPickupPlugin.getInstance(), "custom_name");
    private static final NamespacedKey BURN_TIME_KEY = new NamespacedKey(BlockPickupPlugin.getInstance(), "burn_time");
    private static final NamespacedKey COOK_TIME_KEY = new NamespacedKey(BlockPickupPlugin.getInstance(), "cook_time");
    private static final NamespacedKey HEALTH_KEY = new NamespacedKey(BlockPickupPlugin.getInstance(), "health");
    private static final NamespacedKey AGE_KEY = new NamespacedKey(BlockPickupPlugin.getInstance(), "age");

    public static ItemStack saveBlockData(ItemStack item, Block block) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        PersistentDataContainer container = meta.getPersistentDataContainer();

        // Speichere Ofen-spezifische Daten
        if (block.getState() instanceof Furnace furnace) {
            container.set(BURN_TIME_KEY, PersistentDataType.SHORT, furnace.getBurnTime());
            container.set(COOK_TIME_KEY, PersistentDataType.SHORT, furnace.getCookTime());
        }

        item.setItemMeta(meta);
        return item;
    }

    public static void loadBlockData(ItemStack item, Block block) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return;

        PersistentDataContainer container = meta.getPersistentDataContainer();

        // Lade Ofen-spezifische Daten
        if (block.getState() instanceof Furnace furnace) {
            if (container.has(BURN_TIME_KEY, PersistentDataType.SHORT)) {
                Short burnTime = container.get(BURN_TIME_KEY, PersistentDataType.SHORT);
                if (burnTime != null) {
                    furnace.setBurnTime(burnTime);
                }
            }

            if (container.has(COOK_TIME_KEY, PersistentDataType.SHORT)) {
                Short cookTime = container.get(COOK_TIME_KEY, PersistentDataType.SHORT);
                if (cookTime != null) {
                    furnace.setCookTime(cookTime);
                }
            }

            furnace.update();
        }
    }

    public static ItemStack saveEntityData(ItemStack item, Entity entity) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        PersistentDataContainer container = meta.getPersistentDataContainer();

        // Speichere Entity-Typ
        container.set(ENTITY_TYPE_KEY, PersistentDataType.STRING, entity.getType().name());

        // Speichere Custom Name
        if (entity.getCustomName() != null) {
            container.set(CUSTOM_NAME_KEY, PersistentDataType.STRING, entity.getCustomName());
        }

        // Speichere Health bei Lebewesen
        if (entity instanceof LivingEntity livingEntity) {
            container.set(HEALTH_KEY, PersistentDataType.DOUBLE, livingEntity.getHealth());
        }

        // Speichere Alter bei Tieren
        if (entity instanceof Ageable ageable) {
            container.set(AGE_KEY, PersistentDataType.INTEGER, ageable.getAge());
        }

        // Speichere Entity-spezifische Daten als serialisierter String
        String entityData = serializeEntityData(entity);
        if (entityData != null) {
            container.set(ENTITY_DATA_KEY, PersistentDataType.STRING, entityData);
        }

        // Aktualisiere Display Name
        if (entity.getCustomName() != null) {
            meta.setDisplayName(entity.getCustomName());
        }

        item.setItemMeta(meta);
        return item;
    }

    public static boolean hasEntityData(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return false;

        ItemMeta meta = item.getItemMeta();
        PersistentDataContainer container = meta.getPersistentDataContainer();

        return container.has(ENTITY_TYPE_KEY, PersistentDataType.STRING);
    }

    public static Entity spawnEntityFromItem(ItemStack item, Location location) {
        if (!hasEntityData(item)) return null;

        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;

        PersistentDataContainer container = meta.getPersistentDataContainer();

        // Lade Entity-Typ
        String typeString = container.get(ENTITY_TYPE_KEY, PersistentDataType.STRING);
        if (typeString == null) return null;

        EntityType type;
        try {
            type = EntityType.valueOf(typeString);
        } catch (IllegalArgumentException e) {
            return null;
        }

        // Spawne Entity
        Entity entity = location.getWorld().spawnEntity(location, type);

        // Setze Custom Name
        if (container.has(CUSTOM_NAME_KEY, PersistentDataType.STRING)) {
            String customName = container.get(CUSTOM_NAME_KEY, PersistentDataType.STRING);
            if (customName != null) {
                entity.setCustomName(customName);
                entity.setCustomNameVisible(true);
            }
        }

        // Setze Health
        if (entity instanceof LivingEntity livingEntity && container.has(HEALTH_KEY, PersistentDataType.DOUBLE)) {
            Double health = container.get(HEALTH_KEY, PersistentDataType.DOUBLE);
            if (health != null && health > 0) {
                livingEntity.setHealth(Math.min(health, livingEntity.getMaxHealth()));
            }
        }

        // Setze Alter
        if (entity instanceof Ageable ageable && container.has(AGE_KEY, PersistentDataType.INTEGER)) {
            Integer age = container.get(AGE_KEY, PersistentDataType.INTEGER);
            if (age != null) {
                ageable.setAge(age);
            }
        }

        // Lade Entity-spezifische Daten
        if (container.has(ENTITY_DATA_KEY, PersistentDataType.STRING)) {
            String entityData = container.get(ENTITY_DATA_KEY, PersistentDataType.STRING);
            if (entityData != null) {
                deserializeEntityData(entity, entityData);
            }
        }

        return entity;
    }

    private static String serializeEntityData(Entity entity) {
        StringBuilder data = new StringBuilder();

        // Speichere spezifische Daten je nach Entity-Typ
        if (entity instanceof Tameable tameable) {
            if (tameable.isTamed() && tameable.getOwner() != null) {
                data.append("tamed:").append(tameable.getOwner().getUniqueId().toString()).append(";");
            }
        }

        if (entity instanceof Sittable sittable) {
            data.append("sitting:").append(sittable.isSitting()).append(";");
        }

        if (entity instanceof Sheep sheep) {
            data.append("color:").append(sheep.getColor().name()).append(";");
            data.append("sheared:").append(sheep.isSheared()).append(";");
        }

        if (entity instanceof Villager villager) {
            data.append("profession:").append(villager.getProfession().name()).append(";");
            data.append("type:").append(villager.getVillagerType().name()).append(";");
        }

        if (entity instanceof Horse horse) {
            data.append("horse_color:").append(horse.getColor().name()).append(";");
            data.append("horse_style:").append(horse.getStyle().name()).append(";");
        }

        return data.length() > 0 ? data.toString() : null;
    }

    private static void deserializeEntityData(Entity entity, String data) {
        String[] parts = data.split(";");

        for (String part : parts) {
            String[] keyValue = part.split(":", 2);
            if (keyValue.length != 2) continue;

            String key = keyValue[0];
            String value = keyValue[1];

            try {
                switch (key) {
                    case "tamed":
                        if (entity instanceof Tameable tameable) {
                            // Owner wird später gesetzt wenn möglich
                            tameable.setTamed(true);
                        }
                        break;
                    case "sitting":
                        if (entity instanceof Sittable sittable) {
                            sittable.setSitting(Boolean.parseBoolean(value));
                        }
                        break;
                    case "color":
                        if (entity instanceof Sheep sheep) {
                            sheep.setColor(org.bukkit.DyeColor.valueOf(value));
                        }
                        break;
                    case "sheared":
                        if (entity instanceof Sheep sheep) {
                            sheep.setSheared(Boolean.parseBoolean(value));
                        }
                        break;
                    case "profession":
                        if (entity instanceof Villager villager) {
                            villager.setProfession(Villager.Profession.valueOf(value));
                        }
                        break;
                    case "type":
                        if (entity instanceof Villager villager) {
                            villager.setVillagerType(Villager.Type.valueOf(value));
                        }
                        break;
                    case "horse_color":
                        if (entity instanceof Horse horse) {
                            horse.setColor(Horse.Color.valueOf(value));
                        }
                        break;
                    case "horse_style":
                        if (entity instanceof Horse horse) {
                            horse.setStyle(Horse.Style.valueOf(value));
                        }
                        break;
                }
            } catch (Exception e) {
                // Ignoriere ungültige Werte
            }
        }
    }
}
