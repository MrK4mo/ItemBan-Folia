package bladesmp.itembanfolia.config;

import bladesmp.itembanfolia.ItemBanPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class ConfigManager {

    private final ItemBanPlugin plugin;
    private FileConfiguration config;
    private File configFile;

    // Default values
    private final Map<String, Object> defaults = new HashMap<>();

    public ConfigManager(ItemBanPlugin plugin) {
        this.plugin = plugin;
        initializeDefaults();
    }

    private void initializeDefaults() {
        // Combat settings
        defaults.put("combat.enabled", true);
        defaults.put("combat.duration", 15);
        defaults.put("combat.banned-items", Arrays.asList("ENDER_PEARL", "CHORUS_FRUIT", "GOLDEN_APPLE"));
        defaults.put("combat.kill-on-logout", true);
        defaults.put("combat.show-actionbar", true);
        defaults.put("combat.actionbar-update-interval", 10); // ticks
        defaults.put("combat.region-lock.enabled", false);
        defaults.put("combat.region-lock.regions", Arrays.asList("spawn", "arena"));

        // World bans
        defaults.put("world-bans.enabled", true);
        defaults.put("world-bans.worlds.spawn", Arrays.asList("ENDER_CRYSTAL", "TNT", "RESPAWN_ANCHOR"));

        // Messages
        defaults.put("messages.enabled", true);
        defaults.put("messages.use-minimessage", true);
        defaults.put("messages.prefix", "<red>[ItemBan]</red>");
        defaults.put("messages.actionbar-enabled", true); // NEW: Separate actionbar control

        // Message texts
        defaults.put("messages.item-banned-region", "&c&lDieses Item ist in dieser Region nicht erlaubt!");
        defaults.put("messages.item-banned-combat", "&c&lDieses Item ist während des Kampfes nicht erlaubt!");
        defaults.put("messages.item-banned-world", "&c&lDieses Item ist in dieser Welt nicht erlaubt!");
        defaults.put("messages.wand-received", "&aRegions-Wand erhalten! Linksklick und Rechtsklick zum Markieren.");
        defaults.put("messages.region-created", "&aRegion '&f{name}&a' erfolgreich erstellt!");
        defaults.put("messages.region-deleted", "&aRegion '&f{name}&a' wurde gelöscht!");
        defaults.put("messages.pos1-set", "&aPosition 1 gesetzt: &f{x}, {y}, {z}");
        defaults.put("messages.pos2-set", "&aPosition 2 gesetzt: &f{x}, {y}, {z}");
        defaults.put("messages.no-permission", "&cDu hast keine Berechtigung für diesen Befehl!");
        defaults.put("messages.config-reloaded", "&aKonfiguration erfolgreich neu geladen!");
        defaults.put("messages.in-combat", "&cDu bist jetzt im Kampf für {duration} Sekunden!");
        defaults.put("messages.combat-end", "&aDu bist nicht mehr im Kampf!");
        defaults.put("messages.combat-actionbar", "&cKampf: &f{time}s");
        defaults.put("messages.combat-logout-death", "&c{player} ist während des Kampfes offline gegangen und gestorben!");
        defaults.put("messages.combat-region-leave-denied", "&cDu kannst diese Region nicht während des Kampfes verlassen!");

        // Wand settings
        defaults.put("wand.material", "DIAMOND_AXE");
        defaults.put("wand.name", "&6&lRegions-Wand");
        defaults.put("wand.lore", Arrays.asList(
                "&7Linksklick: Position 1 setzen",
                "&7Rechtsklick: Position 2 setzen",
                "&7Verwende &f/itemban create <name>&7 um eine Region zu erstellen"
        ));
    }

    public void loadConfig() {
        createConfigFile();
        config = YamlConfiguration.loadConfiguration(configFile);

        // Set defaults if missing
        boolean changed = false;
        for (Map.Entry<String, Object> entry : defaults.entrySet()) {
            if (!config.contains(entry.getKey())) {
                config.set(entry.getKey(), entry.getValue());
                changed = true;
            }
        }

        if (changed) {
            saveConfig();
        }
    }

    private void createConfigFile() {
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        configFile = new File(plugin.getDataFolder(), "config.yml");
        if (!configFile.exists()) {
            plugin.saveDefaultConfig();
        }
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save config.yml: " + e.getMessage());
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    // Combat settings
    public boolean isCombatEnabled() {
        return config.getBoolean("combat.enabled", true);
    }

    public int getCombatDuration() {
        return config.getInt("combat.duration", 15);
    }

    public List<String> getCombatBannedItems() {
        return config.getStringList("combat.banned-items");
    }

    public boolean shouldKillOnLogout() {
        return config.getBoolean("combat.kill-on-logout", true);
    }

    public boolean shouldShowActionbar() {
        return config.getBoolean("combat.show-actionbar", true);
    }

    public int getActionbarUpdateInterval() {
        return config.getInt("combat.actionbar-update-interval", 10);
    }

    // NEW: Combat region lock settings
    public boolean isCombatRegionLockEnabled() {
        return config.getBoolean("combat.region-lock.enabled", false);
    }

    public List<String> getCombatLockedRegions() {
        return config.getStringList("combat.region-lock.regions");
    }

    // World bans
    public boolean areWorldBansEnabled() {
        return config.getBoolean("world-bans.enabled", true);
    }

    public List<String> getWorldBannedItems(String worldName) {
        return config.getStringList("world-bans.worlds." + worldName.toLowerCase());
    }

    public Set<String> getWorldsWithBans() {
        ConfigurationSection worldsSection = config.getConfigurationSection("world-bans.worlds");
        if (worldsSection == null) {
            return new HashSet<>();
        }
        return worldsSection.getKeys(false);
    }

    public void addWorldBannedItem(String worldName, Material material) {
        List<String> items = getWorldBannedItems(worldName);
        if (!items.contains(material.name())) {
            items.add(material.name());
            config.set("world-bans.worlds." + worldName.toLowerCase(), items);
            saveConfig();
        }
    }

    public void removeWorldBannedItem(String worldName, Material material) {
        List<String> items = getWorldBannedItems(worldName);
        items.remove(material.name());
        config.set("world-bans.worlds." + worldName.toLowerCase(), items);
        saveConfig();
    }

    // Messages
    public boolean areMessagesEnabled() {
        return config.getBoolean("messages.enabled", true);
    }

    // NEW: Separate actionbar control
    public boolean isActionbarEnabled() {
        return config.getBoolean("messages.actionbar-enabled", true);
    }

    public boolean useMinimessage() {
        return config.getBoolean("messages.use-minimessage", true);
    }

    public String getPrefix() {
        return config.getString("messages.prefix", "<red>[ItemBan]</red>");
    }

    public String getMessage(String key) {
        String message = config.getString("messages." + key);
        if (message == null) {
            return "Message not found: " + key;
        }
        return message;
    }

    // Wand settings
    public Material getWandMaterial() {
        try {
            return Material.valueOf(config.getString("wand.material", "DIAMOND_AXE"));
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("Invalid wand material, using DIAMOND_AXE");
            return Material.DIAMOND_AXE;
        }
    }

    public String getWandName() {
        return config.getString("wand.name", "&6&lRegions-Wand");
    }

    public List<String> getWandLore() {
        return config.getStringList("wand.lore");
    }
}