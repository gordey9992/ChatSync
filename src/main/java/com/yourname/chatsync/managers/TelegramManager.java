package com.yourname.chatsync.managers;

import com.yourname.chatsync.ChatSync;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.HashMap;
import java.util.Map;

public class TelegramManager {
    
    private final ChatSync plugin;
    private final String botToken;
    private final String chatId;
    private boolean connected;
private int lastUpdateId = 0;

public void checkForNewMessages(int lastReceivedUpdateId) {
    if (!connected) return;
    
    try {
        String response = sendGetRequest("getUpdates?offset=" + (lastReceivedUpdateId + 1) + "&timeout=10");
        
        if (response.contains("\"ok\":true")) {
            parseUpdates(response);
        }
    } catch (Exception e) {
        plugin.getLogger().warning("Ошибка при проверке сообщений: " + e.getMessage());
    }
}

private void parseUpdates(String response) {
    try {
        // Простой парсинг JSON (для простоты)
        // В реальном плагине лучше использовать библиотеку JSON
        
        String[] updates = response.split("\\{\"update_id\"");
        
        for (int i = 1; i < updates.length; i++) {
            String update = "{\"update_id\"" + updates[i];
            
            // Извлекаем update_id
            int updateId = extractUpdateId(update);
            if (updateId > lastUpdateId) {
                lastUpdateId = updateId;
            }
            
            // Извлекаем сообщение
            if (update.contains("\"text\"")) {
                String text = extractText(update);
                String user = extractUsername(update);
                
                if (text != null && user != null && !text.trim().isEmpty()) {
                    // Проверяем, не от нашего ли бота сообщение
                    if (!user.toLowerCase().contains("bot")) {
                        boolean isQuestion = text.contains("?") || 
                                           text.toLowerCase().startsWith("как") ||
                                           text.toLowerCase().startsWith("где") ||
                                           text.toLowerCase().contains("вопрос");
                        
                        sendToMinecraft(user, text, isQuestion);
                    }
                }
            }
        }
    } catch (Exception e) {
        plugin.getLogger().warning("Ошибка парсинга updates: " + e.getMessage());
    }
}

private int extractUpdateId(String update) {
    try {
        String[] parts = update.split("\"update_id\":");
        if (parts.length > 1) {
            String idStr = parts[1].split(",")[0].trim();
            return Integer.parseInt(idStr);
        }
    } catch (Exception e) {
        // Игнорируем ошибки парсинга
    }
    return lastUpdateId;
}

private String extractText(String update) {
    try {
        if (update.contains("\"text\":")) {
            String[] parts = update.split("\"text\":\"");
            if (parts.length > 1) {
                String text = parts[1].split("\"")[0];
                return text.replace("\\\"", "\"");
            }
        }
    } catch (Exception e) {
        // Игнорируем ошибки парсинга
    }
    return null;
}

private String extractUsername(String update) {
    try {
        if (update.contains("\"username\":")) {
            String[] parts = update.split("\"username\":\"");
            if (parts.length > 1) {
                return parts[1].split("\"")[0];
            }
        }
        
        // Если username нет, пробуем из first_name
        if (update.contains("\"first_name\":")) {
            String[] parts = update.split("\"first_name\":\"");
            if (parts.length > 1) {
                return parts[1].split("\"")[0];
            }
        }
    } catch (Exception e) {
        // Игнорируем ошибки парсинга
    }
    return "Неизвестный";
}

// Обновите метод sendToMinecraft:
public void sendToMinecraft(String user, String message, boolean isQuestion) {
    // Игнорируем команды бота и служебные сообщения
    if (message.startsWith("/") || user.toLowerCase().contains("bot")) {
        return;
    }
    
    // Отправка обычного сообщения в Minecraft
    String format;
    if (isQuestion) {
        format = plugin.getConfigManager().getMessage("formats.tg-to-mc-question");
    } else {
        format = plugin.getConfigManager().getMessage("formats.tg-to-mc");
    }
    
    String formattedMessage = format.replace("<user>", user).replace("<message>", message);
    final String finalMessage = formattedMessage;
    
    Bukkit.getScheduler().runTask(plugin, () -> {
        Bukkit.broadcastMessage(finalMessage);
    });
    
    plugin.getLogger().info("Сообщение из Telegram: " + user + ": " + message);
}
    
    public TelegramManager(ChatSync plugin) {
        this.plugin = plugin;
        FileConfiguration config = plugin.getConfig();
        this.botToken = config.getString("telegram.bot-token");
        this.chatId = config.getString("telegram.chat-id");
        this.connected = false;
    }
    
    public boolean connect() {
        if (botToken == null || botToken.isEmpty() || chatId == null || chatId.isEmpty()) {
            plugin.getLogger().warning("Токен бота или ID чата не настроены в config.yml");
            return false;
        }
        
        // Проверяем соединение с Telegram API
        try {
            String response = sendGetRequest("getMe");
            if (response.contains("\"ok\":true")) {
                connected = true;
                
                // Отправляем сообщение о запуске
                sendToTelegram(plugin.getConfigManager().getMessage("bot.server-started"));
                return true;
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка подключения к Telegram: " + e.getMessage());
        }
        
        connected = false;
        return false;
    }
    
    public void disconnect() {
        if (connected) {
            sendToTelegram(plugin.getConfigManager().getMessage("bot.server-stopped"));
            connected = false;
        }
    }
    
// В класс TelegramManager добавьте:

public void sendToTelegram(String message, boolean isQuestion) {
    if (!connected) return;
    
    Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
        try {
            String chatId = isQuestion ? 
                plugin.getConfig().getString("telegram.question-chat-id", this.chatId) : 
                this.chatId;
                
            String url = "https://api.telegram.org/bot" + botToken + "/sendMessage";
            String postData = "chat_id=" + chatId + 
                             "&text=" + URLEncoder.encode(message, "UTF-8") + 
                             "&parse_mode=HTML";
            
            sendPostRequest(url, postData);
        } catch (Exception e) {
            plugin.getLogger().warning("Ошибка отправки сообщения в Telegram: " + e.getMessage());
        }
    });
}

public void sendToMinecraft(String user, String message, boolean isQuestion) {
    if (message.startsWith("!")) {
        // Обработка команд из Telegram
        handleTelegramCommand(user, message);
        return;
    }
    
    // Отправка обычного сообщения в Minecraft
    String format;
    if (isQuestion) {
        format = plugin.getConfigManager().getMessage("formats.tg-to-mc-question");
    } else {
        format = plugin.getConfigManager().getMessage("formats.tg-to-mc");
    }
    
    String formattedMessage = format.replace("<user>", user).replace("<message>", message);
    
    Bukkit.getScheduler().runTask(plugin, () -> {
        Bukkit.broadcastMessage(formattedMessage);
    });
}
    
    private void handleTelegramCommand(String user, String message) {
        String command = message.substring(1).toLowerCase();
        
        switch (command) {
            case "online":
                int online = Bukkit.getOnlinePlayers().size();
                int max = Bukkit.getMaxPlayers();
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("online", String.valueOf(online));
                placeholders.put("max", String.valueOf(max));
                String onlineMsg = plugin.getConfigManager().getMessage("bot.online-players", placeholders);
                sendToTelegram(onlineMsg);
                break;
                
            case "list":
                StringBuilder players = new StringBuilder();
                if (Bukkit.getOnlinePlayers().isEmpty()) {
                    players.append(plugin.getConfigManager().getMessage("bot.no-players"));
                } else {
                    players.append(plugin.getConfigManager().getMessage("bot.player-list")
                            .replace("{players}", getOnlinePlayersList()));
                }
                sendToTelegram(players.toString());
                break;
                
            case "help":
                sendToTelegram(plugin.getConfigManager().getMessage("bot.help"));
                break;
                
            default:
                sendToTelegram(plugin.getConfigManager().getMessage("bot.unknown-command"));
        }
    }
    
    private String getOnlinePlayersList() {
        StringBuilder players = new StringBuilder();
        Bukkit.getOnlinePlayers().forEach(player -> 
            players.append("• ").append(player.getName()).append("\n"));
        return players.toString();
    }
    
    private String sendGetRequest(String method) throws IOException {
        URL url = new URL("https://api.telegram.org/bot" + botToken + "/" + method);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        
        return response.toString();
    }
    
    private String sendPostRequest(String urlString, String postData) throws IOException {
        URL url = new URL(urlString);
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setDoOutput(true);
        
        OutputStream os = conn.getOutputStream();
        os.write(postData.getBytes());
        os.flush();
        os.close();
        
        BufferedReader in = new BufferedReader(new InputStreamReader(conn.getInputStream()));
        String inputLine;
        StringBuilder response = new StringBuilder();
        
        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();
        
        return response.toString();
    }
    
    public boolean isConnected() {
        return connected;
    }
}
