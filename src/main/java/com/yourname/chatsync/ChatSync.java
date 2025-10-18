package com.yourname.chatsync;

import com.yourname.chatsync.managers.ConfigManager;
import com.yourname.chatsync.managers.TelegramManager;
import com.yourname.chatsync.listeners.ChatListener;
import com.yourname.chatsync.commands.TelegramCommand;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

public class ChatSync extends JavaPlugin {
    
    private static ChatSync instance;
    private ConfigManager configManager;
    private TelegramManager telegramManager;
    private int taskId;
    
    @Override
    public void onEnable() {
        getServer().getPluginManager().registerEvents(new ChatListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this); // ← Добавьте эту строку
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
        getServer().getPluginManager().registerEvents(new PlayerListener(this), this); // ← Добавьте эту строку
        
        // Подключение к Telegram
        if (telegramManager.connect()) {
            getLogger().info(configManager.getMessage("plugin.connected"));
            
            // Запускаем получение сообщений из Telegram
            startLongPolling();
        } else {
            getLogger().warning(configManager.getMessage("plugin.connection-failed"));
        }
        
        // Красивое сообщение при запуске
        sendWelcomeMessage();
    }
    
    private void startLongPolling() {
        if (!getConfig().getBoolean("long-polling.enabled", true)) {
            return;
        }
        
        int interval = getConfig().getInt("long-polling.update-interval", 3) * 20;
        
        taskId = new BukkitRunnable() {
            private int lastUpdateId = 0;
            
            @Override
            public void run() {
                telegramManager.checkForNewMessages(lastUpdateId);
                // lastUpdateId будет обновляться внутри checkForNewMessages
            }
        }.runTaskTimerAsynchronously(this, 100L, interval).getTaskId();
        
        getLogger().info("Long Polling запущен с интервалом " + interval/20 + " секунд");
    }
    
    @Override
    public void onDisable() {
        if (telegramManager != null) {
            telegramManager.disconnect();
        }
        
        // Останавливаем Long Polling
        if (taskId != 0) {
            getServer().getScheduler().cancelTask(taskId);
        }
        
        getLogger().info(configManager.getMessage("plugin.disabled"));
    }
    
    // ... остальные методы без изменений
}
