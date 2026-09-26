package io.gitlab.icestom.icestom.entity;

import net.kyori.adventure.key.Key;
import net.minestom.server.MinecraftServer;
import net.minestom.server.entity.Entity;
import net.minestom.server.entity.EntityType;
import net.minestom.server.entity.Player;
import net.minestom.server.event.player.PlayerEntityInteractEvent;
import net.minestom.server.event.player.PlayerPacketEvent;
import net.minestom.server.item.Material;
import net.minestom.server.network.packet.client.play.ClientVehicleMovePacket;
import net.minestom.server.network.packet.server.play.EntityHeadLookPacket;
import net.minestom.server.tag.Tag;
import org.jetbrains.annotations.NotNull;

public class Boat extends Entity {

    public Boat(Key type) {
        super(resolveBoatType(type));

        hasPhysics = false;

        eventNode().addListener(PlayerEntityInteractEvent.class, entityInteractEvent -> {
            if (getPassengers().size() >= 2) return;

            addPassenger(entityInteractEvent.getPlayer());
        });

        eventNode().addListener(PlayerPacketEvent.class, event -> {
            if (event.getPacket() instanceof ClientVehicleMovePacket clientVehicleMovePacket) {
                teleport(clientVehicleMovePacket.position());
            }
        });
    }

    // Resolves a boat_type preference key to its boat entity type. Non-minecraft keys are rejected
    // (a custom handler should have rewritten them); an unknown or non-boat key falls back to oak
    // rather than crashing the join.
    private static EntityType resolveBoatType(Key type) {
        if (!type.namespace().equals(Key.MINECRAFT_NAMESPACE)) {
            throw new RuntimeException("Invalid boat type " + type);
        }

        EntityType entityType = EntityType.fromKey(type);
        String path = type.value();
        boolean isBoat = path.endsWith("_boat") || path.endsWith("_raft");

        if (entityType == null || !isBoat) {
            return EntityType.OAK_BOAT;
        }

        return entityType;
    }

    @Override
    @SuppressWarnings("UnstableApiUsage")
    public void updateNewViewer(@NotNull Player player) {
        super.updateNewViewer(player);
        player.sendPacket(this.getPassengersPacket());
    }

    @Override
    protected void movementTick() {}

    @Override
    public void tick(long time) {
        super.tick(time);

        float yaw = getPosition().yaw();

        for (Entity passenger : getPassengers()) {
            if (passenger instanceof Player player) {
                sendPacketToViewers(new EntityHeadLookPacket(player.getEntityId(), yaw));
            }
        }
    }
}
