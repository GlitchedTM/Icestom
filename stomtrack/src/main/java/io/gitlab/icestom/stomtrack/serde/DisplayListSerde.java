package io.gitlab.icestom.stomtrack.serde;

import com.fasterxml.jackson.core.JsonGenerator;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.*;
import com.fasterxml.jackson.databind.node.ObjectNode;
import io.gitlab.icestom.stomtrack.TrackDisplay;

import java.io.IOException;
import java.util.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Serde for the track {@code displays} section: a {@code List<TrackDisplay>}.
 *
 * <p>Follows the same shape as {@link CheckpointMapSerde} / {@link LocationMapSerde}: each entry is
 * written under an element named after its sealed-type tag (e.g. {@code text}). This keeps the
 * {@code .track.xml} layout consistent with the other sections and lets Jackson resolve the
 * {@link TrackDisplay} subtype via its {@code WRAPPER_OBJECT} type info on read.</p>
 */
public class DisplayListSerde {

    private static final Logger LOGGER = Logger.getLogger(DisplayListSerde.class.getName());

    // Needs the Component (de)serializer since TextDisplay carries an Adventure Component.
    private static final ObjectMapper MAPPER = new ObjectMapper()
            .registerModule(ComponentSerde.module());

    // The sealed-type tags declared via @JsonSubTypes on TrackDisplay. Keep in sync when adding
    // variants (e.g. "block", "item").
    private static final Set<String> KNOWN_TYPES = Set.of("text");

    public static class Serializer extends JsonSerializer<List<TrackDisplay>> {
        @Override
        public void serialize(List<TrackDisplay> value,
                              JsonGenerator gen,
                              SerializerProvider serializers) throws IOException {
            gen.writeStartObject();

            for (TrackDisplay display : value) {
                // valueToTree uses the type's @JsonTypeInfo, yielding { "<tag>": { ...fields } }
                ObjectNode wrapper = MAPPER.valueToTree(display);
                String typeName = wrapper.fieldNames().next();
                ObjectNode inner = (ObjectNode) wrapper.get(typeName);

                gen.writeFieldName(typeName);
                gen.writeTree(inner);
            }

            gen.writeEndObject();
        }
    }

    public static class Deserializer extends JsonDeserializer<List<TrackDisplay>> {
        @Override
        public List<TrackDisplay> deserialize(JsonParser p,
                                              DeserializationContext ctxt) throws IOException {
            JsonNode root = p.getCodec().readTree(p);
            List<TrackDisplay> result = new ArrayList<>();

            collect(root, result);

            return result;
        }

        /**
         * Walks the parsed node collecting displays. Each key is the sealed-type tag (e.g.
         * {@code text}). The Jackson XML mapper (with {@code useWrapping = false} on the field) can
         * hand us either the {@code displays} content directly or the tree still wrapped in a
         * {@code displays} element, so any key that is not a known display type is treated as a
         * wrapper and recursed into.
         */
        private void collect(JsonNode node, List<TrackDisplay> result) {
            if (node == null || !node.isObject()) return;

            Iterator<Map.Entry<String, JsonNode>> fields = node.fields();
            while (fields.hasNext()) {
                Map.Entry<String, JsonNode> field = fields.next();
                String typeName = field.getKey();
                JsonNode fieldValue = field.getValue();

                if (!KNOWN_TYPES.contains(typeName)) {
                    // Wrapper element (e.g. an extra "displays" layer); descend into it.
                    if (fieldValue.isArray()) {
                        for (JsonNode item : fieldValue) collect(item, result);
                    } else {
                        collect(fieldValue, result);
                    }
                    continue;
                }

                if (fieldValue.isArray()) {
                    for (JsonNode item : fieldValue) {
                        addDisplay(typeName, item, result);
                    }
                } else {
                    addDisplay(typeName, fieldValue, result);
                }
            }
        }

        /**
         * Decodes a single display entry. A malformed entry (unparseable fields, a stale
         * pre-reshape shape, an unknown subtype, ...) is logged and skipped rather than propagated,
         * so one bad decoration can never fail the whole track load.
         */
        private void addDisplay(String typeName, JsonNode item,
                                List<TrackDisplay> result) {
            try {
                ObjectNode wrapper = ((ObjectNode) item).objectNode();
                wrapper.set(typeName, item.deepCopy());

                TrackDisplay display = MAPPER.treeToValue(wrapper, TrackDisplay.class);
                result.add(display);
            } catch (Exception e) {
                LOGGER.log(Level.WARNING,
                        "Skipping malformed \"" + typeName + "\" display; the rest of the track is unaffected. Node: " + item,
                        e);
            }
        }
    }
}
