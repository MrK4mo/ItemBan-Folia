package bladesmp.itembanfolia.listeners;

import bladesmp.itembanfolia.ItemBanPlugin;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class CombatListener implements Listener {

    private final ItemBanPlugin plugin;

    public CombatListener(ItemBanPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!plugin.getConfigManager().isCombatEnabled()) {
            return;
        }

        // Check if a player was damaged by another player
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player victim = (Player) event.getEntity();
            Player attacker = (Player) event.getDamager();

            // Put both players in combat
            plugin.getCombatManager().putPlayerInCombat(victim);
            plugin.getCombatManager().putPlayerInCombat(attacker);
        }
        // Check if a player was damaged by another entity (like projectiles shot by players)
        else if (event.getEntity() instanceof Player) {
            Player victim = (Player) event.getEntity();

            // Check if the damager is a projectile shot by a player
            if (event.getDamager() instanceof org.bukkit.entity.Projectile) {
                org.bukkit.entity.Projectile projectile = (org.bukkit.entity.Projectile) event.getDamager();
                if (projectile.getShooter() instanceof Player) {
                    Player attacker = (Player) projectile.getShooter();

                    // Put both players in combat
                    plugin.getCombatManager().putPlayerInCombat(victim);
                    plugin.getCombatManager().putPlayerInCombat(attacker);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // Remove player from combat when they leave
        plugin.getCombatManager().removePlayerFromCombat(event.getPlayer());

        // Clear their region selection positions
        plugin.getRegionManager().clearPositions(event.getPlayer());
    }
}