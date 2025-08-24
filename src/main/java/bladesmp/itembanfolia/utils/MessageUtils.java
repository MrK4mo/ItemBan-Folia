package bladesmp.itembanfolia.utils;

import bladesmp.itembanfolia.ItemBanPlugin;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MessageUtils {

    private final ItemBanPlugin plugin;
    private final MiniMessage miniMessage;

    public MessageUtils(ItemBanPlugin plugin) {
        this.plugin = plugin;
        this.miniMessage = MiniMessage.miniMessage();
    }

    public void sendMessage(CommandSender sender, String messageKey) {
        sendMessage(sender, messageKey, new String[0]);
    }

    public void sendMessage(CommandSender sender, String messageKey, String... replacements) {
        if (plugin.getConfigManager() == null || !plugin.getConfigManager().areMessagesEnabled()) {
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

        // Add prefix if it's not already there and prefix is not empty
        String prefix = plugin.getConfigManager().getPrefix();
        if (!message.startsWith(prefix) && !prefix.isEmpty()) {
            message = prefix + " " + message;
        }

        // Send raw MiniMessage string directly (fallback to string method)
        try {
            // Try to deserialize MiniMessage and convert back to legacy string
            Component component = miniMessage.deserialize(message);

            // Use legacy serializer to convert to string with color codes
            net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer legacySerializer =
                    net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection();
            String legacyMessage = legacySerializer.serialize(component);

            // Send as string (compatible with all server versions)
            sender.sendMessage(legacyMessage);

        } catch (Exception e) {
            // Final fallback: Send raw message
            plugin.getLogger().warning("Failed to send MiniMessage: " + message + " - Error: " + e.getMessage());
            sender.sendMessage(message);
        }
    }

    public String formatMessage(String message) {
        try {
            Component component = miniMessage.deserialize(message);
            return PlainTextComponentSerializer.plainText().serialize(component);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to format MiniMessage: " + message + " - Error: " + e.getMessage());
            return message;
        }
    }

    public Component formatMessageToComponent(String message) {
        try {
            return miniMessage.deserialize(message);
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to parse MiniMessage component: " + message + " - Error: " + e.getMessage());
            return Component.text(message);
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