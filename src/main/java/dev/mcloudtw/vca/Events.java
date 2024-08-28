package dev.mcloudtw.vca;

import com.ghostchu.quickshop.QuickShop;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.HashMap;

public class Events implements Listener {
    CommandSender channelAdapter = Bukkit.createCommandSender((ignored)->{});
    HashMap<Player, MessageClassify.MessageCategory> playerLastChannel = new HashMap<>();

    @EventHandler
    public void AsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        if (event.getMessage().startsWith("!")) return;
        if (Bukkit.getPluginManager().getPlugin("QuickShop-Hikari") != null) {
            System.out.println("QuickShop is enabled");
            if (QuickShop.getInstance().getShopManager().getInteractiveManager().containsKey(event.getPlayer().getUniqueId())) {
                System.out.println("QuickShop is enabled and player is in shop mode");
                return;
            }
            System.out.println("QuickShop is enabled and player is not in shop mode");
        }


        Player player = event.getPlayer();
        String message = event.getMessage();
        if (message.startsWith("<vcap>")) {
            event.setMessage(message.replace("<vcap>", ""));
            return;
        }

        event.setCancelled(true);
        MessageClassify messageClassify = new MessageClassify(message);
        messageClassify.messageCategory.thenAcceptAsync(messageCategory -> {
            Bukkit.getScheduler().runTask(Main.getPlugin(Main.class), ()->{
                MessageClassify.MessageCategory lastChannel = playerLastChannel.get(player);
                if (lastChannel != messageCategory) {
                    Bukkit.dispatchCommand(channelAdapter, "setchannel "+player.getName()+" "+messageCategory.name());
                    playerLastChannel.put(player, messageCategory);
                }
                player.chat("<vcap>"+messageClassify.message);
            });
        });
    }
}
