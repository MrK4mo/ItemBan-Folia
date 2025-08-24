package bladesmp.itembanfolia.utils;

import bladesmp.itembanfolia.ItemBanPlugin;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageUtils {

    private final ItemBanPlugin plugin;
    private final Pattern hexPattern = Pattern.compile("&#([A-Fa-f0-9]{6})");

    public MessageUtils(ItemBanPlugin plugin) {
        this.plugin = plugin;
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

        String formattedMessage = formatMessage(message);
        sender.sendMessage(formattedMessage);
    }

    public String formatMessage(String message) {
        // Handle hex colors first
        message = translateHexColors(message);

        // Convert MiniMessage format to legacy if present
        if (plugin.getConfigManager().useMinimessage()) {
            message = convertMiniMessageToLegacy(message);
        }

        // Translate legacy color codes
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    private String convertMiniMessageToLegacy(String message) {
        // Convert common MiniMessage tags to legacy codes
        return message.replace("<black>", "&0")
                .replace("<dark_blue>", "&1")
                .replace("<dark_green>", "&2")
                .replace("<dark_aqua>", "&3")
                .replace("<dark_red>", "&4")
                .replace("<dark_purple>", "&5")
                .replace("<gold>", "&6")
                .replace("<gray>", "&7")
                .replace("<dark_gray>", "&8")
                .replace("<blue>", "&9")
                .replace("<green>", "&a")
                .replace("<aqua>", "&b")
                .replace("<red>", "&c")
                .replace("<light_purple>", "&d")
                .replace("<yellow>", "&e")
                .replace("<white>", "&f")
                .replace("<bold>", "&l")
                .replace("<strikethrough>", "&m")
                .replace("<underlined>", "&n")
                .replace("<italic>", "&o")
                .replace("<reset>", "&r")
                .replaceAll("<color:#([A-Fa-f0-9]{6})>", "&#$1");
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

        if (upperHex.startsWith("FF") && upperHex.contains("0")) {
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
        return ChatColor.stripColor(formatMessage(message));
    }

    public void broadcastMessage(String messageKey, String... replacements) {
        for (Player player : plugin.getServer().getOnlinePlayers()) {
            sendMessage(player, messageKey, replacements);
        }
    }
}