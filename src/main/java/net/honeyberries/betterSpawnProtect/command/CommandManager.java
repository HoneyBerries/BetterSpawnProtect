package net.honeyberries.betterSpawnProtect.command;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.DoubleArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.builder.RequiredArgumentBuilder;
import com.mojang.brigadier.tree.LiteralCommandNode;
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

/**
 * Manages the registration and execution of commands for the BetterSpawnProtect plugin.
 * Provides commands for viewing protection info, setting the center and radius of the
 * protected area, and reloading the plugin configuration.
 */
public class CommandManager {

    // Reference to the main plugin instance
    private final BetterSpawnProtect plugin;

    // Reference to the ProtectionManager for managing protection logic
    private final ProtectionManager protectionManager;

    /**
     * Constructs a CommandManager with the given plugin and ProtectionManager.
     *
     * @param plugin The main plugin instance.
     * @param protectionManager The ProtectionManager instance.
     */
    public CommandManager(BetterSpawnProtect plugin, ProtectionManager protectionManager) {
        this.plugin = plugin;
        this.protectionManager = protectionManager;
    }

    /**
     * Builds and returns the root command node for the BetterSpawnProtect commands.
     *
     * @return The root command node.
     */
    public LiteralCommandNode<CommandSourceStack> getBuildCommand() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("betterspawnprotect")
                .requires(source -> source.getSender().hasPermission("betterspawnprotect.admin"))
                .then(info())
                .then(setCenter())
                .then(setRadius())
                .then(reload())
                .executes(ctx -> {
                    sendHelp(ctx.getSource().getSender());
                    return Command.SINGLE_SUCCESS;
                }).build();
    }

    /**
     * Creates the "info" subcommand, which displays the current protection info.
     *
     * @return The "info" command builder.
     */
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

    /**
     * Creates the "setcenter" subcommand, which sets the center of the protected area.
     * Allows setting the center to the player's current location or specified coordinates.
     *
     * @return The "setcenter" command builder.
     */
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

    /**
     * Creates the "setradius" subcommand, which sets the radius of the protected area.
     *
     * @return The "setradius" command builder.
     */
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

    /**
     * Creates the "reload" subcommand, which reloads the plugin configuration.
     *
     * @return The "reload" command builder.
     */
    private LiteralArgumentBuilder<CommandSourceStack> reload() {
        return LiteralArgumentBuilder.<CommandSourceStack>literal("reload")
                .executes(ctx -> {
                    plugin.reloadAll();
                    ctx.getSource().getSender().sendMessage(Component.text("BetterSpawnProtect reloaded.", NamedTextColor.GREEN));
                    return Command.SINGLE_SUCCESS;
                });
    }

    /**
     * Sends a help message to the command sender, listing all available commands.
     *
     * @param sender The command sender.
     */
    private void sendHelp(CommandSender sender) {
        sender.sendMessage(Component.text("------------ BetterSpawnProtect Commands ------------", NamedTextColor.GOLD));
        sender.sendMessage(Component.text("/bsp info", NamedTextColor.GREEN)
                .append(Component.text(" - Show protection info", NamedTextColor.AQUA)));
        sender.sendMessage(Component.text("/bsp setcenter [x y z]", NamedTextColor.GREEN)
                .append(Component.text(" - Set center (coords or your location)", NamedTextColor.AQUA)));
        sender.sendMessage(Component.text("/bsp setradius <r>", NamedTextColor.GREEN)
                .append(Component.text(" - Set radius", NamedTextColor.AQUA)));
        sender.sendMessage(Component.text("/bsp reload", NamedTextColor.GREEN)
                .append(Component.text(" - Reload config", NamedTextColor.AQUA)));
        sender.sendMessage(Component.text("----------------------------------------------------", NamedTextColor.GOLD));
    }
}