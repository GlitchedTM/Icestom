package io.gitlab.icestom.stomtrack.type;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.SerializerProvider;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.deser.std.StdDeserializer;
import com.fasterxml.jackson.databind.ser.std.StdSerializer;
import org.jetbrains.annotations.NotNull;

import java.io.IOException;

/**
 * A rotation quaternion in {@code x,y,z,w} order &mdash; the same layout used by vanilla display
 * entity {@code left_rotation} / {@code right_rotation} NBT and by Minestom's
 * {@code AbstractDisplayMeta.setLeftRotation(float[])} / {@code setRightRotation(float[])} (which
 * take a {@code [x, y, z, w]} float array). Stored verbatim so display transforms round-trip
 * losslessly with no euler decomposition.
 */
@JsonSerialize(using = Quaternion.Serializer.class)
@JsonDeserialize(using = Quaternion.Deserializer.class)
public record Quaternion(float x, float y, float z, float w) {

    /** Identity rotation. */
    public static final Quaternion IDENTITY = new Quaternion(0, 0, 0, 1);

    /** The {@code [x, y, z, w]} array expected by Minestom display metadata rotation setters. */
    public float[] toArray() {
        return new float[]{x, y, z, w};
    }

    @Override
    public @NotNull String toString() {
        return x + "," + y + "," + z + "," + w;
    }

    static Quaternion parse(String text) {
        String[] parts = text.split(",");
        if (parts.length != 4) {
            throw new IllegalArgumentException("Expected \"x,y,z,w\", got: " + text);
        }
        return new Quaternion(
                Float.parseFloat(parts[0].trim()),
                Float.parseFloat(parts[1].trim()),
                Float.parseFloat(parts[2].trim()),
                Float.parseFloat(parts[3].trim())
        );
    }

    static final class Serializer extends StdSerializer<Quaternion> {
        Serializer() { super(Quaternion.class); }

        @Override
        public void serialize(Quaternion value, JsonGenerator gen, SerializerProvider provider) throws IOException {
            gen.writeString(value.toString());
        }
    }

    static final class Deserializer extends StdDeserializer<Quaternion> {
        Deserializer() { super(Quaternion.class); }

        @Override
        public Quaternion deserialize(JsonParser p, DeserializationContext context) throws IOException {
            return Quaternion.parse(p.getValueAsString());
        }
    }
}
