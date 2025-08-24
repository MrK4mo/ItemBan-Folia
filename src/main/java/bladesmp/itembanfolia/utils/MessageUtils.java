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
        if (message == null || message.isEmpty() || message.startsWith("Message not found:")) {
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

        // Convert to legacy string format instead of Component
        String formattedMessage = formatMessage(message);

        // Use legacy sendMessage method with String
        sender.sendMessage(formattedMessage);
    }

    public String formatMessage(String message) {
        if (plugin.getConfigManager().useMinimessage()) {
            // First handle MiniMessage format properly
            try {
                // Convert legacy codes to MiniMessage format for compatibility
                String miniMessageText = convertLegacyToMiniMessage(message);
                Component component = miniMessage.deserialize(miniMessageText);
                return legacySerializer.serialize(component);
            } catch (Exception e) {
                // Fallback to legacy if MiniMessage fails
                return formatLegacyMessage(message);
            }
        } else {
            return formatLegacyMessage(message);
        }
    }

    public Component formatMessageToComponent(String message) {
        if (plugin.getConfigManager().useMinimessage()) {
            try {
                // First convert legacy codes to MiniMessage format for compatibility
                message = convertLegacyToMiniMessage(message);
                return miniMessage.deserialize(message);
            } catch (Exception e) {
                // Fallback to legacy component
                return legacySerializer.deserialize(formatLegacyMessage(message));
            }
        } else {
            return legacySerializer.deserialize(formatLegacyMessage(message));
        }
    }

    private String formatLegacyMessage(String message) {
        // Handle hex colors first
        message = translateHexColors(message);
        // Translate legacy color codes
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private String convertLegacyToMiniMessage(String message) {
        // First handle hex colors (#fc0000 format)
        Pattern hexPattern2 = Pattern.compile("<#([A-Fa-f0-9]{6})>");
        Matcher hexMatcher = hexPattern2.matcher(message);
        while (hexMatcher.find()) {
            String hexColor = hexMatcher.group(1);
            message = message.replace("<#" + hexColor + ">", "<color:#" + hexColor + ">");
        }

        // Handle &#RRGGBB format
        Matcher matcher = hexPattern.matcher(message);
        while (matcher.find()) {
            String hexColor = matcher.group(1);
            message = message.replace("&#" + hexColor, "<color:#" + hexColor + ">");
        }

        // Convert legacy codes to MiniMessage format (only if not already in MiniMessage format)
        if (!message.contains("<color:") && !message.contains("<red>")) {
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

        return message;
    }

    private ChatColor getClosestChatColor(String hex) {
        // Simple mapping of hex colors to closest ChatColor
        String upperHex = hex.toUpperCase();

        if (upperHex.startsWith("FC") || upperHex.startsWith("FF")) {
            return ChatColor.RED;
        } else if (upperHex.startsWith("00") && upperHex.contains("FF")) {
            return ChatColor.GREEN;
        } else if (upperHex.endsWith("FF") && upperHex.startsWith("00")) {
            return ChatColor.BLUE;
        } else if (upperHex.contains("FF")) {
            return ChatColor.YELLOW;
        } else if (upperHex.contains("8") || upperHex.contains("7")) {
            return ChatColor.GRAY;
        } else if (upperHex.contains("4") || upperHex.contains("3")) {
            return ChatColor.DARK_GRAY;
        } else {
            return ChatColor.WHITE;
        }
    }

    public String formatString(String message) {
        return formatMessage(message);
    }

    public void broadcastMessage(String messageKey, String... replacements) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            sendMessage(player, messageKey, replacements);
        }
    }
}