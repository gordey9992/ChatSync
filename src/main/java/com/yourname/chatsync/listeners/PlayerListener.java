package com.yourname.chatsync.listeners;

import com.yourname.chatsync.ChatSync;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;

public class PlayerListener implements Listener {
    
    private final ChatSync plugin;
    
    public PlayerListener(ChatSync plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        if (!plugin.getConfig().getBoolean("notifications.player-join-quit", true)) {
            return;
        }
        
        Player player = event.getPlayer();
        
        // Форматируем сообщение для Telegram
        String format;
        if (plugin.getConfig().getBoolean("notifications.show-online-count", true)) {
            format = plugin.getConfigManager().getMessage("player.join-with-count");
        } else {
            format = plugin.getConfigManager().getMessage("player.join");
        }
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getName());
        placeholders.put("online", String.valueOf(plugin.getServer().getOnlinePlayers().size()));
        placeholders.put("max", String.valueOf(plugin.getServer().getMaxPlayers()));
        
        String formattedMessage = format;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            formattedMessage = formattedMessage.replace("<" + entry.getKey() + ">", entry.getValue());
        }
        
        // Отправляем в Telegram
        plugin.getTelegramManager().sendToTelegram(formattedMessage, false);
    }
    
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        if (!plugin.getConfig().getBoolean("notifications.player-join-quit", true)) {
            return;
        }
        
        Player player = event.getPlayer();
        
        // Форматируем сообщение для Telegram
        String format;
        if (plugin.getConfig().getBoolean("notifications.show-online-count", true)) {
            format = plugin.getConfigManager().getMessage("player.quit-with-count");
        } else {
            format = plugin.getConfigManager().getMessage("player.quit");
        }
        
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put("player", player.getName());
        placeholders.put("online", String.valueOf(plugin.getServer().getOnlinePlayers().size() - 1)); // -1 потому что игрок еще не вышел
        placeholders.put("max", String.valueOf(plugin.getServer().getMaxPlayers()));
        
        String formattedMessage = format;
        for (Map.Entry<String, String> entry : placeholders.entrySet()) {
            formattedMessage = formattedMessage.replace("<" + entry.getKey() + ">", entry.getValue());
        }
        
        // Отправляем в Telegram
        plugin.getTelegramManager().sendToTelegram(formattedMessage, false);
    }
}
