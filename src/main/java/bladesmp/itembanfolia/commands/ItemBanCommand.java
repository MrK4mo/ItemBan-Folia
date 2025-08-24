package bladesmp.itembanfolia.commands;

import bladesmp.itembanfolia.ItemBanPlugin;
import bladesmp.itembanfolia.models.ItemBanRegion;
import bladesmp.itembanfolia.utils.WandUtils;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.*;
import java.util.stream.Collectors;

public class ItemBanCommand implements CommandExecutor, TabCompleter {

    private final ItemBanPlugin plugin;

    public ItemBanCommand(ItemBanPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("itemban.command")) {
            plugin.getMessageUtils().sendMessage(sender, "no-permission");
            return true;
        }

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "wand":
                return handleWandCommand(sender);
            case "create":
                return handleCreateCommand(sender, args);
            case "delete":
                return handleDeleteCommand(sender, args);
            case "list":
                return handleListCommand(sender);
            case "info":
                return handleInfoCommand(sender, args);
            case "additem":
                return handleAddItemCommand(sender, args);
            case "removeitem":
                return handleRemoveItemCommand(sender, args);
            case "reload":
                return handleReloadCommand(sender);
            case "help":
            default:
                sendHelp(sender);
                return true;
        }
    }

    private boolean handleWandCommand(CommandSender sender) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        Player player = (Player) sender;
        ItemStack wand = WandUtils.createWand(plugin);
        player.getInventory().addItem(wand);
        plugin.getMessageUtils().sendMessage(player, "wand-received");
        return true;
    }

    private boolean handleCreateCommand(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("Usage: /itemban create <name> [item1,item2,...]");
            return true;
        }

        Player player = (Player) sender;
        String regionName = args[1];

        if (!plugin.getRegionManager().hasBothPositions(player)) {
            sender.sendMessage("You need to set both positions first using the wand.");
            return true;
        }

        if (plugin.getRegionManager().getRegion(regionName) != null) {
            sender.sendMessage("A region with that name already exists.");
            return true;
        }

        List<Material> bannedItems = new ArrayList<>();

        // Parse banned items if provided
        if (args.length > 2) {
            String[] itemStrings = args[2].split(",");
            for (String itemString : itemStrings) {
                try {
                    Material material = Material.valueOf(itemString.toUpperCase());
                    bannedItems.add(material);
                } catch (IllegalArgumentException e) {
                    sender.sendMessage("Invalid item: " + itemString);
                }
            }
        }

        if (plugin.getRegionManager().createRegion(player, regionName, bannedItems)) {
            plugin.getMessageUtils().sendMessage(player, "region-created", "name", regionName);
        } else {
            sender.sendMessage("Failed to create region. Make sure both positions are in the same world.");
        }

        return true;
    }

    private boolean handleDeleteCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /itemban delete <name>");
            return true;
        }

        String regionName = args[1];

        if (plugin.getRegionManager().deleteRegion(regionName)) {
            plugin.getMessageUtils().sendMessage(sender, "region-deleted", "name", regionName);
        } else {
            sender.sendMessage("Region '" + regionName + "' not found.");
        }

        return true;
    }

    private boolean handleListCommand(CommandSender sender) {
        Collection<ItemBanRegion> regions = plugin.getRegionManager().getAllRegions();

        if (regions.isEmpty()) {
            sender.sendMessage("No regions found.");
            return true;
        }

        sender.sendMessage("§6=== ItemBan Regions ===");
        for (ItemBanRegion region : regions) {
            sender.sendMessage("§e" + region.getName() + " §7- " + region.getBannedItems().size() + " banned items");
        }

        return true;
    }

    private boolean handleInfoCommand(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage("Usage: /itemban info <name>");
            return true;
        }

        String regionName = args[1];
        ItemBanRegion region = plugin.getRegionManager().getRegion(regionName);

        if (region == null) {
            sender.sendMessage("Region '" + regionName + "' not found.");
            return true;
        }

        sender.sendMessage("§6=== Region Info: " + region.getName() + " ===");
        sender.sendMessage("§eWorld: §f" + region.getMinLocation().getWorld().getName());
        sender.sendMessage("§eMin: §f" + region.getMinLocation().getBlockX() + ", " +
                region.getMinLocation().getBlockY() + ", " + region.getMinLocation().getBlockZ());
        sender.sendMessage("§eMax: §f" + region.getMaxLocation().getBlockX() + ", " +
                region.getMaxLocation().getBlockY() + ", " + region.getMaxLocation().getBlockZ());
        sender.sendMessage("§eBanned Items: §f" + region.getBannedItems().size());

        if (!region.getBannedItems().isEmpty()) {
            String items = region.getBannedItems().stream()
                    .map(Material::name)
                    .collect(Collectors.joining(", "));
            sender.sendMessage("§7Items: " + items);
        }

        return true;
    }

    private boolean handleAddItemCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /itemban additem <region> <item>");
            return true;
        }

        String regionName = args[1];
        String itemName = args[2].toUpperCase();

        ItemBanRegion region = plugin.getRegionManager().getRegion(regionName);
        if (region == null) {
            sender.sendMessage("Region '" + regionName + "' not found.");
            return true;
        }

        try {
            Material material = Material.valueOf(itemName);
            region.addBannedItem(material);
            plugin.getRegionManager().saveRegions();
            sender.sendMessage("§aAdded " + material.name() + " to banned items in region " + regionName);
        } catch (IllegalArgumentException e) {
            sender.sendMessage("Invalid item: " + itemName);
        }

        return true;
    }

    private boolean handleRemoveItemCommand(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage("Usage: /itemban removeitem <region> <item>");
            return true;
        }

        String regionName = args[1];
        String itemName = args[2].toUpperCase();

        ItemBanRegion region = plugin.getRegionManager().getRegion(regionName);
        if (region == null) {
            sender.sendMessage("Region '" + regionName + "' not found.");
            return true;
        }

        try {
            Material material = Material.valueOf(itemName);
            region.removeBannedItem(material);
            plugin.getRegionManager().saveRegions();
            sender.sendMessage("§aRemoved " + material.name() + " from banned items in region " + regionName);
        } catch (IllegalArgumentException e) {
            sender.sendMessage("Invalid item: " + itemName);
        }

        return true;
    }

    private boolean handleReloadCommand(CommandSender sender) {
        try {
            // Safe reload with null checks and error handling
            plugin.getLogger().info("Starting configuration reload...");

            if (plugin.getConfigManager() != null) {
                plugin.getConfigManager().loadConfig();
                plugin.getLogger().info("Config manager reloaded");
            }

            if (plugin.getRegionManager() != null) {
                plugin.getRegionManager().loadRegions();
                plugin.getLogger().info("Region manager reloaded");
            }

            if (plugin.getCombatManager() != null) {
                plugin.getCombatManager().reloadConfig();
                plugin.getLogger().info("Combat manager reloaded");
            }

            if (plugin.getEnderPearlManager() != null) {
                plugin.getEnderPearlManager().reloadConfig();
                plugin.getLogger().info("Ender Pearl manager reloaded");
            }

            if (plugin.getWorldBanManager() != null) {
                plugin.getWorldBanManager().reloadConfig();
                plugin.getLogger().info("World ban manager reloaded");
            }

            plugin.getMessageUtils().sendMessage(sender, "config-reloaded");
            plugin.getLogger().info("Plugin configuration successfully reloaded");

        } catch (Exception e) {
            plugin.getLogger().severe("Error during reload: " + e.getMessage());
            e.printStackTrace();
            sender.sendMessage("§cError during reload: " + e.getMessage());
        }
        return true;
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage("§6=== ItemBan Commands ===");
        sender.sendMessage("§e/itemban wand §7- Get the region selection wand");
        sender.sendMessage("§e/itemban create <name> [items] §7- Create a new region");
        sender.sendMessage("§e/itemban delete <name> §7- Delete a region");
        sender.sendMessage("§e/itemban list §7- List all regions");
        sender.sendMessage("§e/itemban info <name> §7- Show region information");
        sender.sendMessage("§e/itemban additem <region> <item> §7- Add banned item to region");
        sender.sendMessage("§e/itemban removeitem <region> <item> §7- Remove banned item from region");
        sender.sendMessage("§e/itemban reload §7- Reload configuration");
        sender.sendMessage("§e/itemban help §7- Show this help");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            completions.addAll(Arrays.asList("wand", "create", "delete", "list", "info", "additem", "removeitem", "reload", "help"));
        } else if (args.length == 2) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("delete") || subCommand.equals("info") ||
                    subCommand.equals("additem") || subCommand.equals("removeitem")) {
                completions.addAll(plugin.getRegionManager().getAllRegions().stream()
                        .map(ItemBanRegion::getName)
                        .collect(Collectors.toList()));
            }
        } else if (args.length == 3) {
            String subCommand = args[0].toLowerCase();
            if (subCommand.equals("additem") || subCommand.equals("removeitem")) {
                completions.addAll(Arrays.stream(Material.values())
                        .map(Material::name)
                        .filter(name -> name.toLowerCase().startsWith(args[2].toLowerCase()))
                        .collect(Collectors.toList()));
            }
        }

        return completions.stream()
                .filter(s -> s.toLowerCase().startsWith(args[args.length - 1].toLowerCase()))
                .collect(Collectors.toList());
    }
}