package com.yourname.chatsync;

import com.yourname.chatsync.managers.ConfigManager;
import com.yourname.chatsync.managers.TelegramManager;
import com.yourname.chatsync.listeners.ChatListener;
import com.yourname.chatsync.commands.TelegramCommand;
import org.bukkit.plugin.java.JavaPlugin;

public class ChatSync extends JavaPlugin {
    
    private static ChatSync instance;
    private ConfigManager configManager;
    private TelegramManager telegramManager;
    
    @Override
    public void onEnable() {
        instance = this;
        
        // Инициализация менеджеров
        this.configManager = new ConfigManager(this);
        this.telegramManager = new TelegramManager(this);
        
        // Загрузка конфигураций
        configManager.loadMessages();
        
        // Регистрация ивентов
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        
        // Регистрация команд
        getCommand("telegram").setExecutor(new TelegramCommand(this));
        
        // Подключение к Telegram
        if (telegramManager.connect()) {
            getLogger().info(configManager.getMessage("plugin.connected"));
        } else {
            getLogger().warning(configManager.getMessage("plugin.connection-failed"));
        }
        
        // Красивое сообщение при запуске
        sendWelcomeMessage();
    }
    
    @Override
    public void onDisable() {
        if (telegramManager != null) {
            telegramManager.disconnect();
        }
        getLogger().info(configManager.getMessage("plugin.disabled"));
    }
    
    private void sendWelcomeMessage() {
        getServer().getConsoleSender().sendMessage("§6╔══════════════════════════════════╗");
        getServer().getConsoleSender().sendMessage("§6║          §e§lChatSync §6v1.0.0         ║");
        getServer().getConsoleSender().sendMessage("§6║    §aПлагин успешно загружен!     ║");
        getServer().getConsoleSender().sendMessage("§6║ §7Синхронизация Minecraft ↔ Telegram ║");
        getServer().getConsoleSender().sendMessage("§6╚══════════════════════════════════╝");
    }
    
    public static ChatSync getInstance() {
        return instance;
    }
    
    public ConfigManager getConfigManager() {
        return configManager;
    }
    
    public TelegramManager getTelegramManager() {
        return telegramManager;
    }
}
