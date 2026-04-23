package com.niqbit.qochat;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.title.Title;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;

import java.time.Duration;

public class WelcomePlugin extends JavaPlugin implements Listener {

    public String getFormattedTime(World world) {
        long ticks = world.getTime();

        long worldTime = (ticks + 6000) % 24000;

        int hours = (int) (worldTime / 1000);
        int minutes = (int) ((worldTime % 1000) * 60 / 1000);

        return String.format("%02d:%02d", hours, minutes);
    }


    @Override
    public void onEnable() {
        try {
            JoinLeaveListener joinLeaveListener = new JoinLeaveListener(this);
            DeathListener deathListener = new DeathListener(this);
            getServer().getPluginManager().registerEvents(joinLeaveListener, this);
            getServer().getPluginManager().registerEvents(new ChatListener(this, joinLeaveListener), this);
            getServer().getPluginManager().registerEvents(deathListener, this);
        } catch (Exception e) {
            getLogger().severe("Failed to initialize listeners: " + e.getClass().getName() + " — " + e.getMessage());
        }

        getServer().getPluginManager().registerEvents(this, this);
        getLogger().info("WelcomePlugin enabled!");
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        boolean isNewPlayer = !player.hasPlayedBefore();

        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (!player.isOnline()) {
                getLogger().info("Player left before text could be displayed");
                return;
            }

            Component title = isNewPlayer
                    ? Component.text("Welcome,")
                    : Component.text("Welcome back,");
            Component subtitle = Component.text(player.getName());

            Title.Times times = Title.Times.times(
                    Duration.ofMillis(500),
                    Duration.ofSeconds(3),
                    Duration.ofMillis(500)
            );
            player.showTitle(Title.title(title, subtitle, times));

            Bukkit.getScheduler().runTaskLater(this, () -> {
                Component actionBar = Component.text("Current Time: ")
                        .append(Component.text(getFormattedTime(player.getWorld())));
                player.sendActionBar(actionBar);
            }, 10L);

        }, 80L);
    }
}