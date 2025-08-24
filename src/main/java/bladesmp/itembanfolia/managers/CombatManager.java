package bladesmp.itembanfolia.managers;

import bladesmp.itembanfolia.ItemBanPlugin;
import org.bukkit.Material;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CombatManager {

    private final ItemBanPlugin plugin;
    private final Map<UUID, Long> combatPlayers;
    private final Set<Material> bannedItemsInCombat;

    public CombatManager(ItemBanPlugin plugin) {
        this.plugin = plugin;
        this.combatPlayers = new ConcurrentHashMap<>();
        this.bannedItemsInCombat = new HashSet<>();

        // Load banned items after construction
        loadBannedItems();
        startCleanupTask();
    }

    private void loadBannedItems() {
        bannedItemsInCombat.clear();

        // Add null check for config manager
        if (plugin.getConfigManager() == null) {
            plugin.getLogger().warning("ConfigManager not initialized yet, using default combat banned items");
            // Add default items
            bannedItemsInCombat.add(Material.ENDER_PEARL);
            bannedItemsInCombat.add(Material.CHORUS_FRUIT);
            bannedItemsInCombat.add(Material.GOLDEN_APPLE);
            return;
        }

        List<String> itemStrings = plugin.getConfigManager().getCombatBannedItems();
        if (itemStrings == null || itemStrings.isEmpty()) {
            plugin.getLogger().info("No combat banned items configured, using defaults");
            bannedItemsInCombat.add(Material.ENDER_PEARL);
            bannedItemsInCombat.add(Material.CHORUS_FRUIT);
            bannedItemsInCombat.add(Material.GOLDEN_APPLE);
            return;
        }

        for (String itemString : itemStrings) {
            try {
                Material material = Material.valueOf(itemString.toUpperCase());
                bannedItemsInCombat.add(material);
            } catch (IllegalArgumentException e) {
                plugin.getLogger().warning("Invalid combat banned item: " + itemString);
            }
        }

        plugin.getLogger().info("Loaded " + bannedItemsInCombat.size() + " combat banned items");
    }

    public void putPlayerInCombat(Player player) {
        if (plugin.getConfigManager() != null && !plugin.getConfigManager().isCombatEnabled()) {
            return;
        }

        UUID playerId = player.getUniqueId();
        int duration = plugin.getConfigManager() != null ? plugin.getConfigManager().getCombatDuration() : 15;
        long combatEndTime = System.currentTimeMillis() + (duration * 1000L);

        boolean wasInCombat = combatPlayers.containsKey(playerId);
        combatPlayers.put(playerId, combatEndTime);

        if (!wasInCombat && plugin.getMessageUtils() != null) {
            plugin.getMessageUtils().sendMessage(player, "in-combat",
                    "duration", String.valueOf(duration)
            );
        }
    }

    public void removePlayerFromCombat(Player player) {
        UUID playerId = player.getUniqueId();
        if (combatPlayers.remove(playerId) != null && plugin.getMessageUtils() != null) {
            plugin.getMessageUtils().sendMessage(player, "combat-end");
        }
    }

    public boolean isPlayerInCombat(Player player) {
        UUID playerId = player.getUniqueId();
        Long combatEndTime = combatPlayers.get(playerId);

        if (combatEndTime == null) {
            return false;
        }

        if (System.currentTimeMillis() >= combatEndTime) {
            combatPlayers.remove(playerId);
            if (plugin.getMessageUtils() != null) {
                plugin.getMessageUtils().sendMessage(player, "combat-end");
            }
            return false;
        }

        return true;
    }

    public boolean isItemBannedInCombat(Material material) {
        return bannedItemsInCombat.contains(material);
    }

    public long getRemainingCombatTime(Player player) {
        UUID playerId = player.getUniqueId();
        Long combatEndTime = combatPlayers.get(playerId);

        if (combatEndTime == null) {
            return 0;
        }

        long remaining = combatEndTime - System.currentTimeMillis();
        return Math.max(0, remaining / 1000);
    }

    public Set<UUID> getCombatPlayers() {
        return new HashSet<>(combatPlayers.keySet());
    }

    public void addBannedItem(Material material) {
        bannedItemsInCombat.add(material);
        // Update config
        if (plugin.getConfigManager() != null) {
            List<String> items = plugin.getConfigManager().getCombatBannedItems();
            if (items != null && !items.contains(material.name())) {
                items.add(material.name());
                plugin.getConfigManager().getConfig().set("combat.banned-items", items);
                plugin.getConfigManager().saveConfig();
            }
        }
    }

    public void removeBannedItem(Material material) {
        bannedItemsInCombat.remove(material);
        // Update config
        if (plugin.getConfigManager() != null) {
            List<String> items = plugin.getConfigManager().getCombatBannedItems();
            if (items != null) {
                items.remove(material.name());
                plugin.getConfigManager().getConfig().set("combat.banned-items", items);
                plugin.getConfigManager().saveConfig();
            }
        }
    }

    public Set<Material> getBannedItems() {
        return new HashSet<>(bannedItemsInCombat);
    }

    private void startCleanupTask() {
        // Clean up expired combat entries every 30 seconds
        Runnable cleanupTask = () -> {
            long currentTime = System.currentTimeMillis();
            Iterator<Map.Entry<UUID, Long>> iterator = combatPlayers.entrySet().iterator();

            while (iterator.hasNext()) {
                Map.Entry<UUID, Long> entry = iterator.next();
                if (currentTime >= entry.getValue()) {
                    iterator.remove();
                    // Notify player if they're online
                    Player player = plugin.getServer().getPlayer(entry.getKey());
                    if (player != null && player.isOnline() && plugin.getMessageUtils() != null) {
                        // Schedule message on player's region for Folia compatibility
                        if (plugin.isFolia()) {
                            plugin.scheduleEntityTask(player, () -> {
                                plugin.getMessageUtils().sendMessage(player, "combat-end");
                            });
                        } else {
                            plugin.getMessageUtils().sendMessage(player, "combat-end");
                        }
                    }
                }
            }
        };

        if (plugin.isFolia()) {
            plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(
                    plugin,
                    scheduledTask -> cleanupTask.run(),
                    600L, // 30 seconds initial delay
                    600L  // 30 seconds period
            );
        } else {
            plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                    plugin,
                    cleanupTask,
                    600L, // 30 seconds initial delay
                    600L  // 30 seconds period
            );
        }
    }

    public void cleanup() {
        combatPlayers.clear();
    }

    public void reloadConfig() {
        loadBannedItems();
    }
}