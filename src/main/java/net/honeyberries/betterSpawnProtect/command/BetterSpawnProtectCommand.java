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
 * This class provides commands for viewing protection information, setting the center
 * and radius of the protected area, and reloading the plugin's configuration. It is
 * designed to be used with Paper's Brigadier command system.
 */
public class BetterSpawnProtectCommand {

    private final BetterSpawnProtect plugin;
    private final ProtectionManager protectionManager;

    /**
     * Constructs a new {@code BetterSpawnProtectCommand} with a reference to the main plugin
     * instance and the {@link ProtectionManager}.
     *
     * @param plugin            The main instance of the BetterSpawnProtect plugin.
     * @param protectionManager The manager for handling protection logic.
     */
    public BetterSpawnProtectCommand(BetterSpawnProtect plugin, ProtectionManager protectionManager) {
        this.plugin = plugin;
        this.protectionManager = protectionManager;
    }

    /**
     * Builds and returns the root command node for all BetterSpawnProtect commands. This
     * node serves as the entry point for all subcommands and requires the sender to have
     * the {@code betterspawnprotect.admin} permission.
     *
     * @return The fully constructed {@link LiteralCommandNode} for the root command.
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
     * Creates the "info" subcommand, which displays the current protection status,
     * including the center coordinates and radius.
     *
     * @return A {@link LiteralArgumentBuilder} for the "info" subcommand.
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
     * Creates the "setcenter" subcommand, allowing an admin to set the center of the
     * protected area. If executed by a player, it uses the player's current location.
     * It also supports specifying coordinates.
     *
     * @return A {@link LiteralArgumentBuilder} for the "setcenter" subcommand.
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
                        return Command.SINGLE_SUCCESS;
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
     * Creates the "setradius" subcommand for setting the radius of the protected area.
     * The radius must be a non-negative double value.
     *
     * @return A {@link LiteralArgumentBuilder} for the "setradius" subcommand.
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
     * Creates the "reload" subcommand, which reloads the plugin's configuration from disk.
     *
     * @return A {@link LiteralArgumentBuilder} for the "reload" subcommand.
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
     * Sends a formatted help message to the command sender, listing all available commands
     * and their descriptions.
     *
     * @param sender The {@link CommandSender} to receive the help message.
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