package bladesmp.itembanfolia.managers;

import bladesmp.itembanfolia.ItemBanPlugin;
import org.bukkit.Material;
import org.bukkit.World;

import java.util.*;

public class WorldBanManager {

    private final ItemBanPlugin plugin;
    private final Map<String, Set<Material>> worldBannedItems;

    public WorldBanManager(ItemBanPlugin plugin) {
        this.plugin = plugin;
        this.worldBannedItems = new HashMap<>();
        loadWorldBans();
    }

    private void loadWorldBans() {
        worldBannedItems.clear();

        if (plugin.getConfigManager() == null || !plugin.getConfigManager().areWorldBansEnabled()) {
            return;
        }

        Set<String> worldNames = plugin.getConfigManager().getWorldsWithBans();
        for (String worldName : worldNames) {
            List<String> itemStrings = plugin.getConfigManager().getWorldBannedItems(worldName);
            Set<Material> bannedMaterials = new HashSet<>();

            for (String itemString : itemStrings) {
                try {
                    Material material = Material.valueOf(itemString.toUpperCase());
                    bannedMaterials.add(material);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid banned item '" + itemString + "' for world '" + worldName + "'");
                }
            }

            if (!bannedMaterials.isEmpty()) {
                worldBannedItems.put(worldName.toLowerCase(), bannedMaterials);
                plugin.getLogger().info("Loaded " + bannedMaterials.size() + " banned items for world: " + worldName);
            }
        }
    }

    public boolean isItemBannedInWorld(World world, Material material) {
        if (!plugin.getConfigManager().areWorldBansEnabled()) {
            return false;
        }

        Set<Material> bannedItems = worldBannedItems.get(world.getName().toLowerCase());
        return bannedItems != null && bannedItems.contains(material);
    }

    public boolean isItemBannedInWorld(String worldName, Material material) {
        if (!plugin.getConfigManager().areWorldBansEnabled()) {
            return false;
        }

        Set<Material> bannedItems = worldBannedItems.get(worldName.toLowerCase());
        return bannedItems != null && bannedItems.contains(material);
    }

    public Set<Material> getBannedItemsForWorld(String worldName) {
        Set<Material> bannedItems = worldBannedItems.get(worldName.toLowerCase());
        return bannedItems != null ? new HashSet<>(bannedItems) : new HashSet<>();
    }

    public Set<String> getWorldsWithBans() {
        return new HashSet<>(worldBannedItems.keySet());
    }

    public void addBannedItem(String worldName, Material material) {
        worldBannedItems.computeIfAbsent(worldName.toLowerCase(), k -> new HashSet<>()).add(material);
        plugin.getConfigManager().addWorldBannedItem(worldName, material);
    }

    public void removeBannedItem(String worldName, Material material) {
        Set<Material> bannedItems = worldBannedItems.get(worldName.toLowerCase());
        if (bannedItems != null) {
            bannedItems.remove(material);
            if (bannedItems.isEmpty()) {
                worldBannedItems.remove(worldName.toLowerCase());
            }
        }
        plugin.getConfigManager().removeWorldBannedItem(worldName, material);
    }

    public void reloadConfig() {
        loadWorldBans();
    }
}