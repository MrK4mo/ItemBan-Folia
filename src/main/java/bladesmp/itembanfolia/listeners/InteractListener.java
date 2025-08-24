package bladesmp.itembanfolia.listeners;

import bladesmp.itembanfolia.ItemBanPlugin;
import bladesmp.itembanfolia.utils.WandUtils;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;

public class InteractListener implements Listener {

    private final ItemBanPlugin plugin;

    public InteractListener(ItemBanPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        ItemStack item = event.getItem();

        if (item == null || item.getType() == Material.AIR) {
            return;
        }

        // Skip if it's the wand
        if (WandUtils.isWand(item, plugin)) {
            return;
        }

        // Skip if player has bypass permission
        if (player.hasPermission("itemban.bypass")) {
            return;
        }

        Material material = item.getType();

        // Check world restrictions
        if (plugin.getWorldBanManager().isItemBannedInWorld(player.getWorld(), material)) {
            event.setCancelled(true);
            plugin.getMessageUtils().sendMessage(player, "item-banned-world");
            return;
        }

        // Check combat restrictions
        if (plugin.getCombatManager().isPlayerInCombat(player) &&
                plugin.getCombatManager().isItemBannedInCombat(material)) {
            event.setCancelled(true);
            plugin.getMessageUtils().sendMessage(player, "item-banned-combat");
            return;
        }

        // Check region restrictions
        if (plugin.getRegionManager().isItemBannedAt(player.getLocation(), material)) {
            event.setCancelled(true);
            plugin.getMessageUtils().sendMessage(player, "item-banned-region");
            return;
        }

        // Additional check for right-click actions that might not trigger above
        if (event.getAction() == Action.RIGHT_CLICK_AIR || event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            // Check if the item is consumable or has special right-click behavior
            if (isConsumableOrUsable(material)) {
                // World restrictions
                if (plugin.getWorldBanManager().isItemBannedInWorld(player.getWorld(), material)) {
                    event.setCancelled(true);
                    plugin.getMessageUtils().sendMessage(player, "item-banned-world");
                    return;
                }

                // Combat restrictions
                if (plugin.getCombatManager().isPlayerInCombat(player) &&
                        plugin.getCombatManager().isItemBannedInCombat(material)) {
                    event.setCancelled(true);
                    plugin.getMessageUtils().sendMessage(player, "item-banned-combat");
                    return;
                }

                // Region restrictions
                if (plugin.getRegionManager().isItemBannedAt(player.getLocation(), material)) {
                    event.setCancelled(true);
                    plugin.getMessageUtils().sendMessage(player, "item-banned-region");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();
        Material material = event.getItem().getType();

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

        // Check combat restrictions
        if (plugin.getCombatManager().isPlayerInCombat(player) &&
                plugin.getCombatManager().isItemBannedInCombat(material)) {
            event.setCancelled(true);
            plugin.getMessageUtils().sendMessage(player, "item-banned-combat");
            return;
        }

        // Check region restrictions
        if (plugin.getRegionManager().isItemBannedAt(player.getLocation(), material)) {
            event.setCancelled(true);
            plugin.getMessageUtils().sendMessage(player, "item-banned-region");
        }
    }

    private boolean isConsumableOrUsable(Material material) {
        // Check if the material is consumable (food, potions, etc.)
        if (material.isEdible()) {
            return true;
        }

        // Check for specific usable items
        switch (material) {
            case POTION:
            case SPLASH_POTION:
            case LINGERING_POTION:
            case ENDER_PEARL:
            case CHORUS_FRUIT:
            case GOLDEN_APPLE:
            case ENCHANTED_GOLDEN_APPLE:
            case TOTEM_OF_UNDYING:
            case FIREWORK_ROCKET:
            case BOW:
            case CROSSBOW:
            case TRIDENT:
            case SHIELD:
            case FISHING_ROD:
            case FLINT_AND_STEEL:
            case FIRE_CHARGE:
            case SNOWBALL:
            case EGG:
            case BUCKET:
            case WATER_BUCKET:
            case LAVA_BUCKET:
            case MILK_BUCKET:
            case POWDER_SNOW_BUCKET:
            case AXOLOTL_BUCKET:
            case COD_BUCKET:
            case SALMON_BUCKET:
            case PUFFERFISH_BUCKET:
            case TROPICAL_FISH_BUCKET:
            case TADPOLE_BUCKET:
            case ENDER_EYE:
            case EXPERIENCE_BOTTLE:
                return true;
            default:
                return false;
        }
    }
}