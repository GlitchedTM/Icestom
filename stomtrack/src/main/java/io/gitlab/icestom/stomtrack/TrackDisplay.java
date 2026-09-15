package io.gitlab.icestom.stomtrack;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.gitlab.icestom.stomtrack.type.Quaternion;
import io.gitlab.icestom.stomtrack.type.Vec3;
import net.kyori.adventure.text.Component;

/**
 * An in-world display entity placed on a track (a vanilla {@code display} entity).
 *
 * <p>This is a sealed type modelled after {@link TrackCheckpoint} / {@link TrackRegion}: it
 * serializes with a {@code WRAPPER_OBJECT} type tag so new variants (e.g. {@code block_display},
 * {@code item_display}) can be added later without a format break. v1 ships {@link TextDisplay}
 * only.</p>
 *
 * <p>Displays carry the entity's world {@link #position()} plus its <strong>raw transform</strong>
 * (translation, left/right rotation quaternions, scale) exactly as stored on a vanilla display
 * entity. The mod harvests these four transform components verbatim from NBT and the server applies
 * them directly to the display metadata, so there is no lossy euler-angle conversion and no
 * yaw/pitch convention to reconcile.</p>
 */
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.WRAPPER_OBJECT)
@JsonSubTypes({
        @JsonSubTypes.Type(value = TrackDisplay.TextDisplay.class, name = "text"),
})
public sealed interface TrackDisplay permits TrackDisplay.TextDisplay {

    /** The display entity's world position (block/precise coordinate). */
    Vec3 position();

    /**
     * The transform's translation, applied on top of {@link #position()}. Maps to Minestom
     * {@code AbstractDisplayMeta.setTranslation}.
     */
    Vec3 translation();

    /**
     * The transform's left rotation quaternion. Maps to Minestom
     * {@code AbstractDisplayMeta.setLeftRotation}.
     */
    Quaternion leftRotation();

    /**
     * The transform's right rotation quaternion. Maps to Minestom
     * {@code AbstractDisplayMeta.setRightRotation}.
     */
    Quaternion rightRotation();

    /**
     * The transform's scale. Maps to Minestom {@code AbstractDisplayMeta.setScale}.
     */
    Vec3 scale();

    /**
     * A vanilla {@code text_display}.
     *
     * <p>Transform fields ({@code position}, {@code translation}, {@code leftRotation},
     * {@code rightRotation}, {@code scale}) are harvested verbatim from the display entity. The
     * remaining fields mirror vanilla {@code text_display} metadata:</p>
     * <ul>
     *     <li>{@code text} &mdash; the displayed text as an Adventure {@link Component}. On disk it
     *     is stored as a gson-serialized component (via the {@code ComponentSerde} module already
     *     registered on the track {@code XmlMapper}). The editor/mod must produce a gson-serialized
     *     Adventure Component for this field.</li>
     *     <li>{@code billboard} &mdash; how the display rotates to face the viewer.</li>
     *     <li>{@code backgroundColor} &mdash; ARGB background color. A negative value (default
     *     {@code -1}) means "use the client default background".</li>
     *     <li>{@code textOpacity} &mdash; text opacity as a signed byte (vanilla convention;
     *     {@code -1} = fully opaque).</li>
     *     <li>{@code seeThrough} &mdash; whether the text renders through blocks.</li>
     *     <li>{@code shadow} &mdash; whether the text has a drop shadow.</li>
     *     <li>{@code alignment} &mdash; horizontal text alignment.</li>
     *     <li>{@code lineWidth} &mdash; max line width before wrapping.</li>
     * </ul>
     */
    record TextDisplay(
            Vec3 position,
            Vec3 translation,
            Quaternion leftRotation,
            Quaternion rightRotation,
            Vec3 scale,
            Component text,
            Billboard billboard,
            int backgroundColor,
            byte textOpacity,
            boolean seeThrough,
            boolean shadow,
            Alignment alignment,
            int lineWidth
    ) implements TrackDisplay {
    }

    /**
     * Billboard constraint, mapping 1:1 to Minestom
     * {@code AbstractDisplayMeta.BillboardConstraints}.
     */
    enum Billboard {
        FIXED,
        VERTICAL,
        HORIZONTAL,
        CENTER
    }

    /**
     * Horizontal text alignment, mapping 1:1 to Minestom {@code TextDisplayMeta.Alignment}.
     */
    enum Alignment {
        LEFT,
        CENTER,
        RIGHT
    }
}
