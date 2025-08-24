package bladesmp.itembanfolia.utils;

import bladesmp.itembanfolia.ItemBanPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtils {

    private final ItemBanPlugin plugin;
    private final MiniMessage miniMessage;
    private final LegacyComponentSerializer legacySerializer;
    private final Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public MessageUtils(ItemBanPlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
        this.legacySerializer = LegacyComponentSerializer.legacyAmpersand();
    }

    public void sendMessage(CommandSender sender, String messageKey) {
        sendMessage(sender, messageKey, new String[0]);
    }

    public void sendMessage(CommandSender sender, String messageKey, String... replacements) {
        if (!plugin.getConfigManager().areMessagesEnabled()) {
            return;
        }

        String message = plugin.getConfigManager().getMessage(messageKey);
        if (message == null || message.isEmpty()) {
            return;
        }

        // Apply replacements
        if (replacements != null && replacements.length > 0) {
            for (int i = 0; i < replacements.length; i += 2) {
                if (i + 1 < replacements.length) {
                    message = message.replace("{" + replacements[i] + "}", replacements[i + 1]);
                }
            }
        }

        // Add prefix if it's not already there
        String prefix = plugin.getConfigManager().getPrefix();
        if (!message.startsWith(prefix) && !prefix.isEmpty()) {
            message = prefix + " " + message;
        }

        Component component = formatMessage(message);
        sender.sendMessage(component);
    }

    public Component formatMessage(String message) {
        if (plugin.getConfigManager().useMinimessage()) {
            // First convert legacy codes to minimessage if present
            message = convertLegacyToMiniMessage(message);
            return miniMessage.deserialize(message);
        } else {
            // Use legacy formatting with hex support
            message = translateHexColors(message);
            return legacySerializer.deserialize(message);
        }
    }

    private String convertLegacyToMiniMessage(String message) {
        // Convert common legacy codes to MiniMessage
        message = message.replace("&0", "<black>")
                .replace("&1", "<dark_blue>")
                .replace("&2", "<dark_green>")
                .replace("&3", "<dark_aqua>")
                .replace("&4", "<dark_red>")
                .replace("&5", "<dark_purple>")
                .replace("&6", "<gold>")
                .replace("&7", "<gray>")
                .replace("&8", "<dark_gray>")
                .replace("&9", "<blue>")
                .replace("&a", "<green>")
                .replace("&b", "<aqua>")
                .replace("&c", "<red>")
                .replace("&d", "<light_purple>")
                .replace("&e", "<yellow>")
                .replace("&f", "<white>")
                .replace("&l", "<bold>")
                .replace("&m", "<strikethrough>")
                .replace("&n", "<underlined>")
                .replace("&o", "<italic>")
                .replace("&r", "<reset>");

        // Convert hex colors to MiniMessage format
        Matcher matcher = hexPattern.matcher(message);
        while (matcher.find()) {
            String hexColor = matcher.group(1);
            message = message.replace("&#" + hexColor, "<color:#" + hexColor + ">");
        }

        return message;
    }

    private String translateHexColors(String message) {
        // Support for &#RRGGBB hex colors - fallback to closest legacy color
        Matcher matcher = hexPattern.matcher(message);
        while (matcher.find()) {
            String hexColor = matcher.group(1);
            // Convert hex to closest ChatColor
            ChatColor closestColor = getClosestChatColor(hexColor);
            message = message.replace("&#" + hexColor, closestColor.toString());
        }

        // Translate legacy color codes
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private ChatColor getClosestChatColor(String hex) {
        // Simple mapping of hex colors to closest ChatColor
        // This is a basic implementation - you could make it more sophisticated
        String upperHex = hex.toUpperCase();

        if (upperHex.startsWith("FF") || upperHex.contains("F") && upperHex.contains("0")) {
            return ChatColor.RED;
        } else if (upperHex.startsWith("00FF") || (upperHex.contains("0") && upperHex.contains("F"))) {
            return ChatColor.GREEN;
        } else if (upperHex.endsWith("FF") || (upperHex.contains("00") && upperHex.contains("FF"))) {
            return ChatColor.BLUE;
        } else if (upperHex.contains("FF")) {
            return ChatColor.YELLOW;
        } else if (upperHex.contains("8")) {
            return ChatColor.GRAY;
        } else if (upperHex.contains("4") || upperHex.contains("3")) {
            return ChatColor.DARK_GRAY;
        } else {
            return ChatColor.WHITE;
        }
    }

    public String formatString(String message) {
        if (plugin.getConfigManager().useMinimessage()) {
            message = convertLegacyToMiniMessage(message);
            Component component = miniMessage.deserialize(message);
            return legacySerializer.serialize(component);
        } else {
            return ChatColor.stripColor(translateHexColors(message));
        }
    }

    public void broadcastMessage(String messageKey, String... replacements) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            sendMessage(player, messageKey, replacements);
        }
    }
}