package net.honeyberries.betterSpawnProtect.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.command.brigadier.argument.ArgumentTypes;
import io.papermc.paper.command.brigadier.argument.resolvers.BlockPositionResolver;
import net.honeyberries.betterSpawnProtect.BetterSpawnProtect;
import net.honeyberries.betterSpawnProtect.manager.ProtectionManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import io.papermc.paper.math.BlockPosition;

public class CommandManager {

    private final BetterSpawnProtect plugin;
    private final ProtectionManager protectionManager;

    public CommandManager(BetterSpawnProtect plugin, ProtectionManager protectionManager) {
        this.plugin = plugin;
        this.protectionManager = protectionManager;
    }

    public LiteralArgumentBuilder<CommandSourceStack> getRootCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("betterspawnprotect")
                .requires(source -> source.getSender().hasPermission("betterspawnprotect.admin"))
                .then(info())
                .then(setCenter())
                .then(setRadius())
                .then(reload())
                .executes(ctx -> {
                    sendHelp(ctx.getSource().getSender());
                    return Command.SINGLE_SUCCESS;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> info() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("info")
                .executes(ctx -> {
                    ctx.getSource().getSender().sendMessage(
                            Component.text("[BetterSpawnProtect] ", NamedTextColor.GREEN)
                                    .append(Component.text(protectionManager.getCenterSummary()))
                    );
                    return Command.SINGLE_SUCCESS;
                });
    }

    private LiteralArgumentBuilder<CommandSourceStack> setCenter() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("setcenter")
                .executes(ctx -> {
                    if (ctx.getSource().getSender() instanceof Player player) {
                        protectionManager.setCenter(player.getLocation());
                        ctx.getSource().getSender().sendMessage(
                                Component.text("Spawn protection center set to your location. ", NamedTextColor.GREEN)
                                        .append(Component.text(protectionManager.getCenterSummary()))
                        );
                        return Command.SINGLE_SUCCESS;
                    } else {
                        ctx.getSource().getSender().sendMessage(Component.text("Console must specify coordinates.", NamedTextColor.RED));
                        return 0;
                    }
                })
                .then(RequiredArgumentBuilder.<CommandSourceStack, BlockPositionResolver>argument("position", ArgumentTypes.blockPosition())
                        .executes(ctx -> {
                            BlockPosition pos = ctx.getArgument("position", BlockPositionResolver.class).resolve(ctx.getSource());
                            Location loc = new Location(protectionManager.getWorld(), pos.x(), pos.y(), pos.z());
                            protectionManager.setCenter(loc);
                            ctx.getSource().getSender().sendMessage(
                                    Component.text("Spawn protection center set. ", NamedTextColor.GREEN)
                                            .append(Component.text(protectionManager.getCenterSummary()))
                            );
                            return Command.SINGLE_SUCCESS;
                        }));
    }

    private LiteralArgumentBuilder<CommandSourceStack> setRadius() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("setradius")
                .then(RequiredArgumentBuilder.<CommandSourceStack, Double>argument("radius", DoubleArgumentType.doubleArg(0))
                        .executes(ctx -> {
                            double radius = ctx.getArgument("radius", Double.class);
                            protectionManager.setRadius(radius);
                            ctx.getSource().getSender().sendMessage(
                                    Component.text("Spawn protection radius set to " + radius, NamedTextColor.GREEN)
                            );
                            return Command.SINGLE_SUCCESS;
                        }));
    }

    private LiteralArgumentBuilder<CommandSourceStack> reload() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("reload")
                .executes(ctx -> {
                    plugin.reloadAll();
                    ctx.getSource().getSender().sendMessage(Component.text("BetterSpawnProtect reloaded.", NamedTextColor.GREEN));
                    return Command.SINGLE_SUCCESS;
                });
    }

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("--- BetterSpawnProtect Commands ---", NamedTextColor.YELLOW));
        sender.sendMessage(Component.text("/bsp info", NamedTextColor.YELLOW)
                .append(Component.text(" - Show protection info", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/bsp setcenter [x y z]", NamedTextColor.YELLOW)
                .append(Component.text(" - Set center (coords or your location)", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/bsp setradius <r>", NamedTextColor.YELLOW)
                .append(Component.text(" - Set radius", NamedTextColor.GRAY)));
        sender.sendMessage(Component.text("/bsp reload", NamedTextColor.YELLOW)
                .append(Component.text(" - Reload config", NamedTextColor.GRAY)));
    }
}
