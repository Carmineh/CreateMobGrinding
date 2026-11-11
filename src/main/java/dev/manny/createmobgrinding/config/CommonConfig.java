package dev.manny.createmobgrinding.config;

import net.neoforged.neoforge.common.ModConfigSpec;

import java.util.List;

/**
 * Keys that existed before 1.5.0 keep their name and section, so players don't lose their values on update.
 */
public class CommonConfig {
    // Spawner
    public final ModConfigSpec.DoubleValue spawnTimeTier1;
    public final ModConfigSpec.DoubleValue spawnTimeTier2;
    public final ModConfigSpec.DoubleValue spawnTimeTier3;
    public final ModConfigSpec.DoubleValue spawnTimeTier4;
    public final ModConfigSpec.DoubleValue spawnTimeTier5;
    public final ModConfigSpec.ConfigValue<List<? extends String>> spawnerBlacklist;
    public final ModConfigSpec.DoubleValue spawnerBaseStress;
    public final ModConfigSpec.DoubleValue stressMultiplierTier1;
    public final ModConfigSpec.DoubleValue stressMultiplierTier2;
    public final ModConfigSpec.DoubleValue stressMultiplierTier3;
    public final ModConfigSpec.DoubleValue stressMultiplierTier4;
    public final ModConfigSpec.DoubleValue stressMultiplierTier5;
    public final ModConfigSpec.IntValue maxNearbyMobs;
    public final ModConfigSpec.IntValue mobCapRadius;
    public final ModConfigSpec.IntValue monsterMaxLight;
    public final ModConfigSpec.IntValue animalMinLight;

    // Chunk stacking
    public final ModConfigSpec.IntValue maxStackedChunks;
    public final ModConfigSpec.DoubleValue speedBonusPerChunk;
    public final ModConfigSpec.DoubleValue stressPerExtraChunk;

    // Grinder
    public final ModConfigSpec.DoubleValue grinderBaseDamage;
    public final ModConfigSpec.DoubleValue attackInterval;
    public final ModConfigSpec.DoubleValue brassDamageMultiplier;
    public final ModConfigSpec.DoubleValue diamondDamageMultiplier;
    public final ModConfigSpec.DoubleValue netheriteDamageMultiplier;
    public final ModConfigSpec.DoubleValue ironBladeStress;
    public final ModConfigSpec.DoubleValue brassBladeStress;
    public final ModConfigSpec.DoubleValue diamondBladeStress;
    public final ModConfigSpec.DoubleValue netheriteBladeStress;
    public final ModConfigSpec.DoubleValue stressPerEnchantmentLevel;

    // Soul Extractor
    public final ModConfigSpec.DoubleValue extractionHealthThreshold;
    public final ModConfigSpec.BooleanValue protectPetsAndNamedMobs;

    // Enchantments
    public final ModConfigSpec.ConfigValue<List<? extends Double>> beheadingChances;

    // Trading
    public final ModConfigSpec.BooleanValue wanderingTraderSellsBrokenChunk;

    public CommonConfig(ModConfigSpec.Builder builder) {
        builder.comment("Rotational Mob Spawner").push("spawner");

        builder.comment("Spawn time: progress points needed for one spawn, per mob tier.",
                "The spawner gains one point per RPM every tick (64 RPM = 1280 points per second).", "Seconds per spawn = points / (RPM * 20).");
        spawnTimeTier1 = builder
                .comment("Tier 1 mobs. Default: 2000.0 (~1.6s at 64 RPM)")
                .defineInRange("spawnTimeTier1", 2000.0, 1.0, Double.MAX_VALUE);
        spawnTimeTier2 = builder
                .comment("Tier 2 mobs. Default: 4000.0 (~3.1s at 64 RPM)")
                .defineInRange("spawnTimeTier2", 4000.0, 1.0, Double.MAX_VALUE);
        spawnTimeTier3 = builder
                .comment("Tier 3 mobs. Default: 6000.0 (~4.7s at 64 RPM)")
                .defineInRange("spawnTimeTier3", 6000.0, 1.0, Double.MAX_VALUE);
        spawnTimeTier4 = builder
                .comment("Tier 4 mobs. Default: 8000.0 (~6.3s at 64 RPM)")
                .defineInRange("spawnTimeTier4", 8000.0, 1.0, Double.MAX_VALUE);
        spawnTimeTier5 = builder
                .comment("Tier 5 mobs. Default: 10000.0 (~7.8s at 64 RPM)")
                .defineInRange("spawnTimeTier5", 10000.0, 1.0, Double.MAX_VALUE);

        spawnerBlacklist = builder
                .comment("Entity types that can't be spawned or extracted (e.g. minecraft:warden).")
                .defineList("spawnerBlacklist", List.of("minecraft:warden", "minecraft:ender_dragon"), obj -> obj instanceof String);

        spawnerBaseStress = builder
                .comment("Stress impact (SU per RPM) of a spawner with a Tier 1 mob. Higher tiers multiply it. Default: 32.0")
                .defineInRange("baseStress", 32.0, 0.0, 10000.0);
        stressMultiplierTier1 = builder.comment("Stress multiplier for Tier 1 mobs. Default: 1.0")
                .defineInRange("stressMultiplierTier1", 1.0, 0.0, 1000.0);
        stressMultiplierTier2 = builder.comment("Stress multiplier for Tier 2 mobs. Default: 1.5")
                .defineInRange("stressMultiplierTier2", 1.5, 0.0, 1000.0);
        stressMultiplierTier3 = builder.comment("Stress multiplier for Tier 3 mobs. Default: 2.0")
                .defineInRange("stressMultiplierTier3", 2.0, 0.0, 1000.0);
        stressMultiplierTier4 = builder.comment("Stress multiplier for Tier 4 mobs. Default: 6.0")
                .defineInRange("stressMultiplierTier4", 6.0, 0.0, 1000.0);
        stressMultiplierTier5 = builder.comment("Stress multiplier for Tier 5 mobs. Default: 12.0")
                .defineInRange("stressMultiplierTier5", 12.0, 0.0, 1000.0);

        maxNearbyMobs = builder
                .comment("The spawner pauses while this many mobs of its type are around it. Default: 16")
                .defineInRange("maxNearbyMobs", 16, 1, 256);
        mobCapRadius = builder
                .comment("Radius (in blocks) checked for maxNearbyMobs. Default: 4")
                .defineInRange("mobCapRadius", 4, 1, 32);
        monsterMaxLight = builder
                .comment("Monsters only spawn at this light level or lower (ignored with the No Conditions upgrade). Default: 7")
                .defineInRange("monsterMaxLight", 7, 0, 15);
        animalMinLight = builder
                .comment("Animals only spawn at this light level or higher (ignored with the No Conditions upgrade). Default: 9")
                .defineInRange("animalMinLight", 9, 0, 15);

        builder.comment("Stacking chunks of the same mob in one spawner: each chunk spawns one mob per cycle,",
                        "so a spawner with 8 chunks works like 8 spawners in a single block.")
                .push("chunkStacking");
        maxStackedChunks = builder
                .comment("How many chunks of the same mob a spawner can hold. 1 disables stacking. Default: 8")
                .defineInRange("maxStackedChunks", 8, 1, 64);
        speedBonusPerChunk = builder
                .comment("Extra spawn speed for each chunk after the first (0.15 = +15%), on top of the extra mobs. Default: 0.0")
                .defineInRange("speedBonusPerChunk", 0.0, 0.0, 10.0);
        stressPerExtraChunk = builder
                .comment("Stress added by each chunk after the first, as a fraction of the spawner's stress (0.75 = +75%).",
                        "Below 1.0 a stacked spawner needs less stress than separate spawners. Default: 0.75 (8 chunks = x6.25)")
                .defineInRange("stressPerExtraChunk", 0.75, 0.0, 10.0);
        builder.pop();

        builder.pop();

        builder.comment("Rotational Mob Grinder").push("grinder");
        grinderBaseDamage = builder
                .comment("Damage dealt per hit by an Iron Blade. Other blades multiply it. Default: 2.0")
                .defineInRange("bladeBaseDamage", 2.0, 0.0, 1000.0);
        attackInterval = builder
                .comment("Progress points between two hits; the grinder gains one point per RPM each tick.",
                        "Default: 2000.0 (one hit every ~0.4s at 256 RPM)")
                .defineInRange("attackInterval", 2000.0, 1.0, Double.MAX_VALUE);
        brassDamageMultiplier = builder.comment("Damage multiplier of the Brass Blade. Default: 2.0")
                .defineInRange("brassDamageMultiplier", 2.0, 0.0, 1000.0);
        diamondDamageMultiplier = builder.comment("Damage multiplier of the Diamond Blade. Default: 4.0")
                .defineInRange("diamondDamageMultiplier", 4.0, 0.0, 1000.0);
        netheriteDamageMultiplier = builder.comment("Damage multiplier of the Netherite Blade. Default: 8.0")
                .defineInRange("netheriteDamageMultiplier", 8.0, 0.0, 1000.0);
        ironBladeStress = builder.comment("Stress impact with the Iron Blade. Default: 8.0")
                .defineInRange("ironBladeStress", 8.0, 0.0, 10000.0);
        brassBladeStress = builder.comment("Stress impact with the Brass Blade. Default: 16.0")
                .defineInRange("brassBladeStress", 16.0, 0.0, 10000.0);
        diamondBladeStress = builder.comment("Stress impact with the Diamond Blade. Default: 32.0")
                .defineInRange("diamondBladeStress", 32.0, 0.0, 10000.0);
        netheriteBladeStress = builder.comment("Stress impact with the Netherite Blade. Default: 64.0")
                .defineInRange("netheriteBladeStress", 64.0, 0.0, 10000.0);
        stressPerEnchantmentLevel = builder
                .comment("Stress added for each enchantment level on the blade. Default: 4.0")
                .defineInRange("stressPerEnchantmentLevel", 4.0, 0.0, 1000.0);
        builder.pop();

        builder.comment("Soul Extractors").push("soulExtractor");
        extractionHealthThreshold = builder
                .comment("A mob's soul can be extracted only at or below this fraction of its max health (0.1 = 10%). Default: 0.1")
                .defineInRange("extractionHealthThreshold", 0.1, 0.0, 1.0);
        protectPetsAndNamedMobs = builder
                .comment("Prevent extracting the soul of tamed or name-tagged mobs. Default: true")
                .define("protectPetsAndNamedMobs", true);
        builder.pop();

        builder.comment("Enchantments").push("enchantments");
        beheadingChances = builder
                .comment("Head drop chance for each Beheading level (0.1 = 10%). Default: [0.1, 0.25, 0.4]")
                .defineList("beheadingChances", List.of(0.1, 0.25, 0.4), obj -> obj instanceof Double d && d >= 0 && d <= 1);
        builder.pop();

        builder.comment("Trading").push("trading");
        wanderingTraderSellsBrokenChunk = builder
                .comment("The Wandering Trader can offer a Scrap Spawner Chunk for 32 Emeralds (rare offer, single use).",
                        "Changes apply after restarting the world. Default: true")
                .define("wanderingTraderSellsBrokenChunk", true);
        builder.pop();
    }

    public double getSpawnTime(int tier) {
        return switch (tier) {
            case 5 -> spawnTimeTier5.get();
            case 4 -> spawnTimeTier4.get();
            case 3 -> spawnTimeTier3.get();
            case 2 -> spawnTimeTier2.get();
            default -> spawnTimeTier1.get();
        };
    }

    public double getStressMultiplier(int tier) {
        return switch (tier) {
            case 5 -> stressMultiplierTier5.get();
            case 4 -> stressMultiplierTier4.get();
            case 3 -> stressMultiplierTier3.get();
            case 2 -> stressMultiplierTier2.get();
            default -> stressMultiplierTier1.get();
        };
    }

    public boolean isBlacklisted(net.minecraft.resources.ResourceLocation entity) {
        return entity != null && spawnerBlacklist.get().contains(entity.toString());
    }
}
