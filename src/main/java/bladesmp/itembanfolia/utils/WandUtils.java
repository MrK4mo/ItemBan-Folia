package bladesmp.itembanfolia.utils;

import bladesmp.itembanfolia.ItemBanPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public class WandUtils {

    public static final String WAND_KEY = "itemban_wand";

    public static ItemStack createWand(ItemBanPlugin plugin) {
        Material material = plugin.getConfigManager().getWandMaterial();
        ItemStack wand = new ItemStack(material);
        ItemMeta meta = wand.getItemMeta();

        if (meta != null) {
            // Set display name using legacy format (compatible)
            String name = plugin.getConfigManager().getWandName();
            String legacyName = ChatColor.translateAlternateColorCodes('&', name);
            meta.setDisplayName(legacyName);

            // Set lore using legacy format
            List<String> loreStrings = plugin.getConfigManager().getWandLore();
            List<String> legacyLore = new ArrayList<>();
            for (String loreString : loreStrings) {
                legacyLore.add(ChatColor.translateAlternateColorCodes('&', loreString));
            }
            meta.setLore(legacyLore);

            // Set persistent data to identify as wand
            NamespacedKey key = new NamespacedKey(plugin, WAND_KEY);
            meta.getPersistentDataContainer().set(key, PersistentDataType.BYTE, (byte) 1);

            wand.setItemMeta(meta);
        }

        return wand;
    }

    public static boolean isWand(ItemStack item, ItemBanPlugin plugin) {
        if (item == null || !item.hasItemMeta()) {
            return false;
        }

        ItemMeta meta = item.getItemMeta();
        NamespacedKey key = new NamespacedKey(plugin, WAND_KEY);

        return meta.getPersistentDataContainer().has(key, PersistentDataType.BYTE);
    }
}