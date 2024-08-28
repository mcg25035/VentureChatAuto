package dev.mcloudtw.vca;

import com.ghostchu.quickshop.QuickShop;
import io.papermc.paper.event.player.AbstractChatEvent;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic
        getServer().getPluginManager().registerEvents(new Events(), this);

    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
