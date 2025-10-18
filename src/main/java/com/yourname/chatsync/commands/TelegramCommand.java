package com.yourname.chatsync.commands;

import com.yourname.chatsync.ChatSync;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

import java.util.HashMap;
import java.util.Map;

public class TelegramCommand implements CommandExecutor {
    
    private final ChatSync plugin;
    
    public TelegramCommand(ChatSync plugin) {
        this.plugin = plugin;
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!sender.hasPermission("chatsync.admin")) {
            sender.sendMessage(plugin.getConfigManager().getMessage("no-permission"));
            return true;
        }
        
        if (args.length == 0) {
            sender.sendMessage(plugin.getConfigManager().getMessage("telegram.usage"));
            return true;
        }
        
        switch (args[0].toLowerCase()) {
            case "reload":
                plugin.getConfigManager().reloadConfigs();
                plugin.getTelegramManager().disconnect();
                if (plugin.getTelegramManager().connect()) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("telegram.reload.success"));
                } else {
                    sender.sendMessage(plugin.getConfigManager().getMessage("telegram.reload.error"));
                }
                break;
                
            case "status":
                boolean connected = plugin.getTelegramManager().isConnected();
                if (connected) {
                    sender.sendMessage(plugin.getConfigManager().getMessage("telegram.status.connected"));
                } else {
                    sender.sendMessage(plugin.getConfigManager().getMessage("telegram.status.disconnected"));
                }
                break;
                
            default:
                Map<String, String> placeholders = new HashMap<>();
                placeholders.put("usage", "/telegram <reload|status>");
                sender.sendMessage(plugin.getConfigManager().getMessage("usage", placeholders));
        }
        
        return true;
    }
}
