package fr.xen0xys.multimc.papermc.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

public class BungeeMessagingReadyEvent extends PlayerEvent {

    private static final HandlerList handlers = new HandlerList();

    public BungeeMessagingReadyEvent(@NotNull final Player player) {
        super(player, true);
    }

    @Override
    public @NotNull HandlerList getHandlers() {
        return handlers;
    }
}
