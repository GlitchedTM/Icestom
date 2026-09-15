package io.gitlab.icestom.icestom.ui.menu;

import io.gitlab.icestom.icestom.entity.BoatType;
import io.gitlab.icestom.icestom.entity.IceStomPlayer;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import net.kyori.adventure.text.format.Style;
import net.kyori.adventure.text.format.TextDecoration;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.minestom.server.entity.EntityType;
import net.minestom.server.event.GlobalEventHandler;
import net.minestom.server.event.inventory.InventoryPreClickEvent;
import net.minestom.server.inventory.AbstractInventory;
import net.minestom.server.inventory.Inventory;
import net.minestom.server.inventory.InventoryType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;

import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;

/**
 * A clickable inventory GUI for choosing a boat type ({@code /s boat}).
 *
 * <p>Each slot maps to a selectable {@link BoatType} entry; clicking one records the choice on the
 * {@link IceStomPlayer} (applied the next time a boat spawns for them) and closes the menu. The
 * menu is display-only: all clicks are cancelled so nothing can be taken out.</p>
 */
public final class BoatSelectorMenu {

    // Weak keys so closed menus drop out without manual cleanup.
    private static final Map<AbstractInventory, List<EntityType>> OPEN_MENUS = new WeakHashMap<>();

    private BoatSelectorMenu() {}

    public static void registerListener(GlobalEventHandler globalEventHandler) {
        globalEventHandler.addListener(InventoryPreClickEvent.class, event -> {
            List<EntityType> slots = OPEN_MENUS.get(event.getInventory());
            if (slots == null) return;

            event.setCancelled(true);

            if (!(event.getPlayer() instanceof IceStomPlayer player)) return;

            int slot = event.getSlot();
            if (slot < 0 || slot >= slots.size()) return;

            EntityType chosen = slots.get(slot);
            if (chosen == null) return;

            player.setBoatType(chosen);
            player.sendMessage(boatSelectedMessage(chosen));

            player.closeInventory();
        });
    }

    static Component boatSelectedMessage(EntityType type) {
        return Component.translatable("setting.boat.selected",
                Argument.component("boat", Component.text(BoatType.displayName(type))));
    }

    public static void open(IceStomPlayer player) {
        List<EntityType> boats = BoatType.values();

        InventoryType size = sizeFor(boats.size());
        Inventory inventory = new Inventory(size, Component.translatable("menu.boat.title"));

        EntityType current = player.getBoatType();

        for (int slot = 0; slot < boats.size(); slot++) {
            EntityType type = boats.get(slot);
            inventory.setItemStack(slot, icon(type, type.equals(current)));
        }

        OPEN_MENUS.put(inventory, boats);
        player.openInventory(inventory);
    }

    private static ItemStack icon(EntityType type, boolean selected) {
        Material material = BoatType.iconMaterial(type);
        if (material == null) material = Material.OAK_BOAT;

        NamedTextColor color = selected ? NamedTextColor.GREEN : NamedTextColor.WHITE;

        Component name = Component.text(BoatType.displayName(type), color)
                .style(Style.style().decoration(TextDecoration.ITALIC, false).build());

        return ItemStack.of(material).withCustomName(name);
    }

    private static InventoryType sizeFor(int count) {
        int rows = Math.max(1, Math.min(6, (count + 8) / 9));

        return switch (rows) {
            case 1 -> InventoryType.CHEST_1_ROW;
            case 2 -> InventoryType.CHEST_2_ROW;
            case 3 -> InventoryType.CHEST_3_ROW;
            case 4 -> InventoryType.CHEST_4_ROW;
            case 5 -> InventoryType.CHEST_5_ROW;
            default -> InventoryType.CHEST_6_ROW;
        };
    }
}
