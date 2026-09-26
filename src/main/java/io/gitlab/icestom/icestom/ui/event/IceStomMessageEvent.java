package io.gitlab.icestom.icestom.ui.event;

import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Player;
import net.minestom.server.event.trait.CancellableEvent;
import net.minestom.server.event.trait.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Fired before IceStom sends a player-facing chat message, giving listeners a chance to suppress or
 * replace it. This is the interception seam for built-in messages: a server or plugin can cancel a
 * message it wants to hide, or rewrite it via {@link #setComponent(Component)}, without editing
 * IceStom source.
 *
 * <p>Each message carries a stable {@link #getKey() key} (see {@code IceStomMessages}) so listeners
 * can target a specific message without matching on text. If the event is cancelled the message is
 * not sent; otherwise the (possibly replaced) component is sent to the player.</p>
 */
public class IceStomMessageEvent implements PlayerEvent, MessageEvent, CancellableEvent {

    private final @NotNull Player player;
    private final @NotNull Key key;
    private @NotNull Component component;
    private boolean cancelled = false;

    public IceStomMessageEvent(@NotNull Player player, @NotNull Key key, @NotNull Component component) {
        this.player = player;
        this.key = key;
        this.component = component;
    }

    @Override
    public @NotNull Player getPlayer() {
        return player;
    }

    /** The stable identity of this message (e.g. {@code icestom:timetrial.get_record}). */
    public @NotNull Key getKey() {
        return key;
    }

    @Override
    public @NotNull Component getComponent() {
        return component;
    }

    /** Replaces the message that will be sent. Ignored if the event is cancelled. */
    public void setComponent(@NotNull Component component) {
        this.component = component;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancelled) {
        this.cancelled = cancelled;
    }
}
