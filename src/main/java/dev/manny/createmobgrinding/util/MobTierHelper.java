package dev.manny.createmobgrinding.util;

import dev.manny.createmobgrinding.config.ModConfigs;
import dev.manny.createmobgrinding.registry.ModTags;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;

import java.util.List;

public class MobTierHelper {
    public static int getMobTier(EntityType<?> type) {
        if (type.is(ModTags.TIER_5)) return 5;
        if (type.is(ModTags.TIER_4)) return 4;
        if (type.is(ModTags.TIER_3)) return 3;
        if (type.is(ModTags.TIER_2)) return 2;
        if (type.is(ModTags.TIER_1)) return 1;
        
        return 1;
    }
    
    public static boolean hasExplicitTier(EntityType<?> type) {
        return type.is(ModTags.TIER_5) || 
               type.is(ModTags.TIER_4) || 
               type.is(ModTags.TIER_3) || 
               type.is(ModTags.TIER_2) || 
               type.is(ModTags.TIER_1);
    }
}
