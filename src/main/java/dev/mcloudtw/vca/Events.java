package dev.mcloudtw.vca;

import com.booksaw.betterTeams.Team;
import com.booksaw.betterTeams.TeamPlayer;
import com.ghostchu.quickshop.QuickShop;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitTask;

import java.time.Instant;
import java.util.HashMap;

public class Events implements Listener {
    CommandSender channelAdapter = Bukkit.createCommandSender((ignored)->{});
    HashMap<Player, MessageClassify.MessageCategory> playerLastChannel = new HashMap<>();

    @EventHandler
    public void AsyncPlayerChatEvent(AsyncPlayerChatEvent event) {
        if (event.getMessage().startsWith("!")) return;
        if (Bukkit.getPluginManager().getPlugin("QuickShop-Hikari") != null) {
            if (QuickShop.getInstance().getShopManager().getInteractiveManager().containsKey(event.getPlayer().getUniqueId())) {
                return;
            }
        }

        if (Bukkit.getPluginManager().getPlugin("BetterTeams") != null) {
            Player player = event.getPlayer();
            Team team = Team.getTeam(player);

            if (team != null) {
                TeamPlayer teamPlayer = team.getTeamPlayer(player);
                if (teamPlayer != null) {
                    if (teamPlayer.isInTeamChat() || teamPlayer.isInAllyChat()) {
                        return;
                    }
                }
            }
        }


        Player player = event.getPlayer();
        String message = event.getMessage();
        if (message.startsWith("<vcap>")) {
            event.setMessage(message.replace("<vcap>", ""));
            return;
        }

        event.setCancelled(true);
        MessageClassify messageClassify = new MessageClassify(message, player.getName());
        Instant now = Instant.now();
        BukkitTask notify = Bukkit.getScheduler().runTaskTimer(Main.getPlugin(Main.class), ()->{
            Instant now1 = Instant.now();
            long diff = now1.toEpochMilli() - now.toEpochMilli();
            double duration = diff/1000.0;
            player.sendActionBar(
                    MiniMessage.miniMessage().deserialize(
                            "<gray>正在分類您的訊息..." + duration + "s"
                    )
            );
        }, 1, 1);
        messageClassify.messageCategory.thenAcceptAsync(messageCategory -> {
            Bukkit.getScheduler().runTask(Main.getPlugin(Main.class), ()->{
                player.sendActionBar(
                        MiniMessage.miniMessage().deserialize("")
                );
                notify.cancel();
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
