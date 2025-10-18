package com.yourname.chatsync.managers;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class ConfigManager {
    
    private final JavaPlugin plugin;
    private FileConfiguration messagesConfig;
    private File messagesFile;
    private Map<String, String> messages;
    
    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.messages = new HashMap<>();
    }
    
    public void loadMessages() {
        // Создаем файлы конфигурации если их нет
        plugin.saveDefaultConfig();
        saveDefaultMessages();
        
        // Загружаем messages.yml
        messagesFile = new File(plugin.getDataFolder(), "messages.yml");
        messagesConfig = YamlConfiguration.loadConfiguration(messagesFile);
        
        // Загружаем все сообщения в память
        loadAllMessages();
        
        plugin.getLogger().info("Сообщения успешно загружены!");
    }
    
    private void saveDefaultMessages() {
        plugin.saveResource("messages.yml", false);
    }
    
    private void loadAllMessages() {
        messages.clear();
        
        // Загружаем общие сообщения
        addMessage("no-permission");
        addMessage("usage");
        addMessage("unknown-command");
        
        // Загружаем сообщения команды /telegram
        addMessage("telegram.usage");
        addMessage("telegram.reload.success");
        addMessage("telegram.reload.error");
        addMessage("telegram.status.connected");
        addMessage("telegram.status.disconnected");
        
        // Загружаем сообщения бота
        addMessage("bot.server-started");
        addMessage("bot.server-stopped");
        addMessage("bot.online-players");
        addMessage("bot.player-list");
        addMessage("bot.no-players");
        addMessage("bot.unknown-command");
        addMessage("bot.help");
        
        // Загружаем форматы сообщений
        addMessage("formats.mc-to-tg");
        addMessage("formats.tg-to-mc");
        
        // Загружаем сообщения плагина
        addMessage("plugin.enabled");
        addMessage("plugin.disabled");
        addMessage("plugin.connected");
        addMessage("plugin.connection-failed");
        addMessage("plugin.config-reloaded");
    }
    
    private void addMessage(String path) {
        String message = messagesConfig.getString(path);
        if (message == null) {
            plugin.getLogger().warning("Сообщение не найдено: " + path);
            messages.put(path, "&cСообщение не найдено: " + path);
        } else {
            messages.put(path, ChatColor.translateAlternateColorCodes('&', message));
        }
    }
    
    public String getMessage(String path, Map<String, String> placeholders) {
        String message = messages.getOrDefault(path, path);
        
        if (placeholders != null) {
            for (Map.Entry<String, String> entry : placeholders.entrySet()) {
                message = message.replace("{" + entry.getKey() + "}", entry.getValue());
            }
        }
        
        return message;
    }
    
    public String getMessage(String path, String placeholder, String value) {
        Map<String, String> placeholders = new HashMap<>();
        placeholders.put(placeholder, value);
        return getMessage(path, placeholders);
    }
    
    public String getMessage(String path) {
        return messages.getOrDefault(path, path);
    }
    
    public void reloadConfigs() {
        plugin.reloadConfig();
        loadMessages();
    }
}
