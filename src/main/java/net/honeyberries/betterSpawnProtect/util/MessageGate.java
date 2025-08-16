package net.honeyberries.betterSpawnProtect.util;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages message sending cooldowns for individual players to prevent spam. This utility
 * ensures that messages are not sent to a player more frequently than a specified cooldown
 * period. It is particularly useful for notifying players about protection restrictions
 * without overwhelming them with messages.
 */
public class MessageGate {

    /**
     * The cooldown period in milliseconds.
     */
    private final long cooldownMs;

    /**
     * A map storing the last time a message was sent to each player, identified by their UUID.
     */
    private final Map<UUID, Long> lastMessage = new HashMap<>();

    /**
     * Constructs a new {@code MessageGate} with the specified cooldown period.
     *
     * @param cooldownMs The cooldown period in milliseconds.
     */
    public MessageGate(long cooldownMs) {
        this.cooldownMs = cooldownMs;
    }

    /**
     * Determines if a message can be sent to the player with the given UUID based on the
     * cooldown. If the player is not on cooldown, this method updates their last message
     * time and returns {@code true}.
     *
     * @param uuid The UUID of the player.
     * @return {@code true} if the message can be sent, {@code false} otherwise.
     */
    public boolean canSend(UUID uuid) {
        long currentTime = System.currentTimeMillis();
        Long previous = lastMessage.get(uuid);

        // If the player has no previous message time or the cooldown has expired
        if (previous == null || (currentTime - previous) >= cooldownMs) {
            lastMessage.put(uuid, currentTime);
            return true;
        }
        return false;
    }
}