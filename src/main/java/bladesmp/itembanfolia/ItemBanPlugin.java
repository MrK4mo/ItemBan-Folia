package bladesmp.itembanfolia;

import bladesmp.itembanfolia.commands.ItemBanCommand;
import bladesmp.itembanfolia.config.ConfigManager;
import bladesmp.itembanfolia.listeners.CombatListener;
import bladesmp.itembanfolia.listeners.InteractListener;
import bladesmp.itembanfolia.listeners.RegionListener;
import bladesmp.itembanfolia.managers.CombatManager;
import bladesmp.itembanfolia.managers.RegionManager;
import bladesmp.itembanfolia.utils.MessageUtils;
import org.bukkit.plugin.java.JavaPlugin;

public class ItemBanPlugin extends JavaPlugin {

    private static ItemBanPlugin instance;
    private ConfigManager configManager;
    private RegionManager regionManager;
    private CombatManager combatManager;
    private MessageUtils messageUtils;

    @Override
    public void onEnable() {
        instance = this;

        // Initialize config manager first and load config
        this.configManager = new ConfigManager(this);
        configManager.loadConfig();

        // Initialize other managers after config is loaded
        this.regionManager = new RegionManager(this);
        this.messageUtils = new MessageUtils(this);
        this.combatManager = new CombatManager(this);

        // Load regions after all managers are initialized
        regionManager.loadRegions();

        // Register commands programmatically (Paper plugin requirement)
        getServer().getCommandMap().register("itemban", new bladesmp.itembanfolia.commands.ItemBanCommandWrapper(this));

        // Register listeners
        getServer().getPluginManager().registerEvents(new RegionListener(this), this);
        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new InteractListener(this), this);

        getLogger().info("ItemBan Plugin enabled successfully!");
        getLogger().info("Folia support: " + isFolia());
    }

    @Override
    public void onDisable() {
        if (regionManager != null) {
            regionManager.saveRegions();
        }
        if (combatManager != null) {
            combatManager.cleanup();
        }
        getLogger().info("ItemBan Plugin disabled!");
    }

    public static ItemBanPlugin getInstance() {
        return instance;
    }

    public ConfigManager getConfigManager() {
        return configManager;
    }

    public RegionManager getRegionManager() {
        return regionManager;
    }

    public CombatManager getCombatManager() {
        return combatManager;
    }

    public MessageUtils getMessageUtils() {
        return messageUtils;
    }

    public boolean isFolia() {
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            return true;
        } catch (ClassNotFoundException e) {
            return false;
        }
    }

    public void scheduleTask(Runnable task) {
        if (isFolia()) {
            getServer().getGlobalRegionScheduler().run(this, scheduledTask -> task.run());
        } else {
            getServer().getScheduler().runTask(this, task);
        }
    }

    public void scheduleDelayedTask(Runnable task, long delay) {
        if (isFolia()) {
            getServer().getGlobalRegionScheduler().runDelayed(this, scheduledTask -> task.run(), delay);
        } else {
            getServer().getScheduler().runTaskLater(this, task, delay);
        }
    }

    public void scheduleAsyncTask(Runnable task) {
        if (isFolia()) {
            getServer().getAsyncScheduler().runNow(this, scheduledTask -> task.run());
        } else {
            getServer().getScheduler().runTaskAsynchronously(this, task);
        }
    }

    public void scheduleEntityTask(org.bukkit.entity.Entity entity, Runnable task) {
        if (isFolia()) {
            entity.getScheduler().run(this, scheduledTask -> task.run(), null);
        } else {
            getServer().getScheduler().runTask(this, task);
        }
    }

    public void scheduleLocationTask(org.bukkit.Location location, Runnable task) {
        if (isFolia()) {
            getServer().getRegionScheduler().run(this, location, scheduledTask -> task.run());
        } else {
            getServer().getScheduler().runTask(this, task);
        }
    }
}