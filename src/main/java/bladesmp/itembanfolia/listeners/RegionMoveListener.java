package bladesmp.itembanfolia.listeners;

import bladesmp.itembanfolia.ItemBanPlugin;
import bladesmp.itembanfolia.models.ItemBanRegion;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.entity.Player;

import java.util.List;

public class RegionMoveListener implements Listener {

    private final ItemBanPlugin plugin;

    public RegionMoveListener(ItemBanPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();

        // Skip if player has bypass permission
        if (player.hasPermission("itemban.bypass")) {
            return;
        }

        // Skip if player is not in combat
        if (!plugin.getCombatManager().isPlayerInCombat(player)) {
            return;
        }

        // Skip if region lock is disabled
        if (!plugin.getConfigManager().isCombatRegionLockEnabled()) {
            return;
        }

        // Check if player is trying to leave a locked region
        if (!plugin.getCombatManager().canPlayerLeaveRegion(player)) {
            // Get regions at the TO location
            List<ItemBanRegion> regionsAtTo = plugin.getRegionManager().getRegionsAt(event.getTo());
            List<ItemBanRegion> regionsAtFrom = plugin.getRegionManager().getRegionsAt(event.getFrom());
            List<String> lockedRegions = plugin.getConfigManager().getCombatLockedRegions();

            // Check if player is trying to leave a locked region
            boolean leavingLockedRegion = false;
            boolean enteringLockedRegion = false;

            // Check if FROM location has a locked region
            for (ItemBanRegion region : regionsAtFrom) {
                if (lockedRegions.contains(region.getName())) {
                    leavingLockedRegion = true;
                    break;
                }
            }

            // Check if TO location has a locked region
            for (ItemBanRegion region : regionsAtTo) {
                if (lockedRegions.contains(region.getName())) {
                    enteringLockedRegion = true;
                    break;
                }
            }

            // If player is leaving a locked region but not entering one, cancel the move
            if (leavingLockedRegion && !enteringLockedRegion) {
                event.setCancelled(true);
                plugin.getMessageUtils().sendMessage(player, "combat-region-leave-denied");
            }
        }
    }
}