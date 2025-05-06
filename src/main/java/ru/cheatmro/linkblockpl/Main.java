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

import java.awt.Color;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Main extends JavaPlugin implements Listener {

    private final Pattern urlPattern = Pattern.compile("(https?://\\S+)");
    private final Pattern gradientPattern = Pattern.compile("<gradient:([#a-fA-F0-9:,]+)>(.*?)</gradient>");

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
                if (!player.hasPermission("mylink.reload")) {
                    player.sendMessage(ChatColor.RED + "У вас нет прав для выполнения этой команды.");
                    return true;
                }
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
                Matcher gradientMatcher = gradientPattern.matcher(line);
                if (gradientMatcher.find()) {
                    String[] colors = gradientMatcher.group(1).split(",");
                    String text = gradientMatcher.group(2);
                    TextComponent gradientComponent = applyGradient(text, colors);
                    fullMessage.addExtra(gradientComponent);
                    fullMessage.addExtra(new TextComponent("\n"));
                    continue;
                }

                String coloredLine = ChatColor.translateAlternateColorCodes('&', line);
                Matcher matcher = urlPattern.matcher(coloredLine);
                int lastEnd = 0;

                // Добавление всех ссылок в строке
                while (matcher.find()) {
                    String before = coloredLine.substring(lastEnd, matcher.start());
                    String url = matcher.group();

                    // Добавляем текст до ссылки
                    if (!before.isEmpty()) {
                        fullMessage.addExtra(new TextComponent(before));
                    }

                    // Добавляем саму ссылку
                    TextComponent urlComp = new TextComponent(url);
                    urlComp.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, url));
                    fullMessage.addExtra(urlComp);

                    lastEnd = matcher.end();
                }

                // Если после последней ссылки что-то осталось, добавляем это
                if (lastEnd < coloredLine.length()) {
                    fullMessage.addExtra(new TextComponent(coloredLine.substring(lastEnd)));
                }

                fullMessage.addExtra(new TextComponent("\n"));
            }

            player.spigot().sendMessage(fullMessage);
            return true;
        }

        return false;
    }

    private TextComponent applyGradient(String text, String[] hexColors) {
        List<Color> colors = new ArrayList<>();
        for (String hex : hexColors) {
            colors.add(Color.decode(hex));
        }

        TextComponent component = new TextComponent();
        int length = text.length();

        for (int i = 0; i < length; i++) {
            float ratio = (float) i / Math.max(1, length - 1);
            Color blended = blendColors(colors, ratio);
            String hex = String.format("#%02x%02x%02x", blended.getRed(), blended.getGreen(), blended.getBlue());

            TextComponent charComp = new TextComponent(String.valueOf(text.charAt(i)));
            charComp.setColor(net.md_5.bungee.api.ChatColor.of(hex));
            component.addExtra(charComp);
        }

        return component;
    }

    private Color blendColors(List<Color> colors, float ratio) {
        if (colors.size() == 1) return colors.get(0);

        float scaled = ratio * (colors.size() - 1);
        int index = (int) Math.floor(scaled);
        float subRatio = scaled - index;

        if (index >= colors.size() - 1) return colors.get(colors.size() - 1);

        Color c1 = colors.get(index);
        Color c2 = colors.get(index + 1);

        int red = (int) (c1.getRed() + (c2.getRed() - c1.getRed()) * subRatio);
        int green = (int) (c1.getGreen() + (c2.getGreen() - c1.getGreen()) * subRatio);
        int blue = (int) (c1.getBlue() + (c2.getBlue() - c1.getBlue()) * subRatio);

        return new Color(red, green, blue);
    }
}
