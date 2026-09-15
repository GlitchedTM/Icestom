package io.gitlab.icestom.stomtrack;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlProperty;
import io.github.openboatutils.protocol.channels.OBUSettingsPacket;
import io.gitlab.icestom.stomtrack.type.Location;
import net.kyori.adventure.text.Component;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Map;
import java.util.Set;

public interface TrackFile {

    /**
     * On-disk track format version. Written to the {@code version} attribute of every track file.
     *
     * <p>History:</p>
     * <ul>
     *     <li>2 &mdash; pre-displays.</li>
     *     <li>3 &mdash; added the {@code displays} section (initial {@code text} shape: a
     *     {@code location} + scalar {@code scale}).</li>
     *     <li>4 &mdash; reshaped {@code text} displays to carry the raw entity transform
     *     ({@code position}, {@code translation}, {@code leftRotation}, {@code rightRotation},
     *     {@code scale}). The version-3 display shape is not readable; such entries are skipped with
     *     a warning by {@code DisplayListSerde} rather than failing the load.</li>
     * </ul>
     *
     * <p>Note: nothing currently branches on this value at load time; it is bookkeeping/telemetry.
     * Load resilience for stale shapes is handled by {@code DisplayListSerde} skipping malformed
     * entries.</p>
     */
    int VERSION = 4;

    @JacksonXmlProperty(isAttribute = true)
    default int getVersion() {
        return VERSION;
    }

    @NotNull String getId();
    @NotNull Component getName();

    boolean isLooped();

    @NotNull Location getSpawnLocation();
    @NotNull Map<String, String> getTags();
    @NotNull Map<TrackCheckpoint, Integer> getCheckpoints();
    @NotNull List<Location> getGrid();
    @NotNull List<OBUSettingsPacket> getOpenBoatUtils();
    @NotNull Map<TrackRegion, Set<String>> getRegions();
    @NotNull Map<TrackTrigger, Set<String>> getTriggers();
    @NotNull Map<String, Location> getLocations();
    @NotNull List<TrackDisplay> getDisplays();
}
