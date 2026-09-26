package io.gitlab.icestom.icestom.ui.event;

import io.gitlab.icestom.icestom.IceStom;
import net.kyori.adventure.key.Key;
import net.kyori.adventure.text.Component;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * Stable identities for IceStom's built-in chat messages, plus the dispatch entry point that routes
 * a message through {@link IceStomMessageEvent} before sending.
 *
 * <p>Call sites use {@link #send(Player, Key, Component)} instead of {@code player.sendMessage(...)}
 * so every message can be suppressed or replaced by a listener. The keys here let a listener target
 * one message by identity rather than by matching its text.</p>
 */
public final class IceStomMessages {

    private IceStomMessages() {}

    private static Key key(@NotNull String value) {
        return Key.key(IceStom.NAMESPACE, value);
    }

    // Time trial
    public static final Key TIMETRIAL_CHECKPOINT_PASS = key("timetrial.checkpoint_pass");
    public static final Key TIMETRIAL_START = key("timetrial.start");
    public static final Key TIMETRIAL_LAP_COMPLETE = key("timetrial.lap_complete");
    public static final Key TIMETRIAL_PRACTICEPOINT_PLACE = key("timetrial.practicepoint_place");
    public static final Key TIMETRIAL_PRACTICEPOINT_REMOVE = key("timetrial.practicepoint_remove");
    public static final Key TIMETRIAL_GET_RECORD = key("timetrial.get_record");

    // Generic (routed from GenericMessageEvent / GenericErrorMessageEvent)
    public static final Key GENERIC = key("generic");
    public static final Key GENERIC_ERROR = key("generic_error");

    /**
     * Routes a message through {@link IceStomMessageEvent} and sends it unless a listener cancels.
     * The event may replace the component before it is sent.
     */
    public static void send(@NotNull Player player, @NotNull Key key, @NotNull Component component) {
        IceStomMessageEvent event = new IceStomMessageEvent(player, key, component);
        MinecraftServer.getGlobalEventHandler().call(event);

        if (event.isCancelled()) return;

        player.sendMessage(event.getComponent());
    }
}
