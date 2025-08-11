package net.honeyberries.betterSpawnProtect;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class MessageGate {
    private final long cooldownMs;
    private final Map<UUID, Long> last = new HashMap<>();

    public MessageGate(long cooldownMs) {
        this.cooldownMs = cooldownMs;
    }

    public boolean canSend(UUID uuid) {
        long now = System.currentTimeMillis();
        Long prev = last.get(uuid);
        if (prev == null || (now - prev) >= cooldownMs) {
            last.put(uuid, now);
            return true;
        }
        return false;
    }
}
