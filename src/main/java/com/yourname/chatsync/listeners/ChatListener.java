package com.yourname.chatsync.listeners;

import com.yourname.chatsync.ChatSync;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class ChatListener implements Listener {
    
    private final ChatSync plugin;
    
    public ChatListener(ChatSync plugin) {
        this.plugin = plugin;
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        String message = event.getMessage();
        
        FileConfiguration config = plugin.getConfig();
        
        // Проверяем игнорирование команд
        if (config.getBoolean("sync.ignore-commands", true) && message.startsWith("/")) {
            return;
        }
        
        // Форматируем сообщение для Telegram
        String format = plugin.getConfigManager().getMessage("formats.mc-to-tg");
        String formattedMessage = format.replace("<player>", player.getName())
                                       .replace("<message>", message);
        
        // Отправляем в Telegram
        plugin.getTelegramManager().sendToTelegram(formattedMessage);
    }
}
