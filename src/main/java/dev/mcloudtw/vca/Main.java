package dev.mcloudtw.vca;

import com.ghostchu.quickshop.QuickShop;
import io.papermc.paper.event.player.AbstractChatEvent;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitTask;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.Timer;
import java.util.TimerTask;

public final class Main extends JavaPlugin {
    public static String AUTH;
    public TimerTask refreshLLMCounterTask = new TimerTask() {
        @Override
        public void run() {
            MessageClassify.LLM_CLASSIFY_COUNT_PER_MINUTES = 0;
        }
    };
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
    public static boolean apiReset() {
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
                Bukkit.getLogger().warning("API Reset failed");
                Bukkit.getLogger().warning("Authorization incorrect or API server error");
                return false;
            }
        }
        catch (Exception e) {
            Bukkit.getLogger().warning("API Reset failed");
            e.printStackTrace();
            return false;
        }
        Bukkit.getLogger().info("API Reset success");
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

        (new Timer()).scheduleAtFixedRate(refreshLLMCounterTask, 0, 60000);

        getServer().getPluginManager().registerEvents(new Events(), this);
    }

    @Override
    public void onDisable() {
        refreshLLMCounterTask.cancel();
        // Plugin shutdown logic
    }
}
