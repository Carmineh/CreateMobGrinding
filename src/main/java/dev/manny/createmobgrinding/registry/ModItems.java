package dev.manny.createmobgrinding.registry;

import dev.manny.createmobgrinding.CreateMobGrinding;
import net.minecraft.world.item.Item;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModItems {
    public static final DeferredRegister.Items ITEMS = DeferredRegister.createItems(CreateMobGrinding.MOD_ID);

    public static final Supplier<Item> MOB_SPAWNER_CHUNK = ITEMS.register("mob_spawner_chunk", 
        () -> new dev.manny.createmobgrinding.item.MobSpawnerChunkItem(new Item.Properties()));

    public static final Supplier<Item> BLANK_SPAWNER_CHUNK = ITEMS.register("blank_spawner_chunk", 
        () -> new Item(new Item.Properties()));

    public static final Supplier<Item> UNFINISHED_SPAWNER_CHUNK = ITEMS.register("unfinished_spawner_chunk", 
        () -> new dev.manny.createmobgrinding.item.MobSpawnerChunkItem(new Item.Properties()));

    public static final Supplier<Item> BROKEN_SPAWNER_CHUNK = ITEMS.register("broken_spawner_chunk", 
        () -> new Item(new Item.Properties()));
        
    public static final Supplier<Item> ROTATIONAL_MOB_SPAWNER = ITEMS.register("rotational_mob_spawner",
        () -> new net.minecraft.world.item.BlockItem(ModBlocks.ROTATIONAL_MOB_SPAWNER.get(), new Item.Properties()));

    public static final Supplier<Item> ROTATIONAL_MOB_GRINDER = ITEMS.register("rotational_mob_grinder",
        () -> new net.minecraft.world.item.BlockItem(ModBlocks.ROTATIONAL_MOB_GRINDER.get(), new Item.Properties()));

    // Grinder Blades
    public static final Supplier<Item> IRON_GRINDER_BLADE = ITEMS.register("iron_grinder_blade", 
        () -> new dev.manny.createmobgrinding.item.GrinderBladeItem(new Item.Properties().stacksTo(1), 14));
    public static final Supplier<Item> BRASS_GRINDER_BLADE = ITEMS.register("brass_grinder_blade", 
        () -> new dev.manny.createmobgrinding.item.GrinderBladeItem(new Item.Properties().stacksTo(1), 18));
    public static final Supplier<Item> DIAMOND_GRINDER_BLADE = ITEMS.register("diamond_grinder_blade", 
        () -> new dev.manny.createmobgrinding.item.GrinderBladeItem(new Item.Properties().stacksTo(1), 10));
    public static final Supplier<Item> NETHERITE_GRINDER_BLADE = ITEMS.register("netherite_grinder_blade",
        () -> new dev.manny.createmobgrinding.item.GrinderBladeItem(new Item.Properties().stacksTo(1).fireResistant(), 15));

    public static final Supplier<Item> CREATIVE_GRINDER_BLADE = ITEMS.register("creative_grinder_blade",
            () -> new dev.manny.createmobgrinding.item.GrinderBladeItem(new Item.Properties().stacksTo(1).rarity(net.minecraft.world.item.Rarity.EPIC), 50) {
                @Override
                public boolean isFoil(net.minecraft.world.item.ItemStack stack) {
                    return true;
                }
            });

    // Spawner Upgrades
    public static final Supplier<Item> SPAWNER_UPGRADE_NO_AI = ITEMS.register("spawner_upgrade_no_ai", 
            () -> new Item(new Item.Properties().stacksTo(64)));
    public static final Supplier<Item> SPAWNER_UPGRADE_NO_CONDITIONS = ITEMS.register("spawner_upgrade_no_conditions", 
            () -> new Item(new Item.Properties().stacksTo(64)));
            
    // Grinder Upgrades
    public static final Supplier<Item> GRINDER_UPGRADE_PROTECTION = ITEMS.register("grinder_upgrade_protection", 
            () -> new Item(new Item.Properties().stacksTo(64)));
    public static final Supplier<Item> GRINDER_UPGRADE_VACUUM = ITEMS.register("grinder_upgrade_vacuum", 
            () -> new Item(new Item.Properties().stacksTo(64)));

    // Soul Extractors
    public static final Supplier<Item> BASIC_SOUL_EXTRACTOR = ITEMS.register("basic_soul_extractor", 
        () -> new dev.manny.createmobgrinding.item.SoulExtractorItem(new Item.Properties().stacksTo(1), 2));
    public static final Supplier<Item> ADVANCED_SOUL_EXTRACTOR = ITEMS.register("advanced_soul_extractor", 
        () -> new dev.manny.createmobgrinding.item.SoulExtractorItem(new Item.Properties().stacksTo(1), 4));
    public static final Supplier<Item> ELITE_SOUL_EXTRACTOR = ITEMS.register("elite_soul_extractor", 
        () -> new dev.manny.createmobgrinding.item.SoulExtractorItem(new Item.Properties().stacksTo(1), 5));

    public static final Supplier<Item> BASIC_FILLED_SOUL_EXTRACTOR = ITEMS.register("basic_filled_soul_extractor", 
        () -> new dev.manny.createmobgrinding.item.FilledSoulExtractorItem(new Item.Properties().durability(5)));
    public static final Supplier<Item> ADVANCED_FILLED_SOUL_EXTRACTOR = ITEMS.register("advanced_filled_soul_extractor", 
        () -> new dev.manny.createmobgrinding.item.FilledSoulExtractorItem(new Item.Properties().durability(5)));
    public static final Supplier<Item> ELITE_FILLED_SOUL_EXTRACTOR = ITEMS.register("elite_filled_soul_extractor", 
        () -> new dev.manny.createmobgrinding.item.FilledSoulExtractorItem(new Item.Properties().durability(5)));
}

