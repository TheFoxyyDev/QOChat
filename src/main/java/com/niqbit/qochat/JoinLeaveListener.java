package com.niqbit.qochat;

import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class JoinLeaveListener implements Listener {

    private final LuckPerms luckPerms;
    private final Logger logger;

    public JoinLeaveListener(JavaPlugin plugin) {
        this.logger = plugin.getLogger();
        this.luckPerms = LuckPermsProvider.get();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onJoin(PlayerJoinEvent event) {
        event.joinMessage(null);

        Player player = event.getPlayer();
        RoleInfo role = getRoleInfo(player);

        Component message = Component.text("[", NamedTextColor.GRAY)
                .append(Component.text("+", TextColor.color(0x55FF55)))
                .append(Component.text("] ", NamedTextColor.GRAY))
                .append(Component.text(role.prefix, role.color))
                .append(Component.text(" | ", NamedTextColor.GRAY))
                .append(Component.text(player.getName(), NamedTextColor.WHITE))
                .append(Component.text(" joined the game", NamedTextColor.GRAY));

        Bukkit.broadcast(message);
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onQuit(PlayerQuitEvent event) {
        event.quitMessage(null);

        Player player = event.getPlayer();
        RoleInfo role = getRoleInfo(player);

        Component message = Component.text("[", NamedTextColor.GRAY)
                .append(Component.text("-", TextColor.color(0xFF5555)))
                .append(Component.text("] ", NamedTextColor.GRAY))
                .append(Component.text(role.prefix, role.color))
                .append(Component.text(" | ", NamedTextColor.GRAY))
                .append(Component.text(player.getName(), NamedTextColor.WHITE))
                .append(Component.text(" left the game", NamedTextColor.GRAY));

        Bukkit.broadcast(message);
    }

    RoleInfo getRoleInfo(Player player) {
        try {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user != null) {
                CachedMetaData meta = user.getCachedData().getMetaData();

                String prefix = meta.getPrefix();
                if (prefix == null || prefix.isBlank()) {
                    prefix = user.getPrimaryGroup();
                }

                String colorHex = meta.getMetaValue("color");
                TextColor color = NamedTextColor.YELLOW; // default
                if (colorHex != null && !colorHex.isBlank()) {
                    try {
                        if (!colorHex.startsWith("#")) {
                            colorHex = "#" + colorHex;
                        }
                        color = TextColor.fromHexString(colorHex);
                        if (color == null) color = NamedTextColor.YELLOW;
                    } catch (Exception e) {
                        logger.warning("Invalid color meta value for " + player.getName() + ": " + colorHex);
                    }
                }

                return new RoleInfo(prefix, color);
            }
        } catch (Exception e) {
            logger.warning("Error fetching LuckPerms data for " + player.getName() + ": " + e.getMessage());
        }
        return new RoleInfo("default", NamedTextColor.YELLOW);
    }

    static class RoleInfo {
        final String prefix;
        final TextColor color;

        RoleInfo(String prefix, TextColor color) {
            this.prefix = prefix;
            this.color = color;
        }
    }
}