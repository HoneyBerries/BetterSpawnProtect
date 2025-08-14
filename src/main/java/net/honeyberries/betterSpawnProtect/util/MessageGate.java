package net.honeyberries.betterSpawnProtect.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages message sending cooldowns for players.
 * Ensures that messages are not sent more frequently than the specified cooldown period.
 */
public class MessageGate {

    // The cooldown period in milliseconds
    private final long cooldownMs;

    // A map storing the lastMessage message send time for each player (by UUID)
    private final Map<UUID, Long> lastMessage = new HashMap<>();

    /**
     * Constructs a MessageGate with the specified cooldown period.
     *
     * @param cooldownMs The cooldown period in milliseconds.
     */
    public MessageGate(long cooldownMs) {
        this.cooldownMs = cooldownMs;
    }

    /**
     * Checks if a message can be sent by the player with the given UUID.
     * Updates the lastMessage message send time if the message can be sent.
     *
     * @param uuid The UUID of the player.
     * @return true if the message can be sent, false otherwise.
     */
    public boolean canSend(UUID uuid) {
        long currentTime = System.currentTimeMillis();
        Long previous = lastMessage.get(uuid);
        if (previous == null || (currentTime - previous) >= cooldownMs) {
            lastMessage.put(uuid, currentTime);
            return true;
        }
        return false;
    }
}