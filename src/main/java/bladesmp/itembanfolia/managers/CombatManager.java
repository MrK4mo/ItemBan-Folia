package bladesmp.itembanfolia.managers;

import bladesmp.itembanfolia.ItemBanPlugin;
import bladesmp.itembanfolia.models.ItemBanRegion;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class CombatManager {

    private final ItemBanPlugin plugin;
    private final Map<UUID, Long> combatPlayers;
    private final Set<Material> bannedItemsInCombat;
    private final Set<UUID> playersWithActionbar;
    private final Set<UUID> playersToKillOnRejoin; // NEW: Track players who should die on rejoin

    public CombatManager(ItemBanPlugin plugin) {
        this.plugin = plugin;
        this.combatPlayers = new ConcurrentHashMap<>();
        this.bannedItemsInCombat = new HashSet<>();
        this.playersWithActionbar = ConcurrentHashMap.newKeySet();
        this.playersToKillOnRejoin = ConcurrentHashMap.newKeySet(); // NEW

        // Load banned items after construction
        loadBannedItems();
        startCleanupTask();
        startActionbarTask();
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

        // Add to actionbar tracking (use separate actionbar control)
        if (plugin.getConfigManager() != null && plugin.getConfigManager().shouldShowActionbar() && plugin.getConfigManager().isActionbarEnabled()) {
            playersWithActionbar.add(playerId);
        }

        if (!wasInCombat && plugin.getMessageUtils() != null) {
            plugin.getMessageUtils().sendMessage(player, "in-combat",
                    "duration", String.valueOf(duration)
            );
        }
    }

    public void removePlayerFromCombat(Player player) {
        UUID playerId = player.getUniqueId();
        playersWithActionbar.remove(playerId);

        if (combatPlayers.remove(playerId) != null && plugin.getMessageUtils() != null) {
            plugin.getMessageUtils().sendMessage(player, "combat-end");
            // Clear actionbar
            if (plugin.getConfigManager() != null && plugin.getConfigManager().shouldShowActionbar() && plugin.getConfigManager().isActionbarEnabled()) {
                player.sendActionBar("");
            }
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
            playersWithActionbar.remove(playerId);
            if (plugin.getMessageUtils() != null) {
                plugin.getMessageUtils().sendMessage(player, "combat-end");
                // Clear actionbar
                if (plugin.getConfigManager() != null && plugin.getConfigManager().shouldShowActionbar() && plugin.getConfigManager().isActionbarEnabled()) {
                    player.sendActionBar("");
                }
            }
            return false;
        }

        return true;
    }

    // NEW: Check if player can leave region during combat
    public boolean canPlayerLeaveRegion(Player player) {
        if (!isPlayerInCombat(player)) {
            return true; // Not in combat, can leave
        }

        if (plugin.getConfigManager() == null || !plugin.getConfigManager().isCombatRegionLockEnabled()) {
            return true; // Region lock disabled
        }

        // Check if player is in a locked region
        List<ItemBanRegion> regionsAt = plugin.getRegionManager().getRegionsAt(player.getLocation());
        List<String> lockedRegions = plugin.getConfigManager().getCombatLockedRegions();

        for (ItemBanRegion region : regionsAt) {
            if (lockedRegions.contains(region.getName())) {
                return false; // Player is in a locked region and in combat
            }
        }

        return true; // Not in a locked region
    }

    public void handlePlayerLogout(Player player) {
        UUID playerId = player.getUniqueId();

        if (combatPlayers.containsKey(playerId)) {
            // NEW: Check if player is in a locked region
            boolean inLockedRegion = false;
            if (plugin.getConfigManager() != null && plugin.getConfigManager().isCombatRegionLockEnabled()) {
                List<ItemBanRegion> regionsAt = plugin.getRegionManager().getRegionsAt(player.getLocation());
                List<String> lockedRegions = plugin.getConfigManager().getCombatLockedRegions();

                for (ItemBanRegion region : regionsAt) {
                    if (lockedRegions.contains(region.getName())) {
                        inLockedRegion = true;
                        break;
                    }
                }
            }

            // Handle combat logging based on region lock status
            if (!inLockedRegion && plugin.getConfigManager() != null && plugin.getConfigManager().shouldKillOnLogout()) {
                // Broadcast message about combat logging
                if (plugin.getMessageUtils() != null) {
                    plugin.getMessageUtils().broadcastMessage("combat-logout-death",
                            "player", player.getName());
                }

                // Drop inventory contents immediately
                ItemStack[] inventoryContents = player.getInventory().getContents();
                for (ItemStack item : inventoryContents) {
                    if (item != null && item.getType() != Material.AIR) {
                        player.getWorld().dropItemNaturally(player.getLocation(), item);
                    }
                }

                // Drop armor contents
                ItemStack[] armorContents = player.getInventory().getArmorContents();
                for (ItemStack item : armorContents) {
                    if (item != null && item.getType() != Material.AIR) {
                        player.getWorld().dropItemNaturally(player.getLocation(), item);
                    }
                }

                // Clear inventory immediately
                player.getInventory().clear();

                // Mark player for death on rejoin
                playersToKillOnRejoin.add(playerId);
            }

            // Remove from combat
            combatPlayers.remove(playerId);
            playersWithActionbar.remove(playerId);
        }
    }

    // NEW: Handle player rejoining after combat logging
    public void handlePlayerRejoin(Player player) {
        UUID playerId = player.getUniqueId();

        if (playersToKillOnRejoin.contains(playerId)) {
            // Kill the player
            player.setHealth(0.0);
            playersToKillOnRejoin.remove(playerId);

            plugin.getLogger().info("Player " + player.getName() + " died due to combat logging");
        }
    }

    private void startActionbarTask() {
        // Use separate actionbar control instead of general messages control
        if (plugin.getConfigManager() == null || !plugin.getConfigManager().shouldShowActionbar() || !plugin.getConfigManager().isActionbarEnabled()) {
            return;
        }

        int interval = plugin.getConfigManager().getActionbarUpdateInterval();

        Runnable actionbarTask = () -> {
            long currentTime = System.currentTimeMillis();

            for (UUID playerId : new HashSet<>(playersWithActionbar)) {
                Player player = plugin.getServer().getPlayer(playerId);
                if (player == null || !player.isOnline()) {
                    playersWithActionbar.remove(playerId);
                    continue;
                }

                Long combatEndTime = combatPlayers.get(playerId);
                if (combatEndTime == null) {
                    playersWithActionbar.remove(playerId);
                    player.sendActionBar("");
                    continue;
                }

                long remaining = (combatEndTime - currentTime) / 1000;
                if (remaining <= 0) {
                    playersWithActionbar.remove(playerId);
                    player.sendActionBar("");
                    continue;
                }

                // Get formatted actionbar message from config without debug spam
                String actionbarMessage = plugin.getConfigManager().getMessage("combat-actionbar")
                        .replace("{time}", String.valueOf(remaining));

                // Use MessageUtils to format the actionbar properly
                String formattedMessage = plugin.getMessageUtils().formatMessage(actionbarMessage);
                player.sendActionBar(formattedMessage);
            }
        };

        if (plugin.isFolia()) {
            plugin.getServer().getGlobalRegionScheduler().runAtFixedRate(
                    plugin,
                    scheduledTask -> actionbarTask.run(),
                    interval,
                    interval
            );
        } else {
            plugin.getServer().getScheduler().runTaskTimerAsynchronously(
                    plugin,
                    actionbarTask,
                    interval,
                    interval
            );
        }
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
                    playersWithActionbar.remove(entry.getKey());

                    // Notify player if they're online
                    Player player = plugin.getServer().getPlayer(entry.getKey());
                    if (player != null && player.isOnline() && plugin.getMessageUtils() != null) {
                        // Schedule message on player's region for Folia compatibility
                        if (plugin.isFolia()) {
                            plugin.scheduleEntityTask(player, () -> {
                                plugin.getMessageUtils().sendMessage(player, "combat-end");
                                if (plugin.getConfigManager() != null && plugin.getConfigManager().shouldShowActionbar() && plugin.getConfigManager().isActionbarEnabled()) {
                                    player.sendActionBar("");
                                }
                            });
                        } else {
                            plugin.getMessageUtils().sendMessage(player, "combat-end");
                            if (plugin.getConfigManager() != null && plugin.getConfigManager().shouldShowActionbar() && plugin.getConfigManager().isActionbarEnabled()) {
                                player.sendActionBar("");
                            }
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
        playersWithActionbar.clear();
        playersToKillOnRejoin.clear();
    }

    public void reloadConfig() {
        loadBannedItems();
    }
}