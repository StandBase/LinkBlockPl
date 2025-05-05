package com.example.linkblockpl;

import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.TextComponent;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main extends JavaPlugin implements Listener {

    private final Pattern urlPattern = Pattern.compile("(https?://\\S+)");

    @Override
    public void onEnable() {
        saveDefaultConfig();
        getServer().getPluginManager().registerEvents(this, this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (cmd.getName().equalsIgnoreCase("mylink")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Эту команду может использовать только игрок.");
                return true;
            }

            Player player = (Player) sender;

            if (args.length == 1 && args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                player.sendMessage(ChatColor.GREEN + "Конфигурация перезагружена.");
                return true;
            }

            player.sendMessage("Используй /link для просмотра ссылок.");
            return true;
        }

        if (cmd.getName().equalsIgnoreCase("link")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("Эту команду может использовать только игрок.");
                return true;
            }

            Player player = (Player) sender;
            FileConfiguration config = getConfig();
            List<String> messages = config.getStringList("link-message");

            TextComponent fullMessage = new TextComponent();

            for (String line : messages) {
                String coloredLine = ChatColor.translateAlternateColorCodes('&', line);
                Matcher matcher = urlPattern.matcher(coloredLine);
                int lastEnd = 0;

                while (matcher.find()) {
                    String before = coloredLine.substring(lastEnd, matcher.start());
                    String url = matcher.group();

                    TextComponent beforeComp = new TextComponent(before);
                    TextComponent urlComp = new TextComponent(url);
                    urlComp.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));

                    fullMessage.addExtra(beforeComp);
                    fullMessage.addExtra(urlComp);

                    lastEnd = matcher.end();
                }

                if (lastEnd < coloredLine.length()) {
                    TextComponent after = new TextComponent(coloredLine.substring(lastEnd));
                    fullMessage.addExtra(after);
                }

                fullMessage.addExtra(new TextComponent("\n"));
            }

            player.spigot().sendMessage(fullMessage);
            return true;
        }

        return false;
    }
}
