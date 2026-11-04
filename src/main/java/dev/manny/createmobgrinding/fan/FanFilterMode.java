package dev.manny.createmobgrinding.fan;

import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;

/**
 * Which entities an Encased Fan with the Fan Filter Upgrade ignores.
 */
public enum FanFilterMode {
    ITEMS,
    PLAYERS,
    MOBS;

    public boolean ignores(Entity entity) {
        return switch (this) {
            case ITEMS -> entity instanceof ItemEntity;
            case PLAYERS -> entity instanceof Player;
            case MOBS -> entity instanceof LivingEntity && !(entity instanceof Player);
        };
    }

    public FanFilterMode next() {
        return values()[(ordinal() + 1) % values().length];
    }

    public String getTranslationKey() {
        return "fan_filter.createmobgrinding.ignore_" + name().toLowerCase(java.util.Locale.ROOT);
    }

    public static FanFilterMode byName(String name) {
        for (FanFilterMode mode : values()) {
            if (mode.name().equals(name)) return mode;
        }
        return null;
    }
}
