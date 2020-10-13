package net.craftgalaxy.galaxycore.util.java;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class CooldownList<T> {

    private final List<T> cache;
    private final Map<T, Long> cooldownTimes;
    private final long duration;

    public CooldownList(TimeUnit unit, long duration) {
        this.cache = new ArrayList<>();
        this.cooldownTimes = new HashMap<>();
        this.duration = unit.toMillis(duration);
    }

    public void addCooldown(T value) {
        this.cache.add(value);
        this.cooldownTimes.put(value, System.currentTimeMillis());
    }

    public long getSecondsRemaining(T value) {
        if (this.cache.contains(value) && this.cooldownTimes.containsKey(value) && !this.isExpired(value)) {
            return TimeUnit.MILLISECONDS.toSeconds(this.duration - System.currentTimeMillis() + this.cooldownTimes.get(value));
        }

        this.cache.remove(value);
        this.cooldownTimes.remove(value);
        return 0L;
    }

    public boolean isExpired(T value) {
        return !this.cooldownTimes.containsKey(value) || System.currentTimeMillis() - this.cooldownTimes.get(value) >= this.duration;
    }

    public void clear() {
        this.cooldownTimes.clear();
        this.cache.clear();
    }
}