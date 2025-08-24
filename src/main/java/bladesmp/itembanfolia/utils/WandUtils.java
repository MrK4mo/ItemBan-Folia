package bladesmp.itembanfolia.utils;

import bladesmp.itembanfolia.ItemBanPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
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
            // Set display name using MiniMessage -> Legacy conversion
            String name = plugin.getConfigManager().getWandName();
            try {
                Component nameComponent = MiniMessage.miniMessage().deserialize(name);
                LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacySection();
                String legacyName = legacySerializer.serialize(nameComponent);
                meta.setDisplayName(legacyName);
            } catch (Exception e) {
                // Fallback to raw name
                meta.setDisplayName(name);
            }

            // Set lore using MiniMessage -> Legacy conversion
            List<String> loreStrings = plugin.getConfigManager().getWandLore();
            List<String> legacyLore = new ArrayList<>();

            for (String loreString : loreStrings) {
                try {
                    Component loreComponent = MiniMessage.miniMessage().deserialize(loreString);
                    LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacySection();
                    String legacyLoreString = legacySerializer.serialize(loreComponent);
                    legacyLore.add(legacyLoreString);
                } catch (Exception e) {
                    legacyLore.add(loreString);
                }
            }

            // Add hardcoded credit line
            try {
                Component creditComponent = MiniMessage.miniMessage().deserialize("<dark_gray>ItemBan Plugin by MrEnte_</dark_gray>");
                LegacyComponentSerializer legacySerializer = LegacyComponentSerializer.legacySection();
                String legacyCredit = legacySerializer.serialize(creditComponent);
                legacyLore.add(legacyCredit);
            } catch (Exception e) {
                legacyLore.add("§8ItemBan Plugin by MrEnte_");
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