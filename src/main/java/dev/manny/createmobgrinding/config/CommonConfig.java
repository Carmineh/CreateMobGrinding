package dev.manny.createmobgrinding.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class CommonConfig {
    public final ModConfigSpec.DoubleValue spawnerBaseProgress;
    public final ModConfigSpec.DoubleValue grinderBaseDamage;
    public final ModConfigSpec.ConfigValue<List<? extends String>> spawnerBlacklist;

    public CommonConfig(ModConfigSpec.Builder builder) {
        builder.push("spawner");
        spawnerBaseProgress = builder
                .comment("Base progress points required for a spawn. Will be multiplied by mob tier.")
                .defineInRange("spawnerBaseProgress", 2000.0, 1.0, Double.MAX_VALUE);
                
        spawnerBlacklist = builder
                .comment("List of entity types that cannot be spawned. (e.g. minecraft:warden)")
                .defineList("spawnerBlacklist", List.of("minecraft:warden", "minecraft:ender_dragon"), obj -> obj instanceof String);
        builder.pop();

        builder.push("grinder");
        grinderBaseDamage = builder
                .comment("Base multiplier for grinder damage. Actual damage is speed * multiplier.")
                .defineInRange("grinderBaseDamage", 0.1, 0.0, 100.0);
        builder.pop();
    }
}
