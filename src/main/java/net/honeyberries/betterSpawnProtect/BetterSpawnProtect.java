package net.honeyberries.betterSpawnProtect;

import net.honeyberries.betterSpawnProtect.command.CommandManager;
import net.honeyberries.betterSpawnProtect.manager.MessageGate;
import net.honeyberries.betterSpawnProtect.manager.ProtectionListener;
import net.honeyberries.betterSpawnProtect.manager.ProtectionManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class BetterSpawnProtect extends JavaPlugin {

    private static BetterSpawnProtect instance;
    private ProtectionManager protectionManager;
    private MessageGate messageGate;

    public static BetterSpawnProtect getInstance() {
        return instance;
    }

    @Override
    public void onLoad() {
        instance = this;
    }

    @Override
    public void onEnable() {
        this.protectionManager = new ProtectionManager();
        this.messageGate = new MessageGate(2000L); // 2s cooldown per player

        // Register listeners
        Bukkit.getPluginManager().registerEvents(new ProtectionListener(protectionManager, messageGate), this);

        // Register command
        CommandManager commandManager = new CommandManager(this, protectionManager);
        this.getLifecycleManager().registerEventHandler(io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents.COMMANDS, event -> {
            event.registrar().register(commandManager.getRootCommand().build(), "BetterSpawnProtect command", java.util.List.of("bsp"));
        });

        getLogger().info("BetterSpawnProtect enabled. Center: " + protectionManager.getCenterSummary());
    }

    @Override
    public void onDisable() {
        // Nothing to do here anymore
    }

    public void reloadAll() {
        protectionManager.reloadFromConfig();
        getLogger().info("BetterSpawnProtect reloaded. " + protectionManager.getCenterSummary());
    }
}