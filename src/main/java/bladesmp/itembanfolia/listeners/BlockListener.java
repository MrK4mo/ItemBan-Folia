package bladesmp.itembanfolia.listeners;

import bladesmp.itembanfolia.ItemBanPlugin;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockListener implements Listener {

    private final ItemBanPlugin plugin;

    public BlockListener(ItemBanPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockPlace(BlockPlaceEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Material material = block.getType();

        // Skip if player has bypass permission
        if (player.hasPermission("itemban.bypass")) {
            return;
        }

        // Check world restrictions
        if (plugin.getWorldBanManager().isItemBannedInWorld(player.getWorld(), material)) {
            event.setCancelled(true);
            plugin.getMessageUtils().sendMessage(player, "item-banned-world");
            return;
        }

        // Check combat restrictions for placing blocks
        if (plugin.getCombatManager().isPlayerInCombat(player) &&
                plugin.getCombatManager().isItemBannedInCombat(material)) {
            event.setCancelled(true);
            plugin.getMessageUtils().sendMessage(player, "item-banned-combat");
            return;
        }

        // Check region restrictions at the BLOCK LOCATION (not player location)
        if (plugin.getRegionManager().isItemBannedAt(block.getLocation(), material)) {
            event.setCancelled(true);
            plugin.getMessageUtils().sendMessage(player, "item-banned-region");
        }
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent event) {
        Player player = event.getPlayer();
        Block block = event.getBlock();

        // Skip if player has bypass permission
        if (player.hasPermission("itemban.bypass")) {
            return;
        }

        // Check if the block being broken is in a region where tools are banned
        Material toolInHand = player.getInventory().getItemInMainHand().getType();

        // Check region restrictions at the BLOCK LOCATION (not player location)
        if (plugin.getRegionManager().isItemBannedAt(block.getLocation(), toolInHand)) {
            event.setCancelled(true);
            plugin.getMessageUtils().sendMessage(player, "item-banned-region");
            return;
        }

        // Check world restrictions for tool usage
        if (plugin.getWorldBanManager().isItemBannedInWorld(player.getWorld(), toolInHand)) {
            event.setCancelled(true);
            plugin.getMessageUtils().sendMessage(player, "item-banned-world");
            return;
        }

        // Check combat restrictions for tool usage
        if (plugin.getCombatManager().isPlayerInCombat(player) &&
                plugin.getCombatManager().isItemBannedInCombat(toolInHand)) {
            event.setCancelled(true);
            plugin.getMessageUtils().sendMessage(player, "item-banned-combat");
        }
    }
}