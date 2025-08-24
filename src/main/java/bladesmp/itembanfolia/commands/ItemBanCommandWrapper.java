package bladesmp.itembanfolia.commands;

import bladesmp.itembanfolia.ItemBanPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class ItemBanCommandWrapper extends Command {

    private final ItemBanCommand executor;

    public ItemBanCommandWrapper(ItemBanPlugin plugin) {
        super("itemban");
        this.executor = new ItemBanCommand(plugin);

        // Set command properties
        setDescription("ItemBan main command");
        setUsage("/itemban <wand|reload|help>");
        setPermission("itemban.command");
        setAliases(List.of("ib"));
    }

    @Override
    public boolean execute(@NotNull CommandSender sender, @NotNull String commandLabel, @NotNull String[] args) {
        return executor.onCommand(sender, this, commandLabel, args);
    }

    @Override
    public @Nullable List<String> tabComplete(@NotNull CommandSender sender, @NotNull String alias, @NotNull String[] args) {
        return executor.onTabComplete(sender, this, alias, args);
    }
}