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
        defaults.put("combat.actionbar-update-interval", 10);
        defaults.put("combat.region-lock.enabled", false);
        defaults.put("combat.region-lock.regions", Arrays.asList("spawn", "arena"));

        // Ender Pearl cooldown settings
        defaults.put("ender-pearl.enabled", true);
        defaults.put("ender-pearl.cooldown", 5);
        defaults.put("ender-pearl.show-cooldown-message", true);
        defaults.put("ender-pearl.global-cooldown", true);

        // World bans
        defaults.put("world-bans.enabled", true);
        defaults.put("world-bans.worlds.spawn", Arrays.asList("ENDER_CRYSTAL", "TNT", "RESPAWN_ANCHOR"));

        // Messages - Nur MiniMessage
        defaults.put("messages.enabled", true);
        defaults.put("messages.actionbar-enabled", true);
        defaults.put("messages.prefix", "<color:#ff00b1><bold>ITEMBAN</bold></color> <gray>»</gray>");

        // Message texts - Nur MiniMessage Format
        defaults.put("messages.item-banned-region", "<red><bold>Dieses Item ist in dieser Region nicht erlaubt!");
        defaults.put("messages.item-banned-combat", "<red><bold>Dieses Item ist während des Kampfes nicht erlaubt!");
        defaults.put("messages.item-banned-world", "<red><bold>Dieses Item ist in dieser Welt nicht erlaubt!");
        defaults.put("messages.wand-received", "<green>Regions-Wand erhalten! Linksklick und Rechtsklick zum Markieren.");
        defaults.put("messages.region-created", "<green>Region '<white>{name}</white>' erfolgreich erstellt!");
        defaults.put("messages.region-deleted", "<green>Region '<white>{name}</white>' wurde gelöscht!");
        defaults.put("messages.pos1-set", "<green>Position 1 gesetzt: <white>{x}, {y}, {z}</white>");
        defaults.put("messages.pos2-set", "<green>Position 2 gesetzt: <white>{x}, {y}, {z}</white>");
        defaults.put("messages.no-permission", "<red>Du hast keine Berechtigung für diesen Befehl!");
        defaults.put("messages.config-reloaded", "<green>Konfiguration erfolgreich neu geladen!");
        defaults.put("messages.in-combat", "<red>Du bist jetzt im Kampf für {duration} Sekunden!");
        defaults.put("messages.combat-end", "<green>Du bist nicht mehr im Kampf!");
        defaults.put("messages.combat-actionbar", "<color:#fc0000><underlined>COMBAT:</underlined> <white>{time}s</white>");
        defaults.put("messages.combat-logout-death", "<red>{player} ist während des Kampfes offline gegangen und gestorben!");
        defaults.put("messages.combat-region-leave-denied", "<red>Du kannst diese Region nicht während des Kampfes verlassen!");
        defaults.put("messages.ender-pearl-cooldown", "<red><bold>Ender Pearl ist noch für <yellow>{time}</yellow> Sekunden im Cooldown!");

        // Wand settings
        defaults.put("wand.material", "DIAMOND_AXE");
        defaults.put("wand.name", "<gold><bold>Regions-Wand</bold></gold>");
        defaults.put("wand.lore", Arrays.asList(
                "<gray>Linksklick: Position 1 setzen</gray>",
                "<gray>Rechtsklick: Position 2 setzen</gray>",
                "<gray>Verwende <white>/itemban create <name></white> um eine Region zu erstellen</gray>"
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

    public boolean isCombatRegionLockEnabled() {
        return config.getBoolean("combat.region-lock.enabled", false);
    }

    public List<String> getCombatLockedRegions() {
        return config.getStringList("combat.region-lock.regions");
    }

    // Ender Pearl cooldown settings
    public boolean isEnderPearlCooldownEnabled() {
        return config.getBoolean("ender-pearl.enabled", true);
    }

    public int getEnderPearlCooldown() {
        return config.getInt("ender-pearl.cooldown", 5);
    }

    public boolean shouldShowEnderPearlCooldownMessage() {
        return config.getBoolean("ender-pearl.show-cooldown-message", true);
    }

    public boolean isEnderPearlGlobalCooldown() {
        return config.getBoolean("ender-pearl.global-cooldown", true);
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

    public boolean isActionbarEnabled() {
        return config.getBoolean("messages.actionbar-enabled", true);
    }

    // Entfernt: use-minimessage - wir verwenden nur noch MiniMessage
    public boolean useMinimessage() {
        return true; // Immer true
    }

    public String getPrefix() {
        return config.getString("messages.prefix", "<color:#ff00b1><bold>ITEMBAN</bold></color> <gray>»</gray>");
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
        return config.getString("wand.name", "<gold><bold>Regions-Wand</bold></gold>");
    }

    public List<String> getWandLore() {
        return config.getStringList("wand.lore");
    }
}