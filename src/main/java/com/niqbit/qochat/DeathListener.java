package com.niqbit.qochat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class DeathListener implements Listener {

    private final Logger logger;
    private final LuckPerms luckPerms;

    public DeathListener(JavaPlugin plugin) {
        this.logger = plugin.getLogger();
        this.luckPerms = LuckPermsProvider.get();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onPlayerDeath(PlayerDeathEvent event) {
        Player player = event.getEntity();

        String msgs = event.getDeathMessage();
        Component msg = Component.text(msgs.substring(msgs.indexOf(" ")), NamedTextColor.GRAY);

        event.deathMessage(null);

        RoleInfo role = getRoleInfo(player);

        Component message = Component.text("[", NamedTextColor.GRAY)
                                .append(Component.text("\uD83D\uDC80", NamedTextColor.RED))
                                .append(Component.text("] ", NamedTextColor.GRAY))
                                .append(Component.text(role.prefix, role.color))
                                .append(Component.text(" | ", NamedTextColor.GRAY))
                                .append(Component.text(player.getName(), NamedTextColor.WHITE))
                                .append(msg);

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
