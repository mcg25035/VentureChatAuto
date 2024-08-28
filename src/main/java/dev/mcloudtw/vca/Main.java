package dev.mcloudtw.vca;

import com.ghostchu.quickshop.QuickShop;
import io.papermc.paper.event.player.AbstractChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

public final class Main extends JavaPlugin {
    public static String AUTH;
    public boolean loadConfig() {
        AUTH = getConfig().getString("api-auth");
        if (AUTH == null) {
            getConfig().set("api-auth", "your_auth_here");
            saveConfig();
            getLogger().warning("API Auth not found in config.yml");
            return false;
        }
        return true;
    }

    public boolean apiReset() {
        URI uri = URI.create("https://mc.llm.codingbear.mcloudtw.com/refresh");
        try{
            HttpClient client = HttpClient.newBuilder()
                    .sslContext(Utils.getUnsafeSslContext())
                    .build();
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .uri(uri)
                    .header("Content-Type", "application/json")
                    .header("Authorization", AUTH)
                    .GET()
                    .build();
            HttpResponse<String> res = client.send(request, HttpResponse.BodyHandlers.ofString());
            if (res.statusCode() != 200) {
                getLogger().warning("API Reset failed");
                getLogger().warning("Authorization incorrect or API server error");
                return false;
            }
        }
        catch (Exception e) {
            getLogger().warning("API Reset failed");
            e.printStackTrace();
            return false;
        }
        getLogger().info("API Reset success");
        return true;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        if (!loadConfig()) {
            getLogger().warning("Plugin disabled due to missing config");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        if (!apiReset()) {
            getLogger().warning("Plugin disabled due to API reset failure");
            Bukkit.getPluginManager().disablePlugin(this);
            return;
        }
        getServer().getPluginManager().registerEvents(new Events(), this);
    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
