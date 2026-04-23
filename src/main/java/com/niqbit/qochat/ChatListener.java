package com.niqbit.qochat;

import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.serializer.plain.PlainTextComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.logging.Logger;

public class ChatListener implements Listener {

    private final JoinLeaveListener joinLeaveListener;
    private final Logger logger;

    public ChatListener(JavaPlugin plugin, JoinLeaveListener joinLeaveListener) {
        this.joinLeaveListener = joinLeaveListener;
        this.logger = plugin.getLogger();
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncChatEvent event) {
        event.setCancelled(true);

        Player player = event.getPlayer();
        JoinLeaveListener.RoleInfo role = joinLeaveListener.getRoleInfo(player);

        String messageText = PlainTextComponentSerializer.plainText().serialize(event.message());

        // {prefix} | {user} > {message}
        Component formatted = Component.text(role.prefix, role.color)
                .append(Component.text(" | ", NamedTextColor.GRAY))
                .append(Component.text(player.getName(), NamedTextColor.WHITE))
                .append(Component.text(" > ", NamedTextColor.GRAY))
                .append(Component.text(messageText, NamedTextColor.WHITE));

        player.getServer().broadcast(formatted);
    }
}