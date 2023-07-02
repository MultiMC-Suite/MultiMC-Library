package fr.xen0xys.multimc.papermc.listeners;

import fr.xen0xys.multimc.papermc.events.BungeeMessagingReadyEvent;
import org.bukkit.Bukkit;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;
import org.jetbrains.annotations.NotNull;

public class OnPlayerLogin implements Listener {

    private final JavaPlugin instance;

    public OnPlayerLogin(@NotNull final JavaPlugin instance) {
        this.instance = instance;
    }

    @EventHandler
    public void onPlayerLogin(@NotNull final PlayerLoginEvent e) {
        new BukkitRunnable(){
            @Override
            public void run() {
                Bukkit.getPluginManager().callEvent(new BungeeMessagingReadyEvent(e.getPlayer()));
            }
        }.runTaskLaterAsynchronously(this.instance, 10);
    }
}
