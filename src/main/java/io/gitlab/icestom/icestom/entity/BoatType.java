package io.gitlab.icestom.icestom.entity;

import net.minestom.server.entity.EntityType;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * The set of rideable boat entity types a player may choose from via {@code /s boat}.
 *
 * <p>The list is derived from the entity registry at load time (every {@link EntityType} whose key
 * path ends in {@code _boat} or {@code _raft}, excluding chest variants) so it stays correct across
 * Minestom/Minecraft updates without a hand-maintained list. Ordering is registry order, which
 * groups the wood boats together with the bamboo raft last.</p>
 */
public final class BoatType {

    private static final Map<String, EntityType> BY_NAME = new LinkedHashMap<>();

    static {
        for (EntityType type : EntityType.values()) {
            String path = type.key().value();

            boolean isBoat = path.endsWith("_boat") || path.endsWith("_raft");
            boolean isChest = path.contains("chest");

            if (isBoat && !isChest) {
                BY_NAME.put(path, type);
            }
        }
    }

    private BoatType() {}

    public static EntityType defaultType() {
        return EntityType.OAK_BOAT;
    }

    public static List<EntityType> values() {
        return List.copyOf(BY_NAME.values());
    }

    public static List<String> names() {
        return List.copyOf(BY_NAME.keySet());
    }

    public static @Nullable EntityType fromName(String name) {
        return BY_NAME.get(name.toLowerCase(Locale.ROOT));
    }

    public static @Nullable Material iconMaterial(EntityType type) {
        return Material.fromKey(type.key());
    }

    public static String displayName(EntityType type) {
        String path = type.key().value();
        String[] parts = path.split("_");

        List<String> words = new ArrayList<>(parts.length);
        for (String part : parts) {
            if (part.isEmpty()) continue;
            words.add(Character.toUpperCase(part.charAt(0)) + part.substring(1));
        }

        return String.join(" ", words);
    }
}
