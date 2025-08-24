package bladesmp.itembanfolia.listeners;

import bladesmp.itembanfolia.ItemBanPlugin;
import bladesmp.itembanfolia.utils.WandUtils;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class RegionListener implements Listener {

    private final ItemBanPlugin plugin;

    public RegionListener(ItemBanPlugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        ItemStack item = event.getItem();

        if (!WandUtils.isWand(item, plugin)) {
            return;
        }

        if (event.getClickedBlock() == null) {
            return;
        }

        event.setCancelled(true);

        if (event.getAction() == Action.LEFT_CLICK_BLOCK) {
            // Set position 1
            plugin.getRegionManager().setPos1(event.getPlayer(), event.getClickedBlock().getLocation());
        } else if (event.getAction() == Action.RIGHT_CLICK_BLOCK) {
            // Set position 2
            plugin.getRegionManager().setPos2(event.getPlayer(), event.getClickedBlock().getLocation());
        }
    }
}