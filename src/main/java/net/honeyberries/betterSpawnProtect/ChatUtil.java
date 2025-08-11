package net.honeyberries.betterSpawnProtect;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ChatUtil {

    public static Component mm(String s) {
        return MiniMessage.miniMessage().deserialize(s);
    }

    public static Component color(String s) {
        if (s == null) return Component.empty();
        return LegacyComponentSerializer.legacyAmpersand().deserialize(s);
    }

    public static void sendMessage(CommandSender sender, String msg) {
        Component comp = mm(msg);
        if (sender instanceof Player) {
            sender.sendMessage(comp);
        } else {
            sender.sendMessage(MiniMessage.miniMessage().stripTags(msg));
        }
    }
}