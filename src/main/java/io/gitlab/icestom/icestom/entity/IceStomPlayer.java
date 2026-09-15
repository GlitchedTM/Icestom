package io.gitlab.icestom.icestom.entity;

import io.gitlab.icestom.icestom.IceStom;
import io.gitlab.icestom.icestom.event.EventParticipant;
import io.gitlab.icestom.icestom.ui.theme.Themes;
import io.gitlab.icestom.icestom.ui.translation.TranslationManager;
import net.kyori.adventure.text.Component;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.item.Material;
import net.minestom.server.network.player.GameProfile;
import net.minestom.server.network.player.PlayerConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jspecify.annotations.NonNull;

import java.util.List;

public class IceStomPlayer extends Player implements EventParticipant {

    private static final TranslationManager translationManager = IceStom.getInstance().getTranslationManager();

    private Integer openBoatUtilsVersion = null;

    private boolean checkpointAnnouncements = true;

    private boolean showDeltas = false;

    private boolean hidePlayers = false;

    private EntityType boatType = BoatType.defaultType();

    // null means "use the server default reset item from config".
    private @Nullable Material resetItem = null;

    public IceStomPlayer(@NotNull PlayerConnection playerConnection, GameProfile profile) {
        super(playerConnection, profile);
    }

    public @NotNull Component translate(@NotNull Component component) {
        return translationManager.render(component, getLocale(), Themes.DEFAULT_THEME);
    }

    @Override
    public void sendMessage(@NotNull Component message) {
        super.sendMessage(translate(message));
    }

    @Override
    public void kick(@NonNull Component message) {
        super.kick(translate(message));
    }

    public boolean hasPermission(String permission) {
        return false;
    }

    public void setOpenBoatUtilsVersion(Integer openBoatUtilsVersion) { this.openBoatUtilsVersion = openBoatUtilsVersion; }

    public @Nullable Integer getOpenBoatUtilsVersion() {
        return openBoatUtilsVersion;
    }

    public boolean getCheckpointAnnouncements() {
        return checkpointAnnouncements;
    }

    public void setCheckpointAnnouncements(boolean checkpointAnnouncements) {
        this.checkpointAnnouncements = checkpointAnnouncements;
    }

    public boolean getShowDeltas() {
        return showDeltas;
    }

    public void setShowDeltas(boolean showDeltas) {
        this.showDeltas = showDeltas;
    }

    public boolean getHidePlayers() {
        return hidePlayers;
    }

    public void setHidePlayers(boolean hidePlayers) {
        this.hidePlayers = hidePlayers;

        updateViewerRule(hidePlayers ? this::shouldView : null);
    }

    private boolean shouldView(@NotNull Entity entity) {
        if (entity == this || entity == getVehicle()) return true;

        return !(entity instanceof Player) && !(entity instanceof Boat);
    }

    public @NotNull EntityType getBoatType() {
        return boatType;
    }

    public void setBoatType(@NotNull EntityType boatType) {
        this.boatType = boatType;
    }

    public @Nullable Material getResetItem() {
        return resetItem;
    }

    public void setResetItem(@NotNull Material resetItem) {
        this.resetItem = resetItem;
    }

    @Override
    public Player getCurrentPlayer() {
        return this;
    }

    @Override
    public List<Player> getParticipants() {
        return List.of(this);
    }
}
