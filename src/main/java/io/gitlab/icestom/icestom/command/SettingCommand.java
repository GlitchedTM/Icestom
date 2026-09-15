package io.gitlab.icestom.icestom.command;

import io.gitlab.icestom.icestom.entity.BoatType;
import io.gitlab.icestom.icestom.entity.IceStomPlayer;
import io.gitlab.icestom.icestom.ui.menu.BoatSelectorMenu;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.translation.Argument;
import net.minestom.server.command.builder.Command;
import net.minestom.server.command.builder.CommandExecutor;
import net.minestom.server.command.builder.arguments.ArgumentType;
import net.minestom.server.command.builder.arguments.ArgumentWord;
import net.minestom.server.command.builder.arguments.minecraft.ArgumentItemStack;
import net.minestom.server.command.builder.suggestion.SuggestionEntry;
import net.minestom.server.entity.EntityType;
import net.minestom.server.item.ItemStack;
import net.minestom.server.item.Material;
import org.jetbrains.annotations.Nullable;

import java.util.function.BiConsumer;

public class SettingCommand extends Command {

    public SettingCommand() {
        super("setting", "s");

        ArgumentWord toggle = ArgumentType.Word("toggle");
        toggle.setSuggestionCallback((sender, context, suggestion) -> {
            suggestion.addEntry(new SuggestionEntry("true"));
            suggestion.addEntry(new SuggestionEntry("false"));
        });

        setDefaultExecutor((sender, context) ->
                sender.sendMessage(Component.translatable("command.setting.usage")));

        addSyntax(toggleExecutor(toggle,
                        IceStomPlayer::setCheckpointAnnouncements,
                        "setting.checkpoint_announcements.enabled",
                        "setting.checkpoint_announcements.disabled"),
                ArgumentType.Literal("checkpointAnnouncements"), toggle);

        addSyntax(toggleExecutor(toggle,
                        IceStomPlayer::setShowDeltas,
                        "setting.deltas.enabled",
                        "setting.deltas.disabled"),
                ArgumentType.Literal("deltas"), toggle);

        addSyntax(toggleExecutor(toggle,
                        IceStomPlayer::setHidePlayers,
                        "setting.hide_players.enabled",
                        "setting.hide_players.disabled"),
                ArgumentType.Literal("hideplayers"), toggle);

        ArgumentWord boatType = ArgumentType.Word("boatType");
        boatType.setSuggestionCallback((sender, context, suggestion) -> {
            for (String name : BoatType.names()) {
                suggestion.addEntry(new SuggestionEntry(name));
            }
        });

        addSyntax((sender, context) -> {
            if (!(sender instanceof IceStomPlayer player)) {
                sender.sendMessage(Component.translatable("command.generic.must_be_player"));
                return;
            }

            BoatSelectorMenu.open(player);
        }, ArgumentType.Literal("boat"));

        addSyntax((sender, context) -> {
            if (!(sender instanceof IceStomPlayer player)) {
                sender.sendMessage(Component.translatable("command.generic.must_be_player"));
                return;
            }

            @Nullable EntityType chosen = BoatType.fromName(context.get(boatType));

            if (chosen == null) {
                player.sendMessage(Component.translatable("setting.boat.unknown",
                        Argument.component("boat", Component.text(context.get(boatType)))));
                return;
            }

            player.setBoatType(chosen);
            player.sendMessage(Component.translatable("setting.boat.selected",
                    Argument.component("boat", Component.text(BoatType.displayName(chosen)))));
        }, ArgumentType.Literal("boat"), boatType);

        // ItemStack arg uses the client's native item picker, so every item tab-completes with no
        // server-sent suggestion list.
        ArgumentItemStack resetItem = ArgumentType.ItemStack("item");

        addSyntax((sender, context) -> {
            if (!(sender instanceof IceStomPlayer player)) {
                sender.sendMessage(Component.translatable("command.generic.must_be_player"));
                return;
            }

            ItemStack parsed = context.get(resetItem);
            Material material = parsed.material();

            if (parsed.isAir()) {
                player.sendMessage(Component.translatable("setting.reset_item.invalid"));
                return;
            }

            player.setResetItem(material);
            player.sendMessage(Component.translatable("setting.reset_item.selected",
                    Argument.component("item", Component.translatable(material.registry().translationKey()))));
        }, ArgumentType.Literal("resetitem"), resetItem);
    }

    private static CommandExecutor toggleExecutor(ArgumentWord toggle,
                                                  BiConsumer<IceStomPlayer, Boolean> apply,
                                                  String enabledKey,
                                                  String disabledKey) {
        return (sender, context) -> {
            if (!(sender instanceof IceStomPlayer player)) {
                sender.sendMessage(Component.translatable("command.generic.must_be_player"));
                return;
            }

            @Nullable Boolean value = parseToggle(context.get(toggle));

            if (value == null) {
                player.sendMessage(Component.translatable("command.setting.usage"));
                return;
            }

            apply.accept(player, value);

            player.sendMessage(Component.translatable(value ? enabledKey : disabledKey));
        };
    }

    private static @Nullable Boolean parseToggle(String raw) {
        return switch (raw.toLowerCase()) {
            case "on", "true", "enable", "enabled" -> Boolean.TRUE;
            case "off", "false", "disable", "disabled" -> Boolean.FALSE;
            default -> null;
        };
    }
}
