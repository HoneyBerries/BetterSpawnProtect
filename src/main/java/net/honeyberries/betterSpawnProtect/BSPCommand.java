package net.honeyberries.betterSpawnProtect;

import org.bukkit.Location;
import org.bukkit.command.*;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;

public class BSPCommand implements TabExecutor {

    private final ProtectionManager protectionManager;
    private final ConfigManager configManager;

    public BSPCommand(ProtectionManager protectionManager, ConfigManager configManager) {
        this.protectionManager = protectionManager;
        this.configManager = configManager;
    }

    private boolean hasAdmin(CommandSender sender) {
        return sender.hasPermission("betterspawnprotect.admin") || sender.isOp();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {

        if (args.length == 0) {
            sendHelp(sender, label);
            return true;
        }

        String sub = args[0].toLowerCase();

        switch (sub) {
            case "info":
                ChatUtil.sendMessage(sender, "<green>[BetterSpawnProtect] " + protectionManager.getCenterSummary());
                return true;

            case "setcenter":
                if (!hasAdmin(sender)) {
                    noPerm(sender);
                    return true;
                }
                if (!(sender instanceof Player) && args.length < 4) {
                    ChatUtil.sendMessage(sender, "<red>Console must specify coordinates: /" + label + " setcenter <x> <y> <z>");
                    return true;
                }
                Location loc;
                if (args.length >= 4) {
                    try {
                        double x = Double.parseDouble(args[1]);
                        double y = Double.parseDouble(args[2]);
                        double z = Double.parseDouble(args[3]);
                        if (sender instanceof Player) {
                            loc = new Location(((Player) sender).getWorld(), x, y, z);
                        } else {
                            // Console: world must already be the configured world (or attempt world from config)
                            if (protectionManager.getWorld() == null) {
                                ChatUtil.sendMessage(sender, "<red>World not available. Load the world first.");
                                return true;
                            }
                            loc = new Location(protectionManager.getWorld(), x, y, z);
                        }
                    } catch (NumberFormatException e) {
                        ChatUtil.sendMessage(sender, "<red>Invalid coordinates.");
                        return true;
                    }
                } else {
                    // Player's current location
                    Player p = (Player) sender;
                    loc = p.getLocation();
                }
                protectionManager.setCenter(loc);
                ChatUtil.sendMessage(sender, "<green>Spawn protection center set. " + protectionManager.getCenterSummary());
                return true;

            case "setradius":
                if (!hasAdmin(sender)) {
                    noPerm(sender);
                    return true;
                }
                if (args.length < 2) {
                    ChatUtil.sendMessage(sender, "<red>Usage: /" + label + " setradius <number>");
                    return true;
                }
                try {
                    double r = Double.parseDouble(args[1]);
                    if (r < 0) {
                        ChatUtil.sendMessage(sender, "<red>Radius must be >= 0");
                        return true;
                    }
                    protectionManager.setRadius(r);
                    ChatUtil.sendMessage(sender, "<green>Spawn protection radius set to " + r);
                } catch (NumberFormatException e) {
                    ChatUtil.sendMessage(sender, "<red>Invalid radius.");
                }
                return true;

            case "reload":
                if (!hasAdmin(sender)) {
                    noPerm(sender);
                    return true;
                }
                BetterSpawnProtect.getInstance().reloadAll();
                ChatUtil.sendMessage(sender, "<green>BetterSpawnProtect reloaded.");
                return true;

            default:
                sendHelp(sender, label);
                return true;
        }
    }

    private void sendHelp(CommandSender sender, String label) {
        ChatUtil.sendMessage(sender, "<yellow>--- BetterSpawnProtect Commands ---");
        ChatUtil.sendMessage(sender, "<yellow>/" + label + " info <gray>- Show protection info");
        if (hasAdmin(sender)) {
            ChatUtil.sendMessage(sender, "<yellow>/" + label + " setcenter [x y z] <gray>- Set center (coords or your location)");
            ChatUtil.sendMessage(sender, "<yellow>/" + label + " setradius <r> <gray>- Set radius");
            ChatUtil.sendMessage(sender, "<yellow>/" + label + " reload <gray>- Reload config");
        }
    }

    private void noPerm(CommandSender sender) {
        ChatUtil.sendMessage(sender, "<red>You lack permission.");
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            String partial = args[0].toLowerCase();
            if ("info".startsWith(partial)) out.add("info");
            if (hasAdmin(sender)) {
                if ("setcenter".startsWith(partial)) out.add("setcenter");
                if ("setradius".startsWith(partial)) out.add("setradius");
                if ("reload".startsWith(partial)) out.add("reload");
            }
        }
        return out;
    }
}