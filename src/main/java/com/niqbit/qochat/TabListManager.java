package com.niqbit.qochat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.TextColor;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.cacheddata.CachedMetaData;
import net.luckperms.api.model.user.User;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class TabListManager implements Listener {

    private final LuckPerms luckPerms;
    private final Logger logger;

    public TabListManager(JavaPlugin plugin) {
        this.logger = plugin.getLogger();
        this.luckPerms = LuckPermsProvider.get();
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Player player = event.getPlayer();
        RoleInfo role = getRoleInfo(player);

        Component name = Component.text(" " + role.prefix, role.color)
                         .append(Component.text(" | ", NamedTextColor.GRAY))
                         .append(Component.text(player.getName(), NamedTextColor.WHITE));

        player.playerListName(name);

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
                TextColor color = NamedTextColor.YELLOW;
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
