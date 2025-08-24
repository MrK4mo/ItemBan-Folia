package bladesmp.itembanfolia.managers;

import bladesmp.itembanfolia.ItemBanPlugin;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class EnderPearlManager {

    private final ItemBanPlugin plugin;
    private final Map<UUID, Long> cooldowns;

    public EnderPearlManager(ItemBanPlugin plugin) {
        this.plugin = plugin;
        this.cooldowns = new ConcurrentHashMap<>();
        startCleanupTask();
    }

    public boolean canUseEnderPearl(Player player) {
        if (!plugin.getConfigManager().isEnderPearlCooldownEnabled()) {
            return true;
        }

        // Check if global cooldown or only in specific contexts
        if (!plugin.getConfigManager().isEnderPearlGlobalCooldown()) {
            // Only apply cooldown in combat or regions
            boolean inCombat = plugin.getCombatManager().isPlayerInCombat(player);
            boolean inRegion = plugin.getRegionManager().getRegionsAt(player.getLocation()).size() > 0;

            if (!inCombat && !inRegion) {
                return true; // No cooldown outside combat/regions
            }
        }

        UUID playerId = player.getUniqueId();
        Long cooldownEnd = cooldowns.get(playerId);

        if (cooldownEnd == null) {
            return true;
        }

        return System.currentTimeMillis() >= cooldownEnd;
    }

    public void useEnderPearl(Player player) {
        if (!plugin.getConfigManager().isEnderPearlCooldownEnabled()) {
            return;
        }

        UUID playerId = player.getUniqueId();
        int cooldownSeconds = plugin.getConfigManager().getEnderPearlCooldown();
        long cooldownEnd = System.currentTimeMillis() + (cooldownSeconds * 1000L);

        cooldowns.put(playerId, cooldownEnd);
    }

    public long getRemainingCooldown(Player player) {
        UUID playerId = player.getUniqueId();
        Long cooldownEnd = cooldowns.get(playerId);

        if (cooldownEnd == null) {
            return 0;
        }

        long remaining = cooldownEnd - System.currentTimeMillis();
        return Math.max(0, remaining / 1000);
    }

    public void sendCooldownMessage(Player player) {
        if (!plugin.getConfigManager().shouldShowEnderPearlCooldownMessage()) {
            return;
        }

        long remaining = getRemainingCooldown(player);
        plugin.getMessageUtils().sendMessage(player, "ender-pearl-cooldown",
                "time", String.valueOf(remaining));
    }

    private void startCleanupTask() {
        // Clean up expired cooldowns every 30 seconds
        Runnable cleanupTask = () -> {
            long currentTime = System.currentTimeMillis();
            cooldowns.entrySet().removeIf(entry -> currentTime >= entry.getValue());
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
        cooldowns.clear();
    }

    public void reloadConfig() {
        // Nothing specific to reload for now
    }
}