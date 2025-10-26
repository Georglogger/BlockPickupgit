package de.blockpickup.utils;

import org.bukkit.Location;
import org.bukkit.Material;
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

    /**
     * Serialisiert ALLE Entity-Daten für CarryOn-Style Pickup
     * Speichert komplette Entity-Informationen inkl. Inventar, Health, etc.
     */
    public static String serializeCompleteEntity(Entity entity) {
        StringBuilder data = new StringBuilder();

        // Entity-Typ (bereits separat gespeichert, aber zur Sicherheit)
        data.append("type:").append(entity.getType().name()).append(";");

        // Custom Name
        if (entity.getCustomName() != null) {
            data.append("customName:").append(entity.getCustomName().replace(";", "\\;")).append(";");
            data.append("customNameVisible:").append(entity.isCustomNameVisible()).append(";");
        }

        // LivingEntity Daten
        if (entity instanceof LivingEntity living) {
            data.append("health:").append(living.getHealth()).append(";");
            data.append("maxHealth:").append(living.getMaxHealth()).append(";");

            // Rüstung und Equipment
            if (living.getEquipment() != null) {
                var equipment = living.getEquipment();
                data.append("helmet:").append(serializeItem(equipment.getHelmet())).append(";");
                data.append("chestplate:").append(serializeItem(equipment.getChestplate())).append(";");
                data.append("leggings:").append(serializeItem(equipment.getLeggings())).append(";");
                data.append("boots:").append(serializeItem(equipment.getBoots())).append(";");
                data.append("mainHand:").append(serializeItem(equipment.getItemInMainHand())).append(";");
                data.append("offHand:").append(serializeItem(equipment.getItemInOffHand())).append(";");
            }

            // AI & Verhalten
            data.append("ai:").append(living.hasAI()).append(";");
            data.append("canPickupItems:").append(living.getCanPickupItems()).append(";");
        }

        // Ageable (Baby/Adult)
        if (entity instanceof Ageable ageable) {
            data.append("age:").append(ageable.getAge()).append(";");
            data.append("isAdult:").append(ageable.isAdult()).append(";");
            data.append("ageLock:").append(ageable.getAgeLock()).append(";");
        }

        // Tameable (Zähmbare Tiere)
        if (entity instanceof Tameable tameable) {
            data.append("tamed:").append(tameable.isTamed()).append(";");
            if (tameable.isTamed() && tameable.getOwner() != null) {
                data.append("owner:").append(tameable.getOwner().getUniqueId().toString()).append(";");
            }
        }

        // Sittable (Sitzende Tiere)
        if (entity instanceof Sittable sittable) {
            data.append("sitting:").append(sittable.isSitting()).append(";");
        }

        // Schaf-spezifisch
        if (entity instanceof Sheep sheep) {
            data.append("color:").append(sheep.getColor().name()).append(";");
            data.append("sheared:").append(sheep.isSheared()).append(";");
        }

        // Villager-spezifisch
        if (entity instanceof Villager villager) {
            data.append("profession:").append(villager.getProfession().name()).append(";");
            data.append("villagerType:").append(villager.getVillagerType().name()).append(";");
            data.append("villagerLevel:").append(villager.getVillagerLevel()).append(";");
            data.append("villagerExperience:").append(villager.getVillagerExperience()).append(";");
        }

        // Pferd-spezifisch
        if (entity instanceof Horse horse) {
            data.append("horseColor:").append(horse.getColor().name()).append(";");
            data.append("horseStyle:").append(horse.getStyle().name()).append(";");
            data.append("domestication:").append(horse.getDomestication()).append(";");
            data.append("jumpStrength:").append(horse.getJumpStrength()).append(";");

            // Inventar (Sattel, Rüstung)
            if (horse.getInventory() != null) {
                data.append("saddle:").append(serializeItem(horse.getInventory().getSaddle())).append(";");
                data.append("horseArmor:").append(serializeItem(horse.getInventory().getArmor())).append(";");
            }
        }

        // Llama-spezifisch
        if (entity instanceof Llama llama) {
            data.append("llamaColor:").append(llama.getColor().name()).append(";");
            data.append("strength:").append(llama.getStrength()).append(";");

            // Inventar
            if (llama.getInventory() != null) {
                data.append("decor:").append(serializeItem(llama.getInventory().getDecor())).append(";");
            }
        }

        // Cat-spezifisch
        if (entity instanceof Cat cat) {
            data.append("catType:").append(cat.getCatType().name()).append(";");
            data.append("collarColor:").append(cat.getCollarColor().name()).append(";");
        }

        // Wolf-spezifisch
        if (entity instanceof Wolf wolf) {
            data.append("collarColor:").append(wolf.getCollarColor().name()).append(";");
            data.append("angry:").append(wolf.isAngry()).append(";");
        }

        // ArmorStand-spezifisch
        if (entity instanceof ArmorStand armorStand) {
            data.append("hasArms:").append(armorStand.hasArms()).append(";");
            data.append("hasBasePlate:").append(armorStand.hasBasePlate()).append(";");
            data.append("isSmall:").append(armorStand.isSmall()).append(";");
            data.append("isMarker:").append(armorStand.isMarker()).append(";");
            data.append("isVisible:").append(armorStand.isVisible()).append(";");
        }

        // Minecart-spezifisch
        if (entity instanceof org.bukkit.entity.minecart.StorageMinecart storageMinecart) {
            // Inventar speichern
            StringBuilder inv = new StringBuilder();
            for (int i = 0; i < storageMinecart.getInventory().getSize(); i++) {
                ItemStack item = storageMinecart.getInventory().getItem(i);
                if (item != null) {
                    inv.append(i).append(":").append(serializeItem(item)).append(",");
                }
            }
            if (inv.length() > 0) {
                data.append("minecartInventory:").append(inv.toString()).append(";");
            }
        }

        // Item Frame
        if (entity instanceof ItemFrame itemFrame) {
            data.append("frameItem:").append(serializeItem(itemFrame.getItem())).append(";");
            data.append("rotation:").append(itemFrame.getRotation().name()).append(";");
        }

        return data.toString();
    }

    /**
     * Deserialisiert Entity-Daten und wendet sie auf die Entity an
     */
    public static void deserializeCompleteEntity(Entity entity, String serializedData) {
        if (serializedData == null || serializedData.isEmpty()) return;

        String[] parts = serializedData.split(";");
        for (String part : parts) {
            String[] keyValue = part.split(":", 2);
            if (keyValue.length != 2) continue;

            String key = keyValue[0];
            String value = keyValue[1];

            try {
                switch (key) {
                    case "customName":
                        entity.setCustomName(value.replace("\\;", ";"));
                        break;
                    case "customNameVisible":
                        entity.setCustomNameVisible(Boolean.parseBoolean(value));
                        break;
                    case "health":
                        if (entity instanceof LivingEntity living) {
                            double health = Double.parseDouble(value);
                            living.setHealth(Math.min(health, living.getMaxHealth()));
                        }
                        break;
                    case "helmet":
                    case "chestplate":
                    case "leggings":
                    case "boots":
                    case "mainHand":
                    case "offHand":
                        if (entity instanceof LivingEntity living && living.getEquipment() != null) {
                            ItemStack item = deserializeItem(value);
                            switch (key) {
                                case "helmet" -> living.getEquipment().setHelmet(item);
                                case "chestplate" -> living.getEquipment().setChestplate(item);
                                case "leggings" -> living.getEquipment().setLeggings(item);
                                case "boots" -> living.getEquipment().setBoots(item);
                                case "mainHand" -> living.getEquipment().setItemInMainHand(item);
                                case "offHand" -> living.getEquipment().setItemInOffHand(item);
                            }
                        }
                        break;
                    case "ai":
                        if (entity instanceof LivingEntity living) {
                            living.setAI(Boolean.parseBoolean(value));
                        }
                        break;
                    case "canPickupItems":
                        if (entity instanceof LivingEntity living) {
                            living.setCanPickupItems(Boolean.parseBoolean(value));
                        }
                        break;
                    case "age":
                        if (entity instanceof Ageable ageable) {
                            ageable.setAge(Integer.parseInt(value));
                        }
                        break;
                    case "ageLock":
                        if (entity instanceof Ageable ageable) {
                            ageable.setAgeLock(Boolean.parseBoolean(value));
                        }
                        break;
                    case "tamed":
                        if (entity instanceof Tameable tameable) {
                            tameable.setTamed(Boolean.parseBoolean(value));
                        }
                        break;
                    case "owner":
                        if (entity instanceof Tameable tameable) {
                            // Owner wird durch UUID wiederhergestellt (falls online)
                            try {
                                java.util.UUID ownerId = java.util.UUID.fromString(value);
                                org.bukkit.OfflinePlayer owner = org.bukkit.Bukkit.getOfflinePlayer(ownerId);
                                tameable.setOwner(owner);
                            } catch (Exception ignored) {}
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
                    case "villagerType":
                        if (entity instanceof Villager villager) {
                            villager.setVillagerType(Villager.Type.valueOf(value));
                        }
                        break;
                    case "villagerLevel":
                        if (entity instanceof Villager villager) {
                            villager.setVillagerLevel(Integer.parseInt(value));
                        }
                        break;
                    case "villagerExperience":
                        if (entity instanceof Villager villager) {
                            villager.setVillagerExperience(Integer.parseInt(value));
                        }
                        break;
                    case "horseColor":
                        if (entity instanceof Horse horse) {
                            horse.setColor(Horse.Color.valueOf(value));
                        }
                        break;
                    case "horseStyle":
                        if (entity instanceof Horse horse) {
                            horse.setStyle(Horse.Style.valueOf(value));
                        }
                        break;
                    case "domestication":
                        if (entity instanceof Horse horse) {
                            horse.setDomestication(Integer.parseInt(value));
                        }
                        break;
                    case "jumpStrength":
                        if (entity instanceof Horse horse) {
                            horse.setJumpStrength(Double.parseDouble(value));
                        }
                        break;
                    case "saddle":
                        if (entity instanceof Horse horse && horse.getInventory() != null) {
                            horse.getInventory().setSaddle(deserializeItem(value));
                        }
                        break;
                    case "horseArmor":
                        if (entity instanceof Horse horse && horse.getInventory() != null) {
                            horse.getInventory().setArmor(deserializeItem(value));
                        }
                        break;
                    case "llamaColor":
                        if (entity instanceof Llama llama) {
                            llama.setColor(Llama.Color.valueOf(value));
                        }
                        break;
                    case "strength":
                        if (entity instanceof Llama llama) {
                            llama.setStrength(Integer.parseInt(value));
                        }
                        break;
                    case "decor":
                        if (entity instanceof Llama llama && llama.getInventory() != null) {
                            llama.getInventory().setDecor(deserializeItem(value));
                        }
                        break;
                    case "catType":
                        if (entity instanceof Cat cat) {
                            cat.setCatType(Cat.Type.valueOf(value));
                        }
                        break;
                    case "collarColor":
                        if (entity instanceof Cat cat) {
                            cat.setCollarColor(org.bukkit.DyeColor.valueOf(value));
                        } else if (entity instanceof Wolf wolf) {
                            wolf.setCollarColor(org.bukkit.DyeColor.valueOf(value));
                        }
                        break;
                    case "angry":
                        if (entity instanceof Wolf wolf) {
                            wolf.setAngry(Boolean.parseBoolean(value));
                        }
                        break;
                    case "hasArms":
                        if (entity instanceof ArmorStand armorStand) {
                            armorStand.setArms(Boolean.parseBoolean(value));
                        }
                        break;
                    case "hasBasePlate":
                        if (entity instanceof ArmorStand armorStand) {
                            armorStand.setBasePlate(Boolean.parseBoolean(value));
                        }
                        break;
                    case "isSmall":
                        if (entity instanceof ArmorStand armorStand) {
                            armorStand.setSmall(Boolean.parseBoolean(value));
                        }
                        break;
                    case "isMarker":
                        if (entity instanceof ArmorStand armorStand) {
                            armorStand.setMarker(Boolean.parseBoolean(value));
                        }
                        break;
                    case "isVisible":
                        if (entity instanceof ArmorStand armorStand) {
                            armorStand.setVisible(Boolean.parseBoolean(value));
                        }
                        break;
                    case "frameItem":
                        if (entity instanceof ItemFrame itemFrame) {
                            itemFrame.setItem(deserializeItem(value));
                        }
                        break;
                    case "rotation":
                        if (entity instanceof ItemFrame itemFrame) {
                            itemFrame.setRotation(org.bukkit.Rotation.valueOf(value));
                        }
                        break;
                    case "minecartInventory":
                        if (entity instanceof org.bukkit.entity.minecart.StorageMinecart storageMinecart) {
                            String[] items = value.split(",");
                            for (String itemData : items) {
                                String[] slotItem = itemData.split(":", 2);
                                if (slotItem.length == 2) {
                                    int slot = Integer.parseInt(slotItem[0]);
                                    ItemStack item = deserializeItem(slotItem[1]);
                                    storageMinecart.getInventory().setItem(slot, item);
                                }
                            }
                        }
                        break;
                }
            } catch (Exception e) {
                // Ignoriere ungültige Werte
            }
        }
    }

    /**
     * Serialisiert ein ItemStack zu einem String (mit allen NBT-Daten)
     */
    private static String serializeItem(ItemStack item) {
        if (item == null || item.getType() == Material.AIR) {
            return "AIR";
        }

        try {
            // Verwende Bukkit's Serialisierung für vollständige Item-Daten
            byte[] bytes = item.serializeAsBytes();
            if (bytes != null && bytes.length > 0) {
                return "B64:" + java.util.Base64.getEncoder().encodeToString(bytes);
            }
        } catch (Exception e) {
            // Log warning but continue with fallback
            BlockPickupPlugin.getInstance().getLogger().warning(
                "Failed to serialize item " + item.getType() + " using bytes, using fallback: " + e.getMessage()
            );
        }

        // Fallback: Verwende YAML Serialisierung von Bukkit
        try {
            var map = item.serialize();
            StringBuilder sb = new StringBuilder("YAML:");
            sb.append(map.get("v")).append("|");  // Version
            sb.append(map.get("type")).append("|");  // Material
            if (map.containsKey("amount")) {
                sb.append(map.get("amount"));
            } else {
                sb.append("1");
            }
            if (map.containsKey("meta")) {
                sb.append("|").append(java.util.Base64.getEncoder().encodeToString(
                    map.get("meta").toString().getBytes(java.nio.charset.StandardCharsets.UTF_8)
                ));
            }
            return sb.toString();
        } catch (Exception e) {
            // Final fallback: Nur Material und Amount
            BlockPickupPlugin.getInstance().getLogger().warning(
                "Failed to serialize item " + item.getType() + " completely, data may be lost: " + e.getMessage()
            );
            return "SIMPLE:" + item.getType().name() + ":" + item.getAmount();
        }
    }

    /**
     * Deserialisiert einen String zu einem ItemStack (mit allen NBT-Daten)
     */
    private static ItemStack deserializeItem(String data) {
        if (data == null || data.equals("AIR") || data.isEmpty()) {
            return null;
        }

        try {
            // Erkenne Format anhand des Prefixes
            if (data.startsWith("B64:")) {
                // Neues Base64 Format
                byte[] bytes = java.util.Base64.getDecoder().decode(data.substring(4));
                return ItemStack.deserializeBytes(bytes);
            } else if (data.startsWith("YAML:")) {
                // YAML Fallback Format
                String[] parts = data.substring(5).split("\\|");
                if (parts.length >= 3) {
                    Material material = Material.valueOf(parts[1]);
                    int amount = Integer.parseInt(parts[2]);
                    ItemStack item = new ItemStack(material, amount);
                    // Meta könnte in parts[3] sein, aber das ist komplex zu parsen
                    // Für jetzt: Basis Item ohne Meta
                    return item;
                }
            } else if (data.startsWith("SIMPLE:")) {
                // Einfaches Format
                String[] parts = data.substring(7).split(":");
                if (parts.length >= 2) {
                    Material material = Material.valueOf(parts[0]);
                    int amount = Integer.parseInt(parts[1]);
                    return new ItemStack(material, amount);
                }
            } else {
                // Legacy Format (alter Code ohne Prefix) - versuche Base64
                try {
                    byte[] bytes = java.util.Base64.getDecoder().decode(data);
                    return ItemStack.deserializeBytes(bytes);
                } catch (Exception ignored) {
                    // Nicht Base64, versuche einfaches Format
                    String[] parts = data.split(":");
                    if (parts.length >= 2) {
                        Material material = Material.valueOf(parts[0]);
                        int amount = Integer.parseInt(parts[1]);
                        return new ItemStack(material, amount);
                    }
                }
            }
        } catch (Exception e) {
            BlockPickupPlugin.getInstance().getLogger().warning(
                "Failed to deserialize item data: " + e.getMessage()
            );
        }

        return null;
    }
}
