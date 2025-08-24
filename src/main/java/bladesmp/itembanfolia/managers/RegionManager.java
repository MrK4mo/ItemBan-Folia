package bladesmp.itembanfolia.managers;

import bladesmp.itembanfolia.ItemBanPlugin;
import bladesmp.itembanfolia.models.ItemBanRegion;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class RegionManager {

    private final ItemBanPlugin plugin;
    private final Map<String, ItemBanRegion> regions;
    private final Map<UUID, Location> pos1Map;
    private final Map<UUID, Location> pos2Map;
    private File regionsFile;
    private FileConfiguration regionsConfig;

    public RegionManager(ItemBanPlugin plugin) {
        this.plugin = plugin;
        this.regions = new HashMap<>();
        this.pos1Map = new HashMap<>();
        this.pos2Map = new HashMap<>();

        createRegionsFile();
    }

    private void createRegionsFile() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        regionsFile = new File(plugin.getDataFolder(), "regions.yml");
        if (!regionsFile.exists()) {
            try {
                regionsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create regions.yml: " + e.getMessage());
            }
        }

        regionsConfig = YamlConfiguration.loadConfiguration(regionsFile);
    }

    public void loadRegions() {
        regions.clear();

        ConfigurationSection regionsSection = regionsConfig.getConfigurationSection("regions");
        if (regionsSection == null) {
            return;
        }

        for (String regionName : regionsSection.getKeys(false)) {
            try {
                ConfigurationSection section = regionsSection.getConfigurationSection(regionName);
                if (section == null) continue;

                String worldName = section.getString("world");
                World world = Bukkit.getWorld(worldName);
                if (world == null) {
                    plugin.getLogger().warning("World '" + worldName + "' not found for region '" + regionName + "'");
                    continue;
                }

                int minX = section.getInt("min.x");
                int minY = section.getInt("min.y");
                int minZ = section.getInt("min.z");
                int maxX = section.getInt("max.x");
                int maxY = section.getInt("max.y");
                int maxZ = section.getInt("max.z");

                Location min = new Location(world, minX, minY, minZ);
                Location max = new Location(world, maxX, maxY, maxZ);

                List<String> bannedItemStrings = section.getStringList("banned-items");
                List<Material> bannedItems = new ArrayList<>();

                for (String itemString : bannedItemStrings) {
                    try {
                        Material material = Material.valueOf(itemString);
                        bannedItems.add(material);
                    } catch (IllegalArgumentException e) {
                        plugin.getLogger().warning("Invalid material '" + itemString + "' in region '" + regionName + "'");
                    }
                }

                ItemBanRegion region = new ItemBanRegion(regionName, min, max, bannedItems);
                regions.put(regionName, region);

            } catch (Exception e) {
                plugin.getLogger().severe("Error loading region '" + regionName + "': " + e.getMessage());
            }
        }

        plugin.getLogger().info("Loaded " + regions.size() + " regions");
    }

    public void saveRegions() {
        regionsConfig.set("regions", null);

        for (ItemBanRegion region : regions.values()) {
            String path = "regions." + region.getName();

            regionsConfig.set(path + ".world", region.getMinLocation().getWorld().getName());
            regionsConfig.set(path + ".min.x", region.getMinLocation().getBlockX());
            regionsConfig.set(path + ".min.y", region.getMinLocation().getBlockY());
            regionsConfig.set(path + ".min.z", region.getMinLocation().getBlockZ());
            regionsConfig.set(path + ".max.x", region.getMaxLocation().getBlockX());
            regionsConfig.set(path + ".max.y", region.getMaxLocation().getBlockY());
            regionsConfig.set(path + ".max.z", region.getMaxLocation().getBlockZ());

            List<String> bannedItemStrings = new ArrayList<>();
            for (Material material : region.getBannedItems()) {
                bannedItemStrings.add(material.name());
            }
            regionsConfig.set(path + ".banned-items", bannedItemStrings);
        }

        try {
            regionsConfig.save(regionsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save regions.yml: " + e.getMessage());
        }
    }

    public void setPos1(Player player, Location location) {
        pos1Map.put(player.getUniqueId(), location);
        plugin.getMessageUtils().sendMessage(player, "pos1-set",
                "x", String.valueOf(location.getBlockX()),
                "y", String.valueOf(location.getBlockY()),
                "z", String.valueOf(location.getBlockZ())
        );
    }

    public void setPos2(Player player, Location location) {
        pos2Map.put(player.getUniqueId(), location);
        plugin.getMessageUtils().sendMessage(player, "pos2-set",
                "x", String.valueOf(location.getBlockX()),
                "y", String.valueOf(location.getBlockY()),
                "z", String.valueOf(location.getBlockZ())
        );
    }

    public boolean createRegion(Player player, String name, List<Material> bannedItems) {
        UUID playerId = player.getUniqueId();

        if (!pos1Map.containsKey(playerId) || !pos2Map.containsKey(playerId)) {
            return false;
        }

        Location pos1 = pos1Map.get(playerId);
        Location pos2 = pos2Map.get(playerId);

        if (!pos1.getWorld().equals(pos2.getWorld())) {
            return false;
        }

        // Calculate min and max coordinates
        int minX = Math.min(pos1.getBlockX(), pos2.getBlockX());
        int minY = Math.min(pos1.getBlockY(), pos2.getBlockY());
        int minZ = Math.min(pos1.getBlockZ(), pos2.getBlockZ());
        int maxX = Math.max(pos1.getBlockX(), pos2.getBlockX());
        int maxY = Math.max(pos1.getBlockY(), pos2.getBlockY());
        int maxZ = Math.max(pos1.getBlockZ(), pos2.getBlockZ());

        Location min = new Location(pos1.getWorld(), minX, minY, minZ);
        Location max = new Location(pos1.getWorld(), maxX, maxY, maxZ);

        ItemBanRegion region = new ItemBanRegion(name, min, max, bannedItems);
        regions.put(name, region);

        // Clear positions
        pos1Map.remove(playerId);
        pos2Map.remove(playerId);

        saveRegions();
        return true;
    }

    public boolean deleteRegion(String name) {
        if (regions.containsKey(name)) {
            regions.remove(name);
            saveRegions();
            return true;
        }
        return false;
    }

    public ItemBanRegion getRegion(String name) {
        return regions.get(name);
    }

    public Collection<ItemBanRegion> getAllRegions() {
        return regions.values();
    }

    public List<ItemBanRegion> getRegionsAt(Location location) {
        List<ItemBanRegion> regionsAtLocation = new ArrayList<>();

        for (ItemBanRegion region : regions.values()) {
            if (region.contains(location)) {
                regionsAtLocation.add(region);
            }
        }

        return regionsAtLocation;
    }

    public boolean isItemBannedAt(Location location, Material material) {
        List<ItemBanRegion> regionsAtLocation = getRegionsAt(location);

        for (ItemBanRegion region : regionsAtLocation) {
            if (region.getBannedItems().contains(material)) {
                return true;
            }
        }

        return false;
    }

    public boolean hasPos1(Player player) {
        return pos1Map.containsKey(player.getUniqueId());
    }

    public boolean hasPos2(Player player) {
        return pos2Map.containsKey(player.getUniqueId());
    }

    public boolean hasBothPositions(Player player) {
        return hasPos1(player) && hasPos2(player);
    }

    public void clearPositions(Player player) {
        pos1Map.remove(player.getUniqueId());
        pos2Map.remove(player.getUniqueId());
    }
}