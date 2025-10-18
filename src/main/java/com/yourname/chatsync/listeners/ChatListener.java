package com.yourname.chatsync.listeners;

import com.yourname.chatsync.ChatSync;
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
        String message = event.getMessage().trim();
        
        // Проверяем игнорирование команд
        if (plugin.getConfig().getBoolean("sync.filter.ignore-commands", true) && message.startsWith("/")) {
            return;
        }
        
        // Проверяем минимальную длину
        int minLength = plugin.getConfig().getInt("sync.filter.min-length", 3);
        if (message.length() < minLength) {
            player.sendMessage(plugin.getConfigManager().getMessage("filter.message-too-short"));
            return;
        }
        
        // Проверяем знаки препинания
        boolean ignoreNoPunctuation = plugin.getConfig().getBoolean("sync.filter.ignore-no-punctuation", true);
        if (ignoreNoPunctuation && !hasPunctuation(message)) {
            player.sendMessage(plugin.getConfigManager().getMessage("filter.no-punctuation"));
            return;
        }
        
        // Определяем тип сообщения (вопрос или обычное)
        boolean isQuestion = isQuestion(message);
        
        // Форматируем сообщение для Telegram
        String format;
        if (isQuestion) {
            format = plugin.getConfigManager().getMessage("formats.mc-to-tg-question");
        } else {
            format = plugin.getConfigManager().getMessage("formats.mc-to-tg");
        }
        
        String formattedMessage = format.replace("<player>", player.getName())
                                       .replace("<message>", message);
        
        // Отправляем в Telegram
        plugin.getTelegramManager().sendToTelegram(formattedMessage, isQuestion);
    }
    
    private boolean hasPunctuation(String message) {
        return message.contains("?") || message.contains("!") || message.contains(".") || 
               message.contains(")") || message.contains("(") || message.contains(",") ||
               message.contains(":") || message.contains(";") || message.contains("-");
    }
    
    private boolean isQuestion(String message) {
        return message.contains("?") || 
               message.toLowerCase().startsWith("как") ||
               message.toLowerCase().startsWith("где") ||
               message.toLowerCase().startsWith("кто") ||
               message.toLowerCase().startsWith("что") ||
               message.toLowerCase().startsWith("почему") ||
               message.toLowerCase().startsWith("зачем") ||
               message.toLowerCase().startsWith("сколько") ||
               message.toLowerCase().contains("помогите") ||
               message.toLowerCase().contains("помощь") ||
               message.toLowerCase().contains("вопрос");
    }
}
