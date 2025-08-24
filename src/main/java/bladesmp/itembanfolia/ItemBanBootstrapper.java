package bladesmp.itembanfolia;

import io.papermc.paper.plugin.bootstrap.BootstrapContext;
import io.papermc.paper.plugin.bootstrap.PluginBootstrap;
import io.papermc.paper.plugin.bootstrap.PluginProviderContext;
import org.jetbrains.annotations.NotNull;

public class ItemBanBootstrapper implements PluginBootstrap {

    @Override
    public void bootstrap(@NotNull BootstrapContext context) {
        // Bootstrap logic for Folia if needed
        context.getLogger().info("ItemBan Plugin bootstrapping...");

        // Check if we're running on Folia
        try {
            Class.forName("io.papermc.paper.threadedregions.RegionizedServer");
            context.getLogger().info("Detected Folia server - enabling Folia optimizations");
        } catch (ClassNotFoundException e) {
            context.getLogger().info("Detected non-Folia server - using standard optimizations");
        }
    }

    @Override
    public @NotNull ItemBanPlugin createPlugin(@NotNull PluginProviderContext context) {
        return new ItemBanPlugin();
    }
}