package dev.manny.createmobgrinding.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

public class CommonConfig {
    public final ModConfigSpec.DoubleValue spawnTimeTier1;
    public final ModConfigSpec.DoubleValue spawnTimeTier2;
    public final ModConfigSpec.DoubleValue spawnTimeTier3;
    public final ModConfigSpec.DoubleValue spawnTimeTier4;
    public final ModConfigSpec.DoubleValue spawnTimeTier5;
    public final ModConfigSpec.DoubleValue grinderBaseDamage;
    public final ModConfigSpec.ConfigValue<List<? extends String>> spawnerBlacklist;

    public CommonConfig(ModConfigSpec.Builder builder) {
        builder.push("spawner");
        spawnTimeTier1 = builder
                .comment("Progress points required for a Tier 1 mob spawn.", "Example: 1000.0 means at 64 RPM it takes ~15.6 seconds.", "Default: 2000.0 (~31s at 64 RPM)")
                .defineInRange("spawnTimeTier1", 2000.0, 1.0, Double.MAX_VALUE);
                
        spawnTimeTier2 = builder
                .comment("Progress points required for a Tier 2 mob spawn.", "Example: 4000.0 means at 64 RPM it takes ~62.5 seconds.", "Default: 4000.0 (~62s at 64 RPM)")
                .defineInRange("spawnTimeTier2", 4000.0, 1.0, Double.MAX_VALUE);
                
        spawnTimeTier3 = builder
                .comment("Progress points required for a Tier 3 mob spawn.", "Example: 6000.0 means at 64 RPM it takes ~93.7 seconds.", "Default: 6000.0 (~93s at 64 RPM)")
                .defineInRange("spawnTimeTier3", 6000.0, 1.0, Double.MAX_VALUE);
                
        spawnTimeTier4 = builder
                .comment("Progress points required for a Tier 4 mob spawn.", "Example: 8000.0 means at 64 RPM it takes ~125.0 seconds.", "Default: 8000.0 (~125s at 64 RPM)")
                .defineInRange("spawnTimeTier4", 8000.0, 1.0, Double.MAX_VALUE);
                
        spawnTimeTier5 = builder
                .comment("Progress points required for a Tier 5 mob spawn.", "Example: 10000.0 means at 64 RPM it takes ~156.2 seconds.", "Default: 10000.0 (~156s at 64 RPM)")
                .defineInRange("spawnTimeTier5", 10000.0, 1.0, Double.MAX_VALUE);
                
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
