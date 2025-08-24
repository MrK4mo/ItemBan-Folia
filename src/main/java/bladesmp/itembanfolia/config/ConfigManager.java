package bladesmp.itembanfolia.config;

import bladesmp.itembanfolia.ItemBanPlugin;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        // Messages
        defaults.put("messages.enabled", true);
        defaults.put("messages.use-minimessage", true);
        defaults.put("messages.prefix", "<red>[ItemBan]</red>");

        // Message texts
        defaults.put("messages.item-banned-region", "&c&lDieses Item ist in dieser Region nicht erlaubt!");
        defaults.put("messages.item-banned-combat", "&c&lDieses Item ist während des Kampfes nicht erlaubt!");
        defaults.put("messages.wand-received", "&aRegions-Wand erhalten! Linksklick und Rechtsklick zum Markieren.");
        defaults.put("messages.region-created", "&aRegion '&f{name}&a' erfolgreich erstellt!");
        defaults.put("messages.region-deleted", "&aRegion '&f{name}&a' wurde gelöscht!");
        defaults.put("messages.pos1-set", "&aPosition 1 gesetzt: &f{x}, {y}, {z}");
        defaults.put("messages.pos2-set", "&aPosition 2 gesetzt: &f{x}, {y}, {z}");
        defaults.put("messages.no-permission", "&cDu hast keine Berechtigung für diesen Befehl!");
        defaults.put("messages.config-reloaded", "&aKonfiguration erfolgreich neu geladen!");
        defaults.put("messages.in-combat", "&cDu bist jetzt im Kampf für {duration} Sekunden!");
        defaults.put("messages.combat-end", "&aDu bist nicht mehr im Kampf!");

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

    // Convenience methods
    public boolean isCombatEnabled() {
        return config.getBoolean("combat.enabled", true);
    }

    public int getCombatDuration() {
        return config.getInt("combat.duration", 15);
    }

    public List<String> getCombatBannedItems() {
        return config.getStringList("combat.banned-items");
    }

    public boolean areMessagesEnabled() {
        return config.getBoolean("messages.enabled", true);
    }

    public boolean useMinimessage() {
        return config.getBoolean("messages.use-minimessage", true);
    }

    public String getPrefix() {
        return config.getString("messages.prefix", "<red>[ItemBan]</red>");
    }

    public String getMessage(String key) {
        return config.getString("messages." + key, "Message not found: " + key);
    }

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